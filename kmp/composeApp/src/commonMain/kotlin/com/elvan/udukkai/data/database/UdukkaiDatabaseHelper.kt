package com.elvan.udukkai.data.database

import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.model.ReceiptInvoiceLinkModel
import com.elvan.udukkai.data.model.ReceiptModel
import com.elvan.udukkai.data.model.ProductModel
import com.elvan.udukkai.data.model.CustomerModel
import com.elvan.udukkai.data.settings.BusinessProfile

/**
 * Clean, decoupled database interface for Udukkai applications.
 * Completely free of any runtime mode parameterization.
 * Each standalone app (Factory and Textiles) has its own dedicated instance.
 */
interface UdukkaiDatabaseHelper {
    // ── Merchants / Customers ────────────────────────────────────────────────
    fun loadAllMerchants(): List<CustomerModel>
    fun saveMerchant(merchant: CustomerModel): Long
    fun deleteMerchant(id: Long): Boolean
    fun loadDeletedMerchants(): List<CustomerModel>
    fun restoreMerchant(id: Long): Boolean
    fun permanentDeleteMerchant(id: Long): Boolean
    fun purgeExpiredMerchants(days: Int = 30): Int

    // ── Products / Items ─────────────────────────────────────────────────────
    fun loadAllItems(): List<ProductModel>
    fun saveItem(item: ProductModel): Long
    fun deleteItem(id: Long): Boolean
    fun loadDeletedItems(): List<ProductModel>
    fun restoreItem(id: Long): Boolean
    fun permanentDeleteItem(id: Long): Boolean
    fun purgeExpiredItems(days: Int = 30): Int

    // ── Invoices ─────────────────────────────────────────────────────────────
    fun loadAllInvoices(): List<InvoiceModel>
    fun saveInvoice(invoice: InvoiceModel): Long
    fun deleteInvoice(id: Long): Boolean
    fun loadDeletedInvoices(): List<InvoiceModel>
    fun restoreInvoice(id: Long): Boolean
    fun permanentDeleteInvoice(id: Long): Boolean
    fun purgeExpiredInvoices(days: Int = 30): Int

    // ── Receipts ─────────────────────────────────────────────────────────────
    fun loadAllReceipts(): List<ReceiptModel>
    fun saveReceipt(receipt: ReceiptModel): Long
    fun deleteReceipt(id: Long): Boolean
    fun loadDeletedReceipts(): List<ReceiptModel>
    fun restoreReceipt(id: Long): Boolean
    fun permanentDeleteReceipt(id: Long): Boolean
    fun purgeExpiredReceipts(days: Int = 30): Int

    // ── Receipt ↔ Invoice Junction Links ─────────────────────────────────────
    fun getLinksForPatru(patruId: Long): List<ReceiptInvoiceLinkModel>
    fun saveReceiptWithLinks(receipt: ReceiptModel, links: List<ReceiptInvoiceLinkModel>): Long
    fun getPaidAmountForInvoice(invoiceId: Long): Double
    fun getPaidAmountsForInvoices(invoiceIds: List<Long>): Map<Long, Double>

    // ── Business Profiles ────────────────────────────────────────────────────
    fun loadProfile(profileId: Long? = null): BusinessProfile?
    fun loadAllProfiles(): List<BusinessProfile>
    fun saveProfile(profile: BusinessProfile): Boolean
    fun deleteProfile(profileId: Long): Boolean
    fun clearProfiles(): Boolean

    // ── Maintenance ──────────────────────────────────────────────────────────
    fun clearAllData(): Boolean
}

expect fun getFactoryDatabaseHelper(): UdukkaiDatabaseHelper
expect fun getTextilesDatabaseHelper(): UdukkaiDatabaseHelper
