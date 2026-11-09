package com.elvan.udukkai.ui.screens.view

import com.elvan.udukkai.data.model.InvoiceModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ViewHistoryTest {
    private fun bill(id: Long, customer: Long = 1L, company: Long = 10L, date: Long = id * 1000, total: Double = 100.0, type: String = "tax-invoice") =
        InvoiceModel(id = id, companyId = company, invoiceNumber = "INV-$id", customerId = customer, invoiceDate = date, totalAmount = total, invoiceType = type)

    @Test
    fun customerHistoryIsOnlyThisCustomerInThisCompanyNewestFirst() {
        val bills = listOf(bill(1), bill(2, date = 9000), bill(3, customer = 2), bill(4, company = 20), bill(5, type = "proforma"))
        val rows = ViewHistory.customerRows(bills, customerId = 1L, companyId = 10L, paid = emptyMap())
        assertEquals(listOf(2L, 1L), rows.map { it.id })
    }

    @Test
    fun aBillSyncedTwiceIsListedOnce() {
        assertEquals(1, ViewHistory.customerRows(listOf(bill(1), bill(1)), 1L, 10L, emptyMap()).size)
    }

    @Test
    fun paidAndBalanceComeFromTheReceipts() {
        val rows = ViewHistory.customerRows(listOf(bill(1, total = 100.0), bill(2, total = 50.0)), 1L, 10L, mapOf(1L to 40.0))
        val partly = rows.first { it.id == 1L }
        assertTrue(partly.isPaid)
        assertEquals(40.0, partly.paidAmount)
        assertEquals(60.0, partly.balance)
        assertFalse(rows.first { it.id == 2L }.isPaid)
    }

    @Test
    fun itemRowAddsUpItsOwnLinesAndKeepsTheBillTotalApart() {
        val rows = ViewHistory.itemRows(
            invoices = listOf(bill(1, total = 900.0), bill(2, total = 300.0), bill(3, company = 20, total = 700.0)),
            companyId = 10L,
            paid = emptyMap(),
            linesOf = { if (it.id == 1L) listOf(ItemLine(2.0, 100.0, "Nos"), ItemLine(3.0, 150.0, "Nos")) else if (it.id == 3L) listOf(ItemLine(1.0, 700.0)) else emptyList() },
            customerNameOf = { "Sivaram" },
            quantityText = { lines -> "${lines.sumOf { it.quantity }}" }
        )
        assertEquals(1, rows.size)
        assertEquals(250.0, rows[0].amount)
        assertEquals(900.0, rows[0].invoiceTotal)
        assertEquals("5.0", rows[0].note)
        assertEquals("Sivaram", rows[0].customerName)
    }
}
