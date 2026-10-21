package com.elvan.udukkai.textiles.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.elvan.udukkai.core.backup.AutoBackupManager
import com.elvan.udukkai.data.database.getTextilesDatabaseHelper
import com.elvan.udukkai.core.utils.SearchHelper
import com.elvan.udukkai.data.model.CustomerModel

/**
 * Dedicated customer repository for Udukkai Textiles (சவுளி).
 * Distinct customer fields: Name, GSTIN, Phone, Email, PIN, State, District, City, Address.
 */
object TextilesCustomerRepository {
    var customers by mutableStateOf<List<CustomerModel>>(emptyList())
        private set

    var deletedCustomers by mutableStateOf<List<CustomerModel>>(emptyList())
        private set

    var searchQuery by mutableStateOf("")

    val filteredCustomers: List<CustomerModel>
        get() {
            return customers.filter { c ->
                SearchHelper.matchesCustomer(c, searchQuery)
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
            customers = getTextilesDatabaseHelper().loadAllCustomers()
        } catch (_: Exception) {
            customers = emptyList()
        }
    }

    fun purgeExpired(days: Int = 30): Int {
        val count = getTextilesDatabaseHelper().purgeExpiredCustomers(days)
        if (count > 0) loadDeleted()
        return count
    }

    fun loadDeleted() {
        try {
            deletedCustomers = getTextilesDatabaseHelper().loadDeletedCustomers()
        } catch (_: Exception) {
            deletedCustomers = emptyList()
        }
    }

    fun save(customer: CustomerModel): Long {
        val helper = getTextilesDatabaseHelper()
        val id = helper.saveCustomer(customer)
        if (id > 0) {
            loadAll()
            AutoBackupManager.onDataChanged()
        }
        return id
    }

    fun delete(id: Long): Boolean {
        val success = getTextilesDatabaseHelper().deleteCustomer(id)
        if (success) {
            loadAll()
            loadDeleted()
            AutoBackupManager.onDataChanged()
        }
        return success
    }

    fun restore(id: Long): Boolean {
        val success = getTextilesDatabaseHelper().restoreCustomer(id)
        if (success) {
            loadAll()
            loadDeleted()
            AutoBackupManager.onDataChanged()
        }
        return success
    }

    fun permanentDelete(id: Long): Boolean {
        val success = getTextilesDatabaseHelper().permanentDeleteCustomer(id)
        if (success) {
            loadDeleted()
            AutoBackupManager.onDataChanged()
        }
        return success
    }

    fun getById(id: Long): CustomerModel? = customers.find { it.id == id }
}
