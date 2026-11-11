package com.elvan.udukkai.factory.ui.screens

import com.elvan.udukkai.data.model.InvoiceItemCatalog
import com.elvan.udukkai.data.model.ItemNames
import com.elvan.udukkai.data.model.CustomerModel
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.model.ItemModel
import com.elvan.udukkai.data.model.ReceiptModel
import com.elvan.udukkai.factory.data.FactoryCustomerRepository
import com.elvan.udukkai.factory.data.FactoryInvoiceRepository
import com.elvan.udukkai.factory.data.FactoryItemRepository
import com.elvan.udukkai.factory.data.FactoryProfileRepository
import com.elvan.udukkai.factory.data.FactoryReceiptRepository
import com.elvan.udukkai.factory.ui.screens.editor.FactoryCalculation
import com.elvan.udukkai.ui.screens.view.ItemLine
import com.elvan.udukkai.ui.screens.view.ViewActivity
import com.elvan.udukkai.ui.screens.view.ViewDeleteResult
import com.elvan.udukkai.ui.screens.view.ViewHistory
import kotlin.math.roundToLong

/** "33.680 kg": three decimals, as on the bill. */
private fun kg(weight: Double): String {
    val grams = (weight * 1000).roundToLong()
    return "${grams / 1000}.${(grams % 1000).toString().padStart(3, '0')} kg"
}

private fun paidAmounts(invoices: List<InvoiceModel>): Map<Long, Double> =
    invoices.map { it.id }.chunked(500).fold(HashMap()) { acc, ids -> acc.apply { putAll(FactoryReceiptRepository.getPaidAmounts(ids)) } }

/** A customer's bills in the open company: what they have been billed, what is still unpaid, every bill. */
fun factoryCustomerActivity(customer: CustomerModel): ViewActivity {
    val invoices = FactoryInvoiceRepository.activeCompanyInvoices
    val rows = ViewHistory.customerRows(invoices, customer.id, FactoryProfileRepository.profile.id, paidAmounts(invoices))
    return ViewActivity(
        count = rows.size,
        billed = rows.sumOf { it.amount },
        unpaid = rows.filter { !it.isPaid }.sumOf { it.amount },
        bills = rows,
        receipts = ViewHistory.receiptRows(FactoryReceiptRepository.activeCompanyReceipts, customer.id, FactoryProfileRepository.profile.id),
        estimates = ViewHistory.customerRows(invoices, customer.id, FactoryProfileRepository.profile.id, emptyMap(), estimates = true),
    )
}

/** An item's bills in the open company: how much of it was billed, and every bill with its weight and line value. */
fun factoryItemActivity(item: ItemModel): ViewActivity {
    val id = item.id
    val profile = FactoryProfileRepository.profile
    val invoices = FactoryInvoiceRepository.activeCompanyInvoices
    val catalog = InvoiceItemCatalog(FactoryItemRepository.items, profile.id)
    val paid = paidAmounts(invoices)
    var totalWeight = 0.0
    fun rowsOf(estimates: Boolean) = ViewHistory.itemRows(
        invoices = invoices,
        companyId = profile.id,
        paid = paid,
        linesOf = { inv -> FactoryCalculation.itemsFromJson(inv.itemsJson).filter { catalog.find(it.itemId, ItemNames.snapshot(it.languageMap, it.itemName, it.itemNameEn))?.id == id }.map { ItemLine(it.weight, it.rowTotal.toDouble()) } },
        customerNameOf = { com.elvan.udukkai.data.settings.DataLanguageDisplay(profile).first(it.customerName) },
        quantityText = { lines -> lines.sumOf { it.quantity }.let { if (!estimates) totalWeight += it; kg(it) } },
        estimates = estimates
    )
    val rows = rowsOf(false)
    return ViewActivity(
        count = rows.size,
        billed = rows.sumOf { it.amount },
        quantityText = if (rows.isEmpty()) "" else kg(totalWeight),
        bills = rows,
        estimates = rowsOf(true)
    )
}

fun factoryDeleteCustomer(id: Long) = if (FactoryCustomerRepository.delete(id)) ViewDeleteResult.Deleted else ViewDeleteResult.Failed

fun factoryDeleteItem(id: Long) = if (FactoryItemRepository.delete(id)) ViewDeleteResult.Deleted else ViewDeleteResult.Failed

/** The rules of the list's delete: a bill with a receipt stays, and so does one in a closed year. */
fun factoryDeleteInvoice(invoice: InvoiceModel) = when {
    FactoryInvoiceRepository.hasLinkedReceipts(invoice.id) -> ViewDeleteResult.HasReceipts
    FactoryInvoiceRepository.isLocked(invoice) -> ViewDeleteResult.BooksLocked
    FactoryInvoiceRepository.delete(invoice.id) -> ViewDeleteResult.Deleted
    else -> ViewDeleteResult.Failed
}

fun factoryDeleteReceipt(receipt: ReceiptModel) = when {
    FactoryReceiptRepository.isLocked(receipt) -> ViewDeleteResult.BooksLocked
    FactoryReceiptRepository.delete(receipt.id) -> ViewDeleteResult.Deleted
    else -> ViewDeleteResult.Failed
}
