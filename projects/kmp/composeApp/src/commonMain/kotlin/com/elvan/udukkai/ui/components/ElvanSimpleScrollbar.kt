package com.elvan.udukkai.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * Simple passive scrollbar indicator for subpages (Settings, Edit forms, Details, etc.).
 * Matches default Android scrollbar behavior:
 * - Dynamic top padding adapting to collapsible header (starts below expanded header, glides up to minTopPadding)
 * - Proportional thumb height (32dp - 56dp)
 * - Auto-fades after 1200ms of scroll inactivity
 * - Not draggable (passive indicator only)
 * - Thin 3dp rounded pill on the right edge
 */
@Composable
fun ElvanSimpleScrollbar(
    scrollState: LazyListState,
    modifier: Modifier = Modifier,
    colors: ShellColors = rememberShellColors(),
    topPadding: Dp = 104.dp,
    bottomPadding: Dp = 32.dp,
    headerItemsCount: Int = 1
) {
    val isDark = colors.isDark
    val density = LocalDensity.current

    val isScrolling = scrollState.isScrollInProgress

    var isVisible by remember { mutableStateOf(true) }
    LaunchedEffect(isScrolling) {
        if (isScrolling) {
            isVisible = true
        } else {
            delay(1200)
            isVisible = false
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) (if (isDark) 0.50f else 0.45f) else 0f,
        animationSpec = tween(if (isVisible) 150 else 300),
        label = "scrollbarAlpha"
    )

    if (alpha <= 0.01f) return

    val layoutInfo = scrollState.layoutInfo
    val totalItems = layoutInfo.totalItemsCount
    val visibleItems = layoutInfo.visibleItemsInfo
    val visibleCount = visibleItems.size

    if (totalItems <= 1 || visibleItems.isEmpty() || totalItems <= visibleCount) return

    val fraction = run {
        if (!scrollState.canScrollBackward) return@run 0f
        if (!scrollState.canScrollForward) return@run 1f

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

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val containerHeightPx = with(density) { maxHeight.toPx() }
        val minTopPaddingPx = with(density) { topPadding.toPx() }
        val bottomPaddingPx = with(density) { bottomPadding.toPx() }

        // Dynamic top padding: smoothly tracks the bottom edge of header items (e.g. collapsible top spacer)
        val lastHeaderItem = if (headerItemsCount > 0) {
            visibleItems.lastOrNull { it.index < headerItemsCount }
        } else null

        val topPaddingPx = if (lastHeaderItem != null) {
            maxOf(minTopPaddingPx, (lastHeaderItem.offset + lastHeaderItem.size).toFloat() + with(density) { 8.dp.toPx() })
        } else {
            minTopPaddingPx
        }

        val availableTrackHeight = (containerHeightPx - topPaddingPx - bottomPaddingPx).coerceAtLeast(100f)
        val thumbRatio = (visibleCount.toFloat() / totalItems.toFloat()).coerceIn(0.08f, 0.35f)
        val thumbHeightPx = (thumbRatio * availableTrackHeight).coerceIn(
            with(density) { 32.dp.toPx() },
            with(density) { 56.dp.toPx() }
        )
        val maxTravelPx = (availableTrackHeight - thumbHeightPx).coerceAtLeast(1f)
        val thumbYPx = (topPaddingPx + (fraction * maxTravelPx)).coerceIn(topPaddingPx, topPaddingPx + maxTravelPx)

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 4.dp)
                .offset { IntOffset(0, thumbYPx.roundToInt()) }
                .width(3.dp)
                .height(with(density) { thumbHeightPx.toDp() })
                .graphicsLayer { this.alpha = alpha }
                .clip(CircleShape)
                .background(
                    color = if (isDark) Color(0xFFE0E0E0) else Color(0xFF999999)
                )
        )
    }
}
