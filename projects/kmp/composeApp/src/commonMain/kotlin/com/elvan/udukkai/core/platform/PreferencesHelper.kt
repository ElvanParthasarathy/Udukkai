package com.elvan.udukkai.core.platform

/**
 * Cross-platform persistent key-value preferences store.
 * - Android: backed by SharedPreferences ("udukkai_preferences")
 * - Desktop: backed by java.util.prefs.Preferences
 */
interface PreferencesHelper {
    fun getString(key: String, defaultValue: String? = null): String?
    fun setString(key: String, value: String?)
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean
    fun setBoolean(key: String, value: Boolean)
    fun getInt(key: String, defaultValue: Int = 0): Int
    fun setInt(key: String, value: Int)
}

expect fun getPreferencesHelper(): PreferencesHelper

/**
 * Returns the device's system language code ("ta", "en", or "ta-Latn").
 */
expect fun getSystemLanguageCode(): String
