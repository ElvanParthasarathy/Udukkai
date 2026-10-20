package com.elvan.udukkai.textiles.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.elvan.udukkai.core.backup.AutoBackupManager
import com.elvan.udukkai.data.database.getTextilesDatabaseHelper
import com.elvan.udukkai.data.model.PorulTharavuru

/**
 * Dedicated product repository for Udukkai Textiles (சவுளி).
 * Finished goods, textiles merchandise, and retail/wholesale items.
 */
object TextilesProductRepository {
    val products: List<com.elvan.udukkai.data.model.PorulTharavuru> get() = items
    val deletedProducts: List<com.elvan.udukkai.data.model.PorulTharavuru> get() = deletedItems


    var items by mutableStateOf<List<PorulTharavuru>>(emptyList())
        private set

    var deletedItems by mutableStateOf<List<PorulTharavuru>>(emptyList())
        private set

    var searchQuery by mutableStateOf("")

    val filteredItems: List<PorulTharavuru>
        get() {
            val q = searchQuery.trim().lowercase()
            if (q.isEmpty()) return items

            return items.filter { item ->
                val nameMatches = item.porulPeyar.values.any { it.lowercase().contains(q) }
                val hsnMatches = item.hsnCode.lowercase().contains(q)
                nameMatches || hsnMatches
            }
        }

    init {
        loadAll()
        loadDeleted()
    }

    fun loadAll() {
        try {
            items = getTextilesDatabaseHelper().loadAllItems()
        } catch (_: Exception) {
            items = emptyList()
        }
    }

    fun purgeExpired(days: Int = 30): Int {
        val count = getTextilesDatabaseHelper().purgeExpiredItems(days)
        if (count > 0) loadDeleted()
        return count
    }

    fun loadDeleted() {
        try {
            deletedItems = getTextilesDatabaseHelper().loadDeletedItems()
        } catch (_: Exception) {
            deletedItems = emptyList()
        }
    }

    fun save(item: PorulTharavuru): Long {
        val helper = getTextilesDatabaseHelper()
        val id = helper.saveItem(item)
        if (id > 0) {
            loadAll()
            AutoBackupManager.onDataChanged()
        }
        return id
    }

    fun delete(id: Long): Boolean {
        val success = getTextilesDatabaseHelper().deleteItem(id)
        if (success) {
            loadAll()
            loadDeleted()
            AutoBackupManager.onDataChanged()
        }
        return success
    }

    fun restore(id: Long): Boolean {
        val success = getTextilesDatabaseHelper().restoreItem(id)
        if (success) {
            loadAll()
            loadDeleted()
            AutoBackupManager.onDataChanged()
        }
        return success
    }

    fun permanentDelete(id: Long): Boolean {
        val success = getTextilesDatabaseHelper().permanentDeleteItem(id)
        if (success) {
            loadDeleted()
            AutoBackupManager.onDataChanged()
        }
        return success
    }

    fun getById(id: Long): PorulTharavuru? = items.find { it.id == id }
}
