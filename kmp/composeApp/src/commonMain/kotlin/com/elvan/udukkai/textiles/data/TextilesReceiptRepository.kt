package com.elvan.udukkai.textiles.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.elvan.udukkai.core.backup.AutoBackupManager
import com.elvan.udukkai.data.database.getTextilesDatabaseHelper
import com.elvan.udukkai.data.model.ReceiptInvoiceLinkModel
import com.elvan.udukkai.core.utils.SearchHelper
import com.elvan.udukkai.data.model.ReceiptModel
import com.elvan.udukkai.data.model.InvoiceModel

/**
 * Dedicated receipt repository for Udukkai Textiles (சவுளி).
 */
object TextilesReceiptRepository {

    var receipts by mutableStateOf<List<ReceiptModel>>(emptyList())
        private set

    var deletedReceipts by mutableStateOf<List<ReceiptModel>>(emptyList())
        private set

    var searchQuery by mutableStateOf("")

    var startDateFilter by mutableStateOf<Long?>(null)
    var endDateFilter by mutableStateOf<Long?>(null)
    val isDateFilterActive: Boolean get() = startDateFilter != null || endDateFilter != null

    fun setDateRange(start: Long?, end: Long?) {
        startDateFilter = start
        endDateFilter = end
    }

    fun clearDateFilter() {
        startDateFilter = null
        endDateFilter = null
    }

    val activeCompanyReceipts: List<ReceiptModel>
        get() {
            val activeId = TextilesProfileRepository.profile.id
            return if (activeId != null) {
                receipts.filter { it.companyId == activeId }
            } else {
                receipts
            }
        }

    val filteredReceipts: List<ReceiptModel>
        get() {
            return activeCompanyReceipts.filter { r ->
                val matchesQuery = SearchHelper.matchesReceipt(r, searchQuery)
                val matchesDate = when {
                    startDateFilter != null && endDateFilter != null -> r.receiptDate in startDateFilter!!..endDateFilter!!
                    startDateFilter != null -> r.receiptDate >= startDateFilter!!
                    endDateFilter != null -> r.receiptDate <= endDateFilter!!
                    else -> true
                }
                matchesQuery && matchesDate
            }
        }

    val recentReceipts: List<ReceiptModel>
        get() = activeCompanyReceipts.take(5)

    init {
        loadAll()
        loadDeleted()
    }

    private val linksCache = mutableMapOf<Long, List<String>>()

    fun loadAll() {
        try {
            receipts = getTextilesDatabaseHelper().loadAllReceipts()
            linksCache.clear()
            for (r in receipts) {
                getLinkedInvoiceNumbers(r.id)
            }
        } catch (_: Exception) {
            receipts = emptyList()
        }
    }

    fun loadDeleted() {
        try {
            deletedReceipts = getTextilesDatabaseHelper().loadDeletedReceipts()
            for (r in deletedReceipts) {
                getLinkedInvoiceNumbers(r.id)
            }
        } catch (_: Exception) {
            deletedReceipts = emptyList()
        }
    }

    fun purgeExpired(days: Int = 30): Int {
        val count = getTextilesDatabaseHelper().purgeExpiredReceipts(days)
        if (count > 0) {
            loadDeleted()
        }
        return count
    }

    fun save(receipt: ReceiptModel): Long {
        val helper = getTextilesDatabaseHelper()
        val id = helper.saveReceipt(receipt)
        if (id > 0) {
            loadAll()
            AutoBackupManager.onDataChanged()
        }
        return id
    }

    fun saveWithLinks(receipt: ReceiptModel, links: List<ReceiptInvoiceLinkModel>): Long {
        val helper = getTextilesDatabaseHelper()
        val id = helper.saveReceiptWithLinks(receipt, links)
        if (id > 0) {
            loadAll()
            TextilesInvoiceRepository.loadAll()
            AutoBackupManager.onDataChanged()
        }
        return id
    }

    fun delete(id: Long): Boolean {
        val success = getTextilesDatabaseHelper().deleteReceipt(id)
        if (success) {
            loadAll()
            loadDeleted()
            TextilesInvoiceRepository.loadAll()
            AutoBackupManager.onDataChanged()
        }
        return success
    }

    fun restore(id: Long): Boolean {
        val success = getTextilesDatabaseHelper().restoreReceipt(id)
        if (success) {
            loadAll()
            loadDeleted()
            TextilesInvoiceRepository.loadAll()
            AutoBackupManager.onDataChanged()
        }
        return success
    }

    fun permanentDelete(id: Long): Boolean {
        val success = getTextilesDatabaseHelper().permanentDeleteReceipt(id)
        if (success) {
            loadDeleted()
            TextilesInvoiceRepository.loadAll()
            AutoBackupManager.onDataChanged()
        }
        return success
    }

    fun getLinks(receiptId: Long): List<ReceiptInvoiceLinkModel> =
        getTextilesDatabaseHelper().getLinksForReceipt(receiptId)

    fun getLinksForReceipt(receiptId: Long): List<ReceiptInvoiceLinkModel> {
        val existing = getLinks(receiptId)
        if (existing.isNotEmpty()) return existing

        val receipt = receipts.find { it.id == receiptId }
            ?: deletedReceipts.find { it.id == receiptId }
            ?: return emptyList()

        val mockItem = com.elvan.udukkai.data.mock.mockTextilesReceipts.find { it["receiptNo"] == receipt.receiptNumber }
            ?: return emptyList()
        val against = mockItem["againstInvoice"] as? String ?: return emptyList()

        val invoices = TextilesInvoiceRepository.invoices.ifEmpty {
            try { getTextilesDatabaseHelper().loadAllInvoices() } catch (_: Exception) { emptyList() }
        }
        val invEnToId = invoices.associate { it.invoiceNumber to it.id }
        val invEnToTotal = invoices.associate { it.invoiceNumber to it.totalAmount }

        val links = mutableListOf<ReceiptInvoiceLinkModel>()
        val invList = against.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        var remaining = receipt.amount
        for (invEn in invList) {
            val id = invEnToId[invEn] ?: continue
            val balance = invEnToTotal[invEn] ?: 0.0
            val apply = remaining.coerceIn(0.0, balance)
            if (apply > 0.0) {
                links.add(ReceiptInvoiceLinkModel(receiptId = receipt.id, invoiceId = id, appliedAmount = apply))
                remaining -= apply
            }
        }
        if (links.isNotEmpty()) {
            try {
                getTextilesDatabaseHelper().saveReceiptWithLinks(receipt, links)
            } catch (_: Exception) {}
            return links
        }
        return emptyList()
    }

    fun getLinkedInvoiceNumbers(receiptId: Long): List<String> {
        return linksCache.getOrPut(receiptId) {
            val links = getLinksForReceipt(receiptId)
            if (links.isEmpty()) return@getOrPut emptyList()
            val invIds = links.map { it.invoiceId }.toSet()
            val invoices = TextilesInvoiceRepository.invoices.ifEmpty {
                try { getTextilesDatabaseHelper().loadAllInvoices() } catch (_: Exception) { emptyList() }
            }.filter { it.id in invIds }
            links.mapNotNull { link -> invoices.find { it.id == link.invoiceId }?.invoiceNumber }
        }
    }


    fun getPaidAmount(invoiceId: Long): Double =
        getTextilesDatabaseHelper().getPaidAmountForInvoice(invoiceId)

    fun getPaidAmounts(invoiceIds: List<Long>): Map<Long, Double> =
        getTextilesDatabaseHelper().getPaidAmountsForInvoices(invoiceIds)

    fun getPendingBalance(invoice: InvoiceModel): Double {
        val paid = getPaidAmount(invoice.id)
        return (invoice.totalAmount - paid).coerceAtLeast(0.0)
    }

    fun getNextVanakkam(companyId: Long?): Int {
        val matching = receipts.filter {
            if (companyId != null) it.companyId == companyId else true
        }
        if (matching.isEmpty()) return 1
        val maxVanakkam = matching.maxOfOrNull { it.vanakkam } ?: 0
        val maxParsed = matching.mapNotNull { r ->
            val numStr = r.receiptNumber.substringAfterLast('/', "").ifEmpty {
                r.receiptNumber.substringAfterLast('-', "")
            }
            numStr.toIntOrNull()
        }.maxOfOrNull { it } ?: 0
        return maxOf(maxVanakkam, maxParsed) + 1
    }

    fun formatReceiptNumber(bizShort: String, vanakkam: Int): String {
        val short = bizShort.trim().ifEmpty { "BIZ" }
        val padded = if (vanakkam < 10) "0$vanakkam" else vanakkam.toString()
        return "RCP/$short/$padded"
    }

    fun isReceiptNumberDuplicate(
        patruEn: String,
        companyId: Long?,
        excludeId: Long? = null
    ): Boolean {
        val target = patruEn.trim().uppercase()
        return receipts.any { r ->
            (excludeId == null || r.id != excludeId) &&
            (companyId == null || r.companyId == companyId) &&
            r.receiptNumber.trim().uppercase() == target
        }
    }

    fun validateLinks(
        links: List<ReceiptInvoiceLinkModel>,
        excludePatruId: Long? = null
    ): String? {
        for (link in links) {
            if (link.appliedAmount <= 0) continue
            val invoice = TextilesInvoiceRepository.invoices.firstOrNull { it.id == link.invoiceId } ?: continue
            val totalPaid = getPaidAmount(link.invoiceId)
            var currentReceiptAllocation = 0.0
            if (excludePatruId != null && excludePatruId > 0) {
                val existingLinks = getLinksForReceipt(excludePatruId)
                currentReceiptAllocation = existingLinks.find { it.invoiceId == link.invoiceId }?.appliedAmount ?: 0.0
            }
            val remaining = (invoice.totalAmount - (totalPaid - currentReceiptAllocation)).coerceAtLeast(0.0)
            if (link.appliedAmount > remaining + 0.01) {
                return "${invoice.invoiceNumber} - Allocated amount exceeds remaining balance."
            }
        }
        return null
    }
}
