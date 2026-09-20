package com.elvan.udukkai.core.sync

import androidx.compose.runtime.State
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.data.model.PattiyalTharavuru
import com.elvan.udukkai.data.model.PatruPattiyalInaippuTharavuru
import com.elvan.udukkai.data.model.PatrugalTharavuru
import com.elvan.udukkai.data.model.PorulTharavuru
import com.elvan.udukkai.data.model.VaangunarTharavuru

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
    fun startSync(mode: AppMode)
    fun stopSync()
    fun triggerManualSync()
    fun dismissGuardAlert()

    fun pushMerchant(merchant: VaangunarTharavuru, mode: AppMode)
    fun pushItem(item: PorulTharavuru, mode: AppMode)
    fun pushInvoice(invoice: PattiyalTharavuru, mode: AppMode)
    fun pushReceipt(receipt: PatrugalTharavuru, mode: AppMode)
    fun pushReceiptLinks(receiptId: Long, links: List<PatruPattiyalInaippuTharavuru>, mode: AppMode)
}

expect fun getFirebaseSyncManager(): FirebaseSyncManager
