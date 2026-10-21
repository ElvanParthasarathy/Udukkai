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
    // Backward-compatible accessors for legacy Tanglish property names
    val niruvanamId: Long? get() = companyId
    val patruEn: String get() = receiptNumber
    val vaangunarId: Long? get() = customerId
    val vaangunarPeyar: Map<String, String> get() = customerName
    val vaangunarMunvari: Map<String, String> get() = customerAddress
    val patruNaal: Long get() = receiptDate
    val thogai: Double get() = amount
    val seluthumMurai: String get() = paymentMode
    val vangiPeyar: String? get() = bankName
    val parivarthanaiEn: String? get() = transactionNumber
    val ullkurippu: String get() = notes
}

typealias PatrugalTharavuru = ReceiptModel
typealias Receipt = ReceiptModel
