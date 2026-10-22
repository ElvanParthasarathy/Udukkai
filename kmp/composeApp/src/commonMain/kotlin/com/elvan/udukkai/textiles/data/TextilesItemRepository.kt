package com.elvan.udukkai.textiles.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.elvan.udukkai.core.backup.AutoBackupManager
import com.elvan.udukkai.data.database.getTextilesDatabaseHelper
import com.elvan.udukkai.core.utils.SearchHelper
import com.elvan.udukkai.data.model.ItemModel

/**
 * Dedicated item repository for Udukkai Textiles (சவுளி).
 * Finished goods, textiles merchandise, and retail/wholesale items.
 */
object TextilesItemRepository {
    var items by mutableStateOf<List<ItemModel>>(emptyList())
        private set

    var deletedItems by mutableStateOf<List<ItemModel>>(emptyList())
        private set

    var searchQuery by mutableStateOf("")

    val filteredItems: List<ItemModel>
        get() {
            val activeId = TextilesProfileRepository.profile.id
            return items.filter { item ->
                val belongsToActiveCompany = if (item.companyIds.isEmpty()) {
                    true
                } else {
                    activeId != null && item.companyIds.contains(activeId)
                }
                belongsToActiveCompany && SearchHelper.matchesItem(item, searchQuery)
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

    fun save(item: ItemModel): Long {
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

    fun getById(id: Long): ItemModel? = items.find { it.id == id }
}
