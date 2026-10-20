package com.elvan.udukkai.core.backup

/**
 * Cross-platform backup service interface.
 * Binary format: 48-byte header (6 x 8-byte little-endian longs) + raw file data.
 * Files packed: coolie.db, coolie.db-wal, coolie.db-shm, silk.db, silk.db-wal, silk.db-shm.
 * 100% compatible with Udukkai backup binary format, enhanced with
 * atomic writes, rolling 3-slot versioning (Latest, Previous, Safety Vault), and trailer metadata.
 */

enum class BackupSlot(val fileSuffix: String) {
    LATEST("latest"),
    PREVIOUS("previous"),
    SAFETY_VAULT("safety_vault")
}

data class BackupStats(
    val lastModified: Long,
    val sizeBytes: Long
)

data class BackupItemMetadata(
    val slot: BackupSlot,
    val lastModified: Long,
    val sizeBytes: Long,
    val invoiceCount: Int = 0,
    val receiptCount: Int = 0,
    val customerCount: Int = 0,
    val productCount: Int = 0,
    val mode: String? = null,
    val isSafetySnapshot: Boolean = false,
    val isValid: Boolean = true
)

data class DatabaseLiveStats(
    val totalDbSizeBytes: Long,
    val invoiceCount: Int,
    val receiptCount: Int,
    val customerCount: Int,
    val productCount: Int
)

/**
 * Invalidate in-memory cached state for Factory repositories and reload from SQLite.
 */
fun reloadFactoryRepositories() {
    try { com.elvan.udukkai.factory.data.FactoryInvoiceRepository.loadAll() } catch (_: Exception) {}
    try { com.elvan.udukkai.factory.data.FactoryInvoiceRepository.loadDeleted() } catch (_: Exception) {}
    try { com.elvan.udukkai.factory.data.FactoryReceiptRepository.loadAll() } catch (_: Exception) {}
    try { com.elvan.udukkai.factory.data.FactoryReceiptRepository.loadDeleted() } catch (_: Exception) {}
    try { com.elvan.udukkai.factory.data.FactoryProductRepository.loadAll() } catch (_: Exception) {}
    try { com.elvan.udukkai.factory.data.FactoryProductRepository.loadDeleted() } catch (_: Exception) {}
    try { com.elvan.udukkai.factory.data.FactoryCustomerRepository.loadAll() } catch (_: Exception) {}
    try { com.elvan.udukkai.factory.data.FactoryCustomerRepository.loadDeleted() } catch (_: Exception) {}
    try { com.elvan.udukkai.factory.data.FactoryProfileRepository.refreshFromDatabase() } catch (_: Exception) {}
}

/**
 * Invalidate in-memory cached state for Textiles repositories and reload from SQLite.
 */
fun reloadTextilesRepositories() {
    try { com.elvan.udukkai.textiles.data.TextilesInvoiceRepository.loadAll() } catch (_: Exception) {}
    try { com.elvan.udukkai.textiles.data.TextilesInvoiceRepository.loadDeleted() } catch (_: Exception) {}
    try { com.elvan.udukkai.textiles.data.TextilesReceiptRepository.loadAll() } catch (_: Exception) {}
    try { com.elvan.udukkai.textiles.data.TextilesReceiptRepository.loadDeleted() } catch (_: Exception) {}
    try { com.elvan.udukkai.textiles.data.TextilesProductRepository.loadAll() } catch (_: Exception) {}
    try { com.elvan.udukkai.textiles.data.TextilesProductRepository.loadDeleted() } catch (_: Exception) {}
    try { com.elvan.udukkai.textiles.data.TextilesCustomerRepository.loadAll() } catch (_: Exception) {}
    try { com.elvan.udukkai.textiles.data.TextilesCustomerRepository.loadDeleted() } catch (_: Exception) {}
    try { com.elvan.udukkai.textiles.data.TextilesProfileRepository.refreshFromDatabase() } catch (_: Exception) {}
}

/**
 * Backwards-compatible reload dispatch.
 */
fun reloadAllRepositories() {
    reloadFactoryRepositories()
    reloadTextilesRepositories()
}

interface UdukkaiBackupService {
    /**
     * Performs an atomic backup to [BackupSlot.LATEST].
     * Automatically rotates previous latest to [BackupSlot.PREVIOUS].
     */
    fun createBackup(): Boolean

    /**
     * Creates an untouchable Safety Vault snapshot before dangerous operations (e.g. restore, migration).
     */
    fun createSafetySnapshot(): Boolean

    /**
     * Restores data from the specified slot (or defaults to LATEST).
     * Automatically creates a Safety Snapshot of the current state BEFORE restoring.
     * After restoring, refreshes in-memory repositories.
     */
    fun restoreFromSlot(slot: BackupSlot): Boolean
    fun restoreFromBackup(): Boolean = restoreFromSlot(BackupSlot.LATEST)
    fun restoreFromSafetySnapshot(): Boolean = restoreFromSlot(BackupSlot.SAFETY_VAULT)

    fun hasBackup(slot: BackupSlot = BackupSlot.LATEST): Boolean
    fun hasSafetySnapshot(): Boolean = hasBackup(BackupSlot.SAFETY_VAULT)

    fun deleteBackup(slot: BackupSlot = BackupSlot.LATEST): Boolean

    fun getBackupStats(): BackupStats? = getBackupStats(BackupSlot.LATEST)
    fun getBackupStats(slot: BackupSlot): BackupStats?
    fun getSafetySnapshotStats(): BackupStats? = getBackupStats(BackupSlot.SAFETY_VAULT)

    fun getBackupMetadata(slot: BackupSlot): BackupItemMetadata?
    fun getAllAvailableBackups(): List<BackupItemMetadata>

    fun getTotalDatabaseSize(): Long
    fun getLiveStats(): DatabaseLiveStats

    /**
     * Export / Share the backup file.
     * Returns true if share dialog was successfully launched.
     */
    fun shareBackup(slot: BackupSlot = BackupSlot.LATEST): Boolean
}

@Deprecated("Use UdukkaiBackupService instead", ReplaceWith("UdukkaiBackupService"))
typealias NirilBackupService = UdukkaiBackupService

expect fun getUdukkaiBackupService(): UdukkaiBackupService

@Deprecated("Use getUdukkaiBackupService instead", ReplaceWith("getUdukkaiBackupService()"))
fun getNirilBackupService(): UdukkaiBackupService = getUdukkaiBackupService()
