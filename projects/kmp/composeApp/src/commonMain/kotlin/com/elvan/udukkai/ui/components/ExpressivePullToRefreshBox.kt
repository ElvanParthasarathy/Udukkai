package com.elvan.udukkai.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors

private const val PULL_REFRESH_MAX_OFFSET = 180f
private const val PULL_REFRESH_REFRESHING_OFFSET = 180f
private val REFRESH_INDICATOR_SIZE = 48.dp

/**
 * ExpressivePullToRefreshBox - Shared component for consistent pull-to-refresh UX.
 * Ported directly from Neram's ExpressivePullToRefreshBox.
 *
 * Wraps Material3 pullToRefresh modifier in a persistent Box hierarchy so that toggling
 * [enabled] (e.g. entering/exiting selection mode) never disposes or recreates the content subtree,
 * preserving list scroll state and enabling smooth card selection animations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpressivePullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    pullRefreshState: PullToRefreshState = rememberPullToRefreshState(),
    colors: ShellColors = rememberShellColors(),
    showIndicator: Boolean = true,
    overlay: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val fraction = pullRefreshState.distanceFraction

    // Smoothly animate the offset to prevent jumps between pull and refresh states
    val targetOffset = if (isRefreshing && enabled) {
        PULL_REFRESH_REFRESHING_OFFSET
    } else if (enabled) {
        (fraction * PULL_REFRESH_MAX_OFFSET).coerceIn(0f, PULL_REFRESH_MAX_OFFSET)
    } else {
        0f
    }
    val animatedOffset by animateFloatAsState(
        targetValue = targetOffset,
        label = "pull_offset"
    )

    val pullModifier = if (enabled) {
        Modifier.pullToRefresh(
            state = pullRefreshState,
            isRefreshing = isRefreshing,
            enabled = enabled,
            onRefresh = onRefresh
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(pullModifier)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // If overlay is true or not enabled, do NOT translate content
                    translationY = if (overlay || !enabled) 0f else animatedOffset
                }
        ) {
            content()
        }

        if (enabled && showIndicator && (isRefreshing || fraction > 0f)) {
            ExpressiveRefreshIndicator(
                isRefreshing = isRefreshing,
                fraction = fraction,
                colors = colors,
                animatedOffset = animatedOffset,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun ExpressiveRefreshIndicator(
    isRefreshing: Boolean,
    fraction: Float,
    colors: ShellColors,
    animatedOffset: Float,
    modifier: Modifier = Modifier
) {
    // Smoothly animate scale to prevent "breaking" appearance/disappearance
    val targetScale = if (isRefreshing) 1f else fraction.coerceIn(0f, 1f)
    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        label = "pull_scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                translationY = animatedOffset
                scaleX = animatedScale
                scaleY = animatedScale
                alpha = animatedScale
            },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(REFRESH_INDICATOR_SIZE),
            shape = CircleShape,
            color = colors.surface,
            shadowElevation = 6.dp,
            tonalElevation = 2.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = colors.accent,
                        strokeWidth = 2.5.dp,
                        trackColor = colors.accent.copy(alpha = 0.2f)
                    )
                } else {
                    CircularProgressIndicator(
                        progress = { fraction.coerceIn(0f, 1f) },
                        modifier = Modifier.size(24.dp),
                        color = colors.accent,
                        strokeWidth = 2.5.dp,
                        trackColor = colors.border
                    )
                }
            }
        }
    }
}

