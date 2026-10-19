package com.elvan.udukkai.data.settings

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.platform.AppContext
import java.io.File

class AndroidSettingsDatabaseHelper : SettingsDatabaseHelper {

    private val tag = "SettingsDatabaseHelper"
    private val factoryDbName = "udukkai_factory.db"
    private val textilesDbName = "udukkai_textiles.db"

    private fun getDbName(mode: AppMode): String =
        if (mode == AppMode.FACTORY) factoryDbName else textilesDbName

    private fun getTableName(mode: AppMode): String =
        if (mode == AppMode.FACTORY) "factory_niruvana_tharavugal_table" else "textiles_niruvana_tharavugal_table"

    private fun resolveActiveDatabase(dbName: String): File? {
        if (!AppContext.isInitialized) {
            Log.w(tag, "AppContext is not initialized yet!")
            return null
        }
        val localDb = AppContext.context.getDatabasePath(dbName)
        localDb.parentFile?.mkdirs()

        // ── Auto-migration: if target doesn't exist, check for legacy DB ──
        if (!localDb.exists()) {
            val legacyName = if (dbName == factoryDbName) "udukkai_kooli.db" else if (dbName == textilesDbName) "udukkai_pattu.db" else null
            if (legacyName != null) {
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
        }
        return localDb
    }

    override fun scanAndSync(): DatabaseScanReport {
        val factoryFile = resolveActiveDatabase(factoryDbName)
        val textilesFile = resolveActiveDatabase(textilesDbName)

        val report = DatabaseScanReport(
            isFactoryFound = factoryFile != null && factoryFile.exists(),
            factoryPath = factoryFile?.absolutePath,
            isTextilesFound = textilesFile != null && textilesFile.exists(),
            textilesPath = textilesFile?.absolutePath,
            message = "Factory: ${factoryFile?.absolutePath ?: "Not Found"}, Textiles: ${textilesFile?.absolutePath ?: "Not Found"}"
        )
        Log.i(tag, "Database scan result: ${report.message}")
        return report
    }

    private fun ensureTables(db: SQLiteDatabase, mode: AppMode) {
        val legacyName = if (mode == AppMode.FACTORY) "kooli_niruvana_tharavugal_table" else "pattu_niruvana_tharavugal_table"
        val tableName = getTableName(mode)
        try {
            val curOld = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", arrayOf(legacyName))
            val oldExists = curOld.moveToFirst()
            curOld.close()
            if (oldExists) {
                val curNew = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", arrayOf(tableName))
                val newExists = curNew.moveToFirst()
                curNew.close()
                if (!newExists) {
                    Log.i(tag, "Renaming legacy table $legacyName to $tableName")
                    db.execSQL("ALTER TABLE \"$legacyName\" RENAME TO \"$tableName\"")
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Table rename error: ${e.message}")
        }
        val sql = """
            CREATE TABLE IF NOT EXISTS "$tableName" (
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
        """.trimIndent()
        db.execSQL(sql)
        ensureColumns(db, tableName)
    }

    override fun loadProfile(mode: AppMode, profileId: Long?): NiruvanaTharavugal? {
        val dbName = getDbName(mode)
        val tableName = getTableName(mode)
        val dbFile = resolveActiveDatabase(dbName) ?: return null

        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            ensureTables(db, mode)
            val sql = if (profileId != null) {
                "SELECT * FROM $tableName WHERE id = ? AND is_deleted = 0 LIMIT 1"
            } else {
                "SELECT * FROM $tableName WHERE is_deleted = 0 ORDER BY id ASC LIMIT 1"
            }
            val args = if (profileId != null) arrayOf(profileId.toString()) else null
            cursor = db.rawQuery(sql, args)
            if (cursor.moveToFirst()) {
                return cursorToProfile(cursor, mode)
            }
        } catch (e: Exception) {
            Log.e(tag, "Error loading profile from $tableName: ${e.message}", e)
        } finally {
            cursor?.close()
            db?.close()
        }
        return null
    }

    override fun loadAllProfiles(mode: AppMode): List<NiruvanaTharavugal> {
        val dbName = getDbName(mode)
        val tableName = getTableName(mode)
        val dbFile = resolveActiveDatabase(dbName) ?: return emptyList()

        val list = mutableListOf<NiruvanaTharavugal>()
        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            ensureTables(db, mode)
            cursor = db.rawQuery("SELECT * FROM $tableName WHERE is_deleted = 0 ORDER BY id ASC", null)
            while (cursor.moveToNext()) {
                list.add(cursorToProfile(cursor, mode))
            }
        } catch (e: Exception) {
            Log.e(tag, "Error loading all profiles from $tableName: ${e.message}", e)
        } finally {
            cursor?.close()
            db?.close()
        }
        return list
    }

    private fun ensureColumns(db: SQLiteDatabase, tableName: String) {
        val columns = mutableSetOf<String>()
        try {
            db.rawQuery("PRAGMA table_info(\"$tableName\")", null).use { cursor ->
                val nameIdx = cursor.getColumnIndex("name")
                while (cursor.moveToNext()) {
                    if (nameIdx >= 0) columns.add(cursor.getString(nameIdx).lowercase())
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to read PRAGMA table_info on $tableName: ${e.message}")
        }

        fun addCol(colName: String, defSql: String) {
            if (!columns.contains(colName.lowercase())) {
                try {
                    db.execSQL("ALTER TABLE \"$tableName\" ADD COLUMN $colName $defSql")
                } catch (e: Exception) {
                    Log.w(tag, "Failed to add column $colName to $tableName: ${e.message}")
                }
            }
        }

        addCol("mudhan_mozhi", "TEXT DEFAULT 'ta'")
        addCol("thunai_mozhi", "TEXT DEFAULT 'en'")
        addCol("iru_mozhi", "INTEGER DEFAULT 1")
        addCol("gst_pirippugal", "INTEGER DEFAULT 0")
        addCol("thoatra_niram", "TEXT DEFAULT '#388e3c'")
        addCol("oppam_mudhal_peyar", "TEXT DEFAULT ''")
        addCol("oppam_irudhi_peyar", "TEXT DEFAULT ''")
    }

    override fun saveProfile(mode: AppMode, profile: NiruvanaTharavugal): Boolean {
        val dbName = getDbName(mode)
        val tableName = getTableName(mode)
        val dbFile = resolveActiveDatabase(dbName) ?: return false

        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            ensureTables(db, mode)

            val values = ContentValues().apply {
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
                put("oppam_peyar", profile.fullSignatoryName)
                put("oppam_mudhal_peyar", profile.oppamMudhalPeyar)
                put("oppam_irudhi_peyar", profile.oppamIrudhiPeyar)
                put("adaimozhi", MozhiJsonConverter.stringify(profile.adaimozhi))
                put("upi_id", profile.upiId)
                put("thoatra_niram", profile.thoatraNiram)
                put("updated_at", System.currentTimeMillis() / 1000)

                put("mudhan_mozhi", profile.mudhanMozhi.ifEmpty { "ta" })
                put("thunai_mozhi", profile.thunaiMozhi.ifEmpty { "en" })
                put("iru_mozhi", if (profile.iruMozhi) 1 else 0)
                put("gst_pirippugal", if (profile.gstPirippugal) 1 else 0)
            }

            var rowsAffected = if (profile.id != null && profile.id!! > 0L) {
                db.update(tableName, values, "id = ?", arrayOf(profile.id.toString()))
            } else 0

            if (rowsAffected == 0) {
                values.put("is_deleted", 0)
                val newId = db.insert(tableName, null, values)
                if (newId > 0) {
                    profile.id = newId
                    rowsAffected = 1
                }
            }

            return rowsAffected > 0
        } catch (e: Exception) {
            Log.e(tag, "Error saving profile to $tableName: ${e.message}", e)
            return false
        } finally {
            db?.close()
        }
    }

    override fun deleteProfile(mode: AppMode, profileId: Long): Boolean {
        val dbName = getDbName(mode)
        val tableName = getTableName(mode)
        val dbFile = resolveActiveDatabase(dbName) ?: return false

        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            ensureTables(db, mode)
            val values = ContentValues().apply {
                put("is_deleted", 1)
                put("updated_at", System.currentTimeMillis() / 1000)
            }
            val rows = db.update(tableName, values, "id = ?", arrayOf(profileId.toString()))
            return rows > 0
        } catch (e: Exception) {
            Log.e(tag, "Error deleting profile $profileId from $tableName: ${e.message}", e)
            return false
        } finally {
            db?.close()
        }
    }

    override fun clearProfiles(mode: AppMode): Boolean {
        val dbName = getDbName(mode)
        val tableName = getTableName(mode)
        val dbFile = resolveActiveDatabase(dbName) ?: return false

        var db: SQLiteDatabase? = null
        return try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            ensureTables(db, mode)
            db.delete(tableName, null, null)
            true
        } catch (e: Exception) {
            Log.e(tag, "Error clearing profiles for $mode: ${e.message}", e)
            false
        } finally {
            db?.close()
        }
    }

    private fun cursorToProfile(cursor: Cursor, mode: AppMode): NiruvanaTharavugal {
        fun getString(col: String): String {
            val idx = cursor.getColumnIndex(col)
            return if (idx >= 0 && !cursor.isNull(idx)) cursor.getString(idx) else ""
        }
        fun getLong(col: String): Long? {
            val idx = cursor.getColumnIndex(col)
            return if (idx >= 0 && !cursor.isNull(idx)) cursor.getLong(idx) else null
        }
        fun getInt(col: String, defaultVal: Int): Int {
            val idx = cursor.getColumnIndex(col)
            return if (idx >= 0 && !cursor.isNull(idx)) cursor.getInt(idx) else defaultVal
        }

        val mudhan = getString("mudhan_mozhi").ifEmpty { "ta" }
        val thunai = getString("thunai_mozhi").ifEmpty { "en" }
        val iru = getInt("iru_mozhi", 1) == 1
        val gstPirippugal = getInt("gst_pirippugal", 0) == 1

        return NiruvanaTharavugal(
            id = getLong("id"),
            mudhanMozhi = mudhan,
            thunaiMozhi = thunai,
            iruMozhi = iru,
            gstPirippugal = gstPirippugal,
            niruvanathinPeyar = MozhiJsonConverter.parse(getString("niruvanathin_peyar")),
            kurumPeyar = getString("kurum_peyar"),
            tholaipaesi1 = getString("tholaipaesi1"),
            tholaipaesi2 = getString("tholaipaesi2"),
            minnanjal = getString("minnanjal"),
            gstin = getString("gstin"),
            mugavari = MozhiJsonConverter.parse(getString("mugavari")),
            oor = MozhiJsonConverter.parse(getString("oor")),
            maavattam = MozhiJsonConverter.parse(getString("maavattam")),
            maanilam = MozhiJsonConverter.parse(getString("maanilam")),
            naadu = MozhiJsonConverter.parse(getString("naadu")).ifEmpty { mutableMapOf("en" to "India", "ta" to "இந்தியா") },
            anjalKuriyeedu = getString("anjal_kuriyeedu"),
            vangiPeyar = MozhiJsonConverter.parse(getString("vangi_peyar")),
            kilai = MozhiJsonConverter.parse(getString("kilai")),
            vangiKanakku = getString("vangi_kanakku"),
            ifsc = getString("ifsc"),
            oavuru = getString("oavuru"),
            agalaOavuru = getString("agala_oavuru"),
            thalaippuVadivu = getString("thalaippu_vadivu").ifEmpty { "small" },
            kaiyoppam = getString("kaiyoppam"),
            oppamPeyar = getString("oppam_peyar"),
            oppamMudhalPeyar = getString("oppam_mudhal_peyar"),
            oppamIrudhiPeyar = getString("oppam_irudhi_peyar"),
            adaimozhi = MozhiJsonConverter.parse(getString("adaimozhi")),
            upiId = getString("upi_id"),
            thoatraNiram = getString("thoatra_niram").ifEmpty { if (mode == AppMode.PATTU) "#6a1b9a" else "#388e3c" }
        )
    }
}

private val androidHelperInstance by lazy { AndroidSettingsDatabaseHelper() }

actual fun getSettingsDatabaseHelper(): SettingsDatabaseHelper = androidHelperInstance
