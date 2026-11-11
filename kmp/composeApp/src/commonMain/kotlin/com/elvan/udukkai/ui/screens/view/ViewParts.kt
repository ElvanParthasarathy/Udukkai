package com.elvan.udukkai.ui.screens.view

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.ElvanCommonCard
import com.elvan.udukkai.ui.components.shell.ElvanActionSheet
import com.elvan.udukkai.ui.components.shell.ElvanPopupMenuItem
import com.elvan.udukkai.ui.components.shell.ElvanSnackbar
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.finance.BillNumbering
import com.elvan.udukkai.core.utils.CurrencyUtils
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.ShellDefaults
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.ui.components.shell.ElvanSettingsDivider
import com.elvan.udukkai.ui.components.shell.ElvanSettingsSection
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.create.components.ActiveFilterPill
import com.elvan.udukkai.ui.screens.create.components.CreateDateFilterSheet
import com.elvan.udukkai.ui.screens.create.components.dateFilterLabel
import com.elvan.udukkai.ui.screens.home.components.ElvanStatsCard

/** One bill in a view page's "recent" list. */
data class ViewBillRow(
    val id: Long,
    val number: String,
    val date: Long,
    val amount: Double,
    val isPaid: Boolean,
    /** A short extra fact for this page, e.g. the weight of the item on that bill. */
    val note: String = "",
    /** Item pages: who the bill is for. */
    val customerName: String = "",
    /** Item pages: the whole bill, when [amount] is only this item's lines. */
    val invoiceTotal: Double? = null,
    /** From the bill's linked receipts. */
    val paidAmount: Double = 0.0,
    val balance: Double = 0.0,
    val kind: ViewRowKind = ViewRowKind.Invoice,
    /** Receipts: how it was paid. */
    val paymentMode: String = ""
)

/** What a view page shows about an item's or customer's bills. */
data class ViewActivity(
    val count: Int = 0,
    /** Money on the bills: the customer's bills, or the rows of this item on them. */
    val billed: Double = 0.0,
    /** Customers only: the sum of their bills that have no receipt yet. */
    val unpaid: Double = 0.0,
    /** Items only: the total weight or quantity billed, ready to show (e.g. "33.680 kg"). */
    val quantityText: String = "",
    val bills: List<ViewBillRow> = emptyList(),
    /** Customers: their receipts. */
    val receipts: List<ViewBillRow> = emptyList(),
    /** Proforma invoices, where the app has them. */
    val estimates: List<ViewBillRow> = emptyList()
)

/** The record's card: its round icon, then its name on one line with the second-language line in grey under it. */
@Composable
fun ViewHero(primary: String, secondary: String, icon: ImageVector, tint: androidx.compose.ui.graphics.Color, colors: ShellColors) {
    val ff = LocalAppFontFamily.current
    ElvanCommonCard(
        padding = PaddingValues(horizontal = com.elvan.udukkai.theme.Dimens.CardPaddingHorizontal, vertical = com.elvan.udukkai.theme.Dimens.CardPaddingVertical),
        borderRadius = com.elvan.udukkai.theme.Dimens.CardRadius
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            com.elvan.udukkai.ui.components.ElvanCardLeadingIcon(icon = icon, tint = tint)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = primary.preventBrokenLigatures(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = ff, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                )
                if (secondary.isNotEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = secondary.preventBrokenLigatures(),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = ff, fontSize = 13.5.sp, color = colors.textSecondary)
                    )
                }
            }
        }
    }
}

/** Two totals side by side, in the same tiles as the home page. */
@Composable
fun ViewStats(
    left: Triple<ImageVector, String, String>,
    right: Triple<ImageVector, String, String>,
    colors: ShellColors
) {
    if (LocalDocLandscape.current) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            for ((icon, label, value) in listOf(left, right)) {
                val hue = viewIconHue(icon, colors)
                ElvanStatsCard(icon = icon, label = label, value = value, colors = colors,
                    accentColor = hue, isFullWidth = true)
            }
        }
        return
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        for ((icon, label, value) in listOf(left, right)) {
            val hue = viewIconHue(icon, colors)
            ElvanStatsCard(
                icon = icon, label = label, value = value, colors = colors, modifier = Modifier.weight(1f),
                accentColor = hue,
                gradientColors = listOf(androidx.compose.ui.graphics.lerp(hue, androidx.compose.ui.graphics.Color.White, 0.3f), hue)
            )
        }
    }
}

/** Round actions under the hero (call, email), each a 56 dp circle with its label under it. */
@Composable
fun ViewQuickActions(actions: List<Triple<ImageVector, String, () -> Unit>>, colors: ShellColors) {
    if (actions.isEmpty()) return
    val ff = LocalAppFontFamily.current
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        actions.forEach { (icon, label, onClick) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ShellDefaults.ripple(colors, bounded = true),
                        role = Role.Button,
                        onClick = onClick
                    )
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                com.elvan.udukkai.ui.components.ElvanTintedIcon(icon = icon, tint = viewIconHue(icon, colors), size = 56.dp, iconSize = 24.dp)
                Spacer(Modifier.height(6.dp))
                Text(label.preventBrokenLigatures(), style = TextStyle(fontFamily = ff, fontSize = 13.sp, color = colors.textSecondary))
            }
        }
    }
}

/** Label-and-value rows in one rounded section with slit dividers (empty values are left out). */
@Composable
fun ViewInfoSection(title: String, rows: List<Pair<String, String>>, colors: ShellColors, labelMaxLines: Int = Int.MAX_VALUE) {
    val shown = rows.filter { it.second.isNotBlank() }
    if (shown.isEmpty()) return
    val ff = LocalAppFontFamily.current
    ElvanSettingsSection(title = title, colors = colors) {
        shown.forEachIndexed { index, (label, value) ->
            if (index > 0) ElvanSettingsDivider(colors = colors)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp)
                    .semantics(mergeDescendants = true) {},
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = label.preventBrokenLigatures(),
                    style = TextStyle(fontFamily = ff, fontSize = 14.sp, color = colors.textSecondary),
                    modifier = Modifier.weight(0.42f),
                    maxLines = labelMaxLines,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = value.preventBrokenLigatures(),
                    style = TextStyle(fontFamily = ff, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary, textAlign = TextAlign.End),
                    modifier = Modifier.weight(0.58f)
                )
            }
        }
    }
}

/** A postal address in one rounded box, left-aligned like on an envelope: one line per part (empty parts are skipped). */
@Composable
fun ViewAddressBox(title: String?, lines: List<String>, colors: ShellColors, maxLinesPerEntry: Int = Int.MAX_VALUE) {
    val shown = lines.filter { it.isNotBlank() }
    if (shown.isEmpty()) return
    val ff = LocalAppFontFamily.current
    ElvanSettingsSection(title = title, colors = colors) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp).semantics(mergeDescendants = true) {},
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            shown.forEach {
                Text(it.preventBrokenLigatures(), style = TextStyle(fontFamily = ff, fontSize = 16.sp, color = colors.textPrimary, lineHeight = 22.sp),
                    maxLines = maxLinesPerEntry, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

sealed interface ViewHistoryState {
    data object Loading : ViewHistoryState
    data class Ready(val activity: ViewActivity) : ViewHistoryState
    data object Failed : ViewHistoryState
}

/** Works out the page's bills off the main thread (an item has every bill's lines to read). Re-runs when [key] changes; the second value retries. */
@Composable
fun rememberViewHistory(subject: Any?, key: Any?, load: () -> ViewActivity): Pair<ViewHistoryState, () -> Unit> {
    var state by remember(subject, key) { mutableStateOf<ViewHistoryState>(ViewHistoryState.Loading) }
    var attempt by remember { mutableStateOf(0) }
    val latestLoad by rememberUpdatedState(load)
    LaunchedEffect(subject, key, attempt) {
        state = try {
            ViewHistoryState.Ready(withContext(Dispatchers.Default) { latestLoad() })
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            ViewHistoryState.Failed
        }
    }
    return state to { state = ViewHistoryState.Loading; attempt++ }
}

/** Document type, date range and sort order for the Transactions tab. */
class ViewHistoryUi {
    var tab by mutableStateOf(0)
    var dateRange by mutableStateOf<Pair<Long?, Long?>>(null to null)
    var sort by mutableStateOf(HistorySort.DateNew)
    var showFilter by mutableStateOf(false)
    var showSort by mutableStateOf(false)
    var showType by mutableStateOf(false)
    internal var tabs: List<HistoryTabInfo> = emptyList() // rebuilt with the page; not state, or setting it would recompose forever
}

internal class HistoryTabInfo(val item: com.elvan.udukkai.ui.screens.create.components.PillShifterItem, val rows: List<ViewBillRow>, val empty: String)

/**
 * Transactions use the same date picker as Make. Proformas appear with invoices, and customer pages also list receipts.
 */
@Composable
fun rememberViewHistoryUi(state: ViewHistoryState, emptyInvoices: String, withReceipts: Boolean): ViewHistoryUi {
    val ui = remember { ViewHistoryUi() }
    val activity = (state as? ViewHistoryState.Ready)?.activity ?: ViewActivity()
    val symbols = MaterialSymbols.Rounded
    val invoiceItem = com.elvan.udukkai.ui.screens.create.components.PillShifterItem(K.invoices.tr(), symbols.Invoice, symbols.InvoiceFill)
    val receiptItem = com.elvan.udukkai.ui.screens.create.components.PillShifterItem(K.receipts.tr(), symbols.Receipt, symbols.ReceiptFill)
    val noReceipts = K.noCustomerReceipts.tr()
    ui.tabs = buildList {
        add(HistoryTabInfo(invoiceItem, ViewHistory.withEstimates(activity.bills, activity.estimates), emptyInvoices))
        if (withReceipts) add(HistoryTabInfo(receiptItem, activity.receipts, noReceipts))
    }
    if (ui.tab > ui.tabs.lastIndex) ui.tab = 0

    val colors = rememberShellColors()
    if (ui.showFilter) {
        CreateDateFilterSheet(
            isOpen = true, currentStartMillis = ui.dateRange.first, currentEndMillis = ui.dateRange.second,
            onDismissRequest = { ui.showFilter = false },
            onApplyFilter = { start, end, _ -> ui.dateRange = start to end; ui.showFilter = false },
            colors = colors
        )
    }
    if (ui.showSort) {
        val labels = mapOf(HistorySort.DateNew to K.sortDateNew.tr(), HistorySort.DateOld to K.sortDateOld.tr(), HistorySort.NumberHigh to K.sortNumberHigh.tr(), HistorySort.NumberLow to K.sortNumberLow.tr())
        com.elvan.udukkai.ui.components.shell.ElvanSelectionBottomSheet(
            title = K.sortBy.tr(), items = HistorySort.entries, currentValue = ui.sort, colors = colors,
            onSelected = { ui.sort = it; ui.showSort = false }, onDismissRequest = { ui.showSort = false },
            itemLabelBuilder = { labels.getValue(it) }
        )
    }
    if (ui.showType) {
        com.elvan.udukkai.ui.components.shell.ElvanSelectionBottomSheet(
            title = K.transactionsTab.tr(), items = ui.tabs.indices.toList(), currentValue = ui.tab, colors = colors,
            onSelected = { ui.tab = it; ui.showType = false }, onDismissRequest = { ui.showType = false },
            itemLabelBuilder = { ui.tabs[it].item.label }
        )
    }
    return ui
}

/** The type and filter row, then the chosen type's rows, in the tab's own list so a long history stays smooth. */
fun LazyListScope.viewHistoryItems(
    state: ViewHistoryState,
    ui: ViewHistoryUi,
    colors: ShellColors,
    onBillClick: (Long) -> Unit,
    onReceiptClick: (Long) -> Unit,
    onRetry: () -> Unit,
    /** Blank room kept under the type row, so a short list leaves the page where it was. */
    minListHeight: androidx.compose.ui.unit.Dp,
    isLandscape: Boolean = false
) {
    val tabs = ui.tabs
    val tab = tabs.getOrNull(ui.tab) ?: return
    val (start, end) = ui.dateRange
    val rows = ViewHistory.apply(tab.rows, ui.sort, start, end)
    val filtered = start != null || end != null
    item(key = "history_tools") {
        Row(Modifier.fillMaxWidth().height(HISTORY_TOOLS_HEIGHT).padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(
                Modifier
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(50))
                    .clickable(enabled = tabs.size > 1) { ui.showType = true }
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = (if (state is ViewHistoryState.Ready) "${tab.item.label} (${rows.size})" else tab.item.label).preventBrokenLigatures(),
                    style = TextStyle(fontFamily = LocalAppFontFamily.current, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.3.sp, color = colors.textPrimary.copy(alpha = 0.55f))
                )
                if (tabs.size > 1) Icon(MaterialSymbols.Rounded.KeyboardArrowDown, null, tint = colors.textPrimary.copy(alpha = 0.55f), modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.weight(1f))
            com.elvan.udukkai.ui.components.shell.ElvanTopBarIconButton(onClick = { ui.showFilter = true }) {
                Icon(MaterialSymbols.Rounded.FilterList, K.a11yFilter.tr(), tint = if (filtered) colors.accent else colors.textPrimary, modifier = Modifier.size(20.dp))
            }
            com.elvan.udukkai.ui.components.shell.ElvanTopBarIconButton(onClick = { ui.showSort = true }) {
                Icon(MaterialSymbols.Rounded.Sort, K.sortBy.tr(), tint = if (ui.sort == HistorySort.DateNew) colors.textPrimary else colors.accent, modifier = Modifier.size(20.dp))
            }
        }
    }
    if (filtered) {
        item("history_active_filter") {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ActiveFilterPill(dateFilterLabel(start, end), colors) { ui.dateRange = null to null }
            }
        }
    }
    if (isLandscape) {
        val pairs = rows.chunked(2)
        if (state is ViewHistoryState.Ready && rows.size > SHORT_LIST_ROWS) {
            items(pairs, key = { "history_pair_${it.joinToString("_") { r -> "${r.kind}_${r.id}" }}" }) { pair ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(RECORD_GAP)) {
                    pair.forEach { row ->
                        Box(Modifier.weight(1f)) {
                            ViewBillCard(row, colors) { if (row.kind == ViewRowKind.Receipt) onReceiptClick(row.id) else onBillClick(row.id) }
                        }
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        } else item(key = "history_short") {
            Column(Modifier.fillMaxWidth().heightIn(min = minListHeight), verticalArrangement = Arrangement.spacedBy(RECORD_GAP)) {
                when {
                    state == ViewHistoryState.Loading -> repeat(3) { com.elvan.udukkai.ui.components.shell.ItemCardSkeleton() }
                    state == ViewHistoryState.Failed -> HistoryMessage(null, K.historyLoadFailed.tr(), colors) {
                        com.elvan.udukkai.ui.components.shell.ElvanActionButton(label = K.retryBtn.tr(), onClick = onRetry, reachPillEdge = false)
                    }
                    rows.isEmpty() -> HistoryMessage(tab.item.icon, if (filtered) K.noHistoryInFilter.tr() else tab.empty, colors)
                    else -> pairs.forEach { pair ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(RECORD_GAP)) {
                            pair.forEach { row ->
                                Box(Modifier.weight(1f)) {
                                    ViewBillCard(row, colors) { if (row.kind == ViewRowKind.Receipt) onReceiptClick(row.id) else onBillClick(row.id) }
                                }
                            }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    } else {
        if (state is ViewHistoryState.Ready && rows.size > SHORT_LIST_ROWS) {
            items(rows, key = { "history_${it.kind}_${it.id}" }, contentType = { "bill" }) { row ->
                ViewBillCard(row, colors) { if (row.kind == ViewRowKind.Receipt) onReceiptClick(row.id) else onBillClick(row.id) }
            }
        } else item(key = "history_short") {
            // A short list is one block with a minimum height: the blank space under it holds the page still
            Column(Modifier.fillMaxWidth().heightIn(min = minListHeight), verticalArrangement = Arrangement.spacedBy(RECORD_GAP)) {
                when {
                    state == ViewHistoryState.Loading -> repeat(3) { com.elvan.udukkai.ui.components.shell.ItemCardSkeleton() }
                    state == ViewHistoryState.Failed -> HistoryMessage(null, K.historyLoadFailed.tr(), colors) {
                        com.elvan.udukkai.ui.components.shell.ElvanActionButton(label = K.retryBtn.tr(), onClick = onRetry, reachPillEdge = false)
                    }
                    rows.isEmpty() -> HistoryMessage(tab.item.icon, if (filtered) K.noHistoryInFilter.tr() else tab.empty, colors)
                    else -> rows.forEach { row ->
                        androidx.compose.runtime.key(row.kind, row.id) {
                            ViewBillCard(row, colors) { if (row.kind == ViewRowKind.Receipt) onReceiptClick(row.id) else onBillClick(row.id) }
                        }
                    }
                }
            }
        }
    }
}

/** More rows than this always pass a screen, so they stay lazy and need no blank room. */
private const val SHORT_LIST_ROWS = 12

internal val HISTORY_TOOLS_HEIGHT = 44.dp


@Composable
private fun HistoryMessage(icon: androidx.compose.ui.graphics.vector.ImageVector?, text: String, colors: ShellColors, action: (@Composable () -> Unit)? = null) {
    // The blank state of the Create page: an icon and a line, in open space
    Column(Modifier.fillMaxWidth().padding(vertical = 56.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        if (icon != null) {
            Icon(icon, null, tint = colors.textQuaternary, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(14.dp))
        }
        Text(text.preventBrokenLigatures(), style = TextStyle(fontFamily = LocalAppFontFamily.current, fontSize = 15.sp, color = colors.textTertiary, textAlign = TextAlign.Center))
        if (action != null) { Spacer(Modifier.height(8.dp)); action() }
    }
}

/** A bill on a customer or item page. */
@Composable
private fun ViewBillCard(bill: ViewBillRow, colors: ShellColors, onClick: () -> Unit) {
    val ff = LocalAppFontFamily.current
    val status = when (bill.kind) {
        ViewRowKind.Receipt -> com.elvan.udukkai.data.model.PaymentType.fromStored(bill.paymentMode).label()
        ViewRowKind.Estimate -> K.proforma.tr() // as on the home cards
        ViewRowKind.Invoice -> if (bill.isPaid) K.statusPaid.tr() else K.statusUnpaid.tr()
    }
    val dateStr = DateUtils.formatEpochMillis(bill.date)
    val amountStr = CurrencyUtils.formatInr(bill.amount)
    val partlyPaid = bill.kind == ViewRowKind.Invoice && bill.paidAmount > 0.0 && bill.balance > 0.004
    val paidLine = if (partlyPaid) "${K.statusPaid.tr()} ${CurrencyUtils.formatInr(bill.paidAmount)}  •  ${K.balanceDue.tr()} ${CurrencyUtils.formatInr(bill.balance)}" else ""
    val totalLine = bill.invoiceTotal?.let { "${K.invoiceTotalLabel.tr()} ${CurrencyUtils.formatInr(it)}" }.orEmpty()
    val secondary = TextStyle(fontFamily = ff, fontSize = 13.sp, color = colors.textSecondary)
    ElvanCommonCard(
        onClick = onClick,
        padding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = listOf(bill.number, dateStr, bill.customerName, bill.note, amountStr, status, paidLine, totalLine).filter { it.isNotBlank() }.joinToString(", ")
        }
    ) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(bill.number, style = TextStyle(fontFamily = ff, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("FY ${BillNumbering.fyShort(bill.date)}", modifier = Modifier.padding(start = 8.dp).weight(1f), style = TextStyle(fontFamily = ff, fontSize = 12.5.sp, color = colors.textTertiary), maxLines = 1)
                Spacer(Modifier.width(8.dp))
                Text(amountStr, style = TextStyle(fontFamily = ff, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = colors.textPrimary), maxLines = 1)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(listOf(dateStr, bill.note).filter { it.isNotBlank() }.joinToString("  •  ").preventBrokenLigatures(), style = secondary, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                Text(status.preventBrokenLigatures(), style = TextStyle(fontFamily = ff, fontSize = 12.sp, color = colors.textSecondary), maxLines = 1)
            }
            if (bill.customerName.isNotBlank()) Text(bill.customerName.preventBrokenLigatures(), style = secondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (paidLine.isNotBlank()) Text(paidLine.preventBrokenLigatures(), style = secondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (totalLine.isNotBlank()) Text(totalLine.preventBrokenLigatures(), style = TextStyle(fontFamily = ff, fontSize = 12.5.sp, color = colors.textTertiary), maxLines = 1)
        }
    }
}

/**
 * Delete from a record's own page: the list's rules, with a confirmation first. Shows the sheet itself and returns the
 * menu row, or null when this page has no delete. [onDone] leaves the page once the record has gone.
 */
@Composable
fun rememberViewDeleteItem(onDelete: (() -> ViewDeleteResult)?, deletedMessage: String, onDone: () -> Unit): ElvanPopupMenuItem? {
    if (onDelete == null) return null
    val colors = rememberShellColors()
    var confirming by remember { mutableStateOf(false) }
    val hasReceiptsMsg = K.invoiceHasReceipts.tr()
    val booksLockedMsg = K.booksLockedMsg.tr()
    val failedMsg = K.error.tr()
    if (confirming) {
        ElvanActionSheet(
            title = K.deleteRecordPrompt.tr(),
            cancelText = K.cancelBtn.tr(),
            confirmText = K.deleteBtn.tr(),
            confirmColor = colors.danger,
            colors = colors,
            onDismissRequest = { confirming = false },
            onConfirm = {
                confirming = false
                when (onDelete()) {
                    ViewDeleteResult.Deleted -> { ElvanSnackbar.show(deletedMessage); onDone() }
                    ViewDeleteResult.HasReceipts -> ElvanSnackbar.show(hasReceiptsMsg)
                    ViewDeleteResult.BooksLocked -> ElvanSnackbar.show(booksLockedMsg)
                    ViewDeleteResult.Failed -> ElvanSnackbar.show(failedMsg)
                }
            }
        )
    }
    return ElvanPopupMenuItem(K.deleteBtn.tr(), MaterialSymbols.Rounded.Delete, { confirming = true }, isDestructive = true)
}

/** The colour a view page gives an icon: money green, bills blue, unpaid amber, weight amber, call green, mail blue. */
private fun viewIconHue(icon: ImageVector, colors: ShellColors): androidx.compose.ui.graphics.Color = with(MaterialSymbols.Rounded) {
    when (icon) {
        CurrencyRupeeCircle -> colors.receiptColor
        InvoiceFill -> colors.invoiceColor
        Receipt -> colors.itemColor
        Straighten -> colors.itemColor
        Call -> colors.receiptColor
        Email -> colors.invoiceColor
        else -> colors.customerColor
    }
}
