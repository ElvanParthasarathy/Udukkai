package com.elvan.udukkai.data.mock

import com.elvan.udukkai.core.auth.AuthManager
import com.elvan.udukkai.core.backup.getNirilBackupService
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.mode.ModeManager
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.data.business.getBusinessDatabaseHelper
import com.elvan.udukkai.data.model.PattiyalTharavuru
import com.elvan.udukkai.data.model.PatruPattiyalInaippuTharavuru
import com.elvan.udukkai.data.model.PatrugalTharavuru
import com.elvan.udukkai.data.model.PorulTharavuru
import com.elvan.udukkai.data.model.VaangunarTharavuru
import com.elvan.udukkai.data.repository.PatrugalRepository
import com.elvan.udukkai.data.repository.PattiyalRepository
import com.elvan.udukkai.data.repository.PorulRepository
import com.elvan.udukkai.data.repository.VaangunarRepository
import com.elvan.udukkai.data.settings.NiruvanaTharavugal
import com.elvan.udukkai.data.settings.NiruvanaTharavugalRepository
import com.elvan.udukkai.localization.Language
import com.elvan.udukkai.localization.LanguageManager
import com.elvan.udukkai.ui.screens.editor.invoice.components.KooliKanakku
import com.elvan.udukkai.ui.screens.editor.invoice.components.KooliUrupadi
import com.elvan.udukkai.ui.screens.editor.invoice.components.PattuKanakku
import com.elvan.udukkai.ui.screens.editor.invoice.components.PattuUrupadi

/**
 * Service to seed and erase test mock data.
 * Matches Flutter `sodhanai_tharavu_uruvakki.dart` 1:1.
 */
object SodhanaiTharavuUruvakki {

    fun parseDate(dateStr: String): Long {
        if (dateStr.contains("/")) {
            val parts = dateStr.split("/")
            if (parts.size == 3) {
                val d = parts[0].toIntOrNull() ?: 1
                val m = parts[1].toIntOrNull() ?: 1
                val y = parts[2].toIntOrNull() ?: 2026
                return DateUtils.parseToEpochMillis("$d/$m/$y") ?: System.currentTimeMillis()
            }
        }
        if (dateStr.contains("-")) {
            val parts = dateStr.split("-")
            if (parts.size == 3) {
                val y = parts[0].toIntOrNull() ?: 2026
                val m = parts[1].toIntOrNull() ?: 1
                val d = parts[2].toIntOrNull() ?: 1
                return DateUtils.parseToEpochMillis("$d/$m/$y") ?: System.currentTimeMillis()
            }
        }
        return System.currentTimeMillis()
    }

    fun profileFromMap(d: Map<String, Any>): NiruvanaTharavugal {
        return NiruvanaTharavugal(
            mudhanMozhi = d["mudhanMozhi"] as? String ?: "ta",
            thunaiMozhi = d["thunaiMozhi"] as? String ?: "en",
            iruMozhi = d["iruMozhi"] as? Boolean ?: true,
            niruvanathinPeyar = mutableMapOf(
                "ta" to (d["niruvanathinPeyar_ta"] as? String ?: ""),
                "en" to (d["niruvanathinPeyar_en"] as? String ?: "")
            ),
            kurumPeyar = d["kurumPeyar"] as? String ?: "",
            tholaipaesi1 = d["tholaipesi_1"] as? String ?: "",
            tholaipaesi2 = d["tholaipesi_2"] as? String ?: "",
            minnanjal = d["minnanjal"] as? String ?: "",
            gstin = d["gstin"] as? String ?: "",
            mugavari = mutableMapOf(
                "ta" to (d["mugavari_ta"] as? String ?: ""),
                "en" to (d["mugavari_en"] as? String ?: "")
            ),
            oor = mutableMapOf(
                "ta" to (d["oor_ta"] as? String ?: ""),
                "en" to (d["oor_en"] as? String ?: "")
            ),
            maavattam = mutableMapOf(
                "ta" to (d["maavattam_ta"] as? String ?: ""),
                "en" to (d["maavattam_en"] as? String ?: "")
            ),
            maanilam = mutableMapOf(
                "ta" to (d["maanilam_ta"] as? String ?: ""),
                "en" to (d["maanilam_en"] as? String ?: "")
            ),
            naadu = mutableMapOf(
                "ta" to (d["naadu_ta"] as? String ?: ""),
                "en" to (d["naadu_en"] as? String ?: "")
            ),
            anjalKuriyeedu = d["anjalKuriyeedu"] as? String ?: "",
            vangiPeyar = mutableMapOf(
                "ta" to (d["vangiPeyar_ta"] as? String ?: ""),
                "en" to (d["vangiPeyar_en"] as? String ?: "")
            ),
            kilai = mutableMapOf(
                "ta" to (d["kilai_ta"] as? String ?: ""),
                "en" to (d["kilai_en"] as? String ?: "")
            ),
            vangiKanakku = d["vangiKanakku"] as? String ?: "",
            ifsc = d["ifsc"] as? String ?: "",
            oavuru = d["oavuru"] as? String ?: "",
            agalaOavuru = d["agalaOavuru"] as? String ?: "",
            thalaippuVadivu = d["thalaippuVadivu"] as? String ?: "small",
            kaiyoppam = d["kaiyoppam"] as? String ?: "",
            oppamPeyar = d["oppamPeyar"] as? String ?: "",
            adaimozhi = mutableMapOf(
                "ta" to (d["adaimozhi_ta"] as? String ?: ""),
                "en" to (d["adaimozhi_en"] as? String ?: "")
            ),
            upiId = d["upiId"] as? String ?: "",
            thoatraNiram = d["thoatraNiram"] as? String ?: "#6a1b9a"
        )
    }

    /**
     * Erases all business data and business profiles across both modes.
     */
    suspend fun eraseAllData() {
        // Clear business profiles in both DBs
        NiruvanaTharavugalRepository.clearProfiles(AppMode.KOOLI)
        NiruvanaTharavugalRepository.clearProfiles(AppMode.PATTU)

        // Clear business tables in both DBs
        val helper = getBusinessDatabaseHelper()
        helper.clearAllData(AppMode.KOOLI)
        helper.clearAllData(AppMode.PATTU)

        // Refresh all reactive repositories to empty state
        val mode = ModeManager.currentMode
        NiruvanaTharavugalRepository.refreshFromDatabase()
        VaangunarRepository.loadAll(mode)
        PorulRepository.loadAll(mode)
        PattiyalRepository.loadAll(mode)
        PatrugalRepository.loadAll(mode)

        // Log out & reset mode
        AuthManager.logout()
        ModeManager.resetStartupState()

        try {
            getNirilBackupService().deleteBackup()
        } catch (_: Exception) {}
    }

    /**
     * Seeds complete dataset for Silk and Coolie modes:
     * - Profiles
     * - Products
     * - Merchants
     * - Invoices
     * - Receipts with Invoice Links
     */
    suspend fun seedAllData() {
        val helper = getBusinessDatabaseHelper()

        // ── 0. Erase all existing data across both DBs ──
        NiruvanaTharavugalRepository.clearProfiles(AppMode.KOOLI)
        NiruvanaTharavugalRepository.clearProfiles(AppMode.PATTU)
        helper.clearAllData(AppMode.KOOLI)
        helper.clearAllData(AppMode.PATTU)

        // ── 1. Seed Silk Mode (Pattu) ──
        seedSilkData(helper)

        // ── 2. Seed Coolie Mode ──
        seedCoolieData(helper)

        // Preserve active mode
        val activeMode = ModeManager.currentMode

        // Log in test account and refresh profile status
        AuthManager.login("test@udukkai.com", "kadavuchol")
        AuthManager.refreshProfileStatus()

        // Reload all 5 reactive repositories with freshly seeded data
        NiruvanaTharavugalRepository.refreshFromDatabase()
        VaangunarRepository.loadAll(activeMode)
        PorulRepository.loadAll(activeMode)
        PattiyalRepository.loadAll(activeMode)
        PatrugalRepository.loadAll(activeMode)

        // Create unified backup surviving reinstall
        try {
            getNirilBackupService().createBackup()
        } catch (_: Exception) {}
    }

    private fun seedSilkData(helper: com.elvan.udukkai.data.business.BusinessDatabaseHelper) {
        // A. Profiles
        for (map in mockSilkProfiles) {
            val p = profileFromMap(map)
            NiruvanaTharavugalRepository.createProfile(AppMode.PATTU, p)
        }
        val silkProfiles = NiruvanaTharavugalRepository.getAllProfiles(AppMode.PATTU)
        val silkProfileMap = silkProfiles.associateBy { it.kurumPeyar }

        // B. Products
        for (item in mockSilkPorulgal) {
            val peyarTa = item["peyar_ta"] as? String ?: ""
            val peyarEn = item["peyar_en"] as? String ?: ""
            val hsn = item["hsnCode"] as? String ?: ""
            val vilai = (item["vilai"] as? Number)?.toDouble() ?: 0.0
            val vari = (item["variVeetham"] as? Number)?.toDouble() ?: 0.0
            val alagu = item["alagu"] as? String ?: "Nos"
            val vagai = item["alavuVagai"] as? String ?: "quantity"

            helper.saveItem(
                AppMode.PATTU,
                PorulTharavuru(
                    porulPeyar = mapOf("ta" to peyarTa, "en" to peyarEn),
                    hsnCode = hsn,
                    vilai = vilai,
                    variVeetham = vari,
                    alagu = alagu,
                    alavuVagai = vagai
                )
            )
        }
        val silkItems = helper.loadAllItems(AppMode.PATTU)

        // C. Merchants
        for (m in mockSilkVaangunargal) {
            helper.saveMerchant(
                AppMode.PATTU,
                VaangunarTharavuru(
                    peyar = mapOf("ta" to (m["peyar_ta"] ?: ""), "en" to (m["peyar_en"] ?: "")),
                    mugavari = mapOf("ta" to (m["mugavari_ta"] ?: ""), "en" to (m["mugavari_en"] ?: "")),
                    oor = mapOf("ta" to (m["oor_ta"] ?: ""), "en" to (m["oor_en"] ?: "")),
                    maavattam = mapOf("ta" to (m["maavattam_ta"] ?: ""), "en" to (m["maavattam_en"] ?: "")),
                    maanilam = mapOf("ta" to (m["maanilam_ta"] ?: ""), "en" to (m["maanilam_en"] ?: "")),
                    naadu = mapOf("ta" to (m["naadu_ta"] ?: "இந்தியா"), "en" to (m["naadu_en"] ?: "India")),
                    anjalKuriyeedu = m["anjalKuriyeedu"] ?: "",
                    gstin = m["gstin"] ?: ""
                )
            )
        }
        val silkMerchants = helper.loadAllMerchants(AppMode.PATTU)

        // D. Invoices
        for (data in mockSilkPattiyalgal) {
            val dateStr = data["invoiceDate"] as? String ?: "2026-06-11"
            val dateMillis = parseDate(dateStr)
            val invoiceNo = data["invoiceNumber"] as? String ?: "SJPS-1"
            val prefix = invoiceNo.split("-").firstOrNull() ?: ""
            val vanakkamNum = invoiceNo.split("-").lastOrNull()?.toIntOrNull() ?: 1
            val profileId = silkProfileMap[prefix]?.id

            val custName = data["customer_name"] as? String ?: ""
            val matchedCust = silkMerchants.find { it.peyar["ta"] == custName }
            val custId = matchedCust?.id
            val custPeyar = matchedCust?.peyar ?: mapOf("ta" to custName, "en" to (data["customer_name_en"] as? String ?: custName))
            val custOor = matchedCust?.oor ?: emptyMap()

            val rawItems = data["items"] as? String ?: "[]"
            // Re-encode items with actual product IDs
            val parsedItems = parseSilkItems(rawItems, silkItems)
            val enrichedJson = PattuKanakku.pattuListToJson(parsedItems)

            val grandTotal = (data["grandTotal"] as? Number)?.toDouble() ?: 0.0
            val discTotal = (data["totalDiscount"] as? Number)?.toDouble() ?: 0.0
            val taxTotal = (data["totalTax"] as? Number)?.toDouble() ?: 0.0

            helper.saveInvoice(
                AppMode.PATTU,
                PattiyalTharavuru(
                    niruvanamId = profileId,
                    patrucheettuEn = invoiceNo,
                    finYear = 2026,
                    vanakkam = vanakkamNum,
                    pattiyalVagai = "tax-invoice",
                    vaangunarId = custId,
                    vaangunarPeyar = custPeyar,
                    vaangunarMunvari = custOor,
                    pattiyalNaal = dateMillis,
                    tharavugal = enrichedJson,
                    mothaThogai = grandTotal,
                    thallupadi = discTotal,
                    podhuThallupadiMathippu = discTotal,
                    podhuThallupadiVagai = "₹",
                    podhuThallupadiThogai = discTotal,
                    variThogai = taxTotal,
                    createdAt = dateMillis,
                    updatedAt = dateMillis
                )
            )
        }
        val silkInvoices = helper.loadAllInvoices(AppMode.PATTU)
        val invEnToId = silkInvoices.associate { it.patrucheettuEn to it.id }
        val invEnToTotal = silkInvoices.associate { it.patrucheettuEn to it.mothaThogai }

        // E. Receipts with Links
        for (r in mockSilkPatrugal) {
            val dateStr = r["date"] as? String ?: "2026-06-11"
            val dateMillis = parseDate(dateStr)
            val patruEn = r["receiptNo"] as? String ?: "RCP/SJPS/01"
            val parts = patruEn.split("/")
            val prefix = if (parts.size > 1) parts[1] else ""
            val vanakkamNum = parts.lastOrNull()?.toIntOrNull() ?: 1
            val profileId = silkProfileMap[prefix]?.id

            val clientName = r["clientName"] as? String ?: ""
            val matchedCust = silkMerchants.find { it.peyar["ta"] == clientName }
            val amount = (r["amount"] as? Number)?.toDouble() ?: 0.0
            val pMode = (r["paymentMode"] as? String) ?: "cash"

            val against = r["againstInvoice"] as? String ?: ""
            val links = buildReceiptLinks(against, amount, invEnToId, invEnToTotal)

            helper.saveReceiptWithLinks(
                AppMode.PATTU,
                PatrugalTharavuru(
                    niruvanamId = profileId,
                    patruEn = patruEn,
                    vanakkam = vanakkamNum,
                    finYear = "2026",
                    vaangunarId = matchedCust?.id,
                    vaangunarPeyar = mapOf("ta" to clientName, "en" to ((r["clientNameEn"] as? String) ?: clientName)),
                    patruNaal = dateMillis,
                    thogai = amount,
                    seluthumMurai = pMode,
                    parivarthanaiEn = r["referenceNo"] as? String,
                    createdAt = dateMillis,
                    updatedAt = dateMillis
                ),
                links
            )
        }
    }

    private fun seedCoolieData(helper: com.elvan.udukkai.data.business.BusinessDatabaseHelper) {
        // A. Profiles
        for (map in mockCoolieProfiles) {
            val p = profileFromMap(map)
            NiruvanaTharavugalRepository.createProfile(AppMode.KOOLI, p)
        }
        val coolieProfiles = NiruvanaTharavugalRepository.getAllProfiles(AppMode.KOOLI)
        val coolieProfileMap = coolieProfiles.associateBy { it.kurumPeyar }

        // B. Products
        for (item in mockCooliePorulgal) {
            val peyarTa = item["peyar_ta"] ?: ""
            val peyarEn = item["peyar_en"] ?: ""
            helper.saveItem(
                AppMode.KOOLI,
                PorulTharavuru(
                    porulPeyar = mapOf("ta" to peyarTa, "en" to peyarEn)
                )
            )
        }
        val coolieItems = helper.loadAllItems(AppMode.KOOLI)

        // C. Customers
        for (m in mockCoolieVaangunargal) {
            helper.saveMerchant(
                AppMode.KOOLI,
                VaangunarTharavuru(
                    peyar = mapOf("ta" to (m["peyar_ta"] ?: ""), "en" to (m["peyar_en"] ?: "")),
                    oor = mapOf("ta" to (m["oor_ta"] ?: ""), "en" to (m["oor_en"] ?: ""))
                )
            )
        }
        val coolieMerchants = helper.loadAllMerchants(AppMode.KOOLI)

        // D. Invoices
        for (data in mockCooliePattiyalgal) {
            val dateStr = data["date"] as? String ?: "21/02/2026"
            val dateMillis = parseDate(dateStr)
            val billNo = data["bill_no"] as? String ?: "VRM-101"
            val prefix = billNo.split("-").firstOrNull() ?: ""
            val vanakkamNum = billNo.split("-").lastOrNull()?.toIntOrNull() ?: 1
            val profileId = coolieProfileMap[prefix]?.id

            val custName = data["customer_name"] as? String ?: ""
            val custOor = data["customer_oor"] as? String ?: ""
            val matchedCust = coolieMerchants.find {
                it.peyar["ta"] == custName && (custOor.isEmpty() || it.oor["ta"] == custOor)
            } ?: coolieMerchants.find { it.peyar["ta"] == custName }

            val custId = matchedCust?.id
            val custPeyar = matchedCust?.peyar ?: mapOf("ta" to custName, "en" to (data["customer_name_en"] as? String ?: custName))
            val custMunvari = matchedCust?.oor ?: mapOf("ta" to custOor, "en" to custOor)

            val rawItems = data["items"] as? String ?: "[]"
            val parsedItems = parseCoolieItems(rawItems, coolieItems)
            val enrichedJson = KooliKanakku.kooliListToJson(parsedItems)

            val grandTotal = (data["grand_total"] as? Number)?.toDouble() ?: 0.0
            val mothaEdai = (data["mothaEdai"] as? Number)?.toDouble() ?: 0.0
            val setharam = (data["setharamGrams"] as? Number)?.toDouble() ?: 0.0
            val thabaal = (data["thabaalThogai"] as? Number)?.toDouble() ?: 0.0
            val ahimsa = (data["ahimsaPattuThogai"] as? Number)?.toDouble() ?: 0.0

            helper.saveInvoice(
                AppMode.KOOLI,
                PattiyalTharavuru(
                    niruvanamId = profileId,
                    patrucheettuEn = billNo,
                    finYear = 2026,
                    vanakkam = vanakkamNum,
                    pattiyalVagai = "tax-invoice",
                    vaangunarId = custId,
                    vaangunarPeyar = custPeyar,
                    vaangunarMunvari = custMunvari,
                    pattiyalNaal = dateMillis,
                    tharavugal = enrichedJson,
                    mothaThogai = grandTotal,
                    mothaEdai = mothaEdai,
                    setharamGrams = setharam,
                    thabaalThogai = thabaal,
                    ahimsaPattuThogai = ahimsa,
                    createdAt = dateMillis,
                    updatedAt = dateMillis
                )
            )
        }
        val coolieInvoices = helper.loadAllInvoices(AppMode.KOOLI)
        val invEnToId = coolieInvoices.associate { it.patrucheettuEn to it.id }
        val invEnToTotal = coolieInvoices.associate { it.patrucheettuEn to it.mothaThogai }

        // E. Receipts with Links
        for (r in mockCooliePatrugal) {
            val dateStr = r["date"] as? String ?: "2026-06-08"
            val dateMillis = parseDate(dateStr)
            val patruEn = r["receiptNo"] as? String ?: "RCP/PVS/01"
            val parts = patruEn.split("/")
            val prefix = if (parts.size > 1) parts[1] else ""
            val vanakkamNum = parts.lastOrNull()?.toIntOrNull() ?: 1
            val profileId = coolieProfileMap[prefix]?.id

            val clientName = r["clientName"] as? String ?: ""
            val matchedCust = coolieMerchants.find { it.peyar["ta"] == clientName }
            val amount = (r["amount"] as? Number)?.toDouble() ?: 0.0
            val pMode = (r["paymentMode"] as? String) ?: "cash"

            val against = r["againstInvoice"] as? String ?: ""
            val links = buildReceiptLinks(against, amount, invEnToId, invEnToTotal)

            helper.saveReceiptWithLinks(
                AppMode.KOOLI,
                PatrugalTharavuru(
                    niruvanamId = profileId,
                    patruEn = patruEn,
                    vanakkam = vanakkamNum,
                    finYear = "2026",
                    vaangunarId = matchedCust?.id,
                    vaangunarPeyar = mapOf("ta" to clientName, "en" to ((r["clientNameEn"] as? String) ?: clientName)),
                    patruNaal = dateMillis,
                    thogai = amount,
                    seluthumMurai = pMode,
                    parivarthanaiEn = r["referenceNo"] as? String,
                    createdAt = dateMillis,
                    updatedAt = dateMillis
                ),
                links
            )
        }
    }

    private fun parseSilkItems(rawJson: String, items: List<PorulTharavuru>): List<PattuUrupadi> {
        val parsed = PattuKanakku.pattuListFromJson(rawJson)
        return parsed.map { item ->
            val matched = items.find {
                it.porulPeyar["ta"] == item.porulPeyar ||
                    (item.porulPeyarEn.isNotEmpty() && it.porulPeyar["en"] == item.porulPeyarEn)
            }
            val id = matched?.id?.toString() ?: item.porulId
            val hsn = if (item.hsnKuriyeedu.isNotEmpty()) item.hsnKuriyeedu else (matched?.hsnCode ?: "")
            val vilai = if (item.vilai > 0.0) item.vilai else (matched?.vilai ?: 0.0)
            val vari = if (item.variVizhukkaadu > 0.0) item.variVizhukkaadu else (matched?.variVeetham ?: 0.0)
            val alagu = if (item.alagu.isNotEmpty()) item.alagu else (matched?.alagu ?: "Nos")
            val mozhiMap = if (item.mozhiMap.isNotEmpty()) item.mozhiMap else (matched?.porulPeyar ?: mapOf("ta" to item.porulPeyar, "en" to item.porulPeyarEn))
            item.copy(
                porulId = id,
                porulPeyarEn = mozhiMap["en"] ?: item.porulPeyarEn,
                hsnKuriyeedu = hsn,
                alagu = alagu,
                vilai = vilai,
                variVizhukkaadu = vari,
                mozhiMap = mozhiMap
            )
        }
    }

    private fun parseCoolieItems(rawJson: String, items: List<PorulTharavuru>): List<KooliUrupadi> {
        val parsed = KooliKanakku.kooliListFromJson(rawJson)
        return parsed.map { item ->
            val matched = items.find { it.porulPeyar["ta"] == item.porulPeyar }
            item.copy(
                porulId = matched?.id?.toString(),
                porulPeyarEn = matched?.porulPeyar?.get("en") ?: item.porulPeyarEn,
                mozhiMap = matched?.porulPeyar ?: emptyMap()
            )
        }
    }

    private fun buildReceiptLinks(
        againstInvoice: String,
        amount: Double,
        invEnToId: Map<String, Long>,
        invEnToTotal: Map<String, Double>
    ): List<PatruPattiyalInaippuTharavuru> {
        val links = mutableListOf<PatruPattiyalInaippuTharavuru>()
        val invList = againstInvoice.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        var remaining = amount
        for (invEn in invList) {
            val id = invEnToId[invEn] ?: continue
            val balance = invEnToTotal[invEn] ?: 0.0
            val apply = remaining.coerceIn(0.0, balance)
            if (apply > 0.0) {
                links.add(PatruPattiyalInaippuTharavuru(pattiyalId = id, poruthiyaThogai = apply))
                remaining -= apply
            }
        }
        return links
    }

    /**
     * Toggles extra Silk profile (EPS) — adds if missing, deletes if exists.
     */
    suspend fun toggleExtraSilk(): String {
        val profiles = NiruvanaTharavugalRepository.getAllProfiles(AppMode.PATTU)
        val existing = profiles.find { it.kurumPeyar == "EPS" }
        val msg = if (existing != null && existing.id != null) {
            NiruvanaTharavugalRepository.deleteProfile(AppMode.PATTU, existing.id!!)
            "Silk EPS Removed ✗"
        } else {
            if (mockSilkProfiles.size > 1) {
                NiruvanaTharavugalRepository.createProfile(AppMode.PATTU, profileFromMap(mockSilkProfiles[1]))
            }
            "Silk EPS Added ✓"
        }
        NiruvanaTharavugalRepository.refreshFromDatabase()
        try {
            getNirilBackupService().createBackup()
        } catch (_: Exception) {}
        return msg
    }

    /**
     * Toggles extra Coolie profile (PVS) — adds if missing, deletes if exists.
     */
    suspend fun toggleExtraCoolie(): String {
        val profiles = NiruvanaTharavugalRepository.getAllProfiles(AppMode.KOOLI)
        val existing = profiles.find { it.kurumPeyar == "PVS" }
        val msg = if (existing != null && existing.id != null) {
            NiruvanaTharavugalRepository.deleteProfile(AppMode.KOOLI, existing.id!!)
            "Coolie PVS Removed ✗"
        } else {
            if (mockCoolieProfiles.size > 1) {
                NiruvanaTharavugalRepository.createProfile(AppMode.KOOLI, profileFromMap(mockCoolieProfiles[1]))
            }
            "Coolie PVS Added ✓"
        }
        NiruvanaTharavugalRepository.refreshFromDatabase()
        try {
            getNirilBackupService().createBackup()
        } catch (_: Exception) {}
        return msg
    }

    /**
     * Cycles UI Language: Tamil -> English -> Tamil Latin (Tanglish) -> Tamil.
     */
    fun toggleLanguage(): String {
        val next = when (LanguageManager.currentLanguage) {
            Language.TAMIL -> Language.ENGLISH
            Language.ENGLISH -> Language.TAMIL_LATIN
            else -> Language.TAMIL
        }
        LanguageManager.setLanguage(next)
        return "Language: ${next.displayName}"
    }

    /**
     * Toggles bilingual mode on the currently active profile.
     */
    suspend fun toggleBilingual(): String {
        val profile = NiruvanaTharavugalRepository.getProfile(ModeManager.currentMode)
        val updated = profile.copy(iruMozhi = !profile.iruMozhi)
        NiruvanaTharavugalRepository.updateProfile(ModeManager.currentMode, updated)
        NiruvanaTharavugalRepository.refreshFromDatabase()
        try {
            getNirilBackupService().createBackup()
        } catch (_: Exception) {}
        return if (updated.iruMozhi) "Bilingual Mode: ON ✓" else "Bilingual Mode: OFF ✗"
    }

    /**
     * Swaps primary and secondary data languages on the currently active profile.
     */
    suspend fun swapDataLanguages(): String {
        val mode = ModeManager.currentMode
        val allProfiles = NiruvanaTharavugalRepository.getAllProfiles(mode)
        val activeProfile = NiruvanaTharavugalRepository.getProfile(mode)
        val currentPrimary = activeProfile.mudhanMozhi.ifEmpty { "ta" }
        val currentSecondary = activeProfile.thunaiMozhi.ifEmpty { "en" }

        allProfiles.forEach { p ->
            val pPrimary = p.mudhanMozhi.ifEmpty { "ta" }
            val pSecondary = p.thunaiMozhi.ifEmpty { "en" }
            val updated = p.copy(
                mudhanMozhi = if (pPrimary == currentPrimary) currentSecondary else currentPrimary,
                thunaiMozhi = if (pSecondary == currentSecondary) currentPrimary else currentSecondary
            )
            NiruvanaTharavugalRepository.updateProfile(mode, updated)
        }
        NiruvanaTharavugalRepository.refreshFromDatabase()
        try {
            getNirilBackupService().createBackup()
        } catch (_: Exception) {}
        return "Swapped: $currentSecondary ↔ $currentPrimary"
    }
}
