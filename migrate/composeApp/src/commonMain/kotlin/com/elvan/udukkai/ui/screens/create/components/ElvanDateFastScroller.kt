package com.elvan.udukkai.ui.screens.create.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.ElvanFastScroller

/**
 * Backward-compatible wrapper for [ElvanFastScroller] with date pill functionality.
 */
@Composable
fun ElvanDateFastScroller(
    scrollState: LazyListState,
    dateProvider: (Int) -> String,
    modifier: Modifier = Modifier,
    colors: ShellColors = rememberShellColors(),
    topPadding: Dp = 104.dp,
    bottomPadding: Dp = 104.dp,
    headerItemsCount: Int = 0
) {
    ElvanFastScroller(
        scrollState = scrollState,
        modifier = modifier,
        dateProvider = dateProvider,
        colors = colors,
        topPadding = topPadding,
        bottomPadding = bottomPadding,
        headerItemsCount = headerItemsCount
    )
}
