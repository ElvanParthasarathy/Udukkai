package com.elvan.udukkai.core.sync

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.model.ReceiptInvoiceLinkModel
import com.elvan.udukkai.data.model.ReceiptModel
import com.elvan.udukkai.data.model.ItemModel
import com.elvan.udukkai.data.model.CustomerModel

class DesktopFirebaseSyncManager : FirebaseSyncManager {
    private val _syncStatus = mutableStateOf(SyncStatus.IDLE)
    override val syncStatus: State<SyncStatus> = _syncStatus

    private val _lastSyncTime = mutableStateOf(0L)
    override val lastSyncTime: State<Long> = _lastSyncTime

    private val _guardAlertMessage = mutableStateOf<String?>(null)
    override val guardAlertMessage: State<String?> = _guardAlertMessage

    private val _isOnline = mutableStateOf(true)
    override val isOnline: State<Boolean> = _isOnline

    private val _restoreProgress = mutableStateOf<RestoreProgress?>(null)
    override val restoreProgress: State<RestoreProgress?> = _restoreProgress

    private val _profilesRestored = mutableStateOf(true)
    override val profilesRestored: State<Boolean> = _profilesRestored

    override fun initialize() {}
    override fun startSync(scope: String) {}
    override fun stopSync() {}
    override fun triggerManualSync() {}
    override fun dismissGuardAlert() { _guardAlertMessage.value = null }

    override fun pushProfile(profile: com.elvan.udukkai.data.settings.BusinessProfile, scope: String) {}
    override fun wipeScope(scope: String) {}
    override fun pushRemove(collection: String, id: Long, scope: String) {}
    override fun pushMerchant(merchant: CustomerModel, scope: String) {}
    override fun pushItem(item: ItemModel, scope: String) {}
    override fun pushInvoice(invoice: InvoiceModel, scope: String) {}
    override fun pushReceipt(receipt: ReceiptModel, scope: String) {}
    override fun pushReceiptLinks(receiptId: Long, links: List<ReceiptInvoiceLinkModel>, scope: String) {}
}

private val desktopSyncManagerInstance by lazy { DesktopFirebaseSyncManager() }

actual fun getFirebaseSyncManager(): FirebaseSyncManager = desktopSyncManagerInstance
