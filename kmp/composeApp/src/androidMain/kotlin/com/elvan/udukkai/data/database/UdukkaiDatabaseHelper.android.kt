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
import com.elvan.udukkai.data.model.ProductJsonConverter
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

    private val customersTable = "${tablePrefix}customers"
    private val productsTable = "${tablePrefix}products"
    private val invoicesTable = "${tablePrefix}invoices"
    private val receiptsTable = "${tablePrefix}receipts"
    private val linksTable = "${tablePrefix}receipt_invoice_links"
    private val profilesTable = "${tablePrefix}business_profiles"

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

    private fun renameColumnIfExists(db: SQLiteDatabase, tableName: String, oldCol: String, newCol: String) {
        try {
            val existingCols = mutableSetOf<String>()
            db.rawQuery("PRAGMA table_info(\"$tableName\")", null).use { c ->
                val nameIdx = c.getColumnIndex("name")
                if (nameIdx >= 0) {
                    while (c.moveToNext()) {
                        existingCols.add(c.getString(nameIdx).lowercase())
                    }
                }
            }
            if (existingCols.contains(oldCol.lowercase()) && !existingCols.contains(newCol.lowercase())) {
                Log.i(tag, "Renaming legacy column $tableName.$oldCol -> $newCol")
                db.execSQL("ALTER TABLE \"$tableName\" RENAME COLUMN \"$oldCol\" TO \"$newCol\"")
            }
        } catch (e: Exception) {
            Log.e(tag, "Column rename $tableName.$oldCol -> $newCol error: ${e.message}")
        }
    }

    private fun ensureTables(db: SQLiteDatabase) {
        val legacyPrefix = if (isFactory) "kooli_" else "pattu_"

        // 1. Rename any legacy / Tanglish tables to standardized English tables
        renameTableIfExists(db, "${legacyPrefix}vaangunar_table", customersTable)
        renameTableIfExists(db, "${tablePrefix}vaangunar_table", customersTable)

        renameTableIfExists(db, "${legacyPrefix}porul_table", productsTable)
        renameTableIfExists(db, "${tablePrefix}porul_table", productsTable)

        renameTableIfExists(db, "${legacyPrefix}pattiyal_table", invoicesTable)
        renameTableIfExists(db, "${tablePrefix}pattiyal_table", invoicesTable)

        renameTableIfExists(db, "${legacyPrefix}patrugal_table", receiptsTable)
        renameTableIfExists(db, "${tablePrefix}patrugal_table", receiptsTable)

        renameTableIfExists(db, "${legacyPrefix}patru_pattiyal_table", linksTable)
        renameTableIfExists(db, "${tablePrefix}patru_pattiyal_table", linksTable)

        renameTableIfExists(db, "${legacyPrefix}niruvana_tharavugal_table", profilesTable)
        renameTableIfExists(db, "${tablePrefix}niruvana_tharavugal_table", profilesTable)

        // 2. Rename legacy columns in customersTable
        renameColumnIfExists(db, customersTable, "peyar", "name")
        renameColumnIfExists(db, customersTable, "mugavari", "address")
        renameColumnIfExists(db, customersTable, "oor", "city")
        renameColumnIfExists(db, customersTable, "maavattam", "district")
        renameColumnIfExists(db, customersTable, "maanilam", "state")
        renameColumnIfExists(db, customersTable, "naadu", "country")
        renameColumnIfExists(db, customersTable, "velinaad_mugavari", "international_address")
        renameColumnIfExists(db, customersTable, "anjal_kuriyeedu", "pin_code")
        renameColumnIfExists(db, customersTable, "minnanjal", "email")
        renameColumnIfExists(db, customersTable, "tholaipaesi", "phone")

        // 3. Rename legacy columns in productsTable
        renameColumnIfExists(db, productsTable, "porul_peyar", "name")
        renameColumnIfExists(db, productsTable, "vilai", "price")
        renameColumnIfExists(db, productsTable, "vari_veetham", "tax_rate")
        renameColumnIfExists(db, productsTable, "alavu_vagai", "quantity_type")
        renameColumnIfExists(db, productsTable, "alagu", "unit")
        renameColumnIfExists(db, productsTable, "niruvanam_ids", "company_ids")

        // 4. Rename legacy columns in invoicesTable
        renameColumnIfExists(db, invoicesTable, "niruvanam_id", "company_id")
        renameColumnIfExists(db, invoicesTable, "patrucheettu_en", "invoice_number")
        renameColumnIfExists(db, invoicesTable, "pattiyal_vagai", "invoice_type")
        renameColumnIfExists(db, invoicesTable, "vaangunar_id", "customer_id")
        renameColumnIfExists(db, invoicesTable, "vaangunar_peyar", "customer_name")
        renameColumnIfExists(db, invoicesTable, "vaangunar_munvari", "customer_address")
        renameColumnIfExists(db, invoicesTable, "pattiyal_naal", "invoice_date")
        renameColumnIfExists(db, invoicesTable, "tharavugal", "items_json")
        renameColumnIfExists(db, invoicesTable, "motha_thogai", "total_amount")
        renameColumnIfExists(db, invoicesTable, "thallupadi", "discount")
        renameColumnIfExists(db, invoicesTable, "podhu_thallupadi_mathippu", "global_discount_value")
        renameColumnIfExists(db, invoicesTable, "podhu_thallupadi_vagai", "global_discount_type")
        renameColumnIfExists(db, invoicesTable, "podhu_thallupadi_thogai", "global_discount_amount")
        renameColumnIfExists(db, invoicesTable, "vari_thogai", "tax_amount")
        renameColumnIfExists(db, invoicesTable, "vari_tharavugal", "tax_data_json")
        renameColumnIfExists(db, invoicesTable, "motha_edai", "total_weight")
        renameColumnIfExists(db, invoicesTable, "setharam_grams", "wastage_grams")
        renameColumnIfExists(db, invoicesTable, "thabaal_thogai", "postal_charges")
        renameColumnIfExists(db, invoicesTable, "ahimsa_pattu_thogai", "ahimsa_silk_amount")
        renameColumnIfExists(db, invoicesTable, "piravari_vugal", "other_taxes_json")
        renameColumnIfExists(db, invoicesTable, "sontha_viruppangal", "custom_preferences_json")
        renameColumnIfExists(db, invoicesTable, "nibandhanaigal", "terms_and_conditions")
        renameColumnIfExists(db, invoicesTable, "ullkurippu", "notes")
        renameColumnIfExists(db, invoicesTable, "vangi_tharavugal", "bank_details_json")

        // 5. Rename legacy columns in receiptsTable
        renameColumnIfExists(db, receiptsTable, "niruvanam_id", "company_id")
        renameColumnIfExists(db, receiptsTable, "patru_en", "receipt_number")
        renameColumnIfExists(db, receiptsTable, "vaangunar_id", "customer_id")
        renameColumnIfExists(db, receiptsTable, "vaangunar_peyar", "customer_name")
        renameColumnIfExists(db, receiptsTable, "vaangunar_munvari", "customer_address")
        renameColumnIfExists(db, receiptsTable, "patru_naal", "receipt_date")
        renameColumnIfExists(db, receiptsTable, "thogai", "amount")
        renameColumnIfExists(db, receiptsTable, "seluthum_murai", "payment_mode")
        renameColumnIfExists(db, receiptsTable, "vangi_peyar", "bank_name")
        renameColumnIfExists(db, receiptsTable, "parivarthanai_en", "transaction_number")
        renameColumnIfExists(db, receiptsTable, "ullkurippu", "notes")

        // 6. Rename legacy columns in linksTable
        renameColumnIfExists(db, linksTable, "patru_id", "receipt_id")
        renameColumnIfExists(db, linksTable, "pattiyal_id", "invoice_id")
        renameColumnIfExists(db, linksTable, "inaippu_thogai", "applied_amount")

        // 7. Rename legacy columns in profilesTable
        renameColumnIfExists(db, profilesTable, "mudhan_mozhi", "primary_language")
        renameColumnIfExists(db, profilesTable, "thunai_mozhi", "secondary_language")
        renameColumnIfExists(db, profilesTable, "iru_mozhi", "is_bilingual")
        renameColumnIfExists(db, profilesTable, "gst_pirippugal", "split_gst")
        renameColumnIfExists(db, profilesTable, "niruvanathin_peyar", "business_name")
        renameColumnIfExists(db, profilesTable, "kurum_peyar", "short_name")
        renameColumnIfExists(db, profilesTable, "tholaipaesi1", "phone1")
        renameColumnIfExists(db, profilesTable, "tholaipaesi2", "phone2")
        renameColumnIfExists(db, profilesTable, "minnanjal", "email")
        renameColumnIfExists(db, profilesTable, "mugavari", "address")
        renameColumnIfExists(db, profilesTable, "oor", "city")
        renameColumnIfExists(db, profilesTable, "maavattam", "district")
        renameColumnIfExists(db, profilesTable, "maanilam", "state")
        renameColumnIfExists(db, profilesTable, "naadu", "country")
        renameColumnIfExists(db, profilesTable, "anjal_kuriyeedu", "pin_code")
        renameColumnIfExists(db, profilesTable, "vangi_peyar", "bank_name")
        renameColumnIfExists(db, profilesTable, "kilai", "branch")
        renameColumnIfExists(db, profilesTable, "vangi_kanakku", "account_number")
        renameColumnIfExists(db, profilesTable, "oavuru", "logo")
        renameColumnIfExists(db, profilesTable, "agala_oavuru", "wide_logo")
        renameColumnIfExists(db, profilesTable, "thalaippu_vadivu", "header_layout")
        renameColumnIfExists(db, profilesTable, "kaiyoppam", "signature")
        renameColumnIfExists(db, profilesTable, "oppam_peyar", "signatory_name")
        renameColumnIfExists(db, profilesTable, "oppam_mudhal_peyar", "signatory_first_name")
        renameColumnIfExists(db, profilesTable, "oppam_irudhi_peyar", "signatory_last_name")
        renameColumnIfExists(db, profilesTable, "adaimozhi", "tagline")
        renameColumnIfExists(db, profilesTable, "thoatra_niram", "theme_color")

        // 1. Customers Table
        db.execSQL("""
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

        // 2. Products Table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS "$productsTable" (
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
        db.execSQL(createInvoiceSql)

        // 4. Receipts Table
        db.execSQL("""
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
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS "$linksTable" (
                "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                "receipt_id" INTEGER NOT NULL,
                "invoice_id" INTEGER NOT NULL,
                "applied_amount" REAL NOT NULL DEFAULT 0.0,
                "created_at" INTEGER NOT NULL DEFAULT (CAST(strftime('%s', CURRENT_TIMESTAMP) AS INTEGER)),
                FOREIGN KEY("receipt_id") REFERENCES "$receiptsTable"("id") ON DELETE CASCADE,
                FOREIGN KEY("invoice_id") REFERENCES "$invoicesTable"("id") ON DELETE CASCADE
            )
        """.trimIndent())

        try { db.execSQL("ALTER TABLE \"$customersTable\" ADD COLUMN \"company_ids\" TEXT NOT NULL DEFAULT '[]'") } catch (_: Exception) {}

        // 6. Profiles Table
        db.execSQL("""
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

    // ── Safe Cursor Parsing Helpers ──────────────────────────────────────────
    private fun getColIdx(c: Cursor, vararg names: String): Int {
        for (name in names) {
            val idx = c.getColumnIndex(name)
            if (idx >= 0) return idx
        }
        return -1
    }

    private fun getStringCol(c: Cursor, vararg names: String, default: String = ""): String {
        val idx = getColIdx(c, *names)
        if (idx < 0 || c.isNull(idx)) return default
        return c.getString(idx)
    }

    private fun getDoubleCol(c: Cursor, vararg names: String, default: Double = 0.0): Double {
        val idx = getColIdx(c, *names)
        if (idx < 0 || c.isNull(idx)) return default
        return c.getDouble(idx)
    }

    private fun getLongCol(c: Cursor, vararg names: String, default: Long = 0L): Long {
        val idx = getColIdx(c, *names)
        if (idx < 0 || c.isNull(idx)) return default
        return c.getLong(idx)
    }

    private fun getLongOrNull(c: Cursor, vararg names: String): Long? {
        val idx = getColIdx(c, *names)
        if (idx < 0 || c.isNull(idx)) return null
        return c.getLong(idx)
    }

    private fun getStringOrNull(c: Cursor, vararg names: String): String? {
        val idx = getColIdx(c, *names)
        if (idx < 0 || c.isNull(idx)) return null
        return c.getString(idx)
    }

    private fun getIntCol(c: Cursor, vararg names: String, default: Int = 0): Int {
        val idx = getColIdx(c, *names)
        if (idx < 0 || c.isNull(idx)) return default
        return c.getInt(idx)
    }

    // ── Customers ────────────────────────────────────────────────────────────
    override fun loadAllCustomers(): List<CustomerModel> = useDb { db ->
        val list = mutableListOf<CustomerModel>()
        db.rawQuery("SELECT * FROM \"$customersTable\" WHERE is_deleted = 0 ORDER BY id DESC", null).use { c ->
            while (c.moveToNext()) list.add(cursorToMerchant(c))
        }
        list
    } ?: emptyList()

    override fun saveCustomer(customer: CustomerModel): Long = useDb { db ->
        val cv = ContentValues().apply {
            put("name", LanguageJsonConverter.stringify(customer.name))
            put("address", LanguageJsonConverter.stringify(customer.address))
            put("city", LanguageJsonConverter.stringify(customer.city))
            put("district", LanguageJsonConverter.stringify(customer.district))
            put("state", LanguageJsonConverter.stringify(customer.state))
            put("country", LanguageJsonConverter.stringify(customer.country))
            put("international_address", LanguageJsonConverter.stringify(customer.internationalAddress))
            put("pin_code", customer.pinCode)
            put("gstin", customer.gstin)
            put("email", customer.email)
            put("phone", customer.phone)
            put("company_ids", customer.companyIds.toString())
            put("updated_at", System.currentTimeMillis() / 1000)
            put("is_deleted", 0)
        }
        if (customer.id != null && customer.id > 0) {
            db.update("\"$customersTable\"", cv, "id = ?", arrayOf(customer.id.toString()))
            customer.id
        } else {
            cv.put("created_at", System.currentTimeMillis() / 1000)
            db.insert("\"$customersTable\"", null, cv)
        }
    } ?: -1L

    override fun deleteCustomer(id: Long): Boolean = useDb { db ->
        val cv = ContentValues().apply {
            put("is_deleted", 1)
            put("deleted_at", System.currentTimeMillis() / 1000)
        }
        db.update("\"$customersTable\"", cv, "id = ?", arrayOf(id.toString())) > 0
    } ?: false

    override fun loadDeletedCustomers(): List<CustomerModel> = useDb { db ->
        val list = mutableListOf<CustomerModel>()
        db.rawQuery("SELECT * FROM \"$customersTable\" WHERE is_deleted = 1 ORDER BY deleted_at DESC", null).use { c ->
            while (c.moveToNext()) list.add(cursorToMerchant(c))
        }
        list
    } ?: emptyList()

    override fun restoreCustomer(id: Long): Boolean = useDb { db ->
        val cv = ContentValues().apply {
            put("is_deleted", 0)
            putNull("deleted_at")
        }
        db.update("\"$customersTable\"", cv, "id = ?", arrayOf(id.toString())) > 0
    } ?: false

    override fun permanentDeleteCustomer(id: Long): Boolean = useDb { db ->
        db.delete("\"$customersTable\"", "id = ?", arrayOf(id.toString())) > 0
    } ?: false

    override fun purgeExpiredCustomers(days: Int): Int = useDb { db ->
        val threshold = (System.currentTimeMillis() / 1000) - (days * 86400L)
        db.delete("\"$customersTable\"", "is_deleted = 1 AND deleted_at < ?", arrayOf(threshold.toString()))
    } ?: 0

    // Backward-compatible delegates
    override fun loadAllMerchants(): List<CustomerModel> = loadAllCustomers()
    override fun saveMerchant(merchant: CustomerModel): Long = saveCustomer(merchant)
    override fun deleteMerchant(id: Long): Boolean = deleteCustomer(id)
    override fun loadDeletedMerchants(): List<CustomerModel> = loadDeletedCustomers()
    override fun restoreMerchant(id: Long): Boolean = restoreCustomer(id)
    override fun permanentDeleteMerchant(id: Long): Boolean = permanentDeleteCustomer(id)
    override fun purgeExpiredMerchants(days: Int): Int = purgeExpiredCustomers(days)

    // ── Products ─────────────────────────────────────────────────────────────
    override fun loadAllProducts(): List<ProductModel> = useDb { db ->
        val list = mutableListOf<ProductModel>()
        db.rawQuery("SELECT * FROM \"$productsTable\" WHERE is_deleted = 0 ORDER BY id DESC", null).use { c ->
            while (c.moveToNext()) list.add(cursorToItem(c))
        }
        list
    } ?: emptyList()

    override fun saveProduct(product: ProductModel): Long = useDb { db ->
        val cv = ContentValues().apply {
            put("name", LanguageJsonConverter.stringify(product.name))
            put("hsn_code", product.hsnCode)
            put("price", product.price)
            put("tax_rate", product.taxRate)
            put("quantity_type", product.quantityType)
            put("unit", product.unit)
            put("company_ids", product.companyIds.toString())
            put("company_prices", product.companyPrices.toString())
            put("updated_at", System.currentTimeMillis() / 1000)
            put("is_deleted", 0)
        }
        if (product.id != null && product.id > 0) {
            db.update("\"$productsTable\"", cv, "id = ?", arrayOf(product.id.toString()))
            product.id
        } else {
            cv.put("created_at", System.currentTimeMillis() / 1000)
            db.insert("\"$productsTable\"", null, cv)
        }
    } ?: -1L

    override fun deleteProduct(id: Long): Boolean = useDb { db ->
        val cv = ContentValues().apply {
            put("is_deleted", 1)
            put("deleted_at", System.currentTimeMillis() / 1000)
        }
        db.update("\"$productsTable\"", cv, "id = ?", arrayOf(id.toString())) > 0
    } ?: false

    override fun loadDeletedProducts(): List<ProductModel> = useDb { db ->
        val list = mutableListOf<ProductModel>()
        db.rawQuery("SELECT * FROM \"$productsTable\" WHERE is_deleted = 1 ORDER BY deleted_at DESC", null).use { c ->
            while (c.moveToNext()) list.add(cursorToItem(c))
        }
        list
    } ?: emptyList()

    override fun restoreProduct(id: Long): Boolean = useDb { db ->
        val cv = ContentValues().apply {
            put("is_deleted", 0)
            putNull("deleted_at")
        }
        db.update("\"$productsTable\"", cv, "id = ?", arrayOf(id.toString())) > 0
    } ?: false

    override fun permanentDeleteProduct(id: Long): Boolean = useDb { db ->
        db.delete("\"$productsTable\"", "id = ?", arrayOf(id.toString())) > 0
    } ?: false

    override fun purgeExpiredProducts(days: Int): Int = useDb { db ->
        val threshold = (System.currentTimeMillis() / 1000) - (days * 86400L)
        db.delete("\"$productsTable\"", "is_deleted = 1 AND deleted_at < ?", arrayOf(threshold.toString()))
    } ?: 0

    // Backward-compatible delegates
    override fun loadAllItems(): List<ProductModel> = loadAllProducts()
    override fun saveItem(item: ProductModel): Long = saveProduct(item)
    override fun deleteItem(id: Long): Boolean = deleteProduct(id)
    override fun loadDeletedItems(): List<ProductModel> = loadDeletedProducts()
    override fun restoreItem(id: Long): Boolean = restoreProduct(id)
    override fun permanentDeleteItem(id: Long): Boolean = permanentDeleteProduct(id)
    override fun purgeExpiredItems(days: Int): Int = purgeExpiredProducts(days)

    // ── Invoices ─────────────────────────────────────────────────────────────
    override fun loadAllInvoices(): List<InvoiceModel> = useDb { db ->
        val list = mutableListOf<InvoiceModel>()
        db.rawQuery("SELECT * FROM \"$invoicesTable\" WHERE is_deleted = 0 ORDER BY invoice_date DESC, id DESC", null).use { c ->
            while (c.moveToNext()) list.add(cursorToInvoice(c))
        }
        list
    } ?: emptyList()

    override fun saveInvoice(invoice: InvoiceModel): Long = useDb { db ->
        val cv = ContentValues().apply {
            put("company_id", invoice.companyId)
            put("invoice_number", invoice.invoiceNumber)
            put("fin_year", invoice.finYear)
            put("vanakkam", invoice.vanakkam)
            put("invoice_type", invoice.invoiceType)
            put("customer_id", invoice.customerId)
            put("customer_name", LanguageJsonConverter.stringify(invoice.customerName))
            put("customer_address", LanguageJsonConverter.stringify(invoice.customerAddress))
            put("invoice_date", invoice.invoiceDate)
            put("items_json", invoice.itemsJson.toString())
            put("total_amount", invoice.totalAmount)
            put("discount", invoice.discount)
            put("global_discount_value", invoice.globalDiscountValue)
            put("global_discount_type", invoice.globalDiscountType)
            put("global_discount_amount", invoice.globalDiscountAmount)
            put("tax_amount", invoice.taxAmount)
            put("tax_data_json", invoice.taxDataJson.toString())
            if (isFactory) {
                put("total_weight", invoice.totalWeight)
                put("wastage_grams", invoice.wastageGrams)
                put("postal_charges", invoice.postalCharges)
                put("ahimsa_silk_amount", invoice.ahimsaSilkAmount)
                put("other_taxes_json", invoice.otherTaxesJson.toString())
                put("bank_details_json", invoice.bankDetailsJson.toString())
            }
            put("custom_preferences_json", invoice.customPreferencesJson)
            put("terms_and_conditions", invoice.termsAndConditions)
            put("notes", invoice.notes)
            put("updated_at", System.currentTimeMillis() / 1000)
            put("is_deleted", 0)
        }
        if (invoice.id != null && invoice.id > 0) {
            db.update("\"$invoicesTable\"", cv, "id = ?", arrayOf(invoice.id.toString()))
            invoice.id
        } else {
            cv.put("created_at", System.currentTimeMillis() / 1000)
            db.insert("\"$invoicesTable\"", null, cv)
        }
    } ?: -1L

    override fun deleteInvoice(id: Long): Boolean = useDb { db ->
        val cv = ContentValues().apply {
            put("is_deleted", 1)
            put("deleted_at", System.currentTimeMillis() / 1000)
        }
        db.update("\"$invoicesTable\"", cv, "id = ?", arrayOf(id.toString())) > 0
    } ?: false

    override fun loadDeletedInvoices(): List<InvoiceModel> = useDb { db ->
        val list = mutableListOf<InvoiceModel>()
        db.rawQuery("SELECT * FROM \"$invoicesTable\" WHERE is_deleted = 1 ORDER BY deleted_at DESC", null).use { c ->
            while (c.moveToNext()) list.add(cursorToInvoice(c))
        }
        list
    } ?: emptyList()

    override fun restoreInvoice(id: Long): Boolean = useDb { db ->
        val cv = ContentValues().apply {
            put("is_deleted", 0)
            putNull("deleted_at")
        }
        db.update("\"$invoicesTable\"", cv, "id = ?", arrayOf(id.toString())) > 0
    } ?: false

    override fun permanentDeleteInvoice(id: Long): Boolean = useDb { db ->
        db.delete("\"$invoicesTable\"", "id = ?", arrayOf(id.toString())) > 0
    } ?: false

    override fun purgeExpiredInvoices(days: Int): Int = useDb { db ->
        val threshold = (System.currentTimeMillis() / 1000) - (days * 86400L)
        db.delete("\"$invoicesTable\"", "is_deleted = 1 AND deleted_at < ?", arrayOf(threshold.toString()))
    } ?: 0

    // ── Receipts ─────────────────────────────────────────────────────────────
    override fun loadAllReceipts(): List<ReceiptModel> = useDb { db ->
        val list = mutableListOf<ReceiptModel>()
        db.rawQuery("SELECT * FROM \"$receiptsTable\" WHERE is_deleted = 0 ORDER BY receipt_date DESC, id DESC", null).use { c ->
            while (c.moveToNext()) list.add(cursorToReceipt(c))
        }
        list
    } ?: emptyList()

    override fun saveReceipt(receipt: ReceiptModel): Long = useDb { db ->
        val cv = ContentValues().apply {
            put("company_id", receipt.companyId)
            put("receipt_number", receipt.receiptNumber)
            put("fin_year", receipt.finYear)
            put("vanakkam", receipt.vanakkam)
            put("customer_id", receipt.customerId)
            put("customer_name", LanguageJsonConverter.stringify(receipt.customerName))
            put("customer_address", LanguageJsonConverter.stringify(receipt.customerAddress))
            put("receipt_date", receipt.receiptDate)
            put("amount", receipt.amount)
            put("payment_mode", receipt.paymentMode)
            put("bank_name", receipt.bankName)
            put("transaction_number", receipt.transactionNumber)
            put("notes", receipt.notes)
            put("updated_at", System.currentTimeMillis() / 1000)
            put("is_deleted", 0)
        }
        if (receipt.id != null && receipt.id > 0) {
            db.update("\"$receiptsTable\"", cv, "id = ?", arrayOf(receipt.id.toString()))
            receipt.id
        } else {
            cv.put("created_at", System.currentTimeMillis() / 1000)
            db.insert("\"$receiptsTable\"", null, cv)
        }
    } ?: -1L

    override fun deleteReceipt(id: Long): Boolean = useDb { db ->
        val cv = ContentValues().apply {
            put("is_deleted", 1)
            put("deleted_at", System.currentTimeMillis() / 1000)
        }
        db.update("\"$receiptsTable\"", cv, "id = ?", arrayOf(id.toString())) > 0
    } ?: false

    override fun loadDeletedReceipts(): List<ReceiptModel> = useDb { db ->
        val list = mutableListOf<ReceiptModel>()
        db.rawQuery("SELECT * FROM \"$receiptsTable\" WHERE is_deleted = 1 ORDER BY deleted_at DESC", null).use { c ->
            while (c.moveToNext()) list.add(cursorToReceipt(c))
        }
        list
    } ?: emptyList()

    override fun restoreReceipt(id: Long): Boolean = useDb { db ->
        val cv = ContentValues().apply {
            put("is_deleted", 0)
            putNull("deleted_at")
        }
        db.update("\"$receiptsTable\"", cv, "id = ?", arrayOf(id.toString())) > 0
    } ?: false

    override fun permanentDeleteReceipt(id: Long): Boolean = useDb { db ->
        db.delete("\"$receiptsTable\"", "id = ?", arrayOf(id.toString())) > 0
    } ?: false

    override fun purgeExpiredReceipts(days: Int): Int = useDb { db ->
        val threshold = (System.currentTimeMillis() / 1000) - (days * 86400L)
        db.delete("\"$receiptsTable\"", "is_deleted = 1 AND deleted_at < ?", arrayOf(threshold.toString()))
    } ?: 0

    // ── Junction Links ───────────────────────────────────────────────────────
    override fun getLinksForReceipt(receiptId: Long): List<ReceiptInvoiceLinkModel> = useDb { db ->
        val list = mutableListOf<ReceiptInvoiceLinkModel>()
        db.rawQuery("SELECT * FROM \"$linksTable\" WHERE receipt_id = ?", arrayOf(receiptId.toString())).use { c ->
            while (c.moveToNext()) {
                list.add(ReceiptInvoiceLinkModel(
                    id = getLongCol(c, "id"),
                    receiptId = getLongCol(c, "receipt_id", "patru_id"),
                    invoiceId = getLongCol(c, "invoice_id", "pattiyal_id"),
                    appliedAmount = getDoubleCol(c, "applied_amount", "inaippu_thogai")
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
                db.delete("\"$linksTable\"", "receipt_id = ?", arrayOf(rId.toString()))
                for (link in links) {
                    val cv = ContentValues().apply {
                        put("receipt_id", rId)
                        put("invoice_id", link.invoiceId)
                        put("applied_amount", link.appliedAmount)
                        put("created_at", System.currentTimeMillis() / 1000)
                    }
                    db.insert("\"$linksTable\"", null, cv)
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
        db.rawQuery("SELECT SUM(applied_amount) FROM \"$linksTable\" WHERE invoice_id = ?", arrayOf(invoiceId.toString())).use { c ->
            if (c.moveToFirst() && !c.isNull(0)) sum = c.getDouble(0)
        }
        sum
    } ?: 0.0

    override fun getPaidAmountsForInvoices(invoiceIds: List<Long>): Map<Long, Double> = useDb { db ->
        val result = mutableMapOf<Long, Double>()
        if (invoiceIds.isEmpty()) return@useDb result
        val placeholders = invoiceIds.joinToString(",") { "?" }
        val args = invoiceIds.map { it.toString() }.toTypedArray()
        db.rawQuery("SELECT invoice_id, SUM(applied_amount) FROM \"$linksTable\" WHERE invoice_id IN ($placeholders) GROUP BY invoice_id", args).use { c ->
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
            "SELECT * FROM \"$profilesTable\" WHERE id = ? AND is_deleted = 0 LIMIT 1"
        } else {
            "SELECT * FROM \"$profilesTable\" WHERE is_deleted = 0 ORDER BY id ASC LIMIT 1"
        }
        val args = if (profileId != null) arrayOf(profileId.toString()) else null
        db.rawQuery(sql, args).use { c ->
            if (c.moveToFirst()) p = cursorToProfile(c)
        }
        p
    }

    override fun loadAllProfiles(): List<BusinessProfile> = useDb { db ->
        val list = mutableListOf<BusinessProfile>()
        db.rawQuery("SELECT * FROM \"$profilesTable\" WHERE is_deleted = 0 ORDER BY id ASC", null).use { c ->
            while (c.moveToNext()) list.add(cursorToProfile(c))
        }
        list
    } ?: emptyList()

    override fun saveProfile(profile: BusinessProfile): Boolean = useDb { db ->
        val cv = ContentValues().apply {
            put("primary_language", profile.primaryLanguage)
            put("secondary_language", profile.secondaryLanguage)
            put("is_bilingual", if (profile.isBilingual) 1 else 0)
            put("split_gst", if (profile.splitGst) 1 else 0)
            put("business_name", LanguageJsonConverter.stringify(profile.businessName))
            put("short_name", profile.shortName)
            put("phone1", profile.phone1)
            put("phone2", profile.phone2)
            put("email", profile.email)
            put("gstin", profile.gstin)
            put("address", LanguageJsonConverter.stringify(profile.address))
            put("city", LanguageJsonConverter.stringify(profile.city))
            put("district", LanguageJsonConverter.stringify(profile.district))
            put("state", LanguageJsonConverter.stringify(profile.state))
            put("country", LanguageJsonConverter.stringify(profile.country))
            put("pin_code", profile.pinCode)
            put("bank_name", LanguageJsonConverter.stringify(profile.bankName))
            put("branch", LanguageJsonConverter.stringify(profile.branch))
            put("account_number", profile.accountNumber)
            put("ifsc", profile.ifsc)
            put("logo", profile.logo)
            put("wide_logo", profile.wideLogo)
            put("header_layout", profile.headerLayout)
            put("signature", profile.signature)
            put("signatory_name", profile.signatoryName)
            put("signatory_first_name", profile.signatoryFirstName)
            put("signatory_last_name", profile.signatoryLastName)
            put("tagline", LanguageJsonConverter.stringify(profile.tagline))
            put("upi_id", profile.upiId)
            put("theme_color", profile.themeColor)
            put("updated_at", System.currentTimeMillis() / 1000)
            put("is_deleted", 0)
        }
        val exists = if (profile.id != null) {
            val checkCur = db.rawQuery("SELECT id FROM \"$profilesTable\" WHERE id = ?", arrayOf(profile.id.toString()))
            val found = checkCur.moveToFirst()
            checkCur.close()
            found
        } else false

        if (exists) {
            db.update("\"$profilesTable\"", cv, "id = ?", arrayOf(profile.id.toString())) > 0
        } else {
            cv.put("created_at", System.currentTimeMillis() / 1000)
            if (profile.id != null) cv.put("id", profile.id)
            db.insert("\"$profilesTable\"", null, cv) > 0
        }
    } ?: false

    override fun deleteProfile(profileId: Long): Boolean = useDb { db ->
        val cv = ContentValues().apply {
            put("is_deleted", 1)
            put("deleted_at", System.currentTimeMillis() / 1000)
        }
        db.update("\"$profilesTable\"", cv, "id = ?", arrayOf(profileId.toString())) > 0
    } ?: false

    override fun clearProfiles(): Boolean = useDb { db ->
        db.delete("\"$profilesTable\"", null, null) > 0
    } ?: false

    override fun clearAllData(): Boolean = useDb { db ->
        db.delete("\"$linksTable\"", null, null)
        db.delete("\"$receiptsTable\"", null, null)
        db.delete("\"$invoicesTable\"", null, null)
        db.delete("\"$productsTable\"", null, null)
        db.delete("\"$customersTable\"", null, null)
        db.delete("\"$profilesTable\"", null, null)
        true
    } ?: false

    // ── Helper Cursor Parsers ────────────────────────────────────────────────
    private fun cursorToMerchant(c: Cursor): CustomerModel = CustomerModel(
        id = getLongCol(c, "id"),
        name = LanguageJsonConverter.parse(getStringCol(c, "name", "peyar")),
        address = LanguageJsonConverter.parse(getStringCol(c, "address", "mugavari")),
        city = LanguageJsonConverter.parse(getStringCol(c, "city", "oor")),
        district = LanguageJsonConverter.parse(getStringCol(c, "district", "maavattam")),
        state = LanguageJsonConverter.parse(getStringCol(c, "state", "maanilam")),
        country = LanguageJsonConverter.parse(getStringCol(c, "country", "naadu", default = "{\"en\":\"India\",\"ta\":\"இந்தியா\"}")),
        internationalAddress = LanguageJsonConverter.parse(getStringCol(c, "international_address", "velinaad_mugavari")),
        pinCode = getStringCol(c, "pin_code", "anjal_kuriyeedu"),
        gstin = getStringCol(c, "gstin"),
        email = getStringCol(c, "email", "minnanjal"),
        phone = getStringCol(c, "phone", "tholaipaesi"),
        companyIds = ProductJsonConverter.parseLongList(getStringCol(c, "company_ids", "niruvanam_ids", default = "[]")),
        createdAt = getLongCol(c, "created_at"),
        updatedAt = getLongCol(c, "updated_at"),
        isDeleted = getIntCol(c, "is_deleted") == 1,
        deletedAt = getLongOrNull(c, "deleted_at")
    )

    private fun cursorToItem(c: Cursor): ProductModel = ProductModel(
        id = getLongCol(c, "id"),
        name = LanguageJsonConverter.parse(getStringCol(c, "name", "porul_peyar")),
        hsnCode = getStringCol(c, "hsn_code"),
        price = getDoubleCol(c, "price", "vilai"),
        taxRate = getDoubleCol(c, "tax_rate", "vari_veetham"),
        quantityType = getStringCol(c, "quantity_type", "alavu_vagai", default = "quantity"),
        unit = getStringCol(c, "unit", "alagu", default = "Nos"),
        companyIds = ProductJsonConverter.parseLongList(getStringCol(c, "company_ids", "niruvanam_ids", default = "[]")),
        companyPrices = ProductJsonConverter.parseCompanyPrices(getStringCol(c, "company_prices", default = "{}")),
        createdAt = getLongCol(c, "created_at"),
        updatedAt = getLongCol(c, "updated_at"),
        isDeleted = getIntCol(c, "is_deleted") == 1,
        deletedAt = getLongOrNull(c, "deleted_at")
    )

    private fun cursorToInvoice(c: Cursor): InvoiceModel = InvoiceModel(
        id = getLongCol(c, "id"),
        companyId = getLongOrNull(c, "company_id", "niruvanam_id"),
        invoiceNumber = getStringCol(c, "invoice_number", "patrucheettu_en"),
        finYear = getIntCol(c, "fin_year"),
        vanakkam = getIntCol(c, "vanakkam", default = 1),
        invoiceType = getStringCol(c, "invoice_type", "pattiyal_vagai", default = "tax-invoice"),
        customerId = getLongOrNull(c, "customer_id", "vaangunar_id"),
        customerName = LanguageJsonConverter.parse(getStringCol(c, "customer_name", "vaangunar_peyar")),
        customerAddress = LanguageJsonConverter.parse(getStringCol(c, "customer_address", "vaangunar_munvari")),
        invoiceDate = getLongCol(c, "invoice_date", "pattiyal_naal"),
        itemsJson = getStringCol(c, "items_json", "tharavugal", default = "[]"),
        totalAmount = getDoubleCol(c, "total_amount", "motha_thogai"),
        discount = getDoubleCol(c, "discount", "thallupadi"),
        globalDiscountValue = getDoubleCol(c, "global_discount_value", "podhu_thallupadi_mathippu"),
        globalDiscountType = getStringCol(c, "global_discount_type", "podhu_thallupadi_vagai", default = "%"),
        globalDiscountAmount = getDoubleCol(c, "global_discount_amount", "podhu_thallupadi_thogai"),
        taxAmount = getDoubleCol(c, "tax_amount", "vari_thogai"),
        taxDataJson = getStringCol(c, "tax_data_json", "vari_tharavugal", default = "{}"),
        totalWeight = if (isFactory) getDoubleCol(c, "total_weight", "motha_edai") else 0.0,
        wastageGrams = if (isFactory) getDoubleCol(c, "wastage_grams", "setharam_grams") else 0.0,
        postalCharges = if (isFactory) getDoubleCol(c, "postal_charges", "thabaal_thogai") else 0.0,
        ahimsaSilkAmount = if (isFactory) getDoubleCol(c, "ahimsa_silk_amount", "ahimsa_pattu_thogai") else 0.0,
        otherTaxesJson = if (isFactory) getStringCol(c, "other_taxes_json", "piravari_vugal", default = "[]") else "[]",
        customPreferencesJson = getStringCol(c, "custom_preferences_json", "sontha_viruppangal", default = "{}"),
        termsAndConditions = getStringCol(c, "terms_and_conditions", "nibandhanaigal"),
        notes = getStringCol(c, "notes", "ullkurippu"),
        bankDetailsJson = if (isFactory) getStringCol(c, "bank_details_json", "vangi_tharavugal", default = "{}") else "{}",
        createdAt = getLongCol(c, "created_at"),
        updatedAt = getLongCol(c, "updated_at"),
        isDeleted = getIntCol(c, "is_deleted") == 1,
        deletedAt = getLongOrNull(c, "deleted_at")
    )

    private fun cursorToReceipt(c: Cursor): ReceiptModel = ReceiptModel(
        id = getLongCol(c, "id"),
        companyId = getLongOrNull(c, "company_id", "niruvanam_id"),
        receiptNumber = getStringCol(c, "receipt_number", "patru_en"),
        finYear = getStringCol(c, "fin_year"),
        vanakkam = getIntCol(c, "vanakkam", default = 1),
        customerId = getLongOrNull(c, "customer_id", "vaangunar_id"),
        customerName = LanguageJsonConverter.parse(getStringCol(c, "customer_name", "vaangunar_peyar")),
        customerAddress = LanguageJsonConverter.parse(getStringCol(c, "customer_address", "vaangunar_munvari")),
        receiptDate = getLongCol(c, "receipt_date", "patru_naal"),
        amount = getDoubleCol(c, "amount", "thogai"),
        paymentMode = getStringCol(c, "payment_mode", "seluthum_murai", default = "cash"),
        bankName = getStringOrNull(c, "bank_name", "vangi_peyar"),
        transactionNumber = getStringOrNull(c, "transaction_number", "parivarthanai_en"),
        notes = getStringCol(c, "notes", "ullkurippu"),
        createdAt = getLongCol(c, "created_at"),
        updatedAt = getLongCol(c, "updated_at"),
        isDeleted = getIntCol(c, "is_deleted") == 1,
        deletedAt = getLongOrNull(c, "deleted_at")
    )

    private fun cursorToProfile(c: Cursor): BusinessProfile = BusinessProfile(
        id = getLongCol(c, "id"),
        primaryLanguage = getStringCol(c, "primary_language", "mudhan_mozhi", default = "ta"),
        secondaryLanguage = getStringCol(c, "secondary_language", "thunai_mozhi", default = "en"),
        isBilingual = getIntCol(c, "is_bilingual", "iru_mozhi", default = 1) == 1,
        splitGst = getIntCol(c, "split_gst", "gst_pirippugal") == 1,
        businessName = LanguageJsonConverter.parse(getStringCol(c, "business_name", "niruvanathin_peyar")),
        shortName = getStringCol(c, "short_name", "kurum_peyar"),
        phone1 = getStringCol(c, "phone1", "tholaipaesi1"),
        phone2 = getStringCol(c, "phone2", "tholaipaesi2"),
        email = getStringCol(c, "email", "minnanjal"),
        gstin = getStringCol(c, "gstin"),
        address = LanguageJsonConverter.parse(getStringCol(c, "address", "mugavari")),
        city = LanguageJsonConverter.parse(getStringCol(c, "city", "oor")),
        district = LanguageJsonConverter.parse(getStringCol(c, "district", "maavattam")),
        state = LanguageJsonConverter.parse(getStringCol(c, "state", "maanilam")),
        country = LanguageJsonConverter.parse(getStringCol(c, "country", "naadu", default = "{\"en\":\"India\",\"ta\":\"இந்தியா\"}")),
        pinCode = getStringCol(c, "pin_code", "anjal_kuriyeedu"),
        bankName = LanguageJsonConverter.parse(getStringCol(c, "bank_name", "vangi_peyar")),
        branch = LanguageJsonConverter.parse(getStringCol(c, "branch", "kilai")),
        accountNumber = getStringCol(c, "account_number", "vangi_kanakku"),
        ifsc = getStringCol(c, "ifsc"),
        logo = getStringCol(c, "logo", "oavuru"),
        wideLogo = getStringCol(c, "wide_logo", "agala_oavuru"),
        headerLayout = getStringCol(c, "header_layout", "thalaippu_vadivu", default = "small"),
        signature = getStringCol(c, "signature", "kaiyoppam"),
        signatoryName = getStringCol(c, "signatory_name", "oppam_peyar"),
        signatoryFirstName = getStringCol(c, "signatory_first_name", "oppam_mudhal_peyar"),
        signatoryLastName = getStringCol(c, "signatory_last_name", "oppam_irudhi_peyar"),
        tagline = LanguageJsonConverter.parse(getStringCol(c, "tagline", "adaimozhi")),
        upiId = getStringCol(c, "upi_id"),
        themeColor = getStringCol(c, "theme_color", "thoatra_niram")
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
