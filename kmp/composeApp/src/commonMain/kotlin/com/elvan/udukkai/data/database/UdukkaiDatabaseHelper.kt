package com.elvan.udukkai.data.database

import com.elvan.udukkai.data.model.PattiyalTharavuru
import com.elvan.udukkai.data.model.PatruPattiyalInaippuTharavuru
import com.elvan.udukkai.data.model.PatrugalTharavuru
import com.elvan.udukkai.data.model.PorulTharavuru
import com.elvan.udukkai.data.model.VaangunarTharavuru
import com.elvan.udukkai.data.settings.NiruvanaTharavugal

/**
 * Clean, decoupled database interface for Udukkai applications.
 * Completely free of any runtime mode parameterization.
 * Each standalone app (Factory and Textiles) has its own dedicated instance.
 */
interface UdukkaiDatabaseHelper {
    // ── Merchants / Customers ────────────────────────────────────────────────
    fun loadAllMerchants(): List<VaangunarTharavuru>
    fun saveMerchant(merchant: VaangunarTharavuru): Long
    fun deleteMerchant(id: Long): Boolean
    fun loadDeletedMerchants(): List<VaangunarTharavuru>
    fun restoreMerchant(id: Long): Boolean
    fun permanentDeleteMerchant(id: Long): Boolean
    fun purgeExpiredMerchants(days: Int = 30): Int

    // ── Products / Items ─────────────────────────────────────────────────────
    fun loadAllItems(): List<PorulTharavuru>
    fun saveItem(item: PorulTharavuru): Long
    fun deleteItem(id: Long): Boolean
    fun loadDeletedItems(): List<PorulTharavuru>
    fun restoreItem(id: Long): Boolean
    fun permanentDeleteItem(id: Long): Boolean
    fun purgeExpiredItems(days: Int = 30): Int

    // ── Invoices ─────────────────────────────────────────────────────────────
    fun loadAllInvoices(): List<PattiyalTharavuru>
    fun saveInvoice(invoice: PattiyalTharavuru): Long
    fun deleteInvoice(id: Long): Boolean
    fun loadDeletedInvoices(): List<PattiyalTharavuru>
    fun restoreInvoice(id: Long): Boolean
    fun permanentDeleteInvoice(id: Long): Boolean
    fun purgeExpiredInvoices(days: Int = 30): Int

    // ── Receipts ─────────────────────────────────────────────────────────────
    fun loadAllReceipts(): List<PatrugalTharavuru>
    fun saveReceipt(receipt: PatrugalTharavuru): Long
    fun deleteReceipt(id: Long): Boolean
    fun loadDeletedReceipts(): List<PatrugalTharavuru>
    fun restoreReceipt(id: Long): Boolean
    fun permanentDeleteReceipt(id: Long): Boolean
    fun purgeExpiredReceipts(days: Int = 30): Int

    // ── Receipt ↔ Invoice Junction Links ─────────────────────────────────────
    fun getLinksForPatru(patruId: Long): List<PatruPattiyalInaippuTharavuru>
    fun saveReceiptWithLinks(receipt: PatrugalTharavuru, links: List<PatruPattiyalInaippuTharavuru>): Long
    fun getPaidAmountForInvoice(invoiceId: Long): Double
    fun getPaidAmountsForInvoices(invoiceIds: List<Long>): Map<Long, Double>

    // ── Business Profiles ────────────────────────────────────────────────────
    fun loadProfile(profileId: Long? = null): NiruvanaTharavugal?
    fun loadAllProfiles(): List<NiruvanaTharavugal>
    fun saveProfile(profile: NiruvanaTharavugal): Boolean
    fun deleteProfile(profileId: Long): Boolean
    fun clearProfiles(): Boolean

    // ── Maintenance ──────────────────────────────────────────────────────────
    fun clearAllData(): Boolean
}

expect fun getFactoryDatabaseHelper(): UdukkaiDatabaseHelper
expect fun getTextilesDatabaseHelper(): UdukkaiDatabaseHelper
