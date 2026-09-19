package com.elvan.udukkai

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.core.view.WindowCompat
import com.elvan.udukkai.core.mode.ModeManager
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        com.elvan.udukkai.core.platform.AppContext.context = applicationContext
        com.elvan.udukkai.localization.LanguageManager.init()
        com.elvan.udukkai.theme.ThemeManager.init()
        com.elvan.udukkai.theme.FontManager.init()
        com.elvan.udukkai.core.auth.AuthManager.init()
        com.elvan.udukkai.data.settings.NiruvanaTharavugalRepository.refreshFromDatabase()

        if (savedInstanceState == null) {
            ModeManager.resetStartupState()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                ModeManager.resetStartupState()
                finish()
            }
        })

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
            App()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isChangingConfigurations) {
            ModeManager.resetStartupState()
        }
    }
}
