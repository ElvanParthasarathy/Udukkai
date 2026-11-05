package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
 * கடவுச்சொல் உருவாக்கும் திரை — Modern Password Creation Studio.
 * Industry-grade password studio featuring:
 * - Security key hero badge with clean, natural Senthamizh description.
 * - Dynamic 4-segment animated Password Strength Meter (Weak, Medium, Strong).
 * - Real-time requirement checklist with animated checkmark states.
 * - Prominent full-width Save Password pill button with loading feedback.
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

    // ── Live Criteria Validation ──
    val hasMinLength = newPassword.length >= 8
    val hasLetters = newPassword.any { it.isLetter() }
    val hasDigits = newPassword.any { it.isDigit() }
    val hasMixedContent = hasLetters && hasDigits
    val passwordsMatch = newPassword.isNotEmpty() && newPassword == confirmPassword

    // Password strength score (0 to 4)
    val strengthScore = remember(newPassword) {
        var score = 0
        if (newPassword.length >= 6) score++
        if (newPassword.length >= 8) score++
        if (newPassword.any { it.isLetter() } && newPassword.any { it.isDigit() }) score++
        if (newPassword.any { !it.isLetterOrDigit() } || newPassword.any { it.isUpperCase() }) score++
        score
    }

    val strengthColor = when (strengthScore) {
        0, 1 -> colors.error
        2 -> Color(0xFFF59E0B) // Amber
        else -> colors.accent
    }

    val strengthLabel = when (strengthScore) {
        0, 1 -> K.passwordStrengthWeak.tr()
        2 -> K.passwordStrengthMedium.tr()
        else -> K.passwordStrengthStrong.tr()
    }

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

            item(key = "password_studio_card") {
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
                                .padding(horizontal = 20.dp, vertical = 24.dp)
                        ) {
                            // Hero Security Key Icon
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(colors.iconBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = MaterialSymbols.Rounded.Key,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = K.createPasswordTitle.tr(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = colors.textPrimary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "உங்கள் மின்னஞ்சல் மூலம் உள்நுழையப் புதிய கடவுச்சொல்லை அமைக்கவும்.",
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 13.5.sp,
                                    lineHeight = 19.sp
                                ),
                                color = colors.textPrimary.copy(alpha = 0.55f)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // 1. New Password Field
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

                            // Password Strength Bar
                            if (newPassword.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    for (i in 1..4) {
                                        val active = strengthScore >= i
                                        val barColor by animateColorAsState(
                                            targetValue = if (active) strengthColor else colors.iconBg,
                                            animationSpec = tween(200)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(4.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(barColor)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = strengthLabel,
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = strengthColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 2. Confirm Password Field
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

                            Spacer(modifier = Modifier.height(20.dp))

                            // Requirements Checklist Card
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = colors.iconBg
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    PasswordRequirementItem(
                                        label = K.min8Chars.tr(),
                                        isSatisfied = hasMinLength,
                                        colors = colors
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    PasswordRequirementItem(
                                        label = K.hasLetterAndNumber.tr(),
                                        isSatisfied = hasMixedContent,
                                        colors = colors
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    PasswordRequirementItem(
                                        label = K.passwordsMatch.tr(),
                                        isSatisfied = passwordsMatch,
                                        colors = colors
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Prominent Save Password Pill Button
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
        }
    }
}

@Composable
private fun PasswordRequirementItem(
    label: String,
    isSatisfied: Boolean,
    colors: ShellColors
) {
    val ff = LocalAppFontFamily.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (isSatisfied) MaterialSymbols.Rounded.CheckCircle else MaterialSymbols.Rounded.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isSatisfied) colors.accent else colors.textPrimary.copy(alpha = 0.3f),
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            style = TextStyle(
                fontFamily = ff,
                fontSize = 12.5.sp,
                fontWeight = if (isSatisfied) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = if (isSatisfied) colors.textPrimary else colors.textPrimary.copy(alpha = 0.5f)
        )
    }
}
