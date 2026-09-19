package com.elvan.udukkai.data.repository

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.mode.ModeManager
import com.elvan.udukkai.data.business.getBusinessDatabaseHelper
import com.elvan.udukkai.data.model.PorulTharavuru

/**
 * Reactive repository managing products/items (Porul) for both Kooli and Pattu modes.
 */
object PorulRepository {

    var items by mutableStateOf<List<PorulTharavuru>>(emptyList())
        private set

    var deletedItems by mutableStateOf<List<PorulTharavuru>>(emptyList())
        private set

    var searchQuery by mutableStateOf("")

    val filteredItems: List<PorulTharavuru>
        get() {
            val q = searchQuery.trim().lowercase()
            if (q.isEmpty()) return items

            val mode = ModeManager.currentMode
            return items.filter { item ->
                val nameMatches = item.porulPeyar.values.any { it.lowercase().contains(q) }
                if (mode == AppMode.KOOLI) {
                    nameMatches
                } else {
                    val hsnMatches = item.hsnCode.lowercase().contains(q)
                    nameMatches || hsnMatches
                }
            }
        }

    init {
        loadAll()
    }

    fun loadAll(mode: AppMode = ModeManager.currentMode) {
        try {
            val helper = getBusinessDatabaseHelper()
            items = helper.loadAllItems(mode)
        } catch (_: Exception) {
            items = emptyList()
        }
    }

    fun save(item: PorulTharavuru, mode: AppMode = ModeManager.currentMode): Long {
        return try {
            val helper = getBusinessDatabaseHelper()
            val id = helper.saveItem(mode, item)
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
            val success = helper.deleteItem(mode, id)
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
            deletedItems = helper.loadDeletedItems(mode)
        } catch (_: Exception) {
            deletedItems = emptyList()
        }
    }

    fun restore(id: Long, mode: AppMode = ModeManager.currentMode): Boolean {
        return try {
            val helper = getBusinessDatabaseHelper()
            val success = helper.restoreItem(mode, id)
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
            val success = helper.permanentDeleteItem(mode, id)
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
            val count = helper.purgeExpiredItems(mode, days)
            if (count > 0) {
                loadDeleted(mode)
            }
            count
        } catch (_: Exception) {
            0
        }
    }

    fun getById(id: Long): PorulTharavuru? {
        return items.find { it.id == id }
    }
}
