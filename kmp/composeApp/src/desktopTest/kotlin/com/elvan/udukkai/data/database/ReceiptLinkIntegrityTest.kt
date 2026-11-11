package com.elvan.udukkai.data.database

import com.elvan.udukkai.data.DesktopDbPaths
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.model.ReceiptInvoiceLinkModel
import com.elvan.udukkai.data.model.ReceiptModel
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReceiptLinkIntegrityTest {
    @Test
    fun rejectIneligibleBillsAndKeepExistingAllocationsAtomic() {
        for (factory in listOf(true, false)) {
            val file = "receipt-link-integrity-${UUID.randomUUID()}.db"
            val db = DesktopUdukkaiDatabaseHelper(file, "t_", isFactory = factory)
            try {
                val bill = InvoiceModel(companyId = 5, customerId = 8, invoiceDate = 2000, totalAmount = 100.0, invoiceNumber = "A-1")
                val billId = db.saveInvoice(bill)
                val links = listOf(ReceiptInvoiceLinkModel(invoiceId = billId, appliedAmount = 100.0))
                val receipt = ReceiptModel(companyId = 5, customerId = 8, receiptDate = 3000, amount = 100.0, receiptNumber = "R-1")
                assertEquals(-1L, db.saveReceiptWithLinks(receipt.copy(companyId = 6), links))
                assertEquals(-1L, db.saveReceiptWithLinks(receipt.copy(customerId = 9), links))
                assertEquals(-1L, db.saveReceiptWithLinks(receipt.copy(receiptDate = 1000), links))
                val receiptId = db.saveReceiptWithLinks(receipt, links)
                assertTrue(receiptId > 0)
                assertEquals(-1L, db.saveReceiptWithLinks(receipt.copy(receiptNumber = "R-2"), links))
                assertEquals(-1L, db.saveReceiptWithLinks(receipt.copy(id = receiptId, amount = 99.0), links))
                assertEquals(receiptId, db.saveReceiptWithLinks(receipt.copy(id = receiptId), links))
                assertEquals(1, db.loadAllReceipts().size)
                assertEquals(100.0, db.getPaidAmountForInvoice(billId))
            } finally {
                DesktopDbPaths.getDbFile(file).delete()
            }
        }
    }
}
