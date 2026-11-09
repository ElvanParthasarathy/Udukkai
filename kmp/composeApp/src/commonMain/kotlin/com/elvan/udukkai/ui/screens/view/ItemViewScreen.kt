package com.elvan.udukkai.ui.screens.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.core.utils.CurrencyUtils
import com.elvan.udukkai.data.model.ItemModel
import com.elvan.udukkai.data.settings.BusinessProfile
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
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
    loadHistory: () -> ViewActivity = { ViewActivity() },
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
    val (history, retryHistory) = rememberViewHistory(item.id, historyKey, loadHistory)
    val activity = (history as? ViewHistoryState.Ready)?.activity ?: ViewActivity()
    val ready = history is ViewHistoryState.Ready
    val historyUi = rememberViewHistoryUi(history, K.noItemInvoices.tr(), withReceipts = false)
    val deleteItem = rememberViewDeleteItem(onDelete, K.itemDeleted.tr(), onBack)

    val isBilingual = profile.isBilingual
    val primaryLang = profile.primaryLanguage.ifEmpty { "ta" }
    val secondaryLang = profile.secondaryLanguage.ifEmpty { "en" }

    val p1 = item.name[primaryLang] ?: item.name["ta"] ?: ""
    val p2 = item.name[secondaryLang] ?: item.name["en"] ?: ""
    val primaryName = p1.ifEmpty { p2.ifEmpty { "-" } }
    val secondaryName = if (isBilingual && p1.isNotEmpty() && p2.isNotEmpty() && p1 != p2) p2 else ""

    fun companyName(id: Long): String {
        val p = allProfiles.find { it.id == id }
        return p?.shortName?.ifEmpty { p.businessName.values.firstOrNull().orEmpty() }.orEmpty().ifEmpty { "#$id" }
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
        onRetryHistory = retryHistory
    ) {
            ViewHero(primary = primaryName, secondary = secondaryName, icon = MaterialSymbols.Rounded.Inventory2, tint = colors.itemColor, colors = colors)

            run {
                ViewStats(
                    left = if (isCoolie) Triple(MaterialSymbols.Rounded.Straighten, K.weightBilled.tr(), activity.quantityText.ifEmpty { "-" })
                    else Triple(MaterialSymbols.Rounded.CurrencyRupeeCircle, K.totalBilledAmount.tr(), if (ready) CurrencyUtils.formatInr(activity.billed) else "-"),
                    right = Triple(MaterialSymbols.Rounded.InvoiceFill, K.invoices.tr(), if (ready) "${activity.count}" else "-"),
                    colors = colors
                )
            }

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
                    colors = colors
                )
            }

            if (!isCoolie && item.companyPrices.isNotEmpty()) {
                run {
                    ViewInfoSection(
                        title = K.sellingRate.tr(),
                        rows = item.companyPrices.map { (id, price) -> companyName(id) to CurrencyUtils.formatInr(price) },
                        colors = colors
                    )
                }
            }
    }
}

/** 5.0 shows as 5, 2.5 stays 2.5. */
internal fun formatRate(rate: Double): String =
    if (rate % 1.0 == 0.0) rate.toLong().toString() else rate.toString()
