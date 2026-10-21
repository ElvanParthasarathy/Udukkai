package com.elvan.udukkai.textiles.ui.screens

import com.elvan.udukkai.ui.screens.recyclebin.MeetpagamTab
import com.elvan.udukkai.ui.screens.recyclebin.RecycleBinBottomNavBar
import com.elvan.udukkai.ui.screens.recyclebin.RecycleBinSelectionBar

import androidx.compose.animation.*
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.platform.AppBackHandler
import com.elvan.udukkai.core.platform.PlatformType
import com.elvan.udukkai.core.platform.currentPlatform
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.LocalAppLanguage
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.ElvanActionSheet
import com.elvan.udukkai.ui.components.shell.ElvanShell
import com.elvan.udukkai.ui.components.shell.ElvanSnackbar
import com.elvan.udukkai.ui.components.shell.ElvanTopBarIconButton
import com.elvan.udukkai.ui.components.shell.LocalElvanTopSpacerHeight
import com.elvan.udukkai.ui.navigation.ElvanSearchBar
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.product.TextilesProductCard
import com.elvan.udukkai.ui.screens.create.components.ElvanPillShifter
import com.elvan.udukkai.ui.screens.create.components.TextilesInvoiceCard
import com.elvan.udukkai.ui.screens.create.components.ReceiptCard
import com.elvan.udukkai.ui.screens.create.components.PillShifterItem
import com.elvan.udukkai.ui.screens.customer.TextilesCustomerCard
import kotlinx.coroutines.launch

/**
 * MeetpagamScreen — One UI 7 / iOS 18 styled Recycle Bin screen.
 * Features:
 * - Floating bottom navigation bar matching outside design: Aakku (bills/receipts), Porul (products), Vaangunar (customers).
 * - Full native source cards with clear visual distinction.
 * - Multi-selection mode with Restore and Permanent Delete floating capsule bar.
 * - Selection mode stays open even if all items are deselected (until explicit back/cancel).
 * - Pure Tamil "கைவிடு" used for cancel throughout.
 * - Search in top bar, NO 3-dot overflow menu.
 */
@Composable
fun TextilesRecycleBinScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = rememberShellColors()
    val isDark = colors.isDark
    val ff = LocalAppFontFamily.current
    val scope = rememberCoroutineScope()
    val restoreSuccessMsg = K.restoredSuccessfully.tr()
    val deleteSuccessMsg = K.deletedPermanently.tr()

    // Auto-purge items older than 30 days on launch & load deleted items
    LaunchedEffect(Unit) {
        com.elvan.udukkai.textiles.data.TextilesProductRepository.purgeExpired(days = 30)
        com.elvan.udukkai.textiles.data.TextilesCustomerRepository.purgeExpired(days = 30)
        com.elvan.udukkai.textiles.data.TextilesInvoiceRepository.purgeExpired(days = 30)
        com.elvan.udukkai.textiles.data.TextilesReceiptRepository.purgeExpired(days = 30)

        com.elvan.udukkai.textiles.data.TextilesProductRepository.loadDeleted()
        com.elvan.udukkai.textiles.data.TextilesCustomerRepository.loadDeleted()
        com.elvan.udukkai.textiles.data.TextilesInvoiceRepository.loadDeleted()
        com.elvan.udukkai.textiles.data.TextilesReceiptRepository.loadDeleted()
    }

    var selectedTab by remember { mutableStateOf(MeetpagamTab.Create) }
    var createSegment by remember { mutableIntStateOf(0) } // 0 = Invoices, 1 = Receipts

    val createScrollState = rememberLazyListState()
    val productsScrollState = rememberLazyListState()
    val customersScrollState = rememberLazyListState()

    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedItemIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var showBulkDeleteConfirm by remember { mutableStateOf(false) }

    val isDesktop = currentPlatform == PlatformType.DESKTOP

    // Reset selection & search when tab switches
    LaunchedEffect(selectedTab) {
        isSearchActive = false
        isSelectionMode = false
        selectedItemIds = emptySet()
        searchQuery = ""
    }

    // Active Company Profile for workspace isolation
    val activeProfile = com.elvan.udukkai.textiles.data.TextilesProfileRepository.profile
    val activeId = activeProfile.id

    // Filtered data sets scoped to active company and search query (sorted by deleted_at DESC)
    val allDeletedInvoices = com.elvan.udukkai.textiles.data.TextilesInvoiceRepository.deletedInvoices
    val displayedInvoices = remember(allDeletedInvoices, activeId, searchQuery) {
        allDeletedInvoices.filter { invoice ->
            val matchesQuery = com.elvan.udukkai.core.utils.SearchHelper.matchesInvoice(invoice, searchQuery)
            val matchesProfile = if (activeId != null) invoice.companyId == activeId else true
            matchesQuery && matchesProfile
        }
    }

    val allDeletedReceipts = com.elvan.udukkai.textiles.data.TextilesReceiptRepository.deletedReceipts
    val displayedReceipts = remember(allDeletedReceipts, activeId, searchQuery) {
        allDeletedReceipts.filter { receipt ->
            val matchesQuery = com.elvan.udukkai.core.utils.SearchHelper.matchesReceipt(receipt, searchQuery)
            val matchesProfile = if (activeId != null) receipt.companyId == activeId else true
            matchesQuery && matchesProfile
        }
    }

    val allDeletedProducts = com.elvan.udukkai.textiles.data.TextilesProductRepository.deletedProducts
    val displayedProducts = remember(allDeletedProducts, activeId, searchQuery) {
        allDeletedProducts.filter { item ->
            val matchesCompany = if (activeId != null) item.isAvailableForCompany(activeId) else true
            val matchesQuery = com.elvan.udukkai.core.utils.SearchHelper.matchesProduct(item, searchQuery)
            matchesCompany && matchesQuery
        }
    }

    val allDeletedCustomers = com.elvan.udukkai.textiles.data.TextilesCustomerRepository.deletedCustomers
    val displayedCustomers = remember(allDeletedCustomers, searchQuery) {
        allDeletedCustomers.filter { merchant ->
            com.elvan.udukkai.core.utils.SearchHelper.matchesCustomer(merchant, searchQuery)
        }
    }

    // Active selection set
    val allSelectionIds: Set<Long> = when (selectedTab) {
        MeetpagamTab.Create -> {
            if (createSegment == 0) displayedInvoices.map { it.id }.toSet()
            else displayedReceipts.map { it.id }.toSet()
        }
        MeetpagamTab.Products -> displayedProducts.map { it.id }.toSet()
        MeetpagamTab.Customers -> displayedCustomers.map { it.id }.toSet()
    }
    val isAllSelected = selectedItemIds.isNotEmpty() && allSelectionIds.isNotEmpty() && selectedItemIds.size == allSelectionIds.size

    val onToggleItem: (Long) -> Unit = { id ->
        selectedItemIds = if (selectedItemIds.contains(id)) selectedItemIds - id else selectedItemIds + id
    }
    val onStartSelection: (Long) -> Unit = { id ->
        isSelectionMode = true
        selectedItemIds = setOf(id)
    }

    // System Back handling
    AppBackHandler(enabled = isSelectionMode || isSearchActive || showBulkDeleteConfirm) {
        if (showBulkDeleteConfirm) {
            showBulkDeleteConfirm = false
        } else if (isSelectionMode) {
            isSelectionMode = false
            selectedItemIds = emptySet()
        } else if (isSearchActive) {
            isSearchActive = false
            searchQuery = ""
        }
    }

    val onRestoreSelected: () -> Unit = {
        if (selectedItemIds.isNotEmpty()) {
            scope.launch {
                when (selectedTab) {
                    MeetpagamTab.Create -> {
                        if (createSegment == 0) {
                            selectedItemIds.forEach { id -> com.elvan.udukkai.textiles.data.TextilesInvoiceRepository.restore(id) }
                        } else {
                            selectedItemIds.forEach { id -> com.elvan.udukkai.textiles.data.TextilesReceiptRepository.restore(id) }
                        }
                    }
                    MeetpagamTab.Products -> {
                        selectedItemIds.forEach { id -> com.elvan.udukkai.textiles.data.TextilesProductRepository.restore(id) }
                    }
                    MeetpagamTab.Customers -> {
                        selectedItemIds.forEach { id -> com.elvan.udukkai.textiles.data.TextilesCustomerRepository.restore(id) }
                    }
                }
                ElvanSnackbar.show(restoreSuccessMsg)
                isSelectionMode = false
                selectedItemIds = emptySet()
            }
        }
    }

    val onPermanentDeleteSelected: () -> Unit = {
        if (selectedItemIds.isNotEmpty()) {
            scope.launch {
                when (selectedTab) {
                    MeetpagamTab.Create -> {
                        if (createSegment == 0) {
                            selectedItemIds.forEach { id -> com.elvan.udukkai.textiles.data.TextilesInvoiceRepository.permanentDelete(id) }
                        } else {
                            selectedItemIds.forEach { id -> com.elvan.udukkai.textiles.data.TextilesReceiptRepository.permanentDelete(id) }
                        }
                    }
                    MeetpagamTab.Products -> {
                        selectedItemIds.forEach { id -> com.elvan.udukkai.textiles.data.TextilesProductRepository.permanentDelete(id) }
                    }
                    MeetpagamTab.Customers -> {
                        selectedItemIds.forEach { id -> com.elvan.udukkai.textiles.data.TextilesCustomerRepository.permanentDelete(id) }
                    }
                }
                ElvanSnackbar.show(deleteSuccessMsg)
                showBulkDeleteConfirm = false
                isSelectionMode = false
                selectedItemIds = emptySet()
            }
        }
    }

    val currentScrollState = when (selectedTab) {
        MeetpagamTab.Create -> createScrollState
        MeetpagamTab.Products -> productsScrollState
        MeetpagamTab.Customers -> customersScrollState
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

    ElvanShell(
        scrollState = currentScrollState,
        title = K.recycleBin.tr(),
        onBack = onBack,
        showNavbar = true,
        hasActions = !isSearchActive && !isSelectionMode,
        isSearchActive = isSearchActive,
        isSelectionMode = isSelectionMode,
        selectedCount = selectedItemIds.size,
        isAllSelected = isAllSelected,
        onSelectAll = {
            selectedItemIds = if (selectedItemIds.size == allSelectionIds.size) emptySet() else allSelectionIds
        },
        onCancelSelection = {
            isSelectionMode = false
            selectedItemIds = emptySet()
        },
        actions = {
            // Search Icon Button
            ElvanTopBarIconButton(
                onClick = { isSearchActive = true }
            ) {
                Icon(
                    imageVector = MaterialSymbols.Rounded.Search,
                    contentDescription = K.search.tr(),
                    tint = colors.textPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        },
        navbar = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = isSelectionMode,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(220, easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f))) +
                         scaleIn(initialScale = 0.94f, animationSpec = tween(220))) togetherWith
                        (fadeOut(animationSpec = tween(160, easing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f))) +
                         scaleOut(targetScale = 0.94f, animationSpec = tween(160)))
                    },
                    label = "recycleBinBottomBarSelectionCrossfade"
                ) { inSelection ->
                    if (inSelection) {
                        RecycleBinSelectionBar(
                            visible = true,
                            selectedCount = selectedItemIds.size,
                            onRestore = onRestoreSelected,
                            onPermanentDelete = {
                                if (selectedItemIds.isNotEmpty()) {
                                    showBulkDeleteConfirm = true
                                }
                            },
                            colors = colors
                        )
                    } else {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RecycleBinBottomNavBar(
                                selectedTab = selectedTab,
                                onTabSelected = { tab ->
                                    if (selectedTab != tab) {
                                        selectedTab = tab
                                    }
                                },
                                hideContent = isSearchActive
                            )

                            ElvanSearchBar(
                                visible = isSearchActive,
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                                onClose = {
                                    isSearchActive = false
                                    searchQuery = ""
                                },
                                colors = colors
                            )
                        }
                    }
                }
            }
        }
    ) {
        when (selectedTab) {
            MeetpagamTab.Create -> {
                LazyColumn(
                    state = createScrollState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 0.dp, top = 0.dp, end = 0.dp, bottom = Dimens.ContentPaddingBottom),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item(key = "meetpagam_create_top_spacer") {
                        Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
                    }

                    // Pill Shifter for Invoices vs Receipts
                    item(key = "meetpagam_top_shifter") {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            ElvanPillShifter(
                                items = shifterItems,
                                selectedIndex = createSegment,
                                onIndexSelected = {
                                    createSegment = it
                                    isSelectionMode = false
                                    selectedItemIds = emptySet()
                                },
                                colors = colors,
                                isFullWidth = false
                            )
                        }
                    }

                    if (createSegment == 0) {
                        if (displayedInvoices.isEmpty()) {
                            item(key = "empty_invoices") {
                                EmptyMeetpagamState(
                                    colors = colors,
                                    emptyTitle = K.noDeletedInvoices.tr()
                                )
                            }
                        } else {
                            itemsIndexed(displayedInvoices, key = { _, inv -> inv.id }) { index, invoice ->
                                val isSelected = selectedItemIds.contains(invoice.id)
                                val onCardClick = {
                                    if (isSelectionMode) onToggleItem(invoice.id) else onStartSelection(invoice.id)
                                }
                                val onCardLongClick = { onStartSelection(invoice.id) }

                                TextilesInvoiceCard(
                                    index = index + 1,
                                    pattiyal = invoice,
                                    colors = colors,
                                    isSelectionMode = isSelectionMode,
                                    isSelected = isSelected,
                                    onClick = onCardClick,
                                    onLongClick = onCardLongClick,
                                    modifier = Modifier.padding(horizontal = Dimens.ContentPadding)
                                )
                            }
                        }
                    } else {
                        if (displayedReceipts.isEmpty()) {
                            item(key = "empty_receipts") {
                                EmptyMeetpagamState(
                                    colors = colors,
                                    emptyTitle = K.noDeletedReceipts.tr()
                                )
                            }
                        } else {
                            itemsIndexed(displayedReceipts, key = { _, rec -> rec.id }) { index, receipt ->
                                val isSelected = selectedItemIds.contains(receipt.id)
                                val onCardClick = {
                                    if (isSelectionMode) onToggleItem(receipt.id) else onStartSelection(receipt.id)
                                }
                                val onCardLongClick = { onStartSelection(receipt.id) }

                                ReceiptCard(
                                    index = index + 1,
                                    receipt = receipt,
                                    colors = colors,
                                    isSelectionMode = isSelectionMode,
                                    isSelected = isSelected,
                                    onClick = onCardClick,
                                    onLongClick = onCardLongClick,
                                    modifier = Modifier.padding(horizontal = Dimens.ContentPadding)
                                )
                            }
                        }
                    }
                }
            }

            MeetpagamTab.Products -> {
                LazyColumn(
                    state = productsScrollState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = Dimens.ContentPadding, top = 0.dp, end = Dimens.ContentPadding, bottom = Dimens.ContentPaddingBottom),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item(key = "meetpagam_products_top_spacer") {
                        Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
                    }

                    if (displayedProducts.isEmpty()) {
                        item(key = "empty_products") {
                            EmptyMeetpagamState(
                                colors = colors,
                                emptyTitle = K.noDeletedProducts.tr()
                            )
                        }
                    } else {
                        itemsIndexed(displayedProducts, key = { _, item -> item.id }) { index, item ->
                            val isSelected = selectedItemIds.contains(item.id)
                            val onCardClick = {
                                if (isSelectionMode) onToggleItem(item.id) else onStartSelection(item.id)
                            }
                            val onCardLongClick = { onStartSelection(item.id) }

                            TextilesProductCard(
                                index = index + 1,
                                porul = item,
                                onClick = onCardClick,
                                onLongClick = onCardLongClick,
                                colors = colors,
                                isSelectionMode = isSelectionMode,
                                isSelected = isSelected
                            )
                        }
                    }
                }
            }

            MeetpagamTab.Customers -> {
                LazyColumn(
                    state = customersScrollState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = Dimens.ContentPadding, top = 0.dp, end = Dimens.ContentPadding, bottom = Dimens.ContentPaddingBottom),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item(key = "meetpagam_customers_top_spacer") {
                        Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
                    }

                    if (displayedCustomers.isEmpty()) {
                        item(key = "empty_customers") {
                            EmptyMeetpagamState(
                                colors = colors,
                                emptyTitle = K.noDeletedCustomers.tr()
                            )
                        }
                    } else {
                        itemsIndexed(displayedCustomers, key = { _, merchant -> merchant.id }) { index, merchant ->
                            val isSelected = selectedItemIds.contains(merchant.id)
                            val onCardClick = {
                                if (isSelectionMode) onToggleItem(merchant.id) else onStartSelection(merchant.id)
                            }
                            val onCardLongClick = { onStartSelection(merchant.id) }

                            TextilesCustomerCard(
                                index = index + 1,
                                merchant = merchant,
                                onClick = onCardClick,
                                onLongClick = onCardLongClick,
                                colors = colors,
                                isSelectionMode = isSelectionMode,
                                isSelected = isSelected
                            )
                        }
                    }
                }
            }
        }
    }

    // Confirmation Sheet for Permanent Delete
    if (showBulkDeleteConfirm) {
        ElvanActionSheet(
            title = K.deletePermanentlyQuestion.tr(),
            onDismissRequest = { showBulkDeleteConfirm = false },
            onConfirm = onPermanentDeleteSelected,
            cancelText = K.cancelBtn.tr(),
            confirmText = K.deleteBtn.tr(),
            confirmColor = colors.textPrimary,
            customContent = {
                Text(
                    text = K.deletePermanentlyDesc.tr(),
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 14.sp,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }
        )
    }
}

@Composable
private fun EmptyMeetpagamState(
    colors: ShellColors,
    emptyTitle: String,
    modifier: Modifier = Modifier
) {
    val ff = LocalAppFontFamily.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 60.dp, bottom = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(colors.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = MaterialSymbols.Rounded.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = colors.textSecondary.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = emptyTitle.preventBrokenLigatures(),
                style = TextStyle(
                    fontFamily = ff,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = K.recycleBin30Days.tr().preventBrokenLigatures(),
                style = TextStyle(
                    fontFamily = ff,
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}
