package com.elvan.udukkai.ui.screens.editor.invoice.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Animated list wrapper for editor line items.
 * Matches Flutter's `ElvanAsaiPattiyal` 1:1:
 * Provides smooth 300ms easeOutQuart expansion and collapse when items are added or removed.
 */
@Composable
fun ElvanAsaiPattiyal(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = CubicBezierEasing(0.25f, 1.0f, 0.5f, 1.0f) // Curves.easeOutQuart
                )
            )
    ) {
        content()
    }
}
