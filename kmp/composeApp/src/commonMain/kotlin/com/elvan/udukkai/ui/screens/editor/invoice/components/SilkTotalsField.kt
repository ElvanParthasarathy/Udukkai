package com.elvan.udukkai.ui.screens.editor.invoice.components

import androidx.compose.foundation.layout.*
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
import com.elvan.udukkai.ui.screens.editor.ElvanThiruthiAttai

/**
 * Displays invoice totals — subtotal, discount, CGST/SGST/IGST breakdown, round-off, and grand total.
 * Matches Flutter's `pattu_mothangal_kooru.dart` 1:1.
 */
@Composable
fun PattuMothangalKooru(
    totals: PattuMothangal,
    modifier: Modifier = Modifier
) {
    val colors = rememberShellColors()
    val ff = LocalAppFontFamily.current

    ElvanThiruthiAttai(
        padding = PaddingValues(24.dp),
        borderRadius = 24.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
        // Subtotal
        TotalsRow(
            label = K.subtotal.tr(),
            amount = totals.adippadaiMothangal
        )

        // Discount (if any)
        if (totals.thallupadiMothangal > 0) {
            TotalsRow(
                label = K.discount.tr(),
                amount = -totals.thallupadiMothangal,
                textColor = Color(0xFFE53935)
            )
        }

        // CGST
        if (totals.cgst > 0) {
            TotalsRow(
                label = "CGST",
                amount = totals.cgst
            )
        }

        // SGST
        if (totals.sgst > 0) {
            TotalsRow(
                label = "SGST",
                amount = totals.sgst
            )
        }

        // IGST
        if (totals.igst > 0) {
            TotalsRow(
                label = "IGST",
                amount = totals.igst
            )
        }

        // Round-off
        if (totals.suttruOff != 0.0) {
            TotalsRow(
                label = K.roundOff.tr(),
                amount = totals.suttruOff
            )
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = colors.textPrimary.copy(alpha = 0.08f),
            modifier = Modifier.padding(vertical = 4.dp)
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
                text = CurrencyUtils.formatInr(totals.mothaMothangal),
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
                fontWeight = FontWeight.Medium,
                color = textColor ?: colors.textPrimary
            )
        )
    }
}
