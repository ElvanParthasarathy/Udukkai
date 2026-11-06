package com.elvan.udukkai.core.update

import android.app.Activity
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

/**
 * Handles Google Play In-App Updates for Play Store distribution.
 * Uses [AppUpdateType.IMMEDIATE] to ensure mandatory updates block the app
 * until completed, identical to Neram's update service.
 */
object PlayStoreUpdateHelper {
    private const val TAG = "PlayStoreUpdate"
    const val REQUEST_CODE = 1001

    private var appUpdateManager: AppUpdateManager? = null

    fun init(activity: Activity) {
        if (!AppUpdate.isPlayStoreChannel()) return
        try {
            appUpdateManager = AppUpdateManagerFactory.create(activity)
            checkForAppUpdate(activity)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to init PlayStore AppUpdateManager: ${e.message}")
        }
    }

    fun checkForAppUpdate(activity: Activity) {
        val manager = appUpdateManager ?: return
        manager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                startImmediateUpdate(activity, manager, info)
            }
        }.addOnFailureListener {
            Log.w(TAG, "PlayStore update check failed: ${it.message}")
        }
    }

    fun onResume(activity: Activity) {
        val manager = appUpdateManager ?: return
        manager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                startImmediateUpdate(activity, manager, info)
            }
        }
    }

    fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int) {
        if (requestCode == REQUEST_CODE && resultCode != Activity.RESULT_OK) {
            // If the update was cancelled or failed, re-launch immediately to make app unusable until updated
            checkForAppUpdate(activity)
        }
    }

    private fun startImmediateUpdate(activity: Activity, manager: AppUpdateManager, info: AppUpdateInfo) {
        try {
            manager.startUpdateFlowForResult(
                info,
                activity,
                AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE),
                REQUEST_CODE
            )
        } catch (e: Exception) {
            Log.e(TAG, "startUpdateFlowForResult failed: ${e.message}")
        }
    }
}
