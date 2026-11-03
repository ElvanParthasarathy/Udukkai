package com.elvan.udukkai.core.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.elvan.udukkai.core.platform.AppContext
import com.elvan.udukkai.core.platform.CurrentActivity

private const val AUTHENTICATORS = BIOMETRIC_STRONG or DEVICE_CREDENTIAL

/** Whether this device has biometric or screen lock (PIN/pattern/password) set and available. */
actual fun fingerprintAvailable(): Boolean = try {
    BiometricManager.from(AppContext.context).canAuthenticate(AUTHENTICATORS) == BiometricManager.BIOMETRIC_SUCCESS
} catch (_: Exception) {
    false
}

/**
 * Asks for Android system authentication: biometric (Fingerprint/Face) with seamless
 * fallback to device screen lock (PIN/Pattern/Password). Always called on the main thread.
 */
actual fun authenticateFingerprint(title: String, cancelText: String, onResult: (Boolean) -> Unit) {
    try {
        val activity = CurrentActivity.activity as? FragmentActivity
        if (activity == null || !fingerprintAvailable()) return onResult(false)
        val prompt = BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onResult(true)
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) = onResult(false)
            override fun onAuthenticationFailed() {
                // Biometric mismatch; user can retry on sensor or tap device screen lock
            }
        })
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setAllowedAuthenticators(AUTHENTICATORS)
            .setConfirmationRequired(false)
            .build()
        prompt.authenticate(promptInfo)
    } catch (e: Exception) {
        android.util.Log.e("Fingerprint", "Could not authenticate with system credentials: ${e.message}", e)
        onResult(false)
    }
}
