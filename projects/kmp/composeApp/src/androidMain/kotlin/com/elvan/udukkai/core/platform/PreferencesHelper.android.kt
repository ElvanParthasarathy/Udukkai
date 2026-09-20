package com.elvan.udukkai.core.platform

import android.content.Context
import android.content.SharedPreferences
import java.util.Locale

class AndroidPreferencesHelper : PreferencesHelper {
    private val prefs: SharedPreferences?
        get() = if (AppContext.isInitialized) {
            AppContext.context.getSharedPreferences("udukkai_preferences", Context.MODE_PRIVATE)
        } else null

    override fun getString(key: String, defaultValue: String?): String? {
        return prefs?.getString(key, defaultValue) ?: defaultValue
    }

    override fun setString(key: String, value: String?) {
        prefs?.edit()?.apply {
            if (value == null) remove(key) else putString(key, value)
            apply()
        }
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return prefs?.getBoolean(key, defaultValue) ?: defaultValue
    }

    override fun setBoolean(key: String, value: Boolean) {
        prefs?.edit()?.putBoolean(key, value)?.apply()
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return prefs?.getInt(key, defaultValue) ?: defaultValue
    }

    override fun setInt(key: String, value: Int) {
        prefs?.edit()?.putInt(key, value)?.apply()
    }
}

private val androidPrefsInstance by lazy { AndroidPreferencesHelper() }

actual fun getPreferencesHelper(): PreferencesHelper = androidPrefsInstance

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
