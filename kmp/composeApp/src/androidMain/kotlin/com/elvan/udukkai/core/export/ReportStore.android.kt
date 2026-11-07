package com.elvan.udukkai.core.export

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.elvan.udukkai.BuildConfig
import com.elvan.udukkai.core.platform.AppContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual fun saveReport(baseName: String, extension: String, mimeType: String, bytes: ByteArray, businessFolder: String): SavedReport? {
    // Public Documents without a storage permission needs MediaStore (Android 10+), as for the bill PDFs
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
    return try {
        val app = if (BuildConfig.FLAVOR == "factory") "Udukkai Factory" else "Udukkai Textiles"
        val folder = "${Environment.DIRECTORY_DOCUMENTS}/Udukkai/$app/$businessFolder/Reports"
        val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val name = "${baseName}_$stamp.$extension"
        val resolver = AppContext.context.contentResolver
        val uri = resolver.insert(
            MediaStore.Files.getContentUri("external"),
            ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, "$folder/")
            }
        ) ?: return null
        resolver.openOutputStream(uri)?.use { it.write(bytes) } ?: return null
        SavedReport(uri.toString(), name, folder, mimeType)
    } catch (e: Exception) {
        Log.e("ReportStore", "Saving $baseName failed: ${e.message}", e)
        null
    }
}

actual fun openSavedReport(report: SavedReport): Boolean = try {
    val intent = Intent(Intent.ACTION_VIEW)
        .setDataAndType(Uri.parse(report.uri), report.mimeType)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    AppContext.context.startActivity(intent)
    true
} catch (_: Exception) {
    false
}
