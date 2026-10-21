package com.elvan.udukkai.data.model

/**
 * Domain model representing a link between a Payment Receipt and an Invoice.
 * Standardized English property names with backward-compatible Tanglish accessors.
 */
data class ReceiptInvoiceLinkModel(
    val id: Long = 0L,
    val receiptId: Long = 0L,
    val invoiceId: Long = 0L,
    val appliedAmount: Double = 0.0
)
typealias ReceiptInvoiceLink = ReceiptInvoiceLinkModel
