package com.elvan.udukkai.core.utils

import com.elvan.udukkai.core.finance.FinancialYearStatus
import com.elvan.udukkai.core.finance.YearLock
import java.util.TimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** India is UTC+5:30, so between midnight and 05:30 the phone's day is one ahead of the UTC day. */
class LocalDayTest {
    private val saved = TimeZone.getDefault()
    private val hour = 3_600_000L
    private val minute = 60_000L

    @BeforeTest fun india() = TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"))
    @AfterTest fun restore() = TimeZone.setDefault(saved)

    /** The instant a clock in India shows that day and time. */
    private fun ist(y: Int, m: Int, d: Int, h: Int, min: Int = 0) = DateUtils.millisOf(y, m, d) + h * hour + min * minute - (5 * hour + 30 * minute)
    private fun day(y: Int, m: Int, d: Int) = DateUtils.millisOf(y, m, d)

    @Test fun todayIsThePhonesDayAcrossMidnightAnd0530() {
        assertEquals(day(2026, 11, 4), DateUtils.todayMillis(ist(2026, 11, 4, 23, 59)))
        assertEquals(day(2026, 11, 5), DateUtils.todayMillis(ist(2026, 11, 5, 0, 0)))
        assertEquals(day(2026, 11, 5), DateUtils.todayMillis(ist(2026, 11, 5, 5, 29)))
        assertEquals(day(2026, 11, 5), DateUtils.todayMillis(ist(2026, 11, 5, 5, 31)))
    }

    @Test fun todayIsAlwaysAUtcMidnightSoStoredDatesKeepTheirForm() {
        val t = DateUtils.todayMillis(ist(2026, 11, 5, 1, 7))
        assertEquals(0L, t % 86_400_000L)
        assertEquals("05/11/2026", DateUtils.formatDate(t))
    }

    @Test fun yearAndLeapBoundaries() {
        assertEquals(day(2027, 1, 1), DateUtils.todayMillis(ist(2027, 1, 1, 2)))
        assertEquals(day(2028, 2, 29), DateUtils.todayMillis(ist(2028, 2, 29, 2)))
        assertEquals(day(2028, 3, 1), DateUtils.todayMillis(ist(2028, 3, 1, 0, 1)))
    }

    @Test fun todayLabelBeforeDawn() {
        val today = DateUtils.todayMillis(ist(2026, 11, 5, 1))
        assertEquals("Today", DateGroupUtils.formatDateHeader(day(2026, 11, 5), isBilingual = false, primaryLang = "en", today = today).first)
        assertEquals("Yesterday", DateGroupUtils.formatDateHeader(day(2026, 11, 4), isBilingual = false, primaryLang = "en", today = today).first)
        assertEquals("Today", DateGroupUtils.formatPillDate(day(2026, 11, 5), isBilingual = false, primaryLang = "en", today = today))
    }

    @Test fun financialYearChangesAtTheLocalMidnightOfFirstApril() {
        assertEquals("2026-27", FinancialYearStatus.of(ist(2027, 3, 31, 23, 59)).label)
        assertEquals("2027-28", FinancialYearStatus.of(ist(2027, 4, 1, 0, 1)).label)
    }

    @Test fun yearLockStartsAtTheLocalMidnightOfFirstDecember() {
        val bill = day(2025, 6, 1) // financial year 2025-26, closes 1 December 2026
        assertFalse(YearLock.isLocked(bill, ist(2026, 11, 30, 23, 59)))
        assertTrue(YearLock.isLocked(bill, ist(2026, 12, 1, 0, 1)))
    }
}
