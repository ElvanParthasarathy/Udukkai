package com.elvan.udukkai.factory.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.elvan.udukkai.core.backup.AutoBackupManager
import com.elvan.udukkai.data.database.getFactoryDatabaseHelper
import com.elvan.udukkai.core.utils.ThedalUdhavi
import com.elvan.udukkai.data.model.PattiyalTharavuru

object FactoryInvoiceRepository {

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

    val activeCompanyInvoices: List<PattiyalTharavuru>
        get() {
            val activeId = FactoryProfileRepository.profile.id
            return if (activeId != null) {
                invoices.filter { it.niruvanamId == activeId }
            } else {
                invoices
            }
        }

    val filteredInvoices: List<PattiyalTharavuru>
        get() {
            return activeCompanyInvoices.filter { inv ->
                val matchesQuery = ThedalUdhavi.matchesInvoice(inv, searchQuery)

                val matchesDate = when {
                    startDateFilter != null && endDateFilter != null ->
                        inv.pattiyalNaal in startDateFilter!!..endDateFilter!!
                    startDateFilter != null -> inv.pattiyalNaal >= startDateFilter!!
                    endDateFilter != null -> inv.pattiyalNaal <= endDateFilter!!
                    else -> true
                }

                matchesQuery && matchesDate
            }
        }

    val overallTotal: Double
        get() = activeCompanyInvoices.sumOf { it.mothaThogai }

    val recentInvoices: List<PattiyalTharavuru>
        get() = activeCompanyInvoices.take(5)

    init {
        loadAll()
        loadDeleted()
    }

    fun getNextVanakkam(niruvanamId: Long?): Int {
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

    fun getNextInvoiceNumber(niruvanamId: Long?, prefix: String): String {
        val vanakkam = getNextVanakkam(niruvanamId)
        return formatPattiyalEn(prefix, vanakkam)
    }

    fun loadAll() {
        try {
            invoices = getFactoryDatabaseHelper().loadAllInvoices()
        } catch (_: Exception) {
            invoices = emptyList()
        }
    }

    fun purgeExpired(days: Int = 30): Int {
        val count = getFactoryDatabaseHelper().purgeExpiredInvoices(days)
        if (count > 0) loadDeleted()
        return count
    }

    fun loadDeleted() {
        try {
            deletedInvoices = getFactoryDatabaseHelper().loadDeletedInvoices()
        } catch (_: Exception) {
            deletedInvoices = emptyList()
        }
    }

    fun save(invoice: PattiyalTharavuru): Long {
        val helper = getFactoryDatabaseHelper()
        val id = helper.saveInvoice(invoice)
        if (id > 0) {
            loadAll()
            AutoBackupManager.onDataChanged()
        }
        return id
    }

    fun delete(id: Long): Boolean {
        val success = getFactoryDatabaseHelper().deleteInvoice(id)
        if (success) {
            loadAll()
            loadDeleted()
            AutoBackupManager.onDataChanged()
        }
        return success
    }

    fun restore(id: Long): Boolean {
        val success = getFactoryDatabaseHelper().restoreInvoice(id)
        if (success) {
            loadAll()
            loadDeleted()
            AutoBackupManager.onDataChanged()
        }
        return success
    }

    fun permanentDelete(id: Long): Boolean {
        val success = getFactoryDatabaseHelper().permanentDeleteInvoice(id)
        if (success) {
            loadDeleted()
            AutoBackupManager.onDataChanged()
        }
        return success
    }
}
