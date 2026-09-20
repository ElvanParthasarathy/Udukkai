package com.elvan.udukkai.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
actual fun getStatusBarTopPadding(): Dp = 0.dp

@Composable
actual fun getNavBarBottomPadding(): Dp = 0.dp

@Composable
actual fun getImeBottomPadding(): Dp = 0.dp

@Composable
actual fun Modifier.navigationBarsPaddingIfMobile(): Modifier = this
