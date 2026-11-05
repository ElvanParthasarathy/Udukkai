package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
 * Two-step secure workflow:
 * Step 1: Verify current password
 * Step 2: Enter & confirm new password with live strength criteria
 * Step 3: Success confirmation
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
    val sendPasswordResetMsg = K.sendPasswordReset.tr()
    val errorGeneric = K.error.tr()
    val btnTextClr = if (colors.isDark) Color.Black else Color.White

    var step by remember { mutableIntStateOf(1) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }

    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val passwordValid = newPassword.length >= 6
    val passwordsMatch = newPassword.isNotEmpty() && newPassword == confirmPassword

    LaunchedEffect(step) {
        if (step == 3) {
            delay(1500)
            onBack()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Dimens.ContentPadding,
                end = Dimens.ContentPadding,
                bottom = Dimens.SubpageContentPaddingBottom
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
        ) {
            item(key = "shell_top_spacer") {
                Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
            }

            item(key = "content_card") {
                ElvanSettingsSection(colors = colors) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        when (step) {
                            1 -> {
                                Text(
                                    text = K.currentPassword.tr(),
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

                                if (errorMessage != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = errorMessage!!,
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = colors.error,
                                        modifier = Modifier.padding(start = 12.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = {
                                        if (currentPassword.isEmpty()) {
                                            errorMessage = enterCurrentPasswordMsg
                                            return@Button
                                        }
                                        isProcessing = true
                                        scope.launch {
                                            val dummyNew = currentPassword
                                            val res = AuthManager.changePassword(currentPassword, dummyNew)
                                            isProcessing = false
                                            if (res.isSuccess) {
                                                step = 2
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
                                        containerColor = colors.accent,
                                        contentColor = btnTextClr
                                    ),
                                    enabled = !isProcessing
                                ) {
                                    if (isProcessing) {
                                        ExpressiveLoadingIndicator(color = btnTextClr, modifier = Modifier.size(18.dp))
                                    } else {
                                        Text(
                                            text = K.verifyAndContinue.tr(),
                                            style = TextStyle(fontFamily = ff, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                TextButton(
                                    onClick = {
                                        val email = AuthManager.signedInEmail
                                        if (!email.isNullOrEmpty()) {
                                            scope.launch {
                                                val ok = platformSendPasswordReset(email)
                                                if (ok) {
                                                    ElvanSnackbar.show(sendPasswordResetMsg)
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text(
                                        text = K.forgotPassword.tr(),
                                        style = TextStyle(fontFamily = ff, fontSize = 13.5.sp, fontWeight = FontWeight.Medium),
                                        color = colors.textPrimary.copy(alpha = 0.7f)
                                    )
                                }
                            }

                            2 -> {
                                Text(
                                    text = K.newPassword.tr(),
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = colors.textPrimary
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                ElvanSettingsTextField(
                                    label = K.newPassword.tr(),
                                    value = newPassword,
                                    onValueChange = { newPassword = it },
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

                                Spacer(modifier = Modifier.height(12.dp))

                                ElvanSettingsTextField(
                                    label = K.confirmNewPassword.tr(),
                                    value = confirmPassword,
                                    onValueChange = { confirmPassword = it },
                                    placeholder = K.confirmPassword.tr(),
                                    visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                    colors = colors
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Validation Checklist
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = colors.iconBg
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        ValidationRow(
                                            text = K.atLeast6Chars.tr(),
                                            isValid = passwordValid,
                                            colors = colors
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        ValidationRow(
                                            text = K.passwordsMatch.tr(),
                                            isValid = passwordsMatch,
                                            colors = colors
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = {
                                        isProcessing = true
                                        scope.launch {
                                            val res = AuthManager.changePassword(currentPassword, newPassword)
                                            isProcessing = false
                                            if (res.isSuccess) {
                                                step = 3
                                            } else {
                                                errorMessage = res.exceptionOrNull()?.message ?: errorGeneric
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.accent,
                                        contentColor = btnTextClr
                                    ),
                                    enabled = passwordValid && passwordsMatch && !isProcessing
                                ) {
                                    if (isProcessing) {
                                        ExpressiveLoadingIndicator(color = btnTextClr, modifier = Modifier.size(18.dp))
                                    } else {
                                        Text(
                                            text = K.updatePassword.tr(),
                                            style = TextStyle(fontFamily = ff, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                        )
                                    }
                                }
                            }

                            3 -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Surface(
                                        modifier = Modifier.size(60.dp),
                                        shape = CircleShape,
                                        color = colors.accent.copy(alpha = 0.12f)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = MaterialSymbols.Rounded.CheckCircle,
                                                contentDescription = null,
                                                tint = colors.accent,
                                                modifier = Modifier.size(36.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = K.passwordUpdated.tr(),
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = colors.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ValidationRow(
    text: String,
    isValid: Boolean,
    colors: ShellColors
) {
    val ff = LocalAppFontFamily.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (isValid) MaterialSymbols.Rounded.CheckCircle else MaterialSymbols.Rounded.Cancel,
            contentDescription = null,
            tint = if (isValid) colors.accent else colors.textPrimary.copy(alpha = 0.35f),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = TextStyle(
                fontFamily = ff,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Normal
            ),
            color = if (isValid) colors.textPrimary else colors.textPrimary.copy(alpha = 0.5f)
        )
    }
}
