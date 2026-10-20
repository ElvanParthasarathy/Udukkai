package com.elvan.udukkai.core.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.elvan.udukkai.core.platform.getPreferencesHelper
import com.elvan.udukkai.factory.data.FactoryProfileRepository
import com.elvan.udukkai.textiles.data.TextilesProfileRepository

/**
 * Central authentication and onboarding state manager.
 * Manages login status and profile setup completion for the app flow.
 */
object AuthManager {

    private const val PREF_KEY_LOGGED_IN = "is_logged_in"
    private const val PREF_KEY_BILLING_LANG = "udukkai_setup_billingLang"
    private const val LEGACY_PREF_KEY_BILLING_LANG = "elvanniril_setup_billingLang"
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
            val prefs = getPreferencesHelper()
            prefs.getString(PREF_KEY_BILLING_LANG)
                ?: prefs.getString(LEGACY_PREF_KEY_BILLING_LANG)
                ?: "ta"
        } catch (_: Exception) { "ta" }
    }

    fun refreshProfileStatus() {
        try {
            FactoryProfileRepository.refreshFromDatabase()
            TextilesProfileRepository.refreshFromDatabase()
            isProfilesLoading = false
        } catch (_: Exception) {
            isProfilesLoading = false
        }
    }

    /**
     * Returns true if at least one profile exists across Factory or Textiles.
     */
    val isSetupComplete: Boolean
        get() {
            val factoryProfiles = FactoryProfileRepository.allProfiles
            val textilesProfiles = TextilesProfileRepository.allProfiles
            return factoryProfiles.isNotEmpty() || textilesProfiles.isNotEmpty()
        }

    /**
     * Returns which profiles are missing ("factory" and/or "textiles").
     */
    val missingProfiles: List<String>
        get() {
            val missing = mutableListOf<String>()
            if (FactoryProfileRepository.allProfiles.isEmpty()) missing.add("factory")
            if (TextilesProfileRepository.allProfiles.isEmpty()) missing.add("textiles")
            return missing
        }

    fun hasProfile(scope: String = "factory"): Boolean {
        return if (scope.equals("textiles", ignoreCase = true) || scope.equals("pattu", ignoreCase = true)) {
            TextilesProfileRepository.allProfiles.isNotEmpty()
        } else {
            FactoryProfileRepository.allProfiles.isNotEmpty()
        }
    }
}
