package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.auth.AuthManager
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.settings.AccountAvatar
import com.elvan.udukkai.ui.screens.settings.SettingsRoute

/**
 * கணக்கு அமைப்புகள் — Account Settings Screen.
 * Layout mirrors Neram Mobile's Account Settings:
 * - Top Profile Pill Card with avatar, name, and email.
 * - Card 1 (Personal Info): Name edit (navigates to EditName subpage), Email (no checkmark).
 * - Card 2 (Security & Sign-in): Google link ("கூகுள் இணைப்பு"), Password status, Sign Out with clean ActionSheet.
 * - Card 3 (Danger Zone): Delete account step-by-step wizard.
 * All section titles above cards are removed for a clean, minimalist UI.
 */
@Composable
fun AccountSettingsScreen(
    onNavigate: (SettingsRoute) -> Unit = {},
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current

    val email = AuthManager.signedInEmail ?: ""
    val displayName = AuthManager.signedInDisplayName?.takeIf { it.isNotEmpty() } ?: email.substringBefore("@")
    val hasPassword = AuthManager.hasPassword
    val isGoogleLinked = AuthManager.isGoogleLinked

    var showSignOutConfirm by remember { mutableStateOf(false) }

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

            // ── Top Profile Pill Card (Matching Neram) ──
            item(key = "profile_pill_header") {
                ElvanSectionContainer {
                    ElvanProfilePillCard(
                        title = displayName.ifEmpty { K.userAccount.tr() },
                        subtitle = email.ifEmpty { K.notConnected.tr() },
                        onClick = { onNavigate(SettingsRoute.EditName) },
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
                        trailing = {
                            Icon(
                                imageVector = MaterialSymbols.Rounded.ChevronRight,
                                contentDescription = null,
                                tint = colors.textTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        colors = colors
                    )
                }
            }

            // ── Card 1: Personal Profile Rows (No title above card) ──
            item(key = "personal_profile_card") {
                ElvanSectionContainer {
                    ElvanSettingsSection(colors = colors) {
                        // Display Name Subpage Navigation
                        ElvanSettingsRow(
                            icon = MaterialSymbols.Rounded.Person,
                            title = K.displayName.tr(),
                            description = displayName.ifEmpty { K.userAccount.tr() },
                            onClick = { onNavigate(SettingsRoute.EditName) },
                            colors = colors
                        )

                        ElvanSettingsDivider(colors = colors)

                        // Email Address Row (Clean, NO checkmark)
                        ElvanSettingsRow(
                            icon = MaterialSymbols.Rounded.Email,
                            title = K.email.tr(),
                            description = email.ifEmpty { K.notConnected.tr() },
                            onClick = {},
                            colors = colors
                        )
                    }
                }
            }

            // ── Card 2: Security & Sign-in Rows (No title above card) ──
            item(key = "security_signin_card") {
                ElvanSectionContainer {
                    ElvanSettingsSection(colors = colors) {
                        // Google Link Row ("கூகுள் இணைப்பு")
                        ElvanSettingsRow(
                            icon = MaterialSymbols.Rounded.Link,
                            title = K.googleLink.tr(),
                            description = if (isGoogleLinked) email.ifEmpty { K.connected.tr() } else K.notConnected.tr(),
                            onClick = { onNavigate(SettingsRoute.LinkedAccounts) },
                            colors = colors
                        )

                        ElvanSettingsDivider(colors = colors)

                        // Password Row
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
                            colors = colors
                        )

                        ElvanSettingsDivider(colors = colors)

                        // Sign Out Row (Tapping card directly asks confirmation via ActionSheet)
                        ElvanSettingsRow(
                            icon = MaterialSymbols.Rounded.Logout,
                            title = K.logout.tr(),
                            description = K.logoutApplication.tr(),
                            onClick = { showSignOutConfirm = true },
                            colors = colors
                        )
                    }
                }
            }

            // ── Card 3: Danger Zone / Delete Account (No title above card) ──
            item(key = "account_deletion_card") {
                ElvanSectionContainer {
                    ElvanSettingsSection(colors = colors) {
                        ElvanSettingsRow(
                            icon = MaterialSymbols.Rounded.DeleteForever,
                            title = K.deleteAccount.tr(),
                            description = K.deleteAccountDesc.tr(),
                            onClick = { onNavigate(SettingsRoute.DeleteAccount) },
                            colors = colors
                        )
                    }
                }
            }
        }

        // ── Clean Sign Out Action Sheet (No Redundant Email) ──
        if (showSignOutConfirm) {
            ElvanActionSheet(
                title = K.signOutConfirm.tr(),
                cancelText = K.cancelBtn.tr(),
                confirmText = K.logout.tr(),
                confirmColor = colors.textPrimary,
                onDismissRequest = { showSignOutConfirm = false },
                onConfirm = {
                    showSignOutConfirm = false
                    AuthManager.logout()
                },
                colors = colors,
                customContent = {
                    Text(
                        text = K.signOutPageDesc.tr(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        ),
                        color = colors.textPrimary.copy(alpha = 0.65f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            )
        }
    }
}
