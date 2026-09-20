package com.elvan.udukkai.data.model

/**
 * Domain model for a Payment Receipt (Patru / Patrugal) in both Coolie and Silk modes.
 * Maps 1:1 with the SQLite database schema and Drift table.
 */
data class PatrugalTharavuru(
    val id: Long = 0L,
    val niruvanamId: Long? = null,
    val patruEn: String = "",
    val finYear: String = "",
    val vanakkam: Int = 1,
    val vaangunarId: Long? = null,
    val vaangunarPeyar: Map<String, String> = emptyMap(),
    val vaangunarMunvari: Map<String, String> = emptyMap(),
    val patruNaal: Long = System.currentTimeMillis(),
    val thogai: Double = 0.0,
    val seluthumMurai: String = "cash",
    val vangiPeyar: String? = null,
    val parivarthanaiEn: String? = null,
    val ullkurippu: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)
