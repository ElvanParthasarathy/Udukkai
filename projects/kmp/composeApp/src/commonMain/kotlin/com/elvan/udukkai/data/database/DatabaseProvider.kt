package com.elvan.udukkai.data.database

import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.mode.ModeManager

/**
 * Provides the correct database instance based on the current AppMode.
 *
 * THE WALL — How complete data isolation works:
 *
 * ┌─────────────────────────────────────────────────────┐
 * │                    Udukkai App                      │
 * │                                                     │
 * │   ┌─────────────────┐    ┌─────────────────┐       │
 * │   │   KOOLI Mode    │    │   PATTU Mode    │       │
 * │   │                 │    │                 │       │
 * │   │ udukkai_kooli  │    │ udukkai_pattu  │       │
 * │   │     .db         │    │     .db         │       │
 * │   │                 │    │                 │       │
 * │   │ • Invoices      │    │ • Invoices      │       │
 * │   │ • Customers     │    │ • Customers     │       │
 * │   │ • Products      │    │ • Products      │       │
 * │   │ • Receipts      │    │ • Receipts      │       │
 * │   │ • Settings      │    │ • Settings      │       │
 * │   └─────────────────┘    └─────────────────┘       │
 * │         ▲                       ▲                   │
 * │         │                       │                   │
 * │         └───── NEVER TOUCH ─────┘                   │
 * │                                                     │
 * └─────────────────────────────────────────────────────┘
 *
 * When the user switches from Kooli to Pattu:
 * 1. ModeManager.setMode(AppMode.PATTU) is called
 * 2. DatabaseProvider switches to the Pattu database instance
 * 3. The entire UI recomposes with Pattu data
 * 4. Kooli data remains untouched in its own .db file
 */
object DatabaseProvider {

    // Cache database instances per mode so we don't recreate them
    // The actual Room database creation will be added when Room is set up
    // private val databases = mutableMapOf<AppMode, AppDatabase>()

    /**
     * Returns the database name for the current mode.
     */
    fun currentDatabaseName(): String = ModeManager.currentDatabaseName

    /**
     * Returns the database name for a specific mode.
     */
    fun databaseNameFor(mode: AppMode): String = mode.databaseName

    /**
     * Returns the preferences key prefix for the current mode.
     * Use this when reading/writing SharedPreferences or DataStore.
     *
     * Example:
     *   val key = DatabaseProvider.prefsKey("lastInvoiceNumber")
     *   // In Kooli mode: "udukkai_kooli_lastInvoiceNumber"
     *   // In Pattu mode: "udukkai_pattu_lastInvoiceNumber"
     */
    fun prefsKey(key: String): String = "${ModeManager.prefsNamespace}$key"

    /**
     * Returns the file storage subdirectory for the current mode.
     * Use this when saving files (logos, signatures, exports).
     *
     * Example:
     *   val dir = DatabaseProvider.storageDir()
     *   // In Kooli mode: "udukkai_kooli"
     *   // In Pattu mode: "udukkai_pattu"
     */
    fun storageDir(): String = ModeManager.storageDir
}
