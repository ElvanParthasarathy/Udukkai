package com.elvan.udukkai.activities

import com.elvan.udukkai.data.model.CustomerModel
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.model.ItemModel
import com.elvan.udukkai.data.model.ReceiptModel

/**
 * Thread-safe in-memory bridge for passing non-primitive or newly drafted models
 * across Android Activity boundaries within the same application process.
 */
object PlatformNavigationState {
    var pendingInvoice: InvoiceModel? = null
    var pendingReceipt: ReceiptModel? = null
    var pendingItem: ItemModel? = null
    var pendingCustomer: CustomerModel? = null

    fun consumePendingInvoice(): InvoiceModel? = pendingInvoice.also { pendingInvoice = null }
    fun consumePendingReceipt(): ReceiptModel? = pendingReceipt.also { pendingReceipt = null }
    fun consumePendingItem(): ItemModel? = pendingItem.also { pendingItem = null }
    fun consumePendingCustomer(): CustomerModel? = pendingCustomer.also { pendingCustomer = null }
}
