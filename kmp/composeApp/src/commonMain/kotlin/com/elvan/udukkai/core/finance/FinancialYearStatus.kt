package com.elvan.udukkai.core.finance

import com.elvan.udukkai.core.utils.DateUtils

/**
 * Where the books are in the financial year (April to March): which year is running, how many days are left, and
 * when the year before it closes for changes (see [YearLock]). The invoice and receipt numbers start again at 001
 * on 1 April by themselves, because numbering counts per financial year.
 */
object FinancialYearStatus {
    private const val DAY = 24L * 60 * 60 * 1000

    data class Status(
        val label: String,           // "2026-27"
        val nextLabel: String,       // "2027-28"
        val daysLeft: Int,           // days until 1 April, 1 on 31 March
        val nextStart: String,       // "01/04/2027"
        val previousLabel: String?,  // the year before, while it can still be changed
        val previousClosesInDays: Int?
    )

    private fun daysUntil(target: Long, now: Long): Int = ((target - now + DAY - 1) / DAY).toInt().coerceAtLeast(0)

    fun of(now: Long = System.currentTimeMillis()): Status {
        val fy = DateUtils.financialYearStart(now)
        val nextStartMillis = DateUtils.parseToEpochMillis("01/04/${fy + 1}")!!
        val prevStart = fy - 1
        val close = YearLock.closeDay(prevStart)
        val prevOpen = now < close
        fun label(start: Int) = "$start-${((start + 1) % 100).toString().padStart(2, '0')}"
        return Status(
            label = label(fy),
            nextLabel = label(fy + 1),
            daysLeft = daysUntil(nextStartMillis, now),
            nextStart = "01/04/${fy + 1}",
            previousLabel = if (prevOpen) label(prevStart) else null,
            previousClosesInDays = if (prevOpen) daysUntil(close, now) else null
        )
    }
}
