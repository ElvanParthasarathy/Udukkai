package com.elvan.udukkai.core.backup

import android.util.Log
import com.elvan.udukkai.core.mode.AppLaunchConfig
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.platform.AppContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AndroidNirilBackupService : NirilBackupService {

    private val tag = "NirilBackup"

    private fun currentMode(): AppMode? = AppLaunchConfig.fixedMode

    private fun getBackupDir(): File {
        val dir = File("/storage/emulated/0/Documents/Udukkai/backup")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun getBackupFile(mode: AppMode?): File {
        val dir = getBackupDir()
        return when (mode) {
            AppMode.KOOLI -> File(dir, "udukkai_kooli_backup.db")
            AppMode.PATTU -> File(dir, "udukkai_pattu_backup.db")
            null -> File(dir, "udukkai_backup.db")
        }
    }

    private fun getBackupFileForWriting(): File = getBackupFile(currentMode())

    private fun getBackupFileForReading(): File {
        val modeFile = getBackupFile(currentMode())
        if (modeFile.exists()) return modeFile
        val legacyFile = getBackupFile(null)
        if (legacyFile.exists()) return legacyFile
        return modeFile
    }

    private fun getSafetySnapshotFile(mode: AppMode?): File {
        val dir = getBackupDir()
        return when (mode) {
            AppMode.KOOLI -> File(dir, "udukkai_kooli_safety_snapshot.db")
            AppMode.PATTU -> File(dir, "udukkai_pattu_safety_snapshot.db")
            null -> File(dir, "udukkai_safety_snapshot.db")
        }
    }

    private fun getSafetySnapshotFileForWriting(): File = getSafetySnapshotFile(currentMode())

    private fun getSafetySnapshotFileForReading(): File {
        val modeFile = getSafetySnapshotFile(currentMode())
        if (modeFile.exists()) return modeFile
        val legacyFile = getSafetySnapshotFile(null)
        if (legacyFile.exists()) return legacyFile
        return modeFile
    }

    private fun getCoolieDbPath(): String {
        if (!AppContext.isInitialized) return ""
        return AppContext.context.getDatabasePath("udukkai_kooli.db").absolutePath
    }

    private fun getSilkDbPath(): String {
        if (!AppContext.isInitialized) return ""
        return AppContext.context.getDatabasePath("udukkai_pattu.db").absolutePath
    }

    private fun getFilesToPack(): List<String> {
        val coolie = getCoolieDbPath()
        val silk = getSilkDbPath()
        return listOf(
            coolie,
            "$coolie-wal",
            "$coolie-shm",
            silk,
            "$silk-wal",
            "$silk-shm"
        )
    }

    private fun checkpointWal(dbPath: String) {
        if (dbPath.isEmpty()) return
        try {
            val file = File(dbPath)
            if (!file.exists()) return
            val db = android.database.sqlite.SQLiteDatabase.openDatabase(
                dbPath, null, android.database.sqlite.SQLiteDatabase.OPEN_READWRITE
            )
            db.rawQuery("PRAGMA wal_checkpoint(TRUNCATE)", null)?.close()
            db.close()
        } catch (e: Exception) {
            Log.w(tag, "Checkpoint failed for $dbPath: ${e.message}")
        }
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

                if (shouldPack && filesToPack[i].isNotEmpty()) {
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
            Log.i(tag, "Packed to: ${targetFile.absolutePath} (${targetFile.length()} bytes)")
            true
        } catch (e: Exception) {
            Log.e(tag, "Pack failed for ${targetFile.absolutePath}: ${e.message}", e)
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
                if (filesToUnpack[i].isEmpty()) {
                    offset += size
                    continue
                }

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
            Log.i(tag, "Restore completed from ${sourceFile.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e(tag, "Unpack failed from ${sourceFile.absolutePath}: ${e.message}", e)
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
                if (path.isEmpty()) 0L else {
                    val f = File(path)
                    if (f.exists()) f.length() else 0L
                }
            }
        } catch (_: Exception) { 0L }
    }
}

private val androidBackupInstance by lazy { AndroidNirilBackupService() }

actual fun getNirilBackupService(): NirilBackupService = androidBackupInstance
