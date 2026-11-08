package com.elvan.udukkai.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val FIRST_YEAR = 1900
private const val LAST_YEAR = 2100

// Timings measured on One UI 8: a month step about 120ms, the mode change about 230ms, the caret 350ms
private val Decelerate = CubicBezierEasing(0f, 0f, 0.2f, 1f)
private val CaretEasing = CubicBezierEasing(0.22f, 0.25f, 0f, 1f)
private const val MONTH_STEP_MS = 130
private const val MODE_CHANGE_MS = 230

/**
 * One UI 8 calendar in the app's action sheet: the chosen date large at the top, a centred month with arrows either side,
 * and six fixed rows that page sideways under a weekday row that stays still (forward moves the month left, also by swipe).
 * A tap on the month swaps the grid in place for day, month and year wheels. Days are UTC midnights, as everywhere in the app.
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
    var wheels by remember { mutableStateOf(false) }
    val today = remember { DateUtils.todayMillis() }
    val scope = rememberCoroutineScope()
    val measurer = rememberTextMeasurer()
    val font = LocalAppFontFamily.current
    val dayText = remember(measurer, font) { DayText(measurer, TextStyle(fontFamily = font, fontSize = 15.sp)) }

    fun indexOf(millis: Long) = DateGroupUtils.getDateComponents(millis).let { ElvanCalendar.monthIndex(it.year, it.month) }
    val firstMonth = min?.let(::indexOf) ?: ElvanCalendar.monthIndex(FIRST_YEAR, 1)
    val lastMonth = max?.let(::indexOf) ?: ElvanCalendar.monthIndex(LAST_YEAR, 12)
    val pager = rememberPagerState(initialPage = indexOf(initial.start ?: today) - firstMonth) { lastMonth - firstMonth + 1 }
    val (year, month) = ElvanCalendar.monthOfIndex(firstMonth + pager.targetPage)

    // Taps come faster than the animation: each one aims from the last aim, and restarts the slide from wherever the page is
    var aim by remember { mutableStateOf(pager.currentPage) }
    fun slideTo(page: Int) {
        aim = page.coerceIn(0, pager.pageCount - 1)
        scope.launch { pager.animateScrollToPage(aim, animationSpec = tween(MONTH_STEP_MS, easing = Decelerate)) }
    }
    fun shift(delta: Int) = slideTo((if (pager.isScrollInProgress) aim else pager.currentPage) + delta)
    fun jumpTo(index: Int) { scope.launch { pager.scrollToPage(index - firstMonth) } }

    ElvanActionSheet(
        title = title,
        onDismissRequest = onDismiss,
        onConfirm = { span.start?.let { onDone(it, span.end ?: it) } },
        colors = colors,
        isConfirmFilled = true,
        slideDistance = 32.dp,
        fadeWithSlide = true,
        customContent = {
            BoxWithConstraints {
                // Landscape and small windows get smaller day cells so the sheet still fits
                val cell = if (maxHeight < 560.dp) 34.dp else 42.dp
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = headline(span, range, tamil),
                        style = TextStyle(fontFamily = LocalAppFontFamily.current, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    MonthBar(
                        label = "${DateGroupUtils.monthFullName(month, tamil)} $year",
                        wheels = wheels,
                        onToggle = {
                            // The wheels show the chosen day, so the month behind them follows it
                            if (!wheels && !range) span.start?.let { jumpTo(indexOf(it)) }
                            wheels = !wheels
                        },
                        previousLabel = ElvanCalendar.shiftMonth(year, month, -1).let { (y, m) -> "${DateGroupUtils.monthFullName(m, tamil)} $y" },
                        nextLabel = ElvanCalendar.shiftMonth(year, month, 1).let { (y, m) -> "${DateGroupUtils.monthFullName(m, tamil)} $y" },
                        canGoBack = pager.targetPage > 0,
                        canGoForward = pager.targetPage < pager.pageCount - 1,
                        onShift = ::shift,
                        colors = colors
                    )
                    Spacer(Modifier.height(4.dp))
                    // The same height in both modes, so nothing moves when they swap
                    Crossfade(wheels, Modifier.fillMaxWidth().height(cell * 6 + 28.dp), animationSpec = tween(MODE_CHANGE_MS), label = "calendarMode") { showWheels ->
                        if (showWheels) {
                            DateWheels(
                                range = range,
                                date = span.start ?: DateUtils.millisOf(year, month, 1),
                                shownYear = year,
                                shownMonth = month,
                                firstYear = min?.let { DateGroupUtils.getDateComponents(it).year } ?: FIRST_YEAR,
                                lastYear = max?.let { DateGroupUtils.getDateComponents(it).year } ?: LAST_YEAR,
                                tamil = tamil,
                                colors = colors,
                                onDate = { y, m, d ->
                                    val day = ElvanCalendar.clampToRange(ElvanCalendar.dayIn(y, m, d), min, max)
                                    span = DaySpan(day)
                                    jumpTo(indexOf(day))
                                },
                                onMonth = { y, m -> jumpTo(ElvanCalendar.monthIndex(y, m).coerceIn(firstMonth, lastMonth)) }
                            )
                        } else {
                            Column(Modifier.fillMaxSize()) {
                                WeekdayRow(tamil, colors)
                                HorizontalPager(
                                    state = pager,
                                    beyondViewportPageCount = 1,
                                    flingBehavior = PagerDefaults.flingBehavior(pager, snapAnimationSpec = tween(320, easing = Decelerate)),
                                    modifier = Modifier.fillMaxWidth().height(cell * 6)
                                ) { page ->
                                    val (y, m) = ElvanCalendar.monthOfIndex(firstMonth + page)
                                    MonthGrid(y, m, cell, span, range, today, min, max, tamil, colors, dayText) { tapped, inMonth ->
                                        span = ElvanCalendar.tap(span, tapped, range)
                                        if (!inMonth) slideTo(indexOf(tapped) - firstMonth)
                                    }
                                }
                            }
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
    label: String, wheels: Boolean, onToggle: () -> Unit,
    previousLabel: String, nextLabel: String, canGoBack: Boolean, canGoForward: Boolean,
    onShift: (Int) -> Unit, colors: ShellColors
) {
    val caret by animateFloatAsState(if (wheels) 180f else 0f, tween(350, easing = CaretEasing), label = "caret")
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        // The arrows go at once when the wheels open, and keep their space so the month does not move
        Box(Modifier.size(44.dp)) { if (!wheels) ArrowButton(previousLabel, canGoBack, flip = true, colors) { onShift(-1) } }
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Row(
                Modifier.clip(RoundedCornerShape(50)).clickable(role = Role.Button, onClick = onToggle).padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, style = TextStyle(fontFamily = LocalAppFontFamily.current, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary))
                Icon(
                    MaterialSymbols.Rounded.KeyboardArrowDown, contentDescription = null, tint = colors.textSecondary,
                    modifier = Modifier.size(20.dp).graphicsLayer { rotationZ = caret }
                )
            }
        }
        Box(Modifier.size(44.dp)) { if (!wheels) ArrowButton(nextLabel, canGoForward, flip = false, colors) { onShift(1) } }
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
private fun WeekdayRow(tamil: Boolean, colors: ShellColors) {
    Row(Modifier.fillMaxWidth().height(28.dp)) {
        for (w in 0..6) {
            Text(
                DateGroupUtils.weekdayNarrowName(w, tamil),
                modifier = Modifier.weight(1f).align(Alignment.CenterVertically),
                textAlign = TextAlign.Center,
                style = TextStyle(fontFamily = LocalAppFontFamily.current, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = if (w == 0) colors.error else colors.textSecondary)
            )
        }
    }
}

/** Laid-out day numbers, measured once for the whole sheet: paging must not measure text. */
private class DayText(private val measurer: TextMeasurer, private val style: TextStyle) {
    private val laidOut = HashMap<Int, TextLayoutResult>()
    fun of(day: Int, bold: Boolean): TextLayoutResult =
        laidOut.getOrPut(if (bold) -day else day) { measurer.measure("$day", style.copy(fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal)) }
}

/**
 * One month, drawn in one pass. A page slides in on every month step, and composing 42 full cells for it held the main
 * thread for about 250ms on the phone; only the screen reader still needs a node per day.
 */
@Composable
private fun MonthGrid(
    year: Int, month: Int, cell: Dp, span: DaySpan, range: Boolean, today: Long,
    min: Long?, max: Long?, tamil: Boolean, colors: ShellColors, text: DayText, onTap: (Long, Boolean) -> Unit
) {
    val onAccent = if (colors.isDark) Color.Black else Color.White
    val band = colors.accent.copy(alpha = 0.16f)
    val cells = remember(year, month) { ElvanCalendar.monthCells(year, month) }
    val tap by rememberUpdatedState(onTap)
    val allowed by rememberUpdatedState { c: MonthCell -> ElvanCalendar.isSelectable(c.millis, min, max) }
    fun chosen(c: MonthCell) = c.inMonth && (c.millis == span.start || (range && c.millis == span.end))
    Box(
        Modifier.fillMaxWidth().height(cell * 6)
            .pointerInput(cells) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val up = waitForUpOrCancellation() ?: return@awaitEachGesture
                    if ((up.position - down.position).getDistance() > viewConfiguration.touchSlop) return@awaitEachGesture // a swipe is not a tap
                    val row = (up.position.y / size.height * 6).toInt().coerceIn(0, 5)
                    val col = (up.position.x / size.width * 7).toInt().coerceIn(0, 6)
                    val c = cells[row * 7 + col]
                    if (allowed(c)) tap(c.millis, c.inMonth)
                }
            }
            .drawBehind {
                val w = size.width / 7
                val h = size.height / 6
                val radius = (cell - 4.dp).toPx() / 2
                val joined = range && span.end != null && span.end != span.start
                cells.forEachIndexed { i, c ->
                    val col = i % 7
                    val left = col * w
                    val top = (i / 7) * h
                    val center = Offset(left + w / 2, top + h / 2)
                    val on = chosen(c)
                    val isStart = on && c.millis == span.start
                    when {
                        c.inMonth && range && !on && ElvanCalendar.contains(span, c.millis) -> drawRect(band, Offset(left, top), Size(w, h))
                        on && joined -> drawRect(band, Offset(if (isStart) left + w / 2 else left, top), Size(w / 2, h)) // from the middle of the circle at each end
                    }
                    val isToday = c.inMonth && c.millis == today
                    if (on) drawCircle(colors.accent, radius, center)
                    else if (isToday) drawCircle(colors.textPrimary.copy(alpha = 0.45f), radius, center, style = Stroke(1.dp.toPx()))
                    val base = if (col == 0) colors.error else colors.textPrimary
                    val ink = when {
                        on -> onAccent
                        !allowed(c) -> base.copy(alpha = 0.3f)
                        !c.inMonth -> base.copy(alpha = 0.4f)
                        else -> base
                    }
                    val number = text.of(c.day, on || isToday)
                    drawText(number, ink, Offset(center.x - number.size.width / 2f, center.y - number.size.height / 2f))
                }
            }
    ) {
        Column(Modifier.fillMaxSize()) {
            for (row in 0 until 6) {
                Row(Modifier.fillMaxWidth().weight(1f)) {
                    for (col in 0..6) {
                        val c = cells[row * 7 + col]
                        Spacer(
                            Modifier.weight(1f).fillMaxHeight().semantics {
                                val of = DateGroupUtils.getDateComponents(c.millis)
                                contentDescription = "${c.day} ${DateGroupUtils.monthFullName(of.month, tamil)} ${of.year}"
                                role = Role.Button
                                selected = chosen(c)
                                if (!allowed(c)) disabled()
                                onClick { if (allowed(c)) { tap(c.millis, c.inMonth); true } else false }
                            }
                        )
                    }
                }
            }
        }
    }
}

/** Day, month and year wheels for one date. A range has no single day to set, so there only month and year show, to move the calendar. */
@Composable
private fun DateWheels(
    range: Boolean, date: Long, shownYear: Int, shownMonth: Int, firstYear: Int, lastYear: Int, tamil: Boolean, colors: ShellColors,
    onDate: (Int, Int, Int) -> Unit, onMonth: (Int, Int) -> Unit
) {
    val c = DateGroupUtils.getDateComponents(date)
    val y = if (range) shownYear else c.year
    val m = if (range) shownMonth else c.month
    val latestDay by rememberUpdatedState(c.day)
    val set: (Int, Int, Int) -> Unit = { ny, nm, nd -> if (range) onMonth(ny, nm) else onDate(ny, nm, nd) }
    val years = remember(firstYear, lastYear) { (firstYear..lastYear).toList() }
    val months = remember(tamil) { (1..12).map { DateGroupUtils.monthShortName(it, tamil) } }
    Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
        if (!range) {
            val days = remember(y, m) { (1..ElvanCalendar.daysInMonth(y, m)).map { "$it" } }
            Wheel(days, c.day - 1, Modifier.weight(1f), colors) { set(y, m, it + 1) }
        }
        Wheel(months, m - 1, Modifier.weight(1f), colors) { set(y, it + 1, latestDay) }
        Wheel(remember(years) { years.map { "$it" } }, (y - firstYear).coerceIn(0, years.lastIndex), Modifier.weight(1f), colors) { set(years[it], m, latestDay) }
    }
}

private val WHEEL_ROW = 56.dp

/** Three rows showing, the middle one chosen. Reports the row as it reaches the middle, and follows [selected] when it is changed from outside. */
@Composable
private fun Wheel(labels: List<String>, selected: Int, modifier: Modifier, colors: ShellColors, onSelect: (Int) -> Unit) {
    val state = rememberLazyListState(initialFirstVisibleItemIndex = selected)
    val scope = rememberCoroutineScope()
    val rowPx = with(LocalDensity.current) { WHEEL_ROW.roundToPx() }
    val middle by remember(labels.size) {
        derivedStateOf { (state.firstVisibleItemIndex + if (state.firstVisibleItemScrollOffset > rowPx / 2) 1 else 0).coerceIn(0, labels.lastIndex) }
    }
    val report by rememberUpdatedState(onSelect)
    LaunchedEffect(state, labels.size) {
        snapshotFlow { middle to state.isScrollInProgress }.collectLatest { (row, moving) -> if (moving) report(row) }
    }
    LaunchedEffect(selected, labels.size) {
        if (!state.isScrollInProgress && middle != selected) state.scrollToItem(selected)
    }
    LazyColumn(
        state = state,
        flingBehavior = rememberSnapFlingBehavior(state, SnapPosition.Center),
        contentPadding = PaddingValues(vertical = WHEEL_ROW),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.height(WHEEL_ROW * 3)
    ) {
        itemsIndexed(labels) { index, label ->
            val on = index == middle
            Box(
                Modifier.fillMaxWidth().height(WHEEL_ROW)
                    .clickable(role = Role.Button) { scope.launch { state.animateScrollToItem(index) } }
                    .semantics { this.selected = on },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    style = TextStyle(
                        fontFamily = LocalAppFontFamily.current, fontSize = if (on) 26.sp else 22.sp,
                        fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal,
                        color = colors.textPrimary.copy(alpha = if (on) 1f else 0.3f)
                    )
                )
            }
        }
    }
}
