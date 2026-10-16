package com.elvan.udukkai.core.mode

import com.elvan.udukkai.BuildConfig

/**
 * Reads the flavor-injected BuildConfig.APP_MODE constant and calls
 * AppLaunchConfig.setFixedMode() before App() renders.
 *
 * Must be called once in MainActivity.onCreate() before setContent {}.
 *
 * BuildConfig.APP_MODE values (set in composeApp/build.gradle.kts productFlavors):
 *   "kooli"  → Udukkai Factory  (fixed, no mode selector)
 *   "pattu"  → Udukkai Textiles (fixed, no mode selector)
 *   ""       → Udukkai Combined (legacy — mode selector shown at startup)
 */
fun initAndroidLaunchMode() {
    val modeKey = BuildConfig.APP_MODE
    val mode = when {
        modeKey.equals("kooli", ignoreCase = true) -> AppMode.KOOLI
        modeKey.equals("pattu", ignoreCase = true) -> AppMode.PATTU
        else -> null  // combined / legacy build — no fixed mode
    }
    AppLaunchConfig.setFixedMode(mode)
}
