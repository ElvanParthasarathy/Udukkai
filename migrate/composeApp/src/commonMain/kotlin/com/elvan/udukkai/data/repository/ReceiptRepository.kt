package com.elvan.udukkai.data.repository

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.mode.ModeManager
import com.elvan.udukkai.data.business.getBusinessDatabaseHelper
import com.elvan.udukkai.data.model.PattiyalTharavuru
import com.elvan.udukkai.data.model.PatruPattiyalInaippuTharavuru
import com.elvan.udukkai.data.model.PatrugalTharavuru

/**
 * Reactive repository managing payment receipts (Patrugal) and invoice junction links
 * for both Coolie and Silk modes. Matches Flutter's PatruKalanjiyam architecture 1:1.
 */
object PatrugalRepository {

    var receipts by mutableStateOf<List<PatrugalTharavuru>>(emptyList())
        private set

    var deletedReceipts by mutableStateOf<List<PatrugalTharavuru>>(emptyList())
        private set

    var searchQuery by mutableStateOf("")
    var startDateFilter by mutableStateOf<Long?>(null)
    var endDateFilter by mutableStateOf<Long?>(null)

    val isDateFilterActive: Boolean
        get() = startDateFilter != null || endDateFilter != null

    fun setDateRange(start: Long?, end: Long?) {
        startDateFilter = start
        endDateFilter = end
    }

    fun clearDateFilter() {
        startDateFilter = null
        endDateFilter = null
    }

    val filteredReceipts: List<PatrugalTharavuru>
        get() {
            val q = searchQuery.trim().lowercase()
            return receipts.filter { receipt ->
                val matchesQuery = if (q.isEmpty()) true else {
                    receipt.patruEn.lowercase().contains(q) ||
                    receipt.vaangunarPeyar.values.any { it.lowercase().contains(q) } ||
                    receipt.seluthumMurai.lowercase().contains(q) ||
                    receipt.vangiPeyar?.lowercase()?.contains(q) == true
                }
                val s = startDateFilter
                val e = endDateFilter
                val matchesDate = when {
                    s != null && e != null -> receipt.patruNaal in s..e
                    s != null -> receipt.patruNaal >= s
                    e != null -> receipt.patruNaal <= e
                    else -> true
                }
                matchesQuery && matchesDate
            }
        }

    val overallTotal: Double
        get() = receipts.sumOf { it.thogai }

    init {
        loadAll()
    }

    fun loadAll(mode: AppMode = ModeManager.currentMode) {
        try {
            val helper = getBusinessDatabaseHelper()
            receipts = helper.loadAllReceipts(mode)
        } catch (_: Exception) {
            receipts = emptyList()
        }
    }

    fun loadDeleted(mode: AppMode = ModeManager.currentMode) {
        try {
            val helper = getBusinessDatabaseHelper()
            deletedReceipts = helper.loadDeletedReceipts(mode)
        } catch (_: Exception) {
            deletedReceipts = emptyList()
        }
    }

    fun save(receipt: PatrugalTharavuru, mode: AppMode = ModeManager.currentMode): Long {
        return try {
            val helper = getBusinessDatabaseHelper()
            val id = helper.saveReceipt(mode, receipt)
            if (id > 0L) {
                loadAll(mode)
            }
            id
        } catch (_: Exception) {
            -1L
        }
    }

    fun saveWithLinks(
        receipt: PatrugalTharavuru,
        links: List<PatruPattiyalInaippuTharavuru>,
        mode: AppMode = ModeManager.currentMode
    ): Long {
        return try {
            val helper = getBusinessDatabaseHelper()
            val id = helper.saveReceiptWithLinks(mode, receipt, links)
            if (id > 0L) {
                loadAll(mode)
            }
            id
        } catch (_: Exception) {
            -1L
        }
    }

    fun delete(id: Long, mode: AppMode = ModeManager.currentMode): Boolean {
        return try {
            val helper = getBusinessDatabaseHelper()
            val success = helper.deleteReceipt(mode, id)
            if (success) {
                loadAll(mode)
            }
            success
        } catch (_: Exception) {
            false
        }
    }

    fun restore(id: Long, mode: AppMode = ModeManager.currentMode): Boolean {
        return try {
            val helper = getBusinessDatabaseHelper()
            val success = helper.restoreReceipt(mode, id)
            if (success) {
                loadAll(mode)
                loadDeleted(mode)
            }
            success
        } catch (_: Exception) {
            false
        }
    }

    fun permanentDelete(id: Long, mode: AppMode = ModeManager.currentMode): Boolean {
        return try {
            val helper = getBusinessDatabaseHelper()
            val success = helper.permanentDeleteReceipt(mode, id)
            if (success) {
                loadDeleted(mode)
            }
            success
        } catch (_: Exception) {
            false
        }
    }

    fun purgeExpired(days: Int = 30, mode: AppMode = ModeManager.currentMode): Int {
        return try {
            val helper = getBusinessDatabaseHelper()
            val count = helper.purgeExpiredReceipts(mode, days)
            if (count > 0) {
                loadDeleted(mode)
            }
            count
        } catch (_: Exception) {
            0
        }
    }

    fun getById(id: Long): PatrugalTharavuru? {
        return receipts.find { it.id == id }
    }

    fun getLinksForPatru(patruId: Long, mode: AppMode = ModeManager.currentMode): List<PatruPattiyalInaippuTharavuru> {
        return try {
            val helper = getBusinessDatabaseHelper()
            helper.getLinksForPatru(mode, patruId)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getPaidAmount(invoiceId: Long, mode: AppMode = ModeManager.currentMode): Double {
        return try {
            val helper = getBusinessDatabaseHelper()
            helper.getPaidAmountForInvoice(mode, invoiceId)
        } catch (_: Exception) {
            0.0
        }
    }

    fun getPaidAmountsForInvoices(invoiceIds: List<Long>, mode: AppMode = ModeManager.currentMode): Map<Long, Double> {
        return try {
            val helper = getBusinessDatabaseHelper()
            helper.getPaidAmountsForInvoices(mode, invoiceIds)
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun getPendingBalance(invoice: PattiyalTharavuru, mode: AppMode = ModeManager.currentMode): Double {
        val paid = getPaidAmount(invoice.id, mode)
        return (invoice.mothaThogai - paid).coerceAtLeast(0.0)
    }

    /**
     * Sequence auto-numbering for receipts per company.
     * Searches max vanakkam or parses trailing digits from existing receipt numbers.
     */
    fun getNextVanakkam(niruvanamId: Long?, mode: AppMode = ModeManager.currentMode): Int {
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

    /**
     * Formats receipt number: RCP/<bizShort>/<01-padded sequence>
     * E.g. RCP/KPM/01
     */
    fun formatPatruEn(bizShort: String, vanakkam: Int): String {
        val short = bizShort.trim().ifEmpty { "BIZ" }
        val padded = if (vanakkam < 10) "0$vanakkam" else vanakkam.toString()
        return "RCP/$short/$padded"
    }

    /**
     * Check if a receipt number already exists for this business.
     */
    fun isPatruEnDuplicate(
        niruvanamId: Long?,
        patruEn: String,
        excludeId: Long? = null,
        mode: AppMode = ModeManager.currentMode
    ): Boolean {
        val target = patruEn.trim().uppercase()
        return receipts.any { r ->
            (excludeId == null || r.id != excludeId) &&
            (niruvanamId == null || r.niruvanamId == niruvanamId) &&
            r.patruEn.trim().uppercase() == target
        }
    }

    /**
     * Validate link amounts against invoice balances.
     */
    fun validateLinks(
        links: List<PatruPattiyalInaippuTharavuru>,
        excludePatruId: Long? = null,
        mode: AppMode = ModeManager.currentMode
    ): String? {
        for (link in links) {
            if (link.poruthiyaThogai <= 0) continue
            val invoice = PattiyalRepository.getById(link.pattiyalId) ?: continue
            val totalPaid = getPaidAmount(link.pattiyalId, mode)
            var currentReceiptAllocation = 0.0
            if (excludePatruId != null && excludePatruId > 0) {
                val existingLinks = getLinksForPatru(excludePatruId, mode)
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
