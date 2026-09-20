package com.elvan.udukkai.ui.components.shell

import androidx.compose.runtime.Composable

/**
 * Platform image picker launcher supporting Gallery and Files.
 */
interface ImagePickerLauncher {
    fun launch()
    fun launchGallery() = launch()
    fun launchFiles() = launch()
}

@Composable
expect fun rememberImagePicker(onImagePicked: (String) -> Unit): ImagePickerLauncher
