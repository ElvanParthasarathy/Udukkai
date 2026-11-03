package com.elvan.udukkai.ui.screens.settings

import com.elvan.udukkai.SettingsActivity
import com.elvan.udukkai.core.platform.CurrentActivity

actual fun openPlatformSettings(route: SettingsRoute): Boolean {
    val activity = CurrentActivity.activity ?: return false
    SettingsActivity.start(activity, route)
    return true
}
