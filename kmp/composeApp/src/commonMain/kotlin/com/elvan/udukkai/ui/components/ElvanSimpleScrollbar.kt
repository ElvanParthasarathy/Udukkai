package com.elvan.udukkai.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt

/** The thumb draws from lazy list state and captures touches only when hit. */
@Composable
internal fun Modifier.elvanDocumentScrollbar(
    scrollState: LazyListState,
    enabled: Boolean,
    topInset: Dp,
    bottomInset: Dp,
    color: Color,
    documentIndices: IntArray,
    labelForIndex: (Int) -> String,
    leadingItemKey: String,
    labelStyle: TextStyle,
    labelBackground: Color
): Modifier {
    val scrolling = scrollState.isScrollInProgress
    var draggedProgress by remember(scrollState, documentIndices) { mutableStateOf<Float?>(null) }
    val dragging = draggedProgress != null
    var visible by remember(scrollState) { mutableStateOf(scrolling) }
    LaunchedEffect(scrollState, enabled, scrolling, dragging) {
        if (enabled && !scrolling && !dragging) delay(850)
        visible = enabled && (scrolling || dragging)
    }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 0.8f else 0f,
        animationSpec = tween(if (visible) 125 else 210, easing = FastOutSlowInEasing),
        label = "shellScrollbarAlpha"
    )
    val thumbWidth by animateDpAsState(
        targetValue = if (dragging) 4.dp else 3.dp,
        animationSpec = tween(110),
        label = "documentScrollbarWidth"
    )
    val dateAlpha by animateFloatAsState(
        targetValue = if (dragging) 1f else 0f,
        animationSpec = tween(if (dragging) 125 else 100, easing = FastOutSlowInEasing),
        label = "documentScrollbarDateAlpha"
    )

    val insets by rememberUpdatedState(topInset to bottomInset)
    val acceptsDrag by rememberUpdatedState(enabled)
    val dateForIndex by rememberUpdatedState(labelForIndex)
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val direction = LocalLayoutDirection.current
    val label by remember(scrollState, documentIndices, leadingItemKey, density) {
        derivedStateOf {
            if (draggedProgress == null) return@derivedStateOf ""
            with(density) {
                val (top, bottom) = insets
                val trackBottom = scrollState.layoutInfo.viewportSize.height - bottom.toPx()
                val thumb = scrollState.documentThumb(
                    top.toPx(), trackBottom, 6.dp.toPx(), 40.dp.toPx(), documentIndices, leadingItemKey
                ) ?: return@with ""
                val center = thumb.top + (draggedProgress ?: thumb.progress) * thumb.travel + thumb.height / 2
                scrollState.layoutInfo.documentAt(center, thumb.top, trackBottom, documentIndices)
                    ?.let(dateForIndex).orEmpty()
            }
        }
    }
    var currentLabel by remember(scrollState, documentIndices) { mutableStateOf("") }
    var previousLabel by remember(scrollState, documentIndices) { mutableStateOf("") }
    val dateTransition = remember(scrollState, documentIndices) { Animatable(1f) }
    LaunchedEffect(label, dateTransition) {
        if (label.isBlank()) {
            delay(100)
            dateTransition.snapTo(1f)
            previousLabel = ""
            currentLabel = ""
            return@LaunchedEffect
        }
        if (label == currentLabel) return@LaunchedEffect
        previousLabel = currentLabel
        currentLabel = label
        if (previousLabel.isNotBlank()) {
            dateTransition.snapTo(0f)
            dateTransition.animateTo(1f, tween(115, easing = FastOutSlowInEasing))
        }
        previousLabel = ""
    }
    return this.pointerInput(scrollState, direction, documentIndices, leadingItemKey) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = true, pass = PointerEventPass.Initial)
            if (!acceptsDrag || alpha < 0.1f) return@awaitEachGesture
            val edgeDistance = if (direction == LayoutDirection.Ltr) size.width - down.position.x else down.position.x
            if (edgeDistance !in 0f..32.dp.toPx()) return@awaitEachGesture
            val (topInset, bottomInset) = insets
            val thumb = scrollState.documentThumb(
                topInset.toPx(), size.height - bottomInset.toPx(), 6.dp.toPx(),
                40.dp.toPx(), documentIndices, leadingItemKey
            )
                ?: return@awaitEachGesture
            if (down.position.y !in (thumb.y - 10.dp.toPx())..(thumb.y + thumb.height + 10.dp.toPx())) {
                return@awaitEachGesture
            }
            down.consume()
            val grabOffset = down.position.y - thumb.y
            var lastIndex = thumb.firstDocumentIndex
            draggedProgress = thumb.progress
            try {
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    change.consume()
                    if (!change.pressed || !acceptsDrag) break
                    if (change.position.y == change.previousPosition.y) continue
                    val progress = ((change.position.y - grabOffset - thumb.top) / thumb.travel).coerceIn(0f, 1f)
                    draggedProgress = progress
                    val index = when (progress) {
                        0f -> 0
                        1f -> documentIndices.last()
                        else -> documentIndices[(progress * thumb.lastStartOrdinal).roundToInt()]
                    }
                    if (index == lastIndex) continue
                    lastIndex = index
                    val offset = if (progress > 0f && progress < 1f) {
                        // Put the destination card just below the toolbar.
                        -(insets.first.toPx() + scrollState.layoutInfo.viewportStartOffset).roundToInt()
                    } else 0
                    scrollState.requestScrollToItem(index, offset)
                }
            } finally {
                draggedProgress = null
            }
        }
    }.drawWithContent {
        drawContent()
        if (!enabled) return@drawWithContent
        val opacity = alpha
        if (opacity <= 0f) return@drawWithContent
        val bottom = size.height - bottomInset.toPx()
        val thumb = scrollState.documentThumb(
            topInset.toPx(), bottom, 6.dp.toPx(), 40.dp.toPx(), documentIndices, leadingItemKey
        )
            ?: return@drawWithContent
        val width = thumbWidth.toPx()
        val edgeGap = 5.dp.toPx()
        val thumbX = if (layoutDirection == LayoutDirection.Ltr) size.width - edgeGap - width else edgeGap
        val thumbY = thumb.top + (draggedProgress ?: thumb.progress) * thumb.travel
        drawRoundRect(
            color = color,
            topLeft = Offset(thumbX, thumbY),
            size = Size(width, thumb.height),
            cornerRadius = CornerRadius(width / 2),
            alpha = opacity
        )
        val bubbleAlpha = dateAlpha * opacity / 0.8f
        if (bubbleAlpha <= 0f || currentLabel.isBlank()) return@drawWithContent
        val gap = 8.dp.toPx()
        val horizontalPadding = 12.dp.toPx()
        val verticalPadding = 6.dp.toPx()
        val maxTextWidth = (size.width - 2 * edgeGap - width - gap - 2 * horizontalPadding).toInt()
        if (maxTextWidth <= 0) return@drawWithContent
        fun measureDate(date: String) = textMeasurer.measure(
            text = date,
            style = labelStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            constraints = Constraints(maxWidth = maxTextWidth)
        )
        val text = measureDate(currentLabel)
        val previousText = previousLabel.takeIf { it.isNotBlank() }?.let(::measureDate)
        val progress = dateTransition.value
        val previousWidth = previousText?.size?.width ?: text.size.width
        val bubbleWidth = previousWidth + (text.size.width - previousWidth) * progress + 2 * horizontalPadding
        val bubbleHeight = maxOf(text.size.height, previousText?.size?.height ?: 0) + 2 * verticalPadding
        if (bubbleHeight > bottom - thumb.top) return@drawWithContent
        val revealOffset = (1f - bubbleAlpha) * 4.dp.toPx()
        val bubbleX = if (layoutDirection == LayoutDirection.Ltr) thumbX - gap - bubbleWidth + revealOffset else thumbX + width + gap - revealOffset
        val bubbleY = (thumbY + (thumb.height - bubbleHeight) / 2).coerceIn(thumb.top, bottom - bubbleHeight)
        drawRoundRect(
            color = labelBackground,
            topLeft = Offset(bubbleX, bubbleY),
            size = Size(bubbleWidth, bubbleHeight),
            cornerRadius = CornerRadius(bubbleHeight / 2),
            alpha = bubbleAlpha
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(bubbleX, bubbleY),
            size = Size(bubbleWidth, bubbleHeight),
            cornerRadius = CornerRadius(bubbleHeight / 2),
            style = Stroke(1.dp.toPx()),
            alpha = bubbleAlpha * 0.18f
        )
        val lift = 2.dp.toPx()
        clipRect(bubbleX, bubbleY, bubbleX + bubbleWidth, bubbleY + bubbleHeight) {
            previousText?.let {
                drawText(
                    it,
                    topLeft = Offset(bubbleX + (bubbleWidth - it.size.width) / 2, bubbleY + verticalPadding - progress * lift),
                    alpha = bubbleAlpha * (1f - progress)
                )
            }
            drawText(
                text,
                topLeft = Offset(bubbleX + (bubbleWidth - text.size.width) / 2, bubbleY + verticalPadding + (1f - progress) * lift),
                alpha = bubbleAlpha * progress
            )
        }
    }
}

private data class DocumentThumb(
    val top: Float,
    val height: Float,
    val travel: Float,
    val progress: Float,
    val firstDocumentIndex: Int,
    val lastStartOrdinal: Int
) {
    val y get() = top + progress * travel
}

private fun LazyListState.documentThumb(
    headerBottom: Float,
    bottom: Float,
    gap: Float,
    thumbHeight: Float,
    documentIndices: IntArray,
    leadingItemKey: String
): DocumentThumb? {
    if (documentIndices.isEmpty() || (!canScrollBackward && !canScrollForward)) return null
    val layout = layoutInfo
    val top = maxOf(headerBottom, layout.trailingEdgeOf(leadingItemKey)?.plus(gap) ?: headerBottom)
    val trackHeight = bottom - top
    if (trackHeight <= thumbHeight) return null
    val trackStart = top + layout.viewportStartOffset
    val trackEnd = bottom + layout.viewportStartOffset
    val cards = layout.visibleItemsInfo.filter {
        it.offset + it.size > trackStart && it.offset < trackEnd && documentIndices.binarySearch(it.index) >= 0
    }
    val first = cards.firstOrNull() ?: return null
    val ordinal = documentIndices.binarySearch(first.index)
    val last = cards.last()
    val lastOrdinal = documentIndices.binarySearch(last.index)
    val partialCard = ((trackStart - first.offset) / first.size.coerceAtLeast(1)).coerceIn(0f, 1f)
    val below = ((last.offset + last.size - trackEnd) / last.size.coerceAtLeast(1)).coerceIn(0f, 1f)
    val traveled = ordinal + partialCard
    val remaining = documentIndices.lastIndex - lastOrdinal + below
    val travelOrdinals = traveled + remaining
    val lastStartOrdinal = travelOrdinals.roundToInt().coerceIn(0, documentIndices.lastIndex)
    val progress = when {
        !canScrollBackward -> 0f
        !canScrollForward -> 1f
        else -> (traveled / travelOrdinals.coerceAtLeast(1f)).coerceIn(0f, 1f)
    }
    return DocumentThumb(top, thumbHeight, trackHeight - thumbHeight, progress, first.index, lastStartOrdinal)
}

private fun LazyListLayoutInfo.documentAt(center: Float, top: Float, bottom: Float, indices: IntArray): Int? {
    val centerInList = center + viewportStartOffset
    return visibleItemsInfo.asSequence()
        .filter { item ->
            indices.binarySearch(item.index) >= 0 &&
                item.offset + item.size > top + viewportStartOffset &&
                item.offset < bottom + viewportStartOffset
        }
        .minByOrNull { item ->
            abs(centerInList - centerInList.coerceIn(item.offset.toFloat(), (item.offset + item.size).toFloat()))
        }?.index
}

private fun LazyListLayoutInfo.trailingEdgeOf(key: String): Float? =
    visibleItemsInfo.firstOrNull { it.key == key }?.let { (it.offset + it.size - viewportStartOffset).toFloat() }

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
        targetValue = if (isVisible) (if (isDark) 0.22f else 0.20f) else 0f,
        animationSpec = tween(if (isVisible) 150 else 300),
        label = "scrollbarAlpha"
    )

    if (alpha <= 0.01f) return

    val canScroll = scrollState.canScrollBackward || scrollState.canScrollForward
    val layoutInfo = scrollState.layoutInfo
    val totalItems = layoutInfo.totalItemsCount
    val visibleItems = layoutInfo.visibleItemsInfo
    val visibleCount = visibleItems.size

    if (!canScroll || totalItems <= 1 || visibleItems.isEmpty()) return

    val firstItem = visibleItems.first()
    val lastItem = visibleItems.last()

    val laidOutArea = (lastItem.offset + lastItem.size) - firstItem.offset
    val laidOutRange = (lastItem.index - firstItem.index + 1).coerceAtLeast(1)
    val avgItemSize = laidOutArea.toFloat() / laidOutRange.toFloat()

    val viewportHeight = (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset).toFloat()
    val estimatedTotalHeight = avgItemSize * totalItems
    val maxScroll = (estimatedTotalHeight - viewportHeight).coerceAtLeast(1f)

    val fraction = run {
        if (!scrollState.canScrollBackward) return@run 0f
        if (!scrollState.canScrollForward) return@run 1f

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
        val minThumbHeightPx = with(density) { 28.dp.toPx() }
        val maxThumbHeightPx = (availableTrackHeight - with(density) { 12.dp.toPx() }).coerceAtLeast(minThumbHeightPx)
        val thumbRatio = if (estimatedTotalHeight > 0f) {
            (viewportHeight / estimatedTotalHeight).coerceIn(0.05f, 0.90f)
        } else {
            (visibleCount.toFloat() / totalItems.toFloat()).coerceIn(0.05f, 0.90f)
        }
        val thumbHeightPx = (thumbRatio * availableTrackHeight).coerceIn(minThumbHeightPx, maxThumbHeightPx)
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
                    color = if (isDark) Color(0xFFCCCCCC) else Color(0xFF666666)
                )
        )
    }
}
