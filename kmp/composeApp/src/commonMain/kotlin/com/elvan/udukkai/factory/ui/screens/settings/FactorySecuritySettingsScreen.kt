package com.elvan.udukkai.factory.ui.screens.settings

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

import com.elvan.udukkai.ui.screens.settings.SettingsRoute

/**
 * Factory Security Settings Screen (ஆலை பாதுகாப்பு அமைப்புகள்).
 * Dedicated to Udukkai Factory with zero cross-app dependencies.
 */
@Composable
fun FactorySecuritySettingsScreen(
    onNavigate: (SettingsRoute) -> Unit = {},
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val scope = rememberCoroutineScope()

    var showSyncLoading by remember { mutableStateOf(false) }
    var showSignOutConfirm by remember { mutableStateOf(false) }
    var showSignOutLoading by remember { mutableStateOf(false) }
    val guard = rememberPinGuard(colors)

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

        // Section 1: User Account & Sign-in Methods
        item(key = "account_credentials_section") {
            ElvanSectionContainer {
                ElvanSettingsSection(colors = colors) {
                    ElvanSettingsRow(
                        icon = MaterialSymbols.Rounded.Person,
                        title = K.userAccount.tr(),
                        description = AuthManager.signedInEmail?.ifEmpty { K.signInMethods.tr() } ?: K.signInMethods.tr(),
                        onClick = { onNavigate(SettingsRoute.Account) },
                        colors = colors
                    )
                    ElvanSettingsDivider(colors = colors)
                    ElvanSettingsRow(
                        icon = MaterialSymbols.Rounded.Link,
                        title = K.linkedAccounts.tr(),
                        description = if (AuthManager.isGoogleLinked) K.connected.tr() else K.notConnected.tr(),
                        onClick = { onNavigate(SettingsRoute.LinkedAccounts) },
                        colors = colors
                    )
                    ElvanSettingsDivider(colors = colors)
                    if (AuthManager.hasPassword) {
                        ElvanSettingsRow(
                            icon = MaterialSymbols.Rounded.Lock,
                            title = K.changePassword.tr(),
                            description = K.passwordSet.tr(),
                            onClick = { onNavigate(SettingsRoute.ChangePassword) },
                            colors = colors
                        )
                    } else {
                        ElvanSettingsRow(
                            icon = MaterialSymbols.Rounded.Lock,
                            title = K.createPassword.tr(),
                            description = K.noPasswordSet.tr(),
                            onClick = { onNavigate(SettingsRoute.CreatePassword) },
                            colors = colors
                        )
                    }
                }
            }
        }

        // Section 2: Sync & Session
        item(key = "sync_session_section") {
            ElvanSectionContainer {
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

        // Section 3: Danger Zone / Delete Account
        item(key = "danger_zone_section") {
            ElvanSectionContainer {
                ElvanSettingsSection(colors = colors) {
                    ElvanSettingsRow(
                        icon = MaterialSymbols.Rounded.DeleteForever,
                        title = K.deleteAccount.tr(),
                        description = K.deleteAccountDesc.tr(),
                        onClick = { onNavigate(SettingsRoute.DeleteAccount) },
                        titleColor = colors.error,
                        iconTint = colors.error,
                        colors = colors
                    )
                }
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

