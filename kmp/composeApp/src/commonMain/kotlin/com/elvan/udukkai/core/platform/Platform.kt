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

@androidx.compose.runtime.Composable
expect fun DisableOverscroll(content: @androidx.compose.runtime.Composable () -> Unit)
