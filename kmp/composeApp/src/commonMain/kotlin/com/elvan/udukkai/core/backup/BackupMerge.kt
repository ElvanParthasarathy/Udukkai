package com.elvan.udukkai.core.backup

/** What a restore brings back from a backup file. Bills are the bills, the receipts and the links between them. */
enum class RestoreCategory { BUSINESSES, CUSTOMERS, ITEMS, BILLS }

enum class RestoreMode {
    /** Adds what the file has and the app does not; nothing that is already in the app is changed or removed. */
    MERGE,

    /** The app's data becomes the file's data (a safety copy of the current data is kept first). */
    REPLACE
}

/** Why a backup file cannot be restored: shown to the user by [com.elvan.udukkai.ui.screens.settings.RestoreFlow]. */
object BackupProblem {
    const val NOT_A_BACKUP = "not_a_backup"
    const val DAMAGED = "damaged"
    const val OTHER_APP = "other_app"
    const val NO_SAFETY_COPY = "no_safety_copy"
    const val NOTHING_CHOSEN = "nothing_chosen"
    const val FAILED = "failed"
    const val UNSUPPORTED = "unsupported"
}

/** What is inside a backup file, read without touching the app's data; [problem] says why it cannot be used. */
data class BackupPreview(
    val problem: String? = null,
    val createdAt: Long = 0L,
    val businesses: Int = 0,
    val customers: Int = 0,
    val items: Int = 0,
    val invoices: Int = 0,
    val receipts: Int = 0
) {
    val ok: Boolean get() = problem == null
}

/** The outcome of a restore: per category how many were added and how many were already there and left as they are. */
data class RestoreReport(
    val ok: Boolean,
    val problem: String? = null,
    val added: Map<RestoreCategory, Int> = emptyMap(),
    val kept: Map<RestoreCategory, Int> = emptyMap()
)

/** One open SQLite database; the backup being merged in is attached to it as "bak". */
interface MergeDb {
    fun count(sql: String): Int
    fun columns(schema: String, table: String): List<String>
    fun exec(sql: String)
    fun begin()
    fun commit()
    fun rollback()
}

/**
 * Adds the records of a backup to the live database without touching what is there: a record whose id exists is left
 * as it is (so existing bills always win), a record that is new is copied, and everything happens in one transaction,
 * so a failure anywhere leaves the live data exactly as it was.
 * shortcut: "the same record" is the same id (ids are made from the clock, so they match across a phone's own backups);
 * a file from another phone with a clashing id is counted as kept, never merged over.
 */
object BackupMerge {
    private fun tableOf(prefix: String, name: String) = prefix + name

    private fun exists(db: MergeDb, schema: String, table: String) = db.columns(schema, table).isNotEmpty()

    private fun total(db: MergeDb, schema: String, table: String, notDeleted: Boolean = false): Int {
        if (!exists(db, schema, table)) return 0
        val where = if (notDeleted && "is_deleted" in db.columns(schema, table)) " WHERE is_deleted = 0" else ""
        return db.count("SELECT COUNT(*) FROM $schema.\"$table\"$where")
    }

    /** The counts of a database (the app's tables carry [prefix], e.g. "textiles_"); [schema] is "main" or "bak". */
    fun preview(db: MergeDb, prefix: String, schema: String, createdAt: Long): BackupPreview = BackupPreview(
        createdAt = createdAt,
        businesses = total(db, schema, tableOf(prefix, "business_profiles")),
        customers = total(db, schema, tableOf(prefix, "customers"), notDeleted = true),
        items = total(db, schema, tableOf(prefix, "items"), notDeleted = true),
        invoices = total(db, schema, tableOf(prefix, "invoices"), notDeleted = true),
        receipts = total(db, schema, tableOf(prefix, "receipts"), notDeleted = true)
    )

    private fun tablesOf(category: RestoreCategory, prefix: String): List<String> = when (category) {
        RestoreCategory.BUSINESSES -> listOf("business_profiles")
        RestoreCategory.CUSTOMERS -> listOf("customers")
        RestoreCategory.ITEMS -> listOf("items")
        RestoreCategory.BILLS -> listOf("invoices", "receipts", "receipt_invoice_links")
    }.map { tableOf(prefix, it) }

    /** Copies the rows of bak.[table] whose id is not in main.[table]; returns (added, kept). */
    private fun copyNew(db: MergeDb, table: String): Pair<Int, Int> {
        if (!exists(db, "main", table) || !exists(db, "bak", table)) return 0 to 0
        val mine = db.columns("main", table)
        val theirs = db.columns("bak", table).toSet()
        val cols = mine.filter { it in theirs }.joinToString(",") { "\"$it\"" }
        // Counted as the user sees them: what is in the recycle bin is copied too but not counted
        val before = total(db, "main", table, notDeleted = true)
        val offered = total(db, "bak", table, notDeleted = true)
        db.exec("INSERT OR IGNORE INTO main.\"$table\" ($cols) SELECT $cols FROM bak.\"$table\"")
        val added = total(db, "main", table, notDeleted = true) - before
        return added to (offered - added).coerceAtLeast(0)
    }

    /**
     * The links between receipts and bills have ids of their own that clash between files, so they are added by what
     * they join: a pair already linked is left alone, and a link is only added when both its receipt and its bill are in the app.
     */
    private fun copyNewLinks(db: MergeDb, prefix: String): Pair<Int, Int> {
        val links = tableOf(prefix, "receipt_invoice_links")
        val receipts = tableOf(prefix, "receipts")
        val invoices = tableOf(prefix, "invoices")
        if (!exists(db, "main", links) || !exists(db, "bak", links)) return 0 to 0
        val theirs = db.columns("bak", links).toSet()
        val cols = db.columns("main", links).filter { it != "id" && it in theirs }.joinToString(",") { "\"$it\"" }
        val b = db.columns("main", links).filter { it != "id" && it in theirs }.joinToString(",") { "b.\"$it\"" }
        val before = db.count("SELECT COUNT(*) FROM main.\"$links\"")
        val offered = db.count("SELECT COUNT(*) FROM bak.\"$links\"")
        db.exec(
            "INSERT INTO main.\"$links\" ($cols) SELECT $b FROM bak.\"$links\" b " +
                "WHERE NOT EXISTS (SELECT 1 FROM main.\"$links\" m WHERE m.receipt_id = b.receipt_id AND m.invoice_id = b.invoice_id) " +
                "AND b.receipt_id IN (SELECT id FROM main.\"$receipts\") AND b.invoice_id IN (SELECT id FROM main.\"$invoices\")"
        )
        val added = db.count("SELECT COUNT(*) FROM main.\"$links\"") - before
        return added to (offered - added)
    }

    /** Merges the chosen [categories]; any failure rolls everything back and reports the problem. */
    fun merge(db: MergeDb, prefix: String, categories: Set<RestoreCategory>): RestoreReport {
        val added = mutableMapOf<RestoreCategory, Int>()
        val kept = mutableMapOf<RestoreCategory, Int>()
        return try {
            db.begin()
            // Businesses first, bills last: a bill's receipt links need both ends to be in place
            RestoreCategory.entries.filter { it in categories }.forEach { category ->
                var a = 0
                var k = 0
                tablesOf(category, prefix).forEach { table ->
                    val (ta, tk) = if (table.endsWith("receipt_invoice_links")) copyNewLinks(db, prefix) else copyNew(db, table)
                    // The links are counted with the bills only as a part: the numbers shown are the bills and receipts
                    if (!table.endsWith("receipt_invoice_links")) {
                        a += ta
                        k += tk
                    }
                }
                added[category] = a
                kept[category] = k
            }
            db.commit()
            RestoreReport(ok = true, added = added, kept = kept)
        } catch (e: Throwable) {
            try { db.rollback() } catch (_: Throwable) {}
            RestoreReport(ok = false, problem = BackupProblem.FAILED)
        }
    }
}
