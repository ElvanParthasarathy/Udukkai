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
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.settings.AccountAvatar
import com.elvan.udukkai.ui.screens.settings.SettingsRoute

/**
 * கணக்கு மற்றும் பாதுகாப்பு அமைப்புகள் — Account & Security Hub.
 * Calm, minimal layout inspired by Google Account & Apple HIG:
 * 1. Personal Info: Display name (with edit sub-page) & Email.
 * 2. Security & Sign-in: Google account, Password, Sign out sub-pages.
 * 3. Account Management: Step-by-step account deletion sub-page.
 * All items navigate to sub-pages with clean neutral styling (no jarring red).
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
    val isGoogleLinked = AuthManager.isGoogleLinked

    LazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = Dimens.SubpageContentPaddingBottom),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
    ) {
        item(key = "shell_top_spacer") {
            Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
        }

        // ── 1. Personal Info Section (தனிநபர் தரவுகள்) ──
        item(key = "personal_info_section") {
            ElvanSectionContainer {
                ElvanSettingsSection(
                    title = K.personalInfo.tr(),
                    colors = colors
                ) {
                    // Display Name (opens EditName sub-page)
                    ElvanSettingsRow(
                        icon = MaterialSymbols.Rounded.Person,
                        title = K.displayName.tr(),
                        description = displayName.ifEmpty { K.userAccount.tr() },
                        onClick = { onNavigate(SettingsRoute.EditName) },
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

                    // Email Address (Informational)
                    ElvanSettingsRow(
                        icon = MaterialSymbols.Rounded.Email,
                        title = K.email.tr(),
                        description = email.ifEmpty { K.notConnected.tr() },
                        onClick = {},
                        customTrailing = {
                            Icon(
                                imageVector = MaterialSymbols.Rounded.CheckCircle,
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

        // ── 2. Security & Sign-in Section (பாதுகாப்பும் உள்நுழைவும்) ──
        item(key = "security_signin_section") {
            ElvanSectionContainer {
                ElvanSettingsSection(
                    title = K.securityAndSignIn.tr(),
                    colors = colors
                ) {
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

                    // Password Subpage
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

                    // Sign Out Subpage (Standard neutral row)
                    ElvanSettingsRow(
                        icon = MaterialSymbols.Rounded.Logout,
                        title = K.logout.tr(),
                        description = K.logoutApplication.tr(),
                        onClick = { onNavigate(SettingsRoute.SignOut) },
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

        // ── 3. Account Management Section (கணக்கு மேலாண்மை) ──
        item(key = "account_management_section") {
            ElvanSectionContainer {
                ElvanSettingsSection(
                    title = K.accountManagement.tr(),
                    colors = colors
                ) {
                    // Delete Account Subpage (Standard neutral row)
                    ElvanSettingsRow(
                        icon = MaterialSymbols.Rounded.DeleteForever,
                        title = K.deleteAccount.tr(),
                        description = K.deleteAccountDesc.tr(),
                        onClick = { onNavigate(SettingsRoute.DeleteAccount) },
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
    }
}
