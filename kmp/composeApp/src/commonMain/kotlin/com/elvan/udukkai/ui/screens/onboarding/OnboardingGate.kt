package com.elvan.udukkai.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.HomeStatsSkeleton
import com.elvan.udukkai.ui.components.shell.LoginShimmer
import com.elvan.udukkai.ui.components.shell.UruvakkuCardSkeleton
import com.elvan.udukkai.ui.components.shell.rememberShimmerBrush
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.*
import androidx.compose.ui.text.TextStyle
import com.elvan.udukkai.theme.ElvanSansFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.elvan.udukkai.core.auth.AppScope
import com.elvan.udukkai.core.auth.AuthManager
import com.elvan.udukkai.core.platform.AppBackHandler
import com.elvan.udukkai.core.sync.getFirebaseSyncManager
import androidx.compose.ui.Alignment
import com.elvan.udukkai.ui.components.ExpressiveLoadingIndicator
import com.elvan.udukkai.ui.screens.onboarding.screens.NeramColors
import com.elvan.udukkai.ui.screens.onboarding.screens.LanguageSelectScreen
import com.elvan.udukkai.ui.screens.onboarding.components.AuthBackdrop
import com.elvan.udukkai.ui.screens.onboarding.components.LocalHasAuthBackdrop
import com.elvan.udukkai.ui.screens.onboarding.screens.UllnuzhaivuThirai
import com.elvan.udukkai.ui.screens.onboarding.screens.WelcomeScreen
import com.elvan.udukkai.ui.screens.onboarding.screens.WelcomeSlideScreen

private enum class GateStage { AUTH, LOADING, SETUP, APP }
private enum class AuthPage { LANGUAGE, WELCOME, LOGIN }

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

    // Every logged-out visit starts at the language page, as in Neram; signing out brings it back
    var page by remember(AuthManager.isLoggedIn) { mutableStateOf(AuthPage.LANGUAGE) }

    // Right after signing in the real home page opens with placeholder blocks, which fill in as the data arrives
    var freshLogin by remember { mutableStateOf(false) }
    LaunchedEffect(AuthManager.isLoggedIn) {
        if (!AuthManager.isLoggedIn) { freshLogin = false }
    }
    LaunchedEffect(freshLogin) {
        if (freshLogin) {
            delay(500)
            // Never hold the page for more than 20 seconds, even if the connection stalls halfway
            kotlinx.coroutines.withTimeoutOrNull(20_000) {
                snapshotFlow { getFirebaseSyncManager().restoreProgress.value }.first { it == null }
            }
        }
    }

    val stage = when {
        !AuthManager.isLoggedIn -> GateStage.AUTH
        AuthManager.isProfilesLoading -> GateStage.LOADING
        // A new phone: wait for the business profiles that are being restored instead of asking to create one
        !AuthManager.isSetupComplete(scope) && !getFirebaseSyncManager().profilesRestored.value -> GateStage.LOADING
        !AuthManager.isSetupComplete(scope) -> GateStage.SETUP
        else -> GateStage.APP
    }

    AppBackHandler(enabled = stage == GateStage.AUTH && page != AuthPage.LANGUAGE) {
        page = if (page == AuthPage.LOGIN) AuthPage.WELCOME else AuthPage.LANGUAGE
    }

    // Every page of the sign-in and setup flow is set in Elvan Sans
    CompositionLocalProvider(
        LocalTextStyle provides LocalTextStyle.current.merge(TextStyle(fontFamily = ElvanSansFontFamily))
    ) {
    AnimatedContent(
        targetState = stage,
        transitionSpec = {
            fadeIn(tween(600, easing = EaseOutCubic)) togetherWith fadeOut(tween(600, easing = EaseInCubic))
        },
        modifier = Modifier.fillMaxSize(),
        label = "onboarding_gate"
    ) { s ->
        when (s) {
            // One background for the three pages, so the shapes keep floating while the pages fade over each other
            GateStage.AUTH -> Box(Modifier.fillMaxSize()) {
                AuthBackdrop()
                CompositionLocalProvider(LocalHasAuthBackdrop provides true) {
                    AnimatedContent(
                        targetState = page,
                        transitionSpec = {
                            fadeIn(tween(300, easing = EaseOutCubic)) togetherWith fadeOut(tween(200, easing = EaseInCubic))
                        },
                        modifier = Modifier.fillMaxSize(),
                        label = "auth_pages"
                    ) { p ->
                        when (p) {
                            AuthPage.LANGUAGE -> LanguageSelectScreen(onContinue = { page = AuthPage.WELCOME })
                            AuthPage.WELCOME -> WelcomeScreen(onNavigateToLogin = { page = AuthPage.LOGIN })
                            AuthPage.LOGIN -> UllnuzhaivuThirai(
                                onBack = { page = AuthPage.WELCOME },
                                onLoginSuccess = {
                                    freshLogin = true
                                    AuthManager.refreshProfileStatus()
                                }
                            )
                        }
                    }
                }
            }
            // The home page while the business data is still arriving: grey blocks that shimmer
            GateStage.LOADING -> Box(Modifier.fillMaxSize()) {
                AuthBackdrop()
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ExpressiveLoadingIndicator(
                        modifier = Modifier.size(36.dp),
                        color = NeramColors.textPrimary()
                    )
                }
            }
            GateStage.SETUP -> WelcomeSlideScreen(
                scope = scope,
                onSetupComplete = { AuthManager.refreshProfileStatus() }
            )
            GateStage.APP -> content()
        }
    }
    }
}

/** The home page drawn as shimmering grey blocks, shown while the business data is being fetched after sign-in. */
@Composable
private fun HomeLoadingSkeleton() {
    val colors = rememberShellColors()
    val brush = rememberShimmerBrush()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .padding(top = 24.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(Dimens.ItemSpacing)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dimens.ContentPadding),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(brush))
            Spacer(Modifier.width(14.dp))
            Column {
                Box(Modifier.width(120.dp).height(18.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(6.dp)).background(brush))
                Spacer(Modifier.height(8.dp))
                Box(Modifier.width(170.dp).height(13.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(5.dp)).background(brush))
            }
        }
        HomeStatsSkeleton()
        repeat(5) {
            Box(Modifier.padding(horizontal = Dimens.ContentPadding)) { UruvakkuCardSkeleton() }
        }
    }
}
