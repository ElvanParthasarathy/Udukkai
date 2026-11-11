package com.elvan.udukkai.ui.screens.create.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.LocalShellColors
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * The text of a bill or receipt card.
 * Preserves the exact line count before selection mode so neither 1-line cards
 * wrap downwards nor 2-line cards shrink upwards when long-pressing.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DocCardText(
    colors: ShellColors,
    primary: String,
    secondLine: String,
    amount: String,
    date: String,
    number: String,
    fy: String,
    status: String,
    textLines: Int,
    isSelectionMode: Boolean = false,
    modifier: Modifier = Modifier,
    paymentIcon: ImageVector? = null
) {
    val separatePaymentDetails = paymentIcon != null
    val ff = LocalAppFontFamily.current
    val secondary = LocalShellColors.current.textSecondary
    val metadataLineHeight = 17.sp
    val numberStyle = TextStyle(fontFamily = ff, fontSize = if (separatePaymentDetails) 13.sp else 13.5.sp, lineHeight = metadataLineHeight, color = secondary)
    val dateStyle = TextStyle(fontFamily = ff, fontSize = 13.sp, lineHeight = metadataLineHeight, color = secondary)

    var primaryLineCount by remember { mutableStateOf<Int?>(null) }
    var secondLineCount by remember { mutableStateOf<Int?>(null) }

    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = primary.preventBrokenLigatures(),
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary
                    ),
                    maxLines = primaryLineCount ?: maxOf(2, textLines),
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { layoutResult ->
                        if (!isSelectionMode) {
                            primaryLineCount = layoutResult.lineCount
                        }
                    }
                )
                if (secondLine.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = secondLine.preventBrokenLigatures(),
                        style = TextStyle(fontFamily = ff, fontSize = 13.5.sp, color = secondary),
                        maxLines = secondLineCount ?: 2,
                        overflow = TextOverflow.Ellipsis,
                        onTextLayout = { layoutResult ->
                            if (!isSelectionMode) {
                                secondLineCount = layoutResult.lineCount
                            }
                        }
                    )
                }
            }
            Icon(
                imageVector = MaterialSymbols.Rounded.ChevronRight,
                contentDescription = null,
                tint = LocalShellColors.current.border,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.height(7.dp))

        val amountStyle = TextStyle(
            fontFamily = ff,
            fontSize = if (amount.length > 11) 13.sp else 15.sp,
            lineHeight = metadataLineHeight,
            fontWeight = FontWeight.ExtraBold,
            color = colors.textPrimary
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (separatePaymentDetails) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(number, style = numberStyle, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("•", style = dateStyle.copy(color = LocalShellColors.current.textTertiary))
                            Text(date.preventBrokenLigatures(), style = dateStyle)
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(number, style = numberStyle, maxLines = 1)
                        Text(
                            text = "•",
                            modifier = Modifier.padding(horizontal = 6.dp),
                            style = dateStyle.copy(color = LocalShellColors.current.textTertiary)
                        )
                        Text(date.preventBrokenLigatures(), style = dateStyle, maxLines = 1)
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = fy,
                    style = TextStyle(fontFamily = ff, fontSize = if (separatePaymentDetails) 12.sp else 12.5.sp, lineHeight = metadataLineHeight, color = LocalShellColors.current.textTertiary),
                    maxLines = 1
                )
            }
            Spacer(Modifier.width(8.dp))
            if (paymentIcon != null) {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Icon(paymentIcon, contentDescription = status, tint = secondary, modifier = Modifier.size(16.dp))
                    Text(amount, style = amountStyle, maxLines = 1)
                }
            } else {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (status.isNotBlank()) Text(
                        text = status.preventBrokenLigatures(),
                        style = TextStyle(fontFamily = ff, fontSize = 12.sp, lineHeight = metadataLineHeight, color = secondary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(amount, style = amountStyle, maxLines = 1)
                }
            }
        }
    }
}
