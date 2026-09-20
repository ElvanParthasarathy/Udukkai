package com.elvan.udukkai.core.backup

import com.elvan.udukkai.data.DesktopDbPaths
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.sql.DriverManager

class DesktopNirilBackupService : NirilBackupService {

    private fun getBackupDir(): File = DesktopDbPaths.getBackupDir()

    private fun getBackupFile(): File = DesktopDbPaths.getBackupFile()

    private fun getCoolieDbPath(): String = DesktopDbPaths.getDbFile("udukkai_kooli.db").absolutePath

    private fun getSilkDbPath(): String = DesktopDbPaths.getDbFile("udukkai_pattu.db").absolutePath

    private fun getFilesToPack(): List<String> {
        val coolie = getCoolieDbPath()
        val silk = getSilkDbPath()
        return listOf(coolie, "$coolie-wal", "$coolie-shm", silk, "$silk-wal", "$silk-shm")
    }

    private fun checkpointWal(dbPath: String) {
        try {
            val file = File(dbPath)
            if (!file.exists()) return
            Class.forName("org.sqlite.JDBC")
            DriverManager.getConnection("jdbc:sqlite:$dbPath").use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.execute("PRAGMA wal_checkpoint(TRUNCATE)")
                }
            }
        } catch (_: Exception) {}
    }

    override fun createBackup(): Boolean {
        return try {
            checkpointWal(getCoolieDbPath())
            checkpointWal(getSilkDbPath())

            val filesToPack = getFilesToPack()
            val header = ByteBuffer.allocate(48).order(ByteOrder.LITTLE_ENDIAN)
            val fileBytesList = mutableListOf<ByteArray>()

            for (path in filesToPack) {
                val file = File(path)
                if (file.exists() && file.canRead()) {
                    val bytes = file.readBytes()
                    header.putLong(bytes.size.toLong())
                    fileBytesList.add(bytes)
                } else {
                    header.putLong(0L)
                    fileBytesList.add(ByteArray(0))
                }
            }

            val backupFile = getBackupFile()
            backupFile.parentFile?.mkdirs()
            FileOutputStream(backupFile).use { fos ->
                fos.write(header.array())
                for (bytes in fileBytesList) {
                    if (bytes.isNotEmpty()) fos.write(bytes)
                }
            }
            true
        } catch (e: Exception) {
            println("Desktop backup failed: ${e.message}")
            false
        }
    }

    override fun restoreFromBackup(): Boolean {
        return try {
            val backupFile = getBackupFile()
            if (!backupFile.exists() || backupFile.length() < 48) return false

            val allBytes = backupFile.readBytes()
            val header = ByteBuffer.wrap(allBytes, 0, 48).order(ByteOrder.LITTLE_ENDIAN)
            var offset = 48

            val filesToUnpack = getFilesToPack()
            for (i in filesToUnpack.indices) {
                val size = header.getLong().toInt()
                val file = File(filesToUnpack[i])
                if (size > 0) {
                    file.parentFile?.mkdirs()
                    FileOutputStream(file).use { fos ->
                        fos.write(allBytes, offset, size)
                    }
                    offset += size
                } else {
                    if (file.exists()) file.delete()
                }
            }
            true
        } catch (e: Exception) {
            println("Desktop restore failed: ${e.message}")
            false
        }
    }

    override fun hasBackup(): Boolean {
        return try {
            val f = getBackupFile()
            f.exists() && f.length() > 48
        } catch (_: Exception) { false }
    }

    override fun deleteBackup(): Boolean {
        return try {
            val f = getBackupFile()
            if (f.exists()) f.delete() else true
        } catch (_: Exception) { false }
    }

    override fun getBackupStats(): BackupStats? {
        return try {
            val f = getBackupFile()
            if (f.exists()) BackupStats(f.lastModified(), f.length()) else null
        } catch (_: Exception) { null }
    }

    override fun getTotalDatabaseSize(): Long {
        return try {
            getFilesToPack().sumOf { path ->
                val f = File(path)
                if (f.exists()) f.length() else 0L
            }
        } catch (_: Exception) { 0L }
    }
}

private val desktopBackupInstance by lazy { DesktopNirilBackupService() }

actual fun getNirilBackupService(): NirilBackupService = desktopBackupInstance
