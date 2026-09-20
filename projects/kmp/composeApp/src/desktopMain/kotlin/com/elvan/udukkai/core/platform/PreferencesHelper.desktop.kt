package com.elvan.udukkai.core.platform

import java.util.Locale
import java.util.prefs.Preferences

class DesktopPreferencesHelper : PreferencesHelper {
    private val prefs: Preferences by lazy {
        Preferences.userNodeForPackage(DesktopPreferencesHelper::class.java)
    }

    override fun getString(key: String, defaultValue: String?): String? {
        return prefs.get(key, defaultValue)
    }

    override fun setString(key: String, value: String?) {
        if (value == null) {
            prefs.remove(key)
        } else {
            prefs.put(key, value)
        }
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }

    override fun setBoolean(key: String, value: Boolean) {
        prefs.putBoolean(key, value)
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return prefs.getInt(key, defaultValue)
    }

    override fun setInt(key: String, value: Int) {
        prefs.putInt(key, value)
    }
}

private val desktopPrefsInstance by lazy { DesktopPreferencesHelper() }

actual fun getPreferencesHelper(): PreferencesHelper = desktopPrefsInstance

actual fun getSystemLanguageCode(): String {
    return try {
        val locale = Locale.getDefault()
        val lang = locale.language.lowercase()
        val script = locale.script.lowercase()
        if (lang == "ta" && script.contains("latn")) {
            "ta-Latn"
        } else if (lang == "ta") {
            "ta"
        } else {
            "en"
        }
    } catch (_: Exception) {
        "en"
    }
}
