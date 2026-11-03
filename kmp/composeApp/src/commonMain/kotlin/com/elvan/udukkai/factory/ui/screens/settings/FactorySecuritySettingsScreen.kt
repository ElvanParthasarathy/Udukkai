package com.elvan.udukkai.factory.ui.screens.settings

import com.elvan.udukkai.core.security.AppLock
import com.elvan.udukkai.core.security.authenticateDeviceLock
import com.elvan.udukkai.core.security.deviceLockAvailable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.elvan.udukkai.core.auth.AuthManager
import com.elvan.udukkai.core.backup.getUdukkaiBackupService
import com.elvan.udukkai.core.backup.reloadFactoryRepositories
import com.elvan.udukkai.core.platform.getPreferencesHelper
import com.elvan.udukkai.factory.data.FactoryProfileRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Factory Security Settings Screen (ஆலை பாதுகாப்பு அமைப்புகள்).
 * Dedicated to Udukkai Factory with zero cross-app dependencies.
 */
@Composable
fun FactorySecuritySettingsScreen(
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val scope = rememberCoroutineScope()

    var showSyncLoading by remember { mutableStateOf(false) }
    var showSignOutConfirm by remember { mutableStateOf(false) }
    var showSignOutLoading by remember { mutableStateOf(false) }
    val guard = rememberPinGuard(colors)
    var lockTick by remember { mutableStateOf(0) }
    val lockTitle = K.appLock.tr()
    val noScreenLockMsg = K.appLockNeedsScreenLock.tr()



    val syncSuccessMsg = K.synced.tr()
    val signOutSuccessMsg = K.signedOutSuccessfully.tr()

    val currentProfile = FactoryProfileRepository.profile

    LazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Dimens.ContentPadding,
            end = Dimens.ContentPadding,
            bottom = Dimens.SubpageContentPaddingBottom
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
    ) {
        item(key = "shell_top_spacer") {
            Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
        }

        item {
            ElvanSettingsSection(colors = colors) {
                // 1. Sync App (Orunginai Cheyali)
                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.Sync,
                    title = K.syncAppBtn.tr(),
                    description = K.syncRecentChanges.tr(),
                    onClick = {
                        showSyncLoading = true
                        scope.launch {
                            try {
                                reloadFactoryRepositories()
                            } catch (_: Exception) {}
                            try {
                                com.elvan.udukkai.core.sync.getFirebaseSyncManager().triggerManualSync()
                            } catch (_: Exception) {}
                            delay(600)
                            showSyncLoading = false
                            ElvanSnackbar.show(syncSuccessMsg)
                        }
                    },
                    colors = colors
                )
                ElvanSettingsDivider(colors = colors)

                // App lock: the phone's own screen lock, asked at opening and before the actions that can lose or give away data
                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.Lock,
                    title = lockTitle,
                    description = (if (lockLabelOn(lockTick)) K.pinOn else K.pinOff).tr() + "\n" + K.appLockHint.tr(),
                    onClick = {
                        if (!deviceLockAvailable()) {
                            // The phone's screen lock is gone: the switch can still be turned off, never on
                            if (AppLock.isEnabled()) { AppLock.setEnabled(false); lockTick++ } else ElvanSnackbar.show(noScreenLockMsg)
                        } else {
                            // Turning it on or off needs the phone's lock too, so only its owner can change it
                            authenticateDeviceLock(lockTitle) { ok ->
                                if (ok) { AppLock.setEnabled(!AppLock.isEnabled()); lockTick++ }
                            }
                        }
                    },
                    colors = colors
                )
                ElvanSettingsDivider(colors = colors)

                // 2. Sign Out (Veliyaeru)
                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.Logout,
                    title = K.logout.tr(),
                    description = K.logoutApplication.tr(),
                    onClick = { guard { showSignOutConfirm = true } },
                    colors = colors
                )
            }
        }
    }

    // Sign Out Confirmation Action Sheet
    if (showSignOutConfirm) {
        ElvanActionSheet(
            title = K.logout.tr(),
            cancelText = K.cancelBtn.tr(),
            confirmText = K.logout.tr(),
            confirmColor = Color(0xFFBA1A1A),
            onDismissRequest = { showSignOutConfirm = false },
            onConfirm = {
                showSignOutConfirm = false
                showSignOutLoading = true
                scope.launch {
                    try {
                        AuthManager.logout()
                    } catch (_: Exception) {}
                    delay(500)
                    showSignOutLoading = false
                    ElvanSnackbar.show(signOutSuccessMsg)
                }
            },
            colors = colors
        )
    }

    if (showSyncLoading) {
        ElvanLoadingOverlay(
            text = K.syncing.tr(),
            colors = colors
        )
    }

    if (showSignOutLoading) {
        ElvanLoadingOverlay(
            text = K.signingOut.tr(),
            colors = colors
        )
    }
}

// Reading the tick makes the row follow the switch
private fun lockLabelOn(tick: Int): Boolean = tick >= 0 && AppLock.isEnabled()
