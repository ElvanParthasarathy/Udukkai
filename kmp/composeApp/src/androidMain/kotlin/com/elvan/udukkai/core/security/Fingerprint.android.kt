package com.elvan.udukkai.core.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.elvan.udukkai.core.platform.AppContext
import com.elvan.udukkai.core.platform.CurrentActivity

actual fun fingerprintAvailable(): Boolean = try {
    BiometricManager.from(AppContext.context).canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
} catch (_: Exception) {
    false
}

/** Fingerprint only: the phone's screen lock is never offered. Anything but a match (cancel, error, lock-out) is "no". */
actual fun authenticateFingerprint(title: String, cancelText: String, onResult: (Boolean) -> Unit) {
    // Whatever goes wrong with the sensor, the app must not crash: it counts as not matched, and the PIN is there
    try {
        val activity = CurrentActivity.activity as? FragmentActivity
        if (activity == null || !fingerprintAvailable()) return onResult(false)
        val prompt = BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onResult(true)
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) = onResult(false)
        })
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setNegativeButtonText(cancelText)
                .setAllowedAuthenticators(BIOMETRIC_STRONG)
                .setConfirmationRequired(false)
                .build()
        )
    } catch (e: Exception) {
        android.util.Log.e("Fingerprint", "Could not ask for a fingerprint: ${e.message}", e)
        onResult(false)
    }
}
