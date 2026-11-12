package com.elvan.udukkai.core.finance

import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.model.ReceiptInvoiceLinkModel
import com.elvan.udukkai.data.model.ReceiptModel
import com.elvan.udukkai.core.utils.DateUtils
import kotlin.math.roundToLong

/** A receipt settles its selected bills completely; several bills may share one receipt. */
object FullReceiptSettlement {
    /** A new receipt may settle existing invoices in a closed book without reopening that book. */
    fun canRecordInYear(date: Long, isNew: Boolean, invoices: List<InvoiceModel>): Boolean {
        if (!YearLock.isLocked(date)) return true
        val year = DateUtils.financialYearStart(date)
        return isNew && invoices.isNotEmpty() && invoices.all {
            !it.isDeleted && it.invoiceType != "proforma" && DateUtils.financialYearStart(it.invoiceDate) == year
        }
    }

    fun total(invoices: List<InvoiceModel>): Double = invoices.sumOf { cents(it.totalAmount) } / 100.0

    fun matches(amount: Double, invoices: List<InvoiceModel>): Boolean =
        amount.isFinite() && amount > 0.0 && invoices.isNotEmpty() &&
            invoices.all { it.totalAmount.isFinite() && it.totalAmount >= 0.0 } &&
            cents(amount) == invoices.sumOf { cents(it.totalAmount) }

    fun matches(receipt: ReceiptModel, links: List<ReceiptInvoiceLinkModel>, invoices: List<InvoiceModel>): Boolean {
        // Old unlinked receipts remain editable; the editor requires bills for a new receipt.
        if (links.isEmpty()) return true
        if (links.map { it.invoiceId }.distinct().size != links.size) return false
        val byId = invoices.associateBy { it.id }
        val selected = links.map { byId[it.invoiceId] ?: return false }
        return selected.all { !it.isDeleted && it.companyId == receipt.companyId &&
            it.customerId == receipt.customerId && DateUtils.dayOf(it.invoiceDate) <= DateUtils.dayOf(receipt.receiptDate) } &&
            matches(receipt.amount, selected) && links.all {
            it.appliedAmount.isFinite() && cents(it.appliedAmount) == cents(byId.getValue(it.invoiceId).totalAmount)
        }
    }

    /** Read eligible bills inside the receipt's write transaction, excluding the receipt being edited. */
    internal fun eligibleBillsQuery(invoices: String, receipts: String, links: String, count: Int): String {
        val ids = List(count) { "?" }.joinToString()
        return """
            SELECT i.* FROM "$invoices" i
            WHERE i.id IN ($ids) AND i.is_deleted = 0 AND NOT EXISTS (
                SELECT 1 FROM "$links" l JOIN "$receipts" r ON r.id = l.receipt_id
                WHERE l.invoice_id = i.id AND r.is_deleted = 0 AND r.id <> ?
            )
        """.trimIndent()
    }

    private fun cents(amount: Double): Long = (amount * 100).roundToLong()
}
