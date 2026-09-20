package com.elvan.udukkai.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.theme.LocalShellColors
import com.elvan.udukkai.theme.gradientFor
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * A completely reusable Elvan Card component.
 * Exact 1:1 Kotlin Compose port of Flutter's ElvanCommonCard.
 *
 * It wraps the content to provide the native ripple effect upon tap.
 * It also smoothly animates press scale (0.985f) and background color changes
 * when `isSelected` changes.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ElvanCommonCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    isSelected: Boolean = false,
    padding: PaddingValues = PaddingValues(16.dp),
    borderRadius: Dp = 24.dp,
    content: @Composable () -> Unit
) {
    val colors = rememberShellColors()
    val isDark = colors.isDark

    val defaultBg = LocalShellColors.current.surface
    val selectedBg = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f)
    val bgColor = if (isSelected) selectedBg else defaultBg

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && (onClick != null || onLongClick != null)) 0.985f else 1.0f,
        animationSpec = tween(
            durationMillis = if (isPressed) 100 else 200
        ),
        label = "pothuAttaiPressScale"
    )

    val shape = RoundedCornerShape(borderRadius)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(bgColor)
            .then(
                if (onClick != null || onLongClick != null) {
                    Modifier.combinedClickable(
                        interactionSource = interactionSource,
                        indication = ripple(bounded = true, color = colors.ripple),
                        onClick = { onClick?.invoke() },
                        onLongClick = onLongClick
                    )
                } else Modifier
            )
            .padding(padding)
    ) {
        content()
    }
}

/**
 * Smoothly gliding One UI selection checkbox for list cards.
 * - When `isSelectionMode` is false: completely hidden (0 width), so cards have NO index circles.
 * - When `isSelectionMode` is true: smoothly slides in from the left, shifting the card content right.
 * - When `isSelected` is true: shows filled checkmark circle (CheckCircleFill) in accent color.
 * - When `isSelected` is false: shows empty circle outline (RadioButtonUnchecked).
 */
@Composable
fun ElvanCardSelector(
    isSelectionMode: Boolean,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalShellColors.current
    val isDark = colors.isDark

    AnimatedVisibility(
        visible = isSelectionMode,
        enter = expandHorizontally(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            expandFrom = Alignment.Start
        ) + fadeIn(
            animationSpec = tween(220, easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f))
        ),
        exit = shrinkHorizontally(
            animationSpec = tween(180, easing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)),
            shrinkTowards = Alignment.Start
        ) + fadeOut(
            animationSpec = tween(140)
        ),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 1.dp)
                    .size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSelected) {
                        MaterialSymbols.Rounded.CheckCircleFill
                    } else {
                        MaterialSymbols.Rounded.RadioButtonUnchecked
                    },
                    contentDescription = null,
                    tint = if (isSelected) {
                        colors.modeAccent
                    } else {
                        if (isDark) Color.White.copy(alpha = 0.38f) else Color.Black.copy(alpha = 0.32f)
                    },
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        }
    }
}

/**
 * Leading icon for cards, rendering the symbol alone with a rich, vibrant gradient.
 */
@Composable
fun ElvanCardLeadingIcon(
    icon: ImageVector,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    size: Dp = 26.dp,
    iconSize: Dp = 26.dp,
    borderRadius: Dp = 0.dp,
    tint: Color? = null,
    gradientColors: List<Color>? = null
) {
    val colors = LocalShellColors.current
    val effectiveGradient = gradientColors ?: colors.gradientFor(tint ?: colors.modeAccent)
    val effectiveTint = tint ?: colors.modeAccent

    Box(
        modifier = modifier
            .padding(top = 1.dp)
            .size(iconSize),
        contentAlignment = Alignment.Center
    ) {
        if (effectiveGradient != null && effectiveGradient.size >= 2) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier
                    .size(iconSize)
                    .graphicsLayer(alpha = 0.99f)
                    .drawWithCache {
                        val brush = Brush.linearGradient(
                            colors = effectiveGradient,
                            start = Offset.Zero,
                            end = Offset(this.size.width, this.size.height)
                        )
                        onDrawWithContent {
                            drawContent()
                            drawRect(brush = brush, blendMode = BlendMode.SrcIn)
                        }
                    }
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = effectiveTint,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}


