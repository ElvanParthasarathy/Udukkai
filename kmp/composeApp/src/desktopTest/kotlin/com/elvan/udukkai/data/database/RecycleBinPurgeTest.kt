package com.elvan.udukkai.data.database

import com.elvan.udukkai.data.DesktopDbPaths
import com.elvan.udukkai.data.model.CustomerModel
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.model.ItemModel
import com.elvan.udukkai.data.model.ReceiptModel
import java.sql.DriverManager
import kotlin.test.Test
import kotlin.test.assertEquals

/** The recycle bin says 30 days: a deleted customer, item, bill or receipt is cleared after that, not before. Runs on a throw-away database file. */
class RecycleBinPurgeTest {
    private val dbName = "recycle_bin_purge_test.db"

    private fun backdate(table: String, id: Long, days: Int) {
        Class.forName("org.sqlite.JDBC")
        DriverManager.getConnection("jdbc:sqlite:${DesktopDbPaths.getDbFile(dbName).absolutePath}").use { c ->
            c.prepareStatement("UPDATE \"$table\" SET deleted_at = ? WHERE id = ?").use {
                it.setLong(1, System.currentTimeMillis() / 1000 - days * 86400L)
                it.setLong(2, id)
                it.executeUpdate()
            }
        }
    }

    @Test
    fun deletedRecordsGoAfterThirtyDaysOnly() {
        val db = DesktopUdukkaiDatabaseHelper(dbName, "t_", isFactory = false)
        try {
            val young = db.saveCustomer(CustomerModel(name = mapOf("en" to "Young")))
            val old = db.saveCustomer(CustomerModel(name = mapOf("en" to "Old")))
            val keep = db.saveCustomer(CustomerModel(name = mapOf("en" to "Kept")))
            val item = db.saveItem(ItemModel(name = mapOf("en" to "Old item")))
            val youngItem = db.saveItem(ItemModel(name = mapOf("en" to "Young item")))
            listOf(young, old).forEach { db.deleteCustomer(it) }
            listOf(item, youngItem).forEach { db.deleteItem(it) }
            backdate("t_customers", young, 29)
            backdate("t_customers", old, 31)
            backdate("t_items", item, 31)
            backdate("t_items", youngItem, 29)

            assertEquals(1, db.purgeExpiredCustomers(30))
            assertEquals(1, db.purgeExpiredItems(30))

            val oldBill = db.saveInvoice(InvoiceModel(invoiceNumber = "OLD-1"))
            val youngBill = db.saveInvoice(InvoiceModel(invoiceNumber = "YOUNG-1"))
            val oldReceipt = db.saveReceipt(ReceiptModel(receiptNumber = "R-OLD"))
            val youngReceipt = db.saveReceipt(ReceiptModel(receiptNumber = "R-YOUNG"))
            listOf(oldBill, youngBill).forEach { db.deleteInvoice(it) }
            listOf(oldReceipt, youngReceipt).forEach { db.deleteReceipt(it) }
            backdate("t_invoices", oldBill, 31)
            backdate("t_invoices", youngBill, 29)
            backdate("t_receipts", oldReceipt, 31)
            backdate("t_receipts", youngReceipt, 29)
            assertEquals(1, db.purgeExpiredInvoices(30))
            assertEquals(1, db.purgeExpiredReceipts(30))
            assertEquals(listOf("YOUNG-1"), db.loadDeletedInvoices().map { it.invoiceNumber })
            assertEquals(listOf("R-YOUNG"), db.loadDeletedReceipts().map { it.receiptNumber })

            assertEquals(listOf("Young"), db.loadDeletedCustomers().map { it.name["en"] })
            assertEquals(listOf("Young item"), db.loadDeletedItems().map { it.name["en"] })
            assertEquals(listOf("Kept"), db.loadAllCustomers().map { it.name["en"] })
        } finally {
            DesktopDbPaths.getDbFile(dbName).delete()
        }
    }
}
