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
import com.elvan.udukkai.data.model.ItemModel
import com.elvan.udukkai.data.model.ItemFirebasePayloadConverter
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

    private val _restoreProgress = mutableStateOf<RestoreProgress?>(null)
    override val restoreProgress: State<RestoreProgress?> = _restoreProgress

    private val _profilesRestored = mutableStateOf(true)
    override val profilesRestored: State<Boolean> = _profilesRestored

    /** Cloud records are applied one batch at a time on this thread, so the screen never freezes. */
    private val worker = java.util.concurrent.Executors.newSingleThreadExecutor()
    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())

    /** Bumped on every start and stop, so callbacks of an older sync are ignored. */
    @Volatile private var syncGeneration = 0

    /** Receipts that were just taken from the cloud: their links are taken from the cloud as well. */
    private val receiptsFromCloud = java.util.Collections.synchronizedSet(HashSet<Long>())

    /** True only while the first snapshot of a phase is being applied: later live updates stay silent. */
    @Volatile private var reportProgress = false

    /** Thrown by the merge when one sync would overwrite an unusual number of records; the sync pauses and asks. */
    private class GuardStop(val kind: RestorePhase, val toCloud: Int, val toPhone: Int) : RuntimeException()
    @Volatile private var guardApproved = false
    @Volatile private var guardScope: String? = null

    private class Phase(val kind: RestorePhase, val node: String)

    private val phases = listOf(
        Phase(RestorePhase.PROFILES, "profiles"),
        Phase(RestorePhase.CUSTOMERS, "customers"),
        Phase(RestorePhase.ITEMS, "items"),
        Phase(RestorePhase.INVOICES, "invoices"),
        Phase(RestorePhase.RECEIPTS, "receipts")
    )

    private companion object {
        const val TEXTILES_DB_URL = "https://udukkai-textiles-default-rtdb.asia-southeast1.firebasedatabase.app"
        const val FACTORY_DB_URL = "https://udukkai-factory-default-rtdb.asia-southeast1.firebasedatabase.app"
    }

    private var databaseInstance: FirebaseDatabase? = null
    private var isPersistenceSet = false
    private val activeListeners = mutableMapOf<DatabaseReference, ValueEventListener>()
    /** "collection:id" of records deleted for good on this phone: an older cloud snapshot must not bring them back. */
    private val removedLocally = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()

    @Volatile private var isApplyingRemoteChange = false

    private fun getDatabase(): FirebaseDatabase? {
        // Audit runs must neither read cloud data nor enqueue writes, even with network available.
        if (com.elvan.udukkai.BuildConfig.DEBUG && AppContext.isInitialized && java.io.File(AppContext.context.filesDir, "release-audit-offline").exists()) return null
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
            installAppCheck()

            // A config downloaded before the database existed has no address: use the known one
            val db = if (FirebaseApp.getInstance().options.databaseUrl.isNullOrBlank()) {
                FirebaseDatabase.getInstance(
                    if (com.elvan.udukkai.BuildConfig.FLAVOR == "factory") FACTORY_DB_URL else TEXTILES_DB_URL
                )
            } else {
                FirebaseDatabase.getInstance()
            }
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

    /** Proves to Firebase that the request comes from this genuine app (Play Integrity; a debug token while developing). */
    private fun installAppCheck() {
        try {
            val factory = if (com.elvan.udukkai.BuildConfig.DEBUG) {
                com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory.getInstance()
            } else {
                com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory.getInstance()
            }
            com.google.firebase.appcheck.FirebaseAppCheck.getInstance().installAppCheckProviderFactory(factory)
        } catch (e: Exception) {
            Log.w(tag, "App Check could not start: ${e.message}")
        }
    }

    override fun initialize() {
        getDatabase()
    }

    // Debug builds use their own Firebase project (src/<flavor>/debug/google-services.json), so testing never touches the real books
    private fun getPrefix(scope: String): String = if (scope == "factory" || scope == "kooli") "factory" else "textiles"

    override fun startSync(scope: String) {
        stopSync()
        // Nothing is read or written until an approved Google account is signed in
        if (!isSyncAllowed()) {
            _syncStatus.value = SyncStatus.IDLE
            return
        }
        val db = getDatabase() ?: return

        // Take automatic Copy 2 Safety Snapshot before sync session
        try {
            // At most daily, so launches cannot push a good vault out with copies of bad data
            val svc = getUdukkaiBackupService()
            val last = svc.getSafetySnapshotStats()?.lastModified ?: 0L
            if (System.currentTimeMillis() - last > 24 * 3600_000L) svc.createSafetySnapshot()
            Log.i(tag, "Copy 2 Safety Snapshot created prior to RTDB sync session.")
        } catch (e: Exception) {
            Log.w(tag, "Pre-sync safety snapshot warning: ${e.message}")
        }

        val generation = ++syncGeneration
        _syncStatus.value = SyncStatus.SYNCING
        _profilesRestored.value = false
        // Offline or slow: do not hold the app back for the business profiles forever
        mainHandler.postDelayed({
            if (generation == syncGeneration) _profilesRestored.value = true
        }, 15000)

        val rootRef = db.getReference(getPrefix(scope))
        _restoreProgress.value = null
        attachPhase(0, scope, rootRef, generation)
        Log.i(tag, "RTDB restore started for ${getPrefix(scope)}")
    }

    /** Phases run one after the other: each starts when the one before it has been stored. */
    private fun attachPhase(index: Int, scope: String, rootRef: DatabaseReference, generation: Int) {
        if (generation != syncGeneration) return
        if (index >= phases.size) {
            attachLinks(scope, rootRef, generation)
            return
        }
        val phase = phases[index]
        val ref = rootRef.child(phase.node)
        ref.keepSynced(true)
        var first = true
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isFirst = first
                first = false
                worker.execute {
                    if (generation != syncGeneration) return@execute
                    reportProgress = isFirst
                    try {
                        applyPhase(phase.kind, scope, snapshot)
                    } catch (g: GuardStop) {
                        pauseForGuard(g, scope)
                        return@execute
                    } catch (e: Exception) {
                        Log.e(tag, "Applying ${phase.node} failed: ${e.message}", e)
                        _syncStatus.value = SyncStatus.ERROR
                    }
                    if (isFirst) mainHandler.post {
                        if (phase.kind == RestorePhase.PROFILES) _profilesRestored.value = true
                        attachPhase(index + 1, scope, rootRef, generation)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(tag, "${phase.node} sync cancelled: ${error.message}")
                _syncStatus.value = SyncStatus.ERROR
                if (first) {
                    first = false
                    _restoreProgress.value = null
                    _profilesRestored.value = true
                }
            }
        }
        ref.addValueEventListener(listener)
        activeListeners[ref] = listener
    }

    private fun attachLinks(scope: String, rootRef: DatabaseReference, generation: Int) {
        val ref = rootRef.child("receipt_links")
        ref.keepSynced(true)
        var first = true
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isFirst = first
                first = false
                worker.execute {
                    if (generation != syncGeneration) return@execute
                    try {
                        applyLinks(scope, snapshot)
                    } catch (e: Exception) {
                        Log.e(tag, "Applying receipt links failed: ${e.message}", e)
                        _syncStatus.value = SyncStatus.ERROR
                    }
                    if (isFirst) {
                        _restoreProgress.value = null
                        _syncStatus.value = SyncStatus.SYNCED
                        guardApproved = false
                        _lastSyncTime.value = System.currentTimeMillis()
                        // No automatic cloud prune: it deleted cloud records the phone lacked (e.g. after restoring an old backup)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(tag, "Receipt links sync cancelled: ${error.message}")
                _restoreProgress.value = null
                _syncStatus.value = SyncStatus.ERROR
            }
        }
        ref.addValueEventListener(listener)
        activeListeners[ref] = listener
    }

    override fun stopSync() {
        syncGeneration++
        activeListeners.forEach { (ref, listener) ->
            ref.removeEventListener(listener)
        }
        activeListeners.clear()
        _restoreProgress.value = null
        _profilesRestored.value = true
        _syncStatus.value = SyncStatus.IDLE
    }

    override fun wipeScope(scope: String) {
        val prefix = getPrefix(scope)
        try {
            stopSync()
            _profilesRestored.value = true
            _restoreProgress.value = null
            _syncStatus.value = SyncStatus.IDLE
            if (com.elvan.udukkai.BuildConfig.DEBUG && isSyncAllowed()) {
                val db = getDatabase()
                db?.getReference(prefix)?.removeValue()
                Log.i(tag, "Purged remote Firebase RTDB branch: $prefix")
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to wipe remote scope $prefix: ${e.message}")
        }
    }

    override fun triggerManualSync() {
        val scope = if (com.elvan.udukkai.BuildConfig.FLAVOR == "factory") "factory" else "textiles"
        startSync(scope)
    }

    override fun cleanCloudToMatchLocal(scope: String, onComplete: ((Boolean) -> Unit)?) {
        if (!isSyncAllowed()) {
            onComplete?.invoke(false)
            return
        }
        val db = getDatabase()
        if (db == null) {
            onComplete?.invoke(false)
            return
        }

        worker.execute {
            try {
                val prefix = getPrefix(scope)
                val helper = helperFor(scope)
                val rootRef = db.getReference(prefix)

                // Local active truth sets (NEVER modified, only read)
                val activeProfiles = helper.loadAllProfiles()
                val activeCompanyIds = activeProfiles.mapNotNull { it.id }.toSet()
                val activeCustomers = helper.loadAllCustomers()
                val activeCustomerIds = activeCustomers.mapNotNull { it.id }.toSet()
                val activeItems = helper.loadAllItems()
                val activeItemIds = activeItems.map { it.id }.toSet()
                val activeInvoices = helper.loadAllInvoices().filter { it.companyId in activeCompanyIds }
                val activeInvoiceIds = activeInvoices.map { it.id }.toSet()
                val activeReceipts = helper.loadAllReceipts().filter { it.companyId in activeCompanyIds }
                val activeReceiptIds = activeReceipts.map { it.id }.toSet()

                Log.i(tag, "Starting cleanCloudToMatchLocal for $prefix. Active local: ${activeProfiles.size} profiles, ${activeInvoices.size} invoices, ${activeReceipts.size} receipts, ${activeItems.size} items, ${activeCustomers.size} customers")

                // 1. Clean Profiles: remove any remote profile not in activeCompanyIds
                rootRef.child("profiles").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (child in snapshot.children) {
                            val id = child.key?.toLongOrNull()
                            if (id == null || id !in activeCompanyIds) {
                                Log.i(tag, "Pruning remote orphan/deleted profile: ${child.key}")
                                child.ref.removeValue()
                            }
                        }
                        activeProfiles.forEach { pushProfile(it, scope) }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.e(tag, "Clean profiles cancelled: ${error.message}")
                    }
                })

                // 2. Clean Invoices: remove any remote invoice not in activeInvoiceIds or not in activeCompanyIds
                rootRef.child("invoices").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (child in snapshot.children) {
                            val id = child.key?.toLongOrNull()
                            val compId = (child.child("companyId").value as? Number)?.toLong()
                            val isDel = child.child("isDeleted").value as? Boolean ?: false
                            if (id == null || id !in activeInvoiceIds || (compId != null && compId !in activeCompanyIds) || isDel) {
                                Log.i(tag, "Pruning remote extra/deleted invoice: ${child.key}")
                                child.ref.removeValue()
                            }
                        }
                        activeInvoices.forEach { pushInvoice(it, scope) }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.e(tag, "Clean invoices cancelled: ${error.message}")
                    }
                })

                // 3. Clean Receipts & Receipt Links
                rootRef.child("receipts").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (child in snapshot.children) {
                            val id = child.key?.toLongOrNull()
                            val compId = (child.child("companyId").value as? Number)?.toLong()
                            val isDel = child.child("isDeleted").value as? Boolean ?: false
                            if (id == null || id !in activeReceiptIds || (compId != null && compId !in activeCompanyIds) || isDel) {
                                Log.i(tag, "Pruning remote extra/deleted receipt: ${child.key}")
                                child.ref.removeValue()
                                rootRef.child("receipt_links").child("${child.key}").removeValue()
                            }
                        }
                        activeReceipts.forEach { receipt ->
                            pushReceipt(receipt, scope)
                            val links = helper.getLinksForReceipt(receipt.id)
                            if (links.isNotEmpty()) {
                                pushReceiptLinks(receipt.id, links, scope)
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.e(tag, "Clean receipts cancelled: ${error.message}")
                    }
                })

                rootRef.child("receipt_links").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (child in snapshot.children) {
                            val id = child.key?.toLongOrNull()
                            if (id == null || id !in activeReceiptIds) {
                                child.ref.removeValue()
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })

                // 4. Clean Customers
                rootRef.child("customers").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (child in snapshot.children) {
                            val id = child.key?.toLongOrNull()
                            val isDel = child.child("isDeleted").value as? Boolean ?: false
                            if (id == null || id !in activeCustomerIds || isDel) {
                                Log.i(tag, "Pruning remote extra/deleted customer: ${child.key}")
                                child.ref.removeValue()
                            }
                        }
                        activeCustomers.forEach { pushMerchant(it, scope) }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })

                // 5. Clean Items
                rootRef.child("items").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (child in snapshot.children) {
                            val id = child.key?.toLongOrNull()
                            val isDel = child.child("isDeleted").value as? Boolean ?: false
                            if (id == null || id !in activeItemIds || isDel) {
                                Log.i(tag, "Pruning remote extra/deleted item: ${child.key}")
                                child.ref.removeValue()
                            }
                        }
                        activeItems.forEach { pushItem(it, scope) }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })

                mainHandler.post {
                    _lastSyncTime.value = System.currentTimeMillis()
                    _syncStatus.value = SyncStatus.SYNCED
                    onComplete?.invoke(true)
                }
            } catch (e: Exception) {
                Log.e(tag, "cleanCloudToMatchLocal failed: ${e.message}", e)
                mainHandler.post { onComplete?.invoke(false) }
            }
        }
    }

    /** Stop: the sync stays off (nothing was changed) until the user syncs again. */
    override fun dismissGuardAlert() {
        _guardAlertMessage.value = null
        if (_syncStatus.value == SyncStatus.GUARD_TRIGGERED) _syncStatus.value = SyncStatus.IDLE
    }

    /** Continue: the paused sync runs, keeping the newest version of each record. */
    override fun approveGuard() {
        val scope = guardScope ?: return
        _guardAlertMessage.value = null
        guardApproved = true
        startSync(scope)
    }

    // --- Anti-Erasure Guard & RTDB Inbound Snapshot Processors ---

    private fun isFactoryScope(scope: String) = scope == "factory" || scope == "kooli"

    private fun helperFor(scope: String) =
        if (isFactoryScope(scope)) com.elvan.udukkai.data.database.getFactoryDatabaseHelper()
        else com.elvan.udukkai.data.database.getTextilesDatabaseHelper()

    private fun isSyncAllowed(): Boolean = com.elvan.udukkai.core.auth.AllowedAccounts.isAllowed(
        com.elvan.udukkai.core.auth.platformSignedInEmail()
    )

    private fun applyPhase(kind: RestorePhase, scope: String, snapshot: DataSnapshot) {
        val isFact = isFactoryScope(scope)
        val helper = helperFor(scope)
        when (kind) {
            RestorePhase.PROFILES -> mergeCollection(
                kind, scope, snapshot, helper.loadEveryProfile(),
                parse = { snapshotToProfile(it) }, id = { it.id ?: 0L }, updated = { it.updatedAt },
                isDeleted = { it.isDeleted }, deletedAt = { null },
                upsert = { helper.upsertProfilesFromSync(it) }, push = { pushProfile(it, scope) },
                reload = {
                    if (isFact) com.elvan.udukkai.factory.data.FactoryProfileRepository.refreshFromDatabase()
                    else com.elvan.udukkai.textiles.data.TextilesProfileRepository.refreshFromDatabase()
                    com.elvan.udukkai.core.auth.AuthManager.refreshProfileStatus()
                }
            )
            RestorePhase.CUSTOMERS -> mergeCollection(
                kind, scope, snapshot, helper.loadEveryCustomer(),
                parse = { snapshotToMerchant(it) }, id = { it.id ?: 0L }, updated = { it.updatedAt },
                isDeleted = { it.isDeleted }, deletedAt = { it.deletedAt },
                upsert = { helper.upsertCustomersFromSync(it) }, push = { pushMerchant(it, scope) },
                reload = {
                    if (isFact) {
                        com.elvan.udukkai.factory.data.FactoryCustomerRepository.loadAll()
                        com.elvan.udukkai.factory.data.FactoryCustomerRepository.loadDeleted()
                    } else {
                        com.elvan.udukkai.textiles.data.TextilesCustomerRepository.loadAll()
                        com.elvan.udukkai.textiles.data.TextilesCustomerRepository.loadDeleted()
                    }
                }
            )
            RestorePhase.ITEMS -> mergeCollection(
                kind, scope, snapshot, helper.loadEveryItem(),
                parse = { snapshotToItem(it) }, id = { it.id }, updated = { it.updatedAt },
                isDeleted = { it.isDeleted }, deletedAt = { it.deletedAt },
                upsert = { helper.upsertItemsFromSync(it) }, push = { pushItem(it, scope) },
                reload = {
                    if (isFact) {
                        com.elvan.udukkai.factory.data.FactoryItemRepository.loadAll()
                        com.elvan.udukkai.factory.data.FactoryItemRepository.loadDeleted()
                    } else {
                        com.elvan.udukkai.textiles.data.TextilesItemRepository.loadAll()
                        com.elvan.udukkai.textiles.data.TextilesItemRepository.loadDeleted()
                    }
                }
            )
            RestorePhase.INVOICES -> mergeCollection(
                kind, scope, snapshot, helper.loadEveryInvoice(),
                parse = { snapshotToInvoice(it) }, id = { it.id }, updated = { it.updatedAt },
                isDeleted = { it.isDeleted }, deletedAt = { it.deletedAt },
                upsert = { helper.upsertInvoicesFromSync(it) }, push = { pushInvoice(it, scope) },
                reload = {
                    if (isFact) {
                        com.elvan.udukkai.factory.data.FactoryInvoiceRepository.loadAll()
                        com.elvan.udukkai.factory.data.FactoryInvoiceRepository.loadDeleted()
                    } else {
                        com.elvan.udukkai.textiles.data.TextilesInvoiceRepository.loadAll()
                        com.elvan.udukkai.textiles.data.TextilesInvoiceRepository.loadDeleted()
                    }
                }
            )
            RestorePhase.RECEIPTS -> mergeCollection(
                kind, scope, snapshot, helper.loadEveryReceipt(),
                parse = { snapshotToReceipt(it) }, id = { it.id }, updated = { it.updatedAt },
                isDeleted = { it.isDeleted }, deletedAt = { it.deletedAt },
                upsert = { helper.upsertReceiptsFromSync(it) }, push = { pushReceipt(it, scope) },
                reload = {
                    if (isFact) {
                        com.elvan.udukkai.factory.data.FactoryReceiptRepository.loadAll()
                        com.elvan.udukkai.factory.data.FactoryReceiptRepository.loadDeleted()
                    } else {
                        com.elvan.udukkai.textiles.data.TextilesReceiptRepository.loadAll()
                        com.elvan.udukkai.textiles.data.TextilesReceiptRepository.loadDeleted()
                    }
                },
                onApplied = { receiptsFromCloud.add(it.id) }
            )
        }
    }

    /**
     * Merges one cloud collection with the phone, both ways: newer cloud records are stored (in batches,
     * so the lists fill up while it runs), newer or missing phone records are uploaded. Nothing is ever
     * removed from the phone because it is missing in the cloud.
     */
    private fun <T> mergeCollection(
        kind: RestorePhase,
        scope: String,
        snapshot: DataSnapshot,
        local: List<T>,
        parse: (DataSnapshot) -> T?,
        id: (T) -> Long,
        updated: (T) -> Long,
        isDeleted: (T) -> Boolean,
        deletedAt: (T) -> Long?,
        upsert: (List<T>) -> Unit,
        push: (T) -> Unit,
        reload: () -> Unit,
        onApplied: (T) -> Unit = {}
    ) {
        val total = snapshot.childrenCount.toInt()
        val localById = local.associateBy(id)
        val remoteIds = HashSet<Long>()
        val batch = ArrayList<T>()
        var done = 0
        var storedAny = false
        val purgeLimit = System.currentTimeMillis() / 1000 - 30 * 86400
        val activeCompanyIds = if (kind == RestorePhase.INVOICES || kind == RestorePhase.RECEIPTS || kind == RestorePhase.PROFILES) {
            helperFor(scope).loadAllProfiles().mapNotNull { it.id }.toSet()
        } else emptySet()

        // Guard: nothing is applied in either direction when one sync would overwrite unusually many records
        if (!guardApproved) {
            var toCloud = 0
            var toPhone = 0
            for (child in snapshot.children) {
                val remote = parse(child) ?: continue
                if (isDeleted(remote)) continue
                val mine = localById[id(remote)] ?: continue
                if (updated(mine) > updated(remote)) toCloud++ else if (updated(remote) > updated(mine)) toPhone++
            }
            val limit = maxOf(20, local.size / 5)
            if (toCloud > limit || toPhone > limit) throw GuardStop(kind, toCloud, toPhone)
        }

        fun flush() {
            if (batch.isEmpty()) return
            isApplyingRemoteChange = true
            try {
                upsert(batch.toList())
            } finally {
                isApplyingRemoteChange = false
            }
            storedAny = true
            batch.forEach(onApplied)
            batch.clear()
            _lastSyncTime.value = System.currentTimeMillis()
            mainHandler.post { reload() }
        }

        for (child in snapshot.children) {
            val remote = parse(child)
            if (remote != null) {
                val remoteId = id(remote)

                // 1. Never restore deleted records from cloud - purge from cloud
                if (isDeleted(remote)) {
                    // kept in the cloud: sync never deletes data
                    continue
                }

                // 2. Invoices & receipts: if company doesn't exist locally, purge from cloud
                if (kind == RestorePhase.INVOICES) {
                    val compId = (remote as? InvoiceModel)?.companyId
                    if (compId != null && compId !in activeCompanyIds) {
                        // kept in the cloud: sync never deletes data
                        continue
                    }
                }
                if (kind == RestorePhase.RECEIPTS) {
                    val compId = (remote as? ReceiptModel)?.companyId
                    if (compId != null && compId !in activeCompanyIds) {
                        // kept in the cloud: sync never deletes data
                        continue
                    }
                }

                // 3. Profiles: if marked deleted locally, purge from cloud
                if (kind == RestorePhase.PROFILES) {
                    val localProfile = localById[remoteId] as? com.elvan.udukkai.data.settings.BusinessProfile
                    if (localProfile?.isDeleted == true) {
                        // kept in the cloud: sync never deletes data
                        continue
                    }
                }

                remoteIds.add(remoteId)
                val mine = localById[remoteId]
                // Deleted for good on this phone a moment ago: an older snapshot must not revive it
                if (mine == null && removedLocally.contains("${kind.name.lowercase()}:$remoteId")) {
                    // kept in the cloud: sync never deletes data
                    continue
                }
                val oldTombstone = mine == null && isDeleted(remote) && (deletedAt(remote) ?: Long.MAX_VALUE) < purgeLimit
                if (!oldTombstone && (mine == null || updated(remote) > updated(mine))) {
                    batch.add(remote)
                    if (batch.size >= 40) flush()
                } else if (mine != null && updated(mine) > updated(remote)) {
                    push(mine)
                }
            } else {
                // kept in the cloud: sync never deletes data
            }
            done++
            if (reportProgress && (storedAny || batch.isNotEmpty()) && (done % 10 == 0 || done == total)) {
                _restoreProgress.value = RestoreProgress(kind, done, total)
            }
        }
        flush()
        // Records only this phone has (first sync, or made while offline) go up - active only
        for (mine in local) {
            if (!isDeleted(mine) && id(mine) !in remoteIds) push(mine)
        }
    }

    private fun sameLinks(a: List<ReceiptInvoiceLinkModel>, b: List<ReceiptInvoiceLinkModel>): Boolean {
        fun key(l: List<ReceiptInvoiceLinkModel>) = l.map { "${it.invoiceId}:${it.appliedAmount}" }.sorted()
        return key(a) == key(b)
    }

    private fun applyLinks(scope: String, snapshot: DataSnapshot) {
        val isFact = isFactoryScope(scope)
        val helper = helperFor(scope)
        val remote = HashMap<Long, List<ReceiptInvoiceLinkModel>>()
        for (child in snapshot.children) {
            val receiptId = child.key?.toLongOrNull() ?: continue
            remote[receiptId] = child.children.mapNotNull { l ->
                val invoiceId = ((l.child("pattiyalId").value ?: l.child("invoiceId").value) as? Number)?.toLong() ?: return@mapNotNull null
                val amount = ((l.child("poruthiyaThogai").value ?: l.child("appliedAmount").value) as? Number)?.toDouble() ?: 0.0
                ReceiptInvoiceLinkModel(id = 0L, receiptId = receiptId, invoiceId = invoiceId, appliedAmount = amount)
            }
        }
        val receiptIds = helper.loadEveryReceipt().map { it.id }.toSet()
        val toApply = HashMap<Long, List<ReceiptInvoiceLinkModel>>()
        for ((receiptId, links) in remote) {
            if (receiptId !in receiptIds) continue
            val mine = helper.getLinksForReceipt(receiptId)
            if (sameLinks(mine, links)) continue
            if (mine.isEmpty() || receiptId in receiptsFromCloud) toApply[receiptId] = links
            else pushReceiptLinks(receiptId, mine, scope)
        }
        for (receiptId in receiptIds) {
            if (receiptId in remote) continue
            val mine = helper.getLinksForReceipt(receiptId)
            if (mine.isNotEmpty()) pushReceiptLinks(receiptId, mine, scope)
        }
        if (toApply.isNotEmpty()) {
            isApplyingRemoteChange = true
            try {
                helper.replaceLinksFromSync(toApply)
            } finally {
                isApplyingRemoteChange = false
            }
            mainHandler.post {
                if (isFact) {
                    com.elvan.udukkai.factory.data.FactoryReceiptRepository.loadAll()
                    com.elvan.udukkai.factory.data.FactoryInvoiceRepository.loadAll()
                } else {
                    com.elvan.udukkai.textiles.data.TextilesReceiptRepository.loadAll()
                    com.elvan.udukkai.textiles.data.TextilesInvoiceRepository.loadAll()
                }
            }
        }
    }

    private fun pauseForGuard(g: GuardStop, scope: String) {
        Log.w(tag, "Sync guard: ${g.kind} would overwrite ${g.toCloud} cloud and ${g.toPhone} phone records. Paused.")
        try { getUdukkaiBackupService().createSafetySnapshot() } catch (_: Exception) {}
        mainHandler.post {
            stopSync()
            guardScope = scope
            _guardAlertMessage.value = "${g.kind.name.lowercase()}: ${g.toCloud} \u2191  ${g.toPhone} \u2193"
            _syncStatus.value = SyncStatus.GUARD_TRIGGERED
        }
    }

    override fun pushMerchant(merchant: CustomerModel, scope: String) {
        if (isApplyingRemoteChange || !isSyncAllowed()) return
        val db = getDatabase() ?: return
        val prefix = getPrefix(scope)
        val ref = db.getReference(prefix).child("customers").child("${merchant.id}")

        val data = hashMapOf<String, Any?>(
            "id" to merchant.id,
            "name" to LanguageJsonConverter.stringify(merchant.name),
            "address" to LanguageJsonConverter.stringify(merchant.address),
            "city" to LanguageJsonConverter.stringify(merchant.city),
            "district" to LanguageJsonConverter.stringify(merchant.district),
            "state" to LanguageJsonConverter.stringify(merchant.state),
            "country" to LanguageJsonConverter.stringify(merchant.country),
            "internationalAddress" to LanguageJsonConverter.stringify(merchant.internationalAddress),
            "extra" to LanguageJsonConverter.stringify(merchant.extra),
            "pinCode" to merchant.pinCode,
            "gstin" to merchant.gstin,
            "email" to merchant.email,
            "phone" to merchant.phone,
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

    override fun pushItem(item: ItemModel, scope: String) {
        if (isApplyingRemoteChange || !isSyncAllowed()) return
        val db = getDatabase() ?: return
        val prefix = getPrefix(scope)
        val itemRef = db.getReference(prefix).child("items").child("${item.id}")

        val data = ItemFirebasePayloadConverter.toFirebasePayload(item)

        itemRef.setValue(data).addOnSuccessListener {
            _lastSyncTime.value = System.currentTimeMillis()
            _syncStatus.value = SyncStatus.SYNCED
        }.addOnFailureListener { e ->
            Log.w(tag, "Push item ${item.id} queued offline: ${e.message}")
        }
    }

    override fun pushInvoice(invoice: InvoiceModel, scope: String) {
        if (isApplyingRemoteChange || !isSyncAllowed()) return
        val db = getDatabase() ?: return
        val prefix = getPrefix(scope)
        val ref = db.getReference(prefix).child("invoices").child("${invoice.id}")

        val data = hashMapOf<String, Any?>(
            "id" to invoice.id,
            "companyId" to invoice.companyId,
            "invoiceNumber" to invoice.invoiceNumber,
            "finYear" to invoice.finYear,
            "vanakkam" to invoice.vanakkam,
            "invoiceType" to invoice.invoiceType,
            "customerId" to invoice.customerId,
            "customerName" to LanguageJsonConverter.stringify(invoice.customerName),
            "customerAddress" to LanguageJsonConverter.stringify(invoice.customerAddress),
            "invoiceDate" to invoice.invoiceDate,
            "itemsJson" to invoice.itemsJson,
            "totalAmount" to invoice.totalAmount,
            "discount" to invoice.discount,
            "globalDiscountValue" to invoice.globalDiscountValue,
            "globalDiscountType" to invoice.globalDiscountType,
            "globalDiscountAmount" to invoice.globalDiscountAmount,
            "taxAmount" to invoice.taxAmount,
            "taxDataJson" to invoice.taxDataJson,
            "totalWeight" to invoice.totalWeight,
            "wastageGrams" to invoice.wastageGrams,
            "postalCharges" to invoice.postalCharges,
            "ahimsaSilkAmount" to invoice.ahimsaSilkAmount,
            "otherTaxesJson" to invoice.otherTaxesJson,
            "customPreferencesJson" to invoice.customPreferencesJson,
            "termsAndConditions" to invoice.termsAndConditions,
            "notes" to invoice.notes,
            "bankDetailsJson" to invoice.bankDetailsJson,
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
        if (isApplyingRemoteChange || !isSyncAllowed()) return
        val db = getDatabase() ?: return
        val prefix = getPrefix(scope)
        val ref = db.getReference(prefix).child("receipts").child("${receipt.id}")

        val data = hashMapOf<String, Any?>(
            "id" to receipt.id,
            "companyId" to receipt.companyId,
            "receiptNumber" to receipt.receiptNumber,
            "finYear" to receipt.finYear,
            "vanakkam" to receipt.vanakkam,
            "customerId" to receipt.customerId,
            "customerName" to LanguageJsonConverter.stringify(receipt.customerName),
            "customerAddress" to LanguageJsonConverter.stringify(receipt.customerAddress),
            "receiptDate" to receipt.receiptDate,
            "amount" to receipt.amount,
            "paymentMode" to receipt.paymentMode,
            "bankName" to receipt.bankName,
            "transactionNumber" to receipt.transactionNumber,
            "notes" to receipt.notes,
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
        if (isApplyingRemoteChange || !isSyncAllowed()) return
        val db = getDatabase() ?: return
        val prefix = getPrefix(scope)
        val root = db.getReference(prefix).child("receipt_links").child("$receiptId")

        if (links.isEmpty()) {
            root.removeValue()
            return
        }
        val linksMap = hashMapOf<String, Any>()
        links.forEach { link ->
            linksMap["${link.invoiceId}"] = hashMapOf(
                "receiptId" to receiptId,
                "invoiceId" to link.invoiceId,
                "appliedAmount" to link.appliedAmount
            )
        }
        root.setValue(linksMap)
    }

    override fun pushProfile(profile: com.elvan.udukkai.data.settings.BusinessProfile, scope: String) {
        if (isApplyingRemoteChange || !isSyncAllowed()) return
        val id = profile.id ?: return
        val db = getDatabase() ?: return
        val ref = db.getReference(getPrefix(scope)).child("profiles").child("$id")
        fun text(map: Map<String, String>) = LanguageJsonConverter.stringify(map)
        val data = hashMapOf<String, Any?>(
            "id" to id,
            "primaryLanguage" to profile.primaryLanguage,
            "secondaryLanguage" to profile.secondaryLanguage,
            "isBilingual" to profile.isBilingual,
            "splitGst" to profile.splitGst,
            "businessName" to text(profile.businessName),
            "shortName" to profile.shortName,
            "phone1" to profile.phone1,
            "phone2" to profile.phone2,
            "email" to profile.email,
            "gstin" to profile.gstin,
            "pan" to profile.pan,
            "address" to text(profile.address),
            "city" to text(profile.city),
            "district" to text(profile.district),
            "state" to text(profile.state),
            "country" to text(profile.country),
            "pinCode" to profile.pinCode,
            "bankName" to text(profile.bankName),
            "branch" to text(profile.branch),
            "accountNumber" to profile.accountNumber,
            "ifsc" to profile.ifsc,
            "logo" to profile.logo,
            "wideLogo" to profile.wideLogo,
            "headerLayout" to profile.headerLayout,
            "signature" to profile.signature,
            "signatoryName" to profile.signatoryName,
            "signatoryFirstName" to profile.signatoryFirstName,
            "signatoryLastName" to profile.signatoryLastName,
            "tagline" to text(profile.tagline),
            "upiId" to profile.upiId,
            "themeColor" to profile.themeColor,
            "paymentAccountsJson" to profile.paymentAccountsJson,
            "printSettingsJson" to profile.printSettingsJson,
            "updatedAt" to profile.updatedAt
        )
        ref.setValue(data).addOnSuccessListener {
            _lastSyncTime.value = System.currentTimeMillis()
        }.addOnFailureListener { e ->
            Log.w(tag, "Push profile $id queued offline: ${e.message}")
        }
    }

    override fun pushRemove(collection: String, id: Long, scope: String) {
        removedLocally.add("$collection:$id")
        if (!isSyncAllowed()) return
        val db = getDatabase() ?: return
        val root = db.getReference(getPrefix(scope))
        root.child(collection).child("$id").removeValue()
        if (collection == "receipts") root.child("receipt_links").child("$id").removeValue()
    }

    private fun snapshotToProfile(s: DataSnapshot): com.elvan.udukkai.data.settings.BusinessProfile? {
        return try {
            val id = (s.child("id").value as? Number)?.toLong() ?: s.key?.toLongOrNull() ?: return null
            fun str(key: String) = s.child(key).value as? String ?: ""
            fun map(key: String) = LanguageJsonConverter.parse(s.child(key).value as? String ?: "{}")
            com.elvan.udukkai.data.settings.BusinessProfile(
                id = id,
                primaryLanguage = str("primaryLanguage").ifEmpty { "ta" },
                secondaryLanguage = str("secondaryLanguage").ifEmpty { "en" },
                isBilingual = s.child("isBilingual").value as? Boolean ?: true,
                splitGst = s.child("splitGst").value as? Boolean ?: false,
                businessName = map("businessName"),
                shortName = str("shortName"),
                phone1 = str("phone1"),
                phone2 = str("phone2"),
                email = str("email"),
                gstin = str("gstin"),
                pan = str("pan"),
                address = map("address"),
                city = map("city"),
                district = map("district"),
                state = map("state"),
                country = map("country"),
                pinCode = str("pinCode"),
                bankName = map("bankName"),
                branch = map("branch"),
                accountNumber = str("accountNumber"),
                ifsc = str("ifsc"),
                logo = str("logo"),
                wideLogo = str("wideLogo"),
                headerLayout = str("headerLayout").ifEmpty { "small" },
                signature = str("signature"),
                signatoryName = str("signatoryName"),
                signatoryFirstName = str("signatoryFirstName"),
                signatoryLastName = str("signatoryLastName"),
                tagline = map("tagline"),
                upiId = str("upiId"),
                themeColor = str("themeColor").ifEmpty { "#388e3c" },
                paymentAccountsJson = str("paymentAccountsJson").ifEmpty { "[]" },
                printSettingsJson = str("printSettingsJson").ifEmpty { "{}" },
                updatedAt = (s.child("updatedAt").value as? Number)?.toLong() ?: 0L
            )
        } catch (e: Exception) {
            Log.w(tag, "Skipping unreadable profile ${s.key}: ${e.message}")
            null
        }
    }

    // --- Deserialization Helpers ---

    private fun snapshotToMerchant(s: DataSnapshot): CustomerModel? {
        return try {
            val id = (s.child("id").value as? Number)?.toLong() ?: s.key?.toLongOrNull() ?: return null
            val nameStr = (s.child("name").value ?: s.child("peyar").value) as? String ?: "{}"
            val addressStr = (s.child("address").value ?: s.child("mugavari").value) as? String ?: "{}"
            val cityStr = (s.child("city").value ?: s.child("oor").value) as? String ?: "{}"
            val districtStr = (s.child("district").value ?: s.child("maavattam").value) as? String ?: "{}"
            val stateStr = (s.child("state").value ?: s.child("maanilam").value) as? String ?: "{}"
            val countryStr = (s.child("country").value ?: s.child("naadu").value) as? String ?: """{"en":"India","ta":"இந்தியா"}"""
            val intlAddressStr = (s.child("internationalAddress").value ?: s.child("velinaadMugavari").value) as? String ?: "{}"

            CustomerModel(
                id = id,
                name = LanguageJsonConverter.parse(nameStr),
                address = LanguageJsonConverter.parse(addressStr),
                city = LanguageJsonConverter.parse(cityStr),
                district = LanguageJsonConverter.parse(districtStr),
                state = LanguageJsonConverter.parse(stateStr),
                country = LanguageJsonConverter.parse(countryStr),
                internationalAddress = LanguageJsonConverter.parse(intlAddressStr),
                extra = LanguageJsonConverter.parse(s.child("extra").value as? String ?: "{}"),
                pinCode = (s.child("pinCode").value ?: s.child("anjalKuriyeedu").value) as? String ?: "",
                gstin = s.child("gstin").value as? String ?: "",
                email = (s.child("email").value ?: s.child("minnanjal").value) as? String ?: "",
                phone = (s.child("phone").value ?: s.child("tholaipaesi").value) as? String ?: "",
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

    private fun snapshotToItem(s: DataSnapshot): ItemModel? {
        return try {
            val id = (s.child("id").value as? Number)?.toLong() ?: s.key?.toLongOrNull() ?: return null
            val itemNameStr = (s.child("itemName").value ?: s.child("name").value ?: s.child("productName").value ?: s.child("porulPeyar").value) as? String ?: "{}"

            val companyIds = if (s.hasChild("companyIds") || s.hasChild("niruvanamIds")) {
                val rawVal = s.child("companyIds").value ?: s.child("niruvanamIds").value
                val parsed = ItemFirebasePayloadConverter.parseCompanyIds(rawVal)
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
                val parsed = ItemFirebasePayloadConverter.parseCompanyPrices(rawVal)
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

            ItemModel(
                id = id,
                name = LanguageJsonConverter.parse(itemNameStr),
                hsnCode = s.child("hsnCode").value as? String ?: "",
                price = (s.child("price").value as? Number)?.toDouble() ?: (s.child("vilai").value as? Number)?.toDouble() ?: 0.0,
                taxRate = (s.child("taxRate").value as? Number)?.toDouble() ?: (s.child("variVeetham").value as? Number)?.toDouble() ?: 0.0,
                quantityType = (s.child("quantityType").value ?: s.child("alavuVagai").value) as? String ?: "quantity",
                unit = (s.child("unit").value ?: s.child("alagu").value) as? String ?: "Nos",
                companyIds = companyIds,
                companyPrices = companyPrices,
                createdAt = (s.child("createdAt").value as? Number)?.toLong() ?: (System.currentTimeMillis() / 1000),
                updatedAt = (s.child("updatedAt").value as? Number)?.toLong() ?: (System.currentTimeMillis() / 1000),
                isDeleted = s.child("isDeleted").value as? Boolean ?: false,
                deletedAt = (s.child("deletedAt").value as? Number)?.toLong()
            )
        } catch (e: Exception) {
            Log.w(tag, "Failed to parse RTDB item: ${e.message}")
            null
        }
    }

    private fun snapshotToInvoice(s: DataSnapshot): InvoiceModel? {
        return try {
            val id = (s.child("id").value as? Number)?.toLong() ?: s.key?.toLongOrNull() ?: return null
            val customerNameStr = (s.child("customerName").value ?: s.child("vaangunarPeyar").value) as? String ?: "{}"
            val customerAddressStr = (s.child("customerAddress").value ?: s.child("vaangunarMunvari").value) as? String ?: "{}"

            InvoiceModel(
                id = id,
                companyId = ((s.child("companyId").value ?: s.child("niruvanamId").value) as? Number)?.toLong(),
                invoiceNumber = (s.child("invoiceNumber").value ?: s.child("patrucheettuEn").value) as? String ?: "",
                finYear = (s.child("finYear").value as? Number)?.toInt() ?: 0,
                vanakkam = (s.child("vanakkam").value as? Number)?.toInt() ?: 1,
                invoiceType = (s.child("invoiceType").value ?: s.child("pattiyalVagai").value) as? String ?: "tax-invoice",
                customerId = ((s.child("customerId").value ?: s.child("vaangunarId").value) as? Number)?.toLong(),
                customerName = LanguageJsonConverter.parse(customerNameStr),
                customerAddress = LanguageJsonConverter.parse(customerAddressStr),
                invoiceDate = ((s.child("invoiceDate").value ?: s.child("pattiyalNaal").value) as? Number)?.toLong() ?: (System.currentTimeMillis() / 1000),
                itemsJson = (s.child("itemsJson").value ?: s.child("tharavugal").value) as? String ?: "[]",
                totalAmount = ((s.child("totalAmount").value ?: s.child("mothaThogai").value) as? Number)?.toDouble() ?: 0.0,
                discount = ((s.child("discount").value ?: s.child("thallupadi").value) as? Number)?.toDouble() ?: 0.0,
                globalDiscountValue = ((s.child("globalDiscountValue").value ?: s.child("podhuThallupadiMathippu").value) as? Number)?.toDouble() ?: 0.0,
                globalDiscountType = (s.child("globalDiscountType").value ?: s.child("podhuThallupadiVagai").value) as? String ?: "%",
                globalDiscountAmount = ((s.child("globalDiscountAmount").value ?: s.child("podhuThallupadiThogai").value) as? Number)?.toDouble() ?: 0.0,
                taxAmount = ((s.child("taxAmount").value ?: s.child("variThogai").value) as? Number)?.toDouble() ?: 0.0,
                taxDataJson = (s.child("taxDataJson").value ?: s.child("variTharavugal").value) as? String ?: "{}",
                totalWeight = ((s.child("totalWeight").value ?: s.child("mothaEdai").value) as? Number)?.toDouble() ?: 0.0,
                wastageGrams = ((s.child("wastageGrams").value ?: s.child("setharamGrams").value) as? Number)?.toDouble() ?: 0.0,
                postalCharges = ((s.child("postalCharges").value ?: s.child("thabaalThogai").value) as? Number)?.toDouble() ?: 0.0,
                ahimsaSilkAmount = ((s.child("ahimsaSilkAmount").value ?: s.child("ahimsaPattuThogai").value) as? Number)?.toDouble() ?: 0.0,
                otherTaxesJson = (s.child("otherTaxesJson").value ?: s.child("piravariVugal").value) as? String ?: "[]",
                customPreferencesJson = (s.child("customPreferencesJson").value ?: s.child("sonthaViruppangal").value) as? String ?: "{}",
                termsAndConditions = (s.child("termsAndConditions").value ?: s.child("nibandhanaigal").value) as? String ?: "",
                notes = (s.child("notes").value ?: s.child("ullkurippu").value) as? String ?: "",
                bankDetailsJson = (s.child("bankDetailsJson").value ?: s.child("vangiTharavugal").value) as? String ?: "{}",
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
            val customerNameStr = (s.child("customerName").value ?: s.child("vaangunarPeyar").value) as? String ?: "{}"
            val customerAddressStr = (s.child("customerAddress").value ?: s.child("vaangunarMunvari").value) as? String ?: "{}"

            ReceiptModel(
                id = id,
                companyId = ((s.child("companyId").value ?: s.child("niruvanamId").value) as? Number)?.toLong(),
                receiptNumber = (s.child("receiptNumber").value ?: s.child("patruEn").value) as? String ?: "",
                finYear = s.child("finYear").value as? String ?: "",
                vanakkam = (s.child("vanakkam").value as? Number)?.toInt() ?: 1,
                customerId = ((s.child("customerId").value ?: s.child("vaangunarId").value) as? Number)?.toLong(),
                customerName = LanguageJsonConverter.parse(customerNameStr),
                customerAddress = LanguageJsonConverter.parse(customerAddressStr),
                receiptDate = ((s.child("receiptDate").value ?: s.child("patruNaal").value) as? Number)?.toLong() ?: (System.currentTimeMillis() / 1000),
                amount = ((s.child("amount").value ?: s.child("thogai").value) as? Number)?.toDouble() ?: 0.0,
                paymentMode = (s.child("paymentMode").value ?: s.child("seluthumMurai").value) as? String ?: "cash",
                bankName = (s.child("bankName").value ?: s.child("vangiPeyar").value) as? String,
                transactionNumber = (s.child("transactionNumber").value ?: s.child("parivarthanaiEn").value) as? String,
                notes = (s.child("notes").value ?: s.child("ullkurippu").value) as? String ?: "",
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
