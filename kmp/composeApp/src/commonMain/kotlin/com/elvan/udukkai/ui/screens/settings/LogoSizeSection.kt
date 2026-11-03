package com.elvan.udukkai.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.ui.components.shell.ElvanImageViewer

/**
 * The size of a logo on the bill: a slider (50% to 130% of the size the bill draws it at; 100% is how it always was)
 * with a live preview. The preview is the logo's place at the top of the white bill: the logo is drawn at the bill's
 * size times the slider and cut off where the place ends, so a logo that is too big shows what it loses.
 * [baseWidth] x [baseHeight] is the size the bill gives that logo, in the bill's own pixels.
 * shortcut: the top is 130% so a bigger logo does not run into the bill number bar; raise it if the header gets room.
 */
@Composable
fun LogoSizeEditor(imagePath: String?, scale: Float, onScale: (Float) -> Unit, baseWidth: Int, baseHeight: Int, title: String, colors: ShellColors) {
    val ff = LocalAppFontFamily.current
    val f = (240f / baseWidth).coerceIn(0.75f, 2f)
    val frameW = (baseWidth * f).dp
    val frameH = (baseHeight * f).dp

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, style = TextStyle(fontFamily = ff, fontSize = 13.sp), color = colors.textPrimary.copy(alpha = 0.6f))
        Text("${(scale * 100f + 0.5f).toInt()}%", style = TextStyle(fontFamily = ff, fontSize = 13.sp), color = colors.textPrimary)
    }
    Slider(value = scale, onValueChange = onScale, valueRange = 0.5f..1.3f)
    Spacer(Modifier.height(4.dp))
    // The bill's header: white paper, the logo's place at the left
    Box(
        Modifier.fillMaxWidth().height(frameH + 16.dp).clip(RoundedCornerShape(12.dp))
            .background(Color.White).border(BorderStroke(1.dp, colors.textPrimary.copy(alpha = 0.15f)), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(Modifier.padding(start = 8.dp).size(frameW, frameH).border(BorderStroke(1.dp, Color(0x22000000))))
        ElvanImageViewer(
            value = imagePath,
            modifier = Modifier.padding(start = 8.dp).size(frameW * scale, frameH * scale),
            contentScale = ContentScale.Fit
        )
    }
}
