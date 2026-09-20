package com.elvan.udukkai.core.backup

/**
 * Cross-platform backup service interface.
 * Binary format: 48-byte header (6 x 8-byte little-endian longs) + raw file data.
 * Files packed: coolie.db, coolie.db-wal, coolie.db-shm, silk.db, silk.db-wal, silk.db-shm.
 * 100% compatible with Flutter's NirilBackupService binary format.
 */

data class BackupStats(
    val lastModified: Long,
    val sizeBytes: Long
)

interface NirilBackupService {
    fun createBackup(): Boolean
    fun restoreFromBackup(): Boolean
    fun hasBackup(): Boolean
    fun deleteBackup(): Boolean
    fun getBackupStats(): BackupStats?
    fun getTotalDatabaseSize(): Long
}

expect fun getNirilBackupService(): NirilBackupService
