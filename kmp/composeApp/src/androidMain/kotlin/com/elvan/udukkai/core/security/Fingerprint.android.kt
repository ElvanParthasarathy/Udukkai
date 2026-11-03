package com.elvan.udukkai.core.security

import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import com.elvan.udukkai.core.platform.AppContext
import com.elvan.udukkai.core.platform.CurrentActivity

actual fun fingerprintAvailable(): Boolean = try {
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ->
            AppContext.context.getSystemService(BiometricManager::class.java)
                .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->
            @Suppress("DEPRECATION")
            AppContext.context.getSystemService(BiometricManager::class.java).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
        else -> false
    }
} catch (_: Exception) {
    false
}

actual fun authenticateFingerprint(title: String, cancelText: String, onResult: (Boolean) -> Unit) {
    // Whatever goes wrong with the sensor, the app must not crash: it counts as not matched, and the PIN is there
    try {
        val activity = CurrentActivity.activity
        if (activity == null || !fingerprintAvailable()) return onResult(false)
        val builder = BiometricPrompt.Builder(activity)
            .setTitle(title)
            .setNegativeButton(cancelText, activity.mainExecutor) { _, _ -> onResult(false) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) builder.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        builder.build().authenticate(CancellationSignal(), activity.mainExecutor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) = onResult(true)
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                // The cancel button reports through its own listener
                if (errorCode != BiometricPrompt.BIOMETRIC_ERROR_USER_CANCELED) onResult(false)
            }
        })
    } catch (e: Exception) {
        android.util.Log.e("Fingerprint", "Could not ask for a fingerprint: ${e.message}", e)
        onResult(false)
    }
}
