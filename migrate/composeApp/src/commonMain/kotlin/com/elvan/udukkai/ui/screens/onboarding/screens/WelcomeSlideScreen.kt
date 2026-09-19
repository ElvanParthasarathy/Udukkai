package com.elvan.udukkai.ui.screens.onboarding.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.elvan.udukkai.core.auth.AuthManager
import com.elvan.udukkai.ui.screens.onboarding.components.AuthLayout
import com.elvan.udukkai.ui.screens.onboarding.screens.steps.*

enum class WelcomePhase {
    GREETING, CHECKING_BACKUP, RESTORE, LANGUAGE, BILLING_LANGUAGE, BUSINESS_NAME
}

@Composable
fun NalvaravuWelcomeScreen(
    onSetupComplete: () -> Unit
) {
    var currentPhase by remember { 
        mutableStateOf(
            if (AuthManager.missingProfiles.size == 1) WelcomePhase.BUSINESS_NAME 
            else WelcomePhase.GREETING
        ) 
    }
    var billingLang by remember { mutableStateOf(AuthManager.getBillingLanguage()) }

    AuthLayout {
        AnimatedContent(
            targetState = currentPhase,
            transitionSpec = {
                slideInHorizontally(initialOffsetX = { it }) + fadeIn() togetherWith
                slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
            },
            modifier = Modifier.fillMaxSize()
        ) { phase ->
            when (phase) {
                WelcomePhase.GREETING -> {
                    GreetingStep(
                        onComplete = { currentPhase = WelcomePhase.CHECKING_BACKUP }
                    )
                }
                WelcomePhase.CHECKING_BACKUP -> {
                    BackupCheckStep(
                        onBackupFound = { currentPhase = WelcomePhase.RESTORE },
                        onNoBackupFound = { currentPhase = WelcomePhase.LANGUAGE }
                    )
                }
                WelcomePhase.RESTORE -> {
                    RestorePage(
                        onStartFresh = { currentPhase = WelcomePhase.LANGUAGE },
                        onRestoreComplete = onSetupComplete
                    )
                }
                WelcomePhase.LANGUAGE -> {
                    AppLanguageStep(
                        onBack = { currentPhase = WelcomePhase.CHECKING_BACKUP },
                        onLanguageSelected = { currentPhase = WelcomePhase.BILLING_LANGUAGE }
                    )
                }
                WelcomePhase.BILLING_LANGUAGE -> {
                    BillingLanguageStep(
                        billingLanguage = billingLang,
                        onBack = { currentPhase = WelcomePhase.LANGUAGE },
                        onLanguageSelected = { 
                            billingLang = it
                            AuthManager.saveBillingLanguage(it)
                        },
                        onContinue = { currentPhase = WelcomePhase.BUSINESS_NAME }
                    )
                }
                WelcomePhase.BUSINESS_NAME -> {
                    VanakkamThirai(
                        billingLanguage = billingLang,
                        onSetupComplete = onSetupComplete,
                        onBack = if (AuthManager.missingProfiles.size != 1) {
                            { currentPhase = WelcomePhase.BILLING_LANGUAGE }
                        } else null
                    )
                }
            }
        }
    }
}

