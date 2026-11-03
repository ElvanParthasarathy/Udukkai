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
private const val ROWS = 10

/**
 * "More colours", in the app's action sheet: a Swatches | Spectrum pill shifter (tap or drag), the grid or the spectrum
 * with its brightness slider, the hex code with its red, green and blue values, the colour as it is now beside the new
 * one, and the last colours used. Done keeps the colour (a pale one is darkened to stay readable, see [readable]);
 * Cancel leaves it as it was.
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

    ElvanActionSheet(
        title = K.pdfTheme.tr(),
        onDismissRequest = onDismiss,
        onConfirm = { onDone(billColorHex(readable(color))) },
        colors = colors,
        confirmColor = readable(color),
        isConfirmFilled = true,
        customContent = {
            Column(Modifier.heightIn(max = 470.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                ElvanPillShifter(
                    items = listOf(PillShifterItem(K.swatches.tr()), PillShifterItem(K.spectrum.tr())),
                    selectedIndex = tab,
                    onIndexSelected = { tab = it },
                    colors = colors,
                    isFullWidth = true
                )
                if (tab == 0) {
                    Column(Modifier.clip(RoundedCornerShape(8.dp))) {
                        for (r in 0 until ROWS) {
                            val l = 0.62f - r * (0.54f / (ROWS - 1))
                            Row(Modifier.fillMaxWidth()) {
                                (listOf<Color>(hslColor(0f, 0f, l)) + hues.map { hslColor(it, 1f, l) }).forEach { c ->
                                    Box(
                                        Modifier.weight(1f).aspectRatio(1f).background(c)
                                            .then(if (billColorHex(c) == hex) Modifier.border(BorderStroke(2.dp, Color.White)) else Modifier)
                                            .clickable { pick(c) }
                                    )
                                }
                            }
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
                        Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(8.dp))
                            .onSizeChanged { box = it }
                            .pointerInput(Unit) { detectTapGestures { at(it) } }
                            .pointerInput(Unit) { detectDragGestures(onDragStart = { at(it) }) { change, _ -> at(change.position) } }
                    ) {
                        drawRect(Brush.horizontalGradient((0..6).map { Color.hsv(it * 60f, 1f, 1f) }))
                        drawRect(Brush.verticalGradient(listOf(Color.White, Color.Transparent)))
                        val p = Offset(hsv.first / 360f * size.width, hsv.second * size.height)
                        drawCircle(color, radius = 14.dp.toPx(), center = p)
                        drawCircle(Color.White, radius = 14.dp.toPx(), center = p, style = Stroke(3.dp.toPx()))
                    }
                    Slider(
                        value = hsv.third,
                        onValueChange = { pickHsv(hsv.first, hsv.second, it) },
                        valueRange = 0.1f..1f,
                        colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color)
                    )
                }

                // The colour now and the new one, the hex code and its red, green and blue values
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.size(width = 64.dp, height = 48.dp).clip(RoundedCornerShape(12.dp)).border(BorderStroke(1.dp, colors.textPrimary.copy(alpha = 0.3f)), RoundedCornerShape(12.dp))) {
                        Box(Modifier.weight(1f).fillMaxHeight().background(initial))
                        Box(Modifier.weight(1f).fillMaxHeight().background(color))
                    }
                    Box(Modifier.weight(1f)) {
                        ElvanSettingsTextField(
                            label = K.hexLabel.tr(),
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
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    listOf(K.colorRed.tr() to color.red, K.green.tr() to color.green, K.colorBlue.tr() to color.blue).forEach { (label, v) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(label, style = TextStyle(fontFamily = ff, fontSize = 13.sp), color = colors.textPrimary.copy(alpha = 0.6f))
                            Text((v * 255f + 0.5f).toInt().toString(), style = TextStyle(fontFamily = ff, fontSize = 18.sp, fontWeight = FontWeight.Medium), color = colors.textPrimary)
                        }
                    }
                }

                if (recent.isNotEmpty()) {
                    Text(K.recentColors.tr(), style = TextStyle(fontFamily = ff, fontSize = 13.sp), color = colors.textPrimary.copy(alpha = 0.6f))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        recent.take(5).forEach { h ->
                            Box(Modifier.size(40.dp).clip(CircleShape).background(parseBillColor(h)).clickable { pick(parseBillColor(h)) })
                        }
                    }
                }
            }
        }
    )
}
