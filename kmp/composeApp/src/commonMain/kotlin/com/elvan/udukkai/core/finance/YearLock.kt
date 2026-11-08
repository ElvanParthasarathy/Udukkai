package com.elvan.udukkai.core.finance

import com.elvan.udukkai.core.utils.DateUtils

/**
 * A finished financial year closes by itself on 1 December after it ends: bills and receipts dated in it can no longer
 * be changed. Under GST that is the last day to correct a year's sales (a credit note for it can be issued up to
 * 30 November after the year end, or up to the annual return if that comes first; the annual return is due on 31 December).
 * After that a correction is a new entry in the current year. The owner can still open a closed bill ("Edit anyway")
 * until the app is closed.
 * shortcut: the date rules are the GST ones as of now, change [closeDay] if they change.
 */
object YearLock {
    private val openedForSession = HashSet<Int>()

    /** The day the financial year starting in [fyStart] closes: 1 December of the year it ends in. */
    fun closeDay(fyStart: Int): Long = DateUtils.parseToEpochMillis("01/12/${fyStart + 1}")!!

    fun isLocked(dateMillis: Long, now: Long = System.currentTimeMillis()): Boolean {
        val fy = DateUtils.financialYearStart(dateMillis)
        return fy !in openedForSession && DateUtils.todayMillis(now) >= closeDay(fy)
    }

    /** Lets the closed year of this date be changed until the app is closed. */
    fun openForSession(dateMillis: Long) {
        openedForSession.add(DateUtils.financialYearStart(dateMillis))
    }
}
