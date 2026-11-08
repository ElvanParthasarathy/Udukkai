package com.elvan.udukkai.core.platform

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

actual fun isStoragePermissionGranted(): Boolean {
    if (!AppContext.isInitialized) return false
    // Android 11+: the app writes only its own files (Documents/Udukkai, MediaStore), which needs no permission
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        true
    } else {
        val ctx = AppContext.context
        val readGranted = ContextCompat.checkSelfPermission(
            ctx, Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val writeGranted = ContextCompat.checkSelfPermission(
            ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        readGranted && writeGranted
    }
}

private var hasRequestedPermissionOnce = false

actual fun requestStoragePermission() {
    if (!AppContext.isInitialized) return
    val ctx = AppContext.context
    val activity = CurrentActivity.activity
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return
        } else {
            if (activity != null) {
                val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    activity, Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                if (hasRequestedPermissionOnce && !shouldShowRationale) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${ctx.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    activity.startActivity(intent)
                } else {
                    hasRequestedPermissionOnce = true
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ),
                        1001
                    )
                }
            } else {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${ctx.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                ctx.startActivity(intent)
            }
        }
    } catch (_: Exception) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${ctx.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ctx.startActivity(intent)
        } catch (_: Exception) {}
    }
}
