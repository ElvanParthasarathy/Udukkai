package com.elvan.udukkai.ui.screens.create.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.utils.DateGroupUtils
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.LocalAppLanguage
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.ElvanDateRangePicker
import com.elvan.udukkai.ui.components.shell.ElvanSelectionBottomSheet

enum class DateFilterPreset {
    ALL,
    TODAY,
    YESTERDAY,
    THIS_WEEK,
    THIS_MONTH,
    LAST_MONTH,
    CUSTOM
}

/**
 * Clean Date Filter Modal built on ElvanSelectionBottomSheet matching Settings Dropdown selection 1:1.
 * Features:
 * - Bottom-anchored sheet with top rounded corners and drag handle.
 * - Clean list with monochrome checkmark (✓) for the active preset.
 * - Instant one-tap selection: Tapping any preset immediately applies filter and closes sheet with no jiggles.
 * - Custom Date Range: Opens the app's ElvanDateRangePicker.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDateFilterSheet(
    isOpen: Boolean,
    currentStartMillis: Long?,
    currentEndMillis: Long?,
    onDismissRequest: () -> Unit,
    onApplyFilter: (startMillis: Long?, endMillis: Long?, preset: DateFilterPreset) -> Unit,
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current
    val isDark = colors.isDark

    val todayStart = DateUtils.todayMillis()
    val todayEnd = todayStart + 86399999L

    val yesterdayStart = todayStart - 86400000L
    val yesterdayEnd = todayStart - 1L

    val cThisWeek = DateGroupUtils.getDateComponents(todayStart)
    val dayOffset = if (cThisWeek.dayOfWeek == 0) 6 else cThisWeek.dayOfWeek - 1
    val thisWeekStart = todayStart - (dayOffset * 86400000L)

    val cThisMonth = DateGroupUtils.getDateComponents(todayStart)
    val thisMonthStart = todayStart - ((cThisMonth.day - 1) * 86400000L)

    val lastMonthEnd = thisMonthStart - 1L
    val cLastMonth = DateGroupUtils.getDateComponents(lastMonthEnd)
    val lastMonthStart = lastMonthEnd - ((cLastMonth.day - 1) * 86400000L)

    val activePreset = remember(currentStartMillis, currentEndMillis) {
        when {
            currentStartMillis == null && currentEndMillis == null -> DateFilterPreset.ALL
            currentStartMillis == todayStart && currentEndMillis == todayEnd -> DateFilterPreset.TODAY
            currentStartMillis == yesterdayStart && currentEndMillis == yesterdayEnd -> DateFilterPreset.YESTERDAY
            currentStartMillis == thisWeekStart && currentEndMillis == todayEnd -> DateFilterPreset.THIS_WEEK
            currentStartMillis == thisMonthStart && currentEndMillis == todayEnd -> DateFilterPreset.THIS_MONTH
            currentStartMillis == lastMonthStart && currentEndMillis == lastMonthEnd -> DateFilterPreset.LAST_MONTH
            else -> DateFilterPreset.CUSTOM
        }
    }

    var showDateRangePicker by remember { mutableStateOf(false) }

    val filterTitle = K.dateFilter.tr()
    val allBillsLabel = K.allBills.tr()
    val todayLabel = K.today.tr()
    val yesterdayLabel = K.yesterday.tr()
    val thisWeekLabel = K.thisWeek.tr()
    val thisMonthLabel = K.thisMonth.tr()
    val lastMonthLabel = K.lastMonth.tr()
    val dateRangeSelectorLabel = K.dateRangeSelector.tr()
    val allRecordsLabel = K.allRecords.tr()
    val selectDateRangeLabel = K.selectDateRange.tr()

    val items = remember {
        listOf(
            DateFilterPreset.ALL,
            DateFilterPreset.TODAY,
            DateFilterPreset.YESTERDAY,
            DateFilterPreset.THIS_WEEK,
            DateFilterPreset.THIS_MONTH,
            DateFilterPreset.LAST_MONTH,
            DateFilterPreset.CUSTOM
        )
    }

    // The calendar is narrower than this sheet, so the sheet must not show around it
    if (isOpen && !showDateRangePicker) {
        ElvanSelectionBottomSheet(
            title = filterTitle,
            items = items,
            currentValue = activePreset,
            onDismissRequest = onDismissRequest,
            colors = colors,
            itemLabelBuilder = { preset ->
                when (preset) {
                    DateFilterPreset.ALL -> allBillsLabel
                    DateFilterPreset.TODAY -> todayLabel
                    DateFilterPreset.YESTERDAY -> yesterdayLabel
                    DateFilterPreset.THIS_WEEK -> thisWeekLabel
                    DateFilterPreset.THIS_MONTH -> thisMonthLabel
                    DateFilterPreset.LAST_MONTH -> lastMonthLabel
                    DateFilterPreset.CUSTOM -> dateRangeSelectorLabel
                }
            },
            subtitleBuilder = { preset ->
                when (preset) {
                    DateFilterPreset.ALL -> allRecordsLabel
                    DateFilterPreset.TODAY -> DateUtils.formatDate(todayStart)
                    DateFilterPreset.YESTERDAY -> DateUtils.formatDate(yesterdayStart)
                    DateFilterPreset.THIS_WEEK -> "${DateUtils.formatDate(thisWeekStart)} - ${DateUtils.formatDate(todayEnd)}"
                    DateFilterPreset.THIS_MONTH -> "${DateUtils.formatDate(thisMonthStart)} - ${DateUtils.formatDate(todayEnd)}"
                    DateFilterPreset.LAST_MONTH -> "${DateUtils.formatDate(lastMonthStart)} - ${DateUtils.formatDate(lastMonthEnd)}"
                    DateFilterPreset.CUSTOM -> {
                        if (activePreset == DateFilterPreset.CUSTOM && currentStartMillis != null) {
                            if (currentEndMillis != null && currentEndMillis != currentStartMillis + 86399999L) {
                                "${DateUtils.formatDate(currentStartMillis)} - ${DateUtils.formatDate(currentEndMillis)}"
                            } else {
                                DateUtils.formatDate(currentStartMillis)
                            }
                        } else {
                            selectDateRangeLabel
                        }
                    }
                }
            },

            onSelected = { preset ->
                when (preset) {
                    DateFilterPreset.ALL -> {
                        onApplyFilter(null, null, preset)
                    }
                    DateFilterPreset.TODAY -> {
                        onApplyFilter(todayStart, todayEnd, preset)
                    }
                    DateFilterPreset.YESTERDAY -> {
                        onApplyFilter(yesterdayStart, yesterdayEnd, preset)
                    }
                    DateFilterPreset.THIS_WEEK -> {
                        onApplyFilter(thisWeekStart, todayEnd, preset)
                    }
                    DateFilterPreset.THIS_MONTH -> {
                        onApplyFilter(thisMonthStart, todayEnd, preset)
                    }
                    DateFilterPreset.LAST_MONTH -> {
                        onApplyFilter(lastMonthStart, lastMonthEnd, preset)
                    }
                    DateFilterPreset.CUSTOM -> {
                        showDateRangePicker = true
                    }
                }
            }
        )
    }

    if (showDateRangePicker) {
        ElvanDateRangePicker(
            initialStart = currentStartMillis ?: todayStart,
            initialEnd = currentEndMillis ?: todayEnd,
            onDismiss = {
                showDateRangePicker = false
                onDismissRequest() // cancelled or not, the presets sheet does not come back: one step out, not two
            },
            onConfirm = { start, end ->
                onApplyFilter(start, end + 86399999L, DateFilterPreset.CUSTOM)
                showDateRangePicker = false
                onDismissRequest() // the presets sheet behind has nothing more to ask
            },
            colors = colors
        )
    }
}
