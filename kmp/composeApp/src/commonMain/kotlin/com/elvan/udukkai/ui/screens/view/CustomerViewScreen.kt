package com.elvan.udukkai.ui.screens.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.core.platform.openDialer
import com.elvan.udukkai.core.platform.openMailTo
import com.elvan.udukkai.core.data.locationDataName
import com.elvan.udukkai.core.data.indianStates
import com.elvan.udukkai.core.data.tamilNaduDistricts
import com.elvan.udukkai.core.utils.CurrencyUtils
import com.elvan.udukkai.data.model.CustomerModel
import com.elvan.udukkai.data.settings.DataLanguageDisplay
import com.elvan.udukkai.data.settings.BusinessProfile
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.trWithLang
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.rememberShellColors
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
    val (history, retryHistory) = rememberViewHistory(merchant.id, listOf(historyKey, profile.primaryLanguage, profile.secondaryLanguage, profile.isBilingual), loadHistory)
    val activity = (history as? ViewHistoryState.Ready)?.activity ?: ViewActivity()
    val ready = history is ViewHistoryState.Ready
    val historyUi = rememberViewHistoryUi(history, K.noCustomerInvoices.tr(), withReceipts = true)
    val deleteItem = rememberViewDeleteItem(onDelete, K.customerDeleted.tr(), onBack)

    val language = DataLanguageDisplay(profile)
    val primaryLang = language.primary
    val secondaryLang = language.secondary
    val primaryName = language.first(merchant.name).ifBlank { "-" }
    val secondaryName = language.second(merchant.name)

    fun pick(map: Map<String, String>): String =
        language.first(map)

    fun second(map: Map<String, String>) = language.second(map)

    fun isDefaultCountry(v: String) = v == "India" || v == "இந்தியா"

    fun formatDistrict(district: String, languageCode: String): String {
        val value = district.trim()
        val suffix = K.district.trWithLang(languageCode)
        if (value.isBlank() || value.contains(suffix, ignoreCase = true)) return value
        return "$value $suffix"
    }

    val d1 = formatDistrict(locationDataName(merchant.district, tamilNaduDistricts, primaryLang), primaryLang)
    val d2 = formatDistrict(locationDataName(merchant.district, tamilNaduDistricts, secondaryLang), secondaryLang)

    // Only the configured primary language; saved translations remain untouched.
    val addressLines = listOf(
        pick(merchant.address),
        listOf(pick(merchant.city), d1).filter { it.isNotBlank() }.joinToString(", "),
        listOf(locationDataName(merchant.state, indianStates, primaryLang), merchant.pinCode).filter { it.isNotBlank() }.joinToString(" - "),
        pick(merchant.country).takeUnless { isDefaultCountry(it) }.orEmpty()
    )
    // Second language: only what is really written in it, as its own complete address
    val secondaryLines = listOf(
        second(merchant.address),
        listOf(second(merchant.city), d2).filter { it.isNotBlank() }.joinToString(", "),
        listOf(locationDataName(merchant.state, indianStates, secondaryLang), merchant.pinCode).filter { it.isNotBlank() }.joinToString(" - "),
        second(merchant.country).takeUnless { isDefaultCountry(it) }.orEmpty()
    ).takeIf { lines -> language.languages.size > 1 && lines.any { it.isNotBlank() } && lines != addressLines } ?: emptyList()

    // Under the name: the second-language name with the place after a dot; with no second language the place stands alone
    val heroSecond = if (secondaryName.isNotEmpty())
        listOf(secondaryName, pick(merchant.city)).filter { it.isNotBlank() }.joinToString("  •  ")
    else pick(merchant.city)

    val availability = if (merchant.companyIds.isEmpty()) K.allCompanies.tr()
    else merchant.companyIds.mapNotNull { id ->
        val p = allProfiles.find { it.id == id }
        p?.shortName?.ifEmpty { language.first(p.businessName) }
    }.joinToString(", ").ifEmpty { "${K.select.tr()} ${K.companies.tr()}" }

    val callLabel = K.callBtn.tr()
    val mailLabel = K.email.tr()
    val actions = buildList {
        if (merchant.phone.isNotBlank()) add(Triple(MaterialSymbols.Rounded.Call, callLabel) { openDialer(merchant.phone) })
        if (merchant.email.isNotBlank()) add(Triple(MaterialSymbols.Rounded.Email, mailLabel) { openMailTo(merchant.email) })
    }

    ViewRecordPage(
        title = K.businessDetails.tr(),
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
        summary = {
            ViewHero(primary = primaryName, secondary = heroSecond, icon = MaterialSymbols.Rounded.Person, tint = colors.customerColor, colors = colors)

            run {
                ViewStats(
                    left = Triple(MaterialSymbols.Rounded.CurrencyRupeeCircle, K.totalBilledAmount.tr(), if (ready) CurrencyUtils.formatInr(activity.billed) else "-"),
                    right = Triple(MaterialSymbols.Rounded.Payments, K.unpaidAmount.tr(), if (ready) CurrencyUtils.formatInr(activity.unpaid) else "-"),
                    colors = colors
                )
            }

            if (actions.isNotEmpty()) {
                run { ViewQuickActions(actions = actions, colors = colors) }
            }
        }
    ) {
            run {
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

            run {
                // One box for each language, the second a little under the first
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ViewAddressBox(title = K.address.tr(), lines = addressLines, colors = colors)
                    ViewAddressBox(title = null, lines = secondaryLines, colors = colors)
                }
            }

            run {
                // The title above the card says it; the card holds just the value, as on the item page
                ViewAddressBox(title = K.companies.tr(), lines = listOf(availability), colors = colors)
            }
    }
}
