package com.elvan.udukkai.core.utils

object DateUtils {
    /**
     * Converts epoch milliseconds (UTC) to "DD/MM/YYYY" format.
     */
    fun formatDate(millis: Long): String = formatEpochMillis(millis)

    /** Date and time as the clock on the wall shows them (the phone's time zone), e.g. 03/11/2026 15:26. */
    fun formatDateTime(millis: Long): String {
        val local = millis + java.util.TimeZone.getDefault().getOffset(millis)
        val dateStr = formatEpochMillis(local)
        val totalSeconds = local / 1000
        val minute = ((totalSeconds / 60) % 60).toInt()
        val hour = ((totalSeconds / 3600) % 24).toInt()
        val minStr = if (minute < 10) "0$minute" else "$minute"
        val hrStr = if (hour < 10) "0$hour" else "$hour"
        return "$dateStr $hrStr:$minStr"
    }

    fun formatEpochMillis(millis: Long): String {
        val days = (millis / 86400000L).toInt()
        val z = days + 719468
        val era = (if (z >= 0) z else z - 146096) / 146097
        val doe = z - era * 146097
        val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
        val y = yoe + era * 400
        val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
        val mp = (5 * doy + 2) / 153
        val d = doy - (153 * mp + 2) / 5 + 1
        val m = mp + (if (mp < 10) 3 else -9)
        val year = y + (if (m <= 2) 1 else 0)

        val dayStr = if (d < 10) "0$d" else "$d"
        val monthStr = if (m < 10) "0$m" else "$m"
        return "$dayStr/$monthStr/$year"
    }

    /**
     * Parses "DD/MM/YYYY" to epoch milliseconds (UTC) for initializing date pickers.
     */
    fun parseToEpochMillis(dateStr: String): Long? {
        val parts = dateStr.split("/")
        if (parts.size != 3) return null
        val d = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        val y = parts[2].toIntOrNull() ?: return null

        return millisOf(y, m, d)
    }

    /** UTC midnight of a calendar day, the form every date in the app is kept in. */
    fun millisOf(y: Int, m: Int, d: Int): Long {
        val adjustedYear = if (m <= 2) y - 1 else y
        val era = (if (adjustedYear >= 0) adjustedYear else adjustedYear - 399) / 400
        val yoe = adjustedYear - era * 400
        val mp = if (m > 2) m - 3 else m + 9
        val doy = (153 * mp + 2) / 5 + d - 1
        val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
        val days = era * 146097 + doe - 719468
        return days * 86400000L
    }

    /** Today's calendar day (as the phone's clock shows it) as UTC midnight, unlike `now / 86400000` which turns over at 05:30 in India. */
    fun todayMillis(now: Long = System.currentTimeMillis()): Long {
        val local = now + java.util.TimeZone.getDefault().getOffset(now)
        return Math.floorDiv(local, 86400000L) * 86400000L
    }

    /** Whole days since 1970 in the same UTC calendar [formatEpochMillis] shows. */
    fun dayOf(millis: Long): Long = millis / 86400000L

    /** First year of the Indian financial year (April to March) the date falls in. */
    fun financialYearStart(millis: Long): Int {
        val parts = formatEpochMillis(millis).split("/")
        val month = parts[1].toInt()
        val year = parts[2].toInt()
        return if (month >= 4) year else year - 1
    }

    /** "2026-27" for a date between April 2026 and March 2027. */
    fun isPastFinancialYear(millis: Long): Boolean =
        financialYearStart(millis) < financialYearStart(todayMillis())

    fun isCurrentFinancialYear(millis: Long): Boolean =
        financialYearStart(millis) == financialYearStart(todayMillis())

    fun financialYearLabel(millis: Long): String {
        val start = financialYearStart(millis)
        return "$start-${((start + 1) % 100).toString().padStart(2, '0')}"
    }

    /** Normalizes a financial year string (e.g. "2026", "26-27", or "") into full label like "2026-27". */
    fun normalizeFinYear(fy: String, dateMillis: Long): String {
        val trimmed = fy.trim()
        if (trimmed.matches(Regex("^\\d{4}-\\d{2}$"))) return trimmed
        if (trimmed.matches(Regex("^\\d{2}-\\d{2}$"))) return "20$trimmed"
        val year = trimmed.toIntOrNull()
        if (year != null && year in 1900..2100) {
            return "$year-${((year + 1) % 100).toString().padStart(2, '0')}"
        }
        return financialYearLabel(dateMillis)
    }
}
