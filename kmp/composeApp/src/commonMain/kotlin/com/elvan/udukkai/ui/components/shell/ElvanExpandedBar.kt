package com.elvan.udukkai.ui.components.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.elvan.udukkai.ui.navigation.MaterialSymbols

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
    isSelectionMode: Boolean = false,
    banners: List<ShellBanner> = emptyList()
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
        val baseTitleOpacity = ((1.0f - liftProgress) * selectionFade).coerceIn(0f, 1f)

        // Banner detection & fade logic
        val activeBanners = remember(banners) { banners.filter { it.message.isNotBlank() || it.title.isNotBlank() } }
        val hasActiveBanners = activeBanners.isNotEmpty() && !isSelectionMode
        val bannerFadeOffsetPx = with(density) { 50.dp.toPx() }
        val bannerOpacity = (1.0f - (scrollOffsetPx / bannerFadeOffsetPx)).coerceIn(0f, 1f)

        // In banner mode: Title is completely hidden until collapsed (t >= 0.7f), then smoothly fades in
        val titleOpacity = if (hasActiveBanners) {
            if (t >= 0.7f) {
                ((t - 0.7f) / 0.3f).coerceIn(0f, 1f) * baseTitleOpacity
            } else {
                0f
            }
        } else {
            baseTitleOpacity
        }

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

        // Centered Floating Inset Banner Carousel
        if (hasActiveBanners && bannerOpacity > 0f) {
            val bannerCardHeight = 140.dp
            val actionsTopDp = expandedHeight - 64.dp
            val availableCarouselHeight = (actionsTopDp - statusBarHeight).coerceAtLeast(bannerCardHeight + 24.dp)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = statusBarHeight)
                    .height(availableCarouselHeight)
                    .align(Alignment.TopCenter)
                    .graphicsLayer {
                        this.alpha = bannerOpacity
                    },
                contentAlignment = Alignment.Center
            ) {
                if (activeBanners.size == 1) {
                    val singleBanner = activeBanners.first()
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        MonochromeBannerCard(
                            banner = singleBanner,
                            colors = colors,
                            ff = ff,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(bannerCardHeight)
                        )
                    }
                } else {
                    val pagerState = rememberPagerState(pageCount = { activeBanners.size })
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(bannerCardHeight),
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            pageSpacing = 10.dp,
                            verticalAlignment = Alignment.Top
                        ) { page ->
                            val item = activeBanners.getOrNull(page)
                            if (item != null) {
                                MonochromeBannerCard(
                                    banner = item,
                                    colors = colors,
                                    ff = ff,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(activeBanners.size) { index ->
                                val isSelected = pagerState.currentPage == index
                                val dotWidth = if (isSelected) 12.dp else 4.dp
                                val dotColor = if (isSelected) {
                                    if (colors.isDark) Color.White.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.85f)
                                } else {
                                    if (colors.isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.15f)
                                }

                                Box(
                                    modifier = Modifier
                                        .height(3.5.dp)
                                        .width(dotWidth)
                                        .clip(CircleShape)
                                        .background(dotColor)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonochromeBannerCard(
    banner: ShellBanner,
    colors: ShellColors,
    ff: androidx.compose.ui.text.font.FontFamily?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = banner.onClick),
        shape = RoundedCornerShape(20.dp),
        color = colors.surface,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
        ) {
            // Top Row: Badge Pill + Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Badge Pill with Sparkle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(colors.textPrimary.copy(alpha = 0.08f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "✦",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = colors.accent,
                        modifier = Modifier.offset(y = (-0.5).dp)
                    )
                    Text(
                        text = banner.badge.removePrefix("✦").trim(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.5.sp,
                            letterSpacing = 0.2.sp
                        ),
                        color = colors.textPrimary
                    )
                }

                // Easy-Touch Close (×) Button
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .clickable(onClick = banner.onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.Close,
                        contentDescription = "நீக்குக",
                        tint = colors.textSecondary.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Title
            if (banner.title.isNotBlank()) {
                Text(
                    text = banner.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
            }

            // Message text
            Text(
                text = banner.message,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontFamily = ff,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 16.5.sp
                ),
                color = colors.textSecondary,
                modifier = Modifier.padding(end = 6.dp)
            )

            // Bottom Action Pill
            if (banner.actionText.isNotBlank()) {
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(colors.accent.copy(alpha = 0.12f))
                            .clickable(onClick = banner.onClick)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = banner.actionText,
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = colors.accent
                        )
                        Icon(
                            imageVector = MaterialSymbols.Rounded.ArrowForward,
                            contentDescription = banner.actionText,
                            tint = colors.accent,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}
