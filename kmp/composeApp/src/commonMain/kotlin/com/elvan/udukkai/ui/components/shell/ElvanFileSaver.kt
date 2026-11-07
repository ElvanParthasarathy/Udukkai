package com.elvan.udukkai.ui.components.shell

import androidx.compose.runtime.Composable

/** Saves bytes to a file the user chooses. Nothing is written until the user taps Save in the system's own save dialog. */
interface FileSaver {
    fun save(fileName: String, mimeType: String, bytes: ByteArray)
}

/** [onDone] gets true when the file was written, false when it failed (cancelling the dialog is not reported). */
@Composable
expect fun rememberFileSaver(onDone: (Boolean) -> Unit): FileSaver
