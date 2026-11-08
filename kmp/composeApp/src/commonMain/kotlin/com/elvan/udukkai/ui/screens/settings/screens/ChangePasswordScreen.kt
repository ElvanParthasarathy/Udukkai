package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.elvan.udukkai.core.auth.AuthManager
import com.elvan.udukkai.core.auth.AppScope
import com.elvan.udukkai.ui.components.shell.ElvanPill
import com.elvan.udukkai.ui.components.shell.ElvanTopBarIconButton
import com.elvan.udukkai.ui.screens.onboarding.components.AuthBackdrop
import com.elvan.udukkai.ui.screens.onboarding.components.LocalHasAuthBackdrop
import migrate.composeapp.generated.resources.Res
import migrate.composeapp.generated.resources.factory_logo_vector
import migrate.composeapp.generated.resources.textiles_logo_vector
import org.jetbrains.compose.resources.painterResource
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.ElvanSansFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.ElvanSnackbar
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.onboarding.screens.NeramButton
import com.elvan.udukkai.ui.screens.onboarding.screens.NeramColors
import com.elvan.udukkai.ui.screens.onboarding.screens.NeramElement
import com.elvan.udukkai.ui.screens.onboarding.screens.NeramField
import com.elvan.udukkai.ui.screens.onboarding.screens.NeramHeader
import com.elvan.udukkai.ui.screens.onboarding.screens.NeramPage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Password change in the log-in / forgot-password page layout (NeramPage, NeramField, NeramButton), on its own page
 * with no settings shell around it. Back (the link or the system Back) returns to the page it was opened from.
 * Steps: 1 verify the current password, 2 new password + confirm with live rules, 3 done, then it returns by itself.
 */
@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
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

    fun verify() {
        if (currentPassword.isEmpty()) { errorMessage = incorrectPasswordMsg; return }
        isProcessing = true
        errorMessage = null
        scope.launch {
            val res = AuthManager.verifyPassword(currentPassword)
            isProcessing = false
            if (res.isSuccess) step = 2 else errorMessage = incorrectPasswordMsg
        }
    }

    fun update() {
        isProcessing = true
        errorMessage = null
        scope.launch {
            val res = AuthManager.changePassword(currentPassword, newPassword)
            isProcessing = false
            if (res.isSuccess) step = 3 else errorMessage = res.exceptionOrNull()?.message ?: genericErrorMsg
        }
    }

    Box(Modifier.fillMaxSize()) {
    AuthBackdrop() // the log-in page's animated shapes
    CompositionLocalProvider(LocalHasAuthBackdrop provides true) {
    NeramPage(
        bottom = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (step) {
                    1 -> NeramButton(text = K.verifyAndContinue.tr(), onClick = { verify() }, loading = isProcessing, enabled = currentPassword.isNotEmpty() && !isProcessing)
                    2 -> NeramButton(text = K.updatePassword.tr(), onClick = { update() }, loading = isProcessing, enabled = passwordsMatch && passwordValid && !isProcessing)
                }
            }
        }
    ) {
        if (step != 3) {
            val isFactory = AuthManager.appScope == AppScope.FACTORY
            Icon(
                painter = painterResource(if (isFactory) Res.drawable.factory_logo_vector else Res.drawable.textiles_logo_vector),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(96.dp).clip(RoundedCornerShape(24.dp))
            )
            Spacer(Modifier.height(18.dp))
        }
        when (step) {
            1 -> {
                NeramElement(delayIndex = 0) { NeramHeader(title = K.enterCurrentPassword.tr(), subtitle = K.confirmIdentityBeforeNewPassword.tr()) }
                Spacer(Modifier.height(16.dp))
                NeramElement(delayIndex = 1, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth()) {
                        NeramField(
                            value = currentPassword,
                            onValueChange = { currentPassword = it; errorMessage = null },
                            placeholder = K.currentPassword.tr(),
                            isPassword = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { verify() })
                        )
                        ErrorLine(errorMessage, colors)
                        Text(
                            text = K.forgotPassword.tr(),
                            style = TextStyle(fontFamily = ElvanSansFontFamily, fontSize = 13.5.sp, fontWeight = FontWeight.Medium),
                            color = NeramColors.textSecondary(),
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(color = NeramColors.textPrimary(), bounded = true),
                                    role = Role.Button
                                ) {
                                    val email = AuthManager.signedInEmail
                                    if (!email.isNullOrBlank()) {
                                        scope.launch {
                                            com.elvan.udukkai.core.auth.platformSendPasswordReset(email)
                                            ElvanSnackbar.show(resetSentMsg)
                                        }
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                }
            }
            2 -> {
                NeramElement(delayIndex = 0) { NeramHeader(title = K.createNewPassword.tr(), subtitle = K.atLeast6Chars.tr()) }
                Spacer(Modifier.height(16.dp))
                NeramElement(delayIndex = 1, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        NeramField(
                            value = newPassword,
                            onValueChange = { newPassword = it; errorMessage = null },
                            placeholder = K.newPassword.tr(),
                            isPassword = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                        NeramField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it; errorMessage = null },
                            placeholder = K.confirmPassword.tr(),
                            isPassword = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { if (passwordsMatch && passwordValid && !isProcessing) update() })
                        )
                        Column(Modifier.padding(start = 16.dp, top = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            PasswordValidationRow(K.atLeast6Chars.tr(), passwordValid, colors)
                            PasswordValidationRow(K.passwordsMatch.tr(), passwordsMatch, colors)
                        }
                        ErrorLine(errorMessage, colors)
                    }
                }
            }
            else -> NeramElement(delayIndex = 0) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.CheckCircleFill,
                        contentDescription = null,
                        tint = NeramColors.textPrimary(),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    NeramHeader(title = K.passwordUpdated.tr(), subtitle = K.returningToSecuritySettings.tr())
                }
            }
        }
    }
    }
    // The chevron where the view pages have it, in its floating circle
    ElvanPill(
        liftProgress = 1f,
        colors = colors,
        modifier = Modifier.statusBarsPadding().padding(start = 16.dp, top = 12.dp).size(50.dp)
    ) {
        ElvanTopBarIconButton(onClick = onBack, size = 50.dp) {
            Icon(
                imageVector = MaterialSymbols.Rounded.ArrowBack,
                contentDescription = K.back.tr(),
                tint = colors.textPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
    }
}

@Composable
private fun ErrorLine(message: String?, colors: ShellColors) {
    message?.let {
        Text(
            text = it,
            color = colors.danger,
            style = TextStyle(fontFamily = ElvanSansFontFamily, fontSize = 13.sp, lineHeight = 18.sp),
            modifier = Modifier.padding(start = 16.dp, top = 10.dp)
        )
    }
}

@Composable
private fun PasswordValidationRow(text: String, isValid: Boolean, colors: ShellColors) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (isValid) MaterialSymbols.Rounded.CheckCircleFill else MaterialSymbols.Rounded.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isValid) colors.textPrimary else colors.textPrimary.copy(alpha = 0.3f),
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = TextStyle(fontFamily = ElvanSansFontFamily, fontSize = 12.5.sp),
            color = if (isValid) colors.textPrimary else colors.textPrimary.copy(alpha = 0.45f)
        )
    }
}
