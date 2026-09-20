package com.elvan.udukkai.ui.screens.recyclebin

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
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.mode.LocalAppMode
import com.elvan.udukkai.core.platform.AppBackHandler
import com.elvan.udukkai.core.platform.PlatformType
import com.elvan.udukkai.core.platform.currentPlatform
import com.elvan.udukkai.data.repository.PattiyalRepository
import com.elvan.udukkai.data.repository.PatrugalRepository
import com.elvan.udukkai.data.repository.PorulRepository
import com.elvan.udukkai.data.repository.VaangunarRepository
import com.elvan.udukkai.data.settings.NiruvanaTharavugalRepository
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
import com.elvan.udukkai.ui.screens.product.CoolieProductCard
import com.elvan.udukkai.ui.screens.product.SilkProductCard
import com.elvan.udukkai.ui.screens.create.components.ElvanPillShifter
import com.elvan.udukkai.ui.screens.create.components.CoolieInvoiceCard
import com.elvan.udukkai.ui.screens.create.components.SilkInvoiceCard
import com.elvan.udukkai.ui.screens.create.components.ReceiptCard
import com.elvan.udukkai.ui.screens.create.components.PillShifterItem
import com.elvan.udukkai.ui.screens.create.components.CreateDateFilterSheet
import com.elvan.udukkai.ui.screens.customer.CoolieCustomerCard
import com.elvan.udukkai.ui.screens.customer.SilkCustomerCard
import kotlinx.coroutines.launch

/**
 * MeetpagamScreen — One UI 7 / iOS 18 styled Recycle Bin screen.
 * Features:
 * - Floating bottom navigation bar matching outside design: Aakku (bills/receipts), Porul (products), Vaangunar (customers).
 * - Full native source cards with clear visual distinction.
 * - Multi-selection mode with Restore and Permanent Delete floating capsule bar.
 * - Selection mode stays open even if all items are deselected (until explicit back/cancel).
 * - Pure Tamil "கைவிடு" used for cancel throughout.
 * - Search and Date Filter in top bar, NO 3-dot overflow menu.
 */
@Composable
fun RecycleBinScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mode = LocalAppMode.current
    val colors = rememberShellColors()
    val isDark = colors.isDark
    val ff = LocalAppFontFamily.current
    val scope = rememberCoroutineScope()
    val isTamil = LocalAppLanguage.current.startsWith("ta")

    val restoreSuccessMsg = if (isTamil) "வெற்றிகரமாக மீட்டெடுக்கப்பட்டது" else "Restored successfully"
    val deleteSuccessMsg = if (isTamil) "நிரந்தரமாக அழிக்கப்பட்டது" else "Deleted permanently"

    // Auto-purge items older than 30 days on launch & load deleted items
    LaunchedEffect(mode) {
        PorulRepository.purgeExpired(days = 30, mode = mode)
        VaangunarRepository.purgeExpired(days = 30, mode = mode)
        PattiyalRepository.purgeExpired(days = 30, mode = mode)
        PatrugalRepository.purgeExpired(days = 30, mode = mode)

        PorulRepository.loadDeleted(mode)
        VaangunarRepository.loadDeleted(mode)
        PattiyalRepository.loadDeleted(mode)
        PatrugalRepository.loadDeleted(mode)
    }

    var selectedTab by remember { mutableStateOf(MeetpagamTab.Create) }
    var createSegment by remember { mutableIntStateOf(0) } // 0 = Invoices, 1 = Receipts
    var selectedProfileFilterIndex by remember { mutableIntStateOf(0) }

    val createScrollState = rememberLazyListState()
    val productsScrollState = rememberLazyListState()
    val customersScrollState = rememberLazyListState()

    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedItemIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var showBulkDeleteConfirm by remember { mutableStateOf(false) }
    var showDateFilterSheet by remember { mutableStateOf(false) }

    val isDesktop = currentPlatform == PlatformType.DESKTOP
    DisposableEffect(isDesktop) {
        if (!isDesktop) {
            ElvanSnackbar.isBottomBarVisible = true
        }
        onDispose {
            ElvanSnackbar.isBottomBarVisible = false
        }
    }

    // Reset selection & search when tab switches
    LaunchedEffect(selectedTab) {
        isSearchActive = false
        isSelectionMode = false
        selectedItemIds = emptySet()
        searchQuery = ""
    }

    // Company Profiles for Aakku tab
    val profiles = remember(mode) {
        NiruvanaTharavugalRepository.getAllProfiles(mode)
    }

    val allLabel = K.all.tr()
    val businessShifterItems = remember(profiles, allLabel) {
        val list = mutableListOf<PillShifterItem>()
        list.add(PillShifterItem(label = allLabel))
        profiles.forEach { p ->
            val shortName = p.kurumPeyar.ifEmpty {
                p.niruvanathinPeyar.values.firstOrNull().orEmpty()
            }
            list.add(PillShifterItem(label = shortName))
        }
        list
    }
    val safeProfileIndex = selectedProfileFilterIndex.coerceIn(0, (businessShifterItems.size - 1).coerceAtLeast(0))

    // Filtered data sets
    val allDeletedInvoices = PattiyalRepository.deletedInvoices
    val displayedInvoices = remember(
        allDeletedInvoices, safeProfileIndex, profiles, searchQuery,
        PattiyalRepository.startDateFilter, PattiyalRepository.endDateFilter
    ) {
        val q = searchQuery.trim().lowercase()
        val s = PattiyalRepository.startDateFilter
        val e = PattiyalRepository.endDateFilter
        val targetProfile = if (safeProfileIndex > 0) profiles.getOrNull(safeProfileIndex - 1) else null

        allDeletedInvoices.filter { invoice ->
            val matchesQuery = if (q.isEmpty()) true else {
                invoice.patrucheettuEn.lowercase().contains(q) ||
                invoice.vaangunarPeyar.values.any { it.lowercase().contains(q) } ||
                invoice.vaangunarMunvari.values.any { it.lowercase().contains(q) }
            }
            val matchesDate = when {
                s != null && e != null -> invoice.pattiyalNaal in s..e
                s != null -> invoice.pattiyalNaal >= s
                e != null -> invoice.pattiyalNaal <= e
                else -> true
            }
            val matchesProfile = if (targetProfile != null) invoice.niruvanamId == targetProfile.id else true
            matchesQuery && matchesDate && matchesProfile
        }
    }

    val allDeletedReceipts = PatrugalRepository.deletedReceipts
    val displayedReceipts = remember(
        allDeletedReceipts, safeProfileIndex, profiles, searchQuery,
        PatrugalRepository.startDateFilter, PatrugalRepository.endDateFilter
    ) {
        val q = searchQuery.trim().lowercase()
        val s = PatrugalRepository.startDateFilter
        val e = PatrugalRepository.endDateFilter
        val targetProfile = if (safeProfileIndex > 0) profiles.getOrNull(safeProfileIndex - 1) else null

        allDeletedReceipts.filter { receipt ->
            val matchesQuery = if (q.isEmpty()) true else {
                receipt.patruEn.lowercase().contains(q) ||
                receipt.vaangunarPeyar.values.any { it.lowercase().contains(q) } ||
                receipt.vaangunarMunvari.values.any { it.lowercase().contains(q) }
            }
            val matchesDate = when {
                s != null && e != null -> receipt.patruNaal in s..e
                s != null -> receipt.patruNaal >= s
                e != null -> receipt.patruNaal <= e
                else -> true
            }
            val matchesProfile = if (targetProfile != null) receipt.niruvanamId == targetProfile.id else true
            matchesQuery && matchesDate && matchesProfile
        }
    }

    val allDeletedProducts = PorulRepository.deletedItems
    val displayedProducts = remember(allDeletedProducts, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) allDeletedProducts else {
            allDeletedProducts.filter { item ->
                item.porulPeyar.values.any { it.lowercase().contains(q) } ||
                item.hsnCode.lowercase().contains(q)
            }
        }
    }

    val allDeletedCustomers = VaangunarRepository.deletedMerchants
    val displayedCustomers = remember(allDeletedCustomers, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) allDeletedCustomers else {
            allDeletedCustomers.filter { merchant ->
                merchant.peyar.values.any { it.lowercase().contains(q) } ||
                merchant.oor.values.any { it.lowercase().contains(q) } ||
                merchant.tholaipaesi.lowercase().contains(q)
            }
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
                            selectedItemIds.forEach { id -> PattiyalRepository.restore(id, mode) }
                        } else {
                            selectedItemIds.forEach { id -> PatrugalRepository.restore(id, mode) }
                        }
                    }
                    MeetpagamTab.Products -> {
                        selectedItemIds.forEach { id -> PorulRepository.restore(id, mode) }
                    }
                    MeetpagamTab.Customers -> {
                        selectedItemIds.forEach { id -> VaangunarRepository.restore(id, mode) }
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
                            selectedItemIds.forEach { id -> PattiyalRepository.permanentDelete(id, mode) }
                        } else {
                            selectedItemIds.forEach { id -> PatrugalRepository.permanentDelete(id, mode) }
                        }
                    }
                    MeetpagamTab.Products -> {
                        selectedItemIds.forEach { id -> PorulRepository.permanentDelete(id, mode) }
                    }
                    MeetpagamTab.Customers -> {
                        selectedItemIds.forEach { id -> VaangunarRepository.permanentDelete(id, mode) }
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
            icon = MaterialSymbols.Rounded.Description,
            activeIcon = MaterialSymbols.Rounded.DescriptionFill
        ),
        PillShifterItem(
            label = K.receipts.tr(),
            icon = MaterialSymbols.Rounded.ReceiptLong,
            activeIcon = MaterialSymbols.Rounded.ReceiptLongFill
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

            // Filter Icon Button (exclusive for Create/Aakku tab)
            if (selectedTab == MeetpagamTab.Create) {
                val isFilterActive = PattiyalRepository.isDateFilterActive || PatrugalRepository.isDateFilterActive
                ElvanTopBarIconButton(
                    onClick = { showDateFilterSheet = true }
                ) {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.FilterList,
                        contentDescription = "Filter",
                        tint = if (isFilterActive) colors.accent else colors.textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
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

                    // Pill Shifter for Invoices vs Receipts and Company Profiles
                    item(key = "meetpagam_top_shifters") {
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
                        }
                    }

                    if (createSegment == 0) {
                        if (displayedInvoices.isEmpty()) {
                            item(key = "empty_invoices") {
                                EmptyMeetpagamState(
                                    colors = colors,
                                    isTamil = isTamil,
                                    emptyTitle = if (isTamil) "நீக்கப்பட்ட பட்டியல்கள் இல்லை" else "No deleted invoices"
                                )
                            }
                        } else {
                            itemsIndexed(displayedInvoices, key = { _, inv -> inv.id }) { index, invoice ->
                                val isSelected = selectedItemIds.contains(invoice.id)
                                val onCardClick = {
                                    if (isSelectionMode) onToggleItem(invoice.id) else onStartSelection(invoice.id)
                                }
                                val onCardLongClick = { onStartSelection(invoice.id) }

                                if (mode == AppMode.PATTU) {
                                    SilkInvoiceCard(
                                        index = index + 1,
                                        pattiyal = invoice,
                                        colors = colors,
                                        isSelectionMode = isSelectionMode,
                                        isSelected = isSelected,
                                        onClick = onCardClick,
                                        onLongClick = onCardLongClick,
                                        modifier = Modifier.padding(horizontal = Dimens.ContentPadding)
                                    )
                                } else {
                                    CoolieInvoiceCard(
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
                        }
                    } else {
                        if (displayedReceipts.isEmpty()) {
                            item(key = "empty_receipts") {
                                EmptyMeetpagamState(
                                    colors = colors,
                                    isTamil = isTamil,
                                    emptyTitle = if (isTamil) "நீக்கப்பட்ட பற்றுச்சீட்டுகள் இல்லை" else "No deleted receipts"
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
                                isTamil = isTamil,
                                emptyTitle = if (isTamil) "நீக்கப்பட்ட பொருட்கள் இல்லை" else "No deleted products"
                            )
                        }
                    } else {
                        itemsIndexed(displayedProducts, key = { _, item -> item.id }) { index, item ->
                            val isSelected = selectedItemIds.contains(item.id)
                            val onCardClick = {
                                if (isSelectionMode) onToggleItem(item.id) else onStartSelection(item.id)
                            }
                            val onCardLongClick = { onStartSelection(item.id) }

                            if (mode == AppMode.KOOLI) {
                                CoolieProductCard(
                                    index = index + 1,
                                    porul = item,
                                    onClick = onCardClick,
                                    onLongClick = onCardLongClick,
                                    colors = colors,
                                    isSelectionMode = isSelectionMode,
                                    isSelected = isSelected
                                )
                            } else {
                                SilkProductCard(
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
                                isTamil = isTamil,
                                emptyTitle = if (isTamil) "நீக்கப்பட்ட வாங்குநர்கள் இல்லை" else "No deleted customers"
                            )
                        }
                    } else {
                        itemsIndexed(displayedCustomers, key = { _, merchant -> merchant.id }) { index, merchant ->
                            val isSelected = selectedItemIds.contains(merchant.id)
                            val onCardClick = {
                                if (isSelectionMode) onToggleItem(merchant.id) else onStartSelection(merchant.id)
                            }
                            val onCardLongClick = { onStartSelection(merchant.id) }

                            if (mode == AppMode.KOOLI) {
                                CoolieCustomerCard(
                                    index = index + 1,
                                    merchant = merchant,
                                    onClick = onCardClick,
                                    onLongClick = onCardLongClick,
                                    colors = colors,
                                    isSelectionMode = isSelectionMode,
                                    isSelected = isSelected
                                )
                            } else {
                                SilkCustomerCard(
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
    }

    // Confirmation Sheet for Permanent Delete
    if (showBulkDeleteConfirm) {
        ElvanActionSheet(
            title = if (isTamil) "முழுமையாக நீக்கவா?" else "Delete permanently?",
            onDismissRequest = { showBulkDeleteConfirm = false },
            onConfirm = onPermanentDeleteSelected,
            cancelText = if (isTamil) "கைவிடு" else "Cancel",
            confirmText = if (isTamil) "நீக்கவும்" else "Delete",
            confirmColor = colors.textPrimary,
            customContent = {
                Text(
                    text = if (isTamil)
                        "தேர்வு செய்யப்பட்டவை நிரந்தரமாக அழிக்கப்படும். இதை திரும்பப் பெற முடியாது."
                    else
                        "Selected items will be permanently erased. This action cannot be undone.",
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

    // Date Filter Bottom Sheet for Create tab
    if (showDateFilterSheet) {
        CreateDateFilterSheet(
            isOpen = showDateFilterSheet,
            currentStartMillis = if (createSegment == 0) PattiyalRepository.startDateFilter else PatrugalRepository.startDateFilter,
            currentEndMillis = if (createSegment == 0) PattiyalRepository.endDateFilter else PatrugalRepository.endDateFilter,
            onDismissRequest = { showDateFilterSheet = false },
            onApplyFilter = { start, end, _ ->
                if (createSegment == 0) {
                    PattiyalRepository.setDateRange(start, end)
                } else {
                    PatrugalRepository.setDateRange(start, end)
                }
                showDateFilterSheet = false
            },
            colors = colors
        )
    }
}

@Composable
private fun EmptyMeetpagamState(
    colors: ShellColors,
    isTamil: Boolean,
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
                text = (if (isTamil)
                    "நீக்கப்பட்ட உருப்படிகள் 30 நாட்களுக்குப் பிறகு தானாகவே அழிக்கப்படும்"
                else
                    "Deleted items will be automatically erased after 30 days").preventBrokenLigatures(),
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
