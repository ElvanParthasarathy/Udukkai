package com.elvan.udukkai.ui.screens.recyclebin

import com.elvan.udukkai.ui.navigation.trackPillTabs
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.extensions.cssShadow
import com.elvan.udukkai.core.platform.navigationBarsPaddingIfMobile
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.elvan.udukkai.ui.navigation.PillMotion
import com.elvan.udukkai.ui.navigation.PillTab
import com.elvan.udukkai.ui.navigation.pillNearness
import kotlin.math.floor



enum class RecycleBinTab(
    val icon: ImageVector,
    val activeIcon: ImageVector,
    val titleKey: String,
    val headerTitleKey: String = titleKey
) {
    Create(
        icon = MaterialSymbols.CustomNav.Create,
        activeIcon = MaterialSymbols.CustomNav.CreateFill,
        titleKey = K.make,
        headerTitleKey = K.createBtn
    ),
    Items(
        icon = MaterialSymbols.CustomNav.Items,
        activeIcon = MaterialSymbols.CustomNav.ItemsFill,
        titleKey = K.item,
        headerTitleKey = K.items
    ),
    Customers(
        icon = MaterialSymbols.CustomNav.Customers,
        activeIcon = MaterialSymbols.CustomNav.CustomersFill,
        titleKey = K.customer,
        headerTitleKey = K.customers
    );

    @Composable
    fun getLocalizedLabel(): String = titleKey.tr()

    @Composable
    fun getLocalizedHeader(): String = headerTitleKey.tr()
}

/**
 * RecycleBinBottomNavBar — Floating Draggable Bottom Navigation Bar for Recycle Bin.
 * Contains 3 tabs (Aakku, Items, Vaangunar), no home tab, and no '+' button.
 */
@Composable
fun RecycleBinBottomNavBar(
    selectedTab: RecycleBinTab,
    onTabSelected: (RecycleBinTab) -> Unit,
    hideContent: Boolean = false,
    modifier: Modifier = Modifier
) {
    val tabs = RecycleBinTab.entries
    val coroutineScope = rememberCoroutineScope()
    val pageDispatch = remember(coroutineScope) { com.elvan.udukkai.ui.navigation.PillPageDispatch(coroutineScope) }
    DisposableEffect(pageDispatch) { onDispose { pageDispatch.cancel() } }
    val colors = rememberShellColors()
    val isDark = colors.isDark

    val itemCount = tabs.size
    val layoutWidth = 65.dp
    val bgWidth = 73.dp
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

    // As in the main bar: a newer tap cancels an older one still waiting, and our own tap lets the pill finish its glide

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
        animationSpec = tween(200, easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)),
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
                        .background(
                            color = colors.floatingBg.copy(alpha = 0.88f),
                            shape = CircleShape
                        )
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
                                    hoverIndex = floor(position / layoutWidthPx).toInt().coerceIn(0, itemCount - 1)
                                    touchOffsetFromCenterPx = position - (hoverIndex!! * layoutWidthPx + layoutWidthPx / 2f)
                                    dragOffsetPx = null
                                },
                                onDrag = { position ->
                                    val targetCenter = position - touchOffsetFromCenterPx
                                    dragOffsetPx = targetCenter
                                    hoverIndex = floor(targetCenter / layoutWidthPx).toInt().coerceIn(0, itemCount - 1)
                                },
                                onEnd = { isDrag ->
                                    val finalIndex = hoverIndex
                                    if (finalIndex != null) localLockedIndex = finalIndex
                                    isInteracting = false
                                    dragOffsetPx = null
                                    hoverIndex = null
                                    if (finalIndex != null) pageDispatch.select(animatedLeft, ((finalIndex * layoutWidthPx) - overlapPx).coerceIn(minLeftPx, maxLeftPx)) { currentOnTabSelected(tabs[finalIndex]) }
                                },
                                onCancel = {
                                    isInteracting = false
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
        }
    }
}


/** The recycle bin's tabs: the bottom pill upright, the side rail when the screen is wider than tall. */
@Composable
fun RecycleBinTabs(
    selectedTab: RecycleBinTab,
    onTabSelected: (RecycleBinTab) -> Unit,
    hideContent: Boolean = false
) {
    if (com.elvan.udukkai.ui.components.shell.LocalElvanSideBar.current) {
        val items = RecycleBinTab.entries.map { com.elvan.udukkai.ui.navigation.SideRailItem(it.icon, it.activeIcon, it.getLocalizedLabel()) }
        com.elvan.udukkai.ui.navigation.SideTabRail(
            items = items,
            selectedIndex = selectedTab.ordinal,
            onSelected = { i, _ -> onTabSelected(RecycleBinTab.entries[i]) },
            hideContent = hideContent,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        RecycleBinBottomNavBar(selectedTab = selectedTab, onTabSelected = onTabSelected, hideContent = hideContent)
    }
}
