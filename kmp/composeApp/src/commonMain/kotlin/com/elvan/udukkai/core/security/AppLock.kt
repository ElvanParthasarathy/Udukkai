package com.elvan.udukkai.core.security

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.elvan.udukkai.core.platform.getPreferencesHelper

/**
 * The app lock uses the phone's own screen lock (its PIN, pattern or password, and the fingerprint or face the phone
 * allows there), so there is no second PIN to remember or lose. When it is on, the app asks for it when it opens and when it comes back after a minute away and
 * before the actions that can lose or give away data (replacing data by a restore, going back to an earlier copy,
 * deleting a business, sharing a backup, signing out). See [authenticateDeviceLock].
 */
object AppLock {
    private const val ON = "app_lock_on"

    /** How long the app may be out of sight before it asks for the phone's lock again. */
    private const val AWAY_MS = 60_000L

    /** True while the phone's lock has been passed and the app was not away for long; the lock page shows when false. */
    var unlockedThisRun by mutableStateOf(false)

    private var leftAt = 0L

    /** The app went out of sight. */
    fun left(now: Long = System.currentTimeMillis()) { leftAt = now }

    /** The app is back in sight: locks again if it was away for more than a minute. */
    fun returned(now: Long = System.currentTimeMillis()) {
        if (leftAt > 0L && now - leftAt > AWAY_MS) unlockedThisRun = false
        leftAt = 0L
    }

    fun isEnabled(): Boolean = getPreferencesHelper().getBoolean(ON, false)

    fun setEnabled(on: Boolean) {
        getPreferencesHelper().setBoolean(ON, on)
        if (on) unlockedThisRun = true
    }
}
