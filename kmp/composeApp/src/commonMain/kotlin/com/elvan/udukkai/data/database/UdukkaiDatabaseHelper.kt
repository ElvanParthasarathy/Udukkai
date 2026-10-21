package com.elvan.udukkai.data.database

import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.model.ReceiptInvoiceLinkModel
import com.elvan.udukkai.data.model.ReceiptModel
import com.elvan.udukkai.data.model.ProductModel
import com.elvan.udukkai.data.model.CustomerModel
import com.elvan.udukkai.data.settings.BusinessProfile

/**
 * Clean, decoupled database interface for Udukkai applications.
 * Standardized English interface with full backward-compatibility defaults.
 * Each standalone app (Factory and Textiles) has its own dedicated instance.
 */
interface UdukkaiDatabaseHelper {
    // ── Customers ────────────────────────────────────────────────────────────
    fun loadAllCustomers(): List<CustomerModel>
    fun saveCustomer(customer: CustomerModel): Long
    fun deleteCustomer(id: Long): Boolean
    fun loadDeletedCustomers(): List<CustomerModel>
    fun restoreCustomer(id: Long): Boolean
    fun permanentDeleteCustomer(id: Long): Boolean
    fun purgeExpiredCustomers(days: Int = 30): Int

    fun loadAllMerchants(): List<CustomerModel> = loadAllCustomers()
    fun saveMerchant(merchant: CustomerModel): Long = saveCustomer(merchant)
    fun deleteMerchant(id: Long): Boolean = deleteCustomer(id)
    fun loadDeletedMerchants(): List<CustomerModel> = loadDeletedCustomers()
    fun restoreMerchant(id: Long): Boolean = restoreCustomer(id)
    fun permanentDeleteMerchant(id: Long): Boolean = permanentDeleteCustomer(id)
    fun purgeExpiredMerchants(days: Int = 30): Int = purgeExpiredCustomers(days)

    // ── Products ─────────────────────────────────────────────────────────────
    fun loadAllProducts(): List<ProductModel>
    fun saveProduct(product: ProductModel): Long
    fun deleteProduct(id: Long): Boolean
    fun loadDeletedProducts(): List<ProductModel>
    fun restoreProduct(id: Long): Boolean
    fun permanentDeleteProduct(id: Long): Boolean
    fun purgeExpiredProducts(days: Int = 30): Int

    fun loadAllItems(): List<ProductModel> = loadAllProducts()
    fun saveItem(item: ProductModel): Long = saveProduct(item)
    fun deleteItem(id: Long): Boolean = deleteProduct(id)
    fun loadDeletedItems(): List<ProductModel> = loadDeletedProducts()
    fun restoreItem(id: Long): Boolean = restoreProduct(id)
    fun permanentDeleteItem(id: Long): Boolean = permanentDeleteProduct(id)
    fun purgeExpiredItems(days: Int = 30): Int = purgeExpiredProducts(days)

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
    fun getLinksForReceipt(receiptId: Long): List<ReceiptInvoiceLinkModel>
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
