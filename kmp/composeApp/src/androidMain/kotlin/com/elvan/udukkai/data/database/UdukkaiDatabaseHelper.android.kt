package com.elvan.udukkai.data.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.elvan.udukkai.core.platform.AppContext
import com.elvan.udukkai.data.model.CustomerModel
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.model.ReceiptInvoiceLinkModel
import com.elvan.udukkai.data.model.ReceiptModel
import com.elvan.udukkai.data.model.PorulJsonConverter
import com.elvan.udukkai.data.model.ProductModel
import com.elvan.udukkai.data.settings.LanguageJsonConverter
import com.elvan.udukkai.data.settings.BusinessProfile
import java.io.File

class AndroidUdukkaiDatabaseHelper(
    val dbName: String,
    val tablePrefix: String,
    val isFactory: Boolean
) : UdukkaiDatabaseHelper {

    private val tag = "UdukkaiDbHelper"

    private val vaangunarTable = "${tablePrefix}vaangunar_table"
    private val porulTable = "${tablePrefix}porul_table"
    private val pattiyalTable = "${tablePrefix}pattiyal_table"
    private val patrugalTable = "${tablePrefix}patrugal_table"
    private val junctionTable = "${tablePrefix}patru_pattiyal_table"
    private val profileTable = "${tablePrefix}niruvana_tharavugal_table"

    private fun resolveActiveDatabase(): File? {
        if (!AppContext.isInitialized) {
            Log.w(tag, "AppContext is not initialized yet!")
            return null
        }
        val localDb = AppContext.context.getDatabasePath(dbName)
        localDb.parentFile?.mkdirs()

        // Auto-migration from legacy files if target doesn't exist yet
        if (!localDb.exists()) {
            val legacyName = if (isFactory) "udukkai_kooli.db" else "udukkai_pattu.db"
            val legacyFile = AppContext.context.getDatabasePath(legacyName)
            if (legacyFile.exists()) {
                try {
                    Log.i(tag, "Auto-migrating legacy DB $legacyName -> $dbName")
                    legacyFile.copyTo(localDb, overwrite = true)
                    val legacyWal = File(legacyFile.path + "-wal")
                    if (legacyWal.exists()) legacyWal.copyTo(File(localDb.path + "-wal"), overwrite = true)
                    val legacyShm = File(legacyFile.path + "-shm")
                    if (legacyShm.exists()) legacyShm.copyTo(File(localDb.path + "-shm"), overwrite = true)
                } catch (e: Exception) {
                    Log.e(tag, "Failed to copy legacy DB $legacyName: ${e.message}")
                }
            }
        }
        return localDb
    }

    private fun renameTableIfExists(db: SQLiteDatabase, oldName: String, newName: String) {
        try {
            val curOld = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", arrayOf(oldName))
            val oldExists = curOld.moveToFirst()
            curOld.close()
            if (oldExists) {
                val curNew = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", arrayOf(newName))
                val newExists = curNew.moveToFirst()
                curNew.close()
                if (!newExists) {
                    Log.i(tag, "Renaming legacy table $oldName to $newName")
                    db.execSQL("ALTER TABLE \"$oldName\" RENAME TO \"$newName\"")
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Table rename $oldName -> $newName error: ${e.message}")
        }
    }

    private fun ensureTables(db: SQLiteDatabase) {
        if (isFactory) {
            renameTableIfExists(db, "kooli_vaangunar_table", vaangunarTable)
            renameTableIfExists(db, "kooli_porul_table", porulTable)
            renameTableIfExists(db, "kooli_pattiyal_table", pattiyalTable)
            renameTableIfExists(db, "kooli_patrugal_table", patrugalTable)
            renameTableIfExists(db, "kooli_patru_pattiyal_table", junctionTable)
            renameTableIfExists(db, "kooli_niruvana_tharavugal_table", profileTable)
        } else {
            renameTableIfExists(db, "pattu_vaangunar_table", vaangunarTable)
            renameTableIfExists(db, "pattu_porul_table", porulTable)
            renameTableIfExists(db, "pattu_pattiyal_table", pattiyalTable)
            renameTableIfExists(db, "pattu_patrugal_table", patrugalTable)
            renameTableIfExists(db, "pattu_patru_pattiyal_table", junctionTable)
            renameTableIfExists(db, "pattu_niruvana_tharavugal_table", profileTable)
        }

        // 1. Customers Table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS "$vaangunarTable" (
                "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                "peyar" TEXT NOT NULL DEFAULT '{}',
                "mugavari" TEXT NOT NULL DEFAULT '{}',
                "oor" TEXT NOT NULL DEFAULT '{}',
                "maavattam" TEXT NOT NULL DEFAULT '{}',
                "maanilam" TEXT NOT NULL DEFAULT '{}',
                "naadu" TEXT NOT NULL DEFAULT '{"en": "India", "ta": "இந்தியா"}',
                "velinaad_mugavari" TEXT NOT NULL DEFAULT '{}',
                "anjal_kuriyeedu" TEXT NOT NULL DEFAULT '',
                "gstin" TEXT NOT NULL DEFAULT '',
                "minnanjal" TEXT NOT NULL DEFAULT '',
                "tholaipaesi" TEXT NOT NULL DEFAULT '',
                "created_at" INTEGER NOT NULL DEFAULT (CAST(strftime('%s', CURRENT_TIMESTAMP) AS INTEGER)),
                "updated_at" INTEGER NOT NULL DEFAULT (CAST(strftime('%s', CURRENT_TIMESTAMP) AS INTEGER)),
                "is_deleted" INTEGER NOT NULL DEFAULT 0 CHECK ("is_deleted" IN (0, 1)),
                "deleted_at" INTEGER NULL
            )
        """.trimIndent())

        // 2. Products Table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS "$porulTable" (
                "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                "porul_peyar" TEXT NOT NULL DEFAULT '{}',
                "hsn_code" TEXT NOT NULL DEFAULT '',
                "vilai" REAL NOT NULL DEFAULT 0.0,
                "vari_veetham" REAL NOT NULL DEFAULT 0.0,
                "alavu_vagai" TEXT NOT NULL DEFAULT 'quantity',
                "alagu" TEXT NOT NULL DEFAULT 'Nos',
                "niruvanam_ids" TEXT NOT NULL DEFAULT '[]',
                "company_prices" TEXT NOT NULL DEFAULT '{}',
                "created_at" INTEGER NOT NULL DEFAULT (CAST(strftime('%s', CURRENT_TIMESTAMP) AS INTEGER)),
                "updated_at" INTEGER NOT NULL DEFAULT (CAST(strftime('%s', CURRENT_TIMESTAMP) AS INTEGER)),
                "is_deleted" INTEGER NOT NULL DEFAULT 0 CHECK ("is_deleted" IN (0, 1)),
                "deleted_at" INTEGER NULL
            )
        """.trimIndent())

        // 3. Invoices Table
        val createPattiyalSql = if (isFactory) {
            """
            CREATE TABLE IF NOT EXISTS "$pattiyalTable" (
                "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                "niruvanam_id" INTEGER NULL,
                "patrucheettu_en" TEXT NOT NULL,
                "fin_year" INTEGER NOT NULL,
                "vanakkam" INTEGER NOT NULL DEFAULT 1,
                "pattiyal_vagai" TEXT NOT NULL DEFAULT 'tax-invoice',
                "vaangunar_id" INTEGER NULL,
                "vaangunar_peyar" TEXT NOT NULL DEFAULT '{}',
                "vaangunar_munvari" TEXT NOT NULL DEFAULT '{}',
                "pattiyal_naal" INTEGER NOT NULL DEFAULT (CAST(strftime('%s', CURRENT_TIMESTAMP) AS INTEGER)),
                "tharavugal" TEXT NOT NULL DEFAULT '[]',
                "motha_thogai" REAL NOT NULL DEFAULT 0.0,
                "thallupadi" REAL NOT NULL DEFAULT 0.0,
                "podhu_thallupadi_mathippu" REAL NOT NULL DEFAULT 0.0,
                "podhu_thallupadi_vagai" TEXT NOT NULL DEFAULT '%',
                "podhu_thallupadi_thogai" REAL NOT NULL DEFAULT 0.0,
                "vari_thogai" REAL NOT NULL DEFAULT 0.0,
                "vari_tharavugal" TEXT NOT NULL DEFAULT '{}',
                "motha_edai" REAL NOT NULL DEFAULT 0.0,
                "setharam_grams" REAL NOT NULL DEFAULT 0.0,
                "thabaal_thogai" REAL NOT NULL DEFAULT 0.0,
                "ahimsa_pattu_thogai" REAL NOT NULL DEFAULT 0.0,
                "piravari_vugal" TEXT NOT NULL DEFAULT '[]',
                "sontha_viruppangal" TEXT NOT NULL DEFAULT '{}',
                "nibandhanaigal" TEXT NOT NULL DEFAULT '',
                "ullkurippu" TEXT NOT NULL DEFAULT '',
                "vangi_tharavugal" TEXT NOT NULL DEFAULT '{}',
                "created_at" INTEGER NOT NULL DEFAULT (CAST(strftime('%s', CURRENT_TIMESTAMP) AS INTEGER)),
                "updated_at" INTEGER NOT NULL DEFAULT (CAST(strftime('%s', CURRENT_TIMESTAMP) AS INTEGER)),
                "is_deleted" INTEGER NOT NULL DEFAULT 0 CHECK ("is_deleted" IN (0, 1)),
                "deleted_at" INTEGER NULL
            )
            """.trimIndent()
        } else {
            """
            CREATE TABLE IF NOT EXISTS "$pattiyalTable" (
                "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                "niruvanam_id" INTEGER NULL,
                "patrucheettu_en" TEXT NOT NULL,
                "fin_year" INTEGER NOT NULL,
                "vanakkam" INTEGER NOT NULL DEFAULT 1,
                "pattiyal_vagai" TEXT NOT NULL DEFAULT 'tax-invoice',
                "vaangunar_id" INTEGER NULL,
                "vaangunar_peyar" TEXT NOT NULL DEFAULT '{}',
                "vaangunar_munvari" TEXT NOT NULL DEFAULT '{}',
                "pattiyal_naal" INTEGER NOT NULL DEFAULT (CAST(strftime('%s', CURRENT_TIMESTAMP) AS INTEGER)),
                "tharavugal" TEXT NOT NULL DEFAULT '[]',
                "motha_thogai" REAL NOT NULL DEFAULT 0.0,
                "thallupadi" REAL NOT NULL DEFAULT 0.0,
                "podhu_thallupadi_mathippu" REAL NOT NULL DEFAULT 0.0,
                "podhu_thallupadi_vagai" TEXT NOT NULL DEFAULT '%',
                "podhu_thallupadi_thogai" REAL NOT NULL DEFAULT 0.0,
                "vari_thogai" REAL NOT NULL DEFAULT 0.0,
                "vari_tharavugal" TEXT NOT NULL DEFAULT '{}',
                "sontha_viruppangal" TEXT NOT NULL DEFAULT '{}',
                "nibandhanaigal" TEXT NOT NULL DEFAULT '',
                "ullkurippu" TEXT NOT NULL DEFAULT '',
                "created_at" INTEGER NOT NULL DEFAULT (CAST(strftime('%s', CURRENT_TIMESTAMP) AS INTEGER)),
                "updated_at" INTEGER NOT NULL DEFAULT (CAST(strftime('%s', CURRENT_TIMESTAMP) AS INTEGER)),
                "is_deleted" INTEGER NOT NULL DEFAULT 0 CHECK ("is_deleted" IN (0, 1)),
                "deleted_at" INTEGER NULL
            )
            """.trimIndent()
        }
        db.execSQL(createPattiyalSql)

        // 4. Receipts Table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS "$patrugalTable" (
                "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                "niruvanam_id" INTEGER NULL,
                "patru_en" TEXT NOT NULL,
                "fin_year" TEXT NOT NULL DEFAULT '',
                "vanakkam" INTEGER NOT NULL DEFAULT 1,
                "vaangunar_id" INTEGER NULL,
                "vaangunar_peyar" TEXT NOT NULL DEFAULT '{}',
                "vaangunar_munvari" TEXT NOT NULL DEFAULT '{}',
                "patru_naal" INTEGER NOT NULL DEFAULT (CAST(strftime('%s', CURRENT_TIMESTAMP) AS INTEGER)),
                "thogai" REAL NOT NULL DEFAULT 0.0,
                "seluthum_murai" TEXT NOT NULL DEFAULT 'cash',
                "vangi_peyar" TEXT NULL,
                "parivarthanai_en" TEXT NULL,
                "ullkurippu" TEXT NOT NULL DEFAULT '',
                "created_at" INTEGER NOT NULL DEFAULT (CAST(strftime('%s', CURRENT_TIMESTAMP) AS INTEGER)),
                "updated_at" INTEGER NOT NULL DEFAULT (CAST(strftime('%s', CURRENT_TIMESTAMP) AS INTEGER)),
                "is_deleted" INTEGER NOT NULL DEFAULT 0 CHECK ("is_deleted" IN (0, 1)),
                "deleted_at" INTEGER NULL
            )
        """.trimIndent())

        // 5. Junction Table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS "$junctionTable" (
                "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                "patru_id" INTEGER NOT NULL,
                "pattiyal_id" INTEGER NOT NULL,
                "inaippu_thogai" REAL NOT NULL DEFAULT 0.0,
                "created_at" INTEGER NOT NULL DEFAULT (CAST(strftime('%s', CURRENT_TIMESTAMP) AS INTEGER)),
                FOREIGN KEY("patru_id") REFERENCES "$patrugalTable"("id") ON DELETE CASCADE,
                FOREIGN KEY("pattiyal_id") REFERENCES "$pattiyalTable"("id") ON DELETE CASCADE
            )
        """.trimIndent())

        // 6. Profiles Table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS "$profileTable" (
                "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                "mudhan_mozhi" TEXT NOT NULL DEFAULT 'ta',
                "thunai_mozhi" TEXT NOT NULL DEFAULT 'en',
                "iru_mozhi" INTEGER NOT NULL DEFAULT 1 CHECK ("iru_mozhi" IN (0, 1)),
                "gst_pirippugal" INTEGER NOT NULL DEFAULT 0 CHECK ("gst_pirippugal" IN (0, 1)),
                "niruvanathin_peyar" TEXT NOT NULL DEFAULT '{}',
                "kurum_peyar" TEXT NOT NULL DEFAULT '',
                "tholaipaesi1" TEXT NOT NULL DEFAULT '',
                "tholaipaesi2" TEXT NOT NULL DEFAULT '',
                "minnanjal" TEXT NOT NULL DEFAULT '',
                "gstin" TEXT NOT NULL DEFAULT '',
                "mugavari" TEXT NOT NULL DEFAULT '{}',
                "oor" TEXT NOT NULL DEFAULT '{}',
                "maavattam" TEXT NOT NULL DEFAULT '{}',
                "maanilam" TEXT NOT NULL DEFAULT '{}',
                "naadu" TEXT NOT NULL DEFAULT '{"en": "India", "ta": "இந்தியா"}',
                "anjal_kuriyeedu" TEXT NOT NULL DEFAULT '',
                "vangi_peyar" TEXT NOT NULL DEFAULT '{}',
                "kilai" TEXT NOT NULL DEFAULT '{}',
                "vangi_kanakku" TEXT NOT NULL DEFAULT '',
                "ifsc" TEXT NOT NULL DEFAULT '',
                "oavuru" TEXT NOT NULL DEFAULT '',
                "agala_oavuru" TEXT NOT NULL DEFAULT '',
                "thalaippu_vadivu" TEXT NOT NULL DEFAULT 'small',
                "kaiyoppam" TEXT NOT NULL DEFAULT '',
                "oppam_peyar" TEXT NOT NULL DEFAULT '',
                "oppam_mudhal_peyar" TEXT NOT NULL DEFAULT '',
                "oppam_irudhi_peyar" TEXT NOT NULL DEFAULT '',
                "adaimozhi" TEXT NOT NULL DEFAULT '{}',
                "upi_id" TEXT NOT NULL DEFAULT '',
                "thoatra_niram" TEXT NOT NULL DEFAULT '',
                "created_at" INTEGER NOT NULL DEFAULT (CAST(strftime('%s', CURRENT_TIMESTAMP) AS INTEGER)),
                "updated_at" INTEGER NOT NULL DEFAULT (CAST(strftime('%s', CURRENT_TIMESTAMP) AS INTEGER)),
                "is_deleted" INTEGER NOT NULL DEFAULT 0 CHECK ("is_deleted" IN (0, 1)),
                "deleted_at" INTEGER NULL
            )
        """.trimIndent())
    }

    private inline fun <T> useDb(crossinline block: (SQLiteDatabase) -> T): T? {
        val file = resolveActiveDatabase() ?: return null
        var db: SQLiteDatabase? = null
        return try {
            db = SQLiteDatabase.openOrCreateDatabase(file, null)
            ensureTables(db)
            block(db)
        } catch (e: Exception) {
            Log.e(tag, "Database execution error ($dbName): ${e.message}", e)
            null
        } finally {
            try { db?.close() } catch (_: Exception) {}
        }
    }

    // ── Merchants / Customers ────────────────────────────────────────────────
    override fun loadAllMerchants(): List<CustomerModel> = useDb { db ->
        val list = mutableListOf<CustomerModel>()
        db.rawQuery("SELECT * FROM \"$vaangunarTable\" WHERE is_deleted = 0 ORDER BY id DESC", null).use { c ->
            while (c.moveToNext()) list.add(cursorToMerchant(c))
        }
        list
    } ?: emptyList()

    override fun saveMerchant(merchant: CustomerModel): Long = useDb { db ->
        val cv = ContentValues().apply {
            put("peyar", LanguageJsonConverter.stringify(merchant.name))
            put("mugavari", LanguageJsonConverter.stringify(merchant.address))
            put("oor", LanguageJsonConverter.stringify(merchant.city))
            put("maavattam", LanguageJsonConverter.stringify(merchant.district))
            put("maanilam", LanguageJsonConverter.stringify(merchant.state))
            put("naadu", LanguageJsonConverter.stringify(merchant.country))
            put("velinaad_mugavari", LanguageJsonConverter.stringify(merchant.velinaadMugavari))
            put("anjal_kuriyeedu", merchant.pinCode)
            put("gstin", merchant.gstin)
            put("minnanjal", merchant.email)
            put("tholaipaesi", merchant.phone)
            put("updated_at", System.currentTimeMillis() / 1000)
            put("is_deleted", 0)
        }
        if (merchant.id != null && merchant.id > 0) {
            db.update("\"$vaangunarTable\"", cv, "id = ?", arrayOf(merchant.id.toString()))
            merchant.id
        } else {
            cv.put("created_at", System.currentTimeMillis() / 1000)
            db.insert("\"$vaangunarTable\"", null, cv)
        }
    } ?: -1L

    override fun deleteMerchant(id: Long): Boolean = useDb { db ->
        val cv = ContentValues().apply {
            put("is_deleted", 1)
            put("deleted_at", System.currentTimeMillis() / 1000)
        }
        db.update("\"$vaangunarTable\"", cv, "id = ?", arrayOf(id.toString())) > 0
    } ?: false

    override fun loadDeletedMerchants(): List<CustomerModel> = useDb { db ->
        val list = mutableListOf<CustomerModel>()
        db.rawQuery("SELECT * FROM \"$vaangunarTable\" WHERE is_deleted = 1 ORDER BY deleted_at DESC", null).use { c ->
            while (c.moveToNext()) list.add(cursorToMerchant(c))
        }
        list
    } ?: emptyList()

    override fun restoreMerchant(id: Long): Boolean = useDb { db ->
        val cv = ContentValues().apply {
            put("is_deleted", 0)
            putNull("deleted_at")
        }
        db.update("\"$vaangunarTable\"", cv, "id = ?", arrayOf(id.toString())) > 0
    } ?: false

    override fun permanentDeleteMerchant(id: Long): Boolean = useDb { db ->
        db.delete("\"$vaangunarTable\"", "id = ?", arrayOf(id.toString())) > 0
    } ?: false

    override fun purgeExpiredMerchants(days: Int): Int = useDb { db ->
        val threshold = (System.currentTimeMillis() / 1000) - (days * 86400L)
        db.delete("\"$vaangunarTable\"", "is_deleted = 1 AND deleted_at < ?", arrayOf(threshold.toString()))
    } ?: 0

    // ── Products / Items ─────────────────────────────────────────────────────
    override fun loadAllItems(): List<ProductModel> = useDb { db ->
        val list = mutableListOf<ProductModel>()
        db.rawQuery("SELECT * FROM \"$porulTable\" WHERE is_deleted = 0 ORDER BY id DESC", null).use { c ->
            while (c.moveToNext()) list.add(cursorToItem(c))
        }
        list
    } ?: emptyList()

    override fun saveItem(item: ProductModel): Long = useDb { db ->
        val cv = ContentValues().apply {
            put("porul_peyar", LanguageJsonConverter.stringify(item.name))
            put("hsn_code", item.hsnCode)
            put("vilai", item.price)
            put("vari_veetham", item.taxRate)
            put("alavu_vagai", item.quantityType)
            put("alagu", item.unit)
            put("niruvanam_ids", item.companyIds.toString())
            put("company_prices", item.companyPrices.toString())
            put("updated_at", System.currentTimeMillis() / 1000)
            put("is_deleted", 0)
        }
        if (item.id != null && item.id > 0) {
            db.update("\"$porulTable\"", cv, "id = ?", arrayOf(item.id.toString()))
            item.id
        } else {
            cv.put("created_at", System.currentTimeMillis() / 1000)
            db.insert("\"$porulTable\"", null, cv)
        }
    } ?: -1L

    override fun deleteItem(id: Long): Boolean = useDb { db ->
        val cv = ContentValues().apply {
            put("is_deleted", 1)
            put("deleted_at", System.currentTimeMillis() / 1000)
        }
        db.update("\"$porulTable\"", cv, "id = ?", arrayOf(id.toString())) > 0
    } ?: false

    override fun loadDeletedItems(): List<ProductModel> = useDb { db ->
        val list = mutableListOf<ProductModel>()
        db.rawQuery("SELECT * FROM \"$porulTable\" WHERE is_deleted = 1 ORDER BY deleted_at DESC", null).use { c ->
            while (c.moveToNext()) list.add(cursorToItem(c))
        }
        list
    } ?: emptyList()

    override fun restoreItem(id: Long): Boolean = useDb { db ->
        val cv = ContentValues().apply {
            put("is_deleted", 0)
            putNull("deleted_at")
        }
        db.update("\"$porulTable\"", cv, "id = ?", arrayOf(id.toString())) > 0
    } ?: false

    override fun permanentDeleteItem(id: Long): Boolean = useDb { db ->
        db.delete("\"$porulTable\"", "id = ?", arrayOf(id.toString())) > 0
    } ?: false

    override fun purgeExpiredItems(days: Int): Int = useDb { db ->
        val threshold = (System.currentTimeMillis() / 1000) - (days * 86400L)
        db.delete("\"$porulTable\"", "is_deleted = 1 AND deleted_at < ?", arrayOf(threshold.toString()))
    } ?: 0

    // ── Invoices ─────────────────────────────────────────────────────────────
    override fun loadAllInvoices(): List<InvoiceModel> = useDb { db ->
        val list = mutableListOf<InvoiceModel>()
        db.rawQuery("SELECT * FROM \"$pattiyalTable\" WHERE is_deleted = 0 ORDER BY pattiyal_naal DESC, id DESC", null).use { c ->
            while (c.moveToNext()) list.add(cursorToInvoice(c))
        }
        list
    } ?: emptyList()

    override fun saveInvoice(invoice: InvoiceModel): Long = useDb { db ->
        val cv = ContentValues().apply {
            put("niruvanam_id", invoice.companyId)
            put("patrucheettu_en", invoice.invoiceNumber)
            put("fin_year", invoice.finYear)
            put("vanakkam", invoice.vanakkam)
            put("pattiyal_vagai", invoice.invoiceType)
            put("vaangunar_id", invoice.customerId)
            put("vaangunar_peyar", LanguageJsonConverter.stringify(invoice.customerName))
            put("vaangunar_munvari", LanguageJsonConverter.stringify(invoice.customerAddress))
            put("pattiyal_naal", invoice.invoiceDate)
            put("tharavugal", invoice.itemsJson.toString())
            put("motha_thogai", invoice.totalAmount)
            put("thallupadi", invoice.discount)
            put("podhu_thallupadi_mathippu", invoice.globalDiscountValue)
            put("podhu_thallupadi_vagai", invoice.globalDiscountType)
            put("podhu_thallupadi_thogai", invoice.globalDiscountAmount)
            put("vari_thogai", invoice.taxAmount)
            put("vari_tharavugal", invoice.taxDataJson.toString())
            if (isFactory) {
                put("motha_edai", invoice.mothaEdai)
                put("setharam_grams", invoice.wastageGrams)
                put("thabaal_thogai", invoice.thabaalThogai)
                put("ahimsa_pattu_thogai", invoice.ahimsaSilkAmount)
                put("piravari_vugal", invoice.piravariVugal.toString())
                put("vangi_tharavugal", invoice.bankDetailsJson.toString())
            }
            put("sontha_viruppangal", invoice.sonthaViruppangal)
            put("nibandhanaigal", invoice.nibandhanaigal)
            put("ullkurippu", invoice.notes)
            put("updated_at", System.currentTimeMillis() / 1000)
            put("is_deleted", 0)
        }
        if (invoice.id != null && invoice.id > 0) {
            db.update("\"$pattiyalTable\"", cv, "id = ?", arrayOf(invoice.id.toString()))
            invoice.id
        } else {
            cv.put("created_at", System.currentTimeMillis() / 1000)
            db.insert("\"$pattiyalTable\"", null, cv)
        }
    } ?: -1L

    override fun deleteInvoice(id: Long): Boolean = useDb { db ->
        val cv = ContentValues().apply {
            put("is_deleted", 1)
            put("deleted_at", System.currentTimeMillis() / 1000)
        }
        db.update("\"$pattiyalTable\"", cv, "id = ?", arrayOf(id.toString())) > 0
    } ?: false

    override fun loadDeletedInvoices(): List<InvoiceModel> = useDb { db ->
        val list = mutableListOf<InvoiceModel>()
        db.rawQuery("SELECT * FROM \"$pattiyalTable\" WHERE is_deleted = 1 ORDER BY deleted_at DESC", null).use { c ->
            while (c.moveToNext()) list.add(cursorToInvoice(c))
        }
        list
    } ?: emptyList()

    override fun restoreInvoice(id: Long): Boolean = useDb { db ->
        val cv = ContentValues().apply {
            put("is_deleted", 0)
            putNull("deleted_at")
        }
        db.update("\"$pattiyalTable\"", cv, "id = ?", arrayOf(id.toString())) > 0
    } ?: false

    override fun permanentDeleteInvoice(id: Long): Boolean = useDb { db ->
        db.delete("\"$pattiyalTable\"", "id = ?", arrayOf(id.toString())) > 0
    } ?: false

    override fun purgeExpiredInvoices(days: Int): Int = useDb { db ->
        val threshold = (System.currentTimeMillis() / 1000) - (days * 86400L)
        db.delete("\"$pattiyalTable\"", "is_deleted = 1 AND deleted_at < ?", arrayOf(threshold.toString()))
    } ?: 0

    // ── Receipts ─────────────────────────────────────────────────────────────
    override fun loadAllReceipts(): List<ReceiptModel> = useDb { db ->
        val list = mutableListOf<ReceiptModel>()
        db.rawQuery("SELECT * FROM \"$patrugalTable\" WHERE is_deleted = 0 ORDER BY patru_naal DESC, id DESC", null).use { c ->
            while (c.moveToNext()) list.add(cursorToReceipt(c))
        }
        list
    } ?: emptyList()

    override fun saveReceipt(receipt: ReceiptModel): Long = useDb { db ->
        val cv = ContentValues().apply {
            put("niruvanam_id", receipt.companyId)
            put("patru_en", receipt.receiptNumber)
            put("fin_year", receipt.finYear)
            put("vanakkam", receipt.vanakkam)
            put("vaangunar_id", receipt.customerId)
            put("vaangunar_peyar", LanguageJsonConverter.stringify(receipt.customerName))
            put("vaangunar_munvari", LanguageJsonConverter.stringify(receipt.customerAddress))
            put("patru_naal", receipt.receiptDate)
            put("thogai", receipt.amount)
            put("seluthum_murai", receipt.seluthumMurai)
            put("vangi_peyar", receipt.bankName)
            put("parivarthanai_en", receipt.transactionNumber)
            put("ullkurippu", receipt.notes)
            put("updated_at", System.currentTimeMillis() / 1000)
            put("is_deleted", 0)
        }
        if (receipt.id != null && receipt.id > 0) {
            db.update("\"$patrugalTable\"", cv, "id = ?", arrayOf(receipt.id.toString()))
            receipt.id
        } else {
            cv.put("created_at", System.currentTimeMillis() / 1000)
            db.insert("\"$patrugalTable\"", null, cv)
        }
    } ?: -1L

    override fun deleteReceipt(id: Long): Boolean = useDb { db ->
        val cv = ContentValues().apply {
            put("is_deleted", 1)
            put("deleted_at", System.currentTimeMillis() / 1000)
        }
        db.update("\"$patrugalTable\"", cv, "id = ?", arrayOf(id.toString())) > 0
    } ?: false

    override fun loadDeletedReceipts(): List<ReceiptModel> = useDb { db ->
        val list = mutableListOf<ReceiptModel>()
        db.rawQuery("SELECT * FROM \"$patrugalTable\" WHERE is_deleted = 1 ORDER BY deleted_at DESC", null).use { c ->
            while (c.moveToNext()) list.add(cursorToReceipt(c))
        }
        list
    } ?: emptyList()

    override fun restoreReceipt(id: Long): Boolean = useDb { db ->
        val cv = ContentValues().apply {
            put("is_deleted", 0)
            putNull("deleted_at")
        }
        db.update("\"$patrugalTable\"", cv, "id = ?", arrayOf(id.toString())) > 0
    } ?: false

    override fun permanentDeleteReceipt(id: Long): Boolean = useDb { db ->
        db.delete("\"$patrugalTable\"", "id = ?", arrayOf(id.toString())) > 0
    } ?: false

    override fun purgeExpiredReceipts(days: Int): Int = useDb { db ->
        val threshold = (System.currentTimeMillis() / 1000) - (days * 86400L)
        db.delete("\"$patrugalTable\"", "is_deleted = 1 AND deleted_at < ?", arrayOf(threshold.toString()))
    } ?: 0

    // ── Junction Links ───────────────────────────────────────────────────────
    override fun getLinksForPatru(patruId: Long): List<ReceiptInvoiceLinkModel> = useDb { db ->
        val list = mutableListOf<ReceiptInvoiceLinkModel>()
        db.rawQuery("SELECT * FROM \"$junctionTable\" WHERE patru_id = ?", arrayOf(patruId.toString())).use { c ->
            val idIdx = c.getColumnIndexOrThrow("id")
            val pIdIdx = c.getColumnIndexOrThrow("patru_id")
            val invIdIdx = c.getColumnIndexOrThrow("pattiyal_id")
            val amountIdx = c.getColumnIndexOrThrow("inaippu_thogai")
            val dateIdx = c.getColumnIndexOrThrow("created_at")
            while (c.moveToNext()) {
                list.add(ReceiptInvoiceLinkModel(
                    id = c.getLong(idIdx),
                    receiptId = c.getLong(pIdIdx),
                    invoiceId = c.getLong(invIdIdx),
                    appliedAmount = c.getDouble(amountIdx)
                ))
            }
        }
        list
    } ?: emptyList()

    override fun saveReceiptWithLinks(receipt: ReceiptModel, links: List<ReceiptInvoiceLinkModel>): Long = useDb { db ->
        db.beginTransaction()
        try {
            val rId = saveReceipt(receipt)
            if (rId > 0) {
                db.delete("\"$junctionTable\"", "patru_id = ?", arrayOf(rId.toString()))
                for (link in links) {
                    val cv = ContentValues().apply {
                        put("patru_id", rId)
                        put("pattiyal_id", link.invoiceId)
                        put("inaippu_thogai", link.appliedAmount)
                        put("created_at", System.currentTimeMillis() / 1000)
                    }
                    db.insert("\"$junctionTable\"", null, cv)
                }
                db.setTransactionSuccessful()
            }
            rId
        } finally {
            db.endTransaction()
        }
    } ?: -1L

    override fun getPaidAmountForInvoice(invoiceId: Long): Double = useDb { db ->
        var sum = 0.0
        db.rawQuery("SELECT SUM(inaippu_thogai) FROM \"$junctionTable\" WHERE pattiyal_id = ?", arrayOf(invoiceId.toString())).use { c ->
            if (c.moveToFirst() && !c.isNull(0)) sum = c.getDouble(0)
        }
        sum
    } ?: 0.0

    override fun getPaidAmountsForInvoices(invoiceIds: List<Long>): Map<Long, Double> = useDb { db ->
        val result = mutableMapOf<Long, Double>()
        if (invoiceIds.isEmpty()) return@useDb result
        val placeholders = invoiceIds.joinToString(",") { "?" }
        val args = invoiceIds.map { it.toString() }.toTypedArray()
        db.rawQuery("SELECT pattiyal_id, SUM(inaippu_thogai) FROM \"$junctionTable\" WHERE pattiyal_id IN ($placeholders) GROUP BY pattiyal_id", args).use { c ->
            while (c.moveToNext()) {
                result[c.getLong(0)] = c.getDouble(1)
            }
        }
        result
    } ?: emptyMap()

    // ── Business Profiles ────────────────────────────────────────────────────
    override fun loadProfile(profileId: Long?): BusinessProfile? = useDb { db ->
        var p: BusinessProfile? = null
        val sql = if (profileId != null) {
            "SELECT * FROM \"$profileTable\" WHERE id = ? AND is_deleted = 0 LIMIT 1"
        } else {
            "SELECT * FROM \"$profileTable\" WHERE is_deleted = 0 ORDER BY id ASC LIMIT 1"
        }
        val args = if (profileId != null) arrayOf(profileId.toString()) else null
        db.rawQuery(sql, args).use { c ->
            if (c.moveToFirst()) p = cursorToProfile(c)
        }
        p
    }

    override fun loadAllProfiles(): List<BusinessProfile> = useDb { db ->
        val list = mutableListOf<BusinessProfile>()
        db.rawQuery("SELECT * FROM \"$profileTable\" WHERE is_deleted = 0 ORDER BY id ASC", null).use { c ->
            while (c.moveToNext()) list.add(cursorToProfile(c))
        }
        list
    } ?: emptyList()

    override fun saveProfile(profile: BusinessProfile): Boolean = useDb { db ->
        val cv = ContentValues().apply {
            put("mudhan_mozhi", profile.primaryLanguage)
            put("thunai_mozhi", profile.thunaiMozhi)
            put("iru_mozhi", if (profile.isBilingual) 1 else 0)
            put("gst_pirippugal", if (profile.gstPirippugal) 1 else 0)
            put("niruvanathin_peyar", LanguageJsonConverter.stringify(profile.businessName))
            put("kurum_peyar", profile.kurumPeyar)
            put("tholaipaesi1", profile.phone1)
            put("tholaipaesi2", profile.phone2)
            put("minnanjal", profile.email)
            put("gstin", profile.gstin)
            put("mugavari", LanguageJsonConverter.stringify(profile.address))
            put("oor", LanguageJsonConverter.stringify(profile.city))
            put("maavattam", LanguageJsonConverter.stringify(profile.district))
            put("maanilam", LanguageJsonConverter.stringify(profile.state))
            put("naadu", LanguageJsonConverter.stringify(profile.country))
            put("anjal_kuriyeedu", profile.pinCode)
            put("vangi_peyar", LanguageJsonConverter.stringify(profile.bankName))
            put("kilai", LanguageJsonConverter.stringify(profile.branch))
            put("vangi_kanakku", profile.vangiKanakku)
            put("ifsc", profile.ifsc)
            put("oavuru", profile.logo)
            put("agala_oavuru", profile.wideLogo)
            put("thalaippu_vadivu", profile.headerLayout)
            put("kaiyoppam", profile.signature)
            put("oppam_peyar", profile.oppamPeyar)
            put("oppam_mudhal_peyar", profile.oppamMudhalPeyar)
            put("oppam_irudhi_peyar", profile.oppamIrudhiPeyar)
            put("adaimozhi", LanguageJsonConverter.stringify(profile.adaimozhi))
            put("upi_id", profile.upiId)
            put("thoatra_niram", profile.themeColor)
            put("updated_at", System.currentTimeMillis() / 1000)
            put("is_deleted", 0)
        }
        val exists = if (profile.id != null) {
            val checkCur = db.rawQuery("SELECT id FROM \"$profileTable\" WHERE id = ?", arrayOf(profile.id.toString()))
            val found = checkCur.moveToFirst()
            checkCur.close()
            found
        } else false

        if (exists) {
            db.update("\"$profileTable\"", cv, "id = ?", arrayOf(profile.id.toString())) > 0
        } else {
            cv.put("created_at", System.currentTimeMillis() / 1000)
            if (profile.id != null) cv.put("id", profile.id)
            db.insert("\"$profileTable\"", null, cv) > 0
        }
    } ?: false

    override fun deleteProfile(profileId: Long): Boolean = useDb { db ->
        val cv = ContentValues().apply {
            put("is_deleted", 1)
            put("deleted_at", System.currentTimeMillis() / 1000)
        }
        db.update("\"$profileTable\"", cv, "id = ?", arrayOf(profileId.toString())) > 0
    } ?: false

    override fun clearProfiles(): Boolean = useDb { db ->
        db.delete("\"$profileTable\"", null, null) > 0
    } ?: false

    override fun clearAllData(): Boolean = useDb { db ->
        db.delete("\"$junctionTable\"", null, null)
        db.delete("\"$patrugalTable\"", null, null)
        db.delete("\"$pattiyalTable\"", null, null)
        db.delete("\"$porulTable\"", null, null)
        db.delete("\"$vaangunarTable\"", null, null)
        db.delete("\"$profileTable\"", null, null)
        true
    } ?: false

    // ── Helper Cursor Parsers ────────────────────────────────────────────────
    private fun cursorToMerchant(c: Cursor): CustomerModel = CustomerModel(
        id = c.getLong(c.getColumnIndexOrThrow("id")),
        name = LanguageJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("peyar"))),
        address = LanguageJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("mugavari"))),
        city = LanguageJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("oor"))),
        district = LanguageJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("maavattam"))),
        state = LanguageJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("maanilam"))),
        country = LanguageJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("naadu"))),
        internationalAddress = LanguageJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("velinaad_mugavari"))),
        pinCode = c.getString(c.getColumnIndexOrThrow("anjal_kuriyeedu")),
        gstin = c.getString(c.getColumnIndexOrThrow("gstin")),
        email = c.getString(c.getColumnIndexOrThrow("minnanjal")),
        phone = c.getString(c.getColumnIndexOrThrow("tholaipaesi")),
        createdAt = c.getLong(c.getColumnIndexOrThrow("created_at")),
        updatedAt = c.getLong(c.getColumnIndexOrThrow("updated_at")),
        isDeleted = c.getInt(c.getColumnIndexOrThrow("is_deleted")) == 1,
        deletedAt = if (c.isNull(c.getColumnIndexOrThrow("deleted_at"))) null else c.getLong(c.getColumnIndexOrThrow("deleted_at"))
    )

    private fun cursorToItem(c: Cursor): ProductModel = ProductModel(
        id = c.getLong(c.getColumnIndexOrThrow("id")),
        name = LanguageJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("porul_peyar"))),
        hsnCode = c.getString(c.getColumnIndexOrThrow("hsn_code")),
        price = c.getDouble(c.getColumnIndexOrThrow("vilai")),
        taxRate = c.getDouble(c.getColumnIndexOrThrow("vari_veetham")),
        quantityType = c.getString(c.getColumnIndexOrThrow("alavu_vagai")),
        unit = c.getString(c.getColumnIndexOrThrow("alagu")),
        companyIds = PorulJsonConverter.parseLongList(c.getString(c.getColumnIndexOrThrow("niruvanam_ids"))),
        companyPrices = PorulJsonConverter.parseCompanyPrices(c.getString(c.getColumnIndexOrThrow("company_prices"))),
        createdAt = c.getLong(c.getColumnIndexOrThrow("created_at")),
        updatedAt = c.getLong(c.getColumnIndexOrThrow("updated_at")),
        isDeleted = c.getInt(c.getColumnIndexOrThrow("is_deleted")) == 1,
        deletedAt = if (c.isNull(c.getColumnIndexOrThrow("deleted_at"))) null else c.getLong(c.getColumnIndexOrThrow("deleted_at"))
    )

    private fun cursorToInvoice(c: Cursor): InvoiceModel = InvoiceModel(
        id = c.getLong(c.getColumnIndexOrThrow("id")),
        companyId = if (c.isNull(c.getColumnIndexOrThrow("niruvanam_id"))) null else c.getLong(c.getColumnIndexOrThrow("niruvanam_id")),
        invoiceNumber = c.getString(c.getColumnIndexOrThrow("patrucheettu_en")),
        finYear = c.getInt(c.getColumnIndexOrThrow("fin_year")),
        vanakkam = c.getInt(c.getColumnIndexOrThrow("vanakkam")),
        invoiceType = c.getString(c.getColumnIndexOrThrow("pattiyal_vagai")),
        customerId = if (c.isNull(c.getColumnIndexOrThrow("vaangunar_id"))) null else c.getLong(c.getColumnIndexOrThrow("vaangunar_id")),
        customerName = LanguageJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("vaangunar_peyar"))),
        customerAddress = LanguageJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("vaangunar_munvari"))),
        invoiceDate = c.getLong(c.getColumnIndexOrThrow("pattiyal_naal")),
        itemsJson = c.getString(c.getColumnIndexOrThrow("tharavugal")) ?: "[]",
        totalAmount = c.getDouble(c.getColumnIndexOrThrow("motha_thogai")),
        discount = c.getDouble(c.getColumnIndexOrThrow("thallupadi")),
        globalDiscountValue = c.getDouble(c.getColumnIndexOrThrow("podhu_thallupadi_mathippu")),
        globalDiscountType = c.getString(c.getColumnIndexOrThrow("podhu_thallupadi_vagai")),
        globalDiscountAmount = c.getDouble(c.getColumnIndexOrThrow("podhu_thallupadi_thogai")),
        taxAmount = c.getDouble(c.getColumnIndexOrThrow("vari_thogai")),
        taxDataJson = c.getString(c.getColumnIndexOrThrow("vari_tharavugal")) ?: "{}",
        totalWeight = if (isFactory && c.getColumnIndex("motha_edai") >= 0 && !c.isNull(c.getColumnIndex("motha_edai"))) c.getDouble(c.getColumnIndex("motha_edai")) else 0.0,
        wastageGrams = if (isFactory && c.getColumnIndex("setharam_grams") >= 0 && !c.isNull(c.getColumnIndex("setharam_grams"))) c.getDouble(c.getColumnIndex("setharam_grams")) else 0.0,
        postalCharges = if (isFactory && c.getColumnIndex("thabaal_thogai") >= 0 && !c.isNull(c.getColumnIndex("thabaal_thogai"))) c.getDouble(c.getColumnIndex("thabaal_thogai")) else 0.0,
        ahimsaSilkAmount = if (isFactory && c.getColumnIndex("ahimsa_pattu_thogai") >= 0 && !c.isNull(c.getColumnIndex("ahimsa_pattu_thogai"))) c.getDouble(c.getColumnIndex("ahimsa_pattu_thogai")) else 0.0,
        otherTaxesJson = if (isFactory && c.getColumnIndex("piravari_vugal") >= 0) (c.getString(c.getColumnIndex("piravari_vugal")) ?: "[]") else "[]",
        customPreferencesJson = c.getString(c.getColumnIndexOrThrow("sontha_viruppangal")) ?: "{}",
        termsAndConditions = c.getString(c.getColumnIndexOrThrow("nibandhanaigal")),
        notes = c.getString(c.getColumnIndexOrThrow("ullkurippu")),
        bankDetailsJson = if (isFactory && c.getColumnIndex("vangi_tharavugal") >= 0) (c.getString(c.getColumnIndex("vangi_tharavugal")) ?: "{}") else "{}",
        createdAt = c.getLong(c.getColumnIndexOrThrow("created_at")),
        updatedAt = c.getLong(c.getColumnIndexOrThrow("updated_at")),
        isDeleted = c.getInt(c.getColumnIndexOrThrow("is_deleted")) == 1,
        deletedAt = if (c.isNull(c.getColumnIndexOrThrow("deleted_at"))) null else c.getLong(c.getColumnIndexOrThrow("deleted_at"))
    )

    private fun cursorToReceipt(c: Cursor): ReceiptModel = ReceiptModel(
        id = c.getLong(c.getColumnIndexOrThrow("id")),
        companyId = if (c.isNull(c.getColumnIndexOrThrow("niruvanam_id"))) null else c.getLong(c.getColumnIndexOrThrow("niruvanam_id")),
        receiptNumber = c.getString(c.getColumnIndexOrThrow("patru_en")),
        finYear = c.getString(c.getColumnIndexOrThrow("fin_year")),
        vanakkam = c.getInt(c.getColumnIndexOrThrow("vanakkam")),
        customerId = if (c.isNull(c.getColumnIndexOrThrow("vaangunar_id"))) null else c.getLong(c.getColumnIndexOrThrow("vaangunar_id")),
        customerName = LanguageJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("vaangunar_peyar"))),
        customerAddress = LanguageJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("vaangunar_munvari"))),
        receiptDate = c.getLong(c.getColumnIndexOrThrow("patru_naal")),
        amount = c.getDouble(c.getColumnIndexOrThrow("thogai")),
        paymentMode = c.getString(c.getColumnIndexOrThrow("seluthum_murai")),
        bankName = if (c.isNull(c.getColumnIndexOrThrow("vangi_peyar"))) null else c.getString(c.getColumnIndexOrThrow("vangi_peyar")),
        transactionNumber = if (c.isNull(c.getColumnIndexOrThrow("parivarthanai_en"))) null else c.getString(c.getColumnIndexOrThrow("parivarthanai_en")),
        notes = c.getString(c.getColumnIndexOrThrow("ullkurippu")),
        createdAt = c.getLong(c.getColumnIndexOrThrow("created_at")),
        updatedAt = c.getLong(c.getColumnIndexOrThrow("updated_at")),
        isDeleted = c.getInt(c.getColumnIndexOrThrow("is_deleted")) == 1,
        deletedAt = if (c.isNull(c.getColumnIndexOrThrow("deleted_at"))) null else c.getLong(c.getColumnIndexOrThrow("deleted_at"))
    )

    private fun cursorToProfile(c: Cursor): BusinessProfile = BusinessProfile(
        id = c.getLong(c.getColumnIndexOrThrow("id")),
        primaryLanguage = c.getString(c.getColumnIndexOrThrow("mudhan_mozhi")),
        secondaryLanguage = c.getString(c.getColumnIndexOrThrow("thunai_mozhi")),
        isBilingual = c.getInt(c.getColumnIndexOrThrow("iru_mozhi")) == 1,
        splitGst = c.getInt(c.getColumnIndexOrThrow("gst_pirippugal")) == 1,
        businessName = LanguageJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("niruvanathin_peyar"))),
        shortName = c.getString(c.getColumnIndexOrThrow("kurum_peyar")),
        phone1 = c.getString(c.getColumnIndexOrThrow("tholaipaesi1")),
        phone2 = c.getString(c.getColumnIndexOrThrow("tholaipaesi2")),
        email = c.getString(c.getColumnIndexOrThrow("minnanjal")),
        gstin = c.getString(c.getColumnIndexOrThrow("gstin")),
        address = LanguageJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("mugavari"))),
        city = LanguageJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("oor"))),
        district = LanguageJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("maavattam"))),
        state = LanguageJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("maanilam"))),
        country = LanguageJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("naadu"))),
        pinCode = c.getString(c.getColumnIndexOrThrow("anjal_kuriyeedu")),
        bankName = LanguageJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("vangi_peyar"))),
        branch = LanguageJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("kilai"))),
        accountNumber = c.getString(c.getColumnIndexOrThrow("vangi_kanakku")),
        ifsc = c.getString(c.getColumnIndexOrThrow("ifsc")),
        logo = c.getString(c.getColumnIndexOrThrow("oavuru")),
        wideLogo = c.getString(c.getColumnIndexOrThrow("agala_oavuru")),
        headerLayout = c.getString(c.getColumnIndexOrThrow("thalaippu_vadivu")),
        signature = c.getString(c.getColumnIndexOrThrow("kaiyoppam")),
        signatoryName = c.getString(c.getColumnIndexOrThrow("oppam_peyar")),
        signatoryFirstName = c.getString(c.getColumnIndexOrThrow("oppam_mudhal_peyar")),
        signatoryLastName = c.getString(c.getColumnIndexOrThrow("oppam_irudhi_peyar")),
        tagline = LanguageJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("adaimozhi"))),
        upiId = c.getString(c.getColumnIndexOrThrow("upi_id")),
        themeColor = c.getString(c.getColumnIndexOrThrow("thoatra_niram"))
    )
}

private val factoryDbHelperInstance by lazy {
    AndroidUdukkaiDatabaseHelper(
        dbName = "udukkai_factory.db",
        tablePrefix = "factory_",
        isFactory = true
    )
}

private val textilesDbHelperInstance by lazy {
    AndroidUdukkaiDatabaseHelper(
        dbName = "udukkai_textiles.db",
        tablePrefix = "textiles_",
        isFactory = false
    )
}

actual fun getFactoryDatabaseHelper(): UdukkaiDatabaseHelper = factoryDbHelperInstance
actual fun getTextilesDatabaseHelper(): UdukkaiDatabaseHelper = textilesDbHelperInstance
