package com.elvan.udukkai.ui.components.shell

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
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
 * Reproduction of Neram / Flutter Niril's ElvanPopupMenu.
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
    modifier: Modifier = Modifier
) {
    SideEffect {
        ElvanMenuState.isMenuOpen = expanded
    }
    DisposableEffect(Unit) {
        onDispose {
            ElvanMenuState.isMenuOpen = false
        }
    }

    if (!expanded) return

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val animProgress by animateFloatAsState(
        targetValue = if (isVisible) 1.0f else 0.0f,
        animationSpec = tween(150, easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)),
        label = "popupAnim"
    )

    Popup(
        alignment = Alignment.TopEnd,
        offset = IntOffset(x = 8, y = -16),
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
                    scaleX = 0.2f + (0.8f * animProgress)
                    scaleY = 0.2f + (0.8f * animProgress)
                    transformOrigin = TransformOrigin(1f, 0f) // Anchored at top-right
                    alpha = animProgress
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
                .width(IntrinsicSize.Max)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
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
