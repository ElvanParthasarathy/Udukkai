package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.core.auth.AuthManager
import com.elvan.udukkai.core.backup.reloadAllRepositories
import com.elvan.udukkai.core.sync.SyncStatus
import com.elvan.udukkai.core.sync.getFirebaseSyncManager
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.settings.AccountAvatar
import com.elvan.udukkai.ui.screens.settings.SettingsRoute
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * பயனர் கணக்கும் பாதுகாப்பும் — Account & Security Settings Screen.
 * Unified screen containing profile overview, linked accounts, password management,
 * cloud sync, sign-out, and account deletion with consistent One UI styling.
 */
@Composable
fun AccountSettingsScreen(
    onNavigate: (SettingsRoute) -> Unit = {},
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val scope = rememberCoroutineScope()
    val email = AuthManager.signedInEmail ?: ""
    val displayName = AuthManager.signedInDisplayName?.takeIf { it.isNotEmpty() } ?: email.substringBefore("@")
    val hasPassword = AuthManager.hasPassword

    val sync = remember { getFirebaseSyncManager() }
    val syncStatus by sync.syncStatus
    val lastSync by sync.lastSyncTime

    var showSignOutConfirm by remember { mutableStateOf(false) }
    var isSyncing by remember { mutableStateOf(false) }

    val syncSuccessMsg = K.synced.tr()

    val syncStatusDesc = when (syncStatus) {
        SyncStatus.SYNCED -> if (lastSync > 0L) "${K.lastSyncedAt.tr()}: ${DateUtils.formatDateTime(lastSync)}" else K.syncStatusSynced.tr()
        SyncStatus.SYNCING -> K.syncStatusSyncing.tr()
        SyncStatus.OFFLINE -> K.syncStatusOffline.tr()
        SyncStatus.ERROR, SyncStatus.GUARD_TRIGGERED -> K.syncStatusError.tr()
        SyncStatus.IDLE -> if (lastSync > 0L) "${K.lastSyncedAt.tr()}: ${DateUtils.formatDateTime(lastSync)}" else K.syncStatusIdle.tr()
    }

    Box(modifier = Modifier.fillMaxSize()) {
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

            // ── 1. User Profile Pill Card ──
            item(key = "profile_pill") {
                ElvanProfilePillCard(
                    title = displayName.ifEmpty { K.userAccount.tr() },
                    subtitle = email.ifEmpty { K.connected.tr() },
                    iconWidget = {
                        AccountAvatar(fallback = {
                            Icon(
                                imageVector = MaterialSymbols.Rounded.Person,
                                contentDescription = null,
                                tint = colors.textPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        })
                    },
                    onClick = {},
                    colors = colors
                )
            }

            // ── 2. Account Credentials Section (Linked Accounts & Password) ──
            item(key = "account_credentials") {
                ElvanSectionContainer {
                    ElvanSettingsSection(colors = colors) {
                        // Linked Accounts
                        ElvanSettingsRow(
                            icon = MaterialSymbols.Rounded.Link,
                            title = K.linkedAccounts.tr(),
                            description = if (AuthManager.isGoogleLinked) K.connected.tr() else K.notConnected.tr(),
                            onClick = { onNavigate(SettingsRoute.LinkedAccounts) },
                            colors = colors
                        )

                        ElvanSettingsDivider(colors = colors)

                        // Password (Change or Create)
                        ElvanSettingsRow(
                            icon = MaterialSymbols.Rounded.Key,
                            title = if (hasPassword) K.changePassword.tr() else K.createPassword.tr(),
                            description = if (hasPassword) K.passwordSet.tr() else K.noPasswordSet.tr(),
                            onClick = {
                                if (hasPassword) {
                                    onNavigate(SettingsRoute.ChangePassword)
                                } else {
                                    onNavigate(SettingsRoute.CreatePassword)
                                }
                            },
                            colors = colors
                        )
                    }
                }
            }

            // ── 3. Cloud Sync Section ──
            item(key = "cloud_sync") {
                ElvanSectionContainer {
                    ElvanSettingsSection(colors = colors) {
                        ElvanSettingsRow(
                            icon = MaterialSymbols.Rounded.Sync,
                            title = K.syncAppBtn.tr(),
                            description = if (isSyncing) K.syncStatusSyncing.tr() else syncStatusDesc,
                            onClick = {
                                isSyncing = true
                                scope.launch {
                                    try {
                                        reloadAllRepositories()
                                    } catch (_: Exception) {}
                                    try {
                                        sync.triggerManualSync()
                                    } catch (_: Exception) {}
                                    delay(600)
                                    isSyncing = false
                                    ElvanSnackbar.show(syncSuccessMsg)
                                }
                            },
                            colors = colors
                        )
                    }
                }
            }

            // ── 4. Session Section (Sign Out) ──
            item(key = "session_section") {
                ElvanSectionContainer {
                    ElvanSettingsSection(colors = colors) {
                        ElvanSettingsRow(
                            icon = MaterialSymbols.Rounded.Logout,
                            title = K.logout.tr(),
                            description = K.logoutApplication.tr(),
                            onClick = { showSignOutConfirm = true },
                            titleColor = colors.error,
                            iconTint = colors.error,
                            colors = colors
                        )
                    }
                }
            }

            // ── 5. Danger Zone: Delete Account ──
            item(key = "danger_zone") {
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
                onDismissRequest = { showSignOutConfirm = false },
                onConfirm = {
                    showSignOutConfirm = false
                    AuthManager.logout()
                },
                colors = colors,
                confirmColor = colors.error
            )
        }
    }
}
