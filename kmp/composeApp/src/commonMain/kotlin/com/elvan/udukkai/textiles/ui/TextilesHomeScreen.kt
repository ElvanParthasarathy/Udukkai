package com.elvan.udukkai.textiles.ui

import com.elvan.udukkai.ui.navigation.smoothScrollToTop

import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.core.print.billBusinessFolder
import androidx.compose.ui.graphics.graphicsLayer
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
import com.elvan.udukkai.core.update.triggerAppUpdateCheck
import com.elvan.udukkai.core.sync.getFirebaseSyncManager
import com.elvan.udukkai.textiles.data.TextilesCustomerRepository
import com.elvan.udukkai.core.print.PrintPayload
import com.elvan.udukkai.textiles.data.TextilesInvoiceRepository
import com.elvan.udukkai.textiles.data.TextilesItemRepository
import com.elvan.udukkai.textiles.data.TextilesProfileRepository
import com.elvan.udukkai.textiles.data.TextilesReceiptRepository
import com.elvan.udukkai.textiles.ui.screens.TextilesCreateScreen
import com.elvan.udukkai.textiles.ui.screens.TextilesCustomerScreen
import com.elvan.udukkai.textiles.ui.screens.TextilesDashboardScreen
import com.elvan.udukkai.textiles.ui.screens.TextilesItemScreen
import com.elvan.udukkai.textiles.ui.screens.TextilesSettingsScreen
import com.elvan.udukkai.ui.screens.settings.SettingsRoute
import com.elvan.udukkai.ui.screens.settings.openPlatformSettings
import com.elvan.udukkai.ui.navigation.openPlatformSubpage
import com.elvan.udukkai.textiles.ui.screens.editor.TextilesCustomerEditorScreen
import com.elvan.udukkai.textiles.ui.screens.editor.TextilesInvoiceEditorScreen
import com.elvan.udukkai.textiles.ui.screens.editor.TextilesItemEditorScreen
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
import com.elvan.udukkai.ui.navigation.LandscapeSideNavBar
import com.elvan.udukkai.ui.navigation.LandscapeTopActionButton
import com.elvan.udukkai.ui.components.shell.LandscapeEmptyDetailPane
import com.elvan.udukkai.ui.navigation.ElvanSearchBar
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.navigation.NavTab
import com.elvan.udukkai.ui.screens.home.desktop.DesktopSideBar
import com.elvan.udukkai.ui.screens.create.components.CreateDateFilterSheet
import com.elvan.udukkai.textiles.ui.screens.TextilesRecycleBinScreen
import com.elvan.udukkai.textiles.ui.screens.TextilesReportsScreen
import com.elvan.udukkai.textiles.ui.screens.editor.TextilesReceiptEditorScreen
import com.elvan.udukkai.textiles.ui.screens.textilesCustomerActivity
import com.elvan.udukkai.textiles.ui.screens.textilesItemActivity
import com.elvan.udukkai.ui.screens.view.CustomerViewScreen
import com.elvan.udukkai.ui.screens.view.InvoiceViewScreen
import com.elvan.udukkai.ui.screens.view.ItemViewScreen
import com.elvan.udukkai.ui.screens.view.ReceiptViewScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
/** A running bulk delete: what tab it came from and the ids still to be handled. */
private class BulkDelete(val tab: NavTab, val segment: Int, val ids: List<Long>)

@Composable
fun TextilesHomeScreen() {
        val ff = LocalAppFontFamily.current

    val homeScrollState = rememberLazyListState()
    val createScrollState = rememberLazyListState()
    val itemsScrollState = rememberLazyListState()
    val customersScrollState = rememberLazyListState()

    var selectedTab by remember { mutableStateOf(NavTab.Home) }
    var tabScrollJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    var uruvakkuSegment by remember { mutableStateOf(0) } // 0 = Invoices, 1 = Receipts
    var isLandscapeMode by remember { mutableStateOf(false) }
    var subpageStack by remember { mutableStateOf<List<ActiveSubpage>>(emptyList()) }
    val activeSubpage = subpageStack.lastOrNull()
    // The same page is never pushed twice on top of itself (a double tap would slide it in twice and need two Backs)
    val navigateToSubpage: (ActiveSubpage) -> Unit = { subpage ->
        if (openPlatformSubpage(subpage)) {
            // Handled natively by Activity on Android
        } else if (subpageStack.lastOrNull() != subpage) {
            if (isLandscapeMode && (subpage is ActiveSubpage.InvoiceView || subpage is ActiveSubpage.ItemView || subpage is ActiveSubpage.CustomerView || subpage is ActiveSubpage.ReceiptView)) {
                subpageStack = listOf(subpage)
            } else {
                subpageStack = subpageStack + subpage
            }
        }
    }
    val popSubpage: () -> Unit = { if (subpageStack.isNotEmpty()) subpageStack = subpageStack.dropLast(1) }
    val clearSubpages: () -> Unit = { subpageStack = emptyList() }
    var isRefreshing by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedItemIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var showBulkDeleteConfirm by remember { mutableStateOf(false) }
    var bulkDelete by remember { mutableStateOf<BulkDelete?>(null) }
    var closedYearEdit by remember { mutableStateOf<com.elvan.udukkai.data.model.InvoiceModel?>(null) }
    var showDateFilterSheet by remember { mutableStateOf(false) }

    val onToggleItem: (Long) -> Unit = { id ->
        val newSet = if (selectedItemIds.contains(id)) selectedItemIds - id else selectedItemIds + id
        selectedItemIds = newSet
    }
    val onStartSelection: (Long) -> Unit = { id ->
        // Holding another card while selecting adds it (the drag that follows adds the ones it passes)
        selectedItemIds = if (isSelectionMode) selectedItemIds + id else setOf(id)
        isSelectionMode = true
    }

    val copyInvoiceAndEdit: (InvoiceModel) -> Unit = { invoice ->
        val copied = invoice.copy(
            id = 0L,
            invoiceNumber = "",
            invoiceDate = DateUtils.todayMillis(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        isSelectionMode = false
        selectedItemIds = emptySet()
        navigateToSubpage(ActiveSubpage.InvoiceEditor(copied))
    }

    val copyItemAndEdit: (ItemModel) -> Unit = { item ->
        val copied = item.copy(
            id = 0L,
            name = item.name,
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
            name = merchant.name,
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
                TextilesItemRepository.searchQuery = ""
                TextilesCustomerRepository.searchQuery = ""
            } else if (selectedTab == NavTab.Create && uruvakkuSegment == 1) {
                // Receipts step back to bills first; the next Back returns to Home
                uruvakkuSegment = 0
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
        TextilesItemRepository.loadAll()
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
        TextilesItemRepository.searchQuery = ""
        TextilesCustomerRepository.searchQuery = ""
    }

    val currentScrollState = when (selectedTab) {
        NavTab.Home -> homeScrollState
        NavTab.Create -> createScrollState
        NavTab.Items -> itemsScrollState
        NavTab.Customers -> customersScrollState
    }

    val currentSearchQuery = when (selectedTab) {
        NavTab.Home -> ""
        NavTab.Create -> if (uruvakkuSegment == 0) TextilesInvoiceRepository.searchQuery else TextilesReceiptRepository.searchQuery
        NavTab.Items -> TextilesItemRepository.searchQuery
        NavTab.Customers -> TextilesCustomerRepository.searchQuery
    }

    val isDesktop = currentPlatform == PlatformType.DESKTOP

    val landscapeControlHaze = dev.chrisbanes.haze.rememberHazeState()
    androidx.compose.runtime.CompositionLocalProvider(com.elvan.udukkai.ui.components.shell.LocalHazeState provides landscapeControlHaze.takeIf { com.elvan.udukkai.core.platform.platformLayoutPolicy.supportsLandscapeControls }) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Folder of the business a bill belongs to
        val billBusiness: (Long?) -> String = { id ->
            billBusinessFolder(TextilesProfileRepository.profiles.find { it.id == id } ?: TextilesProfileRepository.profile)
        }

        // Bills that have no PDF yet get one a moment after the app opens
        LaunchedEffect(Unit) {
            delay(8000)
            BillPdfAutoSave.syncMissing(TextilesInvoiceRepository.invoices, TextilesReceiptRepository.receipts, billBusiness)
        }

        // Writes the PDF of every saved bill to the documents folder
        BillPdfAutoSaveHost(businessOf = billBusiness) { request ->
            val lang = if (LanguageManager.activeLanguageCode.startsWith("ta")) "ta" else "en"
            when (request.kind) {
                BillPdfAutoSave.Kind.Invoice -> TextilesInvoiceRepository.invoices.find { it.id == request.id }?.let { invoice ->
                    val profile = TextilesProfileRepository.profiles.find { it.id == invoice.companyId } ?: TextilesProfileRepository.profile
                    BillPdfJob(
                        PrintPayload.forTextilesInvoice(
                            invoice = invoice,
                            profile = profile,
                            customer = invoice.customerId?.let { TextilesCustomerRepository.getById(it) },
                            appLanguage = lang
                        ),
                        invoicePdfName(invoice),
                        billBusinessFolder(profile)
                    )
                }
                BillPdfAutoSave.Kind.Receipt -> TextilesReceiptRepository.receipts.find { it.id == request.id }?.let { receipt ->
                    val profile = TextilesProfileRepository.profiles.find { it.id == receipt.companyId } ?: TextilesProfileRepository.profile
                    BillPdfJob(
                        PrintPayload.forReceipt(
                            receipt = receipt,
                            profile = profile,
                            invoiceNumbers = TextilesReceiptRepository.getLinksForReceipt(receipt.id).mapNotNull { link ->
                                TextilesInvoiceRepository.invoices.find { it.id == link.invoiceId }?.invoiceNumber
                            },
                            isCoolie = false,
                            appLanguage = lang
                        ),
                        receiptPdfName(receipt),
                        billBusinessFolder(profile)
                    )
                }
            }
        }

        // Desktop windows get the sidebar; a phone/tablet turned sideways keeps the floating trailing-edge controls
        val isWideScreen = isDesktop && maxWidth >= 768.dp
        val isLandscape = com.elvan.udukkai.core.platform.platformLayoutPolicy.screenLayout(maxWidth, maxHeight) == com.elvan.udukkai.core.platform.ScreenLayout.AndroidLandscape
        SideEffect { isLandscapeMode = isLandscape }

        val shouldShowBottomBarForSnackbar = (activeSubpage !is ActiveSubpage.Settings) && !isWideScreen
        SideEffect {
            ElvanSnackbar.isBottomBarVisible = shouldShowBottomBarForSnackbar
        }


        @Composable
        fun HomeSearchBar() {
ElvanSearchBar(
                                        landscape = isLandscape,
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
                                                NavTab.Items -> {
                                                    TextilesItemRepository.searchQuery = newQuery
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
                                            TextilesItemRepository.searchQuery = ""
                                            TextilesCustomerRepository.searchQuery = ""
                                        },
                                        colors = colors
                                    )
        }

        @Composable
        fun RenderSubpage(subpage: ActiveSubpage) {
            when (subpage) {
                is ActiveSubpage.Settings -> {
                    TextilesSettingsScreen(
                        onBack = popSubpage
                    )
                }
                is ActiveSubpage.Reports -> {
                    TextilesReportsScreen(onBack = popSubpage)
                }
                is ActiveSubpage.RecycleBin -> {
                    TextilesRecycleBinScreen(
                        onBack = popSubpage
                    )
                }
                is ActiveSubpage.ItemEditor -> {
                    TextilesItemEditorScreen(
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
                    TextilesReceiptEditorScreen(
                        receipt = subpage.receipt,
                        forInvoice = subpage.forInvoice,
                        onBack = popSubpage
                    )
                }
                is ActiveSubpage.CustomerView -> {
                    val currentCustomer = TextilesCustomerRepository.customers.find { it.id == subpage.customer.id } ?: subpage.customer
                    CustomerViewScreen(
                        merchant = currentCustomer,
                       historyKey = TextilesInvoiceRepository.invoices to TextilesReceiptRepository.receipts,
                       loadHistory = { textilesCustomerActivity(currentCustomer) },
                       onReceiptClick = { id -> TextilesReceiptRepository.receipts.find { it.id == id }?.let { navigateToSubpage(ActiveSubpage.ReceiptView(it)) } },
                       onDelete = { com.elvan.udukkai.textiles.ui.screens.textilesDeleteCustomer(currentCustomer.id) },
                        onBillClick = { id -> TextilesInvoiceRepository.invoices.find { it.id == id }?.let { navigateToSubpage(ActiveSubpage.InvoiceView(it)) } },
                        profile = TextilesProfileRepository.profile,
                        allProfiles = TextilesProfileRepository.profiles,
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
                    val currentItem = TextilesItemRepository.items.find { it.id == subpage.item.id } ?: subpage.item
                    ItemViewScreen(
                        item = currentItem,
                       historyKey = TextilesInvoiceRepository.invoices to TextilesReceiptRepository.receipts,
                       loadHistory = { textilesItemActivity(currentItem) },
                       onDelete = { com.elvan.udukkai.textiles.ui.screens.textilesDeleteItem(currentItem.id) },
                        onBillClick = { id -> TextilesInvoiceRepository.invoices.find { it.id == id }?.let { navigateToSubpage(ActiveSubpage.InvoiceView(it)) } },
                        profile = TextilesProfileRepository.profile,
                        allProfiles = TextilesProfileRepository.profiles,
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
                    val currentInvoice = TextilesInvoiceRepository.invoices.find { it.id == subpage.invoice.id } ?: subpage.invoice
                    val invoiceLockedMsg = K.invoiceLockedDueToReceipt.tr()
                    InvoiceViewScreen(
                        invoice = currentInvoice,
                        profile = TextilesProfileRepository.profile,
                        allProfiles = TextilesProfileRepository.profiles,
                        isPaid = TextilesInvoiceRepository.hasLinkedReceipts(currentInvoice.id),
                        onDelete = { com.elvan.udukkai.textiles.ui.screens.textilesDeleteInvoice(currentInvoice) },
                        receipts = TextilesReceiptRepository.receiptsForInvoice(currentInvoice.id),
                        onOpenReceipt = { navigateToSubpage(ActiveSubpage.ReceiptView(it)) },
                        onRecordPayment = { navigateToSubpage(ActiveSubpage.ReceiptEditor(null, currentInvoice)) },
                        onOpenCustomer = currentInvoice.customerId?.let { id -> TextilesCustomerRepository.getById(id) }?.let { c -> { navigateToSubpage(ActiveSubpage.CustomerView(c)) } },
                        isFactory = false,
                        onBack = popSubpage,
                        onEdit = {
                            if (TextilesInvoiceRepository.hasLinkedReceipts(currentInvoice.id)) {
                                ElvanSnackbar.show(invoiceLockedMsg)
                            } else if (TextilesInvoiceRepository.isLocked(currentInvoice)) {
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
                                invoiceDate = DateUtils.todayMillis(),
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                            navigateToSubpage(ActiveSubpage.InvoiceEditor(converted))
                        },
                        printPayload = { activeProfile ->
                            PrintPayload.forTextilesInvoice(
                                invoice = currentInvoice,
                                profile = activeProfile,
                                customer = currentInvoice.customerId?.let { TextilesCustomerRepository.getById(it) },
                                appLanguage = if (LanguageManager.activeLanguageCode.startsWith("ta")) "ta" else "en"
                            )
                        }
                    )
                }
                is ActiveSubpage.ReceiptView -> {
                    val currentReceipt = TextilesReceiptRepository.receipts.find { it.id == subpage.receipt.id } ?: subpage.receipt
                    ReceiptViewScreen(
                        receipt = currentReceipt,
                        profile = TextilesProfileRepository.profile,
                        allProfiles = TextilesProfileRepository.profiles,
                        onDelete = { com.elvan.udukkai.textiles.ui.screens.textilesDeleteReceipt(currentReceipt) },
                        invoices = TextilesReceiptRepository.getLinksForReceipt(currentReceipt.id).mapNotNull { link -> TextilesInvoiceRepository.invoices.find { it.id == link.invoiceId } },
                        onOpenInvoice = { navigateToSubpage(ActiveSubpage.InvoiceView(it)) },
                        onOpenCustomer = currentReceipt.customerId?.let { id -> TextilesCustomerRepository.getById(id) }?.let { c -> { navigateToSubpage(ActiveSubpage.CustomerView(c)) } },
                        onBack = popSubpage,
                        onEdit = {
                            navigateToSubpage(ActiveSubpage.ReceiptEditor(currentReceipt))
                        },
                        printPayload = { activeProfile ->
                            PrintPayload.forReceipt(
                                receipt = currentReceipt,
                                profile = activeProfile,
                                invoiceNumbers = TextilesReceiptRepository.getLinksForReceipt(currentReceipt.id).mapNotNull { link ->
                                    TextilesInvoiceRepository.invoices.find { it.id == link.invoiceId }?.invoiceNumber
                                },
                                isCoolie = false,
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
                        NavTab.Items -> TextilesItemRepository.filteredItems.map { it.id }.toSet()
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
                    val updateBanner = com.elvan.udukkai.core.update.rememberAppUpdateBanner()
                    val activeBanners = remember(updateBanner, selectedTab) {
                        if (selectedTab == NavTab.Home && updateBanner != null) listOf(updateBanner) else emptyList()
                    }

                    ElvanShell(
                        scrollState = currentScrollState,
                        hideTitleSideways = true,
                        title = selectedTab.getLocalizedHeader(),
                        banners = activeBanners,
                        showNavbar = isSelectionMode || (!isLandscape && !isWideScreen),
                        hasActions = (!isSearchActive && !isSelectionMode),
                        leadingActions = null,
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
                                        contentDescription = K.a11yFilter.tr(),
                                        tint = if (isFilterActive) colors.accent else colors.textPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            // 3-Dot More Menu (மேலும்) - in actions for portrait only
                            if (isLandscape) {
                                ElvanTopBarIconButton(onClick = { navigateToSubpage(ActiveSubpage.Settings) }) {
                                    Icon(imageVector = MaterialSymbols.Rounded.Settings, contentDescription = K.settings.tr(), tint = colors.textPrimary, modifier = Modifier.size(22.dp))
                                }
                                ElvanTopBarIconButton(onClick = { navigateToSubpage(ActiveSubpage.RecycleBin) }) {
                                    Icon(imageVector = MaterialSymbols.Rounded.Delete, contentDescription = K.recycleBin.tr(), tint = colors.textPrimary, modifier = Modifier.size(22.dp))
                                }
                            } else {
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
                            }
                        },
                        navbar = {
                            val shellController = LocalElvanShellController.current

androidx.compose.animation.AnimatedContent(
                                targetState = isSelectionMode,
                                // Apple-style morph: the old pill softly shrinks and fades while the new one grows out of the same
                                // spot on a spring, overlapping, instead of one sliding away and the other sliding in
                                transitionSpec = {
                                    if (isLandscape) fadeIn(tween(180)).togetherWith(fadeOut(tween(140))) else {
                                    val origin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
                                    (androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(280, delayMillis = 50)) +
                                        androidx.compose.animation.scaleIn(androidx.compose.animation.core.spring(dampingRatio = 0.82f, stiffness = 380f), initialScale = 0.86f, transformOrigin = origin))
                                        .togetherWith(
                                            androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(170)) +
                                                androidx.compose.animation.scaleOut(androidx.compose.animation.core.tween(230), targetScale = 0.92f, transformOrigin = origin)
                                        )
                                        .using(androidx.compose.animation.SizeTransform(clip = false))
                                    }
                                },
                                label = "bottomBarSwap"
                            ) { selecting ->
                                                        if (selecting) {
                                val canCopyTab = selectedTab == NavTab.Items ||
                                                 selectedTab == NavTab.Customers ||
                                                 (selectedTab == NavTab.Create && uruvakkuSegment == 0)

                                val onSelectionCopy: (() -> Unit)? = if (canCopyTab) {
                                    {
                                        if (selectedItemIds.size == 1) {
                                            val selectedId = selectedItemIds.first()
                                            when (selectedTab) {
                                                NavTab.Items -> {
                                                    TextilesItemRepository.items.find { it.id == selectedId }?.let { copyItemAndEdit(it) }
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

                                Box(
                                    contentAlignment = Alignment.BottomCenter,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
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
                                }
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
                                                tabScrollJob?.cancel()
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
                                                tabScrollJob?.cancel()
                                                targetScrollState.requestScrollToItem(0, 0)
                                                selectedTab = tab
                                            }
                                        }
                                    )

                                    if (!isLandscape) HomeSearchBar()
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
                                            triggerAppUpdateCheck()
                                            getFirebaseSyncManager().triggerManualSync()
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
                                        onOpenReports = { navigateToSubpage(ActiveSubpage.Reports) },
                                        onInvoiceClick = { invoice ->
                                            navigateToSubpage(ActiveSubpage.InvoiceView(invoice))
                                        },
                                        onManageProfiles = {
                                            if (!openPlatformSettings(SettingsRoute.ManageProfiles)) {
                                                navigateToSubpage(ActiveSubpage.Settings)
                                            }
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
                                            triggerAppUpdateCheck()
                                            getFirebaseSyncManager().triggerManualSync()
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

                            NavTab.Items -> {
                                ExpressivePullToRefreshBox(
                                    enabled = !isSelectionMode,
                                    isRefreshing = isRefreshing,
                                    onRefresh = {
                                        scope.launch {
                                            isRefreshing = true
                                            triggerAppUpdateCheck()
                                            getFirebaseSyncManager().triggerManualSync()
                                            TextilesItemRepository.loadAll()
                                            delay(700)
                                            isRefreshing = false
                                        }
                                    },
                                    colors = colors
                                ) {
                                    TextilesItemScreen(
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
                                            triggerAppUpdateCheck()
                                            getFirebaseSyncManager().triggerManualSync()
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

        Row(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))) {
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
                            // Mobile tabs retain their viewport; forcing a remeasure here competes with navigation.
                                                if (isWideScreen) scope.launch { targetScrollState.scrollToItem(0, 0) }
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
            // ── Unified Adaptive Navigation (Zero split screen; full single-pane across portrait & landscape) ──
            androidx.compose.runtime.CompositionLocalProvider(com.elvan.udukkai.ui.components.shell.LocalElvanLandscapeChromeHandled provides isLandscape) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (isLandscape) Modifier.padding(start = 76.dp, end = if (subpageStack.isNotEmpty()) 80.dp else 0.dp) else Modifier)
            ) {
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
            }
            }

            if (isLandscape) {
                Box(Modifier.align(Alignment.TopCenter).fillMaxWidth().statusBarsPadding().padding(top = 8.dp, start = 72.dp, end = 80.dp)) { HomeSearchBar() }
            }

            // Floating Controls on the Trailing Edge in Landscape (Apple HIG Landscape Controls)
            if (isLandscape) androidx.compose.animation.AnimatedVisibility(
                visible = !isSelectionMode && !isSearchActive && com.elvan.udukkai.core.platform.getImeBottomPadding() == 0.dp,
                enter = fadeIn(tween(180)), exit = fadeOut(tween(120)), modifier = Modifier.fillMaxSize()
            ) { Box(Modifier.fillMaxSize()) {
                val landscapeActionAlpha by androidx.compose.animation.core.animateFloatAsState(
                    if (showDateFilterSheet || isSearchActive) 0f else 1f,
                    androidx.compose.animation.core.tween(240), label = "landscapeActionVisibility"
                )
                // Top-Right Action Button (Stationary at TopEnd)
                // Shows ArrowBack (<) when viewing a subpage; shows Add (+) on the main screen
                AnimatedContent(
                    targetState = subpageStack.isNotEmpty(),
                    transitionSpec = {
                        fadeIn(tween(180)).togetherWith(fadeOut(tween(140)))
                    },
                    modifier = Modifier.align(Alignment.TopStart).graphicsLayer {
                        alpha = landscapeActionAlpha
                        scaleX = .94f + .06f * landscapeActionAlpha
                        scaleY = .94f + .06f * landscapeActionAlpha
                    },
                    label = "TopActionButtonTransition"
                ) { hasSubpage ->
                    if (hasSubpage) {
                        LandscapeTopActionButton(
                            icon = MaterialSymbols.Rounded.ArrowBack,
                            contentDescription = K.back.tr(),
                            onClick = popSubpage,
                            colors = colors,
                            alignStart = true
                        )
                    } else {
                        LandscapeTopActionButton(
                            icon = MaterialSymbols.Rounded.Add,
                            contentDescription = K.add.tr(),
                            onClick = {
                                when (selectedTab) {
                                    NavTab.Home -> navigateToSubpage(ActiveSubpage.InvoiceEditor(null))
                                    NavTab.Items -> navigateToSubpage(ActiveSubpage.ItemEditor(null))
                                    NavTab.Customers -> navigateToSubpage(ActiveSubpage.MerchantEditor(null))
                                    NavTab.Create -> {
                                        if (uruvakkuSegment == 0) navigateToSubpage(ActiveSubpage.InvoiceEditor(null))
                                        else navigateToSubpage(ActiveSubpage.ReceiptEditor(null))
                                    }
                                }
                            },
                            colors = colors,
                            alignStart = true
                        )
                    }
                }

                // Bottom-Right Trailing Navigation Rail (Close to bottom, normal drag doesn't expand, long-press expands)
                if (subpageStack.isEmpty()) {
                    LandscapeSideNavBar(
                        selectedTab = selectedTab,
                        hideContent = isSearchActive || showDateFilterSheet,
                        morphWhenHidden = showDateFilterSheet,
                        onTabSelected = { tab, _ ->
                            if (selectedTab == tab) {
                                tabScrollJob?.cancel()
                                tabScrollJob = scope.launch { currentScrollState.smoothScrollToTop() }
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
                                tabScrollJob?.cancel()
                                targetScrollState.requestScrollToItem(0, 0)
                                selectedTab = tab
                            }
                        },
                        modifier = Modifier.align(Alignment.BottomStart).windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)).padding(bottom = 8.dp)
                    )
                }
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
                    bulkDelete = BulkDelete(selectedTab, uruvakkuSegment, selectedItemIds.toList())
                    showBulkDeleteConfirm = false
                },
                onDismissRequest = {
                    showBulkDeleteConfirm = false
                },
                colors = colors
            )
        }

        bulkDelete?.let { job ->
            val label = when (job.tab) {
                NavTab.Items -> K.itemDeleted.tr()
                NavTab.Customers -> K.customerDeleted.tr()
                NavTab.Create -> if (job.segment == 0) K.invoices.tr() else K.receipts.tr()
                else -> ""
            }
            ElvanBulkProgressSheet(
                title = "${job.ids.size} $label",
                ids = job.ids,
                colors = colors,
                process = { id ->
                    when (job.tab) {
                        NavTab.Items -> TextilesItemRepository.delete(id)
                        NavTab.Customers -> TextilesCustomerRepository.delete(id)
                        NavTab.Create -> if (job.segment == 0) TextilesInvoiceRepository.delete(id) else TextilesReceiptRepository.delete(id)
                        else -> false
                    }
                },
                onFinished = { deleted ->
                    when (job.tab) {
                        NavTab.Items -> ElvanSnackbar.show(itemDeletedMsg)
                        NavTab.Customers -> ElvanSnackbar.show(customerDeletedMsg)
                        NavTab.Create -> if (job.segment == 0) {
                            if (deleted > 0) ElvanSnackbar.show("$deleted $invoicesLabel $erasingLabel")
                            // Bills that have a receipt are paid and stay
                            if (deleted < job.ids.size) {
                                val inLockedYear = TextilesInvoiceRepository.invoices.any { it.id in job.ids && TextilesInvoiceRepository.isLocked(it) }
                                ElvanSnackbar.show(if (inLockedYear) booksLockedMsg else invoiceHasReceiptsMsg)
                            }
                        } else {
                            if (deleted > 0) ElvanSnackbar.show("$deleted $receiptsLabel $erasingLabel")
                            if (deleted < job.ids.size) ElvanSnackbar.show(booksLockedMsg)
                        }
                        else -> {}
                    }
                    isSelectionMode = false
                    selectedItemIds = emptySet()
                    bulkDelete = null
                }
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
}
