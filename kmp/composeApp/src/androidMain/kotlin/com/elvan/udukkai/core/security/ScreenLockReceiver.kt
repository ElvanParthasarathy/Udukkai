package com.elvan.udukkai.core.security

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

object ScreenLockReceiver {
    private var registered = false

    fun register(context: Context) {
        if (registered) return
        try {
            val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
            context.applicationContext.registerReceiver(object : BroadcastReceiver() {
                override fun onReceive(c: Context?, intent: Intent?) {
                    if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                        AppLock.onScreenOff()
                    }
                }
            }, filter)
            registered = true
        } catch (_: Exception) {
        }
    }
}
