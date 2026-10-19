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
import java.sql.DriverManager

class DesktopNirilBackupService : NirilBackupService {

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
                    header.putLong(0L)
                    fileBytesList.add(ByteArray(0))
                }
            }

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

            if (!tempFile.exists() || tempFile.length() < 48) {
                tempFile.delete()
                return false
            }

            if (targetFile.exists()) targetFile.delete()
            val renamed = tempFile.renameTo(targetFile)
            if (!renamed) {
                tempFile.copyTo(targetFile, overwrite = true)
                tempFile.delete()
            }
            true
        } catch (e: Exception) {
            println("Desktop pack failed: ${e.message}")
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

            // Immediately reload in-memory repositories across app
            reloadAllRepositories()
            true
        } catch (e: Exception) {
            println("Desktop unpack failed: ${e.message}")
            false
        }
    }

    override fun createBackup(): Boolean {
        val latestFile = DesktopDbPaths.getBackupFile(currentMode(), BackupSlot.LATEST)
        val previousFile = DesktopDbPaths.getBackupFile(currentMode(), BackupSlot.PREVIOUS)

        if (latestFile.exists() && latestFile.length() > 48) {
            try {
                latestFile.copyTo(previousFile, overwrite = true)
            } catch (_: Exception) {}
        }

        return packToFile(latestFile)
    }

    override fun createSafetySnapshot(): Boolean {
        val vaultFile = DesktopDbPaths.getBackupFile(currentMode(), BackupSlot.SAFETY_VAULT)
        return packToFile(vaultFile)
    }

    override fun restoreFromSlot(slot: BackupSlot): Boolean {
        val file = DesktopDbPaths.resolveExistingBackupFile(currentMode(), slot)
        if (!file.exists() || file.length() < 48) return false

        try {
            createSafetySnapshot()
        } catch (_: Exception) {}

        return unpackFromFile(file)
    }

    override fun hasBackup(slot: BackupSlot): Boolean {
        return try {
            val f = DesktopDbPaths.resolveExistingBackupFile(currentMode(), slot)
            f.exists() && f.length() > 48
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

private val desktopBackupInstance by lazy { DesktopNirilBackupService() }

actual fun getNirilBackupService(): NirilBackupService = desktopBackupInstance
