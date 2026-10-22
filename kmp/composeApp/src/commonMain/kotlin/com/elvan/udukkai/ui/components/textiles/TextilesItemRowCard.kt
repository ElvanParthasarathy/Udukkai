package com.elvan.udukkai.ui.components.textiles

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.ShellDefaults
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors

/**
 * Textiles (சவுளி / Goods) specific billing item component.
 */
@Composable
fun TextilesItemRowCard(
    itemName: String,
    hsnCode: String,
    weightGrams: String,
    gstRate: String,
    price: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    colors: ShellColors = rememberShellColors()
) {
    val shape = RoundedCornerShape(20.dp)
    val ff = LocalAppFontFamily.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(0.5.dp, colors.border, shape)
            .then(
                if (onClick != null) Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ShellDefaults.ripple(colors, bounded = true),
                    onClick = onClick
                ) else Modifier
            ),
        shape = shape,
        color = colors.surface,
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = itemName.preventBrokenLigatures(),
                    style = TextStyle(fontFamily = ff, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                )
                Text(
                    text = price.preventBrokenLigatures(),
                    style = TextStyle(fontFamily = ff, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.accent)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${K.hsnSacCode.tr()}: $hsnCode".preventBrokenLigatures(),
                    style = TextStyle(fontFamily = ff, fontSize = 13.sp, color = colors.textSecondary)
                )
                Text(
                    text = "${K.gstRate.tr()}: $gstRate%".preventBrokenLigatures(),
                    style = TextStyle(fontFamily = ff, fontSize = 13.sp, color = colors.textSecondary)
                )
            }
        }
    }
}
