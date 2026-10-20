package com.elvan.udukkai.core.sync

import androidx.compose.runtime.State
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
    fun startSync(scope: String = "factory")
    fun stopSync()
    fun triggerManualSync()
    fun dismissGuardAlert()

    fun pushMerchant(merchant: VaangunarTharavuru, scope: String = "factory")
    fun pushItem(item: PorulTharavuru, scope: String = "factory")
    fun pushInvoice(invoice: PattiyalTharavuru, scope: String = "factory")
    fun pushReceipt(receipt: PatrugalTharavuru, scope: String = "factory")
    fun pushReceiptLinks(receiptId: Long, links: List<PatruPattiyalInaippuTharavuru>, scope: String = "factory")
}

expect fun getFirebaseSyncManager(): FirebaseSyncManager
