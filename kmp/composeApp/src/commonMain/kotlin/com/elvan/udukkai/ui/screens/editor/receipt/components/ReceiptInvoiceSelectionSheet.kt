package com.elvan.udukkai.ui.screens.editor.receipt.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.data.model.PattiyalTharavuru
import com.elvan.udukkai.data.repository.VaangunarRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.LocalShellColors
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.ShellDefaults
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.ElvanSelectionBottomSheet
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * ReceiptInvoiceSelectionSheet — Rock-Solid Bottom Sheet for picking invoices to allocate receipt payments.
 * Standardized 1:1 on top of `ElvanSelectionBottomSheet` matching Address Settings and Flutter's `PatruPattiyalTheervuMaeladukku`.
 * Features:
 * - Downward drag handle to dismiss (Zero Detach, Zero Upward Bounce).
 * - Multi-selection with monochrome circular checkboxes.
 * - Sticky bottom Done button that is never pushed off-screen.
 * - Non-dismissing list scrollbar (ElvanSimpleScrollbar).
 * - Smart Filtering: Locking to the same company profile and customer of the first selected invoice.
 * - Instant search filter across invoice numbers and merchant names.
 */
@Composable
fun ReceiptInvoiceSelectionSheet(
    invoices: List<PattiyalTharavuru>,
    initialSelectedIds: Set<Long>,
    onConfirmed: (List<PattiyalTharavuru>) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    colors: ShellColors = rememberShellColors()
) {
    val allMerchants = VaangunarRepository.merchants
    val ff = LocalAppFontFamily.current
    val sheetBg = LocalShellColors.current.surface

    val initialSelected = remember(initialSelectedIds, invoices) {
        invoices.filter { initialSelectedIds.contains(it.id) }.toSet()
    }

    var currentSelection by remember { mutableStateOf(initialSelected) }
    val firstSelected = currentSelection.firstOrNull()

    val availableInvoices = remember(firstSelected, invoices) {
        if (firstSelected != null) {
            invoices.filter {
                it.niruvanamId == firstSelected.niruvanamId &&
                it.vaangunarId == firstSelected.vaangunarId
            }
        } else {
            invoices
        }
    }

    ElvanSelectionBottomSheet<PattiyalTharavuru>(
        title = K.forWhichInvoice.tr(),
        items = availableInvoices,
        selectedValues = initialSelected,
        onConfirmed = onConfirmed,
        confirmLabel = K.done.tr(),
        onDismissRequest = onDismissRequest,
        showSearch = true,
        emptyMessage = K.noInvoicesYet.tr(),
        onSelectionChanged = { updatedList ->
            currentSelection = updatedList.toSet()
        },
        searchFilter = { inv, q ->
            val query = q.trim().lowercase()
            val enMatch = inv.patrucheettuEn.lowercase().contains(query)
            val peyarTa = inv.vaangunarPeyar["ta"]?.lowercase().orEmpty()
            val peyarEn = inv.vaangunarPeyar["en"]?.lowercase().orEmpty()
            enMatch || peyarTa.contains(query) || peyarEn.contains(query)
        },
        itemLabelBuilder = { it.patrucheettuEn },
        itemRowContent = { inv, isSelected, onToggle ->
            val customer = allMerchants.firstOrNull { it.id == inv.vaangunarId }
            val oorText = customer?.oor?.get("ta")
                ?: customer?.oor?.get("en")
                ?: ""

            val dateStr = DateUtils.formatDate(inv.pattiyalNaal)

            val customerName = inv.vaangunarPeyar["ta"]
                ?: inv.vaangunarPeyar["en"]
                ?: ""

            val formattedAmount = "₹ " + if (inv.mothaThogai % 1.0 == 0.0) {
                inv.mothaThogai.toLong().toString()
            } else {
                val rounded = (inv.mothaThogai * 100).toLong() / 100.0
                rounded.toString()
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ShellDefaults.ripple(colors, bounded = true),
                        onClick = onToggle
                    )
                    .padding(horizontal = 32.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular checkbox (monochrome matching Elvan Design System)
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) colors.textPrimary else Color.Transparent)
                        .border(
                            width = 1.5.dp,
                            color = if (isSelected) colors.textPrimary else colors.textSecondary.copy(alpha = 0.35f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.Check,
                            contentDescription = null,
                            tint = sheetBg,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = inv.patrucheettuEn,
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                        )
                        if (dateStr.isNotEmpty()) {
                            Text(
                                text = dateStr,
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 12.sp,
                                    color = colors.textSecondary.copy(alpha = 0.6f)
                                )
                            )
                        }
                    }

                    if (customerName.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = customerName.preventBrokenLigatures(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 13.sp,
                                color = colors.textSecondary.copy(alpha = 0.8f)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    val amountDetail = if (oorText.isNotEmpty()) "$oorText • $formattedAmount" else formattedAmount
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = amountDetail.preventBrokenLigatures(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textSecondary.copy(alpha = 0.65f)
                        )
                    )
                }
            }
        },
        modifier = modifier,
        colors = colors
    )
}
