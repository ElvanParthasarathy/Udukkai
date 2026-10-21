package com.elvan.udukkai.core.sync

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.elvan.udukkai.core.backup.getUdukkaiBackupService
import com.elvan.udukkai.core.platform.AppContext
import com.elvan.udukkai.data.model.CustomerModel
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.model.ReceiptInvoiceLinkModel
import com.elvan.udukkai.data.model.ReceiptModel
import com.elvan.udukkai.data.model.ProductModel
import com.elvan.udukkai.data.model.PorulFirebasePayloadConverter
import com.elvan.udukkai.data.settings.LanguageJsonConverter
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

    private fun getPrefix(scope: String): String = if (scope == "factory" || scope == "kooli") "kooli" else "pattu"

    override fun startSync(scope: String) {
        stopSync()
        val db = getDatabase() ?: return

        // Take automatic Copy 2 Safety Snapshot before sync session
        try {
            getUdukkaiBackupService().createSafetySnapshot()
            Log.i(tag, "Copy 2 Safety Snapshot created prior to RTDB sync session.")
        } catch (e: Exception) {
            Log.w(tag, "Pre-sync safety snapshot warning: ${e.message}")
        }

        _syncStatus.value = SyncStatus.SYNCING
        val prefix = getPrefix(scope)
        val rootRef = db.getReference("udukkai").child(prefix)

        // 1. Merchants Listener
        val merchantRef = rootRef.child("merchants")
        merchantRef.keepSynced(true)
        val merchantListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                handleMerchantsSnapshot(scope, snapshot)
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
                handleProductsSnapshot(scope, snapshot)
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
                handleInvoicesSnapshot(scope, snapshot)
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
                handleReceiptsSnapshot(scope, snapshot)
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
        startSync(if (com.elvan.udukkai.BuildConfig.FLAVOR == "factory") "factory" else "textiles")
    }

    override fun dismissGuardAlert() {
        _guardAlertMessage.value = null
        if (_syncStatus.value == SyncStatus.GUARD_TRIGGERED) {
            _syncStatus.value = SyncStatus.SYNCED
        }
    }

    // --- Anti-Erasure Guard & RTDB Inbound Snapshot Processors ---

    private fun handleMerchantsSnapshot(scope: String, snapshot: DataSnapshot) {
        val isFact = scope == "factory" || scope == "kooli"
        val helper = if (isFact) com.elvan.udukkai.data.database.getFactoryDatabaseHelper() else com.elvan.udukkai.data.database.getTextilesDatabaseHelper()
        val localList = helper.loadAllMerchants()

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
                    helper.saveMerchant(merchant)
                    anyChanged = true
                }
            }
        } finally {
            isApplyingRemoteChange = false
        }

        if (anyChanged) {
            _lastSyncTime.value = System.currentTimeMillis()
            CoroutineScope(Dispatchers.Main).launch {
                if (isFact) {
                    com.elvan.udukkai.factory.data.FactoryCustomerRepository.loadAll()
                    com.elvan.udukkai.factory.data.FactoryCustomerRepository.loadDeleted()
                } else {
                    com.elvan.udukkai.textiles.data.TextilesCustomerRepository.loadAll()
                    com.elvan.udukkai.textiles.data.TextilesCustomerRepository.loadDeleted()
                }
            }
        }
    }

    private fun handleProductsSnapshot(scope: String, snapshot: DataSnapshot) {
        val isFact = scope == "factory" || scope == "kooli"
        val helper = if (isFact) com.elvan.udukkai.data.database.getFactoryDatabaseHelper() else com.elvan.udukkai.data.database.getTextilesDatabaseHelper()
        val localList = helper.loadAllItems()

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
                    helper.saveItem(item)
                    anyChanged = true
                }
            }
        } finally {
            isApplyingRemoteChange = false
        }

        if (anyChanged) {
            _lastSyncTime.value = System.currentTimeMillis()
            CoroutineScope(Dispatchers.Main).launch {
                if (isFact) {
                    com.elvan.udukkai.factory.data.FactoryProductRepository.loadAll()
                    com.elvan.udukkai.factory.data.FactoryProductRepository.loadDeleted()
                } else {
                    com.elvan.udukkai.textiles.data.TextilesProductRepository.loadAll()
                    com.elvan.udukkai.textiles.data.TextilesProductRepository.loadDeleted()
                }
            }
        }
    }

    private fun handleInvoicesSnapshot(scope: String, snapshot: DataSnapshot) {
        val isFact = scope == "factory" || scope == "kooli"
        val helper = if (isFact) com.elvan.udukkai.data.database.getFactoryDatabaseHelper() else com.elvan.udukkai.data.database.getTextilesDatabaseHelper()
        val localList = helper.loadAllInvoices()

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
                    helper.saveInvoice(invoice)
                    anyChanged = true
                }
            }
        } finally {
            isApplyingRemoteChange = false
        }

        if (anyChanged) {
            _lastSyncTime.value = System.currentTimeMillis()
            CoroutineScope(Dispatchers.Main).launch {
                if (isFact) {
                    com.elvan.udukkai.factory.data.FactoryInvoiceRepository.loadAll()
                    com.elvan.udukkai.factory.data.FactoryInvoiceRepository.loadDeleted()
                } else {
                    com.elvan.udukkai.textiles.data.TextilesInvoiceRepository.loadAll()
                    com.elvan.udukkai.textiles.data.TextilesInvoiceRepository.loadDeleted()
                }
            }
        }
    }

    private fun handleReceiptsSnapshot(scope: String, snapshot: DataSnapshot) {
        val isFact = scope == "factory" || scope == "kooli"
        val helper = if (isFact) com.elvan.udukkai.data.database.getFactoryDatabaseHelper() else com.elvan.udukkai.data.database.getTextilesDatabaseHelper()
        val localList = helper.loadAllReceipts()

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
                    helper.saveReceipt(receipt)
                    anyChanged = true
                }
            }
        } finally {
            isApplyingRemoteChange = false
        }

        if (anyChanged) {
            _lastSyncTime.value = System.currentTimeMillis()
            CoroutineScope(Dispatchers.Main).launch {
                if (isFact) {
                    com.elvan.udukkai.factory.data.FactoryReceiptRepository.loadAll()
                    com.elvan.udukkai.factory.data.FactoryReceiptRepository.loadDeleted()
                } else {
                    com.elvan.udukkai.textiles.data.TextilesReceiptRepository.loadAll()
                    com.elvan.udukkai.textiles.data.TextilesReceiptRepository.loadDeleted()
                }
            }
        }
    }

    private fun triggerAntiErasureGuard(entityName: String, localCount: Int) {
        Log.w(tag, "Anti-Erasure Guard Triggered! Prevented remote wipe of $localCount $entityName.")
        getUdukkaiBackupService().createSafetySnapshot() // Copy 2 Safety Vault locked
        _guardAlertMessage.value = "Anti-Erasure Guard: Remote wipe of $localCount $entityName was blocked. Local data & Copy 2 Safety Vault are safe."
        _syncStatus.value = SyncStatus.GUARD_TRIGGERED
    }

    // --- Outbound Push Operations (Offline-Persistent) ---

    override fun pushMerchant(merchant: CustomerModel, scope: String) {
        if (isApplyingRemoteChange) return
        val db = getDatabase() ?: return
        val prefix = getPrefix(scope)
        val ref = db.getReference("udukkai").child(prefix).child("merchants").child("${merchant.id}")

        val data = hashMapOf<String, Any?>(
            "id" to merchant.id,
            "peyar" to LanguageJsonConverter.stringify(merchant.name),
            "mugavari" to LanguageJsonConverter.stringify(merchant.address),
            "oor" to LanguageJsonConverter.stringify(merchant.city),
            "maavattam" to LanguageJsonConverter.stringify(merchant.district),
            "maanilam" to LanguageJsonConverter.stringify(merchant.state),
            "naadu" to LanguageJsonConverter.stringify(merchant.country),
            "velinaadMugavari" to LanguageJsonConverter.stringify(merchant.velinaadMugavari),
            "anjalKuriyeedu" to merchant.pinCode,
            "gstin" to merchant.gstin,
            "minnanjal" to merchant.email,
            "tholaipaesi" to merchant.phone,
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

    override fun pushItem(item: ProductModel, scope: String) {
        if (isApplyingRemoteChange) return
        val db = getDatabase() ?: return
        val prefix = getPrefix(scope)
        val ref = db.getReference("udukkai").child(prefix).child("products").child("${item.id}")

        val data = PorulFirebasePayloadConverter.toFirebasePayload(item)

        ref.setValue(data).addOnSuccessListener {
            _lastSyncTime.value = System.currentTimeMillis()
            _syncStatus.value = SyncStatus.SYNCED
        }.addOnFailureListener { e ->
            Log.w(tag, "Push product ${item.id} queued offline: ${e.message}")
        }
    }

    override fun pushInvoice(invoice: InvoiceModel, scope: String) {
        if (isApplyingRemoteChange) return
        val db = getDatabase() ?: return
        val prefix = getPrefix(scope)
        val ref = db.getReference("udukkai").child(prefix).child("invoices").child("${invoice.id}")

        val data = hashMapOf<String, Any?>(
            "id" to invoice.id,
            "niruvanamId" to invoice.companyId,
            "patrucheettuEn" to invoice.invoiceNumber,
            "finYear" to invoice.finYear,
            "vanakkam" to invoice.vanakkam,
            "pattiyalVagai" to invoice.invoiceType,
            "vaangunarId" to invoice.customerId,
            "vaangunarPeyar" to LanguageJsonConverter.stringify(invoice.customerName),
            "vaangunarMunvari" to LanguageJsonConverter.stringify(invoice.customerAddress),
            "pattiyalNaal" to invoice.invoiceDate,
            "tharavugal" to invoice.itemsJson,
            "mothaThogai" to invoice.totalAmount,
            "thallupadi" to invoice.discount,
            "podhuThallupadiMathippu" to invoice.globalDiscountValue,
            "podhuThallupadiVagai" to invoice.globalDiscountType,
            "podhuThallupadiThogai" to invoice.globalDiscountAmount,
            "variThogai" to invoice.taxAmount,
            "variTharavugal" to invoice.taxDataJson,
            "mothaEdai" to invoice.mothaEdai,
            "setharamGrams" to invoice.wastageGrams,
            "thabaalThogai" to invoice.thabaalThogai,
            "ahimsaPattuThogai" to invoice.ahimsaSilkAmount,
            "piravariVugal" to invoice.piravariVugal,
            "sonthaViruppangal" to invoice.sonthaViruppangal,
            "nibandhanaigal" to invoice.nibandhanaigal,
            "ullkurippu" to invoice.notes,
            "vangiTharavugal" to invoice.bankDetailsJson,
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

    override fun pushReceipt(receipt: ReceiptModel, scope: String) {
        if (isApplyingRemoteChange) return
        val db = getDatabase() ?: return
        val prefix = getPrefix(scope)
        val ref = db.getReference("udukkai").child(prefix).child("receipts").child("${receipt.id}")

        val data = hashMapOf<String, Any?>(
            "id" to receipt.id,
            "niruvanamId" to receipt.companyId,
            "patruEn" to receipt.receiptNumber,
            "finYear" to receipt.finYear,
            "vanakkam" to receipt.vanakkam,
            "vaangunarId" to receipt.customerId,
            "vaangunarPeyar" to LanguageJsonConverter.stringify(receipt.customerName),
            "vaangunarMunvari" to LanguageJsonConverter.stringify(receipt.customerAddress),
            "patruNaal" to receipt.receiptDate,
            "thogai" to receipt.amount,
            "seluthumMurai" to receipt.seluthumMurai,
            "vangiPeyar" to receipt.bankName,
            "parivarthanaiEn" to receipt.transactionNumber,
            "ullkurippu" to receipt.notes,
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

    override fun pushReceiptLinks(receiptId: Long, links: List<ReceiptInvoiceLinkModel>, scope: String) {
        if (isApplyingRemoteChange) return
        val db = getDatabase() ?: return
        val prefix = getPrefix(scope)
        val root = db.getReference("udukkai").child(prefix).child("receipt_links").child("$receiptId")

        val linksMap = hashMapOf<String, Any>()
        links.forEach { link ->
            linksMap["${link.id}"] = hashMapOf(
                "id" to link.id,
                "patruId" to link.receiptId,
                "pattiyalId" to link.invoiceId,
                "poruthiyaThogai" to link.appliedAmount
            )
        }
        root.setValue(linksMap)
    }

    // --- Deserialization Helpers ---

    private fun snapshotToMerchant(s: DataSnapshot): CustomerModel? {
        return try {
            val id = (s.child("id").value as? Number)?.toLong() ?: s.key?.toLongOrNull() ?: return null
            val peyarStr = s.child("peyar").value as? String ?: "{}"
            val mugavariStr = s.child("mugavari").value as? String ?: "{}"
            val oorStr = s.child("oor").value as? String ?: "{}"
            val maavattamStr = s.child("maavattam").value as? String ?: "{}"
            val maanilamStr = s.child("maanilam").value as? String ?: "{}"
            val naaduStr = s.child("naadu").value as? String ?: """{"en":"India","ta":"இந்தியா"}"""
            val velinaadStr = s.child("velinaadMugavari").value as? String ?: "{}"

            CustomerModel(
                id = id,
                name = LanguageJsonConverter.parse(peyarStr),
                address = LanguageJsonConverter.parse(mugavariStr),
                city = LanguageJsonConverter.parse(oorStr),
                district = LanguageJsonConverter.parse(maavattamStr),
                state = LanguageJsonConverter.parse(maanilamStr),
                country = LanguageJsonConverter.parse(naaduStr),
                internationalAddress = LanguageJsonConverter.parse(velinaadStr),
                pinCode = s.child("anjalKuriyeedu").value as? String ?: "",
                gstin = s.child("gstin").value as? String ?: "",
                email = s.child("minnanjal").value as? String ?: "",
                phone = s.child("tholaipaesi").value as? String ?: "",
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

    private fun snapshotToItem(s: DataSnapshot): ProductModel? {
        return try {
            val id = (s.child("id").value as? Number)?.toLong() ?: s.key?.toLongOrNull() ?: return null
            val porulPeyarStr = s.child("porulPeyar").value as? String ?: "{}"

            val niruvanamIds = if (s.hasChild("niruvanamIds")) {
                val rawVal = s.child("niruvanamIds").value
                val parsed = PorulFirebasePayloadConverter.parseNiruvanamIds(rawVal)
                if (parsed.isNotEmpty()) {
                    parsed
                } else {
                    s.child("niruvanamIds").children.mapNotNull { child ->
                        (child.value as? Number)?.toLong() ?: (child.value as? String)?.trim()?.toLongOrNull()
                    }
                }
            } else {
                emptyList()
            }

            val companyPrices = if (s.hasChild("companyPrices")) {
                val rawVal = s.child("companyPrices").value
                val parsed = PorulFirebasePayloadConverter.parseCompanyPrices(rawVal)
                if (parsed.isNotEmpty()) {
                    parsed
                } else {
                    val fallbackMap = mutableMapOf<Long, Double>()
                    for (child in s.child("companyPrices").children) {
                        val key = child.key?.toLongOrNull()
                        val value = (child.value as? Number)?.toDouble() ?: (child.value as? String)?.trim()?.toDoubleOrNull()
                        if (key != null && value != null && value > 0.0) {
                            fallbackMap[key] = value
                        }
                    }
                    fallbackMap
                }
            } else {
                emptyMap()
            }

            ProductModel(
                id = id,
                name = LanguageJsonConverter.parse(porulPeyarStr),
                hsnCode = s.child("hsnCode").value as? String ?: "",
                price = (s.child("price").value as? Number)?.toDouble() ?: (s.child("vilai").value as? Number)?.toDouble() ?: 0.0,
                taxRate = (s.child("taxRate").value as? Number)?.toDouble() ?: (s.child("variVeetham").value as? Number)?.toDouble() ?: 0.0,
                quantityType = (s.child("quantityType").value ?: s.child("alavuVagai").value) as? String ?: "quantity",
                unit = (s.child("unit").value ?: s.child("alagu").value) as? String ?: "Nos",
                companyIds = niruvanamIds,
                companyPrices = companyPrices,
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

    private fun snapshotToInvoice(s: DataSnapshot): InvoiceModel? {
        return try {
            val id = (s.child("id").value as? Number)?.toLong() ?: s.key?.toLongOrNull() ?: return null
            val vaangunarPeyarStr = s.child("vaangunarPeyar").value as? String ?: "{}"
            val vaangunarMunvariStr = s.child("vaangunarMunvari").value as? String ?: "{}"

            InvoiceModel(
                id = id,
                companyId = (s.child("niruvanamId").value as? Number)?.toLong(),
                invoiceNumber = s.child("patrucheettuEn").value as? String ?: "",
                finYear = (s.child("finYear").value as? Number)?.toInt() ?: 0,
                vanakkam = (s.child("vanakkam").value as? Number)?.toInt() ?: 1,
                invoiceType = s.child("pattiyalVagai").value as? String ?: "tax-invoice",
                customerId = (s.child("vaangunarId").value as? Number)?.toLong(),
                customerName = LanguageJsonConverter.parse(vaangunarPeyarStr),
                customerAddress = LanguageJsonConverter.parse(vaangunarMunvariStr),
                invoiceDate = (s.child("pattiyalNaal").value as? Number)?.toLong() ?: (System.currentTimeMillis() / 1000),
                itemsJson = s.child("tharavugal").value as? String ?: "[]",
                totalAmount = (s.child("mothaThogai").value as? Number)?.toDouble() ?: 0.0,
                discount = (s.child("thallupadi").value as? Number)?.toDouble() ?: 0.0,
                globalDiscountValue = (s.child("podhuThallupadiMathippu").value as? Number)?.toDouble() ?: 0.0,
                globalDiscountType = s.child("podhuThallupadiVagai").value as? String ?: "%",
                globalDiscountAmount = (s.child("podhuThallupadiThogai").value as? Number)?.toDouble() ?: 0.0,
                taxAmount = (s.child("variThogai").value as? Number)?.toDouble() ?: 0.0,
                taxDataJson = s.child("variTharavugal").value as? String ?: "{}",
                totalWeight = (s.child("mothaEdai").value as? Number)?.toDouble() ?: 0.0,
                wastageGrams = (s.child("setharamGrams").value as? Number)?.toDouble() ?: 0.0,
                postalCharges = (s.child("thabaalThogai").value as? Number)?.toDouble() ?: 0.0,
                ahimsaSilkAmount = (s.child("ahimsaPattuThogai").value as? Number)?.toDouble() ?: 0.0,
                otherTaxesJson = s.child("piravariVugal").value as? String ?: "[]",
                customPreferencesJson = s.child("sonthaViruppangal").value as? String ?: "{}",
                termsAndConditions = s.child("nibandhanaigal").value as? String ?: "",
                notes = s.child("ullkurippu").value as? String ?: "",
                bankDetailsJson = s.child("vangiTharavugal").value as? String ?: "{}",
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

    private fun snapshotToReceipt(s: DataSnapshot): ReceiptModel? {
        return try {
            val id = (s.child("id").value as? Number)?.toLong() ?: s.key?.toLongOrNull() ?: return null
            val vaangunarPeyarStr = s.child("vaangunarPeyar").value as? String ?: "{}"
            val vaangunarMunvariStr = s.child("vaangunarMunvari").value as? String ?: "{}"

            ReceiptModel(
                id = id,
                companyId = (s.child("niruvanamId").value as? Number)?.toLong(),
                receiptNumber = s.child("patruEn").value as? String ?: "",
                finYear = s.child("finYear").value as? String ?: "",
                vanakkam = (s.child("vanakkam").value as? Number)?.toInt() ?: 1,
                customerId = (s.child("vaangunarId").value as? Number)?.toLong(),
                customerName = LanguageJsonConverter.parse(vaangunarPeyarStr),
                customerAddress = LanguageJsonConverter.parse(vaangunarMunvariStr),
                receiptDate = (s.child("patruNaal").value as? Number)?.toLong() ?: (System.currentTimeMillis() / 1000),
                amount = (s.child("thogai").value as? Number)?.toDouble() ?: 0.0,
                paymentMode = s.child("seluthumMurai").value as? String ?: "cash",
                bankName = s.child("vangiPeyar").value as? String,
                transactionNumber = s.child("parivarthanaiEn").value as? String,
                notes = s.child("ullkurippu").value as? String ?: "",
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
