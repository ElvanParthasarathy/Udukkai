package com.elvan.udukkai.core.security

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.elvan.udukkai.core.platform.AppContext
import com.elvan.udukkai.core.platform.CurrentActivity

object DeviceAuthHelper {
    var pendingAuthCallback: ((Boolean) -> Unit)? = null
    const val REQUEST_CODE_CONFIRM_CREDENTIAL = 9988

    fun onActivityResult(requestCode: Int, resultCode: Int): Boolean {
        if (requestCode == REQUEST_CODE_CONFIRM_CREDENTIAL) {
            val cb = pendingAuthCallback
            pendingAuthCallback = null
            cb?.invoke(resultCode == Activity.RESULT_OK)
            return true
        }
        return false
    }
}

/** Whether this device has biometric or screen lock (PIN/pattern/password) set and available. */
actual fun fingerprintAvailable(): Boolean {
    if (!AppContext.isInitialized) return false
    val ctx = AppContext.context
    val km = ctx.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
    val hasDeviceLock = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        km?.isDeviceSecure == true
    } else {
        km?.isKeyguardSecure == true
    }
    if (hasDeviceLock) return true

    return try {
        val bm = BiometricManager.from(ctx)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            bm.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS
        } else {
            @Suppress("DEPRECATION")
            bm.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
        }
    } catch (_: Exception) {
        false
    }
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
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                // If biometric prompt cannot be shown on Android 10 or device has only PIN/password,
                // fall back to native KeyguardManager device credential confirmation
                if (errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS ||
                    errorCode == BiometricPrompt.ERROR_HW_NOT_PRESENT ||
                    errorCode == BiometricPrompt.ERROR_HW_UNAVAILABLE
                ) {
                    launchKeyguardConfirmation(activity, title, onResult)
                } else {
                    onResult(false)
                }
            }
            override fun onAuthenticationFailed() {
                // Biometric mismatch; user can retry on sensor or enter device credentials
            }
        })

        val builder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setConfirmationRequired(false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
        } else {
            @Suppress("DEPRECATION")
            builder.setDeviceCredentialAllowed(true)
        }

        prompt.authenticate(builder.build())
    } catch (e: Exception) {
        android.util.Log.e("Fingerprint", "BiometricPrompt failed, falling back: ${e.message}", e)
        val activity = CurrentActivity.activity
        if (activity != null) {
            launchKeyguardConfirmation(activity, title, onResult)
        } else {
            onResult(false)
        }
    }
}

private fun launchKeyguardConfirmation(activity: Activity, title: String, onResult: (Boolean) -> Unit) {
    try {
        val km = activity.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        val intent = km?.createConfirmDeviceCredentialIntent(title, "")
        if (intent != null) {
            DeviceAuthHelper.pendingAuthCallback = onResult
            activity.startActivityForResult(intent, DeviceAuthHelper.REQUEST_CODE_CONFIRM_CREDENTIAL)
        } else {
            onResult(false)
        }
    } catch (e: Exception) {
        android.util.Log.e("Fingerprint", "Keyguard fallback failed: ${e.message}", e)
        onResult(false)
    }
}

actual fun openDeviceSecuritySettings() {
    try {
        // Direct intent to set up a new lock screen PIN / pattern / password
        val intent = android.content.Intent(android.app.admin.DevicePolicyManager.ACTION_SET_NEW_PASSWORD).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        }
        AppContext.context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val intent = android.content.Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            }
            AppContext.context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val fallback = android.content.Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                }
                AppContext.context.startActivity(fallback)
            } catch (_: Exception) {}
        }
    }
}
