package com.elvan.udukkai.ui.components.shell.sheets

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors

/**
 * ElvanSheetHeader — Bottom sheet header title matching Flutter's `elvan_maeladukku_thalaipu.dart` 1:1.
 * Padding: left 32dp, right 32dp, bottom 8dp, top 0dp.
 * Font: 14sp, FontWeight.Medium (500), 50% opacity onSurface.
 */
@Composable
fun ElvanSheetHeader(
    title: String,
    modifier: Modifier = Modifier,
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current
    Text(
        text = title,
        style = TextStyle(
            fontFamily = ff,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        ),
        color = colors.textPrimary.copy(alpha = 0.5f),
        modifier = modifier.padding(start = 32.dp, end = 32.dp, bottom = 8.dp)
    )
}
