package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors

/**
 * மென்பொருள் வடிவாளர் பற்றி திரை — About Developer Screen.
 * Unified with Elvan Navil Studio screen.
 */
@Composable
fun AboutDeveloperScreen(
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    TransliteratorSettingsScreen(scrollState = scrollState, colors = colors)
}
