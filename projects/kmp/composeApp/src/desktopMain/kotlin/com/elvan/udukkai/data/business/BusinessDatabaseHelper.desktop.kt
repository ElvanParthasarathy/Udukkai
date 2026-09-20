package com.elvan.udukkai.data.business

import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.data.DesktopDbPaths
import com.elvan.udukkai.data.model.PattiyalTharavuru
import com.elvan.udukkai.data.model.PatruPattiyalInaippuTharavuru
import com.elvan.udukkai.data.model.PatrugalTharavuru
import com.elvan.udukkai.data.model.PorulTharavuru
import com.elvan.udukkai.data.model.VaangunarTharavuru
import com.elvan.udukkai.data.settings.MozhiJsonConverter
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Types

class DesktopBusinessDatabaseHelper : BusinessDatabaseHelper {

    private val coolieDbName = "udukkai_kooli.db"
    private val silkDbName = "udukkai_pattu.db"

    private fun resolveActiveDatabase(dbName: String): File {
        return DesktopDbPaths.getDbFile(dbName)
    }

    private fun getConnection(file: File): Connection {
        Class.forName("org.sqlite.JDBC")
        file.parentFile?.mkdirs()
        return DriverManager.getConnection("jdbc:sqlite:${file.absolutePath}")
    }

    private fun ensureTables(conn: Connection, mode: AppMode) {
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

        conn.createStatement().use { stmt ->
            stmt.execute(createVaangunarSql)
            stmt.execute(createPorulSql)
            stmt.execute(createPattiyalSql)
            stmt.execute(createPatrugalSql)
            stmt.execute(createJunctionSql)
        }
    }

    override fun loadAllMerchants(mode: AppMode): List<VaangunarTharavuru> {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_vaangunar_table" else "pattu_vaangunar_table"
        val file = resolveActiveDatabase(dbName)

        val list = mutableListOf<VaangunarTharavuru>()
        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                val sql = "SELECT * FROM $tableName WHERE is_deleted = 0 ORDER BY updated_at DESC, id DESC"
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sql).use { rs ->
                        while (rs.next()) {
                            list.add(rsToMerchant(rs))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Desktop error loading merchants from $tableName: ${e.message}")
        }
        return list
    }

    override fun saveMerchant(mode: AppMode, merchant: VaangunarTharavuru): Long {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_vaangunar_table" else "pattu_vaangunar_table"
        val file = resolveActiveDatabase(dbName)

        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                val nowSec = System.currentTimeMillis() / 1000
                val isUpdate = merchant.id > 0L

                val sql = if (isUpdate) {
                    """
                    UPDATE $tableName SET
                        peyar = ?, mugavari = ?, oor = ?, maavattam = ?, maanilam = ?, naadu = ?,
                        velinaad_mugavari = ?, anjal_kuriyeedu = ?, gstin = ?, minnanjal = ?, tholaipaesi = ?,
                        updated_at = ?, is_deleted = ?, deleted_at = ?
                    WHERE id = ?
                    """.trimIndent()
                } else {
                    """
                    INSERT INTO $tableName (
                        peyar, mugavari, oor, maavattam, maanilam, naadu,
                        velinaad_mugavari, anjal_kuriyeedu, gstin, minnanjal, tholaipaesi,
                        created_at, updated_at, is_deleted, deleted_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """.trimIndent()
                }

                conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { stmt ->
                    var idx = 1
                    stmt.setString(idx++, MozhiJsonConverter.stringify(merchant.peyar))
                    stmt.setString(idx++, MozhiJsonConverter.stringify(merchant.mugavari))
                    stmt.setString(idx++, MozhiJsonConverter.stringify(merchant.oor))
                    stmt.setString(idx++, MozhiJsonConverter.stringify(merchant.maavattam))
                    stmt.setString(idx++, MozhiJsonConverter.stringify(merchant.maanilam))
                    stmt.setString(idx++, MozhiJsonConverter.stringify(merchant.naadu))
                    stmt.setString(idx++, MozhiJsonConverter.stringify(merchant.velinaadMugavari))
                    stmt.setString(idx++, merchant.anjalKuriyeedu)
                    stmt.setString(idx++, merchant.gstin)
                    stmt.setString(idx++, merchant.minnanjal)
                    stmt.setString(idx++, merchant.tholaipaesi)

                    if (!isUpdate) {
                        val createdSec = if (merchant.createdAt > 10000000000L) merchant.createdAt / 1000 else if (merchant.createdAt > 0L) merchant.createdAt else nowSec
                        stmt.setLong(idx++, createdSec)
                    }

                    stmt.setLong(idx++, nowSec)
                    stmt.setInt(idx++, if (merchant.isDeleted) 1 else 0)

                    if (merchant.deletedAt != null) {
                        val delSec = if (merchant.deletedAt > 10000000000L) merchant.deletedAt / 1000 else merchant.deletedAt
                        stmt.setLong(idx++, delSec)
                    } else {
                        stmt.setNull(idx++, Types.INTEGER)
                    }

                    if (isUpdate) {
                        stmt.setLong(idx++, merchant.id)
                    }

                    val affected = stmt.executeUpdate()
                    if (isUpdate) {
                        return if (affected > 0) merchant.id else -1L
                    } else {
                        stmt.generatedKeys.use { gks ->
                            if (gks.next()) {
                                return gks.getLong(1)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Desktop error saving merchant to $tableName: ${e.message}")
        }
        return -1L
    }

    override fun deleteMerchant(mode: AppMode, id: Long): Boolean {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_vaangunar_table" else "pattu_vaangunar_table"
        val file = resolveActiveDatabase(dbName)

        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                val sql = "UPDATE $tableName SET is_deleted = 1, deleted_at = ?, updated_at = ? WHERE id = ?"
                conn.prepareStatement(sql).use { stmt ->
                    val nowSec = System.currentTimeMillis() / 1000
                    stmt.setLong(1, nowSec)
                    stmt.setLong(2, nowSec)
                    stmt.setLong(3, id)
                    val updated = stmt.executeUpdate()
                    return updated > 0
                }
            }
        } catch (e: Exception) {
            println("Desktop error deleting merchant $id from $tableName: ${e.message}")
            return false
        }
    }

    override fun loadDeletedMerchants(mode: AppMode): List<VaangunarTharavuru> {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_vaangunar_table" else "pattu_vaangunar_table"
        val file = resolveActiveDatabase(dbName)

        val list = mutableListOf<VaangunarTharavuru>()
        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                val sql = "SELECT * FROM $tableName WHERE is_deleted = 1 ORDER BY deleted_at DESC, id DESC"
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sql).use { rs ->
                        while (rs.next()) {
                            list.add(rsToMerchant(rs))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Desktop error loading deleted merchants from $tableName: ${e.message}")
        }
        return list
    }

    override fun restoreMerchant(mode: AppMode, id: Long): Boolean {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_vaangunar_table" else "pattu_vaangunar_table"
        val file = resolveActiveDatabase(dbName)

        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                val sql = "UPDATE $tableName SET is_deleted = 0, deleted_at = NULL, updated_at = ? WHERE id = ?"
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setLong(1, System.currentTimeMillis() / 1000)
                    stmt.setLong(2, id)
                    return stmt.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            println("Desktop error restoring merchant $id: ${e.message}")
            return false
        }
    }

    override fun permanentDeleteMerchant(mode: AppMode, id: Long): Boolean {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_vaangunar_table" else "pattu_vaangunar_table"
        val file = resolveActiveDatabase(dbName)

        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                val sql = "DELETE FROM $tableName WHERE id = ?"
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setLong(1, id)
                    return stmt.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            println("Desktop error permanently deleting merchant $id: ${e.message}")
            return false
        }
    }

    override fun purgeExpiredMerchants(mode: AppMode, days: Int): Int {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_vaangunar_table" else "pattu_vaangunar_table"
        val file = resolveActiveDatabase(dbName)

        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                val cutoffSec = (System.currentTimeMillis() / 1000) - (days * 86400L)
                val sql = "DELETE FROM $tableName WHERE is_deleted = 1 AND deleted_at < ?"
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setLong(1, cutoffSec)
                    return stmt.executeUpdate()
                }
            }
        } catch (e: Exception) {
            println("Desktop error purging expired merchants: ${e.message}")
            return 0
        }
    }

    override fun loadAllItems(mode: AppMode): List<PorulTharavuru> {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_porul_table" else "pattu_porul_table"
        val file = resolveActiveDatabase(dbName)

        val list = mutableListOf<PorulTharavuru>()
        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                val sql = "SELECT * FROM $tableName WHERE is_deleted = 0 ORDER BY updated_at DESC, id DESC"
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sql).use { rs ->
                        while (rs.next()) {
                            list.add(rsToItem(rs))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Desktop error loading items from $tableName: ${e.message}")
        }
        return list
    }

    override fun saveItem(mode: AppMode, item: PorulTharavuru): Long {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_porul_table" else "pattu_porul_table"
        val file = resolveActiveDatabase(dbName)

        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                val nowSec = System.currentTimeMillis() / 1000
                val isUpdate = item.id > 0L

                val sql = if (isUpdate) {
                    """
                    UPDATE $tableName SET
                        porul_peyar = ?, hsn_code = ?, vilai = ?, vari_veetham = ?, alavu_vagai = ?, alagu = ?,
                        updated_at = ?, is_deleted = ?, deleted_at = ?
                    WHERE id = ?
                    """.trimIndent()
                } else {
                    """
                    INSERT INTO $tableName (
                        porul_peyar, hsn_code, vilai, vari_veetham, alavu_vagai, alagu,
                        created_at, updated_at, is_deleted, deleted_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """.trimIndent()
                }

                conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { stmt ->
                    var idx = 1
                    stmt.setString(idx++, MozhiJsonConverter.stringify(item.porulPeyar))
                    stmt.setString(idx++, item.hsnCode)
                    stmt.setDouble(idx++, item.vilai)
                    stmt.setDouble(idx++, item.variVeetham)
                    stmt.setString(idx++, item.alavuVagai)
                    stmt.setString(idx++, item.alagu)

                    if (!isUpdate) {
                        val createdSec = if (item.createdAt > 10000000000L) item.createdAt / 1000 else if (item.createdAt > 0L) item.createdAt else nowSec
                        stmt.setLong(idx++, createdSec)
                    }

                    stmt.setLong(idx++, nowSec)
                    stmt.setInt(idx++, if (item.isDeleted) 1 else 0)

                    if (item.deletedAt != null) {
                        val delSec = if (item.deletedAt > 10000000000L) item.deletedAt / 1000 else item.deletedAt
                        stmt.setLong(idx++, delSec)
                    } else {
                        stmt.setNull(idx++, Types.INTEGER)
                    }

                    if (isUpdate) {
                        stmt.setLong(idx++, item.id)
                    }

                    val affected = stmt.executeUpdate()
                    if (isUpdate) {
                        return if (affected > 0) item.id else -1L
                    } else {
                        stmt.generatedKeys.use { gks ->
                            if (gks.next()) {
                                return gks.getLong(1)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Desktop error saving item to $tableName: ${e.message}")
        }
        return -1L
    }

    override fun deleteItem(mode: AppMode, id: Long): Boolean {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_porul_table" else "pattu_porul_table"
        val file = resolveActiveDatabase(dbName)

        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                val sql = "UPDATE $tableName SET is_deleted = 1, deleted_at = ?, updated_at = ? WHERE id = ?"
                conn.prepareStatement(sql).use { stmt ->
                    val nowSec = System.currentTimeMillis() / 1000
                    stmt.setLong(1, nowSec)
                    stmt.setLong(2, nowSec)
                    stmt.setLong(3, id)
                    val updated = stmt.executeUpdate()
                    return updated > 0
                }
            }
        } catch (e: Exception) {
            println("Desktop error deleting item $id from $tableName: ${e.message}")
            return false
        }
    }

    override fun loadDeletedItems(mode: AppMode): List<PorulTharavuru> {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_porul_table" else "pattu_porul_table"
        val file = resolveActiveDatabase(dbName)

        val list = mutableListOf<PorulTharavuru>()
        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                val sql = "SELECT * FROM $tableName WHERE is_deleted = 1 ORDER BY deleted_at DESC, id DESC"
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sql).use { rs ->
                        while (rs.next()) {
                            list.add(rsToItem(rs))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Desktop error loading deleted items from $tableName: ${e.message}")
        }
        return list
    }

    override fun restoreItem(mode: AppMode, id: Long): Boolean {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_porul_table" else "pattu_porul_table"
        val file = resolveActiveDatabase(dbName)

        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                val sql = "UPDATE $tableName SET is_deleted = 0, deleted_at = NULL, updated_at = ? WHERE id = ?"
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setLong(1, System.currentTimeMillis() / 1000)
                    stmt.setLong(2, id)
                    return stmt.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            println("Desktop error restoring item $id: ${e.message}")
            return false
        }
    }

    override fun permanentDeleteItem(mode: AppMode, id: Long): Boolean {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_porul_table" else "pattu_porul_table"
        val file = resolveActiveDatabase(dbName)

        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                val sql = "DELETE FROM $tableName WHERE id = ?"
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setLong(1, id)
                    return stmt.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            println("Desktop error permanently deleting item $id: ${e.message}")
            return false
        }
    }

    override fun purgeExpiredItems(mode: AppMode, days: Int): Int {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_porul_table" else "pattu_porul_table"
        val file = resolveActiveDatabase(dbName)

        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                val cutoffSec = (System.currentTimeMillis() / 1000) - (days * 86400L)
                val sql = "DELETE FROM $tableName WHERE is_deleted = 1 AND deleted_at < ?"
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setLong(1, cutoffSec)
                    return stmt.executeUpdate()
                }
            }
        } catch (e: Exception) {
            println("Desktop error purging expired items: ${e.message}")
            return 0
        }
    }

    override fun loadAllInvoices(mode: AppMode): List<PattiyalTharavuru> {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_pattiyal_table" else "pattu_pattiyal_table"
        val file = resolveActiveDatabase(dbName)

        val list = mutableListOf<PattiyalTharavuru>()
        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                val sql = "SELECT * FROM $tableName WHERE is_deleted = 0 ORDER BY pattiyal_naal DESC, id DESC"
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sql).use { rs ->
                        while (rs.next()) {
                            list.add(rsToInvoice(rs))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Desktop error loading invoices from $tableName: ${e.message}")
        }
        return list
    }

    override fun saveInvoice(mode: AppMode, invoice: PattiyalTharavuru): Long {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_pattiyal_table" else "pattu_pattiyal_table"
        val file = resolveActiveDatabase(dbName)

        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                val nowSec = System.currentTimeMillis() / 1000
                val naalSec = if (invoice.pattiyalNaal > 10000000000L) invoice.pattiyalNaal / 1000 else invoice.pattiyalNaal
                val isUpdate = invoice.id > 0L

                val sql = if (mode == AppMode.KOOLI) {
                    if (isUpdate) {
                        """
                        UPDATE $tableName SET
                            niruvanam_id = ?, patrucheettu_en = ?, fin_year = ?, vanakkam = ?, pattiyal_vagai = ?,
                            vaangunar_id = ?, vaangunar_peyar = ?, vaangunar_munvari = ?, pattiyal_naal = ?,
                            tharavugal = ?, motha_thogai = ?, thallupadi = ?, podhu_thallupadi_mathippu = ?,
                            podhu_thallupadi_vagai = ?, podhu_thallupadi_thogai = ?, vari_thogai = ?, vari_tharavugal = ?,
                            motha_edai = ?, setharam_grams = ?, thabaal_thogai = ?, ahimsa_pattu_thogai = ?,
                            piravari_vugal = ?, sontha_viruppangal = ?, nibandhanaigal = ?, ullkurippu = ?,
                            vangi_tharavugal = ?, updated_at = ?, is_deleted = ?, deleted_at = ?
                        WHERE id = ?
                        """.trimIndent()
                    } else {
                        """
                        INSERT INTO $tableName (
                            niruvanam_id, patrucheettu_en, fin_year, vanakkam, pattiyal_vagai,
                            vaangunar_id, vaangunar_peyar, vaangunar_munvari, pattiyal_naal,
                            tharavugal, motha_thogai, thallupadi, podhu_thallupadi_mathippu,
                            podhu_thallupadi_vagai, podhu_thallupadi_thogai, vari_thogai, vari_tharavugal,
                            motha_edai, setharam_grams, thabaal_thogai, ahimsa_pattu_thogai,
                            piravari_vugal, sontha_viruppangal, nibandhanaigal, ullkurippu,
                            vangi_tharavugal, created_at, updated_at, is_deleted, deleted_at
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """.trimIndent()
                    }
                } else {
                    if (isUpdate) {
                        """
                        UPDATE $tableName SET
                            niruvanam_id = ?, patrucheettu_en = ?, fin_year = ?, vanakkam = ?, pattiyal_vagai = ?,
                            vaangunar_id = ?, vaangunar_peyar = ?, vaangunar_munvari = ?, pattiyal_naal = ?,
                            tharavugal = ?, motha_thogai = ?, thallupadi = ?, podhu_thallupadi_mathippu = ?,
                            podhu_thallupadi_vagai = ?, podhu_thallupadi_thogai = ?, vari_thogai = ?, vari_tharavugal = ?,
                            sontha_viruppangal = ?, nibandhanaigal = ?, ullkurippu = ?,
                            updated_at = ?, is_deleted = ?, deleted_at = ?
                        WHERE id = ?
                        """.trimIndent()
                    } else {
                        """
                        INSERT INTO $tableName (
                            niruvanam_id, patrucheettu_en, fin_year, vanakkam, pattiyal_vagai,
                            vaangunar_id, vaangunar_peyar, vaangunar_munvari, pattiyal_naal,
                            tharavugal, motha_thogai, thallupadi, podhu_thallupadi_mathippu,
                            podhu_thallupadi_vagai, podhu_thallupadi_thogai, vari_thogai, vari_tharavugal,
                            sontha_viruppangal, nibandhanaigal, ullkurippu,
                            created_at, updated_at, is_deleted, deleted_at
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """.trimIndent()
                    }
                }

                conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { stmt ->
                    var idx = 1
                    if (invoice.niruvanamId != null) stmt.setLong(idx++, invoice.niruvanamId) else stmt.setNull(idx++, Types.INTEGER)
                    stmt.setString(idx++, invoice.patrucheettuEn)
                    stmt.setInt(idx++, invoice.finYear)
                    stmt.setInt(idx++, invoice.vanakkam)
                    stmt.setString(idx++, invoice.pattiyalVagai)
                    if (invoice.vaangunarId != null) stmt.setLong(idx++, invoice.vaangunarId) else stmt.setNull(idx++, Types.INTEGER)
                    stmt.setString(idx++, MozhiJsonConverter.stringify(invoice.vaangunarPeyar))
                    stmt.setString(idx++, MozhiJsonConverter.stringify(invoice.vaangunarMunvari))
                    stmt.setLong(idx++, naalSec)
                    stmt.setString(idx++, invoice.tharavugal)
                    stmt.setDouble(idx++, invoice.mothaThogai)
                    stmt.setDouble(idx++, invoice.thallupadi)
                    stmt.setDouble(idx++, invoice.podhuThallupadiMathippu)
                    stmt.setString(idx++, invoice.podhuThallupadiVagai)
                    stmt.setDouble(idx++, invoice.podhuThallupadiThogai)
                    stmt.setDouble(idx++, invoice.variThogai)
                    stmt.setString(idx++, invoice.variTharavugal)
                    if (mode == AppMode.KOOLI) {
                        stmt.setDouble(idx++, invoice.mothaEdai)
                        stmt.setDouble(idx++, invoice.setharamGrams)
                        stmt.setDouble(idx++, invoice.thabaalThogai)
                        stmt.setDouble(idx++, invoice.ahimsaPattuThogai)
                        stmt.setString(idx++, invoice.piravariVugal)
                    }
                    stmt.setString(idx++, invoice.sonthaViruppangal)
                    stmt.setString(idx++, invoice.nibandhanaigal)
                    stmt.setString(idx++, invoice.ullkurippu)
                    if (mode == AppMode.KOOLI) {
                        stmt.setString(idx++, invoice.vangiTharavugal)
                    }

                    if (!isUpdate) {
                        val createdSec = if (invoice.createdAt > 10000000000L) invoice.createdAt / 1000 else if (invoice.createdAt > 0L) invoice.createdAt else nowSec
                        stmt.setLong(idx++, createdSec)
                    }

                    stmt.setLong(idx++, nowSec)
                    stmt.setInt(idx++, if (invoice.isDeleted) 1 else 0)

                    if (invoice.deletedAt != null) {
                        val delSec = if (invoice.deletedAt > 10000000000L) invoice.deletedAt / 1000 else invoice.deletedAt
                        stmt.setLong(idx++, delSec)
                    } else {
                        stmt.setNull(idx++, Types.INTEGER)
                    }

                    if (isUpdate) {
                        stmt.setLong(idx++, invoice.id)
                    }

                    val affected = stmt.executeUpdate()
                    if (isUpdate) {
                        return if (affected > 0) invoice.id else -1L
                    } else {
                        stmt.generatedKeys.use { gks ->
                            if (gks.next()) {
                                return gks.getLong(1)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Desktop error saving invoice to $tableName: ${e.message}")
        }
        return -1L
    }

    override fun deleteInvoice(mode: AppMode, id: Long): Boolean {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_pattiyal_table" else "pattu_pattiyal_table"
        val file = resolveActiveDatabase(dbName)

        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                val sql = "UPDATE $tableName SET is_deleted = 1, deleted_at = ?, updated_at = ? WHERE id = ?"
                conn.prepareStatement(sql).use { stmt ->
                    val nowSec = System.currentTimeMillis() / 1000
                    stmt.setLong(1, nowSec)
                    stmt.setLong(2, nowSec)
                    stmt.setLong(3, id)
                    val updated = stmt.executeUpdate()
                    return updated > 0
                }
            }
        } catch (e: Exception) {
            println("Desktop error deleting invoice $id from $tableName: ${e.message}")
            return false
        }
    }

    override fun loadDeletedInvoices(mode: AppMode): List<PattiyalTharavuru> {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_pattiyal_table" else "pattu_pattiyal_table"
        val file = resolveActiveDatabase(dbName)

        val list = mutableListOf<PattiyalTharavuru>()
        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                val sql = "SELECT * FROM $tableName WHERE is_deleted = 1 ORDER BY deleted_at DESC, id DESC"
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sql).use { rs ->
                        while (rs.next()) {
                            list.add(rsToInvoice(rs))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Desktop error loading deleted invoices from $tableName: ${e.message}")
        }
        return list
    }

    override fun restoreInvoice(mode: AppMode, id: Long): Boolean {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_pattiyal_table" else "pattu_pattiyal_table"
        val file = resolveActiveDatabase(dbName)

        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                val sql = "UPDATE $tableName SET is_deleted = 0, deleted_at = NULL, updated_at = ? WHERE id = ?"
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setLong(1, System.currentTimeMillis() / 1000)
                    stmt.setLong(2, id)
                    return stmt.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            println("Desktop error restoring invoice $id: ${e.message}")
            return false
        }
    }

    override fun permanentDeleteInvoice(mode: AppMode, id: Long): Boolean {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_pattiyal_table" else "pattu_pattiyal_table"
        val junctionTable = if (mode == AppMode.KOOLI) "kooli_patru_pattiyal_table" else "pattu_patru_pattiyal_table"
        val file = resolveActiveDatabase(dbName)

        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                conn.autoCommit = false
                try {
                    conn.prepareStatement("DELETE FROM $junctionTable WHERE pattiyal_id = ?").use { stmt ->
                        stmt.setLong(1, id)
                        stmt.executeUpdate()
                    }
                    val affected = conn.prepareStatement("DELETE FROM $tableName WHERE id = ?").use { stmt ->
                        stmt.setLong(1, id)
                        stmt.executeUpdate()
                    }
                    conn.commit()
                    return affected > 0
                } catch (ex: Exception) {
                    conn.rollback()
                    throw ex
                } finally {
                    conn.autoCommit = true
                }
            }
        } catch (e: Exception) {
            println("Desktop error permanently deleting invoice $id: ${e.message}")
            return false
        }
    }

    override fun purgeExpiredInvoices(mode: AppMode, days: Int): Int {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_pattiyal_table" else "pattu_pattiyal_table"
        val junctionTable = if (mode == AppMode.KOOLI) "kooli_patru_pattiyal_table" else "pattu_patru_pattiyal_table"
        val file = resolveActiveDatabase(dbName)

        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                val cutoffSec = (System.currentTimeMillis() / 1000) - (days * 86400L)
                conn.autoCommit = false
                try {
                    conn.prepareStatement("DELETE FROM $junctionTable WHERE pattiyal_id IN (SELECT id FROM $tableName WHERE is_deleted = 1 AND deleted_at < ?)").use { stmt ->
                        stmt.setLong(1, cutoffSec)
                        stmt.executeUpdate()
                    }
                    val deleted = conn.prepareStatement("DELETE FROM $tableName WHERE is_deleted = 1 AND deleted_at < ?").use { stmt ->
                        stmt.setLong(1, cutoffSec)
                        stmt.executeUpdate()
                    }
                    conn.commit()
                    return deleted
                } catch (ex: Exception) {
                    conn.rollback()
                    throw ex
                } finally {
                    conn.autoCommit = true
                }
            }
        } catch (e: Exception) {
            println("Desktop error purging expired invoices: ${e.message}")
            return 0
        }
    }

    override fun loadAllReceipts(mode: AppMode): List<PatrugalTharavuru> {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_patrugal_table" else "pattu_patrugal_table"
        val file = resolveActiveDatabase(dbName)

        val list = mutableListOf<PatrugalTharavuru>()
        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                val sql = "SELECT * FROM $tableName WHERE is_deleted = 0 ORDER BY patru_naal DESC, id DESC"
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sql).use { rs ->
                        while (rs.next()) {
                            list.add(rsToReceipt(rs))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Desktop error loading receipts from $tableName: ${e.message}")
        }
        return list
    }

    override fun saveReceipt(mode: AppMode, receipt: PatrugalTharavuru): Long {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_patrugal_table" else "pattu_patrugal_table"
        val file = resolveActiveDatabase(dbName)

        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                val nowSec = System.currentTimeMillis() / 1000
                val naalSec = if (receipt.patruNaal > 10000000000L) receipt.patruNaal / 1000 else receipt.patruNaal
                val isUpdate = receipt.id > 0L

                val sql = if (isUpdate) {
                    """
                    UPDATE $tableName SET
                        niruvanam_id = ?, patru_en = ?, fin_year = ?, vanakkam = ?,
                        vaangunar_id = ?, vaangunar_peyar = ?, vaangunar_munvari = ?, patru_naal = ?,
                        thogai = ?, seluthum_murai = ?, vangi_peyar = ?, parivarthanai_en = ?, ullkurippu = ?,
                        updated_at = ?, is_deleted = ?, deleted_at = ?
                    WHERE id = ?
                    """.trimIndent()
                } else {
                    """
                    INSERT INTO $tableName (
                        niruvanam_id, patru_en, fin_year, vanakkam,
                        vaangunar_id, vaangunar_peyar, vaangunar_munvari, patru_naal,
                        thogai, seluthum_murai, vangi_peyar, parivarthanai_en, ullkurippu,
                        created_at, updated_at, is_deleted, deleted_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """.trimIndent()
                }

                conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { stmt ->
                    var idx = 1
                    if (receipt.niruvanamId != null) stmt.setLong(idx++, receipt.niruvanamId) else stmt.setNull(idx++, Types.INTEGER)
                    stmt.setString(idx++, receipt.patruEn)
                    stmt.setString(idx++, receipt.finYear)
                    stmt.setInt(idx++, receipt.vanakkam)
                    if (receipt.vaangunarId != null) stmt.setLong(idx++, receipt.vaangunarId) else stmt.setNull(idx++, Types.INTEGER)
                    stmt.setString(idx++, MozhiJsonConverter.stringify(receipt.vaangunarPeyar))
                    stmt.setString(idx++, MozhiJsonConverter.stringify(receipt.vaangunarMunvari))
                    stmt.setLong(idx++, naalSec)
                    stmt.setDouble(idx++, receipt.thogai)
                    stmt.setString(idx++, receipt.seluthumMurai)
                    if (receipt.vangiPeyar != null) stmt.setString(idx++, receipt.vangiPeyar) else stmt.setNull(idx++, Types.VARCHAR)
                    if (receipt.parivarthanaiEn != null) stmt.setString(idx++, receipt.parivarthanaiEn) else stmt.setNull(idx++, Types.VARCHAR)
                    stmt.setString(idx++, receipt.ullkurippu)

                    if (!isUpdate) {
                        val createdSec = if (receipt.createdAt > 10000000000L) receipt.createdAt / 1000 else if (receipt.createdAt > 0L) receipt.createdAt else nowSec
                        stmt.setLong(idx++, createdSec)
                    }

                    stmt.setLong(idx++, nowSec)
                    stmt.setInt(idx++, if (receipt.isDeleted) 1 else 0)

                    if (receipt.deletedAt != null) {
                        val delSec = if (receipt.deletedAt > 10000000000L) receipt.deletedAt / 1000 else receipt.deletedAt
                        stmt.setLong(idx++, delSec)
                    } else {
                        stmt.setNull(idx++, Types.INTEGER)
                    }

                    if (isUpdate) {
                        stmt.setLong(idx++, receipt.id)
                    }

                    val affected = stmt.executeUpdate()
                    if (isUpdate) {
                        return if (affected > 0) receipt.id else -1L
                    } else {
                        stmt.generatedKeys.use { gks ->
                            if (gks.next()) {
                                return gks.getLong(1)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Desktop error saving receipt to $tableName: ${e.message}")
        }
        return -1L
    }

    override fun deleteReceipt(mode: AppMode, id: Long): Boolean {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_patrugal_table" else "pattu_patrugal_table"
        val file = resolveActiveDatabase(dbName)

        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                val sql = "UPDATE $tableName SET is_deleted = 1, deleted_at = ?, updated_at = ? WHERE id = ?"
                conn.prepareStatement(sql).use { stmt ->
                    val nowSec = System.currentTimeMillis() / 1000
                    stmt.setLong(1, nowSec)
                    stmt.setLong(2, nowSec)
                    stmt.setLong(3, id)
                    val updated = stmt.executeUpdate()
                    return updated > 0
                }
            }
        } catch (e: Exception) {
            println("Desktop error deleting receipt $id from $tableName: ${e.message}")
            return false
        }
    }

    override fun loadDeletedReceipts(mode: AppMode): List<PatrugalTharavuru> {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_patrugal_table" else "pattu_patrugal_table"
        val file = resolveActiveDatabase(dbName)

        val list = mutableListOf<PatrugalTharavuru>()
        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                val sql = "SELECT * FROM $tableName WHERE is_deleted = 1 ORDER BY deleted_at DESC, id DESC"
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sql).use { rs ->
                        while (rs.next()) {
                            list.add(rsToReceipt(rs))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Desktop error loading deleted receipts from $tableName: ${e.message}")
        }
        return list
    }

    override fun restoreReceipt(mode: AppMode, id: Long): Boolean {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_patrugal_table" else "pattu_patrugal_table"
        val file = resolveActiveDatabase(dbName)

        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                val sql = "UPDATE $tableName SET is_deleted = 0, deleted_at = NULL, updated_at = ? WHERE id = ?"
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setLong(1, System.currentTimeMillis() / 1000)
                    stmt.setLong(2, id)
                    return stmt.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            println("Desktop error restoring receipt $id: ${e.message}")
            return false
        }
    }

    override fun permanentDeleteReceipt(mode: AppMode, id: Long): Boolean {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_patrugal_table" else "pattu_patrugal_table"
        val junctionTable = if (mode == AppMode.KOOLI) "kooli_patru_pattiyal_table" else "pattu_patru_pattiyal_table"
        val file = resolveActiveDatabase(dbName)

        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                conn.autoCommit = false
                try {
                    conn.prepareStatement("DELETE FROM $junctionTable WHERE patru_id = ?").use { stmt ->
                        stmt.setLong(1, id)
                        stmt.executeUpdate()
                    }
                    val affected = conn.prepareStatement("DELETE FROM $tableName WHERE id = ?").use { stmt ->
                        stmt.setLong(1, id)
                        stmt.executeUpdate()
                    }
                    conn.commit()
                    return affected > 0
                } catch (ex: Exception) {
                    conn.rollback()
                    throw ex
                } finally {
                    conn.autoCommit = true
                }
            }
        } catch (e: Exception) {
            println("Desktop error permanently deleting receipt $id: ${e.message}")
            return false
        }
    }

    override fun purgeExpiredReceipts(mode: AppMode, days: Int): Int {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val tableName = if (mode == AppMode.KOOLI) "kooli_patrugal_table" else "pattu_patrugal_table"
        val junctionTable = if (mode == AppMode.KOOLI) "kooli_patru_pattiyal_table" else "pattu_patru_pattiyal_table"
        val file = resolveActiveDatabase(dbName)

        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                val cutoffSec = (System.currentTimeMillis() / 1000) - (days * 86400L)
                conn.autoCommit = false
                try {
                    conn.prepareStatement("DELETE FROM $junctionTable WHERE patru_id IN (SELECT id FROM $tableName WHERE is_deleted = 1 AND deleted_at < ?)").use { stmt ->
                        stmt.setLong(1, cutoffSec)
                        stmt.executeUpdate()
                    }
                    val deleted = conn.prepareStatement("DELETE FROM $tableName WHERE is_deleted = 1 AND deleted_at < ?").use { stmt ->
                        stmt.setLong(1, cutoffSec)
                        stmt.executeUpdate()
                    }
                    conn.commit()
                    return deleted
                } catch (ex: Exception) {
                    conn.rollback()
                    throw ex
                } finally {
                    conn.autoCommit = true
                }
            }
        } catch (e: Exception) {
            println("Desktop error purging expired receipts: ${e.message}")
            return 0
        }
    }

    override fun getLinksForPatru(mode: AppMode, patruId: Long): List<PatruPattiyalInaippuTharavuru> {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val junctionTable = if (mode == AppMode.KOOLI) "kooli_patru_pattiyal_table" else "pattu_patru_pattiyal_table"
        val file = resolveActiveDatabase(dbName)

        val list = mutableListOf<PatruPattiyalInaippuTharavuru>()
        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                val sql = "SELECT * FROM $junctionTable WHERE patru_id = ? ORDER BY id ASC"
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setLong(1, patruId)
                    stmt.executeQuery().use { rs ->
                        while (rs.next()) {
                            list.add(
                                PatruPattiyalInaippuTharavuru(
                                    id = rs.getLong("id"),
                                    patruId = rs.getLong("patru_id"),
                                    pattiyalId = rs.getLong("pattiyal_id"),
                                    poruthiyaThogai = rs.getDouble("poruthiya_thogai")
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Desktop error getting links for receipt $patruId: ${e.message}")
        }
        return list
    }

    override fun saveReceiptWithLinks(mode: AppMode, receipt: PatrugalTharavuru, links: List<PatruPattiyalInaippuTharavuru>): Long {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val junctionTable = if (mode == AppMode.KOOLI) "kooli_patru_pattiyal_table" else "pattu_patru_pattiyal_table"
        val file = resolveActiveDatabase(dbName)

        val receiptId = saveReceipt(mode, receipt)
        if (receiptId <= 0L) return -1L

        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                conn.autoCommit = false
                try {
                    // Delete old links for this receipt
                    conn.prepareStatement("DELETE FROM $junctionTable WHERE patru_id = ?").use { delStmt ->
                        delStmt.setLong(1, receiptId)
                        delStmt.executeUpdate()
                    }

                    // Insert fresh links
                    if (links.isNotEmpty()) {
                        val insSql = "INSERT INTO $junctionTable (patru_id, pattiyal_id, poruthiya_thogai) VALUES (?, ?, ?)"
                        conn.prepareStatement(insSql).use { insStmt ->
                            for (link in links) {
                                insStmt.setLong(1, receiptId)
                                insStmt.setLong(2, link.pattiyalId)
                                insStmt.setDouble(3, link.poruthiyaThogai)
                                insStmt.executeUpdate()
                            }
                        }
                    }
                    conn.commit()
                } catch (ex: Exception) {
                    conn.rollback()
                    throw ex
                } finally {
                    conn.autoCommit = true
                }
            }
        } catch (e: Exception) {
            println("Desktop error saving links for receipt $receiptId: ${e.message}")
        }

        return receiptId
    }

    override fun getPaidAmountForInvoice(mode: AppMode, invoiceId: Long): Double {
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val patrugalTable = if (mode == AppMode.KOOLI) "kooli_patrugal_table" else "pattu_patrugal_table"
        val junctionTable = if (mode == AppMode.KOOLI) "kooli_patru_pattiyal_table" else "pattu_patru_pattiyal_table"
        val file = resolveActiveDatabase(dbName)

        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                val sql = """
                    SELECT SUM(j.poruthiya_thogai) FROM $junctionTable j
                    JOIN $patrugalTable p ON j.patru_id = p.id
                    WHERE j.pattiyal_id = ? AND p.is_deleted = 0
                """.trimIndent()
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setLong(1, invoiceId)
                    stmt.executeQuery().use { rs ->
                        if (rs.next()) {
                            return rs.getDouble(1)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Desktop error getting paid amount for invoice $invoiceId: ${e.message}")
        }
        return 0.0
    }

    override fun getPaidAmountsForInvoices(mode: AppMode, invoiceIds: List<Long>): Map<Long, Double> {
        if (invoiceIds.isEmpty()) return emptyMap()
        val dbName = if (mode == AppMode.KOOLI) coolieDbName else silkDbName
        val patrugalTable = if (mode == AppMode.KOOLI) "kooli_patrugal_table" else "pattu_patrugal_table"
        val junctionTable = if (mode == AppMode.KOOLI) "kooli_patru_pattiyal_table" else "pattu_patru_pattiyal_table"
        val file = resolveActiveDatabase(dbName)

        val map = mutableMapOf<Long, Double>()
        try {
            getConnection(file).use { conn ->
                ensureTables(conn, mode)
                val inClause = invoiceIds.joinToString(",")
                val sql = """
                    SELECT j.pattiyal_id, SUM(j.poruthiya_thogai) as total_paid
                    FROM $junctionTable j
                    JOIN $patrugalTable p ON j.patru_id = p.id
                    WHERE j.pattiyal_id IN ($inClause) AND p.is_deleted = 0
                    GROUP BY j.pattiyal_id
                """.trimIndent()
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sql).use { rs ->
                        while (rs.next()) {
                            map[rs.getLong("pattiyal_id")] = rs.getDouble("total_paid")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Desktop error getting paid amounts for invoices: ${e.message}")
        }
        return map
    }

    private fun rsToMerchant(rs: ResultSet): VaangunarTharavuru {
        fun getString(col: String): String {
            return try { rs.getString(col) ?: "" } catch (_: Exception) { "" }
        }
        fun getLong(col: String, defaultVal: Long = 0L): Long {
            return try { val v = rs.getLong(col); if (rs.wasNull()) defaultVal else v } catch (_: Exception) { defaultVal }
        }
        fun getNullableLong(col: String): Long? {
            return try { val v = rs.getLong(col); if (rs.wasNull()) null else v } catch (_: Exception) { null }
        }
        fun getInt(col: String, defaultVal: Int = 0): Int {
            return try { val v = rs.getInt(col); if (rs.wasNull()) defaultVal else v } catch (_: Exception) { defaultVal }
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

    private fun rsToItem(rs: ResultSet): PorulTharavuru {
        fun getString(col: String): String {
            return try { rs.getString(col) ?: "" } catch (_: Exception) { "" }
        }
        fun getLong(col: String, defaultVal: Long = 0L): Long {
            return try { val v = rs.getLong(col); if (rs.wasNull()) defaultVal else v } catch (_: Exception) { defaultVal }
        }
        fun getNullableLong(col: String): Long? {
            return try { val v = rs.getLong(col); if (rs.wasNull()) null else v } catch (_: Exception) { null }
        }
        fun getInt(col: String, defaultVal: Int = 0): Int {
            return try { val v = rs.getInt(col); if (rs.wasNull()) defaultVal else v } catch (_: Exception) { defaultVal }
        }
        fun getDouble(col: String, defaultVal: Double = 0.0): Double {
            return try { val v = rs.getDouble(col); if (rs.wasNull()) defaultVal else v } catch (_: Exception) { defaultVal }
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

    private fun rsToInvoice(rs: ResultSet): PattiyalTharavuru {
        fun getString(col: String): String {
            return try { rs.getString(col) ?: "" } catch (_: Exception) { "" }
        }
        fun getLong(col: String, defaultVal: Long = 0L): Long {
            return try { val v = rs.getLong(col); if (rs.wasNull()) defaultVal else v } catch (_: Exception) { defaultVal }
        }
        fun getNullableLong(col: String): Long? {
            return try { val v = rs.getLong(col); if (rs.wasNull()) null else v } catch (_: Exception) { null }
        }
        fun getInt(col: String, defaultVal: Int = 0): Int {
            return try { val v = rs.getInt(col); if (rs.wasNull()) defaultVal else v } catch (_: Exception) { defaultVal }
        }
        fun getDouble(col: String, defaultVal: Double = 0.0): Double {
            return try { val v = rs.getDouble(col); if (rs.wasNull()) defaultVal else v } catch (_: Exception) { defaultVal }
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

    private fun rsToReceipt(rs: ResultSet): PatrugalTharavuru {
        fun getString(col: String): String {
            return try { rs.getString(col) ?: "" } catch (_: Exception) { "" }
        }
        fun getNullableString(col: String): String? {
            return try { rs.getString(col) } catch (_: Exception) { null }
        }
        fun getLong(col: String, defaultVal: Long = 0L): Long {
            return try { val v = rs.getLong(col); if (rs.wasNull()) defaultVal else v } catch (_: Exception) { defaultVal }
        }
        fun getNullableLong(col: String): Long? {
            return try { val v = rs.getLong(col); if (rs.wasNull()) null else v } catch (_: Exception) { null }
        }
        fun getInt(col: String, defaultVal: Int = 0): Int {
            return try { val v = rs.getInt(col); if (rs.wasNull()) defaultVal else v } catch (_: Exception) { defaultVal }
        }
        fun getDouble(col: String, defaultVal: Double = 0.0): Double {
            return try { val v = rs.getDouble(col); if (rs.wasNull()) defaultVal else v } catch (_: Exception) { defaultVal }
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
        val dbFile = resolveActiveDatabase(if (mode == AppMode.KOOLI) coolieDbName else silkDbName)
        val vaangunarTable = if (mode == AppMode.KOOLI) "kooli_vaangunar_table" else "pattu_vaangunar_table"
        val porulTable = if (mode == AppMode.KOOLI) "kooli_porul_table" else "pattu_porul_table"
        val pattiyalTable = if (mode == AppMode.KOOLI) "kooli_pattiyal_table" else "pattu_pattiyal_table"
        val patrugalTable = if (mode == AppMode.KOOLI) "kooli_patrugal_table" else "pattu_patrugal_table"
        val junctionTable = if (mode == AppMode.KOOLI) "kooli_patru_pattiyal_table" else "pattu_patru_pattiyal_table"

        return try {
            val conn = getConnection(dbFile)
            conn.use { c ->
                ensureTables(c, mode)
                c.createStatement().use { stmt ->
                    stmt.executeUpdate("DELETE FROM $junctionTable")
                    stmt.executeUpdate("DELETE FROM $patrugalTable")
                    stmt.executeUpdate("DELETE FROM $pattiyalTable")
                    stmt.executeUpdate("DELETE FROM $porulTable")
                    stmt.executeUpdate("DELETE FROM $vaangunarTable")
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}

private val desktopBusinessHelperInstance by lazy { DesktopBusinessDatabaseHelper() }

actual fun getBusinessDatabaseHelper(): BusinessDatabaseHelper = desktopBusinessHelperInstance
