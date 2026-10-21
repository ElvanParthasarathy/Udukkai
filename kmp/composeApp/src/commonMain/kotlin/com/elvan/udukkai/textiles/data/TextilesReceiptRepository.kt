package com.elvan.udukkai.textiles.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.elvan.udukkai.core.backup.AutoBackupManager
import com.elvan.udukkai.data.database.getTextilesDatabaseHelper
import com.elvan.udukkai.data.model.PatruPattiyalInaippuTharavuru
import com.elvan.udukkai.core.utils.ThedalUdhavi
import com.elvan.udukkai.data.model.PatrugalTharavuru
import com.elvan.udukkai.data.model.PattiyalTharavuru

/**
 * Dedicated receipt repository for Udukkai Textiles (சவுளி).
 */
object TextilesReceiptRepository {

    var receipts by mutableStateOf<List<PatrugalTharavuru>>(emptyList())
        private set

    var deletedReceipts by mutableStateOf<List<PatrugalTharavuru>>(emptyList())
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

    val activeCompanyReceipts: List<PatrugalTharavuru>
        get() {
            val activeId = TextilesProfileRepository.profile.id
            return if (activeId != null) {
                receipts.filter { it.niruvanamId == activeId }
            } else {
                receipts
            }
        }

    val filteredReceipts: List<PatrugalTharavuru>
        get() {
            return activeCompanyReceipts.filter { r ->
                val matchesQuery = ThedalUdhavi.matchesReceipt(r, searchQuery)
                val matchesDate = when {
                    startDateFilter != null && endDateFilter != null -> r.patruNaal in startDateFilter!!..endDateFilter!!
                    startDateFilter != null -> r.patruNaal >= startDateFilter!!
                    endDateFilter != null -> r.patruNaal <= endDateFilter!!
                    else -> true
                }
                matchesQuery && matchesDate
            }
        }

    val recentReceipts: List<PatrugalTharavuru>
        get() = activeCompanyReceipts.take(5)

    init {
        loadAll()
        loadDeleted()
    }

    fun loadAll() {
        try {
            receipts = getTextilesDatabaseHelper().loadAllReceipts()
        } catch (_: Exception) {
            receipts = emptyList()
        }
    }

    fun loadDeleted() {
        try {
            deletedReceipts = getTextilesDatabaseHelper().loadDeletedReceipts()
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

    fun save(receipt: PatrugalTharavuru): Long {
        val helper = getTextilesDatabaseHelper()
        val id = helper.saveReceipt(receipt)
        if (id > 0) {
            loadAll()
            AutoBackupManager.onDataChanged()
        }
        return id
    }

    fun saveWithLinks(receipt: PatrugalTharavuru, links: List<PatruPattiyalInaippuTharavuru>): Long {
        val helper = getTextilesDatabaseHelper()
        val id = helper.saveReceiptWithLinks(receipt, links)
        if (id > 0) {
            loadAll()
            AutoBackupManager.onDataChanged()
        }
        return id
    }

    fun delete(id: Long): Boolean {
        val success = getTextilesDatabaseHelper().deleteReceipt(id)
        if (success) {
            loadAll()
            loadDeleted()
            AutoBackupManager.onDataChanged()
        }
        return success
    }

    fun restore(id: Long): Boolean {
        val success = getTextilesDatabaseHelper().restoreReceipt(id)
        if (success) {
            loadAll()
            loadDeleted()
            AutoBackupManager.onDataChanged()
        }
        return success
    }

    fun permanentDelete(id: Long): Boolean {
        val success = getTextilesDatabaseHelper().permanentDeleteReceipt(id)
        if (success) {
            loadDeleted()
            AutoBackupManager.onDataChanged()
        }
        return success
    }

    fun getLinks(patruId: Long): List<PatruPattiyalInaippuTharavuru> =
        getTextilesDatabaseHelper().getLinksForPatru(patruId)

    fun getLinksForPatru(patruId: Long): List<PatruPattiyalInaippuTharavuru> =
        getLinks(patruId)

    fun getPaidAmount(invoiceId: Long): Double =
        getTextilesDatabaseHelper().getPaidAmountForInvoice(invoiceId)

    fun getPaidAmounts(invoiceIds: List<Long>): Map<Long, Double> =
        getTextilesDatabaseHelper().getPaidAmountsForInvoices(invoiceIds)

    fun getPendingBalance(invoice: PattiyalTharavuru): Double {
        val paid = getPaidAmount(invoice.id)
        return (invoice.mothaThogai - paid).coerceAtLeast(0.0)
    }

    fun getNextVanakkam(niruvanamId: Long?): Int {
        val matching = receipts.filter {
            if (niruvanamId != null) it.niruvanamId == niruvanamId else true
        }
        if (matching.isEmpty()) return 1
        val maxVanakkam = matching.maxOfOrNull { it.vanakkam } ?: 0
        val maxParsed = matching.mapNotNull { r ->
            val numStr = r.patruEn.substringAfterLast('/', "").ifEmpty {
                r.patruEn.substringAfterLast('-', "")
            }
            numStr.toIntOrNull()
        }.maxOfOrNull { it } ?: 0
        return maxOf(maxVanakkam, maxParsed) + 1
    }

    fun formatPatruEn(bizShort: String, vanakkam: Int): String {
        val short = bizShort.trim().ifEmpty { "BIZ" }
        val padded = if (vanakkam < 10) "0$vanakkam" else vanakkam.toString()
        return "RCP/$short/$padded"
    }

    fun isPatruEnDuplicate(
        patruEn: String,
        niruvanamId: Long?,
        excludeId: Long? = null
    ): Boolean {
        val target = patruEn.trim().uppercase()
        return receipts.any { r ->
            (excludeId == null || r.id != excludeId) &&
            (niruvanamId == null || r.niruvanamId == niruvanamId) &&
            r.patruEn.trim().uppercase() == target
        }
    }

    fun validateLinks(
        links: List<PatruPattiyalInaippuTharavuru>,
        excludePatruId: Long? = null
    ): String? {
        for (link in links) {
            if (link.poruthiyaThogai <= 0) continue
            val invoice = TextilesInvoiceRepository.invoices.firstOrNull { it.id == link.pattiyalId } ?: continue
            val totalPaid = getPaidAmount(link.pattiyalId)
            var currentReceiptAllocation = 0.0
            if (excludePatruId != null && excludePatruId > 0) {
                val existingLinks = getLinksForPatru(excludePatruId)
                currentReceiptAllocation = existingLinks.find { it.pattiyalId == link.pattiyalId }?.poruthiyaThogai ?: 0.0
            }
            val remaining = (invoice.mothaThogai - (totalPaid - currentReceiptAllocation)).coerceAtLeast(0.0)
            if (link.poruthiyaThogai > remaining + 0.01) {
                return "${invoice.patrucheettuEn} - Allocated amount exceeds remaining balance."
            }
        }
        return null
    }
}
