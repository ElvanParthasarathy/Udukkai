package com.elvan.udukkai.core.backup

import com.elvan.udukkai.core.mode.AppLaunchConfig
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.mode.ModeManager
import com.elvan.udukkai.data.DesktopDbPaths
import com.elvan.udukkai.data.repository.PattiyalRepository
import com.elvan.udukkai.data.repository.PatrugalRepository
import com.elvan.udukkai.data.repository.PorulRepository
import com.elvan.udukkai.data.repository.VaangunarRepository
import com.elvan.udukkai.data.settings.NiruvanaTharavugalRepository
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.sql.DriverManager
import java.util.zip.CRC32

class DesktopUdukkaiBackupService : UdukkaiBackupService {

    private val magicConstant = 0x5544554B4B414921L // "UDUKKAI!"

    private fun currentMode(): AppMode? = AppLaunchConfig.fixedMode ?: ModeManager.currentMode

    private fun getBackupDir(): File = DesktopDbPaths.getBackupDir()

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
                println("Desktop Files.move fallback failed: ${e.message}")
            }
        } catch (e: Exception) {
            println("Desktop atomic move failed: ${e.message}")
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
            println("Desktop fallback replace failed: ${e.message}")
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

    private fun validateArchive(file: File): Boolean {
        if (!file.exists() || !file.isFile || file.length() < 124) {
            return validateLegacyArchive(file)
        }

        return try {
            RandomAccessFile(file, "r").use { raf ->
                val fileLen = raf.length()

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

                raf.seek(64)
                var payloadTotal = 0L
                for (i in 0 until 6) {
                    val clen = java.lang.Long.reverseBytes(raf.readLong())
                    if (clen < 0 || clen > 500L * 1024 * 1024) return false
                    payloadTotal += clen
                }

                val expectedTotal = 112L + payloadTotal + trailerJsonLen + 12L
                if (fileLen != expectedTotal) return false

                raf.seek(fileLen - 8)
                val endMagic = java.lang.Long.reverseBytes(raf.readLong())
                if (endMagic != magicConstant) return false

                raf.seek(fileLen - 12)
                val endJsonLen = java.lang.Integer.reverseBytes(raf.readInt())
                if (endJsonLen != trailerJsonLen) return false

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

                crc.value == expectedCrc
            }
        } catch (e: Exception) {
            false
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
            val chunkSizes = LongArray(6)
            val fileBytesList = mutableListOf<ByteArray>()
            val crc = CRC32()

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

            val chunkTable = ByteBuffer.allocate(48).order(ByteOrder.LITTLE_ENDIAN)
            for (size in chunkSizes) {
                chunkTable.putLong(size)
            }

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
            true
        } catch (e: Exception) {
            println("Desktop pack failed: ${e.message}")
            false
        }
    }

    private fun unpackFromFile(sourceFile: File): Boolean {
        return try {
            if (!validateArchive(sourceFile)) {
                println("Desktop restore rejected: archive corrupted or invalid: ${sourceFile.absolutePath}")
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

            // Immediately reload in-memory repositories across app
            reloadAllRepositories()
            true
        } catch (e: Exception) {
            println("Desktop unpack failed: ${e.message}")
            false
        }
    }

    override fun createBackup(): Boolean {
        val backupDir = getBackupDir()
        val prefix = when (currentMode()) {
            AppMode.KOOLI -> "kooli"
            AppMode.PATTU -> "pattu"
            null -> "common"
        }
        val tempNewFile = File(backupDir, "udukkai_${prefix}_backup_new.tmp")

        if (!packToFile(tempNewFile)) {
            try { tempNewFile.delete() } catch (_: Exception) {}
            return false
        }

        if (!validateArchive(tempNewFile)) {
            println("Desktop created backup failed validation.")
            try { tempNewFile.delete() } catch (_: Exception) {}
            return false
        }

        val latestFile = DesktopDbPaths.getBackupFile(currentMode(), BackupSlot.LATEST)
        val previousFile = DesktopDbPaths.getBackupFile(currentMode(), BackupSlot.PREVIOUS)

        if (latestFile.exists() && latestFile.length() >= 60 && validateArchive(latestFile)) {
            try {
                latestFile.copyTo(previousFile, overwrite = true)
            } catch (_: Exception) {}
        }

        val replaced = atomicReplace(tempNewFile, latestFile)
        return replaced
    }

    override fun createSafetySnapshot(): Boolean {
        val backupDir = getBackupDir()
        val prefix = when (currentMode()) {
            AppMode.KOOLI -> "kooli"
            AppMode.PATTU -> "pattu"
            null -> "common"
        }
        val tempVaultFile = File(backupDir, "udukkai_${prefix}_safety_vault_new.tmp")

        if (!packToFile(tempVaultFile)) {
            try { tempVaultFile.delete() } catch (_: Exception) {}
            return false
        }

        if (!validateArchive(tempVaultFile)) {
            println("Desktop created safety snapshot failed validation.")
            try { tempVaultFile.delete() } catch (_: Exception) {}
            return false
        }

        val vaultFile = DesktopDbPaths.getBackupFile(currentMode(), BackupSlot.SAFETY_VAULT)
        return atomicReplace(tempVaultFile, vaultFile)
    }

    override fun restoreFromSlot(slot: BackupSlot): Boolean {
        val file = DesktopDbPaths.resolveExistingBackupFile(currentMode(), slot)
        if (!file.exists() || !validateArchive(file)) return false

        // Isolate restore source if slot is SAFETY_VAULT to avoid snapshot overwrite
        val restoreSource: File
        val isTempSource: Boolean
        if (slot == BackupSlot.SAFETY_VAULT) {
            val tempRestoreFile = File(getBackupDir(), "udukkai_vault_restore_${System.currentTimeMillis()}.tmp")
            try {
                file.copyTo(tempRestoreFile, overwrite = true)
                restoreSource = tempRestoreFile
                isTempSource = true
            } catch (e: Exception) {
                println("Failed to isolate desktop safety vault source: ${e.message}")
                return false
            }
        } else {
            restoreSource = file
            isTempSource = false
        }

        return try {
            val secured = createSafetySnapshot()
            if (!secured) {
                println("Aborting desktop restore: Failed to secure pre-restore safety snapshot.")
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
            val f = DesktopDbPaths.resolveExistingBackupFile(currentMode(), slot)
            f.exists() && f.length() >= 60 && validateArchive(f)
        } catch (_: Exception) { false }
    }

    override fun deleteBackup(slot: BackupSlot): Boolean {
        return try {
            val f = DesktopDbPaths.resolveExistingBackupFile(currentMode(), slot)
            if (f.exists()) f.delete() else true
        } catch (_: Exception) { false }
    }

    override fun getBackupStats(slot: BackupSlot): BackupStats? {
        return try {
            val f = DesktopDbPaths.resolveExistingBackupFile(currentMode(), slot)
            if (f.exists()) BackupStats(f.lastModified(), f.length()) else null
        } catch (_: Exception) { null }
    }

    override fun getBackupMetadata(slot: BackupSlot): BackupItemMetadata? {
        val file = DesktopDbPaths.resolveExistingBackupFile(currentMode(), slot)
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
                val f = File(path)
                if (f.exists()) f.length() else 0L
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
            val dir = getBackupDir()
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(dir)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }
}

@Deprecated("Use DesktopUdukkaiBackupService", ReplaceWith("DesktopUdukkaiBackupService"))
typealias DesktopNirilBackupService = DesktopUdukkaiBackupService

private val desktopBackupInstance by lazy { DesktopUdukkaiBackupService() }

actual fun getUdukkaiBackupService(): UdukkaiBackupService = desktopBackupInstance
