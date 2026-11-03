package com.elvan.udukkai.core.security

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import androidx.activity.result.contract.ActivityResultContracts
import com.elvan.udukkai.core.platform.AppContext
import com.elvan.udukkai.core.platform.CurrentActivity

actual fun deviceLockAvailable(): Boolean = try {
    (AppContext.context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).isDeviceSecure
} catch (_: Exception) {
    false
}

actual fun authenticateDeviceLock(title: String, onResult: (Boolean) -> Unit) {
    // Whatever goes wrong with the phone's lock, the app must not crash: it counts as not unlocked
    try {
        askDeviceLock(title, onResult)
    } catch (e: Exception) {
        android.util.Log.e("DeviceLock", "Could not ask for the phone lock: ${e.message}", e)
        onResult(false)
    }
}

private fun askDeviceLock(title: String, onResult: (Boolean) -> Unit) {
    val activity: Activity = CurrentActivity.activity ?: return onResult(false)
    if (!deviceLockAvailable()) return onResult(false)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        // The system sheet: fingerprint / face first, the phone's PIN, pattern or password as the way in otherwise
        val prompt = BiometricPrompt.Builder(activity)
            .setTitle(title)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()
        prompt.authenticate(CancellationSignal(), activity.mainExecutor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) = onResult(true)
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) = onResult(false)
        })
    } else {
        // Older phones: the phone's own confirm-your-lock page
        @Suppress("DEPRECATION")
        val intent = (activity.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).createConfirmDeviceCredentialIntent(title, null)
            ?: return onResult(false)
        val key = "device_lock_${System.nanoTime()}"
        var launcher: androidx.activity.result.ActivityResultLauncher<android.content.Intent>? = null
        val componentActivity = activity as? androidx.activity.ComponentActivity ?: return onResult(false)
        launcher = componentActivity.activityResultRegistry.register(key, ActivityResultContracts.StartActivityForResult()) { result ->
            launcher?.unregister()
            onResult(result.resultCode == Activity.RESULT_OK)
        }
        launcher.launch(intent)
    }
}
