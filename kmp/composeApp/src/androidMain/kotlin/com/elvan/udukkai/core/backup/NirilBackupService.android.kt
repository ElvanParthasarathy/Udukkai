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
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.CRC32

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
     * Replaces destination with source atomically without deleting destination first.
     */
    private fun atomicReplace(source: File, destination: File): Boolean {
        destination.parentFile?.mkdirs()
        try {
            Files.move(
                source.toPath(),
                destination.toPath(),
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING
            )
            return true
        } catch (_: AtomicMoveNotSupportedException) {
            try {
                Files.move(
                    source.toPath(),
                    destination.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
                return true
            } catch (e: Exception) {
                Log.e(tag, "Files.move fallback failed: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(tag, "Atomic move failed: ${e.message}")
        }

        return try {
            if (source.renameTo(destination)) {
                true
            } else {
                source.copyTo(destination, overwrite = true)
                source.delete()
                true
            }
        } catch (e: Exception) {
            Log.e(tag, "Fallback replace failed: ${e.message}")
            false
        }
    }

    private fun validateLegacyArchive(file: File): Boolean {
        if (!file.exists() || file.length() < 60) return false
        return try {
            RandomAccessFile(file, "r").use { raf ->
                val len = raf.length()
                if (len < 60) return false
                raf.seek(len - 8)
                val magic = java.lang.Long.reverseBytes(raf.readLong())
                if (magic != magicConstant) return false

                raf.seek(len - 12)
                val jsonLen = java.lang.Integer.reverseBytes(raf.readInt())
                if (jsonLen !in 1..65536 || len < (48 + 12 + jsonLen)) return false

                raf.seek(0)
                var sum = 0L
                for (i in 0 until 6) {
                    val clen = java.lang.Long.reverseBytes(raf.readLong())
                    if (clen < 0) return false
                    sum += clen
                }
                len == (48L + sum + jsonLen + 12L)
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Validates archive integrity: magic constants, version, total size, trailer, and CRC32 payload checksum.
     */
    private fun validateArchive(file: File): Boolean {
        if (!file.exists() || !file.isFile || file.length() < 124) {
            return validateLegacyArchive(file)
        }

        return try {
            RandomAccessFile(file, "r").use { raf ->
                val fileLen = raf.length()

                // Check v1 header magic
                raf.seek(0)
                val headerMagic = java.lang.Long.reverseBytes(raf.readLong())
                if (headerMagic != magicConstant) {
                    return validateLegacyArchive(file)
                }

                val version = java.lang.Integer.reverseBytes(raf.readInt())
                if (version != 1) return false

                val modeFlag = java.lang.Integer.reverseBytes(raf.readInt())
                val timestamp = java.lang.Long.reverseBytes(raf.readLong())
                val expectedCrc = java.lang.Long.reverseBytes(raf.readLong())
                val chunkCount = java.lang.Integer.reverseBytes(raf.readInt())
                if (chunkCount != 6) return false

                val trailerJsonLen = java.lang.Integer.reverseBytes(raf.readInt())
                if (trailerJsonLen < 0 || trailerJsonLen > 65536) return false

                // Skip 24 reserved bytes to reach chunk sizes table at offset 64
                raf.seek(64)
                var payloadTotal = 0L
                for (i in 0 until 6) {
                    val clen = java.lang.Long.reverseBytes(raf.readLong())
                    if (clen < 0 || clen > 500L * 1024 * 1024) return false
                    payloadTotal += clen
                }

                // Verify file size matches exact layout
                val expectedTotal = 112L + payloadTotal + trailerJsonLen + 12L
                if (fileLen != expectedTotal) {
                    Log.w(tag, "Archive size mismatch: file=$fileLen, expected=$expectedTotal")
                    return false
                }

                // Verify trailer footer magic & json length
                raf.seek(fileLen - 8)
                val endMagic = java.lang.Long.reverseBytes(raf.readLong())
                if (endMagic != magicConstant) {
                    Log.w(tag, "Archive trailer magic mismatch")
                    return false
                }

                raf.seek(fileLen - 12)
                val endJsonLen = java.lang.Integer.reverseBytes(raf.readInt())
                if (endJsonLen != trailerJsonLen) {
                    Log.w(tag, "Archive trailer JSON len mismatch")
                    return false
                }

                // Verify CRC32 of payload data
                raf.seek(112)
                val crc = CRC32()
                val buf = ByteArray(8192)
                var remaining = payloadTotal
                while (remaining > 0) {
                    val toRead = minOf(remaining, buf.size.toLong()).toInt()
                    val read = raf.read(buf, 0, toRead)
                    if (read <= 0) return false
                    crc.update(buf, 0, read)
                    remaining -= read
                }

                if (crc.value != expectedCrc) {
                    Log.w(tag, "Archive CRC32 mismatch: calculated=${crc.value}, expected=$expectedCrc")
                    return false
                }

                true
            }
        } catch (e: Exception) {
            Log.w(tag, "Validation failed: ${e.message}")
            false
        }
    }

    /**
     * Packs databases safely into targetFile with 64-byte v1 header, CRC32, and trailer metadata.
     */
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
            val chunkSizes = LongArray(6)
            val fileBytesList = mutableListOf<ByteArray>()
            val crc = CRC32()

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
                        chunkSizes[i] = bytes.size.toLong()
                        fileBytesList.add(bytes)
                        if (bytes.isNotEmpty()) {
                            crc.update(bytes)
                        }
                    } else {
                        chunkSizes[i] = 0L
                        fileBytesList.add(ByteArray(0))
                    }
                } else {
                    chunkSizes[i] = 0L
                    fileBytesList.add(ByteArray(0))
                }
            }

            val payloadCrc32 = crc.value

            val stats = getLiveStats()
            val jsonString = """{"invoices":${stats.invoiceCount},"receipts":${stats.receiptCount},"customers":${stats.customerCount},"products":${stats.productCount},"time":${System.currentTimeMillis()},"mode":"${mode?.key ?: "common"}"}"""
            val jsonBytes = jsonString.toByteArray(Charsets.UTF_8)

            // 64-byte v1 header
            val header = ByteBuffer.allocate(64).order(ByteOrder.LITTLE_ENDIAN)
            header.putLong(magicConstant)
            header.putInt(1)
            header.putInt(when (mode) {
                AppMode.KOOLI -> 0
                AppMode.PATTU -> 1
                null -> 2
            })
            header.putLong(System.currentTimeMillis())
            header.putLong(payloadCrc32)
            header.putInt(6)
            header.putInt(jsonBytes.size)
            header.put(ByteArray(24))

            // 48-byte chunk table
            val chunkTable = ByteBuffer.allocate(48).order(ByteOrder.LITTLE_ENDIAN)
            for (size in chunkSizes) {
                chunkTable.putLong(size)
            }

            // 12-byte trailer footer
            val trailerFooter = ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN)
            trailerFooter.putInt(jsonBytes.size)
            trailerFooter.putLong(magicConstant)

            targetFile.parentFile?.mkdirs()
            FileOutputStream(targetFile).use { fos ->
                fos.write(header.array())
                fos.write(chunkTable.array())
                for (bytes in fileBytesList) {
                    if (bytes.isNotEmpty()) fos.write(bytes)
                }
                fos.write(jsonBytes)
                fos.write(trailerFooter.array())
                fos.flush()
            }

            Log.i(tag, "Successfully packed backup to: ${targetFile.absolutePath} (${targetFile.length()} bytes, CRC=$payloadCrc32)")
            true
        } catch (e: Exception) {
            Log.e(tag, "Pack failed for ${targetFile.absolutePath}: ${e.message}", e)
            false
        }
    }

    private fun unpackFromFile(sourceFile: File): Boolean {
        return try {
            if (!validateArchive(sourceFile)) {
                Log.e(tag, "Refusing to restore: Archive validation failed for ${sourceFile.absolutePath}")
                return false
            }

            RandomAccessFile(sourceFile, "r").use { raf ->
                raf.seek(0)
                val first8 = java.lang.Long.reverseBytes(raf.readLong())
                val isV1 = (first8 == magicConstant)

                val headerOffset: Long
                val chunkLengths = LongArray(6)

                if (isV1) {
                    headerOffset = 112L
                    raf.seek(64)
                    for (i in 0 until 6) {
                        chunkLengths[i] = java.lang.Long.reverseBytes(raf.readLong())
                    }
                } else {
                    headerOffset = 48L
                    raf.seek(0)
                    for (i in 0 until 6) {
                        chunkLengths[i] = java.lang.Long.reverseBytes(raf.readLong())
                    }
                }

                val filesToUnpack = getFilesToPack()
                val mode = currentMode()
                var currentOffset = headerOffset

                for (i in filesToUnpack.indices) {
                    val size = chunkLengths[i]
                    if (filesToUnpack[i].isEmpty()) {
                        currentOffset += size
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
                            val tempUnpack = File(file.parentFile, "${file.name}.restore.tmp")
                            FileOutputStream(tempUnpack).use { fos ->
                                raf.seek(currentOffset)
                                val buf = ByteArray(8192)
                                var remaining = size
                                while (remaining > 0) {
                                    val toRead = minOf(remaining, buf.size.toLong()).toInt()
                                    val read = raf.read(buf, 0, toRead)
                                    if (read <= 0) break
                                    fos.write(buf, 0, read)
                                    remaining -= read
                                }
                                fos.flush()
                            }
                            atomicReplace(tempUnpack, file)
                        }
                        currentOffset += size
                    } else {
                        if (shouldRestore && (i == 1 || i == 2 || i == 4 || i == 5)) {
                            if (file.exists()) file.delete()
                        }
                    }
                }
            }

            // Immediately reload in-memory cached repositories across app
            reloadAllRepositories()

            Log.i(tag, "Restore completed successfully from ${sourceFile.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e(tag, "Unpack failed from ${sourceFile.absolutePath}: ${e.message}", e)
            false
        }
    }

    override fun createBackup(): Boolean {
        val internalDir = getInternalBackupDir()
        val prefix = modePrefix(currentMode())
        val tempNewFile = File(internalDir, "udukkai_${prefix}_backup_new.tmp")

        if (!packToFile(tempNewFile)) {
            try { tempNewFile.delete() } catch (_: Exception) {}
            return false
        }

        if (!validateArchive(tempNewFile)) {
            Log.e(tag, "Created backup failed validation check. Discarding temp file.")
            try { tempNewFile.delete() } catch (_: Exception) {}
            return false
        }

        val latestFile = getFileForWriting(BackupSlot.LATEST)
        val previousFile = getFileForWriting(BackupSlot.PREVIOUS)

        // Rotate existing LATEST to PREVIOUS atomically only after new backup is verified
        if (latestFile.exists() && latestFile.length() >= 60 && validateArchive(latestFile)) {
            try {
                latestFile.copyTo(previousFile, overwrite = true)
            } catch (e: Exception) {
                Log.w(tag, "Failed to rotate latest to previous: ${e.message}")
            }
        }

        val replaced = atomicReplace(tempNewFile, latestFile)
        if (!replaced) {
            Log.e(tag, "Failed to atomically replace latest backup with new backup")
            return false
        }

        // Also mirror to external Documents if available (best effort)
        try {
            val extDir = getExternalBackupDir()
            if (extDir != null) {
                val extFile = File(extDir, latestFile.name)
                latestFile.copyTo(extFile, overwrite = true)
            }
        } catch (_: Exception) {}

        return true
    }

    override fun createSafetySnapshot(): Boolean {
        val internalDir = getInternalBackupDir()
        val prefix = modePrefix(currentMode())
        val tempVaultFile = File(internalDir, "udukkai_${prefix}_safety_vault_new.tmp")

        if (!packToFile(tempVaultFile)) {
            try { tempVaultFile.delete() } catch (_: Exception) {}
            return false
        }

        if (!validateArchive(tempVaultFile)) {
            Log.e(tag, "Created safety snapshot failed validation. Discarding.")
            try { tempVaultFile.delete() } catch (_: Exception) {}
            return false
        }

        val vaultFile = getFileForWriting(BackupSlot.SAFETY_VAULT)
        val replaced = atomicReplace(tempVaultFile, vaultFile)
        if (!replaced) return false

        // Best-effort mirror to external
        try {
            val extDir = getExternalBackupDir()
            if (extDir != null) {
                val extVault = File(extDir, vaultFile.name)
                vaultFile.copyTo(extVault, overwrite = true)
            }
        } catch (_: Exception) {}

        return true
    }

    override fun restoreFromSlot(slot: BackupSlot): Boolean {
        val file = resolveExistingFileForReading(slot) ?: run {
            Log.e(tag, "Cannot restore: backup file for slot $slot not found")
            return false
        }

        if (!validateArchive(file)) {
            Log.e(tag, "Cannot restore: backup archive for slot $slot failed validation")
            return false
        }

        // Isolate restore source if slot is SAFETY_VAULT to avoid snapshot overwrite
        val restoreSource: File
        val isTempSource: Boolean
        if (slot == BackupSlot.SAFETY_VAULT) {
            val tempRestoreFile = File(AppContext.context.cacheDir, "udukkai_vault_restore_${System.currentTimeMillis()}.tmp")
            try {
                file.copyTo(tempRestoreFile, overwrite = true)
                restoreSource = tempRestoreFile
                isTempSource = true
            } catch (e: Exception) {
                Log.e(tag, "Failed to isolate safety vault source: ${e.message}")
                return false
            }
        } else {
            restoreSource = file
            isTempSource = false
        }

        return try {
            // Before restoring, take an untouchable safety snapshot of CURRENT state
            val secured = createSafetySnapshot()
            if (!secured) {
                Log.e(tag, "Aborting restore: Failed to create safety snapshot of current data before restoring.")
                false
            } else {
                unpackFromFile(restoreSource)
            }
        } finally {
            if (isTempSource) {
                try { restoreSource.delete() } catch (_: Exception) {}
            }
        }
    }

    override fun hasBackup(slot: BackupSlot): Boolean {
        return try {
            val f = resolveExistingFileForReading(slot)
            f != null && f.exists() && f.length() >= 60 && validateArchive(f)
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
        if (!file.exists() || file.length() < 60) return null

        val isValid = validateArchive(file)
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
                        if (jsonLen in 1..65536 && length >= (12 + jsonLen)) {
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
            isValid = isValid
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
