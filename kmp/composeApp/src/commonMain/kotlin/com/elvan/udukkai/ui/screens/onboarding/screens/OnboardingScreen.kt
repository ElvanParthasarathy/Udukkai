package com.elvan.udukkai.ui.screens.onboarding.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.theme.ElvanSansFontFamily
import com.elvan.udukkai.core.auth.AllowedAccounts
import com.elvan.udukkai.core.auth.AppScope
import com.elvan.udukkai.core.auth.AuthManager
import com.elvan.udukkai.core.auth.GoogleSignInResult
import com.elvan.udukkai.core.auth.platformSendPasswordReset
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.ui.components.ExpressiveLoadingIndicator
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import kotlinx.coroutines.launch
import migrate.composeapp.generated.resources.Res
import migrate.composeapp.generated.resources.factory_logo_vector
import migrate.composeapp.generated.resources.textiles_logo_vector
import org.jetbrains.compose.resources.painterResource

private val ErrorRed = Color(0xFFBA1A1A)

/**
 * Login page laid out like Neram's: email and password, Log in, "or", then Google.
 * Only the approved accounts get in and there is no sign-up.
 */
@Composable
fun UllnuzhaivuThirai(
    onBack: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailBusy by remember { mutableStateOf(false) }
    var googleBusy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(true) }
    var showReset by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val notAllowedMsg = K.accountNotAllowed.tr()
    val failedMsg = K.signInFailedTryAgain.tr()
    val wrongMsg = K.wrongEmailOrPassword.tr()
    val enterEmailMsg = K.enterEmailFirst.tr()
    val requiredMsg = K.passwordRequired.tr()
    val busy = emailBusy || googleBusy

    fun fail(text: String) { message = text; isError = true }

    fun handle(result: GoogleSignInResult, failure: String) {
        when (result) {
            is GoogleSignInResult.Success -> onLoginSuccess()
            is GoogleSignInResult.NotAllowed, is GoogleSignInResult.EmailMismatch -> fail(notAllowedMsg)
            GoogleSignInResult.Cancelled -> {}
            GoogleSignInResult.Failed -> fail(failure)
        }
    }

    fun logIn() {
        if (email.isBlank()) { fail(enterEmailMsg); return }
        if (password.isEmpty()) { fail(requiredMsg); return }
        emailBusy = true
        message = null
        scope.launch {
            handle(AuthManager.signInWithEmail(email, password), wrongMsg)
            emailBusy = false
        }
    }

    com.elvan.udukkai.core.platform.AppBackHandler(enabled = showReset) { showReset = false }
    if (showReset) {
        ResetPasswordPage(initialEmail = email, onBack = { showReset = false })
        return
    }

    NeramPage(
        pinActions = false,
        header = {
            NeramElement(delayIndex = 0) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val isFactory = (AuthManager.appScope == AppScope.FACTORY)
                    Icon(
                        painter = painterResource(if (isFactory) Res.drawable.factory_logo_vector else Res.drawable.textiles_logo_vector),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(if (LocalAuthLandscape.current) 64.dp else 96.dp)
                            .clip(RoundedCornerShape(24.dp))
                    )
                    Spacer(Modifier.height(if (LocalAuthLandscape.current) 12.dp else 18.dp))
                    NeramHeader(
                        title = if (isFactory) K.udukkaiFactory.tr() else K.udukkaiTextiles.tr(),
                        subtitle = K.loginToAccessApp.tr()
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

        }
    ) {
            NeramElement(delayIndex = 1, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    NeramField(
                        value = email,
                        onValueChange = { email = it; message = null },
                        placeholder = K.emailAddress.tr(),
                        contentType = androidx.compose.ui.autofill.ContentType.EmailAddress,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                    )
                    Spacer(Modifier.height(if (LocalAuthLandscape.current) 8.dp else 16.dp))
                    NeramField(
                        value = password,
                        onValueChange = { password = it; message = null },
                        placeholder = K.password.tr(),
                        isPassword = true,
                        contentType = androidx.compose.ui.autofill.ContentType.Password,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { logIn() })
                    )
                    message?.let {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = it,
                            color = if (isError) ErrorRed else NeramColors.textSecondary(),
                            style = TextStyle(fontFamily = ElvanSansFontFamily, fontSize = 13.sp, lineHeight = 18.sp),
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(if (LocalAuthLandscape.current) 8.dp else 16.dp))

            NeramElement(delayIndex = 2, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    NeramButton(text = K.logIn.tr(), onClick = { logIn() }, loading = emailBusy, enabled = !busy)

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = K.forgotPassword.tr(),
                        style = TextStyle(fontFamily = ElvanSansFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold),
                        color = NeramColors.textSecondary(),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = NeramColors.textPrimary(), bounded = true),
                                role = androidx.compose.ui.semantics.Role.Button
                            ) { showReset = true }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    )

                    Spacer(Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), thickness = 0.5.dp, color = NeramColors.divider())
                        Text(
                            text = K.orDivider.tr(),
                            style = TextStyle(fontFamily = ElvanSansFontFamily, fontSize = 12.sp),
                            color = NeramColors.textMuted(),
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), thickness = 0.5.dp, color = NeramColors.divider())
                    }

                    Spacer(Modifier.height(if (LocalAuthLandscape.current) 8.dp else 16.dp))

                    GoogleButton(
                        text = K.signInWithGoogle.tr(),
                        loading = googleBusy,
                        enabled = !busy,
                        onClick = {
                            googleBusy = true
                            message = null
                            scope.launch {
                                handle(AuthManager.signInWithGoogle(), failedMsg)
                                googleBusy = false
                            }
                        }
                    )
                }
            }
    }
}

@Composable
internal fun NeramField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    contentType: androidx.compose.ui.autofill.ContentType? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var visible by remember { mutableStateOf(false) }
    val primary = NeramColors.textPrimary()
    val secondary = NeramColors.textSecondary()
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = secondary, style = TextStyle(fontFamily = ElvanSansFontFamily, fontSize = 14.sp, color = secondary)) },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { visible = !visible }) {
                    Icon(
                        imageVector = if (visible) MaterialSymbols.Rounded.VisibilityOff else MaterialSymbols.Rounded.Visibility,
                        contentDescription = (if (visible) K.hidePassword else K.showPassword).tr(),
                        tint = secondary
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        shape = RoundedCornerShape(50),
        colors = TextFieldDefaults.colors(
            focusedTextColor = primary,
            unfocusedTextColor = primary,
            focusedContainerColor = NeramColors.inputBackground(),
            unfocusedContainerColor = NeramColors.inputBackground(),
            cursorColor = primary,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        textStyle = TextStyle(fontFamily = ElvanSansFontFamily, fontSize = 16.sp, color = primary),
        singleLine = true,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        modifier = Modifier.fillMaxWidth().neramShadow().then(if (contentType != null) Modifier.semantics { this.contentType = contentType } else Modifier)
    )
}

@Composable
private fun GoogleButton(text: String, loading: Boolean, enabled: Boolean, onClick: () -> Unit) {
    if (LocalAuthKeyboardVisible.current) return
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .neramPill()
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = NeramColors.textPrimary(), bounded = true),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            ExpressiveLoadingIndicator(modifier = Modifier.size(24.dp), color = NeramColors.textPrimary())
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("G", style = TextStyle(fontFamily = ElvanSansFontFamily, fontSize = 22.sp, fontWeight = FontWeight.Bold), color = Color(0xFFDB4437), modifier = Modifier.padding(end = 12.dp))
                Text(text, style = TextStyle(fontFamily = ElvanSansFontFamily, fontSize = 16.sp, fontWeight = FontWeight.Medium), color = NeramColors.textPrimary())
            }
        }
    }
}

/**
 * Forgot password: its own page in the log-in page's layout. The mail is sent only to the approved accounts, and the page
 * says so plainly when it has gone, when the address is not one of them, or when there is no internet.
 */
@Composable
private fun ResetPasswordPage(initialEmail: String, onBack: () -> Unit) {
    var email by remember { mutableStateOf(initialEmail) }
    var busy by remember { mutableStateOf(false) }
    var sent by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val enterEmailMsg = K.enterEmailFirst.tr()
    val notAllowedMsg = K.accountNotAllowed.tr()
    val sentMsg = K.resetMailSent.tr()
    val notSentMsg = K.resetNotSent.tr()

    fun send() {
        if (email.isBlank()) { message = enterEmailMsg; return }
        busy = true
        message = null
        scope.launch {
            val ok = try { platformSendPasswordReset(email.trim()) } catch (_: Throwable) { false }
            busy = false
            message = when {
                !AllowedAccounts.isAllowed(email) -> notAllowedMsg
                ok -> { sent = true; null }
                else -> notSentMsg
            }
        }
    }

    NeramPage(
        pinActions = false,
        header = {
            NeramElement(delayIndex = 0) {
                NeramHeader(title = K.resetPasswordTitle.tr(), subtitle = if (sent) sentMsg else K.resetPasswordHint.tr())
            }

            Spacer(Modifier.height(16.dp))

        }
    ) {
            if (!sent) {
                NeramElement(delayIndex = 1, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        NeramField(
                            value = email,
                            onValueChange = { email = it; message = null },
                            placeholder = K.emailAddress.tr(),
                            contentType = androidx.compose.ui.autofill.ContentType.EmailAddress,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { send() })
                        )
                        message?.let {
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = it,
                                color = ErrorRed,
                                style = TextStyle(fontFamily = ElvanSansFontFamily, fontSize = 13.sp, lineHeight = 18.sp),
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                NeramElement(delayIndex = 2, modifier = Modifier.fillMaxWidth()) {
                    NeramButton(text = K.sendResetLink.tr(), onClick = { send() }, loading = busy, enabled = !busy)
                }
                Spacer(Modifier.height(8.dp))
            }

            Text(
                text = K.backToLogIn.tr(),
                style = TextStyle(fontFamily = ElvanSansFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold),
                color = NeramColors.textSecondary(),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = NeramColors.textPrimary(), bounded = true),
                        role = androidx.compose.ui.semantics.Role.Button,
                        onClick = onBack
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
    }
}
