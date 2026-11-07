package com.elvan.udukkai.core.security

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.elvan.udukkai.core.platform.getPreferencesHelper

/**
 * System App Lock manager. Protects business records using the device's native screen lock
 * (Fingerprint, Face unlock, or Phone PIN/Pattern).
 * Enabled by default. Asks for system authentication when the app is opened fresh and
 * when the screen has turned off since; switching to another app and back does not lock it.
 */
object AppLock {
    private const val KEY_ENABLED = "app_lock_system_enabled"

    /** True while system authentication has passed and the app was not away for long. */
    var unlockedThisRun by mutableStateOf(false)

    private var leftAt = 0L
    private var activityCount = 0

    private val prefs get() = getPreferencesHelper()

    /** Whether App Lock (device screen lock protection) is enabled. Permanent (true) by design (banking standard). */
    fun isSet(): Boolean = true

    /** Compatibility helper: App Lock is permanent and cannot be disabled. */
    fun setEnabled(enabled: Boolean) {
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

    /** The app is back in sight. Leaving the app alone never locks it: only the screen turning off does (see [onScreenOff]). */
    fun returned(now: Long = System.currentTimeMillis()) {
        leftAt = 0L
    }

    internal fun clearForTests() {
        unlockedThisRun = false
        leftAt = 0L
        activityCount = 0
    }
}
