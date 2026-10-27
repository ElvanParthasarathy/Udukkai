package com.elvan.udukkai.ui.components.shell

import java.io.File
import org.jetbrains.compose.resources.decodeToSvgPainter

actual fun loadElvanImageBytes(value: String): ByteArray? = try {
    val file = File(value.removePrefix("file://").removePrefix("file:"))
    if (file.isFile) file.readBytes() else null
} catch (_: Exception) {
    null
}

@OptIn(org.jetbrains.compose.resources.ExperimentalResourceApi::class)
actual fun decodeSvgPainter(bytes: ByteArray, density: androidx.compose.ui.unit.Density): androidx.compose.ui.graphics.painter.Painter? =
    try {
        bytes.decodeToSvgPainter(density)
    } catch (_: Exception) {
        null
    }
