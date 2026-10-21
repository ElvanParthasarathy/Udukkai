package com.elvan.udukkai.factory.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.elvan.udukkai.core.backup.AutoBackupManager
import com.elvan.udukkai.data.database.getFactoryDatabaseHelper
import com.elvan.udukkai.core.utils.SearchHelper
import com.elvan.udukkai.data.model.CustomerModel

/**
 * Dedicated customer repository for Udukkai Factory (ஆலை).
 * Distinct customer fields: Name, City, Address, Phone.
 */
object FactoryCustomerRepository {
    val customers: List<com.elvan.udukkai.data.model.CustomerModel> get() = merchants
    val deletedCustomers: List<com.elvan.udukkai.data.model.CustomerModel> get() = deletedMerchants


    var merchants by mutableStateOf<List<CustomerModel>>(emptyList())
        private set

    var deletedMerchants by mutableStateOf<List<CustomerModel>>(emptyList())
        private set

    var searchQuery by mutableStateOf("")

    val filteredMerchants: List<CustomerModel>
        get() {
            return merchants.filter { m ->
                SearchHelper.matchesCustomer(m, searchQuery)
            }
        }

    init {
        loadAll()
        loadDeleted()
    }

    fun loadAll() {
        try {
            merchants = getFactoryDatabaseHelper().loadAllMerchants()
        } catch (_: Exception) {
            merchants = emptyList()
        }
    }

    fun purgeExpired(days: Int = 30): Int {
        val count = getFactoryDatabaseHelper().purgeExpiredMerchants(days)
        if (count > 0) loadDeleted()
        return count
    }

    fun loadDeleted() {
        try {
            deletedMerchants = getFactoryDatabaseHelper().loadDeletedMerchants()
        } catch (_: Exception) {
            deletedMerchants = emptyList()
        }
    }

    fun save(merchant: CustomerModel): Long {
        val helper = getFactoryDatabaseHelper()
        val id = helper.saveMerchant(merchant)
        if (id > 0) {
            loadAll()
            AutoBackupManager.onDataChanged()
        }
        return id
    }

    fun delete(id: Long): Boolean {
        val success = getFactoryDatabaseHelper().deleteMerchant(id)
        if (success) {
            loadAll()
            loadDeleted()
            AutoBackupManager.onDataChanged()
        }
        return success
    }

    fun restore(id: Long): Boolean {
        val success = getFactoryDatabaseHelper().restoreMerchant(id)
        if (success) {
            loadAll()
            loadDeleted()
            AutoBackupManager.onDataChanged()
        }
        return success
    }

    fun permanentDelete(id: Long): Boolean {
        val success = getFactoryDatabaseHelper().permanentDeleteMerchant(id)
        if (success) {
            loadDeleted()
            AutoBackupManager.onDataChanged()
        }
        return success
    }

    fun getById(id: Long): CustomerModel? = merchants.find { it.id == id }
}
