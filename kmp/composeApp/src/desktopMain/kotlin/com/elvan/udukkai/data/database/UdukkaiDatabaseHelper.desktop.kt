package com.elvan.udukkai.data.database

import com.elvan.udukkai.data.DesktopDbPaths
import com.elvan.udukkai.data.model.PattiyalTharavuru
import com.elvan.udukkai.data.model.PatruPattiyalInaippuTharavuru
import com.elvan.udukkai.data.model.PatrugalTharavuru
import com.elvan.udukkai.data.model.PorulJsonConverter
import com.elvan.udukkai.data.model.PorulTharavuru
import com.elvan.udukkai.data.model.VaangunarTharavuru
import com.elvan.udukkai.data.settings.MozhiJsonConverter
import com.elvan.udukkai.data.settings.NiruvanaTharavugal
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Types

class DesktopUdukkaiDatabaseHelper(
    val dbName: String,
    val tablePrefix: String,
    val isFactory: Boolean
) : UdukkaiDatabaseHelper {

    private val vaangunarTable = "${tablePrefix}vaangunar_table"
    private val porulTable = "${tablePrefix}porul_table"
    private val pattiyalTable = "${tablePrefix}pattiyal_table"
    private val patrugalTable = "${tablePrefix}patrugal_table"
    private val junctionTable = "${tablePrefix}patru_pattiyal_table"
    private val profileTable = "${tablePrefix}niruvana_tharavugal_table"

    private fun resolveActiveDatabase(): File {
        val target = DesktopDbPaths.getDbFile(dbName)
        if (!target.exists()) {
            val legacyName = if (isFactory) "udukkai_kooli.db" else "udukkai_pattu.db"
            val legacyFile = DesktopDbPaths.getDbFile(legacyName)
            if (legacyFile.exists()) {
                try {
                    println("Desktop auto-migrating legacy DB $legacyName -> $dbName")
                    legacyFile.copyTo(target, overwrite = true)
                } catch (e: Exception) {
                    println("Failed to copy legacy DB $legacyName: ${e.message}")
                }
            }
        }
        return target
    }

    private fun getConnection(): Connection {
        Class.forName("org.sqlite.JDBC")
        val file = resolveActiveDatabase()
        file.parentFile?.mkdirs()
        return DriverManager.getConnection("jdbc:sqlite:${file.absolutePath}")
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

    private fun ensureTables(conn: Connection) {
        if (isFactory) {
            renameTableIfExists(conn, "kooli_vaangunar_table", vaangunarTable)
            renameTableIfExists(conn, "kooli_porul_table", porulTable)
            renameTableIfExists(conn, "kooli_pattiyal_table", pattiyalTable)
            renameTableIfExists(conn, "kooli_patrugal_table", patrugalTable)
            renameTableIfExists(conn, "kooli_patru_pattiyal_table", junctionTable)
            renameTableIfExists(conn, "kooli_niruvana_tharavugal_table", profileTable)
        } else {
            renameTableIfExists(conn, "pattu_vaangunar_table", vaangunarTable)
            renameTableIfExists(conn, "pattu_porul_table", porulTable)
            renameTableIfExists(conn, "pattu_pattiyal_table", pattiyalTable)
            renameTableIfExists(conn, "pattu_patrugal_table", patrugalTable)
            renameTableIfExists(conn, "pattu_patru_pattiyal_table", junctionTable)
            renameTableIfExists(conn, "pattu_niruvana_tharavugal_table", profileTable)
        }

        conn.createStatement().use { stmt ->
            // 1. Customers Table
            stmt.execute("""
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
            stmt.execute("""
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
            stmt.execute(createPattiyalSql)

            // 4. Receipts Table
            stmt.execute("""
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
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS "$junctionTable" (
                    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    "patru_id" INTEGER NOT NULL,
                    "pattiyal_id" INTEGER NOT NULL,
                    "inaippu_thogai" REAL NOT NULL DEFAULT 0.0,
                    "created_at" INTEGER NOT NULL DEFAULT (CAST(strftime('%s', CURRENT_TIMESTAMP) AS INTEGER))
                )
            """.trimIndent())

            // 6. Profiles Table
            stmt.execute("""
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
    }

    private inline fun <T> useConn(block: (Connection) -> T): T {
        val conn = getConnection()
        return try {
            ensureTables(conn)
            block(conn)
        } finally {
            try { conn.close() } catch (_: Exception) {}
        }
    }

    // ── Merchants / Customers ────────────────────────────────────────────────
    override fun loadAllMerchants(): List<VaangunarTharavuru> = useConn { conn ->
        val list = mutableListOf<VaangunarTharavuru>()
        conn.createStatement().use { stmt ->
            stmt.executeQuery("SELECT * FROM \"$vaangunarTable\" WHERE is_deleted = 0 ORDER BY id DESC").use { rs ->
                while (rs.next()) list.add(rsToMerchant(rs))
            }
        }
        list
    }

    override fun saveMerchant(merchant: VaangunarTharavuru): Long = useConn { conn ->
        val isUpdate = merchant.id != null && merchant.id > 0
        val sql = if (isUpdate) {
            "UPDATE \"$vaangunarTable\" SET peyar=?, mugavari=?, oor=?, maavattam=?, maanilam=?, naadu=?, velinaad_mugavari=?, anjal_kuriyeedu=?, gstin=?, minnanjal=?, tholaipaesi=?, updated_at=?, is_deleted=0 WHERE id=?"
        } else {
            "INSERT INTO \"$vaangunarTable\" (peyar, mugavari, oor, maavattam, maanilam, naadu, velinaad_mugavari, anjal_kuriyeedu, gstin, minnanjal, tholaipaesi, created_at, updated_at, is_deleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)"
        }
        conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { stmt ->
            stmt.setString(1, MozhiJsonConverter.stringify(merchant.peyar))
            stmt.setString(2, MozhiJsonConverter.stringify(merchant.mugavari))
            stmt.setString(3, MozhiJsonConverter.stringify(merchant.oor))
            stmt.setString(4, MozhiJsonConverter.stringify(merchant.maavattam))
            stmt.setString(5, MozhiJsonConverter.stringify(merchant.maanilam))
            stmt.setString(6, MozhiJsonConverter.stringify(merchant.naadu))
            stmt.setString(7, MozhiJsonConverter.stringify(merchant.velinaadMugavari))
            stmt.setString(8, merchant.anjalKuriyeedu)
            stmt.setString(9, merchant.gstin)
            stmt.setString(10, merchant.minnanjal)
            stmt.setString(11, merchant.tholaipaesi)
            val now = System.currentTimeMillis() / 1000
            if (isUpdate) {
                stmt.setLong(12, now)
                stmt.setLong(13, merchant.id!!)
                stmt.executeUpdate()
                merchant.id!!
            } else {
                stmt.setLong(12, now)
                stmt.setLong(13, now)
                stmt.executeUpdate()
                stmt.generatedKeys.use { rs -> if (rs.next()) rs.getLong(1) else -1L }
            }
        }
    }

    override fun deleteMerchant(id: Long): Boolean = useConn { conn ->
        conn.prepareStatement("UPDATE \"$vaangunarTable\" SET is_deleted = 1, deleted_at = ? WHERE id = ?").use { stmt ->
            stmt.setLong(1, System.currentTimeMillis() / 1000)
            stmt.setLong(2, id)
            stmt.executeUpdate() > 0
        }
    }

    override fun loadDeletedMerchants(): List<VaangunarTharavuru> = useConn { conn ->
        val list = mutableListOf<VaangunarTharavuru>()
        conn.createStatement().use { stmt ->
            stmt.executeQuery("SELECT * FROM \"$vaangunarTable\" WHERE is_deleted = 1 ORDER BY deleted_at DESC").use { rs ->
                while (rs.next()) list.add(rsToMerchant(rs))
            }
        }
        list
    }

    override fun restoreMerchant(id: Long): Boolean = useConn { conn ->
        conn.prepareStatement("UPDATE \"$vaangunarTable\" SET is_deleted = 0, deleted_at = NULL WHERE id = ?").use { stmt ->
            stmt.setLong(1, id)
            stmt.executeUpdate() > 0
        }
    }

    override fun permanentDeleteMerchant(id: Long): Boolean = useConn { conn ->
        conn.prepareStatement("DELETE FROM \"$vaangunarTable\" WHERE id = ?").use { stmt ->
            stmt.setLong(1, id)
            stmt.executeUpdate() > 0
        }
    }

    override fun purgeExpiredMerchants(days: Int): Int = useConn { conn ->
        val threshold = (System.currentTimeMillis() / 1000) - (days * 86400L)
        conn.prepareStatement("DELETE FROM \"$vaangunarTable\" WHERE is_deleted = 1 AND deleted_at < ?").use { stmt ->
            stmt.setLong(1, threshold)
            stmt.executeUpdate()
        }
    }

    // ── Products / Items ─────────────────────────────────────────────────────
    override fun loadAllItems(): List<PorulTharavuru> = useConn { conn ->
        val list = mutableListOf<PorulTharavuru>()
        conn.createStatement().use { stmt ->
            stmt.executeQuery("SELECT * FROM \"$porulTable\" WHERE is_deleted = 0 ORDER BY id DESC").use { rs ->
                while (rs.next()) list.add(rsToItem(rs))
            }
        }
        list
    }

    override fun saveItem(item: PorulTharavuru): Long = useConn { conn ->
        val isUpdate = item.id != null && item.id > 0
        val sql = if (isUpdate) {
            "UPDATE \"$porulTable\" SET porul_peyar=?, hsn_code=?, vilai=?, vari_veetham=?, alavu_vagai=?, alagu=?, niruvanam_ids=?, company_prices=?, updated_at=?, is_deleted=0 WHERE id=?"
        } else {
            "INSERT INTO \"$porulTable\" (porul_peyar, hsn_code, vilai, vari_veetham, alavu_vagai, alagu, niruvanam_ids, company_prices, created_at, updated_at, is_deleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)"
        }
        conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { stmt ->
            stmt.setString(1, MozhiJsonConverter.stringify(item.porulPeyar))
            stmt.setString(2, item.hsnCode)
            stmt.setDouble(3, item.vilai)
            stmt.setDouble(4, item.variVeetham)
            stmt.setString(5, item.alavuVagai)
            stmt.setString(6, item.alagu)
            stmt.setString(7, item.niruvanamIds.toString())
            stmt.setString(8, item.companyPrices.toString())
            val now = System.currentTimeMillis() / 1000
            if (isUpdate) {
                stmt.setLong(9, now)
                stmt.setLong(10, item.id!!)
                stmt.executeUpdate()
                item.id!!
            } else {
                stmt.setLong(9, now)
                stmt.setLong(10, now)
                stmt.executeUpdate()
                stmt.generatedKeys.use { rs -> if (rs.next()) rs.getLong(1) else -1L }
            }
        }
    }

    override fun deleteItem(id: Long): Boolean = useConn { conn ->
        conn.prepareStatement("UPDATE \"$porulTable\" SET is_deleted = 1, deleted_at = ? WHERE id = ?").use { stmt ->
            stmt.setLong(1, System.currentTimeMillis() / 1000)
            stmt.setLong(2, id)
            stmt.executeUpdate() > 0
        }
    }

    override fun loadDeletedItems(): List<PorulTharavuru> = useConn { conn ->
        val list = mutableListOf<PorulTharavuru>()
        conn.createStatement().use { stmt ->
            stmt.executeQuery("SELECT * FROM \"$porulTable\" WHERE is_deleted = 1 ORDER BY deleted_at DESC").use { rs ->
                while (rs.next()) list.add(rsToItem(rs))
            }
        }
        list
    }

    override fun restoreItem(id: Long): Boolean = useConn { conn ->
        conn.prepareStatement("UPDATE \"$porulTable\" SET is_deleted = 0, deleted_at = NULL WHERE id = ?").use { stmt ->
            stmt.setLong(1, id)
            stmt.executeUpdate() > 0
        }
    }

    override fun permanentDeleteItem(id: Long): Boolean = useConn { conn ->
        conn.prepareStatement("DELETE FROM \"$porulTable\" WHERE id = ?").use { stmt ->
            stmt.setLong(1, id)
            stmt.executeUpdate() > 0
        }
    }

    override fun purgeExpiredItems(days: Int): Int = useConn { conn ->
        val threshold = (System.currentTimeMillis() / 1000) - (days * 86400L)
        conn.prepareStatement("DELETE FROM \"$porulTable\" WHERE is_deleted = 1 AND deleted_at < ?").use { stmt ->
            stmt.setLong(1, threshold)
            stmt.executeUpdate()
        }
    }

    // ── Invoices ─────────────────────────────────────────────────────────────
    override fun loadAllInvoices(): List<PattiyalTharavuru> = useConn { conn ->
        val list = mutableListOf<PattiyalTharavuru>()
        conn.createStatement().use { stmt ->
            stmt.executeQuery("SELECT * FROM \"$pattiyalTable\" WHERE is_deleted = 0 ORDER BY pattiyal_naal DESC, id DESC").use { rs ->
                while (rs.next()) list.add(rsToInvoice(rs))
            }
        }
        list
    }

    override fun saveInvoice(invoice: PattiyalTharavuru): Long = useConn { conn ->
        val isUpdate = invoice.id != null && invoice.id > 0
        val sql = if (isFactory) {
            if (isUpdate) {
                "UPDATE \"$pattiyalTable\" SET niruvanam_id=?, patrucheettu_en=?, fin_year=?, vanakkam=?, pattiyal_vagai=?, vaangunar_id=?, vaangunar_peyar=?, vaangunar_munvari=?, pattiyal_naal=?, tharavugal=?, motha_thogai=?, thallupadi=?, podhu_thallupadi_mathippu=?, podhu_thallupadi_vagai=?, podhu_thallupadi_thogai=?, vari_thogai=?, vari_tharavugal=?, motha_edai=?, setharam_grams=?, thabaal_thogai=?, ahimsa_pattu_thogai=?, piravari_vugal=?, sontha_viruppangal=?, nibandhanaigal=?, ullkurippu=?, vangi_tharavugal=?, updated_at=?, is_deleted=0 WHERE id=?"
            } else {
                "INSERT INTO \"$pattiyalTable\" (niruvanam_id, patrucheettu_en, fin_year, vanakkam, pattiyal_vagai, vaangunar_id, vaangunar_peyar, vaangunar_munvari, pattiyal_naal, tharavugal, motha_thogai, thallupadi, podhu_thallupadi_mathippu, podhu_thallupadi_vagai, podhu_thallupadi_thogai, vari_thogai, vari_tharavugal, motha_edai, setharam_grams, thabaal_thogai, ahimsa_pattu_thogai, piravari_vugal, sontha_viruppangal, nibandhanaigal, ullkurippu, vangi_tharavugal, created_at, updated_at, is_deleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)"
            }
        } else {
            if (isUpdate) {
                "UPDATE \"$pattiyalTable\" SET niruvanam_id=?, patrucheettu_en=?, fin_year=?, vanakkam=?, pattiyal_vagai=?, vaangunar_id=?, vaangunar_peyar=?, vaangunar_munvari=?, pattiyal_naal=?, tharavugal=?, motha_thogai=?, thallupadi=?, podhu_thallupadi_mathippu=?, podhu_thallupadi_vagai=?, podhu_thallupadi_thogai=?, vari_thogai=?, vari_tharavugal=?, sontha_viruppangal=?, nibandhanaigal=?, ullkurippu=?, updated_at=?, is_deleted=0 WHERE id=?"
            } else {
                "INSERT INTO \"$pattiyalTable\" (niruvanam_id, patrucheettu_en, fin_year, vanakkam, pattiyal_vagai, vaangunar_id, vaangunar_peyar, vaangunar_munvari, pattiyal_naal, tharavugal, motha_thogai, thallupadi, podhu_thallupadi_mathippu, podhu_thallupadi_vagai, podhu_thallupadi_thogai, vari_thogai, vari_tharavugal, sontha_viruppangal, nibandhanaigal, ullkurippu, created_at, updated_at, is_deleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)"
            }
        }

        conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { stmt ->
            if (invoice.niruvanamId != null) stmt.setLong(1, invoice.niruvanamId) else stmt.setNull(1, Types.INTEGER)
            stmt.setString(2, invoice.patrucheettuEn)
            stmt.setInt(3, invoice.finYear)
            stmt.setInt(4, invoice.vanakkam)
            stmt.setString(5, invoice.pattiyalVagai)
            if (invoice.vaangunarId != null) stmt.setLong(6, invoice.vaangunarId) else stmt.setNull(6, Types.INTEGER)
            stmt.setString(7, MozhiJsonConverter.stringify(invoice.vaangunarPeyar))
            stmt.setString(8, MozhiJsonConverter.stringify(invoice.vaangunarMunvari))
            stmt.setLong(9, invoice.pattiyalNaal)
            stmt.setString(10, invoice.tharavugal.toString())
            stmt.setDouble(11, invoice.mothaThogai)
            stmt.setDouble(12, invoice.thallupadi)
            stmt.setDouble(13, invoice.podhuThallupadiMathippu)
            stmt.setString(14, invoice.podhuThallupadiVagai)
            stmt.setDouble(15, invoice.podhuThallupadiThogai)
            stmt.setDouble(16, invoice.variThogai)
            stmt.setString(17, invoice.variTharavugal.toString())

            val now = System.currentTimeMillis() / 1000
            if (isFactory) {
                stmt.setDouble(18, invoice.mothaEdai)
                stmt.setDouble(19, invoice.setharamGrams)
                stmt.setDouble(20, invoice.thabaalThogai)
                stmt.setDouble(21, invoice.ahimsaPattuThogai)
                stmt.setString(22, invoice.piravariVugal.toString())
                stmt.setString(23, invoice.sonthaViruppangal)
                stmt.setString(24, invoice.nibandhanaigal)
                stmt.setString(25, invoice.ullkurippu)
                stmt.setString(26, invoice.vangiTharavugal.toString())
                if (isUpdate) {
                    stmt.setLong(27, now)
                    stmt.setLong(28, invoice.id!!)
                    stmt.executeUpdate()
                    invoice.id!!
                } else {
                    stmt.setLong(27, now)
                    stmt.setLong(28, now)
                    stmt.executeUpdate()
                    stmt.generatedKeys.use { rs -> if (rs.next()) rs.getLong(1) else -1L }
                }
            } else {
                stmt.setString(18, invoice.sonthaViruppangal)
                stmt.setString(19, invoice.nibandhanaigal)
                stmt.setString(20, invoice.ullkurippu)
                if (isUpdate) {
                    stmt.setLong(21, now)
                    stmt.setLong(22, invoice.id!!)
                    stmt.executeUpdate()
                    invoice.id!!
                } else {
                    stmt.setLong(21, now)
                    stmt.setLong(22, now)
                    stmt.executeUpdate()
                    stmt.generatedKeys.use { rs -> if (rs.next()) rs.getLong(1) else -1L }
                }
            }
        }
    }

    override fun deleteInvoice(id: Long): Boolean = useConn { conn ->
        conn.prepareStatement("UPDATE \"$pattiyalTable\" SET is_deleted = 1, deleted_at = ? WHERE id = ?").use { stmt ->
            stmt.setLong(1, System.currentTimeMillis() / 1000)
            stmt.setLong(2, id)
            stmt.executeUpdate() > 0
        }
    }

    override fun loadDeletedInvoices(): List<PattiyalTharavuru> = useConn { conn ->
        val list = mutableListOf<PattiyalTharavuru>()
        conn.createStatement().use { stmt ->
            stmt.executeQuery("SELECT * FROM \"$pattiyalTable\" WHERE is_deleted = 1 ORDER BY deleted_at DESC").use { rs ->
                while (rs.next()) list.add(rsToInvoice(rs))
            }
        }
        list
    }

    override fun restoreInvoice(id: Long): Boolean = useConn { conn ->
        conn.prepareStatement("UPDATE \"$pattiyalTable\" SET is_deleted = 0, deleted_at = NULL WHERE id = ?").use { stmt ->
            stmt.setLong(1, id)
            stmt.executeUpdate() > 0
        }
    }

    override fun permanentDeleteInvoice(id: Long): Boolean = useConn { conn ->
        conn.prepareStatement("DELETE FROM \"$pattiyalTable\" WHERE id = ?").use { stmt ->
            stmt.setLong(1, id)
            stmt.executeUpdate() > 0
        }
    }

    override fun purgeExpiredInvoices(days: Int): Int = useConn { conn ->
        val threshold = (System.currentTimeMillis() / 1000) - (days * 86400L)
        conn.prepareStatement("DELETE FROM \"$pattiyalTable\" WHERE is_deleted = 1 AND deleted_at < ?").use { stmt ->
            stmt.setLong(1, threshold)
            stmt.executeUpdate()
        }
    }

    // ── Receipts ─────────────────────────────────────────────────────────────
    override fun loadAllReceipts(): List<PatrugalTharavuru> = useConn { conn ->
        val list = mutableListOf<PatrugalTharavuru>()
        conn.createStatement().use { stmt ->
            stmt.executeQuery("SELECT * FROM \"$patrugalTable\" WHERE is_deleted = 0 ORDER BY patru_naal DESC, id DESC").use { rs ->
                while (rs.next()) list.add(rsToReceipt(rs))
            }
        }
        list
    }

    override fun saveReceipt(receipt: PatrugalTharavuru): Long = useConn { conn ->
        val isUpdate = receipt.id != null && receipt.id > 0
        val sql = if (isUpdate) {
            "UPDATE \"$patrugalTable\" SET niruvanam_id=?, patru_en=?, fin_year=?, vanakkam=?, vaangunar_id=?, vaangunar_peyar=?, vaangunar_munvari=?, patru_naal=?, thogai=?, seluthum_murai=?, vangi_peyar=?, parivarthanai_en=?, ullkurippu=?, updated_at=?, is_deleted=0 WHERE id=?"
        } else {
            "INSERT INTO \"$patrugalTable\" (niruvanam_id, patru_en, fin_year, vanakkam, vaangunar_id, vaangunar_peyar, vaangunar_munvari, patru_naal, thogai, seluthum_murai, vangi_peyar, parivarthanai_en, ullkurippu, created_at, updated_at, is_deleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)"
        }
        conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { stmt ->
            if (receipt.niruvanamId != null) stmt.setLong(1, receipt.niruvanamId) else stmt.setNull(1, Types.INTEGER)
            stmt.setString(2, receipt.patruEn)
            stmt.setString(3, receipt.finYear)
            stmt.setInt(4, receipt.vanakkam)
            if (receipt.vaangunarId != null) stmt.setLong(5, receipt.vaangunarId) else stmt.setNull(5, Types.INTEGER)
            stmt.setString(6, MozhiJsonConverter.stringify(receipt.vaangunarPeyar))
            stmt.setString(7, MozhiJsonConverter.stringify(receipt.vaangunarMunvari))
            stmt.setLong(8, receipt.patruNaal)
            stmt.setDouble(9, receipt.thogai)
            stmt.setString(10, receipt.seluthumMurai)
            if (receipt.vangiPeyar != null) stmt.setString(11, receipt.vangiPeyar) else stmt.setNull(11, Types.VARCHAR)
            if (receipt.parivarthanaiEn != null) stmt.setString(12, receipt.parivarthanaiEn) else stmt.setNull(12, Types.VARCHAR)
            stmt.setString(13, receipt.ullkurippu)
            val now = System.currentTimeMillis() / 1000
            if (isUpdate) {
                stmt.setLong(14, now)
                stmt.setLong(15, receipt.id!!)
                stmt.executeUpdate()
                receipt.id!!
            } else {
                stmt.setLong(14, now)
                stmt.setLong(15, now)
                stmt.executeUpdate()
                stmt.generatedKeys.use { rs -> if (rs.next()) rs.getLong(1) else -1L }
            }
        }
    }

    override fun deleteReceipt(id: Long): Boolean = useConn { conn ->
        conn.prepareStatement("UPDATE \"$patrugalTable\" SET is_deleted = 1, deleted_at = ? WHERE id = ?").use { stmt ->
            stmt.setLong(1, System.currentTimeMillis() / 1000)
            stmt.setLong(2, id)
            stmt.executeUpdate() > 0
        }
    }

    override fun loadDeletedReceipts(): List<PatrugalTharavuru> = useConn { conn ->
        val list = mutableListOf<PatrugalTharavuru>()
        conn.createStatement().use { stmt ->
            stmt.executeQuery("SELECT * FROM \"$patrugalTable\" WHERE is_deleted = 1 ORDER BY deleted_at DESC").use { rs ->
                while (rs.next()) list.add(rsToReceipt(rs))
            }
        }
        list
    }

    override fun restoreReceipt(id: Long): Boolean = useConn { conn ->
        conn.prepareStatement("UPDATE \"$patrugalTable\" SET is_deleted = 0, deleted_at = NULL WHERE id = ?").use { stmt ->
            stmt.setLong(1, id)
            stmt.executeUpdate() > 0
        }
    }

    override fun permanentDeleteReceipt(id: Long): Boolean = useConn { conn ->
        conn.prepareStatement("DELETE FROM \"$patrugalTable\" WHERE id = ?").use { stmt ->
            stmt.setLong(1, id)
            stmt.executeUpdate() > 0
        }
    }

    override fun purgeExpiredReceipts(days: Int): Int = useConn { conn ->
        val threshold = (System.currentTimeMillis() / 1000) - (days * 86400L)
        conn.prepareStatement("DELETE FROM \"$patrugalTable\" WHERE is_deleted = 1 AND deleted_at < ?").use { stmt ->
            stmt.setLong(1, threshold)
            stmt.executeUpdate()
        }
    }

    // ── Junction Links ───────────────────────────────────────────────────────
    override fun getLinksForPatru(patruId: Long): List<PatruPattiyalInaippuTharavuru> = useConn { conn ->
        val list = mutableListOf<PatruPattiyalInaippuTharavuru>()
        conn.prepareStatement("SELECT * FROM \"$junctionTable\" WHERE patru_id = ?").use { stmt ->
            stmt.setLong(1, patruId)
            stmt.executeQuery().use { rs ->
                while (rs.next()) {
                    list.add(PatruPattiyalInaippuTharavuru(
                        id = rs.getLong("id"),
                        patruId = rs.getLong("patru_id"),
                        pattiyalId = rs.getLong("pattiyal_id"),
                        poruthiyaThogai = rs.getDouble("inaippu_thogai")
                    ))
                }
            }
        }
        list
    }

    override fun saveReceiptWithLinks(receipt: PatrugalTharavuru, links: List<PatruPattiyalInaippuTharavuru>): Long = useConn { conn ->
        conn.autoCommit = false
        try {
            val rId = saveReceipt(receipt)
            if (rId > 0) {
                conn.prepareStatement("DELETE FROM \"$junctionTable\" WHERE patru_id = ?").use { delStmt ->
                    delStmt.setLong(1, rId)
                    delStmt.executeUpdate()
                }
                conn.prepareStatement("INSERT INTO \"$junctionTable\" (patru_id, pattiyal_id, inaippu_thogai, created_at) VALUES (?, ?, ?, ?)").use { insStmt ->
                    val now = System.currentTimeMillis() / 1000
                    for (link in links) {
                        insStmt.setLong(1, rId)
                        insStmt.setLong(2, link.pattiyalId)
                        insStmt.setDouble(3, link.poruthiyaThogai)
                        insStmt.setLong(4, now)
                        insStmt.addBatch()
                    }
                    insStmt.executeBatch()
                }
                conn.commit()
            }
            rId
        } catch (e: Exception) {
            conn.rollback()
            throw e
        } finally {
            conn.autoCommit = true
        }
    }

    override fun getPaidAmountForInvoice(invoiceId: Long): Double = useConn { conn ->
        var sum = 0.0
        conn.prepareStatement("SELECT SUM(inaippu_thogai) FROM \"$junctionTable\" WHERE pattiyal_id = ?").use { stmt ->
            stmt.setLong(1, invoiceId)
            stmt.executeQuery().use { rs ->
                if (rs.next()) sum = rs.getDouble(1)
            }
        }
        sum
    }

    override fun getPaidAmountsForInvoices(invoiceIds: List<Long>): Map<Long, Double> = useConn { conn ->
        val result = mutableMapOf<Long, Double>()
        if (invoiceIds.isEmpty()) return@useConn result
        val placeholders = invoiceIds.joinToString(",") { "?" }
        conn.prepareStatement("SELECT pattiyal_id, SUM(inaippu_thogai) FROM \"$junctionTable\" WHERE pattiyal_id IN ($placeholders) GROUP BY pattiyal_id").use { stmt ->
            invoiceIds.forEachIndexed { idx, id -> stmt.setLong(idx + 1, id) }
            stmt.executeQuery().use { rs ->
                while (rs.next()) {
                    result[rs.getLong(1)] = rs.getDouble(2)
                }
            }
        }
        result
    }

    // ── Business Profiles ────────────────────────────────────────────────────
    override fun loadProfile(profileId: Long?): NiruvanaTharavugal? = useConn { conn ->
        var p: NiruvanaTharavugal? = null
        val sql = if (profileId != null) {
            "SELECT * FROM \"$profileTable\" WHERE id = ? AND is_deleted = 0 LIMIT 1"
        } else {
            "SELECT * FROM \"$profileTable\" WHERE is_deleted = 0 ORDER BY id ASC LIMIT 1"
        }
        conn.prepareStatement(sql).use { stmt ->
            if (profileId != null) stmt.setLong(1, profileId)
            stmt.executeQuery().use { rs ->
                if (rs.next()) p = rsToProfile(rs)
            }
        }
        p
    }

    override fun loadAllProfiles(): List<NiruvanaTharavugal> = useConn { conn ->
        val list = mutableListOf<NiruvanaTharavugal>()
        conn.createStatement().use { stmt ->
            stmt.executeQuery("SELECT * FROM \"$profileTable\" WHERE is_deleted = 0 ORDER BY id ASC").use { rs ->
                while (rs.next()) list.add(rsToProfile(rs))
            }
        }
        list
    }

    override fun saveProfile(profile: NiruvanaTharavugal): Boolean = useConn { conn ->
        val exists = if (profile.id != null) {
            conn.prepareStatement("SELECT id FROM \"$profileTable\" WHERE id = ?").use { stmt ->
                stmt.setLong(1, profile.id!!)
                stmt.executeQuery().use { rs -> rs.next() }
            }
        } else false

        val sql = if (exists) {
            "UPDATE \"$profileTable\" SET mudhan_mozhi=?, thunai_mozhi=?, iru_mozhi=?, gst_pirippugal=?, niruvanathin_peyar=?, kurum_peyar=?, tholaipaesi1=?, tholaipaesi2=?, minnanjal=?, gstin=?, mugavari=?, oor=?, maavattam=?, maanilam=?, naadu=?, anjal_kuriyeedu=?, vangi_peyar=?, kilai=?, vangi_kanakku=?, ifsc=?, oavuru=?, agala_oavuru=?, thalaippu_vadivu=?, kaiyoppam=?, oppam_peyar=?, oppam_mudhal_peyar=?, oppam_irudhi_peyar=?, adaimozhi=?, upi_id=?, thoatra_niram=?, updated_at=?, is_deleted=0 WHERE id=?"
        } else {
            "INSERT INTO \"$profileTable\" (mudhan_mozhi, thunai_mozhi, iru_mozhi, gst_pirippugal, niruvanathin_peyar, kurum_peyar, tholaipaesi1, tholaipaesi2, minnanjal, gstin, mugavari, oor, maavattam, maanilam, naadu, anjal_kuriyeedu, vangi_peyar, kilai, vangi_kanakku, ifsc, oavuru, agala_oavuru, thalaippu_vadivu, kaiyoppam, oppam_peyar, oppam_mudhal_peyar, oppam_irudhi_peyar, adaimozhi, upi_id, thoatra_niram, created_at, updated_at, is_deleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)"
        }

        conn.prepareStatement(sql).use { stmt ->
            stmt.setString(1, profile.mudhanMozhi)
            stmt.setString(2, profile.thunaiMozhi)
            stmt.setInt(3, if (profile.iruMozhi) 1 else 0)
            stmt.setInt(4, if (profile.gstPirippugal) 1 else 0)
            stmt.setString(5, MozhiJsonConverter.stringify(profile.niruvanathinPeyar))
            stmt.setString(6, profile.kurumPeyar)
            stmt.setString(7, profile.tholaipaesi1)
            stmt.setString(8, profile.tholaipaesi2)
            stmt.setString(9, profile.minnanjal)
            stmt.setString(10, profile.gstin)
            stmt.setString(11, MozhiJsonConverter.stringify(profile.mugavari))
            stmt.setString(12, MozhiJsonConverter.stringify(profile.oor))
            stmt.setString(13, MozhiJsonConverter.stringify(profile.maavattam))
            stmt.setString(14, MozhiJsonConverter.stringify(profile.maanilam))
            stmt.setString(15, MozhiJsonConverter.stringify(profile.naadu))
            stmt.setString(16, profile.anjalKuriyeedu)
            stmt.setString(17, MozhiJsonConverter.stringify(profile.vangiPeyar))
            stmt.setString(18, MozhiJsonConverter.stringify(profile.kilai))
            stmt.setString(19, profile.vangiKanakku)
            stmt.setString(20, profile.ifsc)
            stmt.setString(21, profile.oavuru)
            stmt.setString(22, profile.agalaOavuru)
            stmt.setString(23, profile.thalaippuVadivu)
            stmt.setString(24, profile.kaiyoppam)
            stmt.setString(25, profile.oppamPeyar)
            stmt.setString(26, profile.oppamMudhalPeyar)
            stmt.setString(27, profile.oppamIrudhiPeyar)
            stmt.setString(28, MozhiJsonConverter.stringify(profile.adaimozhi))
            stmt.setString(29, profile.upiId)
            stmt.setString(30, profile.thoatraNiram)
            val now = System.currentTimeMillis() / 1000
            if (exists) {
                stmt.setLong(31, now)
                stmt.setLong(32, profile.id!!)
            } else {
                stmt.setLong(31, now)
                stmt.setLong(32, now)
            }
            stmt.executeUpdate() > 0
        }
    }

    override fun deleteProfile(profileId: Long): Boolean = useConn { conn ->
        conn.prepareStatement("UPDATE \"$profileTable\" SET is_deleted = 1, deleted_at = ? WHERE id = ?").use { stmt ->
            stmt.setLong(1, System.currentTimeMillis() / 1000)
            stmt.setLong(2, profileId)
            stmt.executeUpdate() > 0
        }
    }

    override fun clearProfiles(): Boolean = useConn { conn ->
        conn.createStatement().use { stmt ->
            stmt.execute("DELETE FROM \"$profileTable\"")
            true
        }
    }

    override fun clearAllData(): Boolean = useConn { conn ->
        conn.createStatement().use { stmt ->
            stmt.execute("DELETE FROM \"$junctionTable\"")
            stmt.execute("DELETE FROM \"$patrugalTable\"")
            stmt.execute("DELETE FROM \"$pattiyalTable\"")
            stmt.execute("DELETE FROM \"$porulTable\"")
            stmt.execute("DELETE FROM \"$vaangunarTable\"")
            stmt.execute("DELETE FROM \"$profileTable\"")
            true
        }
    }

    // ── ResultSet Helpers ────────────────────────────────────────────────────
    private fun rsToMerchant(rs: ResultSet): VaangunarTharavuru = VaangunarTharavuru(
        id = rs.getLong("id"),
        peyar = MozhiJsonConverter.parse(rs.getString("peyar")),
        mugavari = MozhiJsonConverter.parse(rs.getString("mugavari")),
        oor = MozhiJsonConverter.parse(rs.getString("oor")),
        maavattam = MozhiJsonConverter.parse(rs.getString("maavattam")),
        maanilam = MozhiJsonConverter.parse(rs.getString("maanilam")),
        naadu = MozhiJsonConverter.parse(rs.getString("naadu")),
        velinaadMugavari = MozhiJsonConverter.parse(rs.getString("velinaad_mugavari")),
        anjalKuriyeedu = rs.getString("anjal_kuriyeedu") ?: "",
        gstin = rs.getString("gstin") ?: "",
        minnanjal = rs.getString("minnanjal") ?: "",
        tholaipaesi = rs.getString("tholaipaesi") ?: "",
        createdAt = rs.getLong("created_at"),
        updatedAt = rs.getLong("updated_at"),
        isDeleted = rs.getInt("is_deleted") == 1,
        deletedAt = if (rs.getObject("deleted_at") == null) null else rs.getLong("deleted_at")
    )

    private fun rsToItem(rs: ResultSet): PorulTharavuru = PorulTharavuru(
        id = rs.getLong("id"),
        porulPeyar = MozhiJsonConverter.parse(rs.getString("porul_peyar")),
        hsnCode = rs.getString("hsn_code") ?: "",
        vilai = rs.getDouble("vilai"),
        variVeetham = rs.getDouble("vari_veetham"),
        alavuVagai = rs.getString("alavu_vagai") ?: "quantity",
        alagu = rs.getString("alagu") ?: "Nos",
        niruvanamIds = PorulJsonConverter.parseLongList(rs.getString("niruvanam_ids")),
        companyPrices = PorulJsonConverter.parseCompanyPrices(rs.getString("company_prices")),
        createdAt = rs.getLong("created_at"),
        updatedAt = rs.getLong("updated_at"),
        isDeleted = rs.getInt("is_deleted") == 1,
        deletedAt = if (rs.getObject("deleted_at") == null) null else rs.getLong("deleted_at")
    )

    private fun rsToInvoice(rs: ResultSet): PattiyalTharavuru {
        fun hasColumn(name: String): Boolean = try { rs.findColumn(name); true } catch (_: Exception) { false }
        val mothaEdai = if (isFactory && hasColumn("motha_edai")) rs.getDouble("motha_edai") else 0.0
        val setharamGrams = if (isFactory && hasColumn("setharam_grams")) rs.getDouble("setharam_grams") else 0.0
        val thabaalThogai = if (isFactory && hasColumn("thabaal_thogai")) rs.getDouble("thabaal_thogai") else 0.0
        val ahimsaPattuThogai = if (isFactory && hasColumn("ahimsa_pattu_thogai")) rs.getDouble("ahimsa_pattu_thogai") else 0.0
        val piravariVugal = if (isFactory && hasColumn("piravari_vugal")) (rs.getString("piravari_vugal") ?: "[]") else "[]"
        val vangiTharavugal = if (isFactory && hasColumn("vangi_tharavugal")) (rs.getString("vangi_tharavugal") ?: "{}") else "{}"

        return PattiyalTharavuru(
            id = rs.getLong("id"),
            niruvanamId = if (rs.getObject("niruvanam_id") == null) null else rs.getLong("niruvanam_id"),
            patrucheettuEn = rs.getString("patrucheettu_en") ?: "",
            finYear = rs.getInt("fin_year"),
            vanakkam = rs.getInt("vanakkam"),
            pattiyalVagai = rs.getString("pattiyal_vagai") ?: "tax-invoice",
            vaangunarId = if (rs.getObject("vaangunar_id") == null) null else rs.getLong("vaangunar_id"),
            vaangunarPeyar = MozhiJsonConverter.parse(rs.getString("vaangunar_peyar")),
            vaangunarMunvari = MozhiJsonConverter.parse(rs.getString("vaangunar_munvari")),
            pattiyalNaal = rs.getLong("pattiyal_naal"),
            tharavugal = rs.getString("tharavugal") ?: "[]",
            mothaThogai = rs.getDouble("motha_thogai"),
            thallupadi = rs.getDouble("thallupadi"),
            podhuThallupadiMathippu = rs.getDouble("podhu_thallupadi_mathippu"),
            podhuThallupadiVagai = rs.getString("podhu_thallupadi_vagai") ?: "%",
            podhuThallupadiThogai = rs.getDouble("podhu_thallupadi_thogai"),
            variThogai = rs.getDouble("vari_thogai"),
            variTharavugal = rs.getString("vari_tharavugal") ?: "{}",
            mothaEdai = mothaEdai,
            setharamGrams = setharamGrams,
            thabaalThogai = thabaalThogai,
            ahimsaPattuThogai = ahimsaPattuThogai,
            piravariVugal = piravariVugal,
            sonthaViruppangal = rs.getString("sontha_viruppangal") ?: "{}",
            nibandhanaigal = rs.getString("nibandhanaigal") ?: "",
            ullkurippu = rs.getString("ullkurippu") ?: "",
            vangiTharavugal = vangiTharavugal,
            createdAt = rs.getLong("created_at"),
            updatedAt = rs.getLong("updated_at"),
            isDeleted = rs.getInt("is_deleted") == 1,
            deletedAt = if (rs.getObject("deleted_at") == null) null else rs.getLong("deleted_at")
        )
    }

    private fun rsToReceipt(rs: ResultSet): PatrugalTharavuru = PatrugalTharavuru(
        id = rs.getLong("id"),
        niruvanamId = if (rs.getObject("niruvanam_id") == null) null else rs.getLong("niruvanam_id"),
        patruEn = rs.getString("patru_en") ?: "",
        finYear = rs.getString("fin_year") ?: "",
        vanakkam = rs.getInt("vanakkam"),
        vaangunarId = if (rs.getObject("vaangunar_id") == null) null else rs.getLong("vaangunar_id"),
        vaangunarPeyar = MozhiJsonConverter.parse(rs.getString("vaangunar_peyar")),
        vaangunarMunvari = MozhiJsonConverter.parse(rs.getString("vaangunar_munvari")),
        patruNaal = rs.getLong("patru_naal"),
        thogai = rs.getDouble("thogai"),
        seluthumMurai = rs.getString("seluthum_murai") ?: "cash",
        vangiPeyar = rs.getString("vangi_peyar"),
        parivarthanaiEn = rs.getString("parivarthanai_en"),
        ullkurippu = rs.getString("ullkurippu") ?: "",
        createdAt = rs.getLong("created_at"),
        updatedAt = rs.getLong("updated_at"),
        isDeleted = rs.getInt("is_deleted") == 1,
        deletedAt = if (rs.getObject("deleted_at") == null) null else rs.getLong("deleted_at")
    )

    private fun rsToProfile(rs: ResultSet): NiruvanaTharavugal = NiruvanaTharavugal(
        id = rs.getLong("id"),
        mudhanMozhi = rs.getString("mudhan_mozhi") ?: "ta",
        thunaiMozhi = rs.getString("thunai_mozhi") ?: "en",
        iruMozhi = rs.getInt("iru_mozhi") == 1,
        gstPirippugal = rs.getInt("gst_pirippugal") == 1,
        niruvanathinPeyar = MozhiJsonConverter.parse(rs.getString("niruvanathin_peyar")),
        kurumPeyar = rs.getString("kurum_peyar") ?: "",
        tholaipaesi1 = rs.getString("tholaipaesi1") ?: "",
        tholaipaesi2 = rs.getString("tholaipaesi2") ?: "",
        minnanjal = rs.getString("minnanjal") ?: "",
        gstin = rs.getString("gstin") ?: "",
        mugavari = MozhiJsonConverter.parse(rs.getString("mugavari")),
        oor = MozhiJsonConverter.parse(rs.getString("oor")),
        maavattam = MozhiJsonConverter.parse(rs.getString("maavattam")),
        maanilam = MozhiJsonConverter.parse(rs.getString("maanilam")),
        naadu = MozhiJsonConverter.parse(rs.getString("naadu")),
        anjalKuriyeedu = rs.getString("anjal_kuriyeedu") ?: "",
        vangiPeyar = MozhiJsonConverter.parse(rs.getString("vangi_peyar")),
        kilai = MozhiJsonConverter.parse(rs.getString("kilai")),
        vangiKanakku = rs.getString("vangi_kanakku") ?: "",
        ifsc = rs.getString("ifsc") ?: "",
        oavuru = rs.getString("oavuru") ?: "",
        agalaOavuru = rs.getString("agala_oavuru") ?: "",
        thalaippuVadivu = rs.getString("thalaippu_vadivu") ?: "small",
        kaiyoppam = rs.getString("kaiyoppam") ?: "",
        oppamPeyar = rs.getString("oppam_peyar") ?: "",
        oppamMudhalPeyar = rs.getString("oppam_mudhal_peyar") ?: "",
        oppamIrudhiPeyar = rs.getString("oppam_irudhi_peyar") ?: "",
        adaimozhi = MozhiJsonConverter.parse(rs.getString("adaimozhi")),
        upiId = rs.getString("upi_id") ?: "",
        thoatraNiram = rs.getString("thoatra_niram") ?: ""
    )
}

private val desktopFactoryDbInstance by lazy {
    DesktopUdukkaiDatabaseHelper("udukkai_factory.db", "factory_", true)
}

private val desktopTextilesDbInstance by lazy {
    DesktopUdukkaiDatabaseHelper("udukkai_textiles.db", "textiles_", false)
}

actual fun getFactoryDatabaseHelper(): UdukkaiDatabaseHelper = desktopFactoryDbInstance
actual fun getTextilesDatabaseHelper(): UdukkaiDatabaseHelper = desktopTextilesDbInstance
