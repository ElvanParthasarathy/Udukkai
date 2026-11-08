package com.elvan.udukkai.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.core.utils.DateGroupUtils
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.localization.LocalAppLanguage
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.ElvanActionSheet
import kotlinx.coroutines.launch

private const val FIRST_YEAR = 1900
private const val LAST_YEAR = 2100

// Measured on One UI 8: a month step about 120ms, the mode change about 230ms, the caret 350ms
private val Decelerate = CubicBezierEasing(0f, 0f, 0.2f, 1f)
private val CaretEasing = CubicBezierEasing(0.22f, 0.25f, 0f, 1f)
private const val MONTH_STEP_MS = 130
private const val MODE_CHANGE_MS = 230

// The One UI 8 calendar's layout, in dp
private val CARD_TOP = 20.dp
private val HEADER_HEIGHT = 36.dp
private val SIDE_MARGIN = 15.dp
private val ROW_HEIGHT = 32.dp
private val WEEKDAY_TO_DAYS = 18.dp
private val BELOW_DAYS = 12.dp
private val DAY_CIRCLE_RADIUS = 14.dp

/** One UI sizes its calendar text in dp, so it stays this size whatever the phone's font scale. */
@Composable
internal fun Dp.fixedSp(): TextUnit = with(LocalDensity.current) { this@fixedSp.toSp() }

/** The calendar's colours are the app's own, so it matches the sheets and cards around it. */
private class CalendarInk(colors: ShellColors) {
    val text = colors.textPrimary
    val strong = colors.textPrimary
    val weekday = colors.textSecondary
    val sunday = colors.error
    val accent = colors.accent
    val onSelected = if (colors.isDark) Color.Black else Color.White
}

/**
 * The One UI 8 calendar, in the picker form of the action sheet: a centred month with arrows either side, a weekday row
 * that stays still, and six fixed rows that page sideways under it (forward moves the month left, also by swipe). A tap on
 * the month swaps the grid in place for day, month and year wheels. Days are UTC midnights, as everywhere in the app.
 * [minMillis] and [maxMillis] (inclusive) grey out days that cannot be chosen; the opening day is pulled inside them.
 */
@Composable
fun ElvanDatePicker(
    initialMillis: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
    minMillis: Long? = null,
    maxMillis: Long? = null,
    colors: ShellColors = rememberShellColors()
) {
    val start = ElvanCalendar.clampToRange(initialMillis, minMillis, maxMillis)
    CalendarSheet(range = false, initial = DaySpan(start), minMillis, maxMillis, colors, onDismiss) { s, _ -> onConfirm(s) }
}

/** The same calendar for a run of days: the first tap starts it, the second ends it (either order), a third starts again. Both ends come back as day starts. */
@Composable
fun ElvanDateRangePicker(
    initialStart: Long?,
    initialEnd: Long?,
    onDismiss: () -> Unit,
    onConfirm: (startMillis: Long, endMillis: Long) -> Unit,
    minMillis: Long? = null,
    maxMillis: Long? = null,
    colors: ShellColors = rememberShellColors()
) {
    val start = ElvanCalendar.clampToRange(initialStart ?: DateUtils.todayMillis(), minMillis, maxMillis)
    val end = initialEnd?.let { ElvanCalendar.clampToRange(it, minMillis, maxMillis) }?.takeIf { it >= start }
    CalendarSheet(range = true, initial = DaySpan(start, end), minMillis, maxMillis, colors, onDismiss, onConfirm)
}

@Composable
private fun CalendarSheet(
    range: Boolean,
    initial: DaySpan,
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
    val dayFontSize = 15.dp.fixedSp()
    val dayText = remember(measurer, font, dayFontSize) { DayText(measurer, TextStyle(fontFamily = font, fontSize = dayFontSize)) }
    val ink = remember(colors) { CalendarInk(colors) }

    fun indexOf(millis: Long) = DateGroupUtils.getDateComponents(millis).let { ElvanCalendar.monthIndex(it.year, it.month) }
    val firstMonth = min?.let(::indexOf) ?: ElvanCalendar.monthIndex(FIRST_YEAR, 1)
    val lastMonth = max?.let(::indexOf) ?: ElvanCalendar.monthIndex(LAST_YEAR, 12)
    val pager = rememberPagerState(initialPage = indexOf(initial.start ?: today) - firstMonth) { lastMonth - firstMonth + 1 }
    // The month on show: one value that the header and the wheels both read. The pager follows it when the grid is on screen
    var shown by remember { mutableStateOf(firstMonth + pager.currentPage) }
    val (year, month) = ElvanCalendar.monthOfIndex(shown)
    LaunchedEffect(pager) { snapshotFlow { pager.targetPage }.collect { shown = firstMonth + it } }
    LaunchedEffect(wheels) { if (!wheels) pager.scrollToPage(shown - firstMonth) }

    // Taps come faster than the animation: each one aims from the last aim, and restarts the slide from wherever the page is
    var aim by remember { mutableStateOf(pager.currentPage) }
    fun slideTo(page: Int) {
        aim = page.coerceIn(0, pager.pageCount - 1)
        scope.launch { pager.animateScrollToPage(aim, animationSpec = tween(MONTH_STEP_MS, easing = Decelerate)) }
    }
    fun shift(delta: Int) = slideTo((if (pager.isScrollInProgress) aim else pager.currentPage) + delta)
    fun show(index: Int) { shown = index.coerceIn(firstMonth, lastMonth) }

    ElvanActionSheet(
        title = "",
        onDismissRequest = onDismiss,
        onConfirm = { span.start?.let { onDone(it, span.end ?: it) } },
        colors = colors,
        pickerCard = true,
        slideDistance = 440.dp,
        customContent = {
            BoxWithConstraints {
                // A short window (landscape) gets shorter rows so the sheet still fits
                val row = if (maxHeight < 430.dp) 24.dp else ROW_HEIGHT
                Column(Modifier.fillMaxWidth().padding(top = CARD_TOP)) {
                    MonthBar(
                        label = "${DateGroupUtils.monthFullName(month, tamil)} $year",
                        wheels = wheels,
                        onToggle = {
                            // The wheels show the chosen day, so the month behind them follows it
                            if (!wheels && !range) span.start?.let { show(indexOf(it)) }
                            wheels = !wheels
                        },
                        previousLabel = ElvanCalendar.shiftMonth(year, month, -1).let { (y, m) -> "${DateGroupUtils.monthFullName(m, tamil)} $y" },
                        nextLabel = ElvanCalendar.shiftMonth(year, month, 1).let { (y, m) -> "${DateGroupUtils.monthFullName(m, tamil)} $y" },
                        canGoBack = shown > firstMonth,
                        canGoForward = shown < lastMonth,
                        onShift = ::shift,
                        ink = ink
                    )
                    // The same height in both modes, so nothing moves when they swap
                    Crossfade(wheels, Modifier.fillMaxWidth().height(ROW_HEIGHT + WEEKDAY_TO_DAYS + row * 6 + BELOW_DAYS), animationSpec = tween(MODE_CHANGE_MS), label = "calendarMode") { showWheels ->
                        if (showWheels) {
                            DateWheels(
                                range = range,
                                date = span.start ?: DateUtils.millisOf(year, month, 1),
                                shownYear = year,
                                shownMonth = month,
                                firstYear = min?.let { DateGroupUtils.getDateComponents(it).year } ?: FIRST_YEAR,
                                lastYear = max?.let { DateGroupUtils.getDateComponents(it).year } ?: LAST_YEAR,
                                tamil = tamil,
                                ink = ink,
                                onDate = { y, m, d ->
                                    val day = ElvanCalendar.clampToRange(ElvanCalendar.dayIn(y, m, d), min, max)
                                    span = DaySpan(day)
                                    show(indexOf(day))
                                },
                                onMonth = { y, m -> show(ElvanCalendar.monthIndex(y, m)) }
                            )
                        } else {
                            Column(Modifier.fillMaxSize().padding(horizontal = SIDE_MARGIN)) {
                                WeekdayRow(tamil, ink)
                                Spacer(Modifier.height(WEEKDAY_TO_DAYS))
                                HorizontalPager(
                                    state = pager,
                                    beyondViewportPageCount = 1,
                                    flingBehavior = PagerDefaults.flingBehavior(pager, snapAnimationSpec = tween(320, easing = Decelerate)),
                                    modifier = Modifier.fillMaxWidth().height(row * 6)
                                ) { page ->
                                    val (y, m) = ElvanCalendar.monthOfIndex(firstMonth + page)
                                    MonthGrid(y, m, row, span, range, today, min, max, tamil, colors, ink, dayText) { tapped, inMonth ->
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

@Composable
private fun MonthBar(
    label: String, wheels: Boolean, onToggle: () -> Unit,
    previousLabel: String, nextLabel: String, canGoBack: Boolean, canGoForward: Boolean,
    onShift: (Int) -> Unit, ink: CalendarInk
) {
    val caret by animateFloatAsState(if (wheels) 180f else 0f, tween(350, easing = CaretEasing), label = "caret")
    Row(Modifier.fillMaxWidth().height(HEADER_HEIGHT).padding(horizontal = SIDE_MARGIN), verticalAlignment = Alignment.CenterVertically) {
        // The arrows go at once when the wheels open, and keep their space so the month does not move
        Box(Modifier.size(HEADER_HEIGHT)) { if (!wheels) ArrowButton(previousLabel, canGoBack, flip = true, ink) { onShift(-1) } }
        Box(Modifier.weight(1f).offset(x = 3.dp), contentAlignment = Alignment.Center) {
            Row(
                Modifier.clip(RoundedCornerShape(50)).clickable(role = Role.Button, onClick = onToggle).padding(horizontal = 8.dp).height(HEADER_HEIGHT),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, style = TextStyle(fontFamily = LocalAppFontFamily.current, fontSize = 16.dp.fixedSp(), color = ink.text), maxLines = 1)
                Canvas(Modifier.size(width = 22.dp, height = 24.dp).graphicsLayer { rotationZ = caret }) {
                    val w = 4.dp.toPx()
                    val h = 2.75.dp.toPx()
                    val c = Offset(size.width / 2, size.height / 2)
                    val tip = Path().apply {
                        moveTo(c.x - w, c.y - h)
                        lineTo(c.x + w, c.y - h)
                        lineTo(c.x, c.y + h)
                        close()
                    }
                    drawPath(tip, ink.text, style = Fill)
                    drawPath(tip, ink.text, style = Stroke(1.dp.toPx(), join = StrokeJoin.Round))
                }
            }
        }
        Box(Modifier.size(HEADER_HEIGHT)) { if (!wheels) ArrowButton(nextLabel, canGoForward, flip = false, ink) { onShift(1) } }
    }
}

@Composable
private fun ArrowButton(description: String, enabled: Boolean, flip: Boolean, ink: CalendarInk, onClick: () -> Unit) {
    Box(
        Modifier.size(HEADER_HEIGHT).clip(CircleShape).clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // One UI's chevron is thinner than the icon set's
        Canvas(Modifier.size(24.dp).graphicsLayer { rotationZ = if (flip) 180f else 0f }.semantics { contentDescription = description }) {
            val half = 5.5.dp.toPx()
            val c = Offset(size.width / 2, size.height / 2)
            drawPath(
                Path().apply {
                    moveTo(c.x - half / 2, c.y - half)
                    lineTo(c.x + half / 2, c.y)
                    lineTo(c.x - half / 2, c.y + half)
                },
                ink.text.copy(alpha = if (enabled) 1f else 0.25f),
                style = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

@Composable
private fun WeekdayRow(tamil: Boolean, ink: CalendarInk) {
    Row(Modifier.fillMaxWidth().height(ROW_HEIGHT)) {
        for (w in 0..6) {
            Text(
                DateGroupUtils.weekdayAbbreviation(w, tamil),
                maxLines = 1,
                modifier = Modifier.weight(1f).align(Alignment.CenterVertically).offset(y = 2.dp),
                textAlign = TextAlign.Center,
                style = TextStyle(fontFamily = LocalAppFontFamily.current, fontSize = 11.dp.fixedSp(), color = if (w == 0) ink.sunday else ink.weekday)
            )
        }
    }
}

/** Laid-out day numbers, measured once for the whole sheet: paging must not measure text. */
private class DayText(private val measurer: TextMeasurer, private val style: TextStyle) {
    private val laidOut = HashMap<Int, TextLayoutResult>()
    fun of(day: Int): TextLayoutResult = laidOut.getOrPut(day) { measurer.measure("$day", style) }
}

/**
 * One month, drawn in one pass. A page slides in on every month step, and composing 42 full cells for it held the main
 * thread for about 250ms on the phone; only the screen reader still needs a node per day.
 */
@Composable
private fun MonthGrid(
    year: Int, month: Int, row: Dp, span: DaySpan, range: Boolean, today: Long,
    min: Long?, max: Long?, tamil: Boolean, colors: ShellColors, ink: CalendarInk, text: DayText, onTap: (Long, Boolean) -> Unit
) {
    val band = ink.accent.copy(alpha = 0.16f)
    val cells = remember(year, month) { ElvanCalendar.monthCells(year, month) }
    val tap by rememberUpdatedState(onTap)
    val allowed by rememberUpdatedState { c: MonthCell -> ElvanCalendar.isSelectable(c.millis, min, max) }
    fun chosen(c: MonthCell) = c.inMonth && (c.millis == span.start || (range && c.millis == span.end))
    Box(
        Modifier.fillMaxWidth().height(row * 6)
            .pointerInput(cells) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val up = waitForUpOrCancellation() ?: return@awaitEachGesture
                    if ((up.position - down.position).getDistance() > viewConfiguration.touchSlop) return@awaitEachGesture // a swipe is not a tap
                    val r = (up.position.y / size.height * 6).toInt().coerceIn(0, 5)
                    val col = (up.position.x / size.width * 7).toInt().coerceIn(0, 6)
                    val c = cells[r * 7 + col]
                    if (allowed(c)) tap(c.millis, c.inMonth)
                }
            }
            .drawBehind {
                val w = size.width / 7
                val h = size.height / 6
                val radius = DAY_CIRCLE_RADIUS.toPx().coerceAtMost(h / 2)
                val joined = range && span.end != null && span.end != span.start
                cells.forEachIndexed { i, c ->
                    val col = i % 7
                    val left = col * w
                    val top = (i / 7) * h
                    val center = Offset(left + w / 2, top + h / 2)
                    val on = chosen(c)
                    val isStart = on && c.millis == span.start
                    val bandTop = center.y - radius
                    when {
                        c.inMonth && range && !on && ElvanCalendar.contains(span, c.millis) -> drawRect(band, Offset(left, bandTop), Size(w, radius * 2))
                        on && joined -> drawRect(band, Offset(if (isStart) left + w / 2 else left, bandTop), Size(w / 2, radius * 2)) // from the middle of the circle at each end
                    }
                    if (on) drawCircle(ink.accent, radius, center)
                    else if (c.inMonth && c.millis == today) drawCircle(ink.text.copy(alpha = 0.45f), radius, center, style = Stroke(1.dp.toPx()))
                    val base = if (col == 0) ink.sunday else ink.text
                    val color = when {
                        on -> ink.onSelected
                        !allowed(c) || !c.inMonth -> base.copy(alpha = 0.4f)
                        else -> base
                    }
                    val number = text.of(c.day)
                    drawText(number, color, Offset(center.x - number.size.width / 2f, center.y - number.size.height / 2f + 0.6.dp.toPx()))
                }
            }
    ) {
        Column(Modifier.fillMaxSize()) {
            for (r in 0 until 6) {
                Row(Modifier.fillMaxWidth().weight(1f)) {
                    for (col in 0..6) {
                        val c = cells[r * 7 + col]
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
    range: Boolean, date: Long, shownYear: Int, shownMonth: Int, firstYear: Int, lastYear: Int, tamil: Boolean, ink: CalendarInk,
    onDate: (Int, Int, Int) -> Unit, onMonth: (Int, Int) -> Unit
) {
    val c = DateGroupUtils.getDateComponents(date)
    val y = if (range) shownYear else c.year
    val m = if (range) shownMonth else c.month
    val latestDay by rememberUpdatedState(c.day)
    val set: (Int, Int, Int) -> Unit = { ny, nm, nd -> if (range) onMonth(ny, nm) else onDate(ny, nm, nd) }
    val years = remember(firstYear, lastYear) { (firstYear..lastYear).toList() }
    val months = remember(tamil) { (1..12).map { DateGroupUtils.monthShortName(it, tamil) } }
    Row(Modifier.fillMaxSize().padding(start = 27.dp, end = 38.dp), verticalAlignment = Alignment.CenterVertically) {
        if (!range) {
            val days = remember(y, m) { (1..ElvanCalendar.daysInMonth(y, m)).map { "$it" } }
            Wheel(days, c.day - 1, Modifier.weight(85f), ink) { set(y, m, it + 1) }
        }
        Wheel(months, m - 1, Modifier.weight(95f), ink) { set(y, it + 1, latestDay) }
        Wheel(remember(years) { years.map { "$it" } }, (y - firstYear).coerceIn(0, years.lastIndex), Modifier.weight(95f), ink) { set(years[it], m, latestDay) }
    }
}

private val WHEEL_ROW = 53.5.dp

/**
 * Three rows showing, the middle one chosen. The choice is reported once the wheel has stopped, not on every row it
 * passes: reporting each row rebuilt the calendar behind it dozens of times in one fling and left the header and the wheel
 * out of step. It follows [selected] when that is changed from outside (a day pulled back to the end of a shorter month).
 */
@Composable
private fun Wheel(labels: List<String>, selected: Int, modifier: Modifier, ink: CalendarInk, onSelect: (Int) -> Unit) {
    val last = labels.lastIndex
    val state = rememberLazyListState(initialFirstVisibleItemIndex = selected.coerceIn(0, last))
    val scope = rememberCoroutineScope()
    val rowPx = with(LocalDensity.current) { WHEEL_ROW.roundToPx() }
    val middle by remember(last) {
        derivedStateOf { (state.firstVisibleItemIndex + if (state.firstVisibleItemScrollOffset > rowPx / 2) 1 else 0).coerceIn(0, last) }
    }
    val chosen by rememberUpdatedState(selected)
    val report by rememberUpdatedState(onSelect)
    LaunchedEffect(state) {
        snapshotFlow { state.isScrollInProgress }.collect { moving -> if (!moving && middle != chosen) report(middle) }
    }
    LaunchedEffect(selected, labels.size) {
        if (!state.isScrollInProgress && middle != selected) state.scrollToItem(selected.coerceIn(0, last))
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
                    maxLines = 1,
                    style = TextStyle(
                        fontFamily = LocalAppFontFamily.current, fontSize = 29.dp.fixedSp(),
                        fontWeight = if (on) FontWeight.Bold else FontWeight.Normal,
                        color = ink.strong.copy(alpha = if (on) 1f else 0.3f)
                    )
                )
            }
        }
    }
}
