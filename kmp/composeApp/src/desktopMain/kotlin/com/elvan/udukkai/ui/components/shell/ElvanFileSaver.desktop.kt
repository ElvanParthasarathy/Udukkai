package com.elvan.udukkai.ui.components.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberFileSaver(onDone: (Boolean) -> Unit): FileSaver = remember {
    object : FileSaver {
        override fun save(fileName: String, mimeType: String, bytes: ByteArray) {
            // Desktop: written to the user's Downloads folder (the desktop app is not shipped yet)
            val ok = try {
                val dir = java.io.File(System.getProperty("user.home"), "Downloads").apply { mkdirs() }
                java.io.File(dir, fileName).writeBytes(bytes)
                true
            } catch (_: Exception) { false }
            onDone(ok)
        }
    }
}
