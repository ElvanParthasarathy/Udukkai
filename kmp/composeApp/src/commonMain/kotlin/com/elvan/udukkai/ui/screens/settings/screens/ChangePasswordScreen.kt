package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
 * கடவுச்சொல் மாற்றும் திரை — Change Password Screen.
 * Industry-standard single-screen secure workflow matching Samsung One UI & Apple HIG:
 * - Real-time password criteria feedback.
 * - Expressive loaders during network authentication.
 * - ElvanActionSheet for guided password reset dispatch.
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
    val atLeast6CharsMsg = K.atLeast6Chars.tr()
    val passwordsMatchMsg = K.passwordsMatch.tr()
    val passwordUpdatedMsg = K.passwordUpdated.tr()

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }

    var isProcessing by remember { mutableStateOf(false) }
    var isSendingReset by remember { mutableStateOf(false) }
    var showResetSheet by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val passwordValid = newPassword.length >= 6
    val passwordsMatch = newPassword.isNotEmpty() && newPassword == confirmPassword

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

            item(key = "change_password_card") {
                ElvanSectionContainer {
                    ElvanSettingsSection(colors = colors) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 20.dp)
                        ) {
                            Text(
                                text = K.changePassword.tr(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = colors.textPrimary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = K.confirmIdentityBeforeNewPassword.tr(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                ),
                                color = colors.textPrimary.copy(alpha = 0.55f)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // 1. Current Password
                            ElvanSettingsTextField(
                                label = K.currentPassword.tr(),
                                value = currentPassword,
                                onValueChange = {
                                    currentPassword = it
                                    errorMessage = null
                                },
                                placeholder = K.enterCurrentPassword.tr(),
                                visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    Icon(
                                        imageVector = if (showCurrentPassword) MaterialSymbols.Rounded.Visibility else MaterialSymbols.Rounded.VisibilityOff,
                                        contentDescription = null,
                                        tint = colors.textPrimary.copy(alpha = 0.45f),
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable { showCurrentPassword = !showCurrentPassword }
                                    )
                                },
                                colors = colors
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // 2. New Password
                            ElvanSettingsTextField(
                                label = K.newPassword.tr(),
                                value = newPassword,
                                onValueChange = {
                                    newPassword = it
                                    errorMessage = null
                                },
                                placeholder = K.enterNewPassword.tr(),
                                visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    Icon(
                                        imageVector = if (showNewPassword) MaterialSymbols.Rounded.Visibility else MaterialSymbols.Rounded.VisibilityOff,
                                        contentDescription = null,
                                        tint = colors.textPrimary.copy(alpha = 0.45f),
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable { showNewPassword = !showNewPassword }
                                    )
                                },
                                colors = colors
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // 3. Confirm New Password
                            ElvanSettingsTextField(
                                label = K.confirmNewPassword.tr(),
                                value = confirmPassword,
                                onValueChange = {
                                    confirmPassword = it
                                    errorMessage = null
                                },
                                placeholder = K.confirmPassword.tr(),
                                visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                colors = colors
                            )

                            // Error Display
                            if (errorMessage != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = errorMessage!!,
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = colors.error,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Live Criteria Indicators
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = colors.iconBg
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    ChangePasswordValidationRow(
                                        text = atLeast6CharsMsg,
                                        satisfied = passwordValid,
                                        colors = colors
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    ChangePasswordValidationRow(
                                        text = passwordsMatchMsg,
                                        satisfied = passwordsMatch,
                                        colors = colors
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Update Password Button (Standard Udukkai 50% Pill)
                            Button(
                                onClick = {
                                    if (currentPassword.isBlank()) {
                                        errorMessage = enterCurrentPasswordMsg
                                        return@Button
                                    }
                                    if (newPassword.length < 6) {
                                        errorMessage = atLeast6CharsMsg
                                        return@Button
                                    }
                                    if (newPassword != confirmPassword) {
                                        errorMessage = passwordsMatchMsg
                                        return@Button
                                    }
                                    isProcessing = true
                                    errorMessage = null
                                    scope.launch {
                                        val res = AuthManager.changePassword(currentPassword, newPassword)
                                        isProcessing = false
                                        if (res.isSuccess) {
                                            ElvanSnackbar.show(passwordUpdatedMsg)
                                            delay(350)
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
                                enabled = !isProcessing
                            ) {
                                if (isProcessing) {
                                    ExpressiveLoadingIndicator(
                                        color = if (colors.isDark) Color.Black else Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Text(
                                        text = K.updatePassword.tr(),
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Forgot Password Link
                            TextButton(
                                onClick = { showResetSheet = true },
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                enabled = !isSendingReset
                            ) {
                                if (isSendingReset) {
                                    ExpressiveLoadingIndicator(color = colors.accent, modifier = Modifier.size(16.dp))
                                } else {
                                    Text(
                                        text = K.forgotPassword.tr(),
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = colors.textPrimary.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
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
                confirmColor = colors.accent,
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
                                fontSize = 13.5.sp,
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
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold
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

@Composable
private fun ChangePasswordValidationRow(
    text: String,
    satisfied: Boolean,
    colors: ShellColors
) {
    val ff = LocalAppFontFamily.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (satisfied) MaterialSymbols.Rounded.CheckCircle else MaterialSymbols.Rounded.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (satisfied) colors.accent else colors.textPrimary.copy(alpha = 0.35f),
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = TextStyle(
                fontFamily = ff,
                fontSize = 12.sp,
                fontWeight = if (satisfied) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = if (satisfied) colors.textPrimary else colors.textPrimary.copy(alpha = 0.5f)
        )
    }
}
