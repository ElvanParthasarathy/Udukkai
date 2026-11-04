package com.elvan.udukkai.ui.components.shell

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.platform.AppBackHandler
import com.elvan.udukkai.core.security.AppLock
import com.elvan.udukkai.core.security.authenticateFingerprint
import com.elvan.udukkai.core.security.fingerprintAvailable
import com.elvan.udukkai.core.security.openDeviceSecuritySettings
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.ShellDefaults
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.onboarding.screens.ScreenLockRequiredScreen
import kotlinx.coroutines.delay

/**
 * The app lock gate. When enabled, it displays the native Udukkai lock screen
 * and immediately requests Android system credentials (Fingerprint / Face / Device PIN / Pattern)
 * with zero delay. If the device has no screen lock set, it shows a clean ScreenLockRequiredScreen.
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
                UdukkaiAppLockScreen(
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
 * Dedicated Native App Lock Screen matching Udukkai's dark/light aesthetic.
 * Replaces the splash screen with a clean lock container, app title, Pure Senthamizh subtitle,
 * and a biometric/PIN unlock action, with zero external branding or splash shapes.
 */
@Composable
fun UdukkaiAppLockScreen(
    colors: ShellColors,
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ff = LocalAppFontFamily.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            // Elegant Lock Icon Container
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(colors.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = MaterialSymbols.Rounded.Lock,
                    contentDescription = null,
                    tint = colors.textPrimary,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // App Name
            Text(
                text = "உடுக்கை",
                style = TextStyle(
                    fontFamily = ff,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            )

            Spacer(Modifier.height(8.dp))

            // Subtitle
            Text(
                text = K.appLockRowHint.tr(),
                style = TextStyle(
                    fontFamily = ff,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = colors.textSecondary
                )
            )

            Spacer(Modifier.height(36.dp))

            // Unlock Button
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ShellDefaults.ripple(colors, bounded = true),
                        onClick = onUnlock
                    ),
                shape = RoundedCornerShape(999.dp),
                color = colors.surface,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.Lock,
                        contentDescription = null,
                        tint = colors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = K.unlockApp.tr(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                    )
                }
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
