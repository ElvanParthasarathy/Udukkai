package com.elvan.udukkai.textiles.ui.screens

import com.elvan.udukkai.data.model.CustomerModel
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.model.ItemModel
import com.elvan.udukkai.textiles.data.TextilesInvoiceRepository
import com.elvan.udukkai.textiles.ui.screens.editor.TextilesCalculation
import com.elvan.udukkai.ui.screens.view.ViewActivity
import com.elvan.udukkai.ui.screens.view.ViewBillRow

private fun billRow(inv: InvoiceModel, note: String = "") = ViewBillRow(
    id = inv.id,
    number = inv.invoiceNumber,
    date = inv.invoiceDate,
    amount = inv.totalAmount,
    isPaid = TextilesInvoiceRepository.hasLinkedReceipts(inv.id),
    note = note
)

/** 2.0 shows as 2, 2.5 stays 2.5. */
private fun count(q: Double): String = if (q % 1.0 == 0.0) q.toLong().toString() else q.toString()

/** A customer's tax invoices (estimates are left out, as in the GST report): billed, still unpaid, latest five. */
fun textilesCustomerActivity(customer: CustomerModel): ViewActivity {
    val bills = TextilesInvoiceRepository.invoices
        .filter { it.customerId == customer.id && it.invoiceType != "proforma" }
        .sortedByDescending { it.invoiceDate }
    val rows = bills.map { billRow(it) }
    return ViewActivity(
        count = bills.size,
        billed = bills.sumOf { it.totalAmount },
        unpaid = rows.filter { !it.isPaid }.sumOf { it.amount },
        bills = rows.take(5)
    )
}

/** An item's tax invoices: how often and how much it was billed, and the latest five with its quantity on each. */
fun textilesItemActivity(item: ItemModel): ViewActivity {
    val id = item.id.toString()
    val used = TextilesInvoiceRepository.invoices
        .filter { it.invoiceType != "proforma" }
        .map { inv -> inv to TextilesCalculation.itemsFromJson(inv.itemsJson).filter { it.itemId == id } }
        .filter { it.second.isNotEmpty() }
        .sortedByDescending { it.first.invoiceDate }
    val lines = used.flatMap { it.second }
    return ViewActivity(
        count = used.size,
        billed = lines.sumOf { it.rowTotal },
        bills = used.take(5).map { (inv, rows) -> billRow(inv, "${count(rows.sumOf { it.quantity })} ${rows.first().unit}") }
    )
}
