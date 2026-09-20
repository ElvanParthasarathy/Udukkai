package com.elvan.udukkai.ui.components.silk

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
 * Silk / Goods specific billing item component.
 * Upgraded with One UI 20.dp radius, AMOLED surface (#111111), and M3 tactile ripple.
 */
@Composable
fun SilkProductCard(
    productName: String,
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
                Column {
                    Text(
                        text = productName.preventBrokenLigatures(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "HSN: $hsnCode",
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 12.sp
                        ),
                        color = colors.textSecondary
                    )
                }
                Text(
                    text = "₹$price",
                    style = TextStyle(
                        fontFamily = ff,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = colors.accent
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${K.weight.tr()}: $weightGrams g",
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 13.sp
                    ),
                    color = colors.textSecondary
                )
                Text(
                    text = "${K.gstRate.tr()}: $gstRate%",
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 13.sp
                    ),
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Deprecated("Use SilkProductCard", ReplaceWith("SilkProductCard(productName, hsnCode, weightGrams, gstRate, price, modifier, onClick, colors)"))
@Composable
fun PattuSilkProductCard(
    productName: String,
    hsnCode: String,
    weightGrams: String,
    gstRate: String,
    price: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    colors: ShellColors = rememberShellColors()
) = SilkProductCard(productName, hsnCode, weightGrams, gstRate, price, modifier, onClick, colors)
