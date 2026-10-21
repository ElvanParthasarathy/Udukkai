package com.elvan.udukkai.factory.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.elvan.udukkai.core.backup.AutoBackupManager
import com.elvan.udukkai.data.database.getFactoryDatabaseHelper
import com.elvan.udukkai.core.utils.SearchHelper
import com.elvan.udukkai.data.model.ProductModel

/**
 * Dedicated product/labor repository for Udukkai Factory (ஆலை).
 */
object FactoryProductRepository {
    val products: List<com.elvan.udukkai.data.model.ProductModel> get() = items
    val deletedProducts: List<com.elvan.udukkai.data.model.ProductModel> get() = deletedItems


    var items by mutableStateOf<List<ProductModel>>(emptyList())
        private set

    var deletedItems by mutableStateOf<List<ProductModel>>(emptyList())
        private set

    var searchQuery by mutableStateOf("")

    val filteredItems: List<ProductModel>
        get() {
            return items.filter { item ->
                SearchHelper.matchesProduct(item, searchQuery)
            }
        }

    init {
        loadAll()
        loadDeleted()
    }

    fun loadAll() {
        try {
            items = getFactoryDatabaseHelper().loadAllItems()
        } catch (_: Exception) {
            items = emptyList()
        }
    }

    fun purgeExpired(days: Int = 30): Int {
        val count = getFactoryDatabaseHelper().purgeExpiredItems(days)
        if (count > 0) loadDeleted()
        return count
    }

    fun loadDeleted() {
        try {
            deletedItems = getFactoryDatabaseHelper().loadDeletedItems()
        } catch (_: Exception) {
            deletedItems = emptyList()
        }
    }

    fun save(item: ProductModel): Long {
        val helper = getFactoryDatabaseHelper()
        val id = helper.saveItem(item)
        if (id > 0) {
            loadAll()
            AutoBackupManager.onDataChanged()
        }
        return id
    }

    fun delete(id: Long): Boolean {
        val success = getFactoryDatabaseHelper().deleteItem(id)
        if (success) {
            loadAll()
            loadDeleted()
            AutoBackupManager.onDataChanged()
        }
        return success
    }

    fun restore(id: Long): Boolean {
        val success = getFactoryDatabaseHelper().restoreItem(id)
        if (success) {
            loadAll()
            loadDeleted()
            AutoBackupManager.onDataChanged()
        }
        return success
    }

    fun permanentDelete(id: Long): Boolean {
        val success = getFactoryDatabaseHelper().permanentDeleteItem(id)
        if (success) {
            loadDeleted()
            AutoBackupManager.onDataChanged()
        }
        return success
    }

    fun getById(id: Long): ProductModel? = items.find { it.id == id }
}
