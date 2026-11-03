package com.elvan.udukkai.core.platform

import android.content.Context
import android.content.SharedPreferences
import java.util.Locale

class AndroidPreferencesHelper : PreferencesHelper {
    private val memoryFallback = mutableMapOf<String, Any?>()

    private val prefs: SharedPreferences?
        get() = if (AppContext.isInitialized) {
            AppContext.context.getSharedPreferences("udukkai_preferences", Context.MODE_PRIVATE)
        } else null

    override fun getString(key: String, defaultValue: String?): String? {
        val p = prefs
        return if (p != null) {
            p.getString(key, defaultValue) ?: defaultValue
        } else {
            memoryFallback[key] as? String ?: defaultValue
        }
    }

    override fun setString(key: String, value: String?) {
        val p = prefs
        if (p != null) {
            p.edit()?.apply {
                if (value == null) remove(key) else putString(key, value)
                apply()
            }
        } else {
            if (value == null) memoryFallback.remove(key) else memoryFallback[key] = value
        }
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val p = prefs
        return if (p != null) {
            p.getBoolean(key, defaultValue)
        } else {
            memoryFallback[key] as? Boolean ?: defaultValue
        }
    }

    override fun setBoolean(key: String, value: Boolean) {
        val p = prefs
        if (p != null) {
            p.edit()?.putBoolean(key, value)?.apply()
        } else {
            memoryFallback[key] = value
        }
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        val p = prefs
        return if (p != null) {
            p.getInt(key, defaultValue)
        } else {
            memoryFallback[key] as? Int ?: defaultValue
        }
    }

    override fun setInt(key: String, value: Int) {
        val p = prefs
        if (p != null) {
            p.edit()?.putInt(key, value)?.apply()
        } else {
            memoryFallback[key] = value
        }
    }

    override fun clear() {
        prefs?.edit()?.clear()?.apply()
        memoryFallback.clear()
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
