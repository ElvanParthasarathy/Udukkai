package com.elvan.udukkai.ui.screens.onboarding.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.theme.ElvanSansFontFamily
import com.elvan.udukkai.core.auth.AuthManager
import com.elvan.udukkai.core.auth.GoogleSignInResult
import com.elvan.udukkai.core.auth.platformSendPasswordReset
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.ui.components.ExpressiveLoadingIndicator
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import kotlinx.coroutines.launch

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
    val scope = rememberCoroutineScope()

    val notAllowedMsg = K.accountNotAllowed.tr()
    val failedMsg = K.signInFailedTryAgain.tr()
    val wrongMsg = K.wrongEmailOrPassword.tr()
    val enterEmailMsg = K.enterEmailFirst.tr()
    val resetSentMsg = K.resetMailSent.tr()
    val requiredMsg = K.passwordRequired.tr()
    val busy = emailBusy || googleBusy

    fun fail(text: String) { message = text; isError = true }

    fun handle(result: GoogleSignInResult, failure: String) {
        when (result) {
            is GoogleSignInResult.Success -> onLoginSuccess()
            is GoogleSignInResult.NotAllowed -> fail(notAllowedMsg)
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

    BoxWithConstraints(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).imePadding()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = maxHeight)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NeramElement(delayIndex = 0) {
                NeramHeader(title = K.udukkai.tr(), subtitle = K.loginToAccessApp.tr())
            }

            Spacer(Modifier.height(16.dp))

            NeramElement(delayIndex = 1, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    NeramField(
                        value = email,
                        onValueChange = { email = it; message = null },
                        placeholder = K.emailAddress.tr(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                    )
                    Spacer(Modifier.height(16.dp))
                    NeramField(
                        value = password,
                        onValueChange = { password = it; message = null },
                        placeholder = K.password.tr(),
                        isPassword = true,
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

            Spacer(Modifier.height(16.dp))

            NeramElement(delayIndex = 2, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    NeramButton(text = K.logIn.tr(), onClick = { logIn() }, loading = emailBusy, enabled = !busy)

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = K.forgotPassword.tr(),
                        style = TextStyle(fontFamily = ElvanSansFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold),
                        color = NeramColors.textSecondary(),
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (email.isBlank()) {
                                    fail(enterEmailMsg)
                                } else {
                                    scope.launch {
                                        if (platformSendPasswordReset(email)) {
                                            message = resetSentMsg
                                            isError = false
                                        } else {
                                            fail(notAllowedMsg)
                                        }
                                    }
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    )

                    Spacer(Modifier.height(10.dp))

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

                    Spacer(Modifier.height(16.dp))

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
}

@Composable
internal fun NeramField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var visible by remember { mutableStateOf(false) }
    val primary = NeramColors.textPrimary()
    val secondary = NeramColors.textSecondary()
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = secondary, style = TextStyle(fontFamily = ElvanSansFontFamily, fontSize = 16.sp, color = secondary)) },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { visible = !visible }) {
                    Icon(
                        imageVector = if (visible) MaterialSymbols.Rounded.VisibilityOff else MaterialSymbols.Rounded.Visibility,
                        contentDescription = null,
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
        modifier = Modifier.fillMaxWidth().neramShadow()
    )
}

@Composable
private fun GoogleButton(text: String, loading: Boolean, enabled: Boolean, onClick: () -> Unit) {
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
