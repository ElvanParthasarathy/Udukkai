package com.elvan.udukkai.core.security

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.elvan.udukkai.core.platform.getPreferencesHelper

/**
 * System App Lock manager. Protects business records using the device's native screen lock
 * (Fingerprint, Face unlock, or Phone PIN/Pattern).
 * When enabled in Settings, the app asks for system authentication when it opens and
 * when returning after a minute away.
 */
object AppLock {
    private const val KEY_ENABLED = "app_lock_system_enabled"
    private const val AWAY_MS = 60_000L

    /** True while system authentication has passed and the app was not away for long. */
    var unlockedThisRun by mutableStateOf(false)

    private var leftAt = 0L

    private val prefs get() = getPreferencesHelper()

    /** Whether App Lock (device screen lock protection) is enabled by the user. */
    fun isSet(): Boolean = prefs.getBoolean(KEY_ENABLED, false)

    /** Enables or disables App Lock. */
    fun setEnabled(enabled: Boolean) {
        prefs.setBoolean(KEY_ENABLED, enabled)
        if (enabled) unlockedThisRun = true
    }

    /** Compatibility helper for existing callers. */
    fun fingerprintOn(): Boolean = isSet()
    fun setFingerprint(on: Boolean) = setEnabled(on)

    /** The app went out of sight. */
    fun left(now: Long = System.currentTimeMillis()) { leftAt = now }

    /** The app is back in sight: locks again if away for more than a minute. */
    fun returned(now: Long = System.currentTimeMillis()) {
        if (leftAt > 0L && now - leftAt > AWAY_MS) unlockedThisRun = false
        leftAt = 0L
    }

    internal fun clearForTests() {
        prefs.setBoolean(KEY_ENABLED, false)
        unlockedThisRun = false
        leftAt = 0L
    }
}
