package com.elvan.udukkai.core.sync

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.data.model.PattiyalTharavuru
import com.elvan.udukkai.data.model.PatruPattiyalInaippuTharavuru
import com.elvan.udukkai.data.model.PatrugalTharavuru
import com.elvan.udukkai.data.model.PorulTharavuru
import com.elvan.udukkai.data.model.VaangunarTharavuru

class DesktopFirebaseSyncManager : FirebaseSyncManager {
    private val _syncStatus = mutableStateOf(SyncStatus.IDLE)
    override val syncStatus: State<SyncStatus> = _syncStatus

    private val _lastSyncTime = mutableStateOf(0L)
    override val lastSyncTime: State<Long> = _lastSyncTime

    private val _guardAlertMessage = mutableStateOf<String?>(null)
    override val guardAlertMessage: State<String?> = _guardAlertMessage

    private val _isOnline = mutableStateOf(true)
    override val isOnline: State<Boolean> = _isOnline

    override fun initialize() {}
    override fun startSync(mode: AppMode) {}
    override fun stopSync() {}
    override fun triggerManualSync() {}
    override fun dismissGuardAlert() { _guardAlertMessage.value = null }

    override fun pushMerchant(merchant: VaangunarTharavuru, mode: AppMode) {}
    override fun pushItem(item: PorulTharavuru, mode: AppMode) {}
    override fun pushInvoice(invoice: PattiyalTharavuru, mode: AppMode) {}
    override fun pushReceipt(receipt: PatrugalTharavuru, mode: AppMode) {}
    override fun pushReceiptLinks(receiptId: Long, links: List<PatruPattiyalInaippuTharavuru>, mode: AppMode) {}
}

private val desktopSyncManagerInstance by lazy { DesktopFirebaseSyncManager() }

actual fun getFirebaseSyncManager(): FirebaseSyncManager = desktopSyncManagerInstance
