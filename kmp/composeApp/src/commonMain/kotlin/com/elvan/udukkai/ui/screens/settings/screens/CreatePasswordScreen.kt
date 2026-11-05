package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
 * கடவுச்சொல் உருவாக்கும் திரை — Clean Native Password Creation.
 * - Internet-oriented loading experience.
 * - Shows status card ("No password set").
 * - Minimal, integrated card with seamless inline rows and quiet footer requirement hints.
 */
@Composable
fun CreatePasswordScreen(
    onBack: () -> Unit = {},
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current
    val scope = rememberCoroutineScope()

    var isCheckingStatus by remember { mutableStateOf(true) }
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

                // ── Status Section ("No password set") ──
                item(key = "status_section") {
                    ElvanSectionContainer {
                        ElvanSettingsSection(colors = colors) {
                            ElvanSettingsRow(
                                icon = MaterialSymbols.Rounded.Key,
                                title = K.password.tr(),
                                description = K.noPasswordSet.tr(),
                                onClick = {},
                                customTrailing = {
                                    Icon(
                                        imageVector = MaterialSymbols.Rounded.RadioButtonUnchecked,
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

                // ── Seamless Password Input Card ──
                item(key = "password_card") {
                    ElvanSectionContainer {
                        ElvanSettingsSection(colors = colors) {
                            // 1. New Password Row
                            PasswordInlineInputRow(
                                label = K.newPassword.tr(),
                                value = newPassword,
                                onValueChange = {
                                    newPassword = it
                                    errorMessage = null
                                },
                                placeholder = K.enterNewPassword.tr(),
                                isPasswordVisible = showPassword,
                                onToggleVisibility = { showPassword = !showPassword },
                                colors = colors
                            )

                            ElvanSettingsDivider(colors = colors)

                            // 2. Confirm Password Row
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

                // ── Error / Mismatch feedback ──
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

                // ── Save Button ──
                item(key = "save_btn") {
                    ElvanSectionContainer {
                        Button(
                            onClick = {
                                isProcessing = true
                                errorMessage = null
                                scope.launch {
                                    val res = AuthManager.createPassword(newPassword)
                                    isProcessing = false
                                    if (res.isSuccess) {
                                        ElvanSnackbar.show(passwordCreatedMsg)
                                        delay(300)
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
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Text(
                                    text = K.savePassword.tr(),
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
    }
}

@Composable
internal fun PasswordInlineInputRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPasswordVisible: Boolean,
    onToggleVisibility: () -> Unit,
    colors: ShellColors,
    modifier: Modifier = Modifier
) {
    val ff = LocalAppFontFamily.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = ff,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.2.sp
                ),
                color = colors.textPrimary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = colors.textPrimary.copy(alpha = 0.35f)
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = ff,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Normal,
                        color = colors.textPrimary
                    ),
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    cursorBrush = SolidColor(colors.accent),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        IconButton(
            onClick = onToggleVisibility,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = if (isPasswordVisible) MaterialSymbols.Rounded.Visibility else MaterialSymbols.Rounded.VisibilityOff,
                contentDescription = null,
                tint = colors.textPrimary.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
