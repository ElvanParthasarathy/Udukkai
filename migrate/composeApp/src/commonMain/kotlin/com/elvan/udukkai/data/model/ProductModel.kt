package com.elvan.udukkai.data.model

data class PorulTharavuru(
    val id: Long = 0,
    val porulPeyar: Map<String, String> = emptyMap(),
    val hsnCode: String = "",
    val vilai: Double = 0.0,
    val variVeetham: Double = 0.0,
    val alavuVagai: String = "quantity",
    val alagu: String = "Nos",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)
