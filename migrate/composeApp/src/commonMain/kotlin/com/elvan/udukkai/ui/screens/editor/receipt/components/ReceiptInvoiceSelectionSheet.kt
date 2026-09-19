package com.elvan.udukkai.ui.screens.editor.receipt.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import com.elvan.udukkai.core.platform.ConfigureDialogWindow
import com.elvan.udukkai.core.platform.PlatformType
import com.elvan.udukkai.core.platform.currentPlatform
import com.elvan.udukkai.data.model.PattiyalTharavuru
import com.elvan.udukkai.data.repository.VaangunarRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.sheets.ElvanSheetSearch
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.theme.LocalShellColors

/**
 * Bottom-sheet dialog for picking one or more invoices for receipt allocation.
 * Matches Flutter's `ReceiptInvoiceSelectionSheet` 1:1.
 * Features:
 * - Smart Filtering: Once the first invoice is picked, subsequent choices are locked
 *   to the same company profile (`niruvanamId`) and same customer (`vaangunarId`).
 * - Search by invoice number and customer name.
 * - Multi-select checkboxes.
 * - Done confirmation button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptInvoiceSelectionSheet(
    invoices: List<PattiyalTharavuru>,
    initialSelectedIds: Set<Long>,
    onConfirmed: (List<PattiyalTharavuru>) -> Unit,
    onDismissRequest: () -> Unit
) {
    val colors = rememberShellColors()
    val isDark = colors.isDark
    val ff = LocalAppFontFamily.current

    val selectedIds = remember { mutableStateListOf<Long>().apply { addAll(initialSelectedIds) } }
    var searchQuery by remember { mutableStateOf("") }
    val allMerchants = VaangunarRepository.merchants

    // Smart Filtering: Lock to the first selected invoice's company and customer
    val firstSelected = remember(selectedIds.toList(), invoices) {
        if (selectedIds.isNotEmpty()) {
            val firstId = selectedIds.first()
            invoices.firstOrNull { it.id == firstId }
        } else null
    }

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

    val filteredInvoices = remember(availableInvoices, searchQuery) {
        if (searchQuery.isBlank()) {
            availableInvoices
        } else {
            val q = searchQuery.trim().lowercase()
            availableInvoices.filter { inv ->
                val enMatch = inv.patrucheettuEn.lowercase().contains(q)
                val peyarTa = inv.vaangunarPeyar["ta"]?.lowercase().orEmpty()
                val peyarEn = inv.vaangunarPeyar["en"]?.lowercase().orEmpty()
                enMatch || peyarTa.contains(q) || peyarEn.contains(q)
            }
        }
    }

    val sheetBg = LocalShellColors.current.surface

    val sheetContent: @Composable () -> Unit = {
        ConfigureDialogWindow(isDark = isDark)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = 16.dp)
        ) {
            // Header: Title + Done Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = K.forWhichInvoice.tr().preventBrokenLigatures(),
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = {
                            val selectedList = invoices.filter { selectedIds.contains(it.id) }
                            onConfirmed(selectedList)
                            onDismissRequest()
                        }
                    ) {
                        Text(
                            text = K.okBtn.tr().preventBrokenLigatures(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.accent
                            )
                        )
                    }

                    IconButton(onClick = onDismissRequest) {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.Close,
                            contentDescription = "Close",
                            tint = colors.textSecondary
                        )
                    }
                }
            }

            // Search pill
            ElvanSheetSearch(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                colors = colors
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Invoices List
            if (filteredInvoices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = K.noInvoicesYet.tr().preventBrokenLigatures(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 14.sp,
                            color = colors.textSecondary.copy(alpha = 0.6f)
                        )
                    )
                }
            } else {
                val renderInvoiceRow: @Composable (PattiyalTharavuru) -> Unit = { inv ->
                    val isSelected = selectedIds.contains(inv.id)
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

                    val itemBg = if (isSelected) {
                        colors.accent.copy(alpha = 0.08f)
                    } else {
                        Color.Transparent
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(itemBg)
                            .clickable {
                                if (isSelected) {
                                    selectedIds.remove(inv.id)
                                } else {
                                    selectedIds.add(inv.id)
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Circular checkbox
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) colors.accent else colors.textSecondary.copy(alpha = 0.2f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = MaterialSymbols.Rounded.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Invoice info
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
                                            color = colors.textSecondary.copy(alpha = 0.7f)
                                        )
                                    )
                                }
                            }

                            if (customerName.isNotEmpty()) {
                                Text(
                                    text = customerName.preventBrokenLigatures(),
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 13.sp,
                                        color = colors.textSecondary
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            val subDetail = if (oorText.isNotEmpty()) "$oorText • $formattedAmount" else formattedAmount
                            Text(
                                text = subDetail.preventBrokenLigatures(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.accent
                                )
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        thickness = 0.5.dp,
                        color = colors.divider.copy(alpha = 0.3f)
                    )
                }

                if (filteredInvoices.size <= 5) {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                            .verticalScroll(scrollState)
                            .padding(horizontal = 16.dp)
                    ) {
                        filteredInvoices.forEach { inv ->
                            renderInvoiceRow(inv)
                        }
                    }
                } else {
                    val listState = rememberLazyListState()
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(420.dp)
                            .padding(horizontal = 16.dp)
                    ) {
                        items(filteredInvoices, key = { it.id }) { inv ->
                            renderInvoiceRow(inv)
                        }
                    }
                }
            }
        }
    }

    if (currentPlatform == PlatformType.DESKTOP) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = onDismissRequest
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                color = sheetBg,
                tonalElevation = 8.dp
            ) {
                sheetContent()
            }
        }
    } else {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            containerColor = sheetBg,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            sheetContent()
        }
    }
}
