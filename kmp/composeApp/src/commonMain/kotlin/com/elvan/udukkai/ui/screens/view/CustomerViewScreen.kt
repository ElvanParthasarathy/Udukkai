package com.elvan.udukkai.ui.screens.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.core.platform.openDialer
import com.elvan.udukkai.core.platform.openMailTo
import com.elvan.udukkai.core.utils.CurrencyUtils
import com.elvan.udukkai.data.model.CustomerModel
import com.elvan.udukkai.data.settings.BusinessProfile
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.LocalElvanTopSpacerHeight
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * Customer / Merchant View Screen (வாங்குநர் பார்வை): the customer, what they have been billed and still owe,
 * call and mail in one tap, their contact and address, and the latest bills.
 */
@Composable
fun CustomerViewScreen(
    merchant: CustomerModel,
    profile: BusinessProfile,
    allProfiles: List<BusinessProfile> = emptyList(),
    showGst: Boolean = true,
    activity: ViewActivity = ViewActivity(),
    onBillClick: (Long) -> Unit = {},
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onCopy: (() -> Unit)? = null
) {
    val colors = rememberShellColors()

    val isBilingual = profile.isBilingual
    val primaryLang = profile.primaryLanguage.ifEmpty { "ta" }
    val secondaryLang = profile.secondaryLanguage.ifEmpty { "en" }

    val p1 = merchant.name[primaryLang] ?: merchant.name["ta"] ?: ""
    val p2 = merchant.name[secondaryLang] ?: merchant.name["en"] ?: ""
    val primaryName = p1.ifEmpty { p2.ifEmpty { "-" } }
    val secondaryName = if (isBilingual && p1.isNotEmpty() && p2.isNotEmpty() && p1 != p2) p2 else ""

    fun pick(map: Map<String, String>): String =
        map[primaryLang] ?: map[secondaryLang] ?: map.values.firstOrNull { it.isNotBlank() }.orEmpty()

    val city = pick(merchant.city)
    val district = pick(merchant.district)
    val state = pick(merchant.state)
    val country = pick(merchant.country).takeIf { it != "India" && it != "இந்தியா" }.orEmpty()
    val address = pick(merchant.address)

    val availability = if (merchant.companyIds.isEmpty()) "${K.all.tr()} ${K.companies.tr()}"
    else merchant.companyIds.mapNotNull { id ->
        val p = allProfiles.find { it.id == id }
        p?.shortName?.ifEmpty { p.businessName.values.firstOrNull() }
    }.joinToString(", ").ifEmpty { "${K.select.tr()} ${K.companies.tr()}" }

    val callLabel = K.callBtn.tr()
    val mailLabel = K.email.tr()
    val actions = buildList {
        if (merchant.phone.isNotBlank()) add(Triple(MaterialSymbols.Rounded.Call, callLabel) { openDialer(merchant.phone) })
        if (merchant.email.isNotBlank()) add(Triple(MaterialSymbols.Rounded.Email, mailLabel) { openMailTo(merchant.email) })
    }

    val scrollState = rememberLazyListState()

    ViewTile(
        title = K.businessDetails.tr(),
        onBack = onBack,
        scrollState = scrollState,
        fixedTopBar = true,
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
                    left = Triple(MaterialSymbols.Rounded.CurrencyRupeeCircle, K.totalBilledAmount.tr(), CurrencyUtils.formatInr(activity.billed)),
                    right = Triple(MaterialSymbols.Rounded.Receipt, K.unpaidAmount.tr(), CurrencyUtils.formatInr(activity.unpaid)),
                    colors = colors
                )
            }

            if (actions.isNotEmpty()) {
                item(key = "actions") { ViewQuickActions(actions = actions, colors = colors) }
            }

            item(key = "contact") {
                ViewInfoSection(
                    title = K.contactSection.tr(),
                    rows = listOf(
                        K.telephone.tr() to merchant.phone,
                        K.email.tr() to merchant.email,
                        "GSTIN" to if (showGst) merchant.gstin else ""
                    ),
                    colors = colors
                )
            }

            item(key = "address") {
                ViewInfoSection(
                    title = K.address.tr(),
                    rows = listOf(
                        K.address.tr() to address,
                        K.city.tr() to city,
                        K.district.tr() to district,
                        K.state.tr() to state,
                        K.country.tr() to country,
                        K.pincode.tr() to merchant.pinCode,
                        K.companies.tr() to availability
                    ),
                    colors = colors
                )
            }

            item(key = "bills") {
                ViewBillsSection(title = K.recentBills.tr(), bills = activity.bills, colors = colors, onBillClick = onBillClick)
            }
        }
    }
}
