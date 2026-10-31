package com.elvan.udukkai.ui.screens.create.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.utils.CurrencyUtils
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.data.model.ReceiptModel
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.ui.components.ElvanCommonCard
import com.elvan.udukkai.ui.components.ElvanCardSelector
import com.elvan.udukkai.ui.components.ElvanCardLeadingIcon
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.theme.LocalShellColors

@Composable
fun ReceiptCard(
    index: Int,
    receipt: ReceiptModel,
    colors: ShellColors,
    modifier: Modifier = Modifier,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    profile: com.elvan.udukkai.data.settings.BusinessProfile? = null,
    allProfiles: List<com.elvan.udukkai.data.settings.BusinessProfile> = emptyList(),
    linkedInvoiceNumbers: List<String> = emptyList()
) {
    val ff = LocalAppFontFamily.current

    val resolvedProfile = (if (receipt.companyId != null) allProfiles.find { it.id == receipt.companyId } else null)
        ?: profile
        ?: com.elvan.udukkai.data.settings.BusinessProfile()
    val isBilingual = resolvedProfile.isBilingual
    val primaryLang = resolvedProfile.primaryLanguage.ifEmpty { "ta" }
    val secondaryLang = resolvedProfile.secondaryLanguage.ifEmpty { "en" }

    val primary = receipt.customerName[primaryLang]
        ?: receipt.customerName["ta"]
        ?: receipt.customerName["en"]
        ?: receipt.customerName.values.firstOrNull()
        ?: receipt.receiptNumber

    val secondary = if (isBilingual) receipt.customerName[secondaryLang].orEmpty() else ""
    val showSecondary = isBilingual && secondary.isNotBlank() && secondary != primary

    val dateStr = DateUtils.formatEpochMillis(receipt.receiptDate)
    val amountStr = CurrencyUtils.formatInr(receipt.amount)

    val (taLabel, enLabel) = when (receipt.paymentMode.lowercase()) {
        "cash" -> "காசு" to "Cash"
        "upi" -> "UPI" to "UPI"
        "bank_transfer" -> "வங்கி" to "Bank"
        "cheque" -> "காசோலை" to "Cheque"
        "card" -> "அட்டை" to "Card"
        else -> receipt.paymentMode to ""
    }

    val paymentModeText = if (primaryLang.lowercase().startsWith("en") && enLabel.isNotEmpty()) enLabel else taLabel
    val linkedInvoicesText = linkedInvoiceNumbers.joinToString(", ")

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
                icon = MaterialSymbols.Rounded.ReceiptFill,
                tint = colors.receiptColor
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
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

                // Bottom Section: Left (Receipt # • Date, Linked Invoices) + Right (Payment Mode over Amount)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Left Column: Receipt #  •  Date + Linked Invoices
                    Column(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "${receipt.receiptNumber}  •  $dateStr",
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 13.5.sp,
                                color = LocalShellColors.current.textSecondary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (linkedInvoicesText.isNotEmpty()) {
                            Text(
                                text = linkedInvoicesText.preventBrokenLigatures(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 13.sp,
                                    color = LocalShellColors.current.textSecondary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Right Column: Payment mode on top of Amount
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = paymentModeText.preventBrokenLigatures(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = LocalShellColors.current.textSecondary
                            ),
                            maxLines = 1
                        )

                        Text(
                            text = amountStr,
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = if (amountStr.length > 11) 13.sp else 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.textPrimary
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
