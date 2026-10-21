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
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)

typealias Customer = CustomerModel
