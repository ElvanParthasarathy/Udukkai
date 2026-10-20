package com.elvan.udukkai.data.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.elvan.udukkai.core.platform.AppContext
import com.elvan.udukkai.data.model.PattiyalTharavuru
import com.elvan.udukkai.data.model.PatruPattiyalInaippuTharavuru
import com.elvan.udukkai.data.model.PatrugalTharavuru
import com.elvan.udukkai.data.model.PorulJsonConverter
import com.elvan.udukkai.data.model.PorulTharavuru
import com.elvan.udukkai.data.model.VaangunarTharavuru
import com.elvan.udukkai.data.settings.MozhiJsonConverter
import com.elvan.udukkai.data.settings.NiruvanaTharavugal
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
    override fun loadAllMerchants(): List<VaangunarTharavuru> = useDb { db ->
        val list = mutableListOf<VaangunarTharavuru>()
        db.rawQuery("SELECT * FROM \"$vaangunarTable\" WHERE is_deleted = 0 ORDER BY id DESC", null).use { c ->
            while (c.moveToNext()) list.add(cursorToMerchant(c))
        }
        list
    } ?: emptyList()

    override fun saveMerchant(merchant: VaangunarTharavuru): Long = useDb { db ->
        val cv = ContentValues().apply {
            put("peyar", MozhiJsonConverter.stringify(merchant.peyar))
            put("mugavari", MozhiJsonConverter.stringify(merchant.mugavari))
            put("oor", MozhiJsonConverter.stringify(merchant.oor))
            put("maavattam", MozhiJsonConverter.stringify(merchant.maavattam))
            put("maanilam", MozhiJsonConverter.stringify(merchant.maanilam))
            put("naadu", MozhiJsonConverter.stringify(merchant.naadu))
            put("velinaad_mugavari", MozhiJsonConverter.stringify(merchant.velinaadMugavari))
            put("anjal_kuriyeedu", merchant.anjalKuriyeedu)
            put("gstin", merchant.gstin)
            put("minnanjal", merchant.minnanjal)
            put("tholaipaesi", merchant.tholaipaesi)
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

    override fun loadDeletedMerchants(): List<VaangunarTharavuru> = useDb { db ->
        val list = mutableListOf<VaangunarTharavuru>()
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
    override fun loadAllItems(): List<PorulTharavuru> = useDb { db ->
        val list = mutableListOf<PorulTharavuru>()
        db.rawQuery("SELECT * FROM \"$porulTable\" WHERE is_deleted = 0 ORDER BY id DESC", null).use { c ->
            while (c.moveToNext()) list.add(cursorToItem(c))
        }
        list
    } ?: emptyList()

    override fun saveItem(item: PorulTharavuru): Long = useDb { db ->
        val cv = ContentValues().apply {
            put("porul_peyar", MozhiJsonConverter.stringify(item.porulPeyar))
            put("hsn_code", item.hsnCode)
            put("vilai", item.vilai)
            put("vari_veetham", item.variVeetham)
            put("alavu_vagai", item.alavuVagai)
            put("alagu", item.alagu)
            put("niruvanam_ids", item.niruvanamIds.toString())
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

    override fun loadDeletedItems(): List<PorulTharavuru> = useDb { db ->
        val list = mutableListOf<PorulTharavuru>()
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
    override fun loadAllInvoices(): List<PattiyalTharavuru> = useDb { db ->
        val list = mutableListOf<PattiyalTharavuru>()
        db.rawQuery("SELECT * FROM \"$pattiyalTable\" WHERE is_deleted = 0 ORDER BY pattiyal_naal DESC, id DESC", null).use { c ->
            while (c.moveToNext()) list.add(cursorToInvoice(c))
        }
        list
    } ?: emptyList()

    override fun saveInvoice(invoice: PattiyalTharavuru): Long = useDb { db ->
        val cv = ContentValues().apply {
            put("niruvanam_id", invoice.niruvanamId)
            put("patrucheettu_en", invoice.patrucheettuEn)
            put("fin_year", invoice.finYear)
            put("vanakkam", invoice.vanakkam)
            put("pattiyal_vagai", invoice.pattiyalVagai)
            put("vaangunar_id", invoice.vaangunarId)
            put("vaangunar_peyar", MozhiJsonConverter.stringify(invoice.vaangunarPeyar))
            put("vaangunar_munvari", MozhiJsonConverter.stringify(invoice.vaangunarMunvari))
            put("pattiyal_naal", invoice.pattiyalNaal)
            put("tharavugal", invoice.tharavugal.toString())
            put("motha_thogai", invoice.mothaThogai)
            put("thallupadi", invoice.thallupadi)
            put("podhu_thallupadi_mathippu", invoice.podhuThallupadiMathippu)
            put("podhu_thallupadi_vagai", invoice.podhuThallupadiVagai)
            put("podhu_thallupadi_thogai", invoice.podhuThallupadiThogai)
            put("vari_thogai", invoice.variThogai)
            put("vari_tharavugal", invoice.variTharavugal.toString())
            if (isFactory) {
                put("motha_edai", invoice.mothaEdai)
                put("setharam_grams", invoice.setharamGrams)
                put("thabaal_thogai", invoice.thabaalThogai)
                put("ahimsa_pattu_thogai", invoice.ahimsaPattuThogai)
                put("piravari_vugal", invoice.piravariVugal.toString())
                put("vangi_tharavugal", invoice.vangiTharavugal.toString())
            }
            put("sontha_viruppangal", invoice.sonthaViruppangal)
            put("nibandhanaigal", invoice.nibandhanaigal)
            put("ullkurippu", invoice.ullkurippu)
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

    override fun loadDeletedInvoices(): List<PattiyalTharavuru> = useDb { db ->
        val list = mutableListOf<PattiyalTharavuru>()
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
    override fun loadAllReceipts(): List<PatrugalTharavuru> = useDb { db ->
        val list = mutableListOf<PatrugalTharavuru>()
        db.rawQuery("SELECT * FROM \"$patrugalTable\" WHERE is_deleted = 0 ORDER BY patru_naal DESC, id DESC", null).use { c ->
            while (c.moveToNext()) list.add(cursorToReceipt(c))
        }
        list
    } ?: emptyList()

    override fun saveReceipt(receipt: PatrugalTharavuru): Long = useDb { db ->
        val cv = ContentValues().apply {
            put("niruvanam_id", receipt.niruvanamId)
            put("patru_en", receipt.patruEn)
            put("fin_year", receipt.finYear)
            put("vanakkam", receipt.vanakkam)
            put("vaangunar_id", receipt.vaangunarId)
            put("vaangunar_peyar", MozhiJsonConverter.stringify(receipt.vaangunarPeyar))
            put("vaangunar_munvari", MozhiJsonConverter.stringify(receipt.vaangunarMunvari))
            put("patru_naal", receipt.patruNaal)
            put("thogai", receipt.thogai)
            put("seluthum_murai", receipt.seluthumMurai)
            put("vangi_peyar", receipt.vangiPeyar)
            put("parivarthanai_en", receipt.parivarthanaiEn)
            put("ullkurippu", receipt.ullkurippu)
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

    override fun loadDeletedReceipts(): List<PatrugalTharavuru> = useDb { db ->
        val list = mutableListOf<PatrugalTharavuru>()
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
    override fun getLinksForPatru(patruId: Long): List<PatruPattiyalInaippuTharavuru> = useDb { db ->
        val list = mutableListOf<PatruPattiyalInaippuTharavuru>()
        db.rawQuery("SELECT * FROM \"$junctionTable\" WHERE patru_id = ?", arrayOf(patruId.toString())).use { c ->
            val idIdx = c.getColumnIndexOrThrow("id")
            val pIdIdx = c.getColumnIndexOrThrow("patru_id")
            val invIdIdx = c.getColumnIndexOrThrow("pattiyal_id")
            val amountIdx = c.getColumnIndexOrThrow("inaippu_thogai")
            val dateIdx = c.getColumnIndexOrThrow("created_at")
            while (c.moveToNext()) {
                list.add(PatruPattiyalInaippuTharavuru(
                    id = c.getLong(idIdx),
                    patruId = c.getLong(pIdIdx),
                    pattiyalId = c.getLong(invIdIdx),
                    poruthiyaThogai = c.getDouble(amountIdx)
                ))
            }
        }
        list
    } ?: emptyList()

    override fun saveReceiptWithLinks(receipt: PatrugalTharavuru, links: List<PatruPattiyalInaippuTharavuru>): Long = useDb { db ->
        db.beginTransaction()
        try {
            val rId = saveReceipt(receipt)
            if (rId > 0) {
                db.delete("\"$junctionTable\"", "patru_id = ?", arrayOf(rId.toString()))
                for (link in links) {
                    val cv = ContentValues().apply {
                        put("patru_id", rId)
                        put("pattiyal_id", link.pattiyalId)
                        put("inaippu_thogai", link.poruthiyaThogai)
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
    override fun loadProfile(profileId: Long?): NiruvanaTharavugal? = useDb { db ->
        var p: NiruvanaTharavugal? = null
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

    override fun loadAllProfiles(): List<NiruvanaTharavugal> = useDb { db ->
        val list = mutableListOf<NiruvanaTharavugal>()
        db.rawQuery("SELECT * FROM \"$profileTable\" WHERE is_deleted = 0 ORDER BY id ASC", null).use { c ->
            while (c.moveToNext()) list.add(cursorToProfile(c))
        }
        list
    } ?: emptyList()

    override fun saveProfile(profile: NiruvanaTharavugal): Boolean = useDb { db ->
        val cv = ContentValues().apply {
            put("mudhan_mozhi", profile.mudhanMozhi)
            put("thunai_mozhi", profile.thunaiMozhi)
            put("iru_mozhi", if (profile.iruMozhi) 1 else 0)
            put("gst_pirippugal", if (profile.gstPirippugal) 1 else 0)
            put("niruvanathin_peyar", MozhiJsonConverter.stringify(profile.niruvanathinPeyar))
            put("kurum_peyar", profile.kurumPeyar)
            put("tholaipaesi1", profile.tholaipaesi1)
            put("tholaipaesi2", profile.tholaipaesi2)
            put("minnanjal", profile.minnanjal)
            put("gstin", profile.gstin)
            put("mugavari", MozhiJsonConverter.stringify(profile.mugavari))
            put("oor", MozhiJsonConverter.stringify(profile.oor))
            put("maavattam", MozhiJsonConverter.stringify(profile.maavattam))
            put("maanilam", MozhiJsonConverter.stringify(profile.maanilam))
            put("naadu", MozhiJsonConverter.stringify(profile.naadu))
            put("anjal_kuriyeedu", profile.anjalKuriyeedu)
            put("vangi_peyar", MozhiJsonConverter.stringify(profile.vangiPeyar))
            put("kilai", MozhiJsonConverter.stringify(profile.kilai))
            put("vangi_kanakku", profile.vangiKanakku)
            put("ifsc", profile.ifsc)
            put("oavuru", profile.oavuru)
            put("agala_oavuru", profile.agalaOavuru)
            put("thalaippu_vadivu", profile.thalaippuVadivu)
            put("kaiyoppam", profile.kaiyoppam)
            put("oppam_peyar", profile.oppamPeyar)
            put("oppam_mudhal_peyar", profile.oppamMudhalPeyar)
            put("oppam_irudhi_peyar", profile.oppamIrudhiPeyar)
            put("adaimozhi", MozhiJsonConverter.stringify(profile.adaimozhi))
            put("upi_id", profile.upiId)
            put("thoatra_niram", profile.thoatraNiram)
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
    private fun cursorToMerchant(c: Cursor): VaangunarTharavuru = VaangunarTharavuru(
        id = c.getLong(c.getColumnIndexOrThrow("id")),
        peyar = MozhiJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("peyar"))),
        mugavari = MozhiJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("mugavari"))),
        oor = MozhiJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("oor"))),
        maavattam = MozhiJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("maavattam"))),
        maanilam = MozhiJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("maanilam"))),
        naadu = MozhiJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("naadu"))),
        velinaadMugavari = MozhiJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("velinaad_mugavari"))),
        anjalKuriyeedu = c.getString(c.getColumnIndexOrThrow("anjal_kuriyeedu")),
        gstin = c.getString(c.getColumnIndexOrThrow("gstin")),
        minnanjal = c.getString(c.getColumnIndexOrThrow("minnanjal")),
        tholaipaesi = c.getString(c.getColumnIndexOrThrow("tholaipaesi")),
        createdAt = c.getLong(c.getColumnIndexOrThrow("created_at")),
        updatedAt = c.getLong(c.getColumnIndexOrThrow("updated_at")),
        isDeleted = c.getInt(c.getColumnIndexOrThrow("is_deleted")) == 1,
        deletedAt = if (c.isNull(c.getColumnIndexOrThrow("deleted_at"))) null else c.getLong(c.getColumnIndexOrThrow("deleted_at"))
    )

    private fun cursorToItem(c: Cursor): PorulTharavuru = PorulTharavuru(
        id = c.getLong(c.getColumnIndexOrThrow("id")),
        porulPeyar = MozhiJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("porul_peyar"))),
        hsnCode = c.getString(c.getColumnIndexOrThrow("hsn_code")),
        vilai = c.getDouble(c.getColumnIndexOrThrow("vilai")),
        variVeetham = c.getDouble(c.getColumnIndexOrThrow("vari_veetham")),
        alavuVagai = c.getString(c.getColumnIndexOrThrow("alavu_vagai")),
        alagu = c.getString(c.getColumnIndexOrThrow("alagu")),
        niruvanamIds = PorulJsonConverter.parseLongList(c.getString(c.getColumnIndexOrThrow("niruvanam_ids"))),
        companyPrices = PorulJsonConverter.parseCompanyPrices(c.getString(c.getColumnIndexOrThrow("company_prices"))),
        createdAt = c.getLong(c.getColumnIndexOrThrow("created_at")),
        updatedAt = c.getLong(c.getColumnIndexOrThrow("updated_at")),
        isDeleted = c.getInt(c.getColumnIndexOrThrow("is_deleted")) == 1,
        deletedAt = if (c.isNull(c.getColumnIndexOrThrow("deleted_at"))) null else c.getLong(c.getColumnIndexOrThrow("deleted_at"))
    )

    private fun cursorToInvoice(c: Cursor): PattiyalTharavuru = PattiyalTharavuru(
        id = c.getLong(c.getColumnIndexOrThrow("id")),
        niruvanamId = if (c.isNull(c.getColumnIndexOrThrow("niruvanam_id"))) null else c.getLong(c.getColumnIndexOrThrow("niruvanam_id")),
        patrucheettuEn = c.getString(c.getColumnIndexOrThrow("patrucheettu_en")),
        finYear = c.getInt(c.getColumnIndexOrThrow("fin_year")),
        vanakkam = c.getInt(c.getColumnIndexOrThrow("vanakkam")),
        pattiyalVagai = c.getString(c.getColumnIndexOrThrow("pattiyal_vagai")),
        vaangunarId = if (c.isNull(c.getColumnIndexOrThrow("vaangunar_id"))) null else c.getLong(c.getColumnIndexOrThrow("vaangunar_id")),
        vaangunarPeyar = MozhiJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("vaangunar_peyar"))),
        vaangunarMunvari = MozhiJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("vaangunar_munvari"))),
        pattiyalNaal = c.getLong(c.getColumnIndexOrThrow("pattiyal_naal")),
        tharavugal = c.getString(c.getColumnIndexOrThrow("tharavugal")) ?: "[]",
        mothaThogai = c.getDouble(c.getColumnIndexOrThrow("motha_thogai")),
        thallupadi = c.getDouble(c.getColumnIndexOrThrow("thallupadi")),
        podhuThallupadiMathippu = c.getDouble(c.getColumnIndexOrThrow("podhu_thallupadi_mathippu")),
        podhuThallupadiVagai = c.getString(c.getColumnIndexOrThrow("podhu_thallupadi_vagai")),
        podhuThallupadiThogai = c.getDouble(c.getColumnIndexOrThrow("podhu_thallupadi_thogai")),
        variThogai = c.getDouble(c.getColumnIndexOrThrow("vari_thogai")),
        variTharavugal = c.getString(c.getColumnIndexOrThrow("vari_tharavugal")) ?: "{}",
        mothaEdai = if (isFactory && c.getColumnIndex("motha_edai") >= 0 && !c.isNull(c.getColumnIndex("motha_edai"))) c.getDouble(c.getColumnIndex("motha_edai")) else 0.0,
        setharamGrams = if (isFactory && c.getColumnIndex("setharam_grams") >= 0 && !c.isNull(c.getColumnIndex("setharam_grams"))) c.getDouble(c.getColumnIndex("setharam_grams")) else 0.0,
        thabaalThogai = if (isFactory && c.getColumnIndex("thabaal_thogai") >= 0 && !c.isNull(c.getColumnIndex("thabaal_thogai"))) c.getDouble(c.getColumnIndex("thabaal_thogai")) else 0.0,
        ahimsaPattuThogai = if (isFactory && c.getColumnIndex("ahimsa_pattu_thogai") >= 0 && !c.isNull(c.getColumnIndex("ahimsa_pattu_thogai"))) c.getDouble(c.getColumnIndex("ahimsa_pattu_thogai")) else 0.0,
        piravariVugal = if (isFactory && c.getColumnIndex("piravari_vugal") >= 0) (c.getString(c.getColumnIndex("piravari_vugal")) ?: "[]") else "[]",
        sonthaViruppangal = c.getString(c.getColumnIndexOrThrow("sontha_viruppangal")) ?: "{}",
        nibandhanaigal = c.getString(c.getColumnIndexOrThrow("nibandhanaigal")),
        ullkurippu = c.getString(c.getColumnIndexOrThrow("ullkurippu")),
        vangiTharavugal = if (isFactory && c.getColumnIndex("vangi_tharavugal") >= 0) (c.getString(c.getColumnIndex("vangi_tharavugal")) ?: "{}") else "{}",
        createdAt = c.getLong(c.getColumnIndexOrThrow("created_at")),
        updatedAt = c.getLong(c.getColumnIndexOrThrow("updated_at")),
        isDeleted = c.getInt(c.getColumnIndexOrThrow("is_deleted")) == 1,
        deletedAt = if (c.isNull(c.getColumnIndexOrThrow("deleted_at"))) null else c.getLong(c.getColumnIndexOrThrow("deleted_at"))
    )

    private fun cursorToReceipt(c: Cursor): PatrugalTharavuru = PatrugalTharavuru(
        id = c.getLong(c.getColumnIndexOrThrow("id")),
        niruvanamId = if (c.isNull(c.getColumnIndexOrThrow("niruvanam_id"))) null else c.getLong(c.getColumnIndexOrThrow("niruvanam_id")),
        patruEn = c.getString(c.getColumnIndexOrThrow("patru_en")),
        finYear = c.getString(c.getColumnIndexOrThrow("fin_year")),
        vanakkam = c.getInt(c.getColumnIndexOrThrow("vanakkam")),
        vaangunarId = if (c.isNull(c.getColumnIndexOrThrow("vaangunar_id"))) null else c.getLong(c.getColumnIndexOrThrow("vaangunar_id")),
        vaangunarPeyar = MozhiJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("vaangunar_peyar"))),
        vaangunarMunvari = MozhiJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("vaangunar_munvari"))),
        patruNaal = c.getLong(c.getColumnIndexOrThrow("patru_naal")),
        thogai = c.getDouble(c.getColumnIndexOrThrow("thogai")),
        seluthumMurai = c.getString(c.getColumnIndexOrThrow("seluthum_murai")),
        vangiPeyar = if (c.isNull(c.getColumnIndexOrThrow("vangi_peyar"))) null else c.getString(c.getColumnIndexOrThrow("vangi_peyar")),
        parivarthanaiEn = if (c.isNull(c.getColumnIndexOrThrow("parivarthanai_en"))) null else c.getString(c.getColumnIndexOrThrow("parivarthanai_en")),
        ullkurippu = c.getString(c.getColumnIndexOrThrow("ullkurippu")),
        createdAt = c.getLong(c.getColumnIndexOrThrow("created_at")),
        updatedAt = c.getLong(c.getColumnIndexOrThrow("updated_at")),
        isDeleted = c.getInt(c.getColumnIndexOrThrow("is_deleted")) == 1,
        deletedAt = if (c.isNull(c.getColumnIndexOrThrow("deleted_at"))) null else c.getLong(c.getColumnIndexOrThrow("deleted_at"))
    )

    private fun cursorToProfile(c: Cursor): NiruvanaTharavugal = NiruvanaTharavugal(
        id = c.getLong(c.getColumnIndexOrThrow("id")),
        mudhanMozhi = c.getString(c.getColumnIndexOrThrow("mudhan_mozhi")),
        thunaiMozhi = c.getString(c.getColumnIndexOrThrow("thunai_mozhi")),
        iruMozhi = c.getInt(c.getColumnIndexOrThrow("iru_mozhi")) == 1,
        gstPirippugal = c.getInt(c.getColumnIndexOrThrow("gst_pirippugal")) == 1,
        niruvanathinPeyar = MozhiJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("niruvanathin_peyar"))),
        kurumPeyar = c.getString(c.getColumnIndexOrThrow("kurum_peyar")),
        tholaipaesi1 = c.getString(c.getColumnIndexOrThrow("tholaipaesi1")),
        tholaipaesi2 = c.getString(c.getColumnIndexOrThrow("tholaipaesi2")),
        minnanjal = c.getString(c.getColumnIndexOrThrow("minnanjal")),
        gstin = c.getString(c.getColumnIndexOrThrow("gstin")),
        mugavari = MozhiJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("mugavari"))),
        oor = MozhiJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("oor"))),
        maavattam = MozhiJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("maavattam"))),
        maanilam = MozhiJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("maanilam"))),
        naadu = MozhiJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("naadu"))),
        anjalKuriyeedu = c.getString(c.getColumnIndexOrThrow("anjal_kuriyeedu")),
        vangiPeyar = MozhiJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("vangi_peyar"))),
        kilai = MozhiJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("kilai"))),
        vangiKanakku = c.getString(c.getColumnIndexOrThrow("vangi_kanakku")),
        ifsc = c.getString(c.getColumnIndexOrThrow("ifsc")),
        oavuru = c.getString(c.getColumnIndexOrThrow("oavuru")),
        agalaOavuru = c.getString(c.getColumnIndexOrThrow("agala_oavuru")),
        thalaippuVadivu = c.getString(c.getColumnIndexOrThrow("thalaippu_vadivu")),
        kaiyoppam = c.getString(c.getColumnIndexOrThrow("kaiyoppam")),
        oppamPeyar = c.getString(c.getColumnIndexOrThrow("oppam_peyar")),
        oppamMudhalPeyar = c.getString(c.getColumnIndexOrThrow("oppam_mudhal_peyar")),
        oppamIrudhiPeyar = c.getString(c.getColumnIndexOrThrow("oppam_irudhi_peyar")),
        adaimozhi = MozhiJsonConverter.parse(c.getString(c.getColumnIndexOrThrow("adaimozhi"))),
        upiId = c.getString(c.getColumnIndexOrThrow("upi_id")),
        thoatraNiram = c.getString(c.getColumnIndexOrThrow("thoatra_niram"))
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
