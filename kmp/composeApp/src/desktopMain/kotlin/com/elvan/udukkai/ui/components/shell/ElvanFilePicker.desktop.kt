package com.elvan.udukkai.ui.components.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
actual fun rememberBackupFilePicker(onPicked: (String) -> Unit): FilePickerLauncher = remember {
    object : FilePickerLauncher {
        override fun launch() {
            val dialog = FileDialog(null as Frame?, "Select backup file", FileDialog.LOAD)
            dialog.isVisible = true
            val dir = dialog.directory
            val name = dialog.file
            if (dir != null && name != null) {
                val file = File(dir, name)
                if (file.isFile) onPicked(file.absolutePath)
            }
        }
    }
}
