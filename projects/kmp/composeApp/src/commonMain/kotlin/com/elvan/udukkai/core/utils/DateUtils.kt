package com.elvan.udukkai.core.utils

object DateUtils {
    /**
     * Converts epoch milliseconds (UTC) to "DD/MM/YYYY" format.
     */
    fun formatDate(millis: Long): String = formatEpochMillis(millis)

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

        val adjustedYear = if (m <= 2) y - 1 else y
        val era = (if (adjustedYear >= 0) adjustedYear else adjustedYear - 399) / 400
        val yoe = adjustedYear - era * 400
        val mp = if (m > 2) m - 3 else m + 9
        val doy = (153 * mp + 2) / 5 + d - 1
        val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
        val days = era * 146097 + doe - 719468
        return days * 86400000L
    }
}
