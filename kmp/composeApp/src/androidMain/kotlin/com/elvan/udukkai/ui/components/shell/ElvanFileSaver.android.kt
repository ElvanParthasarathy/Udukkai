package com.elvan.udukkai.ui.components.shell

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext

/** What is waiting for the user to choose where to save it (kept in one object so recomposition cannot reset it). */
private class PendingSave {
    var bytes: ByteArray? = null
}

/** The system "save as" dialog (Storage Access Framework): the user picks the folder and name, the app needs no storage permission. */
@Composable
actual fun rememberFileSaver(onDone: (Boolean) -> Unit): FileSaver {
    val context = LocalContext.current
    val done = rememberUpdatedState(onDone)
    val pending = remember { PendingSave() }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri ->
        val data = pending.bytes
        pending.bytes = null
        if (uri == null || data == null) return@rememberLauncherForActivityResult
        val ok = try {
            context.contentResolver.openOutputStream(uri)?.use { it.write(data) } != null
        } catch (_: Exception) {
            false
        }
        done.value(ok)
    }
    return remember(launcher) {
        object : FileSaver {
            override fun save(fileName: String, mimeType: String, bytes: ByteArray) {
                pending.bytes = bytes
                launcher.launch(fileName)
            }
        }
    }
}
