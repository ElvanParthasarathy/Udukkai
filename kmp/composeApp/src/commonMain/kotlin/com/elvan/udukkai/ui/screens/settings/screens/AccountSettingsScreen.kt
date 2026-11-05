package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.core.auth.AuthManager
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.settings.SettingsRoute

/**
 * பயனர் கணக்கு அமைப்புகள் — User Account Settings Screen.
 * Displays profile overview, linked accounts management, password settings, and sign-out.
 */
@Composable
fun AccountSettingsScreen(
    onNavigate: (SettingsRoute) -> Unit = {},
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val email = AuthManager.signedInEmail ?: ""
    val displayName = AuthManager.signedInDisplayName?.takeIf { it.isNotEmpty() } ?: email.substringBefore("@")
    val hasPassword = AuthManager.hasPassword

    var showSignOutConfirm by remember { mutableStateOf(false) }

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

            // ── User Profile Pill Card ──
            item(key = "profile_pill") {
                ElvanProfilePillCard(
                    title = displayName.ifEmpty { K.userAccount.tr() },
                    subtitle = email.ifEmpty { K.connected.tr() },
                    icon = MaterialSymbols.Rounded.Person,
                    onClick = {},
                    colors = colors
                )
            }

            // ── Account Management Actions ──
            item(key = "account_actions") {
                ElvanSettingsSection(colors = colors) {
                    // 1. Linked Accounts
                    ElvanSettingsRow(
                        icon = MaterialSymbols.Rounded.Link,
                        title = K.linkedAccounts.tr(),
                        description = K.linkedAccountsDesc.tr(),
                        onClick = { onNavigate(SettingsRoute.LinkedAccounts) },
                        colors = colors
                    )

                    ElvanSettingsDivider(colors = colors)

                    // 2. Password (Change or Create)
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

                    ElvanSettingsDivider(colors = colors)

                    // 3. Sign Out
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

            // ── Danger Zone: Delete Account ──
            item(key = "danger_zone") {
                ElvanSettingsSection(colors = colors) {
                    ElvanSettingsRow(
                        icon = MaterialSymbols.Rounded.Delete,
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
