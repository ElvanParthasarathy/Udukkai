package com.elvan.udukkai.ui.screens.create.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.elvan.udukkai.ui.navigation.PillMotion
import com.elvan.udukkai.ui.navigation.pillNearness
import kotlin.math.floor
import kotlin.math.roundToInt
import com.elvan.udukkai.theme.LocalShellColors

data class PillShifterItem(
    val label: String,
    val icon: ImageVector? = null,
    val activeIcon: ImageVector? = icon
)

/**
 * Pixel-perfect port of Neram's Schedule ViewTypeTabsRow Pill Shifter.
 *
 * Features:
 * - Fluid pointer dragging and real-time thumb tracking.
 * - Symmetrical expansion in all directions when pressed/dragged.
 * - 1.02x outer container interactive zoom on touch.
 * - Snap-to-slot animation with smooth easing curve.
 * - Distinct active & inactive colors and icon variants.
 * - Supports fixed centered mode (isFullWidth = false) and end-to-end adaptive mode (isFullWidth = true).
 */
@Composable
fun ElvanPillShifter(
    items: List<PillShifterItem>,
    selectedIndex: Int,
    onIndexSelected: (Int) -> Unit,
    colors: ShellColors = rememberShellColors(),
    modifier: Modifier = Modifier,
    isSmall: Boolean = false,
    isFullWidth: Boolean = false,
    height: Dp = if (isFullWidth || isSmall) 40.dp else 48.dp,
    onInteraction: (Boolean) -> Unit = {},
    onDragProgress: (Float) -> Unit = {}
) {
    val ff = LocalAppFontFamily.current
    val isDark = colors.isDark
    val itemCount = items.size.coerceAtLeast(1)
    val actualIndex = selectedIndex.coerceIn(0, itemCount - 1)

    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    var isInteracting by remember { mutableStateOf(false) }
    var dragOffsetPx by remember { mutableStateOf<Float?>(null) }
    var touchOffsetFromCenterPx by remember { mutableStateOf(0f) }
    var hoverIndex by remember { mutableStateOf<Int?>(null) }
    var localLockedIndex by remember { mutableStateOf<Int?>(null) }
    var snapNextFrame by remember { mutableStateOf(false) }
    // As in the tab bar: the list behind swaps only once the pill has landed, and a newer tap cancels an older one
    var pendingSelect by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    val currentOnIndexSelected by rememberUpdatedState(onIndexSelected)

    LaunchedEffect(actualIndex) {
        // Our own tap: let the pill finish gliding there. A change from elsewhere: jump straight there.
        if (localLockedIndex != actualIndex) snapNextFrame = true
        localLockedIndex = null
    }
    LaunchedEffect(snapNextFrame) {
        if (snapNextFrame) {
            kotlinx.coroutines.yield()
            snapNextFrame = false
        }
    }

    val activeVisualIndex = if (isInteracting && hoverIndex != null) {
        hoverIndex!!
    } else {
        localLockedIndex ?: actualIndex
    }

    // Outer AnimatedScale: 1.02x on interaction (BottomNavBar / Schedule match)
    val containerScale by animateFloatAsState(
        targetValue = if (isInteracting) 1.02f else 1.0f,
        animationSpec = PillMotion.press,
        label = "containerScale"
    )

    // Pill scale on interaction: Symmetrical expansion in all directions, with the lens bounce of the tab bar
    val pillScaleX by animateFloatAsState(
        targetValue = if (isInteracting) 1.055f else 1.0f,
        animationSpec = PillMotion.press,
        label = "pillScaleX"
    )
    val pillScaleY by animateFloatAsState(
        targetValue = if (isInteracting) 1.20f else 1.0f,
        animationSpec = PillMotion.press,
        label = "pillScaleY"
    )

    val fixedLayoutWidth = if (isSmall) 104.dp else 136.dp
    val fixedHorizontalPadding = if (isSmall) 4.dp else 8.dp
    val fixedTotalWidth = (fixedLayoutWidth * itemCount) + (fixedHorizontalPadding * 2)

    BoxWithConstraints(
        modifier = modifier
            .graphicsLayer {
                scaleX = containerScale
                scaleY = containerScale
                clip = false
            }
            .height(height)
            .then(if (isFullWidth) Modifier.fillMaxWidth() else Modifier.width(fixedTotalWidth)),
        contentAlignment = Alignment.Center
    ) {
        val horizontalPadding = if (isFullWidth || isSmall) 4.dp else 8.dp
        val verticalPadding = if (isFullWidth || isSmall) 3.dp else 4.dp

        val (layoutWidth, bgWidth, contentWidth) = if (isFullWidth) {
            val available = (maxWidth - (horizontalPadding * 2)).coerceAtLeast(0.dp)
            val slot = available / itemCount
            Triple(slot, slot, available)
        } else {
            val fixedSlot = if (isSmall) 104.dp else 136.dp
            val fixedBg = if (isSmall) 104.dp else 144.dp
            Triple(fixedSlot, fixedBg, fixedSlot * itemCount)
        }

        val layoutWidthPx = with(density) { layoutWidth.toPx() }
        val bgWidthPx = with(density) { bgWidth.toPx() }

        // Pixel offset clamping
        val overlapPx = if (isFullWidth) 0f else (bgWidthPx - layoutWidthPx) / 2f
        val maxLeftPx = if (isFullWidth) {
            ((itemCount - 1) * layoutWidthPx).coerceAtLeast(0f)
        } else {
            ((itemCount - 1) * layoutWidthPx) - overlapPx
        }
        val minLeftPx = if (isFullWidth) 0f else -overlapPx

        val targetLeftPx = if (isInteracting && dragOffsetPx != null) {
            (dragOffsetPx!! - (bgWidthPx / 2f)).coerceIn(minLeftPx, maxLeftPx)
        } else {
            ((activeVisualIndex * layoutWidthPx) - overlapPx).coerceIn(minLeftPx, maxLeftPx)
        }

        val animatedLeft = animateFloatAsState(
            targetValue = targetLeftPx,
            animationSpec = when {
                snapNextFrame -> snap()
                isInteracting && dragOffsetPx != null -> PillMotion.follow
                else -> PillMotion.settle
            },
            label = "pillX"
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    color = colors.surface,
                    shape = CircleShape
                )
        )

        // Layer 2: Foreground & Draggable Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding, vertical = verticalPadding)
                .pointerInput(itemCount, layoutWidthPx) {
                    if (layoutWidthPx <= 0f) return@pointerInput
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        pendingSelect?.cancel()
                        isInteracting = true
                        onInteraction(true)

                        val initialX = down.position.x
                        hoverIndex = floor(initialX / layoutWidthPx).toInt().coerceIn(0, itemCount - 1)
                        val slotCenter = (hoverIndex!! * layoutWidthPx) + (layoutWidthPx / 2f)
                        touchOffsetFromCenterPx = initialX - slotCenter
                        dragOffsetPx = null

                        val pointerId = down.id

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                            if (!change.pressed) {
                                break
                            }
                            val currentPos = change.position
                            if (kotlin.math.abs(currentPos.x - down.position.x) > 4f) {
                                val targetCenter = currentPos.x - touchOffsetFromCenterPx
                                dragOffsetPx = targetCenter
                                hoverIndex = floor(targetCenter / layoutWidthPx).toInt().coerceIn(0, itemCount - 1)
                                onDragProgress(targetCenter / layoutWidthPx)
                                change.consume()
                            }
                        }

                        val finalIndex = hoverIndex
                        if (finalIndex != null) {
                            localLockedIndex = finalIndex
                        }
                        isInteracting = false
                        onInteraction(false)
                        dragOffsetPx = null
                        hoverIndex = null

                        if (finalIndex != null) {
                            pendingSelect?.cancel()
                            pendingSelect = coroutineScope.launch {
                                PillMotion.awaitLanded(animatedLeft, ((finalIndex * layoutWidthPx) - overlapPx).coerceIn(minLeftPx, maxLeftPx))
                                currentOnIndexSelected(finalIndex)
                            }
                        }
                    }
                },
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .width(contentWidth)
                    .fillMaxHeight()
            ) {
                // Master Background Pill (Detached & Draggable)
                Box(
                    modifier = Modifier
                        .offset { IntOffset(animatedLeft.value.roundToInt(), 0) }
                        .fillMaxHeight()
                        .width(bgWidth)
                        .graphicsLayer {
                            val stretch = PillMotion.stretch(targetLeftPx, animatedLeft.value, bgWidthPx)
                            scaleX = pillScaleX * (1f + stretch * 0.35f)
                            scaleY = pillScaleY * (1f - stretch * 0.12f)
                            transformOrigin = TransformOrigin.Center
                            clip = false
                        }
                        .background(
                            color = if (isDark) Color(0xFF333333)
                            else Color(0xFFE5E5E5),
                            shape = CircleShape
                        )
                )

                // Foreground Tabs Content
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val idleColor = if (isDark) Color(0xFF9E9E9E) else Color(0xFF7C7C80)
                    val activeColor = LocalShellColors.current.textPrimary
                    items.forEachIndexed { index, item ->
                        // Idle and active looks lie on top of each other and trade places as the pill passes, read only
                        // while drawing so the moving pill never rebuilds the tabs (as in the bottom bar's PillTab)
                        val near = { pillNearness(animatedLeft.value, bgWidthPx, layoutWidthPx, index) }
                        val idle = Modifier.graphicsLayer { val n = near(); alpha = 1f - n * n }
                        val active = Modifier.graphicsLayer { alpha = near() }

                        Box(
                            modifier = Modifier
                                .width(layoutWidth)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (item.icon != null) {
                                    Box(Modifier.size(if (itemCount > 2) 16.dp else 18.dp)) {
                                        Icon(item.icon, null, tint = idleColor, modifier = Modifier.matchParentSize().then(idle))
                                        Icon(item.activeIcon ?: item.icon, null, tint = activeColor, modifier = Modifier.matchParentSize().then(active))
                                    }
                                    Spacer(modifier = Modifier.width(if (itemCount > 2) 6.dp else 8.dp))
                                }
                                Box(contentAlignment = Alignment.CenterStart) {
                                    listOf(Triple(FontWeight.Medium, idleColor, idle), Triple(FontWeight.SemiBold, activeColor, active)).forEach { (weight, color, layer) ->
                                        Text(
                                            text = item.label.preventBrokenLigatures(),
                                            style = TextStyle(
                                                fontFamily = ff,
                                                fontSize = if (isFullWidth) 13.sp else (if (itemCount > 2) 13.sp else 14.sp),
                                                fontWeight = weight
                                            ),
                                            color = color,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = layer.then(if (weight == FontWeight.SemiBold) Modifier.clearAndSetSemantics { } else Modifier)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
