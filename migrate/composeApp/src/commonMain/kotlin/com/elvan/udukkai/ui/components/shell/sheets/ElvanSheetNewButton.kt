package com.elvan.udukkai.ui.components.shell.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.ShellDefaults
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * ElvanSheetNewButton — "+ Add New" bottom action matching Flutter's `elvan_maeladukku_pudhiya_pothan.dart` 1:1.
 * Top divider: outline 10% opacity.
 * Centered Row: add icon (20dp) + label (15sp, FontWeight.Medium 500, 80% opacity onSurface).
 * Padding: horizontal 32dp, vertical 16dp.
 */
@Composable
fun ElvanSheetNewButton(
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = K.addNew.tr(),
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(
            thickness = 1.dp,
            color = colors.textPrimary.copy(alpha = 0.1f)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ShellDefaults.ripple(colors, bounded = true),
                    onClick = onTap
                )
                .padding(horizontal = 32.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = MaterialSymbols.Rounded.AddCircle,
                contentDescription = null,
                tint = colors.textPrimary.copy(alpha = 0.8f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = ff,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = colors.textPrimary.copy(alpha = 0.8f)
            )
        }
    }
}
