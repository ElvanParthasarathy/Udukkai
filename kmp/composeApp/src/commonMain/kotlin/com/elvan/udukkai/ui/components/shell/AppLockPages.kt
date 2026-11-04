package com.elvan.udukkai.ui.components.shell

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.elvan.udukkai.core.platform.AppBackHandler
import com.elvan.udukkai.core.security.AppLock
import com.elvan.udukkai.core.security.authenticateFingerprint
import com.elvan.udukkai.core.security.fingerprintAvailable
import com.elvan.udukkai.core.security.openDeviceSecuritySettings
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.ui.components.splash.UdukkaiSplashScreen
import com.elvan.udukkai.ui.screens.onboarding.screens.ScreenLockRequiredScreen
import kotlinx.coroutines.delay

/**
 * The app lock gate. When enabled by the user in Settings, it displays the expressive
 * Udukkai splash screen and immediately requests Android system credentials
 * (Fingerprint / Face / Device PIN / Pattern) with zero delay.
 * If the device has no screen lock set, it shows a clean ScreenLockRequiredScreen.
 */
@Composable
fun AppLockGate(colors: ShellColors, content: @Composable () -> Unit) {
    val unlocked = AppLock.unlockedThisRun
    val isSet = AppLock.isSet()
    val locked = isSet && !unlocked
    var appShown by remember { mutableStateOf(!isSet || unlocked) }
    LaunchedEffect(locked) { if (!locked) appShown = true }

    var hasLock by remember { mutableStateOf(fingerprintAvailable()) }
    LaunchedEffect(locked) {
        while (locked) {
            val avail = fingerprintAvailable()
            if (avail != hasLock) {
                hasLock = avail
            }
            delay(500)
        }
    }

    val title = K.appLock.tr()

    fun askAuth() {
        if (fingerprintAvailable()) {
            authenticateFingerprint(title, "") { ok ->
                if (ok) AppLock.unlockedThisRun = true
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        if (appShown) {
            content()
        }

        val leave = fadeOut(tween(260)) + scaleOut(tween(260), targetScale = 1.06f)
        AnimatedVisibility(visible = locked, enter = EnterTransition.None, exit = leave) {
            AppBackHandler { }
            if (hasLock) {
                UdukkaiSplashScreen(
                    colors = colors,
                    onUnlock = { askAuth() }
                )
                LaunchedEffect(Unit) {
                    askAuth()
                }
            } else {
                ScreenLockRequiredScreen(
                    onOpenSettings = { openDeviceSecuritySettings() }
                )
            }
        }
    }
}

/**
 * Runs an action only after system authentication succeeds when AppLock is enabled.
 * Used for sensitive actions like deleting backups or restoring data.
 */
@Composable
fun rememberPinGuard(colors: ShellColors): (() -> Unit) -> Unit {
    val title = K.appLock.tr()
    return { action ->
        if (!AppLock.isSet() || !fingerprintAvailable()) {
            action()
        } else {
            authenticateFingerprint(title, "") { ok ->
                if (ok) action()
            }
        }
    }
}
