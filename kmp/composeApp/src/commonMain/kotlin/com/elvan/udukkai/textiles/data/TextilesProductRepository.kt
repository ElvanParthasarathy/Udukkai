package com.elvan.udukkai.textiles.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.elvan.udukkai.core.backup.AutoBackupManager
import com.elvan.udukkai.data.database.getTextilesDatabaseHelper
import com.elvan.udukkai.core.utils.SearchHelper
import com.elvan.udukkai.data.model.ProductModel

/**
 * Dedicated product repository for Udukkai Textiles (சவுளி).
 * Finished goods, textiles merchandise, and retail/wholesale items.
 */
object TextilesProductRepository {
    var products by mutableStateOf<List<ProductModel>>(emptyList())
        private set

    var deletedProducts by mutableStateOf<List<ProductModel>>(emptyList())
        private set

    var searchQuery by mutableStateOf("")

    val filteredProducts: List<ProductModel>
        get() {
            val activeId = TextilesProfileRepository.profile.id
            return products.filter { product ->
                val belongsToActiveCompany = if (product.companyIds.isEmpty()) {
                    true
                } else {
                    activeId != null && product.companyIds.contains(activeId)
                }
                belongsToActiveCompany && SearchHelper.matchesProduct(product, searchQuery)
            }
        }

    // Backward-compatible delegates
    val items: List<ProductModel> get() = products
    val deletedItems: List<ProductModel> get() = deletedProducts
    val filteredItems: List<ProductModel> get() = filteredProducts

    init {
        loadAll()
        loadDeleted()
    }

    fun loadAll() {
        try {
            products = getTextilesDatabaseHelper().loadAllProducts()
        } catch (_: Exception) {
            products = emptyList()
        }
    }

    fun purgeExpired(days: Int = 30): Int {
        val count = getTextilesDatabaseHelper().purgeExpiredProducts(days)
        if (count > 0) loadDeleted()
        return count
    }

    fun loadDeleted() {
        try {
            deletedProducts = getTextilesDatabaseHelper().loadDeletedProducts()
        } catch (_: Exception) {
            deletedProducts = emptyList()
        }
    }

    fun save(product: ProductModel): Long {
        val helper = getTextilesDatabaseHelper()
        val id = helper.saveProduct(product)
        if (id > 0) {
            loadAll()
            AutoBackupManager.onDataChanged()
        }
        return id
    }

    fun delete(id: Long): Boolean {
        val success = getTextilesDatabaseHelper().deleteProduct(id)
        if (success) {
            loadAll()
            loadDeleted()
            AutoBackupManager.onDataChanged()
        }
        return success
    }

    fun restore(id: Long): Boolean {
        val success = getTextilesDatabaseHelper().restoreProduct(id)
        if (success) {
            loadAll()
            loadDeleted()
            AutoBackupManager.onDataChanged()
        }
        return success
    }

    fun permanentDelete(id: Long): Boolean {
        val success = getTextilesDatabaseHelper().permanentDeleteProduct(id)
        if (success) {
            loadDeleted()
            AutoBackupManager.onDataChanged()
        }
        return success
    }

    fun getById(id: Long): ProductModel? = products.find { it.id == id }
}
