package com.elvan.udukkai.textiles.data

import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.trStatic
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

    // Invoice id -> paid amount. Cards ask for this once each while composing, so it is filled
    // by ONE batched query per data change instead of one database open per card.
    private var paidCache: Map<Long, Double>? = null

    fun invalidatePaidCache() {
        paidCache = null
    }

    fun hasPaidAmount(invoiceId: Long): Boolean {
        val cache = paidCache ?: run {
            val ids = TextilesInvoiceRepository.invoices.map { it.id }
            val loaded = try {
                ids.chunked(500).fold(HashMap<Long, Double>()) { acc, chunk ->
                    acc.apply { putAll(getTextilesDatabaseHelper().getPaidAmountsForInvoices(chunk)) }
                }
            } catch (_: Exception) {
                emptyMap<Long, Double>()
            }
            loaded.also { paidCache = it }
        }
        return (cache[invoiceId] ?: 0.0) > 0.0
    }

    fun loadAll() {
        try {
            paidCache = null
            receipts = getTextilesDatabaseHelper().loadAllReceipts()
            linksCache.clear()
            cacheLinkedNumbers(receipts)
        } catch (_: Exception) {
            receipts = emptyList()
        }
    }

    fun loadDeleted() {
        try {
            paidCache = null
            deletedReceipts = getTextilesDatabaseHelper().loadDeletedReceipts()
            cacheLinkedNumbers(deletedReceipts)
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

    enum class RestoreBlock { NumberTaken, InvoicePaid }

    /** Why a deleted receipt cannot come back, or null when it can. */
    fun restoreBlocker(id: Long): RestoreBlock? {
        val r = deletedReceipts.find { it.id == id } ?: return null
        val target = r.receiptNumber.trim().uppercase()
        if (receipts.any { it.companyId == r.companyId && it.receiptNumber.trim().uppercase() == target }) return RestoreBlock.NumberTaken
        val activeInvoiceIds = TextilesInvoiceRepository.invoices.map { it.id }.toSet()
        if (getLinks(id).any { it.invoiceId in activeInvoiceIds && getPaidAmount(it.invoiceId) > 0.0 }) return RestoreBlock.InvoicePaid
        return null
    }

    fun restore(id: Long): Boolean {
        if (restoreBlocker(id) != null) return false
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

    fun getLinksForReceipt(receiptId: Long): List<ReceiptInvoiceLinkModel> = getLinks(receiptId)

    // The linked invoice numbers of many receipts from one links query, not one query per receipt
    private fun cacheLinkedNumbers(list: List<ReceiptModel>) {
        val links = try { getTextilesDatabaseHelper().loadAllLinks().groupBy { it.receiptId } } catch (_: Exception) { emptyMap() }
        if (links.isEmpty()) return
        val numbers = TextilesInvoiceRepository.invoices.associate { it.id to it.invoiceNumber }
        for (r in list) {
            linksCache[r.id] = links[r.id].orEmpty().mapNotNull { numbers[it.invoiceId] }
        }
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

    fun getNextVanakkam(companyId: Long?): Int {
        val matching = (receipts + deletedReceipts).filter {
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
        return (receipts + deletedReceipts).any { r ->
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
            if (invoice.invoiceType == "proforma") return "${invoice.invoiceNumber} - ${K.proformaNoReceipt.trStatic()}"
            val totalPaid = getPaidAmount(link.invoiceId)
            var currentReceiptAllocation = 0.0
            if (excludePatruId != null && excludePatruId > 0) {
                val existingLinks = getLinksForReceipt(excludePatruId)
                currentReceiptAllocation = existingLinks.find { it.invoiceId == link.invoiceId }?.appliedAmount ?: 0.0
            }
            // A receipt pays the invoice: another receipt on it is refused
            if (totalPaid - currentReceiptAllocation > 0.0) return "${invoice.invoiceNumber} - ${K.invoiceAlreadyPaid.trStatic()}"
            val remaining = (invoice.totalAmount - (totalPaid - currentReceiptAllocation)).coerceAtLeast(0.0)
            if (link.appliedAmount > remaining + 0.01) {
                return "${invoice.invoiceNumber} - Allocated amount exceeds remaining balance."
            }
        }
        return null
    }
}
