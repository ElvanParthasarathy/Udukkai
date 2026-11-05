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
 * கடவுச்சொல் உருவாக்கும் திரை — Clean Password Creation.
 * Single card with input fields and a quiet footer hint. No strength bars or checklists.
 */
@Composable
fun CreatePasswordScreen(
    onBack: () -> Unit = {},
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current
    val scope = rememberCoroutineScope()

    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val passwordCreatedMsg = K.passwordCreated.tr()
    val errorGeneric = K.error.tr()

    // Validation
    val hasMinLength = newPassword.length >= 8
    val hasMixedContent = newPassword.any { it.isLetter() } && newPassword.any { it.isDigit() }
    val passwordsMatch = newPassword.isNotEmpty() && newPassword == confirmPassword
    val canSave = hasMinLength && hasMixedContent && passwordsMatch && !isProcessing

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

            // ── Password Fields Card ──
            item(key = "password_card") {
                ElvanSectionContainer {
                    ElvanSettingsSection(colors = colors) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 20.dp)
                        ) {
                            // New Password
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

                            // Confirm Password
                            ElvanSettingsTextField(
                                label = K.confirmNewPassword.tr(),
                                value = confirmPassword,
                                onValueChange = {
                                    confirmPassword = it
                                    errorMessage = null
                                },
                                placeholder = K.confirmPassword.tr(),
                                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    Icon(
                                        imageVector = if (showConfirmPassword) MaterialSymbols.Rounded.Visibility else MaterialSymbols.Rounded.VisibilityOff,
                                        contentDescription = null,
                                        tint = colors.textPrimary.copy(alpha = 0.45f),
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable { showConfirmPassword = !showConfirmPassword }
                                    )
                                },
                                colors = colors
                            )

                            // Error message
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
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }

                            // Mismatch hint (only show when both fields filled but don't match)
                            if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = K.passwordsDoNotMatch.tr(),
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    color = colors.error.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Save Password Button
                            Button(
                                onClick = {
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
                                enabled = canSave
                            ) {
                                if (isProcessing) {
                                    ExpressiveLoadingIndicator(
                                        color = if (colors.isDark) Color.Black else Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Text(
                                        text = K.savePassword.tr(),
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Quiet Footer Hint ──
            item(key = "requirements_hint") {
                ElvanSectionContainer {
                    Text(
                        text = "${K.min8Chars.tr()} · ${K.hasLetterAndNumber.tr()}",
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 12.5.sp,
                            lineHeight = 17.sp
                        ),
                        color = colors.textPrimary.copy(alpha = 0.4f),
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
        }
    }
}
