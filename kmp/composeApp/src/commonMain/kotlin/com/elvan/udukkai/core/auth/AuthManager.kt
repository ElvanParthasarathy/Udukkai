package com.elvan.udukkai.core.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.mode.ModeManager
import com.elvan.udukkai.core.platform.getPreferencesHelper
import com.elvan.udukkai.data.settings.NiruvanaTharavugalRepository

/**
 * Central authentication and onboarding state manager.
 * Manages login status and profile setup completion for the app flow.
 */
object AuthManager {

    private const val PREF_KEY_LOGGED_IN = "is_logged_in"
    private const val PREF_KEY_BILLING_LANG = "elvanniril_setup_billingLang"
    private const val PREF_KEY_SKIP_RESTORE = "skip_restore"

    var isLoggedIn by mutableStateOf(false)
        private set

    var isProfilesLoading by mutableStateOf(true)
        private set

    var skipRestore by mutableStateOf(false)
        private set

    fun init() {
        try {
            val prefs = getPreferencesHelper()
            isLoggedIn = prefs.getBoolean(PREF_KEY_LOGGED_IN, false)
            skipRestore = prefs.getBoolean(PREF_KEY_SKIP_RESTORE, false)
        } catch (_: Exception) {
            isLoggedIn = false
        }
        refreshProfileStatus()
    }

    fun login(email: String, password: String): Boolean {
        // Mock authentication — any credentials work
        isLoggedIn = true
        try {
            getPreferencesHelper().setBoolean(PREF_KEY_LOGGED_IN, true)
        } catch (_: Exception) {}
        return true
    }

    fun logout() {
        isLoggedIn = false
        try {
            getPreferencesHelper().setBoolean(PREF_KEY_LOGGED_IN, false)
        } catch (_: Exception) {}
    }

    fun updateSkipRestore(skip: Boolean) {
        skipRestore = skip
        try {
            getPreferencesHelper().setBoolean(PREF_KEY_SKIP_RESTORE, skip)
        } catch (_: Exception) {}
    }

    fun saveBillingLanguage(lang: String) {
        try {
            getPreferencesHelper().setString(PREF_KEY_BILLING_LANG, lang)
        } catch (_: Exception) {}
    }

    fun getBillingLanguage(): String {
        return try {
            getPreferencesHelper().getString(PREF_KEY_BILLING_LANG, "ta") ?: "ta"
        } catch (_: Exception) { "ta" }
    }

    fun refreshProfileStatus() {
        try {
            NiruvanaTharavugalRepository.refreshFromDatabase()
            isProfilesLoading = false
        } catch (_: Exception) {
            isProfilesLoading = false
        }
    }

    /**
     * Returns true if at least one profile (either Kooli or Pattu) exists in the database.
     */
    val isSetupComplete: Boolean
        get() {
            if (com.elvan.udukkai.core.mode.AppLaunchConfig.isFixed) {
                return NiruvanaTharavugalRepository.getAllProfiles(ModeManager.currentMode).isNotEmpty()
            }
            val kooliProfiles = NiruvanaTharavugalRepository.getAllProfiles(AppMode.KOOLI)
            val pattuProfiles = NiruvanaTharavugalRepository.getAllProfiles(AppMode.PATTU)
            return kooliProfiles.isNotEmpty() || pattuProfiles.isNotEmpty()
        }

    /**
     * Returns which profiles are missing ("kooli" and/or "pattu").
     */
    val missingProfiles: List<String>
        get() {
            if (com.elvan.udukkai.core.mode.AppLaunchConfig.isFixed) {
                val profiles = NiruvanaTharavugalRepository.getAllProfiles(ModeManager.currentMode)
                return if (profiles.isEmpty()) listOf(ModeManager.currentMode.key) else emptyList()
            }
            val missing = mutableListOf<String>()
            val kooliProfiles = NiruvanaTharavugalRepository.getAllProfiles(AppMode.KOOLI)
            val pattuProfiles = NiruvanaTharavugalRepository.getAllProfiles(AppMode.PATTU)
            if (kooliProfiles.isEmpty()) missing.add("kooli")
            if (pattuProfiles.isEmpty()) missing.add("pattu")
            return missing
        }

    /**
     * Returns true if the current mode has a valid profile.
     */
    fun hasProfileForMode(mode: AppMode): Boolean {
        return NiruvanaTharavugalRepository.getAllProfiles(mode).isNotEmpty()
    }
}
