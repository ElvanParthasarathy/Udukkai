package com.elvan.udukkai.activities

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.elvan.udukkai.BuildConfig
import com.elvan.udukkai.core.platform.AppContext
import com.elvan.udukkai.core.platform.CurrentActivity
import com.elvan.udukkai.core.security.AppLock
import com.elvan.udukkai.localization.ProvideAppLanguage
import com.elvan.udukkai.theme.ThemeManager
import com.elvan.udukkai.theme.UdukkaiTheme
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.ElvanBottomSheetController
import com.elvan.udukkai.ui.components.shell.ElvanBottomSheetHost
import com.elvan.udukkai.ui.components.shell.ElvanSnackbarHost
import androidx.compose.runtime.mutableStateOf
import com.elvan.udukkai.ui.components.shell.LocalElvanBottomSheetController
import androidx.compose.runtime.MutableState
import com.elvan.udukkai.ui.components.shell.GlobalHeaderState
import com.elvan.udukkai.ui.components.shell.AppLockGate
import com.elvan.udukkai.core.security.ScreenLockReceiver
import com.elvan.udukkai.ui.components.shell.LocalGlobalHeaderExpanded
import com.elvan.udukkai.updateSystemBarsAppearance

/**
 * Common base Activity for all subpage activities in the multi-window architecture.
 * Manages window lifecycle, system bar insets, edge-to-edge drawing, and app lock.
 */

abstract class BaseSubpageActivity : FragmentActivity() {
    private var currentHeaderExpanded: MutableState<Boolean>? = null

    val isFactory: Boolean get() = BuildConfig.FLAVOR == "factory"

    @Composable
    abstract fun ScreenContent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        AppContext.context = applicationContext
        ScreenLockReceiver.register(applicationContext)
        CurrentActivity.activity = this

        val isSystemDark = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        val isInitialDark = when (ThemeManager.currentThemeMode) {
            com.elvan.udukkai.theme.ThemeMode.LIGHT -> false
            com.elvan.udukkai.theme.ThemeMode.DARK -> true
            com.elvan.udukkai.theme.ThemeMode.SYSTEM -> isSystemDark
        }
        val initialBg = if (isInitialDark) Color.BLACK else Color.parseColor("#F5F5F7")
        window.setBackgroundDrawable(ColorDrawable(initialBg))
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        window.decorView.setBackgroundColor(initialBg)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )

        setContent {
            val isDark = ThemeManager.isDark()
            DisposableEffect(isDark) {
                enableEdgeToEdge(
                    statusBarStyle = if (isDark) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    },
                    navigationBarStyle = if (isDark) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    }
                )

                val currentBg = if (isDark) Color.BLACK else Color.parseColor("#F5F5F7")
                window.setBackgroundDrawable(ColorDrawable(currentBg))
                window.statusBarColor = Color.TRANSPARENT
                window.navigationBarColor = Color.TRANSPARENT
                window.decorView.setBackgroundColor(currentBg)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    window.isNavigationBarContrastEnforced = false
                    window.isStatusBarContrastEnforced = false
                }
                updateSystemBarsAppearance(window, isDark)

                onDispose {}
            }

            ProvideAppLanguage {
                UdukkaiTheme {
                    val colors = rememberShellColors()
                    val bottomSheetController = remember { ElvanBottomSheetController() }

                    val headerExpandedState = remember { mutableStateOf(GlobalHeaderState.isExpanded) }
                    DisposableEffect(headerExpandedState) {
                        currentHeaderExpanded = headerExpandedState
                        onDispose { currentHeaderExpanded = null }
                    }

                    CompositionLocalProvider(
                        LocalGlobalHeaderExpanded provides headerExpandedState,
                        LocalElvanBottomSheetController provides bottomSheetController
                    ) {
                        androidx.compose.runtime.LaunchedEffect(com.elvan.udukkai.core.auth.AuthManager.isLoggedIn) {
                            if (!com.elvan.udukkai.core.auth.AuthManager.isLoggedIn) {
                                finish()
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(colors.background)
                        ) {
                            AppLockGate(colors, appNameKey = if (isFactory) com.elvan.udukkai.localization.K.udukkaiFactory else com.elvan.udukkai.localization.K.udukkaiTextiles) {
                                ScreenContent()
                            }
                            ElvanBottomSheetHost(bottomSheetController)
                            ElvanSnackbarHost(colors = colors)
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        CurrentActivity.activity = this
        AppLock.onActivityStarted()
    }

    override fun onResume() {
        super.onResume()
        if (!com.elvan.udukkai.core.auth.AuthManager.isLoggedIn) {
            finish()
            return
        }
        CurrentActivity.activity = this
        // Sync expand/collapse visual state: global → local.
        // Safe now because ElvanShell.kt no longer calls scrollToItem(0,0) on this change —
        // it only adjusts headerCollapsePx (the visual header height). Scroll is untouched.
        // This keeps expand/collapse state connected across all pages.
        currentHeaderExpanded?.let { state ->
            if (state.value != GlobalHeaderState.isExpanded) {
                state.value = GlobalHeaderState.isExpanded
            }
        }
    }

    override fun onStop() {
        super.onStop()
        AppLock.onActivityStopped()
    }

    override fun finish() {
        com.elvan.udukkai.ui.components.shell.ElvanSnackbar.dismiss()
        super.finish()
    }

    override fun onDestroy() {
        com.elvan.udukkai.ui.components.shell.ElvanSnackbar.dismiss()
        super.onDestroy()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        com.elvan.udukkai.core.security.DeviceAuthHelper.onActivityResult(requestCode, resultCode)
    }
}
