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
        // A title too wide for one line sits on two centred lines while the bar is open. The two lines are two pieces of
        // text that glide into one line as the bar closes: the first goes to the closed place, the second slides up beside it.
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
        val breakAt = wrappedLayout?.getLineEnd(0, visibleEnd = false) ?: 0
        val firstPart = if (wraps) safeTitle.substring(0, breakAt).trimEnd() else safeTitle
        val secondPart = if (wraps) safeTitle.substring(breakAt).trimStart() else ""
        val firstW = remember(firstPart, ff) { textMeasurer.measure(text = firstPart, style = titleStyle).size.width.toFloat() }
        val secondW = remember(secondPart, ff) { if (secondPart.isEmpty()) 0f else textMeasurer.measure(text = secondPart, style = titleStyle).size.width.toFloat() }
        val spaceW = remember(ff) { textMeasurer.measure(text = " ", style = titleStyle).size.width.toFloat().coerceAtLeast(1f) }
        val lineH = wrappedLayout?.let { (it.getLineBottom(0) - it.getLineTop(0)) } ?: fullLayout.size.height.toFloat()

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
                    .widthIn(max = maxAllowedWidthDp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = titleOpacity
                        transformOrigin = TransformOrigin(0f, 1f)
                    }
            )
        }
        if (titleOpacity > 0f && wraps) {
            // Open: each line centred on its own, the first above the second. Closed: one line from the closed place.
            val firstLeftOpen = (screenWidthPx - minOf(firstW, maxAvailableWidthPx)) / 2f
            val secondLeftOpen = (screenWidthPx - minOf(secondW, maxAvailableWidthPx)) / 2f
            val firstLeftClosed = if (isSelectionMode) firstLeftOpen else targetLeftPx
            val secondLeftClosed = if (isSelectionMode) secondLeftOpen else targetLeftPx + (firstW + spaceW) * finalScale
            fun lerp(a: Float, b: Float) = a + (b - a) * t
            val firstBottomOpen = startTextBottomPx - lineH
            val secondBottomOpen = startTextBottomPx
            val bottomClosed = targetTextBottomPx
            // The room each line may use, as the page closes: the whole width open, what is left beside the buttons closed
            val closedUnscaledPx = closedRoomPx / finalScale
            val firstCapClosed = minOf(maxAvailableWidthPx, closedUnscaledPx)
            val secondCapClosed = (closedUnscaledPx - firstW - spaceW).coerceIn(0f, maxAvailableWidthPx)
            for (piece in 0..1) {
                val text = if (piece == 0) firstPart else secondPart
                if (text.isEmpty()) continue
                // The first line moves like a plain title. The second follows it like a train: it keeps just behind the first
                // line's right end as that shrinks, and eases up into the row, so the two lines never run into each other.
                fun smooth(x: Float) = x.coerceIn(0f, 1f).let { it * it * (3f - 2f * it) }
                val firstLeftNow = lerp(firstLeftOpen, firstLeftClosed)
                val hugLeft = if (isSelectionMode) secondLeftOpen else firstLeftNow + (firstW + spaceW) * scale
                val leftPx = if (piece == 0) firstLeftNow else secondLeftOpen + (hugLeft - secondLeftOpen) * smooth(t / 0.55f)
                val bottomPx = if (piece == 0) lerp(firstBottomOpen, bottomClosed) else secondBottomOpen + (bottomClosed - secondBottomOpen) * smooth((t - 0.15f) / 0.85f)
                Text(
                    text = text,
                    maxLines = 1,
                    softWrap = false,
                    // While the second line is on its way it is simply cut at the margin; the dots come when it has settled
                    overflow = if (piece == 1 && t < 0.9f) TextOverflow.Clip else TextOverflow.Ellipsis,
                    style = titleStyle,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = with(density) { leftPx.toDp() }, y = with(density) { (bottomPx - fullLayout.size.height.toFloat()).toDp() })
                        .widthIn(max = with(density) {
                            val byProgress = if (piece == 0) lerp(maxAvailableWidthPx, firstCapClosed) else lerp(maxAvailableWidthPx, secondCapClosed)
                            // and never past the page's right margin, wherever the line is on its way
                            val byEdge = ((screenWidthPx - 16.dp.toPx() - leftPx) / scale).coerceAtLeast(0f)
                            minOf(byProgress, byEdge).toDp()
                        })
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = titleOpacity
                            transformOrigin = TransformOrigin(0f, 1f)
                        }
                )
            }
        }
    }
}
