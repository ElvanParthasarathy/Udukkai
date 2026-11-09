package com.elvan.udukkai.core.platform

import com.elvan.udukkai.BuildConfig

actual fun appVersionName(): String = BuildConfig.VERSION_NAME

actual fun hasInAppUpdater(): Boolean = !BuildConfig.IS_PLAY_STORE_BUILD

actual fun appBuildNumber(): Int = BuildConfig.VERSION_CODE

actual fun appPackageName(): String = BuildConfig.APPLICATION_ID

actual fun appSourceName(): String = if (BuildConfig.IS_PLAY_STORE_BUILD) "Google Play" else "GitHub"
