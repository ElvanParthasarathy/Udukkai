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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.ui.components.shell.ElvanActionSheet
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

// The swatch grid's ten columns of colour, and how each is lightened in the top rows and darkened below
private val SWATCH_BASES = listOf(0xFF0000, 0xFF8000, 0xFFFF00, 0x00FF00, 0x00FF66, 0x00FFFF, 0x0080FF, 0x0000FF, 0x6600FF, 0xFF00FF)
private val SWATCH_TINT = listOf(0.66f, 0.42f, 0.24f, 0.08f)
private val SWATCH_SHADE = listOf(1f, 0.98f, 0.86f, 0.66f, 0.36f, 0.2f)
private const val ROWS = 10
private const val COLS = 11
private const val RECENT_SLOTS = 5

/** The swatch at [row] from the top and [col] from the left: greys in the first column, then each colour from pale to dark. */
internal fun swatchColor(row: Int, col: Int): Color {
    if (col == 0) {
        val v = if (row == 0) 1f else (9 - row) / 10f
        return Color(v, v, v)
    }
    val base = Color(SWATCH_BASES[col - 1] or (0xFF shl 24))
    fun mix(a: Float, t: Float) = a + (1f - a) * t
    return if (row < SWATCH_TINT.size) SWATCH_TINT[row].let { Color(mix(base.red, it), mix(base.green, it), mix(base.blue, it)) }
    else SWATCH_SHADE[row - SWATCH_TINT.size].let { Color(base.red * it, base.green * it, base.blue * it) }
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

// The One UI 8 colour picker's layout, in dp
private val SIDE = 24.dp
private val TAB_HEIGHT = 40.dp
private val BAR_HEIGHT = 20.dp
private val SWATCH_HEIGHT = 277.dp
private val SPECTRUM_HEIGHT = 248.dp

private class PickerInk(dark: Boolean) {
    val strong = if (dark) Color(0xFFFAFAFF) else Color(0xFF010102)
    val soft = if (dark) Color(0xFFAAAAAD) else Color(0xFF6E6E73)
    val tab = if (dark) Color(0xFF2D2D30) else Color(0xFFEDEDF0)
    val emptySlot = if (dark) Color(0xFF3A3A3D) else Color(0xFFE4E4E7)
    val checkerA = if (dark) Color(0xFF3C3C40) else Color(0xFFFFFFFF)
    val checkerB = if (dark) Color(0xFF2C2C30) else Color(0xFFD8D8DC)
    val line = if (dark) Color(0xFF55555A) else Color(0xFFC4C4C8)
}

/**
 * The One UI 8 colour picker, in the picker form of the action sheet: Swatches and Spectrum tabs, the grid or the
 * hue and saturation square with brightness, opacity, the colour as it is now beside the new one, editable hex, red, green
 * and blue, and the last colours used. Done gives the chosen colour with its opacity; Cancel leaves it.
 * Keeping [recent] is the caller's job (see [pushRecentColor] and [parseRecentColors]).
 */
@Composable
fun ElvanColorPicker(
    initial: Color,
    colors: ShellColors,
    recent: List<String>,
    onDismiss: () -> Unit,
    onDone: (Color) -> Unit
) {
    val ff = LocalAppFontFamily.current
    val focus = LocalFocusManager.current
    val ink = remember(colors.isDark) { PickerInk(colors.isDark) }
    val swatchesLabel = K.swatches.tr()
    val spectrumLabel = K.spectrum.tr()
    var tab by remember { mutableStateOf(0) }
    var color by remember { mutableStateOf(initial) }
    var hsv by remember { mutableStateOf(rgbToHsv(initial)) }
    var opacity by remember { mutableStateOf(1f) }

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
        title = "",
        onDismissRequest = onDismiss,
        onConfirm = { onDone(color.copy(alpha = opacity)) },
        colors = colors,
        pickerCard = true,
        slideDistance = 720.dp,
        customContent = {
            BoxWithConstraints {
                // The grid gives way when the keyboard or a short window leaves less room than the full 634dp card
                val shortBy = (634.dp - maxHeight).coerceAtLeast(0.dp)
                Column(Modifier.fillMaxWidth().padding(start = SIDE, end = SIDE, top = 20.dp, bottom = 12.dp)) {
                    Row(Modifier.fillMaxWidth().height(TAB_HEIGHT)) {
                        listOf(swatchesLabel, spectrumLabel).forEachIndexed { i, label ->
                            val on = tab == i
                            Box(
                                Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(TAB_HEIGHT / 2))
                                    .background(if (on) ink.tab else Color.Transparent)
                                    .clickable(role = Role.Tab) { tab = i },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, maxLines = 1, style = TextStyle(fontFamily = ff, fontSize = 15.dp.fixedSp(), fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal), color = if (on) ink.strong else ink.soft)
                            }
                        }
                    }

                    // Touching the colour area leaves the text fields: the keyboard's own Back keeps their focus
                    Column(Modifier.fillMaxWidth().pointerInput(Unit) { awaitEachGesture { awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial); focus.clearFocus() } }) {
                        if (tab == 0) {
                            Spacer(Modifier.height(24.dp))
                            Canvas(
                                Modifier.fillMaxWidth().height((SWATCH_HEIGHT - shortBy).coerceAtLeast(80.dp)).clip(RoundedCornerShape(4.dp))
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
                                    val grow = 1.dp.toPx()
                                    val corner = CornerRadius(2.dp.toPx())
                                    drawRoundRect(Color.Black.copy(alpha = 0.3f), at + Offset(-grow, -grow + 1.5.dp.toPx()), Size(w + 2 * grow, h + 2 * grow), corner)
                                    drawRoundRect(cc, at + Offset(-grow, -grow), Size(w + 2 * grow, h + 2 * grow), corner)
                                }
                            }
                            Spacer(Modifier.height(40.dp))
                        } else {
                            Spacer(Modifier.height(24.dp))
                            Canvas(
                                Modifier.fillMaxWidth().height((SPECTRUM_HEIGHT - shortBy).coerceAtLeast(80.dp)).clip(RoundedCornerShape(4.dp))
                                    .semantics { contentDescription = "$spectrumLabel $hex" }
                                    .followFinger { o, area -> pickHsv((o.x / area.width * 360f).coerceIn(0f, 360f), (o.y / area.height).coerceIn(0f, 1f), hsv.third) }
                            ) {
                                drawRect(Brush.horizontalGradient((0..6).map { Color.hsv(it * 60f, 1f, 1f) }))
                                drawRect(Brush.verticalGradient(listOf(Color.White, Color.Transparent)))
                                val r = 13.dp.toPx()
                                val p = Offset((hsv.first / 360f * size.width).coerceIn(r, size.width - r), (hsv.second * size.height).coerceIn(r, size.height - r))
                                drawCircle(color, radius = r, center = p)
                                drawCircle(Color.White, radius = r, center = p, style = Stroke(2.dp.toPx()))
                            }
                            Spacer(Modifier.height(25.dp))
                            ColorBar(hsv.third, ink, null, listOf(Color.Black, Color.hsv(hsv.first, hsv.second, 1f))) { pickHsv(hsv.first, hsv.second, it) }
                            Spacer(Modifier.height(29.dp))
                        }
                        ColorBar(opacity, ink, ink, listOf(color.copy(alpha = 0f), color.copy(alpha = 1f))) { opacity = it }
                    }

                    Spacer(Modifier.height(26.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.5.dp)) {
                        Row(Modifier.size(width = 56.dp, height = 36.dp).clip(RoundedCornerShape(10.dp)).border(BorderStroke(1.dp, ink.strong.copy(alpha = 0.3f)), RoundedCornerShape(10.dp))) {
                            Box(Modifier.weight(1f).fillMaxHeight().background(initial))
                            Box(Modifier.weight(1f).fillMaxHeight().background(color.copy(alpha = opacity)))
                        }
                        ColorField(K.hexLabel.tr(), hex, ::cleanHexInput, KeyboardType.Ascii, ink, Modifier.weight(85f)) { t -> parseHexColor(t)?.let { pick(it); true } ?: false }
                        ColorField(K.colorRed.tr(), "${channel(color.red)}", ::cleanChannelInput, KeyboardType.Number, ink, Modifier.weight(40f)) { t -> t.toIntOrNull()?.let { pick(Color(it, channel(color.green), channel(color.blue))); true } ?: false }
                        ColorField(K.green.tr(), "${channel(color.green)}", ::cleanChannelInput, KeyboardType.Number, ink, Modifier.weight(40f)) { t -> t.toIntOrNull()?.let { pick(Color(channel(color.red), it, channel(color.blue))); true } ?: false }
                        ColorField(K.colorBlue.tr(), "${channel(color.blue)}", ::cleanChannelInput, KeyboardType.Number, ink, Modifier.weight(40f)) { t -> t.toIntOrNull()?.let { pick(Color(channel(color.red), channel(color.green), it)); true } ?: false }
                    }

                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.height(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(K.recentColors.tr(), maxLines = 1, style = TextStyle(fontFamily = ff, fontSize = 13.dp.fixedSp()), color = ink.soft)
                        Spacer(Modifier.width(16.dp))
                        Canvas(Modifier.weight(1f).height(2.dp)) {
                            val step = 3.dp.toPx()
                            var x = 0f
                            while (x < size.width) {
                                drawCircle(ink.line, 0.75.dp.toPx(), Offset(x, size.height / 2))
                                x += step
                            }
                        }
                    }
                    Spacer(Modifier.height(9.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        val shown = recent.take(RECENT_SLOTS).mapNotNull { h -> parseHexColor(h)?.let { h to it } }
                        shown.forEach { (h, c) ->
                            Box(
                                Modifier.size(40.dp).clip(CircleShape).background(c).border(1.dp, Color.Black.copy(alpha = 0.6f), CircleShape)
                                    .semantics { contentDescription = h.uppercase() }
                                    .clickable { pick(c) }
                            )
                        }
                        repeat(RECENT_SLOTS - shown.size) { Box(Modifier.size(40.dp).clip(CircleShape).background(ink.emptySlot).border(1.dp, Color.Black.copy(alpha = 0.6f), CircleShape)) }
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

/** A One UI colour bar with its percentage. A non-null [checker] draws the transparency check pattern under [gradient]. */
@Composable
private fun ColorBar(value: Float, ink: PickerInk, checker: PickerInk?, gradient: List<Color>, onChange: (Float) -> Unit) {
    val percent = (value * 100f + 0.5f).toInt()
    val thumbPx = with(LocalDensity.current) { 9.dp.toPx() }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(
            Modifier.weight(1f).height(BAR_HEIGHT).clip(RoundedCornerShape(BAR_HEIGHT / 2))
                .semantics {
                    contentDescription = "$percent%"
                    progressBarRangeInfo = ProgressBarRangeInfo(value, 0f..1f)
                    setProgress { onChange(it.coerceIn(0f, 1f)); true }
                }
                .followFinger { o, area -> onChange(((o.x - thumbPx) / (area.width - 2 * thumbPx)).coerceIn(0f, 1f)) }
        ) {
            if (checker != null) {
                val cell = 5.dp.toPx()
                drawRect(checker.checkerA)
                for (r in 0..(size.height / cell).toInt()) for (c in 0..(size.width / cell).toInt()) {
                    if ((r + c) % 2 == 0) drawRect(checker.checkerB, Offset(c * cell, r * cell), Size(cell, cell))
                }
            }
            drawRect(Brush.horizontalGradient(gradient))
            drawCircle(Color.White, 8.dp.toPx(), Offset(thumbPx + value * (size.width - 2 * thumbPx), size.height / 2))
        }
        Text("$percent%", style = TextStyle(fontFamily = LocalAppFontFamily.current, fontSize = 14.dp.fixedSp(), textAlign = TextAlign.End), color = ink.strong, modifier = Modifier.width(52.dp))
    }
}

/**
 * A small label over an editable value. While it has focus what is typed stays exactly as typed, so "#123" can go on to
 * become "#123456"; a value that does not parse shows in the error colour and changes nothing. On leaving it shows the
 * real value again. [apply] sets the colour and says whether the text was usable.
 */
@Composable
private fun ColorField(
    label: String, shown: String, clean: (String) -> String, keyboard: KeyboardType, ink: PickerInk, modifier: Modifier, apply: (String) -> Boolean
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
        Text(label, maxLines = 1, style = TextStyle(fontFamily = ff, fontSize = 12.dp.fixedSp()), color = ink.soft)
        BasicTextField(
            value = text,
            onValueChange = { v ->
                text = clean(v)
                valid = apply(text)
            },
            singleLine = true,
            textStyle = TextStyle(fontFamily = ff, fontSize = 14.dp.fixedSp(), textAlign = TextAlign.Center, color = if (valid) ink.strong else Color(0xFFD93E36)),
            cursorBrush = SolidColor(ink.strong),
            keyboardOptions = KeyboardOptions(keyboardType = keyboard, capitalization = KeyboardCapitalization.Characters, autoCorrectEnabled = false, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp).onFocusChanged { focused = it.isFocused }.semantics { contentDescription = label }
        )
    }
}
