package com.elvan.udukkai.core.mode

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Manages the current operating mode (Kooli vs Pattu).
 *
 * THE WALL:
 * - Each mode gets its own database file (udukkai_kooli.db / udukkai_pattu.db)
 * - Each mode gets its own preferences namespace
 * - Each mode gets its own file storage directory
 * - Switching modes = switching to a completely different data universe
 *
 * Usage:
 *   ModeManager.setMode(AppMode.KOOLI)
 *   val db = ModeManager.currentDatabaseName  // "udukkai_kooli.db"
 *   val prefsKey = ModeManager.prefsNamespace  // "udukkai_kooli_"
 */
object ModeManager {

    var currentMode by mutableStateOf(AppMode.KOOLI)
        private set

    /** Tracks whether the user has chosen their workspace mode during startup */
    var hasSelectedModeAtStartup by mutableStateOf(false)

    /** Tracks whether the full-screen mode switcher overlay is open */
    var isModeSelectorOpen by mutableStateOf(false)

    /** Database file name for the current mode */
    val currentDatabaseName: String
        get() = currentMode.databaseName

    /** Preferences namespace prefix for the current mode */
    val prefsNamespace: String
        get() = "udukkai_${currentMode.key}_"

    /** Storage directory name for the current mode */
    val storageDir: String
        get() = "udukkai_${currentMode.key}"

    fun openModeSelector() {
        isModeSelectorOpen = true
    }

    fun closeModeSelector() {
        isModeSelectorOpen = false
    }

    /**
     * Switch the app to a different mode.
     * This will cause the entire UI to re-compose with the new mode's data.
     */
    fun setMode(mode: AppMode) {
        currentMode = mode
        hasSelectedModeAtStartup = true
        isModeSelectorOpen = false
    }

    fun toggleMode() {
        openModeSelector()
    }

    /**
     * Resets the startup mode selection state so the next launch requires
     * choosing mode at startup.
     */
    fun resetStartupState() {
        hasSelectedModeAtStartup = false
        isModeSelectorOpen = false
    }

    /** Check if we are in Kooli mode */
    val isKooli: Boolean get() = currentMode == AppMode.KOOLI

    /** Check if we are in Pattu mode */
    val isPattu: Boolean get() = currentMode == AppMode.PATTU
}
