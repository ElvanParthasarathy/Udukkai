package com.elvan.udukkai.ui.components

import com.elvan.udukkai.core.utils.DateGroupUtils
import com.elvan.udukkai.core.utils.DateUtils

/** A chosen day, or an inclusive run of days; [end] is null while only the first day of a range has been tapped. */
data class DaySpan(val start: Long?, val end: Long? = null)

/** One square of a month grid; [inMonth] is false for the dimmed days of the months either side. */
data class MonthCell(val millis: Long, val day: Int, val inMonth: Boolean)

/**
 * Calendar rules behind [ElvanDatePicker] and [ElvanDateRangePicker], kept free of UI so they can be tested.
 * Days are UTC midnights, the same form the rest of the app keeps its dates in.
 */
object ElvanCalendar {
    const val DAY = 86_400_000L

    fun isLeapYear(year: Int) = (year % 4 == 0 && year % 100 != 0) || year % 400 == 0

    fun daysInMonth(year: Int, month: Int): Int = when (month) {
        2 -> if (isLeapYear(year)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }

    /** 0 = Sunday. */
    fun weekdayOf(year: Int, month: Int, day: Int): Int = DateGroupUtils.getDateComponents(DateUtils.millisOf(year, month, day)).dayOfWeek

    /** The month [delta] months away; the year rolls over in either direction. */
    fun shiftMonth(year: Int, month: Int, delta: Int): Pair<Int, Int> {
        val index = year * 12 + (month - 1) + delta
        return index.floorDiv(12) to index.mod(12) + 1
    }

    fun startOfDay(millis: Long): Long = Math.floorDiv(millis, DAY) * DAY

    /** [min] and [max] are inclusive and may be any time of their day. */
    fun isSelectable(day: Long, min: Long?, max: Long?): Boolean =
        (min == null || day >= startOfDay(min)) && (max == null || day <= startOfDay(max))

    /** The day a picker opens on: the one given, pulled into the allowed range. */
    fun clampToRange(day: Long, min: Long?, max: Long?): Long {
        val d = startOfDay(day)
        val lo = min?.let { startOfDay(it) }
        val hi = max?.let { startOfDay(it) }
        return when {
            lo != null && d < lo -> lo
            hi != null && d > hi -> hi
            else -> d
        }
    }

    /**
     * What a tap on [day] does. One day: it becomes the choice. A range: the first tap starts one, the second ends it
     * (whichever order the days come in), and a third starts again.
     */
    fun tap(span: DaySpan, day: Long, range: Boolean): DaySpan = when {
        !range || span.start == null || span.end != null -> DaySpan(day, null)
        else -> DaySpan(minOf(span.start, day), maxOf(span.start, day))
    }

    fun contains(span: DaySpan, day: Long): Boolean {
        val s = span.start ?: return false
        return day in s..(span.end ?: s)
    }

    /** A month laid out in weeks starting Sunday, filled out with the days of the months either side. Always six rows, so a sheet never changes height. */
    fun monthCells(year: Int, month: Int): List<MonthCell> {
        val lead = weekdayOf(year, month, 1)
        val days = daysInMonth(year, month)
        val first = DateUtils.millisOf(year, month, 1) - lead * DAY
        val previous = shiftMonth(year, month, -1).let { (y, m) -> daysInMonth(y, m) }
        return List(42) { i ->
            val inMonth = i in lead until lead + days
            val day = when {
                i < lead -> previous - lead + i + 1
                inMonth -> i - lead + 1
                else -> i - lead - days + 1
            }
            MonthCell(first + i * DAY, day, inMonth)
        }
    }

    /** Months as one running number, for paging through them. */
    fun monthIndex(year: Int, month: Int): Int = year * 12 + month - 1
    fun monthOfIndex(index: Int): Pair<Int, Int> = index.floorDiv(12) to index.mod(12) + 1

    /** [day] of [month]: the 31st asked of a shorter month becomes its last day. */
    fun dayIn(year: Int, month: Int, day: Int): Long = DateUtils.millisOf(year, month, day.coerceAtMost(daysInMonth(year, month)))
}
