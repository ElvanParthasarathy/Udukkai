package com.elvan.udukkai.ui.components.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
actual fun rememberImagePicker(onImagePicked: (String) -> Unit): ImagePickerLauncher {
    return remember {
        object : ImagePickerLauncher {
            private fun openChooser() {
                val dialog = FileDialog(null as Frame?, "Select Image", FileDialog.LOAD)
                dialog.setFilenameFilter { _, name ->
                    val lower = name.lowercase()
                    lower.endsWith(".png") || lower.endsWith(".jpg") ||
                            lower.endsWith(".jpeg") || lower.endsWith(".webp") ||
                            lower.endsWith(".svg")
                }
                dialog.isVisible = true
                if (dialog.directory != null && dialog.file != null) {
                    val file = File(dialog.directory, dialog.file)
                    if (file.exists()) {
                        onImagePicked(file.absolutePath)
                    }
                }
            }

            override fun launch() {
                openChooser()
            }

            override fun launchGallery() {
                openChooser()
            }

            override fun launchFiles() {
                openChooser()
            }
        }
    }
}
