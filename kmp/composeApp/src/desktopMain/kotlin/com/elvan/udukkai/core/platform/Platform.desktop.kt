package com.elvan.udukkai.core.platform

actual val currentPlatform: PlatformType = PlatformType.DESKTOP

@androidx.compose.runtime.Composable
actual fun ConfigureDialogWindow(
    isDark: Boolean,
    clearDim: Boolean,
    navBarColor: androidx.compose.ui.graphics.Color?
) {
    // No-op on desktop
}

@androidx.compose.runtime.Composable
actual fun DisableOverscroll(content: @androidx.compose.runtime.Composable () -> Unit) {
    content()
}
