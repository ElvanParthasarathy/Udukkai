package com.elvan.udukkai.data.repository

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.mode.ModeManager
import com.elvan.udukkai.data.business.getBusinessDatabaseHelper
import com.elvan.udukkai.data.model.PattiyalTharavuru
import com.elvan.udukkai.data.settings.NiruvanaTharavugal

data class CompanyInvoiceStat(
    val id: Long,
    val name: String,
    val total: Double,
    val count: Int
)

/**
 * Reactive repository managing invoices (Pattiyal) for both Coolie and Silk modes.
 */
object PattiyalRepository {

    var invoices by mutableStateOf<List<PattiyalTharavuru>>(emptyList())
        private set

    var deletedInvoices by mutableStateOf<List<PattiyalTharavuru>>(emptyList())
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

    val filteredInvoices: List<PattiyalTharavuru>
        get() {
            val q = searchQuery.trim().lowercase()
            return invoices.filter { invoice ->
                val matchesQuery = if (q.isEmpty()) true else {
                    invoice.patrucheettuEn.lowercase().contains(q) ||
                    invoice.vaangunarPeyar.values.any { it.lowercase().contains(q) } ||
                    invoice.vaangunarMunvari.values.any { it.lowercase().contains(q) }
                }
                val s = startDateFilter
                val e = endDateFilter
                val matchesDate = when {
                    s != null && e != null -> invoice.pattiyalNaal in s..e
                    s != null -> invoice.pattiyalNaal >= s
                    e != null -> invoice.pattiyalNaal <= e
                    else -> true
                }
                matchesQuery && matchesDate
            }
        }

    val recentInvoices: List<PattiyalTharavuru>
        get() = invoices.take(8)

    val overallTotal: Double
        get() = invoices.sumOf { it.mothaThogai }

    init {
        loadAll()
    }

    fun loadAll(mode: AppMode = ModeManager.currentMode) {
        try {
            val helper = getBusinessDatabaseHelper()
            invoices = helper.loadAllInvoices(mode)
        } catch (_: Exception) {
            invoices = emptyList()
        }
    }

    fun loadDeleted(mode: AppMode = ModeManager.currentMode) {
        try {
            val helper = getBusinessDatabaseHelper()
            deletedInvoices = helper.loadDeletedInvoices(mode)
        } catch (_: Exception) {
            deletedInvoices = emptyList()
        }
    }

    fun save(invoice: PattiyalTharavuru, mode: AppMode = ModeManager.currentMode): Long {
        return try {
            val helper = getBusinessDatabaseHelper()
            val id = helper.saveInvoice(mode, invoice)
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
            val success = helper.deleteInvoice(mode, id)
            if (success) {
                loadAll(mode)
                loadDeleted(mode)
            }
            success
        } catch (_: Exception) {
            false
        }
    }

    fun restore(id: Long, mode: AppMode = ModeManager.currentMode): Boolean {
        return try {
            val helper = getBusinessDatabaseHelper()
            val success = helper.restoreInvoice(mode, id)
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
            val success = helper.permanentDeleteInvoice(mode, id)
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
            val count = helper.purgeExpiredInvoices(mode, days)
            if (count > 0) {
                loadDeleted(mode)
            }
            count
        } catch (_: Exception) {
            0
        }
    }

    fun getById(id: Long): PattiyalTharavuru? {
        return invoices.find { it.id == id }
    }

    fun computeActiveCompanies(profiles: List<NiruvanaTharavugal>): List<CompanyInvoiceStat> {
        val byCompany = mutableMapOf<Long, CompanyInvoiceStat>()

        for (p in profiles) {
            val pId = p.id ?: continue
            val cName = p.kurumPeyar.ifEmpty {
                p.niruvanathinPeyar.values.firstOrNull().orEmpty()
            }
            byCompany[pId] = CompanyInvoiceStat(id = pId, name = cName, total = 0.0, count = 0)
        }

        for (inv in invoices) {
            val cId = inv.niruvanamId
            if (cId != null && byCompany.containsKey(cId)) {
                val prev = byCompany[cId]!!
                byCompany[cId] = prev.copy(
                    total = prev.total + inv.mothaThogai,
                    count = prev.count + 1
                )
            }
        }

        return byCompany.values.filter { it.count > 0 }
    }

    fun getCompaniesSummary(profiles: List<NiruvanaTharavugal>): String {
        val active = computeActiveCompanies(profiles)
        return if (active.isEmpty()) {
            if (profiles.isNotEmpty()) {
                val p = profiles.first()
                p.kurumPeyar.ifEmpty { p.niruvanathinPeyar.values.firstOrNull().orEmpty() }.ifEmpty { "—" }
            } else "—"
        } else {
            active.joinToString(", ") { it.name }
        }
    }

    fun getInvoiceCountSummary(profiles: List<NiruvanaTharavugal>): String {
        val active = computeActiveCompanies(profiles)
        return when {
            active.size == 1 -> "${active[0].count} · ${active[0].name}"
            active.size > 1 -> active.joinToString("  +  ") { "${it.count} · ${it.name}" } + "  =  ${invoices.size}"
            else -> invoices.size.toString()
        }
    }

    fun getNextVanakkam(niruvanamId: Long?, mode: AppMode = ModeManager.currentMode): Int {
        val matching = if (niruvanamId != null) {
            invoices.filter { it.niruvanamId == niruvanamId }
        } else {
            invoices.filter { it.niruvanamId == null }
        }
        val maxVanakkam = matching.maxOfOrNull { it.vanakkam } ?: 0
        val parsedMax = matching.mapNotNull { inv ->
            inv.patrucheettuEn.substringAfterLast('-', "").toIntOrNull()
        }.maxOfOrNull { it } ?: 0
        return maxOf(maxVanakkam, parsedMax) + 1
    }

    fun formatPattiyalEn(prefix: String, vanakkam: Int): String {
        val padded = if (vanakkam < 10) vanakkam.toString().padStart(2, '0') else vanakkam.toString()
        return "$prefix-$padded"
    }

    fun getNextInvoiceNumber(niruvanamId: Long?, prefix: String, mode: AppMode = ModeManager.currentMode): String {
        val vanakkam = getNextVanakkam(niruvanamId, mode)
        return formatPattiyalEn(prefix, vanakkam)
    }
}
