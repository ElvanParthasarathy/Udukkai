package com.elvan.udukkai.data.model

data class VaangunarTharavuru(
    val id: Long = 0,
    val peyar: Map<String, String> = emptyMap(),
    val mugavari: Map<String, String> = emptyMap(),
    val oor: Map<String, String> = emptyMap(),
    val maavattam: Map<String, String> = emptyMap(),
    val maanilam: Map<String, String> = emptyMap(),
    val naadu: Map<String, String> = mapOf("en" to "India", "ta" to "இந்தியா"),
    val velinaadMugavari: Map<String, String> = emptyMap(),
    val anjalKuriyeedu: String = "",
    val gstin: String = "",
    val minnanjal: String = "",
    val tholaipaesi: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)
