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
import com.elvan.udukkai.ui.components.shell.frostedGlass
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
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
            modifier = Modifier.size(20.dp),
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
            Spacer(modifier = Modifier.height(1.dp))
            Box(
                modifier = Modifier.graphicsLayer { alpha = labelAlpha },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab.getLocalizedLabel(),
                    color = idleColor,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = ff,
                    maxLines = 1,
                    lineHeight = 10.sp,
                    modifier = idle
                )
                Text(
                    text = tab.getLocalizedLabel(),
                    color = activeColor,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = ff,
                    maxLines = 1,
                    lineHeight = 10.sp,
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
    modifier: Modifier = Modifier,
    alignStart: Boolean = false
) {
    val isDark = colors.isDark
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = tween(120, easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)),
        label = "landscapeTopBtnScale"
    )
    // With the keyboard up the first tap only puts the keyboard away; the page stays
    val keyboard = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    val focus = androidx.compose.ui.platform.LocalFocusManager.current
    val imeOpen = com.elvan.udukkai.core.platform.getImeBottomPadding() > 0.dp

    Box(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top))
            .padding(start = if (alignStart) 12.dp else 0.dp, end = if (alignStart) 0.dp else 12.dp, top = 8.dp)
            .size(60.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                clip = false
            }
            .cssShadow(color = Color.Black, alpha = 0.05f, blurRadius = 16.dp, offsetY = 4.dp)
            .background(colors.floatingBg.copy(alpha = 0.88f), CircleShape)
            .border(
                width = 0.5.dp,
                color = colors.floatingBorder.copy(alpha = if (isDark) 0.15f else 0.6f),
                shape = CircleShape
            )
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, radius = 30.dp),
                onClick = { if (imeOpen) { keyboard?.hide(); focus.clearFocus() } else onClick() }
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
 * The landscape tab rail: the portrait [BottomNavBar] turned on its side. Same capsule, same sliding pill (it grows when
 * touched and stretches as it glides), same drag and tap behaviour and the same tabs ([PillTab]); only the axis differs.
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
    val items = NavTab.entries.map { SideRailItem(it.icon, it.activeIcon, it.getLocalizedLabel()) }
    SideTabRail(
        items = items,
        selectedIndex = NavTab.entries.indexOf(selectedTab),
        onSelected = { i, drag -> onTabSelected(NavTab.entries[i], drag) },
        onInteraction = onInteraction,
        onDragProgress = onDragProgress,
        hideContent = hideContent,
        modifier = modifier
    )
}

/** One tab of a [SideTabRail]. */
class SideRailItem(val icon: ImageVector, val activeIcon: ImageVector, val label: String)

/** The rail for any set of tabs: the main tabs, the recycle bin's, and so on. */
@Composable
fun SideTabRail(
    items: List<SideRailItem>,
    selectedIndex: Int,
    onSelected: (Int, Boolean) -> Unit,
    onInteraction: (Boolean) -> Unit = {},
    onDragProgress: (Float) -> Unit = {},
    hideContent: Boolean = false,
    modifier: Modifier = Modifier
) {
    val tabs = items
    val coroutineScope = rememberCoroutineScope()
    val colors = rememberShellColors()
    val isDark = colors.isDark

    val itemCount = tabs.size
    val layoutHeight = 62.dp
    val bgHeight = 70.dp
    val horizontalPadding = 4.dp
    val overlap = (bgHeight - layoutHeight) / 2
    val verticalPadding = horizontalPadding + overlap
    val totalHeight = (layoutHeight * itemCount) + (verticalPadding * 2)

    var isInteracting by remember { mutableStateOf(false) }
    var dragOffsetPx by remember { mutableStateOf<Float?>(null) }
    var touchOffsetFromCenterPx by remember { mutableStateOf(0f) }
    var hoverIndex by remember { mutableStateOf<Int?>(null) }
    var localLockedIndex by remember { mutableStateOf<Int?>(null) }
    var snapNextFrame by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val layoutHeightPx = with(density) { layoutHeight.toPx() }
    val bgHeightPx = with(density) { bgHeight.toPx() }

    val currentOnTabSelected by rememberUpdatedState(onSelected)
    val currentOnInteraction by rememberUpdatedState(onInteraction)
    val currentOnDragProgress by rememberUpdatedState(onDragProgress)

    var pendingSelect by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    val actualIndex = selectedIndex.coerceIn(0, items.lastIndex)
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

    val activeVisualIndex = if (isInteracting && hoverIndex != null) hoverIndex!! else localLockedIndex ?: actualIndex

    val containerScale by animateFloatAsState(
        targetValue = if (isInteracting) 1.02f else 1.0f,
        animationSpec = PillMotion.press,
        label = "railContainerScale"
    )
    val pillScale by animateFloatAsState(
        targetValue = if (isInteracting) 1.30f else 1.0f,
        animationSpec = PillMotion.press,
        label = "railPillScale"
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (hideContent) 0f else 1f,
        animationSpec = tween(150),
        label = "railContentAlpha"
    )

    val overlapPx = (bgHeightPx - layoutHeightPx) / 2f
    val maxTopPx = ((itemCount - 1) * layoutHeightPx) - overlapPx
    val minTopPx = -overlapPx

    val targetTopPx = if (isInteracting && dragOffsetPx != null) {
        (dragOffsetPx!! - (bgHeightPx / 2f)).coerceIn(minTopPx, maxTopPx)
    } else {
        ((activeVisualIndex * layoutHeightPx) - overlapPx).coerceIn(minTopPx, maxTopPx)
    }

    val animatedTop = animateFloatAsState(
        targetValue = targetTopPx,
        animationSpec = when {
            snapNextFrame -> snap()
            isInteracting && dragOffsetPx != null -> PillMotion.follow
            else -> PillMotion.settle
        },
        label = "railPillY"
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
                // A camera cut-out on this side keeps the rail clear of it; with none it sits at the edge
                .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Start))
                .padding(start = 9.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = containerScale
                        scaleY = containerScale
                        clip = false
                    }
                    .width(66.dp)
                    .height(totalHeight),
                contentAlignment = Alignment.Center
            ) {
                // Layer 1: capsule and border
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .cssShadow(color = Color.Black, alpha = 0.05f, blurRadius = 16.dp, offsetY = 4.dp)
                        .background(colors.floatingBg.copy(alpha = 0.88f), CircleShape)
                        .border(
                            width = 0.5.dp,
                            color = colors.floatingBorder.copy(alpha = if (isDark) 0.15f else 0.6f),
                            shape = CircleShape
                        )
                )

                // Layer 2: content with the drag gesture
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
                                        hoverIndex = floor(initialY / layoutHeightPx).toInt().coerceIn(0, itemCount - 1)
                                        val slotCenter = (hoverIndex!! * layoutHeightPx) + (layoutHeightPx / 2f)
                                        touchOffsetFromCenterPx = initialY - slotCenter
                                        dragOffsetPx = null

                                        var isDrag = false
                                        val pointerId = down.id

                                        while (true) {
                                            val event = awaitPointerEvent()
                                            val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                                            if (!change.pressed) break
                                            val currentPos = change.position
                                            if (abs(currentPos.y - down.position.y) > 4f) {
                                                isDrag = true
                                                val targetCenter = currentPos.y - touchOffsetFromCenterPx
                                                dragOffsetPx = targetCenter
                                                hoverIndex = floor(targetCenter / layoutHeightPx).toInt().coerceIn(0, itemCount - 1)
                                                currentOnDragProgress(targetCenter / layoutHeightPx)
                                                change.consume()
                                            }
                                        }

                                        val finalIndex = hoverIndex
                                        if (finalIndex != null) localLockedIndex = finalIndex
                                        isInteracting = false
                                        currentOnInteraction(false)
                                        dragOffsetPx = null
                                        hoverIndex = null

                                        if (finalIndex != null) {
                                            pendingSelect?.cancel()
                                            pendingSelect = coroutineScope.launch {
                                                PillMotion.awaitLanded(animatedTop, ((finalIndex * layoutHeightPx) - overlapPx).coerceIn(minTopPx, maxTopPx))
                                                currentOnTabSelected(finalIndex, isDrag)
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
                            .height(layoutHeight * itemCount)
                            .fillMaxWidth()
                    ) {
                        // The sliding pill, detached and draggable
                        Box(
                            modifier = Modifier
                                .offset { IntOffset(0, animatedTop.value.roundToInt()) }
                                .fillMaxWidth()
                                .height(bgHeight)
                                .graphicsLayer {
                                    val stretch = PillMotion.stretch(targetTopPx, animatedTop.value, bgHeightPx)
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
                                    shape = CircleShape
                                )
                        )

                        Column(modifier = Modifier.fillMaxSize()) {
                            tabs.forEachIndexed { index, tab ->
                                PillTab(
                                    icon = tab.icon,
                                    activeIcon = tab.activeIcon,
                                    label = tab.label,
                                    near = { pillNearnessVertical(animatedTop.value, bgHeightPx, layoutHeightPx, index) },
                                    idleColor = colors.textSecondary,
                                    activeColor = colors.textPrimary,
                                    modifier = Modifier.fillMaxWidth().height(layoutHeight)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
