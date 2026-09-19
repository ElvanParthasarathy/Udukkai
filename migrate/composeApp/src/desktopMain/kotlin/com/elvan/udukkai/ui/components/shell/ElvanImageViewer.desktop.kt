package com.elvan.udukkai.ui.components.shell

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import org.jetbrains.skia.Image as SkiaImage
import java.io.File
import java.util.Base64

@Composable
actual fun ElvanImageViewer(
    value: String?,
    modifier: Modifier,
    contentScale: ContentScale
) {
    if (value.isNullOrBlank()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Icon(
                imageVector = MaterialSymbols.Rounded.BrokenImage,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
        return
    }

    val imageBitmap = remember(value) {
        try {
            val bytes = when {
                value.contains("/") || value.contains("\\") || value.startsWith("file:") -> {
                    val cleanPath = value.removePrefix("file://").removePrefix("file:")
                    val file = File(cleanPath)
                    if (file.exists()) file.readBytes() else null
                }
                else -> {
                    val cleanBase64 = if (value.contains(",")) value.substringAfter(",") else value
                    Base64.getDecoder().decode(cleanBase64)
                }
            }
            bytes?.let { SkiaImage.makeFromEncoded(it).toComposeImageBitmap() }
        } catch (_: Exception) {
            null
        }
    }

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap,
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
