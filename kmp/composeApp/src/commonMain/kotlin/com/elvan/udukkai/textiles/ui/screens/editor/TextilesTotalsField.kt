package com.elvan.udukkai.textiles.ui.screens.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.utils.CurrencyUtils
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.screens.editor.ElvanEditorCard

/**
 * Displays Textiles invoice totals — subtotal, discount, CGST/SGST/IGST breakdown, round-off, and grand total.
 */
@Composable
fun TextilesTotalsField(
    totals: TextilesTotals,
    modifier: Modifier = Modifier
) {
    val colors = rememberShellColors()
    val ff = LocalAppFontFamily.current

    ElvanEditorCard(
        padding = PaddingValues(24.dp),
        borderRadius = 24.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(0.dp) // rows carry their own 6dp so a hidden row leaves no gap
        ) {
            // Subtotal
            TotalsRow(
                label = K.subtotal.tr(),
                amount = totals.subtotal
            )

            // Rows come and go with the customer (GST or not, same state or not): they grow and shrink like the saved-details box
            AnimatedTotalsRow(visible = totals.totalDiscount > 0, label = K.discount.tr(), amount = -totals.totalDiscount, textColor = Color(0xFFE53935))
            AnimatedTotalsRow(visible = totals.cgst > 0, label = "CGST", amount = totals.cgst)
            AnimatedTotalsRow(visible = totals.sgst > 0, label = "SGST", amount = totals.sgst)
            AnimatedTotalsRow(visible = totals.igst > 0, label = "IGST", amount = totals.igst)
            AnimatedTotalsRow(visible = totals.roundOff != 0.0, label = K.roundOff.tr(), amount = totals.roundOff)

            HorizontalDivider(
                thickness = 1.dp,
                color = colors.textPrimary.copy(alpha = 0.08f),
                modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)
            )

            // Grand Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = K.grandTotal.tr().preventBrokenLigatures(),
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                )
                Text(
                    text = CurrencyUtils.formatInr(totals.grandTotal),
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary
                    )
                )
            }
        }
    }
}

@Composable
private fun AnimatedTotalsRow(visible: Boolean, label: String, amount: Double, textColor: Color? = null) {
    // Keeps the last real amount so the row does not show 0 while it shrinks away
    var shown by remember { mutableStateOf(amount) }
    if (visible) shown = amount
    val spec = spring<androidx.compose.ui.unit.IntSize>(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy)
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(animationSpec = spec) + fadeIn(),
        exit = shrinkVertically(animationSpec = spec) + fadeOut()
    ) {
        Box(Modifier.padding(top = 6.dp)) { TotalsRow(label = label, amount = shown, textColor = textColor) }
    }
}

@Composable
private fun TotalsRow(
    label: String,
    amount: Double,
    textColor: Color? = null
) {
    val colors = rememberShellColors()
    val ff = LocalAppFontFamily.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label.preventBrokenLigatures(),
            style = TextStyle(
                fontFamily = ff,
                fontSize = 14.sp,
                color = colors.textSecondary
            )
        )
        Text(
            text = CurrencyUtils.formatInr(amount),
            style = TextStyle(
                fontFamily = ff,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor ?: colors.textPrimary
            )
        )
    }
}
