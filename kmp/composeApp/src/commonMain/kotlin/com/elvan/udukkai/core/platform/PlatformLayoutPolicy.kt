package com.elvan.udukkai.core.platform

import androidx.compose.ui.unit.Dp

enum class ScreenLayout { AndroidPortrait, AndroidLandscape, Desktop }

/** Each platform owns its layout rules; a wide desktop window is never Android landscape. */
interface PlatformLayoutPolicy {
    val supportsLandscapeControls: Boolean
    fun screenLayout(width: Dp, height: Dp, inLandscapePane: Boolean = false): ScreenLayout
}

expect val platformLayoutPolicy: PlatformLayoutPolicy

enum class SettingsPaneLayout { SinglePane, AndroidLandscape, Desktop }

fun settingsPaneLayout(width: Dp, height: Dp, standalonePage: Boolean = false): SettingsPaneLayout =
    when (platformLayoutPolicy.screenLayout(width, height)) {
        ScreenLayout.AndroidPortrait -> SettingsPaneLayout.SinglePane
        // Android Settings stays a single, predictable page in every phone orientation.
        // A split pane made routes opened from Profile look like they had fallen back into the Settings hub.
        ScreenLayout.AndroidLandscape -> SettingsPaneLayout.SinglePane
        ScreenLayout.Desktop -> if (width > height && width.value >= 600f) SettingsPaneLayout.Desktop else SettingsPaneLayout.SinglePane
    }
