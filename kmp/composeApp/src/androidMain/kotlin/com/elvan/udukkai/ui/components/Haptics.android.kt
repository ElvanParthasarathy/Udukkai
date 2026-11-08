package com.elvan.udukkai.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import com.elvan.udukkai.core.platform.AppContext

private var lastTickAt = 0L

internal actual fun platformVibrate(kind: HapticKind) {
    val context = AppContext.context
    // The user's own touch feedback switch decides
    if (Settings.System.getInt(context.contentResolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) == 0) return
    // A wheel can pass many rows in a moment; ticks closer than this would blur into one buzz
    if (kind == HapticKind.Tick) {
        val now = System.currentTimeMillis()
        if (now - lastTickAt < 28) return
        lastTickAt = now
    }
    val vibrator: Vibrator = (if (Build.VERSION.SDK_INT >= 31) context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        else @Suppress("DEPRECATION") (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)) ?: return
    if (!vibrator.hasVibrator()) return
    val light = kind == HapticKind.Tick || kind == HapticKind.ToggleOff
    try {
        when {
            Build.VERSION.SDK_INT >= 29 -> {
                val effect = VibrationEffect.createPredefined(if (light) VibrationEffect.EFFECT_TICK else VibrationEffect.EFFECT_CLICK)
                if (Build.VERSION.SDK_INT >= 33) vibrator.vibrate(effect, VibrationAttributes.createForUsage(VibrationAttributes.USAGE_TOUCH))
                else vibrator.vibrate(effect)
            }
            Build.VERSION.SDK_INT >= 26 -> vibrator.vibrate(VibrationEffect.createOneShot(if (light) 8 else 16, VibrationEffect.DEFAULT_AMPLITUDE))
            else -> @Suppress("DEPRECATION") vibrator.vibrate(if (light) 8L else 16L)
        }
    } catch (_: Exception) {
        // Haptics are a courtesy: a vibrator that refuses must never break the screen
    }
}
