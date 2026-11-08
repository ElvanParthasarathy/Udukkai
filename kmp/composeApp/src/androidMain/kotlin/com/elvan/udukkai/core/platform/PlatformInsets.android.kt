package com.elvan.udukkai.core.platform

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private var lastKnownStatusBar = 0.dp

/**
 * A new Activity gets its insets a few frames late, so its first frames would lay out for a 0dp status bar and then jump.
 * Until they arrive the height from the previous Activity is used. A bar that is merely hidden still reports its size
 * through the "ignoring visibility" inset, so full-screen pages keep their real 0.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
actual fun getStatusBarTopPadding(): Dp {
    val real = WindowInsets.statusBarsIgnoringVisibility.asPaddingValues().calculateTopPadding()
    if (real > 0.dp) lastKnownStatusBar = real else return lastKnownStatusBar
    return WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
actual fun getStatusBarHeightIgnoringVisibility(): Dp =
    WindowInsets.statusBarsIgnoringVisibility.asPaddingValues().calculateTopPadding()

@OptIn(ExperimentalLayoutApi::class)
@Composable
actual fun getNavBarHeightIgnoringVisibility(): Dp =
    WindowInsets.navigationBarsIgnoringVisibility.asPaddingValues().calculateBottomPadding()

@Composable
actual fun getNavBarBottomPadding(): Dp {
    val composeInsets = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    if (composeInsets > 0.dp) return composeInsets

    val view = androidx.compose.ui.platform.LocalView.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    val rootInsets = androidx.core.view.ViewCompat.getRootWindowInsets(view)
    val navBarsPx = rootInsets?.getInsets(androidx.core.view.WindowInsetsCompat.Type.navigationBars())?.bottom ?: 0
    return with(density) { navBarsPx.toDp() }
}

@Composable
actual fun getImeBottomPadding(): Dp =
    WindowInsets.ime.asPaddingValues().calculateBottomPadding()

@Composable
actual fun Modifier.navigationBarsPaddingIfMobile(): Modifier =
    this.windowInsetsPadding(WindowInsets.navigationBars)
