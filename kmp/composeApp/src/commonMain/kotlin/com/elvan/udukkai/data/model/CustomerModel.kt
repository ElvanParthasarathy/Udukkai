package com.elvan.udukkai.data.model

/**
 * Domain model for a Customer / Merchant.
 * Standardized pure English model.
 */
data class CustomerModel(
    val id: Long = 0,
    val name: Map<String, String> = emptyMap(),
    val address: Map<String, String> = emptyMap(),
    val city: Map<String, String> = emptyMap(),
    val district: Map<String, String> = emptyMap(),
    val state: Map<String, String> = emptyMap(),
    val country: Map<String, String> = mapOf("en" to "India", "ta" to "இந்தியா"),
    val internationalAddress: Map<String, String> = emptyMap(),
    val pinCode: String = "",
    val gstin: String = "",
    val email: String = "",
    val phone: String = "",
    val companyIds: List<Long> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
) {
    /**
     * Determines whether this customer is available for the given company.
     * If [companyIds] is empty, the customer is shared across all companies.
     * If [companyId] is null, it is available if the customer is shared.
     */
    fun isAvailableForCompany(companyId: Long?): Boolean {
        if (companyIds.isEmpty()) return true
        if (companyId == null) return false
        return companyIds.contains(companyId)
    }
}

typealias Customer = CustomerModel
