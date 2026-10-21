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
}
typealias Invoice = InvoiceModel
