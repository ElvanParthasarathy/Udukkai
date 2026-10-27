package com.elvan.udukkai.ui.components.shell

import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import com.caverock.androidsvg.SVG
import com.elvan.udukkai.core.platform.AppContext
import java.io.File
import kotlin.math.roundToInt

actual fun loadElvanImageBytes(value: String): ByteArray? = try {
    if (value.startsWith("content://") || value.startsWith("file://")) {
        AppContext.context.contentResolver.openInputStream(Uri.parse(value))?.use { it.readBytes() }
    } else {
        val file = File(value)
        if (file.isFile) file.readBytes() else null
    }
} catch (_: Exception) {
    null
}

// Rasterises the SVG at 3x its intrinsic size so it stays sharp when scaled to fit.
actual fun decodeSvgPainter(bytes: ByteArray, density: Density): Painter? = try {
    val svg = SVG.getFromString(bytes.decodeToString())
    val box = svg.documentViewBox
    val baseW = if (svg.documentWidth > 0) svg.documentWidth else box?.width() ?: 512f
    val baseH = if (svg.documentHeight > 0) svg.documentHeight else box?.height() ?: 512f
    val scale = 3f
    val w = (baseW * scale).roundToInt().coerceIn(1, 4096)
    val h = (baseH * scale).roundToInt().coerceIn(1, 4096)
    svg.documentWidth = w.toFloat()
    svg.documentHeight = h.toFloat()
    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    svg.renderToCanvas(Canvas(bitmap))
    BitmapPainter(bitmap.asImageBitmap())
} catch (_: Exception) {
    null
}
