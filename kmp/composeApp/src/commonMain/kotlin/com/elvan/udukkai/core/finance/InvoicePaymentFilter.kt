package com.elvan.udukkai.core.finance

enum class InvoicePaymentFilter {
    All, Paid, Unpaid;

    fun matches(isInvoice: Boolean, isPaid: () -> Boolean): Boolean = when (this) {
        All -> true
        Paid -> isInvoice && isPaid()
        Unpaid -> isInvoice && !isPaid()
    }
}
