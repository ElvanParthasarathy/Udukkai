package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
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
 * கூகுள் கணக்குத் திரை — Google Account Management.
 * Shows current link status clearly. Explicit "இணை" / "பிரி" button — no guessing.
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

            // ── Status Row ──
            item(key = "status_row") {
                ElvanSectionContainer {
                    ElvanSettingsSection(colors = colors) {
                        ElvanSettingsRow(
                            icon = MaterialSymbols.Rounded.Link,
                            title = K.googleAccount.tr(),
                            description = if (isGoogleLinked) googleEmail else K.notConnected.tr(),
                            onClick = {},
                            customTrailing = {
                                if (isGoogleLinked) {
                                    Icon(
                                        imageVector = MaterialSymbols.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = colors.accent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = MaterialSymbols.Rounded.RadioButtonUnchecked,
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

            // ── Explicit Action Button ──
            item(key = "action_btn") {
                ElvanSectionContainer {
                    if (isGoogleLinked) {
                        // Unlink — clearly labelled, clearly red
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
                                    style = TextStyle(fontFamily = ff, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    } else {
                        // Link — clearly labelled, standard monochrome
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
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = K.linkGoogle.tr(),
                                    style = TextStyle(fontFamily = ff, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }

            // ── Quiet Status Description ──
            item(key = "status_hint") {
                ElvanSectionContainer {
                    Text(
                        text = if (isGoogleLinked) K.googleLinkedDesc.tr() else K.googleUnlinkedDesc.tr(),
                        style = TextStyle(fontFamily = ff, fontSize = 12.5.sp, lineHeight = 17.sp),
                        color = colors.textPrimary.copy(alpha = 0.45f),
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
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
                            if (ok) ElvanSnackbar.show(unlinkedSuccessMsg)
                            else ElvanSnackbar.show(failedUnlinkMsg)
                        }
                    },
                    colors = colors,
                    customContent = {
                        Text(
                            text = K.unlinkMessage.tr(),
                            style = TextStyle(fontFamily = ff, fontSize = 13.5.sp, lineHeight = 19.sp),
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
                            style = TextStyle(fontFamily = ff, fontSize = 13.5.sp, lineHeight = 19.sp),
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
