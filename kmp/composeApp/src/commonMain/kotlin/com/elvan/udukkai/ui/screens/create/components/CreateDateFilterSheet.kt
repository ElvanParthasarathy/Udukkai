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
import com.elvan.udukkai.core.platform.ConfigureDialogWindow
import com.elvan.udukkai.core.utils.DateGroupUtils
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.LocalAppLanguage
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
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
 * - Custom Date Range: Opens Compose Material 3's DateRangePicker in a DatePickerDialog.
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

    val now = System.currentTimeMillis()
    val todayStart = (now / 86400000L) * 86400000L
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

    if (isOpen) {
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

    // Material 3 Compose DateRangePicker with clean compact typography
    if (showDateRangePicker) {
        val dateRangePickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = currentStartMillis ?: todayStart,
            initialSelectedEndDateMillis = currentEndMillis ?: todayEnd,
            yearRange = DatePickerDefaults.YearRange,
            selectableDates = DatePickerDefaults.AllDates
        )
        val dialogBg = if (isDark) Color(0xFF1E1E1E) else Color.White

        ConfigureDialogWindow(isDark = isDark)
        DatePickerDialog(
            onDismissRequest = { showDateRangePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val s = dateRangePickerState.selectedStartDateMillis
                        val e = dateRangePickerState.selectedEndDateMillis
                        if (s != null && e != null) {
                            val actualStart = minOf(s, e)
                            val actualEnd = maxOf(s, e) + 86399999L
                            onApplyFilter(actualStart, actualEnd, DateFilterPreset.CUSTOM)
                        } else if (s != null) {
                            val actualStart = s
                            val actualEnd = s + 86399999L
                            onApplyFilter(actualStart, actualEnd, DateFilterPreset.CUSTOM)
                        }
                        showDateRangePicker = false
                    }
                ) {
                    Text(
                        text = K.okBtn.tr(),
                        style = TextStyle(fontFamily = ff, fontWeight = FontWeight.Bold, color = colors.accent)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDateRangePicker = false }) {
                    Text(
                        text = K.cancelBtn.tr(),
                        style = TextStyle(fontFamily = ff, color = colors.textSecondary)
                    )
                }
            },
            colors = DatePickerDefaults.colors(containerColor = dialogBg)
        ) {
            MaterialTheme(
                typography = MaterialTheme.typography.copy(
                    headlineLarge = TextStyle(
                        fontFamily = ff,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 24.sp
                    ),
                    headlineMedium = TextStyle(
                        fontFamily = ff,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 24.sp
                    ),
                    headlineSmall = TextStyle(
                        fontFamily = ff,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 22.sp
                    ),
                    titleLarge = TextStyle(
                        fontFamily = ff,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 22.sp
                    ),
                    titleMedium = TextStyle(
                        fontFamily = ff,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 20.sp
                    ),
                    titleSmall = TextStyle(
                        fontFamily = ff,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp
                    ),
                    bodyLarge = TextStyle(
                        fontFamily = ff,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    ),
                    bodyMedium = TextStyle(
                        fontFamily = ff,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    ),
                    labelLarge = TextStyle(
                        fontFamily = ff,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 16.sp
                    )
                )
            ) {
                DateRangePicker(
                    state = dateRangePickerState,
                    title = null,
                    headline = {
                        DateRangePickerDefaults.DateRangePickerHeadline(
                            selectedStartDateMillis = dateRangePickerState.selectedStartDateMillis,
                            selectedEndDateMillis = dateRangePickerState.selectedEndDateMillis,
                            displayMode = dateRangePickerState.displayMode,
                            dateFormatter = remember { DatePickerDefaults.dateFormatter() },
                            modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp, bottom = 8.dp)
                        )
                    },
                    colors = DatePickerDefaults.colors(
                        containerColor = dialogBg,
                        headlineContentColor = colors.textPrimary,
                        titleContentColor = colors.textSecondary,
                        selectedDayContainerColor = colors.accent,
                        selectedDayContentColor = colors.surface,
                        dayInSelectionRangeContainerColor = colors.accent.copy(alpha = 0.2f),
                        dayInSelectionRangeContentColor = colors.textPrimary,
                        todayDateBorderColor = colors.accent,
                        todayContentColor = colors.accent
                    )
                )
            }
        }
    }
}
