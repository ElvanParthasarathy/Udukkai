package com.elvan.udukkai.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.core.mode.LocalAppMode
import com.elvan.udukkai.core.platform.AppBackHandler
import com.elvan.udukkai.core.platform.PlatformType
import com.elvan.udukkai.core.platform.currentPlatform
import com.elvan.udukkai.ui.screens.home.desktop.DesktopSideBar
import com.elvan.udukkai.data.model.PatrugalTharavuru
import com.elvan.udukkai.data.model.PattiyalTharavuru
import com.elvan.udukkai.data.model.PorulTharavuru
import com.elvan.udukkai.data.model.VaangunarTharavuru
import com.elvan.udukkai.data.repository.PattiyalRepository
import com.elvan.udukkai.data.repository.PatrugalRepository
import com.elvan.udukkai.data.repository.PorulRepository
import com.elvan.udukkai.data.repository.VaangunarRepository
import com.elvan.udukkai.data.settings.NiruvanaTharavugalRepository
import com.elvan.udukkai.data.mock.SodhanaiTharavuUruvakki
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.LanguageManager
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.ExpressivePullToRefreshBox
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.BottomNavBar
import com.elvan.udukkai.ui.navigation.ElvanSearchBar
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.navigation.NavTab
import com.elvan.udukkai.ui.screens.recyclebin.RecycleBinScreen
import com.elvan.udukkai.ui.screens.product.ProductScreen
import com.elvan.udukkai.ui.screens.view.ReceiptViewScreen
import com.elvan.udukkai.ui.screens.view.InvoiceViewScreen
import com.elvan.udukkai.ui.screens.view.ProductViewScreen
import com.elvan.udukkai.ui.screens.view.CustomerViewScreen
import com.elvan.udukkai.ui.screens.settings.SettingsScreen
import com.elvan.udukkai.ui.screens.editor.receipt.ReceiptEditorScreen
import com.elvan.udukkai.ui.screens.editor.invoice.CoolieInvoiceEditorScreen
import com.elvan.udukkai.ui.screens.editor.invoice.InvoiceEditorScreen
import com.elvan.udukkai.ui.screens.editor.invoice.SilkInvoiceEditorScreen
import com.elvan.udukkai.ui.screens.editor.product.ProductEditorScreen
import com.elvan.udukkai.ui.screens.editor.customer.CustomerEditorScreen
import androidx.compose.foundation.shape.CircleShape
import com.elvan.udukkai.ui.screens.create.CreateScreen
import com.elvan.udukkai.ui.screens.create.components.CreateDateFilterSheet
import com.elvan.udukkai.ui.screens.customer.CustomerScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class ActiveSubpage {
    data object Settings : ActiveSubpage()
    data object RecycleBin : ActiveSubpage()
    data class ItemEditor(val item: PorulTharavuru? = null) : ActiveSubpage()
    data class MerchantEditor(val merchant: VaangunarTharavuru? = null) : ActiveSubpage()
    data class InvoiceEditor(val invoice: PattiyalTharavuru? = null) : ActiveSubpage()
    data class ReceiptEditor(val receipt: PatrugalTharavuru? = null) : ActiveSubpage()
    data class CustomerView(val customer: VaangunarTharavuru) : ActiveSubpage()
    data class ProductView(val product: PorulTharavuru) : ActiveSubpage()
    data class InvoiceView(val invoice: PattiyalTharavuru) : ActiveSubpage()
    data class ReceiptView(val receipt: PatrugalTharavuru) : ActiveSubpage()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val currentMode = LocalAppMode.current
    val ff = LocalAppFontFamily.current

    val homeScrollState = rememberLazyListState()
    val createScrollState = rememberLazyListState()
    val productsScrollState = rememberLazyListState()
    val customersScrollState = rememberLazyListState()

    var selectedTab by remember { mutableStateOf(NavTab.Home) }
    var uruvakkuSegment by remember { mutableStateOf(0) } // 0 = Invoices, 1 = Receipts
    var activeSubpage by remember { mutableStateOf<ActiveSubpage?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedItemIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var showBulkDeleteConfirm by remember { mutableStateOf(false) }
    var showDateFilterSheet by remember { mutableStateOf(false) }

    val onToggleItem: (Long) -> Unit = { id ->
        val newSet = if (selectedItemIds.contains(id)) selectedItemIds - id else selectedItemIds + id
        selectedItemIds = newSet
    }
    val onStartSelection: (Long) -> Unit = { id ->
        isSelectionMode = true
        selectedItemIds = setOf(id)
    }

    val copyInvoiceAndEdit: (PattiyalTharavuru) -> Unit = { invoice ->
        val copied = invoice.copy(
            id = 0L,
            patrucheettuEn = "",
            pattiyalNaal = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        isSelectionMode = false
        selectedItemIds = emptySet()
        activeSubpage = ActiveSubpage.InvoiceEditor(copied)
    }

    val copyProductAndEdit: (PorulTharavuru) -> Unit = { item ->
        val isTamil = LanguageManager.activeLanguageCode.startsWith("ta")
        val copySuffix = if (isTamil) " (நகல்)" else " (Copy)"
        val copied = item.copy(
            id = 0L,
            porulPeyar = item.porulPeyar.mapValues { "${it.value}$copySuffix" },
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        isSelectionMode = false
        selectedItemIds = emptySet()
        activeSubpage = ActiveSubpage.ItemEditor(copied)
    }

    val copyCustomerAndEdit: (VaangunarTharavuru) -> Unit = { merchant ->
        val isTamil = LanguageManager.activeLanguageCode.startsWith("ta")
        val copySuffix = if (isTamil) " (நகல்)" else " (Copy)"
        val copied = merchant.copy(
            id = 0L,
            peyar = merchant.peyar.mapValues { "${it.value}$copySuffix" },
            gstin = "",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        isSelectionMode = false
        selectedItemIds = emptySet()
        activeSubpage = ActiveSubpage.MerchantEditor(copied)
    }

    val scope = rememberCoroutineScope()
    val colors = rememberShellColors()

    val porulDeletedMsg = K.productDeleted.tr()
    val vaangunarDeletedMsg = K.customerDeleted.tr()
    val pattiyalgalLabel = K.invoices.tr()
    val patrucheettugalLabel = K.receipts.tr()
    val azhikkiradhuLabel = K.erasing.tr()

    // Intercept hardware/system back when a subpage is open, selection mode is active, or search is active
    AppBackHandler(enabled = activeSubpage != null || isSelectionMode || isSearchActive || showBulkDeleteConfirm) {
        if (showBulkDeleteConfirm) {
            showBulkDeleteConfirm = false
        } else if (activeSubpage != null) {
            activeSubpage = null
        } else if (isSelectionMode) {
            isSelectionMode = false
            selectedItemIds = emptySet()
        } else if (isSearchActive) {
            isSearchActive = false
            PattiyalRepository.searchQuery = ""
            PatrugalRepository.searchQuery = ""
            PorulRepository.searchQuery = ""
            VaangunarRepository.searchQuery = ""
        }
    }

    // Load all repositories on launch and when currentMode changes
    LaunchedEffect(currentMode) {
        PattiyalRepository.loadAll(currentMode)
        PatrugalRepository.loadAll(currentMode)
        PorulRepository.loadAll(currentMode)
        VaangunarRepository.loadAll(currentMode)
        NiruvanaTharavugalRepository.refreshFromDatabase()

        if (PattiyalRepository.invoices.isEmpty() && VaangunarRepository.merchants.isEmpty()) {
            SodhanaiTharavuUruvakki.seedAllData()
        }
    }

    // Reset search and selection state on tab switch
    LaunchedEffect(selectedTab) {
        isSearchActive = false
        isSelectionMode = false
        selectedItemIds = emptySet()
        PattiyalRepository.searchQuery = ""
        PatrugalRepository.searchQuery = ""
        PorulRepository.searchQuery = ""
        VaangunarRepository.searchQuery = ""
    }

    val currentScrollState = when (selectedTab) {
        NavTab.Home -> homeScrollState
        NavTab.Create -> createScrollState
        NavTab.Products -> productsScrollState
        NavTab.Customers -> customersScrollState
    }

    val currentSearchQuery = when (selectedTab) {
        NavTab.Home -> ""
        NavTab.Create -> if (uruvakkuSegment == 0) PattiyalRepository.searchQuery else PatrugalRepository.searchQuery
        NavTab.Products -> PorulRepository.searchQuery
        NavTab.Customers -> VaangunarRepository.searchQuery
    }

    val isDesktop = currentPlatform == PlatformType.DESKTOP

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        val isWideScreen = isDesktop && maxWidth >= 768.dp

        LaunchedEffect(activeSubpage, isWideScreen) {
            ElvanSnackbar.isBottomBarVisible = (activeSubpage == null || activeSubpage is ActiveSubpage.RecycleBin) && !isWideScreen
        }
        DisposableEffect(Unit) {
            onDispose { ElvanSnackbar.isBottomBarVisible = false }
        }

        Row(modifier = Modifier.fillMaxSize()) {
            if (isWideScreen) {
                DesktopSideBar(
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        selectedTab = tab
                        activeSubpage = null
                    },
                    onSettingsClick = {
                        activeSubpage = ActiveSubpage.Settings
                    },
                    colors = colors
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                AnimatedContent(
                    targetState = activeSubpage,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.background),
            transitionSpec = {
                if (targetState != null) {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioNoBouncy)
                    ) togetherWith fadeOut(targetAlpha = 0.9f, animationSpec = tween(durationMillis = 50))
                } else {
                    fadeIn(initialAlpha = 0.9f) togetherWith slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioNoBouncy)
                    )
                }
            },
            label = "HomeToSubpageTransition"
        ) { subpage ->
            when (subpage) {
                is ActiveSubpage.Settings -> {
                    SettingsScreen(
                        onBack = { activeSubpage = null }
                    )
                }
                is ActiveSubpage.RecycleBin -> {
                    RecycleBinScreen(
                        onBack = { activeSubpage = null }
                    )
                }
                is ActiveSubpage.ItemEditor -> {
                    ProductEditorScreen(
                        item = subpage.item,
                        onBack = { activeSubpage = null }
                    )
                }
                is ActiveSubpage.MerchantEditor -> {
                    CustomerEditorScreen(
                        merchant = subpage.merchant,
                        onBack = { activeSubpage = null }
                    )
                }
                is ActiveSubpage.InvoiceEditor -> {
                    if (currentMode == com.elvan.udukkai.core.mode.AppMode.PATTU) {
                        SilkInvoiceEditorScreen(
                            invoice = subpage.invoice,
                            onBack = { activeSubpage = null },
                            onRequestAddNewCustomer = {
                                activeSubpage = ActiveSubpage.MerchantEditor(null)
                            },
                            onRequestAddNewProduct = {
                                activeSubpage = ActiveSubpage.ItemEditor(null)
                            }
                        )
                    } else {
                        CoolieInvoiceEditorScreen(
                            invoice = subpage.invoice,
                            onBack = { activeSubpage = null },
                            onRequestAddNewCustomer = {
                                activeSubpage = ActiveSubpage.MerchantEditor(null)
                            },
                            onRequestAddNewProduct = {
                                activeSubpage = ActiveSubpage.ItemEditor(null)
                            }
                        )
                    }
                }
                is ActiveSubpage.ReceiptEditor -> {
                    ReceiptEditorScreen(
                        receipt = subpage.receipt,
                        onBack = { activeSubpage = null }
                    )
                }
                is ActiveSubpage.CustomerView -> {
                    CustomerViewScreen(
                        merchant = subpage.customer,
                        onBack = { activeSubpage = null },
                        onEdit = {
                            activeSubpage = ActiveSubpage.MerchantEditor(subpage.customer)
                        },
                        onCopy = {
                            copyCustomerAndEdit(subpage.customer)
                        }
                    )
                }
                is ActiveSubpage.ProductView -> {
                    ProductViewScreen(
                        item = subpage.product,
                        onBack = { activeSubpage = null },
                        onEdit = {
                            activeSubpage = ActiveSubpage.ItemEditor(subpage.product)
                        },
                        onCopy = {
                            copyProductAndEdit(subpage.product)
                        }
                    )
                }
                is ActiveSubpage.InvoiceView -> {
                    InvoiceViewScreen(
                        invoice = subpage.invoice,
                        onBack = { activeSubpage = null },
                        onEdit = {
                            activeSubpage = ActiveSubpage.InvoiceEditor(subpage.invoice)
                        },
                        onCopy = {
                            copyInvoiceAndEdit(subpage.invoice)
                        }
                    )
                }
                is ActiveSubpage.ReceiptView -> {
                    ReceiptViewScreen(
                        receipt = subpage.receipt,
                        onBack = { activeSubpage = null },
                        onEdit = {
                            activeSubpage = ActiveSubpage.ReceiptEditor(subpage.receipt)
                        }
                    )
                }
                null -> {
                    val allSelectionIds: Set<Long> = when (selectedTab) {
                        NavTab.Products -> PorulRepository.filteredItems.map { it.id }.toSet()
                        NavTab.Customers -> VaangunarRepository.filteredMerchants.map { it.id }.toSet()
                        NavTab.Create -> {
                            if (uruvakkuSegment == 0) {
                                PattiyalRepository.filteredInvoices.map { it.id }.toSet()
                            } else {
                                PatrugalRepository.filteredReceipts.map { it.id }.toSet()
                            }
                        }
                        else -> emptySet()
                    }
                    val isAllSelected = selectedItemIds.isNotEmpty() && allSelectionIds.isNotEmpty() && selectedItemIds.size == allSelectionIds.size

                    ElvanShell(
                        scrollState = currentScrollState,
                        title = selectedTab.getLocalizedHeader(),
                        showNavbar = !isWideScreen,
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
                            if (selectedTab != NavTab.Home) {
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
                            }

                            // Filter Circle Button (exclusive for Uruvakku: Search -> Filter -> 3-Dot)
                            if (selectedTab == NavTab.Create) {
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

                            // 3-Dot More Menu (மேலும்)
                            Box {
                                ElvanTopBarIconButton(
                                    onClick = {
                                        ElvanMenuState.isMenuOpen = true
                                        menuExpanded = true
                                    }
                                ) {
                                    Icon(
                                        imageVector = MaterialSymbols.Rounded.MoreVert,
                                        contentDescription = K.more.tr(),
                                        tint = colors.textPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                val settingsLabel = K.settings.tr()
                                val meetpagamLabel = K.recycleBin.tr()
                                val thaerndheduLabel = K.select.tr()
                                val menuItems = buildList {
                                    add(
                                        ElvanPopupMenuItem(
                                            title = settingsLabel,
                                            icon = MaterialSymbols.Rounded.Settings,
                                            onClick = {
                                                ElvanMenuState.isMenuOpen = false
                                                menuExpanded = false
                                                activeSubpage = ActiveSubpage.Settings
                                            }
                                        )
                                    )
                                    add(
                                        ElvanPopupMenuItem(
                                            title = meetpagamLabel,
                                            icon = MaterialSymbols.Rounded.Delete,
                                            onClick = {
                                                ElvanMenuState.isMenuOpen = false
                                                menuExpanded = false
                                                activeSubpage = ActiveSubpage.RecycleBin
                                            }
                                        )
                                    )
                                    if (selectedTab != NavTab.Home) {
                                        add(
                                            ElvanPopupMenuItem(
                                                title = thaerndheduLabel,
                                                icon = MaterialSymbols.Rounded.CheckCircleFill,
                                                onClick = {
                                                    ElvanMenuState.isMenuOpen = false
                                                    menuExpanded = false
                                                    isSelectionMode = true
                                                }
                                            )
                                        )
                                    }
                                }
                                ElvanPopupMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = {
                                        ElvanMenuState.isMenuOpen = false
                                        menuExpanded = false
                                    },
                                    colors = colors,
                                    items = menuItems
                                )
                            }
                        },
                        navbar = {
                            val shellController = LocalElvanShellController.current

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
                                    label = "bottomBarSelectionCrossfade"
                                ) { inSelection ->
                                    if (inSelection) {
                                        val canCopyTab = selectedTab == NavTab.Products ||
                                                         selectedTab == NavTab.Customers ||
                                                         (selectedTab == NavTab.Create && uruvakkuSegment == 0)

                                        val onSelectionCopy: (() -> Unit)? = if (canCopyTab) {
                                            {
                                                if (selectedItemIds.size == 1) {
                                                    val selectedId = selectedItemIds.first()
                                                    when (selectedTab) {
                                                        NavTab.Products -> {
                                                            PorulRepository.items.find { it.id == selectedId }?.let { copyProductAndEdit(it) }
                                                        }
                                                        NavTab.Customers -> {
                                                            VaangunarRepository.merchants.find { it.id == selectedId }?.let { copyCustomerAndEdit(it) }
                                                        }
                                                        NavTab.Create -> {
                                                            if (uruvakkuSegment == 0) {
                                                                PattiyalRepository.invoices.find { it.id == selectedId }?.let { copyInvoiceAndEdit(it) }
                                                            }
                                                        }
                                                        else -> {}
                                                    }
                                                }
                                            }
                                        } else null

                                        ElvanSelectionBar(
                                            visible = true,
                                            selectedCount = selectedItemIds.size,
                                            onDelete = {
                                                if (selectedItemIds.isNotEmpty()) {
                                                    showBulkDeleteConfirm = true
                                                }
                                            },
                                            onCopy = onSelectionCopy,
                                            colors = colors
                                        )
                                    } else {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            BottomNavBar(
                                                selectedTab = selectedTab,
                                                hideContent = isSearchActive,
                                                onAddClick = {
                                                    when (selectedTab) {
                                                        NavTab.Home -> {
                                                            activeSubpage = ActiveSubpage.InvoiceEditor(null)
                                                        }
                                                        NavTab.Products -> {
                                                            activeSubpage = ActiveSubpage.ItemEditor(null)
                                                        }
                                                        NavTab.Customers -> {
                                                            activeSubpage = ActiveSubpage.MerchantEditor(null)
                                                        }
                                                        NavTab.Create -> {
                                                            if (uruvakkuSegment == 0) {
                                                                activeSubpage = ActiveSubpage.InvoiceEditor(null)
                                                            } else {
                                                                activeSubpage = ActiveSubpage.ReceiptEditor(null)
                                                            }
                                                        }
                                                    }
                                                },
                                                onTabSelected = { tab, _ ->
                                                    if (selectedTab == tab) {
                                                        shellController.toggleHeader()
                                                    } else {
                                                        selectedTab = tab
                                                    }
                                                }
                                            )

                                            ElvanSearchBar(
                                                visible = isSearchActive,
                                                query = currentSearchQuery,
                                                onQueryChange = { newQuery ->
                                                    when (selectedTab) {
                                                        NavTab.Create -> {
                                                            if (uruvakkuSegment == 0) {
                                                                PattiyalRepository.searchQuery = newQuery
                                                            } else {
                                                                PatrugalRepository.searchQuery = newQuery
                                                            }
                                                        }
                                                        NavTab.Products -> {
                                                            PorulRepository.searchQuery = newQuery
                                                        }
                                                        NavTab.Customers -> {
                                                            VaangunarRepository.searchQuery = newQuery
                                                        }
                                                        else -> {}
                                                    }
                                                },
                                                onClose = {
                                                    isSearchActive = false
                                                    PattiyalRepository.searchQuery = ""
                                                    PatrugalRepository.searchQuery = ""
                                                    PorulRepository.searchQuery = ""
                                                    VaangunarRepository.searchQuery = ""
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
                            NavTab.Home -> {
                                ExpressivePullToRefreshBox(
                                    enabled = !isSelectionMode,
                                    isRefreshing = isRefreshing,
                                    onRefresh = {
                                        scope.launch {
                                            isRefreshing = true
                                            PattiyalRepository.loadAll(currentMode)
                                            PatrugalRepository.loadAll(currentMode)
                                            NiruvanaTharavugalRepository.refreshFromDatabase()
                                            delay(700)
                                            isRefreshing = false
                                        }
                                    },
                                    colors = colors
                                ) {
                                    DashboardScreen(
                                        scrollState = homeScrollState,
                                        onSeeAll = {
                                            selectedTab = NavTab.Create
                                            uruvakkuSegment = 0
                                        },
                                        isRefreshing = isRefreshing,
                                        onInvoiceClick = { invoice ->
                                            activeSubpage = ActiveSubpage.InvoiceView(invoice)
                                        }
                                    )
                                }
                            }

                            NavTab.Create -> {
                                ExpressivePullToRefreshBox(
                                    enabled = !isSelectionMode,
                                    isRefreshing = isRefreshing,
                                    onRefresh = {
                                        scope.launch {
                                            isRefreshing = true
                                            PattiyalRepository.loadAll(currentMode)
                                            PatrugalRepository.loadAll(currentMode)
                                            delay(700)
                                            isRefreshing = false
                                        }
                                    },
                                    colors = colors
                                ) {
                                    CreateScreen(
                                        scrollState = createScrollState,
                                        selectedSegment = uruvakkuSegment,
                                        onSegmentSelected = { newSegment ->
                                            uruvakkuSegment = newSegment
                                            isSearchActive = false
                                            isSelectionMode = false
                                            selectedItemIds = emptySet()
                                            PattiyalRepository.searchQuery = ""
                                            PatrugalRepository.searchQuery = ""
                                        },
                                        isRefreshing = isRefreshing,
                                        isSearchActive = isSearchActive,
                                        isSelectionMode = isSelectionMode,
                                        selectedItemIds = selectedItemIds,
                                        onToggleSelect = onToggleItem,
                                        onItemLongClick = onStartSelection,
                                        onInvoiceClick = { invoice ->
                                            activeSubpage = ActiveSubpage.InvoiceView(invoice)
                                        },
                                        onReceiptClick = { receipt ->
                                            activeSubpage = ActiveSubpage.ReceiptView(receipt)
                                        }
                                    )
                                }
                            }

                            NavTab.Products -> {
                                ExpressivePullToRefreshBox(
                                    enabled = !isSelectionMode,
                                    isRefreshing = isRefreshing,
                                    onRefresh = {
                                        scope.launch {
                                            isRefreshing = true
                                            PorulRepository.loadAll(currentMode)
                                            delay(700)
                                            isRefreshing = false
                                        }
                                    },
                                    colors = colors
                                ) {
                                    ProductScreen(
                                        scrollState = productsScrollState,
                                        isRefreshing = isRefreshing,
                                        onItemClick = { activeSubpage = ActiveSubpage.ProductView(it) },
                                        isSelectionMode = isSelectionMode,
                                        selectedItemIds = selectedItemIds,
                                        onToggleSelect = onToggleItem,
                                        onItemLongClick = onStartSelection
                                    )
                                }
                            }

                            NavTab.Customers -> {
                                ExpressivePullToRefreshBox(
                                    enabled = !isSelectionMode,
                                    isRefreshing = isRefreshing,
                                    onRefresh = {
                                        scope.launch {
                                            isRefreshing = true
                                            VaangunarRepository.loadAll(currentMode)
                                            delay(700)
                                            isRefreshing = false
                                        }
                                    },
                                    colors = colors
                                ) {
                                    CustomerScreen(
                                        scrollState = customersScrollState,
                                        isRefreshing = isRefreshing,
                                        onMerchantClick = { activeSubpage = ActiveSubpage.CustomerView(it) },
                                        isSelectionMode = isSelectionMode,
                                        selectedItemIds = selectedItemIds,
                                        onToggleSelect = onToggleItem,
                                        onItemLongClick = onStartSelection
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showBulkDeleteConfirm && selectedItemIds.isNotEmpty()) {
            val deleteSheetTitle = when (selectedTab) {
                NavTab.Products -> "${selectedItemIds.size} ${K.productDeleted.tr()}"
                NavTab.Customers -> "${selectedItemIds.size} ${K.customerDeleted.tr()}"
                NavTab.Create -> {
                    if (uruvakkuSegment == 0) {
                        "${selectedItemIds.size} ${K.invoices.tr()}"
                    } else {
                        "${selectedItemIds.size} ${K.receipts.tr()}"
                    }
                }
                else -> ""
            }
            ElvanActionSheet(
                title = deleteSheetTitle,
                cancelText = K.cancelBtn.tr(),
                confirmText = K.deleteBtn.tr(),
                confirmColor = Color(0xFFBA1A1A),
                onConfirm = {
                    when (selectedTab) {
                        NavTab.Products -> {
                            selectedItemIds.forEach { PorulRepository.delete(it, currentMode) }
                            ElvanSnackbar.show(porulDeletedMsg)
                        }
                        NavTab.Customers -> {
                            selectedItemIds.forEach { VaangunarRepository.delete(it, currentMode) }
                            ElvanSnackbar.show(vaangunarDeletedMsg)
                        }
                        NavTab.Create -> {
                            if (uruvakkuSegment == 0) {
                                selectedItemIds.forEach { PattiyalRepository.delete(it, currentMode) }
                                ElvanSnackbar.show("${selectedItemIds.size} $pattiyalgalLabel $azhikkiradhuLabel")
                            } else {
                                selectedItemIds.forEach { PatrugalRepository.delete(it, currentMode) }
                                ElvanSnackbar.show("${selectedItemIds.size} $patrucheettugalLabel $azhikkiradhuLabel")
                            }
                        }
                        else -> {}
                    }
                    isSelectionMode = false
                    selectedItemIds = emptySet()
                    showBulkDeleteConfirm = false
                },
                onDismissRequest = {
                    showBulkDeleteConfirm = false
                },
                colors = colors
            )
        }

        CreateDateFilterSheet(
            isOpen = showDateFilterSheet,
            currentStartMillis = if (uruvakkuSegment == 0) PattiyalRepository.startDateFilter else PatrugalRepository.startDateFilter,
            currentEndMillis = if (uruvakkuSegment == 0) PattiyalRepository.endDateFilter else PatrugalRepository.endDateFilter,
            onDismissRequest = { showDateFilterSheet = false },
            onApplyFilter = { start, end, _ ->
                PattiyalRepository.setDateRange(start, end)
                PatrugalRepository.setDateRange(start, end)
            },
            colors = colors
        )
            }
        }
    }
}
