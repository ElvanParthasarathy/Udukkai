package com.elvan.udukkai.ui.screens.create.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import com.elvan.udukkai.textiles.data.TextilesInvoiceRepository

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.utils.CurrencyUtils
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.ui.components.ElvanCommonCard
import com.elvan.udukkai.ui.components.ElvanCardSelector
import com.elvan.udukkai.ui.components.ElvanCardLeadingIcon
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.theme.LocalShellColors

/**
 * Textiles (சவுளி) Invoice Card for the Invoices list.
 */
@Composable
fun TextilesInvoiceCard(
    index: Int,
    invoice: InvoiceModel,
    colors: ShellColors,
    modifier: Modifier = Modifier,
    isPaid: Boolean = TextilesInvoiceRepository.hasLinkedReceipts(invoice.id),
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null
) {
    val ff = LocalAppFontFamily.current
    val isDark = colors.isDark

    val profiles = com.elvan.udukkai.textiles.data.TextilesProfileRepository.profiles
    val profile = (if (invoice.companyId != null) profiles.find { it.id == invoice.companyId } else null)
        ?: com.elvan.udukkai.textiles.data.TextilesProfileRepository.profile
    val isBilingual = profile.isBilingual
    val primaryLang = profile.primaryLanguage.ifEmpty { "ta" }
    val secondaryLang = profile.secondaryLanguage.ifEmpty { "en" }

    val primary = invoice.customerName[primaryLang]
        ?: invoice.customerName["ta"]
        ?: invoice.customerName["en"]
        ?: invoice.customerName.values.firstOrNull()
        ?: "-"

    val secondary = if (isBilingual) invoice.customerName[secondaryLang].orEmpty() else ""
    val showSecondary = isBilingual && secondary.isNotBlank() && secondary != primary

    val primaryCity = invoice.customerAddress[primaryLang]
        ?: invoice.customerAddress["ta"]
        ?: invoice.customerAddress["en"]
        ?: invoice.customerAddress.values.firstOrNull()
        ?: ""

    val amountStr = CurrencyUtils.formatInr(invoice.totalAmount)
    val dateStr = DateUtils.formatEpochMillis(invoice.invoiceDate)
    val docTypeLabel = if (invoice.invoiceType == "proforma") K.proforma.tr() else K.taxInvoice.tr()
    val leftDetail = if (primaryCity.isNotBlank()) "$docTypeLabel  •  ${primaryCity.preventBrokenLigatures()}" else docTypeLabel

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
                icon = MaterialSymbols.Rounded.InvoiceFill,
                tint = colors.invoiceColor
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

                // Bottom Section: Left (Invoice # • Date, City) + Right (Status over Amount)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Left Column: Invoice #  •  Date + Oor (Place / Address)
                    Column(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "${invoice.invoiceNumber}  •  $dateStr",
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 13.5.sp,
                                color = LocalShellColors.current.textSecondary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (leftDetail.isNotEmpty()) {
                            Text(
                                text = leftDetail.preventBrokenLigatures(),
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

                    // Right Column: Status on top of Amount (no pill, clean text)
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = (if (isPaid) K.statusPaid.tr() else K.draft.tr()).preventBrokenLigatures(),
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
