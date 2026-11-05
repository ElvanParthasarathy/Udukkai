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
import com.elvan.udukkai.ui.screens.settings.AccountAvatar
import com.elvan.udukkai.ui.screens.settings.SettingsRoute
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * கூகுள் இணைப்புத் திரை — Google Link Screen (Matching Neram Mobile 1:1).
 * - Removed redundant top header (no doubled avatar/email).
 * - Google card: Profile avatar / "G" badge, title "Google", green-tinted connected badge.
 * - Unlink action: Flat centered text row without button box.
 * - Email & Password card: Green checkmark icons for verified email and configured password.
 * - Internet-oriented loading experience.
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
    val primaryEmail = AuthManager.googleEmail ?: AuthManager.signedInEmail ?: ""
    val hasPassword = AuthManager.hasPassword

    var isCheckingStatus by remember { mutableStateOf(true) }
    var showUnlinkSheet by remember { mutableStateOf(false) }
    var isLinking by remember { mutableStateOf(false) }
    var isUnlinking by remember { mutableStateOf(false) }

    val unlinkedSuccessMsg = K.googleAccountUnlinked.tr()
    val failedUnlinkMsg = K.failedToUnlink.tr()
    val connectedMsg = K.connected.tr()

    // Internet-oriented loading experience on enter
    LaunchedEffect(Unit) {
        delay(250)
        isCheckingStatus = false
    }

    ElvanSubShell(
        title = K.googleLink.tr(),
        onBack = { onNavigate(SettingsRoute.Account) },
        scrollState = scrollState
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isCheckingStatus) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ExpressiveLoadingIndicator(color = colors.textPrimary, modifier = Modifier.size(28.dp))
                }
            } else {
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = Dimens.SubpageContentPaddingBottom),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
                ) {
                    item(key = "shell_top_spacer") {
                        Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
                    }

                    // ── Card 1: Google Account Box (Matching Neram 1:1) ──
                    item(key = "google_account_box") {
                        ElvanSectionContainer {
                            ElvanSettingsSection(colors = colors) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Google Profile Image or "G" Fallback Badge
                                    if (isGoogleLinked) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(colors.iconBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AccountAvatar(fallback = {
                                                Text(
                                                    text = "G",
                                                    style = TextStyle(
                                                        fontFamily = ff,
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = colors.textPrimary
                                                )
                                            })
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(colors.iconBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "G",
                                                style = TextStyle(
                                                    fontFamily = ff,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = colors.textPrimary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    // Title only: "Google" (email is shown in Email section below)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Google",
                                            style = TextStyle(
                                                fontFamily = ff,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            color = colors.textPrimary
                                        )
                                    }

                                    // Status Badge (Connected has green tint matching Neram)
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = if (isGoogleLinked) colors.success.copy(alpha = 0.15f) else colors.iconBg
                                    ) {
                                        Text(
                                            text = if (isGoogleLinked) K.connected.tr() else K.notConnected.tr(),
                                            style = TextStyle(fontFamily = ff, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold),
                                            color = if (isGoogleLinked) colors.success else colors.textPrimary.copy(alpha = 0.6f),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                // Action Row: Matching Neram (Centered flat text row, NO button box)
                                if (isGoogleLinked) {
                                    ElvanSettingsDivider(colors = colors)
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showUnlinkSheet = true },
                                        color = Color.Transparent
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (isUnlinking) {
                                                ExpressiveLoadingIndicator(color = colors.danger, modifier = Modifier.size(18.dp))
                                            } else {
                                                Text(
                                                    text = K.disconnectGoogle.tr(),
                                                    color = colors.danger,
                                                    style = TextStyle(
                                                        fontFamily = ff,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    ElvanSettingsDivider(colors = colors)
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
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (isLinking) {
                                                ExpressiveLoadingIndicator(color = colors.textPrimary, modifier = Modifier.size(18.dp))
                                            } else {
                                                Text(
                                                    text = K.linkGoogle.tr(),
                                                    color = colors.textPrimary,
                                                    style = TextStyle(
                                                        fontFamily = ff,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ── Card 2: Email & Password Section (Matching Neram with green checkmarks) ──
                    item(key = "email_password_section") {
                        ElvanSectionContainer {
                            ElvanSettingsSection(colors = colors) {
                                // Email Row with Green Tick Mark
                                ElvanSettingsRow(
                                    icon = MaterialSymbols.Rounded.Email,
                                    title = K.email.tr(),
                                    description = primaryEmail,
                                    onClick = {},
                                    customTrailing = {
                                        Icon(
                                            imageVector = MaterialSymbols.Rounded.CheckCircle,
                                            contentDescription = null,
                                            tint = colors.success,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    colors = colors
                                )

                                ElvanSettingsDivider(colors = colors)

                                // Password Row with Green Tick Mark (if set)
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
                                                tint = colors.success,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        } else {
                                            Surface(
                                                shape = RoundedCornerShape(20.dp),
                                                color = colors.iconBg,
                                                modifier = Modifier.clickable { onNavigate(SettingsRoute.CreatePassword) }
                                            ) {
                                                Text(
                                                    text = K.createBtn.tr(),
                                                    style = TextStyle(fontFamily = ff, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold),
                                                    color = colors.textPrimary,
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

                    // ── Bottom Status Hint ──
                    item(key = "status_hint") {
                        ElvanSectionContainer {
                            Text(
                                text = if (isGoogleLinked) K.googleLinkedDesc.tr() else K.googleUnlinkedDesc.tr(),
                                style = TextStyle(fontFamily = ff, fontSize = 12.sp, lineHeight = 16.sp),
                                color = colors.textPrimary.copy(alpha = 0.45f),
                                modifier = Modifier.padding(horizontal = 20.dp)
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
                        confirmText = K.disconnectGoogle.tr(),
                        confirmColor = colors.danger,
                        onDismissRequest = { showUnlinkSheet = false },
                        onConfirm = {
                            showUnlinkSheet = false
                            isUnlinking = true
                            scope.launch {
                                val ok = AuthManager.unlinkGoogle()
                                isUnlinking = false
                                if (ok) ElvanSnackbar.show(unlinkedSuccessMsg)
                                else ElvanSnackbar.show(failedUnlinkMsg)
                            }
                        },
                        colors = colors,
                        customContent = {
                            Text(
                                text = K.unlinkMessage.tr(),
                                style = TextStyle(fontFamily = ff, fontSize = 13.5.sp, lineHeight = 18.sp),
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
                                style = TextStyle(fontFamily = ff, fontSize = 13.5.sp, lineHeight = 18.sp),
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
}
