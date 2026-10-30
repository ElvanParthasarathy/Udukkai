package com.elvan.udukkai.core.sync

import androidx.compose.runtime.State
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.model.ReceiptInvoiceLinkModel
import com.elvan.udukkai.data.model.ReceiptModel
import com.elvan.udukkai.data.model.ItemModel
import com.elvan.udukkai.data.model.CustomerModel

enum class SyncStatus {
    IDLE,
    SYNCING,
    SYNCED,
    OFFLINE,
    ERROR,
    GUARD_TRIGGERED
}

enum class RestorePhase { PROFILES, CUSTOMERS, ITEMS, INVOICES, RECEIPTS }

/** Where the background restore from the cloud is: [done] of [total] records of one kind. */
data class RestoreProgress(val phase: RestorePhase, val done: Int, val total: Int)

interface FirebaseSyncManager {
    val syncStatus: State<SyncStatus>
    val lastSyncTime: State<Long>
    val guardAlertMessage: State<String?>
    val isOnline: State<Boolean>

    /** Null while nothing is being restored. */
    val restoreProgress: State<RestoreProgress?>

    /** False from the start of a sign-in sync until the business profiles have arrived (or a short timeout). */
    val profilesRestored: State<Boolean>

    fun initialize()
    fun startSync(scope: String = "factory")
    fun stopSync()
    fun triggerManualSync()
    fun dismissGuardAlert()

    fun pushMerchant(merchant: CustomerModel, scope: String = "factory")
    fun pushItem(item: ItemModel, scope: String = "factory")
    fun pushInvoice(invoice: InvoiceModel, scope: String = "factory")
    fun pushReceipt(receipt: ReceiptModel, scope: String = "factory")
    fun pushReceiptLinks(receiptId: Long, links: List<ReceiptInvoiceLinkModel>, scope: String = "factory")
    fun pushProfile(profile: com.elvan.udukkai.data.settings.BusinessProfile, scope: String = "factory")

    /** Removes one record from the cloud (a permanent delete); collection is the database node name. */
    fun pushRemove(collection: String, id: Long, scope: String = "factory")

    /** Wipes all cloud data for the given scope (used by dev clear / erase). */
    fun wipeScope(scope: String = "factory")
}

expect fun getFirebaseSyncManager(): FirebaseSyncManager
