package com.elvan.udukkai.ui.screens.view

import com.elvan.udukkai.core.finance.BillNumbering
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.model.ReceiptModel

/** One bill's lines of a single item, already read from its stored lines. */
data class ItemLine(val quantity: Double, val amount: Double, val unit: String = "")

/** Why a delete from a view page did or did not happen. */
enum class ViewDeleteResult { Deleted, HasReceipts, BooksLocked, Failed }

enum class ViewRowKind { Invoice, Receipt, Estimate }

enum class HistorySort { DateNew, DateOld, NumberHigh, NumberLow }

/**
 * The transaction lists of the customer and item pages, worked out from the saved bills. Estimates are left out as in
 * the GST report, a bill counts once however it came in, and the newest comes first.
 */
object ViewHistory {
    private fun bills(invoices: List<InvoiceModel>, companyId: Long?, estimates: Boolean): List<InvoiceModel> =
        invoices.filter { (it.invoiceType == "proforma") == estimates && !it.isDeleted && (companyId == null || it.companyId == companyId) }
            .distinctBy { it.id }
            .sortedWith(compareByDescending<InvoiceModel> { it.invoiceDate }.thenByDescending { it.id })

    private fun row(inv: InvoiceModel, amount: Double, paid: Map<Long, Double>, note: String = "", customerName: String = "", invoiceTotal: Double? = null, kind: ViewRowKind = ViewRowKind.Invoice): ViewBillRow {
        val paidAmount = paid[inv.id] ?: 0.0
        return ViewBillRow(
            id = inv.id,
            number = inv.invoiceNumber,
            date = inv.invoiceDate,
            amount = amount,
            isPaid = paidAmount > 0.0, // the same rule as the bill's own page: a bill with a receipt is paid
            note = note,
            customerName = customerName,
            invoiceTotal = invoiceTotal,
            paidAmount = paidAmount,
            balance = (inv.totalAmount - paidAmount).coerceAtLeast(0.0),
            kind = kind
        )
    }

    fun customerRows(invoices: List<InvoiceModel>, customerId: Long, companyId: Long?, paid: Map<Long, Double>, estimates: Boolean = false): List<ViewBillRow> =
        bills(invoices, companyId, estimates).filter { it.customerId == customerId }
            .map { row(it, it.totalAmount, paid, kind = if (estimates) ViewRowKind.Estimate else ViewRowKind.Invoice) }

    fun receiptRows(receipts: List<ReceiptModel>, customerId: Long, companyId: Long?): List<ViewBillRow> =
        receipts.filter { it.customerId == customerId && !it.isDeleted && (companyId == null || it.companyId == companyId) }
            .distinctBy { it.id }
            .sortedWith(compareByDescending<ReceiptModel> { it.receiptDate }.thenByDescending { it.id })
            .map { ViewBillRow(id = it.id, number = it.receiptNumber, date = it.receiptDate, amount = it.amount, isPaid = true, kind = ViewRowKind.Receipt, paymentMode = it.paymentMode) }

    /** [linesOf] gives this item's stored lines on a bill; several lines of it on one bill make one row. */
    fun itemRows(
        invoices: List<InvoiceModel>,
        companyId: Long?,
        paid: Map<Long, Double>,
        linesOf: (InvoiceModel) -> List<ItemLine>,
        customerNameOf: (InvoiceModel) -> String,
        quantityText: (List<ItemLine>) -> String,
        estimates: Boolean = false
    ): List<ViewBillRow> = bills(invoices, companyId, estimates).mapNotNull { inv ->
        val lines = linesOf(inv)
        if (lines.isEmpty()) null
        else row(inv, lines.sumOf { it.amount }, paid, quantityText(lines), customerNameOf(inv), inv.totalAmount, if (estimates) ViewRowKind.Estimate else ViewRowKind.Invoice)
    }

    /** Proformas among the invoices, newest first; the totals of a page still count only the invoices. */
    fun withEstimates(bills: List<ViewBillRow>, estimates: List<ViewBillRow>): List<ViewBillRow> =
        (bills + estimates).sortedWith(compareByDescending<ViewBillRow> { it.date }.thenByDescending { it.id })

    fun apply(rows: List<ViewBillRow>, sort: HistorySort, startMillis: Long? = null, endMillis: Long? = null): List<ViewBillRow> {
        val shown = if (startMillis == null && endMillis == null) rows else rows.filter {
            (startMillis == null || it.date >= startMillis) && (endMillis == null || it.date <= endMillis)
        }
        fun serial(r: ViewBillRow) = BillNumbering.serialOf(r.number) ?: 0
        return when (sort) {
            HistorySort.DateNew -> shown // already newest first
            HistorySort.DateOld -> shown.reversed()
            HistorySort.NumberHigh -> shown.sortedByDescending { serial(it) }
            HistorySort.NumberLow -> shown.sortedBy { serial(it) }
        }
    }
}
