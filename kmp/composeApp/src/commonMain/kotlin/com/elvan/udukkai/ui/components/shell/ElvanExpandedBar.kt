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
        val textLayoutResult = remember(safeTitle, ff) {
            textMeasurer.measure(
                text = safeTitle,
                style = titleStyle
            )
        }
        val screenWidthPx = with(density) { screenWidth.toPx() }
        val maxAvailableWidthPx = screenWidthPx - with(density) { 32.dp.toPx() }
        val rawTextWidthPx = textLayoutResult.size.width.toFloat()
        val textWidthPx = minOf(rawTextWidthPx, maxAvailableWidthPx)
        val textHeightPx = textLayoutResult.size.height.toFloat()

        // 2. Compute X endpoints (center to target left)
        val centeredLeftPx = if (rawTextWidthPx > maxAvailableWidthPx) {
            with(density) { 16.dp.toPx() }
        } else {
            (screenWidthPx - textWidthPx) / 2f
        }
        val targetLeftPx = if (isSelectionMode) {
            centeredLeftPx
        } else {
            with(density) { if (hasLeadingWidget || onBack != null) 74.dp.toPx() else 24.dp.toPx() }
        }
        val currentLeftPx = centeredLeftPx + (targetLeftPx - centeredLeftPx) * t
        val currentLeftDp = with(density) { currentLeftPx.toDp() }

        // 3. Compute Y endpoints (bottom of expanded bar to ceiling + 44dp)
        val startTextBottomPx = maxExtentPx - with(density) { 100.dp.toPx() }
        val targetTextBottomPx = ceilingPx + with(density) { 44.dp.toPx() }

        val currentTextBottomPx = startTextBottomPx + (targetTextBottomPx - startTextBottomPx) * t
        val currentTopPx = currentTextBottomPx - textHeightPx
        val currentTopDp = with(density) { currentTopPx.toDp() }

        val finalScale = 22f / 34f
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

        if (titleOpacity > 0f) {
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
    }
}
