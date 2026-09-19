package com.elvan.udukkai.data.model

/**
 * Domain model for an Invoice (Pattiyal) in both Coolie and Silk modes.
 * Maps 1:1 with the SQLite database schema and Drift table.
 */
data class PattiyalTharavuru(
    val id: Long = 0L,
    val niruvanamId: Long? = null,
    val patrucheettuEn: String = "",
    val finYear: Int = 0,
    val vanakkam: Int = 1,
    val pattiyalVagai: String = "tax-invoice",
    val vaangunarId: Long? = null,
    val vaangunarPeyar: Map<String, String> = emptyMap(),
    val vaangunarMunvari: Map<String, String> = emptyMap(),
    val pattiyalNaal: Long = System.currentTimeMillis(),
    val tharavugal: String = "[]",
    val mothaThogai: Double = 0.0,
    val thallupadi: Double = 0.0,
    val podhuThallupadiMathippu: Double = 0.0,
    val podhuThallupadiVagai: String = "%",
    val podhuThallupadiThogai: Double = 0.0,
    val variThogai: Double = 0.0,
    val variTharavugal: String = "{}",
    val mothaEdai: Double = 0.0,
    val setharamGrams: Double = 0.0,
    val thabaalThogai: Double = 0.0,
    val ahimsaPattuThogai: Double = 0.0,
    val piravariVugal: String = "[]",
    val sonthaViruppangal: String = "{}",
    val nibandhanaigal: String = "",
    val ullkurippu: String = "",
    val vangiTharavugal: String = "{}",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)
