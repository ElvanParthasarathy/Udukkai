package com.elvan.udukkai.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
expect fun getStatusBarTopPadding(): Dp

@Composable
expect fun getNavBarBottomPadding(): Dp

/** Height of the status bar even while it is hidden, so layouts do not move when the bars come and go. */
@Composable
expect fun getStatusBarHeightIgnoringVisibility(): Dp

/** Height of the navigation bar even while it is hidden. */
@Composable
expect fun getNavBarHeightIgnoringVisibility(): Dp

@Composable
expect fun getImeBottomPadding(): Dp

@Composable
expect fun Modifier.navigationBarsPaddingIfMobile(): Modifier
