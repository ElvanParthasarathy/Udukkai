package com.elvan.udukkai.core.audit

import com.elvan.udukkai.BuildConfig
import com.elvan.udukkai.core.platform.AppContext
import com.elvan.udukkai.core.backup.*
import com.elvan.udukkai.data.database.*
import com.elvan.udukkai.data.model.*
import com.elvan.udukkai.data.settings.BusinessProfile
import java.io.File
import java.security.MessageDigest

/** Explicitly invoked debug-only recovery exercise. No credentials or record contents enter its report. */
object ReleaseAudit {
    fun run() {
        if (!BuildConfig.DEBUG) return
        val ctx = AppContext.context
        if (!File(ctx.filesDir, "release-audit-offline").exists()) return
        val report = File(ctx.filesDir, "release-audit-results.txt")
        val lines = mutableListOf<String>()
        fun passed(name: String) { lines += "PASS\t$name"; report.writeText(lines.joinToString("\n")) }
        val factory = BuildConfig.FLAVOR == "factory"
        val prefix = if (factory) "factory_" else "textiles_"
        val db = if (factory) getFactoryDatabaseHelper() else getTextilesDatabaseHelper()
        val service = getUdukkaiBackupService()
        val dbFile = ctx.getDatabasePath("udukkai_${BuildConfig.FLAVOR}.db")
        fun fingerprint(): String {
            val md = MessageDigest.getInstance("SHA-256")
            android.database.sqlite.SQLiteDatabase.openDatabase(dbFile.path, null, android.database.sqlite.SQLiteDatabase.OPEN_READONLY).use { sql ->
                val tables = mutableListOf<String>()
                sql.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name LIKE '${prefix}%' ORDER BY name", null).use { c -> while(c.moveToNext()) tables += c.getString(0) }
                for (table in tables) {
                    md.update(table.toByteArray())
                    sql.rawQuery("SELECT * FROM [$table] ORDER BY id", null).use { c ->
                        while(c.moveToNext()) for(i in 0 until c.columnCount) {
                            val bytes = if(c.isNull(i)) byteArrayOf() else if(c.getType(i) == android.database.Cursor.FIELD_TYPE_BLOB) c.getBlob(i) else c.getString(i).toByteArray()
                            md.update(c.getType(i).toByte()); md.update(java.nio.ByteBuffer.allocate(4).putInt(bytes.size).array()); md.update(bytes)
                        }
                    }
                }
            }
            return md.digest().joinToString("") { "%02x".format(it) }
        }
        var original: File? = null
        var before: String? = null
        try {
            check(service.createBackup()); passed("create real-data backup")
            val latest = File(ctx.filesDir, "backup").listFiles()!!.first { it.name.contains("latest") && it.name.contains(BuildConfig.FLAVOR) }
            original = File(ctx.cacheDir, "release-audit-original.db")
            latest.copyTo(original, overwrite = true)
            check(service.inspectBackupFile(original.path).ok); passed("inspect backup")
            before = fingerprint()
            val broken = File(ctx.cacheDir, "release-audit-truncated.db");broken.writeBytes(original.readBytes().take(100).toByteArray())
            check(!service.inspectBackupFile(broken.path).ok)
            check(!service.restoreFromFile(broken.path, RestoreMode.REPLACE, RestoreCategory.entries.toSet()).ok)
            check(fingerprint() == before); passed("reject truncated backup without changing live rows")
            for (language in listOf("ta", "en")) for (bilingual in listOf(false, true)) {
                val stamp = "QA-${language}-${bilingual}-${System.currentTimeMillis()}"
                val profile = BusinessProfile(businessName = mutableMapOf("ta" to "சோதனை", "en" to stamp), primaryLanguage = language, secondaryLanguage = if(language == "ta") "en" else "ta", isBilingual = bilingual)
                check(db.saveProfile(profile));check(db.loadAllProfiles().any { it.businessName["en"] == stamp })
                val cid = db.saveCustomer(CustomerModel(name=mapOf("ta" to "சோதனை வாடிக்கையாளர்", "en" to stamp), address=mapOf("ta" to "12 தெரு", "en" to "12 Street"), city=mapOf("ta" to "ஆரணி", "en" to "Arani"), district=mapOf("ta" to "திருவண்ணாமலை", "en" to "Tiruvannamalai"), state=mapOf("ta" to "தமிழ்நாடு", "en" to "Tamil Nadu"), country=mapOf("ta" to "இந்தியா", "en" to "India"), pinCode="632301", gstin="33ABCDE1234F1Z5", phone="9000000000", email="qa@example.invalid"))
                check(cid > 0);check(db.loadAllCustomers().first { it.id == cid }.address.size == 2)
                val iid=db.saveItem(ItemModel(name=mapOf("ta" to "சோதனை பொருள்", "en" to stamp), price=100.0, taxRate=5.0));check(iid>0)
                val bid=db.saveInvoice(InvoiceModel(invoiceNumber=stamp, customerId=cid, customerName=mapOf("en" to stamp), totalAmount=105.0));check(bid>0)
                val rid=db.saveReceiptWithLinks(ReceiptModel(receiptNumber=stamp, customerId=cid, amount=50.0),listOf(ReceiptInvoiceLinkModel(invoiceId=bid, appliedAmount=50.0)))
                check(rid>0);check(db.getPaidAmountForInvoice(bid)==50.0);check(db.getLinksForReceipt(rid).size==1)
                check(db.deleteReceipt(rid));check(db.loadDeletedReceipts().any{it.id==rid});check(db.restoreReceipt(rid))
                check(db.deleteInvoice(bid));check(db.loadDeletedInvoices().any{it.id==bid});check(db.restoreInvoice(bid))
                check(db.deleteItem(iid));check(db.restoreItem(iid));check(db.deleteCustomer(cid));check(db.restoreCustomer(cid))
                check(db.deleteReceipt(rid));check(db.permanentDeleteReceipt(rid));check(db.deleteInvoice(bid));check(db.permanentDeleteInvoice(bid))
                check(db.deleteItem(iid));check(db.permanentDeleteItem(iid));check(db.deleteCustomer(cid));check(db.permanentDeleteCustomer(cid))
                passed("CRUD, linked receipt, recycle/restore/permanent-delete $language bilingual=$bilingual")
            }
            check(db.clearAllData());check(db.loadAllCustomers().isEmpty() && db.loadAllInvoices().isEmpty() && db.loadAllReceipts().isEmpty() && db.loadAllItems().isEmpty());passed("clear real data locally under sync isolation")
            check(service.restoreFromFile(original.path,RestoreMode.REPLACE,RestoreCategory.entries.toSet()).ok)
            check(fingerprint()==before);passed("replace restore reproduces all original SQL rows")
            check(service.restoreFromFile(original.path,RestoreMode.MERGE,RestoreCategory.entries.toSet()).ok)
            check(fingerprint()==before);passed("merge is idempotent on original rows")
        } catch (e: Throwable) {
            lines += "FAIL\t${e.javaClass.simpleName}: ${e.message}";report.writeText(lines.joinToString("\n"))
        } finally {
            if(original != null && before != null) {
                try {
                    check(service.restoreFromFile(original.path,RestoreMode.REPLACE,RestoreCategory.entries.toSet()).ok)
                    check(fingerprint()==before);passed("finally restored original database rows")
                } catch(e:Throwable) { lines += "RESTORE_FAILED\t${e.javaClass.simpleName}: ${e.message}" }
            }
            lines += "DONE";report.writeText(lines.joinToString("\n"))
        }
    }
}
