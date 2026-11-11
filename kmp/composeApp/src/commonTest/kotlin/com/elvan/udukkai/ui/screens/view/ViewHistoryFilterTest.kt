package com.elvan.udukkai.ui.screens.view

import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.model.ReceiptModel
import kotlin.test.Test
import kotlin.test.assertEquals

class ViewHistoryFilterTest {
    private fun row(id: Long, paid: Double, balance: Double, number: String = "INV-$id") =
        ViewBillRow(id = id, number = number, date = id, amount = 100.0, isPaid = paid > 0, paidAmount = paid, balance = balance)

    private val rows = listOf(row(3, 0.0, 100.0), row(2, 40.0, 60.0), row(1, 100.0, 0.0))

    @Test
    fun dateBoundsAreInclusiveAndIndependentOfSort() {
        assertEquals(listOf(3L, 2L), ViewHistory.apply(rows, HistorySort.DateNew, startMillis = 2).map { it.id })
        assertEquals(listOf(1L, 2L), ViewHistory.apply(rows, HistorySort.DateOld, endMillis = 2).map { it.id })
        assertEquals(listOf(2L), ViewHistory.apply(rows, HistorySort.DateNew, 2, 2).map { it.id })
    }

    @Test
    fun sortsByDateAndByNumberNotAsText() {
        val numbered = listOf(row(1, 0.0, 100.0, "INV-9"), row(2, 0.0, 100.0, "INV-10"))
        assertEquals(listOf(2L, 1L), ViewHistory.apply(numbered, HistorySort.NumberHigh).map { it.id })
        assertEquals(listOf(1L, 2L, 3L), ViewHistory.apply(rows, HistorySort.DateOld).map { it.id })
    }

    @Test
    fun proformasJoinTheInvoicesAndFollowTheDateFilter() {
        val estimate = ViewBillRow(id = 9, number = "PF/A-001", date = 9, amount = 50.0, isPaid = false, kind = ViewRowKind.Estimate)
        val all = ViewHistory.withEstimates(rows, listOf(estimate))
        assertEquals(listOf(9L, 3L, 2L, 1L), ViewHistory.apply(all, HistorySort.DateNew).map { it.id })
        assertEquals(listOf(9L, 3L), ViewHistory.apply(all, HistorySort.DateNew, startMillis = 3).map { it.id })
    }

    @Test
    fun estimatesAndReceiptsAreSeparateAndStayInTheirCompany() {
        val bills = listOf(
            InvoiceModel(id = 1, companyId = 10, customerId = 1, invoiceType = "proforma"),
            InvoiceModel(id = 2, companyId = 10, customerId = 1)
        )
        assertEquals(listOf(1L), ViewHistory.customerRows(bills, 1L, 10L, emptyMap(), estimates = true).map { it.id })
        assertEquals(listOf(2L), ViewHistory.customerRows(bills, 1L, 10L, emptyMap()).map { it.id })
        val receipts = listOf(ReceiptModel(id = 5, companyId = 10, customerId = 1), ReceiptModel(id = 6, companyId = 20, customerId = 1))
        assertEquals(listOf(5L), ViewHistory.receiptRows(receipts, 1L, 10L).map { it.id })
    }
}
