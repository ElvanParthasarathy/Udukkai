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
import com.elvan.udukkai.ui.screens.settings.SettingsRoute
import kotlinx.coroutines.launch

/**
 * கணக்கு மற்றும் பாதுகாப்பு அமைப்புகள் — Account & Security Hub.
 * - Personal Info: Display name with in-place animated edit container (matching Merchant settings).
 * - Security & Sign-in: Google account, Password, Sign out with clean ActionSheet.
 * - Account Management: Step-by-step account deletion sub-page.
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

    var isEditingName by remember { mutableStateOf(false) }
    var tempDisplayName by remember { mutableStateOf(displayName) }
    var isSavingName by remember { mutableStateOf(false) }
    var showSignOutConfirm by remember { mutableStateOf(false) }

    val nameSavedMsg = K.nameSaved.tr()

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

            // ── 1. Personal Info Section (தனிநபர் தரவுகள்) ──
            item(key = "personal_info_section") {
                ElvanSectionContainer {
                    ElvanSettingsSection(
                        title = K.personalInfo.tr(),
                        colors = colors
                    ) {
                        // Display Name with in-place expand & edit (matching Merchant name edit)
                        ElvanSettingsAnimatedExpand(
                            isEditing = isEditingName,
                            displayContent = {
                                ElvanSettingsDisplayRow(
                                    title = K.displayName.tr(),
                                    primaryValue = displayName.ifEmpty { K.userAccount.tr() },
                                    onEdit = {
                                        tempDisplayName = displayName
                                        isEditingName = true
                                    },
                                    colors = colors
                                )
                            },
                            editContent = {
                                ElvanSettingsEditContainer(
                                    title = K.displayName.tr(),
                                    onCancel = { isEditingName = false },
                                    onSave = {
                                        if (!isSavingName && tempDisplayName.isNotBlank()) {
                                            isSavingName = true
                                            scope.launch {
                                                val res = AuthManager.updateDisplayName(tempDisplayName.trim())
                                                isSavingName = false
                                                if (res.isSuccess) {
                                                    isEditingName = false
                                                    ElvanSnackbar.show(nameSavedMsg)
                                                }
                                            }
                                        }
                                    },
                                    colors = colors
                                ) {
                                    ElvanSettingsTextField(
                                        label = K.displayName.tr(),
                                        value = tempDisplayName,
                                        onValueChange = { tempDisplayName = it },
                                        placeholder = K.enterDisplayName.tr(),
                                        colors = colors
                                    )
                                }
                            }
                        )

                        ElvanSettingsDivider(colors = colors)

                        // Email Address
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

                        // Sign Out (Direct action sheet without redundant screens)
                        ElvanSettingsRow(
                            icon = MaterialSymbols.Rounded.Logout,
                            title = K.logout.tr(),
                            description = K.logoutApplication.tr(),
                            onClick = { showSignOutConfirm = true },
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
                        // Delete Account (Opens Step-by-Step Sub-page)
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
