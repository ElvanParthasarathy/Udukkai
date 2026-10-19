package com.elvan.udukkai.core.backup

import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.mode.ModeManager
import com.elvan.udukkai.data.repository.PattiyalRepository
import com.elvan.udukkai.data.repository.PatrugalRepository
import com.elvan.udukkai.data.repository.PorulRepository
import com.elvan.udukkai.data.repository.VaangunarRepository
import com.elvan.udukkai.data.settings.NiruvanaTharavugalRepository

/**
 * Cross-platform backup service interface.
 * Binary format: 48-byte header (6 x 8-byte little-endian longs) + raw file data.
 * Files packed: coolie.db, coolie.db-wal, coolie.db-shm, silk.db, silk.db-wal, silk.db-shm.
 * 100% compatible with Flutter's NirilBackupService binary format, enhanced with
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
    val mode: AppMode? = null,
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
 * Invalidate in-memory cached state across all repositories and reload from SQLite.
 * Must be invoked immediately after any database file restoration.
 */
fun reloadAllRepositories() {
    val mode = ModeManager.currentMode
    try { PattiyalRepository.loadAll(mode) } catch (_: Exception) {}
    try { PattiyalRepository.loadDeleted(mode) } catch (_: Exception) {}
    try { PatrugalRepository.loadAll(mode) } catch (_: Exception) {}
    try { PatrugalRepository.loadDeleted(mode) } catch (_: Exception) {}
    try { PorulRepository.loadAll(mode) } catch (_: Exception) {}
    try { PorulRepository.loadDeleted(mode) } catch (_: Exception) {}
    try { VaangunarRepository.loadAll(mode) } catch (_: Exception) {}
    try { VaangunarRepository.loadDeleted(mode) } catch (_: Exception) {}
    try { NiruvanaTharavugalRepository.getProfile(mode) } catch (_: Exception) {}
}

interface NirilBackupService {
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

expect fun getNirilBackupService(): NirilBackupService
