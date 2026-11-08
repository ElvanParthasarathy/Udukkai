package com.elvan.udukkai.ui.components

import com.elvan.udukkai.core.utils.DateGroupUtils
import com.elvan.udukkai.core.utils.DateUtils
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ElvanCalendarTest {
    private fun day(y: Int, m: Int, d: Int) = DateUtils.millisOf(y, m, d)

    @Test
    fun leapYearsFollowTheGregorianRule() {
        assertTrue(ElvanCalendar.isLeapYear(2024))
        assertTrue(ElvanCalendar.isLeapYear(2000))
        assertFalse(ElvanCalendar.isLeapYear(1900))
        assertFalse(ElvanCalendar.isLeapYear(2100))
        assertFalse(ElvanCalendar.isLeapYear(2025))
    }

    @Test
    fun februaryAndShortMonthsHaveTheRightLength() {
        assertEquals(29, ElvanCalendar.daysInMonth(2024, 2))
        assertEquals(28, ElvanCalendar.daysInMonth(2026, 2))
        assertEquals(28, ElvanCalendar.daysInMonth(2100, 2))
        assertEquals(30, ElvanCalendar.daysInMonth(2026, 4))
        assertEquals(31, ElvanCalendar.daysInMonth(2026, 12))
    }

    @Test
    fun weekdaysMatchKnownDates() {
        assertEquals(4, ElvanCalendar.weekdayOf(2026, 1, 1)) // Thursday
        assertEquals(4, ElvanCalendar.weekdayOf(1970, 1, 1)) // Thursday, day zero
        assertEquals(0, ElvanCalendar.weekdayOf(2026, 11, 8)) // Sunday
        assertEquals(4, ElvanCalendar.weekdayOf(2024, 2, 29)) // leap day, Thursday
    }

    @Test
    fun millisRoundTripThroughTheDateComponents() {
        for ((y, m, d) in listOf(Triple(2024, 2, 29), Triple(1999, 12, 31), Triple(2026, 4, 1), Triple(2100, 3, 1))) {
            val c = DateGroupUtils.getDateComponents(day(y, m, d))
            assertEquals(Triple(y, m, d), Triple(c.year, c.month, c.day))
        }
    }

    @Test
    fun shiftingMonthsRollsTheYearBothWays() {
        assertEquals(2027 to 1, ElvanCalendar.shiftMonth(2026, 12, 1))
        assertEquals(2025 to 12, ElvanCalendar.shiftMonth(2026, 1, -1))
        assertEquals(2027 to 12, ElvanCalendar.shiftMonth(2026, 12, 12))
        assertEquals(2024 to 11, ElvanCalendar.shiftMonth(2026, 11, -24))
    }

    @Test
    fun selectableDaysRespectBothLimitsInclusively() {
        val min = day(2026, 4, 1)
        val max = day(2027, 3, 31) + 5 * 3_600_000L // any time of the last day still allows it
        assertTrue(ElvanCalendar.isSelectable(day(2026, 4, 1), min, max))
        assertTrue(ElvanCalendar.isSelectable(day(2027, 3, 31), min, max))
        assertFalse(ElvanCalendar.isSelectable(day(2026, 3, 31), min, max))
        assertFalse(ElvanCalendar.isSelectable(day(2027, 4, 1), min, max))
        assertTrue(ElvanCalendar.isSelectable(day(1990, 1, 1), null, null))
    }

    @Test
    fun openingDayIsPulledIntoTheAllowedRange() {
        val min = day(2026, 4, 1)
        val max = day(2027, 3, 31)
        assertEquals(min, ElvanCalendar.clampToRange(day(2020, 1, 1), min, max))
        assertEquals(max, ElvanCalendar.clampToRange(day(2030, 1, 1), min, max))
        assertEquals(day(2026, 8, 8), ElvanCalendar.clampToRange(day(2026, 8, 8) + 7_000_000L, min, max))
    }

    @Test
    fun singleDayTapReplacesTheChoice() {
        val a = ElvanCalendar.tap(DaySpan(day(2026, 1, 1)), day(2026, 1, 5), range = false)
        assertEquals(DaySpan(day(2026, 1, 5), null), a)
    }

    @Test
    fun rangeTapsStartEndThenRestart() {
        val first = ElvanCalendar.tap(DaySpan(null), day(2026, 1, 10), range = true)
        assertEquals(DaySpan(day(2026, 1, 10), null), first)
        val second = ElvanCalendar.tap(first, day(2026, 1, 14), range = true)
        assertEquals(DaySpan(day(2026, 1, 10), day(2026, 1, 14)), second)
        val third = ElvanCalendar.tap(second, day(2026, 2, 1), range = true)
        assertEquals(DaySpan(day(2026, 2, 1), null), third)
    }

    @Test
    fun rangeEndedBeforeItsStartIsPutRightWayRound() {
        val first = DaySpan(day(2026, 1, 10))
        assertEquals(DaySpan(day(2026, 1, 3), day(2026, 1, 10)), ElvanCalendar.tap(first, day(2026, 1, 3), range = true))
        assertEquals(DaySpan(day(2026, 1, 10), day(2026, 1, 10)), ElvanCalendar.tap(first, day(2026, 1, 10), range = true))
    }

    @Test
    fun spanContainsItsEndsAndEverythingBetween() {
        val span = DaySpan(day(2026, 1, 10), day(2026, 1, 12))
        assertTrue(ElvanCalendar.contains(span, day(2026, 1, 10)))
        assertTrue(ElvanCalendar.contains(span, day(2026, 1, 11)))
        assertTrue(ElvanCalendar.contains(span, day(2026, 1, 12)))
        assertFalse(ElvanCalendar.contains(span, day(2026, 1, 13)))
        assertTrue(ElvanCalendar.contains(DaySpan(day(2026, 1, 10)), day(2026, 1, 10)))
        assertFalse(ElvanCalendar.contains(DaySpan(null), day(2026, 1, 10)))
    }

    @Test
    fun monthGridHasSixWeeksFilledFromTheMonthsEitherSide() {
        val nov = ElvanCalendar.monthCells(2026, 11) // 1 November 2026 is a Sunday, November has 30 days
        assertEquals(42, nov.size)
        assertEquals(MonthCell(day(2026, 11, 1), 1, true), nov[0])
        assertEquals(MonthCell(day(2026, 11, 30), 30, true), nov[29])
        assertEquals(MonthCell(day(2026, 12, 1), 1, false), nov[30])
        assertEquals(MonthCell(day(2026, 12, 12), 12, false), nov[41])

        val feb = ElvanCalendar.monthCells(2028, 2) // 1 February 2028 is a Tuesday, a leap February
        assertEquals(MonthCell(day(2028, 1, 30), 30, false), feb[0])
        assertEquals(MonthCell(day(2028, 2, 1), 1, true), feb[2])
        assertEquals(MonthCell(day(2028, 2, 29), 29, true), feb[30])
        assertEquals(MonthCell(day(2028, 3, 1), 1, false), feb[31])

        val jan = ElvanCalendar.monthCells(2027, 1) // the row before reaches back over the year
        assertEquals(MonthCell(day(2026, 12, 27), 27, false), jan[0])
    }

    @Test
    fun everyCellIsTheDayBeforeTheNext() {
        val cells = ElvanCalendar.monthCells(2026, 3)
        cells.zipWithNext { a, b -> assertEquals(ElvanCalendar.DAY, b.millis - a.millis) }
    }

    @Test
    fun monthIndexRoundTripsOverYearEnds() {
        assertEquals(2026 to 12, ElvanCalendar.monthOfIndex(ElvanCalendar.monthIndex(2026, 12)))
        assertEquals(2027 to 1, ElvanCalendar.monthOfIndex(ElvanCalendar.monthIndex(2026, 12) + 1))
    }

    @Test
    fun aDayPastTheMonthEndBecomesItsLastDay() {
        assertEquals(day(2026, 2, 28), ElvanCalendar.dayIn(2026, 2, 31))
        assertEquals(day(2028, 2, 29), ElvanCalendar.dayIn(2028, 2, 31))
        assertEquals(day(2026, 3, 31), ElvanCalendar.dayIn(2026, 3, 31))
    }

    @Test
    fun calendarNamesExistInBothLanguages() {
        for (m in 1..12) {
            assertTrue(DateGroupUtils.monthFullName(m, tamil = false).isNotEmpty())
            assertTrue(DateGroupUtils.monthFullName(m, tamil = true).isNotEmpty())
        }
        for (w in 0..6) {
            assertEquals(1, DateGroupUtils.weekdayNarrowName(w, tamil = false).length)
            assertTrue(DateGroupUtils.weekdayNarrowName(w, tamil = true).isNotEmpty())
        }
    }

    @Test
    fun todayTurnsOverAtLocalMidnightNotUtc() {
        // The same instant is a different calendar day on either side of the phone's offset
        val offset = java.util.TimeZone.getDefault().getOffset(0L)
        val justAfterLocalMidnight = Math.floorDiv(1_700_000_000_000L + offset, ElvanCalendar.DAY) * ElvanCalendar.DAY - offset + 60_000L
        assertEquals(Math.floorDiv(justAfterLocalMidnight + offset, ElvanCalendar.DAY) * ElvanCalendar.DAY, DateUtils.todayMillis(justAfterLocalMidnight))
    }
}
