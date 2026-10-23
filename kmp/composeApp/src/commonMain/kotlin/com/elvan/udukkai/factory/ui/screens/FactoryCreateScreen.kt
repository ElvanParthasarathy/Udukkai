package com.elvan.udukkai.factory.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.utils.DateGroupUtils
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.model.ReceiptModel
import com.elvan.udukkai.factory.data.FactoryInvoiceRepository
import com.elvan.udukkai.factory.data.FactoryProfileRepository
import com.elvan.udukkai.factory.data.FactoryReceiptRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.LocalShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.LocalElvanTopSpacerHeight
import com.elvan.udukkai.ui.components.shell.UruvakkuCardSkeleton
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.create.components.*
import kotlinx.coroutines.delay

@Composable
fun FactoryCreateScreen(
    scrollState: LazyListState,
    selectedSegment: Int,
    onSegmentSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isRefreshing: Boolean = false,
    isSearchActive: Boolean = false,
    isSelectionMode: Boolean = false,
    selectedItemIds: Set<Long> = emptySet(),
    onToggleSelect: ((Long) -> Unit)? = null,
    onItemLongClick: ((Long) -> Unit)? = null,
    onInvoiceClick: (InvoiceModel) -> Unit = {},
    onReceiptClick: (ReceiptModel) -> Unit = {}
) {
    val colors = rememberShellColors()
    val isDark = colors.isDark
    val ff = LocalAppFontFamily.current
    val activeProfile = FactoryProfileRepository.profile

    val invoices = FactoryInvoiceRepository.filteredInvoices
    val receipts = FactoryReceiptRepository.filteredReceipts

    val pageSize = 10
    var visibleInvoiceCount by remember { mutableIntStateOf(pageSize) }
    var visibleReceiptCount by remember { mutableIntStateOf(pageSize) }

    LaunchedEffect(activeProfile.id, FactoryInvoiceRepository.startDateFilter, FactoryInvoiceRepository.endDateFilter) {
        visibleInvoiceCount = pageSize
    }
    LaunchedEffect(activeProfile.id, FactoryReceiptRepository.startDateFilter, FactoryReceiptRepository.endDateFilter) {
        visibleReceiptCount = pageSize
    }

    val currentSearchQuery = if (selectedSegment == 0) FactoryInvoiceRepository.searchQuery else FactoryReceiptRepository.searchQuery
    var isSearchLoading by remember { mutableStateOf(false) }

    LaunchedEffect(currentSearchQuery) {
        visibleInvoiceCount = pageSize
        visibleReceiptCount = pageSize
        if (currentSearchQuery.isNotBlank()) {
            isSearchLoading = true
            delay(300)
            isSearchLoading = false
        } else {
            isSearchLoading = false
        }
    }

    val displayedInvoices = remember(invoices, visibleInvoiceCount) {
        invoices.take(visibleInvoiceCount)
    }
    val hasMoreInvoices = invoices.size > visibleInvoiceCount

    val displayedReceipts = remember(receipts, visibleReceiptCount) {
        receipts.take(visibleReceiptCount)
    }
    val hasMoreReceipts = receipts.size > visibleReceiptCount

    val invoiceGroups = remember(displayedInvoices) {
        DateGroupUtils.groupItemsByDate(displayedInvoices) { it.invoiceDate }
    }

    val receiptGroups = remember(displayedReceipts) {
        DateGroupUtils.groupItemsByDate(displayedReceipts) { it.receiptDate }
    }

    val shifterItems = listOf(
        PillShifterItem(
            label = K.invoices.tr(),
            icon = MaterialSymbols.Rounded.Invoice,
            activeIcon = MaterialSymbols.Rounded.InvoiceFill
        ),
        PillShifterItem(
            label = K.receipts.tr(),
            icon = MaterialSymbols.Rounded.Receipt,
            activeIcon = MaterialSymbols.Rounded.ReceiptFill
        )
    )

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = Dimens.ContentPaddingBottom
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item(key = "uruvakku_top_spacer") {
                Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
            }

            item(key = "top_shifters_container") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        ElvanPillShifter(
                            items = shifterItems,
                            selectedIndex = selectedSegment,
                            onIndexSelected = onSegmentSelected,
                            colors = colors,
                            isFullWidth = false
                        )
                    }

                    val isDateFilterActive = if (selectedSegment == 0) {
                        FactoryInvoiceRepository.isDateFilterActive
                    } else {
                        FactoryReceiptRepository.isDateFilterActive
                    }

                    if (isDateFilterActive) {
                        val startMillis = if (selectedSegment == 0) FactoryInvoiceRepository.startDateFilter else FactoryReceiptRepository.startDateFilter
                        val endMillis = if (selectedSegment == 0) FactoryInvoiceRepository.endDateFilter else FactoryReceiptRepository.endDateFilter
                        val todayText = K.today.tr()
                        val yesterdayText = K.yesterday.tr()
                        val filterText = remember(startMillis, endMillis, todayText, yesterdayText) {
                            if (startMillis == null && endMillis == null) {
                                ""
                            } else if (startMillis != null && endMillis != null) {
                                val startKey = DateGroupUtils.getDayKey(startMillis)
                                val endKey = DateGroupUtils.getDayKey(endMillis)
                                if (startKey == endKey) {
                                    val now = System.currentTimeMillis()
                                    val todayKey = DateGroupUtils.getDayKey(now)
                                    val yesterdayKey = DateGroupUtils.getDayKey(now - 86400000L)
                                    when (startKey) {
                                        todayKey -> todayText
                                        yesterdayKey -> yesterdayText
                                        else -> DateUtils.formatDate(startMillis)
                                    }
                                } else {
                                    "${DateUtils.formatDate(startMillis)} - ${DateUtils.formatDate(endMillis)}"
                                }
                            } else if (startMillis != null) {
                                ">= ${DateUtils.formatDate(startMillis)}"
                            } else {
                                "<= ${DateUtils.formatDate(endMillis!!)}"
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimens.ContentPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = colors.accent.copy(alpha = 0.12f),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        FactoryInvoiceRepository.clearDateFilter()
                                        FactoryReceiptRepository.clearDateFilter()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = MaterialSymbols.Rounded.FilterList,
                                        contentDescription = null,
                                        tint = colors.accent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = filterText,
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colors.accent
                                        )
                                    )
                                    Icon(
                                        imageVector = MaterialSymbols.Rounded.Close,
                                        contentDescription = "Clear",
                                        tint = colors.accent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (isSearchLoading || isRefreshing) {
                items(6) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.ContentPadding)
                    ) {
                        UruvakkuCardSkeleton()
                    }
                }
            } else if (selectedSegment == 0) {
                if (invoices.isEmpty()) {
                    item(key = "empty_invoices") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 56.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = MaterialSymbols.Rounded.Invoice,
                                    contentDescription = null,
                                    tint = LocalShellColors.current.textQuaternary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = if (isSearchActive || FactoryInvoiceRepository.isDateFilterActive) {
                                        K.noMatchingInvoices.tr().preventBrokenLigatures()
                                    } else {
                                        K.noInvoicesYet.tr().preventBrokenLigatures()
                                    },
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 15.sp,
                                        color = LocalShellColors.current.textTertiary
                                    )
                                )
                            }
                        }
                    }
                } else {
                    invoiceGroups.forEach { group ->
                        item(key = "date_hdr_inv_${group.dayKey}") {
                            DateSectionHeader(
                                dateMillis = group.dateMillis,
                                colors = colors
                            )
                        }

                        items(
                            items = group.items,
                            key = { "invoice_${it.data.id}" }
                        ) { indexedInvoice ->
                            val index = indexedInvoice.globalIndex
                            val invoice = indexedInvoice.data
                            val isSelected = selectedItemIds.contains(invoice.id)
                            val onCardClick: () -> Unit = {
                                if (isSelectionMode) {
                                    onToggleSelect?.invoke(invoice.id)
                                } else {
                                    onInvoiceClick(invoice)
                                }
                            }
                            val onCardLongClick: () -> Unit = {
                                onItemLongClick?.invoke(invoice.id)
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = Dimens.ContentPadding)
                            ) {
                                FactoryInvoiceCard(
                                    index = index,
                                    invoice = invoice,
                                    colors = colors,
                                    isSelectionMode = isSelectionMode,
                                    isSelected = isSelected,
                                    onClick = onCardClick,
                                    onLongClick = onCardLongClick
                                )
                            }
                        }
                    }

                    if (hasMoreInvoices) {
                        item(key = "inv_bottom_loader") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(26.dp),
                                    strokeWidth = 2.5.dp,
                                    color = colors.accent
                                )
                                LaunchedEffect(Unit) {
                                    delay(350)
                                    visibleInvoiceCount += pageSize
                                }
                            }
                        }
                    }
                }
            } else {
                if (receipts.isEmpty()) {
                    item(key = "empty_receipts") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 56.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = MaterialSymbols.Rounded.Receipt,
                                    contentDescription = null,
                                    tint = LocalShellColors.current.textQuaternary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = if (isSearchActive || FactoryReceiptRepository.isDateFilterActive) {
                                        K.noMatchingReceipts.tr().preventBrokenLigatures()
                                    } else {
                                        K.noReceiptsYet.tr().preventBrokenLigatures()
                                    },
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 15.sp,
                                        color = LocalShellColors.current.textTertiary
                                    )
                                )
                            }
                        }
                    }
                } else {
                    receiptGroups.forEach { group ->
                        item(key = "date_hdr_rec_${group.dayKey}") {
                            DateSectionHeader(
                                dateMillis = group.dateMillis,
                                colors = colors
                            )
                        }

                        items(
                            items = group.items,
                            key = { "receipt_${it.data.id}" }
                        ) { indexedReceipt ->
                            val index = indexedReceipt.globalIndex
                            val receipt = indexedReceipt.data
                            val isSelected = selectedItemIds.contains(receipt.id)
                            val onCardClick: () -> Unit = {
                                if (isSelectionMode) {
                                    onToggleSelect?.invoke(receipt.id)
                                } else {
                                    onReceiptClick(receipt)
                                }
                            }
                            val onCardLongClick: () -> Unit = {
                                onItemLongClick?.invoke(receipt.id)
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = Dimens.ContentPadding)
                            ) {
                                ReceiptCard(
                                    index = index,
                                    receipt = receipt,
                                    colors = colors,
                                    isSelectionMode = isSelectionMode,
                                    isSelected = isSelected,
                                    onClick = onCardClick,
                                    onLongClick = onCardLongClick,
                                    profile = FactoryProfileRepository.profile,
                                    allProfiles = FactoryProfileRepository.allProfiles,
                                    linkedInvoiceNumbers = FactoryReceiptRepository.getLinkedInvoiceNumbers(receipt.id)
                                )
                            }
                        }
                    }

                    if (hasMoreReceipts) {
                        item(key = "rec_bottom_loader") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(26.dp),
                                    strokeWidth = 2.5.dp,
                                    color = colors.accent
                                )
                                LaunchedEffect(Unit) {
                                    delay(350)
                                    visibleReceiptCount += pageSize
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
