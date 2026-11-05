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
import androidx.compose.ui.draw.clip
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
 * கணக்கை நிரந்தரமாக அழிக்கும் திரை — Step-by-Step Account Deletion Sub-page.
 * Minimal, restrained layout without alarmist neon colors:
 * - Step 1: Clear consequence explanation + confirmation checkbox.
 * - Step 2: Verification (Password input if password-backed, biometric ready).
 * - Step 3: Calm monochrome action button + final confirmation sheet.
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

    var understood by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var showDeleteConfirmSheet by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val accountDeletedMsg = K.accountDeleted.tr()
    val errorGeneric = K.error.tr()
    val biometricPromptTitle = K.biometricVerifyTitle.tr()
    val cancelText = K.cancelBtn.tr()

    val canInitiateDelete = understood && (!hasPassword || password.isNotEmpty()) && !isProcessing

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

            // ── Step 1: Consequences (படி 1: விளைவுகள்) ──
            item(key = "step1_card") {
                ElvanSectionContainer {
                    ElvanSettingsSection(
                        title = K.step1Consequences.tr(),
                        colors = colors
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                        ) {
                            Text(
                                text = K.deleteAccountWarning.tr(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 12.5.sp,
                                    lineHeight = 18.sp
                                ),
                                color = colors.textPrimary.copy(alpha = 0.65f)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Checkbox Confirmation
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { understood = !understood }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = understood,
                                    onCheckedChange = { understood = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = if (colors.isDark) Color.White else Color.Black,
                                        checkmarkColor = if (colors.isDark) Color.Black else Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = K.iUnderstandConsequences.tr(),
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = colors.textPrimary
                                )
                            }
                        }
                    }
                }
            }

            // ── Step 2: Verification (படி 2: சரிபார்ப்பு) ──
            if (hasPassword) {
                item(key = "step2_card") {
                    ElvanSectionContainer {
                        ElvanSettingsSection(
                            title = K.step2Verification.tr(),
                            colors = colors
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 16.dp)
                            ) {
                                ElvanSettingsTextField(
                                    label = K.verifyIdentityForDeletion.tr(),
                                    value = password,
                                    onValueChange = {
                                        password = it
                                        errorMessage = null
                                    },
                                    placeholder = K.enterCurrentPassword.tr(),
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
                            }
                        }
                    }
                }
            }

            // ── Step 3: Deletion Action (படி 3: கணக்கு அழித்தல்) ──
            item(key = "step3_card") {
                ElvanSectionContainer {
                    ElvanSettingsSection(
                        title = K.step3DeleteAccount.tr(),
                        colors = colors
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                        ) {
                            Button(
                                onClick = { showDeleteConfirmSheet = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (colors.isDark) Color.White else Color.Black,
                                    contentColor = if (colors.isDark) Color.Black else Color.White
                                ),
                                elevation = ButtonDefaults.buttonElevation(0.dp),
                                enabled = canInitiateDelete
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
                        }
                    }
                }
            }
        }

        // ── Final Destructive Confirmation Action Sheet ──
        if (showDeleteConfirmSheet) {
            ElvanActionSheet(
                title = K.deletePermanentlyQuestion.tr(),
                cancelText = K.cancelBtn.tr(),
                confirmText = K.deleteBtn.tr(),
                confirmColor = colors.textPrimary,
                onDismissRequest = { showDeleteConfirmSheet = false },
                onConfirm = {
                    showDeleteConfirmSheet = false
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
                colors = colors,
                customContent = {
                    Text(
                        text = K.deletePermanentlyDesc.tr(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 13.5.sp,
                            lineHeight = 18.sp
                        ),
                        color = colors.textPrimary.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            )
        }
    }
}
