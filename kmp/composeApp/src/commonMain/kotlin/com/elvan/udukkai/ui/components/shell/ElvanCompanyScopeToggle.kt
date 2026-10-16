package com.elvan.udukkai.ui.components.shell

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.screens.create.components.ElvanPillShifter
import com.elvan.udukkai.ui.screens.create.components.PillShifterItem
import kotlin.jvm.JvmName

/**
 * Reusable Company Scope / Segmented Filter Toggle.
 *
 * Standalone, configurable Compose component for toggling between company scopes
 * (e.g. "All", "VRM", "PVS") or arbitrary segmented options.
 * Preserves fluid animated selection, touch drag, and responsive full-width layout
 * without embedding business or repository logic.
 */
@Composable
fun ElvanCompanyScopeToggle(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    colors: ShellColors = rememberShellColors(),
    isFullWidth: Boolean = true,
    height: Dp = 40.dp
) {
    if (options.isEmpty()) return

    val items = options.map { PillShifterItem(label = it) }

    ElvanPillShifter(
        items = items,
        selectedIndex = selectedIndex.coerceIn(0, options.size - 1),
        onIndexSelected = onOptionSelected,
        colors = colors,
        isFullWidth = isFullWidth,
        height = height,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ContentPadding)
    )
}

/**
 * Overload of [ElvanCompanyScopeToggle] accepting pre-configured [PillShifterItem]s
 * with optional active/inactive icons.
 */
@JvmName("ElvanCompanyScopeToggleWithItems")
@Composable
fun ElvanCompanyScopeToggle(
    items: List<PillShifterItem>,
    selectedIndex: Int,
    onIndexSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    colors: ShellColors = rememberShellColors(),
    isFullWidth: Boolean = true,
    height: Dp = 40.dp
) {
    if (items.isEmpty()) return

    ElvanPillShifter(
        items = items,
        selectedIndex = selectedIndex.coerceIn(0, items.size - 1),
        onIndexSelected = onIndexSelected,
        colors = colors,
        isFullWidth = isFullWidth,
        height = height,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ContentPadding)
    )
}
