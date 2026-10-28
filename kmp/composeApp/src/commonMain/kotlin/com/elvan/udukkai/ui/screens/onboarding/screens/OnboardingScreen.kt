package com.elvan.udukkai.ui.screens.onboarding.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.auth.AuthManager
import com.elvan.udukkai.core.auth.GoogleSignInResult
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.ui.screens.onboarding.components.*
import kotlinx.coroutines.launch

/**
 * Login page: only the approved Google accounts get in; there is no sign-up.
 */
@Composable
fun UllnuzhaivuThirai(
    onBack: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var errorText by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val notAllowedMsg = K.accountNotAllowed.tr()
    val failedMsg = K.signInFailedTryAgain.tr()
    val scope = rememberCoroutineScope()

    AuthLayout {
        AuthAnimatedElement(delayIndex = 0) {
            AuthBackButton(onClick = onBack)
        }

        Spacer(modifier = Modifier.height(16.dp))

        AuthHeader(
            title = K.udukkai.tr(),
            subtitle = K.loginToAccessApp.tr()
        )

        Spacer(modifier = Modifier.height(40.dp))

        errorText?.let {
            Text(
                text = it,
                color = Color(0xFFBA1A1A),
                fontSize = 13.sp,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
            )
        }

        AuthButton(
            text = K.signInWithGoogle.tr(),
            loading = loading,
            onClick = {
                loading = true
                errorText = null
                scope.launch {
                    when (AuthManager.signInWithGoogle()) {
                        is GoogleSignInResult.Success -> onLoginSuccess()
                        is GoogleSignInResult.NotAllowed -> errorText = notAllowedMsg
                        GoogleSignInResult.Cancelled -> {}
                        GoogleSignInResult.Failed -> errorText = failedMsg
                    }
                    loading = false
                }
            }
        )
    }
}
