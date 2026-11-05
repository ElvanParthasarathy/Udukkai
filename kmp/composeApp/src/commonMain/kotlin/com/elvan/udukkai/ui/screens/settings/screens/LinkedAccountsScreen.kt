package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.animation.*
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
 * கூகுள் கணக்குத் திரை — Google Account Sub-page.
 * - Circular avatar: Profile image if linked, blank circle if not linked.
 * - Internet-oriented loading feedback.
 * - Clear link / unlink actions without alarmist styling.
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

    var isCheckingStatus by remember { mutableStateOf(true) }
    var showUnlinkSheet by remember { mutableStateOf(false) }
    var isLinking by remember { mutableStateOf(false) }
    var isUnlinking by remember { mutableStateOf(false) }

    val unlinkedSuccessMsg = K.googleAccountUnlinked.tr()
    val failedUnlinkMsg = K.failedToUnlink.tr()
    val connectedMsg = K.connected.tr()

    // Internet-oriented loading experience on enter
    LaunchedEffect(Unit) {
        delay(350)
        isCheckingStatus = false
    }

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

                // ── Profile Circle (Image if linked, blank circle if unlinked) ──
                item(key = "google_avatar_header") {
                    ElvanSectionContainer {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(colors.iconBg),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isGoogleLinked) {
                                    AccountAvatar(fallback = {
                                        Icon(
                                            imageVector = MaterialSymbols.Rounded.Person,
                                            contentDescription = null,
                                            tint = colors.textPrimary.copy(alpha = 0.6f),
                                            modifier = Modifier.size(36.dp)
                                        )
                                    })
                                } else {
                                    // Blank circle placeholder
                                    Icon(
                                        imageVector = MaterialSymbols.Rounded.Person,
                                        contentDescription = null,
                                        tint = colors.textPrimary.copy(alpha = 0.25f),
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = K.googleAccount.tr(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = colors.textPrimary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (isGoogleLinked) googleEmail else K.notConnected.tr(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                color = colors.textPrimary.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                // ── Action Section ──
                item(key = "action_btn") {
                    ElvanSectionContainer {
                        if (isGoogleLinked) {
                            // Unlink button: Calm secondary styling
                            Button(
                                onClick = { showUnlinkSheet = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.iconBg,
                                    contentColor = colors.textPrimary
                                ),
                                elevation = ButtonDefaults.buttonElevation(0.dp),
                                enabled = !isUnlinking
                            ) {
                                if (isUnlinking) {
                                    ExpressiveLoadingIndicator(color = colors.textPrimary, modifier = Modifier.size(18.dp))
                                } else {
                                    Text(
                                        text = K.unlinkGoogle.tr(),
                                        style = TextStyle(fontFamily = ff, fontSize = 14.5.sp, fontWeight = FontWeight.Medium)
                                    )
                                }
                            }
                        } else {
                            // Link button: Standard monochrome
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
                                    containerColor = if (colors.isDark) Color.White else Color.Black,
                                    contentColor = if (colors.isDark) Color.Black else Color.White
                                ),
                                elevation = ButtonDefaults.buttonElevation(0.dp),
                                enabled = !isLinking
                            ) {
                                if (isLinking) {
                                    ExpressiveLoadingIndicator(
                                        color = if (colors.isDark) Color.Black else Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Text(
                                        text = K.linkGoogle.tr(),
                                        style = TextStyle(fontFamily = ff, fontSize = 14.5.sp, fontWeight = FontWeight.Medium)
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Quiet Hint ──
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
                    confirmText = K.unlinkGoogle.tr(),
                    confirmColor = colors.textPrimary,
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
