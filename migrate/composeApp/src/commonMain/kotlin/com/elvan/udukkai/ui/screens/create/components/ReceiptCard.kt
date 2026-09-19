package com.elvan.udukkai.ui.screens.create.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.mode.LocalAppMode
import com.elvan.udukkai.core.utils.CurrencyUtils
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.data.model.PatrugalTharavuru
import com.elvan.udukkai.data.settings.NiruvanaTharavugalRepository
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.ui.components.ElvanCommonCard
import com.elvan.udukkai.ui.components.ElvanCardSelector
import com.elvan.udukkai.ui.components.ElvanCardLeadingIcon
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.theme.LocalShellColors

/**
 * Pixel-perfect port of Receipt Card matching the exact card design of Invoices.
 */
@Composable
fun ReceiptCard(
    index: Int,
    receipt: PatrugalTharavuru,
    colors: ShellColors,
    modifier: Modifier = Modifier,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null
) {
    val ff = LocalAppFontFamily.current
    val isDark = colors.isDark

    val currentMode = LocalAppMode.current
    val profile = NiruvanaTharavugalRepository.getProfile(currentMode)
    val isBilingual = currentMode == AppMode.KOOLI || profile.iruMozhi
    val primaryLang = profile.mudhanMozhi.ifEmpty { "ta" }
    val secondaryLang = profile.thunaiMozhi.ifEmpty { "en" }

    val primary = receipt.vaangunarPeyar[primaryLang]
        ?: receipt.vaangunarPeyar["ta"]
        ?: receipt.vaangunarPeyar["en"]
        ?: receipt.vaangunarPeyar.values.firstOrNull()
        ?: receipt.patruEn

    val secondary = if (isBilingual) receipt.vaangunarPeyar[secondaryLang].orEmpty() else ""
    val showSecondary = isBilingual && secondary.isNotBlank() && secondary != primary

    val dateStr = DateUtils.formatEpochMillis(receipt.patruNaal)
    val amountStr = CurrencyUtils.formatInr(receipt.thogai)

    val (taLabel, enLabel) = when (receipt.seluthumMurai.lowercase()) {
        "cash" -> "ரொக்கம்" to "Cash"
        "upi" -> "UPI" to "UPI"
        "bank_transfer" -> "வங்கி" to "Bank"
        "cheque" -> "காசோலை" to "Cheque"
        "card" -> "அட்டை" to "Card"
        else -> receipt.seluthumMurai to ""
    }

    val paymentModeText = if (!isBilingual || enLabel.isEmpty() || taLabel.equals(enLabel, ignoreCase = true)) {
        if (primaryLang.lowercase().startsWith("en") && enLabel.isNotEmpty()) enLabel else taLabel
    } else if (primaryLang.lowercase().startsWith("en")) {
        "$enLabel  •  $taLabel"
    } else {
        "$taLabel  •  $enLabel"
    }

    ElvanCommonCard(
        onClick = onClick,
        onLongClick = onLongClick,
        isSelected = isSelected,
        modifier = modifier,
        padding = PaddingValues(16.dp),
        borderRadius = 24.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            ElvanCardSelector(
                isSelectionMode = isSelectionMode,
                isSelected = isSelected
            )

            ElvanCardLeadingIcon(
                icon = MaterialSymbols.Rounded.ReceiptLongFill,
                tint = colors.receiptColor
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Row 1: Name + Chevron
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = primary.preventBrokenLigatures(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.textPrimary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (showSecondary) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = secondary.preventBrokenLigatures(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 13.5.sp,
                                    color = LocalShellColors.current.textSecondary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
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

                Spacer(modifier = Modifier.height(8.dp))

                // Row 2: Receipt #  •  Date
                Text(
                    text = "${receipt.patruEn}  •  $dateStr",
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 13.5.sp,
                        color = LocalShellColors.current.textSecondary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Row 3: Payment mode text on left + Amount on right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = paymentModeText.preventBrokenLigatures(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 13.5.sp,
                            color = LocalShellColors.current.textSecondary
                        ),
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = amountStr,
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = if (amountStr.length > 11) 12.5.sp else 14.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.textPrimary
                        )
                    )
                }
            }
        }
    }
}