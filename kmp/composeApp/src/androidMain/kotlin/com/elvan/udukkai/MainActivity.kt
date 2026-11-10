package com.elvan.udukkai

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import androidx.fragment.app.FragmentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.elvan.udukkai.ui.components.shell.GlobalHeaderState
import com.elvan.udukkai.ui.components.shell.LocalGlobalHeaderExpanded
import androidx.core.view.WindowCompat
import com.elvan.udukkai.factory.FactoryApp
import com.elvan.udukkai.textiles.TextilesApp
import com.elvan.udukkai.theme.ThemeManager

fun updateSystemBarsAppearance(window: Window, isDark: Boolean) {
    val decorView = window.decorView
    val insetsController = WindowCompat.getInsetsController(window, decorView)
    insetsController.isAppearanceLightStatusBars = !isDark
    insetsController.isAppearanceLightNavigationBars = !isDark

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window.insetsController?.let { controller ->
            val appearance = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                             WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            if (!isDark) {
                controller.setSystemBarsAppearance(appearance, appearance)
            } else {
                controller.setSystemBarsAppearance(0, appearance)
            }
        }
    }

    @Suppress("DEPRECATION")
    var flags = decorView.systemUiVisibility
    flags = if (!isDark) {
        flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
    } else {
        flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv() and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
    }
    @Suppress("DEPRECATION")
    decorView.systemUiVisibility = flags
}

class MainActivity : FragmentActivity() {
    private var currentHeaderExpanded: MutableState<Boolean>? = null

    /**
     * Developer switch for debug builds only (ignored in release): the lock screen stays, but
     * `adb shell am start -n <pkg>/com.elvan.udukkai.MainActivity --ez dev_skip_lock true` unlocks it.
     */
    private fun devSkipLock(intent: android.content.Intent?) {
        if (BuildConfig.DEBUG && intent?.getBooleanExtra("dev_skip_lock", false) == true) {
            com.elvan.udukkai.core.security.AppLock.unlockedThisRun = true
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        devSkipLock(intent)
    }

    @OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
    private fun configureComposeRendering() {
        // Samsung Android 16 captures a call stack for Compose 1.9's per-frame refresh-rate hints.
        // Keep normal system refresh-rate scheduling; avoid that measured UI-thread cost.
        if (Build.VERSION.SDK_INT == 36 && Build.MANUFACTURER.equals("samsung", ignoreCase = true)) {
            androidx.compose.ui.ComposeUiFlags.isAdaptiveRefreshRateEnabled = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        configureComposeRendering()
        super.onCreate(savedInstanceState)
        devSkipLock(intent)
        com.elvan.udukkai.core.platform.AppContext.context = applicationContext
        com.elvan.udukkai.core.security.ScreenLockReceiver.register(applicationContext)
        com.elvan.udukkai.core.platform.CurrentActivity.activity = this
        com.elvan.udukkai.localization.LanguageManager.init()
        com.elvan.udukkai.theme.ThemeManager.init()
        com.elvan.udukkai.theme.FontManager.init()
        com.elvan.udukkai.core.update.AppUpdate.init(this)
        com.elvan.udukkai.core.update.PlayStoreUpdateHelper.init(this)

        val isFactory = BuildConfig.FLAVOR == "factory"
        if (isFactory) {
            com.elvan.udukkai.factory.data.FactoryProfileRepository.refreshFromDatabase()
        } else {
            com.elvan.udukkai.textiles.data.TextilesProfileRepository.refreshFromDatabase()
        }

        try {
            com.elvan.udukkai.core.sync.getFirebaseSyncManager().initialize()
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Failed to start sync: ${e.message}")
        }

        // No catch-all back callback here: with one always on, the system treats every back as handled by the app and
        // never shows its back-to-home preview. With none, back at the first screen leaves the app as the system does.

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
            val headerExpandedState = remember { mutableStateOf(GlobalHeaderState.isExpanded) }
            DisposableEffect(headerExpandedState) {
                currentHeaderExpanded = headerExpandedState
                onDispose { currentHeaderExpanded = null }
            }

            CompositionLocalProvider(
                LocalGlobalHeaderExpanded provides headerExpandedState
            ) {
                if (isFactory) {
                    FactoryApp()
                } else {
                    TextilesApp()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        com.elvan.udukkai.core.security.AppLock.onActivityStopped()
        com.elvan.udukkai.core.platform.updatePlatformLauncher(com.elvan.udukkai.localization.LanguageManager.activeLanguageCode)
    }

    override fun onStart() {
        super.onStart()
        com.elvan.udukkai.core.platform.CurrentActivity.activity = this
        com.elvan.udukkai.core.security.AppLock.onActivityStarted()
    }

    override fun onResume() {
        super.onResume()
        com.elvan.udukkai.core.platform.CurrentActivity.activity = this
        com.elvan.udukkai.core.update.AppUpdate.checkIfDue()
        com.elvan.udukkai.core.update.AppUpdate.onWindowResumed(this)
        com.elvan.udukkai.core.update.PlayStoreUpdateHelper.onResume(this)
        currentHeaderExpanded?.let { state ->
            if (state.value != GlobalHeaderState.isExpanded) {
                state.value = GlobalHeaderState.isExpanded
            }
        }
        com.elvan.udukkai.core.platform.updatePlatformLauncher(com.elvan.udukkai.localization.LanguageManager.activeLanguageCode)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        com.elvan.udukkai.core.security.DeviceAuthHelper.onActivityResult(requestCode, resultCode)
        com.elvan.udukkai.core.update.PlayStoreUpdateHelper.onActivityResult(this, requestCode, resultCode)
    }
}
