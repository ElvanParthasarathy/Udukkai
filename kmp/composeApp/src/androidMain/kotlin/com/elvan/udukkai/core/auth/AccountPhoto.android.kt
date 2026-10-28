package com.elvan.udukkai.core.auth

import com.elvan.udukkai.core.platform.AppContext
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

private fun photoFile(): File = File(AppContext.context.cacheDir, "account_photo.jpg")

actual fun platformCachedPhotoBytes(): ByteArray? = try {
    photoFile().takeIf { it.exists() }?.readBytes()
} catch (_: Exception) {
    null
}

actual suspend fun platformProfilePhotoBytes(): ByteArray? = withContext(Dispatchers.IO) {
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
        platformCachedPhotoBytes()
    }
}
