package com.elvan.udukkai.ui.screens.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.platform.AppPredictiveBackHandler
import com.elvan.udukkai.core.finance.InvoicePaymentFilter
import com.elvan.udukkai.core.utils.DateGroupUtils
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.core.utils.SearchHelper
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.model.ReceiptModel
import com.elvan.udukkai.factory.data.FactoryInvoiceRepository
import com.elvan.udukkai.factory.data.FactoryReceiptRepository
import com.elvan.udukkai.factory.data.FactoryProfileRepository
import com.elvan.udukkai.factory.data.FactoryCustomerRepository
import com.elvan.udukkai.factory.ui.screens.factoryDeleteInvoice
import com.elvan.udukkai.factory.ui.screens.factoryDeleteReceipt
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.textiles.data.TextilesInvoiceRepository
import com.elvan.udukkai.textiles.data.TextilesReceiptRepository
import com.elvan.udukkai.textiles.data.TextilesProfileRepository
import com.elvan.udukkai.textiles.data.TextilesCustomerRepository
import com.elvan.udukkai.textiles.ui.screens.textilesDeleteInvoice
import com.elvan.udukkai.textiles.ui.screens.textilesDeleteReceipt
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.ElvanCardLeadingIcon
import com.elvan.udukkai.ui.components.ElvanCommonCard
import com.elvan.udukkai.ui.components.dragSelect
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.ElvanSearchBar
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.create.components.*
import com.elvan.udukkai.ui.screens.view.ViewDeleteResult

@Composable
internal fun FinancialYearsScreen(
    year: Int?,
    isFactory: Boolean,
    onBack: () -> Unit,
    onOpenYear: (Int) -> Unit,
    onOpenInvoice: (InvoiceModel) -> Unit,
    onOpenReceipt: (ReceiptModel) -> Unit
) {
    val colors = rememberShellColors()
    val state = rememberLazyListState()
    val activeInvoices = if (isFactory) FactoryInvoiceRepository.activeCompanyInvoices else TextilesInvoiceRepository.activeCompanyInvoices
    val activeReceipts = if (isFactory) FactoryReceiptRepository.activeCompanyReceipts else TextilesReceiptRepository.activeCompanyReceipts
    val currentYear = DateUtils.financialYearStart(DateUtils.todayMillis())
    val invoicesByYear = remember(activeInvoices) { activeInvoices.groupBy { DateUtils.financialYearStart(it.invoiceDate) } }
    val receiptsByYear = remember(activeReceipts) { activeReceipts.groupBy { DateUtils.financialYearStart(it.receiptDate) } }
    val pastYears = remember(invoicesByYear, receiptsByYear, currentYear) {
        (invoicesByYear.keys + receiptsByYear.keys).filter { it < currentYear }.sortedDescending()
    }
    var segment by rememberSaveable(year) { mutableIntStateOf(0) }
    var searchOpen by rememberSaveable(year) { mutableStateOf(false) }
    var query by rememberSaveable(year) { mutableStateOf("") }
    var start by rememberSaveable(year) { mutableStateOf<Long?>(null) }
    var end by rememberSaveable(year) { mutableStateOf<Long?>(null) }
    var filterOpen by remember { mutableStateOf(false) }
    var dateFilterOpen by remember { mutableStateOf(false) }
    var payment by rememberSaveable(year) { mutableStateOf(InvoicePaymentFilter.All) }
    var selected by remember(year, segment) { mutableStateOf(emptySet<Long>()) }
    var confirmDelete by remember { mutableStateOf(false) }
    val selection = selected.isNotEmpty()
    val dateFiltered = start != null || end != null
    val filtered = dateFiltered || (segment == 0 && payment != InvoicePaymentFilter.All)
    fun inRange(date: Long) = (start?.let { date >= it } ?: true) && (end?.let { date <= it } ?: true)
    fun resetSearch() {
        searchOpen = false
        query = ""
    }
    fun back() {
        when {
            selection -> selected = emptySet()
            searchOpen -> resetSearch()
            else -> onBack()
        }
    }
    fun toggle(id: Long) {
        selected = if (id in selected) selected - id else selected + id
    }
    val invoices = remember(invoicesByYear, year, start, end, query, payment, activeReceipts, isFactory) {
        invoicesByYear[year].orEmpty().filter { invoice ->
            inRange(invoice.invoiceDate) && SearchHelper.matchesInvoice(invoice, query) &&
                payment.matches(invoice.invoiceType != "proforma") {
                    if (isFactory) FactoryInvoiceRepository.hasLinkedReceipts(invoice.id)
                    else TextilesInvoiceRepository.hasLinkedReceipts(invoice.id)
                }
        }
    }
    val receipts = remember(receiptsByYear, year, start, end, query, isFactory, activeInvoices) {
        receiptsByYear[year].orEmpty().filter {
            inRange(it.receiptDate) && SearchHelper.matchesReceipt(it, query, when {
                query.isBlank() -> emptyList()
                isFactory -> FactoryReceiptRepository.getLinkedInvoiceNumbers(it.id)
                else -> TextilesReceiptRepository.getLinkedInvoiceNumbers(it.id)
            })
        }
    }
    val invoiceGroups = remember(invoices) { DateGroupUtils.groupItemsByDate(invoices) { it.invoiceDate } }
    val receiptGroups = remember(receipts) { DateGroupUtils.groupItemsByDate(receipts) { it.receiptDate } }
    val visibleIds = remember(invoices, receipts, segment) {
        if (segment == 0) invoices.map { it.id }.toSet() else receipts.map { it.id }.toSet()
    }
    val tabs = listOf(
        PillShifterItem(K.invoices.tr(), MaterialSymbols.Rounded.Invoice, MaterialSymbols.Rounded.InvoiceFill),
        PillShifterItem(K.receipts.tr(), MaterialSymbols.Rounded.Receipt, MaterialSymbols.Rounded.ReceiptFill)
    )
    AppPredictiveBackHandler(enabled = selection || searchOpen) { back() }
    ElvanShell(
        scrollState = state,
        title = year?.let(::financialYearTitle) ?: K.financialYears.tr(),
        onBack = ::back,
        showNavbar = searchOpen,
        hasFloatingBar = selection,
        hasActions = !searchOpen && !selection,
        isSearchActive = searchOpen,
        isSelectionMode = selection,
        selectedCount = selected.size,
        isAllSelected = selected == visibleIds,
        onSelectAll = { selected = if (selected == visibleIds) emptySet() else visibleIds },
        onCancelSelection = { selected = emptySet() },
        actions = {
            ElvanTopBarIconButton(onClick = { searchOpen = true }) {
                Icon(MaterialSymbols.Rounded.Search, K.search.tr(), tint = colors.textPrimary)
            }
            if (year != null) ElvanTopBarIconButton(onClick = {
                if (segment == 0) filterOpen = true else dateFilterOpen = true
            }) {
                Icon(MaterialSymbols.Rounded.FilterList, K.a11yFilter.tr(), tint = if (filtered) colors.accent else colors.textPrimary)
            }
        },
        navbar = {
            ElvanSearchBar(searchOpen, query, { query = it; state.requestScrollToItem(0) }, ::resetSearch, colors)
        },
        floatingBar = {
            if (selection) ElvanTopBarIconButton(onClick = { confirmDelete = true }) {
                Icon(MaterialSymbols.Rounded.Delete, K.delete.tr(), tint = colors.textPrimary)
            }
        }
    ) {
        if (year == null) {
            val years = pastYears.filter { financialYearTitle(it).contains(query.trim(), ignoreCase = true) }
            ElvanLazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = state,
                verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacing)
            ) {
                item("shell_top_spacer") { Spacer(Modifier.height(LocalElvanTopSpacerHeight.current)) }
                if (years.isEmpty()) item { ArchiveEmpty() }
                items(years, key = { it }) { bookYear ->
                    FinancialYearBookCard(
                        title = financialYearTitle(bookYear),
                        invoices = invoicesByYear[bookYear].orEmpty().size,
                        receipts = receiptsByYear[bookYear].orEmpty().size,
                        colors = colors,
                        onClick = { onOpenYear(bookYear) }
                    )
                }
            }
        } else {
            val landscape = LocalLandscapeContent.current
            CreateDocumentList(
                state = state,
                listModifier = Modifier.dragSelect(state, { it in selected }, { selected = selected + it }, { selected = selected - it }),
                contentPadding = PaddingValues(bottom = Dimens.SubpageContentPaddingBottom),
                landscape = landscape,
                colors = colors,
                groups = if (segment == 0) invoiceGroups else receiptGroups,
                filter = if (filtered) {
                    { Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        val label = listOfNotNull(
                            if (dateFiltered) dateFilterLabel(start, end) else null,
                            if (segment == 0 && payment != InvoicePaymentFilter.All) payment.label() else null
                        ).joinToString("  •  ")
                        ActiveFilterPill(label, colors) {
                            start = null
                            end = null
                            if (segment == 0) payment = InvoicePaymentFilter.All
                        }
                    } }
                } else null,
                tabs = {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        ElvanPillShifter(tabs, segment, { if (segment != it) { state.requestScrollToItem(0); segment = it } }, colors, isFullWidth = false)
                    }
                }
            ) {
                if (segment == 0) {
                    if (invoiceGroups.isEmpty()) item { ArchiveEmpty() }
                    invoiceGroups.forEach { group ->
                        item("date_hdr_inv_${group.dayKey}") { DateSectionHeader(group.dateMillis, colors) }
                        items(group.items, key = { "invoice_${it.data.id}" }) { row ->
                            val invoice = row.data
                            val click = { if (selection) toggle(invoice.id) else onOpenInvoice(invoice) }
                            val hold = { toggle(invoice.id) }
                            val modifier = Modifier.padding(horizontal = Dimens.ContentPadding)
                            if (isFactory) {
                                FactoryInvoiceCard(row.globalIndex, invoice, colors, modifier, isSelectionMode = selection,
                                    isSelected = invoice.id in selected, onClick = click, onLongClick = hold)
                            } else {
                                TextilesInvoiceCard(row.globalIndex, invoice, colors, modifier, isSelectionMode = selection,
                                    isSelected = invoice.id in selected, onClick = click, onLongClick = hold)
                            }
                        }
                    }
                } else {
                    if (receiptGroups.isEmpty()) item { ArchiveEmpty() }
                    receiptGroups.forEach { group ->
                        item("date_hdr_rec_${group.dayKey}") { DateSectionHeader(group.dateMillis, colors) }
                        items(group.items, key = { "receipt_${it.data.id}" }) { row ->
                            val receipt = row.data
                            val profiles = if (isFactory) FactoryProfileRepository.profiles else TextilesProfileRepository.profiles
                            val profile = if (isFactory) FactoryProfileRepository.profile else TextilesProfileRepository.profile
                            val customer = receipt.customerId?.let {
                                if (isFactory) FactoryCustomerRepository.getById(it) else TextilesCustomerRepository.getById(it)
                            }
                            ReceiptCard(
                                row.globalIndex, receipt, colors, Modifier.padding(horizontal = Dimens.ContentPadding),
                                isSelectionMode = selection, isSelected = receipt.id in selected,
                                onClick = { if (selection) toggle(receipt.id) else onOpenReceipt(receipt) },
                                onLongClick = { toggle(receipt.id) },
                                profile = profile, allProfiles = profiles, customerCity = customer?.city.orEmpty()
                            )
                        }
                    }
                }
            }
        }
    }
    if (filterOpen) {
        DocumentFilterSheet(
            payment = payment,
            onPayment = { payment = it; state.requestScrollToItem(0); filterOpen = false },
            start = start,
            end = end,
            onDate = { filterOpen = false; dateFilterOpen = true },
            onDismiss = { filterOpen = false },
            colors = colors
        )
    }
    CreateDateFilterSheet(dateFilterOpen, start, end, { dateFilterOpen = false }, { newStart, newEnd, _ ->
        start = newStart
        end = newEnd
        dateFilterOpen = false
        selected = emptySet()
        state.requestScrollToItem(0)
    }, colors = colors, financialYear = year)
    if (confirmDelete) {
        val deletedLabel = (if (segment == 0) K.invoiceDeletedMsg else K.receiptDeletedMsg).tr()
        val linkedLabel = K.invoiceHasReceipts.tr()
        val lockedLabel = K.booksLockedMsg.tr()
        val failedLabel = K.error.tr()
        ElvanActionSheet(
            title = "${K.deleteRecordPrompt.tr()} (${selected.size})",
            onDismissRequest = { confirmDelete = false },
            onConfirm = {
                val results = if (segment == 0) invoices.filter { it.id in selected }.map {
                    if (isFactory) factoryDeleteInvoice(it) else textilesDeleteInvoice(it)
                } else receipts.filter { it.id in selected }.map {
                    if (isFactory) factoryDeleteReceipt(it) else textilesDeleteReceipt(it)
                }
                val deleted = results.count { it == ViewDeleteResult.Deleted }
                val messages = buildList {
                    if (deleted > 0) add(if (deleted == 1) deletedLabel else "$deleted · $deletedLabel")
                    if (ViewDeleteResult.HasReceipts in results) add(linkedLabel)
                    if (ViewDeleteResult.BooksLocked in results) add(lockedLabel)
                    if (ViewDeleteResult.Failed in results) add(failedLabel)
                }
                if (messages.isNotEmpty()) ElvanSnackbar.show(messages.joinToString(" · "))
                selected = emptySet()
                confirmDelete = false
            },
            confirmText = K.delete.tr(),
            confirmColor = colors.danger
        )
    }
}

private fun financialYearTitle(year: Int) = "$year–${((year + 1) % 100).toString().padStart(2, '0')}"

@Composable
private fun FinancialYearBookCard(title: String, invoices: Int, receipts: Int, colors: ShellColors, onClick: () -> Unit) {
    ElvanCommonCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Dimens.ContentPadding),
        padding = PaddingValues(16.dp),
        borderRadius = 24.dp,
        onClick = onClick
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            ElvanCardLeadingIcon(MaterialSymbols.Rounded.Calendar, tint = colors.invoiceColor)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    title,
                    color = colors.textPrimary,
                    fontFamily = LocalAppFontFamily.current,
                    fontSize = 16.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    BookDocumentCount(MaterialSymbols.Rounded.Invoice, invoices, K.invoices.tr(), colors)
                    BookDocumentCount(MaterialSymbols.Rounded.Receipt, receipts, K.receipts.tr(), colors)
                }
            }
            Icon(MaterialSymbols.Rounded.ChevronRight, null, tint = colors.border, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun BookDocumentCount(icon: ImageVector, count: Int, label: String, colors: ShellColors) {
    Row(
        modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = "$count $label" },
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = colors.textTertiary, modifier = Modifier.size(15.dp))
        Text(count.toString(), color = colors.textSecondary, fontFamily = LocalAppFontFamily.current, fontSize = 13.sp)
    }
}

@Composable
private fun ArchiveEmpty() {
    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
        Text(K.noResults.tr(), color = rememberShellColors().textSecondary)
    }
}
