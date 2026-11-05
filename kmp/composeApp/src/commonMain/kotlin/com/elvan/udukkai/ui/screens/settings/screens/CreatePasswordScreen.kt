package com.elvan.udukkai.ui.screens.settings.screens

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
import androidx.compose.foundation.clickable
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
 * For users who initially logged in with Google OAuth to establish email/password access.
 */
@Composable
fun CreatePasswordScreen(
    onBack: () -> Unit = {},
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current
    val scope = rememberCoroutineScope()
    val errorGeneric = K.error.tr()
    val btnTextClr = if (colors.isDark) Color.Black else Color.White

    var step by remember { mutableIntStateOf(1) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val passwordValid = newPassword.length >= 6
    val passwordsMatch = newPassword.isNotEmpty() && newPassword == confirmPassword

    LaunchedEffect(step) {
        if (step == 2) {
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
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = K.signedInWithGoogleCreatePassword.tr(),
                                            style = TextStyle(
                                                fontFamily = ff,
                                                fontSize = 13.sp,
                                                lineHeight = 18.sp
                                            ),
                                            color = colors.textPrimary.copy(alpha = 0.8f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

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

                                Spacer(modifier = Modifier.height(12.dp))

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

                                Spacer(modifier = Modifier.height(16.dp))

                                // Criteria Box
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
                                        modifier = Modifier.padding(start = 12.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = {
                                        isProcessing = true
                                        scope.launch {
                                            val res = AuthManager.createPassword(newPassword)
                                            isProcessing = false
                                            if (res.isSuccess) {
                                                step = 2
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
                                            text = K.createPassword.tr(),
                                            style = TextStyle(fontFamily = ff, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                        )
                                    }
                                }
                            }

                            2 -> {
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
                                        text = K.passwordCreated.tr(),
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = colors.textPrimary
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = K.canNowSignInWithEmail.tr(),
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Normal
                                        ),
                                        color = colors.textPrimary.copy(alpha = 0.55f)
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
