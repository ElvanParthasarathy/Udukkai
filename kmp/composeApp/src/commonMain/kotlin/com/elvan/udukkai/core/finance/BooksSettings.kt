package com.elvan.udukkai.core.finance

import com.elvan.udukkai.core.platform.PreferencesHelper
import com.elvan.udukkai.core.platform.getPreferencesHelper
import com.elvan.udukkai.core.utils.DateUtils

/**
 * Books settings of one business in one app: how bill numbers are made and the year-end lock.
 * shortcut: kept in the phone's preferences (not the database), so they are not in cloud sync or the database backup, move to the profile table if that is needed.
 */
class BooksSettings(
    private val app: String,
    private val prefs: () -> PreferencesHelper = { getPreferencesHelper() },
) {
    private fun key(companyId: Long?, name: String) = "books_${app}_${companyId ?: 0L}_$name"

    /** True: numbers look like VRM/26-27/0001 and start again every April. False: VRM-07, one series for ever. */
    fun fySeries(companyId: Long?): Boolean = prefs().getBoolean(key(companyId, "fyseries"), false)

    fun setFySeries(companyId: Long?, on: Boolean) = prefs().setBoolean(key(companyId, "fyseries"), on)

    /** The last locked day (days since 1970), or null when nothing is locked. */
    fun lockedThrough(companyId: Long?): Int? = prefs().getInt(key(companyId, "lock"), 0).takeIf { it > 0 }

    fun isLocked(companyId: Long?, dateMillis: Long): Boolean =
        lockedThrough(companyId)?.let { DateUtils.dayOf(dateMillis) <= it } == true

    /** Locks every date up to 31 March of the year that ends the financial year starting in [fyStart]. */
    fun lockThrough(companyId: Long?, fyStart: Int) {
        val end = DateUtils.parseToEpochMillis("31/03/${fyStart + 1}") ?: return
        prefs().setInt(key(companyId, "lock"), DateUtils.dayOf(end).toInt())
    }

    fun unlock(companyId: Long?) = prefs().setInt(key(companyId, "lock"), 0)
}

object Books {
    val textiles = BooksSettings("textiles")
    val factory = BooksSettings("factory")
}
