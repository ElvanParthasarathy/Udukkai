package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.auth.AuthManager
import com.elvan.udukkai.core.backup.reloadAllRepositories
import com.elvan.udukkai.core.security.authenticateFingerprint
import com.elvan.udukkai.core.security.fingerprintAvailable
import com.elvan.udukkai.core.sync.SyncStatus
import com.elvan.udukkai.core.sync.getFirebaseSyncManager
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.ExpressiveLoadingIndicator
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.settings.AccountAvatar
import com.elvan.udukkai.ui.screens.settings.SettingsRoute
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * கணக்கு திரை — Account Hub Settings Screen.
 * Minimal, spacious navigation hub coordinating dedicated sub-settings:
 * - Profile Pill Card (ElvanProfilePillCard) matching Settings Hub.
 * - Card 1: Sub-settings navigation for Google Account, Password Studio, and Cloud Sync.
 * - Card 2: Session management (Sign Out with ElvanActionSheet).
 * - Card 3: Biometric-guarded Danger Zone for Account Deletion.
 */
@Composable
fun AccountSettingsScreen(
    onNavigate: (SettingsRoute) -> Unit = {},
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current
    val scope = rememberCoroutineScope()
    val email = AuthManager.signedInEmail ?: ""
    val displayName = AuthManager.signedInDisplayName?.takeIf { it.isNotEmpty() } ?: email.substringBefore("@")
    val hasPassword = AuthManager.hasPassword
    val isGoogleLinked = AuthManager.isGoogleLinked

    val sync = remember { getFirebaseSyncManager() }
    val syncStatus by sync.syncStatus
    val lastSync by sync.lastSyncTime

    var showSignOutConfirm by remember { mutableStateOf(false) }
    var isSyncing by remember { mutableStateOf(false) }

    val syncSuccessMsg = K.synced.tr()
    val biometricPromptTitle = K.biometricVerifyTitle.tr()
    val cancelText = K.cancelBtn.tr()

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
            contentPadding = PaddingValues(bottom = Dimens.SubpageContentPaddingBottom),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
        ) {
            item(key = "shell_top_spacer") {
                Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
            }

            // ── 1. Top Profile Pill Card ──
            item(key = "profile_pill") {
                ElvanSectionContainer {
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
            }

            // ── 2. Credentials & Cloud Sync Sub-Settings Card ──
            item(key = "credentials_sync_card") {
                ElvanSectionContainer {
                    ElvanSettingsSection(colors = colors) {
                        // Google Account Subpage
                        ElvanSettingsRow(
                            icon = MaterialSymbols.Rounded.Link,
                            title = K.googleAccount.tr(),
                            description = if (isGoogleLinked) email.ifEmpty { K.connected.tr() } else K.notConnected.tr(),
                            onClick = { onNavigate(SettingsRoute.LinkedAccounts) },
                            customTrailing = {
                                Icon(
                                    imageVector = MaterialSymbols.Rounded.ChevronRight,
                                    contentDescription = null,
                                    tint = colors.textTertiary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = colors
                        )

                        ElvanSettingsDivider(colors = colors)

                        // Password Studio Subpage
                        ElvanSettingsRow(
                            icon = MaterialSymbols.Rounded.Key,
                            title = K.password.tr(),
                            description = if (hasPassword) K.passwordSet.tr() else K.noPasswordSet.tr(),
                            onClick = {
                                if (hasPassword) {
                                    onNavigate(SettingsRoute.ChangePassword)
                                } else {
                                    onNavigate(SettingsRoute.CreatePassword)
                                }
                            },
                            customTrailing = {
                                Icon(
                                    imageVector = MaterialSymbols.Rounded.ChevronRight,
                                    contentDescription = null,
                                    tint = colors.textTertiary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = colors
                        )

                        ElvanSettingsDivider(colors = colors)

                        // Instant Cloud Sync
                        ElvanSettingsRow(
                            icon = MaterialSymbols.Rounded.Sync,
                            title = K.syncAppBtn.tr(),
                            description = if (isSyncing) K.syncStatusSyncing.tr() else syncStatusDesc,
                            onClick = {
                                if (!isSyncing) {
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
                                }
                            },
                            customTrailing = {
                                if (isSyncing) {
                                    ExpressiveLoadingIndicator(color = colors.accent, modifier = Modifier.size(18.dp))
                                } else {
                                    Icon(
                                        imageVector = MaterialSymbols.Rounded.Sync,
                                        contentDescription = null,
                                        tint = colors.textTertiary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            colors = colors
                        )
                    }
                }
            }

            // ── 3. Session Section ──
            item(key = "session_card") {
                ElvanSectionContainer {
                    ElvanSettingsSection(colors = colors) {
                        ElvanSettingsRow(
                            icon = MaterialSymbols.Rounded.Logout,
                            title = K.logout.tr(),
                            description = K.logoutApplication.tr(),
                            onClick = { showSignOutConfirm = true },
                            titleColor = colors.error,
                            iconTint = colors.error,
                            customTrailing = {
                                Icon(
                                    imageVector = MaterialSymbols.Rounded.ChevronRight,
                                    contentDescription = null,
                                    tint = colors.error.copy(alpha = 0.5f),
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = colors
                        )
                    }
                }
            }

            // ── 4. Danger Zone (Protected by Native Biometric Lock) ──
            item(key = "danger_card") {
                ElvanSectionContainer {
                    ElvanSettingsSection(colors = colors) {
                        ElvanSettingsRow(
                            icon = MaterialSymbols.Rounded.DeleteForever,
                            title = K.deleteAccount.tr(),
                            description = K.deleteAccountDesc.tr(),
                            onClick = {
                                if (fingerprintAvailable()) {
                                    authenticateFingerprint(
                                        title = biometricPromptTitle,
                                        cancelText = cancelText
                                    ) { success ->
                                        if (success) {
                                            onNavigate(SettingsRoute.DeleteAccount)
                                        }
                                    }
                                } else {
                                    onNavigate(SettingsRoute.DeleteAccount)
                                }
                            },
                            titleColor = colors.error,
                            iconTint = colors.error,
                            customTrailing = {
                                Icon(
                                    imageVector = MaterialSymbols.Rounded.Lock,
                                    contentDescription = null,
                                    tint = colors.error.copy(alpha = 0.5f),
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = colors
                        )
                    }
                }
            }
        }

        // ── Sign Out Action Sheet ──
        if (showSignOutConfirm) {
            ElvanActionSheet(
                title = K.signOutConfirm.tr(),
                cancelText = K.cancelBtn.tr(),
                confirmText = K.logout.tr(),
                confirmColor = colors.error,
                onDismissRequest = { showSignOutConfirm = false },
                onConfirm = {
                    showSignOutConfirm = false
                    AuthManager.logout()
                },
                colors = colors,
                customContent = {
                    Text(
                        text = K.signOutDesc.tr(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 13.5.sp,
                            lineHeight = 18.sp
                        ),
                        color = colors.textPrimary.copy(alpha = 0.65f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            )
        }
    }
}
