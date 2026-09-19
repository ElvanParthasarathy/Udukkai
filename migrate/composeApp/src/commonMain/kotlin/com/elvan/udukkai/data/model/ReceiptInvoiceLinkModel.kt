package com.elvan.udukkai.data.model

/**
 * Domain model representing a link between a Payment Receipt and an Invoice.
 * Maps to `pattu_patru_pattiyal_table` / `kooli_patru_pattiyal_table`.
 */
data class PatruPattiyalInaippuTharavuru(
    val id: Long = 0L,
    val patruId: Long = 0L,
    val pattiyalId: Long = 0L,
    val poruthiyaThogai: Double = 0.0
)
