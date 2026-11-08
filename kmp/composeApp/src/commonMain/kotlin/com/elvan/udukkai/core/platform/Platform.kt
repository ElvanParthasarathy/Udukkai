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
expect fun edgeToEdgeDialogProperties(dismissOnBackPress: Boolean = true, dismissOnClickOutside: Boolean = true): androidx.compose.ui.window.DialogProperties

/** Opens the phone's dialer with [number] filled in (nothing is dialled until the person presses call). */
expect fun openDialer(number: String)

/** Opens the mail app with a new message to [address]. */
expect fun openMailTo(address: String)

@androidx.compose.runtime.Composable
expect fun DisableOverscroll(content: @androidx.compose.runtime.Composable () -> Unit)

/** Hides the status and navigation bars of the current (dialog) window while [hidden], like a photo gallery. */
@androidx.compose.runtime.Composable
expect fun HideSystemBars(hidden: Boolean)

/** Updates the launcher activity alias to match the given language code without restarting the app. */
expect fun updatePlatformLauncher(languageCode: String)

/** Returns true if the device currently has an active internet connection. */
expect fun isNetworkAvailable(): Boolean

/** True where a dialog can blur the screen behind it (Android 12+ with window blur on); sheets stay solid elsewhere. */
expect fun dialogBlurAvailable(): Boolean
