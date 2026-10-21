package com.elvan.udukkai.data.model

/**
 * Domain model for a Payment Receipt (Patru / Patrugal) in both Coolie and Silk modes.
 * Standardized English property names with backward-compatible Tanglish accessors.
 */
data class ReceiptModel(
    val id: Long = 0L,
    val companyId: Long? = null,
    val receiptNumber: String = "",
    val finYear: String = "",
    val vanakkam: Int = 1,
    val customerId: Long? = null,
    val customerName: Map<String, String> = emptyMap(),
    val customerAddress: Map<String, String> = emptyMap(),
    val receiptDate: Long = System.currentTimeMillis(),
    val amount: Double = 0.0,
    val paymentMode: String = "cash",
    val bankName: String? = null,
    val transactionNumber: String? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
) {
}
typealias Receipt = ReceiptModel
