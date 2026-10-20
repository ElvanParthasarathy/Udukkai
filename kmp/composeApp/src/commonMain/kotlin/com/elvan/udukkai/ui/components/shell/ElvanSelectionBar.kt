package com.elvan.udukkai.ui.components.shell

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * ElvanSelectionBar — Floating multi-selection action bar.
 * Matches BottomNavBar position and 56.dp height exactly.
 * In One UI mode, hosts Delete ("நீக்கு") and optional Copy ("நகலெடு") action pills.
 * Powered by the shared ElvanFloatingActionBar component.
 */
@Composable
fun ElvanSelectionBar(
    visible: Boolean,
    selectedCount: Int,
    onDelete: () -> Unit,
    onCopy: (() -> Unit)? = null,
    colors: ShellColors = rememberShellColors(),
    modifier: Modifier = Modifier
) {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 200, easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)),
        label = "thervuPattaiAlpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (visible) 1.0f else 0.92f,
        animationSpec = tween(durationMillis = 200, easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)),
        label = "thervuPattaiScale"
    )

    if (visible || alpha > 0f) {
        val copyLabel = K.copyBtn.tr()
        val deleteLabel = K.deleteBtn.tr()

        val actions = mutableListOf<ElvanFloatingAction>()
        if (onCopy != null) {
            actions.add(
                ElvanFloatingAction(
                    label = copyLabel,
                    icon = MaterialSymbols.Rounded.ContentCopy,
                    onClick = onCopy,
                    enabled = selectedCount == 1
                )
            )
        }
        actions.add(
            ElvanFloatingAction(
                label = deleteLabel,
                icon = MaterialSymbols.Rounded.Delete,
                onClick = onDelete,
                enabled = selectedCount > 0,
                isDestructive = true
            )
        )

        ElvanFloatingActionBar(
            actions = actions,
            colors = colors,
            modifier = modifier.graphicsLayer {
                this.alpha = alpha
                this.scaleX = scale
                this.scaleY = scale
            }
        )
    }
}
