package com.elvan.udukkai.ui.components.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.security.AppLock
import com.elvan.udukkai.core.security.authenticateDeviceLock
import com.elvan.udukkai.core.security.deviceLockAvailable
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * Returns a function that runs an action only after the phone's lock (PIN, pattern, password, fingerprint) was passed;
 * with the app lock off it runs the action straight away. Used for the actions that can lose or give away data.
 */
@Composable
fun rememberPinGuard(colors: ShellColors): (() -> Unit) -> Unit {
    val title = K.appLock.tr()
    val notUnlocked = K.appLockNotUnlocked.tr()
    return { action ->
        if (AppLock.isEnabled() && deviceLockAvailable()) {
            authenticateDeviceLock(title) { ok -> if (ok) action() else ElvanSnackbar.show(notUnlocked) }
        } else {
            action()
        }
    }
}

/**
 * The app lock page: the app is drawn under it (so nothing being edited is lost) but only the lock page shows and takes
 * touches while the phone's lock has not been passed: at opening, and when the app comes back after a minute away.
 * If the phone no longer has a screen lock, the app opens (it must never lock its owner out).
 */
@Composable
fun AppLockGate(colors: ShellColors, content: @Composable () -> Unit) {
    val locked = AppLock.isEnabled() && !AppLock.unlockedThisRun && deviceLockAvailable()
    Box(Modifier.fillMaxSize()) {
        content()
        if (locked) LockPage(colors)
    }
}

@Composable
private fun LockPage(colors: ShellColors) {
    val ff = LocalAppFontFamily.current
    val title = K.appLock.tr()
    fun ask() = authenticateDeviceLock(title) { ok -> if (ok) AppLock.unlockedThisRun = true }
    // The back gesture must not reach the screens under the lock page
    com.elvan.udukkai.core.platform.AppBackHandler { }
    LaunchedEffect(Unit) { ask() }
    Box(
        Modifier.fillMaxSize().background(colors.background)
            .pointerInput(Unit) { awaitPointerEventScope { while (true) awaitPointerEvent().changes.forEach { it.consume() } } },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(Modifier.size(72.dp).clip(CircleShape).background(colors.iconBg).clickable { ask() }, contentAlignment = Alignment.Center) {
                Icon(MaterialSymbols.Rounded.Lock, null, tint = colors.textPrimary, modifier = Modifier.size(30.dp))
            }
            Text(K.appLockUnlock.tr(), style = TextStyle(fontFamily = ff, fontSize = 15.sp, fontWeight = FontWeight.Medium), color = colors.textPrimary)
        }
    }
}
