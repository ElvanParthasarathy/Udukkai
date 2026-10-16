package com.elvan.udukkai.data

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

    fun getBackupFile(mode: AppMode? = null): File {
        val dir = getBackupDir()
        return when (mode) {
            AppMode.KOOLI -> File(dir, "udukkai_kooli_backup.db")
            AppMode.PATTU -> File(dir, "udukkai_pattu_backup.db")
            null -> File(dir, "udukkai_backup.db")
        }
    }

    fun resolveExistingBackupFile(mode: AppMode? = null): File {
        val modeFile = getBackupFile(mode)
        if (modeFile.exists()) return modeFile
        val legacyFile = getBackupFile(null)
        if (legacyFile.exists()) return legacyFile
        return modeFile
    }

    fun getSafetySnapshotFile(mode: AppMode? = null): File {
        val dir = getBackupDir()
        return when (mode) {
            AppMode.KOOLI -> File(dir, "udukkai_kooli_safety_snapshot.db")
            AppMode.PATTU -> File(dir, "udukkai_pattu_safety_snapshot.db")
            null -> File(dir, "udukkai_safety_snapshot.db")
        }
    }

    fun resolveExistingSafetySnapshotFile(mode: AppMode? = null): File {
        val modeFile = getSafetySnapshotFile(mode)
        if (modeFile.exists()) return modeFile
        val legacyFile = getSafetySnapshotFile(null)
        if (legacyFile.exists()) return legacyFile
        return modeFile
    }
}
