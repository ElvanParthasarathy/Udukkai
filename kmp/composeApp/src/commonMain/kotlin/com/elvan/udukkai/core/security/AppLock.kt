package com.elvan.udukkai.core.security

import com.elvan.udukkai.core.platform.getPreferencesHelper

/**
 * The app lock uses the phone's own screen lock (its PIN, pattern or password, and the fingerprint or face the phone
 * allows there), so there is no second PIN to remember or lose. When it is on, the app asks for it when it opens and
 * before the actions that can lose or give away data (replacing data by a restore, going back to an earlier copy,
 * deleting a business, sharing a backup, signing out). See [authenticateDeviceLock].
 */
object AppLock {
    private const val ON = "app_lock_on"

    /** True once the phone's lock was passed in this run of the app, so the opening page is asked once. */
    var unlockedThisRun = false

    fun isEnabled(): Boolean = getPreferencesHelper().getBoolean(ON, false)

    fun setEnabled(on: Boolean) {
        getPreferencesHelper().setBoolean(ON, on)
        if (on) unlockedThisRun = true
    }
}
