package com.elvan.udukkai

import android.content.Context
import android.content.Intent
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.elvan.udukkai.core.platform.AppContext
import com.elvan.udukkai.core.platform.CurrentActivity
import com.elvan.udukkai.core.security.AppLock
import com.elvan.udukkai.factory.ui.screens.FactorySettingsScreen
import com.elvan.udukkai.localization.ProvideAppLanguage
import com.elvan.udukkai.textiles.ui.screens.TextilesSettingsScreen
import com.elvan.udukkai.theme.ThemeManager
import com.elvan.udukkai.theme.UdukkaiTheme
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.ElvanBottomSheetController
import com.elvan.udukkai.ui.components.shell.ElvanBottomSheetHost
import com.elvan.udukkai.ui.components.shell.ElvanSnackbarHost
import com.elvan.udukkai.ui.components.shell.LocalElvanBottomSheetController
import com.elvan.udukkai.ui.screens.settings.SettingsRoute
import com.elvan.udukkai.ui.screens.settings.name
import com.elvan.udukkai.ui.screens.settings.settingsRouteFromName

/**
 * Native Android Activity for Settings screens.
 * Uses Android OS Window Manager and native OEM transitions (e.g. Samsung One UI)
 * for fluid hardware-composited screen sliding and predictive back gestures.
 */
class SettingsActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        AppContext.context = applicationContext
        CurrentActivity.activity = this

        val isFactory = BuildConfig.FLAVOR == "factory"
        val routeName = intent.getStringExtra(EXTRA_ROUTE)
        val route = settingsRouteFromName(routeName)

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

                    CompositionLocalProvider(
                        LocalElvanBottomSheetController provides bottomSheetController
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(colors.background)
                        ) {
                            if (isFactory) {
                                FactorySettingsScreen(
                                    initialRoute = route,
                                    onNavigateOverride = { targetRoute ->
                                        start(this@SettingsActivity, targetRoute)
                                        true
                                    },
                                    onBack = { finish() }
                                )
                            } else {
                                TextilesSettingsScreen(
                                    initialRoute = route,
                                    onNavigateOverride = { targetRoute ->
                                        start(this@SettingsActivity, targetRoute)
                                        true
                                    },
                                    onBack = { finish() }
                                )
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
        AppLock.returned()
    }

    override fun onResume() {
        super.onResume()
        CurrentActivity.activity = this
    }

    override fun onStop() {
        super.onStop()
        AppLock.left()
    }

    companion object {
        const val EXTRA_ROUTE = "extra_settings_route"

        fun start(context: Context, route: SettingsRoute = SettingsRoute.Hub) {
            val intent = Intent(context, SettingsActivity::class.java).apply {
                putExtra(EXTRA_ROUTE, route.name)
            }
            context.startActivity(intent)
        }
    }
}
