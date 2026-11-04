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
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.extensions.cssShadow
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
import com.elvan.udukkai.ui.screens.onboarding.components.AuthBackdrop
import com.elvan.udukkai.ui.screens.onboarding.screens.ScreenLockRequiredScreen
import kotlinx.coroutines.delay

/**
 * The app lock gate. When enabled, it displays the native Udukkai lock screen
 * with auth backdrop shapes and immediately requests Android system credentials
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
 * Dedicated Native App Lock Screen matching Udukkai's login/signup aesthetic.
 * Features the genuine auth backdrop shapes, centered lock container, app title,
 * Pure Senthamizh subtitle, and an interactive biometric/PIN unlock action with tactile
 * touch ripple feedback matching the welcome and security screens.
 */
@Composable
fun UdukkaiAppLockScreen(
    colors: ShellColors,
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ff = LocalAppFontFamily.current
    val isDark = colors.isDark
    // Dark mode uses deep background gray (0xFF1E1E1E); Light mode uses crisp signature black (0xFF111111)
    val lockBg = if (isDark) Color(0xFF1E1E1E) else Color(0xFF111111)
    val lockFg = if (isDark) colors.textPrimary else Color.White
    val lockRipple = Color.White.copy(alpha = if (isDark) 0.16f else 0.25f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        AuthBackdrop()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp)
        ) {
            // Elegant Lock Icon Container with flat gray tone and pure ripple
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(lockBg)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = lockRipple, bounded = true),
                        onClick = onUnlock
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = MaterialSymbols.Rounded.Lock,
                    contentDescription = null,
                    tint = lockFg,
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

            // Unlock Button: flat gray, without outline, with pure ripple
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(lockBg)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = lockRipple, bounded = true),
                        onClick = onUnlock
                    )
                    .padding(horizontal = 32.dp, vertical = 15.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.Lock,
                        contentDescription = null,
                        tint = lockFg,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = K.unlockApp.tr(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = lockFg
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
