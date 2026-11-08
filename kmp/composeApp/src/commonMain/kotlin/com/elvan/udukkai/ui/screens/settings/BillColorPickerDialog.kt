package com.elvan.udukkai.ui.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.ui.components.ElvanColorPicker
import com.elvan.udukkai.ui.components.colorToHex

/** "#rrggbb" of a colour. */
fun billColorHex(c: Color): String = colorToHex(c)

/**
 * The bill's text colour sits on white paper, so a pale colour cannot be read: Done darkens it until it can.
 * shortcut: plain sRGB brightness, good enough to rescue pale colours; use a contrast ratio if finer control is needed.
 */
internal fun readable(c: Color): Color {
    val brightness = 0.299f * c.red + 0.587f * c.green + 0.114f * c.blue
    if (brightness <= 0.6f) return c
    val k = 0.6f / brightness
    return Color(c.red * k, c.green * k, c.blue * k)
}

/** "More colours" for the PDF theme: the shared [ElvanColorPicker], with the colour kept readable on white paper. */
@Composable
fun BillColorPickerDialog(initial: Color, colors: ShellColors, recent: List<String>, onDismiss: () -> Unit, onDone: (String) -> Unit) {
    ElvanColorPicker(
        initial = initial,
        colors = colors,
        recent = recent,
        onDismiss = onDismiss,
        onDone = { onDone(billColorHex(readable(it.compositeOver(Color.White)))) }
    )
}
