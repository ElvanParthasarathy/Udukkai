package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.ExpressiveContainedLoader
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.settings.AccountAvatar
import com.elvan.udukkai.ui.screens.settings.SettingsRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * கணக்கு அமைப்புகள் — Account Settings Screen (Matching Neram Mobile).
 * - A compact Display Name row opens the name editor without repeating the large account profile pill.
 * - Card 1 (Security & Sign-in): Google link ("கூகுள் இணைப்பு"), Password status, Sign Out with clean ActionSheet.
 * - Card 2 (Danger Zone): Delete account step-by-step wizard.
 * - Minimalist monochrome UI with expressive contained page loader and pure Tamil offline state.
 */
@Composable
fun AccountSettingsScreen(
    onBack: () -> Unit = {},
    onNavigate: (SettingsRoute) -> Unit = {},
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current

    val email = AuthManager.signedInEmail ?: ""
    val displayName = AuthManager.signedInDisplayName?.takeIf { it.isNotEmpty() } ?: email.substringBefore("@")
    val hasPassword = AuthManager.hasPassword
    val isGoogleLinked = AuthManager.isGoogleLinked

    var isCheckingStatus by remember { mutableStateOf(true) }
    var isOnline by remember { mutableStateOf(true) }
    var retryTrigger by remember { mutableStateOf(0) }
    var showSignOutConfirm by remember { mutableStateOf(false) }

    // Opening the account page also syncs it: the latest name and picture of the account
    LaunchedEffect(Unit) { AuthManager.refreshAccount() }

    LaunchedEffect(retryTrigger) {
        isCheckingStatus = true
        val online = withContext(Dispatchers.IO) {
            com.elvan.udukkai.core.platform.isNetworkAvailable()
        }
        delay(200)
        isOnline = online
        isCheckingStatus = false
    }

    ElvanSubShell(
        title = K.account.tr(),
        onBack = onBack,
        scrollState = scrollState
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isCheckingStatus) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ExpressiveContainedLoader(
                        modifier = Modifier.size(48.dp),
                        containerColor = colors.surface,
                        indicatorColor = colors.textPrimary
                    )
                }
            } else if (!isOnline) {
                com.elvan.udukkai.ui.components.shell.ElvanLazyColumn(
                    state = scrollState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = Dimens.SubpageContentPaddingBottom),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
                ) {
                    item(key = "shell_top_spacer") {
                        Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
                    }

                    item(key = "offline_card") {
                        ElvanSectionContainer {
                            ElvanSettingsSection(colors = colors) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = MaterialSymbols.Rounded.WifiOff,
                                        contentDescription = null,
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Text(
                                        text = K.noInternetConnection.tr(),
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            lineHeight = 22.sp
                                        ),
                                        color = colors.textPrimary,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = K.noInternetDesc.tr(),
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp
                                        ),
                                        color = colors.textSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = colors.textPrimary,
                                        modifier = Modifier.clickable { retryTrigger++ }
                                    ) {
                                        Text(
                                            text = K.retryBtn.tr(),
                                            color = colors.surface,
                                            style = TextStyle(
                                                fontFamily = ff,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                com.elvan.udukkai.ui.components.shell.ElvanLazyColumn(
                    state = scrollState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = Dimens.SubpageContentPaddingBottom),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
                ) {
                    item(key = "shell_top_spacer") {
                        Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
                    }

                    // Display name is an editable setting here; the large account identity stays on the hub.
                    item(key = "display_name") {
                        ElvanSectionContainer {
                            ElvanSettingsSection(colors = colors) {
                                ElvanSettingsRow(
                                    icon = MaterialSymbols.Rounded.Person,
                                    title = K.displayName.tr(),
                                    description = displayName.ifEmpty { K.userAccount.tr() },
                                    onClick = { onNavigate(SettingsRoute.EditName) },
                                    colors = colors
                                )
                            }
                        }
                    }

                    // ── Card 1: Security & Sign-in (No title above card) ──
                    item(key = "security_signin_card") {
                        ElvanSectionContainer {
                            ElvanSettingsSection(colors = colors) {
                                // Google Link Row ("கூகுள் இணைப்பு" -> opens LinkedAccounts subpage)
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

                    // ── Card 2: Danger Zone / Delete Account (No title above card) ──
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
}
