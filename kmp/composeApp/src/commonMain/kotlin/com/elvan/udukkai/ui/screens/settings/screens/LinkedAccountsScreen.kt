package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.auth.AuthManager
import com.elvan.udukkai.core.auth.GoogleSignInResult
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.ExpressiveLoadingIndicator
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.settings.SettingsRoute
import kotlinx.coroutines.launch

/**
 * இணைக்கப்பட்ட கணக்குகள் திரை — Linked Accounts Screen.
 * Apple HIG & Samsung One UI compliant credential manager:
 * - Google OAuth linking/unlinking with ElvanActionSheet confirmation modal.
 * - Primary email and password status management.
 * - Safety check preventing account lockout if Google is unlinked without an established password.
 */
@Composable
fun LinkedAccountsScreen(
    onNavigate: (SettingsRoute) -> Unit = {},
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current
    val scope = rememberCoroutineScope()

    val isGoogleLinked = AuthManager.isGoogleLinked
    val googleEmail = AuthManager.googleEmail ?: AuthManager.signedInEmail ?: ""
    val hasPassword = AuthManager.hasPassword
    val primaryEmail = AuthManager.signedInEmail ?: ""

    var showUnlinkSheet by remember { mutableStateOf(false) }
    var isLinking by remember { mutableStateOf(false) }
    var isUnlinking by remember { mutableStateOf(false) }

    val unlinkedSuccessMsg = K.googleAccountUnlinked.tr()
    val failedUnlinkMsg = K.failedToUnlink.tr()
    val connectedMsg = K.connected.tr()

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

            // ── 1. Google OAuth Provider Card ──
            item(key = "google_section") {
                ElvanSectionContainer {
                    ElvanSettingsSection(colors = colors) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Google Icon Badge
                                Surface(
                                    shape = CircleShape,
                                    color = colors.iconBg,
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "G",
                                            style = TextStyle(
                                                fontFamily = ff,
                                                fontSize = 19.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = colors.textPrimary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = K.googleAccount.tr(),
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = colors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isGoogleLinked) googleEmail else K.notConnected.tr(),
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Normal
                                        ),
                                        color = colors.textPrimary.copy(alpha = 0.55f),
                                        maxLines = 1
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Status Pill Badge
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isGoogleLinked) colors.accent.copy(alpha = 0.15f) else colors.iconBg
                                ) {
                                    Text(
                                        text = if (isGoogleLinked) K.connected.tr() else K.notConnected.tr(),
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = if (isGoogleLinked) colors.accent else colors.textPrimary.copy(alpha = 0.6f),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            ElvanSettingsDivider(indent = 0.dp, endIndent = 0.dp, colors = colors)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Link / Unlink Action Row
                            if (isGoogleLinked) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showUnlinkSheet = true },
                                    color = Color.Transparent
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isUnlinking) {
                                            ExpressiveLoadingIndicator(color = colors.error, modifier = Modifier.size(18.dp))
                                        } else {
                                            Text(
                                                text = K.unlinkGoogle.tr(),
                                                style = TextStyle(
                                                    fontFamily = ff,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                color = colors.error
                                            )
                                        }
                                    }
                                }
                            } else {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (!isLinking) {
                                                isLinking = true
                                                scope.launch {
                                                    val res = AuthManager.linkGoogle()
                                                    isLinking = false
                                                    if (res is GoogleSignInResult.Success) {
                                                        ElvanSnackbar.show(connectedMsg)
                                                    }
                                                }
                                            }
                                        },
                                    color = Color.Transparent
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isLinking) {
                                            ExpressiveLoadingIndicator(color = colors.accent, modifier = Modifier.size(18.dp))
                                        } else {
                                            Text(
                                                text = K.linkGoogle.tr(),
                                                style = TextStyle(
                                                    fontFamily = ff,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                color = colors.accent
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── 2. Primary Email & Password Section ──
            item(key = "email_password_section") {
                ElvanSectionContainer {
                    ElvanSettingsSection(colors = colors) {
                        // Email Row
                        ElvanSettingsRow(
                            icon = MaterialSymbols.Rounded.Email,
                            title = K.email.tr(),
                            description = primaryEmail,
                            onClick = {},
                            customTrailing = {
                                Icon(
                                    imageVector = MaterialSymbols.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
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
                            customTrailing = {
                                if (hasPassword) {
                                    Icon(
                                        imageVector = MaterialSymbols.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = colors.accent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = colors.accent.copy(alpha = 0.12f),
                                        modifier = Modifier.clickable { onNavigate(SettingsRoute.CreatePassword) }
                                    ) {
                                        Text(
                                            text = K.createPassword.tr(),
                                            style = TextStyle(
                                                fontFamily = ff,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = colors.accent,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            },
                            colors = colors
                        )
                    }
                }
            }
        }

        // ── Unlink Confirmation Action Sheet (Replaces Material 3 AlertDialog) ──
        if (showUnlinkSheet) {
            if (hasPassword) {
                ElvanActionSheet(
                    title = K.unlinkConfirm.tr(),
                    cancelText = K.cancelBtn.tr(),
                    confirmText = K.unlinkGoogle.tr(),
                    confirmColor = colors.error,
                    onDismissRequest = { showUnlinkSheet = false },
                    onConfirm = {
                        showUnlinkSheet = false
                        isUnlinking = true
                        scope.launch {
                            val ok = AuthManager.unlinkGoogle()
                            isUnlinking = false
                            if (ok) {
                                ElvanSnackbar.show(unlinkedSuccessMsg)
                            } else {
                                ElvanSnackbar.show(failedUnlinkMsg)
                            }
                        }
                    },
                    colors = colors,
                    customContent = {
                        Text(
                            text = K.unlinkMessage.tr(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 13.5.sp,
                                lineHeight = 19.sp
                            ),
                            color = colors.textPrimary.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                )
            } else {
                // Safety Guard: Cannot unlink Google without creating a password first
                ElvanActionSheet(
                    title = K.createPasswordFirst.tr(),
                    cancelText = K.cancelBtn.tr(),
                    confirmText = K.createPassword.tr(),
                    confirmColor = colors.accent,
                    onDismissRequest = { showUnlinkSheet = false },
                    onConfirm = {
                        showUnlinkSheet = false
                        onNavigate(SettingsRoute.CreatePassword)
                    },
                    colors = colors,
                    customContent = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = K.createPasswordMsg.tr(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 13.5.sp,
                                    lineHeight = 19.sp
                                ),
                                color = colors.textPrimary.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                )
            }
        }
    }
}
