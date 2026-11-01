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

actual fun openDialer(number: String) {
    // No phone on desktop
}

actual fun openMailTo(address: String) {
    try {
        java.awt.Desktop.getDesktop().mail(java.net.URI("mailto:$address"))
    } catch (_: Exception) {
    }
}

@androidx.compose.runtime.Composable
actual fun DisableOverscroll(content: @androidx.compose.runtime.Composable () -> Unit) {
    content()
}

@androidx.compose.runtime.Composable
actual fun HideSystemBars(hidden: Boolean) {
    // No system bars on desktop
}
