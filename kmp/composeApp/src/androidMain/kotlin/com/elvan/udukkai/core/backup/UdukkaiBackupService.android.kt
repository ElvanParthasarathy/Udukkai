package com.elvan.udukkai.core.backup

import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import com.elvan.udukkai.core.platform.AppContext
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

class AndroidUdukkaiBackupService : UdukkaiBackupService {

    private val tag = "UdukkaiBackup"
    private val magicConstant = 0x5544554B4B414921L // "UDUKKAI!"

    private fun isFactory(): Boolean = com.elvan.udukkai.BuildConfig.FLAVOR == "factory"
    private fun currentMode(): String = if (isFactory()) "factory" else "textiles"

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
     * The copy outside the app: Documents/Udukkai/<Udukkai Textiles|Udukkai Factory>/Backup, visible in My Files
     * and kept when the app is uninstalled. Android 11+ lets an app write its own files in Documents without any
     * storage permission; where that fails the backup inside the app is still made.
     */
    private fun getExternalBackupDir(): File? {
        return try {
            // The debug app keeps its backups apart, so test data is never offered to the release app as a restore
            val app = (if (isFactory()) "Udukkai Factory" else "Udukkai Textiles") + if (com.elvan.udukkai.BuildConfig.DEBUG) " Debug" else ""
            val docs = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOCUMENTS)
            val dir = File(docs, "Udukkai/$app/Backup")
            if (!dir.exists()) dir.mkdirs()
            if (!dir.isDirectory) return null
            moveOldBackups(docs, dir)
            dir
        } catch (_: Exception) {
            null
        }
    }

    /** Backups this app left in the old places (the hidden Documents/.Udukkai/backup, earlier Documents/Udukkai/backup) move here once. */
    private fun moveOldBackups(docs: File, target: File) {
        val mine = if (isFactory()) listOf("udukkai_factory", "udukkai_kooli") else listOf("udukkai_textiles", "udukkai_pattu")
        for (old in listOf(File(docs, ".Udukkai/backup"), File(docs, "Udukkai/backup"))) {
            if (!old.isDirectory) continue
            old.listFiles()?.filter { f -> f.isFile && mine.any { f.name.startsWith(it) } }?.forEach { file ->
                val moved = File(target, file.name)
                try {
                    if (!moved.exists() || moved.length() < file.length()) file.copyTo(moved, overwrite = true)
                    // The original goes only after the copy is complete
                    if (moved.length() == file.length()) { file.delete(); showInFiles(moved) }
                } catch (_: Exception) {}
            }
            try {
                File(old, ".nomedia").takeIf { old.listFiles()?.all { it.name == ".nomedia" } == true }?.delete()
                if (old.listFiles().isNullOrEmpty()) { old.delete(); old.parentFile?.takeIf { it.name == ".Udukkai" && it.listFiles().isNullOrEmpty() }?.delete() }
            } catch (_: Exception) {}
        }
    }

    /** Tells Android the file changed, so My Files and a computer over USB see it at once. */
    private fun showInFiles(file: File) {
        try { android.media.MediaScannerConnection.scanFile(AppContext.context, arrayOf(file.absolutePath), null, null) } catch (_: Exception) {}
    }

    private fun modePrefix(mode: String?): String {
        return when (mode) {
            "factory" -> "factory"
            "textiles" -> "textiles"
            else -> if (isFactory()) "factory" else "textiles"
        }
    }

    private fun getFileForSlot(dir: File, slot: BackupSlot, mode: String?): File {
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
            val legacyNames = if (isFactory()) {
                listOf("udukkai_factory_backup.db", "udukkai_kooli_backup.db")
            } else {
                listOf("udukkai_textiles_backup.db", "udukkai_pattu_backup.db")
            }
            for (legacyName in legacyNames) {
                val internalLegacy = File(internalDir, legacyName)
                if (internalLegacy.exists() && internalLegacy.length() > 48) return internalLegacy
            }

            // Check external documents
            val externalDir = getExternalBackupDir()
            if (externalDir != null) {
                val externalSlot = getFileForSlot(externalDir, slot, mode)
                if (externalSlot.exists() && externalSlot.length() > 48) return externalSlot
                for (legacyName in legacyNames) {
                    val externalLegacy = File(externalDir, legacyName)
                    if (externalLegacy.exists() && externalLegacy.length() > 48) return externalLegacy
                }
            }
        }

        // Fallbacks for SAFETY_VAULT: check legacy snapshot names
        if (slot == BackupSlot.SAFETY_VAULT) {
            val legacySafetyNames = if (isFactory()) {
                listOf("udukkai_factory_safety_snapshot.db", "udukkai_kooli_safety_snapshot.db")
            } else {
                listOf("udukkai_textiles_safety_snapshot.db", "udukkai_pattu_safety_snapshot.db")
            }
            for (legacySafetyName in legacySafetyNames) {
                val internalLegacy = File(internalDir, legacySafetyName)
                if (internalLegacy.exists() && internalLegacy.length() > 48) return internalLegacy
            }

            val externalDir = getExternalBackupDir()
            if (externalDir != null) {
                val externalSlot = getFileForSlot(externalDir, slot, mode)
                if (externalSlot.exists() && externalSlot.length() > 48) return externalSlot
                for (legacySafetyName in legacySafetyNames) {
                    val externalLegacy = File(externalDir, legacySafetyName)
                    if (externalLegacy.exists() && externalLegacy.length() > 48) return externalLegacy
                }
            }
        }

        return if (slotFile.exists()) slotFile else null
    }

    private fun getFactoryDbPath(): String {
        if (!AppContext.isInitialized) return ""
        val f = AppContext.context.getDatabasePath("udukkai_factory.db")
        if (f.exists()) return f.absolutePath
        val legacy = AppContext.context.getDatabasePath("udukkai_kooli.db")
        return if (legacy.exists()) legacy.absolutePath else f.absolutePath
    }

    private fun getTextilesDbPath(): String {
        if (!AppContext.isInitialized) return ""
        val f = AppContext.context.getDatabasePath("udukkai_textiles.db")
        if (f.exists()) return f.absolutePath
        val legacy = AppContext.context.getDatabasePath("udukkai_pattu.db")
        return if (legacy.exists()) legacy.absolutePath else f.absolutePath
    }

    private fun getFilesToPack(): List<String> {
        val factory = getFactoryDbPath()
        val textiles = getTextilesDbPath()
        return listOf(
            factory,
            "$factory-wal",
            "$factory-shm",
            textiles,
            "$textiles-wal",
            "$textiles-shm"
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
            if (isFactory()) {
                checkpointWal(getFactoryDbPath())
            } else {
                checkpointWal(getTextilesDbPath())
            }

            val filesToPack = getFilesToPack()
            val chunkSizes = LongArray(6)
            val fileBytesList = mutableListOf<ByteArray>()
            val crc = CRC32()

            for (i in filesToPack.indices) {
                val shouldPack = if (isFactory()) i < 3 else i >= 3

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
            val jsonString = """{"invoices":${stats.invoiceCount},"receipts":${stats.receiptCount},"customers":${stats.customerCount},"items":${stats.itemCount},"time":${System.currentTimeMillis()},"mode":"${if (isFactory()) "factory" else "textiles"}"}"""
            val jsonBytes = jsonString.toByteArray(Charsets.UTF_8)

            // 64-byte v1 header
            val header = ByteBuffer.allocate(64).order(ByteOrder.LITTLE_ENDIAN)
            header.putLong(magicConstant)
            header.putInt(1)
            header.putInt(if (isFactory()) 0 else 1)
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
                    val shouldRestore = if (isFactory()) i < 3 else i >= 3

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

            // Immediately reload in-memory cached repositories for the active app
            if (isFactory()) {
                reloadFactoryRepositories()
            } else {
                reloadTextilesRepositories()
            }

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

        // An empty database (fresh install, failed restore) must never replace a backup that holds data
        val live = getLiveStats()
        if (live.invoiceCount + live.receiptCount + live.customerCount + live.itemCount == 0) {
            val old = getBackupMetadata(BackupSlot.LATEST)
            if (old != null && old.invoiceCount + old.receiptCount + old.customerCount + old.itemCount > 0) {
                Log.w(tag, "Skipping backup: database is empty but the existing backup has data.")
                try { tempNewFile.delete() } catch (_: Exception) {}
                return true
            }
        }

        // Rotate LATEST to PREVIOUS only when it is over an hour old, so a burst of edits cannot overwrite the older good copy
        if (latestFile.exists() && latestFile.length() >= 60 && System.currentTimeMillis() - latestFile.lastModified() > 3600_000L && validateArchive(latestFile)) {
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
                showInFiles(extFile)
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
        // Keep the vault it replaces for one more generation
        try {
            if (vaultFile.exists() && validateArchive(vaultFile)) {
                vaultFile.copyTo(File(vaultFile.parentFile, vaultFile.name.removeSuffix(".db") + "_older.db"), overwrite = true)
            }
        } catch (_: Exception) {}
        val replaced = atomicReplace(tempVaultFile, vaultFile)
        if (!replaced) return false

        // Best-effort mirror to external
        try {
            val extDir = getExternalBackupDir()
            if (extDir != null) {
                val extVault = File(extDir, vaultFile.name)
                vaultFile.copyTo(extVault, overwrite = true)
                showInFiles(extVault)
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

    // ── Restoring from a file the user picked ───────────────────────────────────────

    private fun beforeRestoreFile(): File = File(getInternalBackupDir(), "udukkai_${modePrefix(currentMode())}_before_restore.db")

    /** Going back is only offered for a day: after that the copy is deleted, so newer bills can never be undone by mistake. */
    private fun undoCopy(): File? {
        val file = beforeRestoreFile()
        if (!file.exists()) return null
        if (System.currentTimeMillis() - file.lastModified() > 24 * 3600_000L) {
            try { file.delete() } catch (_: Exception) {}
            return null
        }
        return file
    }

    override fun lastRestoreBackup(): BackupItemMetadata? = undoCopy()?.let { metadataOf(it, BackupSlot.SAFETY_VAULT) }

    override fun undoLastRestore(): Boolean {
        val file = undoCopy() ?: return false
        if (!validateArchive(file)) return false
        // A copy of the data as it is now is kept first, like any restore
        if (!createSafetySnapshot()) return false
        // Worked on a copy so the kept file is never open while the data is replaced
        val work = File(AppContext.context.cacheDir, "undo_restore_${System.nanoTime()}.tmp")
        return try {
            file.copyTo(work, overwrite = true)
            val done = unpackFromFile(work)
            if (done) { file.delete(); createBackup() }
            done
        } catch (e: Exception) {
            Log.e(tag, "Undoing the restore failed: ${e.message}", e)
            false
        } finally {
            try { work.delete() } catch (_: Exception) {}
        }
    }

    private class AndroidMergeDb(private val db: android.database.sqlite.SQLiteDatabase) : MergeDb {
        override fun count(sql: String): Int = db.rawQuery(sql, null).use { if (it.moveToFirst()) it.getInt(0) else 0 }

        override fun columns(schema: String, table: String): List<String> =
            db.rawQuery("PRAGMA $schema.table_info(\"$table\")", null).use { c ->
                val name = c.getColumnIndexOrThrow("name")
                buildList { while (c.moveToNext()) add(c.getString(name)) }
            }

        override fun exec(sql: String) = db.execSQL(sql)
        override fun begin() = db.beginTransaction()
        override fun commit() { db.setTransactionSuccessful(); db.endTransaction() }
        override fun rollback() = db.endTransaction()
    }

    private fun tablePrefix(): String = if (isFactory()) "factory_" else "textiles_"
    private fun liveDbPath(): String = if (isFactory()) getFactoryDbPath() else getTextilesDbPath()

    private fun hasBackupMagic(file: File): Boolean = try {
        RandomAccessFile(file, "r").use { raf ->
            raf.seek(0)
            val first = java.lang.Long.reverseBytes(raf.readLong())
            raf.seek(raf.length() - 8)
            first == magicConstant || java.lang.Long.reverseBytes(raf.readLong()) == magicConstant
        }
    } catch (_: Exception) {
        false
    }

    private fun trailerTime(file: File): Long = try {
        RandomAccessFile(file, "r").use { raf ->
            val length = raf.length()
            raf.seek(length - 12)
            val jsonLen = java.lang.Integer.reverseBytes(raf.readInt())
            raf.seek(length - 12 - jsonLen)
            val bytes = ByteArray(jsonLen)
            raf.readFully(bytes)
            Regex("\"time\":(\\d+)").find(String(bytes, Charsets.UTF_8))?.groupValues?.get(1)?.toLongOrNull() ?: file.lastModified()
        }
    } catch (_: Exception) {
        file.lastModified()
    }

    private fun deleteWithSidecars(db: File) {
        listOf("", "-wal", "-shm", "-journal").forEach { try { File(db.path + it).delete() } catch (_: Exception) {} }
    }

    /** Checks the archive and copies this app's database out of it to [target]; returns a [BackupProblem] or null. */
    private fun extractCurrentDb(archive: File, target: File): String? {
        if (!validateArchive(archive)) return if (hasBackupMagic(archive)) BackupProblem.DAMAGED else BackupProblem.NOT_A_BACKUP
        return try {
            RandomAccessFile(archive, "r").use { raf ->
                raf.seek(0)
                val isV1 = java.lang.Long.reverseBytes(raf.readLong()) == magicConstant
                raf.seek(if (isV1) 64L else 0L)
                val lengths = LongArray(6) { java.lang.Long.reverseBytes(raf.readLong()) }
                val base = if (isFactory()) 0 else 3
                if (lengths[base] <= 0L) return BackupProblem.OTHER_APP
                var offset = if (isV1) 112L else 48L
                for (i in 0 until 6) {
                    val dest = when (i) {
                        base -> target
                        base + 1 -> File(target.path + "-wal")
                        else -> null
                    }
                    if (dest != null && lengths[i] > 0L) {
                        FileOutputStream(dest).use { out ->
                            raf.seek(offset)
                            val buf = ByteArray(8192)
                            var remaining = lengths[i]
                            while (remaining > 0) {
                                val read = raf.read(buf, 0, minOf(remaining, buf.size.toLong()).toInt())
                                if (read <= 0) return BackupProblem.DAMAGED
                                out.write(buf, 0, read)
                                remaining -= read
                            }
                        }
                    }
                    offset += lengths[i]
                }
            }
            null
        } catch (e: Exception) {
            Log.e(tag, "Extracting the backup failed: ${e.message}", e)
            BackupProblem.DAMAGED
        }
    }

    override fun inspectBackupFile(path: String): BackupPreview {
        val archive = File(path)
        val work = File(AppContext.context.cacheDir, "restore_check_${System.nanoTime()}.db")
        return try {
            extractCurrentDb(archive, work)?.let { return BackupPreview(problem = it) }
            val db = android.database.sqlite.SQLiteDatabase.openDatabase(work.path, null, android.database.sqlite.SQLiteDatabase.OPEN_READWRITE)
            db.use {
                val verdict = it.rawQuery("PRAGMA integrity_check", null).use { c -> if (c.moveToFirst()) c.getString(0) else "" }
                if (verdict != "ok") return BackupPreview(problem = BackupProblem.DAMAGED)
                BackupMerge.preview(AndroidMergeDb(it), tablePrefix(), "main", trailerTime(archive))
            }
        } catch (e: Exception) {
            Log.e(tag, "Inspecting the backup failed: ${e.message}", e)
            BackupPreview(problem = BackupProblem.DAMAGED)
        } finally {
            deleteWithSidecars(work)
        }
    }

    override fun restoreFromFile(path: String, mode: RestoreMode, categories: Set<RestoreCategory>): RestoreReport {
        val archive = File(path)
        val preview = inspectBackupFile(path)
        if (!preview.ok) return RestoreReport(ok = false, problem = preview.problem)
        if (mode == RestoreMode.MERGE && categories.isEmpty()) return RestoreReport(ok = false, problem = BackupProblem.NOTHING_CHOSEN)
        // Nothing is changed until a copy of the current data is safe
        if (!createSafetySnapshot()) return RestoreReport(ok = false, problem = BackupProblem.NO_SAFETY_COPY)
        // Kept apart from the safety vault, which syncing and backups overwrite: this is what "go back" brings back
        try {
            getFileForWriting(BackupSlot.SAFETY_VAULT).copyTo(beforeRestoreFile(), overwrite = true)
        } catch (e: Exception) {
            Log.e(tag, "Could not keep the copy for going back: ${e.message}")
            return RestoreReport(ok = false, problem = BackupProblem.NO_SAFETY_COPY)
        }

        if (mode == RestoreMode.REPLACE) {
            val done = unpackFromFile(archive)
            return if (done) {
                createBackup()
                RestoreReport(
                    ok = true,
                    added = mapOf(
                        RestoreCategory.BUSINESSES to preview.businesses, RestoreCategory.CUSTOMERS to preview.customers,
                        RestoreCategory.ITEMS to preview.items, RestoreCategory.BILLS to preview.invoices + preview.receipts
                    )
                )
            } else {
                RestoreReport(ok = false, problem = BackupProblem.FAILED)
            }
        }

        val work = File(AppContext.context.cacheDir, "restore_merge_${System.nanoTime()}.db")
        var live: android.database.sqlite.SQLiteDatabase? = null
        return try {
            extractCurrentDb(archive, work)?.let { return RestoreReport(ok = false, problem = it) }
            live = android.database.sqlite.SQLiteDatabase.openDatabase(liveDbPath(), null, android.database.sqlite.SQLiteDatabase.OPEN_READWRITE)
            live.execSQL("ATTACH DATABASE '${work.absolutePath.replace("'", "''")}' AS bak")
            val report = BackupMerge.merge(AndroidMergeDb(live), tablePrefix(), categories)
            try { live.execSQL("DETACH DATABASE bak") } catch (_: Exception) {}
            if (report.ok) {
                if (isFactory()) reloadFactoryRepositories() else reloadTextilesRepositories()
            }
            try { live.close() } catch (_: Exception) {}
            if (report.ok) createBackup()
            report
        } catch (e: Exception) {
            Log.e(tag, "Merging the backup failed: ${e.message}", e)
            RestoreReport(ok = false, problem = BackupProblem.FAILED)
        } finally {
            try { live?.close() } catch (_: Exception) {}
            deleteWithSidecars(work)
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
        return metadataOf(file, slot)
    }

    private fun metadataOf(file: File, slot: BackupSlot): BackupItemMetadata? {
        if (!file.exists() || file.length() < 60) return null

        val isValid = validateArchive(file)
        var invoiceCount = 0
        var receiptCount = 0
        var customerCount = 0
        var itemCount = 0
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
                            itemCount = Regex("\"(?:items|products)\":(\\d+)").find(json)?.groupValues?.get(1)?.toIntOrNull() ?: 0
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
            itemCount = itemCount,
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
            getFilesToPack().filterIndexed { index, _ ->
                if (isFactory()) index < 3 else index >= 3
            }.sumOf { path ->
                if (path.isEmpty()) 0L else {
                    val f = File(path)
                    if (f.exists()) f.length() else 0L
                }
            }
        } catch (_: Exception) { 0L }
    }

    override fun getLiveStats(): DatabaseLiveStats {
        val isFact = isFactory()
        val invCount = if (isFact) {
            val activeId = com.elvan.udukkai.factory.data.FactoryProfileRepository.profile.id
            com.elvan.udukkai.factory.data.FactoryInvoiceRepository.invoices.count { (activeId == null || it.companyId == activeId) && !it.isDeleted }
        } else {
            val activeId = com.elvan.udukkai.textiles.data.TextilesProfileRepository.profile.id
            com.elvan.udukkai.textiles.data.TextilesInvoiceRepository.invoices.count { (activeId == null || it.companyId == activeId) && !it.isDeleted }
        }

        val recCount = if (isFact) {
            val activeId = com.elvan.udukkai.factory.data.FactoryProfileRepository.profile.id
            com.elvan.udukkai.factory.data.FactoryReceiptRepository.receipts.count { (activeId == null || it.companyId == activeId) && !it.isDeleted }
        } else {
            val activeId = com.elvan.udukkai.textiles.data.TextilesProfileRepository.profile.id
            com.elvan.udukkai.textiles.data.TextilesReceiptRepository.receipts.count { (activeId == null || it.companyId == activeId) && !it.isDeleted }
        }

        val cusCount = if (isFact) {
            com.elvan.udukkai.factory.data.FactoryCustomerRepository.customers.count { !it.isDeleted }
        } else {
            com.elvan.udukkai.textiles.data.TextilesCustomerRepository.customers.count { !it.isDeleted }
        }

        val itemCountVal = if (isFact) {
            val activeId = com.elvan.udukkai.factory.data.FactoryProfileRepository.profile.id
            com.elvan.udukkai.factory.data.FactoryItemRepository.items.count { (activeId == null || it.isAvailableForCompany(activeId)) && !it.isDeleted }
        } else {
            val activeId = com.elvan.udukkai.textiles.data.TextilesProfileRepository.profile.id
            com.elvan.udukkai.textiles.data.TextilesItemRepository.items.count { (activeId == null || it.isAvailableForCompany(activeId)) && !it.isDeleted }
        }

        return DatabaseLiveStats(
            totalDbSizeBytes = getTotalDatabaseSize(),
            invoiceCount = invCount,
            receiptCount = recCount,
            customerCount = cusCount,
            itemCount = itemCountVal
        )
    }

    override fun shareBackup(slot: BackupSlot): Boolean {
        return try {
            // What is shared is the data as it is now, never an older saved copy
            if (slot == BackupSlot.LATEST) createBackup()
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

@Deprecated("Use AndroidUdukkaiBackupService", ReplaceWith("AndroidUdukkaiBackupService"))


private val androidBackupInstance by lazy { AndroidUdukkaiBackupService() }

actual fun getUdukkaiBackupService(): UdukkaiBackupService = androidBackupInstance
