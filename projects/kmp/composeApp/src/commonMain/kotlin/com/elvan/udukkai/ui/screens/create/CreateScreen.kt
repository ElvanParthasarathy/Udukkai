package com.elvan.udukkai.ui.screens.create

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import com.elvan.udukkai.core.utils.DateGroupUtils
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.ui.components.shell.UruvakkuCardSkeleton
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.mode.LocalAppMode
import com.elvan.udukkai.data.model.PattiyalTharavuru
import com.elvan.udukkai.data.model.PatrugalTharavuru
import com.elvan.udukkai.data.repository.PattiyalRepository
import com.elvan.udukkai.data.repository.PatrugalRepository
import com.elvan.udukkai.data.settings.NiruvanaTharavugalRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.LocalAppLanguage
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.LocalElvanTopSpacerHeight
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.create.components.*
import com.elvan.udukkai.theme.LocalShellColors

/**
 * Pixel-perfect port of Flutter's UruvakkuPage (Create tab).
 * Hosts:
 * 1. ElvanPillShifter ("பட்டியல்கள்" Invoices vs "பற்றுச்சீட்டுகள்" Receipts)
 * 2. Invoices list (Kooli / Pattu) or Receipts list with profile grouping and real-time search filtering.
 */
@Composable
fun CreateScreen(
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
    onInvoiceClick: (PattiyalTharavuru) -> Unit = {},
    onReceiptClick: (PatrugalTharavuru) -> Unit = {}
) {
    val mode = LocalAppMode.current
    val colors = rememberShellColors()
    val isDark = colors.isDark
    val ff = LocalAppFontFamily.current
    val uiLang = LocalAppLanguage.current
    val isTa = uiLang.lowercase().startsWith("ta")

    val profiles = NiruvanaTharavugalRepository.getAllProfiles(mode)

    var selectedProfileFilterIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(mode) {
        selectedProfileFilterIndex = 0
    }

    val allLabel = K.all.tr()
    val businessShifterItems = remember(profiles, allLabel) {
        if (profiles.isEmpty()) {
            emptyList()
        } else {
            val list = mutableListOf<PillShifterItem>()
            list.add(
                PillShifterItem(
                    label = allLabel
                )
            )
            profiles.forEach { p ->
                val shortName = p.kurumPeyar.ifEmpty {
                    p.niruvanathinPeyar.values.firstOrNull().orEmpty()
                }
                list.add(
                    PillShifterItem(
                        label = shortName
                    )
                )
            }
            list
        }
    }

    val safeProfileIndex = selectedProfileFilterIndex.coerceIn(0, (businessShifterItems.size - 1).coerceAtLeast(0))

    val allInvoices = PattiyalRepository.filteredInvoices
    val invoices = remember(allInvoices, safeProfileIndex, profiles) {
        if (safeProfileIndex == 0 || profiles.isEmpty()) {
            allInvoices
        } else {
            val targetProfile = profiles.getOrNull(safeProfileIndex - 1)
            if (targetProfile != null) {
                allInvoices.filter { it.niruvanamId == targetProfile.id }
            } else {
                allInvoices
            }
        }
    }

    val allReceipts = PatrugalRepository.filteredReceipts
    val receipts = remember(allReceipts, safeProfileIndex, profiles) {
        if (safeProfileIndex == 0 || profiles.isEmpty()) {
            allReceipts
        } else {
            val targetProfile = profiles.getOrNull(safeProfileIndex - 1)
            if (targetProfile != null) {
                allReceipts.filter { it.niruvanamId == targetProfile.id }
            } else {
                allReceipts
            }
        }
    }

    val activeProfile = NiruvanaTharavugalRepository.getProfile(mode)
    val isBilingual = mode == AppMode.KOOLI || activeProfile.iruMozhi
    val primaryLang = activeProfile.mudhanMozhi.ifEmpty { "ta" }

    val pageSize = 10
    var visibleInvoiceCount by remember { mutableIntStateOf(pageSize) }
    var visibleReceiptCount by remember { mutableIntStateOf(pageSize) }

    // Reset pagination when mode, company profile filter, or date filter changes
    LaunchedEffect(mode, safeProfileIndex, PattiyalRepository.startDateFilter, PattiyalRepository.endDateFilter) {
        visibleInvoiceCount = pageSize
    }
    LaunchedEffect(mode, safeProfileIndex, PatrugalRepository.startDateFilter, PatrugalRepository.endDateFilter) {
        visibleReceiptCount = pageSize
    }

    val currentSearchQuery = if (selectedSegment == 0) PattiyalRepository.searchQuery else PatrugalRepository.searchQuery
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
        DateGroupUtils.groupItemsByDate(displayedInvoices) { it.pattiyalNaal }
    }

    val receiptGroups = remember(displayedReceipts) {
        DateGroupUtils.groupItemsByDate(displayedReceipts) { it.patruNaal }
    }



    val shifterItems = listOf(
        PillShifterItem(
            label = K.invoices.tr(),
            icon = MaterialSymbols.Rounded.Description,
            activeIcon = MaterialSymbols.Rounded.DescriptionFill
        ),
        PillShifterItem(
            label = K.receipts.tr(),
            icon = MaterialSymbols.Rounded.ReceiptLong,
            activeIcon = MaterialSymbols.Rounded.ReceiptLongFill
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
            // Collapsible Top Header spacer
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
                    // Segmented Pill Shifter (Invoices vs Receipts - Original Centered Style)
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

                    // Business Profile Filter Shifter (below Invoices vs Receipts pill - End to End)
                    if (businessShifterItems.size > 1) {
                        ElvanPillShifter(
                            items = businessShifterItems,
                            selectedIndex = safeProfileIndex,
                            onIndexSelected = { selectedProfileFilterIndex = it },
                            colors = colors,
                            isFullWidth = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimens.ContentPadding)
                        )
                    }

                        val isDateFilterActive = if (selectedSegment == 0) PattiyalRepository.isDateFilterActive else PatrugalRepository.isDateFilterActive
                        if (isDateFilterActive) {
                            val startMillis = if (selectedSegment == 0) PattiyalRepository.startDateFilter else PatrugalRepository.startDateFilter
                            val endMillis = if (selectedSegment == 0) PattiyalRepository.endDateFilter else PatrugalRepository.endDateFilter
                            val filterText = remember(startMillis, endMillis, isTa) {
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
                                            todayKey -> if (isTa) "இன்று" else "Today"
                                            yesterdayKey -> if (isTa) "நேற்று" else "Yesterday"
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
                                    onClick = {
                                        PattiyalRepository.clearDateFilter()
                                        PatrugalRepository.clearDateFilter()
                                    },
                                    shape = CircleShape,
                                    color = colors.accent.copy(alpha = 0.12f),
                                    modifier = Modifier.clip(CircleShape)
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
                                    imageVector = MaterialSymbols.Rounded.Description,
                                    contentDescription = null,
                                    tint = LocalShellColors.current.textQuaternary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = K.noInvoicesYet.tr().preventBrokenLigatures(),
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
                            key = { "inv_${it.data.id}" }
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
                                if (mode == AppMode.KOOLI) {
                                    CoolieInvoiceCard(
                                        index = index,
                                        pattiyal = invoice,
                                        colors = colors,
                                        isSelectionMode = isSelectionMode,
                                        isSelected = isSelected,
                                        onClick = onCardClick,
                                        onLongClick = onCardLongClick
                                    )
                                } else {
                                    SilkInvoiceCard(
                                        index = index,
                                        pattiyal = invoice,
                                        colors = colors,
                                        isSelectionMode = isSelectionMode,
                                        isSelected = isSelected,
                                        onClick = onCardClick,
                                        onLongClick = onCardLongClick
                                    )
                                }
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
                            }
                            LaunchedEffect(Unit) {
                                delay(350)
                                visibleInvoiceCount += pageSize
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
                                    imageVector = MaterialSymbols.Rounded.ReceiptLong,
                                    contentDescription = null,
                                    tint = LocalShellColors.current.textQuaternary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = K.noReceiptsYet.tr().preventBrokenLigatures(),
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
                                    onLongClick = onCardLongClick
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
                            }
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
