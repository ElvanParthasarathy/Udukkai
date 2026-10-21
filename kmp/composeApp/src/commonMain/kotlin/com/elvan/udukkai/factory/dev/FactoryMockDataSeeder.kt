package com.elvan.udukkai.factory.dev

import com.elvan.udukkai.core.backup.AutoBackupManager
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.data.database.getFactoryDatabaseHelper
import com.elvan.udukkai.data.mock.*
import com.elvan.udukkai.data.model.CustomerModel
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.model.ReceiptInvoiceLinkModel
import com.elvan.udukkai.data.model.ReceiptModel
import com.elvan.udukkai.data.model.ProductModel
import com.elvan.udukkai.data.settings.BusinessProfile
import com.elvan.udukkai.factory.data.*
import com.elvan.udukkai.factory.ui.screens.editor.FactoryCalculation
import com.elvan.udukkai.factory.ui.screens.editor.FactoryItem
import com.elvan.udukkai.localization.Language
import com.elvan.udukkai.localization.LanguageManager

/**
 * Factory mock data seeder & testing utilities.
 * Dedicated exclusively to Udukkai Factory (ஆலை) with zero cross-app dependencies.
 */
object FactoryMockDataSeeder {

    fun parseDate(dateStr: String): Long {
        if (dateStr.contains("/")) {
            val parts = dateStr.split("/")
            if (parts.size == 3) {
                val d = parts[0].toIntOrNull() ?: 1
                val m = parts[1].toIntOrNull() ?: 1
                val y = parts[2].toIntOrNull() ?: 2026
                val dStr = if (d < 10) "0$d" else "$d"
                val mStr = if (m < 10) "0$m" else "$m"
                return DateUtils.parseToEpochMillis("$dStr/$mStr/$y") ?: System.currentTimeMillis()
            }
        }
        if (dateStr.contains("-")) {
            val parts = dateStr.split("-")
            if (parts.size == 3) {
                val y = parts[0].toIntOrNull() ?: 2026
                val m = parts[1].toIntOrNull() ?: 1
                val d = parts[2].toIntOrNull() ?: 1
                val dStr = if (d < 10) "0$d" else "$d"
                val mStr = if (m < 10) "0$m" else "$m"
                return DateUtils.parseToEpochMillis("$dStr/$mStr/$y") ?: System.currentTimeMillis()
            }
        }
        return System.currentTimeMillis()
    }

    fun profileFromMap(d: Map<String, Any>): BusinessProfile {
        return BusinessProfile(
            primaryLanguage = d["mudhanMozhi"] as? String ?: "ta",
            secondaryLanguage = d["thunaiMozhi"] as? String ?: "en",
            isBilingual = d["iruMozhi"] as? Boolean ?: true,
            businessName = mutableMapOf(
                "ta" to (d["niruvanathinPeyar_ta"] as? String ?: ""),
                "en" to (d["niruvanathinPeyar_en"] as? String ?: "")
            ),
            shortName = d["kurumPeyar"] as? String ?: "",
            phone1 = d["tholaipesi_1"] as? String ?: "",
            phone2 = d["tholaipesi_2"] as? String ?: "",
            email = d["minnanjal"] as? String ?: "",
            gstin = d["gstin"] as? String ?: "",
            address = mutableMapOf(
                "ta" to (d["mugavari_ta"] as? String ?: ""),
                "en" to (d["mugavari_en"] as? String ?: "")
            ),
            city = mutableMapOf(
                "ta" to (d["oor_ta"] as? String ?: ""),
                "en" to (d["oor_en"] as? String ?: "")
            ),
            district = mutableMapOf(
                "ta" to (d["maavattam_ta"] as? String ?: ""),
                "en" to (d["maavattam_en"] as? String ?: "")
            ),
            state = mutableMapOf(
                "ta" to (d["maanilam_ta"] as? String ?: ""),
                "en" to (d["maanilam_en"] as? String ?: "")
            ),
            country = mutableMapOf(
                "ta" to (d["naadu_ta"] as? String ?: ""),
                "en" to (d["naadu_en"] as? String ?: "")
            ),
            pinCode = d["anjalKuriyeedu"] as? String ?: "",
            bankName = mutableMapOf(
                "ta" to (d["vangiPeyar_ta"] as? String ?: ""),
                "en" to (d["vangiPeyar_en"] as? String ?: "")
            ),
            branch = mutableMapOf(
                "ta" to (d["kilai_ta"] as? String ?: ""),
                "en" to (d["kilai_en"] as? String ?: "")
            ),
            accountNumber = d["vangiKanakku"] as? String ?: "",
            ifsc = d["ifsc"] as? String ?: "",
            logo = d["oavuru"] as? String ?: "",
            wideLogo = d["agalaOavuru"] as? String ?: "",
            headerLayout = d["thalaippuVadivu"] as? String ?: "small",
            signature = d["kaiyoppam"] as? String ?: "",
            signatoryName = d["oppamPeyar"] as? String ?: "",
            tagline = mutableMapOf(
                "ta" to (d["adaimozhi_ta"] as? String ?: ""),
                "en" to (d["adaimozhi_en"] as? String ?: "")
            ),
            upiId = d["upiId"] as? String ?: "",
            themeColor = d["thoatraNiram"] as? String ?: "#2e7d32"
        )
    }

    private fun parseCoolieItems(rawJson: String, items: List<ProductModel>): List<FactoryItem> {
        val parsed = FactoryCalculation.itemsFromJson(rawJson)
        return parsed.map { item ->
            val matched = items.find { it.name["ta"] == item.name }
            item.copy(
                porulId = matched?.id?.toString(),
                porulPeyarEn = matched?.name?.get("en") ?: item.porulPeyarEn,
                mozhiMap = matched?.name ?: emptyMap()
            )
        }
    }

    private fun buildReceiptLinks(
        againstInvoice: String,
        amount: Double,
        invEnToId: Map<String, Long>,
        invEnToTotal: Map<String, Double>
    ): List<ReceiptInvoiceLinkModel> {
        val links = mutableListOf<ReceiptInvoiceLinkModel>()
        val invList = againstInvoice.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        var remaining = amount
        for (invEn in invList) {
            val id = invEnToId[invEn] ?: continue
            val balance = invEnToTotal[invEn] ?: 0.0
            val apply = remaining.coerceIn(0.0, balance)
            if (apply > 0.0) {
                links.add(ReceiptInvoiceLinkModel(invoiceId = id, appliedAmount = apply))
                remaining -= apply
            }
        }
        return links
    }

    suspend fun seedAllData() {
        val helper = getFactoryDatabaseHelper()

        // 0. Erase existing
        FactoryProfileRepository.clearProfiles()
        helper.clearAllData()

        // 1. Seed Profiles
        for (map in mockCoolieProfiles) {
            val p = profileFromMap(map)
            FactoryProfileRepository.createProfile(p)
        }
        val factoryProfiles = helper.loadAllProfiles()
        val profileMap = factoryProfiles.associateBy { it.shortName }

        // 2. Products
        for (item in mockCooliePorulgal) {
            val peyarTa = item["peyar_ta"] ?: ""
            val peyarEn = item["peyar_en"] ?: ""
            helper.saveProduct(
                ProductModel(
                    name = mapOf("ta" to peyarTa, "en" to peyarEn)
                )
            )
        }
        val factoryItems = helper.loadAllProducts()

        // 3. Customers
        for (m in mockCoolieVaangunargal) {
            helper.saveCustomer(
                CustomerModel(
                    name = mapOf("ta" to (m["peyar_ta"] ?: ""), "en" to (m["peyar_en"] ?: "")),
                    city = mapOf("ta" to (m["oor_ta"] ?: ""), "en" to (m["oor_en"] ?: ""))
                )
            )
        }
        val factoryCustomers = helper.loadAllCustomers()

        // 4. Invoices
        for (data in mockCooliePattiyalgal) {
            val dateStr = data["date"] as? String ?: "21/02/2026"
            val dateMillis = parseDate(dateStr)
            val billNo = data["bill_no"] as? String ?: "VRM-101"
            val prefix = billNo.split("-").firstOrNull() ?: ""
            val vanakkamNum = billNo.split("-").lastOrNull()?.toIntOrNull() ?: 1
            val profileId = profileMap[prefix]?.id

            val custName = data["customer_name"] as? String ?: ""
            val custOor = data["customer_oor"] as? String ?: ""
            val matchedCust = factoryCustomers.find {
                it.name["ta"] == custName && (custOor.isEmpty() || it.city["ta"] == custOor)
            } ?: factoryCustomers.find { it.name["ta"] == custName }

            val custId = matchedCust?.id
            val custPeyar = matchedCust?.name ?: mapOf("ta" to custName, "en" to (data["customer_name_en"] as? String ?: custName))
            val custMunvari = matchedCust?.city ?: mapOf("ta" to custOor, "en" to custOor)

            val rawItems = data["items"] as? String ?: "[]"
            val parsedItems = parseCoolieItems(rawItems, factoryItems)
            val enrichedJson = FactoryCalculation.itemsToJson(parsedItems)

            val grandTotal = (data["grand_total"] as? Number)?.toDouble() ?: 0.0
            val mothaEdai = (data["mothaEdai"] as? Number)?.toDouble() ?: 0.0
            val setharam = (data["setharamGrams"] as? Number)?.toDouble() ?: 0.0
            val thabaal = (data["thabaalThogai"] as? Number)?.toDouble() ?: 0.0
            val ahimsa = (data["ahimsaPattuThogai"] as? Number)?.toDouble() ?: 0.0

            helper.saveInvoice(
                InvoiceModel(
                    companyId = profileId,
                    invoiceNumber = billNo,
                    finYear = 2026,
                    vanakkam = vanakkamNum,
                    invoiceType = "tax-invoice",
                    customerId = custId,
                    customerName = custPeyar,
                    customerAddress = custMunvari,
                    invoiceDate = dateMillis,
                    itemsJson = enrichedJson,
                    totalAmount = grandTotal,
                    totalWeight = mothaEdai,
                    wastageGrams = setharam,
                    postalCharges = thabaal,
                    ahimsaSilkAmount = ahimsa,
                    createdAt = dateMillis,
                    updatedAt = dateMillis
                )
            )
        }
        val factoryInvoices = helper.loadAllInvoices()
        val invEnToId = factoryInvoices.associate { it.invoiceNumber to it.id }
        val invEnToTotal = factoryInvoices.associate { it.invoiceNumber to it.totalAmount }

        // 5. Receipts with Links
        for (r in mockCooliePatrugal) {
            val dateStr = r["date"] as? String ?: "2026-06-08"
            val dateMillis = parseDate(dateStr)
            val patruEn = r["receiptNo"] as? String ?: "RCP/PVS/01"
            val parts = patruEn.split("/")
            val prefix = if (parts.size > 1) parts[1] else ""
            val vanakkamNum = parts.lastOrNull()?.toIntOrNull() ?: 1
            val profileId = profileMap[prefix]?.id

            val clientName = r["clientName"] as? String ?: ""
            val matchedCust = factoryCustomers.find { it.name["ta"] == clientName }
            val amount = (r["amount"] as? Number)?.toDouble() ?: 0.0
            val pMode = (r["paymentMode"] as? String) ?: "cash"

            val against = r["againstInvoice"] as? String ?: ""
            val links = buildReceiptLinks(against, amount, invEnToId, invEnToTotal)

            helper.saveReceiptWithLinks(
                ReceiptModel(
                    companyId = profileId,
                    receiptNumber = patruEn,
                    vanakkam = vanakkamNum,
                    finYear = "2026",
                    customerId = matchedCust?.id,
                    customerName = mapOf("ta" to clientName, "en" to ((r["clientNameEn"] as? String) ?: clientName)),
                    receiptDate = dateMillis,
                    amount = amount,
                    paymentMode = pMode,
                    transactionNumber = r["referenceNo"] as? String,
                    createdAt = dateMillis,
                    updatedAt = dateMillis
                ),
                links
            )
        }

        // Reload all reactive repositories
        FactoryProfileRepository.refreshFromDatabase()
        FactoryCustomerRepository.loadAll()
        FactoryProductRepository.loadAll()
        FactoryInvoiceRepository.loadAll()
        FactoryReceiptRepository.loadAll()
        AutoBackupManager.onDataChanged()
    }

    suspend fun eraseAllData() {
        val helper = getFactoryDatabaseHelper()
        FactoryProfileRepository.clearProfiles()
        helper.clearAllData()

        FactoryProfileRepository.refreshFromDatabase()
        FactoryCustomerRepository.loadAll()
        FactoryProductRepository.loadAll()
        FactoryInvoiceRepository.loadAll()
        FactoryReceiptRepository.loadAll()
        AutoBackupManager.onDataChanged()
    }

    suspend fun toggleExtraProfile(): String {
        val profiles = FactoryProfileRepository.allProfiles
        val existing = profiles.find { it.shortName == "PVS" }
        val msg = if (existing != null && existing.id != null) {
            FactoryProfileRepository.deleteProfile(existing.id!!)
            "Factory PVS Removed ✗"
        } else {
            if (mockCoolieProfiles.size > 1) {
                FactoryProfileRepository.createProfile(profileFromMap(mockCoolieProfiles[1]))
            }
            "Factory PVS Added ✓"
        }
        FactoryProfileRepository.refreshFromDatabase()
        AutoBackupManager.onDataChanged()
        return msg
    }

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
        val profile = FactoryProfileRepository.profile
        val updated = profile.copy(isBilingual = !profile.isBilingual)
        FactoryProfileRepository.updateProfile(updated)
        return if (updated.isBilingual) "Bilingual Mode: ON ✓" else "Bilingual Mode: OFF ✗"
    }

    suspend fun swapDataLanguages(): String {
        val allProfiles = FactoryProfileRepository.allProfiles
        val activeProfile = FactoryProfileRepository.profile
        val currentPrimary = activeProfile.primaryLanguage.ifEmpty { "ta" }
        val currentSecondary = activeProfile.secondaryLanguage.ifEmpty { "en" }

        allProfiles.forEach { p ->
            val pPrimary = p.primaryLanguage.ifEmpty { "ta" }
            val pSecondary = p.secondaryLanguage.ifEmpty { "en" }
            val updated = p.copy(
                primaryLanguage = if (pPrimary == currentPrimary) currentSecondary else currentPrimary,
                secondaryLanguage = if (pSecondary == currentSecondary) currentPrimary else currentSecondary
            )
            FactoryProfileRepository.updateProfile(updated)
        }
        return "Swapped: $currentSecondary ↔ $currentPrimary"
    }
}
