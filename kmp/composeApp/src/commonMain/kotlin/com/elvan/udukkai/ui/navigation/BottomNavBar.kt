package com.elvan.udukkai.ui.navigation

import com.elvan.udukkai.ui.components.shell.frostedGlass
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.platform.navigationBarsPaddingIfMobile
import com.elvan.udukkai.core.extensions.cssShadow
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import kotlin.math.abs
import kotlin.math.floor

/** iOS-style pill motion, shared by the tab bars and the pill shifters. */
object PillMotion {
    /** On press the pill grows like a lens, with a little bounce. */
    val press: AnimationSpec<Float> = spring(dampingRatio = 0.55f, stiffness = 600f)
    /** While dragging it trails the finger by a hair, which is what lets it stretch. */
    val follow: AnimationSpec<Float> = spring(dampingRatio = 0.9f, stiffness = 1400f)
    /** On release or tap it glides over and settles with a slight overshoot. */
    val settle: AnimationSpec<Float> = spring(dampingRatio = 0.72f, stiffness = 380f)

    /** How much the pill stretches along its path (0 to 0.6): more the further it still has to travel. */
    fun stretch(targetLeftPx: Float, currentLeftPx: Float, pillWidthPx: Float): Float =
        (abs(targetLeftPx - currentLeftPx) / pillWidthPx).coerceAtMost(0.6f)

    /**
     * Waits until the pill has come to rest at [landingPx], so the page behind it is swapped only after the glide:
     * building a full page takes a heavy frame, and one landing mid-glide makes the pill skip. Gives up after
     * [timeoutMs] so a tap is never lost.
     */
    suspend fun awaitLanded(
        position: State<Float>,
        landingPx: Float,
        nearPx: Float = 2f,
        restPx: Float = 0.5f,
        timeoutMs: Long = 700
    ) {
        withTimeoutOrNull(timeoutMs) {
            var previous = Float.NaN
            snapshotFlow { position.value }.first { now ->
                val resting = previous.isNaN() || abs(now - previous) < restPx
                previous = now
                abs(now - landingPx) < nearPx && resting
            }
        }
    }
}

/** How far a pill whose left edge is at [pillLeftPx] sits over slot [index]: 1 when centred on it, 0 one slot away. */
fun pillNearness(pillLeftPx: Float, pillWidthPx: Float, slotWidthPx: Float, index: Int): Float =
    (1f - abs(pillLeftPx + pillWidthPx / 2f - (index + 0.5f) * slotWidthPx) / slotWidthPx).coerceIn(0f, 1f)

/**
 * One tab of a pill bar: the idle look (outline icon, regular grey label) and the active look (filled icon, bold label)
 * lie on top of each other and trade places as [near] goes from 0 to 1. [near] is read only while drawing, so a moving
 * pill redraws the tabs without rebuilding them each frame.
 */
@Composable
fun PillTab(
    icon: ImageVector,
    activeIcon: ImageVector,
    label: String,
    near: () -> Float,
    idleColor: Color,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    // The idle layer fades a little later than the active one comes in, so the tab never dims halfway
    val idle = Modifier.graphicsLayer { val n = near(); alpha = 1f - n * n }
    val active = Modifier.graphicsLayer { alpha = near() }
    Column(
        modifier = modifier.semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.size(22.dp)) {
            Icon(icon, null, tint = idleColor, modifier = Modifier.matchParentSize().then(idle))
            Icon(activeIcon, null, tint = activeColor, modifier = Modifier.matchParentSize().then(active))
        }
        Spacer(modifier = Modifier.height(1.5.dp))
        Box(contentAlignment = Alignment.Center) {
            val ff = LocalAppFontFamily.current
            Text(label, color = idleColor, fontSize = 9.5.sp, fontWeight = FontWeight.Normal, fontFamily = ff, maxLines = 1, lineHeight = 11.sp, modifier = idle)
            Text(label, color = activeColor, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold, fontFamily = ff, maxLines = 1, lineHeight = 11.sp, modifier = active.clearAndSetSemantics { })
        }
    }
}

/**
 * Udukkai Floating Draggable Bottom Navigation Bar
 * Ported directly from Neram's ElvanKizhPattaiBase
 */
@Composable
fun BottomNavBar(
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
    val pageDispatch = remember(coroutineScope) { com.elvan.udukkai.ui.navigation.PillPageDispatch(coroutineScope) }
    DisposableEffect(pageDispatch) { onDispose { pageDispatch.cancel() } }
    val colors = rememberShellColors()
    val isDark = colors.isDark

    val itemCount = tabs.size
    val layoutWidth = if (onAddClick != null) 58.dp else (if (itemCount <= 4) 65.dp else 59.dp)
    val bgWidth = if (onAddClick != null) 66.dp else (if (itemCount <= 4) 73.dp else 67.dp)
    val verticalPadding = 4.dp
    val overlap = (bgWidth - layoutWidth) / 2
    val horizontalPadding = verticalPadding + overlap
    val totalWidth = (layoutWidth * itemCount) + (horizontalPadding * 2)

    var isInteracting by remember { mutableStateOf(false) }
    var dragOffsetPx by remember { mutableStateOf<Float?>(null) }
    var touchOffsetFromCenterPx by remember { mutableStateOf(0f) }
    var hoverIndex by remember { mutableStateOf<Int?>(null) }
    var localLockedIndex by remember { mutableStateOf<Int?>(null) }

    val density = LocalDensity.current
    val layoutWidthPx = with(density) { layoutWidth.toPx() }
    val bgWidthPx = with(density) { bgWidth.toPx() }

    val currentOnTabSelected by rememberUpdatedState(onTabSelected)
    val currentOnInteraction by rememberUpdatedState(onInteraction)
    val currentOnDragProgress by rememberUpdatedState(onDragProgress)

    // The visual selection changes immediately; only the latest destination is composed after the pill settles.

    val actualIndex = tabs.indexOf(selectedTab).coerceAtLeast(0)
    LaunchedEffect(actualIndex) {
        // Acknowledge only this destination; a newer gesture owns its own visual target.
        // Retarget the existing spring without a one-frame snap, including external navigation.
        if (localLockedIndex == actualIndex) localLockedIndex = null
    }

    val activeVisualIndex = if (isInteracting && hoverIndex != null) {
        hoverIndex!!
    } else {
        localLockedIndex ?: actualIndex
    }

    val containerScale by animateFloatAsState(
        targetValue = if (isInteracting) 1.02f else 1.0f,
        animationSpec = PillMotion.press,
        label = "containerScale"
    )

    val pillScale by animateFloatAsState(
        targetValue = if (isInteracting) 1.30f else 1.0f,
        animationSpec = PillMotion.press,
        label = "pillScale"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (hideContent) 0f else 1f,
        animationSpec = tween(150),
        label = "contentAlpha"
    )

    val overlapPx = (bgWidthPx - layoutWidthPx) / 2f
    val maxLeftPx = ((itemCount - 1) * layoutWidthPx) - overlapPx
    val minLeftPx = -overlapPx

    val targetLeftPx = if (isInteracting && dragOffsetPx != null) {
        (dragOffsetPx!! - (bgWidthPx / 2f)).coerceIn(minLeftPx, maxLeftPx)
    } else {
        ((activeVisualIndex * layoutWidthPx) - overlapPx).coerceIn(minLeftPx, maxLeftPx)
    }

    val animatedLeft = animateFloatAsState(
        targetValue = targetLeftPx,
        animationSpec = when {
            isInteracting && dragOffsetPx != null -> PillMotion.follow
            else -> PillMotion.settle
        },
        label = "pillX"
    )

    val navBarAlpha by animateFloatAsState(
        targetValue = if (hideContent) 0f else 1f,
        animationSpec = tween(260, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)),
        label = "navBarAlpha"
    )

    if (navBarAlpha > 0f) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .navigationBarsPaddingIfMobile()
                .padding(bottom = Dimens.FloatingBarBottomGap)
                .graphicsLayer {
                    alpha = navBarAlpha
                    scaleX = .94f + .06f * navBarAlpha
                    scaleY = .94f + .06f * navBarAlpha
                },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = containerScale
                        scaleY = containerScale
                        clip = false
                    }
                    .height(56.dp)
                    .width(totalWidth),
                contentAlignment = Alignment.Center
            ) {
        // Layer 1: Background Capsule & Border
        Box(
            modifier = Modifier
                .matchParentSize()
                .cssShadow(color = Color.Black, alpha = 0.05f, blurRadius = 16.dp, offsetY = 4.dp)
                .frostedGlass(CircleShape, colors, fallbackAlpha = 0.88f)
                .border(
                    width = 0.5.dp,
                    color = colors.floatingBorder.copy(alpha = if (isDark) 0.15f else 0.6f),
                    shape = CircleShape
                )
        )

        // Layer 2: Foreground Content with Gesture Drag
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding, vertical = verticalPadding)
                .then(
                    if (!hideContent) {
                        Modifier.pointerInput(hideContent) {
                            trackPillTabs(
                                vertical = false,
                                onStart = { position ->
                                    pageDispatch.cancel()
                                    isInteracting = true
                                    currentOnInteraction(true)
                                    hoverIndex = floor(position / layoutWidthPx).toInt().coerceIn(0, itemCount - 1)
                                    touchOffsetFromCenterPx = position - (hoverIndex!! * layoutWidthPx + layoutWidthPx / 2f)
                                    dragOffsetPx = null
                                },
                                onDrag = { position ->
                                    val targetCenter = position - touchOffsetFromCenterPx
                                    dragOffsetPx = targetCenter
                                    hoverIndex = floor(targetCenter / layoutWidthPx).toInt().coerceIn(0, itemCount - 1)
                                    currentOnDragProgress(targetCenter / layoutWidthPx)
                                },
                                onEnd = { isDrag ->
                                    val finalIndex = hoverIndex
                                    if (finalIndex != null) localLockedIndex = finalIndex
                                    isInteracting = false
                                    currentOnInteraction(false)
                                    dragOffsetPx = null
                                    hoverIndex = null
                                    if (finalIndex != null) pageDispatch.select(animatedLeft, ((finalIndex * layoutWidthPx) - overlapPx).coerceIn(minLeftPx, maxLeftPx)) { currentOnTabSelected(tabs[finalIndex], isDrag) }
                                },
                                onCancel = {
                                    isInteracting = false
                                    currentOnInteraction(false)
                                    dragOffsetPx = null
                                    hoverIndex = null
                                }
                            )
                        }
                    } else Modifier
        ),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        alpha = contentAlpha
                        clip = false
                    }
                    .width(layoutWidth * itemCount)
                    .fillMaxHeight()
            ) {
                // Master Background Pill (Detached & Draggable)
                Box(
                    modifier = Modifier
                        .graphicsLayer { translationX = animatedLeft.value }
                        .fillMaxHeight()
                        .width(bgWidth)
                        .graphicsLayer {
                            val stretch = PillMotion.stretch(targetLeftPx, animatedLeft.value, bgWidthPx)
                            scaleX = pillScale * (1f + stretch * 0.35f)
                            scaleY = pillScale * (1f - stretch * 0.12f)
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

                // Foreground Content (Icons & Labels)
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEachIndexed { index, tab ->
                        PillTab(
                            icon = tab.icon,
                            activeIcon = tab.activeIcon,
                            label = tab.getLocalizedLabel(),
                            near = { pillNearness(animatedLeft.value, bgWidthPx, layoutWidthPx, index) },
                            idleColor = colors.textSecondary,
                            activeColor = colors.textPrimary,
                            modifier = Modifier.fillMaxHeight().width(layoutWidth)
                        )
                    }
                }
            }
            }
        }

        if (onAddClick != null) {
            Spacer(modifier = Modifier.width(10.dp))

            val addInteractionSource = remember { MutableInteractionSource() }
            val isAddPressed by addInteractionSource.collectIsPressedAsState()
            val addScale by animateFloatAsState(
                targetValue = if (isAddPressed) 0.92f else 1.0f,
                animationSpec = tween(120, easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)),
                label = "addScale"
            )

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .graphicsLayer {
                        scaleX = addScale
                        scaleY = addScale
                        clip = false
                    }
                    .cssShadow(color = Color.Black, alpha = 0.05f, blurRadius = 16.dp, offsetY = 4.dp)
                    .frostedGlass(CircleShape, colors, fallbackAlpha = 0.88f)
                    .border(
                        width = 0.5.dp,
                        color = colors.floatingBorder.copy(alpha = if (isDark) 0.15f else 0.6f),
                        shape = CircleShape
                    )
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = addInteractionSource,
                        indication = ripple(bounded = true, radius = 28.dp),
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
