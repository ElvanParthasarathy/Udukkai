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
 * Vertical nearness metric for smooth icon interpolation as the selection pill glides along the Y axis.
 */
private fun pillNearnessVertical(pillTopPx: Float, pillHeightPx: Float, slotHeightPx: Float, index: Int): Float =
    (1f - abs(pillTopPx + pillHeightPx / 2f - (index + 0.5f) * slotHeightPx) / slotHeightPx).coerceIn(0f, 1f)

/**
 * One tab slot in the landscape vertical rail.
 * Idle look and active look smoothly blend via nearness metric.
 * When expanded (deep press/hold drag), localized label crossfades in alongside the icon.
 */
@Composable
private fun LandscapePillTab(
    tab: NavTab,
    near: () -> Float,
    labelAlpha: Float,
    isExpanded: Boolean,
    idleColor: Color,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    val idle = Modifier.graphicsLayer { val n = near(); alpha = 1f - n * n }
    val active = Modifier.graphicsLayer { alpha = near() }
    val ff = LocalAppFontFamily.current

    Row(
        modifier = modifier.semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier.size(44.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = null,
                tint = idleColor,
                modifier = Modifier.size(24.dp).then(idle)
            )
            Icon(
                imageVector = tab.activeIcon,
                contentDescription = null,
                tint = activeColor,
                modifier = Modifier.size(24.dp).then(active)
            )
        }

        if (labelAlpha > 0.02f) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer { alpha = labelAlpha },
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = tab.getLocalizedLabel(),
                    color = idleColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = ff,
                    maxLines = 1,
                    modifier = idle
                )
                Text(
                    text = tab.getLocalizedLabel(),
                    color = activeColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = ff,
                    maxLines = 1,
                    modifier = active.clearAndSetSemantics { }
                )
            }
        }
    }
}

/**
 * Udukkai Landscape Floating Side Navigation Bar (Inspired by iPhone Duo Trailing Rail).
 * - Docked along the trailing (right) vertical edge, safely respecting camera notch & navigation bar insets.
 * - In default resting state: compact vertical capsule showing pure icons (no labels) with vertical PillMotion glide.
 * - On light drag: vertical scrubbing glides the active selection pill smoothly with liquid glass stretch physics.
 * - On firm / hold drag: expands horizontally to reveal localized labels while preserving full drag physics.
 */
@Composable
fun LandscapeSideNavBar(
    selectedTab: NavTab?,
    onTabSelected: (NavTab, Boolean) -> Unit,
    onInteraction: (Boolean) -> Unit = {},
    onDragProgress: (Float) -> Unit = {},
    hideContent: Boolean = false,
    onAddClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val tabs = NavTab.entries
    val coroutineScope = rememberCoroutineScope()
    val colors = rememberShellColors()
    val isDark = colors.isDark

    val itemCount = tabs.size
    val slotHeight = 50.dp
    val verticalPadding = 5.dp
    val horizontalPadding = 5.dp

    var isInteracting by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }
    var dragOffsetPx by remember { mutableStateOf<Float?>(null) }
    var touchOffsetFromCenterPx by remember { mutableStateOf(0f) }
    var hoverIndex by remember { mutableStateOf<Int?>(null) }
    var localLockedIndex by remember { mutableStateOf<Int?>(null) }
    var snapNextFrame by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val slotHeightPx = with(density) { slotHeight.toPx() }
    val indicatorHeight = 44.dp
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

    // Width animation: compact (54.dp) -> expanded (160.dp) on hard press / hold drag
    val railWidth by animateDpAsState(
        targetValue = if (isExpanded) 160.dp else 54.dp,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 450f),
        label = "railWidth"
    )

    val labelAlpha by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = tween(durationMillis = 160),
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
        Column(
            modifier = modifier
                .graphicsLayer { alpha = navBarAlpha }
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
                .padding(end = 12.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Tab Bar Floating Capsule
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = containerScale
                        scaleY = containerScale
                        clip = false
                    }
                    .height((slotHeight * itemCount) + (verticalPadding * 2))
                    .width(railWidth),
                contentAlignment = Alignment.TopStart
            ) {
                // Layer 1: Translucent Capsule Background & Border
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .cssShadow(color = Color.Black, alpha = 0.08f, blurRadius = 16.dp, offsetY = 4.dp)
                        .background(
                            color = colors.floatingBg.copy(alpha = 0.88f),
                            shape = RoundedCornerShape(27.dp)
                        )
                        .border(
                            width = 0.5.dp,
                            color = colors.floatingBorder.copy(alpha = if (isDark) 0.15f else 0.6f),
                            shape = RoundedCornerShape(27.dp)
                        )
                )

                // Layer 2: Draggable / Touch Area
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

                                            // Firm press detection: high touch pressure or firm hold (>180ms) expands labels
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
                    contentAlignment = Alignment.TopStart
                ) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                alpha = contentAlpha
                                clip = false
                            }
                            .fillMaxSize()
                    ) {
                        // Master Background Pill (Detached & Draggable along vertical Y axis)
                        Box(
                            modifier = Modifier
                                .offset { IntOffset(0, (animatedTop.value + overlapPy).roundToInt()) }
                                .height(indicatorHeight)
                                .fillMaxWidth()
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
                                    shape = RoundedCornerShape(22.dp)
                                )
                        )

                        // Foreground Tab Items (Vertical Column)
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            tabs.forEachIndexed { index, tab ->
                                LandscapePillTab(
                                    tab = tab,
                                    near = { pillNearnessVertical(animatedTop.value, indicatorHeightPx, slotHeightPx, index) },
                                    labelAlpha = labelAlpha,
                                    isExpanded = isExpanded,
                                    idleColor = colors.textSecondary,
                                    activeColor = colors.textPrimary,
                                    modifier = Modifier.height(slotHeight).fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            // Floating Add (+) Action Button (below the vertical tab capsule, like iPhone Duo toolbar)
            if (onAddClick != null) {
                val addInteractionSource = remember { MutableInteractionSource() }
                val isAddPressed by addInteractionSource.collectIsPressedAsState()
                val addScale by animateFloatAsState(
                    targetValue = if (isAddPressed) 0.92f else 1.0f,
                    animationSpec = tween(120, easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)),
                    label = "landscapeAddScale"
                )

                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .graphicsLayer {
                            scaleX = addScale
                            scaleY = addScale
                            clip = false
                        }
                        .cssShadow(color = Color.Black, alpha = 0.08f, blurRadius = 16.dp, offsetY = 4.dp)
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
                            interactionSource = addInteractionSource,
                            indication = ripple(bounded = true, radius = 27.dp),
                            onClick = onAddClick
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.Add,
                        contentDescription = K.add.tr(),
                        tint = colors.textPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
