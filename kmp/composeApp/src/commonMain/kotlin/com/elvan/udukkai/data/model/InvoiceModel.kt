package com.elvan.udukkai.data.model

/**
 * Domain model for an Invoice (Pattiyal) in both Coolie and Silk modes.
 * Standardized English property names with backward-compatible Tanglish accessors.
 */
data class InvoiceModel(
    val id: Long = 0L,
    val companyId: Long? = null,
    val invoiceNumber: String = "",
    val finYear: Int = 0,
    val vanakkam: Int = 1,
    val invoiceType: String = "tax-invoice",
    val customerId: Long? = null,
    val customerName: Map<String, String> = emptyMap(),
    val customerAddress: Map<String, String> = emptyMap(),
    val invoiceDate: Long = System.currentTimeMillis(),
    val itemsJson: String = "[]",
    val totalAmount: Double = 0.0,
    val discount: Double = 0.0,
    val globalDiscountValue: Double = 0.0,
    val globalDiscountType: String = "%",
    val globalDiscountAmount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val taxDataJson: String = "{}",
    val totalWeight: Double = 0.0,
    val wastageGrams: Double = 0.0,
    val postalCharges: Double = 0.0,
    val ahimsaSilkAmount: Double = 0.0,
    val otherTaxesJson: String = "[]",
    val customPreferencesJson: String = "{}",
    val termsAndConditions: String = "",
    val notes: String = "",
    val bankDetailsJson: String = "{}",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
) {
    // Backward-compatible accessors for legacy Tanglish property names
    val niruvanamId: Long? get() = companyId
    val patrucheettuEn: String get() = invoiceNumber
    val pattiyalVagai: String get() = invoiceType
    val vaangunarId: Long? get() = customerId
    val vaangunarPeyar: Map<String, String> get() = customerName
    val vaangunarMunvari: Map<String, String> get() = customerAddress
    val pattiyalNaal: Long get() = invoiceDate
    val tharavugal: String get() = itemsJson
    val mothaThogai: Double get() = totalAmount
    val thallupadi: Double get() = discount
    val podhuThallupadiMathippu: Double get() = globalDiscountValue
    val podhuThallupadiVagai: String get() = globalDiscountType
    val podhuThallupadiThogai: Double get() = globalDiscountAmount
    val variThogai: Double get() = taxAmount
    val variTharavugal: String get() = taxDataJson
    val mothaEdai: Double get() = totalWeight
    val setharamGrams: Double get() = wastageGrams
    val thabaalThogai: Double get() = postalCharges
    val ahimsaPattuThogai: Double get() = ahimsaSilkAmount
    val piravariVugal: String get() = otherTaxesJson
    val sonthaViruppangal: String get() = customPreferencesJson
    val nibandhanaigal: String get() = termsAndConditions
    val ullkurippu: String get() = notes
    val vangiTharavugal: String get() = bankDetailsJson
}

typealias PattiyalTharavuru = InvoiceModel
typealias Invoice = InvoiceModel
