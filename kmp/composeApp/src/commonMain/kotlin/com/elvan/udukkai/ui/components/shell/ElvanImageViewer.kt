package com.elvan.udukkai.ui.components.shell

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Reads an image stored as a file path or content/file URI (platform specific).
 * Returns null when [value] is not something the platform can open.
 */
expect fun loadElvanImageBytes(value: String): ByteArray?

/** Draws SVG bytes (platform specific: Skia on desktop, AndroidSVG on Android). */
expect fun decodeSvgPainter(bytes: ByteArray, density: Density): Painter?

/**
 * ElvanImageViewer — Replicates Flutter's `ElvanImageViewer` (`elvan_oavuru_kaatchi.dart`).
 * Renders an image from a data URL (`data:image/...;base64,...`, PNG/JPEG/WebP/SVG),
 * a raw base64 string, a file path or a content URI.
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun ElvanImageViewer(
    value: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    val density = LocalDensity.current
    val painter: Painter? = remember(value, density) {
        if (value.isNullOrBlank()) return@remember null
        try {
            val bytes = imageBytes(value) ?: return@remember null
            if (isSvg(value, bytes)) decodeSvgPainter(bytes, density)
            else BitmapPainter(bytes.decodeToImageBitmap())
        } catch (_: Throwable) {
            null
        }
    }

    if (painter != null) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Icon(
                imageVector = MaterialSymbols.Rounded.BrokenImage,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private val base64Pattern = Regex("^[A-Za-z0-9+/=\\s]+$")

// Data URLs are checked first: base64 text contains '/', so it must never be
// mistaken for a file path.
@OptIn(ExperimentalEncodingApi::class)
private fun imageBytes(value: String): ByteArray? = when {
    value.startsWith("data:") -> decodeBase64(value.substringAfter(','))
    value.startsWith("content://") || value.startsWith("file:") -> loadElvanImageBytes(value)
    else -> loadElvanImageBytes(value)
        ?: if (value.length > 64 && base64Pattern.matches(value)) decodeBase64(value) else null
}

@OptIn(ExperimentalEncodingApi::class)
private fun decodeBase64(data: String): ByteArray =
    Base64.Default.decode(data.filterNot { it.isWhitespace() })

private fun isSvg(value: String, bytes: ByteArray): Boolean {
    if (value.startsWith("data:image/svg") || value.endsWith(".svg", ignoreCase = true)) return true
    val head = bytes.copyOfRange(0, minOf(bytes.size, 256)).decodeToString().trimStart()
    return head.startsWith("<svg") || (head.startsWith("<?xml") && head.contains("<svg"))
}
