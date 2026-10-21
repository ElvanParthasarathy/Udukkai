package com.elvan.udukkai.textiles.dev

import com.elvan.udukkai.core.backup.AutoBackupManager
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.data.database.getTextilesDatabaseHelper
import com.elvan.udukkai.data.mock.*
import com.elvan.udukkai.data.model.CustomerModel
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.model.ReceiptInvoiceLinkModel
import com.elvan.udukkai.data.model.ReceiptModel
import com.elvan.udukkai.data.model.ProductModel
import com.elvan.udukkai.data.settings.BusinessProfile
import com.elvan.udukkai.localization.Language
import com.elvan.udukkai.localization.LanguageManager
import com.elvan.udukkai.textiles.data.*
import com.elvan.udukkai.textiles.ui.screens.editor.TextilesCalculation
import com.elvan.udukkai.textiles.ui.screens.editor.TextilesItem

/**
 * Textiles mock data seeder & testing utilities.
 * Dedicated exclusively to Udukkai Textiles (சவுளி) with zero cross-app dependencies.
 */
object TextilesMockDataSeeder {

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
            headerLayout = d["thalaippuVadivu"] as? String ?: "wide",
            signature = d["kaiyoppam"] as? String ?: "",
            signatoryName = d["oppamPeyar"] as? String ?: "",
            tagline = mutableMapOf(
                "ta" to (d["adaimozhi_ta"] as? String ?: ""),
                "en" to (d["adaimozhi_en"] as? String ?: "")
            ),
            upiId = d["upiId"] as? String ?: "",
            themeColor = d["thoatraNiram"] as? String ?: "#6a1b9a"
        )
    }

    private fun parseSilkItems(rawJson: String, items: List<ProductModel>): List<TextilesItem> {
        val parsed = TextilesCalculation.itemsFromJson(rawJson)
        return parsed.map { item ->
            val matched = items.find {
                it.name["ta"] == item.productName ||
                    (item.productNameEn.isNotEmpty() && it.name["en"] == item.productNameEn)
            }
            val id = matched?.id?.toString() ?: item.productId
            val hsn = if (item.hsnCode.isNotEmpty()) item.hsnCode else (matched?.hsnCode ?: "")
            val price = if (item.price > 0.0) item.price else (matched?.price ?: 0.0)
            val taxRate = if (item.taxRate > 0.0) item.taxRate else (matched?.taxRate ?: 0.0)
            val unit = if (item.unit.isNotEmpty()) item.unit else (matched?.unit ?: "Nos")
            val langMap = if (item.languageMap.isNotEmpty()) item.languageMap else (matched?.name ?: mapOf("ta" to item.productName, "en" to item.productNameEn))
            item.copy(
                productId = id,
                productNameEn = langMap["en"] ?: item.productNameEn,
                hsnCode = hsn,
                unit = unit,
                price = price,
                taxRate = taxRate,
                languageMap = langMap
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
        val helper = getTextilesDatabaseHelper()

        // 0. Erase existing
        TextilesProfileRepository.clearProfiles()
        helper.clearAllData()

        // 1. Seed Profiles
        for (map in mockTextilesProfiles) {
            val p = profileFromMap(map)
            TextilesProfileRepository.createProfile(p)
        }
        val textilesProfiles = helper.loadAllProfiles()
        val profileMap = textilesProfiles.associateBy { it.shortName }

        // 2. Products
        for (item in mockTextilesProducts) {
            val nameTa = item["peyar_ta"] as? String ?: ""
            val nameEn = item["peyar_en"] as? String ?: ""
            val hsn = item["hsnCode"] as? String ?: ""
            val price = (item["vilai"] as? Number)?.toDouble() ?: 0.0
            val taxRate = (item["variVeetham"] as? Number)?.toDouble() ?: 0.0
            val unit = item["alagu"] as? String ?: "Nos"
            val quantityType = item["alavuVagai"] as? String ?: "quantity"

            helper.saveProduct(
                ProductModel(
                    name = mapOf("ta" to nameTa, "en" to nameEn),
                    hsnCode = hsn,
                    price = price,
                    taxRate = taxRate,
                    unit = unit,
                    quantityType = quantityType
                )
            )
        }
        val textilesItems = helper.loadAllProducts()

        // 3. Customers
        for (m in mockTextilesCustomers) {
            helper.saveCustomer(
                CustomerModel(
                    name = mapOf("ta" to (m["peyar_ta"] ?: ""), "en" to (m["peyar_en"] ?: "")),
                    address = mapOf("ta" to (m["mugavari_ta"] ?: ""), "en" to (m["mugavari_en"] ?: "")),
                    city = mapOf("ta" to (m["oor_ta"] ?: ""), "en" to (m["oor_en"] ?: "")),
                    district = mapOf("ta" to (m["maavattam_ta"] ?: ""), "en" to (m["maavattam_en"] ?: "")),
                    state = mapOf("ta" to (m["maanilam_ta"] ?: ""), "en" to (m["maanilam_en"] ?: "")),
                    country = mapOf("ta" to (m["naadu_ta"] ?: "இந்தியா"), "en" to (m["naadu_en"] ?: "India")),
                    pinCode = m["anjalKuriyeedu"] ?: "",
                    gstin = m["gstin"] ?: ""
                )
            )
        }
        val textilesCustomers = helper.loadAllCustomers()

        // 4. Invoices
        for (data in mockTextilesInvoices) {
            val dateStr = data["invoiceDate"] as? String ?: "2026-06-11"
            val dateMillis = parseDate(dateStr)
            val invoiceNo = data["invoiceNumber"] as? String ?: "SJPS-1"
            val prefix = invoiceNo.split("-").firstOrNull() ?: ""
            val vanakkamNum = invoiceNo.split("-").lastOrNull()?.toIntOrNull() ?: 1
            val profileId = profileMap[prefix]?.id

            val custName = data["customer_name"] as? String ?: ""
            val matchedCust = textilesCustomers.find { it.name["ta"] == custName }
            val custId = matchedCust?.id
            val custNameMap = matchedCust?.name ?: mapOf("ta" to custName, "en" to (data["customer_name_en"] as? String ?: custName))
            val custCityMap = matchedCust?.city ?: emptyMap()

            val rawItems = data["items"] as? String ?: "[]"
            val parsedItems = parseSilkItems(rawItems, textilesItems)
            val enrichedJson = TextilesCalculation.itemsToJson(parsedItems)

            val grandTotal = (data["grandTotal"] as? Number)?.toDouble() ?: 0.0
            val discTotal = (data["totalDiscount"] as? Number)?.toDouble() ?: 0.0
            val taxTotal = (data["totalTax"] as? Number)?.toDouble() ?: 0.0

            helper.saveInvoice(
                InvoiceModel(
                    companyId = profileId,
                    invoiceNumber = invoiceNo,
                    finYear = 2026,
                    vanakkam = vanakkamNum,
                    invoiceType = "tax-invoice",
                    customerId = custId,
                    customerName = custNameMap,
                    customerAddress = custCityMap,
                    invoiceDate = dateMillis,
                    itemsJson = enrichedJson,
                    totalAmount = grandTotal,
                    discount = discTotal,
                    globalDiscountValue = discTotal,
                    globalDiscountType = "₹",
                    globalDiscountAmount = discTotal,
                    taxAmount = taxTotal,
                    createdAt = dateMillis,
                    updatedAt = dateMillis
                )
            )
        }
        val textilesInvoices = helper.loadAllInvoices()
        val invEnToId = textilesInvoices.associate { it.invoiceNumber to it.id }
        val invEnToTotal = textilesInvoices.associate { it.invoiceNumber to it.totalAmount }

        // 5. Receipts with Links
        for (r in mockTextilesReceipts) {
            val dateStr = r["date"] as? String ?: "2026-06-11"
            val dateMillis = parseDate(dateStr)
            val receiptNo = r["receiptNo"] as? String ?: "RCP/SJPS/01"
            val parts = receiptNo.split("/")
            val prefix = if (parts.size > 1) parts[1] else ""
            val vanakkamNum = parts.lastOrNull()?.toIntOrNull() ?: 1
            val profileId = profileMap[prefix]?.id

            val clientName = r["clientName"] as? String ?: ""
            val matchedCust = textilesCustomers.find { it.name["ta"] == clientName }
            val amount = (r["amount"] as? Number)?.toDouble() ?: 0.0
            val pMode = (r["paymentMode"] as? String) ?: "cash"

            val against = r["againstInvoice"] as? String ?: ""
            val links = buildReceiptLinks(against, amount, invEnToId, invEnToTotal)

            helper.saveReceiptWithLinks(
                ReceiptModel(
                    companyId = profileId,
                    receiptNumber = receiptNo,
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
        TextilesProfileRepository.refreshFromDatabase()
        TextilesCustomerRepository.loadAll()
        TextilesProductRepository.loadAll()
        TextilesInvoiceRepository.loadAll()
        TextilesReceiptRepository.loadAll()
        AutoBackupManager.onDataChanged()
    }

    suspend fun eraseAllData() {
        val helper = getTextilesDatabaseHelper()
        TextilesProfileRepository.clearProfiles()
        helper.clearAllData()

        TextilesProfileRepository.refreshFromDatabase()
        TextilesCustomerRepository.loadAll()
        TextilesProductRepository.loadAll()
        TextilesInvoiceRepository.loadAll()
        TextilesReceiptRepository.loadAll()
        AutoBackupManager.onDataChanged()
    }

    suspend fun toggleExtraProfile(): String {
        val profiles = TextilesProfileRepository.allProfiles
        val existing = profiles.find { it.shortName == "EPS" }
        val msg = if (existing != null && existing.id != null) {
            TextilesProfileRepository.deleteProfile(existing.id!!)
            "Textiles EPS Removed ✗"
        } else {
            if (mockTextilesProfiles.size > 1) {
                TextilesProfileRepository.createProfile(profileFromMap(mockTextilesProfiles[1]))
            }
            "Textiles EPS Added ✓"
        }
        TextilesProfileRepository.refreshFromDatabase()
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
        val profile = TextilesProfileRepository.profile
        val updated = profile.copy(isBilingual = !profile.isBilingual)
        TextilesProfileRepository.updateProfile(updated)
        return if (updated.isBilingual) "Bilingual Mode: ON ✓" else "Bilingual Mode: OFF ✗"
    }

    suspend fun swapDataLanguages(): String {
        val allProfiles = TextilesProfileRepository.allProfiles
        val activeProfile = TextilesProfileRepository.profile
        val currentPrimary = activeProfile.primaryLanguage.ifEmpty { "ta" }
        val currentSecondary = activeProfile.secondaryLanguage.ifEmpty { "en" }

        allProfiles.forEach { p ->
            val pPrimary = p.primaryLanguage.ifEmpty { "ta" }
            val pSecondary = p.secondaryLanguage.ifEmpty { "en" }
            val updated = p.copy(
                primaryLanguage = if (pPrimary == currentPrimary) currentSecondary else currentPrimary,
                secondaryLanguage = if (pSecondary == currentSecondary) currentPrimary else currentSecondary
            )
            TextilesProfileRepository.updateProfile(updated)
        }
        return "Swapped: $currentSecondary ↔ $currentPrimary"
    }
}
