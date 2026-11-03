package com.elvan.udukkai.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.ui.components.shell.ElvanActionSheet
import com.elvan.udukkai.ui.components.shell.ElvanSettingsTextField
import com.elvan.udukkai.ui.screens.create.components.ElvanPillShifter
import com.elvan.udukkai.ui.screens.create.components.PillShifterItem
import kotlin.math.abs

/** "#rrggbb" of a colour. */
fun billColorHex(c: Color): String {
    fun two(v: Float) = (v * 255f + 0.5f).toInt().coerceIn(0, 255).toString(16).padStart(2, '0')
    return "#${two(c.red)}${two(c.green)}${two(c.blue)}"
}

/** hue 0..360, saturation 0..1, value 0..1 */
private fun toHsv(c: Color): Triple<Float, Float, Float> {
    val max = maxOf(c.red, c.green, c.blue)
    val min = minOf(c.red, c.green, c.blue)
    val d = max - min
    val h = when {
        d == 0f -> 0f
        max == c.red -> 60f * (((c.green - c.blue) / d + 6f) % 6f)
        max == c.green -> 60f * ((c.blue - c.red) / d + 2f)
        else -> 60f * ((c.red - c.green) / d + 4f)
    }
    return Triple(h, if (max == 0f) 0f else d / max, max)
}

private fun hslColor(h: Float, s: Float, l: Float): Color {
    val c = (1f - abs(2f * l - 1f)) * s
    val x = c * (1f - abs((h / 60f) % 2f - 1f))
    val m = l - c / 2f
    val (r, g, b) = when {
        h < 60f -> Triple(c, x, 0f)
        h < 120f -> Triple(x, c, 0f)
        h < 180f -> Triple(0f, c, x)
        h < 240f -> Triple(0f, x, c)
        h < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    return Color(r + m, g + m, b + m)
}

/**
 * The bill's text colour sits on white paper, so a pale colour cannot be read: Done darkens it until it can.
 * shortcut: plain sRGB brightness, good enough to rescue pale colours; use a contrast ratio if finer control is needed.
 */
private fun readable(c: Color): Color {
    val brightness = 0.299f * c.red + 0.587f * c.green + 0.114f * c.blue
    if (brightness <= 0.6f) return c
    val k = 0.6f / brightness
    return Color(c.red * k, c.green * k, c.blue * k)
}

private val hues = listOf(0f, 30f, 55f, 85f, 130f, 170f, 200f, 225f, 265f, 305f)
private const val ROWS = 6

/** The colour area is this tall in both modes, so the sheet never changes size when the mode changes. */
private val AREA_HEIGHT = 168.dp

/**
 * "More colours", in the app's action sheet, one fixed size with nothing to scroll: a Swatches | Spectrum pill shifter
 * (tap or drag), the colour area (the same height in both), the hex code, the colour as it is now beside the new one
 * with its red, green and blue values, and the last colours used. Done keeps the colour (a pale one is darkened to stay
 * readable, see [readable]); Cancel leaves it as it was.
 */
@Composable
fun BillColorPickerDialog(initial: Color, colors: ShellColors, recent: List<String>, onDismiss: () -> Unit, onDone: (String) -> Unit) {
    val ff = LocalAppFontFamily.current
    var tab by remember { mutableStateOf(0) }
    var color by remember { mutableStateOf(initial) }
    var hsv by remember { mutableStateOf(toHsv(initial)) }

    fun pick(c: Color) {
        color = c
        hsv = toHsv(c)
    }
    fun pickHsv(h: Float, s: Float, v: Float) {
        hsv = Triple(h, s, v)
        color = Color.hsv(h, s, v)
    }
    val hex = billColorHex(color)
    var hexText by remember(hex) { mutableStateOf(hex.uppercase()) }
    val small = TextStyle(fontFamily = ff, fontSize = 12.sp)

    ElvanActionSheet(
        title = K.pdfTheme.tr(),
        onDismissRequest = onDismiss,
        onConfirm = { onDone(billColorHex(readable(color))) },
        colors = colors,
        slideDistance = 620.dp,
        customContent = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ElvanPillShifter(
                    items = listOf(PillShifterItem(K.swatches.tr()), PillShifterItem(K.spectrum.tr())),
                    selectedIndex = tab,
                    onIndexSelected = { tab = it },
                    colors = colors,
                    isFullWidth = true
                )

                Column(Modifier.fillMaxWidth().height(AREA_HEIGHT)) {
                    if (tab == 0) {
                        val cols = hues.size + 1
                        fun cellColor(r: Int, c: Int): Color {
                            val l = 0.62f - r * (0.50f / (ROWS - 1))
                            return if (c == 0) hslColor(0f, 0f, l) else hslColor(hues[c - 1], 1f, l)
                        }
                        Canvas(
                            Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)).pointerInput(Unit) {
                                detectTapGestures { o ->
                                    val c = (o.x / size.width * cols).toInt().coerceIn(0, cols - 1)
                                    val r = (o.y / size.height * ROWS).toInt().coerceIn(0, ROWS - 1)
                                    pick(cellColor(r, c))
                                }
                            }
                        ) {
                            val w = size.width / cols
                            val h = size.height / ROWS
                            for (r in 0 until ROWS) for (c in 0 until cols) {
                                val cc = cellColor(r, c)
                                drawRect(cc, Offset(c * w, r * h), androidx.compose.ui.geometry.Size(w + 1f, h + 1f))
                                if (billColorHex(cc) == hex) drawRect(Color.White, Offset(c * w + 2f, r * h + 2f), androidx.compose.ui.geometry.Size(w - 4f, h - 4f), style = Stroke(3f))
                            }
                        }
                    } else {
                        var box by remember { mutableStateOf(IntSize(1, 1)) }
                        fun at(o: Offset) = pickHsv(
                            (o.x / box.width * 360f).coerceIn(0f, 360f),
                            (o.y / box.height).coerceIn(0f, 1f),
                            hsv.third
                        )
                        Canvas(
                            Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(10.dp))
                                .onSizeChanged { box = it }
                                .pointerInput(Unit) { detectTapGestures { at(it) } }
                                .pointerInput(Unit) { detectDragGestures(onDragStart = { at(it) }) { change, _ -> at(change.position) } }
                        ) {
                            drawRect(Brush.horizontalGradient((0..6).map { Color.hsv(it * 60f, 1f, 1f) }))
                            drawRect(Brush.verticalGradient(listOf(Color.White, Color.Transparent)))
                            val p = Offset(hsv.first / 360f * size.width, hsv.second * size.height)
                            drawCircle(color, radius = 11.dp.toPx(), center = p)
                            drawCircle(Color.White, radius = 11.dp.toPx(), center = p, style = Stroke(2.5.dp.toPx()))
                        }
                        Slider(
                            value = hsv.third,
                            onValueChange = { pickHsv(hsv.first, hsv.second, it) },
                            valueRange = 0.1f..1f,
                            modifier = Modifier.height(36.dp),
                            colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color)
                        )
                    }
                }

                // The colour now beside the new one, the hex code, and the red, green and blue values
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.size(width = 64.dp, height = 48.dp).clip(RoundedCornerShape(12.dp)).border(BorderStroke(1.dp, colors.textPrimary.copy(alpha = 0.3f)), RoundedCornerShape(12.dp))) {
                        Box(Modifier.weight(1f).fillMaxHeight().background(initial))
                        Box(Modifier.weight(1f).fillMaxHeight().background(color))
                    }
                    Box(Modifier.weight(1f)) {
                        ElvanSettingsTextField(
                            label = "${K.hexLabel.tr()}   ${(color.red * 255f + 0.5f).toInt()} \u00b7 ${(color.green * 255f + 0.5f).toInt()} \u00b7 ${(color.blue * 255f + 0.5f).toInt()}",
                            value = hexText,
                            onValueChange = { v ->
                                hexText = v.uppercase()
                                val digits = v.removePrefix("#")
                                if (digits.length == 6 && digits.all { it in "0123456789abcdefABCDEF" }) pick(Color(digits.toLong(16) or 0xFF000000L))
                            },
                            placeholder = "#RRGGBB",
                            colors = colors
                        )
                    }
                }

                if (recent.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(K.recentColors.tr(), style = small, color = colors.textPrimary.copy(alpha = 0.6f), modifier = Modifier.weight(1f))
                        recent.take(5).forEach { h ->
                            Box(Modifier.size(28.dp).clip(CircleShape).background(parseBillColor(h)).clickable { pick(parseBillColor(h)) })
                        }
                    }
                }
            }
        }
    )
}
