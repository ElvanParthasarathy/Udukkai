package com.elvan.udukkai.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.elvan.udukkai.core.platform.getPreferencesHelper

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

object ThemeManager {
    private const val PREF_KEY_THEME = "app_theme_mode"

    var currentThemeMode by mutableStateOf(ThemeMode.SYSTEM)
        private set

    fun init() {
        try {
            val saved = getPreferencesHelper().getString(PREF_KEY_THEME, null)
            currentThemeMode = when (saved?.lowercase()) {
                "light" -> ThemeMode.LIGHT
                "dark" -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }
        } catch (_: Exception) {
            currentThemeMode = ThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        currentThemeMode = mode
        try {
            val value = when (mode) {
                ThemeMode.LIGHT -> "light"
                ThemeMode.DARK -> "dark"
                ThemeMode.SYSTEM -> "system"
            }
            getPreferencesHelper().setString(PREF_KEY_THEME, value)
        } catch (_: Exception) {}
    }

    fun toggleTheme(isSystemDark: Boolean = false) {
        val next = when (currentThemeMode) {
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.SYSTEM -> if (isSystemDark) ThemeMode.LIGHT else ThemeMode.DARK
        }
        setThemeMode(next)
    }

    @Composable
    fun isDark(): Boolean = when (currentThemeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
}
