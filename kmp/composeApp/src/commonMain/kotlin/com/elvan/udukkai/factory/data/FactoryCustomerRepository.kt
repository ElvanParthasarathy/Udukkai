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
    var customers by mutableStateOf<List<CustomerModel>>(emptyList())
        private set

    var deletedCustomers by mutableStateOf<List<CustomerModel>>(emptyList())
        private set

    var searchQuery by mutableStateOf("")

    val filteredCustomers: List<CustomerModel>
        get() {
            val activeId = FactoryProfileRepository.profile.id
            return customers.filter { c ->
                val belongsToActiveCompany = if (c.companyIds.isEmpty()) {
                    true
                } else {
                    activeId != null && c.companyIds.contains(activeId)
                }
                belongsToActiveCompany && SearchHelper.matchesCustomer(c, searchQuery)
            }
        }

    // Backward-compatible delegates
    val merchants: List<CustomerModel> get() = customers
    val deletedMerchants: List<CustomerModel> get() = deletedCustomers
    val filteredMerchants: List<CustomerModel> get() = filteredCustomers

    init {
        loadAll()
        loadDeleted()
    }

    fun loadAll() {
        try {
            customers = getFactoryDatabaseHelper().loadAllCustomers()
        } catch (_: Exception) {
            customers = emptyList()
        }
    }

    fun purgeExpired(days: Int = 30): Int {
        val count = getFactoryDatabaseHelper().purgeExpiredCustomers(days)
        if (count > 0) loadDeleted()
        return count
    }

    fun loadDeleted() {
        try {
            deletedCustomers = getFactoryDatabaseHelper().loadDeletedCustomers()
        } catch (_: Exception) {
            deletedCustomers = emptyList()
        }
    }

    fun save(customer: CustomerModel): Long {
        val helper = getFactoryDatabaseHelper()
        val id = helper.saveCustomer(customer)
        if (id > 0) {
            loadAll()
            AutoBackupManager.onDataChanged()
        }
        return id
    }

    fun delete(id: Long): Boolean {
        val success = getFactoryDatabaseHelper().deleteCustomer(id)
        if (success) {
            loadAll()
            loadDeleted()
            AutoBackupManager.onDataChanged()
        }
        return success
    }

    fun restore(id: Long): Boolean {
        val success = getFactoryDatabaseHelper().restoreCustomer(id)
        if (success) {
            loadAll()
            loadDeleted()
            AutoBackupManager.onDataChanged()
        }
        return success
    }

    fun permanentDelete(id: Long): Boolean {
        val success = getFactoryDatabaseHelper().permanentDeleteCustomer(id)
        if (success) {
            loadDeleted()
            AutoBackupManager.onDataChanged()
        }
        return success
    }

    fun getById(id: Long): CustomerModel? = customers.find { it.id == id }
}
