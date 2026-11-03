package com.elvan.udukkai.ui.screens.settings.screens

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
import com.elvan.udukkai.ui.components.shell.ElvanSectionContainer

/** The small heading above a group of settings on the settings page, so each setting is found by what it is about. */
@Composable
fun SettingsGroupTitle(text: String, colors: ShellColors) {
    ElvanSectionContainer {
        Text(
            text,
            style = TextStyle(fontFamily = LocalAppFontFamily.current, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.3.sp),
            color = colors.textPrimary.copy(alpha = 0.55f),
            modifier = Modifier.padding(start = 20.dp)
        )
    }
}
