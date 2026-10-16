package com.elvan.udukkai.core.backup

import com.elvan.udukkai.core.mode.AppLaunchConfig
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.mode.ModeManager
import com.elvan.udukkai.data.DesktopDbPaths
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.sql.DriverManager

class DesktopNirilBackupService : NirilBackupService {

    private fun currentMode(): AppMode? = AppLaunchConfig.fixedMode

    private fun getBackupDir(): File = DesktopDbPaths.getBackupDir()

    private fun getBackupFileForWriting(): File =
        DesktopDbPaths.getBackupFile(currentMode())

    private fun getBackupFileForReading(): File =
        DesktopDbPaths.resolveExistingBackupFile(currentMode())

    private fun getSafetySnapshotFileForWriting(): File =
        DesktopDbPaths.getSafetySnapshotFile(currentMode())

    private fun getSafetySnapshotFileForReading(): File =
        DesktopDbPaths.resolveExistingSafetySnapshotFile(currentMode())

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

    private fun packToFile(targetFile: File): Boolean {
        return try {
            val mode = currentMode()
            if (mode == null || mode == AppMode.KOOLI) {
                checkpointWal(getCoolieDbPath())
            }
            if (mode == null || mode == AppMode.PATTU) {
                checkpointWal(getSilkDbPath())
            }

            val filesToPack = getFilesToPack()
            val header = ByteBuffer.allocate(48).order(ByteOrder.LITTLE_ENDIAN)
            val fileBytesList = mutableListOf<ByteArray>()

            for (i in filesToPack.indices) {
                val shouldPack = when (mode) {
                    AppMode.KOOLI -> i < 3       // pack only Kooli chunks 0, 1, 2
                    AppMode.PATTU -> i >= 3      // pack only Silk chunks 3, 4, 5
                    null -> true                 // pack all 6 chunks in combined mode
                }

                if (shouldPack) {
                    val file = File(filesToPack[i])
                    if (file.exists() && file.canRead()) {
                        val bytes = file.readBytes()
                        header.putLong(bytes.size.toLong())
                        fileBytesList.add(bytes)
                    } else {
                        header.putLong(0L)
                        fileBytesList.add(ByteArray(0))
                    }
                } else {
                    // For the other mode, store 0L in header and 0 bytes
                    header.putLong(0L)
                    fileBytesList.add(ByteArray(0))
                }
            }

            targetFile.parentFile?.mkdirs()
            FileOutputStream(targetFile).use { fos ->
                fos.write(header.array())
                for (bytes in fileBytesList) {
                    if (bytes.isNotEmpty()) fos.write(bytes)
                }
            }
            true
        } catch (e: Exception) {
            println("Desktop pack failed: ${e.message}")
            false
        }
    }

    private fun unpackFromFile(sourceFile: File): Boolean {
        return try {
            if (!sourceFile.exists() || sourceFile.length() < 48) return false

            val allBytes = sourceFile.readBytes()
            val header = ByteBuffer.wrap(allBytes, 0, 48).order(ByteOrder.LITTLE_ENDIAN)
            var offset = 48

            val filesToUnpack = getFilesToPack()
            val mode = currentMode()

            for (i in filesToUnpack.indices) {
                val size = header.getLong().toInt()
                val file = File(filesToUnpack[i])

                val shouldRestore = when (mode) {
                    AppMode.KOOLI -> i < 3       // restore only Kooli chunks 0, 1, 2
                    AppMode.PATTU -> i >= 3      // restore only Silk chunks 3, 4, 5
                    null -> true                 // restore all chunks in combined mode
                }

                if (size > 0) {
                    if (shouldRestore) {
                        file.parentFile?.mkdirs()
                        FileOutputStream(file).use { fos ->
                            fos.write(allBytes, offset, size)
                        }
                    }
                    offset += size
                } else {
                    // CRITICAL: NEVER delete files of the other mode!
                    // Only clean up WAL/SHM of the active mode if size is 0
                    if (shouldRestore && (i == 1 || i == 2 || i == 4 || i == 5)) {
                        if (file.exists()) file.delete()
                    }
                }
            }
            true
        } catch (e: Exception) {
            println("Desktop unpack failed: ${e.message}")
            false
        }
    }

    override fun createBackup(): Boolean = packToFile(getBackupFileForWriting())

    override fun restoreFromBackup(): Boolean = unpackFromFile(getBackupFileForReading())

    override fun hasBackup(): Boolean {
        return try {
            val f = getBackupFileForReading()
            f.exists() && f.length() > 48
        } catch (_: Exception) { false }
    }

    override fun deleteBackup(): Boolean {
        return try {
            val f = getBackupFileForWriting()
            if (f.exists()) f.delete() else true
        } catch (_: Exception) { false }
    }

    override fun getBackupStats(): BackupStats? {
        return try {
            val f = getBackupFileForReading()
            if (f.exists()) BackupStats(f.lastModified(), f.length()) else null
        } catch (_: Exception) { null }
    }

    override fun createSafetySnapshot(): Boolean = packToFile(getSafetySnapshotFileForWriting())

    override fun restoreFromSafetySnapshot(): Boolean = unpackFromFile(getSafetySnapshotFileForReading())

    override fun hasSafetySnapshot(): Boolean {
        return try {
            val f = getSafetySnapshotFileForReading()
            f.exists() && f.length() > 48
        } catch (_: Exception) { false }
    }

    override fun getSafetySnapshotStats(): BackupStats? {
        return try {
            val f = getSafetySnapshotFileForReading()
            if (f.exists()) BackupStats(f.lastModified(), f.length()) else null
        } catch (_: Exception) { null }
    }

    override fun getTotalDatabaseSize(): Long {
        return try {
            val mode = currentMode()
            getFilesToPack().filterIndexed { index, _ ->
                when (mode) {
                    AppMode.KOOLI -> index < 3
                    AppMode.PATTU -> index >= 3
                    null -> true
                }
            }.sumOf { path ->
                val f = File(path)
                if (f.exists()) f.length() else 0L
            }
        } catch (_: Exception) { 0L }
    }
}

private val desktopBackupInstance by lazy { DesktopNirilBackupService() }

actual fun getNirilBackupService(): NirilBackupService = desktopBackupInstance
