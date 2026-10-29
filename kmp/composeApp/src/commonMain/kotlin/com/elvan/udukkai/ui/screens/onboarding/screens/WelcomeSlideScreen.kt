package com.elvan.udukkai.ui.screens.onboarding.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.elvan.udukkai.core.auth.AppScope
import com.elvan.udukkai.core.auth.AuthManager
import com.elvan.udukkai.core.platform.AppBackHandler
import com.elvan.udukkai.ui.screens.onboarding.components.AuthBackdrop
import com.elvan.udukkai.ui.screens.onboarding.screens.steps.BillingLanguageStep

enum class WelcomePhase {
    BILLING_LANGUAGE, BUSINESS_NAME
}

private val EaseOutCubic = CubicBezierEasing(0.215f, 0.61f, 0.355f, 1.0f)
private val EaseInCubic = CubicBezierEasing(0.55f, 0.055f, 0.675f, 0.19f)

/**
 * First-run setup of a new account, in the same look as the sign-in pages: the billing language,
 * then the business name. The app language was already chosen on the first page, and the cloud restores an
 * existing account by itself, so there is nothing else to ask.
 */
@Composable
fun WelcomeSlideScreen(
    scope: AppScope,
    onSetupComplete: () -> Unit
) {
    var currentPhase by remember { mutableStateOf(WelcomePhase.BILLING_LANGUAGE) }
    var billingLang by remember { mutableStateOf(AuthManager.getBillingLanguage()) }

    // System back steps back from the business name to the billing language
    AppBackHandler(enabled = currentPhase == WelcomePhase.BUSINESS_NAME) {
        currentPhase = WelcomePhase.BILLING_LANGUAGE
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AuthBackdrop()
        AnimatedContent(
            targetState = currentPhase,
            transitionSpec = {
                fadeIn(tween(300, easing = EaseOutCubic)) togetherWith fadeOut(tween(200, easing = EaseInCubic))
            },
            modifier = Modifier.fillMaxSize(),
            label = "setup_phase"
        ) { phase ->
            when (phase) {
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
                    onSetupComplete = onSetupComplete
                )
            }
        }
    }
}
