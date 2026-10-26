package com.elvan.udukkai.data.database

import com.elvan.udukkai.data.DesktopDbPaths
import com.elvan.udukkai.data.model.CustomerModel
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.model.ReceiptInvoiceLinkModel
import com.elvan.udukkai.data.model.ReceiptModel
import com.elvan.udukkai.data.model.ItemJsonConverter
import com.elvan.udukkai.data.model.ItemModel
import com.elvan.udukkai.data.settings.LanguageJsonConverter
import com.elvan.udukkai.data.settings.BusinessProfile
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

    private val customersTable = "${tablePrefix}customers"
    private val itemsTable = "${tablePrefix}items"
    // itemsTable is standardized
    private val invoicesTable = "${tablePrefix}invoices"
    private val receiptsTable = "${tablePrefix}receipts"
    private val linksTable = "${tablePrefix}receipt_invoice_links"
    private val profilesTable = "${tablePrefix}business_profiles"

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

    private fun renameColumnIfExists(conn: Connection, tableName: String, oldCol: String, newCol: String) {
        try {
            val existingCols = mutableSetOf<String>()
            conn.createStatement().use { stmt ->
                stmt.executeQuery("PRAGMA table_info(\"$tableName\")").use { rs ->
                    while (rs.next()) {
                        existingCols.add(rs.getString("name").lowercase())
                    }
                }
            }
            if (existingCols.contains(oldCol.lowercase()) && !existingCols.contains(newCol.lowercase())) {
                println("Renaming legacy column $tableName.$oldCol -> $newCol")
                conn.createStatement().use { stmt ->
                    stmt.execute("ALTER TABLE \"$tableName\" RENAME COLUMN \"$oldCol\" TO \"$newCol\"")
                }
            }
        } catch (e: Exception) {
            println("Column rename $tableName.$oldCol -> $newCol error: ${e.message}")
        }
    }

    private fun ensureTables(conn: Connection) {
        val legacyPrefix = if (isFactory) "kooli_" else "pattu_"

        // 1. Rename any legacy / Tanglish tables to standardized English tables
        renameTableIfExists(conn, "${legacyPrefix}vaangunar_table", customersTable)
        renameTableIfExists(conn, "${tablePrefix}vaangunar_table", customersTable)

        renameTableIfExists(conn, "${legacyPrefix}porul_table", itemsTable)
        renameTableIfExists(conn, "${tablePrefix}porul_table", itemsTable)
        renameTableIfExists(conn, "${legacyPrefix}products", itemsTable)
        renameTableIfExists(conn, "${tablePrefix}products", itemsTable)

        renameTableIfExists(conn, "${legacyPrefix}pattiyal_table", invoicesTable)
        renameTableIfExists(conn, "${tablePrefix}pattiyal_table", invoicesTable)

        renameTableIfExists(conn, "${legacyPrefix}patrugal_table", receiptsTable)
        renameTableIfExists(conn, "${tablePrefix}patrugal_table", receiptsTable)

        renameTableIfExists(conn, "${legacyPrefix}patru_pattiyal_table", linksTable)
        renameTableIfExists(conn, "${tablePrefix}patru_pattiyal_table", linksTable)

        renameTableIfExists(conn, "${legacyPrefix}niruvana_tharavugal_table", profilesTable)
        renameTableIfExists(conn, "${tablePrefix}niruvana_tharavugal_table", profilesTable)

        // 2. Rename legacy columns in customersTable
        renameColumnIfExists(conn, customersTable, "peyar", "name")
        renameColumnIfExists(conn, customersTable, "mugavari", "address")
        renameColumnIfExists(conn, customersTable, "oor", "city")
        renameColumnIfExists(conn, customersTable, "maavattam", "district")
        renameColumnIfExists(conn, customersTable, "maanilam", "state")
        renameColumnIfExists(conn, customersTable, "naadu", "country")
        renameColumnIfExists(conn, customersTable, "velinaad_mugavari", "international_address")
        renameColumnIfExists(conn, customersTable, "anjal_kuriyeedu", "pin_code")
        renameColumnIfExists(conn, customersTable, "minnanjal", "email")
        renameColumnIfExists(conn, customersTable, "tholaipaesi", "phone")

        // 3. Rename legacy columns in itemsTable
        renameColumnIfExists(conn, itemsTable, "porul_peyar", "name")
        renameColumnIfExists(conn, itemsTable, "vilai", "price")
        renameColumnIfExists(conn, itemsTable, "vari_veetham", "tax_rate")
        renameColumnIfExists(conn, itemsTable, "alavu_vagai", "quantity_type")
        renameColumnIfExists(conn, itemsTable, "alagu", "unit")
        renameColumnIfExists(conn, itemsTable, "niruvanam_ids", "company_ids")

        // 4. Rename legacy columns in invoicesTable
        renameColumnIfExists(conn, invoicesTable, "niruvanam_id", "company_id")
        renameColumnIfExists(conn, invoicesTable, "patrucheettu_en", "invoice_number")
        renameColumnIfExists(conn, invoicesTable, "pattiyal_vagai", "invoice_type")
        renameColumnIfExists(conn, invoicesTable, "vaangunar_id", "customer_id")
        renameColumnIfExists(conn, invoicesTable, "vaangunar_peyar", "customer_name")
        renameColumnIfExists(conn, invoicesTable, "vaangunar_munvari", "customer_address")
        renameColumnIfExists(conn, invoicesTable, "pattiyal_naal", "invoice_date")
        renameColumnIfExists(conn, invoicesTable, "tharavugal", "items_json")
        renameColumnIfExists(conn, invoicesTable, "motha_thogai", "total_amount")
        renameColumnIfExists(conn, invoicesTable, "thallupadi", "discount")
        renameColumnIfExists(conn, invoicesTable, "podhu_thallupadi_mathippu", "global_discount_value")
        renameColumnIfExists(conn, invoicesTable, "podhu_thallupadi_vagai", "global_discount_type")
        renameColumnIfExists(conn, invoicesTable, "podhu_thallupadi_thogai", "global_discount_amount")
        renameColumnIfExists(conn, invoicesTable, "vari_thogai", "tax_amount")
        renameColumnIfExists(conn, invoicesTable, "vari_tharavugal", "tax_data_json")
        renameColumnIfExists(conn, invoicesTable, "motha_edai", "total_weight")
        renameColumnIfExists(conn, invoicesTable, "setharam_grams", "wastage_grams")
        renameColumnIfExists(conn, invoicesTable, "thabaal_thogai", "postal_charges")
        renameColumnIfExists(conn, invoicesTable, "ahimsa_pattu_thogai", "ahimsa_silk_amount")
        renameColumnIfExists(conn, invoicesTable, "piravari_vugal", "other_taxes_json")
        renameColumnIfExists(conn, invoicesTable, "sontha_viruppangal", "custom_preferences_json")
        renameColumnIfExists(conn, invoicesTable, "nibandhanaigal", "terms_and_conditions")
        renameColumnIfExists(conn, invoicesTable, "ullkurippu", "notes")
        renameColumnIfExists(conn, invoicesTable, "vangi_tharavugal", "bank_details_json")

        // 5. Rename legacy columns in receiptsTable
        renameColumnIfExists(conn, receiptsTable, "niruvanam_id", "company_id")
        renameColumnIfExists(conn, receiptsTable, "patru_en", "receipt_number")
        renameColumnIfExists(conn, receiptsTable, "vaangunar_id", "customer_id")
        renameColumnIfExists(conn, receiptsTable, "vaangunar_peyar", "customer_name")
        renameColumnIfExists(conn, receiptsTable, "vaangunar_munvari", "customer_address")
        renameColumnIfExists(conn, receiptsTable, "patru_naal", "receipt_date")
        renameColumnIfExists(conn, receiptsTable, "thogai", "amount")
        renameColumnIfExists(conn, receiptsTable, "seluthum_murai", "payment_mode")
        renameColumnIfExists(conn, receiptsTable, "vangi_peyar", "bank_name")
        renameColumnIfExists(conn, receiptsTable, "parivarthanai_en", "transaction_number")
        renameColumnIfExists(conn, receiptsTable, "ullkurippu", "notes")

        // 6. Rename legacy columns in linksTable
        renameColumnIfExists(conn, linksTable, "patru_id", "receipt_id")
        renameColumnIfExists(conn, linksTable, "pattiyal_id", "invoice_id")
        renameColumnIfExists(conn, linksTable, "inaippu_thogai", "applied_amount")
        renameColumnIfExists(conn, linksTable, "poruthiya_thogai", "applied_amount")

        // 7. Rename legacy columns in profilesTable
        renameColumnIfExists(conn, profilesTable, "mudhan_mozhi", "primary_language")
        renameColumnIfExists(conn, profilesTable, "thunai_mozhi", "secondary_language")
        renameColumnIfExists(conn, profilesTable, "iru_mozhi", "is_bilingual")
        renameColumnIfExists(conn, profilesTable, "gst_pirippugal", "split_gst")
        renameColumnIfExists(conn, profilesTable, "niruvanathin_peyar", "business_name")
        renameColumnIfExists(conn, profilesTable, "kurum_peyar", "short_name")
        renameColumnIfExists(conn, profilesTable, "tholaipaesi1", "phone1")
        renameColumnIfExists(conn, profilesTable, "tholaipaesi2", "phone2")
        renameColumnIfExists(conn, profilesTable, "minnanjal", "email")
        renameColumnIfExists(conn, profilesTable, "mugavari", "address")
        renameColumnIfExists(conn, profilesTable, "oor", "city")
        renameColumnIfExists(conn, profilesTable, "maavattam", "district")
        renameColumnIfExists(conn, profilesTable, "maanilam", "state")
        renameColumnIfExists(conn, profilesTable, "naadu", "country")
        renameColumnIfExists(conn, profilesTable, "anjal_kuriyeedu", "pin_code")
        renameColumnIfExists(conn, profilesTable, "vangi_peyar", "bank_name")
        renameColumnIfExists(conn, profilesTable, "kilai", "branch")
        renameColumnIfExists(conn, profilesTable, "vangi_kanakku", "account_number")
        renameColumnIfExists(conn, profilesTable, "oavuru", "logo")
        renameColumnIfExists(conn, profilesTable, "agala_oavuru", "wide_logo")
        renameColumnIfExists(conn, profilesTable, "thalaippu_vadivu", "header_layout")
        renameColumnIfExists(conn, profilesTable, "kaiyoppam", "signature")
        renameColumnIfExists(conn, profilesTable, "oppam_peyar", "signatory_name")
        renameColumnIfExists(conn, profilesTable, "oppam_mudhal_peyar", "signatory_first_name")
        renameColumnIfExists(conn, profilesTable, "oppam_irudhi_peyar", "signatory_last_name")
        renameColumnIfExists(conn, profilesTable, "adaimozhi", "tagline")
        renameColumnIfExists(conn, profilesTable, "thoatra_niram", "theme_color")

        conn.createStatement().use { stmt ->
            // 1. Customers Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS "$customersTable" (
                    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    "name" TEXT NOT NULL DEFAULT '{}',
                    "address" TEXT NOT NULL DEFAULT '{}',
                    "city" TEXT NOT NULL DEFAULT '{}',
                    "district" TEXT NOT NULL DEFAULT '{}',
                    "state" TEXT NOT NULL DEFAULT '{}',
                    "country" TEXT NOT NULL DEFAULT '{"en": "India", "ta": "இந்தியா"}',
                    "international_address" TEXT NOT NULL DEFAULT '{}',
                    "pin_code" TEXT NOT NULL DEFAULT '',
                    "gstin" TEXT NOT NULL DEFAULT '',
                    "email" TEXT NOT NULL DEFAULT '',
                    "phone" TEXT NOT NULL DEFAULT '',
                    "company_ids" TEXT NOT NULL DEFAULT '[]',
                    "created_at" INTEGER NOT NULL DEFAULT (CAST(strftime('%s', CURRENT_TIMESTAMP) AS INTEGER)),
                    "updated_at" INTEGER NOT NULL DEFAULT (CAST(strftime('%s', CURRENT_TIMESTAMP) AS INTEGER)),
                    "is_deleted" INTEGER NOT NULL DEFAULT 0 CHECK ("is_deleted" IN (0, 1)),
                    "deleted_at" INTEGER NULL
                )
            """.trimIndent())

            // 2. Items Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS "$itemsTable" (
                    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    "name" TEXT NOT NULL DEFAULT '{}',
                    "hsn_code" TEXT NOT NULL DEFAULT '',
                    "price" REAL NOT NULL DEFAULT 0.0,
                    "tax_rate" REAL NOT NULL DEFAULT 0.0,
                    "quantity_type" TEXT NOT NULL DEFAULT 'quantity',
                    "unit" TEXT NOT NULL DEFAULT 'Nos',
                    "company_ids" TEXT NOT NULL DEFAULT '[]',
                    "company_prices" TEXT NOT NULL DEFAULT '{}',
                    "created_at" INTEGER NOT NULL DEFAULT (CAST(strftime('%s', CURRENT_TIMESTAMP) AS INTEGER)),
                    "updated_at" INTEGER NOT NULL DEFAULT (CAST(strftime('%s', CURRENT_TIMESTAMP) AS INTEGER)),
                    "is_deleted" INTEGER NOT NULL DEFAULT 0 CHECK ("is_deleted" IN (0, 1)),
                    "deleted_at" INTEGER NULL
                )
            """.trimIndent())

            // 3. Invoices Table
            val createInvoiceSql = if (isFactory) {
                """
                CREATE TABLE IF NOT EXISTS "$invoicesTable" (
                    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    "company_id" INTEGER NULL,
                    "invoice_number" TEXT NOT NULL,
                    "fin_year" INTEGER NOT NULL,
                    "vanakkam" INTEGER NOT NULL DEFAULT 1,
                    "invoice_type" TEXT NOT NULL DEFAULT 'tax-invoice',
                    "customer_id" INTEGER NULL,
                    "customer_name" TEXT NOT NULL DEFAULT '{}',
                    "customer_address" TEXT NOT NULL DEFAULT '{}',
                    "invoice_date" INTEGER NOT NULL DEFAULT (CAST(strftime('%s', CURRENT_TIMESTAMP) AS INTEGER)),
                    "items_json" TEXT NOT NULL DEFAULT '[]',
                    "total_amount" REAL NOT NULL DEFAULT 0.0,
                    "discount" REAL NOT NULL DEFAULT 0.0,
                    "global_discount_value" REAL NOT NULL DEFAULT 0.0,
                    "global_discount_type" TEXT NOT NULL DEFAULT '%',
                    "global_discount_amount" REAL NOT NULL DEFAULT 0.0,
                    "tax_amount" REAL NOT NULL DEFAULT 0.0,
                    "tax_data_json" TEXT NOT NULL DEFAULT '{}',
                    "total_weight" REAL NOT NULL DEFAULT 0.0,
                    "wastage_grams" REAL NOT NULL DEFAULT 0.0,
                    "postal_charges" REAL NOT NULL DEFAULT 0.0,
                    "ahimsa_silk_amount" REAL NOT NULL DEFAULT 0.0,
                    "other_taxes_json" TEXT NOT NULL DEFAULT '[]',
                    "custom_preferences_json" TEXT NOT NULL DEFAULT '{}',
                    "terms_and_conditions" TEXT NOT NULL DEFAULT '',
                    "notes" TEXT NOT NULL DEFAULT '',
                    "bank_details_json" TEXT NOT NULL DEFAULT '{}',
                    "created_at" INTEGER NOT NULL DEFAULT (CAST(strftime('%s', CURRENT_TIMESTAMP) AS INTEGER)),
                    "updated_at" INTEGER NOT NULL DEFAULT (CAST(strftime('%s', CURRENT_TIMESTAMP) AS INTEGER)),
                    "is_deleted" INTEGER NOT NULL DEFAULT 0 CHECK ("is_deleted" IN (0, 1)),
                    "deleted_at" INTEGER NULL
                )
                """.trimIndent()
            } else {
                """
                CREATE TABLE IF NOT EXISTS "$invoicesTable" (
                    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    "company_id" INTEGER NULL,
                    "invoice_number" TEXT NOT NULL,
                    "fin_year" INTEGER NOT NULL,
                    "vanakkam" INTEGER NOT NULL DEFAULT 1,
                    "invoice_type" TEXT NOT NULL DEFAULT 'tax-invoice',
                    "customer_id" INTEGER NULL,
                    "customer_name" TEXT NOT NULL DEFAULT '{}',
                    "customer_address" TEXT NOT NULL DEFAULT '{}',
                    "invoice_date" INTEGER NOT NULL DEFAULT (CAST(strftime('%s', CURRENT_TIMESTAMP) AS INTEGER)),
                    "items_json" TEXT NOT NULL DEFAULT '[]',
                    "total_amount" REAL NOT NULL DEFAULT 0.0,
                    "discount" REAL NOT NULL DEFAULT 0.0,
                    "global_discount_value" REAL NOT NULL DEFAULT 0.0,
                    "global_discount_type" TEXT NOT NULL DEFAULT '%',
                    "global_discount_amount" REAL NOT NULL DEFAULT 0.0,
                    "tax_amount" REAL NOT NULL DEFAULT 0.0,
                    "tax_data_json" TEXT NOT NULL DEFAULT '{}',
                    "custom_preferences_json" TEXT NOT NULL DEFAULT '{}',
                    "terms_and_conditions" TEXT NOT NULL DEFAULT '',
                    "notes" TEXT NOT NULL DEFAULT '',
                    "created_at" INTEGER NOT NULL DEFAULT (CAST(strftime('%s', CURRENT_TIMESTAMP) AS INTEGER)),
                    "updated_at" INTEGER NOT NULL DEFAULT (CAST(strftime('%s', CURRENT_TIMESTAMP) AS INTEGER)),
                    "is_deleted" INTEGER NOT NULL DEFAULT 0 CHECK ("is_deleted" IN (0, 1)),
                    "deleted_at" INTEGER NULL
                )
                """.trimIndent()
            }
            stmt.execute(createInvoiceSql)

            // 4. Receipts Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS "$receiptsTable" (
                    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    "company_id" INTEGER NULL,
                    "receipt_number" TEXT NOT NULL,
                    "fin_year" TEXT NOT NULL DEFAULT '',
                    "vanakkam" INTEGER NOT NULL DEFAULT 1,
                    "customer_id" INTEGER NULL,
                    "customer_name" TEXT NOT NULL DEFAULT '{}',
                    "customer_address" TEXT NOT NULL DEFAULT '{}',
                    "receipt_date" INTEGER NOT NULL DEFAULT (CAST(strftime('%s', CURRENT_TIMESTAMP) AS INTEGER)),
                    "amount" REAL NOT NULL DEFAULT 0.0,
                    "payment_mode" TEXT NOT NULL DEFAULT 'cash',
                    "bank_name" TEXT NULL,
                    "transaction_number" TEXT NULL,
                    "notes" TEXT NOT NULL DEFAULT '',
                    "created_at" INTEGER NOT NULL DEFAULT (CAST(strftime('%s', CURRENT_TIMESTAMP) AS INTEGER)),
                    "updated_at" INTEGER NOT NULL DEFAULT (CAST(strftime('%s', CURRENT_TIMESTAMP) AS INTEGER)),
                    "is_deleted" INTEGER NOT NULL DEFAULT 0 CHECK ("is_deleted" IN (0, 1)),
                    "deleted_at" INTEGER NULL
                )
            """.trimIndent())

            // 5. Junction Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS "$linksTable" (
                    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    "receipt_id" INTEGER NOT NULL,
                    "invoice_id" INTEGER NOT NULL,
                    "applied_amount" REAL NOT NULL DEFAULT 0.0,
                    "created_at" INTEGER NOT NULL DEFAULT (CAST(strftime('%s', CURRENT_TIMESTAMP) AS INTEGER))
                )
            """.trimIndent())

            try { stmt.execute("ALTER TABLE \"$customersTable\" ADD COLUMN \"company_ids\" TEXT NOT NULL DEFAULT '[]'") } catch (_: Exception) {}
            try { stmt.execute("ALTER TABLE \"$profilesTable\" ADD COLUMN \"print_settings\" TEXT NOT NULL DEFAULT '{}'") } catch (_: Exception) {}
            try { stmt.execute("ALTER TABLE \"$linksTable\" ADD COLUMN \"created_at\" INTEGER NOT NULL DEFAULT 0") } catch (_: Exception) {}
            try { stmt.execute("ALTER TABLE \"$linksTable\" ADD COLUMN \"applied_amount\" REAL NOT NULL DEFAULT 0.0") } catch (_: Exception) {}
            try { stmt.execute("UPDATE \"$linksTable\" SET applied_amount = poruthiya_thogai WHERE (applied_amount = 0.0 OR applied_amount IS NULL) AND poruthiya_thogai > 0.0") } catch (_: Exception) {}

            // 6. Profiles Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS "$profilesTable" (
                    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    "primary_language" TEXT NOT NULL DEFAULT 'ta',
                    "secondary_language" TEXT NOT NULL DEFAULT 'en',
                    "is_bilingual" INTEGER NOT NULL DEFAULT 1 CHECK ("is_bilingual" IN (0, 1)),
                    "split_gst" INTEGER NOT NULL DEFAULT 0 CHECK ("split_gst" IN (0, 1)),
                    "business_name" TEXT NOT NULL DEFAULT '{}',
                    "short_name" TEXT NOT NULL DEFAULT '',
                    "phone1" TEXT NOT NULL DEFAULT '',
                    "phone2" TEXT NOT NULL DEFAULT '',
                    "email" TEXT NOT NULL DEFAULT '',
                    "gstin" TEXT NOT NULL DEFAULT '',
                    "address" TEXT NOT NULL DEFAULT '{}',
                    "city" TEXT NOT NULL DEFAULT '{}',
                    "district" TEXT NOT NULL DEFAULT '{}',
                    "state" TEXT NOT NULL DEFAULT '{}',
                    "country" TEXT NOT NULL DEFAULT '{"en": "India", "ta": "இந்தியா"}',
                    "pin_code" TEXT NOT NULL DEFAULT '',
                    "bank_name" TEXT NOT NULL DEFAULT '{}',
                    "branch" TEXT NOT NULL DEFAULT '{}',
                    "account_number" TEXT NOT NULL DEFAULT '',
                    "ifsc" TEXT NOT NULL DEFAULT '',
                    "logo" TEXT NOT NULL DEFAULT '',
                    "wide_logo" TEXT NOT NULL DEFAULT '',
                    "header_layout" TEXT NOT NULL DEFAULT 'small',
                    "signature" TEXT NOT NULL DEFAULT '',
                    "signatory_name" TEXT NOT NULL DEFAULT '',
                    "signatory_first_name" TEXT NOT NULL DEFAULT '',
                    "signatory_last_name" TEXT NOT NULL DEFAULT '',
                    "tagline" TEXT NOT NULL DEFAULT '{}',
                    "upi_id" TEXT NOT NULL DEFAULT '',
                    "theme_color" TEXT NOT NULL DEFAULT '',
                    "print_settings" TEXT NOT NULL DEFAULT '{}',
                    "created_at" INTEGER NOT NULL DEFAULT (CAST(strftime('%s', CURRENT_TIMESTAMP) AS INTEGER)),
                    "updated_at" INTEGER NOT NULL DEFAULT (CAST(strftime('%s', CURRENT_TIMESTAMP) AS INTEGER)),
                    "is_deleted" INTEGER NOT NULL DEFAULT 0 CHECK ("is_deleted" IN (0, 1)),
                    "deleted_at" INTEGER NULL
                )
            """.trimIndent())
        }
    }

    private inline fun <T> useConn(crossinline block: (Connection) -> T): T {
        val conn = getConnection()
        return try {
            ensureTables(conn)
            block(conn)
        } finally {
            try { conn.close() } catch (_: Exception) {}
        }
    }

    // ── Safe ResultSet Parsing Helpers ───────────────────────────────────────
    private fun getColIdx(rs: ResultSet, vararg names: String): Int {
        val meta = rs.metaData
        val count = meta.columnCount
        for (name in names) {
            for (i in 1..count) {
                if (meta.getColumnLabel(i).equals(name, ignoreCase = true) ||
                    meta.getColumnName(i).equals(name, ignoreCase = true)) {
                    return i
                }
            }
        }
        return -1
    }

    private fun getStringCol(rs: ResultSet, vararg names: String, default: String = ""): String {
        val idx = getColIdx(rs, *names)
        if (idx < 1) return default
        return rs.getString(idx) ?: default
    }

    private fun getDoubleCol(rs: ResultSet, vararg names: String, default: Double = 0.0): Double {
        val idx = getColIdx(rs, *names)
        if (idx < 1) return default
        return rs.getDouble(idx)
    }

    private fun getLongCol(rs: ResultSet, vararg names: String, default: Long = 0L): Long {
        val idx = getColIdx(rs, *names)
        if (idx < 1) return default
        return rs.getLong(idx)
    }

    private fun getLongOrNull(rs: ResultSet, vararg names: String): Long? {
        val idx = getColIdx(rs, *names)
        if (idx < 1) return null
        val v = rs.getLong(idx)
        return if (rs.wasNull()) null else v
    }

    private fun getStringOrNull(rs: ResultSet, vararg names: String): String? {
        val idx = getColIdx(rs, *names)
        if (idx < 1) return null
        return rs.getString(idx)
    }

    private fun getIntCol(rs: ResultSet, vararg names: String, default: Int = 0): Int {
        val idx = getColIdx(rs, *names)
        if (idx < 1) return default
        return rs.getInt(idx)
    }

    // ── Customers ────────────────────────────────────────────────────────────
    override fun loadAllCustomers(): List<CustomerModel> = useConn { conn ->
        val list = mutableListOf<CustomerModel>()
        conn.createStatement().use { stmt ->
            stmt.executeQuery("SELECT * FROM \"$customersTable\" WHERE is_deleted = 0 ORDER BY id DESC").use { rs ->
                while (rs.next()) list.add(rsToMerchant(rs))
            }
        }
        list
    }

    override fun saveCustomer(customer: CustomerModel): Long = useConn { conn ->
        val isUpdate = customer.id != null && customer.id > 0
        val sql = if (isUpdate) {
            "UPDATE \"$customersTable\" SET name=?, address=?, city=?, district=?, state=?, country=?, international_address=?, pin_code=?, gstin=?, email=?, phone=?, company_ids=?, updated_at=?, is_deleted=0 WHERE id=?"
        } else {
            "INSERT INTO \"$customersTable\" (name, address, city, district, state, country, international_address, pin_code, gstin, email, phone, company_ids, created_at, updated_at, is_deleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)"
        }
        conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { stmt ->
            stmt.setString(1, LanguageJsonConverter.stringify(customer.name))
            stmt.setString(2, LanguageJsonConverter.stringify(customer.address))
            stmt.setString(3, LanguageJsonConverter.stringify(customer.city))
            stmt.setString(4, LanguageJsonConverter.stringify(customer.district))
            stmt.setString(5, LanguageJsonConverter.stringify(customer.state))
            stmt.setString(6, LanguageJsonConverter.stringify(customer.country))
            stmt.setString(7, LanguageJsonConverter.stringify(customer.internationalAddress))
            stmt.setString(8, customer.pinCode)
            stmt.setString(9, customer.gstin)
            stmt.setString(10, customer.email)
            stmt.setString(11, customer.phone)
            stmt.setString(12, customer.companyIds.toString())

            val now = System.currentTimeMillis() / 1000
            if (isUpdate) {
                stmt.setLong(13, now)
                stmt.setLong(14, customer.id!!)
                stmt.executeUpdate()
                customer.id!!
            } else {
                stmt.setLong(13, now)
                stmt.setLong(14, now)
                stmt.executeUpdate()
                stmt.generatedKeys.use { gk ->
                    if (gk.next()) gk.getLong(1) else -1L
                }
            }
        }
    }

    override fun deleteCustomer(id: Long): Boolean = useConn { conn ->
        conn.prepareStatement("UPDATE \"$customersTable\" SET is_deleted = 1, deleted_at = ? WHERE id = ?").use { stmt ->
            stmt.setLong(1, System.currentTimeMillis() / 1000)
            stmt.setLong(2, id)
            stmt.executeUpdate() > 0
        }
    }

    override fun loadDeletedCustomers(): List<CustomerModel> = useConn { conn ->
        val list = mutableListOf<CustomerModel>()
        conn.createStatement().use { stmt ->
            stmt.executeQuery("SELECT * FROM \"$customersTable\" WHERE is_deleted = 1 ORDER BY deleted_at DESC").use { rs ->
                while (rs.next()) list.add(rsToMerchant(rs))
            }
        }
        list
    }

    override fun restoreCustomer(id: Long): Boolean = useConn { conn ->
        conn.prepareStatement("UPDATE \"$customersTable\" SET is_deleted = 0, deleted_at = NULL WHERE id = ?").use { stmt ->
            stmt.setLong(1, id)
            stmt.executeUpdate() > 0
        }
    }

    override fun permanentDeleteCustomer(id: Long): Boolean = useConn { conn ->
        conn.prepareStatement("DELETE FROM \"$customersTable\" WHERE id = ?").use { stmt ->
            stmt.setLong(1, id)
            stmt.executeUpdate() > 0
        }
    }

    override fun purgeExpiredCustomers(days: Int): Int = useConn { conn ->
        val threshold = (System.currentTimeMillis() / 1000) - (days * 86400L)
        conn.prepareStatement("DELETE FROM \"$customersTable\" WHERE is_deleted = 1 AND deleted_at < ?").use { stmt ->
            stmt.setLong(1, threshold)
            stmt.executeUpdate()
        }
    }

    // Backward-compatible delegates
    override fun loadAllMerchants(): List<CustomerModel> = loadAllCustomers()
    override fun saveMerchant(merchant: CustomerModel): Long = saveCustomer(merchant)
    override fun deleteMerchant(id: Long): Boolean = deleteCustomer(id)
    override fun loadDeletedMerchants(): List<CustomerModel> = loadDeletedCustomers()
    override fun restoreMerchant(id: Long): Boolean = restoreCustomer(id)
    override fun permanentDeleteMerchant(id: Long): Boolean = permanentDeleteCustomer(id)
    override fun purgeExpiredMerchants(days: Int): Int = purgeExpiredCustomers(days)

    // ── Items ────────────────────────────────────────────────────────────────
    override fun loadAllItems(): List<ItemModel> = useConn { conn ->
        val list = mutableListOf<ItemModel>()
        conn.createStatement().use { stmt ->
            stmt.executeQuery("SELECT * FROM \"$itemsTable\" WHERE is_deleted = 0 ORDER BY id DESC").use { rs ->
                while (rs.next()) list.add(rsToItem(rs))
            }
        }
        list
    }

    override fun saveItem(item: ItemModel): Long = useConn { conn ->
        val isUpdate = item.id != null && item.id > 0
        val sql = if (isUpdate) {
            "UPDATE \"$itemsTable\" SET name=?, hsn_code=?, price=?, tax_rate=?, quantity_type=?, unit=?, company_ids=?, company_prices=?, updated_at=?, is_deleted=0 WHERE id=?"
        } else {
            "INSERT INTO \"$itemsTable\" (name, hsn_code, price, tax_rate, quantity_type, unit, company_ids, company_prices, created_at, updated_at, is_deleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)"
        }
        conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { stmt ->
            stmt.setString(1, LanguageJsonConverter.stringify(item.name))
            stmt.setString(2, item.hsnCode)
            stmt.setDouble(3, item.price)
            stmt.setDouble(4, item.taxRate)
            stmt.setString(5, item.quantityType)
            stmt.setString(6, item.unit)
            stmt.setString(7, item.companyIds.toString())
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
                stmt.generatedKeys.use { gk ->
                    if (gk.next()) gk.getLong(1) else -1L
                }
            }
        }
    }

    override fun deleteItem(id: Long): Boolean = useConn { conn ->
        conn.prepareStatement("UPDATE \"$itemsTable\" SET is_deleted = 1, deleted_at = ? WHERE id = ?").use { stmt ->
            stmt.setLong(1, System.currentTimeMillis() / 1000)
            stmt.setLong(2, id)
            stmt.executeUpdate() > 0
        }
    }

    override fun loadDeletedItems(): List<ItemModel> = useConn { conn ->
        val list = mutableListOf<ItemModel>()
        conn.createStatement().use { stmt ->
            stmt.executeQuery("SELECT * FROM \"$itemsTable\" WHERE is_deleted = 1 ORDER BY deleted_at DESC").use { rs ->
                while (rs.next()) list.add(rsToItem(rs))
            }
        }
        list
    }

    override fun restoreItem(id: Long): Boolean = useConn { conn ->
        conn.prepareStatement("UPDATE \"$itemsTable\" SET is_deleted = 0, deleted_at = NULL WHERE id = ?").use { stmt ->
            stmt.setLong(1, id)
            stmt.executeUpdate() > 0
        }
    }

    override fun permanentDeleteItem(id: Long): Boolean = useConn { conn ->
        conn.prepareStatement("DELETE FROM \"$itemsTable\" WHERE id = ?").use { stmt ->
            stmt.setLong(1, id)
            stmt.executeUpdate() > 0
        }
    }

    override fun purgeExpiredItems(days: Int): Int = useConn { conn ->
        val threshold = (System.currentTimeMillis() / 1000) - (days * 86400L)
        conn.prepareStatement("DELETE FROM \"$itemsTable\" WHERE is_deleted = 1 AND deleted_at < ?").use { stmt ->
            stmt.setLong(1, threshold)
            stmt.executeUpdate()
        }
    }

    // ── Invoices ─────────────────────────────────────────────────────────────
    override fun loadAllInvoices(): List<InvoiceModel> = useConn { conn ->
        val list = mutableListOf<InvoiceModel>()
        conn.createStatement().use { stmt ->
            stmt.executeQuery("SELECT * FROM \"$invoicesTable\" WHERE is_deleted = 0 ORDER BY invoice_date DESC, id DESC").use { rs ->
                while (rs.next()) list.add(rsToInvoice(rs))
            }
        }
        list
    }

    override fun saveInvoice(invoice: InvoiceModel): Long = useConn { conn ->
        val isUpdate = invoice.id != null && invoice.id > 0
        val sql = if (isFactory) {
            if (isUpdate) {
                "UPDATE \"$invoicesTable\" SET company_id=?, invoice_number=?, fin_year=?, vanakkam=?, invoice_type=?, customer_id=?, customer_name=?, customer_address=?, invoice_date=?, items_json=?, total_amount=?, discount=?, global_discount_value=?, global_discount_type=?, global_discount_amount=?, tax_amount=?, tax_data_json=?, total_weight=?, wastage_grams=?, postal_charges=?, ahimsa_silk_amount=?, other_taxes_json=?, custom_preferences_json=?, terms_and_conditions=?, notes=?, bank_details_json=?, updated_at=?, is_deleted=0 WHERE id=?"
            } else {
                "INSERT INTO \"$invoicesTable\" (company_id, invoice_number, fin_year, vanakkam, invoice_type, customer_id, customer_name, customer_address, invoice_date, items_json, total_amount, discount, global_discount_value, global_discount_type, global_discount_amount, tax_amount, tax_data_json, total_weight, wastage_grams, postal_charges, ahimsa_silk_amount, other_taxes_json, custom_preferences_json, terms_and_conditions, notes, bank_details_json, created_at, updated_at, is_deleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)"
            }
        } else {
            if (isUpdate) {
                "UPDATE \"$invoicesTable\" SET company_id=?, invoice_number=?, fin_year=?, vanakkam=?, invoice_type=?, customer_id=?, customer_name=?, customer_address=?, invoice_date=?, items_json=?, total_amount=?, discount=?, global_discount_value=?, global_discount_type=?, global_discount_amount=?, tax_amount=?, tax_data_json=?, custom_preferences_json=?, terms_and_conditions=?, notes=?, updated_at=?, is_deleted=0 WHERE id=?"
            } else {
                "INSERT INTO \"$invoicesTable\" (company_id, invoice_number, fin_year, vanakkam, invoice_type, customer_id, customer_name, customer_address, invoice_date, items_json, total_amount, discount, global_discount_value, global_discount_type, global_discount_amount, tax_amount, tax_data_json, custom_preferences_json, terms_and_conditions, notes, created_at, updated_at, is_deleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)"
            }
        }

        conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { stmt ->
            if (invoice.companyId != null) stmt.setLong(1, invoice.companyId) else stmt.setNull(1, Types.INTEGER)
            stmt.setString(2, invoice.invoiceNumber)
            stmt.setInt(3, invoice.finYear)
            stmt.setInt(4, invoice.vanakkam)
            stmt.setString(5, invoice.invoiceType)
            if (invoice.customerId != null) stmt.setLong(6, invoice.customerId) else stmt.setNull(6, Types.INTEGER)
            stmt.setString(7, LanguageJsonConverter.stringify(invoice.customerName))
            stmt.setString(8, LanguageJsonConverter.stringify(invoice.customerAddress))
            stmt.setLong(9, invoice.invoiceDate)
            stmt.setString(10, invoice.itemsJson.toString())
            stmt.setDouble(11, invoice.totalAmount)
            stmt.setDouble(12, invoice.discount)
            stmt.setDouble(13, invoice.globalDiscountValue)
            stmt.setString(14, invoice.globalDiscountType)
            stmt.setDouble(15, invoice.globalDiscountAmount)
            stmt.setDouble(16, invoice.taxAmount)
            stmt.setString(17, invoice.taxDataJson.toString())

            val now = System.currentTimeMillis() / 1000
            if (isFactory) {
                stmt.setDouble(18, invoice.totalWeight)
                stmt.setDouble(19, invoice.wastageGrams)
                stmt.setDouble(20, invoice.postalCharges)
                stmt.setDouble(21, invoice.ahimsaSilkAmount)
                stmt.setString(22, invoice.otherTaxesJson.toString())
                stmt.setString(23, invoice.customPreferencesJson)
                stmt.setString(24, invoice.termsAndConditions)
                stmt.setString(25, invoice.notes)
                stmt.setString(26, invoice.bankDetailsJson.toString())
                if (isUpdate) {
                    stmt.setLong(27, now)
                    stmt.setLong(28, invoice.id!!)
                    stmt.executeUpdate()
                    invoice.id!!
                } else {
                    stmt.setLong(27, now)
                    stmt.setLong(28, now)
                    stmt.executeUpdate()
                    stmt.generatedKeys.use { gk ->
                        if (gk.next()) gk.getLong(1) else -1L
                    }
                }
            } else {
                stmt.setString(18, invoice.customPreferencesJson)
                stmt.setString(19, invoice.termsAndConditions)
                stmt.setString(20, invoice.notes)
                if (isUpdate) {
                    stmt.setLong(21, now)
                    stmt.setLong(22, invoice.id!!)
                    stmt.executeUpdate()
                    invoice.id!!
                } else {
                    stmt.setLong(21, now)
                    stmt.setLong(22, now)
                    stmt.executeUpdate()
                    stmt.generatedKeys.use { gk ->
                        if (gk.next()) gk.getLong(1) else -1L
                    }
                }
            }
        }
    }

    override fun deleteInvoice(id: Long): Boolean = useConn { conn ->
        conn.prepareStatement("UPDATE \"$invoicesTable\" SET is_deleted = 1, deleted_at = ? WHERE id = ?").use { stmt ->
            stmt.setLong(1, System.currentTimeMillis() / 1000)
            stmt.setLong(2, id)
            stmt.executeUpdate() > 0
        }
    }

    override fun loadDeletedInvoices(): List<InvoiceModel> = useConn { conn ->
        val list = mutableListOf<InvoiceModel>()
        conn.createStatement().use { stmt ->
            stmt.executeQuery("SELECT * FROM \"$invoicesTable\" WHERE is_deleted = 1 ORDER BY deleted_at DESC").use { rs ->
                while (rs.next()) list.add(rsToInvoice(rs))
            }
        }
        list
    }

    override fun restoreInvoice(id: Long): Boolean = useConn { conn ->
        conn.prepareStatement("UPDATE \"$invoicesTable\" SET is_deleted = 0, deleted_at = NULL WHERE id = ?").use { stmt ->
            stmt.setLong(1, id)
            stmt.executeUpdate() > 0
        }
    }

    override fun permanentDeleteInvoice(id: Long): Boolean = useConn { conn ->
        conn.prepareStatement("DELETE FROM \"$invoicesTable\" WHERE id = ?").use { stmt ->
            stmt.setLong(1, id)
            stmt.executeUpdate() > 0
        }
    }

    override fun purgeExpiredInvoices(days: Int): Int = useConn { conn ->
        val threshold = (System.currentTimeMillis() / 1000) - (days * 86400L)
        conn.prepareStatement("DELETE FROM \"$invoicesTable\" WHERE is_deleted = 1 AND deleted_at < ?").use { stmt ->
            stmt.setLong(1, threshold)
            stmt.executeUpdate()
        }
    }

    // ── Receipts ─────────────────────────────────────────────────────────────
    override fun loadAllReceipts(): List<ReceiptModel> = useConn { conn ->
        val list = mutableListOf<ReceiptModel>()
        conn.createStatement().use { stmt ->
            stmt.executeQuery("SELECT * FROM \"$receiptsTable\" WHERE is_deleted = 0 ORDER BY receipt_date DESC, id DESC").use { rs ->
                while (rs.next()) list.add(rsToReceipt(rs))
            }
        }
        list
    }

    private fun saveReceiptInternal(conn: Connection, receipt: ReceiptModel): Long {
        val isUpdate = receipt.id != null && receipt.id > 0
        val sql = if (isUpdate) {
            "UPDATE \"$receiptsTable\" SET company_id=?, receipt_number=?, fin_year=?, vanakkam=?, customer_id=?, customer_name=?, customer_address=?, receipt_date=?, amount=?, payment_mode=?, bank_name=?, transaction_number=?, notes=?, updated_at=?, is_deleted=0 WHERE id=?"
        } else {
            "INSERT INTO \"$receiptsTable\" (company_id, receipt_number, fin_year, vanakkam, customer_id, customer_name, customer_address, receipt_date, amount, payment_mode, bank_name, transaction_number, notes, created_at, updated_at, is_deleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)"
        }

        return conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { stmt ->
            if (receipt.companyId != null) stmt.setLong(1, receipt.companyId) else stmt.setNull(1, Types.INTEGER)
            stmt.setString(2, receipt.receiptNumber)
            stmt.setString(3, receipt.finYear)
            stmt.setInt(4, receipt.vanakkam)
            if (receipt.customerId != null) stmt.setLong(5, receipt.customerId) else stmt.setNull(5, Types.INTEGER)
            stmt.setString(6, LanguageJsonConverter.stringify(receipt.customerName))
            stmt.setString(7, LanguageJsonConverter.stringify(receipt.customerAddress))
            stmt.setLong(8, receipt.receiptDate)
            stmt.setDouble(9, receipt.amount)
            stmt.setString(10, receipt.paymentMode)
            if (receipt.bankName != null) stmt.setString(11, receipt.bankName) else stmt.setNull(11, Types.VARCHAR)
            if (receipt.transactionNumber != null) stmt.setString(12, receipt.transactionNumber) else stmt.setNull(12, Types.VARCHAR)
            stmt.setString(13, receipt.notes)

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
                stmt.generatedKeys.use { gk ->
                    if (gk.next()) gk.getLong(1) else -1L
                }
            }
        }
    }

    override fun saveReceipt(receipt: ReceiptModel): Long = useConn { conn ->
        saveReceiptInternal(conn, receipt)
    }

    override fun deleteReceipt(id: Long): Boolean = useConn { conn ->
        conn.prepareStatement("UPDATE \"$receiptsTable\" SET is_deleted = 1, deleted_at = ? WHERE id = ?").use { stmt ->
            stmt.setLong(1, System.currentTimeMillis() / 1000)
            stmt.setLong(2, id)
            stmt.executeUpdate() > 0
        }
    }

    override fun loadDeletedReceipts(): List<ReceiptModel> = useConn { conn ->
        val list = mutableListOf<ReceiptModel>()
        conn.createStatement().use { stmt ->
            stmt.executeQuery("SELECT * FROM \"$receiptsTable\" WHERE is_deleted = 1 ORDER BY deleted_at DESC").use { rs ->
                while (rs.next()) list.add(rsToReceipt(rs))
            }
        }
        list
    }

    override fun restoreReceipt(id: Long): Boolean = useConn { conn ->
        conn.prepareStatement("UPDATE \"$receiptsTable\" SET is_deleted = 0, deleted_at = NULL WHERE id = ?").use { stmt ->
            stmt.setLong(1, id)
            stmt.executeUpdate() > 0
        }
    }

    override fun permanentDeleteReceipt(id: Long): Boolean = useConn { conn ->
        conn.prepareStatement("DELETE FROM \"$linksTable\" WHERE receipt_id = ?").use { stmt ->
            stmt.setLong(1, id)
            stmt.executeUpdate()
        }
        conn.prepareStatement("DELETE FROM \"$receiptsTable\" WHERE id = ?").use { stmt ->
            stmt.setLong(1, id)
            stmt.executeUpdate() > 0
        }
    }

    override fun purgeExpiredReceipts(days: Int): Int = useConn { conn ->
        val threshold = (System.currentTimeMillis() / 1000) - (days * 86400L)
        conn.prepareStatement("DELETE FROM \"$receiptsTable\" WHERE is_deleted = 1 AND deleted_at < ?").use { stmt ->
            stmt.setLong(1, threshold)
            stmt.executeUpdate()
        }
    }

    // ── Junction Links ───────────────────────────────────────────────────────
    override fun getLinksForReceipt(receiptId: Long): List<ReceiptInvoiceLinkModel> = useConn { conn ->
        val list = mutableListOf<ReceiptInvoiceLinkModel>()
        conn.prepareStatement("SELECT * FROM \"$linksTable\" WHERE receipt_id = ?").use { stmt ->
            stmt.setLong(1, receiptId)
            stmt.executeQuery().use { rs ->
                while (rs.next()) {
                    list.add(ReceiptInvoiceLinkModel(
                        id = getLongCol(rs, "id"),
                        receiptId = getLongCol(rs, "receipt_id", "patru_id"),
                        invoiceId = getLongCol(rs, "invoice_id", "pattiyal_id"),
                        appliedAmount = getDoubleCol(rs, "applied_amount", "inaippu_thogai", "poruthiya_thogai")
                    ))
                }
            }
        }
        list
    }


    override fun saveReceiptWithLinks(receipt: ReceiptModel, links: List<ReceiptInvoiceLinkModel>): Long = useConn { conn ->
        val autoCommit = conn.autoCommit
        conn.autoCommit = false
        try {
            val rId = saveReceiptInternal(conn, receipt)
            if (rId > 0) {
                conn.prepareStatement("DELETE FROM \"$linksTable\" WHERE receipt_id = ?").use { delStmt ->
                    delStmt.setLong(1, rId)
                    delStmt.executeUpdate()
                }
                val now = System.currentTimeMillis() / 1000
                conn.prepareStatement("INSERT INTO \"$linksTable\" (receipt_id, invoice_id, applied_amount, created_at) VALUES (?, ?, ?, ?)").use { insStmt ->
                    for (link in links) {
                        insStmt.setLong(1, rId)
                        insStmt.setLong(2, link.invoiceId)
                        insStmt.setDouble(3, link.appliedAmount)
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
            conn.autoCommit = autoCommit
        }
    }

    override fun getPaidAmountForInvoice(invoiceId: Long): Double = useConn { conn ->
        var sum = 0.0
        val sql = "SELECT SUM(l.applied_amount) FROM \"$linksTable\" l INNER JOIN \"$receiptsTable\" r ON l.receipt_id = r.id WHERE l.invoice_id = ? AND r.is_deleted = 0"
        conn.prepareStatement(sql).use { stmt ->
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
        val sql = "SELECT l.invoice_id, SUM(l.applied_amount) FROM \"$linksTable\" l INNER JOIN \"$receiptsTable\" r ON l.receipt_id = r.id WHERE l.invoice_id IN ($placeholders) AND r.is_deleted = 0 GROUP BY l.invoice_id"
        conn.prepareStatement(sql).use { stmt ->
            invoiceIds.forEachIndexed { i, id ->
                stmt.setLong(i + 1, id)
            }
            stmt.executeQuery().use { rs ->
                while (rs.next()) {
                    result[rs.getLong(1)] = rs.getDouble(2)
                }
            }
        }
        result
    }

    // ── Business Profiles ────────────────────────────────────────────────────
    override fun loadProfile(profileId: Long?): BusinessProfile? = useConn { conn ->
        var p: BusinessProfile? = null
        val sql = if (profileId != null) {
            "SELECT * FROM \"$profilesTable\" WHERE id = ? AND is_deleted = 0 LIMIT 1"
        } else {
            "SELECT * FROM \"$profilesTable\" WHERE is_deleted = 0 ORDER BY id ASC LIMIT 1"
        }
        conn.prepareStatement(sql).use { stmt ->
            if (profileId != null) stmt.setLong(1, profileId)
            stmt.executeQuery().use { rs ->
                if (rs.next()) p = rsToProfile(rs)
            }
        }
        p
    }

    override fun loadAllProfiles(): List<BusinessProfile> = useConn { conn ->
        val list = mutableListOf<BusinessProfile>()
        conn.createStatement().use { stmt ->
            stmt.executeQuery("SELECT * FROM \"$profilesTable\" WHERE is_deleted = 0 ORDER BY id ASC").use { rs ->
                while (rs.next()) list.add(rsToProfile(rs))
            }
        }
        list
    }

    override fun saveProfile(profile: BusinessProfile): Boolean = useConn { conn ->
        val pId = profile.id
        val exists = if (pId != null) {
            conn.prepareStatement("SELECT id FROM \"$profilesTable\" WHERE id = ?").use { stmt ->
                stmt.setLong(1, pId)
                stmt.executeQuery().use { rs -> rs.next() }
            }
        } else false

        val sql = if (exists) {
            """
            UPDATE "$profilesTable" SET 
                primary_language=?, secondary_language=?, is_bilingual=?, split_gst=?, business_name=?, 
                short_name=?, phone1=?, phone2=?, email=?, gstin=?, 
                address=?, city=?, district=?, state=?, country=?, 
                pin_code=?, bank_name=?, branch=?, account_number=?, ifsc=?, 
                logo=?, wide_logo=?, header_layout=?, signature=?, 
                signatory_name=?, signatory_first_name=?, signatory_last_name=?, 
                tagline=?, upi_id=?, theme_color=?, print_settings=?, updated_at=?, is_deleted=0 
            WHERE id=?
            """.trimIndent()
        } else {
            """
            INSERT INTO "$profilesTable" (
                primary_language, secondary_language, is_bilingual, split_gst, business_name, 
                short_name, phone1, phone2, email, gstin, 
                address, city, district, state, country, 
                pin_code, bank_name, branch, account_number, ifsc, 
                logo, wide_logo, header_layout, signature, 
                signatory_name, signatory_first_name, signatory_last_name, 
                tagline, upi_id, theme_color, print_settings, created_at, updated_at, is_deleted
                ${if (pId != null) ", id" else ""}
            ) VALUES (
                ?, ?, ?, ?, ?, 
                ?, ?, ?, ?, ?, 
                ?, ?, ?, ?, ?, 
                ?, ?, ?, ?, ?, 
                ?, ?, ?, ?, 
                ?, ?, ?, 
                ?, ?, ?, ?, ?, ?, 0
                ${if (pId != null) ", ?" else ""}
            )
            """.trimIndent()
        }

        conn.prepareStatement(sql).use { stmt ->
            stmt.setString(1, profile.primaryLanguage)
            stmt.setString(2, profile.secondaryLanguage)
            stmt.setInt(3, if (profile.isBilingual) 1 else 0)
            stmt.setInt(4, if (profile.splitGst) 1 else 0)
            stmt.setString(5, LanguageJsonConverter.stringify(profile.businessName))
            stmt.setString(6, profile.shortName)
            stmt.setString(7, profile.phone1)
            stmt.setString(8, profile.phone2)
            stmt.setString(9, profile.email)
            stmt.setString(10, profile.gstin)
            stmt.setString(11, LanguageJsonConverter.stringify(profile.address))
            stmt.setString(12, LanguageJsonConverter.stringify(profile.city))
            stmt.setString(13, LanguageJsonConverter.stringify(profile.district))
            stmt.setString(14, LanguageJsonConverter.stringify(profile.state))
            stmt.setString(15, LanguageJsonConverter.stringify(profile.country))
            stmt.setString(16, profile.pinCode)
            stmt.setString(17, LanguageJsonConverter.stringify(profile.bankName))
            stmt.setString(18, LanguageJsonConverter.stringify(profile.branch))
            stmt.setString(19, profile.accountNumber)
            stmt.setString(20, profile.ifsc)
            stmt.setString(21, profile.logo)
            stmt.setString(22, profile.wideLogo)
            stmt.setString(23, profile.headerLayout)
            stmt.setString(24, profile.signature)
            stmt.setString(25, profile.signatoryName)
            stmt.setString(26, profile.signatoryFirstName)
            stmt.setString(27, profile.signatoryLastName)
            stmt.setString(28, LanguageJsonConverter.stringify(profile.tagline))
            stmt.setString(29, profile.upiId)
            stmt.setString(30, profile.themeColor)
            stmt.setString(31, profile.printSettingsJson)

            val now = System.currentTimeMillis() / 1000
            if (exists) {
                stmt.setLong(32, now)
                stmt.setLong(33, pId!!)
            } else {
                stmt.setLong(32, now)
                stmt.setLong(33, now)
                if (pId != null) {
                    stmt.setLong(34, pId)
                }
            }
            stmt.executeUpdate() > 0
        }
    }

    override fun deleteProfile(profileId: Long): Boolean = useConn { conn ->
        conn.prepareStatement("UPDATE \"$profilesTable\" SET is_deleted = 1, deleted_at = ? WHERE id = ?").use { stmt ->
            stmt.setLong(1, System.currentTimeMillis() / 1000)
            stmt.setLong(2, profileId)
            stmt.executeUpdate() > 0
        }
    }

    override fun clearProfiles(): Boolean = useConn { conn ->
        conn.createStatement().use { stmt ->
            stmt.execute("DELETE FROM \"$profilesTable\"")
            true
        }
    }

    override fun clearAllData(): Boolean = useConn { conn ->
        conn.createStatement().use { stmt ->
            stmt.execute("DELETE FROM \"$linksTable\"")
            stmt.execute("DELETE FROM \"$receiptsTable\"")
            stmt.execute("DELETE FROM \"$invoicesTable\"")
            stmt.execute("DELETE FROM \"$itemsTable\"")
            stmt.execute("DELETE FROM \"$customersTable\"")
            stmt.execute("DELETE FROM \"$profilesTable\"")
            true
        }
    }

    // ── Helper ResultSet Parsers ─────────────────────────────────────────────
    private fun rsToMerchant(rs: ResultSet): CustomerModel = CustomerModel(
        id = getLongCol(rs, "id"),
        name = LanguageJsonConverter.parse(getStringCol(rs, "name", "peyar")),
        address = LanguageJsonConverter.parse(getStringCol(rs, "address", "mugavari")),
        city = LanguageJsonConverter.parse(getStringCol(rs, "city", "oor")),
        district = LanguageJsonConverter.parse(getStringCol(rs, "district", "maavattam")),
        state = LanguageJsonConverter.parse(getStringCol(rs, "state", "maanilam")),
        country = LanguageJsonConverter.parse(getStringCol(rs, "country", "naadu", default = "{\"en\":\"India\",\"ta\":\"இந்தியா\"}")),
        internationalAddress = LanguageJsonConverter.parse(getStringCol(rs, "international_address", "velinaad_mugavari")),
        pinCode = getStringCol(rs, "pin_code", "anjal_kuriyeedu"),
        gstin = getStringCol(rs, "gstin"),
        email = getStringCol(rs, "email", "minnanjal"),
        phone = getStringCol(rs, "phone", "tholaipaesi"),
        companyIds = ItemJsonConverter.parseLongList(getStringCol(rs, "company_ids", "niruvanam_ids", default = "[]")),
        createdAt = getLongCol(rs, "created_at"),
        updatedAt = getLongCol(rs, "updated_at"),
        isDeleted = getIntCol(rs, "is_deleted") == 1,
        deletedAt = getLongOrNull(rs, "deleted_at")
    )

    private fun rsToItem(rs: ResultSet): ItemModel = ItemModel(
        id = getLongCol(rs, "id"),
        name = LanguageJsonConverter.parse(getStringCol(rs, "name", "porul_peyar")),
        hsnCode = getStringCol(rs, "hsn_code"),
        price = getDoubleCol(rs, "price", "vilai"),
        taxRate = getDoubleCol(rs, "tax_rate", "vari_veetham"),
        quantityType = getStringCol(rs, "quantity_type", "alavu_vagai", default = "quantity"),
        unit = getStringCol(rs, "unit", "alagu", default = "Nos"),
        companyIds = ItemJsonConverter.parseLongList(getStringCol(rs, "company_ids", "niruvanam_ids", default = "[]")),
        companyPrices = ItemJsonConverter.parseCompanyPrices(getStringCol(rs, "company_prices", default = "{}")),
        createdAt = getLongCol(rs, "created_at"),
        updatedAt = getLongCol(rs, "updated_at"),
        isDeleted = getIntCol(rs, "is_deleted") == 1,
        deletedAt = getLongOrNull(rs, "deleted_at")
    )

    private fun rsToInvoice(rs: ResultSet): InvoiceModel {
        val totalWeight = if (isFactory) getDoubleCol(rs, "total_weight", "motha_edai") else 0.0
        val wastageGrams = if (isFactory) getDoubleCol(rs, "wastage_grams", "setharam_grams") else 0.0
        val postalCharges = if (isFactory) getDoubleCol(rs, "postal_charges", "thabaal_thogai") else 0.0
        val ahimsaSilkAmount = if (isFactory) getDoubleCol(rs, "ahimsa_silk_amount", "ahimsa_pattu_thogai") else 0.0
        val otherTaxesJson = if (isFactory) getStringCol(rs, "other_taxes_json", "piravari_vugal", default = "[]") else "[]"
        val bankDetailsJson = if (isFactory) getStringCol(rs, "bank_details_json", "vangi_tharavugal", default = "{}") else "{}"

        return InvoiceModel(
            id = getLongCol(rs, "id"),
            companyId = getLongOrNull(rs, "company_id", "niruvanam_id"),
            invoiceNumber = getStringCol(rs, "invoice_number", "patrucheettu_en"),
            finYear = getIntCol(rs, "fin_year"),
            vanakkam = getIntCol(rs, "vanakkam", default = 1),
            invoiceType = getStringCol(rs, "invoice_type", "pattiyal_vagai", default = "tax-invoice"),
            customerId = getLongOrNull(rs, "customer_id", "vaangunar_id"),
            customerName = LanguageJsonConverter.parse(getStringCol(rs, "customer_name", "vaangunar_peyar")),
            customerAddress = LanguageJsonConverter.parse(getStringCol(rs, "customer_address", "vaangunar_munvari")),
            invoiceDate = getLongCol(rs, "invoice_date", "pattiyal_naal"),
            itemsJson = getStringCol(rs, "items_json", "tharavugal", default = "[]"),
            totalAmount = getDoubleCol(rs, "total_amount", "motha_thogai"),
            discount = getDoubleCol(rs, "discount", "thallupadi"),
            globalDiscountValue = getDoubleCol(rs, "global_discount_value", "podhu_thallupadi_mathippu"),
            globalDiscountType = getStringCol(rs, "global_discount_type", "podhu_thallupadi_vagai", default = "%"),
            globalDiscountAmount = getDoubleCol(rs, "global_discount_amount", "podhu_thallupadi_thogai"),
            taxAmount = getDoubleCol(rs, "tax_amount", "vari_thogai"),
            taxDataJson = getStringCol(rs, "tax_data_json", "vari_tharavugal", default = "{}"),
            totalWeight = totalWeight,
            wastageGrams = wastageGrams,
            postalCharges = postalCharges,
            ahimsaSilkAmount = ahimsaSilkAmount,
            otherTaxesJson = otherTaxesJson,
            customPreferencesJson = getStringCol(rs, "custom_preferences_json", "sontha_viruppangal", default = "{}"),
            termsAndConditions = getStringCol(rs, "terms_and_conditions", "nibandhanaigal"),
            notes = getStringCol(rs, "notes", "ullkurippu"),
            bankDetailsJson = bankDetailsJson,
            createdAt = getLongCol(rs, "created_at"),
            updatedAt = getLongCol(rs, "updated_at"),
            isDeleted = getIntCol(rs, "is_deleted") == 1,
            deletedAt = getLongOrNull(rs, "deleted_at")
        )
    }

    private fun rsToReceipt(rs: ResultSet): ReceiptModel = ReceiptModel(
        id = getLongCol(rs, "id"),
        companyId = getLongOrNull(rs, "company_id", "niruvanam_id"),
        receiptNumber = getStringCol(rs, "receipt_number", "patru_en"),
        finYear = getStringCol(rs, "fin_year"),
        vanakkam = getIntCol(rs, "vanakkam", default = 1),
        customerId = getLongOrNull(rs, "customer_id", "vaangunar_id"),
        customerName = LanguageJsonConverter.parse(getStringCol(rs, "customer_name", "vaangunar_peyar")),
        customerAddress = LanguageJsonConverter.parse(getStringCol(rs, "customer_address", "vaangunar_munvari")),
        receiptDate = getLongCol(rs, "receipt_date", "patru_naal"),
        amount = getDoubleCol(rs, "amount", "thogai"),
        paymentMode = getStringCol(rs, "payment_mode", "seluthum_murai", default = "cash"),
        bankName = getStringOrNull(rs, "bank_name", "vangi_peyar"),
        transactionNumber = getStringOrNull(rs, "transaction_number", "parivarthanai_en"),
        notes = getStringCol(rs, "notes", "ullkurippu"),
        createdAt = getLongCol(rs, "created_at"),
        updatedAt = getLongCol(rs, "updated_at"),
        isDeleted = getIntCol(rs, "is_deleted") == 1,
        deletedAt = getLongOrNull(rs, "deleted_at")
    )

    private fun rsToProfile(rs: ResultSet): BusinessProfile = BusinessProfile(
        id = getLongCol(rs, "id"),
        primaryLanguage = getStringCol(rs, "primary_language", "mudhan_mozhi", default = "ta"),
        secondaryLanguage = getStringCol(rs, "secondary_language", "thunai_mozhi", default = "en"),
        isBilingual = getIntCol(rs, "is_bilingual", "iru_mozhi", default = 1) == 1,
        splitGst = getIntCol(rs, "split_gst", "gst_pirippugal") == 1,
        businessName = LanguageJsonConverter.parse(getStringCol(rs, "business_name", "niruvanathin_peyar")),
        shortName = getStringCol(rs, "short_name", "kurum_peyar"),
        phone1 = getStringCol(rs, "phone1", "tholaipaesi1"),
        phone2 = getStringCol(rs, "phone2", "tholaipaesi2"),
        email = getStringCol(rs, "email", "minnanjal"),
        gstin = getStringCol(rs, "gstin"),
        address = LanguageJsonConverter.parse(getStringCol(rs, "address", "mugavari")),
        city = LanguageJsonConverter.parse(getStringCol(rs, "city", "oor")),
        district = LanguageJsonConverter.parse(getStringCol(rs, "district", "maavattam")),
        state = LanguageJsonConverter.parse(getStringCol(rs, "state", "maanilam")),
        country = LanguageJsonConverter.parse(getStringCol(rs, "country", "naadu", default = "{\"en\":\"India\",\"ta\":\"இந்தியா\"}")),
        pinCode = getStringCol(rs, "pin_code", "anjal_kuriyeedu"),
        bankName = LanguageJsonConverter.parse(getStringCol(rs, "bank_name", "vangi_peyar")),
        branch = LanguageJsonConverter.parse(getStringCol(rs, "branch", "kilai")),
        accountNumber = getStringCol(rs, "account_number", "vangi_kanakku"),
        ifsc = getStringCol(rs, "ifsc"),
        logo = getStringCol(rs, "logo", "oavuru"),
        wideLogo = getStringCol(rs, "wide_logo", "agala_oavuru"),
        headerLayout = getStringCol(rs, "header_layout", "thalaippu_vadivu", default = "small"),
        signature = getStringCol(rs, "signature", "kaiyoppam"),
        signatoryName = getStringCol(rs, "signatory_name", "oppam_peyar"),
        signatoryFirstName = getStringCol(rs, "signatory_first_name", "oppam_mudhal_peyar"),
        signatoryLastName = getStringCol(rs, "signatory_last_name", "oppam_irudhi_peyar"),
        tagline = LanguageJsonConverter.parse(getStringCol(rs, "tagline", "adaimozhi")),
        upiId = getStringCol(rs, "upi_id"),
        themeColor = getStringCol(rs, "theme_color", "thoatra_niram", "thoatraNiram"),
        printSettingsJson = getStringCol(rs, "print_settings", default = "{}")
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
