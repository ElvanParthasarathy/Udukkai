package com.elvan.udukkai.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * The plain Material 3 expressive loader: a shape that morphs as it turns. On Android this is Material 3's own
 * LoadingIndicator, the one Neram uses; the desktop build draws a close copy.
 */
@Composable
expect fun ExpressiveDotsLoader(
    modifier: Modifier = Modifier,
    color: Color = Color.White
)

/**
 * The same loader in a round container, for plain pages, lists and the pull-down refresh (Material 3's
 * ContainedLoadingIndicator on Android). It follows the pull while [progress] is given and spins on its
 * own when it is null.
 */
@Composable
expect fun ExpressiveContainedLoader(
    modifier: Modifier = Modifier,
    progress: (() -> Float)? = null,
    containerColor: Color,
    indicatorColor: Color
)
