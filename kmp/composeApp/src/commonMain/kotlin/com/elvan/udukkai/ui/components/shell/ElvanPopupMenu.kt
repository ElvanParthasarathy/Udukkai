package com.elvan.udukkai.ui.components.shell

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
 * Spawns directly on top of the 3-dot icon button, scales from 20% to 100%
 * with an easeOutCubic curve anchored at the top-right corner, and uses
 * frosted translucent styling with 24dp rounded corners.
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

    // As on iOS: the menu grows out of the button on a spring and settles with a slight overshoot, then shrinks back
    // into it quickly when closed (it stays on screen until that has played)
    val progress = remember { Animatable(0f) }
    var mounted by remember { mutableStateOf(false) }
    LaunchedEffect(expanded) {
        if (expanded) {
            mounted = true
            progress.animateTo(1f, spring(dampingRatio = 0.78f, stiffness = 420f))
        } else if (mounted) {
            progress.animateTo(0f, tween(140, easing = CubicBezierEasing(0.4f, 0f, 1f, 1f)))
            mounted = false
        }
    }

    if (!expanded && !mounted) return

    Popup(
        alignment = if (upward) Alignment.BottomEnd else Alignment.TopEnd,
        offset = if (upward) IntOffset(x = 0, y = -8) else IntOffset(x = 8, y = -16),
        onDismissRequest = {
            ElvanMenuState.isMenuOpen = false
            onDismissRequest()
        },
        properties = PopupProperties(
            focusable = true,
            dismissOnClickOutside = true,
            dismissOnBackPress = true
        )
    ) {
        Box(
            modifier = modifier
                .graphicsLayer {
                    val p = progress.value
                    scaleX = 0.5f + 0.5f * p
                    scaleY = 0.5f + 0.5f * p
                    transformOrigin = TransformOrigin(1f, if (upward) 1f else 0f) // Anchored at the corner where the button is
                    alpha = (p * 2.5f).coerceIn(0f, 1f)
                }
                .cssShadow(
                    color = Color.Black,
                    alpha = 0.05f,
                    blurRadius = 16.dp,
                    offsetY = 4.dp
                )
                .background(
                    color = colors.floatingBg.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(24.dp)
                )
                .border(
                    width = 0.5.dp,
                    color = colors.floatingBorder.copy(alpha = if (colors.isDark) 0.25f else 0.6f),
                    shape = RoundedCornerShape(24.dp)
                )
                .clip(RoundedCornerShape(24.dp))
                .width(IntrinsicSize.Max)
        ) {
            Column(
                // The rows come in just behind the panel, so the menu reads as growing first, then filling
                modifier = Modifier.fillMaxWidth().graphicsLayer {
                    alpha = ((progress.value - 0.25f) / 0.6f).coerceIn(0f, 1f)
                    translationY = (1f - progress.value.coerceIn(0f, 1f)) * -6.dp.toPx()
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
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = colors.textPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = item.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary,
                            fontFamily = LocalAppFontFamily.current
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                }
            }
        }
    }
}
