package com.elvan.udukkai.core.auth

import com.elvan.udukkai.core.platform.AppContext
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

// Kept in the app's own files (the cache folder is emptied by the system and by "clear cache")
private fun photoFile(): File = File(AppContext.context.filesDir, "account_photo.jpg")

/** A picture saved less than a day ago is used as it is: no download on every visit to the settings. */
private const val FRESH_MS = 24L * 60 * 60 * 1000

actual fun platformCachedPhotoBytes(): ByteArray? = try {
    photoFile().takeIf { it.exists() }?.readBytes()
} catch (_: Exception) {
    null
}

actual suspend fun platformProfilePhotoBytes(): ByteArray? = withContext(Dispatchers.IO) {
    photoFile().takeIf { it.exists() && System.currentTimeMillis() - it.lastModified() < FRESH_MS }?.let { return@withContext platformCachedPhotoBytes() }
    val url = try {
        FirebaseAuth.getInstance().currentUser?.photoUrl?.toString()
    } catch (_: Exception) {
        null
    } ?: run {
        android.util.Log.w("AccountPhoto", "The signed-in account has no photo address")
        return@withContext platformCachedPhotoBytes()
    }
    try {
        // Google serves small pictures by default; ask for a sharper one
        val sharp = url.replace(Regex("=s\\d+-c$"), "=s256-c")
        val connection = URL(sharp).openConnection() as HttpURLConnection
        connection.connectTimeout = 8000
        connection.readTimeout = 8000
        val bytes = connection.inputStream.use { it.readBytes() }
        if (bytes.isNotEmpty()) photoFile().writeBytes(bytes)
        bytes
    } catch (e: Exception) {
        android.util.Log.w("AccountPhoto", "Photo download failed: ${e.message}")
        // Try again tomorrow, not on every visit: the saved picture (if any) counts as checked
        photoFile().takeIf { it.exists() }?.setLastModified(System.currentTimeMillis())
        platformCachedPhotoBytes()
    }
}
