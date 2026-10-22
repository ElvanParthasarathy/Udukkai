package com.elvan.udukkai.ui.components.shell

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.rememberShellColors

/**
 * Animated sweep shimmer brush that moves horizontally with a subtle ~24° skew across skeleton placeholders.
 */
@Composable
fun rememberShimmerBrush(
    targetValue: Float = 1400f,
    durationMillis: Int = 1100
): Brush {
    val colors = rememberShellColors()
    val isDark = colors.isDark

    val shimmerColors = if (isDark) {
        listOf(
            Color(0xFF222222),
            Color(0xFF333333),
            Color(0xFF222222)
        )
    } else {
        listOf(
            Color(0xFFE8E8E8),
            Color(0xFFF6F6F6),
            Color(0xFFE8E8E8)
        )
    }

    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 450f, 0f),
        end = Offset(translateAnim, 200f)
    )
}

/**
 * Skeleton card placeholder matching invoice/receipt cards for initial loading & search loading states.
 */
@Composable
fun UruvakkuCardSkeleton(
    modifier: Modifier = Modifier,
    shimmerBrush: Brush = rememberShimmerBrush()
) {
    val shape = RoundedCornerShape(24.dp)
    val colors = rememberShellColors()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Leading icon placeholder (matches 26x26)
            Box(
                modifier = Modifier
                    .padding(top = 1.dp)
                    .size(26.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(shimmerBrush)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Row 1: Name + Chevron placeholder
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(shimmerBrush)
                    )
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(shimmerBrush)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Row 2: Bill # • Date placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.42f)
                        .height(13.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Row 3: Place / Oor + Amount placeholder
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.32f)
                            .height(13.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(shimmerBrush)
                    )
                    Box(
                        modifier = Modifier
                            .size(width = 75.dp, height = 16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(shimmerBrush)
                    )
                }
            }
        }
    }
}

/**
 * Skeleton card placeholder matching customer (Vaangunar) and item (Uruppadi) list cards.
 */
@Composable
fun ItemCardSkeleton(
    modifier: Modifier = Modifier,
    shimmerBrush: Brush = rememberShimmerBrush(),
    hasThirdLine: Boolean = false
) {
    val colors = rememberShellColors()
    val shape = RoundedCornerShape(Dimens.CardRadius)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface)
            .padding(horizontal = Dimens.CardPaddingHorizontal, vertical = Dimens.CardPaddingVertical)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading icon placeholder (matches 26x26)
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(shimmerBrush)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Primary title
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush)
                )

                // Secondary line
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.35f)
                        .height(13.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush)
                )

                if (hasThirdLine) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.25f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(shimmerBrush)
                    )
                }
            }
        }
    }
}

@Composable
fun CustomerCardSkeleton(
    modifier: Modifier = Modifier,
    shimmerBrush: Brush = rememberShimmerBrush()
) = ItemCardSkeleton(modifier = modifier, shimmerBrush = shimmerBrush, hasThirdLine = true)


/**
 * Skeleton placeholder for the Mugappu Home Screen Bento Stats Grid (2 side-by-side summary tiles).
 */
@Composable
fun HomeStatsSkeleton(
    modifier: Modifier = Modifier,
    shimmerBrush: Brush = rememberShimmerBrush()
) {
    val colors = rememberShellColors()
    val shape = RoundedCornerShape(22.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ContentPadding),
        horizontalArrangement = Arrangement.spacedBy(Dimens.ItemSpacing)
    ) {
        repeat(2) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(124.dp)
                    .clip(shape)
                    .background(colors.surface)
                    .padding(14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Icon placeholder
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(shimmerBrush)
                    )
                    // Label & value placeholder
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.55f)
                                .height(12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(shimmerBrush)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.80f)
                                .height(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(shimmerBrush)
                        )
                    }
                }
            }
        }
    }
}


