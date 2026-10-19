package com.elvan.udukkai.data

import com.elvan.udukkai.core.backup.BackupSlot
import com.elvan.udukkai.core.mode.AppMode
import java.io.File

object DesktopDbPaths {
    private val appDataDir: File by lazy {
        val appData = System.getenv("APPDATA")
        val base = if (!appData.isNullOrBlank()) {
            File(appData)
        } else {
            File(System.getProperty("user.home") ?: ".", "AppData/Roaming")
        }
        val dir = File(base, "Udukkai")
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    fun getDbFile(dbName: String): File {
        if (!appDataDir.exists()) appDataDir.mkdirs()
        return File(appDataDir, dbName)
    }

    fun getBackupDir(): File {
        val userHome = System.getProperty("user.home") ?: "."
        val dir = File(userHome, "Documents/Udukkai/backup")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun modePrefix(mode: AppMode?): String {
        return when (mode) {
            AppMode.KOOLI -> "kooli"
            AppMode.PATTU -> "pattu"
            null -> "common"
        }
    }

    fun getBackupFile(mode: AppMode? = null, slot: BackupSlot = BackupSlot.LATEST): File {
        val dir = getBackupDir()
        val prefix = modePrefix(mode)
        return when (slot) {
            BackupSlot.LATEST -> File(dir, "udukkai_${prefix}_backup_latest.db")
            BackupSlot.PREVIOUS -> File(dir, "udukkai_${prefix}_backup_previous.db")
            BackupSlot.SAFETY_VAULT -> File(dir, "udukkai_${prefix}_safety_vault.db")
        }
    }

    fun resolveExistingBackupFile(mode: AppMode? = null, slot: BackupSlot = BackupSlot.LATEST): File {
        val slotFile = getBackupFile(mode, slot)
        if (slotFile.exists() && slotFile.length() > 48) return slotFile

        // Fallbacks for LATEST: check legacy file names
        if (slot == BackupSlot.LATEST) {
            val legacyName = when (mode) {
                AppMode.KOOLI -> "udukkai_kooli_backup.db"
                AppMode.PATTU -> "udukkai_pattu_backup.db"
                null -> "udukkai_backup.db"
            }
            val legacyFile = File(getBackupDir(), legacyName)
            if (legacyFile.exists() && legacyFile.length() > 48) return legacyFile
        }

        // Fallbacks for SAFETY_VAULT: check legacy snapshot names
        if (slot == BackupSlot.SAFETY_VAULT) {
            val legacySafetyName = when (mode) {
                AppMode.KOOLI -> "udukkai_kooli_safety_snapshot.db"
                AppMode.PATTU -> "udukkai_pattu_safety_snapshot.db"
                null -> "udukkai_safety_snapshot.db"
            }
            val legacyFile = File(getBackupDir(), legacySafetyName)
            if (legacyFile.exists() && legacyFile.length() > 48) return legacyFile
        }

        return slotFile
    }

    fun getSafetySnapshotFile(mode: AppMode? = null): File =
        getBackupFile(mode, BackupSlot.SAFETY_VAULT)

    fun resolveExistingSafetySnapshotFile(mode: AppMode? = null): File =
        resolveExistingBackupFile(mode, BackupSlot.SAFETY_VAULT)
}
