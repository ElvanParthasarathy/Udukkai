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
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.theme.LocalShellColors
import com.elvan.udukkai.theme.gradientFor
import com.elvan.udukkai.theme.preventBrokenLigatures
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
    /** While cards are being selected a tap only ticks the card: no ripple and no press dip, so select-all stays calm. */
    isSelectionMode: Boolean = false,
    padding: PaddingValues = PaddingValues(16.dp),
    borderRadius: Dp = 24.dp,
    content: @Composable () -> Unit
) {
    val colors = rememberShellColors()
    val isDark = colors.isDark

    val defaultBg = LocalShellColors.current.surface
    // A selected card only gets its tick (no tint, no flash): select-all stays calm
    val bgColor = defaultBg

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // As on iOS: the card sinks a little under the finger and springs back with a soft bounce when let go
    // (after a long press too, as it turns into a selected card)
    val scale by animateFloatAsState(
        targetValue = if (isPressed && (onClick != null || onLongClick != null)) 0.975f else 1.0f,
        animationSpec = if (isPressed) tween(100) else spring(dampingRatio = 0.55f, stiffness = 500f),
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
        // One calm ease for the whole list: a spring made every card lurch at a different moment
        enter = expandHorizontally(
            animationSpec = tween(300, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)),
            expandFrom = Alignment.Start
        ) + fadeIn(
            animationSpec = tween(260, delayMillis = 40, easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f))
        ),
        // Out on a firm spring too (no bounce), so the content slides back left fluidly instead of on a stiff curve
        exit = shrinkHorizontally(
            animationSpec = tween(260, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)),
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
                ElvanCheckMark(
                    selected = isSelected,
                    selectedTint = colors.modeAccent,
                    idleTint = if (isDark) Color.White.copy(alpha = 0.38f) else Color.Black.copy(alpha = 0.32f),
                    size = 24.dp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        }
    }
}

/**
 * A round tick that fills smoothly: the empty ring fades into the filled tick with a small pop, and the colour
 * blends between the two, instead of swapping in one frame.
 */
@Composable
fun ElvanCheckMark(
    selected: Boolean,
    selectedTint: Color,
    idleTint: Color,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val on by androidx.compose.animation.core.animateFloatAsState(
        if (selected) 1f else 0f,
        tween(460, easing = CubicBezierEasing(0.34f, 1.35f, 0.64f, 1f)),
        label = "checkMark"
    )
    val tint = androidx.compose.ui.graphics.lerp(idleTint, selectedTint, on)
    Box(modifier.size(size)) {
        Icon(
            MaterialSymbols.Rounded.RadioButtonUnchecked, null, tint = tint,
            modifier = Modifier.matchParentSize().graphicsLayer { alpha = 1f - on }
        )
        Icon(
            MaterialSymbols.Rounded.CheckCircleFill, null, tint = tint,
            modifier = Modifier.matchParentSize().graphicsLayer {
                alpha = on
                val pop = 0.6f + 0.4f * on
                scaleX = pop
                scaleY = pop
            }
        )
    }
}

/**
 * The grey line under a card's name. When the record belongs to certain businesses only, their short label sits
 * a small line of its own below the name.
 */
@Composable
fun ElvanCardSecondLine(
    text: String,
    label: String,
    color: Color,
    labelColor: Color,
    fontFamily: androidx.compose.ui.text.font.FontFamily?,
    isSelectionMode: Boolean = false
) {
    if (text.isBlank() && label.isBlank()) return
    var measuredLines by remember { mutableStateOf<Int?>(null) }
    Spacer(modifier = Modifier.height(2.dp))
    // The business tag goes on a line of its own under the name: at the right end it squeezed a long name into "..."
    Column(modifier = Modifier.fillMaxWidth()) {
        if (text.isNotBlank()) {
            androidx.compose.material3.Text(
                text = text.preventBrokenLigatures(),
                modifier = Modifier.fillMaxWidth(),
                style = androidx.compose.ui.text.TextStyle(fontFamily = fontFamily, fontSize = 13.5.sp, color = color),
                maxLines = measuredLines ?: 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                onTextLayout = { layoutResult ->
                    if (!isSelectionMode) {
                        measuredLines = layoutResult.lineCount
                    }
                }
            )
        }
        if (label.isNotBlank()) {
            if (text.isNotBlank()) Spacer(modifier = Modifier.height(2.dp))
            androidx.compose.material3.Text(
                text = label.preventBrokenLigatures(),
                style = androidx.compose.ui.text.TextStyle(fontFamily = fontFamily, fontSize = 12.5.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium, color = labelColor),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * An icon in a round container filled with a soft wash of its own colour, as Samsung's My Files does:
 * a dark, muted circle of the hue with the bright hue on the icon (light mode: a pale circle, a deep icon).
 */
@Composable
fun ElvanTintedIcon(
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    iconSize: Dp = 22.dp,
    contentDescription: String? = null
) {
    val dark = LocalShellColors.current.isDark
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    listOf(tint.copy(alpha = if (dark) 0.30f else 0.20f), tint.copy(alpha = if (dark) 0.15f else 0.10f))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(iconSize))
    }
}

/** Leading icon for cards: the card's colour in a tinted circle. */
@Composable
fun ElvanCardLeadingIcon(
    icon: ImageVector,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    iconSize: Dp = 20.dp,
    borderRadius: Dp = 0.dp,
    tint: Color? = null,
    gradientColors: List<Color>? = null
) {
    val colors = LocalShellColors.current
    // Cards stay monochrome: a neutral circle, the glyph in the text colour
    Box(
        modifier = modifier.size(size).clip(CircleShape).background(colors.iconBg),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription, tint = colors.textPrimary, modifier = Modifier.size(iconSize))
    }
}


