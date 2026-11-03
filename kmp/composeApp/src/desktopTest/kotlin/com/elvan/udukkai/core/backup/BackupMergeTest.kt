package com.elvan.udukkai.core.backup

import com.elvan.udukkai.data.DesktopDbPaths
import com.elvan.udukkai.data.database.DesktopUdukkaiDatabaseHelper
import com.elvan.udukkai.data.model.CustomerModel
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.model.ItemModel
import java.sql.Connection
import java.sql.DriverManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Restoring from a file only ever adds: what is in the app stays, and a failure leaves everything as it was. */
class BackupMergeTest {
    private val live = "merge_live_test.db"
    private val backup = "merge_backup_test.db"

    private fun attached(): Connection {
        Class.forName("org.sqlite.JDBC")
        val c = DriverManager.getConnection("jdbc:sqlite:${DesktopDbPaths.getDbFile(live).absolutePath}")
        c.createStatement().use { it.execute("ATTACH DATABASE '${DesktopDbPaths.getDbFile(backup).absolutePath}' AS bak") }
        return c
    }

    /** The backup is an earlier copy of the app's data plus things the app no longer has. */
    private fun setUp(): DesktopUdukkaiDatabaseHelper {
        val l = DesktopUdukkaiDatabaseHelper(live, "t_", isFactory = false)
        val b = DesktopUdukkaiDatabaseHelper(backup, "t_", isFactory = false)
        for (db in listOf(b, l)) {
            db.saveCustomer(CustomerModel(name = mapOf("en" to "Kept customer")))
            db.saveInvoice(InvoiceModel(invoiceNumber = "A-1", totalAmount = if (db === l) 500.0 else 100.0))
        }
        // Only the backup has these
        b.saveCustomer(CustomerModel(name = mapOf("en" to "Old customer")))
        b.saveItem(ItemModel(name = mapOf("en" to "Old item")))
        b.saveInvoice(InvoiceModel(invoiceNumber = "A-2"))
        return l
    }

    private fun cleanUp() {
        DesktopDbPaths.getDbFile(live).delete()
        DesktopDbPaths.getDbFile(backup).delete()
    }

    @Test
    fun mergeAddsWhatIsMissingAndKeepsWhatIsThere() {
        val l = setUp()
        try {
            attached().use { c ->
                assertTrue(BackupMerge.merge(JdbcMergeDb(c), "t_", RestoreCategory.entries.toSet()).ok)
            }
            assertEquals(listOf("Kept customer", "Old customer"), l.loadAllCustomers().map { it.name["en"].orEmpty() }.sorted())
            assertEquals(listOf("A-1", "A-2"), l.loadAllInvoices().map { it.invoiceNumber }.sorted())
            // The app's own bill is not overwritten by the backup's older copy of it
            assertEquals(500.0, l.loadAllInvoices().first { it.invoiceNumber == "A-1" }.totalAmount)
            assertEquals(1, l.loadAllItems().size)
        } finally {
            cleanUp()
        }
    }

    @Test
    fun onlyTheChosenCategoriesAreBroughtBack() {
        val l = setUp()
        try {
            attached().use { c ->
                val report = BackupMerge.merge(JdbcMergeDb(c), "t_", setOf(RestoreCategory.CUSTOMERS))
                assertEquals(1, report.added[RestoreCategory.CUSTOMERS])
                assertEquals(1, report.kept[RestoreCategory.CUSTOMERS])
                assertFalse(RestoreCategory.BILLS in report.added)
            }
            assertEquals(2, l.loadAllCustomers().size)
            assertEquals(1, l.loadAllInvoices().size)
            assertEquals(0, l.loadAllItems().size)
        } finally {
            cleanUp()
        }
    }

    @Test
    fun aFailureHalfWayChangesNothing() {
        val l = setUp()
        try {
            attached().use { c ->
                val real = JdbcMergeDb(c)
                var execs = 0
                val breaking = object : MergeDb by real {
                    override fun exec(sql: String) {
                        // The customers are copied, then a later copy fails
                        if (++execs == 3) error("disk full")
                        real.exec(sql)
                    }
                }
                val report = BackupMerge.merge(breaking, "t_", RestoreCategory.entries.toSet())
                assertFalse(report.ok)
                assertEquals(BackupProblem.FAILED, report.problem)
            }
            assertEquals(1, l.loadAllCustomers().size)
            assertEquals(1, l.loadAllInvoices().size)
            assertEquals(0, l.loadAllItems().size)
        } finally {
            cleanUp()
        }
    }

    @Test
    fun theBackupIsCountedWithoutBeingRestored() {
        setUp()
        try {
            attached().use { c ->
                val p = BackupMerge.preview(JdbcMergeDb(c), "t_", "bak", 0L)
                assertEquals(2, p.customers)
                assertEquals(1, p.items)
                assertEquals(2, p.invoices)
            }
        } finally {
            cleanUp()
        }
    }
}
