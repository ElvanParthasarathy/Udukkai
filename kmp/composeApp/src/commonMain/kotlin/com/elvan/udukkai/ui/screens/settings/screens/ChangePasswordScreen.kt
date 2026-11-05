package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.animation.core.animateDpAsState
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
 * கடவுச்சொல் மாற்றும் திரை — Step-by-Step Password Change Wizard (Matching Neram Mobile).
 * - 3-Step animated dot progress indicator.
 * - Step 1: Input current password -> Verify identity with ExpressiveLoadingIndicator.
 * - Step 2: Input new password & confirm -> Live validation rules -> Update password.
 * - Step 3: Animated success checkmark -> Auto-returns after completion.
 * - Text fields use login screen pill style (ElvanAuthTextField / NeramField).
 */
@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit = {},
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current
    val scope = rememberCoroutineScope()

    var step by remember { mutableIntStateOf(1) } // 1: Verify, 2: New Password, 3: Success
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val passwordsMatch = newPassword == confirmPassword && newPassword.isNotEmpty()
    val passwordValid = newPassword.length >= 6

    val incorrectPasswordMsg = K.incorrectPassword.tr()
    val genericErrorMsg = K.error.tr()
    val resetSentMsg = K.resetMailSent.tr()

    LaunchedEffect(step) {
        if (step == 3) {
            delay(1500)
            onBack()
        }
    }

    ElvanSubShell(
        title = K.changePassword.tr(),
        onBack = onBack,
        scrollState = scrollState
    ) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = Dimens.SubpageContentPaddingBottom),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
        ) {
            item(key = "shell_top_spacer") {
                Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
            }

            item(key = "password_card") {
                ElvanSectionContainer {
                    ElvanSettingsSection(colors = colors) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 20.dp)
                        ) {
                            // ── 3-Step Progress Dots Indicator (Matching Neram) ──
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(3) { index ->
                                    val isActive = step == index + 1
                                    val isPassed = step > index + 1
                                    val width by animateDpAsState(
                                        targetValue = if (isActive) 22.dp else 7.dp,
                                        label = "DotWidth"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .height(7.dp)
                                            .width(width)
                                            .clip(RoundedCornerShape(100))
                                            .background(
                                                when {
                                                    isActive || isPassed -> if (colors.isDark) Color.White else Color.Black
                                                    else -> if (colors.isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f)
                                                }
                                            )
                                    )
                                    if (index < 2) Spacer(modifier = Modifier.width(6.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            when (step) {
                                // ── STEP 1: VERIFY CURRENT PASSWORD ──
                                1 -> {
                                    Text(
                                        text = K.enterCurrentPassword.tr(),
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = colors.textPrimary
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = K.confirmIdentityBeforeNewPassword.tr(),
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Normal,
                                            lineHeight = 18.sp
                                        ),
                                        color = colors.textPrimary.copy(alpha = 0.5f)
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    ElvanAuthTextField(
                                        value = currentPassword,
                                        onValueChange = {
                                            currentPassword = it
                                            errorMessage = null
                                        },
                                        placeholder = K.currentPassword.tr(),
                                        isPassword = true,
                                        colors = colors
                                    )

                                    errorMessage?.let {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = it,
                                            color = colors.danger,
                                            style = TextStyle(fontFamily = ff, fontSize = 13.sp, fontWeight = FontWeight.Medium),
                                            modifier = Modifier.padding(start = 12.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    Button(
                                        onClick = {
                                            if (currentPassword.isEmpty()) {
                                                errorMessage = incorrectPasswordMsg
                                                return@Button
                                            }
                                            isProcessing = true
                                            errorMessage = null
                                            scope.launch {
                                                val res = AuthManager.verifyPassword(currentPassword)
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
                                            containerColor = if (colors.isDark) Color.White else Color.Black,
                                            contentColor = if (colors.isDark) Color.Black else Color.White
                                        ),
                                        elevation = ButtonDefaults.buttonElevation(0.dp),
                                        enabled = currentPassword.isNotEmpty() && !isProcessing
                                    ) {
                                        if (isProcessing) {
                                            ExpressiveLoadingIndicator(
                                                color = if (colors.isDark) Color.Black else Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        } else {
                                            Text(
                                                text = K.verifyAndContinue.tr(),
                                                style = TextStyle(fontFamily = ff, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    TextButton(
                                        onClick = {
                                            val email = AuthManager.signedInEmail
                                            if (!email.isNullOrBlank()) {
                                                scope.launch {
                                                    com.elvan.udukkai.core.auth.platformSendPasswordReset(email)
                                                    ElvanSnackbar.show(resetSentMsg)
                                                }
                                            }
                                        },
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    ) {
                                        Text(
                                            text = K.forgotPassword.tr(),
                                            style = TextStyle(
                                                fontFamily = ff,
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            color = colors.textPrimary.copy(alpha = 0.65f)
                                        )
                                    }
                                }

                                // ── STEP 2: CREATE & CONFIRM NEW PASSWORD ──
                                2 -> {
                                    Text(
                                        text = K.createNewPassword.tr(),
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = colors.textPrimary
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    ElvanAuthTextField(
                                        value = newPassword,
                                        onValueChange = { newPassword = it; errorMessage = null },
                                        placeholder = K.newPassword.tr(),
                                        isPassword = true,
                                        colors = colors
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    ElvanAuthTextField(
                                        value = confirmPassword,
                                        onValueChange = { confirmPassword = it; errorMessage = null },
                                        placeholder = K.confirmPassword.tr(),
                                        isPassword = true,
                                        colors = colors
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Live Rule Badges
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        color = colors.iconBg
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            PasswordValidationRow(
                                                text = K.atLeast6Chars.tr(),
                                                isValid = passwordValid,
                                                colors = colors
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            PasswordValidationRow(
                                                text = K.passwordsMatch.tr(),
                                                isValid = passwordsMatch,
                                                colors = colors
                                            )
                                        }
                                    }

                                    errorMessage?.let {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = it,
                                            color = colors.danger,
                                            style = TextStyle(fontFamily = ff, fontSize = 13.sp, fontWeight = FontWeight.Medium),
                                            modifier = Modifier.padding(start = 12.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    Button(
                                        onClick = {
                                            isProcessing = true
                                            errorMessage = null
                                            scope.launch {
                                                val res = AuthManager.changePassword(currentPassword, newPassword)
                                                isProcessing = false
                                                if (res.isSuccess) {
                                                    step = 3
                                                } else {
                                                    errorMessage = res.exceptionOrNull()?.message ?: genericErrorMsg
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
                                        enabled = passwordsMatch && passwordValid && !isProcessing
                                    ) {
                                        if (isProcessing) {
                                            ExpressiveLoadingIndicator(
                                                color = if (colors.isDark) Color.Black else Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        } else {
                                            Text(
                                                text = K.updatePassword.tr(),
                                                style = TextStyle(fontFamily = ff, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
                                            )
                                        }
                                    }
                                }

                                // ── STEP 3: SUCCESS STATE ──
                                3 -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Surface(
                                            modifier = Modifier.size(64.dp),
                                            shape = CircleShape,
                                            color = colors.accent.copy(alpha = 0.12f)
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                Icon(
                                                    imageVector = MaterialSymbols.Rounded.CheckCircleFill,
                                                    contentDescription = null,
                                                    tint = colors.accent,
                                                    modifier = Modifier.size(36.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(20.dp))

                                        Text(
                                            text = K.passwordUpdated.tr(),
                                            style = TextStyle(
                                                fontFamily = ff,
                                                fontSize = 19.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = colors.textPrimary
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = K.returningToSecuritySettings.tr(),
                                            style = TextStyle(
                                                fontFamily = ff,
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Normal
                                            ),
                                            color = colors.textPrimary.copy(alpha = 0.5f)
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
}

@Composable
private fun PasswordValidationRow(
    text: String,
    isValid: Boolean,
    colors: ShellColors
) {
    val ff = LocalAppFontFamily.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (isValid) MaterialSymbols.Rounded.CheckCircleFill else MaterialSymbols.Rounded.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isValid) colors.accent else colors.textPrimary.copy(alpha = 0.3f),
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
            color = if (isValid) colors.textPrimary else colors.textPrimary.copy(alpha = 0.45f)
        )
    }
}
