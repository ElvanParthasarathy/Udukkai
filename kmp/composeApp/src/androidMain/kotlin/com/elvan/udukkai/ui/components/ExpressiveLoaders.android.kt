package com.elvan.udukkai.ui.components

import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
actual fun ExpressiveDotsLoader(modifier: Modifier, color: Color) {
    LoadingIndicator(modifier = modifier, color = color)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
actual fun ExpressiveContainedLoader(
    modifier: Modifier,
    progress: (() -> Float)?,
    containerColor: Color,
    indicatorColor: Color
) {
    if (progress == null) {
        ContainedLoadingIndicator(
            modifier = modifier,
            containerColor = containerColor,
            indicatorColor = indicatorColor
        )
    } else {
        ContainedLoadingIndicator(
            progress = progress,
            modifier = modifier,
            containerColor = containerColor,
            indicatorColor = indicatorColor
        )
    }
}
