package com.elvan.udukkai.ui.screens.onboarding.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.elvan.udukkai.core.auth.AppScope
import com.elvan.udukkai.core.auth.AuthManager
import com.elvan.udukkai.core.platform.AppBackHandler
import com.elvan.udukkai.ui.screens.onboarding.components.AuthLayout
import com.elvan.udukkai.ui.screens.onboarding.screens.steps.AppLanguageStep
import com.elvan.udukkai.ui.screens.onboarding.screens.steps.BackupCheckStep
import com.elvan.udukkai.ui.screens.onboarding.screens.steps.BillingLanguageStep
import com.elvan.udukkai.ui.screens.onboarding.screens.steps.GreetingStep

enum class WelcomePhase {
    GREETING, CHECKING_BACKUP, RESTORE, LANGUAGE, BILLING_LANGUAGE, BUSINESS_NAME
}

// Flutter Curves.easeOutCubic / Curves.easeIn
private val EaseOutCubic = CubicBezierEasing(0.215f, 0.61f, 0.355f, 1.0f)
private val EaseIn = CubicBezierEasing(0.42f, 0.0f, 1.0f, 1.0f)

/**
 * First-run setup flow matching Flutter's NalvaravuWelcomePage (nalvaravu_thirai_amaippu.dart) 1:1:
 * greeting -> backup check -> (restore) -> app language -> billing language -> business name.
 */
@Composable
fun WelcomeSlideScreen(
    scope: AppScope,
    onSetupComplete: () -> Unit
) {
    var currentPhase by remember { mutableStateOf(WelcomePhase.GREETING) }
    var billingLang by remember { mutableStateOf(AuthManager.getBillingLanguage()) }

    // Flutter PopScope: system back steps back through the flow
    AppBackHandler(enabled = currentPhase == WelcomePhase.BUSINESS_NAME) {
        currentPhase = when (currentPhase) {
            WelcomePhase.BUSINESS_NAME -> WelcomePhase.BILLING_LANGUAGE
            else -> currentPhase
        }
    }

    AuthLayout {
        AnimatedContent(
            targetState = currentPhase,
            transitionSpec = { flutterSwitcherTransition(targetState) },
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth(),
            label = "welcome_phase"
        ) { phase ->
            when (phase) {
                WelcomePhase.GREETING -> GreetingStep(
                    onComplete = { currentPhase = WelcomePhase.BILLING_LANGUAGE }
                )
                // The cloud restores by itself right after sign-in, so there is no backup to look for here
                WelcomePhase.CHECKING_BACKUP -> BackupCheckStep(
                    onBackupFound = { currentPhase = WelcomePhase.RESTORE },
                    onNoBackupFound = { currentPhase = WelcomePhase.BILLING_LANGUAGE }
                )
                WelcomePhase.RESTORE -> RestorePage(
                    onStartFresh = { currentPhase = WelcomePhase.BILLING_LANGUAGE },
                    onRestoreComplete = {
                        AuthManager.refreshProfileStatus()
                        onSetupComplete()
                    }
                )
                WelcomePhase.LANGUAGE -> AppLanguageStep(
                    onBack = { currentPhase = WelcomePhase.CHECKING_BACKUP },
                    onLanguageSelected = { currentPhase = WelcomePhase.BILLING_LANGUAGE }
                )
                WelcomePhase.BILLING_LANGUAGE -> BillingLanguageStep(
                    billingLanguage = billingLang,
                    onLanguageSelected = { billingLang = it },
                    onContinue = {
                        AuthManager.saveBillingLanguage(billingLang)
                        currentPhase = WelcomePhase.BUSINESS_NAME
                    }
                )
                WelcomePhase.BUSINESS_NAME -> GreetingScreen(
                    scope = scope,
                    billingLanguage = billingLang,
                    onSetupComplete = onSetupComplete,
                    onBack = { currentPhase = WelcomePhase.BILLING_LANGUAGE }
                )
            }
        }
    }
}

/**
 * Flutter AnimatedSwitcher(duration: 900ms, reverseDuration: 0): the old step disappears
 * at once; the new one fades in while sliding up 5%. Steps after the backup check start
 * their animation 40% into the 900ms (Interval(0.4, 1.0)).
 */
private fun flutterSwitcherTransition(target: WelcomePhase): ContentTransform {
    val isInitial = target == WelcomePhase.GREETING || target == WelcomePhase.CHECKING_BACKUP
    val delay = if (isInitial) 0 else 360
    val duration = 900 - delay
    return (fadeIn(tween(duration, delay, EaseIn)) +
        slideInVertically(tween(duration, delay, EaseOutCubic)) { height -> (height * 0.05f).toInt() }) togetherWith
        fadeOut(tween(0))
}
