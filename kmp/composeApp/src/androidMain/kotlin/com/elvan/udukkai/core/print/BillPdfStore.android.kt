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

/** Documents/Udukkai Textiles|Udukkai Factory/<Invoice|Receipt>, one folder per app. */
private fun billFolder(fileName: String): String {
    val app = if (BuildConfig.FLAVOR == "factory") "Udukkai Factory" else "Udukkai Textiles"
    val category = if (fileName.startsWith("RCPT_")) "Receipt" else "Invoice"
    return "${Environment.DIRECTORY_DOCUMENTS}/$app/$category"
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

actual fun storeBillPdf(pdfPath: String, fileName: String): String? {
    // Public Documents without a storage permission needs MediaStore (Android 10+)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
    return try {
        val resolver = AppContext.context.contentResolver
        val folder = billFolder(fileName)
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
        "$folder/$name"
    } catch (e: Exception) {
        Log.e(TAG, "Storing $fileName failed: ${e.message}", e)
        null
    }
}

actual fun deleteBillPdf(fileName: String) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
    try {
        findBillPdf(billFolder(fileName), "$fileName.pdf")?.let {
            AppContext.context.contentResolver.delete(it, null, null)
        }
    } catch (e: Exception) {
        Log.e(TAG, "Removing $fileName failed: ${e.message}", e)
    }
}

actual fun billPdfExists(fileName: String): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true
    return try {
        findBillPdf(billFolder(fileName), "$fileName.pdf") != null
    } catch (e: Exception) {
        true
    }
}
