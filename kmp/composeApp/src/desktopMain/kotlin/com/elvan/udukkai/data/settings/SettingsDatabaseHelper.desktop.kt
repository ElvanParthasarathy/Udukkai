package com.elvan.udukkai.data.settings

import com.elvan.udukkai.core.mode.AppMode
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

class DesktopSettingsDatabaseHelper : SettingsDatabaseHelper {

    private val factoryDbName = "udukkai_factory.db"
    private val textilesDbName = "udukkai_textiles.db"

    private fun getDbName(mode: AppMode): String =
        if (mode == AppMode.FACTORY) factoryDbName else textilesDbName

    private fun getTableName(mode: AppMode): String =
        if (mode == AppMode.FACTORY) "factory_niruvana_tharavugal_table" else "textiles_niruvana_tharavugal_table"

    private fun resolveActiveDatabase(dbName: String): File {
        val target = com.elvan.udukkai.data.DesktopDbPaths.getDbFile(dbName)
        if (!target.exists()) {
            val legacyName = if (dbName == factoryDbName) "udukkai_kooli.db" else if (dbName == textilesDbName) "udukkai_pattu.db" else null
            if (legacyName != null) {
                val legacyFile = com.elvan.udukkai.data.DesktopDbPaths.getDbFile(legacyName)
                if (legacyFile.exists()) {
                    try {
                        println("Desktop auto-migrating legacy DB $legacyName -> $dbName")
                        legacyFile.copyTo(target, overwrite = true)
                    } catch (e: Exception) {
                        println("Failed to copy legacy DB $legacyName: ${e.message}")
                    }
                }
            }
        }
        return target
    }

    private fun getConnection(file: File): Connection {
        Class.forName("org.sqlite.JDBC")
        file.parentFile?.mkdirs()
        return DriverManager.getConnection("jdbc:sqlite:${file.absolutePath}")
    }

    override fun scanAndSync(): DatabaseScanReport {
        val factory = resolveActiveDatabase(factoryDbName)
        val textiles = resolveActiveDatabase(textilesDbName)
        return DatabaseScanReport(
            isFactoryFound = factory.exists(),
            factoryPath = factory.absolutePath,
            isTextilesFound = textiles.exists(),
            textilesPath = textiles.absolutePath,
            message = "Desktop Factory: ${factory.absolutePath}, Textiles: ${textiles.absolutePath}"
        )
    }

    private fun renameTableIfExists(conn: Connection, oldName: String, newName: String) {
        try {
            conn.prepareStatement("SELECT name FROM sqlite_master WHERE type='table' AND name=?").use { stmt ->
                stmt.setString(1, oldName)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) {
                        conn.prepareStatement("SELECT name FROM sqlite_master WHERE type='table' AND name=?").use { stmtNew ->
                            stmtNew.setString(1, newName)
                            stmtNew.executeQuery().use { rsNew ->
                                if (!rsNew.next()) {
                                    conn.createStatement().use { alterStmt ->
                                        alterStmt.execute("ALTER TABLE \"$oldName\" RENAME TO \"$newName\"")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {}
    }

    private fun ensureTables(conn: Connection, mode: AppMode) {
        val legacyName = if (mode == AppMode.FACTORY) "kooli_niruvana_tharavugal_table" else "pattu_niruvana_tharavugal_table"
        val tableName = getTableName(mode)
        renameTableIfExists(conn, legacyName, tableName)

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
        conn.createStatement().use { stmt ->
            stmt.execute(sql)
        }
        ensureColumns(conn, tableName)
    }

    private fun ensureColumns(conn: Connection, tableName: String) {
        val columns = mutableSetOf<String>()
        try {
            conn.createStatement().use { stmt ->
                stmt.executeQuery("PRAGMA table_info(\"$tableName\")").use { rs ->
                    while (rs.next()) {
                        columns.add(rs.getString("name").lowercase())
                    }
                }
            }
        } catch (e: Exception) {
            println("Desktop table_info failed on $tableName: ${e.message}")
        }

        fun addCol(colName: String, defSql: String) {
            if (!columns.contains(colName.lowercase())) {
                try {
                    conn.createStatement().use { stmt ->
                        stmt.execute("ALTER TABLE \"$tableName\" ADD COLUMN $colName $defSql")
                    }
                } catch (e: Exception) {
                    println("Desktop addCol $colName on $tableName failed: ${e.message}")
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

    override fun loadProfile(mode: AppMode, profileId: Long?): NiruvanaTharavugal? {
        val dbName = getDbName(mode)
        val tableName = getTableName(mode)
        val file = resolveActiveDatabase(dbName)

        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                val sql = if (profileId != null) {
                    "SELECT * FROM $tableName WHERE id = ? AND is_deleted = 0 LIMIT 1"
                } else {
                    "SELECT * FROM $tableName WHERE is_deleted = 0 ORDER BY id ASC LIMIT 1"
                }
                conn.prepareStatement(sql).use { stmt ->
                    if (profileId != null) {
                        stmt.setLong(1, profileId)
                    }
                    stmt.executeQuery().use { rs ->
                        if (rs.next()) {
                            return rsToProfile(rs, mode)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Desktop error loading profile from $tableName: ${e.message}")
        }
        return null
    }

    override fun loadAllProfiles(mode: AppMode): List<NiruvanaTharavugal> {
        val dbName = getDbName(mode)
        val tableName = getTableName(mode)
        val file = resolveActiveDatabase(dbName)

        val list = mutableListOf<NiruvanaTharavugal>()
        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                val sql = "SELECT * FROM $tableName WHERE is_deleted = 0 ORDER BY id ASC"
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sql).use { rs ->
                        while (rs.next()) {
                            list.add(rsToProfile(rs, mode))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Desktop error loading all profiles from $tableName: ${e.message}")
        }
        return list
    }

    override fun saveProfile(mode: AppMode, profile: NiruvanaTharavugal): Boolean {
        val dbName = getDbName(mode)
        val tableName = getTableName(mode)
        val file = resolveActiveDatabase(dbName)

        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                val isUpdate = profile.id != null && profile.id!! > 0L

                val sql = if (isUpdate) {
                    """
                    UPDATE $tableName SET
                        mudhan_mozhi = ?, thunai_mozhi = ?, iru_mozhi = ?, gst_pirippugal = ?,
                        niruvanathin_peyar = ?, kurum_peyar = ?, tholaipaesi1 = ?, tholaipaesi2 = ?,
                        minnanjal = ?, gstin = ?, mugavari = ?, oor = ?, maavattam = ?, maanilam = ?,
                        naadu = ?, anjal_kuriyeedu = ?, vangi_peyar = ?, kilai = ?, vangi_kanakku = ?,
                        ifsc = ?, oavuru = ?, agala_oavuru = ?, thalaippu_vadivu = ?, kaiyoppam = ?,
                        oppam_peyar = ?, oppam_mudhal_peyar = ?, oppam_irudhi_peyar = ?,
                        adaimozhi = ?, upi_id = ?, thoatra_niram = ?, updated_at = ?
                    WHERE id = ?
                    """.trimIndent()
                } else {
                    """
                    INSERT INTO $tableName (
                        mudhan_mozhi, thunai_mozhi, iru_mozhi, gst_pirippugal,
                        niruvanathin_peyar, kurum_peyar, tholaipaesi1, tholaipaesi2,
                        minnanjal, gstin, mugavari, oor, maavattam, maanilam,
                        naadu, anjal_kuriyeedu, vangi_peyar, kilai, vangi_kanakku,
                        ifsc, oavuru, agala_oavuru, thalaippu_vadivu, kaiyoppam,
                        oppam_peyar, oppam_mudhal_peyar, oppam_irudhi_peyar,
                        adaimozhi, upi_id, thoatra_niram, updated_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """.trimIndent()
                }

                conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS).use { stmt ->
                    var idx = 1
                    stmt.setString(idx++, profile.mudhanMozhi.ifEmpty { "ta" })
                    stmt.setString(idx++, profile.thunaiMozhi.ifEmpty { "en" })
                    stmt.setInt(idx++, if (profile.iruMozhi) 1 else 0)
                    stmt.setInt(idx++, if (profile.gstPirippugal) 1 else 0)
                    stmt.setString(idx++, MozhiJsonConverter.stringify(profile.niruvanathinPeyar))
                    stmt.setString(idx++, profile.kurumPeyar)
                    stmt.setString(idx++, profile.tholaipaesi1)
                    stmt.setString(idx++, profile.tholaipaesi2)
                    stmt.setString(idx++, profile.minnanjal)
                    stmt.setString(idx++, profile.gstin)
                    stmt.setString(idx++, MozhiJsonConverter.stringify(profile.mugavari))
                    stmt.setString(idx++, MozhiJsonConverter.stringify(profile.oor))
                    stmt.setString(idx++, MozhiJsonConverter.stringify(profile.maavattam))
                    stmt.setString(idx++, MozhiJsonConverter.stringify(profile.maanilam))
                    stmt.setString(idx++, MozhiJsonConverter.stringify(profile.naadu))
                    stmt.setString(idx++, profile.anjalKuriyeedu)
                    stmt.setString(idx++, MozhiJsonConverter.stringify(profile.vangiPeyar))
                    stmt.setString(idx++, MozhiJsonConverter.stringify(profile.kilai))
                    stmt.setString(idx++, profile.vangiKanakku)
                    stmt.setString(idx++, profile.ifsc)
                    stmt.setString(idx++, profile.oavuru)
                    stmt.setString(idx++, profile.agalaOavuru)
                    stmt.setString(idx++, profile.thalaippuVadivu)
                    stmt.setString(idx++, profile.kaiyoppam)
                    stmt.setString(idx++, profile.fullSignatoryName)
                    stmt.setString(idx++, profile.oppamMudhalPeyar)
                    stmt.setString(idx++, profile.oppamIrudhiPeyar)
                    stmt.setString(idx++, MozhiJsonConverter.stringify(profile.adaimozhi))
                    stmt.setString(idx++, profile.upiId)
                    stmt.setString(idx++, profile.thoatraNiram)
                    stmt.setLong(idx++, System.currentTimeMillis() / 1000)

                    if (isUpdate) {
                        stmt.setLong(idx++, profile.id ?: 1L)
                    }

                    val affected = stmt.executeUpdate()
                    if (!isUpdate && affected > 0) {
                        stmt.generatedKeys.use { gks ->
                            if (gks.next()) {
                                profile.id = gks.getLong(1)
                            }
                        }
                    }
                    return affected > 0
                }
            }
        } catch (e: Exception) {
            println("Desktop error saving profile to $tableName: ${e.message}")
            return false
        }
    }

    override fun deleteProfile(mode: AppMode, profileId: Long): Boolean {
        val dbName = getDbName(mode)
        val tableName = getTableName(mode)
        val file = resolveActiveDatabase(dbName)

        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                val sql = "UPDATE $tableName SET is_deleted = 1, updated_at = ?" + " WHERE id = ?"
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setLong(1, System.currentTimeMillis() / 1000)
                    stmt.setLong(2, profileId)
                    val updated = stmt.executeUpdate()
                    return updated > 0
                }
            }
        } catch (e: Exception) {
            println("Desktop error deleting profile $profileId from $tableName: ${e.message}")
            return false
        }
    }

    override fun clearProfiles(mode: AppMode): Boolean {
        val dbName = getDbName(mode)
        val tableName = getTableName(mode)
        val file = resolveActiveDatabase(dbName)

        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                val sql = "DELETE FROM $tableName"
                conn.createStatement().use { stmt ->
                    stmt.executeUpdate(sql)
                    return true
                }
            }
        } catch (e: Exception) {
            println("Desktop error clearing profiles from $tableName: ${e.message}")
            return false
        }
    }

    private fun rsToProfile(rs: ResultSet, mode: AppMode): NiruvanaTharavugal {
        fun getString(col: String): String {
            return try { rs.getString(col) ?: "" } catch (_: Exception) { "" }
        }
        fun getLong(col: String): Long? {
            return try { val v = rs.getLong(col); if (rs.wasNull()) null else v } catch (_: Exception) { null }
        }
        fun getInt(col: String, defaultVal: Int): Int {
            return try { val v = rs.getInt(col); if (rs.wasNull()) defaultVal else v } catch (_: Exception) { defaultVal }
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
            thoatraNiram = getString("thoatra_niram").ifEmpty { if (mode == AppMode.TEXTILES) "#6a1b9a" else "#388e3c" }
        )
    }
}

private val desktopHelperInstance by lazy { DesktopSettingsDatabaseHelper() }

actual fun getSettingsDatabaseHelper(): SettingsDatabaseHelper = desktopHelperInstance
