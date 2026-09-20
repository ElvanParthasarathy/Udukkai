package com.elvan.udukkai.ui.screens.editor.invoice.components

import androidx.compose.animation.*
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Animated list container for editor line items.
 */
@Composable
fun ElvanAsaiPattiyal(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        content()
    }
}

/**
 * High-performance fluid animated wrapper for editor cards.
 * Provides smooth 320ms cubic bezier vertical expansion + fade + scale when added,
 * and graceful 280ms collapse + fade-out + scale-down when deleted.
 * Calls [onDeleted] precisely when the collapse animation completes so there is zero abrupt popping.
 */
@Composable
fun ElvanAsaiCard(
    key: Any,
    isInitial: Boolean = false,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (requestDelete: () -> Unit) -> Unit
) {
    val visibleState = remember(key) {
        MutableTransitionState(isInitial).apply {
            targetState = true
        }
    }

    LaunchedEffect(visibleState.currentState, visibleState.targetState) {
        if (!visibleState.currentState && !visibleState.targetState) {
            onDeleted()
        }
    }

    AnimatedVisibility(
        visibleState = visibleState,
        modifier = modifier.fillMaxWidth(),
        enter = expandVertically(
            animationSpec = tween(durationMillis = 320, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)),
            expandFrom = Alignment.Top
        ) + fadeIn(
            animationSpec = tween(durationMillis = 260, easing = LinearEasing)
        ) + scaleIn(
            initialScale = 0.94f,
            animationSpec = tween(durationMillis = 300, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f))
        ),
        exit = shrinkVertically(
            animationSpec = tween(durationMillis = 280, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)),
            shrinkTowards = Alignment.Top
        ) + fadeOut(
            animationSpec = tween(durationMillis = 200, easing = LinearEasing)
        ) + scaleOut(
            targetScale = 0.94f,
            animationSpec = tween(durationMillis = 260, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f))
        )
    ) {
        content {
            if (visibleState.targetState) {
                visibleState.targetState = false
            }
        }
    }
}
