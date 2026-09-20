package com.elvan.udukkai.core.backup

import android.util.Log
import com.elvan.udukkai.core.platform.AppContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AndroidNirilBackupService : NirilBackupService {

    private val tag = "NirilBackup"

    private fun getBackupDir(): File {
        val dir = File("/storage/emulated/0/Documents/Udukkai/backup")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun getBackupFile(): File = File(getBackupDir(), "udukkai_backup.db")
    private fun getSafetySnapshotFile(): File = File(getBackupDir(), "udukkai_safety_snapshot.db")

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
            checkpointWal(getCoolieDbPath())
            checkpointWal(getSilkDbPath())

            val filesToPack = getFilesToPack()
            val header = ByteBuffer.allocate(48).order(ByteOrder.LITTLE_ENDIAN)
            val fileBytesList = mutableListOf<ByteArray>()

            for (i in filesToPack.indices) {
                val file = File(filesToPack[i])
                if (file.exists() && file.canRead()) {
                    val bytes = file.readBytes()
                    header.putLong(bytes.size.toLong())
                    fileBytesList.add(bytes)
                } else {
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
            Log.i(tag, "Restore completed from ${sourceFile.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e(tag, "Unpack failed from ${sourceFile.absolutePath}: ${e.message}", e)
            false
        }
    }

    override fun createBackup(): Boolean = packToFile(getBackupFile())

    override fun restoreFromBackup(): Boolean = unpackFromFile(getBackupFile())

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

    // --- Copy 2: Protected Safety Vault (Anti-Erasure Guard) ---

    override fun createSafetySnapshot(): Boolean {
        val ok = packToFile(getSafetySnapshotFile())
        if (ok) {
            Log.i(tag, "Copy 2 Safety Snapshot created successfully at ${getSafetySnapshotFile().absolutePath}")
        }
        return ok
    }

    override fun restoreFromSafetySnapshot(): Boolean {
        val ok = unpackFromFile(getSafetySnapshotFile())
        if (ok) {
            Log.i(tag, "Restored from Copy 2 Safety Snapshot!")
        }
        return ok
    }

    override fun hasSafetySnapshot(): Boolean {
        return try {
            val f = getSafetySnapshotFile()
            f.exists() && f.length() > 48
        } catch (_: Exception) { false }
    }

    override fun getSafetySnapshotStats(): BackupStats? {
        return try {
            val f = getSafetySnapshotFile()
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

private val androidBackupInstance by lazy { AndroidNirilBackupService() }

actual fun getNirilBackupService(): NirilBackupService = androidBackupInstance
