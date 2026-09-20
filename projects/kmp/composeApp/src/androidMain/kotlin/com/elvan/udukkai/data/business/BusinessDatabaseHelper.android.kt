package com.elvan.udukkai.data.business

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.platform.AppContext
import com.elvan.udukkai.data.model.PattiyalTharavuru
import com.elvan.udukkai.data.model.PatruPattiyalInaippuTharavuru
import com.elvan.udukkai.data.model.PatrugalTharavuru
import com.elvan.udukkai.data.model.PorulTharavuru
import com.elvan.udukkai.data.model.VaangunarTharavuru
import com.elvan.udukkai.data.settings.MozhiJsonConverter
import java.io.File

class AndroidBusinessDatabaseHelper : BusinessDatabaseHelper {

    private val tag = "BusinessDbHelper"
    private val coolieDbName = "udukkai_kooli.db"
    private val silkDbName = "udukkai_pattu.db"

    private fun resolveActiveDatabase(dbName: String): File? {
        if (!AppContext.isInitialized) {
            Log.w(tag, "AppContext is not initialized yet!")
            return null
        }
        val localDb = AppContext.context.getDatabasePath(dbName)
        localDb.parentFile?.mkdirs()
        return localDb
    }

    private fun ensureTables(db: SQLiteDatabase, mode: AppMode) {
        val vaangunarTable = if (mode == AppMode.KOOLI) "kooli_vaangunar_table" else "pattu_vaangunar_table"
        val porulTable = if (mode == AppMode.KOOLI) "kooli_porul_table" else "pattu_porul_table"
        val pattiyalTable = if (mode == AppMode.KOOLI) "kooli_pattiyal_table" else "pattu_pattiyal_table"
        val patrugalTable = if (mode == AppMode.KOOLI) "kooli_patrugal_table" else "pattu_patrugal_table"
        val junctionTable = if (mode == AppMode.KOOLI) "kooli_patru_pattiyal_table" else "pattu_patru_pattiyal_table"

        val createVaangunarSql = """
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
        """.trimIndent()

        val createPorulSql = """
            CREATE TABLE IF NOT EXISTS "$porulTable" (
                "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                "porul_peyar" TEXT NOT NULL DEFAULT '{}',
                "hsn_code" TEXT NOT NULL DEFAULT '',
                "vilai" REAL NOT NULL DEFAULT 0.0,
                "vari_veetham" REAL NOT NULL DEFAULT 0.0,
                "alavu_vagai" TEXT NOT NULL DEFAULT 'quantity',
                "alagu" TEXT NOT NULL DEFAULT 'Nos',
                "created_at" INTEGER NOT NULL DEFAULT (CAST(strftime('%s', CURRENT_TIMESTAMP) AS INTEGER)),
                "updated_at" INTEGER NOT NULL DEFAULT (CAST(strftime('%s', CURRENT_TIMESTAMP) AS INTEGER)),
                "is_deleted" INTEGER NOT NULL DEFAULT 0 CHECK ("is_deleted" IN (0, 1)),
                "deleted_at" INTEGER NULL
            )
        """.trimIndent()

        val createPattiyalSql = if (mode == AppMode.KOOLI) {
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

        val createPatrugalSql = """
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
        """.trimIndent()

        val createJunctionSql = """
            CREATE TABLE IF NOT EXISTS "$junctionTable" (
                "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                "patru_id" INTEGER NOT NULL,
                "pattiyal_id" INTEGER NOT NULL,
                "poruthiya_thogai" REAL NOT NULL DEFAULT 0.0
            )
        """.trimIndent()

        db.execSQL(createVaangunarSql)
        db.execSQL(createPorulSql)
        db.execSQL(createPattiyalSql)
        db.execSQL(createPatrugalSql)
        db.execSQL(createJunctionSql)
    }

    override fun loadAllMerchants(mode: AppMode): List<VaangunarTharavuru> {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_vaangunar_table" else "pattu_vaangunar_table"
        val dbFile = resolveActiveDatabase(dbName) ?: return emptyList()

        val list = mutableListOf<VaangunarTharavuru>()
        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            ensureTables(db, mode)
            val sql = "SELECT * FROM $tableName WHERE is_deleted = 0 ORDER BY updated_at DESC, id DESC"
            cursor = db.rawQuery(sql, null)
            while (cursor.moveToNext()) {
                list.add(cursorToMerchant(cursor))
            }
        } catch (e: Exception) {
            Log.e(tag, "Error loading merchants from $tableName: ${e.message}", e)
        } finally {
            cursor?.close()
            db?.close()
        }
        return list
    }

    override fun saveMerchant(mode: AppMode, merchant: VaangunarTharavuru): Long {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_vaangunar_table" else "pattu_vaangunar_table"
        val dbFile = resolveActiveDatabase(dbName) ?: return -1L

        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            ensureTables(db, mode)

            val nowSec = System.currentTimeMillis() / 1000
            val values = ContentValues().apply {
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
                put("updated_at", nowSec)
                put("is_deleted", if (merchant.isDeleted) 1 else 0)
                if (merchant.deletedAt != null) {
                    val delSec = if (merchant.deletedAt > 10000000000L) merchant.deletedAt / 1000 else merchant.deletedAt
                    put("deleted_at", delSec)
                } else {
                    putNull("deleted_at")
                }
            }

            val resultId: Long
            if (merchant.id > 0L) {
                val updated = db.update(tableName, values, "id = ?", arrayOf(merchant.id.toString()))
                resultId = if (updated > 0) merchant.id else -1L
            } else {
                val createdSec = if (merchant.createdAt > 10000000000L) merchant.createdAt / 1000 else if (merchant.createdAt > 0L) merchant.createdAt else nowSec
                values.put("created_at", createdSec)
                resultId = db.insert(tableName, null, values)
            }

            return resultId
        } catch (e: Exception) {
            Log.e(tag, "Error saving merchant to $tableName: ${e.message}", e)
            return -1L
        } finally {
            db?.close()
        }
    }

    override fun deleteMerchant(mode: AppMode, id: Long): Boolean {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_vaangunar_table" else "pattu_vaangunar_table"
        val dbFile = resolveActiveDatabase(dbName) ?: return false

        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            ensureTables(db, mode)
            val nowSec = System.currentTimeMillis() / 1000
            val values = ContentValues().apply {
                put("is_deleted", 1)
                put("deleted_at", nowSec)
                put("updated_at", nowSec)
            }
            val rows = db.update(tableName, values, "id = ?", arrayOf(id.toString()))

            return rows > 0
        } catch (e: Exception) {
            Log.e(tag, "Error deleting merchant $id from $tableName: ${e.message}", e)
            return false
        } finally {
            db?.close()
        }
    }

    override fun loadDeletedMerchants(mode: AppMode): List<VaangunarTharavuru> {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_vaangunar_table" else "pattu_vaangunar_table"
        val dbFile = resolveActiveDatabase(dbName) ?: return emptyList()

        val list = mutableListOf<VaangunarTharavuru>()
        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            ensureTables(db, mode)
            val sql = "SELECT * FROM $tableName WHERE is_deleted = 1 ORDER BY deleted_at DESC, id DESC"
            cursor = db.rawQuery(sql, null)
            while (cursor.moveToNext()) {
                list.add(cursorToMerchant(cursor))
            }
        } catch (e: Exception) {
            Log.e(tag, "Error loading deleted merchants from $tableName: ${e.message}", e)
        } finally {
            cursor?.close()
            db?.close()
        }
        return list
    }

    override fun restoreMerchant(mode: AppMode, id: Long): Boolean {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_vaangunar_table" else "pattu_vaangunar_table"
        val dbFile = resolveActiveDatabase(dbName) ?: return false

        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            ensureTables(db, mode)
            val values = ContentValues().apply {
                put("is_deleted", 0)
                putNull("deleted_at")
                put("updated_at", System.currentTimeMillis() / 1000)
            }
            val rows = db.update(tableName, values, "id = ?", arrayOf(id.toString()))
            return rows > 0
        } catch (e: Exception) {
            Log.e(tag, "Error restoring merchant $id: ${e.message}", e)
            return false
        } finally {
            db?.close()
        }
    }

    override fun permanentDeleteMerchant(mode: AppMode, id: Long): Boolean {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_vaangunar_table" else "pattu_vaangunar_table"
        val dbFile = resolveActiveDatabase(dbName) ?: return false

        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            ensureTables(db, mode)
            val rows = db.delete(tableName, "id = ?", arrayOf(id.toString()))
            return rows > 0
        } catch (e: Exception) {
            Log.e(tag, "Error permanently deleting merchant $id: ${e.message}", e)
            return false
        } finally {
            db?.close()
        }
    }

    override fun purgeExpiredMerchants(mode: AppMode, days: Int): Int {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_vaangunar_table" else "pattu_vaangunar_table"
        val dbFile = resolveActiveDatabase(dbName) ?: return 0

        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            ensureTables(db, mode)
            val cutoffSec = (System.currentTimeMillis() / 1000) - (days * 86400L)
            return db.delete(tableName, "is_deleted = 1 AND deleted_at < ?", arrayOf(cutoffSec.toString()))
        } catch (e: Exception) {
            Log.e(tag, "Error purging expired merchants: ${e.message}", e)
            return 0
        } finally {
            db?.close()
        }
    }

    override fun loadAllItems(mode: AppMode): List<PorulTharavuru> {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_porul_table" else "pattu_porul_table"
        val dbFile = resolveActiveDatabase(dbName) ?: return emptyList()

        val list = mutableListOf<PorulTharavuru>()
        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            ensureTables(db, mode)
            val sql = "SELECT * FROM $tableName WHERE is_deleted = 0 ORDER BY updated_at DESC, id DESC"
            cursor = db.rawQuery(sql, null)
            while (cursor.moveToNext()) {
                list.add(cursorToItem(cursor))
            }
        } catch (e: Exception) {
            Log.e(tag, "Error loading items from $tableName: ${e.message}", e)
        } finally {
            cursor?.close()
            db?.close()
        }
        return list
    }

    override fun saveItem(mode: AppMode, item: PorulTharavuru): Long {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_porul_table" else "pattu_porul_table"
        val dbFile = resolveActiveDatabase(dbName) ?: return -1L

        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            ensureTables(db, mode)

            val nowSec = System.currentTimeMillis() / 1000
            val values = ContentValues().apply {
                put("porul_peyar", MozhiJsonConverter.stringify(item.porulPeyar))
                put("hsn_code", item.hsnCode)
                put("vilai", item.vilai)
                put("vari_veetham", item.variVeetham)
                put("alavu_vagai", item.alavuVagai)
                put("alagu", item.alagu)
                put("updated_at", nowSec)
                put("is_deleted", if (item.isDeleted) 1 else 0)
                if (item.deletedAt != null) {
                    val delSec = if (item.deletedAt > 10000000000L) item.deletedAt / 1000 else item.deletedAt
                    put("deleted_at", delSec)
                } else {
                    putNull("deleted_at")
                }
            }

            val resultId: Long
            if (item.id > 0L) {
                val updated = db.update(tableName, values, "id = ?", arrayOf(item.id.toString()))
                resultId = if (updated > 0) item.id else -1L
            } else {
                val createdSec = if (item.createdAt > 10000000000L) item.createdAt / 1000 else if (item.createdAt > 0L) item.createdAt else nowSec
                values.put("created_at", createdSec)
                resultId = db.insert(tableName, null, values)
            }

            return resultId
        } catch (e: Exception) {
            Log.e(tag, "Error saving item to $tableName: ${e.message}", e)
            return -1L
        } finally {
            db?.close()
        }
    }

    override fun deleteItem(mode: AppMode, id: Long): Boolean {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_porul_table" else "pattu_porul_table"
        val dbFile = resolveActiveDatabase(dbName) ?: return false

        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            ensureTables(db, mode)
            val nowSec = System.currentTimeMillis() / 1000
            val values = ContentValues().apply {
                put("is_deleted", 1)
                put("deleted_at", nowSec)
                put("updated_at", nowSec)
            }
            val rows = db.update(tableName, values, "id = ?", arrayOf(id.toString()))

            return rows > 0
        } catch (e: Exception) {
            Log.e(tag, "Error deleting item $id from $tableName: ${e.message}", e)
            return false
        } finally {
            db?.close()
        }
    }

    override fun loadDeletedItems(mode: AppMode): List<PorulTharavuru> {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_porul_table" else "pattu_porul_table"
        val dbFile = resolveActiveDatabase(dbName) ?: return emptyList()

        val list = mutableListOf<PorulTharavuru>()
        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            ensureTables(db, mode)
            val sql = "SELECT * FROM $tableName WHERE is_deleted = 1 ORDER BY deleted_at DESC, id DESC"
            cursor = db.rawQuery(sql, null)
            while (cursor.moveToNext()) {
                list.add(cursorToItem(cursor))
            }
        } catch (e: Exception) {
            Log.e(tag, "Error loading deleted items from $tableName: ${e.message}", e)
        } finally {
            cursor?.close()
            db?.close()
        }
        return list
    }

    override fun restoreItem(mode: AppMode, id: Long): Boolean {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_porul_table" else "pattu_porul_table"
        val dbFile = resolveActiveDatabase(dbName) ?: return false

        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            ensureTables(db, mode)
            val values = ContentValues().apply {
                put("is_deleted", 0)
                putNull("deleted_at")
                put("updated_at", System.currentTimeMillis() / 1000)
            }
            val rows = db.update(tableName, values, "id = ?", arrayOf(id.toString()))
            return rows > 0
        } catch (e: Exception) {
            Log.e(tag, "Error restoring item $id: ${e.message}", e)
            return false
        } finally {
            db?.close()
        }
    }

    override fun permanentDeleteItem(mode: AppMode, id: Long): Boolean {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_porul_table" else "pattu_porul_table"
        val dbFile = resolveActiveDatabase(dbName) ?: return false

        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            ensureTables(db, mode)
            val rows = db.delete(tableName, "id = ?", arrayOf(id.toString()))
            return rows > 0
        } catch (e: Exception) {
            Log.e(tag, "Error permanently deleting item $id: ${e.message}", e)
            return false
        } finally {
            db?.close()
        }
    }

    override fun purgeExpiredItems(mode: AppMode, days: Int): Int {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_porul_table" else "pattu_porul_table"
        val dbFile = resolveActiveDatabase(dbName) ?: return 0

        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            ensureTables(db, mode)
            val cutoffSec = (System.currentTimeMillis() / 1000) - (days * 86400L)
            return db.delete(tableName, "is_deleted = 1 AND deleted_at < ?", arrayOf(cutoffSec.toString()))
        } catch (e: Exception) {
            Log.e(tag, "Error purging expired items: ${e.message}", e)
            return 0
        } finally {
            db?.close()
        }
    }

    override fun loadAllInvoices(mode: AppMode): List<PattiyalTharavuru> {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_pattiyal_table" else "pattu_pattiyal_table"
        val dbFile = resolveActiveDatabase(dbName) ?: return emptyList()

        val list = mutableListOf<PattiyalTharavuru>()
        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            ensureTables(db, mode)
            val sql = "SELECT * FROM $tableName WHERE is_deleted = 0 ORDER BY pattiyal_naal DESC, id DESC"
            cursor = db.rawQuery(sql, null)
            while (cursor.moveToNext()) {
                list.add(cursorToInvoice(cursor))
            }
        } catch (e: Exception) {
            Log.e(tag, "Error loading invoices from $tableName: ${e.message}", e)
        } finally {
            cursor?.close()
            db?.close()
        }
        return list
    }

    override fun saveInvoice(mode: AppMode, invoice: PattiyalTharavuru): Long {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_pattiyal_table" else "pattu_pattiyal_table"
        val dbFile = resolveActiveDatabase(dbName) ?: return -1L

        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            ensureTables(db, mode)

            val nowSec = System.currentTimeMillis() / 1000
            val naalSec = if (invoice.pattiyalNaal > 10000000000L) invoice.pattiyalNaal / 1000 else invoice.pattiyalNaal
            val values = ContentValues().apply {
                if (invoice.niruvanamId != null) put("niruvanam_id", invoice.niruvanamId) else putNull("niruvanam_id")
                put("patrucheettu_en", invoice.patrucheettuEn)
                put("fin_year", invoice.finYear)
                put("vanakkam", invoice.vanakkam)
                put("pattiyal_vagai", invoice.pattiyalVagai)
                if (invoice.vaangunarId != null) put("vaangunar_id", invoice.vaangunarId) else putNull("vaangunar_id")
                put("vaangunar_peyar", MozhiJsonConverter.stringify(invoice.vaangunarPeyar))
                put("vaangunar_munvari", MozhiJsonConverter.stringify(invoice.vaangunarMunvari))
                put("pattiyal_naal", naalSec)
                put("tharavugal", invoice.tharavugal)
                put("motha_thogai", invoice.mothaThogai)
                put("thallupadi", invoice.thallupadi)
                put("podhu_thallupadi_mathippu", invoice.podhuThallupadiMathippu)
                put("podhu_thallupadi_vagai", invoice.podhuThallupadiVagai)
                put("podhu_thallupadi_thogai", invoice.podhuThallupadiThogai)
                put("vari_thogai", invoice.variThogai)
                put("vari_tharavugal", invoice.variTharavugal)
                if (mode == AppMode.KOOLI) {
                    put("motha_edai", invoice.mothaEdai)
                    put("setharam_grams", invoice.setharamGrams)
                    put("thabaal_thogai", invoice.thabaalThogai)
                    put("ahimsa_pattu_thogai", invoice.ahimsaPattuThogai)
                    put("piravari_vugal", invoice.piravariVugal)
                    put("vangi_tharavugal", invoice.vangiTharavugal)
                }
                put("sontha_viruppangal", invoice.sonthaViruppangal)
                put("nibandhanaigal", invoice.nibandhanaigal)
                put("ullkurippu", invoice.ullkurippu)
                put("updated_at", nowSec)
                put("is_deleted", if (invoice.isDeleted) 1 else 0)
                if (invoice.deletedAt != null) {
                    val delSec = if (invoice.deletedAt > 10000000000L) invoice.deletedAt / 1000 else invoice.deletedAt
                    put("deleted_at", delSec)
                } else {
                    putNull("deleted_at")
                }
            }

            val resultId: Long
            if (invoice.id > 0L) {
                val updated = db.update(tableName, values, "id = ?", arrayOf(invoice.id.toString()))
                resultId = if (updated > 0) invoice.id else -1L
            } else {
                val createdSec = if (invoice.createdAt > 10000000000L) invoice.createdAt / 1000 else if (invoice.createdAt > 0L) invoice.createdAt else nowSec
                values.put("created_at", createdSec)
                resultId = db.insert(tableName, null, values)
            }

            return resultId
        } catch (e: Exception) {
            Log.e(tag, "Error saving invoice to $tableName: ${e.message}", e)
            return -1L
        } finally {
            db?.close()
        }
    }

    override fun deleteInvoice(mode: AppMode, id: Long): Boolean {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_pattiyal_table" else "pattu_pattiyal_table"
        val dbFile = resolveActiveDatabase(dbName) ?: return false

        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            ensureTables(db, mode)
            val nowSec = System.currentTimeMillis() / 1000
            val values = ContentValues().apply {
                put("is_deleted", 1)
                put("deleted_at", nowSec)
                put("updated_at", nowSec)
            }
            val rows = db.update(tableName, values, "id = ?", arrayOf(id.toString()))

            return rows > 0
        } catch (e: Exception) {
            Log.e(tag, "Error deleting invoice $id from $tableName: ${e.message}", e)
            return false
        } finally {
            db?.close()
        }
    }

    override fun loadDeletedInvoices(mode: AppMode): List<PattiyalTharavuru> {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_pattiyal_table" else "pattu_pattiyal_table"
        val dbFile = resolveActiveDatabase(dbName) ?: return emptyList()

        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null
        val list = mutableListOf<PattiyalTharavuru>()
        try {
            db = SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READWRITE)
            ensureTables(db, mode)
            cursor = db.rawQuery("SELECT * FROM $tableName WHERE is_deleted = 1 ORDER BY deleted_at DESC, id DESC", null)
            while (cursor.moveToNext()) {
                list.add(cursorToInvoice(cursor))
            }
        } catch (e: Exception) {
            Log.e(tag, "Error loading deleted invoices from $tableName: ${e.message}", e)
        } finally {
            cursor?.close()
            db?.close()
        }
        return list
    }

    override fun restoreInvoice(mode: AppMode, id: Long): Boolean {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_pattiyal_table" else "pattu_pattiyal_table"
        val dbFile = resolveActiveDatabase(dbName) ?: return false

        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READWRITE)
            ensureTables(db, mode)
            val nowSec = System.currentTimeMillis() / 1000
            val values = ContentValues().apply {
                put("is_deleted", 0)
                putNull("deleted_at")
                put("updated_at", nowSec)
            }
            val rows = db.update(tableName, values, "id = ?", arrayOf(id.toString()))
            return rows > 0
        } catch (e: Exception) {
            Log.e(tag, "Error restoring invoice $id from $tableName: ${e.message}", e)
            return false
        } finally {
            db?.close()
        }
    }

    override fun permanentDeleteInvoice(mode: AppMode, id: Long): Boolean {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_pattiyal_table" else "pattu_pattiyal_table"
        val junctionTable = if (mode == AppMode.KOOLI) "kooli_patru_pattiyal_table" else "pattu_patru_pattiyal_table"
        val dbFile = resolveActiveDatabase(dbName) ?: return false

        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READWRITE)
            ensureTables(db, mode)
            db.beginTransaction()
            try {
                db.delete(junctionTable, "pattiyal_id = ?", arrayOf(id.toString()))
                val rows = db.delete(tableName, "id = ?", arrayOf(id.toString()))
                db.setTransactionSuccessful()
                return rows > 0
            } finally {
                db.endTransaction()
            }
        } catch (e: Exception) {
            Log.e(tag, "Error permanently deleting invoice $id from $tableName: ${e.message}", e)
            return false
        } finally {
            db?.close()
        }
    }

    override fun purgeExpiredInvoices(mode: AppMode, days: Int): Int {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_pattiyal_table" else "pattu_pattiyal_table"
        val junctionTable = if (mode == AppMode.KOOLI) "kooli_patru_pattiyal_table" else "pattu_patru_pattiyal_table"
        val dbFile = resolveActiveDatabase(dbName) ?: return 0

        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READWRITE)
            ensureTables(db, mode)
            val cutoffSec = (System.currentTimeMillis() / 1000) - (days * 86400L)
            db.beginTransaction()
            try {
                db.execSQL("DELETE FROM $junctionTable WHERE pattiyal_id IN (SELECT id FROM $tableName WHERE is_deleted = 1 AND deleted_at < $cutoffSec)")
                val deleted = db.delete(tableName, "is_deleted = 1 AND deleted_at < ?", arrayOf(cutoffSec.toString()))
                db.setTransactionSuccessful()
                return deleted
            } finally {
                db.endTransaction()
            }
        } catch (e: Exception) {
            Log.e(tag, "Error purging expired invoices from $tableName: ${e.message}", e)
            return 0
        } finally {
            db?.close()
        }
    }

    override fun loadAllReceipts(mode: AppMode): List<PatrugalTharavuru> {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_patrugal_table" else "pattu_patrugal_table"
        val dbFile = resolveActiveDatabase(dbName) ?: return emptyList()

        val list = mutableListOf<PatrugalTharavuru>()
        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            ensureTables(db, mode)
            val sql = "SELECT * FROM $tableName WHERE is_deleted = 0 ORDER BY patru_naal DESC, id DESC"
            cursor = db.rawQuery(sql, null)
            while (cursor.moveToNext()) {
                list.add(cursorToReceipt(cursor))
            }
        } catch (e: Exception) {
            Log.e(tag, "Error loading receipts from $tableName: ${e.message}", e)
        } finally {
            cursor?.close()
            db?.close()
        }
        return list
    }

    override fun saveReceipt(mode: AppMode, receipt: PatrugalTharavuru): Long {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_patrugal_table" else "pattu_patrugal_table"
        val dbFile = resolveActiveDatabase(dbName) ?: return -1L

        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            ensureTables(db, mode)

            val nowSec = System.currentTimeMillis() / 1000
            val naalSec = if (receipt.patruNaal > 10000000000L) receipt.patruNaal / 1000 else receipt.patruNaal
            val values = ContentValues().apply {
                if (receipt.niruvanamId != null) put("niruvanam_id", receipt.niruvanamId) else putNull("niruvanam_id")
                put("patru_en", receipt.patruEn)
                put("fin_year", receipt.finYear)
                put("vanakkam", receipt.vanakkam)
                if (receipt.vaangunarId != null) put("vaangunar_id", receipt.vaangunarId) else putNull("vaangunar_id")
                put("vaangunar_peyar", MozhiJsonConverter.stringify(receipt.vaangunarPeyar))
                put("vaangunar_munvari", MozhiJsonConverter.stringify(receipt.vaangunarMunvari))
                put("patru_naal", naalSec)
                put("thogai", receipt.thogai)
                put("seluthum_murai", receipt.seluthumMurai)
                if (receipt.vangiPeyar != null) put("vangi_peyar", receipt.vangiPeyar) else putNull("vangi_peyar")
                if (receipt.parivarthanaiEn != null) put("parivarthanai_en", receipt.parivarthanaiEn) else putNull("parivarthanai_en")
                put("ullkurippu", receipt.ullkurippu)
                put("updated_at", nowSec)
                put("is_deleted", if (receipt.isDeleted) 1 else 0)
                if (receipt.deletedAt != null) {
                    val delSec = if (receipt.deletedAt > 10000000000L) receipt.deletedAt / 1000 else receipt.deletedAt
                    put("deleted_at", delSec)
                } else {
                    putNull("deleted_at")
                }
            }

            val resultId: Long
            if (receipt.id > 0L) {
                val updated = db.update(tableName, values, "id = ?", arrayOf(receipt.id.toString()))
                resultId = if (updated > 0) receipt.id else -1L
            } else {
                val createdSec = if (receipt.createdAt > 10000000000L) receipt.createdAt / 1000 else if (receipt.createdAt > 0L) receipt.createdAt else nowSec
                values.put("created_at", createdSec)
                resultId = db.insert(tableName, null, values)
            }

            return resultId
        } catch (e: Exception) {
            Log.e(tag, "Error saving receipt to $tableName: ${e.message}", e)
            return -1L
        } finally {
            db?.close()
        }
    }

    override fun deleteReceipt(mode: AppMode, id: Long): Boolean {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_patrugal_table" else "pattu_patrugal_table"
        val dbFile = resolveActiveDatabase(dbName) ?: return false

        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            ensureTables(db, mode)
            val nowSec = System.currentTimeMillis() / 1000
            val values = ContentValues().apply {
                put("is_deleted", 1)
                put("deleted_at", nowSec)
                put("updated_at", nowSec)
            }
            val rows = db.update(tableName, values, "id = ?", arrayOf(id.toString()))

            return rows > 0
        } catch (e: Exception) {
            Log.e(tag, "Error deleting receipt $id from $tableName: ${e.message}", e)
            return false
        } finally {
            db?.close()
        }
    }

    override fun loadDeletedReceipts(mode: AppMode): List<PatrugalTharavuru> {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_patrugal_table" else "pattu_patrugal_table"
        val dbFile = resolveActiveDatabase(dbName) ?: return emptyList()

        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null
        val list = mutableListOf<PatrugalTharavuru>()
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            ensureTables(db, mode)
            cursor = db.rawQuery("SELECT * FROM $tableName WHERE is_deleted = 1 ORDER BY deleted_at DESC, id DESC", null)
            while (cursor.moveToNext()) {
                list.add(cursorToReceipt(cursor))
            }
        } catch (e: Exception) {
            Log.e(tag, "Error loading deleted receipts from $tableName: ${e.message}", e)
        } finally {
            cursor?.close()
            db?.close()
        }
        return list
    }

    override fun restoreReceipt(mode: AppMode, id: Long): Boolean {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_patrugal_table" else "pattu_patrugal_table"
        val dbFile = resolveActiveDatabase(dbName) ?: return false

        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            ensureTables(db, mode)
            val nowSec = System.currentTimeMillis() / 1000
            val values = ContentValues().apply {
                put("is_deleted", 0)
                putNull("deleted_at")
                put("updated_at", nowSec)
            }
            val rows = db.update(tableName, values, "id = ?", arrayOf(id.toString()))
            return rows > 0
        } catch (e: Exception) {
            Log.e(tag, "Error restoring receipt $id from $tableName: ${e.message}", e)
            return false
        } finally {
            db?.close()
        }
    }

    override fun permanentDeleteReceipt(mode: AppMode, id: Long): Boolean {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_patrugal_table" else "pattu_patrugal_table"
        val junctionTable = if (mode == AppMode.KOOLI) "kooli_patru_pattiyal_table" else "pattu_patru_pattiyal_table"
        val dbFile = resolveActiveDatabase(dbName) ?: return false

        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            ensureTables(db, mode)
            db.beginTransaction()
            try {
                db.delete(junctionTable, "patru_id = ?", arrayOf(id.toString()))
                val rows = db.delete(tableName, "id = ?", arrayOf(id.toString()))
                db.setTransactionSuccessful()
                return rows > 0
            } finally {
                db.endTransaction()
            }
        } catch (e: Exception) {
            Log.e(tag, "Error permanently deleting receipt $id from $tableName: ${e.message}", e)
            return false
        } finally {
            db?.close()
        }
    }

    override fun purgeExpiredReceipts(mode: AppMode, days: Int): Int {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_patrugal_table" else "pattu_patrugal_table"
        val junctionTable = if (mode == AppMode.KOOLI) "kooli_patru_pattiyal_table" else "pattu_patru_pattiyal_table"
        val dbFile = resolveActiveDatabase(dbName) ?: return 0

        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            ensureTables(db, mode)
            val cutoffSec = (System.currentTimeMillis() / 1000) - (days * 86400L)
            db.beginTransaction()
            try {
                db.execSQL("DELETE FROM $junctionTable WHERE patru_id IN (SELECT id FROM $tableName WHERE is_deleted = 1 AND deleted_at < $cutoffSec)")
                val deleted = db.delete(tableName, "is_deleted = 1 AND deleted_at < ?", arrayOf(cutoffSec.toString()))
                db.setTransactionSuccessful()
                return deleted
            } finally {
                db.endTransaction()
            }
        } catch (e: Exception) {
            Log.e(tag, "Error purging expired receipts from $tableName: ${e.message}", e)
            return 0
        } finally {
            db?.close()
        }
    }

    override fun getLinksForPatru(mode: AppMode, patruId: Long): List<PatruPattiyalInaippuTharavuru> {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val junctionTable = if (mode == AppMode.KOOLI) "kooli_patru_pattiyal_table" else "pattu_patru_pattiyal_table"
        val dbFile = resolveActiveDatabase(dbName) ?: return emptyList()

        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null
        val list = mutableListOf<PatruPattiyalInaippuTharavuru>()
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            ensureTables(db, mode)
            cursor = db.rawQuery("SELECT * FROM $junctionTable WHERE patru_id = ? ORDER BY id ASC", arrayOf(patruId.toString()))
            while (cursor.moveToNext()) {
                list.add(
                    PatruPattiyalInaippuTharavuru(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        patruId = cursor.getLong(cursor.getColumnIndexOrThrow("patru_id")),
                        pattiyalId = cursor.getLong(cursor.getColumnIndexOrThrow("pattiyal_id")),
                        poruthiyaThogai = cursor.getDouble(cursor.getColumnIndexOrThrow("poruthiya_thogai"))
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(tag, "Error loading links for receipt $patruId: ${e.message}", e)
        } finally {
            cursor?.close()
            db?.close()
        }
        return list
    }

    override fun saveReceiptWithLinks(mode: AppMode, receipt: PatrugalTharavuru, links: List<PatruPattiyalInaippuTharavuru>): Long {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val junctionTable = if (mode == AppMode.KOOLI) "kooli_patru_pattiyal_table" else "pattu_patru_pattiyal_table"
        val dbFile = resolveActiveDatabase(dbName) ?: return -1L

        val receiptId = saveReceipt(mode, receipt)
        if (receiptId <= 0L) return -1L

        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            ensureTables(db, mode)
            db.beginTransaction()
            try {
                db.delete(junctionTable, "patru_id = ?", arrayOf(receiptId.toString()))
                for (link in links) {
                    val values = ContentValues().apply {
                        put("patru_id", receiptId)
                        put("pattiyal_id", link.pattiyalId)
                        put("poruthiya_thogai", link.poruthiyaThogai)
                    }
                    db.insert(junctionTable, null, values)
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        } catch (e: Exception) {
            Log.e(tag, "Error saving links for receipt $receiptId: ${e.message}", e)
        } finally {
            db?.close()
        }

        return receiptId
    }

    override fun getPaidAmountForInvoice(mode: AppMode, invoiceId: Long): Double {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val patrugalTable = if (mode == AppMode.KOOLI) "kooli_patrugal_table" else "pattu_patrugal_table"
        val junctionTable = if (mode == AppMode.KOOLI) "kooli_patru_pattiyal_table" else "pattu_patru_pattiyal_table"
        val dbFile = resolveActiveDatabase(dbName) ?: return 0.0

        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            ensureTables(db, mode)
            val sql = """
                SELECT SUM(j.poruthiya_thogai) FROM $junctionTable j
                JOIN $patrugalTable p ON j.patru_id = p.id
                WHERE j.pattiyal_id = ? AND p.is_deleted = 0
            """.trimIndent()
            cursor = db.rawQuery(sql, arrayOf(invoiceId.toString()))
            if (cursor.moveToNext() && !cursor.isNull(0)) {
                return cursor.getDouble(0)
            }
        } catch (e: Exception) {
            Log.e(tag, "Error getting paid amount for invoice $invoiceId: ${e.message}", e)
        } finally {
            cursor?.close()
            db?.close()
        }
        return 0.0
    }

    override fun getPaidAmountsForInvoices(mode: AppMode, invoiceIds: List<Long>): Map<Long, Double> {
        if (invoiceIds.isEmpty()) return emptyMap()
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val patrugalTable = if (mode == AppMode.KOOLI) "kooli_patrugal_table" else "pattu_patrugal_table"
        val junctionTable = if (mode == AppMode.KOOLI) "kooli_patru_pattiyal_table" else "pattu_patru_pattiyal_table"
        val dbFile = resolveActiveDatabase(dbName) ?: return emptyMap()

        val map = mutableMapOf<Long, Double>()
        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            ensureTables(db, mode)
            val inClause = invoiceIds.joinToString(",")
            val sql = """
                SELECT j.pattiyal_id, SUM(j.poruthiya_thogai) as total_paid
                FROM $junctionTable j
                JOIN $patrugalTable p ON j.patru_id = p.id
                WHERE j.pattiyal_id IN ($inClause) AND p.is_deleted = 0
                GROUP BY j.pattiyal_id
            """.trimIndent()
            cursor = db.rawQuery(sql, null)
            while (cursor.moveToNext()) {
                map[cursor.getLong(0)] = cursor.getDouble(1)
            }
        } catch (e: Exception) {
            Log.e(tag, "Error getting paid amounts for invoices: ${e.message}", e)
        } finally {
            cursor?.close()
            db?.close()
        }
        return map
    }

    private fun cursorToMerchant(cursor: Cursor): VaangunarTharavuru {
        fun getString(col: String): String {
            val idx = cursor.getColumnIndex(col)
            return if (idx >= 0 && !cursor.isNull(idx)) cursor.getString(idx) else ""
        }
        fun getLong(col: String, defaultVal: Long = 0L): Long {
            val idx = cursor.getColumnIndex(col)
            return if (idx >= 0 && !cursor.isNull(idx)) cursor.getLong(idx) else defaultVal
        }
        fun getNullableLong(col: String): Long? {
            val idx = cursor.getColumnIndex(col)
            return if (idx >= 0 && !cursor.isNull(idx)) cursor.getLong(idx) else null
        }
        fun getInt(col: String, defaultVal: Int = 0): Int {
            val idx = cursor.getColumnIndex(col)
            return if (idx >= 0 && !cursor.isNull(idx)) cursor.getInt(idx) else defaultVal
        }

        val naaduMap = MozhiJsonConverter.parse(getString("naadu"))
        val naadu = if (naaduMap.isEmpty()) mapOf("en" to "India", "ta" to "இந்தியா") else naaduMap
        val rawCreatedAt = getLong("created_at")
        val createdAt = if (rawCreatedAt in 1..9999999999L) rawCreatedAt * 1000 else rawCreatedAt
        val rawUpdatedAt = getLong("updated_at")
        val updatedAt = if (rawUpdatedAt in 1..9999999999L) rawUpdatedAt * 1000 else rawUpdatedAt
        val rawDeletedAt = getNullableLong("deleted_at")
        val deletedAt = if (rawDeletedAt != null && rawDeletedAt in 1..9999999999L) rawDeletedAt * 1000 else rawDeletedAt

        return VaangunarTharavuru(
            id = getLong("id"),
            peyar = MozhiJsonConverter.parse(getString("peyar")),
            mugavari = MozhiJsonConverter.parse(getString("mugavari")),
            oor = MozhiJsonConverter.parse(getString("oor")),
            maavattam = MozhiJsonConverter.parse(getString("maavattam")),
            maanilam = MozhiJsonConverter.parse(getString("maanilam")),
            naadu = naadu,
            velinaadMugavari = MozhiJsonConverter.parse(getString("velinaad_mugavari")),
            anjalKuriyeedu = getString("anjal_kuriyeedu"),
            gstin = getString("gstin"),
            minnanjal = getString("minnanjal"),
            tholaipaesi = getString("tholaipaesi"),
            createdAt = createdAt,
            updatedAt = updatedAt,
            isDeleted = getInt("is_deleted", 0) == 1,
            deletedAt = deletedAt
        )
    }

    private fun cursorToItem(cursor: Cursor): PorulTharavuru {
        fun getString(col: String): String {
            val idx = cursor.getColumnIndex(col)
            return if (idx >= 0 && !cursor.isNull(idx)) cursor.getString(idx) else ""
        }
        fun getLong(col: String, defaultVal: Long = 0L): Long {
            val idx = cursor.getColumnIndex(col)
            return if (idx >= 0 && !cursor.isNull(idx)) cursor.getLong(idx) else defaultVal
        }
        fun getNullableLong(col: String): Long? {
            val idx = cursor.getColumnIndex(col)
            return if (idx >= 0 && !cursor.isNull(idx)) cursor.getLong(idx) else null
        }
        fun getInt(col: String, defaultVal: Int = 0): Int {
            val idx = cursor.getColumnIndex(col)
            return if (idx >= 0 && !cursor.isNull(idx)) cursor.getInt(idx) else defaultVal
        }
        fun getDouble(col: String, defaultVal: Double = 0.0): Double {
            val idx = cursor.getColumnIndex(col)
            return if (idx >= 0 && !cursor.isNull(idx)) cursor.getDouble(idx) else defaultVal
        }

        val rawCreatedAt = getLong("created_at")
        val createdAt = if (rawCreatedAt in 1..9999999999L) rawCreatedAt * 1000 else rawCreatedAt
        val rawUpdatedAt = getLong("updated_at")
        val updatedAt = if (rawUpdatedAt in 1..9999999999L) rawUpdatedAt * 1000 else rawUpdatedAt
        val rawDeletedAt = getNullableLong("deleted_at")
        val deletedAt = if (rawDeletedAt != null && rawDeletedAt in 1..9999999999L) rawDeletedAt * 1000 else rawDeletedAt

        return PorulTharavuru(
            id = getLong("id"),
            porulPeyar = MozhiJsonConverter.parse(getString("porul_peyar")),
            hsnCode = getString("hsn_code"),
            vilai = getDouble("vilai"),
            variVeetham = getDouble("vari_veetham"),
            alavuVagai = getString("alavu_vagai").ifEmpty { "quantity" },
            alagu = getString("alagu").ifEmpty { "Nos" },
            createdAt = createdAt,
            updatedAt = updatedAt,
            isDeleted = getInt("is_deleted", 0) == 1,
            deletedAt = deletedAt
        )
    }

    private fun cursorToInvoice(cursor: Cursor): PattiyalTharavuru {
        fun getString(col: String): String {
            val idx = cursor.getColumnIndex(col)
            return if (idx >= 0 && !cursor.isNull(idx)) cursor.getString(idx) else ""
        }
        fun getLong(col: String, defaultVal: Long = 0L): Long {
            val idx = cursor.getColumnIndex(col)
            return if (idx >= 0 && !cursor.isNull(idx)) cursor.getLong(idx) else defaultVal
        }
        fun getNullableLong(col: String): Long? {
            val idx = cursor.getColumnIndex(col)
            return if (idx >= 0 && !cursor.isNull(idx)) cursor.getLong(idx) else null
        }
        fun getInt(col: String, defaultVal: Int = 0): Int {
            val idx = cursor.getColumnIndex(col)
            return if (idx >= 0 && !cursor.isNull(idx)) cursor.getInt(idx) else defaultVal
        }
        fun getDouble(col: String, defaultVal: Double = 0.0): Double {
            val idx = cursor.getColumnIndex(col)
            return if (idx >= 0 && !cursor.isNull(idx)) cursor.getDouble(idx) else defaultVal
        }

        val rawNaal = getLong("pattiyal_naal")
        val pattiyalNaal = if (rawNaal in 1..9999999999L) rawNaal * 1000 else if (rawNaal > 0L) rawNaal else System.currentTimeMillis()
        val rawCreatedAt = getLong("created_at")
        val createdAt = if (rawCreatedAt in 1..9999999999L) rawCreatedAt * 1000 else rawCreatedAt
        val rawUpdatedAt = getLong("updated_at")
        val updatedAt = if (rawUpdatedAt in 1..9999999999L) rawUpdatedAt * 1000 else rawUpdatedAt
        val rawDeletedAt = getNullableLong("deleted_at")
        val deletedAt = if (rawDeletedAt != null && rawDeletedAt in 1..9999999999L) rawDeletedAt * 1000 else rawDeletedAt

        return PattiyalTharavuru(
            id = getLong("id"),
            niruvanamId = getNullableLong("niruvanam_id"),
            patrucheettuEn = getString("patrucheettu_en"),
            finYear = getInt("fin_year"),
            vanakkam = getInt("vanakkam", 1),
            pattiyalVagai = getString("pattiyal_vagai").ifEmpty { "tax-invoice" },
            vaangunarId = getNullableLong("vaangunar_id"),
            vaangunarPeyar = MozhiJsonConverter.parse(getString("vaangunar_peyar")),
            vaangunarMunvari = MozhiJsonConverter.parse(getString("vaangunar_munvari")),
            pattiyalNaal = pattiyalNaal,
            tharavugal = getString("tharavugal").ifEmpty { "[]" },
            mothaThogai = getDouble("motha_thogai"),
            thallupadi = getDouble("thallupadi"),
            podhuThallupadiMathippu = getDouble("podhu_thallupadi_mathippu"),
            podhuThallupadiVagai = getString("podhu_thallupadi_vagai").ifEmpty { "%" },
            podhuThallupadiThogai = getDouble("podhu_thallupadi_thogai"),
            variThogai = getDouble("vari_thogai"),
            variTharavugal = getString("vari_tharavugal").ifEmpty { "{}" },
            mothaEdai = getDouble("motha_edai"),
            setharamGrams = getDouble("setharam_grams"),
            thabaalThogai = getDouble("thabaal_thogai"),
            ahimsaPattuThogai = getDouble("ahimsa_pattu_thogai"),
            piravariVugal = getString("piravari_vugal").ifEmpty { "[]" },
            sonthaViruppangal = getString("sontha_viruppangal").ifEmpty { "{}" },
            nibandhanaigal = getString("nibandhanaigal"),
            ullkurippu = getString("ullkurippu"),
            vangiTharavugal = getString("vangi_tharavugal").ifEmpty { "{}" },
            createdAt = createdAt,
            updatedAt = updatedAt,
            isDeleted = getInt("is_deleted", 0) == 1,
            deletedAt = deletedAt
        )
    }

    private fun cursorToReceipt(cursor: Cursor): PatrugalTharavuru {
        fun getString(col: String): String {
            val idx = cursor.getColumnIndex(col)
            return if (idx >= 0 && !cursor.isNull(idx)) cursor.getString(idx) else ""
        }
        fun getNullableString(col: String): String? {
            val idx = cursor.getColumnIndex(col)
            return if (idx >= 0 && !cursor.isNull(idx)) cursor.getString(idx) else null
        }
        fun getLong(col: String, defaultVal: Long = 0L): Long {
            val idx = cursor.getColumnIndex(col)
            return if (idx >= 0 && !cursor.isNull(idx)) cursor.getLong(idx) else defaultVal
        }
        fun getNullableLong(col: String): Long? {
            val idx = cursor.getColumnIndex(col)
            return if (idx >= 0 && !cursor.isNull(idx)) cursor.getLong(idx) else null
        }
        fun getInt(col: String, defaultVal: Int = 0): Int {
            val idx = cursor.getColumnIndex(col)
            return if (idx >= 0 && !cursor.isNull(idx)) cursor.getInt(idx) else defaultVal
        }
        fun getDouble(col: String, defaultVal: Double = 0.0): Double {
            val idx = cursor.getColumnIndex(col)
            return if (idx >= 0 && !cursor.isNull(idx)) cursor.getDouble(idx) else defaultVal
        }

        val rawNaal = getLong("patru_naal")
        val patruNaal = if (rawNaal in 1..9999999999L) rawNaal * 1000 else if (rawNaal > 0L) rawNaal else System.currentTimeMillis()
        val rawCreatedAt = getLong("created_at")
        val createdAt = if (rawCreatedAt in 1..9999999999L) rawCreatedAt * 1000 else rawCreatedAt
        val rawUpdatedAt = getLong("updated_at")
        val updatedAt = if (rawUpdatedAt in 1..9999999999L) rawUpdatedAt * 1000 else rawUpdatedAt
        val rawDeletedAt = getNullableLong("deleted_at")
        val deletedAt = if (rawDeletedAt != null && rawDeletedAt in 1..9999999999L) rawDeletedAt * 1000 else rawDeletedAt

        return PatrugalTharavuru(
            id = getLong("id"),
            niruvanamId = getNullableLong("niruvanam_id"),
            patruEn = getString("patru_en"),
            finYear = getString("fin_year"),
            vanakkam = getInt("vanakkam", 1),
            vaangunarId = getNullableLong("vaangunar_id"),
            vaangunarPeyar = MozhiJsonConverter.parse(getString("vaangunar_peyar")),
            vaangunarMunvari = MozhiJsonConverter.parse(getString("vaangunar_munvari")),
            patruNaal = patruNaal,
            thogai = getDouble("thogai"),
            seluthumMurai = getString("seluthum_murai").ifEmpty { "cash" },
            vangiPeyar = getNullableString("vangi_peyar"),
            parivarthanaiEn = getNullableString("parivarthanai_en"),
            ullkurippu = getString("ullkurippu"),
            createdAt = createdAt,
            updatedAt = updatedAt,
            isDeleted = getInt("is_deleted", 0) == 1,
            deletedAt = deletedAt
        )
    }

    override fun clearAllData(mode: AppMode): Boolean {
        val dbFile = resolveActiveDatabase(if (mode == AppMode.KOOLI) coolieDbName else silkDbName) ?: return false
        val vaangunarTable = if (mode == AppMode.KOOLI) "kooli_vaangunar_table" else "pattu_vaangunar_table"
        val porulTable = if (mode == AppMode.KOOLI) "kooli_porul_table" else "pattu_porul_table"
        val pattiyalTable = if (mode == AppMode.KOOLI) "kooli_pattiyal_table" else "pattu_pattiyal_table"
        val patrugalTable = if (mode == AppMode.KOOLI) "kooli_patrugal_table" else "pattu_patrugal_table"
        val junctionTable = if (mode == AppMode.KOOLI) "kooli_patru_pattiyal_table" else "pattu_patru_pattiyal_table"

        return try {
            val db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            db.use {
                ensureTables(it, mode)
                it.delete(junctionTable, null, null)
                it.delete(patrugalTable, null, null)
                it.delete(pattiyalTable, null, null)
                it.delete(porulTable, null, null)
                it.delete(vaangunarTable, null, null)
            }
            true
        } catch (e: Exception) {
            Log.e(tag, "clearAllData failed for mode $mode: ${e.message}")
            false
        }
    }
}

private val androidBusinessHelperInstance by lazy { AndroidBusinessDatabaseHelper() }

actual fun getBusinessDatabaseHelper(): BusinessDatabaseHelper = androidBusinessHelperInstance
