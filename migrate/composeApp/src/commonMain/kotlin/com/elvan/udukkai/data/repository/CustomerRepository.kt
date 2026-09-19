package com.elvan.udukkai.data.repository

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.mode.ModeManager
import com.elvan.udukkai.data.business.getBusinessDatabaseHelper
import com.elvan.udukkai.data.model.VaangunarTharavuru

/**
 * Reactive repository managing merchants/customers (Vaangunar) for both Kooli and Pattu modes.
 */
object VaangunarRepository {

    var merchants by mutableStateOf<List<VaangunarTharavuru>>(emptyList())
        private set

    var deletedMerchants by mutableStateOf<List<VaangunarTharavuru>>(emptyList())
        private set

    var searchQuery by mutableStateOf("")

    val filteredMerchants: List<VaangunarTharavuru>
        get() {
            val q = searchQuery.trim().lowercase()
            if (q.isEmpty()) return merchants

            val mode = ModeManager.currentMode
            return merchants.filter { m ->
                val nameMatches = m.peyar.values.any { it.lowercase().contains(q) }
                val cityMatches = m.oor.values.any { it.lowercase().contains(q) }
                val addressMatches = m.mugavari.values.any { it.lowercase().contains(q) }

                if (mode == AppMode.KOOLI) {
                    nameMatches || cityMatches || addressMatches
                } else {
                    val gstinMatches = m.gstin.lowercase().contains(q)
                    val phoneMatches = m.tholaipaesi.lowercase().contains(q)
                    val emailMatches = m.minnanjal.lowercase().contains(q)
                    val pinMatches = m.anjalKuriyeedu.lowercase().contains(q)
                    val stateMatches = m.maanilam.values.any { it.lowercase().contains(q) }
                    val districtMatches = m.maavattam.values.any { it.lowercase().contains(q) }
                    nameMatches || cityMatches || addressMatches || gstinMatches || phoneMatches || emailMatches || pinMatches || stateMatches || districtMatches
                }
            }
        }

    init {
        loadAll()
    }

    fun loadAll(mode: AppMode = ModeManager.currentMode) {
        try {
            val helper = getBusinessDatabaseHelper()
            merchants = helper.loadAllMerchants(mode)
        } catch (_: Exception) {
            merchants = emptyList()
        }
    }

    fun save(merchant: VaangunarTharavuru, mode: AppMode = ModeManager.currentMode): Long {
        return try {
            val helper = getBusinessDatabaseHelper()
            val id = helper.saveMerchant(mode, merchant)
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
            val success = helper.deleteMerchant(mode, id)
            if (success) {
                loadAll(mode)
                loadDeleted(mode)
            }
            success
        } catch (_: Exception) {
            false
        }
    }

    fun loadDeleted(mode: AppMode = ModeManager.currentMode) {
        try {
            val helper = getBusinessDatabaseHelper()
            deletedMerchants = helper.loadDeletedMerchants(mode)
        } catch (_: Exception) {
            deletedMerchants = emptyList()
        }
    }

    fun restore(id: Long, mode: AppMode = ModeManager.currentMode): Boolean {
        return try {
            val helper = getBusinessDatabaseHelper()
            val success = helper.restoreMerchant(mode, id)
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
            val success = helper.permanentDeleteMerchant(mode, id)
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
            val count = helper.purgeExpiredMerchants(mode, days)
            if (count > 0) {
                loadDeleted(mode)
            }
            count
        } catch (_: Exception) {
            0
        }
    }

    fun getById(id: Long): VaangunarTharavuru? {
        return merchants.find { it.id == id }
    }
}
