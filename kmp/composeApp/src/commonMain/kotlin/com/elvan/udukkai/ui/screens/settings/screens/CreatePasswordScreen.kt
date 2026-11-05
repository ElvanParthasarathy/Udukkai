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
 * கடவுச்சொல் உருவாக்கும் திரை — Create Password Wizard (Matching Neram Mobile).
 * - Step 1: Input new password & confirm -> Live validation rules -> Create password.
 * - Step 2: Success state -> Auto-returns after completion.
 * - Text fields use login screen pill style (ElvanAuthTextField / NeramField).
 */
@Composable
fun CreatePasswordScreen(
    onBack: UnitCallback = {},
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current
    val scope = rememberCoroutineScope()

    var step by remember { mutableIntStateOf(1) } // 1: Input, 2: Success
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val passwordsMatch = newPassword == confirmPassword && newPassword.isNotEmpty()
    val passwordValid = newPassword.length >= 6
    val genericErrorMsg = K.error.tr()

    LaunchedEffect(step) {
        if (step == 2) {
            delay(1500)
            onBack()
        }
    }

    ElvanSubShell(
        title = K.createPassword.tr(),
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

            item(key = "create_password_card") {
                ElvanSectionContainer {
                    ElvanSettingsSection(colors = colors) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 20.dp)
                        ) {
                            when (step) {
                                // ── STEP 1: ENTER PASSWORD & CONFIRM ──
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
                                                tint = colors.textPrimary.copy(alpha = 0.7f),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = K.signedInWithGoogleCreatePassword.tr(),
                                                style = TextStyle(
                                                    fontFamily = ff,
                                                    fontSize = 13.sp,
                                                    lineHeight = 18.sp,
                                                    fontWeight = FontWeight.Normal
                                                ),
                                                color = colors.textPrimary.copy(alpha = 0.75f)
                                            )
                                        }
                                    }

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
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = if (passwordValid) MaterialSymbols.Rounded.CheckCircleFill else MaterialSymbols.Rounded.RadioButtonUnchecked,
                                                    contentDescription = null,
                                                    tint = if (passwordValid) colors.accent else colors.textPrimary.copy(alpha = 0.3f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = K.atLeast6Chars.tr(),
                                                    style = TextStyle(fontFamily = ff, fontSize = 12.5.sp, fontWeight = FontWeight.Normal),
                                                    color = if (passwordValid) colors.textPrimary else colors.textPrimary.copy(alpha = 0.45f)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = if (passwordsMatch) MaterialSymbols.Rounded.CheckCircleFill else MaterialSymbols.Rounded.RadioButtonUnchecked,
                                                    contentDescription = null,
                                                    tint = if (passwordsMatch) colors.accent else colors.textPrimary.copy(alpha = 0.3f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = K.passwordsMatch.tr(),
                                                    style = TextStyle(fontFamily = ff, fontSize = 12.5.sp, fontWeight = FontWeight.Normal),
                                                    color = if (passwordsMatch) colors.textPrimary else colors.textPrimary.copy(alpha = 0.45f)
                                                )
                                            }
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
                                                val res = AuthManager.createPassword(newPassword)
                                                isProcessing = false
                                                if (res.isSuccess) {
                                                    step = 2
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
                                                text = K.createPassword.tr(),
                                                style = TextStyle(fontFamily = ff, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
                                            )
                                        }
                                    }
                                }

                                // ── STEP 2: SUCCESS STATE ──
                                2 -> {
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
                                            text = K.passwordCreated.tr(),
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

private typealias UnitCallback = () -> Unit
