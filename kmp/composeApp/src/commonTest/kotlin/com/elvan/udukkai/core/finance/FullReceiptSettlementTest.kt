package com.elvan.udukkai.core.finance

import com.elvan.udukkai.data.model.*
import kotlin.test.*

class FullReceiptSettlementTest {
    private val bills = listOf(InvoiceModel(id = 1, totalAmount = 1000.0), InvoiceModel(id = 2, totalAmount = 500.0))
    @Test fun oneReceiptCanFullySettleMultipleBills() {
        val links = listOf(ReceiptInvoiceLinkModel(invoiceId = 1, appliedAmount = 1000.0), ReceiptInvoiceLinkModel(invoiceId = 2, appliedAmount = 500.0))
        assertTrue(FullReceiptSettlement.matches(ReceiptModel(amount = 1500.0), links, bills))
        assertFalse(FullReceiptSettlement.matches(ReceiptModel(amount = 400.0), links, bills))
        assertFalse(FullReceiptSettlement.matches(ReceiptModel(amount = 1501.0), links, bills))
    }
    @Test fun partialAndUnallocatedAmountsCannotCloseLinkedBills() {
        assertFalse(FullReceiptSettlement.matches(ReceiptModel(amount = 400.0), listOf(ReceiptInvoiceLinkModel(invoiceId = 1, appliedAmount = 400.0)), bills))
        assertFalse(FullReceiptSettlement.matches(ReceiptModel(amount = 1001.0), listOf(ReceiptInvoiceLinkModel(invoiceId = 1, appliedAmount = 1000.0)), bills))
    }
    @Test fun centsAreComparedWithoutTruncatingTheCombinedAmount() {
        val small = listOf(InvoiceModel(totalAmount = 0.1), InvoiceModel(totalAmount = 0.2))
        assertEquals(0.3, FullReceiptSettlement.total(small))
        assertTrue(FullReceiptSettlement.matches(0.3, small))
        assertFalse(FullReceiptSettlement.matches(Double.NaN, small))
    }
}
