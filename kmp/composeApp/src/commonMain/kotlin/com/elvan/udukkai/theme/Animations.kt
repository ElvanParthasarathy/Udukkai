package com.elvan.udukkai.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Reusable animation specifications and utilities.
 */
object Animations {

    // --- Skeleton Pulse ---
    /**
     * Provides an infinitely animating alpha value (0.3f <-> 0.7f) for skeleton loading states.
     */
    @Composable
    fun skeletonAlpha(): Float {
        val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.7f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "skeletonAlpha"
        )
        return alpha
    }

    // --- Pull-to-Refresh ---
    object PullRefresh {
        const val MaxOffset = 180f
    }

    // --- Tactile Button Physics ---
    /**
     * Modifier that applies Samsung-style asymmetric spring physics on press/release.
     * Press: Snappy scale-down (high stiffness, low damping)
     * Release: Slow, heavy bounce-back (low stiffness)
     */
    fun Modifier.tactilePress(
        pressScale: Float = 0.9f,
        enabled: Boolean = true,
        onClick: () -> Unit = {}
    ): Modifier = composed {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()

        // Asymmetric springs: fast press, slow release
        val scale by animateFloatAsState(
            targetValue = if (isPressed && enabled) pressScale else 1f,
            animationSpec = spring(
                dampingRatio = if (isPressed) 0.55f else Spring.DampingRatioMediumBouncy,
                stiffness = if (isPressed) 800f else 150f
            ),
            label = "tactileScale"
        )

        this
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
    }
}
