package com.elvan.udukkai.ui.navigation

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.extensions.cssShadow
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * Vertical nearness metric for smooth icon & label interpolation as the selection pill glides along the Y axis.
 */
private fun pillNearnessVertical(pillTopPx: Float, pillHeightPx: Float, slotHeightPx: Float, index: Int): Float =
    (1f - abs(pillTopPx + pillHeightPx / 2f - (index + 0.5f) * slotHeightPx) / slotHeightPx).coerceIn(0f, 1f)

/**
 * One tab slot in the landscape vertical rail.
 * Places the localized Pure Tamil label DIRECTLY BELOW the icon (vertical stack) so it matches BottomNavBar tabs.
 */
@Composable
private fun LandscapeVerticalTab(
    tab: NavTab,
    near: () -> Float,
    labelAlpha: Float,
    slotHeight: androidx.compose.ui.unit.Dp,
    idleColor: Color,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    val idle = Modifier.graphicsLayer { val n = near(); alpha = 1f - n * n }
    val active = Modifier.graphicsLayer { alpha = near() }
    val ff = LocalAppFontFamily.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(slotHeight)
            .semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(22.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = null,
                tint = idleColor,
                modifier = Modifier.matchParentSize().then(idle)
            )
            Icon(
                imageVector = tab.activeIcon,
                contentDescription = null,
                tint = activeColor,
                modifier = Modifier.matchParentSize().then(active)
            )
        }

        if (labelAlpha > 0.02f) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier.graphicsLayer { alpha = labelAlpha },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab.getLocalizedLabel(),
                    color = idleColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = ff,
                    maxLines = 1,
                    lineHeight = 11.sp,
                    modifier = idle
                )
                Text(
                    text = tab.getLocalizedLabel(),
                    color = activeColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = ff,
                    maxLines = 1,
                    lineHeight = 11.sp,
                    modifier = active.clearAndSetSemantics { }
                )
            }
        }
    }
}

/**
 * Top-Right floating action button in Landscape mode.
 * - On main screen: displays the Plus Add action button.
 * - Inside subpages: displays the Back Chevron (<) button at the exact same location.
 */
@Composable
fun LandscapeTopActionButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    colors: ShellColors = rememberShellColors(),
    modifier: Modifier = Modifier
) {
    val isDark = colors.isDark
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = tween(120, easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)),
        label = "landscapeTopBtnScale"
    )

    Box(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top))
            .padding(end = 16.dp, top = 16.dp)
            .size(50.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                clip = false
            }
            .cssShadow(color = Color.Black, alpha = 0.08f, blurRadius = 14.dp, offsetY = 3.dp)
            .background(
                color = colors.floatingBg.copy(alpha = 0.88f),
                shape = CircleShape
            )
            .border(
                width = 0.5.dp,
                color = colors.floatingBorder.copy(alpha = if (isDark) 0.15f else 0.6f),
                shape = CircleShape
            )
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, radius = 25.dp),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colors.textPrimary,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Udukkai Landscape Floating Side Navigation Bar (Bottom-Right Trailing Capsule).
 * - Anchored in the bottom-right corner of the landscape screen.
 * - In compact state: 54.dp wide, 46.dp per slot (icons only).
 * - On long press / firm drag: expands BOTH horizontally (to 76.dp) AND vertically (to 58.dp per slot)
 *   with Apple fluid spring physics, smoothly revealing labels beneath icons without text clipping.
 */
@Composable
fun LandscapeSideNavBar(
    selectedTab: NavTab?,
    onTabSelected: (NavTab, Boolean) -> Unit,
    onInteraction: (Boolean) -> Unit = {},
    onDragProgress: (Float) -> Unit = {},
    hideContent: Boolean = false,
    modifier: Modifier = Modifier
) {
    val tabs = NavTab.entries
    val coroutineScope = rememberCoroutineScope()
    val colors = rememberShellColors()
    val isDark = colors.isDark

    val itemCount = tabs.size
    val verticalPadding = 5.dp
    val horizontalPadding = 5.dp

    var isInteracting by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }
    var dragOffsetPx by remember { mutableStateOf<Float?>(null) }
    var touchOffsetFromCenterPx by remember { mutableStateOf(0f) }
    var hoverIndex by remember { mutableStateOf<Int?>(null) }
    var localLockedIndex by remember { mutableStateOf<Int?>(null) }
    var snapNextFrame by remember { mutableStateOf(false) }

    // Apple fluid dual-axis expansion:
    // Width expands 54.dp -> 76.dp so labels have comfortable breathing room
    val railWidth by animateDpAsState(
        targetValue = if (isExpanded) 76.dp else 54.dp,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 420f),
        label = "railWidth"
    )

    // Slot height expands 46.dp -> 58.dp to comfortably fit labels below icons
    val slotHeight by animateDpAsState(
        targetValue = if (isExpanded) 58.dp else 46.dp,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 420f),
        label = "railSlotHeight"
    )

    val indicatorHeight by animateDpAsState(
        targetValue = if (isExpanded) 50.dp else 38.dp,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 420f),
        label = "railIndicatorHeight"
    )

    val indicatorWidth by animateDpAsState(
        targetValue = if (isExpanded) 66.dp else 44.dp,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 420f),
        label = "railIndicatorWidth"
    )

    val labelAlpha by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = tween(durationMillis = 140),
        label = "railLabelAlpha"
    )

    val containerScale by animateFloatAsState(
        targetValue = if (isInteracting) 1.02f else 1.0f,
        animationSpec = PillMotion.press,
        label = "railContainerScale"
    )

    val pillScale by animateFloatAsState(
        targetValue = if (isInteracting) 1.05f else 1.0f,
        animationSpec = PillMotion.press,
        label = "railPillScale"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (hideContent) 0f else 1f,
        animationSpec = tween(150),
        label = "railContentAlpha"
    )

    val density = LocalDensity.current
    val slotHeightPx = with(density) { slotHeight.toPx() }
    val indicatorHeightPx = with(density) { indicatorHeight.toPx() }

    val currentOnTabSelected by rememberUpdatedState(onTabSelected)
    val currentOnInteraction by rememberUpdatedState(onInteraction)
    val currentOnDragProgress by rememberUpdatedState(onDragProgress)

    var pendingSelect by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    val actualIndex = tabs.indexOf(selectedTab).coerceAtLeast(0)
    LaunchedEffect(actualIndex) {
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

    val overlapPy = (slotHeightPx - indicatorHeightPx) / 2f
    val maxTopPx = ((itemCount - 1) * slotHeightPx)
    val minTopPx = 0f

    val targetTopPx = if (isInteracting && dragOffsetPx != null) {
        (dragOffsetPx!! - (slotHeightPx / 2f)).coerceIn(minTopPx, maxTopPx)
    } else {
        (activeVisualIndex * slotHeightPx).coerceIn(minTopPx, maxTopPx)
    }

    val animatedTop = animateFloatAsState(
        targetValue = targetTopPx,
        animationSpec = when {
            snapNextFrame -> snap()
            isInteracting && dragOffsetPx != null -> PillMotion.follow
            else -> PillMotion.settle
        },
        label = "pillY"
    )

    val navBarAlpha by animateFloatAsState(
        targetValue = if (hideContent) 0f else 1f,
        animationSpec = tween(200, easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)),
        label = "railAlpha"
    )

    if (navBarAlpha > 0f) {
        Box(
            modifier = modifier
                .graphicsLayer { alpha = navBarAlpha }
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
                .padding(end = 16.dp, bottom = 16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = containerScale
                        scaleY = containerScale
                        clip = false
                    }
                    .height((slotHeight * itemCount) + (verticalPadding * 2))
                    .width(railWidth),
                contentAlignment = Alignment.TopCenter
            ) {
                // Translucent Capsule Background & Border
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .cssShadow(color = Color.Black, alpha = 0.08f, blurRadius = 16.dp, offsetY = 4.dp)
                        .background(
                            color = colors.floatingBg.copy(alpha = 0.88f),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .border(
                            width = 0.5.dp,
                            color = colors.floatingBorder.copy(alpha = if (isDark) 0.15f else 0.6f),
                            shape = RoundedCornerShape(28.dp)
                        )
                )

                // Gesture Drag Area
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = horizontalPadding, vertical = verticalPadding)
                        .then(
                            if (!hideContent) {
                                Modifier.pointerInput(hideContent) {
                                    awaitEachGesture {
                                        val down = awaitFirstDown(requireUnconsumed = false)
                                        pendingSelect?.cancel()
                                        isInteracting = true
                                        currentOnInteraction(true)

                                        val initialY = down.position.y
                                        val downTime = down.uptimeMillis
                                        hoverIndex = floor(initialY / slotHeightPx).toInt().coerceIn(0, itemCount - 1)
                                        val slotCenter = (hoverIndex!! * slotHeightPx) + (slotHeightPx / 2f)
                                        touchOffsetFromCenterPx = initialY - slotCenter
                                        dragOffsetPx = null

                                        var isDrag = false
                                        val pointerId = down.id

                                        while (true) {
                                            val event = awaitPointerEvent()
                                            val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                                            if (!change.pressed) break

                                            val elapsed = change.uptimeMillis - downTime
                                            val pressure = change.pressure

                                            // Dual-axis expansion on firm press or hold (>180ms)
                                            if (pressure > 1.15f || elapsed > 180L) {
                                                isExpanded = true
                                            }

                                            val currentPos = change.position
                                            if (abs(currentPos.y - down.position.y) > 4f) {
                                                isDrag = true
                                                val targetCenter = currentPos.y - touchOffsetFromCenterPx
                                                dragOffsetPx = targetCenter
                                                hoverIndex = floor(targetCenter / slotHeightPx).toInt().coerceIn(0, itemCount - 1)
                                                currentOnDragProgress(targetCenter / slotHeightPx)
                                                change.consume()
                                            }
                                        }

                                        val finalIndex = hoverIndex
                                        if (finalIndex != null) {
                                            localLockedIndex = finalIndex
                                        }
                                        isInteracting = false
                                        isExpanded = false
                                        currentOnInteraction(false)
                                        dragOffsetPx = null
                                        hoverIndex = null

                                        if (finalIndex != null) {
                                            pendingSelect?.cancel()
                                            pendingSelect = coroutineScope.launch {
                                                PillMotion.awaitLanded(animatedTop, (finalIndex * slotHeightPx).coerceIn(minTopPx, maxTopPx))
                                                currentOnTabSelected(tabs[finalIndex], isDrag)
                                            }
                                        }
                                    }
                                }
                            } else Modifier
                        ),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                alpha = contentAlpha
                                clip = false
                            }
                            .fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        // Master Background Selection Pill
                        Box(
                            modifier = Modifier
                                .offset { IntOffset(0, (animatedTop.value + overlapPy).roundToInt()) }
                                .height(indicatorHeight)
                                .width(indicatorWidth)
                                .graphicsLayer {
                                    val stretch = PillMotion.stretch(targetTopPx, animatedTop.value, slotHeightPx)
                                    scaleY = pillScale * (1f + stretch * 0.35f)
                                    scaleX = pillScale * (1f - stretch * 0.12f)
                                    transformOrigin = TransformOrigin.Center
                                    clip = false
                                }
                                .then(
                                    if (!isDark) Modifier.cssShadow(color = Color.Black, alpha = 0.04f, blurRadius = 4.dp, offsetY = 1.dp) else Modifier
                                )
                                .background(
                                    color = if (isDark) Color(0xFF333333) else Color(0xFFE5E5E5),
                                    shape = RoundedCornerShape(20.dp)
                                )
                        )

                        // Vertical Tab Items
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            tabs.forEachIndexed { index, tab ->
                                LandscapeVerticalTab(
                                    tab = tab,
                                    near = { pillNearnessVertical(animatedTop.value, indicatorHeightPx, slotHeightPx, index) },
                                    labelAlpha = labelAlpha,
                                    slotHeight = slotHeight,
                                    idleColor = colors.textSecondary,
                                    activeColor = colors.textPrimary,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
