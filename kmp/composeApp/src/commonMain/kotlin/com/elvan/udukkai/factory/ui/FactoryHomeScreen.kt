package com.elvan.udukkai.factory.ui

import com.elvan.udukkai.core.print.billBusinessFolder
import androidx.compose.runtime.LaunchedEffect
import com.elvan.udukkai.core.print.BillPdfAutoSave
import com.elvan.udukkai.core.print.BillPdfAutoSaveHost
import com.elvan.udukkai.core.print.BillPdfJob
import com.elvan.udukkai.core.print.invoicePdfName
import com.elvan.udukkai.core.print.receiptPdfName
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
import com.elvan.udukkai.data.model.ReceiptModel
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.model.ItemModel
import com.elvan.udukkai.data.model.CustomerModel
import com.elvan.udukkai.factory.data.FactoryCustomerRepository
import com.elvan.udukkai.core.print.PrintPayload
import com.elvan.udukkai.factory.data.FactoryInvoiceRepository
import com.elvan.udukkai.factory.data.FactoryItemRepository
import com.elvan.udukkai.factory.data.FactoryProfileRepository
import com.elvan.udukkai.factory.data.FactoryReceiptRepository
import com.elvan.udukkai.factory.ui.screens.FactoryCreateScreen
import com.elvan.udukkai.factory.ui.screens.FactoryCustomerScreen
import com.elvan.udukkai.factory.ui.screens.FactoryDashboardScreen
import com.elvan.udukkai.factory.ui.screens.FactoryItemScreen
import com.elvan.udukkai.factory.ui.screens.FactorySettingsScreen
import com.elvan.udukkai.factory.ui.screens.editor.FactoryCustomerEditorScreen
import com.elvan.udukkai.factory.ui.screens.editor.FactoryInvoiceEditorScreen
import com.elvan.udukkai.factory.ui.screens.editor.FactoryItemEditorScreen
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
import com.elvan.udukkai.factory.ui.screens.FactoryRecycleBinScreen
import com.elvan.udukkai.factory.ui.screens.editor.FactoryReceiptEditorScreen
import com.elvan.udukkai.factory.ui.screens.factoryCustomerActivity
import com.elvan.udukkai.factory.ui.screens.factoryItemActivity
import com.elvan.udukkai.ui.screens.view.CustomerViewScreen
import com.elvan.udukkai.ui.screens.view.InvoiceViewScreen
import com.elvan.udukkai.ui.screens.view.ItemViewScreen
import com.elvan.udukkai.ui.screens.view.ReceiptViewScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FactoryHomeScreen() {
        val ff = LocalAppFontFamily.current

    val homeScrollState = rememberLazyListState()
    val createScrollState = rememberLazyListState()
    val itemsScrollState = rememberLazyListState()
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
    var closedYearEdit by remember { mutableStateOf<com.elvan.udukkai.data.model.InvoiceModel?>(null) }
    var showDateFilterSheet by remember { mutableStateOf(false) }

    val onToggleItem: (Long) -> Unit = { id ->
        val newSet = if (selectedItemIds.contains(id)) selectedItemIds - id else selectedItemIds + id
        selectedItemIds = newSet
    }
    val onStartSelection: (Long) -> Unit = { id ->
        isSelectionMode = true
        selectedItemIds = setOf(id)
    }

    val copyInvoiceAndEdit: (InvoiceModel) -> Unit = { invoice ->
        val copied = invoice.copy(
            id = 0L,
            invoiceNumber = "",
            invoiceDate = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        isSelectionMode = false
        selectedItemIds = emptySet()
        navigateToSubpage(ActiveSubpage.InvoiceEditor(copied))
    }

    val copySuffix = " (${K.copyNoun.tr()})"

    val copyItemAndEdit: (ItemModel) -> Unit = { item ->
        val copied = item.copy(
            id = 0L,
            name = item.name.mapValues { "${it.value}$copySuffix" },
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        isSelectionMode = false
        selectedItemIds = emptySet()
        navigateToSubpage(ActiveSubpage.ItemEditor(copied))
    }

    val copyCustomerAndEdit: (CustomerModel) -> Unit = { merchant ->
        val copied = merchant.copy(
            id = 0L,
            name = merchant.name.mapValues { "${it.value}$copySuffix" },
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
    // Scroll positions of the item and customer pages, so they come back where they were after opening a bill
    val viewScrollStates = remember { mutableMapOf<String, androidx.compose.foundation.lazy.LazyListState>() }
    LaunchedEffect(subpageStack) {
        val open = subpageStack.mapNotNull {
            when (it) {
                is ActiveSubpage.CustomerView -> "c${it.customer.id}"
                is ActiveSubpage.ItemView -> "i${it.item.id}"
                else -> null
            }
        }.toSet()
        viewScrollStates.keys.retainAll(open)
    }

    val itemDeletedMsg = K.itemDeleted.tr()
    val customerDeletedMsg = K.customerDeleted.tr()
    val invoicesLabel = K.invoices.tr()
    val receiptsLabel = K.receipts.tr()
    val erasingLabel = K.erasing.tr()
    val invoiceHasReceiptsMsg = K.invoiceHasReceipts.tr()
    val booksLockedMsg = K.booksLockedMsg.tr()


    // Keep Android Back handling enabled for in-app navigation, but let the single
    // AnimatedContent transition below handle all subpage motion.
    AppPredictiveBackHandler(
        enabled = activeSubpage != null || isSelectionMode || isSearchActive || showBulkDeleteConfirm || selectedTab != NavTab.Home,
        // The whole screen shrinks and slides under the finger (see BackGesture), then the page change animates
        onProgress = { p, e -> com.elvan.udukkai.core.platform.BackGesture.progress = p; com.elvan.udukkai.core.platform.BackGesture.edge = e },
        onCancel = { com.elvan.udukkai.core.platform.BackGesture.progress = 0f },
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
                FactoryInvoiceRepository.searchQuery = ""
                FactoryReceiptRepository.searchQuery = ""
                FactoryItemRepository.searchQuery = ""
                FactoryCustomerRepository.searchQuery = ""
            } else if (selectedTab == NavTab.Create && uruvakkuSegment == 1) {
                // Receipts step back to bills first; the next Back returns to Home
                uruvakkuSegment = 0
            } else if (selectedTab != NavTab.Home) {
                // Bottom tabs remain unanimated; Back simply returns to Home.
                selectedTab = NavTab.Home
            }
        }
    )

    // Load all repositories on launch and when "factory" changes
    LaunchedEffect("factory") {
        FactoryInvoiceRepository.loadAll()
        FactoryReceiptRepository.loadAll()
        FactoryItemRepository.loadAll()
        FactoryCustomerRepository.loadAll()
        FactoryProfileRepository.refreshFromDatabase()

        
    }

    // Reset search and selection state on tab switch
    LaunchedEffect(selectedTab) {
        isSearchActive = false
        isSelectionMode = false
        selectedItemIds = emptySet()
        FactoryInvoiceRepository.searchQuery = ""
        FactoryReceiptRepository.searchQuery = ""
        FactoryItemRepository.searchQuery = ""
        FactoryCustomerRepository.searchQuery = ""
    }

    val currentScrollState = when (selectedTab) {
        NavTab.Home -> homeScrollState
        NavTab.Create -> createScrollState
        NavTab.Items -> itemsScrollState
        NavTab.Customers -> customersScrollState
    }

    val currentSearchQuery = when (selectedTab) {
        NavTab.Home -> ""
        NavTab.Create -> if (uruvakkuSegment == 0) FactoryInvoiceRepository.searchQuery else FactoryReceiptRepository.searchQuery
        NavTab.Items -> FactoryItemRepository.searchQuery
        NavTab.Customers -> FactoryCustomerRepository.searchQuery
    }

    val isDesktop = currentPlatform == PlatformType.DESKTOP

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Folder of the business a bill belongs to
        val billBusiness: (Long?) -> String = { id ->
            billBusinessFolder(FactoryProfileRepository.profiles.find { it.id == id } ?: FactoryProfileRepository.profile)
        }

        // Bills that have no PDF yet get one a moment after the app opens
        LaunchedEffect(Unit) {
            delay(8000)
            BillPdfAutoSave.syncMissing(FactoryInvoiceRepository.invoices, FactoryReceiptRepository.receipts, billBusiness)
        }

        // Writes the PDF of every saved bill to the documents folder
        BillPdfAutoSaveHost(businessOf = billBusiness) { request ->
            val lang = if (LanguageManager.activeLanguageCode.startsWith("ta")) "ta" else "en"
            when (request.kind) {
                BillPdfAutoSave.Kind.Invoice -> FactoryInvoiceRepository.invoices.find { it.id == request.id }?.let { invoice ->
                    val profile = FactoryProfileRepository.profiles.find { it.id == invoice.companyId } ?: FactoryProfileRepository.profile
                    BillPdfJob(
                        PrintPayload.forFactoryInvoice(
                            invoice = invoice,
                            profile = profile,
                            customer = invoice.customerId?.let { FactoryCustomerRepository.getById(it) },
                            appLanguage = lang
                        ),
                        invoicePdfName(invoice),
                        billBusinessFolder(profile)
                    )
                }
                BillPdfAutoSave.Kind.Receipt -> FactoryReceiptRepository.receipts.find { it.id == request.id }?.let { receipt ->
                    val profile = FactoryProfileRepository.profiles.find { it.id == receipt.companyId } ?: FactoryProfileRepository.profile
                    BillPdfJob(
                        PrintPayload.forReceipt(
                            receipt = receipt,
                            profile = profile,
                            invoiceNumbers = FactoryReceiptRepository.getLinksForReceipt(receipt.id).mapNotNull { link ->
                                FactoryInvoiceRepository.invoices.find { it.id == link.invoiceId }?.invoiceNumber
                            },
                            isCoolie = true,
                            appLanguage = lang
                        ),
                        receiptPdfName(receipt),
                        billBusinessFolder(profile)
                    )
                }
            }
        }

        val isWideScreen = isDesktop && maxWidth >= 768.dp

        val shouldShowBottomBarForSnackbar = (activeSubpage !is ActiveSubpage.Settings) && !isWideScreen
        SideEffect {
            ElvanSnackbar.isBottomBarVisible = shouldShowBottomBarForSnackbar
        }


        @Composable
        fun RenderSubpage(subpage: ActiveSubpage) {
            when (subpage) {
                is ActiveSubpage.Settings -> {
                    FactorySettingsScreen(
                        onBack = popSubpage
                    )
                }
                // GST reports belong to Textiles; Coolie bills carry no tax
                is ActiveSubpage.Reports -> Unit
                is ActiveSubpage.RecycleBin -> {
                    FactoryRecycleBinScreen(
                        onBack = popSubpage
                    )
                }
                is ActiveSubpage.ItemEditor -> {
                    FactoryItemEditorScreen(
                        item = subpage.item,
                        onBack = popSubpage
                    )
                }
                is ActiveSubpage.MerchantEditor -> {
                    FactoryCustomerEditorScreen(
                        merchant = subpage.merchant,
                        onBack = popSubpage
                    )
                }
                is ActiveSubpage.InvoiceEditor -> {
                    FactoryInvoiceEditorScreen(
                        invoice = subpage.invoice,
                        onBack = popSubpage
                    )
                }
                is ActiveSubpage.ReceiptEditor -> {
                    FactoryReceiptEditorScreen(
                        receipt = subpage.receipt,
                        onBack = popSubpage
                    )
                }
                is ActiveSubpage.CustomerView -> {
                    val currentCustomer = FactoryCustomerRepository.merchants.find { it.id == subpage.customer.id } ?: subpage.customer
                    CustomerViewScreen(
                        merchant = currentCustomer,
                        activity = factoryCustomerActivity(currentCustomer),
                        onBillClick = { id -> FactoryInvoiceRepository.invoices.find { it.id == id }?.let { navigateToSubpage(ActiveSubpage.InvoiceView(it)) } },
                        profile = FactoryProfileRepository.profile,
                        allProfiles = FactoryProfileRepository.profiles,
                        showGst = false,
                        scrollState = viewScrollStates.getOrPut("c${currentCustomer.id}") { androidx.compose.foundation.lazy.LazyListState() },
                        onBack = { viewScrollStates.remove("c${currentCustomer.id}"); popSubpage() },
                        onEdit = {
                            navigateToSubpage(ActiveSubpage.MerchantEditor(currentCustomer))
                        },
                        onCopy = {
                            copyCustomerAndEdit(currentCustomer)
                        }
                    )
                }
                is ActiveSubpage.ItemView -> {
                    val currentItem = FactoryItemRepository.items.find { it.id == subpage.item.id } ?: subpage.item
                    ItemViewScreen(
                        item = currentItem,
                        activity = factoryItemActivity(currentItem),
                        onBillClick = { id -> FactoryInvoiceRepository.invoices.find { it.id == id }?.let { navigateToSubpage(ActiveSubpage.InvoiceView(it)) } },
                        profile = FactoryProfileRepository.profile,
                        allProfiles = FactoryProfileRepository.profiles,
                        isCoolie = true,
                        scrollState = viewScrollStates.getOrPut("i${currentItem.id}") { androidx.compose.foundation.lazy.LazyListState() },
                        onBack = { viewScrollStates.remove("i${currentItem.id}"); popSubpage() },
                        onEdit = {
                            navigateToSubpage(ActiveSubpage.ItemEditor(currentItem))
                        },
                        onCopy = {
                            copyItemAndEdit(currentItem)
                        }
                    )
                }
                is ActiveSubpage.InvoiceView -> {
                    val currentInvoice = FactoryInvoiceRepository.invoices.find { it.id == subpage.invoice.id } ?: subpage.invoice
                    val invoiceLockedMsg = K.invoiceLockedDueToReceipt.tr()
                    InvoiceViewScreen(
                        invoice = currentInvoice,
                        profile = FactoryProfileRepository.profile,
                        allProfiles = FactoryProfileRepository.profiles,
                        isPaid = FactoryInvoiceRepository.hasLinkedReceipts(currentInvoice.id),
                        onBack = popSubpage,
                        onEdit = {
                            if (FactoryInvoiceRepository.hasLinkedReceipts(currentInvoice.id)) {
                                ElvanSnackbar.show(invoiceLockedMsg)
                            } else if (FactoryInvoiceRepository.isLocked(currentInvoice)) {
                                closedYearEdit = currentInvoice
                            } else {
                                navigateToSubpage(ActiveSubpage.InvoiceEditor(currentInvoice))
                            }
                        },
                        onCopy = {
                            copyInvoiceAndEdit(currentInvoice)
                        },
                        onConvertToTaxInvoice = {
                            val converted = currentInvoice.copy(
                                id = 0L,
                                invoiceNumber = "",
                                invoiceType = "tax-invoice",
                                invoiceDate = System.currentTimeMillis(),
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                            navigateToSubpage(ActiveSubpage.InvoiceEditor(converted))
                        },
                        printPayload = { activeProfile ->
                            PrintPayload.forFactoryInvoice(
                                invoice = currentInvoice,
                                profile = activeProfile,
                                customer = currentInvoice.customerId?.let { FactoryCustomerRepository.getById(it) },
                                appLanguage = if (LanguageManager.activeLanguageCode.startsWith("ta")) "ta" else "en"
                            )
                        }
                    )
                }
                is ActiveSubpage.ReceiptView -> {
                    val currentReceipt = FactoryReceiptRepository.receipts.find { it.id == subpage.receipt.id } ?: subpage.receipt
                    ReceiptViewScreen(
                        receipt = currentReceipt,
                        profile = FactoryProfileRepository.profile,
                        allProfiles = FactoryProfileRepository.profiles,
                        onBack = popSubpage,
                        onEdit = {
                            navigateToSubpage(ActiveSubpage.ReceiptEditor(currentReceipt))
                        },
                        printPayload = { activeProfile ->
                            PrintPayload.forReceipt(
                                receipt = currentReceipt,
                                profile = activeProfile,
                                invoiceNumbers = FactoryReceiptRepository.getLinksForReceipt(currentReceipt.id).mapNotNull { link ->
                                    FactoryInvoiceRepository.invoices.find { it.id == link.invoiceId }?.invoiceNumber
                                },
                                isCoolie = true,
                                appLanguage = if (LanguageManager.activeLanguageCode.startsWith("ta")) "ta" else "en"
                            )
                        }
                    )
                }
            }
        }

        @Composable
        fun RenderMainShell() {
            val allSelectionIds: Set<Long> = when (selectedTab) {
                        NavTab.Items -> FactoryItemRepository.filteredItems.map { it.id }.toSet()
                        NavTab.Customers -> FactoryCustomerRepository.filteredMerchants.map { it.id }.toSet()
                        NavTab.Create -> {
                            if (uruvakkuSegment == 0) {
                                FactoryInvoiceRepository.filteredInvoices.map { it.id }.toSet()
                            } else {
                                FactoryReceiptRepository.filteredReceipts.map { it.id }.toSet()
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
                                val isFilterActive = FactoryInvoiceRepository.isDateFilterActive || FactoryReceiptRepository.isDateFilterActive
                                ElvanTopBarIconButton(
                                    onClick = { showDateFilterSheet = true }
                                ) {
                                    Icon(
                                        imageVector = MaterialSymbols.Rounded.FilterList,
                                        contentDescription = K.a11yFilter.tr(),
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
                                    transitionSpec = { com.elvan.udukkai.ui.components.shell.elvanBarSwap() },
                                    label = "bottomBarSelectionCrossfade"
                                ) { inSelection ->
                                    if (inSelection) {
                                        val canCopyTab = selectedTab == NavTab.Items ||
                                                         selectedTab == NavTab.Customers ||
                                                         (selectedTab == NavTab.Create && uruvakkuSegment == 0)

                                        val onSelectionCopy: (() -> Unit)? = if (canCopyTab) {
                                            {
                                                if (selectedItemIds.size == 1) {
                                                    val selectedId = selectedItemIds.first()
                                                    when (selectedTab) {
                                                        NavTab.Items -> {
                                                            FactoryItemRepository.items.find { it.id == selectedId }?.let { copyItemAndEdit(it) }
                                                        }
                                                        NavTab.Customers -> {
                                                            FactoryCustomerRepository.merchants.find { it.id == selectedId }?.let { copyCustomerAndEdit(it) }
                                                        }
                                                        NavTab.Create -> {
                                                            if (uruvakkuSegment == 0) {
                                                                FactoryInvoiceRepository.invoices.find { it.id == selectedId }?.let { copyInvoiceAndEdit(it) }
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
                                                        NavTab.Items -> {
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
                                                            NavTab.Items -> itemsScrollState
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
                                                                FactoryInvoiceRepository.searchQuery = newQuery
                                                            } else {
                                                                FactoryReceiptRepository.searchQuery = newQuery
                                                            }
                                                        }
                                                        NavTab.Items -> {
                                                            FactoryItemRepository.searchQuery = newQuery
                                                        }
                                                        NavTab.Customers -> {
                                                            FactoryCustomerRepository.searchQuery = newQuery
                                                        }
                                                        else -> {}
                                                    }
                                                },
                                                onClose = {
                                                    isSearchActive = false
                                                    FactoryInvoiceRepository.searchQuery = ""
                                                    FactoryReceiptRepository.searchQuery = ""
                                                    FactoryItemRepository.searchQuery = ""
                                                    FactoryCustomerRepository.searchQuery = ""
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
                                            FactoryInvoiceRepository.loadAll()
                                            FactoryReceiptRepository.loadAll()
                                            FactoryProfileRepository.refreshFromDatabase()
                                            delay(700)
                                            isRefreshing = false
                                        }
                                    },
                                    colors = colors
                                ) {
                                    FactoryDashboardScreen(
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
                                            FactoryInvoiceRepository.loadAll()
                                            FactoryReceiptRepository.loadAll()
                                            delay(700)
                                            isRefreshing = false
                                        }
                                    },
                                    colors = colors
                                ) {
                                    FactoryCreateScreen(
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
                                            FactoryInvoiceRepository.searchQuery = ""
                                            FactoryReceiptRepository.searchQuery = ""
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

                            NavTab.Items -> {
                                ExpressivePullToRefreshBox(
                                    enabled = !isSelectionMode,
                                    isRefreshing = isRefreshing,
                                    onRefresh = {
                                        scope.launch {
                                            isRefreshing = true
                                            FactoryItemRepository.loadAll()
                                            delay(700)
                                            isRefreshing = false
                                        }
                                    },
                                    colors = colors
                                ) {
                                    FactoryItemScreen(
                                        scrollState = itemsScrollState,
                                        isRefreshing = isRefreshing,
                                        onItemClick = { navigateToSubpage(ActiveSubpage.ItemView(it)) },
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
                                            FactoryCustomerRepository.loadAll()
                                            delay(700)
                                            isRefreshing = false
                                        }
                                    },
                                    colors = colors
                                ) {
                                    FactoryCustomerScreen(
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
                                NavTab.Items -> itemsScrollState
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
                    appTitle = "Udukkai Factory",
                    subtitle = "ஆலை",
                    activeProfile = FactoryProfileRepository.profile,
                    allProfiles = FactoryProfileRepository.profiles,
                    onSelectProfile = { FactoryProfileRepository.setActiveProfile(it) }
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

        closedYearEdit?.let { target ->
            ElvanActionSheet(
                title = K.yearClosedEditTitle.tr(),
                cancelText = K.cancelBtn.tr(),
                confirmText = K.editAnyway.tr(),
                onConfirm = {
                    com.elvan.udukkai.core.finance.YearLock.openForSession(target.invoiceDate)
                    navigateToSubpage(ActiveSubpage.InvoiceEditor(target))
                    closedYearEdit = null
                },
                onDismissRequest = { closedYearEdit = null }
            )
        }

        if (showBulkDeleteConfirm && selectedItemIds.isNotEmpty()) {
            val deleteSheetTitle = when (selectedTab) {
                NavTab.Items -> "${selectedItemIds.size} ${K.itemDeleted.tr()}"
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
                        NavTab.Items -> {
                            selectedItemIds.forEach { FactoryItemRepository.delete(it) }
                            ElvanSnackbar.show(itemDeletedMsg)
                        }
                        NavTab.Customers -> {
                            selectedItemIds.forEach { FactoryCustomerRepository.delete(it) }
                            ElvanSnackbar.show(customerDeletedMsg)
                        }
                        NavTab.Create -> {
                            if (uruvakkuSegment == 0) {
                                val deleted = selectedItemIds.count { FactoryInvoiceRepository.delete(it) }
                                if (deleted > 0) ElvanSnackbar.show("$deleted $invoicesLabel $erasingLabel")
                                // Bills that have a receipt are paid and stay
                                if (deleted < selectedItemIds.size) {
                                    val inLockedYear = FactoryInvoiceRepository.invoices.any { it.id in selectedItemIds && FactoryInvoiceRepository.isLocked(it) }
                                    ElvanSnackbar.show(if (inLockedYear) booksLockedMsg else invoiceHasReceiptsMsg)
                                }
                            } else {
                                val deleted = selectedItemIds.count { FactoryReceiptRepository.delete(it) }
                                if (deleted > 0) ElvanSnackbar.show("$deleted $receiptsLabel $erasingLabel")
                                if (deleted < selectedItemIds.size) ElvanSnackbar.show(booksLockedMsg)
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
            currentStartMillis = if (uruvakkuSegment == 0) FactoryInvoiceRepository.startDateFilter else FactoryReceiptRepository.startDateFilter,
            currentEndMillis = if (uruvakkuSegment == 0) FactoryInvoiceRepository.endDateFilter else FactoryReceiptRepository.endDateFilter,
            onDismissRequest = { showDateFilterSheet = false },
            onApplyFilter = { start, end, _ ->
                FactoryInvoiceRepository.setDateRange(start, end)
                FactoryReceiptRepository.setDateRange(start, end)
            },
            colors = colors
        )
            }
        }
    }
}
