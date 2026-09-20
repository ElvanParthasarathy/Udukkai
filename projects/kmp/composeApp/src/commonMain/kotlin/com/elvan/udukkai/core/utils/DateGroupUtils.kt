package com.elvan.udukkai.core.utils

import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr

data class DateComponents(
    val year: Int,
    val month: Int, // 1..12
    val day: Int,   // 1..31
    val dayOfWeek: Int // 0 = Sunday, 1 = Monday, ..., 6 = Saturday
)

data class IndexedItem<T>(
    val globalIndex: Int,
    val data: T
)

data class DateGroup<T>(
    val dayKey: Long,
    val dateMillis: Long,
    val items: List<IndexedItem<T>>
)

object DateGroupUtils {

    private val MONTH_NAMES_EN = arrayOf(
        "", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    private val MONTH_FULL_EN = arrayOf(
        "", "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    private val MONTH_NAMES_TA = arrayOf(
        "", "ஜன", "பிப்", "மார்ச்", "ஏப்", "மே", "ஜூன்",
        "ஜூலை", "ஆக", "செப்", "அக்", "நவ", "டிச"
    )

    private val MONTH_FULL_TA = arrayOf(
        "", "ஜனவரி", "பிப்ரவரி", "மார்ச்", "ஏப்ரல்", "மே", "ஜூன்",
        "ஜூலை", "ஆகஸ்ட்", "செப்டம்பர்", "அக்டோபர்", "நவம்பர்", "டிசம்பர்"
    )

    private val WEEKDAY_NAMES_EN = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    private val WEEKDAY_FULL_EN = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

    private val WEEKDAY_NAMES_TA = arrayOf("ஞாயிறு", "திங்கள்", "செவ்வாய்", "புதன்", "வியாழன்", "வெள்ளி", "சனி")

    /**
     * Decomposes epoch milliseconds to calendar date components without java.time dependencies.
     */
    fun getDateComponents(millis: Long): DateComponents {
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
        val dayOfWeek = ((days + 4) % 7 + 7) % 7

        return DateComponents(
            year = year,
            month = m.coerceIn(1, 12),
            day = d.coerceIn(1, 31),
            dayOfWeek = dayOfWeek
        )
    }

    /**
     * Unique day key for grouping (e.g. 20260918).
     */
    fun getDayKey(millis: Long): Long {
        val c = getDateComponents(millis)
        return (c.year.toLong() * 10000L) + (c.month.toLong() * 100L) + c.day.toLong()
    }

    /**
     * Formats Google Photos-style section header label.
     * Returns Pair(primaryLabel, secondaryLabel).
     */
    fun formatDateHeader(millis: Long, isBilingual: Boolean = true, primaryLang: String = "ta"): Pair<String, String> {
        val itemDays = (millis / 86400000L).toInt()
        val nowDays = (System.currentTimeMillis() / 86400000L).toInt()
        val diffDays = nowDays - itemDays

        val c = getDateComponents(millis)
        val mEn = MONTH_NAMES_EN[c.month]
        val mTa = MONTH_NAMES_TA[c.month]
        val wEn = WEEKDAY_NAMES_EN[c.dayOfWeek]
        val wTa = WEEKDAY_NAMES_TA[c.dayOfWeek]

        val isEnglishPrimary = primaryLang.lowercase().startsWith("en")

        val (taPrimary, enPrimary) = when (diffDays) {
            0 -> "இன்று" to "Today"
            1 -> "நேற்று" to "Yesterday"
            in 2..6 -> "$wTa, ${c.day} $mTa" to "$wEn, ${c.day} $mEn"
            else -> "${c.day} $mTa ${c.year}" to "${c.day} $mEn ${c.year}"
        }

        val primary = if (isEnglishPrimary) enPrimary else taPrimary
        val secondary = if (isBilingual) (if (isEnglishPrimary) taPrimary else enPrimary) else ""

        return Pair(primary, secondary)
    }

    /**
     * Weekday subtitle for header (e.g. "வெள்ளிக்கிழமை • Friday").
     */
    fun getWeekdaySubtitle(millis: Long, isBilingual: Boolean = true, primaryLang: String = "ta"): String {
        val c = getDateComponents(millis)
        val wTa = WEEKDAY_NAMES_TA[c.dayOfWeek]
        val wEn = WEEKDAY_FULL_EN[c.dayOfWeek]
        val isEnglishPrimary = primaryLang.lowercase().startsWith("en")

        return if (!isBilingual) {
            if (isEnglishPrimary) wEn else wTa
        } else if (isEnglishPrimary) {
            "$wEn  •  $wTa"
        } else {
            "$wTa  •  $wEn"
        }
    }

    /**
     * Compact date for floating fast-scroll pill (e.g. "18 Sep 2026" or "18 செப் • 18 Sep").
     */
    fun formatPillDate(millis: Long, isBilingual: Boolean = true, primaryLang: String = "ta"): String {
        val itemDays = (millis / 86400000L).toInt()
        val nowDays = (System.currentTimeMillis() / 86400000L).toInt()
        val diffDays = nowDays - itemDays

        val isEnglishPrimary = primaryLang.lowercase().startsWith("en")

        if (diffDays == 0) {
            return if (!isBilingual) (if (isEnglishPrimary) "Today" else "இன்று")
            else if (isEnglishPrimary) "Today  •  இன்று" else "இன்று  •  Today"
        }
        if (diffDays == 1) {
            return if (!isBilingual) (if (isEnglishPrimary) "Yesterday" else "நேற்று")
            else if (isEnglishPrimary) "Yesterday  •  நேற்று" else "நேற்று  •  Yesterday"
        }

        val c = getDateComponents(millis)
        val mEn = MONTH_NAMES_EN[c.month]
        val mTa = MONTH_NAMES_TA[c.month]
        val wEn = WEEKDAY_NAMES_EN[c.dayOfWeek]
        val wTa = WEEKDAY_NAMES_TA[c.dayOfWeek]
        val currentYear = getDateComponents(System.currentTimeMillis()).year
        val yearSuffix = if (c.year != currentYear) " ${c.year}" else ""

        return if (!isBilingual) {
            if (diffDays in 2..6) {
                if (isEnglishPrimary) "$wEn, ${c.day} $mEn" else "$wTa, ${c.day} $mTa"
            } else {
                if (isEnglishPrimary) "${c.day} $mEn$yearSuffix" else "${c.day} $mTa$yearSuffix"
            }
        } else if (isEnglishPrimary) {
            "${c.day} $mEn$yearSuffix  •  ${c.day} $mTa"
        } else {
            "${c.day} $mTa$yearSuffix  •  ${c.day} $mEn"
        }
    }

    /**
     * Groups chronological items into day-wise groups while preserving global index.
     */
    fun <T> groupItemsByDate(
        items: List<T>,
        dateSelector: (T) -> Long
    ): List<DateGroup<T>> {
        if (items.isEmpty()) return emptyList()

        val groupsMap = LinkedHashMap<Long, MutableList<IndexedItem<T>>>()
        val representativeDates = HashMap<Long, Long>()

        items.forEachIndexed { index, item ->
            val date = dateSelector(item)
            val dayKey = getDayKey(date)
            if (!groupsMap.containsKey(dayKey)) {
                groupsMap[dayKey] = mutableListOf()
                representativeDates[dayKey] = date
            }
            groupsMap[dayKey]!!.add(IndexedItem(index, item))
        }

        return groupsMap.map { (key, indexedItems) ->
            DateGroup(
                dayKey = key,
                dateMillis = representativeDates[key] ?: 0L,
                items = indexedItems
            )
        }
    }
}
