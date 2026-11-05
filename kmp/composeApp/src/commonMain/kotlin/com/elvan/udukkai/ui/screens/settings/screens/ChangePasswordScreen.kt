package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.animation.*
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
import com.elvan.udukkai.core.auth.platformSendPasswordReset
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.ExpressiveLoadingIndicator
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * கடவுச்சொல் மாற்றும் திரை — Change Password Sub-page.
 * - Internet-oriented loading experience.
 * - Shows "Password set" status card.
 * - Tapping "Change Password" loads the input fields with animated feedback.
 */
@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit = {},
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current
    val scope = rememberCoroutineScope()
    val enterCurrentPasswordMsg = K.enterCurrentPassword.tr()
    val incorrectPasswordMsg = K.incorrectPassword.tr()
    val sendPasswordResetMsg = K.resetMailSent.tr()
    val passwordUpdatedMsg = K.passwordUpdated.tr()

    var isCheckingStatus by remember { mutableStateOf(true) }
    var isEditingPassword by remember { mutableStateOf(false) }
    var isLoadingFields by remember { mutableStateOf(false) }

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    var isProcessing by remember { mutableStateOf(false) }
    var isSendingReset by remember { mutableStateOf(false) }
    var showResetSheet by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Validation
    val hasMinLength = newPassword.length >= 8
    val hasMixedContent = newPassword.any { it.isLetter() } && newPassword.any { it.isDigit() }
    val passwordsMatch = newPassword.isNotEmpty() && newPassword == confirmPassword
    val canUpdate = currentPassword.isNotBlank() && hasMinLength && hasMixedContent && passwordsMatch && !isProcessing

    // Internet-oriented loading feel
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

                // ── Status Section ("Password created / set") ──
                item(key = "status_section") {
                    ElvanSectionContainer {
                        ElvanSettingsSection(colors = colors) {
                            ElvanSettingsRow(
                                icon = MaterialSymbols.Rounded.Key,
                                title = K.password.tr(),
                                description = K.passwordSet.tr(),
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

                // ── Change Password Trigger or Input Card ──
                if (!isEditingPassword) {
                    item(key = "edit_trigger") {
                        ElvanSectionContainer {
                            ElvanSettingsSection(colors = colors) {
                                ElvanSettingsRow(
                                    icon = MaterialSymbols.Rounded.Edit,
                                    title = K.changePassword.tr(),
                                    description = K.confirmIdentityBeforeNewPassword.tr(),
                                    onClick = {
                                        isLoadingFields = true
                                        scope.launch {
                                            delay(300)
                                            isLoadingFields = false
                                            isEditingPassword = true
                                        }
                                    },
                                    customTrailing = {
                                        if (isLoadingFields) {
                                            ExpressiveLoadingIndicator(color = colors.textPrimary, modifier = Modifier.size(18.dp))
                                        } else {
                                            Icon(
                                                imageVector = MaterialSymbols.Rounded.ChevronRight,
                                                contentDescription = null,
                                                tint = colors.textTertiary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    },
                                    colors = colors
                                )
                            }
                        }
                    }
                } else {
                    // ── Seamless Password Input Card ──
                    item(key = "change_password_card") {
                        ElvanSectionContainer {
                            ElvanSettingsSection(colors = colors) {
                                // 1. Current Password Row
                                PasswordInlineInputRow(
                                    label = K.currentPassword.tr(),
                                    value = currentPassword,
                                    onValueChange = {
                                        currentPassword = it
                                        errorMessage = null
                                    },
                                    placeholder = K.enterCurrentPassword.tr(),
                                    isPasswordVisible = showCurrentPassword,
                                    onToggleVisibility = { showCurrentPassword = !showCurrentPassword },
                                    colors = colors
                                )

                                ElvanSettingsDivider(colors = colors)

                                // 2. New Password Row
                                PasswordInlineInputRow(
                                    label = K.newPassword.tr(),
                                    value = newPassword,
                                    onValueChange = {
                                        newPassword = it
                                        errorMessage = null
                                    },
                                    placeholder = K.enterNewPassword.tr(),
                                    isPasswordVisible = showNewPassword,
                                    onToggleVisibility = { showNewPassword = !showNewPassword },
                                    colors = colors
                                )

                                ElvanSettingsDivider(colors = colors)

                                // 3. Confirm Password Row
                                PasswordInlineInputRow(
                                    label = K.confirmNewPassword.tr(),
                                    value = confirmPassword,
                                    onValueChange = {
                                        confirmPassword = it
                                        errorMessage = null
                                    },
                                    placeholder = K.confirmPassword.tr(),
                                    isPasswordVisible = showConfirmPassword,
                                    onToggleVisibility = { showConfirmPassword = !showConfirmPassword },
                                    colors = colors
                                )
                            }
                        }
                    }

                    // ── Error feedback ──
                    if (errorMessage != null || (confirmPassword.isNotEmpty() && !passwordsMatch)) {
                        item(key = "error_text") {
                            ElvanSectionContainer {
                                Text(
                                    text = errorMessage ?: K.passwordsDoNotMatch.tr(),
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    color = colors.error,
                                    modifier = Modifier.padding(horizontal = 20.dp)
                                )
                            }
                        }
                    }

                    // ── Update Password Button ──
                    item(key = "update_btn") {
                        ElvanSectionContainer {
                            Button(
                                onClick = {
                                    if (currentPassword.isBlank()) {
                                        errorMessage = enterCurrentPasswordMsg
                                        return@Button
                                    }
                                    isProcessing = true
                                    errorMessage = null
                                    scope.launch {
                                        val res = AuthManager.changePassword(currentPassword, newPassword)
                                        isProcessing = false
                                        if (res.isSuccess) {
                                            ElvanSnackbar.show(passwordUpdatedMsg)
                                            delay(300)
                                            onBack()
                                        } else {
                                            errorMessage = incorrectPasswordMsg
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
                                enabled = canUpdate
                            ) {
                                if (isProcessing) {
                                    ExpressiveLoadingIndicator(
                                        color = if (colors.isDark) Color.Black else Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Text(
                                        text = K.updatePassword.tr(),
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // ── Cancel Button ──
                    item(key = "cancel_edit_btn") {
                        ElvanSectionContainer {
                            Button(
                                onClick = { isEditingPassword = false },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.iconBg,
                                    contentColor = colors.textPrimary
                                ),
                                elevation = ButtonDefaults.buttonElevation(0.dp)
                            ) {
                                Text(
                                    text = K.cancelBtn.tr(),
                                    style = TextStyle(fontFamily = ff, fontSize = 14.sp, fontWeight = FontWeight.Normal)
                                )
                            }
                        }
                    }
                }

                // ── Forgot Password Quiet Link ──
                item(key = "forgot_password") {
                    ElvanSectionContainer {
                        TextButton(
                            onClick = { showResetSheet = true },
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            enabled = !isSendingReset
                        ) {
                            if (isSendingReset) {
                                ExpressiveLoadingIndicator(color = colors.textPrimary, modifier = Modifier.size(16.dp))
                            } else {
                                Text(
                                    text = K.forgotPassword.tr(),
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    color = colors.textPrimary.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }

                // ── Quiet Requirements Footer ──
                item(key = "requirements_hint") {
                    ElvanSectionContainer {
                        Text(
                            text = "${K.min8Chars.tr()} · ${K.hasLetterAndNumber.tr()}",
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            ),
                            color = colors.textPrimary.copy(alpha = 0.4f),
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
            }
        }

        // ── Password Reset Confirmation Action Sheet ──
        if (showResetSheet) {
            val email = AuthManager.signedInEmail ?: ""
            ElvanActionSheet(
                title = K.resetPasswordTitle.tr(),
                cancelText = K.cancelBtn.tr(),
                confirmText = K.sendResetLink.tr(),
                confirmColor = colors.textPrimary,
                isConfirmFilled = true,
                onDismissRequest = { showResetSheet = false },
                onConfirm = {
                    showResetSheet = false
                    if (email.isNotEmpty()) {
                        isSendingReset = true
                        scope.launch {
                            val ok = platformSendPasswordReset(email)
                            isSendingReset = false
                            if (ok) {
                                ElvanSnackbar.show(sendPasswordResetMsg)
                            }
                        }
                    }
                },
                colors = colors,
                customContent = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = K.sendResetEmailTo.tr(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            ),
                            color = colors.textPrimary.copy(alpha = 0.65f),
                            textAlign = TextAlign.Center
                        )
                        if (email.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = colors.iconBg
                            ) {
                                Text(
                                    text = email,
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = colors.textPrimary,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}
