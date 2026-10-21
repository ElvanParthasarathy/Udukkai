package com.elvan.udukkai.data.model

/**
 * Domain model for a Customer / Merchant (வாங்குநர்).
 * Standardized English property names with backward-compatible Tanglish accessors.
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
) {
    // Backward-compatible accessors for legacy Tanglish property names
    val peyar: Map<String, String> get() = name
    val mugavari: Map<String, String> get() = address
    val oor: Map<String, String> get() = city
    val maavattam: Map<String, String> get() = district
    val maanilam: Map<String, String> get() = state
    val naadu: Map<String, String> get() = country
    val velinaadMugavari: Map<String, String> get() = internationalAddress
    val anjalKuriyeedu: String get() = pinCode
    val minnanjal: String get() = email
    val tholaipaesi: String get() = phone
}

typealias VaangunarTharavuru = CustomerModel
typealias Customer = CustomerModel
