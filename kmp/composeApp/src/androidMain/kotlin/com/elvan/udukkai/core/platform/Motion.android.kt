package com.elvan.udukkai.core.platform

import android.provider.Settings

actual fun systemAnimationsEnabled(): Boolean {
    if (!AppContext.isInitialized) return true
    return Settings.Global.getFloat(AppContext.context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
}
