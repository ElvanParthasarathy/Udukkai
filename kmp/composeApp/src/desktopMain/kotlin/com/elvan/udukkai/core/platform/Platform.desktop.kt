package com.elvan.udukkai.core.platform

actual val currentPlatform: PlatformType = PlatformType.DESKTOP

actual val isDebugBuild: Boolean = System.getProperty("debug", "true") == "true"

@androidx.compose.runtime.Composable
actual fun ConfigureDialogWindow(
    isDark: Boolean,
    clearDim: Boolean,
    navBarColor: androidx.compose.ui.graphics.Color?
) {
    // No-op on desktop
}

actual fun edgeToEdgeDialogProperties() = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)

@androidx.compose.runtime.Composable
actual fun DisableOverscroll(content: @androidx.compose.runtime.Composable () -> Unit) {
    content()
}

@androidx.compose.runtime.Composable
actual fun HideSystemBars(hidden: Boolean) {
    // No system bars on desktop
}
