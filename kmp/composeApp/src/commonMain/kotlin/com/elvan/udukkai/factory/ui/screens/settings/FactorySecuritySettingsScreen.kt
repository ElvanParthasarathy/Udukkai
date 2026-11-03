package com.elvan.udukkai.factory.ui.screens.settings

import com.elvan.udukkai.core.security.AppLock
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
    var pinSheet by remember { mutableStateOf<PinMode?>(null) }
    var pinTick by remember { mutableStateOf(0) }
    val bottomSheet = LocalElvanBottomSheetController.current
    val pinTitle = K.appPin.tr()
    val changeLabel = K.pinChange.tr()
    val removeLabel = K.pinRemove.tr()



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

                // App PIN: asked at opening and before the actions that can lose or give away data
                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.Lock,
                    title = pinTitle,
                    description = (if (pinTick >= 0 && AppLock.isSet()) K.pinOn else K.pinOff).tr() + "\n" + K.appPinHint.tr(),
                    onClick = {
                        if (!AppLock.isSet()) {
                            pinSheet = PinMode.CREATE
                        } else {
                            bottomSheet.showSelection(
                                title = pinTitle,
                                items = listOf("change", "remove"),
                                currentValue = null,
                                itemLabelBuilder = { if (it == "change") changeLabel else removeLabel },
                                onSelected = { item ->
                                    guard {
                                        if (item == "change") pinSheet = PinMode.CREATE else { AppLock.remove(); pinTick++ }
                                    }
                                }
                            )
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

    pinSheet?.let { mode -> PinSheet(mode, colors) { pinSheet = null; pinTick++ } }

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
