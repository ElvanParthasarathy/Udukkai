package com.elvan.udukkai.core.platform

import android.app.Activity
import android.app.Dialog
import android.content.ContextWrapper
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.View
import android.view.ViewParent
import android.view.Window
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat

actual val currentPlatform: PlatformType = PlatformType.ANDROID

@Composable
actual fun ConfigureDialogWindow(
    isDark: Boolean,
    clearDim: Boolean,
    navBarColor: androidx.compose.ui.graphics.Color?
) {
    val view = LocalView.current

    fun findDialogWindow(): Window? {
        var current: ViewParent? = view.parent
        while (current != null) {
            if (current is DialogWindowProvider) {
                return current.window
            }
            current = current.parent
        }
        var ctx = view.context
        while (ctx is ContextWrapper) {
            if (ctx is Dialog) {
                return ctx.window
            }
            ctx = ctx.baseContext
        }
        return null
    }

    fun findActivity(): ComponentActivity? {
        var ctx = view.context
        while (ctx is ContextWrapper) {
            if (ctx is ComponentActivity) {
                return ctx
            }
            ctx = ctx.baseContext
        }
        return null
    }

    fun applyActivityNavBar(activity: ComponentActivity, color: Int?) {
        val window = activity.window
        if (color != null) {
            activity.enableEdgeToEdge(
                statusBarStyle = if (isDark) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                },
                navigationBarStyle = if (isDark) {
                    SystemBarStyle.dark(color)
                } else {
                    SystemBarStyle.light(color, color)
                }
            )
            window.navigationBarColor = color
        } else {
            activity.enableEdgeToEdge(
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
            window.navigationBarColor = Color.TRANSPARENT
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        val actDecor = window.decorView
        val actInsetsController = WindowCompat.getInsetsController(window, actDecor)
        actInsetsController.isAppearanceLightNavigationBars = !isDark
    }

    fun applySystemBars(window: Window) {
        val decorView = window.decorView
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        if (clearDim) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window.setDimAmount(0f)
            window.setWindowAnimations(0)
        }
        val targetNavBarColor = if (navBarColor != null) {
            navBarColor.toArgb()
        } else {
            Color.TRANSPARENT
        }
        window.navigationBarColor = targetNavBarColor
        window.statusBarColor = Color.TRANSPARENT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }

        val insetsController = WindowCompat.getInsetsController(window, decorView)
        insetsController.isAppearanceLightNavigationBars = !isDark
        insetsController.isAppearanceLightStatusBars = !isDark

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

    SideEffect {
        findDialogWindow()?.let { applySystemBars(it) }
    }

    DisposableEffect(isDark, navBarColor) {
        findDialogWindow()?.let { applySystemBars(it) }
        view.post {
            findDialogWindow()?.let { applySystemBars(it) }
        }
        val activity = findActivity()
        if (navBarColor != null && activity != null) {
            val argb = navBarColor.toArgb()
            applyActivityNavBar(activity, argb)
            view.post { applyActivityNavBar(activity, argb) }
            view.postDelayed({ applyActivityNavBar(activity, argb) }, 100)
        }
        onDispose {
            // Restore activity shell nav bar to transparent when dialog closes
            activity?.let { applyActivityNavBar(it, null) }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
actual fun DisableOverscroll(content: @Composable () -> Unit) {
    androidx.compose.runtime.CompositionLocalProvider(
        androidx.compose.foundation.LocalOverscrollConfiguration provides null
    ) {
        content()
    }
}

