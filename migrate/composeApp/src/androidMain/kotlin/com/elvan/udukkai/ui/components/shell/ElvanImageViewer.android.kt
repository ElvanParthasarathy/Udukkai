package com.elvan.udukkai.ui.components.shell

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import java.io.File

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

    val context = LocalContext.current
    val imageBitmap = remember(value) {
        try {
            when {
                value.startsWith("content://") || value.startsWith("file://") -> {
                    val uri = Uri.parse(value)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)?.asImageBitmap()
                    }
                }
                value.contains("/") || value.contains("\\") -> {
                    val file = File(value)
                    if (file.exists()) {
                        BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                    } else null
                }
                else -> {
                    val cleanBase64 = if (value.contains(",")) value.substringAfter(",") else value
                    val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)?.asImageBitmap()
                }
            }
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
