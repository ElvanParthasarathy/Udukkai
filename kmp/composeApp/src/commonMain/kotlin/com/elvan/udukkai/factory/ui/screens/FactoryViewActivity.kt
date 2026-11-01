package com.elvan.udukkai.factory.ui.screens

import com.elvan.udukkai.data.model.CustomerModel
import com.elvan.udukkai.data.model.ItemModel
import com.elvan.udukkai.factory.data.FactoryInvoiceRepository
import com.elvan.udukkai.factory.ui.screens.editor.FactoryCalculation
import com.elvan.udukkai.ui.screens.view.ViewActivity
import com.elvan.udukkai.ui.screens.view.ViewBillRow
import kotlin.math.roundToLong

/** "33.680 kg": three decimals, as on the bill. */
private fun kg(weight: Double): String {
    val grams = (weight * 1000).roundToLong()
    return "${grams / 1000}.${(grams % 1000).toString().padStart(3, '0')} kg"
}

private fun billRow(inv: com.elvan.udukkai.data.model.InvoiceModel, note: String = "") = ViewBillRow(
    id = inv.id,
    number = inv.invoiceNumber,
    date = inv.invoiceDate,
    amount = inv.totalAmount,
    isPaid = FactoryInvoiceRepository.hasLinkedReceipts(inv.id),
    note = note
)

/** A customer's bills: what they have been billed, what is still unpaid, and the latest five. */
fun factoryCustomerActivity(customer: CustomerModel): ViewActivity {
    val bills = FactoryInvoiceRepository.invoices
        .filter { it.customerId == customer.id }
        .sortedByDescending { it.invoiceDate }
    val rows = bills.map { billRow(it) }
    return ViewActivity(
        count = bills.size,
        billed = bills.sumOf { it.totalAmount },
        unpaid = rows.filter { !it.isPaid }.sumOf { it.amount },
        bills = rows.take(5)
    )
}

/** An item's bills: how often and how much it was billed, and the latest five with its weight on each. */
fun factoryItemActivity(item: ItemModel): ViewActivity {
    val id = item.id.toString()
    val used = FactoryInvoiceRepository.invoices
        .map { inv -> inv to FactoryCalculation.itemsFromJson(inv.itemsJson).filter { it.itemId == id } }
        .filter { it.second.isNotEmpty() }
        .sortedByDescending { it.first.invoiceDate }
    val lines = used.flatMap { it.second }
    return ViewActivity(
        count = used.size,
        billed = lines.sumOf { it.rowTotal.toDouble() },
        quantityText = if (used.isEmpty()) "" else kg(lines.sumOf { it.weight }),
        bills = used.take(5).map { (inv, rows) -> billRow(inv, kg(rows.sumOf { it.weight })) }
    )
}
