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
import com.elvan.udukkai.ui.components.shell.LocalElvanTopSpacerHeight
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
    activity: ViewActivity = ViewActivity(),
    onBillClick: (Long) -> Unit = {},
    /** Kept by the caller, so the page comes back where it was after opening a bill. */
    scrollState: LazyListState = rememberLazyListState(),
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onCopy: (() -> Unit)? = null
) {
    val colors = rememberShellColors()

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
    val availability = if (item.companyIds.isEmpty()) "${K.all.tr()} ${K.companies.tr()}"
    else item.companyIds.joinToString(", ") { companyName(it) }

    ViewTile(
        title = K.itemDetails.tr(),
        onBack = onBack,
        scrollState = scrollState,
        onEdit = onEdit,
        onCopy = onCopy
    ) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = Dimens.ContentPadding, end = Dimens.ContentPadding, bottom = Dimens.SubpageContentPaddingBottom + 64.dp),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
        ) {
            item(key = "top_spacer") { Spacer(Modifier.height(LocalElvanTopSpacerHeight.current)) }

            item(key = "hero") {
                ViewHero(primary = primaryName, secondary = secondaryName, facts = "", colors = colors)
            }

            item(key = "stats") {
                ViewStats(
                    left = if (isCoolie) Triple(MaterialSymbols.Rounded.Straighten, K.weightBilled.tr(), activity.quantityText.ifEmpty { "-" })
                    else Triple(MaterialSymbols.Rounded.CurrencyRupeeCircle, K.totalBilledAmount.tr(), CurrencyUtils.formatInr(activity.billed)),
                    right = Triple(MaterialSymbols.Rounded.InvoiceFill, K.invoices.tr(), "${activity.count}"),
                    colors = colors
                )
            }

            if (!isCoolie) {
                item(key = "pricing") {
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

            item(key = "companies") {
                ViewInfoSection(
                    title = K.companies.tr(),
                    rows = listOf(K.companies.tr() to availability) +
                        if (isCoolie) emptyList()
                        else item.companyPrices.map { (id, price) -> "${companyName(id)} ${K.sellingRate.tr()}" to CurrencyUtils.formatInr(price) },
                    colors = colors
                )
            }

            item(key = "bills") {
                ViewBillsSection(title = K.recentBills.tr(), bills = activity.bills, colors = colors, onBillClick = onBillClick)
            }
        }
    }
}

/** 5.0 shows as 5, 2.5 stays 2.5. */
internal fun formatRate(rate: Double): String =
    if (rate % 1.0 == 0.0) rate.toLong().toString() else rate.toString()
