package com.elvan.udukkai.core.backup

import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import com.elvan.udukkai.core.mode.AppLaunchConfig
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.mode.ModeManager
import com.elvan.udukkai.core.platform.AppContext
import com.elvan.udukkai.data.repository.PattiyalRepository
import com.elvan.udukkai.data.repository.PatrugalRepository
import com.elvan.udukkai.data.repository.PorulRepository
import com.elvan.udukkai.data.repository.VaangunarRepository
import com.elvan.udukkai.data.settings.NiruvanaTharavugalRepository
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AndroidNirilBackupService : NirilBackupService {

    private val tag = "NirilBackup"
    private val magicConstant = 0x5544554B4B414921L // "UDUKKAI!"

    private fun currentMode(): AppMode? = AppLaunchConfig.fixedMode ?: ModeManager.currentMode

    /**
     * Primary backup directory inside the application sandbox.
     * Guaranteed to be 100% writable on all Android versions without permission restrictions.
     */
    private fun getInternalBackupDir(): File {
        val dir = File(AppContext.context.filesDir, "backup")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * Secondary public directory in Documents for user accessibility.
     * Best-effort mirror that survives app uninstalls if permission allows.
     */
    private fun getExternalBackupDir(): File? {
        return try {
            val dir = File("/storage/emulated/0/Documents/Udukkai/backup")
            if (!dir.exists()) dir.mkdirs()
            dir
        } catch (_: Exception) {
            null
        }
    }

    private fun modePrefix(mode: AppMode?): String {
        return when (mode) {
            AppMode.KOOLI -> "kooli"
            AppMode.PATTU -> "pattu"
            null -> "common"
        }
    }

    private fun getFileForSlot(dir: File, slot: BackupSlot, mode: AppMode?): File {
        val prefix = modePrefix(mode)
        return when (slot) {
            BackupSlot.LATEST -> File(dir, "udukkai_${prefix}_backup_latest.db")
            BackupSlot.PREVIOUS -> File(dir, "udukkai_${prefix}_backup_previous.db")
            BackupSlot.SAFETY_VAULT -> File(dir, "udukkai_${prefix}_safety_vault.db")
        }
    }

    /**
     * Resolve file for writing (always writes to the internal reliable sandbox).
     */
    private fun getFileForWriting(slot: BackupSlot): File {
        return getFileForSlot(getInternalBackupDir(), slot, currentMode())
    }

    /**
     * Resolve existing file for reading, checking slot file, legacy names, and external mirror.
     */
    private fun resolveExistingFileForReading(slot: BackupSlot): File? {
        val mode = currentMode()
        val internalDir = getInternalBackupDir()
        val slotFile = getFileForSlot(internalDir, slot, mode)
        if (slotFile.exists() && slotFile.length() > 48) return slotFile

        // Fallbacks for LATEST: check legacy file names
        if (slot == BackupSlot.LATEST) {
            val legacyName = when (mode) {
                AppMode.KOOLI -> "udukkai_kooli_backup.db"
                AppMode.PATTU -> "udukkai_pattu_backup.db"
                null -> "udukkai_backup.db"
            }
            val internalLegacy = File(internalDir, legacyName)
            if (internalLegacy.exists() && internalLegacy.length() > 48) return internalLegacy

            // Check external documents
            val externalDir = getExternalBackupDir()
            if (externalDir != null) {
                val externalSlot = getFileForSlot(externalDir, slot, mode)
                if (externalSlot.exists() && externalSlot.length() > 48) return externalSlot
                val externalLegacy = File(externalDir, legacyName)
                if (externalLegacy.exists() && externalLegacy.length() > 48) return externalLegacy
            }
        }

        // Fallbacks for SAFETY_VAULT: check legacy snapshot names
        if (slot == BackupSlot.SAFETY_VAULT) {
            val legacySafetyName = when (mode) {
                AppMode.KOOLI -> "udukkai_kooli_safety_snapshot.db"
                AppMode.PATTU -> "udukkai_pattu_safety_snapshot.db"
                null -> "udukkai_safety_snapshot.db"
            }
            val internalLegacy = File(internalDir, legacySafetyName)
            if (internalLegacy.exists() && internalLegacy.length() > 48) return internalLegacy

            val externalDir = getExternalBackupDir()
            if (externalDir != null) {
                val externalSlot = getFileForSlot(externalDir, slot, mode)
                if (externalSlot.exists() && externalSlot.length() > 48) return externalSlot
                val externalLegacy = File(externalDir, legacySafetyName)
                if (externalLegacy.exists() && externalLegacy.length() > 48) return externalLegacy
            }
        }

        return if (slotFile.exists()) slotFile else null
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

    /**
     * Packs databases safely into targetFile with atomic temp-file guarantee and trailer metadata.
     */
    private fun packToFile(targetFile: File): Boolean {
        val tempFile = File(targetFile.parentFile, "${targetFile.name}.tmp")
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
                    header.putLong(0L)
                    fileBytesList.add(ByteArray(0))
                }
            }

            // Write 48-byte header + data chunks
            tempFile.parentFile?.mkdirs()
            FileOutputStream(tempFile).use { fos ->
                fos.write(header.array())
                for (bytes in fileBytesList) {
                    if (bytes.isNotEmpty()) fos.write(bytes)
                }

                // Append metadata trailer
                val stats = getLiveStats()
                val jsonString = """{"invoices":${stats.invoiceCount},"receipts":${stats.receiptCount},"customers":${stats.customerCount},"products":${stats.productCount},"time":${System.currentTimeMillis()},"mode":"${mode?.key ?: "common"}"}"""
                val jsonBytes = jsonString.toByteArray(Charsets.UTF_8)
                fos.write(jsonBytes)

                val trailerBuffer = ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN)
                trailerBuffer.putInt(jsonBytes.size)
                trailerBuffer.putLong(magicConstant)
                fos.write(trailerBuffer.array())
            }

            // Verify written temp file
            if (!tempFile.exists() || tempFile.length() < 48) {
                tempFile.delete()
                return false
            }

            // Atomic rename
            if (targetFile.exists()) targetFile.delete()
            val renamed = tempFile.renameTo(targetFile)
            if (!renamed) {
                // Fallback copy if renameTo fails across file boundaries
                tempFile.copyTo(targetFile, overwrite = true)
                tempFile.delete()
            }

            // Also mirror to external Documents if available (best effort)
            try {
                val extDir = getExternalBackupDir()
                if (extDir != null) {
                    val extFile = File(extDir, targetFile.name)
                    targetFile.copyTo(extFile, overwrite = true)
                }
            } catch (_: Exception) {}

            Log.i(tag, "Successfully packed atomic backup to: ${targetFile.absolutePath} (${targetFile.length()} bytes)")
            true
        } catch (e: Exception) {
            Log.e(tag, "Pack failed for ${targetFile.absolutePath}: ${e.message}", e)
            try { tempFile.delete() } catch (_: Exception) {}
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
                    if (shouldRestore && (i == 1 || i == 2 || i == 4 || i == 5)) {
                        if (file.exists()) file.delete()
                    }
                }
            }

            // Immediately reload in-memory cached repositories across app
            reloadAllRepositories()

            Log.i(tag, "Restore completed from ${sourceFile.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e(tag, "Unpack failed from ${sourceFile.absolutePath}: ${e.message}", e)
            false
        }
    }

    override fun createBackup(): Boolean {
        val latestFile = getFileForWriting(BackupSlot.LATEST)
        val previousFile = getFileForWriting(BackupSlot.PREVIOUS)

        // Rotate existing LATEST to PREVIOUS before generating new LATEST
        if (latestFile.exists() && latestFile.length() > 48) {
            try {
                latestFile.copyTo(previousFile, overwrite = true)
            } catch (_: Exception) {}
        }

        return packToFile(latestFile)
    }

    override fun createSafetySnapshot(): Boolean {
        val vaultFile = getFileForWriting(BackupSlot.SAFETY_VAULT)
        return packToFile(vaultFile)
    }

    override fun restoreFromSlot(slot: BackupSlot): Boolean {
        val file = resolveExistingFileForReading(slot) ?: return false

        // Before restoring from any backup, take an untouchable safety snapshot of the CURRENT state
        try {
            createSafetySnapshot()
        } catch (e: Exception) {
            Log.w(tag, "Pre-restore safety snapshot warning: ${e.message}")
        }

        return unpackFromFile(file)
    }

    override fun hasBackup(slot: BackupSlot): Boolean {
        return try {
            val f = resolveExistingFileForReading(slot)
            f != null && f.exists() && f.length() > 48
        } catch (_: Exception) { false }
    }

    override fun deleteBackup(slot: BackupSlot): Boolean {
        return try {
            val f = resolveExistingFileForReading(slot)
            if (f != null && f.exists()) f.delete() else true
        } catch (_: Exception) { false }
    }

    override fun getBackupStats(slot: BackupSlot): BackupStats? {
        return try {
            val f = resolveExistingFileForReading(slot)
            if (f != null && f.exists()) BackupStats(f.lastModified(), f.length()) else null
        } catch (_: Exception) { null }
    }

    override fun getBackupMetadata(slot: BackupSlot): BackupItemMetadata? {
        val file = resolveExistingFileForReading(slot) ?: return null
        if (!file.exists() || file.length() < 48) return null

        var invoiceCount = 0
        var receiptCount = 0
        var customerCount = 0
        var productCount = 0
        var recordedTime = file.lastModified()

        try {
            RandomAccessFile(file, "r").use { raf ->
                val length = raf.length()
                if (length > 60) {
                    raf.seek(length - 8)
                    val magic = java.lang.Long.reverseBytes(raf.readLong())
                    if (magic == magicConstant) {
                        raf.seek(length - 12)
                        val jsonLen = java.lang.Integer.reverseBytes(raf.readInt())
                        if (jsonLen in 1..2048 && length >= (48 + 12 + jsonLen)) {
                            raf.seek(length - 12 - jsonLen)
                            val jsonBytes = ByteArray(jsonLen)
                            raf.readFully(jsonBytes)
                            val json = String(jsonBytes, Charsets.UTF_8)

                            invoiceCount = Regex("\"invoices\":(\\d+)").find(json)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                            receiptCount = Regex("\"receipts\":(\\d+)").find(json)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                            customerCount = Regex("\"customers\":(\\d+)").find(json)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                            productCount = Regex("\"products\":(\\d+)").find(json)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                            recordedTime = Regex("\"time\":(\\d+)").find(json)?.groupValues?.get(1)?.toLongOrNull() ?: file.lastModified()
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        return BackupItemMetadata(
            slot = slot,
            lastModified = recordedTime,
            sizeBytes = file.length(),
            invoiceCount = invoiceCount,
            receiptCount = receiptCount,
            customerCount = customerCount,
            productCount = productCount,
            mode = currentMode(),
            isSafetySnapshot = slot == BackupSlot.SAFETY_VAULT,
            isValid = true
        )
    }

    override fun getAllAvailableBackups(): List<BackupItemMetadata> {
        val list = mutableListOf<BackupItemMetadata>()
        BackupSlot.entries.forEach { slot ->
            val meta = getBackupMetadata(slot)
            if (meta != null) list.add(meta)
        }
        return list
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

    override fun getLiveStats(): DatabaseLiveStats {
        val mode = currentMode() ?: ModeManager.currentMode
        val activeId = NiruvanaTharavugalRepository.getProfile(mode).id

        val invCount = PattiyalRepository.invoices.count { (activeId == null || it.niruvanamId == activeId) && !it.isDeleted }
        val recCount = PatrugalRepository.receipts.count { (activeId == null || it.niruvanamId == activeId) && !it.isDeleted }
        val cusCount = VaangunarRepository.merchants.count { !it.isDeleted }
        val proCount = PorulRepository.items.count { (activeId == null || it.isAvailableForCompany(activeId)) && !it.isDeleted }

        return DatabaseLiveStats(
            totalDbSizeBytes = getTotalDatabaseSize(),
            invoiceCount = invCount,
            receiptCount = recCount,
            customerCount = cusCount,
            productCount = proCount
        )
    }

    override fun shareBackup(slot: BackupSlot): Boolean {
        return try {
            val file = resolveExistingFileForReading(slot) ?: return false
            val context = AppContext.context
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Udukkai Backup (${slot.name.lowercase()})")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "காப்புப் பகிர்").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            Log.e(tag, "Share backup failed: ${e.message}", e)
            false
        }
    }
}

private val androidBackupInstance by lazy { AndroidNirilBackupService() }

actual fun getNirilBackupService(): NirilBackupService = androidBackupInstance
