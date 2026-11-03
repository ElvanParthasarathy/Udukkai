package com.elvan.udukkai.ui.components.shell

import androidx.compose.runtime.Composable

/** Opens the system file chooser; the picked file is handed over as a path the app can read. */
interface FilePickerLauncher {
    fun launch()
}

/** Chooser for a backup file (any file type: the backup is checked, not its name). */
@Composable
expect fun rememberBackupFilePicker(onPicked: (String) -> Unit): FilePickerLauncher
