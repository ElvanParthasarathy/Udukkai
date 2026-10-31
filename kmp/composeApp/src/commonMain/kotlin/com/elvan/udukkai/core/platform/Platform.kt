package com.elvan.udukkai.core.platform

enum class PlatformType { ANDROID, DESKTOP }

expect val currentPlatform: PlatformType

expect val isDebugBuild: Boolean

@androidx.compose.runtime.Composable
expect fun ConfigureDialogWindow(
    isDark: Boolean,
    clearDim: Boolean = false,
    navBarColor: androidx.compose.ui.graphics.Color? = null
)

/** Full-width dialog whose window also reaches under the system bars, so a bottom sheet's colour fills the gap at the foot. */
expect fun edgeToEdgeDialogProperties(): androidx.compose.ui.window.DialogProperties

@androidx.compose.runtime.Composable
expect fun DisableOverscroll(content: @androidx.compose.runtime.Composable () -> Unit)

/** Hides the status and navigation bars of the current (dialog) window while [hidden], like a photo gallery. */
@androidx.compose.runtime.Composable
expect fun HideSystemBars(hidden: Boolean)
