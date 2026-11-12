package com.elvan.udukkai.textiles.ui.screens

import com.elvan.udukkai.data.model.InvoiceItemCatalog
import com.elvan.udukkai.data.model.ItemNames
import com.elvan.udukkai.data.model.CustomerModel
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.model.ItemModel
import com.elvan.udukkai.data.model.ReceiptModel
import com.elvan.udukkai.textiles.data.TextilesCustomerRepository
import com.elvan.udukkai.textiles.data.TextilesInvoiceRepository
import com.elvan.udukkai.textiles.data.TextilesItemRepository
import com.elvan.udukkai.textiles.data.TextilesProfileRepository
import com.elvan.udukkai.textiles.data.TextilesReceiptRepository
import com.elvan.udukkai.textiles.ui.screens.editor.TextilesCalculation
import com.elvan.udukkai.ui.screens.view.ItemLine
import com.elvan.udukkai.ui.screens.view.ViewActivity
import com.elvan.udukkai.ui.screens.view.ViewDeleteResult
import com.elvan.udukkai.ui.screens.view.ViewHistory

/** 2.0 shows as 2, 2.5 stays 2.5. */
private fun count(q: Double): String = if (q % 1.0 == 0.0) q.toLong().toString() else q.toString()

private fun paidAmounts(invoices: List<InvoiceModel>): Map<Long, Double> =
    invoices.map { it.id }.chunked(500).fold(HashMap()) { acc, ids -> acc.apply { putAll(TextilesReceiptRepository.getPaidAmounts(ids)) } }

/** A customer's tax invoices in the open company: billed, still unpaid, every bill. */
fun textilesCustomerActivity(customer: CustomerModel, year: Int): ViewActivity {
    val invoices = TextilesInvoiceRepository.activeCompanyInvoices.filter {
        it.customerId == customer.id && com.elvan.udukkai.core.utils.DateUtils.financialYearStart(it.invoiceDate) == year
    }
    val rows = ViewHistory.customerRows(invoices, customer.id, TextilesProfileRepository.profile.id, paidAmounts(invoices))
    return ViewActivity(
        count = rows.size,
        billed = rows.sumOf { it.amount },
        unpaid = rows.filter { !it.isPaid }.sumOf { it.amount },
        bills = rows,
        receipts = ViewHistory.receiptRows(
            TextilesReceiptRepository.activeCompanyReceipts.filter { com.elvan.udukkai.core.utils.DateUtils.financialYearStart(it.receiptDate) == year },
            customer.id, TextilesProfileRepository.profile.id
        ),
        estimates = ViewHistory.customerRows(invoices, customer.id, TextilesProfileRepository.profile.id, emptyMap(), estimates = true),
    )
}

/** An item's tax invoices in the open company: how much of it was billed, and every bill with its quantity and line value. */
fun textilesItemActivity(item: ItemModel, year: Int): ViewActivity {
    val id = item.id
    val profile = TextilesProfileRepository.profile
    val invoices = TextilesInvoiceRepository.activeCompanyInvoices.filter {
        com.elvan.udukkai.core.utils.DateUtils.financialYearStart(it.invoiceDate) == year
    }
    val catalog = InvoiceItemCatalog(TextilesItemRepository.items, profile.id)
    val paid = paidAmounts(invoices)
    fun rowsOf(estimates: Boolean) = ViewHistory.itemRows(
        invoices = invoices,
        companyId = profile.id,
        paid = paid,
        linesOf = { inv -> TextilesCalculation.itemsFromJson(inv.itemsJson).filter { catalog.find(it.itemId, ItemNames.snapshot(it.languageMap, it.itemName, it.itemNameEn))?.id == id }.map { ItemLine(it.quantity, it.rowTotal, it.unit) } },
        customerNameOf = { com.elvan.udukkai.data.settings.DataLanguageDisplay(profile).first(it.customerName) },
        quantityText = { lines -> "${count(lines.sumOf { it.quantity })} ${lines.first().unit}".trim() },
        estimates = estimates
    )
    val rows = rowsOf(false)
    return ViewActivity(count = rows.size, billed = rows.sumOf { it.amount }, bills = rows, estimates = rowsOf(true))
}

@androidx.compose.runtime.Composable
fun textilesHistoryYears(): List<Int> {
    val invoices = TextilesInvoiceRepository.activeCompanyInvoices
    val receipts = TextilesReceiptRepository.activeCompanyReceipts
    return androidx.compose.runtime.remember(invoices, receipts) { ViewHistory.financialYears(invoices, receipts) }
}

fun textilesDeleteCustomer(id: Long) = if (TextilesCustomerRepository.delete(id)) ViewDeleteResult.Deleted else ViewDeleteResult.Failed

fun textilesDeleteItem(id: Long) = if (TextilesItemRepository.delete(id)) ViewDeleteResult.Deleted else ViewDeleteResult.Failed

/** Linked receipts protect an invoice from deletion, regardless of the financial year. */
fun textilesDeleteInvoice(invoice: InvoiceModel) = when {
    TextilesInvoiceRepository.hasLinkedReceipts(invoice.id) -> ViewDeleteResult.HasReceipts
    TextilesInvoiceRepository.delete(invoice.id) -> ViewDeleteResult.Deleted
    else -> ViewDeleteResult.Failed
}

fun textilesDeleteReceipt(receipt: ReceiptModel) = when {
    TextilesReceiptRepository.isLocked(receipt) -> ViewDeleteResult.BooksLocked
    TextilesReceiptRepository.delete(receipt.id) -> ViewDeleteResult.Deleted
    else -> ViewDeleteResult.Failed
}
