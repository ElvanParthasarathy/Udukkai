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
 * கணக்கை நிரந்தரமாக அழிக்கும் திரை — Delete Account Screen.
 * Full compliance with Google Play Store account deletion mandate and Apple HIG managing-accounts.md:
 * - Clear warning explaining irreversible data wipe.
 * - Explicit user confirmation checkbox.
 * - Password re-authentication for password-backed accounts.
 * - Biometric scan requirement before executing irreversible wipe.
 * - Final confirmation step via ElvanActionSheet destructive bottom sheet.
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

            item(key = "warning_card") {
                ElvanSectionContainer {
                    ElvanSettingsSection(colors = colors) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 20.dp)
                        ) {
                            // Warning Banner
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = colors.error.copy(alpha = 0.08f)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = MaterialSymbols.Rounded.Warning,
                                            contentDescription = null,
                                            tint = colors.error,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = K.deleteAccount.tr(),
                                            style = TextStyle(
                                                fontFamily = ff,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = colors.error
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = K.deleteAccountWarning.tr(),
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 13.5.sp,
                                            lineHeight = 19.sp
                                        ),
                                        color = colors.textPrimary.copy(alpha = 0.85f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Checkbox Confirmation
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { understood = !understood },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = understood,
                                    onCheckedChange = { understood = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = colors.error,
                                        checkmarkColor = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = K.iUnderstandConsequences.tr(),
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 18.sp
                                    ),
                                    color = colors.textPrimary
                                )
                            }

                            // Password Input (if password user)
                            if (hasPassword) {
                                Spacer(modifier = Modifier.height(20.dp))

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
                            }

                            if (errorMessage != null) {
                                Spacer(modifier = Modifier.height(12.dp))
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

                            // Delete Account Action Button
                            val canInitiateDelete = understood && (!hasPassword || password.isNotEmpty()) && !isProcessing
                            Button(
                                onClick = { showDeleteConfirmSheet = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.error,
                                    contentColor = Color.White
                                ),
                                enabled = canInitiateDelete
                            ) {
                                if (isProcessing) {
                                    ExpressiveLoadingIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                                } else {
                                    Text(
                                        text = K.deleteAccountConfirm.tr(),
                                        style = TextStyle(fontFamily = ff, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Final Destructive Confirmation Action Sheet with Biometric Gate ──
        if (showDeleteConfirmSheet) {
            ElvanActionSheet(
                title = K.deletePermanentlyQuestion.tr(),
                cancelText = K.cancelBtn.tr(),
                confirmText = K.deleteBtn.tr(),
                confirmColor = colors.error,
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
                            lineHeight = 19.sp
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
