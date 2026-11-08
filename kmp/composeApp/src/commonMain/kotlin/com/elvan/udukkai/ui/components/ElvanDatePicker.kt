package com.elvan.udukkai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.utils.DateGroupUtils
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.localization.LocalAppLanguage
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.ElvanActionSheet
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * One UI-style calendar in the app's action sheet: the chosen date large at the top, month arrows with a tap on the
 * month to jump by year, round day cells with a ring on today. Days are UTC midnights, as everywhere in the app.
 * [minMillis] and [maxMillis] (inclusive) grey out days that cannot be chosen; the opening day is pulled inside them.
 */
@Composable
fun ElvanDatePicker(
    initialMillis: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
    title: String = "",
    minMillis: Long? = null,
    maxMillis: Long? = null,
    colors: ShellColors = rememberShellColors()
) {
    val start = ElvanCalendar.clampToRange(initialMillis, minMillis, maxMillis)
    CalendarSheet(range = false, initial = DaySpan(start), title, minMillis, maxMillis, colors, onDismiss) { s, _ -> onConfirm(s) }
}

/** The same calendar for a run of days: the first tap starts it, the second ends it (either order), a third starts again. Both ends come back as day starts. */
@Composable
fun ElvanDateRangePicker(
    initialStart: Long?,
    initialEnd: Long?,
    onDismiss: () -> Unit,
    onConfirm: (startMillis: Long, endMillis: Long) -> Unit,
    title: String = "",
    minMillis: Long? = null,
    maxMillis: Long? = null,
    colors: ShellColors = rememberShellColors()
) {
    val start = ElvanCalendar.clampToRange(initialStart ?: DateUtils.todayMillis(), minMillis, maxMillis)
    val end = initialEnd?.let { ElvanCalendar.clampToRange(it, minMillis, maxMillis) }?.takeIf { it >= start }
    CalendarSheet(range = true, initial = DaySpan(start, end), title, minMillis, maxMillis, colors, onDismiss, onConfirm)
}

@Composable
private fun CalendarSheet(
    range: Boolean,
    initial: DaySpan,
    title: String,
    min: Long?,
    max: Long?,
    colors: ShellColors,
    onDismiss: () -> Unit,
    onDone: (Long, Long) -> Unit
) {
    // Tamil Latin ("ta-Latn") shows English names: Tamil script would not match the rest of that screen
    val tamil = LocalAppLanguage.current == "ta"
    var span by remember { mutableStateOf(initial) }
    val focus = DateGroupUtils.getDateComponents(initial.start ?: DateUtils.todayMillis())
    var year by remember { mutableStateOf(focus.year) }
    var month by remember { mutableStateOf(focus.month) }
    var yearMode by remember { mutableStateOf(false) }
    val today = remember { DateUtils.todayMillis() }

    ElvanActionSheet(
        title = title,
        onDismissRequest = onDismiss,
        onConfirm = { span.start?.let { onDone(it, span.end ?: it) } },
        colors = colors,
        isConfirmFilled = true,
        slideDistance = 640.dp,
        customContent = {
            BoxWithConstraints {
                // Landscape and small windows get smaller day cells so the sheet still fits
                val cell = if (maxHeight < 560.dp) 34.dp else 42.dp
                Column(Modifier.fillMaxWidth()) {
                    Text(
                        text = headline(span, range, tamil),
                        style = TextStyle(fontFamily = LocalAppFontFamily.current, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    MonthBar(
                        label = "${DateGroupUtils.monthFullName(month, tamil)} $year",
                        yearMode = yearMode,
                        onToggleYear = { yearMode = !yearMode },
                        previousLabel = ElvanCalendar.shiftMonth(year, month, -1).let { (y, m) -> "${DateGroupUtils.monthFullName(m, tamil)} $y" },
                        nextLabel = ElvanCalendar.shiftMonth(year, month, 1).let { (y, m) -> "${DateGroupUtils.monthFullName(m, tamil)} $y" },
                        canGoBack = ElvanCalendar.shiftMonth(year, month, -1).let { (y, m) ->
                            min == null || DateUtils.millisOf(y, m, ElvanCalendar.daysInMonth(y, m)) >= ElvanCalendar.startOfDay(min)
                        },
                        canGoForward = ElvanCalendar.shiftMonth(year, month, 1).let { (y, m) ->
                            max == null || DateUtils.millisOf(y, m, 1) <= ElvanCalendar.startOfDay(max)
                        },
                        onShift = { delta -> ElvanCalendar.shiftMonth(year, month, delta).let { (y, m) -> year = y; month = m } },
                        colors = colors
                    )
                    Spacer(Modifier.height(4.dp))
                    // Same height in both modes, so toggling never moves the buttons
                    Box(Modifier.fillMaxWidth().height(cell * 6 + 28.dp)) {
                        if (yearMode) {
                            YearGrid(
                                selected = year,
                                from = min?.let { DateGroupUtils.getDateComponents(it).year } ?: 1990,
                                to = max?.let { DateGroupUtils.getDateComponents(it).year } ?: 2100,
                                colors = colors,
                                onPick = { year = it; yearMode = false }
                            )
                        } else {
                            DayGrid(year, month, cell, span, range, today, min, max, tamil, colors) { span = ElvanCalendar.tap(span, it, range) }
                        }
                    }
                }
            }
        }
    )
}

private fun headline(span: DaySpan, range: Boolean, tamil: Boolean): String {
    fun full(millis: Long): String {
        val c = DateGroupUtils.getDateComponents(millis)
        return "${DateGroupUtils.monthShortName(c.month, tamil)} ${c.day}, ${c.year}"
    }
    val s = span.start ?: return ""
    if (!range) {
        val c = DateGroupUtils.getDateComponents(s)
        return "${DateGroupUtils.weekdayShortName(c.dayOfWeek, tamil)}, ${full(s)}"
    }
    val e = span.end
    return if (e == null || e == s) full(s) else "${full(s)} – ${full(e)}"
}

@Composable
private fun MonthBar(
    label: String, yearMode: Boolean, onToggleYear: () -> Unit,
    previousLabel: String, nextLabel: String, canGoBack: Boolean, canGoForward: Boolean,
    onShift: (Int) -> Unit, colors: ShellColors
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Row(
            Modifier.clip(RoundedCornerShape(50)).clickable(onClick = onToggleYear).padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = TextStyle(fontFamily = LocalAppFontFamily.current, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary))
            Icon(
                MaterialSymbols.Rounded.KeyboardArrowDown, contentDescription = null, tint = colors.textSecondary,
                modifier = Modifier.size(20.dp).graphicsLayer { rotationZ = if (yearMode) 180f else 0f }
            )
        }
        Spacer(Modifier.weight(1f))
        ArrowButton(previousLabel, canGoBack, flip = true, colors) { onShift(-1) }
        ArrowButton(nextLabel, canGoForward, flip = false, colors) { onShift(1) }
    }
}

@Composable
private fun ArrowButton(description: String, enabled: Boolean, flip: Boolean, colors: ShellColors, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).clip(CircleShape).clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            MaterialSymbols.Rounded.ChevronRight, contentDescription = description,
            tint = colors.textPrimary.copy(alpha = if (enabled) 1f else 0.25f),
            modifier = Modifier.size(24.dp).graphicsLayer { rotationZ = if (flip) 180f else 0f }
        )
    }
}

@Composable
private fun DayGrid(
    year: Int, month: Int, cell: Dp, span: DaySpan, range: Boolean, today: Long,
    min: Long?, max: Long?, tamil: Boolean, colors: ShellColors, onTap: (Long) -> Unit
) {
    val ff = LocalAppFontFamily.current
    val onAccent = if (colors.isDark) Color.Black else Color.White
    val cells = remember(year, month) { ElvanCalendar.monthCells(year, month) }
    Column {
        Row(Modifier.height(28.dp)) {
            for (w in 0..6) {
                Text(
                    DateGroupUtils.weekdayNarrowName(w, tamil),
                    modifier = Modifier.weight(1f).align(Alignment.CenterVertically),
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontFamily = ff, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = colors.textSecondary)
                )
            }
        }
        for (row in 0 until 6) {
            Row {
                for (col in 0..6) {
                    val d = cells[row * 7 + col]
                    if (d == null) {
                        Spacer(Modifier.weight(1f).height(cell))
                        continue
                    }
                    val millis = DateUtils.millisOf(year, month, d)
                    val ok = ElvanCalendar.isSelectable(millis, min, max)
                    val isEnd = millis == span.start || (range && millis == span.end)
                    val inBetween = range && !isEnd && ElvanCalendar.contains(span, millis)
                    val isToday = millis == today
                    Box(
                        Modifier.weight(1f).height(cell).then(if (inBetween) Modifier.background(colors.accent.copy(alpha = 0.16f)) else Modifier),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            Modifier
                                .size(cell - 4.dp)
                                .clip(CircleShape)
                                .background(if (isEnd) colors.accent else Color.Transparent)
                                .then(if (isToday && !isEnd) Modifier.border(1.5.dp, colors.accent, CircleShape) else Modifier)
                                .clickable(enabled = ok, role = Role.Button) { onTap(millis) }
                                .semantics {
                                    contentDescription = "$d ${DateGroupUtils.monthFullName(month, tamil)} $year"
                                    selected = isEnd
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "$d",
                                style = TextStyle(
                                    fontFamily = ff, fontSize = 14.sp,
                                    fontWeight = if (isEnd || isToday) FontWeight.SemiBold else FontWeight.Normal,
                                    color = when {
                                        isEnd -> onAccent
                                        !ok -> colors.textPrimary.copy(alpha = 0.3f)
                                        isToday -> colors.accent
                                        else -> colors.textPrimary
                                    }
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun YearGrid(selected: Int, from: Int, to: Int, colors: ShellColors, onPick: (Int) -> Unit) {
    val years = remember(from, to) { (from..to).toList() }
    val state = rememberLazyGridState(initialFirstVisibleItemIndex = ((selected - from) / 4 * 4 - 4).coerceAtLeast(0))
    LazyVerticalGrid(GridCells.Fixed(4), state = state, modifier = Modifier.fillMaxSize()) {
        items(years) { y ->
            val on = y == selected
            Box(Modifier.padding(4.dp).height(40.dp).clip(RoundedCornerShape(50)).background(if (on) colors.accent else Color.Transparent).clickable(role = Role.Button) { onPick(y) }, contentAlignment = Alignment.Center) {
                Text(
                    "$y",
                    style = TextStyle(
                        fontFamily = LocalAppFontFamily.current, fontSize = 14.sp,
                        fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (on) (if (colors.isDark) Color.Black else Color.White) else colors.textPrimary
                    )
                )
            }
        }
    }
}
