package com.elvan.udukkai.core.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.elvan.udukkai.core.platform.getPreferencesHelper
import com.elvan.udukkai.core.sync.getFirebaseSyncManager
import com.elvan.udukkai.factory.data.FactoryProfileRepository
import com.elvan.udukkai.textiles.data.TextilesProfileRepository

/** Which standalone app is running: each has its own business profiles. */
enum class AppScope { TEXTILES, FACTORY }

/**
 * Central authentication and onboarding state manager.
 * Manages login status and profile setup completion for the app flow.
 */
object AuthManager {

    private const val PREF_KEY_BILLING_LANG = "udukkai_setup_billingLang"
    private const val LEGACY_PREF_KEY_BILLING_LANG = "elvanniril_setup_billingLang"
    private const val PREF_KEY_SKIP_RESTORE = "skip_restore"

    var isLoggedIn by mutableStateOf(false)
        private set

    /** Google account that is signed in, shown in settings. */
    var signedInEmail by mutableStateOf<String?>(null)
        private set

    /** Set by the onboarding gate; decides which database branch is synced. */
    var appScope: AppScope = AppScope.TEXTILES

    var isProfilesLoading by mutableStateOf(true)
        private set

    var skipRestore by mutableStateOf(false)
        private set

    fun init() {
        try {
            skipRestore = getPreferencesHelper().getBoolean(PREF_KEY_SKIP_RESTORE, false)
        } catch (_: Exception) {}
        // Signed in only while Firebase still holds an allowed Google account
        val email = platformSignedInEmail()
        if (email != null && !AllowedAccounts.isAllowed(email)) platformSignOut()
        isLoggedIn = AllowedAccounts.isAllowed(email)
        signedInEmail = if (isLoggedIn) email else null
        refreshProfileStatus()
        if (isLoggedIn) {
            try {
                getFirebaseSyncManager().startSync(if (appScope == AppScope.FACTORY) "factory" else "textiles")
            } catch (_: Exception) {}
        }
    }

    /** Flutter's isSetupComplete, for one standalone app. */
    fun isSetupComplete(scope: AppScope): Boolean = when (scope) {
        AppScope.TEXTILES -> TextilesProfileRepository.allProfiles.isNotEmpty()
        AppScope.FACTORY -> FactoryProfileRepository.allProfiles.isNotEmpty()
    }

    /** Google sign-in for the allowed accounts; starts the cloud sync once signed in. */
    suspend fun signInWithGoogle(): GoogleSignInResult {
        val result = platformGoogleSignIn()
        if (result is GoogleSignInResult.Success) {
            isLoggedIn = true
            signedInEmail = result.email
            try {
                getFirebaseSyncManager().startSync(if (appScope == AppScope.FACTORY) "factory" else "textiles")
            } catch (_: Exception) {}
        }
        return result
    }

    /** Email and password sign-in for the allowed accounts; same follow-up as the Google one. */
    suspend fun signInWithEmail(email: String, password: String): GoogleSignInResult {
        val result = platformEmailSignIn(email, password)
        if (result is GoogleSignInResult.Success) {
            isLoggedIn = true
            signedInEmail = result.email
            try {
                getFirebaseSyncManager().startSync(if (appScope == AppScope.FACTORY) "factory" else "textiles")
            } catch (_: Exception) {}
        }
        return result
    }

    fun logout() {
        isLoggedIn = false
        signedInEmail = null
        try {
            getFirebaseSyncManager().stopSync()
        } catch (_: Exception) {}
        platformSignOut()
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
