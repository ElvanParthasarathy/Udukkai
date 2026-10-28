package com.elvan.udukkai.core.print

import android.content.ContentUris
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.elvan.udukkai.BuildConfig
import com.elvan.udukkai.core.platform.AppContext
import java.io.File

private const val TAG = "BillPdfStore"

/** Documents/Udukkai/<Udukkai Textiles|Udukkai Factory>/<business>/<Invoice|Receipt>. */
private fun billFolder(fileName: String, business: String): String {
    val app = if (BuildConfig.FLAVOR == "factory") "Udukkai Factory" else "Udukkai Textiles"
    val category = if (fileName.startsWith("RCPT_")) "Receipt" else "Invoice"
    return "${Environment.DIRECTORY_DOCUMENTS}/Udukkai/$app/$business/$category"
}

/** The PDF this app stored earlier under that folder and name, if any. */
private fun findBillPdf(folder: String, name: String): Uri? {
    val collection = MediaStore.Files.getContentUri("external")
    AppContext.context.contentResolver.query(
        collection,
        arrayOf(MediaStore.MediaColumns._ID),
        "${MediaStore.MediaColumns.RELATIVE_PATH}=? AND ${MediaStore.MediaColumns.DISPLAY_NAME}=?",
        arrayOf("$folder/", name),
        null
    )?.use { cursor ->
        if (cursor.moveToFirst()) return ContentUris.withAppendedId(collection, cursor.getLong(0))
    }
    return null
}

/**
 * Next to each stored PDF the app remembers which document it was made from (the cache key of the PDF),
 * so a stored file is only reused when it still matches the bill; the index lives in app storage.
 */
private fun keyFile(fileName: String, business: String): File {
    val dir = File(AppContext.context.filesDir, "bill_pdf_keys").apply { mkdirs() }
    return File(dir, "$business|$fileName".replace(Regex("[\\/:*?\"<>]"), "-") + ".key")
}

/**
 * Copies the stored PDF of this bill into [dest] when it was made from the document with [key];
 * false when it is missing (cleared, deleted) or out of date, and the PDF has to be made again.
 */
internal fun readStoredBillPdf(fileName: String, business: String, key: String, dest: File): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
    return try {
        val index = keyFile(fileName, business)
        if (!index.exists() || index.readText().trim() != key) return false
        val uri = findBillPdf(billFolder(fileName, business), "$fileName.pdf") ?: return false
        val tmp = File(dest.parentFile, "${dest.name}.tmp")
        AppContext.context.contentResolver.openInputStream(uri)?.use { input ->
            tmp.outputStream().use { input.copyTo(it) }
        } ?: return false
        if (tmp.length() <= 0L) { tmp.delete(); return false }
        dest.delete()
        tmp.renameTo(dest)
    } catch (e: Exception) {
        Log.e(TAG, "Reading stored $fileName failed: ${e.message}", e)
        false
    }
}

actual fun storeBillPdf(pdfPath: String, fileName: String, business: String): String? {
    // Public Documents without a storage permission needs MediaStore (Android 10+)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
    return try {
        val resolver = AppContext.context.contentResolver
        val folder = billFolder(fileName, business)
        val name = "$fileName.pdf"
        val uri = findBillPdf(folder, name) ?: resolver.insert(
            MediaStore.Files.getContentUri("external"),
            ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, folder)
            }
        ) ?: return null
        // "wt" truncates, so a shorter new PDF never keeps the old file's tail
        resolver.openOutputStream(uri, "wt")?.use { out ->
            File(pdfPath).inputStream().use { it.copyTo(out) }
        } ?: return null
        // The PDF's name in the print cache is its key: remember which document this stored file shows
        val source = File(pdfPath)
        if (source.parentFile?.name == "print_pdfs") {
            try { keyFile(fileName, business).writeText(source.nameWithoutExtension) } catch (_: Exception) {}
        }
        "$folder/$name"
    } catch (e: Exception) {
        Log.e(TAG, "Storing $fileName failed: ${e.message}", e)
        null
    }
}

actual fun deleteBillPdf(fileName: String, business: String) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
    try {
        keyFile(fileName, business).delete()
        findBillPdf(billFolder(fileName, business), "$fileName.pdf")?.let {
            AppContext.context.contentResolver.delete(it, null, null)
        }
    } catch (e: Exception) {
        Log.e(TAG, "Removing $fileName failed: ${e.message}", e)
    }
}

actual fun billPdfExists(fileName: String, business: String): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true
    return try {
        // A file without a remembered document key is made again once, so it can be trusted from then on
        keyFile(fileName, business).exists() && findBillPdf(billFolder(fileName, business), "$fileName.pdf") != null
    } catch (e: Exception) {
        true
    }
}

actual fun removeOldBillFolders() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
    val resolver = AppContext.context.contentResolver
    val collection = MediaStore.Files.getContentUri("external")
    for (app in listOf("Udukkai Textiles", "Udukkai Factory")) {
        val ids = mutableListOf<Long>()
        try {
            resolver.query(
                collection,
                arrayOf(MediaStore.MediaColumns._ID),
                "${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ? AND ${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ?",
                arrayOf("${Environment.DIRECTORY_DOCUMENTS}/$app/%", "%.pdf"),
                null
            )?.use { cursor -> while (cursor.moveToNext()) ids += cursor.getLong(0) }
        } catch (e: Exception) {
            Log.e(TAG, "Listing old bill folders failed: ${e.message}", e)
        }
        // Only the files this app made can be removed; the other app clears its own
        ids.forEach { id ->
            try { resolver.delete(ContentUris.withAppendedId(collection, id), null, null) } catch (_: Exception) {}
        }
    }
}
