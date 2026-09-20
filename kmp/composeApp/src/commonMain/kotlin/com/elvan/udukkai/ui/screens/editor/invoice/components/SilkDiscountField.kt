package com.elvan.udukkai.ui.screens.editor.invoice.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.screens.editor.ElvanThiruthiUlleedu

/**
 * Global discount input row with % / ₹ toggle pill button.
 * Matches Flutter's `pattu_thallupadi_kooru.dart` 1:1.
 */
@Composable
fun PattuThallupadiKooru(
    discountValue: String,
    discountType: String,
    onValueChanged: (String) -> Unit,
    onTypeChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberShellColors()
    val ff = LocalAppFontFamily.current

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        ElvanThiruthiUlleedu(
            label = K.globalDiscount.tr(),
            value = discountValue,
            onValueChange = onValueChanged,
            placeholder = "0",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            suffixIcon = {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colors.accent.copy(alpha = 0.12f))
                        .clickable {
                            val newType = if (discountType == "%") "₹" else "%"
                            onTypeChanged(newType)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (discountType == "%") "%" else "₹",
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent
                        )
                    )
                }
            }
        )
    }
}
