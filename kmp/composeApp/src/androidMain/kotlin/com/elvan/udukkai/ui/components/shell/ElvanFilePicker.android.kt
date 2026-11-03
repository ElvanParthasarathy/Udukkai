package com.elvan.udukkai.ui.components.shell

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
actual fun rememberBackupFilePicker(onPicked: (String) -> Unit): FilePickerLauncher {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            // The chooser gives an address, not a file: copy it where the app can read it
            val copy = withContext(Dispatchers.IO) {
                try {
                    val target = File(context.cacheDir, "picked_backup_${System.currentTimeMillis()}.bak")
                    context.contentResolver.openInputStream(uri)?.use { input -> target.outputStream().use { input.copyTo(it) } }
                    target.takeIf { it.length() > 0 }
                } catch (_: Exception) {
                    null
                }
            }
            if (copy != null) onPicked(copy.absolutePath)
        }
    }
    return remember(launcher) {
        object : FilePickerLauncher {
            override fun launch() = launcher.launch(arrayOf("*/*"))
        }
    }
}
