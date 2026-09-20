package com.elvan.udukkai.ui.components.shell

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.elvan.udukkai.core.platform.ConfigureDialogWindow
import com.elvan.udukkai.core.platform.PlatformType
import com.elvan.udukkai.core.platform.currentPlatform
import com.elvan.udukkai.core.platform.getNavBarBottomPadding
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.LocalShellColors
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.ElvanSimpleScrollbar
import com.elvan.udukkai.ui.components.shell.sheets.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * ElvanSelectionBottomSheet — Rock-Solid Raw Bottom Sheet.
 * Anchored directly to the bottom of the screen with zero upward spring lift-off (Zero Detach).
 * Built with Dialog + full-screen scrim + downward-only drag to dismiss.
 * Supports single selection and multi-selection with sticky bottom actions.
 */
@Composable
fun <T> ElvanSelectionBottomSheet(
    title: String,
    items: List<T>,
    currentValue: T? = null,
    onSelected: ((T) -> Unit)? = null,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    colors: ShellColors = rememberShellColors(),
    itemLabelBuilder: (T) -> String = { it.toString() },
    subtitleBuilder: ((T) -> String?)? = null,
    leadingBuilder: (@Composable (T) -> Unit)? = null,
    showSearch: Boolean = false,
    searchFilter: ((T, String) -> Boolean)? = null,
    onRequestAddNew: (() -> Unit)? = null,
    addNewLabel: String? = null,
    // Multi-selection additions
    selectedValues: Set<T>? = null,
    onConfirmed: ((List<T>) -> Unit)? = null,
    confirmLabel: String? = null,
    itemRowContent: (@Composable (item: T, isSelected: Boolean, onToggle: () -> Unit) -> Unit)? = null,
    onSelectionChanged: ((List<T>) -> Unit)? = null,
    emptyMessage: String? = null
) {
    val isDark = colors.isDark
    val sheetBg = LocalShellColors.current.surface
    val ff = LocalAppFontFamily.current
    var searchQuery by remember { mutableStateOf("") }
    val isMultiSelect = onConfirmed != null

    val selectedItems = remember(selectedValues) {
        mutableStateListOf<T>().apply {
            if (selectedValues != null) {
                addAll(selectedValues)
            }
        }
    }

    val filteredItems = remember(items, searchQuery) {
        if (searchQuery.isBlank()) {
            items
        } else if (searchFilter != null) {
            items.filter { searchFilter(it, searchQuery) }
        } else {
            items.filter { item ->
                itemLabelBuilder(item).contains(searchQuery, ignoreCase = true) ||
                        (subtitleBuilder?.invoke(item)?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
    }

    // Desktop Presentation (Clean centered modal card)
    if (currentPlatform == PlatformType.DESKTOP) {
        Dialog(onDismissRequest = onDismissRequest) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = sheetBg,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ElvanSheetHeader(title = title, colors = colors)
                    if (showSearch) {
                        ElvanSheetSearch(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            colors = colors
                        )
                    }
                    val desktopListState = rememberLazyListState()
                    LazyColumn(
                        state = desktopListState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        contentPadding = PaddingValues(
                            top = 4.dp,
                            bottom = if (onRequestAddNew != null || onConfirmed != null) 8.dp else 24.dp
                        )
                    ) {
                        if (filteredItems.isEmpty() && emptyMessage != null) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 36.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = emptyMessage.preventBrokenLigatures(),
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 14.sp,
                                            color = colors.textSecondary.copy(alpha = 0.6f)
                                        )
                                    )
                                }
                            }
                        } else {
                            items(filteredItems) { item ->
                                val isSelected = if (isMultiSelect) selectedItems.contains(item) else item == currentValue
                                if (itemRowContent != null) {
                                    itemRowContent(item, isSelected) {
                                        if (isSelected) {
                                            selectedItems.remove(item)
                                        } else {
                                            selectedItems.add(item)
                                        }
                                        onSelectionChanged?.invoke(selectedItems.toList())
                                    }
                                } else {
                                    ElvanSheetItem(
                                        title = itemLabelBuilder(item),
                                        subtitle = subtitleBuilder?.invoke(item),
                                        isSelected = isSelected,
                                        onTap = {
                                            if (isMultiSelect) {
                                                if (isSelected) {
                                                    selectedItems.remove(item)
                                                } else {
                                                    selectedItems.add(item)
                                                }
                                                onSelectionChanged?.invoke(selectedItems.toList())
                                            } else {
                                                onSelected?.invoke(item)
                                                onDismissRequest()
                                            }
                                        },
                                        leading = leadingBuilder?.let { { it(item) } },
                                        colors = colors
                                    )
                                }
                            }
                        }
                    }
                    if (onRequestAddNew != null) {
                        ElvanSheetNewButton(
                            onTap = {
                                onDismissRequest()
                                onRequestAddNew()
                            },
                            label = addNewLabel ?: com.elvan.udukkai.localization.K.addNew.tr(),
                            colors = colors
                        )
                    }
                    if (onConfirmed != null) {
                        ElvanSheetConfirmButton(
                            onTap = {
                                onDismissRequest()
                                onConfirmed(selectedItems.toList())
                            },
                            label = confirmLabel ?: com.elvan.udukkai.localization.K.done.tr(),
                            colors = colors
                        )
                    }
                }
            }
        }
        return
    }

    // Mobile Presentation: Rock-Solid Bottom-Anchored Sheet (Zero Detach, Zero Upward Bounce)
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val initialSheetOffsetPx = with(density) { 1000.dp.toPx() }
    val sheetOffsetY = remember { Animatable(initialSheetOffsetPx) }
    var isClosing by remember { mutableStateOf(false) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val imeBottomPx = WindowInsets.ime.getBottom(density)
    val isKeyboardOpen = imeBottomPx > 0

    LaunchedEffect(Unit) {
        sheetOffsetY.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = 300,
                easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
            )
        )
    }

    fun dismissSheet() {
        if (isClosing) return
        keyboardController?.hide()
        focusManager.clearFocus()
        isClosing = true
        coroutineScope.launch {
            val currentTotal = sheetOffsetY.value + dragOffsetY
            sheetOffsetY.snapTo(currentTotal)
            dragOffsetY = 0f
            sheetOffsetY.animateTo(
                targetValue = initialSheetOffsetPx,
                animationSpec = tween(
                    durationMillis = 240,
                    easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
                )
            )
            onDismissRequest()
        }
    }

    val listState = rememberLazyListState()

    val draggableState = rememberDraggableState { delta ->
        // Clamped at 0f: Only allow dragging DOWNWARDS (delta > 0).
        // Upward dragging is strictly prohibited to guarantee 100% Zero-Detach.
        if (delta > 5f && isKeyboardOpen) {
            keyboardController?.hide()
            focusManager.clearFocus()
        }
        val target = dragOffsetY + delta
        dragOffsetY = target.coerceAtLeast(0f)
    }

    fun snapBackDrag() {
        if (dragOffsetY > 0f) {
            coroutineScope.launch {
                androidx.compose.animation.core.Animatable(dragOffsetY).animateTo(
                    targetValue = 0f,
                    animationSpec = tween(160, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f))
                ) {
                    dragOffsetY = value
                }
            }
        }
    }

    val downwardScrollConnection = remember(isKeyboardOpen) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -10f && isKeyboardOpen) {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
                // If sheet is already pulled down and user drags back up, consume delta
                if (available.y < 0f && dragOffsetY > 0f) {
                    val consumed = available.y.coerceAtLeast(-dragOffsetY)
                    dragOffsetY += consumed
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // Only pull the sheet down if the list is physically at the top
                val isAtTop = !listState.canScrollBackward
                if (available.y > 0f && isAtTop && source == NestedScrollSource.UserInput) {
                    dragOffsetY += available.y * 0.7f
                    return Offset(0f, available.y)
                }
                // UPWARD IS NEVER CONSUMED AND NEVER MOVES SHEET: Zero Bounce!
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                // ONLY dismiss if the sheet itself was dragged down significantly (dragOffsetY > 70f)
                // Never dismiss on regular list flings (when dragOffsetY <= 70f)!
                if (dragOffsetY > 140f || (dragOffsetY > 70f && available.y > 1000f)) {
                    dismissSheet()
                    return available
                } else if (dragOffsetY > 0f) {
                    snapBackDrag()
                    return available
                }
                return Velocity.Zero
            }
        }
    }

    Dialog(
        onDismissRequest = { dismissSheet() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        ConfigureDialogWindow(
            isDark = isDark,
            clearDim = true,
            navBarColor = sheetBg
        )

        // Scrim background with smooth fade
        val scrimAlpha by animateFloatAsState(
            targetValue = if (!isClosing) 0.5f else 0.0f,
            animationSpec = tween(durationMillis = if (!isClosing) 300 else 240),
            label = "scrimAlpha"
        )

        val navBarBottomPadding = getNavBarBottomPadding()

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .background(Color.Black.copy(alpha = scrimAlpha))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    dismissSheet()
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            val availableHeight = maxHeight
            val hasBottomAction = onRequestAddNew != null || onConfirmed != null
            val topDragReserved = 48.dp
            val searchReserved = if (showSearch) 64.dp else 0.dp
            val bottomReserved = if (hasBottomAction) 64.dp else 0.dp
            val navReserved = navBarBottomPadding + 24.dp
            val maxListHeight = (availableHeight - topDragReserved - searchReserved - bottomReserved - navReserved).coerceIn(120.dp, 460.dp)

            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .offset { IntOffset(0, (sheetOffsetY.value + dragOffsetY).roundToInt()) }
            ) {
                Surface(
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    color = sheetBg,
                    contentColor = colors.textPrimary,
                    shadowElevation = 16.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            // Consume clicks so tapping inside the sheet does not dismiss
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .nestedScroll(downwardScrollConnection)
                            .padding(bottom = navBarBottomPadding + 16.dp)
                    ) {
                    // Full Header Draggable Area (Clean Drag Handle only, generous breathing room)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .draggable(
                                state = draggableState,
                                orientation = Orientation.Vertical,
                                onDragStopped = { velocity ->
                                    if (dragOffsetY > 100f || velocity > 800f) {
                                        dismissSheet()
                                    } else {
                                        snapBackDrag()
                                    }
                                }
                            )
                            .padding(top = 16.dp, bottom = if (showSearch) 18.dp else 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (isDark) Color.White.copy(alpha = 0.25f)
                                    else Color.Black.copy(alpha = 0.2f)
                                )
                        )
                    }

                    // Search pill if enabled (with generous spacing and downward drag-to-dismiss)
                    if (showSearch) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .draggable(
                                    state = draggableState,
                                    orientation = Orientation.Vertical,
                                    onDragStopped = { velocity ->
                                        if (dragOffsetY > 100f || velocity > 800f) {
                                            dismissSheet()
                                        } else {
                                            snapBackDrag()
                                        }
                                    }
                                )
                        ) {
                            ElvanSheetSearch(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                colors = colors
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    } else {
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Items List
                    if (filteredItems.size <= 7 && !showSearch && !isMultiSelect) {
                        // Direct forEach: Draggable container so swiping down anywhere on items dismisses sheet
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .draggable(
                                    state = draggableState,
                                    orientation = Orientation.Vertical,
                                    onDragStopped = { velocity ->
                                        if (dragOffsetY > 100f || velocity > 800f) {
                                            dismissSheet()
                                        } else {
                                            snapBackDrag()
                                        }
                                    }
                                )
                        ) {
                            filteredItems.forEach { item ->
                                val isSelected = item == currentValue
                                if (itemRowContent != null) {
                                    itemRowContent(item, isSelected) {
                                        onSelected?.invoke(item)
                                        dismissSheet()
                                    }
                                } else {
                                    ElvanSheetItem(
                                        title = itemLabelBuilder(item),
                                        subtitle = subtitleBuilder?.invoke(item),
                                        isSelected = isSelected,
                                        onTap = {
                                            onSelected?.invoke(item)
                                            dismissSheet()
                                        },
                                        leading = leadingBuilder?.let { { it(item) } },
                                        colors = colors
                                    )
                                }
                            }
                        }
                    } else {
                        // Standard LazyColumn: List scrolls normally; sheet stays firmly pinned
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = maxListHeight)
                        ) {
                            if (filteredItems.isEmpty() && emptyMessage != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 36.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = emptyMessage.preventBrokenLigatures(),
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 14.sp,
                                            color = colors.textSecondary.copy(alpha = 0.6f)
                                        )
                                    )
                                }
                            } else {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(filteredItems) { item ->
                                        val isSelected = if (isMultiSelect) selectedItems.contains(item) else item == currentValue
                                        if (itemRowContent != null) {
                                            itemRowContent(item, isSelected) {
                                                if (isSelected) {
                                                    selectedItems.remove(item)
                                                } else {
                                                    selectedItems.add(item)
                                                }
                                                onSelectionChanged?.invoke(selectedItems.toList())
                                            }
                                        } else {
                                            ElvanSheetItem(
                                                title = itemLabelBuilder(item),
                                                subtitle = subtitleBuilder?.invoke(item),
                                                isSelected = isSelected,
                                                onTap = {
                                                    if (isMultiSelect) {
                                                        if (isSelected) {
                                                            selectedItems.remove(item)
                                                        } else {
                                                            selectedItems.add(item)
                                                        }
                                                        onSelectionChanged?.invoke(selectedItems.toList())
                                                    } else {
                                                        onSelected?.invoke(item)
                                                        dismissSheet()
                                                    }
                                                },
                                                leading = leadingBuilder?.let { { it(item) } },
                                                colors = colors
                                            )
                                        }
                                    }
                                }

                                ElvanSimpleScrollbar(
                                    scrollState = listState,
                                    modifier = Modifier.matchParentSize(),
                                    colors = colors,
                                    topPadding = 4.dp,
                                    bottomPadding = 4.dp,
                                    headerItemsCount = 0
                                )
                            }
                        }
                    }

                    // Optional Add New button
                    if (onRequestAddNew != null) {
                        ElvanSheetNewButton(
                            onTap = {
                                dismissSheet()
                                onRequestAddNew()
                            },
                            label = addNewLabel ?: com.elvan.udukkai.localization.K.addNew.tr(),
                            colors = colors
                        )
                    }

                    // Optional Done / Confirm button
                    if (onConfirmed != null) {
                        ElvanSheetConfirmButton(
                            onTap = {
                                onConfirmed(selectedItems.toList())
                                dismissSheet()
                            },
                            label = confirmLabel ?: com.elvan.udukkai.localization.K.done.tr(),
                            colors = colors
                        )
                    }
                }
            }

            // Solid bottom filler extending behind system navigation bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(navBarBottomPadding)
                    .background(sheetBg)
            )
        }
    }
}
}
