package com.elvan.udukkai.textiles.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.elvan.udukkai.core.backup.AutoBackupManager
import com.elvan.udukkai.data.database.getTextilesDatabaseHelper
import com.elvan.udukkai.data.model.VaangunarTharavuru

/**
 * Dedicated customer repository for Udukkai Textiles (சவுளி).
 * Distinct customer fields: Name, GSTIN, Phone, Email, PIN, State, District, City, Address.
 */
object TextilesCustomerRepository {
    val customers: List<com.elvan.udukkai.data.model.VaangunarTharavuru> get() = merchants
    val deletedCustomers: List<com.elvan.udukkai.data.model.VaangunarTharavuru> get() = deletedMerchants


    var merchants by mutableStateOf<List<VaangunarTharavuru>>(emptyList())
        private set

    var deletedMerchants by mutableStateOf<List<VaangunarTharavuru>>(emptyList())
        private set

    var searchQuery by mutableStateOf("")

    val filteredMerchants: List<VaangunarTharavuru>
        get() {
            val q = searchQuery.trim().lowercase()
            if (q.isEmpty()) return merchants

            return merchants.filter { m ->
                val nameMatches = m.peyar.values.any { it.lowercase().contains(q) }
                val gstinMatches = m.gstin.lowercase().contains(q)
                val phoneMatches = m.tholaipaesi.lowercase().contains(q)
                val emailMatches = m.minnanjal.lowercase().contains(q)
                val pinMatches = m.anjalKuriyeedu.lowercase().contains(q)
                val stateMatches = m.maanilam.values.any { it.lowercase().contains(q) }
                val districtMatches = m.maavattam.values.any { it.lowercase().contains(q) }
                val cityMatches = m.oor.values.any { it.lowercase().contains(q) }
                val addressMatches = m.mugavari.values.any { it.lowercase().contains(q) }

                nameMatches || gstinMatches || phoneMatches || emailMatches || pinMatches || stateMatches || districtMatches || cityMatches || addressMatches
            }
        }

    init {
        loadAll()
        loadDeleted()
    }

    fun loadAll() {
        try {
            merchants = getTextilesDatabaseHelper().loadAllMerchants()
        } catch (_: Exception) {
            merchants = emptyList()
        }
    }

    fun purgeExpired(days: Int = 30): Int {
        val count = getTextilesDatabaseHelper().purgeExpiredMerchants(days)
        if (count > 0) loadDeleted()
        return count
    }

    fun loadDeleted() {
        try {
            deletedMerchants = getTextilesDatabaseHelper().loadDeletedMerchants()
        } catch (_: Exception) {
            deletedMerchants = emptyList()
        }
    }

    fun save(merchant: VaangunarTharavuru): Long {
        val helper = getTextilesDatabaseHelper()
        val id = helper.saveMerchant(merchant)
        if (id > 0) {
            loadAll()
            AutoBackupManager.onDataChanged()
        }
        return id
    }

    fun delete(id: Long): Boolean {
        val success = getTextilesDatabaseHelper().deleteMerchant(id)
        if (success) {
            loadAll()
            loadDeleted()
            AutoBackupManager.onDataChanged()
        }
        return success
    }

    fun restore(id: Long): Boolean {
        val success = getTextilesDatabaseHelper().restoreMerchant(id)
        if (success) {
            loadAll()
            loadDeleted()
            AutoBackupManager.onDataChanged()
        }
        return success
    }

    fun permanentDelete(id: Long): Boolean {
        val success = getTextilesDatabaseHelper().permanentDeleteMerchant(id)
        if (success) {
            loadDeleted()
            AutoBackupManager.onDataChanged()
        }
        return success
    }

    fun getById(id: Long): VaangunarTharavuru? = merchants.find { it.id == id }
}
