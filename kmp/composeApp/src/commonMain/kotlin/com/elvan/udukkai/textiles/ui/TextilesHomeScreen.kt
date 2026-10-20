package com.elvan.udukkai.textiles.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.core.platform.AppPredictiveBackHandler
import com.elvan.udukkai.core.platform.PlatformType
import com.elvan.udukkai.core.platform.currentPlatform
import com.elvan.udukkai.data.model.PatrugalTharavuru
import com.elvan.udukkai.data.model.PattiyalTharavuru
import com.elvan.udukkai.data.model.PorulTharavuru
import com.elvan.udukkai.data.model.VaangunarTharavuru
import com.elvan.udukkai.textiles.data.TextilesCustomerRepository
import com.elvan.udukkai.textiles.data.TextilesInvoiceRepository
import com.elvan.udukkai.textiles.data.TextilesProductRepository
import com.elvan.udukkai.textiles.data.TextilesProfileRepository
import com.elvan.udukkai.textiles.data.TextilesReceiptRepository
import com.elvan.udukkai.textiles.ui.screens.TextilesCreateScreen
import com.elvan.udukkai.textiles.ui.screens.TextilesCustomerScreen
import com.elvan.udukkai.textiles.ui.screens.TextilesDashboardScreen
import com.elvan.udukkai.textiles.ui.screens.TextilesProductScreen
import com.elvan.udukkai.textiles.ui.screens.TextilesSettingsScreen
import com.elvan.udukkai.textiles.ui.screens.editor.TextilesCustomerEditorScreen
import com.elvan.udukkai.textiles.ui.screens.editor.TextilesInvoiceEditorScreen
import com.elvan.udukkai.textiles.ui.screens.editor.TextilesProductEditorScreen
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.LanguageManager
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.Transitions
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.ExpressivePullToRefreshBox
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.ActiveSubpage
import com.elvan.udukkai.ui.navigation.BottomNavBar
import com.elvan.udukkai.ui.navigation.ElvanSearchBar
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.navigation.NavTab
import com.elvan.udukkai.ui.screens.home.desktop.DesktopSideBar
import com.elvan.udukkai.ui.screens.create.components.CreateDateFilterSheet
import com.elvan.udukkai.ui.screens.editor.receipt.ReceiptEditorScreen
import com.elvan.udukkai.ui.screens.recyclebin.RecycleBinScreen
import com.elvan.udukkai.ui.screens.view.CustomerViewScreen
import com.elvan.udukkai.ui.screens.view.InvoiceViewScreen
import com.elvan.udukkai.ui.screens.view.ProductViewScreen
import com.elvan.udukkai.ui.screens.view.ReceiptViewScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextilesHomeScreen() {
        val ff = LocalAppFontFamily.current

    val homeScrollState = rememberLazyListState()
    val createScrollState = rememberLazyListState()
    val productsScrollState = rememberLazyListState()
    val customersScrollState = rememberLazyListState()

    var selectedTab by remember { mutableStateOf(NavTab.Home) }
    var uruvakkuSegment by remember { mutableStateOf(0) } // 0 = Invoices, 1 = Receipts
    var subpageStack by remember { mutableStateOf<List<ActiveSubpage>>(emptyList()) }
    val activeSubpage = subpageStack.lastOrNull()
    val navigateToSubpage: (ActiveSubpage) -> Unit = { subpage -> subpageStack = subpageStack + subpage }
    val popSubpage: () -> Unit = { if (subpageStack.isNotEmpty()) subpageStack = subpageStack.dropLast(1) }
    val clearSubpages: () -> Unit = { subpageStack = emptyList() }
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
        navigateToSubpage(ActiveSubpage.InvoiceEditor(copied))
    }

    val copySuffix = " (${K.copyNoun.tr()})"

    val copyProductAndEdit: (PorulTharavuru) -> Unit = { item ->
        val copied = item.copy(
            id = 0L,
            porulPeyar = item.porulPeyar.mapValues { "${it.value}$copySuffix" },
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        isSelectionMode = false
        selectedItemIds = emptySet()
        navigateToSubpage(ActiveSubpage.ItemEditor(copied))
    }

    val copyCustomerAndEdit: (VaangunarTharavuru) -> Unit = { merchant ->
        val copied = merchant.copy(
            id = 0L,
            peyar = merchant.peyar.mapValues { "${it.value}$copySuffix" },
            gstin = "",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        isSelectionMode = false
        selectedItemIds = emptySet()
        navigateToSubpage(ActiveSubpage.MerchantEditor(copied))
    }

    val scope = rememberCoroutineScope()
    val colors = rememberShellColors()

    val porulDeletedMsg = K.productDeleted.tr()
    val vaangunarDeletedMsg = K.customerDeleted.tr()
    val pattiyalgalLabel = K.invoices.tr()
    val patrucheettugalLabel = K.receipts.tr()
    val azhikkiradhuLabel = K.erasing.tr()


    // Keep Android Back handling enabled for in-app navigation, but let the single
    // AnimatedContent transition below handle all subpage motion.
    AppPredictiveBackHandler(
        enabled = activeSubpage != null || isSelectionMode || isSearchActive || showBulkDeleteConfirm || selectedTab != NavTab.Home,
        onProgress = { _, _ -> },
        onCancel = {},
        onBack = {
            if (showBulkDeleteConfirm) {
                showBulkDeleteConfirm = false
            } else if (subpageStack.isNotEmpty()) {
                popSubpage()
            } else if (isSelectionMode) {
                isSelectionMode = false
                selectedItemIds = emptySet()
            } else if (isSearchActive) {
                isSearchActive = false
                TextilesInvoiceRepository.searchQuery = ""
                TextilesReceiptRepository.searchQuery = ""
                TextilesProductRepository.searchQuery = ""
                TextilesCustomerRepository.searchQuery = ""
            } else if (selectedTab != NavTab.Home) {
                // Bottom tabs remain unanimated; Back simply returns to Home.
                selectedTab = NavTab.Home
            }
        }
    )

    // Load all repositories on launch and when "textiles" changes
    LaunchedEffect("textiles") {
        TextilesInvoiceRepository.loadAll()
        TextilesReceiptRepository.loadAll()
        TextilesProductRepository.loadAll()
        TextilesCustomerRepository.loadAll()
        TextilesProfileRepository.refreshFromDatabase()

        
    }

    // Reset search and selection state on tab switch
    LaunchedEffect(selectedTab) {
        isSearchActive = false
        isSelectionMode = false
        selectedItemIds = emptySet()
        TextilesInvoiceRepository.searchQuery = ""
        TextilesReceiptRepository.searchQuery = ""
        TextilesProductRepository.searchQuery = ""
        TextilesCustomerRepository.searchQuery = ""
    }

    val currentScrollState = when (selectedTab) {
        NavTab.Home -> homeScrollState
        NavTab.Create -> createScrollState
        NavTab.Products -> productsScrollState
        NavTab.Customers -> customersScrollState
    }

    val currentSearchQuery = when (selectedTab) {
        NavTab.Home -> ""
        NavTab.Create -> if (uruvakkuSegment == 0) TextilesInvoiceRepository.searchQuery else TextilesReceiptRepository.searchQuery
        NavTab.Products -> TextilesProductRepository.searchQuery
        NavTab.Customers -> TextilesCustomerRepository.searchQuery
    }

    val isDesktop = currentPlatform == PlatformType.DESKTOP

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        val isWideScreen = isDesktop && maxWidth >= 768.dp

        val shouldShowBottomBarForSnackbar = (activeSubpage == null || activeSubpage is ActiveSubpage.RecycleBin) && !isWideScreen
        SideEffect {
            ElvanSnackbar.isBottomBarVisible = shouldShowBottomBarForSnackbar
        }
        DisposableEffect(Unit) {
            onDispose { ElvanSnackbar.isBottomBarVisible = false }
        }

        @Composable
        fun RenderSubpage(subpage: ActiveSubpage) {
            when (subpage) {
                is ActiveSubpage.Settings -> {
                    TextilesSettingsScreen(
                        onBack = popSubpage
                    )
                }
                is ActiveSubpage.RecycleBin -> {
                    RecycleBinScreen(
                        onBack = popSubpage
                    )
                }
                is ActiveSubpage.ItemEditor -> {
                    TextilesProductEditorScreen(
                        item = subpage.item,
                        onBack = popSubpage
                    )
                }
                is ActiveSubpage.MerchantEditor -> {
                    TextilesCustomerEditorScreen(
                        merchant = subpage.merchant,
                        onBack = popSubpage
                    )
                }
                is ActiveSubpage.InvoiceEditor -> {
                    TextilesInvoiceEditorScreen(
                        invoice = subpage.invoice,
                        onBack = popSubpage
                    )
                }
                is ActiveSubpage.ReceiptEditor -> {
                    ReceiptEditorScreen(
                        receipt = subpage.receipt,
                        onBack = popSubpage
                    )
                }
                is ActiveSubpage.CustomerView -> {
                    CustomerViewScreen(
                        merchant = subpage.customer,
                        onBack = popSubpage,
                        onEdit = {
                            navigateToSubpage(ActiveSubpage.MerchantEditor(subpage.customer))
                        },
                        onCopy = {
                            copyCustomerAndEdit(subpage.customer)
                        },
                        isTextiles = true
                    )
                }
                is ActiveSubpage.ProductView -> {
                    ProductViewScreen(
                        item = subpage.product,
                        onBack = popSubpage,
                        onEdit = {
                            navigateToSubpage(ActiveSubpage.ItemEditor(subpage.product))
                        },
                        onCopy = {
                            copyProductAndEdit(subpage.product)
                        },
                        isTextiles = true
                    )
                }
                is ActiveSubpage.InvoiceView -> {
                    InvoiceViewScreen(
                        invoice = subpage.invoice,
                        onBack = popSubpage,
                        onEdit = {
                            navigateToSubpage(ActiveSubpage.InvoiceEditor(subpage.invoice))
                        },
                        onCopy = {
                            copyInvoiceAndEdit(subpage.invoice)
                        }
                    )
                }
                is ActiveSubpage.ReceiptView -> {
                    ReceiptViewScreen(
                        receipt = subpage.receipt,
                        onBack = popSubpage,
                        onEdit = {
                            navigateToSubpage(ActiveSubpage.ReceiptEditor(subpage.receipt))
                        }
                    )
                }
            }
        }

        @Composable
        fun RenderMainShell() {
            val allSelectionIds: Set<Long> = when (selectedTab) {
                        NavTab.Products -> TextilesProductRepository.filteredItems.map { it.id }.toSet()
                        NavTab.Customers -> TextilesCustomerRepository.filteredMerchants.map { it.id }.toSet()
                        NavTab.Create -> {
                            if (uruvakkuSegment == 0) {
                                TextilesInvoiceRepository.filteredInvoices.map { it.id }.toSet()
                            } else {
                                TextilesReceiptRepository.filteredReceipts.map { it.id }.toSet()
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
                                val isFilterActive = TextilesInvoiceRepository.isDateFilterActive || TextilesReceiptRepository.isDateFilterActive
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
                                                navigateToSubpage(ActiveSubpage.Settings)
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
                                                navigateToSubpage(ActiveSubpage.RecycleBin)
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
                                        if (initialState && !targetState) {
                                            // Back out of selection mode immediately.
                                            EnterTransition.None togetherWith ExitTransition.None
                                        } else {
                                            (fadeIn(animationSpec = tween(220, easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f))) +
                                             scaleIn(initialScale = 0.94f, animationSpec = tween(220))) togetherWith
                                            (fadeOut(animationSpec = tween(160, easing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f))) +
                                             scaleOut(targetScale = 0.94f, animationSpec = tween(160)))
                                        }
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
                                                            TextilesProductRepository.items.find { it.id == selectedId }?.let { copyProductAndEdit(it) }
                                                        }
                                                        NavTab.Customers -> {
                                                            TextilesCustomerRepository.merchants.find { it.id == selectedId }?.let { copyCustomerAndEdit(it) }
                                                        }
                                                        NavTab.Create -> {
                                                            if (uruvakkuSegment == 0) {
                                                                TextilesInvoiceRepository.invoices.find { it.id == selectedId }?.let { copyInvoiceAndEdit(it) }
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
                                                hideContent = isSearchActive || showDateFilterSheet,
                                                onAddClick = {
                                                    when (selectedTab) {
                                                        NavTab.Home -> {
                                                            navigateToSubpage(ActiveSubpage.InvoiceEditor(null))
                                                        }
                                                        NavTab.Products -> {
                                                            navigateToSubpage(ActiveSubpage.ItemEditor(null))
                                                        }
                                                        NavTab.Customers -> {
                                                            navigateToSubpage(ActiveSubpage.MerchantEditor(null))
                                                        }
                                                        NavTab.Create -> {
                                                            if (uruvakkuSegment == 0) {
                                                                navigateToSubpage(ActiveSubpage.InvoiceEditor(null))
                                                            } else {
                                                                navigateToSubpage(ActiveSubpage.ReceiptEditor(null))
                                                            }
                                                        }
                                                    }
                                                },
                                                onTabSelected = { tab, _ ->
                                                    if (selectedTab == tab) {
                                                        shellController.toggleHeader()
                                                    } else {
                                                        if (selectedTab == NavTab.Create || tab != NavTab.Create) {
                                                            uruvakkuSegment = 0
                                                        }
                                                        val targetScrollState = when (tab) {
                                                            NavTab.Home -> homeScrollState
                                                            NavTab.Create -> createScrollState
                                                            NavTab.Products -> productsScrollState
                                                            NavTab.Customers -> customersScrollState
                                                        }
                                                        scope.launch { targetScrollState.scrollToItem(0, 0) }
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
                                                                TextilesInvoiceRepository.searchQuery = newQuery
                                                            } else {
                                                                TextilesReceiptRepository.searchQuery = newQuery
                                                            }
                                                        }
                                                        NavTab.Products -> {
                                                            TextilesProductRepository.searchQuery = newQuery
                                                        }
                                                        NavTab.Customers -> {
                                                            TextilesCustomerRepository.searchQuery = newQuery
                                                        }
                                                        else -> {}
                                                    }
                                                },
                                                onClose = {
                                                    isSearchActive = false
                                                    TextilesInvoiceRepository.searchQuery = ""
                                                    TextilesReceiptRepository.searchQuery = ""
                                                    TextilesProductRepository.searchQuery = ""
                                                    TextilesCustomerRepository.searchQuery = ""
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
                                            TextilesInvoiceRepository.loadAll()
                                            TextilesReceiptRepository.loadAll()
                                            TextilesProfileRepository.refreshFromDatabase()
                                            delay(700)
                                            isRefreshing = false
                                        }
                                    },
                                    colors = colors
                                ) {
                                    TextilesDashboardScreen(
                                        scrollState = homeScrollState,
                                        onSeeAll = {
                                            scope.launch { createScrollState.scrollToItem(0, 0) }
                                            selectedTab = NavTab.Create
                                            uruvakkuSegment = 0
                                        },
                                        onSeeAllReceipts = {
                                            scope.launch { createScrollState.scrollToItem(0, 0) }
                                            selectedTab = NavTab.Create
                                            uruvakkuSegment = 1
                                        },
                                        isRefreshing = isRefreshing,
                                        onInvoiceClick = { invoice ->
                                            navigateToSubpage(ActiveSubpage.InvoiceView(invoice))
                                        },
                                        onManageProfiles = {
                                            navigateToSubpage(ActiveSubpage.Settings)
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
                                            TextilesInvoiceRepository.loadAll()
                                            TextilesReceiptRepository.loadAll()
                                            delay(700)
                                            isRefreshing = false
                                        }
                                    },
                                    colors = colors
                                ) {
                                    TextilesCreateScreen(
                                        scrollState = createScrollState,
                                        selectedSegment = uruvakkuSegment,
                                        onSegmentSelected = { newSegment ->
                                            if (uruvakkuSegment != newSegment) {
                                                scope.launch { createScrollState.scrollToItem(0, 0) }
                                                uruvakkuSegment = newSegment
                                            }
                                            isSearchActive = false
                                            isSelectionMode = false
                                            selectedItemIds = emptySet()
                                            TextilesInvoiceRepository.searchQuery = ""
                                            TextilesReceiptRepository.searchQuery = ""
                                        },
                                        isRefreshing = isRefreshing,
                                        isSearchActive = isSearchActive,
                                        isSelectionMode = isSelectionMode,
                                        selectedItemIds = selectedItemIds,
                                        onToggleSelect = onToggleItem,
                                        onItemLongClick = onStartSelection,
                                        onInvoiceClick = { invoice ->
                                            navigateToSubpage(ActiveSubpage.InvoiceView(invoice))
                                        },
                                        onReceiptClick = { receipt ->
                                            navigateToSubpage(ActiveSubpage.ReceiptView(receipt))
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
                                            TextilesProductRepository.loadAll()
                                            delay(700)
                                            isRefreshing = false
                                        }
                                    },
                                    colors = colors
                                ) {
                                    TextilesProductScreen(
                                        scrollState = productsScrollState,
                                        isRefreshing = isRefreshing,
                                        onItemClick = { navigateToSubpage(ActiveSubpage.ProductView(it)) },
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
                                            TextilesCustomerRepository.loadAll()
                                            delay(700)
                                            isRefreshing = false
                                        }
                                    },
                                    colors = colors
                                ) {
                                    TextilesCustomerScreen(
                                        scrollState = customersScrollState,
                                        onCustomerClick = { navigateToSubpage(ActiveSubpage.CustomerView(it)) },
                                        colors = colors,
                                        isRefreshing = isRefreshing,
                                        isSelectionMode = isSelectionMode,
                                        selectedCustomerIds = selectedItemIds,
                                        onToggleSelect = onToggleItem,
                                        onCustomerLongClick = onStartSelection
                                    )
                                }
                            }
                        }
                    }
        }

        Row(modifier = Modifier.fillMaxSize()) {
            if (isWideScreen) {
                DesktopSideBar(
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        if (selectedTab != tab) {
                            if (selectedTab == NavTab.Create || tab != NavTab.Create) {
                                uruvakkuSegment = 0
                            }
                            val targetScrollState = when (tab) {
                                NavTab.Home -> homeScrollState
                                NavTab.Create -> createScrollState
                                NavTab.Products -> productsScrollState
                                NavTab.Customers -> customersScrollState
                            }
                            scope.launch { targetScrollState.scrollToItem(0, 0) }
                            selectedTab = tab
                        }
                    },
                    onSettingsClick = {
                        navigateToSubpage(ActiveSubpage.Settings)
                    },
                    colors = colors,
                    appTitle = "Udukkai Textiles",
                    subtitle = "சவுளி",
                    activeProfile = TextilesProfileRepository.profile,
                    allProfiles = TextilesProfileRepository.profiles,
                    onSelectProfile = { TextilesProfileRepository.setActiveProfile(it) }
                )
            }

            Box(
                modifier = if (isWideScreen) Modifier.weight(1f).fillMaxHeight() else Modifier.fillMaxSize()
            ) {
            // Use one transition owner for subpages. Rendering the current and previous
            // subpages again in a predictive overlay caused duplicate composition and competing
            // motion on quick Back gestures. The same AnimatedContent now handles each stack change.
            AnimatedContent(
                targetState = subpageStack,
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background),
                transitionSpec = {
                    val isForward = targetState.size >= initialState.size
                    with(Transitions) {
                        smoothHorizontal(forward = isForward)
                    }
                },
                label = "HomeToSubpageTransition"
            ) { stack ->
                val subpage = stack.lastOrNull()
                if (subpage != null) {
                    RenderSubpage(subpage)
                } else {
                    RenderMainShell()
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
                            selectedItemIds.forEach { TextilesProductRepository.delete(it) }
                            ElvanSnackbar.show(porulDeletedMsg)
                        }
                        NavTab.Customers -> {
                            selectedItemIds.forEach { TextilesCustomerRepository.delete(it) }
                            ElvanSnackbar.show(vaangunarDeletedMsg)
                        }
                        NavTab.Create -> {
                            if (uruvakkuSegment == 0) {
                                selectedItemIds.forEach { TextilesInvoiceRepository.delete(it) }
                                ElvanSnackbar.show("${selectedItemIds.size} $pattiyalgalLabel $azhikkiradhuLabel")
                            } else {
                                selectedItemIds.forEach { TextilesReceiptRepository.delete(it) }
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
            currentStartMillis = if (uruvakkuSegment == 0) TextilesInvoiceRepository.startDateFilter else TextilesReceiptRepository.startDateFilter,
            currentEndMillis = if (uruvakkuSegment == 0) TextilesInvoiceRepository.endDateFilter else TextilesReceiptRepository.endDateFilter,
            onDismissRequest = { showDateFilterSheet = false },
            onApplyFilter = { start, end, _ ->
                TextilesInvoiceRepository.setDateRange(start, end)
                TextilesReceiptRepository.setDateRange(start, end)
            },
            colors = colors
        )
            }
        }
    }
}
