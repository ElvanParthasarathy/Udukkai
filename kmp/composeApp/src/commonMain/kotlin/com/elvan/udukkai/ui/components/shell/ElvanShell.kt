package com.elvan.udukkai.ui.components.shell

import dev.chrisbanes.haze.hazeSource
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.LocalAppLanguage
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/** The home screen draws its own trailing-edge landscape controls and gutter; a page inside it must not add a second set. */
val LocalElvanLandscapeChromeHandled = compositionLocalOf { false }
/** True inside one pane of a two-pane page: the pane lays out like a narrow page, never like a landscape screen. */
val LocalElvanPaneChrome = compositionLocalOf { false }
/** The side back button of a two-pane page does the going back: the panes draw no chevron of their own. */
val LocalElvanPaneNoBack = compositionLocalOf { false }
val LocalElvanScrollState = compositionLocalOf<LazyListState?> { null }
val LocalElvanTopSpacerHeight = compositionLocalOf<Dp> { 280.dp - Dimens.SectionSpacing }
object GlobalHeaderState {
    var isExpanded: Boolean = true
}

val LocalGlobalHeaderExpanded = compositionLocalOf<MutableState<Boolean>> { mutableStateOf(GlobalHeaderState.isExpanded) }

class ElvanShellController(
    val toggleHeader: () -> Unit = {},
    val expandHeader: () -> Unit = {},
    val collapseHeader: () -> Unit = {}
)
val LocalElvanShellController = compositionLocalOf { ElvanShellController() }

@Composable
fun ElvanShell(
    scrollState: LazyListState,
    title: String = "",
    useNewDesign: Boolean = true,
    showNavbar: Boolean = true,
    onBack: (() -> Unit)? = null,
    leadingIcon: ImageVector? = null,
    leadingActions: (@Composable RowScope.() -> Unit)? = null,
    hasActions: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
    navbar: @Composable () -> Unit = {},
    isSearchActive: Boolean = false,
    isSelectionMode: Boolean = false,
    selectedCount: Int = 0,
    isAllSelected: Boolean = false,
    onSelectAll: () -> Unit = {},
    onCancelSelection: () -> Unit = {},
    showBottomFade: Boolean = true,
    hasFloatingBar: Boolean = false,
    /** Sideways the page's own title is left out (the tab pages, the view pages). */
    hideTitleSideways: Boolean = false,
    banners: List<ShellBanner> = emptyList(),
    overlay: @Composable () -> Unit = {},
    floatingBar: @Composable () -> Unit = {},
    /** View pages: the header stays small (title beside the back button), it never grows into the big heading. */
    collapsedOnly: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = rememberShellColors()
    // Whether the landscape action toolbar is really showing (an editor with only a Save has none)
    var toolbarPresent by remember { mutableStateOf(false) }
    var isNavbarVisible by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    val selectionTitle = "$selectedCount ${K.selected.tr()}"
    val effectiveTitle = if (isSelectionMode) selectionTitle else title

    val isDesktop = com.elvan.udukkai.core.platform.currentPlatform == com.elvan.udukkai.core.platform.PlatformType.DESKTOP
    val statusBarHeight = com.elvan.udukkai.core.platform.getStatusBarTopPadding()
    val density = LocalDensity.current

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // 
        // Phone/tablet landscape keeps the floating controls; desktop has a sidebar and a plain fixed bar
        val isWide = maxWidth > maxHeight
        val paneChrome = LocalElvanPaneChrome.current
        val isLandscape = isWide && !isDesktop && !paneChrome
        val isCompactHeader = isWide || isDesktop || !useNewDesign || collapsedOnly

        val pillHeight = 50.dp
        val ceiling = if (isDesktop) 16.dp else if (isWide || LocalElvanPaneChrome.current) statusBarHeight + 8.dp else statusBarHeight + 20.dp

        val expandedHeight = if (isCompactHeader) ceiling + pillHeight else 280.dp

        val collisionOffsetDp = expandedHeight - (ceiling + pillHeight)
        val collisionOffsetPx = with(density) { collisionOffsetDp.toPx() }

        val handoffShrinkOffsetDp = if (isCompactHeader) 0.dp else (196.dp - statusBarHeight)
        val handoffShrinkOffsetPx = with(density) { handoffShrinkOffsetDp.toPx() }

        val globalHeaderExpanded = LocalGlobalHeaderExpanded.current

        var headerCollapsePx by remember(scrollState, isCompactHeader) {
            mutableFloatStateOf(if (!isCompactHeader && globalHeaderExpanded.value) 0f else handoffShrinkOffsetPx)
        }

    LaunchedEffect(headerCollapsePx, handoffShrinkOffsetPx) {
        if (collapsedOnly) return@LaunchedEffect
        if (headerCollapsePx >= handoffShrinkOffsetPx && globalHeaderExpanded.value) {
            globalHeaderExpanded.value = false
            GlobalHeaderState.isExpanded = false
        } else if (headerCollapsePx == 0f && !globalHeaderExpanded.value) {
            globalHeaderExpanded.value = true
            GlobalHeaderState.isExpanded = true
        }
    }

    LaunchedEffect(globalHeaderExpanded.value) {
        if (collapsedOnly) return@LaunchedEffect
        GlobalHeaderState.isExpanded = globalHeaderExpanded.value
        if (!isCompactHeader) {
            if (globalHeaderExpanded.value && headerCollapsePx > 0f) {
                // Only snap the header visual — do NOT scroll the list to top.
                // Scroll position is independently managed by the user's scroll gesture;
                // forcing scrollToItem(0,0) here caused the scroll-reset bug when returning
                // from a subpage Activity whose header was in expanded state.
                headerCollapsePx = 0f
            } else if (!globalHeaderExpanded.value && headerCollapsePx == 0f) {
                headerCollapsePx = handoffShrinkOffsetPx
            }
        }
    }

    // When a new Activity opens, window insets resolve on frame 2, causing handoffShrinkOffsetPx
    // to change. Snap headerCollapsePx to the updated value immediately and unconditionally
    // so there is zero residual scroll offset visible on subpage entry.
    LaunchedEffect(handoffShrinkOffsetPx) {
        if (!isCompactHeader && !globalHeaderExpanded.value) {
            headerCollapsePx = handoffShrinkOffsetPx
        }
    }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            if (scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 0) {
                scrollState.scrollToItem(0, 0)
            }
            if (headerCollapsePx < handoffShrinkOffsetPx) {
                androidx.compose.animation.core.animate(
                    initialValue = headerCollapsePx,
                    targetValue = handoffShrinkOffsetPx,
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
                    )
                ) { value, _ -> headerCollapsePx = value }
                globalHeaderExpanded.value = false
            }
        }
    }

    val rawScrollOffset = if (scrollState.firstVisibleItemIndex == 0) {
        scrollState.firstVisibleItemScrollOffset.toFloat()
    } else {
        with(density) { expandedHeight.toPx() }
    }
    // Never past the handoff point: when the insets arrive the point moves up, and the saved value only catches up a frame
    // later (LaunchedEffect above). Unclamped, that frame reads as "scrolled under the bar": the title vanishes and the circle pops in.
    val collapsePx = headerCollapsePx.coerceAtMost(handoffShrinkOffsetPx)
    val currentScrollOffset = (collapsePx + rawScrollOffset).coerceAtMost(with(density) { expandedHeight.toPx() })

    val topSpacerHeight = if (isCompactHeader) {
        // Sideways the title row and the round button are one 56dp band; the page starts just under it
        if (isWide && !isDesktop && !LocalElvanPaneChrome.current) statusBarHeight + 58.dp else ceiling + pillHeight + 10.dp
    } else {
        (expandedHeight - Dimens.SectionSpacing - with(density) { collapsePx.toDp() }).coerceAtLeast(0.dp)
    }

    var isHeaderExpanded by remember(scrollState) {
        mutableStateOf(
            scrollState.firstVisibleItemIndex == 0 &&
            currentScrollOffset < handoffShrinkOffsetPx
        )
    }

    LaunchedEffect(scrollState, handoffShrinkOffsetPx, headerCollapsePx) {
        snapshotFlow {
            scrollState.firstVisibleItemIndex > 0 || currentScrollOffset >= handoffShrinkOffsetPx
        }.distinctUntilChanged().collect { isPastBoundary ->
            if (isPastBoundary && isHeaderExpanded) {
                isHeaderExpanded = false
            }
        }
    }

    var isFlinging by remember { mutableStateOf(false) }

    val nestedScrollConnection = remember(scrollState, handoffShrinkOffsetPx, collisionOffsetPx, density) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val isItem0 = scrollState.firstVisibleItemIndex == 0
                val isListAtTop = isItem0 && scrollState.firstVisibleItemScrollOffset == 0

                // 1. Dragging UP: Header collapses FIRST before list scrolls
                if (delta < 0f && headerCollapsePx < handoffShrinkOffsetPx) {
                    val newCollapse = (headerCollapsePx - delta).coerceIn(0f, handoffShrinkOffsetPx)
                    val consumedY = -(newCollapse - headerCollapsePx)
                    headerCollapsePx = newCollapse
                    return Offset(0f, consumedY)
                }

                // 2. Dragging DOWN: Expand header when list is at top
                if (delta > 0f && isListAtTop && headerCollapsePx > 0f) {
                    if (source == NestedScrollSource.UserInput) {
                        isHeaderExpanded = true
                        val newCollapse = (headerCollapsePx - delta).coerceIn(0f, handoffShrinkOffsetPx)
                        val consumedY = -(newCollapse - headerCollapsePx)
                        headerCollapsePx = newCollapse
                        return Offset(0f, consumedY)
                    } else {
                        return Offset(0f, delta)
                    }
                }

                // 3. Brick Wall Brake
                if (delta > 0f && source != NestedScrollSource.UserInput) {
                    if (isListAtTop && headerCollapsePx >= handoffShrinkOffsetPx) {
                        return Offset(0f, delta)
                    }
                }

                // 4. Navbar hide/show logic matching Flutter One UI Physics
                val reachedPill = if (isCompactHeader) {
                    scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 8
                } else {
                    !isItem0 || currentScrollOffset >= (handoffShrinkOffsetPx.coerceAtMost(collisionOffsetPx) - with(density) { 4.dp.toPx() })
                }
                if (delta > 2f && !isNavbarVisible) {
                    // Scrolling UP (finger moving down) -> Show immediately!
                    isNavbarVisible = true
                } else if (delta < -2f && isNavbarVisible && reachedPill) {
                    // Scrolling DOWN past header (finger moving up) -> Hide!
                    isNavbarVisible = false
                }
                return Offset.Zero
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                val isItem0 = scrollState.firstVisibleItemIndex == 0
                val isListAtTop = isItem0 && scrollState.firstVisibleItemScrollOffset == 0
                if (available.y > 0f && isListAtTop && headerCollapsePx > 0f) {
                    if (source == NestedScrollSource.UserInput) {
                        val newCollapse = (headerCollapsePx - available.y).coerceIn(0f, handoffShrinkOffsetPx)
                        val consumedY = -(newCollapse - headerCollapsePx)
                        headerCollapsePx = newCollapse
                        return Offset(0f, consumedY)
                    } else {
                        return Offset(0f, available.y)
                    }
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                val isItem0 = scrollState.firstVisibleItemIndex == 0
                val isListAtTop = isItem0 && scrollState.firstVisibleItemScrollOffset == 0
                val isSubpage = onBack != null

                if (headerCollapsePx > 0f && headerCollapsePx < handoffShrinkOffsetPx && isListAtTop) {
                    val currentProgress = headerCollapsePx / handoffShrinkOffsetPx
                    val target = when {
                        isSubpage && available.y < -100f -> handoffShrinkOffsetPx
                        available.y < -500f -> handoffShrinkOffsetPx
                        available.y > 500f -> 0f
                        isSubpage && currentProgress >= 0.2f -> handoffShrinkOffsetPx
                        currentProgress >= 0.4f -> handoffShrinkOffsetPx
                        else -> 0f
                    }
                    androidx.compose.animation.core.animate(
                        initialValue = headerCollapsePx,
                        targetValue = target,
                        animationSpec = tween(
                            durationMillis = 260,
                            easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
                        )
                    ) { value, _ -> headerCollapsePx = value }
                    return available
                }

                if (available.y < -300f) {
                    val reachedPill = if (isCompactHeader) {
                        scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 8
                    } else {
                        !isItem0 || currentScrollOffset >= (handoffShrinkOffsetPx.coerceAtMost(collisionOffsetPx) - with(density) { 4.dp.toPx() })
                    }
                    if (reachedPill && isNavbarVisible) {
                        isNavbarVisible = false
                    }
                }
                return Velocity.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                isFlinging = false
                if (!isNavbarVisible) isNavbarVisible = true
                return Velocity.Zero
            }
        }
    }

    val isTruePill = if (isCompactHeader) {
        scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 8
    } else {
        scrollState.firstVisibleItemIndex > 0 || currentScrollOffset >= (handoffShrinkOffsetPx.coerceAtMost(collisionOffsetPx) - with(density) { 4.dp.toPx() })
    }
    LaunchedEffect(isTruePill) {
        if (!isTruePill && !isNavbarVisible) {
            isNavbarVisible = true
        }
    }

    // Restore navbar when scrolling stops (ScrollEndNotification equivalent)
    LaunchedEffect(scrollState.isScrollInProgress) {
        if (!scrollState.isScrollInProgress && !isNavbarVisible) {
            isNavbarVisible = true
        }
    }

    // Always restore navbar when at the top of the list
    LaunchedEffect(scrollState.firstVisibleItemIndex, scrollState.firstVisibleItemScrollOffset) {
        if (scrollState.firstVisibleItemIndex == 0 && scrollState.firstVisibleItemScrollOffset == 0) {
            if (!isNavbarVisible) isNavbarVisible = true
        }
    }

    val navOpacity by animateFloatAsState(
        targetValue = if (isNavbarVisible) 1.0f else 0.0f,
        animationSpec = tween(
            durationMillis = 280,
            easing = if (isNavbarVisible)
                CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
            else
                CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
        ),
        label = "navOpacity"
    )

    val effectiveNavOpacity = if (isSearchActive) 1.0f else (if (isTruePill) navOpacity else 1.0f)

    val shellController = remember(scrollState, handoffShrinkOffsetPx, globalHeaderExpanded) {
        ElvanShellController(
            toggleHeader = {
                coroutineScope.launch {
                    if (scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 0) {
                        scrollState.animateScrollToItem(0, 0)
                    } else {
                        val willExpand = headerCollapsePx > handoffShrinkOffsetPx / 2f
                        val target = if (willExpand) 0f else handoffShrinkOffsetPx
                        androidx.compose.animation.core.animate(
                            initialValue = headerCollapsePx,
                            targetValue = target,
                            animationSpec = tween(
                                durationMillis = 280,
                                easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
                            )
                        ) { value, _ -> headerCollapsePx = value }
                        globalHeaderExpanded.value = willExpand
                    }
                }
            },
            expandHeader = {
                coroutineScope.launch {
                    if (scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 0) {
                        scrollState.scrollToItem(0, 0)
                    }
                    androidx.compose.animation.core.animate(
                        initialValue = headerCollapsePx,
                        targetValue = 0f,
                        animationSpec = tween(280, easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f))
                    ) { value, _ -> headerCollapsePx = value }
                    globalHeaderExpanded.value = true
                }
            },
            collapseHeader = {
                coroutineScope.launch {
                    androidx.compose.animation.core.animate(
                        initialValue = headerCollapsePx,
                        targetValue = handoffShrinkOffsetPx,
                        animationSpec = tween(280, easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f))
                    ) { value, _ -> headerCollapsePx = value }
                    globalHeaderExpanded.value = false
                }
            }
        )
    }

    CompositionLocalProvider(
        LocalElvanScrollState provides scrollState,
        LocalElvanTopSpacerHeight provides topSpacerHeight,
        LocalElvanShellController provides shellController,
        LocalElvanSideBar provides isLandscape
    ) {
        val shouldAttachNestedScroll = !isDesktop && !isWide
        val shellModifier = if (shouldAttachNestedScroll) {
            Modifier
                .fillMaxSize()
                .background(colors.background)
                .nestedScroll(nestedScrollConnection)
        } else {
            Modifier
                .fillMaxSize()
                .background(colors.background)
        }

        val hazeState = dev.chrisbanes.haze.rememberHazeState()
        val menuHost = remember { MenuHostState() }
    androidx.compose.runtime.CompositionLocalProvider(LocalHazeState provides hazeState, LocalMenuHost provides menuHost) {
        Box(
            modifier = shellModifier
        ) {
            // Layer 1: Content (100% Full screen, ZERO translation, ZERO bottom clipping)
            Box(
                modifier = Modifier
                    .hazeSource(hazeState)
                    .fillMaxSize()
                    // Landscape pages: keep content clear of the trailing-edge back button and action column
                    .then(if (isLandscape && ((!LocalElvanLandscapeChromeHandled.current && !showNavbar && onBack != null) || (toolbarPresent && !isSelectionMode))) Modifier.padding(end = 80.dp) else Modifier)
                    // The tab rail on the left edge (the recycle bin) takes this much room
                    .then(if (isLandscape && showNavbar && !LocalElvanLandscapeChromeHandled.current) Modifier.padding(start = 76.dp) else Modifier)
                    // The camera sits on this side: keep the content clear of it
                    .then(if (isLandscape && !LocalElvanLandscapeChromeHandled.current) Modifier.windowInsetsPadding(androidx.compose.foundation.layout.WindowInsets.safeDrawing.only(androidx.compose.foundation.layout.WindowInsetsSides.Start)).padding(start = 6.dp) else Modifier)
                    .then(if (!showNavbar) Modifier.imePadding() else Modifier)
            ) {
                content()
            }

            if (!isCompactHeader) {
                // Layer 2: Top Fade Mask (Solid at top, fading down)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(96.dp)
                        .zIndex(10f)
                        .background(
                            Brush.verticalGradient(
                                0.0f to colors.background,
                                0.35f to colors.background.copy(alpha = 0.55f),
                                0.7f to colors.background.copy(alpha = 0.16f),
                                1.0f to Color.Transparent
                            )
                        )
                )

                // Layer 2.5: The Expanded Header scaling down
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .zIndex(100f)
                ) {
                    ElvanExpandedBar(
                        title = effectiveTitle,
                        colors = colors,
                        scrollOffsetPx = currentScrollOffset,
                        collisionOffsetPx = collisionOffsetPx,
                        expandedHeight = expandedHeight,
                        hasLeadingWidget = onBack != null || isSelectionMode,
                        onBack = onBack,
                        hasActions = if (isSelectionMode) false else hasActions,
                        actions = actions,
                        isSelectionMode = isSelectionMode,
                        banners = banners
                    )
                }

                // Layer 3: ElvanCollapsedBar (Pill) — Fades in only on card collision
                ElvanCollapsedBar(
                    scrollOffset = currentScrollOffset,
                    collisionOffsetPx = collisionOffsetPx,
                    colors = colors,
                    expandedHeight = expandedHeight,
                    title = null,
                    onBack = onBack,
                    leadingIcon = leadingIcon,
                    navOpacity = effectiveNavOpacity,
                    hasActions = if (isSelectionMode) false else hasActions,
                    actions = actions,
                    isSelectionMode = isSelectionMode,
                    selectedCount = selectedCount,
                    isAllSelected = isAllSelected,
                    onSelectAll = onSelectAll,
                    onCancelSelection = onCancelSelection,
                    leadingActions = leadingActions
                )
            } else {
                // Layer 2: Top Fade Mask — strictly for portrait/desktop, NOT landscape (controls moved to sides)
                if (!isLandscape && !paneChrome) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .height(ceiling + pillHeight + 14.dp)
                            .zIndex(10f)
                            .background(
                                Brush.verticalGradient(
                                    0.0f to colors.background,
                                    0.65f to colors.background.copy(alpha = 0.85f),
                                    1.0f to Color.Transparent
                                )
                            )
                    )
                }

                // In landscape the header fades out on scroll (it never slides)
                val isScrolled = scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 8
                val topBarOffset by animateDpAsState(
                    targetValue = if (isLandscape && isScrolled && !isSelectionMode) -(ceiling + pillHeight + 14.dp) else 0.dp,
                    animationSpec = tween(durationMillis = 220, easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)),
                    label = "landscapeTopBarOffset"
                )
                val topBarAlpha by animateFloatAsState(
                    targetValue = if (isLandscape && isScrolled && !isSelectionMode) 0f else 1f,
                    animationSpec = tween(durationMillis = 180),
                    label = "landscapeTopBarAlpha"
                )

                // Landscape: back sits top-right like the home screen controls; the bar leaves room for it
                val chromeHandled = LocalElvanLandscapeChromeHandled.current
                val backOnSide = isLandscape && onBack != null && !isSelectionMode && !chromeHandled
                if (backOnSide) {
                    com.elvan.udukkai.ui.navigation.LandscapeTopActionButton(
                        icon = leadingIcon ?: com.elvan.udukkai.ui.navigation.MaterialSymbols.Rounded.ArrowBack,
                        contentDescription = if (leadingIcon != null) K.cancel.tr() else K.back.tr(),
                        onClick = onBack!!,
                        colors = colors,
                        modifier = Modifier.align(Alignment.TopEnd).zIndex(120f)
                    )
                }
                if (isLandscape) ElvanLandscapeChrome(
                    title = if (hideTitleSideways) "" else effectiveTitle,
                    topInset = statusBarHeight,
                    titleStart = 16.dp,
                    onToolbarPresent = { toolbarPresent = it },
                    titleAlpha = topBarAlpha,
                    hasActions = hasActions,
                    actions = actions,
                    colors = colors
                ) else Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .then(if (backOnSide) Modifier.padding(end = 78.dp) else Modifier)
                        .graphicsLayer { alpha = topBarAlpha }
                        .zIndex(100f)
                ) {
                    ElvanCollapsedBar(
                        scrollOffset = 0f,
                        collisionOffsetPx = 0f,
                        colors = colors,
                        expandedHeight = expandedHeight,
                        title = effectiveTitle,
                        onBack = if (backOnSide) null else onBack,
                        leadingIcon = leadingIcon,
                        navOpacity = effectiveNavOpacity,
                        hasActions = if (isSelectionMode) false else hasActions,
                        actions = actions,
                        isSelectionMode = isSelectionMode,
                        selectedCount = selectedCount,
                        isAllSelected = isAllSelected,
                        onSelectAll = onSelectAll,
                        onCancelSelection = onCancelSelection,
                        isPinnedAlways = true,
                        leadingActions = leadingActions,
                        scrollLift = if (collapsedOnly) {
                            val past = scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 0
                            if (past) (scrollState.firstVisibleItemScrollOffset / with(density) { 28.dp.toPx() }).coerceIn(0f, 1f).let { if (scrollState.firstVisibleItemIndex > 0) 1f else it } else 0f
                        } else null
                    )
                }
            }

            // Layer 4: Bottom Fade Mask and Navbar
            val imeBottom = com.elvan.udukkai.core.platform.getImeBottomPadding()
            val navBarsPadding = com.elvan.udukkai.core.platform.getNavBarBottomPadding()
            val isImeOpen = imeBottom > 0.dp

            if (showNavbar) {
                if (isSearchActive && isImeOpen) {
                    // Search active with keyboard: gentle gradient above keyboard, solid only behind keyboard
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp + 56.dp + 20.dp)
                                .background(
                                    Brush.verticalGradient(
                                        0.0f to Color.Transparent,
                                        0.3f to colors.background.copy(alpha = 0.16f),
                                        0.65f to colors.background.copy(alpha = 0.55f),
                                        1.0f to colors.background
                                    )
                                )
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(imeBottom)
                                .background(colors.background)
                        )
                    }
                } else {
                    // Standard floating navbar / bottom search pill: Smooth subtle gradient, zero solid box.
                    // Content flows freely beneath the navbar.
                    if (!isLandscape) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .height(72.dp + 56.dp + 16.dp + navBarsPadding)
                                .background(
                                    Brush.verticalGradient(
                                        0.0f to Color.Transparent,
                                        0.3f to colors.background.copy(alpha = 0.16f),
                                        0.65f to colors.background.copy(alpha = 0.55f),
                                        1.0f to colors.background
                                    )
                                )
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .then(
                            if (isLandscape) Modifier.fillMaxSize()
                            else Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                        )
                        .graphicsLayer {
                            alpha = effectiveNavOpacity
                        },
                    contentAlignment = if (isLandscape) Alignment.BottomEnd else Alignment.BottomCenter
                ) {
                    navbar()
                }
            } else {
                if (showBottomFade && !isLandscape && !paneChrome) {
                    // Subpages: Bottom Shadow Fade Mask above System Navigation Bar (Layer 4)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(if (hasFloatingBar) (72.dp + 56.dp + 16.dp + navBarsPadding) else (72.dp + navBarsPadding))
                            .background(
                                Brush.verticalGradient(
                                    0.0f to Color.Transparent,
                                    0.3f to colors.background.copy(alpha = 0.16f),
                                    0.65f to colors.background.copy(alpha = 0.55f),
                                    1.0f to colors.background
                                )
                            )
                    )
                }

                // Subpage floating bar rendered ABOVE the bottom shadow fade mask (Layer 5)
                if (effectiveNavOpacity > 0.01f) {
                    Box(
                        modifier = (if (isLandscape) Modifier.fillMaxSize() else Modifier.align(Alignment.BottomCenter).fillMaxWidth())
                            .graphicsLayer {
                                alpha = effectiveNavOpacity
                            }
                            .zIndex(100f),
                        contentAlignment = if (isLandscape) Alignment.BottomEnd else Alignment.BottomCenter
                    ) {
                        floatingBar()
                    }
                }
            }

            // Full-screen layer above the header and bars (the editors' draft preview)
            Box(modifier = Modifier.zIndex(150f)) { overlay() }

            // Layer 6: Android System Navigation Bar Touch Shield
            // Blocks touches in the physical system navigation bar area from leaking to the list underneath,
            // while keeping 100% edge-to-edge transparent rendering (Zero visual change).
            if (!isLandscape && navBarsPadding > 0.dp) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(navBarsPadding)
                        .zIndex(200f)
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Initial)
                                    event.changes.forEach { it.consume() }
                                }
                            }
                        }
                )
            }
            MenuHostLayer(menuHost)
}
        }
    }
    }
}

