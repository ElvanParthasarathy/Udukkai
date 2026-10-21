package com.elvan.udukkai.core.sync

import androidx.compose.runtime.State
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.model.ReceiptInvoiceLinkModel
import com.elvan.udukkai.data.model.ReceiptModel
import com.elvan.udukkai.data.model.ProductModel
import com.elvan.udukkai.data.model.CustomerModel

enum class SyncStatus {
    IDLE,
    SYNCING,
    SYNCED,
    OFFLINE,
    ERROR,
    GUARD_TRIGGERED
}

interface FirebaseSyncManager {
    val syncStatus: State<SyncStatus>
    val lastSyncTime: State<Long>
    val guardAlertMessage: State<String?>
    val isOnline: State<Boolean>

    fun initialize()
    fun startSync(scope: String = "factory")
    fun stopSync()
    fun triggerManualSync()
    fun dismissGuardAlert()

    fun pushMerchant(merchant: CustomerModel, scope: String = "factory")
    fun pushItem(item: ProductModel, scope: String = "factory")
    fun pushInvoice(invoice: InvoiceModel, scope: String = "factory")
    fun pushReceipt(receipt: ReceiptModel, scope: String = "factory")
    fun pushReceiptLinks(receiptId: Long, links: List<ReceiptInvoiceLinkModel>, scope: String = "factory")
}

expect fun getFirebaseSyncManager(): FirebaseSyncManager
