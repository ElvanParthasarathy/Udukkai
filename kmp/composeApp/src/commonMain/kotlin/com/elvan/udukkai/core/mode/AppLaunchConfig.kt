package com.elvan.udukkai.core.mode

/**
 * AppLaunchConfig — Cross-platform fixed-mode launcher configuration.
 *
 * On Android this is an expect/actual pair:
 *   - Each product flavor (factory / textiles / combined) generates its own BuildConfig
 *     class with APP_MODE = "kooli" | "pattu" | "".
 *   - The Android actual reads BuildConfig.APP_MODE and resolves the fixed AppMode.
 *
 * On Desktop, main_factory.kt and main_textiles.kt call setFixedMode() before App().
 *
 * APP_MODE == "" means no fixed mode → the legacy mode selector is shown (combined flavor).
 */
object AppLaunchConfig {

    /**
     * The fixed mode for this app build, or null if the mode selector should be shown
     * (combined / legacy build).
     */
    var fixedMode: AppMode? = null
        private set

    /** True when this build is locked to a single mode. */
    val isFixed: Boolean get() = fixedMode != null

    /**
     * Called once at platform entry point startup (before App() renders).
     * Desktop entry points (main_factory.kt / main_textiles.kt) call this directly.
     * Android reads the flavor-injected BuildConfig constant in AppLaunchConfigInit.kt.
     *
     * Calling this when mode == null resets the config to "show selector" behaviour.
     */
    fun setFixedMode(mode: AppMode?) {
        fixedMode = mode
        if (mode != null) {
            // Pre-set ModeManager so all repositories immediately resolve the correct DB.
            ModeManager.applyFixedMode(mode)
        }
    }
}
