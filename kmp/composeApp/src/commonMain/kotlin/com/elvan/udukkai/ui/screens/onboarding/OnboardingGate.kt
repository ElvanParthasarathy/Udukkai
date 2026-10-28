package com.elvan.udukkai.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.elvan.udukkai.core.auth.AppScope
import com.elvan.udukkai.core.auth.AuthManager
import com.elvan.udukkai.core.platform.AppBackHandler
import com.elvan.udukkai.core.sync.getFirebaseSyncManager
import com.elvan.udukkai.localization.Language
import com.elvan.udukkai.localization.LanguageManager
import com.elvan.udukkai.ui.screens.onboarding.screens.LanguageSelectScreen
import com.elvan.udukkai.ui.screens.onboarding.screens.UllnuzhaivuThirai
import com.elvan.udukkai.ui.screens.onboarding.screens.WelcomeScreen
import com.elvan.udukkai.ui.screens.onboarding.screens.WelcomeSlideScreen

private enum class GateStage { LANGUAGE, WELCOME, LOGIN, LOADING, SETUP, APP }

// Flutter Curves.easeOutCubic / Curves.easeInCubic
private val EaseOutCubic = CubicBezierEasing(0.215f, 0.61f, 0.355f, 1.0f)
private val EaseInCubic = CubicBezierEasing(0.55f, 0.055f, 0.675f, 0.19f)

/**
 * App entry flow matching Flutter's main.dart home builder:
 * not logged in -> WelcomePage (-> LoginPage), logged in without a business ->
 * NalvaravuWelcomePage setup flow, otherwise the app itself.
 */
@Composable
fun OnboardingGate(
    scope: AppScope,
    content: @Composable () -> Unit
) {
    // Synchronous so the first frame already knows the login state (no welcome-page flash)
    remember {
        AuthManager.appScope = scope
        AuthManager.init()
    }

    var showLogin by remember { mutableStateOf(false) }
    // A fresh install asks for the language first; one that already has a saved language skips it
    var languageDone by remember { mutableStateOf(LanguageManager.currentLanguage != Language.SYSTEM) }

    val stage = when {
        !AuthManager.isLoggedIn -> when {
            !languageDone -> GateStage.LANGUAGE
            showLogin -> GateStage.LOGIN
            else -> GateStage.WELCOME
        }
        AuthManager.isProfilesLoading -> GateStage.LOADING
        // A new phone: wait for the business profiles that are being restored instead of asking to create one
        !AuthManager.isSetupComplete(scope) && !getFirebaseSyncManager().profilesRestored.value -> GateStage.LOADING
        !AuthManager.isSetupComplete(scope) -> GateStage.SETUP
        else -> GateStage.APP
    }

    AppBackHandler(enabled = stage == GateStage.LOGIN) { showLogin = false }

    AnimatedContent(
        targetState = stage,
        transitionSpec = {
            // Welcome <-> Login is a Navigator push/pop with a 300ms fade;
            // every other change is the root AnimatedSwitcher (600ms).
            val isLoginRoute = (initialState == GateStage.WELCOME && targetState == GateStage.LOGIN) ||
                (initialState == GateStage.LOGIN && targetState == GateStage.WELCOME)
            if (isLoginRoute) {
                fadeIn(tween(300)) togetherWith fadeOut(tween(300))
            } else {
                fadeIn(tween(600, easing = EaseOutCubic)) togetherWith fadeOut(tween(600, easing = EaseInCubic))
            }
        },
        modifier = Modifier.fillMaxSize(),
        label = "onboarding_gate"
    ) { s ->
        when (s) {
            GateStage.LANGUAGE -> LanguageSelectScreen(onContinue = { languageDone = true })
            GateStage.WELCOME -> WelcomeScreen(onNavigateToLogin = { showLogin = true })
            GateStage.LOGIN -> UllnuzhaivuThirai(
                onBack = { showLogin = false },
                onLoginSuccess = {
                    showLogin = false
                    AuthManager.refreshProfileStatus()
                }
            )
            // Flutter: Scaffold(backgroundColor: Colors.black) while profiles load
            GateStage.LOADING -> Box(Modifier.fillMaxSize().background(Color.Black))
            GateStage.SETUP -> WelcomeSlideScreen(
                scope = scope,
                onSetupComplete = { AuthManager.refreshProfileStatus() }
            )
            GateStage.APP -> content()
        }
    }
}
