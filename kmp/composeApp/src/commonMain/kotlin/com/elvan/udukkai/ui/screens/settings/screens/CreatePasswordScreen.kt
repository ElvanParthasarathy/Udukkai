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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.auth.AuthManager
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
 * கடவுச்சொல் உருவாக்கும் திரை — Create Password Screen.
 * For users who initially signed in with Google OAuth to establish email/password access.
 */
@Composable
fun CreatePasswordScreen(
    onBack: () -> Unit = {},
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current
    val scope = rememberCoroutineScope()
    val atLeast6CharsMsg = K.atLeast6Chars.tr()
    val passwordsMatchMsg = K.passwordsMatch.tr()
    val passwordCreatedMsg = K.passwordCreated.tr()
    val errorGeneric = K.error.tr()

    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    var isProcessing by remember { mutableStateOf(false) }
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

            item(key = "content_card") {
                ElvanSectionContainer {
                    ElvanSettingsSection(colors = colors) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 20.dp)
                        ) {
                            // Info Banner
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = colors.iconBg
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = MaterialSymbols.Rounded.Info,
                                        contentDescription = null,
                                        tint = colors.accent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = K.signedInWithGoogleCreatePassword.tr(),
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp
                                        ),
                                        color = colors.textPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = K.createPasswordTitle.tr(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = colors.textPrimary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = K.createPasswordMsg.tr(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                ),
                                color = colors.textPrimary.copy(alpha = 0.55f)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // 1. New Password
                            ElvanSettingsTextField(
                                label = K.newPassword.tr(),
                                value = newPassword,
                                onValueChange = {
                                    newPassword = it
                                    errorMessage = null
                                },
                                placeholder = K.enterNewPassword.tr(),
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    Icon(
                                        imageVector = if (showPassword) MaterialSymbols.Rounded.Visibility else MaterialSymbols.Rounded.VisibilityOff,
                                        contentDescription = null,
                                        tint = colors.textPrimary.copy(alpha = 0.45f),
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable { showPassword = !showPassword }
                                    )
                                },
                                colors = colors
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // 2. Confirm Password
                            ElvanSettingsTextField(
                                label = K.confirmNewPassword.tr(),
                                value = confirmPassword,
                                onValueChange = {
                                    confirmPassword = it
                                    errorMessage = null
                                },
                                placeholder = K.confirmPassword.tr(),
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                colors = colors
                            )

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

                            // Validation Checklist
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = colors.iconBg
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    CreatePasswordValidationRow(
                                        text = atLeast6CharsMsg,
                                        satisfied = passwordValid,
                                        colors = colors
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    CreatePasswordValidationRow(
                                        text = passwordsMatchMsg,
                                        satisfied = passwordsMatch,
                                        colors = colors
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Set Password Button
                            Button(
                                onClick = {
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
                                        val res = AuthManager.createPassword(newPassword)
                                        isProcessing = false
                                        if (res.isSuccess) {
                                            ElvanSnackbar.show(passwordCreatedMsg)
                                            delay(350)
                                            onBack()
                                        } else {
                                            errorMessage = errorGeneric
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
                                        text = K.createPassword.tr(),
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 15.sp,
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
    }
}

@Composable
private fun CreatePasswordValidationRow(
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
