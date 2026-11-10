package com.elvan.udukkai.core.platform

import androidx.compose.ui.unit.Dp

actual val platformLayoutPolicy: PlatformLayoutPolicy = object : PlatformLayoutPolicy {
    override val supportsLandscapeControls = true
    override fun screenLayout(width: Dp, height: Dp, inLandscapePane: Boolean): ScreenLayout =
        when {
            inLandscapePane || width > height -> ScreenLayout.AndroidLandscape
            else -> ScreenLayout.AndroidPortrait
        }
}
