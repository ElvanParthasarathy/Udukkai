package com.elvan.udukkai.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * ExpressiveLoadingIndicator — Centralized animated circular progress indicator matching Neram.
 */
@Composable
fun ExpressiveLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    @Suppress("UNUSED_PARAMETER") strokeWidth: Dp = 3.dp
) {
    ExpressiveDotsLoader(modifier = modifier, color = color)
}
