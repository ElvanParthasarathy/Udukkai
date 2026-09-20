package com.elvan.udukkai

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import com.elvan.udukkai.core.auth.AuthManager
import com.elvan.udukkai.core.mode.ModeManager
import com.elvan.udukkai.core.mode.ProvideModeContext
import com.elvan.udukkai.core.platform.PlatformType
import com.elvan.udukkai.core.platform.currentPlatform
import com.elvan.udukkai.core.platform.isStoragePermissionGranted
import com.elvan.udukkai.localization.ProvideAppLanguage
import com.elvan.udukkai.theme.UdukkaiTheme
import com.elvan.udukkai.theme.ThemeManager
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.dev.ElvanUruvakkunarMenu
import com.elvan.udukkai.ui.components.shell.ElvanBottomSheetController
import com.elvan.udukkai.ui.components.shell.ElvanBottomSheetHost
import com.elvan.udukkai.ui.components.shell.ElvanSnackbarHost
import com.elvan.udukkai.ui.components.shell.LocalElvanBottomSheetController
import com.elvan.udukkai.ui.screens.home.HomeScreen
import com.elvan.udukkai.ui.screens.mode.ModeSelectorScreen
import com.elvan.udukkai.ui.screens.onboarding.screens.AnumadhiKaavalarThirai
import com.elvan.udukkai.ui.screens.onboarding.screens.NalvaravuThirai
import com.elvan.udukkai.ui.screens.onboarding.screens.NalvaravuWelcomeScreen
import com.elvan.udukkai.ui.screens.onboarding.screens.UllnuzhaivuThirai

private enum class AppFlowState {
    PERMISSION_GUARD,
    WELCOME_LANDING,
    LOGIN,
    ONBOARDING,
    MAIN_APP
}

@Composable
fun App() {
    var hasPermission by remember { 
        mutableStateOf(currentPlatform == PlatformType.DESKTOP || isStoragePermissionGranted()) 
    }
    var showLoginPage by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        AuthManager.init()
    }

    ProvideAppLanguage {
        ProvideModeContext {
            UdukkaiTheme {
                val bottomSheetController = remember { ElvanBottomSheetController() }
                val colors = rememberShellColors()

                val currentFlowState = when {
                    !hasPermission -> AppFlowState.PERMISSION_GUARD
                    !AuthManager.isLoggedIn && !showLoginPage -> AppFlowState.WELCOME_LANDING
                    !AuthManager.isLoggedIn && showLoginPage -> AppFlowState.LOGIN
                    !AuthManager.isSetupComplete -> AppFlowState.ONBOARDING
                    else -> AppFlowState.MAIN_APP
                }

                CompositionLocalProvider(
                    LocalElvanBottomSheetController provides bottomSheetController
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colors.background)
                    ) {
                        AnimatedContent(
                            targetState = currentFlowState,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(280)) togetherWith
                                    fadeOut(animationSpec = tween(200))
                            },
                            label = "app_navigation_flow"
                        ) { flowState ->
                            when (flowState) {
                                AppFlowState.PERMISSION_GUARD -> {
                                    AnumadhiKaavalarThirai(
                                        onPermissionGranted = {
                                            hasPermission = true
                                            AuthManager.refreshProfileStatus()
                                        }
                                    )
                                }
                                AppFlowState.WELCOME_LANDING -> {
                                    NalvaravuThirai(
                                        onNavigateToLogin = { showLoginPage = true }
                                    )
                                }
                                AppFlowState.LOGIN -> {
                                    UllnuzhaivuThirai(
                                        onBack = { showLoginPage = false },
                                        onLoginSuccess = {
                                            showLoginPage = false
                                            AuthManager.refreshProfileStatus()
                                        }
                                    )
                                }
                                AppFlowState.ONBOARDING -> {
                                    NalvaravuWelcomeScreen(
                                        onSetupComplete = {
                                            AuthManager.refreshProfileStatus()
                                        }
                                    )
                                }
                                AppFlowState.MAIN_APP -> {
                                    AnimatedContent(
                                        targetState = ModeManager.hasSelectedModeAtStartup,
                                        transitionSpec = {
                                            fadeIn(animationSpec = tween(280)) togetherWith
                                                fadeOut(animationSpec = tween(200))
                                        },
                                        label = "startup_mode_flow"
                                    ) { hasSelectedMode ->
                                        if (!hasSelectedMode) {
                                            ModeSelectorScreen(
                                                onModeSelected = { selectedMode ->
                                                    ModeManager.setMode(selectedMode)
                                                },
                                                canDismiss = false
                                            )
                                        } else {
                                            Box(modifier = Modifier.fillMaxSize()) {
                                                HomeScreen()

                                                AnimatedVisibility(
                                                    visible = ModeManager.isModeSelectorOpen,
                                                    modifier = Modifier.zIndex(100f),
                                                    enter = fadeIn(animationSpec = tween(300)),
                                                    exit = fadeOut(animationSpec = tween(250))
                                                ) {
                                                    ModeSelectorScreen(
                                                        onModeSelected = { selectedMode ->
                                                            ModeManager.setMode(selectedMode)
                                                        },
                                                        onDismiss = {
                                                            ModeManager.closeModeSelector()
                                                        },
                                                        canDismiss = true
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        ElvanBottomSheetHost(bottomSheetController)

                        ElvanSnackbarHost(colors = colors)

                        Box(modifier = Modifier.fillMaxSize().zIndex(999f)) {
                            ElvanUruvakkunarMenu()
                        }
                    }
                }
            }
        }
    }
}

