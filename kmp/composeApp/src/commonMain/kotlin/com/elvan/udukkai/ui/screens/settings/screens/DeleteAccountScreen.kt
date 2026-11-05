package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
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
import com.elvan.udukkai.core.security.authenticateFingerprint
import com.elvan.udukkai.core.security.fingerprintAvailable
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.ExpressiveLoadingIndicator
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import kotlinx.coroutines.launch

/**
 * கணக்கை நிரந்தரமாக அழிக்கும் திரை — Step-by-Step Account Deletion Flow (NO Checkboxes).
 * - Step 1: Warning and consequence details with a clean "Continue" button.
 * - Step 2: Identity verification (Password re-auth or biometric scan) with "Verify & Continue".
 * - Step 3: Final confirmation step with permanent deletion trigger.
 */
@Composable
fun DeleteAccountScreen(
    onBack: () -> Unit = {},
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current
    val scope = rememberCoroutineScope()

    val hasPassword = AuthManager.hasPassword
    var currentStep by remember { mutableStateOf(1) } // 1, 2, or 3
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val accountDeletedMsg = K.accountDeleted.tr()
    val errorGeneric = K.error.tr()
    val biometricPromptTitle = K.biometricVerifyTitle.tr()
    val cancelText = K.cancelBtn.tr()
    val enterCurrentPasswordMsg = K.enterCurrentPassword.tr()

    ElvanSubShell(
        title = K.deleteAccount.tr(),
        onBack = onBack,
        scrollState = scrollState
    ) {
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

            // ── 3-Step Progress Dots Indicator ──
            item(key = "progress_dots") {
                ElvanSectionContainer {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(3) { index ->
                            val isActive = currentStep == index + 1
                            val isPassed = currentStep > index + 1
                            val width by animateDpAsState(
                                targetValue = if (isActive) 22.dp else 7.dp,
                                label = "DeleteDotWidth"
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
                }
            }

            when (currentStep) {
                // ── STEP 1: CONSEQUENCES (விளைவுகள்) ──
                1 -> {
                    item(key = "step1_consequences") {
                        ElvanSectionContainer {
                            ElvanSettingsSection(colors = colors) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 20.dp)
                                ) {
                                    Text(
                                        text = K.deleteAccountWarning.tr(),
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 13.sp,
                                            lineHeight = 19.sp
                                        ),
                                        color = colors.textPrimary.copy(alpha = 0.7f)
                                    )

                                    Spacer(modifier = Modifier.height(24.dp))

                                    Button(
                                        onClick = { currentStep = 2 },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        shape = RoundedCornerShape(50),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (colors.isDark) Color.White else Color.Black,
                                            contentColor = if (colors.isDark) Color.Black else Color.White
                                        ),
                                        elevation = ButtonDefaults.buttonElevation(0.dp)
                                    ) {
                                        Text(
                                            text = K.continueBtn.tr(),
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
                    }
                }

                // ── STEP 2: VERIFICATION (அடையாளச் சரிபார்ப்பு) ──
                2 -> {
                    item(key = "step2_verification") {
                        ElvanSectionContainer {
                            ElvanSettingsSection(colors = colors) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 20.dp)
                                ) {
                                    if (hasPassword) {
                                        Text(
                                            text = K.verifyIdentityForDeletion.tr(),
                                            style = TextStyle(
                                                fontFamily = ff,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Normal
                                            ),
                                            color = colors.textPrimary.copy(alpha = 0.6f),
                                            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                                        )

                                        ElvanAuthTextField(
                                            value = password,
                                            onValueChange = {
                                                password = it
                                                errorMessage = null
                                            },
                                            placeholder = K.currentPassword.tr(),
                                            isPassword = true,
                                            colors = colors
                                        )

                                        if (errorMessage != null) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = errorMessage!!,
                                                style = TextStyle(
                                                    fontFamily = ff,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium
                                                ),
                                                color = colors.error,
                                                modifier = Modifier.padding(start = 4.dp)
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = K.confirmIdentityBeforeNewPassword.tr(),
                                            style = TextStyle(
                                                fontFamily = ff,
                                                fontSize = 13.sp,
                                                lineHeight = 18.sp
                                            ),
                                            color = colors.textPrimary.copy(alpha = 0.7f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    Button(
                                        onClick = {
                                            if (!hasPassword || password.isNotBlank()) {
                                                currentStep = 3
                                            } else {
                                                errorMessage = enterCurrentPasswordMsg
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
                                        enabled = !hasPassword || password.isNotBlank()
                                    ) {
                                        Text(
                                            text = K.verifyAndContinue.tr(),
                                            style = TextStyle(
                                                fontFamily = ff,
                                                fontSize = 14.5.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = { currentStep = 1 },
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
                                            style = TextStyle(
                                                fontFamily = ff,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Normal
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── STEP 3: FINAL DELETION CONFIRMATION (இறுதி அழிப்பு) ──
                3 -> {
                    item(key = "step3_confirmation") {
                        ElvanSectionContainer {
                            ElvanSettingsSection(colors = colors) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 20.dp)
                                ) {
                                    Text(
                                        text = K.deletePermanentlyDesc.tr(),
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 13.sp,
                                            lineHeight = 19.sp
                                        ),
                                        color = colors.textPrimary.copy(alpha = 0.7f)
                                    )

                                    if (errorMessage != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = errorMessage!!,
                                            style = TextStyle(
                                                fontFamily = ff,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            color = colors.error,
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    Button(
                                        onClick = {
                                            val executeDelete = {
                                                isProcessing = true
                                                scope.launch {
                                                    val res = AuthManager.deleteAccount(password.takeIf { hasPassword })
                                                    isProcessing = false
                                                    if (res.isSuccess) {
                                                        ElvanSnackbar.show(accountDeletedMsg)
                                                    } else {
                                                        errorMessage = res.exceptionOrNull()?.message ?: errorGeneric
                                                    }
                                                }
                                            }

                                            if (fingerprintAvailable()) {
                                                authenticateFingerprint(
                                                    title = biometricPromptTitle,
                                                    cancelText = cancelText
                                                ) { authenticated ->
                                                    if (authenticated) {
                                                        executeDelete()
                                                    }
                                                }
                                            } else {
                                                executeDelete()
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
                                                modifier = Modifier.size(18.dp)
                                            )
                                        } else {
                                            Text(
                                                text = K.deleteAccountConfirm.tr(),
                                                style = TextStyle(
                                                    fontFamily = ff,
                                                    fontSize = 14.5.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = { currentStep = 2 },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(44.dp),
                                        shape = RoundedCornerShape(50),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = colors.iconBg,
                                            contentColor = colors.textPrimary
                                        ),
                                        elevation = ButtonDefaults.buttonElevation(0.dp),
                                        enabled = !isProcessing
                                    ) {
                                        Text(
                                            text = K.cancelBtn.tr(),
                                            style = TextStyle(
                                                fontFamily = ff,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Normal
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
}
}
