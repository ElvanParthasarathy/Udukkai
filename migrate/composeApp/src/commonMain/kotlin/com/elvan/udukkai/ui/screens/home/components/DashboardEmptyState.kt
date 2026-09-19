package com.elvan.udukkai.ui.screens.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.theme.LocalShellColors

/**
 * Pixel-perfect port of Flutter's DashboardEmptyState.
 */
@Composable
fun DashboardEmptyState(
    colors: ShellColors,
    modifier: Modifier = Modifier
) {
    val ff = LocalAppFontFamily.current
    val isDark = colors.isDark

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = MaterialSymbols.Rounded.Description,
                contentDescription = null,
                tint = LocalShellColors.current.textQuaternary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = K.noInvoicesYet.tr().preventBrokenLigatures(),
                style = TextStyle(
                    fontFamily = ff,
                    fontSize = 15.sp,
                    color = LocalShellColors.current.textTertiary
                )
            )
        }
    }
}
