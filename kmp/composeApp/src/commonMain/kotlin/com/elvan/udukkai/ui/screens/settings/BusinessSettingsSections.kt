package com.elvan.udukkai.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.ElvanLazyColumn
import com.elvan.udukkai.ui.components.shell.ElvanSectionContainer
import com.elvan.udukkai.ui.components.shell.ElvanSettingsDivider
import com.elvan.udukkai.ui.components.shell.ElvanSettingsRow
import com.elvan.udukkai.ui.components.shell.ElvanSettingsSection
import com.elvan.udukkai.ui.components.shell.LocalElvanTopSpacerHeight
import com.elvan.udukkai.ui.navigation.MaterialSymbols

private class BusinessRow(val icon: androidx.compose.ui.graphics.vector.ImageVector, val title: String, val description: String, val open: () -> Unit)

/** Search destinations for the Profile tab, kept separate from the global Settings index. */
@Composable
fun businessSectionSearchEntries(
    identityRoute: SettingsRoute,
    profileName: String,
    itemCount: Int,
    customerCount: Int,
    onOpenItems: () -> Unit,
    onOpenCustomers: () -> Unit
): List<SectionSearchEntry> = listOf(
    SectionSearchEntry(K.company.tr(), profileName, MaterialSymbols.Rounded.Apartment, listOf("business", "company", "profile")) { openPlatformSettings(SettingsRoute.Merchant) },
    SectionSearchEntry(K.identifier.tr(), K.identityHint.tr(), MaterialSymbols.Rounded.BusinessCenter, listOf("identity", "logo", "gst", "pan", "signature")) { openPlatformSettings(identityRoute) },
    SectionSearchEntry(K.items.tr(), itemCount.toString(), MaterialSymbols.Rounded.Inventory2, listOf("items", "products", "stock"), onOpenItems),
    SectionSearchEntry(K.customers.tr(), customerCount.toString(), MaterialSymbols.Rounded.Person, listOf("customers", "buyers", "merchants"), onOpenCustomers),
    SectionSearchEntry(K.address.tr(), K.companyAddressDetails.tr(), MaterialSymbols.Rounded.LocationOn, listOf("address", "location", "pincode")) { openPlatformSettings(SettingsRoute.Address) },
    SectionSearchEntry(K.bank.tr(), K.bankDetails.tr(), MaterialSymbols.Rounded.CreditCard, listOf("bank", "account", "ifsc", "upi")) { openPlatformSettings(SettingsRoute.Bank) },
    SectionSearchEntry(K.createBtn.tr(), K.pdfStylingOptions.tr(), MaterialSymbols.Rounded.Description, listOf("invoice", "bill", "receipt", "pdf", "print")) { openPlatformSettings(SettingsRoute.InvoiceCreation) }
)

@Composable
private fun RowGroup(rows: List<BusinessRow>, colors: ShellColors) {
    if (rows.isEmpty()) return
    ElvanSectionContainer {
        ElvanSettingsSection(colors = colors) {
            rows.forEachIndexed { i, row ->
                if (i > 0) ElvanSettingsDivider(colors = colors)
                ElvanSettingsRow(icon = row.icon, title = row.title, description = row.description, onClick = row.open, colors = colors)
            }
        }
    }
}

/** The Profile tab: settings-style cards grouped by the job they perform. */
@Composable
fun BusinessTabScreen(
    scrollState: LazyListState,
    identityRoute: SettingsRoute,
    profileName: String,
    itemCount: Int,
    customerCount: Int,
    onOpenItems: () -> Unit,
    onOpenCustomers: () -> Unit,
    query: String = "",
    colors: ShellColors = rememberShellColors()
) {
    val company = listOf(
        BusinessRow(MaterialSymbols.Rounded.Apartment, K.company.tr(), profileName) { openPlatformSettings(SettingsRoute.Merchant) },
        BusinessRow(MaterialSymbols.Rounded.BusinessCenter, K.identifier.tr(), K.identityHint.tr()) { openPlatformSettings(identityRoute) }
    )
    val records = listOf(
        BusinessRow(MaterialSymbols.Rounded.Inventory2, K.items.tr(), itemCount.toString(), onOpenItems),
        BusinessRow(MaterialSymbols.Rounded.Person, K.customers.tr(), customerCount.toString(), onOpenCustomers)
    )
    val contactAndPayment = listOf(
        BusinessRow(MaterialSymbols.Rounded.LocationOn, K.address.tr(), K.companyAddressDetails.tr()) { openPlatformSettings(SettingsRoute.Address) },
        BusinessRow(MaterialSymbols.Rounded.CreditCard, K.bank.tr(), K.bankDetails.tr()) { openPlatformSettings(SettingsRoute.Bank) }
    )
    val bills = listOf(
        BusinessRow(MaterialSymbols.Rounded.Description, K.createBtn.tr(), K.pdfStylingOptions.tr()) { openPlatformSettings(SettingsRoute.InvoiceCreation) }
    )
    val q = query.trim()
    fun List<BusinessRow>.matching() = if (q.isEmpty()) this else filter { it.title.contains(q, ignoreCase = true) || it.description.contains(q, ignoreCase = true) }
    ElvanLazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = Dimens.ContentPaddingBottom),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
    ) {
        item(key = "business_top_spacer") { Spacer(Modifier.height(LocalElvanTopSpacerHeight.current)) }
        item(key = "business_company") { RowGroup(company.matching(), colors) }
        item(key = "business_records") { RowGroup(records.matching(), colors) }
        item(key = "business_contact_payment") { RowGroup(contactAndPayment.matching(), colors) }
        item(key = "business_documents") { RowGroup(bills.matching(), colors) }
    }
}
