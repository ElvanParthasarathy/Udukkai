package com.elvan.udukkai.core.security

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.elvan.udukkai.core.platform.getPreferencesHelper

/**
 * System App Lock manager. Protects business records using the device's native screen lock
 * (Fingerprint, Face unlock, or Phone PIN/Pattern).
 * Enabled by default. Asks for system authentication when opened and
 * when returning after a minute away or after the screen turned off.
 */
object AppLock {
    private const val KEY_ENABLED = "app_lock_system_enabled"
    private const val AWAY_MS = 60_000L

    /** True while system authentication has passed and the app was not away for long. */
    var unlockedThisRun by mutableStateOf(false)

    private var leftAt = 0L
    private var activityCount = 0

    private val prefs get() = getPreferencesHelper()

    /** Whether App Lock (device screen lock protection) is enabled by the user. Mandatory (true) by default. */
    fun isSet(): Boolean = prefs.getBoolean(KEY_ENABLED, true)

    /** Enables or disables App Lock. */
    fun setEnabled(enabled: Boolean) {
        prefs.setBoolean(KEY_ENABLED, enabled)
        if (enabled) unlockedThisRun = true
    }

    /** Compatibility helper for existing callers. */
    fun fingerprintOn(): Boolean = isSet()
    fun setFingerprint(on: Boolean) = setEnabled(on)

    fun onActivityStarted() {
        if (activityCount == 0) {
            returned()
        }
        activityCount++
    }

    fun onActivityStopped() {
        activityCount = (activityCount - 1).coerceAtLeast(0)
        if (activityCount == 0) {
            left()
        }
    }

    fun onScreenOff() {
        unlockedThisRun = false
        left()
    }

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
        activityCount = 0
    }
}
