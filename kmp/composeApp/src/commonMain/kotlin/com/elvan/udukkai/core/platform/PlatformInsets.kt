package com.elvan.udukkai.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
expect fun getStatusBarTopPadding(): Dp

@Composable
expect fun getNavBarBottomPadding(): Dp

@Composable
expect fun getImeBottomPadding(): Dp

@Composable
expect fun Modifier.navigationBarsPaddingIfMobile(): Modifier
