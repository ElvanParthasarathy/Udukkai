package com.elvan.udukkai.ui.components.shell

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures

@Composable
fun ElvanExpandedBar(
    title: String,
    colors: ShellColors,
    scrollOffsetPx: Float,
    collisionOffsetPx: Float,
    expandedHeight: Dp = 280.dp,
    hasLeadingWidget: Boolean = false,
    onBack: (() -> Unit)? = null,
    hasActions: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
    isSelectionMode: Boolean = false
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(expandedHeight)
    ) {
        val statusBarHeight = com.elvan.udukkai.core.platform.getStatusBarTopPadding()
        val density = LocalDensity.current
        val screenWidth = maxWidth
        val ff = LocalAppFontFamily.current

        val maxExtentPx = with(density) { expandedHeight.toPx() }
        val statusBarHeightPx = with(density) { statusBarHeight.toPx() }
        val ceilingPx = statusBarHeightPx + with(density) { 20.dp.toPx() }

        // Normalized progress 't' hits 1.0 at handoff (when icons reach ceiling)
        val handoffHeightPx = ceilingPx + with(density) { 64.dp.toPx() }
        val handoffShrinkOffsetPx = maxExtentPx - handoffHeightPx
        val t = (scrollOffsetPx / handoffShrinkOffsetPx).coerceIn(0f, 1f)

        val safeTitle = remember(title) { title.preventBrokenLigatures() }

        // 1. Measure text width
        val textMeasurer = rememberTextMeasurer()
        val titleStyle = TextStyle(
            fontFamily = ff,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )
        val fullLayout = remember(safeTitle, ff) {
            textMeasurer.measure(
                text = safeTitle,
                style = titleStyle
            )
        }
        val screenWidthPx = with(density) { screenWidth.toPx() }
        val maxAvailableWidthPx = screenWidthPx - with(density) { 32.dp.toPx() }
        // A title too wide for one line sits on two centred lines while the bar is open (as on Samsung's own pages).
        val wraps = fullLayout.size.width > maxAvailableWidthPx
        val wrappedLayout = remember(safeTitle, ff, maxAvailableWidthPx, wraps) {
            if (!wraps) null else textMeasurer.measure(
                text = safeTitle,
                style = titleStyle,
                constraints = androidx.compose.ui.unit.Constraints(maxWidth = maxAvailableWidthPx.toInt()),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        val rawTextWidthPx = if (wraps) maxAvailableWidthPx else fullLayout.size.width.toFloat()
        val textWidthPx = minOf(rawTextWidthPx, maxAvailableWidthPx)
        val textHeightPx = fullLayout.size.height.toFloat()

        val targetLeftPx = with(density) { if (hasLeadingWidget || onBack != null) 74.dp.toPx() else 24.dp.toPx() }
        // Closed, the title is one line of the usual 22sp beside the buttons, ending in "..." where the room runs out
        val closedRoomPx = screenWidthPx - targetLeftPx - with(density) { (if (hasActions) 78.dp else 16.dp).toPx() }
        val finalScale = 22f / 34f

        // 2. Compute X endpoints (center to target left)
        val centeredLeftPx = if (rawTextWidthPx > maxAvailableWidthPx) {
            with(density) { 16.dp.toPx() }
        } else {
            (screenWidthPx - textWidthPx) / 2f
        }
        val endLeftPx = if (isSelectionMode) centeredLeftPx else targetLeftPx
        val currentLeftPx = centeredLeftPx + (endLeftPx - centeredLeftPx) * t
        val currentLeftDp = with(density) { currentLeftPx.toDp() }

        // 3. Compute Y endpoints (bottom of expanded bar to ceiling + 44dp)
        val startTextBottomPx = maxExtentPx - with(density) { 100.dp.toPx() }
        val targetTextBottomPx = ceilingPx + with(density) { 44.dp.toPx() }
        val currentTextBottomPx = startTextBottomPx + (targetTextBottomPx - startTextBottomPx) * t
        val currentTopPx = currentTextBottomPx - textHeightPx
        val currentTopDp = with(density) { currentTopPx.toDp() }

        val scale = 1.0f - (1.0f - finalScale) * t

        // Lift progress: text fades OUT ONLY when the first card reaches the pill (collision)
        val liftStartOffsetPx = collisionOffsetPx - with(density) { 4.dp.toPx() }
        val liftProgress = if (scrollOffsetPx > liftStartOffsetPx) {
            ((scrollOffsetPx - liftStartOffsetPx) / with(density) { 12.dp.toPx() }).coerceIn(0f, 1f)
        } else {
            0f
        }
        val selectionFade = if (isSelectionMode) (1.0f - t * 1.6f).coerceIn(0f, 1f) else 1.0f
        val titleOpacity = ((1.0f - liftProgress) * selectionFade).coerceIn(0f, 1f)

        val maxAllowedWidthDp = (screenWidth - 32.dp)
        // The room for a title grows as the bar closes (the text is also smaller then), so a title is not cut sooner than it must be
        val closedUnscaledPx = closedRoomPx / finalScale
        val lineCapPx = maxAvailableWidthPx + (maxOf(maxAvailableWidthPx, closedUnscaledPx) - maxAvailableWidthPx) * t

        if (titleOpacity > 0f && !wraps) {
            Text(
                text = safeTitle,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                style = titleStyle,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = currentLeftDp, y = currentTopDp)
                    // (unbounded: the page is narrower than the room the shrunken title can use; Start keeps its left edge in place)
                    .wrapContentWidth(Alignment.Start, unbounded = true)
                    .widthIn(max = with(density) { lineCapPx.toDp() })
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = titleOpacity
                        transformOrigin = TransformOrigin(0f, 1f)
                    }
            )
        }
        if (titleOpacity > 0f && wraps) {
            fun smooth(x: Float) = x.coerceIn(0f, 1f).let { it * it * (3f - 2f * it) }
            // As in Samsung's own pages: the two lines are carried up by the scroll, at the same size, and fade away as they go.
            // They are fully gone before their top reaches the status bar, so they never run into the camera.
            val blockHeightPx = wrappedLayout?.size?.height?.toFloat() ?: textHeightPx
            val blockTopAtRestPx = startTextBottomPx - blockHeightPx
            val goneAtTopPx = statusBarHeightPx + with(density) { 16.dp.toPx() }
            val goneAtScrollPx = (blockTopAtRestPx - goneAtTopPx).coerceAtLeast(1f)
            val blockAlpha = 1f - smooth(scrollOffsetPx / goneAtScrollPx)
            // Then the one line fades in on the back-button row, rising with it until the row docks (never both at once).
            val lineAlpha = smooth((scrollOffsetPx - goneAtScrollPx) / (handoffShrinkOffsetPx - goneAtScrollPx).coerceAtLeast(1f))
            val rowTopPx = maxOf(ceilingPx, maxExtentPx - scrollOffsetPx - with(density) { 64.dp.toPx() })
            val lineBottomPx = rowTopPx + with(density) { 44.dp.toPx() }
            if (blockAlpha > 0f && wrappedLayout != null) {
                Text(
                    text = safeTitle,
                    maxLines = 2,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    style = titleStyle.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(
                            x = with(density) { ((screenWidthPx - maxAvailableWidthPx) / 2f).toDp() },
                            y = with(density) { (startTextBottomPx - scrollOffsetPx - wrappedLayout.size.height.toFloat()).toDp() }
                        )
                        .width(maxAllowedWidthDp)
                        .graphicsLayer { this.alpha = titleOpacity * blockAlpha }
                )
            }
            if (lineAlpha > 0f) {
                Text(
                    text = safeTitle,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    style = titleStyle,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(
                            x = with(density) { (if (isSelectionMode) centeredLeftPx else targetLeftPx).toDp() },
                            y = with(density) { (lineBottomPx - textHeightPx).toDp() }
                        )
                        .wrapContentWidth(Alignment.Start, unbounded = true)
                        .widthIn(max = with(density) { closedUnscaledPx.toDp() })
                        .graphicsLayer {
                            scaleX = finalScale
                            scaleY = finalScale
                            this.alpha = titleOpacity * lineAlpha
                            transformOrigin = TransformOrigin(0f, 1f)
                        }
                )
            }
        }
    }
}
