package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
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
 * கூகுள் கணக்குத் திரை — Dedicated Google Account Management Screen.
 * Rich visual hierarchy featuring:
 * - Dedicated Google Identity Hero Card with 64dp badge, email, and live status pill.
 * - Prominent full-width action buttons for explicit Linking / Unlinking.
 * - Credential overview and security safeguards via ElvanActionSheet.
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

            // ── 1. Hero Google Account Card with Real Action Buttons ──
            item(key = "google_hero_card") {
                ElvanSectionContainer {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        color = colors.surface,
                        shadowElevation = 0.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Large Google Circular Badge
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(colors.iconBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "G",
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = colors.textPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = K.googleAccount.tr(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = colors.textPrimary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (isGoogleLinked) googleEmail else K.notConnected.tr(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                color = colors.textPrimary.copy(alpha = 0.55f),
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Status Pill Badge
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isGoogleLinked) colors.accent.copy(alpha = 0.15f) else colors.iconBg
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isGoogleLinked) {
                                        Icon(
                                            imageVector = MaterialSymbols.Rounded.CheckCircle,
                                            contentDescription = null,
                                            tint = colors.accent,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = if (isGoogleLinked) K.connected.tr() else K.notConnected.tr(),
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = if (isGoogleLinked) colors.accent else colors.textPrimary.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Interactive Full-Width Button
                            if (isGoogleLinked) {
                                Button(
                                    onClick = { showUnlinkSheet = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.error,
                                        contentColor = Color.White
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(0.dp),
                                    enabled = !isUnlinking
                                ) {
                                    if (isUnlinking) {
                                        ExpressiveLoadingIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                    } else {
                                        Text(
                                            text = K.unlinkGoogle.tr(),
                                            style = TextStyle(
                                                fontFamily = ff,
                                                fontSize = 14.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            } else {
                                Button(
                                    onClick = {
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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.accent,
                                        contentColor = if (colors.isDark) Color.Black else Color.White
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(0.dp),
                                    enabled = !isLinking
                                ) {
                                    if (isLinking) {
                                        ExpressiveLoadingIndicator(
                                            color = if (colors.isDark) Color.Black else Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else {
                                        Text(
                                            text = K.linkGoogle.tr(),
                                            style = TextStyle(
                                                fontFamily = ff,
                                                fontSize = 14.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── 2. Credentials & Security Information ──
            item(key = "credentials_overview") {
                ElvanSectionContainer {
                    ElvanSettingsSection(colors = colors) {
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
                    }
                }
            }
        }

        // ── Unlink Confirmation Action Sheet ──
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
                        Text(
                            text = K.createPasswordMsg.tr(),
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
            }
        }
    }
}
