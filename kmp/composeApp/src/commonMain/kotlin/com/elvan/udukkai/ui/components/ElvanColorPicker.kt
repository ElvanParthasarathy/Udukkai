package com.elvan.udukkai.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.ui.components.shell.ElvanActionSheet
import com.elvan.udukkai.ui.screens.create.components.ElvanPillShifter
import com.elvan.udukkai.ui.screens.create.components.PillShifterItem
import kotlin.math.abs

/** "#rrggbb" of a colour (alpha is ignored). */
fun colorToHex(c: Color): String {
    fun two(v: Float) = (v * 255f + 0.5f).toInt().coerceIn(0, 255).toString(16).padStart(2, '0')
    return "#${two(c.red)}${two(c.green)}${two(c.blue)}"
}

/** "#rgb" or "#rrggbb", with or without the "#" and surrounding spaces; null for anything else. */
fun parseHexColor(text: String): Color? {
    val digits = text.trim().removePrefix("#")
    if (digits.isEmpty() || !digits.all { it in "0123456789abcdefABCDEF" }) return null
    val full = when (digits.length) {
        3 -> digits.map { "$it$it" }.joinToString("")
        6 -> digits
        else -> return null
    }
    return Color(full.toLong(16) or 0xFF000000L)
}

/** hue 0..360, saturation 0..1, value 0..1 */
fun rgbToHsv(c: Color): Triple<Float, Float, Float> {
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

fun hslColor(h: Float, s: Float, l: Float): Color {
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

/** The last colours used, newest first, each once (case ignored) and at most [max]. */
fun pushRecentColor(recent: List<String>, hex: String, max: Int = 5): List<String> =
    (listOf(hex.lowercase()) + recent.map { it.lowercase() }).distinct().take(max)

/** Reads the comma-separated list kept in preferences, dropping anything that is not a real "#rrggbb". */
fun parseRecentColors(raw: String?): List<String> =
    raw?.split(",")?.map { it.trim().lowercase() }?.filter { it.length == 7 && it.startsWith("#") && parseHexColor(it) != null }.orEmpty()

private val hues = listOf(0f, 30f, 55f, 120f, 150f, 180f, 210f, 240f, 270f, 300f)
private const val ROWS = 10
private const val COLS = 11
private const val RECENT_SLOTS = 5

/** The swatch at [row] from the top and [col] from the left: greys in the first column, then each hue from pale to dark. */
internal fun swatchColor(row: Int, col: Int): Color {
    val l = 1f - row / (ROWS - 1f)
    return if (col == 0) hslColor(0f, 0f, l) else hslColor(hues[col - 1], 1f, 0.92f - row * (0.84f / (ROWS - 1)))
}

/** What may stand in the hex field while typing: a leading # and up to six hex digits, in capitals. */
internal fun cleanHexInput(text: String): String =
    "#" + text.filter { it in "0123456789abcdefABCDEF" }.take(6).uppercase()

/** What may stand in a red, green or blue field while typing: up to three digits, at most 255. */
internal fun cleanChannelInput(text: String): String {
    val digits = text.filter { it.isDigit() }.take(3)
    return if ((digits.toIntOrNull() ?: 0) > 255) "255" else digits
}

private fun channel(v: Float) = (v * 255f + 0.5f).toInt()

/**
 * A colour chooser in the app's action sheet, one fixed size: a Swatches | Spectrum pill shifter, the colour area (the same
 * height in both), the colour as it is now beside the new one, editable hex (#rgb or #rrggbb) and red, green and blue,
 * and the last colours used. Done gives the chosen colour; Cancel leaves it.
 * Keeping [recent] is the caller's job (see [pushRecentColor] and [parseRecentColors]).
 */
@Composable
fun ElvanColorPicker(
    title: String,
    initial: Color,
    colors: ShellColors,
    recent: List<String>,
    onDismiss: () -> Unit,
    onDone: (Color) -> Unit
) {
    val ff = LocalAppFontFamily.current
    val swatchesLabel = K.swatches.tr()
    val spectrumLabel = K.spectrum.tr()
    val focus = LocalFocusManager.current
    var tab by remember { mutableStateOf(0) }
    var color by remember { mutableStateOf(initial) }
    var hsv by remember { mutableStateOf(rgbToHsv(initial)) }

    fun pick(c: Color) {
        color = c
        hsv = rgbToHsv(c)
    }
    fun pickHsv(h: Float, s: Float, v: Float) {
        hsv = Triple(h, s, v)
        color = Color.hsv(h, s, v)
    }
    val hex = colorToHex(color).uppercase()

    ElvanActionSheet(
        title = title,
        onDismissRequest = onDismiss,
        onConfirm = { onDone(color) },
        colors = colors,
        slideDistance = 32.dp,
        fadeWithSlide = true,
        customContent = {
            BoxWithConstraints {
                val areaHeight = (maxHeight - 360.dp).coerceIn(96.dp, 260.dp)
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    ElvanPillShifter(
                        items = listOf(PillShifterItem(swatchesLabel), PillShifterItem(spectrumLabel)),
                        selectedIndex = tab,
                        onIndexSelected = { tab = it },
                        colors = colors,
                        isFullWidth = true
                    )

                    // Touching the colour area leaves the text fields: the keyboard's own Back keeps their focus
                    Column(Modifier.fillMaxWidth().height(areaHeight).pointerInput(Unit) { awaitEachGesture { awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial); focus.clearFocus() } }) {
                        if (tab == 0) {
                            Canvas(
                                Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp))
                                    .semantics { contentDescription = "$swatchesLabel $hex" }
                                    .followFinger { o, area ->
                                        pick(swatchColor((o.y / area.height * ROWS).toInt().coerceIn(0, ROWS - 1), (o.x / area.width * COLS).toInt().coerceIn(0, COLS - 1)))
                                    }
                            ) {
                                val w = size.width / COLS
                                val h = size.height / ROWS
                                var lifted: Pair<Offset, Color>? = null
                                for (r in 0 until ROWS) for (c in 0 until COLS) {
                                    val cc = swatchColor(r, c)
                                    drawRect(cc, Offset(c * w, r * h), Size(w + 1f, h + 1f))
                                    if (colorToHex(cc).uppercase() == hex) lifted = Offset(c * w, r * h) to cc
                                }
                                // The chosen swatch stands up off the grid
                                lifted?.let { (at, cc) ->
                                    val grow = 3.dp.toPx()
                                    val corner = CornerRadius(3.dp.toPx())
                                    drawRoundRect(Color.Black.copy(alpha = 0.35f), at + Offset(-grow, -grow + 2.dp.toPx()), Size(w + 2 * grow, h + 2 * grow), corner)
                                    drawRoundRect(cc, at + Offset(-grow, -grow), Size(w + 2 * grow, h + 2 * grow), corner)
                                }
                            }
                        } else {
                            Canvas(
                                Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(10.dp))
                                    .semantics { contentDescription = "$spectrumLabel $hex" }
                                    .followFinger { o, area -> pickHsv((o.x / area.width * 360f).coerceIn(0f, 360f), (o.y / area.height).coerceIn(0f, 1f), hsv.third) }
                            ) {
                                drawRect(Brush.horizontalGradient((0..6).map { Color.hsv(it * 60f, 1f, 1f) }))
                                drawRect(Brush.verticalGradient(listOf(Color.White, Color.Transparent)))
                                val r = 13.dp.toPx()
                                val p = Offset((hsv.first / 360f * size.width).coerceIn(r, size.width - r), (hsv.second * size.height).coerceIn(r, size.height - r))
                                drawCircle(color, radius = r, center = p)
                                drawCircle(Color.White, radius = r, center = p, style = Stroke(2.5.dp.toPx()))
                            }
                            Spacer(Modifier.height(12.dp))
                            BrightnessBar(hsv.third, Color.hsv(hsv.first, hsv.second, 1f), colors) { pickHsv(hsv.first, hsv.second, it) }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.size(width = 64.dp, height = 36.dp).clip(RoundedCornerShape(10.dp)).border(BorderStroke(1.dp, colors.textPrimary.copy(alpha = 0.3f)), RoundedCornerShape(10.dp))) {
                            Box(Modifier.weight(1f).fillMaxHeight().background(initial))
                            Box(Modifier.weight(1f).fillMaxHeight().background(color))
                        }
                        ColorField(K.hexLabel.tr(), hex, ::cleanHexInput, KeyboardType.Ascii, colors, Modifier.weight(2f)) { t -> parseHexColor(t)?.let { pick(it); true } ?: false }
                        ColorField("R", "${channel(color.red)}", ::cleanChannelInput, KeyboardType.Number, colors, Modifier.weight(1f)) { t -> t.toIntOrNull()?.let { pick(Color(it, channel(color.green), channel(color.blue))); true } ?: false }
                        ColorField("G", "${channel(color.green)}", ::cleanChannelInput, KeyboardType.Number, colors, Modifier.weight(1f)) { t -> t.toIntOrNull()?.let { pick(Color(channel(color.red), it, channel(color.blue))); true } ?: false }
                        ColorField("B", "${channel(color.blue)}", ::cleanChannelInput, KeyboardType.Number, colors, Modifier.weight(1f)) { t -> t.toIntOrNull()?.let { pick(Color(channel(color.red), channel(color.green), it)); true } ?: false }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(K.recentColors.tr(), style = TextStyle(fontFamily = ff, fontSize = 12.sp), color = colors.textPrimary.copy(alpha = 0.6f))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            val shown = recent.take(RECENT_SLOTS).mapNotNull { h -> parseHexColor(h)?.let { h to it } }
                            shown.forEach { (h, c) ->
                                Box(
                                    Modifier.size(36.dp).clip(CircleShape).background(c).border(1.dp, colors.textPrimary.copy(alpha = 0.2f), CircleShape)
                                        .semantics { contentDescription = h.uppercase() }
                                        .clickable { pick(c) }
                                )
                            }
                            repeat(RECENT_SLOTS - shown.size) { Box(Modifier.size(36.dp).clip(CircleShape).background(colors.textPrimary.copy(alpha = 0.08f))) }
                        }
                    }
                }
            }
        }
    )
}

/** Reports the finger from the moment it lands until it lifts, so the colour follows at once rather than after a drag is recognised. */
private fun Modifier.followFinger(onAt: (Offset, IntSize) -> Unit): Modifier = pointerInput(Unit) {
    awaitEachGesture {
        val down = awaitFirstDown()
        onAt(down.position, size)
        drag(down.id) { change ->
            onAt(change.position, size)
            change.consume()
        }
    }
}

@Composable
private fun BrightnessBar(value: Float, pure: Color, colors: ShellColors, onChange: (Float) -> Unit) {
    val percent = (value * 100f + 0.5f).toInt()
    val thumbPx = with(LocalDensity.current) { 11.dp.toPx() }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Canvas(
            Modifier.weight(1f).height(28.dp)
                .semantics {
                    contentDescription = "$percent%"
                    progressBarRangeInfo = ProgressBarRangeInfo(value, 0f..1f)
                    setProgress { onChange(it.coerceIn(0f, 1f)); true }
                }
                .followFinger { o, area ->
                    onChange(((o.x - thumbPx) / (area.width - 2 * thumbPx)).coerceIn(0f, 1f))
                }
        ) {
            val thumb = 11.dp.toPx()
            val track = 20.dp.toPx()
            drawRoundRect(
                Brush.horizontalGradient(listOf(Color.Black, pure)),
                Offset(0f, (size.height - track) / 2), Size(size.width, track), CornerRadius(track / 2)
            )
            drawCircle(Color.White, thumb, Offset(thumb + value * (size.width - 2 * thumb), size.height / 2))
        }
        Text("$percent%", style = TextStyle(fontFamily = LocalAppFontFamily.current, fontSize = 13.sp, textAlign = TextAlign.End), color = colors.textPrimary, modifier = Modifier.width(44.dp))
    }
}

/**
 * A small label over an editable value. While it has focus what is typed stays exactly as typed, so "#123" can go on to
 * become "#123456"; a value that does not parse shows in the error colour and changes nothing. On leaving it shows the
 * real value again. [apply] sets the colour and says whether the text was usable.
 */
@Composable
private fun ColorField(
    label: String, shown: String, clean: (String) -> String, keyboard: KeyboardType, colors: ShellColors, modifier: Modifier, apply: (String) -> Boolean
) {
    val ff = LocalAppFontFamily.current
    val focus = LocalFocusManager.current
    var text by remember { mutableStateOf(shown) }
    var focused by remember { mutableStateOf(false) }
    var valid by remember { mutableStateOf(true) }
    LaunchedEffect(shown, focused) {
        if (!focused) {
            text = shown
            valid = true
        }
    }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = TextStyle(fontFamily = ff, fontSize = 12.sp), color = colors.textPrimary.copy(alpha = 0.6f))
        BasicTextField(
            value = text,
            onValueChange = { v ->
                text = clean(v)
                valid = apply(text)
            },
            singleLine = true,
            textStyle = TextStyle(fontFamily = ff, fontSize = 16.sp, textAlign = TextAlign.Center, color = if (valid) colors.textPrimary else colors.error),
            cursorBrush = SolidColor(colors.textPrimary),
            keyboardOptions = KeyboardOptions(keyboardType = keyboard, capitalization = KeyboardCapitalization.Characters, autoCorrectEnabled = false, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp).onFocusChanged { focused = it.isFocused }.semantics { contentDescription = label }
        )
    }
}
