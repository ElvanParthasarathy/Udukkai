package com.elvan.udukkai.ui.screens.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.utils.CurrencyUtils
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.data.model.ItemModel
import com.elvan.udukkai.data.settings.DataLanguageDisplay
import com.elvan.udukkai.data.settings.BusinessProfile
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.LocalShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * Item View Screen (உருப்படி பார்வை): the item, what it has earned on bills, how it is priced, which businesses
 * offer it, and the latest bills it is on.
 */
@Composable
fun ItemViewScreen(
    item: ItemModel,
    profile: BusinessProfile,
    allProfiles: List<BusinessProfile>,
    isCoolie: Boolean = false,
    historyKey: Any? = null,
    loadHistory: (Int) -> ViewActivity = { ViewActivity() },
    historyYears: List<Int> = emptyList(),
    onBillClick: (Long) -> Unit = {},
    onReceiptClick: (Long) -> Unit = {},
    /** Kept by the caller, so the page comes back where it was after opening a bill. */
    scrollState: LazyListState = rememberLazyListState(),
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onCopy: (() -> Unit)? = null,
    onDelete: (() -> ViewDeleteResult)? = null
) {
    val colors = rememberShellColors()
    val financialYear = rememberViewFinancialYear(item.id, profile.id, historyYears)
    val year = financialYear.year
    val (history, retryHistory) = rememberViewHistory(
        "Item" to item.id,
        listOf(historyKey, year, profile.id, item.previousNames, profile.primaryLanguage, profile.secondaryLanguage, profile.isBilingual),
        cache = historyKey != null
    ) { loadHistory(year) }
    val activity = (history as? ViewHistoryState.Ready)?.activity ?: ViewActivity()
    val ready = history is ViewHistoryState.Ready
    val historyUi = rememberViewHistoryUi(history, K.noItemInvoices.tr(), withReceipts = false)
    historyUi.financialYear = financialYear
    androidx.compose.runtime.LaunchedEffect(year) { historyUi.dateRange = null to null }
    val deleteItem = rememberViewDeleteItem(onDelete, K.itemDeleted.tr(), onBack)

    val language = DataLanguageDisplay(profile)
    val primaryName = language.first(item.name).ifBlank { "-" }
    val secondaryName = language.second(item.name)

    fun companyName(id: Long): String {
        val p = allProfiles.find { it.id == id }
        return p?.shortName?.ifEmpty { language.first(p.businessName) }.orEmpty().ifEmpty { "#$id" }
    }
    val availability = if (item.companyIds.isEmpty()) K.allCompanies.tr()
    else item.companyIds.joinToString(", ") { companyName(it) }

    ViewRecordPage(
        title = K.itemDetails.tr(),
        onBack = onBack,
        onEdit = onEdit,
        onCopy = onCopy,
        menuItems = listOfNotNull(deleteItem),
        colors = colors,
        scrollState = scrollState,
        history = history,
        historyUi = historyUi,
        onBillClick = onBillClick,
        onReceiptClick = onReceiptClick,
        onRetryHistory = retryHistory,
        recordHistory = { ItemHistoryTimeline(item, language) },
        summary = {
            ViewHero(primary = primaryName, secondary = secondaryName, icon = MaterialSymbols.Rounded.Inventory2, tint = colors.itemColor, colors = colors)

            ViewFinancialYearButton(financialYear, colors)
            run {
                ViewStats(
                    loading = history is ViewHistoryState.Loading,
                    left = if (isCoolie) Triple(MaterialSymbols.Rounded.Straighten, K.weightBilled.tr(), activity.quantityText.ifEmpty { "-" })
                    else Triple(MaterialSymbols.Rounded.CurrencyRupeeCircle, K.totalBilledAmount.tr(), if (ready) CurrencyUtils.formatInr(activity.billed) else "-"),
                    right = Triple(MaterialSymbols.Rounded.InvoiceFill, K.invoices.tr(), if (ready) "${activity.count}" else "-"),
                    colors = colors
                )
            }

        }
    ) {
            if (!isCoolie) {
                run {
                    ViewInfoSection(
                        title = K.billingSection.tr(),
                        rows = listOf(
                            K.sellingRate.tr() to CurrencyUtils.formatInr(item.price),
                            K.gstRate.tr() to "${formatRate(item.taxRate)}%",
                            K.hsnSacCode.tr() to item.hsnCode,
                            K.measureType.tr() to if (item.quantityType == "weight") K.weight.tr() else K.quantity.tr(),
                            K.unit.tr() to item.unit
                        ),
                        colors = colors
                    )
                }
            }

            run {
                // The companies the item is for, one per line in a box of their own (no duplicate label beside the value)
                ViewAddressBox(
                    title = K.companies.tr(),
                    lines = if (item.companyIds.isEmpty()) listOf(availability) else item.companyIds.map { companyName(it) },
                    colors = colors,
                    maxLinesPerEntry = 2
                )
            }

            if (!isCoolie && item.companyPrices.isNotEmpty()) {
                run {
                    ViewInfoSection(
                        title = K.sellingRate.tr(),
                        rows = item.companyPrices.map { (id, price) -> companyName(id) to CurrencyUtils.formatInr(price) },
                        colors = colors,
                        labelMaxLines = 2
                    )
                }
            }
    }
}

/** 5.0 shows as 5, 2.5 stays 2.5. */
internal fun formatRate(rate: Double): String =
    if (rate % 1.0 == 0.0) rate.toLong().toString() else rate.toString()

@Composable
private fun ItemHistoryTimeline(item: ItemModel, language: DataLanguageDisplay) {
    data class Event(val label: String?, val timestamp: Long?, val names: List<Pair<String, Boolean>> = emptyList())

    val events = buildList {
        add(Event(K.itemHistoryCreated.tr(), item.createdAt))
        item.previousNames.sortedBy { it.changedAt ?: 0L }.forEach { revision ->
            val names = language.lines(revision.names)
            if (names.isNotEmpty()) {
                add(Event(null, revision.changedAt, names))
            }
        }
        if (item.updatedAt > item.createdAt) {
            add(Event(K.itemHistoryModified.tr(), item.updatedAt))
        }
    }
    val colors = LocalShellColors.current
    val font = LocalAppFontFamily.current
    val lineColor = colors.textTertiary.copy(alpha = 0.28f)
    Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp)) {
        events.forEachIndexed { index, (label, timestamp, names) ->
            Column(
                Modifier.fillMaxWidth().drawBehind {
                    val x = 7.dp.toPx()
                    val y = 23.dp.toPx()
                    if (index > 0) drawLine(lineColor, Offset(x, 0f), Offset(x, y), 1.dp.toPx())
                    if (index < events.lastIndex) drawLine(lineColor, Offset(x, y), Offset(x, size.height), 1.dp.toPx())
                    drawCircle(colors.accent, radius = 3.5.dp.toPx(), center = Offset(x, y))
                }.padding(start = 22.dp, top = 4.dp, bottom = 4.dp)
                    .background(colors.surface, RoundedCornerShape(Dimens.CardRadius))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (label != null) Text(label, fontFamily = font, fontSize = 13.sp, color = colors.textPrimary)
                names.forEach { (name, isPrimary) ->
                    Text(
                        text = name.preventBrokenLigatures(),
                        fontFamily = font,
                        fontSize = if (isPrimary) 14.sp else 12.sp,
                        fontWeight = if (isPrimary) FontWeight.Medium else FontWeight.Normal,
                        color = if (isPrimary) colors.textPrimary else colors.textSecondary
                    )
                }
                if (timestamp != null && timestamp > 0) {
                    val millis = if (timestamp < 100_000_000_000L) timestamp * 1000 else timestamp
                    Text(DateUtils.formatDateTime(millis), fontFamily = font, fontSize = 12.sp, color = colors.textSecondary)
                }
            }
        }
    }
}
