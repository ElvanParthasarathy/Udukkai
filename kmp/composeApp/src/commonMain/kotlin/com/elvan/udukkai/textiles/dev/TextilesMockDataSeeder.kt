package com.elvan.udukkai.textiles.dev

import com.elvan.udukkai.core.backup.AutoBackupManager
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.data.database.getTextilesDatabaseHelper
import com.elvan.udukkai.data.mock.*
import com.elvan.udukkai.data.model.InvoiceDisplayOptions
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.model.ReceiptInvoiceLinkModel
import com.elvan.udukkai.data.model.ReceiptModel
import com.elvan.udukkai.data.settings.BusinessProfile
import com.elvan.udukkai.core.auth.AuthManager
import com.elvan.udukkai.core.platform.getPreferencesHelper
import com.elvan.udukkai.core.sync.getFirebaseSyncManager
import com.elvan.udukkai.localization.Language
import com.elvan.udukkai.localization.LanguageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.elvan.udukkai.textiles.data.*
import com.elvan.udukkai.textiles.ui.screens.editor.TextilesCalculation
import com.elvan.udukkai.textiles.ui.screens.editor.TextilesItem

/**
 * Textiles dev seeder: wipes everything (local and the cloud copy), then loads the data imported from the old
 * app's export (seedTextilesProfiles ... in MockData.kt). Dedicated exclusively to Udukkai Textiles (சவுளி).
 */
object TextilesMockDataSeeder {

    /** "2026-06-11" or "11/06/2026" -> epoch millis. */
    fun parseDate(dateStr: String): Long {
        val sep = if (dateStr.contains("/")) "/" else "-"
        val parts = dateStr.split(sep).map { it.toIntOrNull() ?: return System.currentTimeMillis() }
        if (parts.size != 3) return System.currentTimeMillis()
        val (d, m, y) = if (sep == "/") parts else listOf(parts[2], parts[1], parts[0])
        return DateUtils.parseToEpochMillis("${d.toString().padStart(2, '0')}/${m.toString().padStart(2, '0')}/$y")
            ?: System.currentTimeMillis()
    }

    /**
     * Links a receipt to the invoices it paid, in the order the old receipt listed them, each up to its total.
     * [against] is the old text: invoice numbers separated by commas. Numbers that are not in the data are skipped.
     */
    fun buildReceiptLinks(
        against: String,
        amount: Double,
        invNoToId: Map<String, Long>,
        invNoToTotal: Map<String, Double>
    ): List<ReceiptInvoiceLinkModel> {
        val links = mutableListOf<ReceiptInvoiceLinkModel>()
        var remaining = amount
        for (no in against.split(",").map { it.trim() }.filter { it.isNotEmpty() }) {
            val id = invNoToId[no] ?: continue
            val apply = remaining.coerceIn(0.0, invNoToTotal[no] ?: 0.0)
            if (apply > 0.0) {
                links.add(ReceiptInvoiceLinkModel(invoiceId = id, appliedAmount = apply))
                remaining -= apply
            }
        }
        return links
    }

    /** The "name.ta"/"name.en" keys of a bill's customer snapshot as a language map. */
    private fun Map<String, String>.group(field: String) =
        filterKeys { it.startsWith("$field.") }.mapKeys { it.key.removePrefix("$field.") }

    suspend fun seedAllData(
        profiles: List<SeedProfile> = seedTextilesProfiles,
        items: List<SeedItem> = seedTextilesItems,
        customers: List<SeedCustomer> = seedTextilesCustomers,
        invoices: List<SeedTextilesInvoice> = seedTextilesInvoices,
        receipts: List<SeedReceipt> = seedTextilesReceipts,
        editProfile: (BusinessProfile) -> BusinessProfile = { it }
    ) = withContext(Dispatchers.IO) {
        // 0. Wipe the cloud copy & stop sync first, so the old data cannot come back
        try {
            getFirebaseSyncManager().wipeScope("textiles")
        } catch (_: Exception) {}

        val helper = getTextilesDatabaseHelper()
        helper.clearAllData()

        try {
            getPreferencesHelper().setString("active_textiles_niruvanam_id", null)
        } catch (_: Exception) {}

        // 1. Businesses
        for (p in profiles) helper.saveProfile(editProfile(ReactSeedAssets.profileOf(p)))
        val textilesProfiles = helper.loadAllProfiles()
        val profileIds = textilesProfiles.associate { it.shortName to it.id }

        // 2. Items and customers (the export's ids -> the new ids)
        val itemIds = items.associate { it.ref to helper.saveItem(ReactSeedAssets.itemOf(it)) }
        val customerIds = customers.associate { it.ref to helper.saveCustomer(ReactSeedAssets.customerOf(it)) }
        val customers = helper.loadAllCustomers().associateBy { it.id }

        // 3. Invoices
        for (inv in invoices) {
            val dateMillis = parseDate(inv.date)
            val items = inv.lines.map { l ->
                TextilesItem(
                    itemId = l.itemRef?.let { itemIds[it]?.toString() },
                    itemName = l.name["ta"] ?: l.name["en"].orEmpty(),
                    itemNameEn = l.name["en"].orEmpty(),
                    hsnCode = l.hsnCode, quantity = l.quantity, unit = l.unit, price = l.price, taxRate = l.taxRate,
                    discount = l.discount, discountType = l.discountType, languageMap = l.name
                )
            }
            val customerId = customerIds[inv.customerRef]
            val customer = customers[customerId]
            val totals = TextilesCalculation.calculate(
                items = items,
                globalDiscountValue = inv.globalDiscountValue, globalDiscountType = inv.globalDiscountType,
                businessState = profiles.first().state["en"].orEmpty(),
                customerState = inv.customer["state.en"] ?: inv.customer["state.ta"].orEmpty(),
                country = "India", taxInclusive = inv.taxInclusive
            )
            helper.saveInvoice(
                InvoiceModel(
                    companyId = profileIds[inv.company],
                    invoiceNumber = inv.number,
                    finYear = DateUtils.financialYearStart(dateMillis),
                    vanakkam = inv.number.substringAfterLast("-").toIntOrNull() ?: 1,
                    invoiceType = inv.type,
                    customerId = customerId,
                    customerName = customer?.name ?: inv.customer.group("name"),
                    customerAddress = customer?.address ?: inv.customer.group("address"),
                    invoiceDate = dateMillis,
                    itemsJson = TextilesCalculation.itemsToJson(items),
                    totalAmount = inv.total,
                    discount = inv.discount,
                    globalDiscountValue = inv.globalDiscountValue,
                    globalDiscountType = inv.globalDiscountType,
                    taxAmount = inv.taxAmount,
                    taxDataJson = TextilesCalculation.taxToJson(totals),
                    termsAndConditions = inv.terms,
                    notes = inv.notes,
                    customPreferencesJson = InvoiceDisplayOptions(
                        taxInclusive = inv.taxInclusive, showHSN = inv.showHSN, showDiscountColumn = inv.showDiscountColumn,
                        globalDiscountValue = inv.globalDiscountValue, globalDiscountType = inv.globalDiscountType,
                        measureMode = inv.measureMode, customer = inv.customer
                    ).toJson(),
                    createdAt = dateMillis,
                    updatedAt = dateMillis
                )
            )
        }
        val invoices = helper.loadAllInvoices()
        val invNoToId = invoices.associate { it.invoiceNumber to it.id }
        val invNoToTotal = invoices.associate { it.invoiceNumber to it.totalAmount }

        // 4. Receipts, linked to the invoices they paid
        for (r in receipts) {
            val dateMillis = parseDate(r.date)
            helper.saveReceiptWithLinks(
                ReceiptModel(
                    companyId = profileIds[r.company],
                    receiptNumber = r.number,
                    vanakkam = r.number.substringAfterLast("/").toIntOrNull() ?: 1,
                    finYear = DateUtils.financialYearLabel(dateMillis),
                    customerId = r.customerRef?.let { customerIds[it] },
                    customerName = r.customerName,
                    customerAddress = r.customerAddress,
                    receiptDate = dateMillis,
                    amount = r.amount,
                    paymentMode = r.paymentMode,
                    transactionNumber = r.reference.ifBlank { null },
                    notes = r.note,
                    createdAt = dateMillis,
                    updatedAt = dateMillis
                ),
                buildReceiptLinks(r.against, r.amount, invNoToId, invNoToTotal)
            )
        }

        // Reload all reactive repositories on Main thread
        withContext(Dispatchers.Main) {
            TextilesProfileRepository.refreshFromDatabase()
            textilesProfiles.firstOrNull()?.id?.let {
                TextilesProfileRepository.setActiveProfile(it)
            }
            TextilesCustomerRepository.loadAll()
            TextilesItemRepository.loadAll()
            TextilesInvoiceRepository.loadAll()
            TextilesReceiptRepository.loadAll()
            AuthManager.refreshProfileStatus()
        }
        try {
            if (AuthManager.isLoggedIn) {
                getFirebaseSyncManager().startSync("textiles")
            }
        } catch (_: Exception) {}
        AutoBackupManager.onDataChanged()
    }

    /** A made-up English, single-language business for the Play Store screenshots (see ScreenshotDemoData.kt). */
    suspend fun seedScreenshotData() = seedAllData(
        demoTextilesProfiles, demoTextilesItems, demoTextilesCustomers, demoTextilesInvoices, demoTextilesReceipts,
        editProfile = { it.also { p -> p.isBilingual = false; p.primaryLanguage = "en"; p.secondaryLanguage = "en" } }
    )

    fun toggleLanguage(): String {
        val next = when (LanguageManager.currentLanguage) {
            Language.TAMIL -> Language.ENGLISH
            Language.ENGLISH -> Language.TAMIL_LATIN
            else -> Language.TAMIL
        }
        LanguageManager.setLanguage(next)
        return "Language: ${next.displayName}"
    }

    suspend fun toggleBilingual(): String {
        val profile = TextilesProfileRepository.profile
        val updated = profile.copy(isBilingual = !profile.isBilingual)
        TextilesProfileRepository.updateProfile(updated)
        return if (updated.isBilingual) "Bilingual Mode: ON ✓" else "Bilingual Mode: OFF ✗"
    }

    suspend fun swapDataLanguages(): String {
        val helper = getTextilesDatabaseHelper()
        val allProfiles = helper.loadAllProfiles()
        val activeProfile = TextilesProfileRepository.profile
        val activeId = activeProfile.id
        val currentPrimary = activeProfile.primaryLanguage.ifEmpty { "ta" }
        val currentSecondary = activeProfile.secondaryLanguage.ifEmpty { "en" }

        val newPrimary = currentSecondary
        val newSecondary = currentPrimary

        allProfiles.forEach { p ->
            val updated = p.copy(
                primaryLanguage = newPrimary,
                secondaryLanguage = newSecondary
            )
            helper.saveProfile(updated)
        }
        TextilesProfileRepository.refreshFromDatabase()
        if (activeId != null) {
            TextilesProfileRepository.setActiveProfile(activeId)
        }
        TextilesCustomerRepository.loadAll()
        TextilesItemRepository.loadAll()
        TextilesInvoiceRepository.loadAll()
        TextilesReceiptRepository.loadAll()
        AutoBackupManager.onDataChanged()

        return "Swapped: $newPrimary ↔ $newSecondary"
    }
}
