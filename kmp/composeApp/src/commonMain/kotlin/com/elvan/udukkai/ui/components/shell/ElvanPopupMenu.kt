package com.elvan.udukkai.ui.components.shell

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.elvan.udukkai.core.extensions.cssShadow
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.ShellDefaults

object ElvanMenuState {
    var isMenuOpen by mutableStateOf(false)
}

/**
 * ElvanPopupMenu for Udukkai.
 * Anchored strictly at the top-right corner (or bottom-right when upward),
 * scales diagonally towards bottom-left with a pure Apple-style physical spring bounce,
 * and collapses back into the top-right corner with a smooth bezier curve.
 */
class ElvanPopupMenuItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun ElvanPopupMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    colors: ShellColors,
    items: List<ElvanPopupMenuItem>,
    modifier: Modifier = Modifier,
    /** True for a button at the bottom of the screen: the menu opens upward and grows from its bottom-right corner. */
    upward: Boolean = false
) {
    SideEffect {
        ElvanMenuState.isMenuOpen = expanded
    }
    DisposableEffect(Unit) {
        onDispose {
            ElvanMenuState.isMenuOpen = false
        }
    }

    // Pure Apple spring: natural physical overshoot that settles organically into place
    val progress = remember { Animatable(0f) }
    var mounted by remember { mutableStateOf(false) }
    val haptics = com.elvan.udukkai.ui.components.rememberHaptics()
    LaunchedEffect(expanded) {
        if (expanded) {
            haptics.click()
            mounted = true
            progress.animateTo(1f, spring(dampingRatio = 0.7f, stiffness = 300f))
        } else if (mounted) {
            // A spring back along the same diagonal (not a timed curve), so a close starts gently and picks up from an unfinished open
            progress.animateTo(0f, spring(dampingRatio = 0.82f, stiffness = 380f, visibilityThreshold = 0.02f))
            mounted = false
        }
    }

    // Where the button is, so the menu opens at the same spot when it is drawn in the page layer
    var anchor by remember { mutableStateOf(androidx.compose.ui.geometry.Rect.Zero) }
    androidx.compose.foundation.layout.Spacer(
        Modifier.size(0.dp).onGloballyPositioned { c ->
            c.parentLayoutCoordinates?.let {
                val at = it.positionInRoot()
                anchor = androidx.compose.ui.geometry.Rect(at.x, at.y, at.x + it.size.width, at.y + it.size.height)
            }
        }
    )

    val host = LocalMenuHost.current
    val dismiss = {
        ElvanMenuState.isMenuOpen = false
        onDismissRequest()
    }

    val panel: @Composable () -> Unit = {
        Box(
            modifier = modifier
                .graphicsLayer {
                    val p = progress.value
                    // Natural unified spring scale: continuous from start to finish with no artificial piecewise jumps
                    scaleX = 0.3f + 0.7f * p
                    scaleY = 0.3f + 0.7f * p
                    // Pinned to the centre of the 24dp corner's curve (where the 3-dot sits), not the sharp corner, which is outside the rounded panel
                    val corner = 24.dp.toPx()
                    transformOrigin = if (size.width > 0f && size.height > 0f) {
                        TransformOrigin(1f - corner / size.width, if (upward) 1f - corner / size.height else corner / size.height)
                    } else TransformOrigin(1f, if (upward) 1f else 0f)
                    alpha = (p * 2.8f).coerceIn(0f, 1f)
                }
                .cssShadow(color = Color.Black, alpha = 0.05f, borderRadius = 24.dp, blurRadius = 16.dp, offsetY = 4.dp)
                .frostedGlass(RoundedCornerShape(24.dp), colors, fallbackAlpha = 0.94f)
                .border(
                    width = 0.5.dp,
                    color = colors.floatingBorder.copy(alpha = if (colors.isDark) 0.25f else 0.6f),
                    shape = RoundedCornerShape(24.dp)
                )
                .clip(RoundedCornerShape(24.dp))
                .width(IntrinsicSize.Max)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
        ) {
            MenuRows(items, colors, progress.value, onDismissRequest)
        }
    }

    if (host != null) {
        if (!expanded && !mounted) {
            SideEffect { if (host.content != null && host.onDismiss === dismissKey) host.content = null }
            return
        }
        val content: @Composable () -> Unit = {
            var origin by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
            androidx.compose.ui.layout.Layout(
                content = panel,
                modifier = Modifier.fillMaxSize().onGloballyPositioned { origin = it.positionInRoot() }
            ) { measurables, constraints ->
                val placeable = measurables.first().measure(constraints.copy(minWidth = 0, minHeight = 0))
                layout(constraints.maxWidth, constraints.maxHeight) {
                    val right = anchor.right - origin.x + (if (upward) -24f else 8f)
                    val x = (right - placeable.width).toInt().coerceIn(0, (constraints.maxWidth - placeable.width).coerceAtLeast(0))
                    val y = if (upward) (anchor.bottom - origin.y - 28f - placeable.height).toInt()
                            else (anchor.top - origin.y - 16f).toInt()
                    placeable.place(x, y.coerceIn(0, (constraints.maxHeight - placeable.height).coerceAtLeast(0)))
                }
            }
        }
        SideEffect {
            host.onDismiss = dismiss
            dismissKey = dismiss
            host.content = content
        }
        DisposableEffect(host) { onDispose { if (host.content === content) host.content = null } }
        return
    }

    if (!expanded && !mounted) return

    Popup(
        alignment = if (upward) Alignment.BottomEnd else Alignment.TopEnd,
        offset = if (upward) IntOffset(x = -24, y = -28) else IntOffset(x = 8, y = -16),
        onDismissRequest = dismiss,
        properties = PopupProperties(focusable = true, dismissOnClickOutside = true, dismissOnBackPress = true)
    ) { panel() }
}

/** The one dismiss callback currently shown, so a closed menu clears only its own panel. */
private var dismissKey: (() -> Unit)? = null

@Composable
private fun MenuRows(items: List<ElvanPopupMenuItem>, colors: ShellColors, progress: Float, onDismissRequest: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().graphicsLayer {
            alpha = ((progress - 0.25f) / 0.6f).coerceIn(0f, 1f)
            translationY = (1f - progress.coerceIn(0f, 1f)) * -6.dp.toPx()
        }
    ) {
        items.forEachIndexed { index, item ->
            val isFirst = index == 0
            val isLast = index == items.size - 1
            val shape = when {
                isFirst && isLast -> RoundedCornerShape(24.dp)
                isFirst -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                isLast -> RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                else -> RoundedCornerShape(0.dp)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(shape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ShellDefaults.ripple(colors, bounded = true),
                        onClick = {
                            onDismissRequest()
                            item.onClick()
                        }
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = item.icon, contentDescription = item.title, tint = colors.textPrimary, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = item.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary, fontFamily = LocalAppFontFamily.current)
                Spacer(modifier = Modifier.width(16.dp))
            }
        }
    }
}
