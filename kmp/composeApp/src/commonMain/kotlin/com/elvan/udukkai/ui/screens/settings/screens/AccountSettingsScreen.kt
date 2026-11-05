package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.auth.AuthManager
import com.elvan.udukkai.core.backup.reloadAllRepositories
import com.elvan.udukkai.core.sync.SyncStatus
import com.elvan.udukkai.core.sync.getFirebaseSyncManager
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.ExpressiveLoadingIndicator
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.settings.AccountAvatar
import com.elvan.udukkai.ui.screens.settings.SettingsRoute
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * பயனர் கணக்கும் பாதுகாப்பும் — Account & Security Settings Screen.
 * Complete redesign grounded in Apple Human Interface Guidelines (managing-accounts.md, action-sheets.md)
 * and Samsung One UI standards:
 * - Prominent Hero Account Card with avatar, name, email, and authentication badge.
 * - Grouped sections: Sign-in Methods, Cloud Sync, and Session / Account Actions.
 * - Intentional destructive actions handled via ElvanActionSheet bottom confirmation sheet.
 * - Strict Pure Senthamizh localization with zero Sanskrit words.
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

            // ── 1. Hero Account Card (Apple HIG Managing Accounts & One UI Header Card) ──
            item(key = "hero_account_card") {
                ElvanSectionContainer {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        color = colors.surface,
                        shadowElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular Avatar
                            Box(
                                modifier = Modifier
                                    .size(58.dp)
                                    .clip(CircleShape)
                                    .background(colors.iconBg),
                                contentAlignment = Alignment.Center
                            ) {
                                AccountAvatar(fallback = {
                                    Icon(
                                        imageVector = MaterialSymbols.Rounded.Person,
                                        contentDescription = null,
                                        tint = colors.textPrimary,
                                        modifier = Modifier.size(30.dp)
                                    )
                                })
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Name, Email, and Auth Badge
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = displayName.ifEmpty { K.userAccount.tr() }.preventBrokenLigatures(),
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.textPrimary
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (email.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = email,
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = colors.textPrimary.copy(alpha = 0.55f)
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Auth Method Chip
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = colors.iconBg
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isGoogleLinked) MaterialSymbols.Rounded.Person else MaterialSymbols.Rounded.Email,
                                            contentDescription = null,
                                            tint = colors.accent,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = if (isGoogleLinked) K.googleAccount.tr() else K.emailAccount.tr(),
                                            style = TextStyle(
                                                fontFamily = ff,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            color = colors.textPrimary.copy(alpha = 0.75f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── 2. Sign-in Methods Section (Grouped Lists & Tables HIG) ──
            item(key = "signin_methods_section") {
                ElvanSectionContainer {
                    ElvanSettingsSection(
                        title = K.signInMethods.tr(),
                        colors = colors
                    ) {
                        // Linked Accounts Row
                        ElvanSettingsRow(
                            icon = MaterialSymbols.Rounded.Link,
                            title = K.linkedAccounts.tr(),
                            description = if (isGoogleLinked) K.connected.tr() else K.notConnected.tr(),
                            onClick = { onNavigate(SettingsRoute.LinkedAccounts) },
                            customTrailing = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isGoogleLinked) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = colors.accent.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = K.connected.tr(),
                                                style = TextStyle(
                                                    fontFamily = ff,
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                color = colors.accent,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Icon(
                                        imageVector = MaterialSymbols.Rounded.ChevronRight,
                                        contentDescription = null,
                                        tint = colors.textTertiary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            colors = colors
                        )

                        ElvanSettingsDivider(colors = colors)

                        // Password Row (Change or Create)
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
                    }
                }
            }

            // ── 3. Cloud Sync Section ──
            item(key = "cloud_sync_section") {
                ElvanSectionContainer {
                    ElvanSettingsSection(
                        title = K.cloudSync.tr(),
                        colors = colors
                    ) {
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
                                    ExpressiveLoadingIndicator(color = colors.accent, modifier = Modifier.size(20.dp))
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

            // ── 4. Session & Account Actions (Danger Zone) ──
            item(key = "session_danger_section") {
                ElvanSectionContainer {
                    ElvanSettingsSection(
                        title = K.session.tr(),
                        colors = colors
                    ) {
                        // Sign Out Row (Destructive styling per Apple HIG)
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

                        ElvanSettingsDivider(colors = colors)

                        // Delete Account Row (Destructive styling per Apple HIG & Google Play mandate)
                        ElvanSettingsRow(
                            icon = MaterialSymbols.Rounded.DeleteForever,
                            title = K.deleteAccount.tr(),
                            description = K.deleteAccountDesc.tr(),
                            onClick = { onNavigate(SettingsRoute.DeleteAccount) },
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
        }

        // ── Sign Out Confirmation Action Sheet (Apple HIG Action Sheets) ──
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
