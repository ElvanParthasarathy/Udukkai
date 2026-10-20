package com.elvan.udukkai.data

import com.elvan.udukkai.core.backup.BackupSlot
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

    private fun scopePrefix(scope: String?): String {
        return when (scope?.lowercase()) {
            "textiles", "pattu", "silk" -> "textiles"
            else -> "factory"
        }
    }

    fun getBackupFile(scope: String = "factory", slot: BackupSlot = BackupSlot.LATEST): File {
        val dir = getBackupDir()
        val prefix = scopePrefix(scope)
        return when (slot) {
            BackupSlot.LATEST -> File(dir, "udukkai_${prefix}_backup_latest.db")
            BackupSlot.PREVIOUS -> File(dir, "udukkai_${prefix}_backup_previous.db")
            BackupSlot.SAFETY_VAULT -> File(dir, "udukkai_${prefix}_safety_vault.db")
        }
    }

    fun resolveExistingBackupFile(scope: String = "factory", slot: BackupSlot = BackupSlot.LATEST): File {
        val slotFile = getBackupFile(scope, slot)
        if (slotFile.exists() && slotFile.length() > 48) return slotFile

        val prefix = scopePrefix(scope)
        // Fallbacks for LATEST: check legacy file names
        if (slot == BackupSlot.LATEST) {
            val legacyNames = if (prefix == "textiles") {
                listOf("udukkai_textiles_backup.db", "udukkai_pattu_backup.db")
            } else {
                listOf("udukkai_factory_backup.db", "udukkai_kooli_backup.db")
            }
            for (legacyName in legacyNames) {
                val legacyFile = File(getBackupDir(), legacyName)
                if (legacyFile.exists() && legacyFile.length() > 48) return legacyFile
            }
        }

        // Fallbacks for SAFETY_VAULT: check legacy snapshot names
        if (slot == BackupSlot.SAFETY_VAULT) {
            val legacySafetyNames = if (prefix == "textiles") {
                listOf("udukkai_textiles_safety_snapshot.db", "udukkai_pattu_safety_snapshot.db")
            } else {
                listOf("udukkai_factory_safety_snapshot.db", "udukkai_kooli_safety_snapshot.db")
            }
            for (legacySafetyName in legacySafetyNames) {
                val legacyFile = File(getBackupDir(), legacySafetyName)
                if (legacyFile.exists() && legacyFile.length() > 48) return legacyFile
            }
        }

        return slotFile
    }

    fun getSafetySnapshotFile(scope: String = "factory"): File =
        getBackupFile(scope, BackupSlot.SAFETY_VAULT)

    fun resolveExistingSafetySnapshotFile(scope: String = "factory"): File =
        resolveExistingBackupFile(scope, BackupSlot.SAFETY_VAULT)
}
