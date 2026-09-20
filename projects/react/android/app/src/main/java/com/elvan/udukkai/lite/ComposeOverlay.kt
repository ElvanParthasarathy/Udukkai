package com.elvan.udukkai.lite

import android.content.Context
import androidx.compose.ui.platform.ComposeView
import com.elvan.udukkai.lite.ui.SplashScreen

object ComposeOverlay {
    fun create(context: Context, isDarkTheme: Boolean): ComposeView {
        return ComposeView(context).apply {
            setContent {
                SplashScreen(isDarkTheme = isDarkTheme)
            }
        }
    }
}
