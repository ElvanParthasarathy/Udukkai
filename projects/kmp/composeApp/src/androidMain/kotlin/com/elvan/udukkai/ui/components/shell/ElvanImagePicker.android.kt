package com.elvan.udukkai.ui.components.shell

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.io.File

private fun persistPickedImage(context: Context, uri: Uri): String? {
    return try {
        val dir = File(context.filesDir, "oavuru")
        if (!dir.exists()) dir.mkdirs()
        val mime = context.contentResolver.getType(uri)
        val ext = when {
            mime?.contains("png", ignoreCase = true) == true -> "png"
            mime?.contains("webp", ignoreCase = true) == true -> "webp"
            mime?.contains("svg", ignoreCase = true) == true -> "svg"
            else -> "jpg"
        }
        val targetFile = File(dir, "img_${System.currentTimeMillis()}.$ext")
        context.contentResolver.openInputStream(uri)?.use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        targetFile.absolutePath
    } catch (_: Exception) {
        null
    }
}

@Composable
actual fun rememberImagePicker(onImagePicked: (String) -> Unit): ImagePickerLauncher {
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val persistentPath = persistPickedImage(context, uri)
            if (persistentPath != null) {
                onImagePicked(persistentPath)
            }
        }
    }

    val filesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val persistentPath = persistPickedImage(context, uri)
            if (persistentPath != null) {
                onImagePicked(persistentPath)
            }
        }
    }

    return remember(galleryLauncher, filesLauncher) {
        object : ImagePickerLauncher {
            override fun launch() {
                filesLauncher.launch("image/*")
            }

            override fun launchGallery() {
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }

            override fun launchFiles() {
                filesLauncher.launch("image/*")
            }
        }
    }
}
