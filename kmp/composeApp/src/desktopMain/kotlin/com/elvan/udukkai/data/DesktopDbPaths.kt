package com.elvan.udukkai.data

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

    fun getBackupFile(): File = File(getBackupDir(), "udukkai_backup.db")
}
