package com.elvan.udukkai.core.platform

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
actual fun getStatusBarTopPadding(): Dp =
    WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

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
