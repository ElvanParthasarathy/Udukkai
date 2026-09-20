package com.elvan.udukkai.core.sync

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.elvan.udukkai.core.backup.getNirilBackupService
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.mode.ModeManager
import com.elvan.udukkai.core.platform.AppContext
import com.elvan.udukkai.data.business.getBusinessDatabaseHelper
import com.elvan.udukkai.data.model.PattiyalTharavuru
import com.elvan.udukkai.data.model.PatruPattiyalInaippuTharavuru
import com.elvan.udukkai.data.model.PatrugalTharavuru
import com.elvan.udukkai.data.model.PorulTharavuru
import com.elvan.udukkai.data.model.VaangunarTharavuru
import com.elvan.udukkai.data.repository.PattiyalRepository
import com.elvan.udukkai.data.repository.PatrugalRepository
import com.elvan.udukkai.data.repository.PorulRepository
import com.elvan.udukkai.data.repository.VaangunarRepository
import com.elvan.udukkai.data.settings.MozhiJsonConverter
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AndroidFirebaseSyncManager : FirebaseSyncManager {

    private val tag = "RTDBSync"

    private val _syncStatus = mutableStateOf(SyncStatus.IDLE)
    override val syncStatus: State<SyncStatus> = _syncStatus

    private val _lastSyncTime = mutableStateOf(0L)
    override val lastSyncTime: State<Long> = _lastSyncTime

    private val _guardAlertMessage = mutableStateOf<String?>(null)
    override val guardAlertMessage: State<String?> = _guardAlertMessage

    private val _isOnline = mutableStateOf(true)
    override val isOnline: State<Boolean> = _isOnline

    private var databaseInstance: FirebaseDatabase? = null
    private var isPersistenceSet = false
    private val activeListeners = mutableMapOf<DatabaseReference, ValueEventListener>()
    private var isApplyingRemoteChange = false

    private fun getDatabase(): FirebaseDatabase? {
        if (databaseInstance != null) return databaseInstance
        return try {
            if (!AppContext.isInitialized) {
                Log.w(tag, "AppContext is not initialized yet!")
                return null
            }
            if (FirebaseApp.getApps(AppContext.context).isEmpty()) {
                FirebaseApp.initializeApp(AppContext.context)
                Log.i(tag, "FirebaseApp initialized.")
            }

            val db = FirebaseDatabase.getInstance()
            if (!isPersistenceSet) {
                try {
                    db.setPersistenceEnabled(true)
                    isPersistenceSet = true
                    Log.i(tag, "Firebase Realtime Database offline persistence enabled.")
                } catch (e: Exception) {
                    Log.w(tag, "Persistence might already be enabled: ${e.message}")
                }
            }
            databaseInstance = db
            db
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize Firebase Realtime Database: ${e.message}", e)
            null
        }
    }

    override fun initialize() {
        getDatabase()
    }

    private fun getPrefix(mode: AppMode): String = if (mode == AppMode.KOOLI) "kooli" else "pattu"

    override fun startSync(mode: AppMode) {
        stopSync()
        val db = getDatabase() ?: return

        // Take automatic Copy 2 Safety Snapshot before sync session
        try {
            getNirilBackupService().createSafetySnapshot()
            Log.i(tag, "Copy 2 Safety Snapshot created prior to RTDB sync session.")
        } catch (e: Exception) {
            Log.w(tag, "Pre-sync safety snapshot warning: ${e.message}")
        }

        _syncStatus.value = SyncStatus.SYNCING
        val prefix = getPrefix(mode)
        val rootRef = db.getReference("udukkai").child(prefix)

        // 1. Merchants Listener
        val merchantRef = rootRef.child("merchants")
        merchantRef.keepSynced(true)
        val merchantListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                handleMerchantsSnapshot(mode, snapshot)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(tag, "Merchant sync cancelled: ${error.message}")
                _syncStatus.value = SyncStatus.ERROR
            }
        }
        merchantRef.addValueEventListener(merchantListener)
        activeListeners[merchantRef] = merchantListener

        // 2. Products Listener
        val productRef = rootRef.child("products")
        productRef.keepSynced(true)
        val productListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                handleProductsSnapshot(mode, snapshot)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(tag, "Product sync cancelled: ${error.message}")
            }
        }
        productRef.addValueEventListener(productListener)
        activeListeners[productRef] = productListener

        // 3. Invoices Listener
        val invoiceRef = rootRef.child("invoices")
        invoiceRef.keepSynced(true)
        val invoiceListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                handleInvoicesSnapshot(mode, snapshot)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(tag, "Invoice sync cancelled: ${error.message}")
            }
        }
        invoiceRef.addValueEventListener(invoiceListener)
        activeListeners[invoiceRef] = invoiceListener

        // 4. Receipts Listener
        val receiptRef = rootRef.child("receipts")
        receiptRef.keepSynced(true)
        val receiptListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                handleReceiptsSnapshot(mode, snapshot)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(tag, "Receipt sync cancelled: ${error.message}")
            }
        }
        receiptRef.addValueEventListener(receiptListener)
        activeListeners[receiptRef] = receiptListener

        _syncStatus.value = SyncStatus.SYNCED
        _lastSyncTime.value = System.currentTimeMillis()
        Log.i(tag, "RTDB fast multi-device sync active for $prefix mode")
    }

    override fun stopSync() {
        activeListeners.forEach { (ref, listener) ->
            ref.removeEventListener(listener)
        }
        activeListeners.clear()
        _syncStatus.value = SyncStatus.IDLE
    }

    override fun triggerManualSync() {
        startSync(ModeManager.currentMode)
    }

    override fun dismissGuardAlert() {
        _guardAlertMessage.value = null
        if (_syncStatus.value == SyncStatus.GUARD_TRIGGERED) {
            _syncStatus.value = SyncStatus.SYNCED
        }
    }

    // --- Anti-Erasure Guard & RTDB Inbound Snapshot Processors ---

    private fun handleMerchantsSnapshot(mode: AppMode, snapshot: DataSnapshot) {
        val helper = getBusinessDatabaseHelper()
        val localList = helper.loadAllMerchants(mode)

        if (localList.isNotEmpty() && (!snapshot.exists() || snapshot.childrenCount == 0L)) {
            triggerAntiErasureGuard("merchants", localList.size)
            return
        }

        var anyChanged = false
        isApplyingRemoteChange = true
        try {
            val localMap = localList.associateBy { it.id }
            for (child in snapshot.children) {
                val merchant = snapshotToMerchant(child) ?: continue
                val existing = localMap[merchant.id]
                val remoteUpdated = merchant.updatedAt
                val localUpdated = existing?.updatedAt ?: 0L

                if (existing == null || remoteUpdated > localUpdated) {
                    helper.saveMerchant(mode, merchant)
                    anyChanged = true
                }
            }
        } finally {
            isApplyingRemoteChange = false
        }

        if (anyChanged) {
            _lastSyncTime.value = System.currentTimeMillis()
            CoroutineScope(Dispatchers.Main).launch {
                VaangunarRepository.loadAll(mode)
                VaangunarRepository.loadDeleted(mode)
            }
        }
    }

    private fun handleProductsSnapshot(mode: AppMode, snapshot: DataSnapshot) {
        val helper = getBusinessDatabaseHelper()
        val localList = helper.loadAllItems(mode)

        if (localList.isNotEmpty() && (!snapshot.exists() || snapshot.childrenCount == 0L)) {
            triggerAntiErasureGuard("products", localList.size)
            return
        }

        var anyChanged = false
        isApplyingRemoteChange = true
        try {
            val localMap = localList.associateBy { it.id }
            for (child in snapshot.children) {
                val item = snapshotToItem(child) ?: continue
                val existing = localMap[item.id]
                val remoteUpdated = item.updatedAt
                val localUpdated = existing?.updatedAt ?: 0L

                if (existing == null || remoteUpdated > localUpdated) {
                    helper.saveItem(mode, item)
                    anyChanged = true
                }
            }
        } finally {
            isApplyingRemoteChange = false
        }

        if (anyChanged) {
            _lastSyncTime.value = System.currentTimeMillis()
            CoroutineScope(Dispatchers.Main).launch {
                PorulRepository.loadAll(mode)
                PorulRepository.loadDeleted(mode)
            }
        }
    }

    private fun handleInvoicesSnapshot(mode: AppMode, snapshot: DataSnapshot) {
        val helper = getBusinessDatabaseHelper()
        val localList = helper.loadAllInvoices(mode)

        if (localList.isNotEmpty() && (!snapshot.exists() || snapshot.childrenCount == 0L)) {
            triggerAntiErasureGuard("invoices", localList.size)
            return
        }

        var anyChanged = false
        isApplyingRemoteChange = true
        try {
            val localMap = localList.associateBy { it.id }
            for (child in snapshot.children) {
                val invoice = snapshotToInvoice(child) ?: continue
                val existing = localMap[invoice.id]
                val remoteUpdated = invoice.updatedAt
                val localUpdated = existing?.updatedAt ?: 0L

                if (existing == null || remoteUpdated > localUpdated) {
                    helper.saveInvoice(mode, invoice)
                    anyChanged = true
                }
            }
        } finally {
            isApplyingRemoteChange = false
        }

        if (anyChanged) {
            _lastSyncTime.value = System.currentTimeMillis()
            CoroutineScope(Dispatchers.Main).launch {
                PattiyalRepository.loadAll(mode)
                PattiyalRepository.loadDeleted(mode)
            }
        }
    }

    private fun handleReceiptsSnapshot(mode: AppMode, snapshot: DataSnapshot) {
        val helper = getBusinessDatabaseHelper()
        val localList = helper.loadAllReceipts(mode)

        if (localList.isNotEmpty() && (!snapshot.exists() || snapshot.childrenCount == 0L)) {
            triggerAntiErasureGuard("receipts", localList.size)
            return
        }

        var anyChanged = false
        isApplyingRemoteChange = true
        try {
            val localMap = localList.associateBy { it.id }
            for (child in snapshot.children) {
                val receipt = snapshotToReceipt(child) ?: continue
                val existing = localMap[receipt.id]
                val remoteUpdated = receipt.updatedAt
                val localUpdated = existing?.updatedAt ?: 0L

                if (existing == null || remoteUpdated > localUpdated) {
                    helper.saveReceipt(mode, receipt)
                    anyChanged = true
                }
            }
        } finally {
            isApplyingRemoteChange = false
        }

        if (anyChanged) {
            _lastSyncTime.value = System.currentTimeMillis()
            CoroutineScope(Dispatchers.Main).launch {
                PatrugalRepository.loadAll(mode)
                PatrugalRepository.loadDeleted(mode)
            }
        }
    }

    private fun triggerAntiErasureGuard(entityName: String, localCount: Int) {
        Log.w(tag, "Anti-Erasure Guard Triggered! Prevented remote wipe of $localCount $entityName.")
        getNirilBackupService().createSafetySnapshot() // Copy 2 Safety Vault locked
        _guardAlertMessage.value = "Anti-Erasure Guard: Remote wipe of $localCount $entityName was blocked. Local data & Copy 2 Safety Vault are safe."
        _syncStatus.value = SyncStatus.GUARD_TRIGGERED
    }

    // --- Outbound Push Operations (Offline-Persistent) ---

    override fun pushMerchant(merchant: VaangunarTharavuru, mode: AppMode) {
        if (isApplyingRemoteChange) return
        val db = getDatabase() ?: return
        val prefix = getPrefix(mode)
        val ref = db.getReference("udukkai").child(prefix).child("merchants").child("${merchant.id}")

        val data = hashMapOf<String, Any?>(
            "id" to merchant.id,
            "peyar" to MozhiJsonConverter.stringify(merchant.peyar),
            "mugavari" to MozhiJsonConverter.stringify(merchant.mugavari),
            "oor" to MozhiJsonConverter.stringify(merchant.oor),
            "maavattam" to MozhiJsonConverter.stringify(merchant.maavattam),
            "maanilam" to MozhiJsonConverter.stringify(merchant.maanilam),
            "naadu" to MozhiJsonConverter.stringify(merchant.naadu),
            "velinaadMugavari" to MozhiJsonConverter.stringify(merchant.velinaadMugavari),
            "anjalKuriyeedu" to merchant.anjalKuriyeedu,
            "gstin" to merchant.gstin,
            "minnanjal" to merchant.minnanjal,
            "tholaipaesi" to merchant.tholaipaesi,
            "createdAt" to merchant.createdAt,
            "updatedAt" to merchant.updatedAt,
            "isDeleted" to merchant.isDeleted,
            "deletedAt" to merchant.deletedAt
        )

        ref.setValue(data).addOnSuccessListener {
            _lastSyncTime.value = System.currentTimeMillis()
            _syncStatus.value = SyncStatus.SYNCED
        }.addOnFailureListener { e ->
            Log.w(tag, "Push merchant ${merchant.id} queued offline: ${e.message}")
        }
    }

    override fun pushItem(item: PorulTharavuru, mode: AppMode) {
        if (isApplyingRemoteChange) return
        val db = getDatabase() ?: return
        val prefix = getPrefix(mode)
        val ref = db.getReference("udukkai").child(prefix).child("products").child("${item.id}")

        val data = hashMapOf<String, Any?>(
            "id" to item.id,
            "porulPeyar" to MozhiJsonConverter.stringify(item.porulPeyar),
            "hsnCode" to item.hsnCode,
            "vilai" to item.vilai,
            "variVeetham" to item.variVeetham,
            "alavuVagai" to item.alavuVagai,
            "alagu" to item.alagu,
            "createdAt" to item.createdAt,
            "updatedAt" to item.updatedAt,
            "isDeleted" to item.isDeleted,
            "deletedAt" to item.deletedAt
        )

        ref.setValue(data).addOnSuccessListener {
            _lastSyncTime.value = System.currentTimeMillis()
            _syncStatus.value = SyncStatus.SYNCED
        }.addOnFailureListener { e ->
            Log.w(tag, "Push product ${item.id} queued offline: ${e.message}")
        }
    }

    override fun pushInvoice(invoice: PattiyalTharavuru, mode: AppMode) {
        if (isApplyingRemoteChange) return
        val db = getDatabase() ?: return
        val prefix = getPrefix(mode)
        val ref = db.getReference("udukkai").child(prefix).child("invoices").child("${invoice.id}")

        val data = hashMapOf<String, Any?>(
            "id" to invoice.id,
            "niruvanamId" to invoice.niruvanamId,
            "patrucheettuEn" to invoice.patrucheettuEn,
            "finYear" to invoice.finYear,
            "vanakkam" to invoice.vanakkam,
            "pattiyalVagai" to invoice.pattiyalVagai,
            "vaangunarId" to invoice.vaangunarId,
            "vaangunarPeyar" to MozhiJsonConverter.stringify(invoice.vaangunarPeyar),
            "vaangunarMunvari" to MozhiJsonConverter.stringify(invoice.vaangunarMunvari),
            "pattiyalNaal" to invoice.pattiyalNaal,
            "tharavugal" to invoice.tharavugal,
            "mothaThogai" to invoice.mothaThogai,
            "thallupadi" to invoice.thallupadi,
            "podhuThallupadiMathippu" to invoice.podhuThallupadiMathippu,
            "podhuThallupadiVagai" to invoice.podhuThallupadiVagai,
            "podhuThallupadiThogai" to invoice.podhuThallupadiThogai,
            "variThogai" to invoice.variThogai,
            "variTharavugal" to invoice.variTharavugal,
            "mothaEdai" to invoice.mothaEdai,
            "setharamGrams" to invoice.setharamGrams,
            "thabaalThogai" to invoice.thabaalThogai,
            "ahimsaPattuThogai" to invoice.ahimsaPattuThogai,
            "piravariVugal" to invoice.piravariVugal,
            "sonthaViruppangal" to invoice.sonthaViruppangal,
            "nibandhanaigal" to invoice.nibandhanaigal,
            "ullkurippu" to invoice.ullkurippu,
            "vangiTharavugal" to invoice.vangiTharavugal,
            "createdAt" to invoice.createdAt,
            "updatedAt" to invoice.updatedAt,
            "isDeleted" to invoice.isDeleted,
            "deletedAt" to invoice.deletedAt
        )

        ref.setValue(data).addOnSuccessListener {
            _lastSyncTime.value = System.currentTimeMillis()
            _syncStatus.value = SyncStatus.SYNCED
        }.addOnFailureListener { e ->
            Log.w(tag, "Push invoice ${invoice.id} queued offline: ${e.message}")
        }
    }

    override fun pushReceipt(receipt: PatrugalTharavuru, mode: AppMode) {
        if (isApplyingRemoteChange) return
        val db = getDatabase() ?: return
        val prefix = getPrefix(mode)
        val ref = db.getReference("udukkai").child(prefix).child("receipts").child("${receipt.id}")

        val data = hashMapOf<String, Any?>(
            "id" to receipt.id,
            "niruvanamId" to receipt.niruvanamId,
            "patruEn" to receipt.patruEn,
            "finYear" to receipt.finYear,
            "vanakkam" to receipt.vanakkam,
            "vaangunarId" to receipt.vaangunarId,
            "vaangunarPeyar" to MozhiJsonConverter.stringify(receipt.vaangunarPeyar),
            "vaangunarMunvari" to MozhiJsonConverter.stringify(receipt.vaangunarMunvari),
            "patruNaal" to receipt.patruNaal,
            "thogai" to receipt.thogai,
            "seluthumMurai" to receipt.seluthumMurai,
            "vangiPeyar" to receipt.vangiPeyar,
            "parivarthanaiEn" to receipt.parivarthanaiEn,
            "ullkurippu" to receipt.ullkurippu,
            "createdAt" to receipt.createdAt,
            "updatedAt" to receipt.updatedAt,
            "isDeleted" to receipt.isDeleted,
            "deletedAt" to receipt.deletedAt
        )

        ref.setValue(data).addOnSuccessListener {
            _lastSyncTime.value = System.currentTimeMillis()
            _syncStatus.value = SyncStatus.SYNCED
        }.addOnFailureListener { e ->
            Log.w(tag, "Push receipt ${receipt.id} queued offline: ${e.message}")
        }
    }

    override fun pushReceiptLinks(receiptId: Long, links: List<PatruPattiyalInaippuTharavuru>, mode: AppMode) {
        if (isApplyingRemoteChange) return
        val db = getDatabase() ?: return
        val prefix = getPrefix(mode)
        val root = db.getReference("udukkai").child(prefix).child("receipt_links").child("$receiptId")

        val linksMap = hashMapOf<String, Any>()
        links.forEach { link ->
            linksMap["${link.id}"] = hashMapOf(
                "id" to link.id,
                "patruId" to link.patruId,
                "pattiyalId" to link.pattiyalId,
                "poruthiyaThogai" to link.poruthiyaThogai
            )
        }
        root.setValue(linksMap)
    }

    // --- Deserialization Helpers ---

    private fun snapshotToMerchant(s: DataSnapshot): VaangunarTharavuru? {
        return try {
            val id = (s.child("id").value as? Number)?.toLong() ?: s.key?.toLongOrNull() ?: return null
            val peyarStr = s.child("peyar").value as? String ?: "{}"
            val mugavariStr = s.child("mugavari").value as? String ?: "{}"
            val oorStr = s.child("oor").value as? String ?: "{}"
            val maavattamStr = s.child("maavattam").value as? String ?: "{}"
            val maanilamStr = s.child("maanilam").value as? String ?: "{}"
            val naaduStr = s.child("naadu").value as? String ?: """{"en":"India","ta":"இந்தியா"}"""
            val velinaadStr = s.child("velinaadMugavari").value as? String ?: "{}"

            VaangunarTharavuru(
                id = id,
                peyar = MozhiJsonConverter.parse(peyarStr),
                mugavari = MozhiJsonConverter.parse(mugavariStr),
                oor = MozhiJsonConverter.parse(oorStr),
                maavattam = MozhiJsonConverter.parse(maavattamStr),
                maanilam = MozhiJsonConverter.parse(maanilamStr),
                naadu = MozhiJsonConverter.parse(naaduStr),
                velinaadMugavari = MozhiJsonConverter.parse(velinaadStr),
                anjalKuriyeedu = s.child("anjalKuriyeedu").value as? String ?: "",
                gstin = s.child("gstin").value as? String ?: "",
                minnanjal = s.child("minnanjal").value as? String ?: "",
                tholaipaesi = s.child("tholaipaesi").value as? String ?: "",
                createdAt = (s.child("createdAt").value as? Number)?.toLong() ?: (System.currentTimeMillis() / 1000),
                updatedAt = (s.child("updatedAt").value as? Number)?.toLong() ?: (System.currentTimeMillis() / 1000),
                isDeleted = s.child("isDeleted").value as? Boolean ?: false,
                deletedAt = (s.child("deletedAt").value as? Number)?.toLong()
            )
        } catch (e: Exception) {
            Log.w(tag, "Failed to parse RTDB merchant: ${e.message}")
            null
        }
    }

    private fun snapshotToItem(s: DataSnapshot): PorulTharavuru? {
        return try {
            val id = (s.child("id").value as? Number)?.toLong() ?: s.key?.toLongOrNull() ?: return null
            val porulPeyarStr = s.child("porulPeyar").value as? String ?: "{}"

            PorulTharavuru(
                id = id,
                porulPeyar = MozhiJsonConverter.parse(porulPeyarStr),
                hsnCode = s.child("hsnCode").value as? String ?: "",
                vilai = (s.child("vilai").value as? Number)?.toDouble() ?: 0.0,
                variVeetham = (s.child("variVeetham").value as? Number)?.toDouble() ?: 0.0,
                alavuVagai = s.child("alavuVagai").value as? String ?: "quantity",
                alagu = s.child("alagu").value as? String ?: "Nos",
                createdAt = (s.child("createdAt").value as? Number)?.toLong() ?: (System.currentTimeMillis() / 1000),
                updatedAt = (s.child("updatedAt").value as? Number)?.toLong() ?: (System.currentTimeMillis() / 1000),
                isDeleted = s.child("isDeleted").value as? Boolean ?: false,
                deletedAt = (s.child("deletedAt").value as? Number)?.toLong()
            )
        } catch (e: Exception) {
            Log.w(tag, "Failed to parse RTDB product: ${e.message}")
            null
        }
    }

    private fun snapshotToInvoice(s: DataSnapshot): PattiyalTharavuru? {
        return try {
            val id = (s.child("id").value as? Number)?.toLong() ?: s.key?.toLongOrNull() ?: return null
            val vaangunarPeyarStr = s.child("vaangunarPeyar").value as? String ?: "{}"
            val vaangunarMunvariStr = s.child("vaangunarMunvari").value as? String ?: "{}"

            PattiyalTharavuru(
                id = id,
                niruvanamId = (s.child("niruvanamId").value as? Number)?.toLong(),
                patrucheettuEn = s.child("patrucheettuEn").value as? String ?: "",
                finYear = (s.child("finYear").value as? Number)?.toInt() ?: 0,
                vanakkam = (s.child("vanakkam").value as? Number)?.toInt() ?: 1,
                pattiyalVagai = s.child("pattiyalVagai").value as? String ?: "tax-invoice",
                vaangunarId = (s.child("vaangunarId").value as? Number)?.toLong(),
                vaangunarPeyar = MozhiJsonConverter.parse(vaangunarPeyarStr),
                vaangunarMunvari = MozhiJsonConverter.parse(vaangunarMunvariStr),
                pattiyalNaal = (s.child("pattiyalNaal").value as? Number)?.toLong() ?: (System.currentTimeMillis() / 1000),
                tharavugal = s.child("tharavugal").value as? String ?: "[]",
                mothaThogai = (s.child("mothaThogai").value as? Number)?.toDouble() ?: 0.0,
                thallupadi = (s.child("thallupadi").value as? Number)?.toDouble() ?: 0.0,
                podhuThallupadiMathippu = (s.child("podhuThallupadiMathippu").value as? Number)?.toDouble() ?: 0.0,
                podhuThallupadiVagai = s.child("podhuThallupadiVagai").value as? String ?: "%",
                podhuThallupadiThogai = (s.child("podhuThallupadiThogai").value as? Number)?.toDouble() ?: 0.0,
                variThogai = (s.child("variThogai").value as? Number)?.toDouble() ?: 0.0,
                variTharavugal = s.child("variTharavugal").value as? String ?: "{}",
                mothaEdai = (s.child("mothaEdai").value as? Number)?.toDouble() ?: 0.0,
                setharamGrams = (s.child("setharamGrams").value as? Number)?.toDouble() ?: 0.0,
                thabaalThogai = (s.child("thabaalThogai").value as? Number)?.toDouble() ?: 0.0,
                ahimsaPattuThogai = (s.child("ahimsaPattuThogai").value as? Number)?.toDouble() ?: 0.0,
                piravariVugal = s.child("piravariVugal").value as? String ?: "[]",
                sonthaViruppangal = s.child("sonthaViruppangal").value as? String ?: "{}",
                nibandhanaigal = s.child("nibandhanaigal").value as? String ?: "",
                ullkurippu = s.child("ullkurippu").value as? String ?: "",
                vangiTharavugal = s.child("vangiTharavugal").value as? String ?: "{}",
                createdAt = (s.child("createdAt").value as? Number)?.toLong() ?: (System.currentTimeMillis() / 1000),
                updatedAt = (s.child("updatedAt").value as? Number)?.toLong() ?: (System.currentTimeMillis() / 1000),
                isDeleted = s.child("isDeleted").value as? Boolean ?: false,
                deletedAt = (s.child("deletedAt").value as? Number)?.toLong()
            )
        } catch (e: Exception) {
            Log.w(tag, "Failed to parse RTDB invoice: ${e.message}")
            null
        }
    }

    private fun snapshotToReceipt(s: DataSnapshot): PatrugalTharavuru? {
        return try {
            val id = (s.child("id").value as? Number)?.toLong() ?: s.key?.toLongOrNull() ?: return null
            val vaangunarPeyarStr = s.child("vaangunarPeyar").value as? String ?: "{}"
            val vaangunarMunvariStr = s.child("vaangunarMunvari").value as? String ?: "{}"

            PatrugalTharavuru(
                id = id,
                niruvanamId = (s.child("niruvanamId").value as? Number)?.toLong(),
                patruEn = s.child("patruEn").value as? String ?: "",
                finYear = s.child("finYear").value as? String ?: "",
                vanakkam = (s.child("vanakkam").value as? Number)?.toInt() ?: 1,
                vaangunarId = (s.child("vaangunarId").value as? Number)?.toLong(),
                vaangunarPeyar = MozhiJsonConverter.parse(vaangunarPeyarStr),
                vaangunarMunvari = MozhiJsonConverter.parse(vaangunarMunvariStr),
                patruNaal = (s.child("patruNaal").value as? Number)?.toLong() ?: (System.currentTimeMillis() / 1000),
                thogai = (s.child("thogai").value as? Number)?.toDouble() ?: 0.0,
                seluthumMurai = s.child("seluthumMurai").value as? String ?: "cash",
                vangiPeyar = s.child("vangiPeyar").value as? String,
                parivarthanaiEn = s.child("parivarthanaiEn").value as? String,
                ullkurippu = s.child("ullkurippu").value as? String ?: "",
                createdAt = (s.child("createdAt").value as? Number)?.toLong() ?: (System.currentTimeMillis() / 1000),
                updatedAt = (s.child("updatedAt").value as? Number)?.toLong() ?: (System.currentTimeMillis() / 1000),
                isDeleted = s.child("isDeleted").value as? Boolean ?: false,
                deletedAt = (s.child("deletedAt").value as? Number)?.toLong()
            )
        } catch (e: Exception) {
            Log.w(tag, "Failed to parse RTDB receipt: ${e.message}")
            null
        }
    }
}

private val androidSyncManagerInstance by lazy { AndroidFirebaseSyncManager() }

actual fun getFirebaseSyncManager(): FirebaseSyncManager = androidSyncManagerInstance
