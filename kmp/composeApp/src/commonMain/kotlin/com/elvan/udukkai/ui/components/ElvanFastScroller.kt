package com.elvan.udukkai.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.extensions.cssShadow
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Universal Fast Scroller matching Elvan / One UI design language across all screens.
 *
 * Engineered for professional smoothness and precision:
 * 1. Fixed sleek 42dp x 4dp thumb pill (no ballooning or awkward stretching).
 * 2. Monotonic, content-proportional progress calculation based on Android's native
 *    FastScroller algorithm (no denominator jumping, no backward pixel shifts).
 * 3. Dynamic top padding that cleanly anchors below header items (collapsible header,
 *    pill shifter, business filter) and smoothly tracks them pixel-by-pixel as they scroll.
 * 4. Content-adaptive speed: moves slowly in long lists, fast in short lists.
 * 5. Date-aware section snapping when [dateProvider] is provided (Uruvakku screen).
 */
@Composable
fun ElvanFastScroller(
    scrollState: LazyListState,
    modifier: Modifier = Modifier,
    dateProvider: ((Int) -> String)? = null,
    colors: ShellColors = rememberShellColors(),
    topPadding: Dp = 84.dp,
    bottomPadding: Dp = 84.dp,
    headerItemsCount: Int = 0
) {
    val ff = LocalAppFontFamily.current
    val isDark = colors.isDark
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableStateOf<Float?>(null) }
    var scrollJob by remember { mutableStateOf<Job?>(null) }
    var lastActiveTime by remember { mutableLongStateOf(0L) }

    val isScrollInProgress = scrollState.isScrollInProgress

    LaunchedEffect(isScrollInProgress, isDragging) {
        if (isScrollInProgress || isDragging) {
            lastActiveTime = System.currentTimeMillis()
        }
    }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(lastActiveTime, isScrollInProgress, isDragging) {
        if (isScrollInProgress || isDragging) {
            isVisible = true
        } else {
            delay(1200)
            isVisible = false
        }
    }

    val layoutInfo = scrollState.layoutInfo
    val totalItems = layoutInfo.totalItemsCount
    val visibleItems = layoutInfo.visibleItemsInfo
    val visibleCount = visibleItems.size
    val isScrollable = scrollState.canScrollForward || scrollState.canScrollBackward || totalItems > visibleCount
    if (!isScrollable || totalItems <= 1) return

    // -----------------------------------------------------------------------------------------
    // Monotonic, Content-Proportional Scroll Fraction (Android Framework FastScroller algorithm)
    // -----------------------------------------------------------------------------------------
    val naturalFraction = remember(
        scrollState.firstVisibleItemIndex,
        scrollState.firstVisibleItemScrollOffset,
        totalItems,
        scrollState.canScrollBackward,
        scrollState.canScrollForward
    ) {
        if (totalItems <= 1 || visibleItems.isEmpty()) return@remember 0f
        if (!scrollState.canScrollBackward) return@remember 0f
        if (!scrollState.canScrollForward) return@remember 1f

        val firstItem = visibleItems.first()
        val lastItem = visibleItems.last()

        val laidOutArea = (lastItem.offset + lastItem.size) - firstItem.offset
        val laidOutRange = (lastItem.index - firstItem.index + 1).coerceAtLeast(1)
        val avgItemSize = laidOutArea.toFloat() / laidOutRange.toFloat()

        val viewportHeight = (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset).toFloat()
        val estimatedTotalHeight = avgItemSize * totalItems
        val maxScroll = (estimatedTotalHeight - viewportHeight).coerceAtLeast(1f)

        val currentScroll = (firstItem.index.toFloat() * avgItemSize) + (-firstItem.offset.toFloat()).coerceAtLeast(0f)
        (currentScroll / maxScroll).coerceIn(0f, 1f)
    }

    // 60ms linear smoothing absorbs any single-frame micro-variance during passive finger scrolling
    val animatedFraction by animateFloatAsState(
        targetValue = naturalFraction,
        animationSpec = tween(durationMillis = 60, easing = LinearEasing),
        label = "scrollerFraction"
    )

    val activeFraction = if (isDragging && dragProgress != null) dragProgress!! else animatedFraction

    val thumbAlpha by animateFloatAsState(
        targetValue = if (isDragging) 0.95f else if (isVisible) (if (isDark) 0.40f else 0.50f) else 0f,
        animationSpec = tween(200),
        label = "thumbAlpha"
    )

    val thumbScale by animateFloatAsState(
        targetValue = if (isDragging) 1.25f else 1.0f,
        animationSpec = tween(150),
        label = "thumbScale"
    )

    // Precalculate date section starts if dateProvider is supplied
    val dateSectionStarts = remember(totalItems, headerItemsCount, dateProvider) {
        if (dateProvider == null) return@remember emptyList<Int>()
        val starts = mutableListOf<Int>()
        var lastDate = ""
        for (i in headerItemsCount until totalItems) {
            val d = dateProvider(i)
            if (d.isNotBlank() && d != lastDate) {
                starts.add(i)
                lastDate = d
            }
        }
        starts
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val containerHeightPx = with(density) { maxHeight.toPx() }
        val minTopPaddingPx = with(density) { topPadding.toPx() }
        val bottomPaddingPx = with(density) { bottomPadding.toPx() }

        // Dynamic top padding: smoothly tracks the bottom edge of header items
        // (collapsible top spacer, pill shifter, business shifter) down to minTopPaddingPx
        val lastHeaderItem = if (headerItemsCount > 0) {
            visibleItems.lastOrNull { it.index < headerItemsCount }
        } else null

        val topPaddingPx = if (lastHeaderItem != null) {
            maxOf(minTopPaddingPx, (lastHeaderItem.offset + lastHeaderItem.size).toFloat() + with(density) { 8.dp.toPx() })
        } else {
            minTopPaddingPx
        }

        val availableTrackHeight = (containerHeightPx - topPaddingPx - bottomPaddingPx).coerceAtLeast(100f)
        val thumbHeightPx = with(density) { 42.dp.toPx() }
        val maxTravelPx = (availableTrackHeight - thumbHeightPx).coerceAtLeast(1f)

        val thumbYPx = (topPaddingPx + (activeFraction * maxTravelPx)).coerceIn(topPaddingPx, topPaddingPx + maxTravelPx)

        // Floating date text calculation
        val pillDateText = remember(activeFraction, totalItems, headerItemsCount, dateSectionStarts, dateProvider) {
            if (dateProvider == null || dateSectionStarts.isEmpty()) return@remember ""
            val sectionIdx = (activeFraction * (dateSectionStarts.size - 1)).roundToInt().coerceIn(0, dateSectionStarts.size - 1)
            val headerIdx = dateSectionStarts.getOrNull(sectionIdx) ?: return@remember ""
            dateProvider(headerIdx)
        }

        // Pill is strictly visible ONLY when user is actively dragging and dateProvider is non-null
        val pillAlpha by animateFloatAsState(
            targetValue = if (dateProvider != null && isDragging && pillDateText.isNotBlank()) 1.0f else 0.0f,
            animationSpec = tween(180, easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)),
            label = "pillAlpha"
        )

        val pillScale by animateFloatAsState(
            targetValue = if (dateProvider != null && isDragging && pillDateText.isNotBlank()) 1.0f else 0.75f,
            animationSpec = tween(180, easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)),
            label = "pillScale"
        )

        val updatedTotalItems by rememberUpdatedState(totalItems)
        val updatedTrackHeight by rememberUpdatedState(availableTrackHeight)
        val updatedMaxTravelPx by rememberUpdatedState(maxTravelPx)
        val updatedTopPaddingPx by rememberUpdatedState(topPaddingPx)
        val updatedDateSectionStarts by rememberUpdatedState(dateSectionStarts)
        val updatedDateProvider by rememberUpdatedState(dateProvider)
        val updatedHeaderItemsCount by rememberUpdatedState(headerItemsCount)

        // Gesture detector on the right 44.dp edge (pointerInput(Unit) ensures touch gesture is never cancelled)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(44.dp)
                .pointerInput(Unit) {
                    var lastSnappedTarget = -1

                    fun performScroll(touchY: Float) {
                        val topPad = updatedTopPaddingPx
                        val maxTravel = updatedMaxTravelPx
                        val tItems = updatedTotalItems
                        val dProvider = updatedDateProvider
                        val dSectionStarts = updatedDateSectionStarts
                        val hCount = updatedHeaderItemsCount

                        val relY = (touchY - topPad - (thumbHeightPx / 2f)).coerceIn(0f, maxTravel)
                        val fraction = (relY / maxTravel).coerceIn(0f, 1f)
                        dragProgress = fraction

                        if (fraction <= 0.01f) {
                            if (lastSnappedTarget != 0) {
                                lastSnappedTarget = 0
                                scrollJob?.cancel()
                                scrollJob = coroutineScope.launch {
                                    scrollState.scrollToItem(0, 0)
                                }
                            }
                            return
                        }

                        if (fraction >= 0.99f) {
                            val endIdx = (tItems - 1).coerceAtLeast(0)
                            if (lastSnappedTarget != endIdx) {
                                lastSnappedTarget = endIdx
                                scrollJob?.cancel()
                                scrollJob = coroutineScope.launch {
                                    scrollState.scrollToItem(endIdx, 0)
                                }
                            }
                            return
                        }

                        if (dProvider != null && dSectionStarts.isNotEmpty()) {
                            // Date-aware snapping: map fraction across date sections
                            val sectionIdx = (fraction * (dSectionStarts.size - 1)).roundToInt().coerceIn(0, dSectionStarts.size - 1)
                            if (sectionIdx != lastSnappedTarget) {
                                lastSnappedTarget = sectionIdx
                                val targetHeaderIndex = dSectionStarts.getOrNull(sectionIdx) ?: 0
                                if (targetHeaderIndex in 0 until tItems) {
                                    scrollJob?.cancel()
                                    scrollJob = coroutineScope.launch {
                                        scrollState.scrollToItem(targetHeaderIndex, 0)
                                    }
                                }
                            }
                        } else {
                            // Non-date lists: map fraction across content items
                            val contentCount = (tItems - hCount).coerceAtLeast(1)
                            val targetIndex = (hCount + (fraction * (contentCount - 1)).roundToInt()).coerceIn(0, tItems - 1)
                            if (targetIndex != lastSnappedTarget && targetIndex in 0 until tItems) {
                                lastSnappedTarget = targetIndex
                                scrollJob?.cancel()
                                scrollJob = coroutineScope.launch {
                                    scrollState.scrollToItem(targetIndex, 0)
                                }
                            }
                        }
                    }

                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val touchY = down.position.y
                        val topPad = updatedTopPaddingPx
                        val trackH = updatedTrackHeight
                        if (touchY in topPad..(topPad + trackH)) {
                            isDragging = true
                            performScroll(touchY)
                        }

                        val pointerId = down.id
                        do {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                            if (change.pressed && isDragging) {
                                performScroll(change.position.y)
                                change.consume()
                            }
                        } while (event.changes.any { it.pressed })

                        isDragging = false
                        dragProgress = null
                        lastSnappedTarget = -1
                    }
                }
        ) {
            // Scroller Thumb (Smooth slender pill, NO track line)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 5.dp)
                    .offset { IntOffset(0, thumbYPx.roundToInt()) }
                    .width(4.dp)
                    .height(42.dp)
                    .graphicsLayer {
                        alpha = thumbAlpha
                        scaleX = thumbScale
                        scaleY = thumbScale
                        transformOrigin = TransformOrigin(1f, 0.5f)
                    }
                    .clip(CircleShape)
                    .background(
                        color = if (isDark) Color(0xFFE0E0E0) else Color(0xFF333333)
                    )
            )
        }

        // Floating Date Pill (ONLY shown if dateProvider is supplied and user is dragging)
        if (dateProvider != null && pillAlpha > 0.01f && pillDateText.isNotBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 18.dp)
                    .offset {
                        IntOffset(
                            x = 0,
                            y = (thumbYPx + (thumbHeightPx / 2f) - with(density) { 16.dp.toPx() }).roundToInt()
                        )
                    }
                    .graphicsLayer {
                        scaleX = pillScale
                        scaleY = pillScale
                        alpha = pillAlpha
                        transformOrigin = TransformOrigin(1f, 0.5f)
                    }
                    .cssShadow(
                        color = Color.Black,
                        alpha = if (isDark) 0.30f else 0.10f,
                        blurRadius = 12.dp,
                        offsetY = 2.dp
                    )
                    .clip(CircleShape)
                    .background(
                        color = if (isDark) Color(0xFF222222) else Color.White
                    )
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                AnimatedContent(
                    targetState = pillDateText,
                    transitionSpec = {
                        ContentTransform(
                            targetContentEnter = fadeIn(tween(100)),
                            initialContentExit = fadeOut(tween(80)),
                            sizeTransform = SizeTransform(clip = false, sizeAnimationSpec = { _, _ -> tween(120) })
                        )
                    },
                    label = "pillDateAnim"
                ) { dateText ->
                    Text(
                        text = dateText.preventBrokenLigatures(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
