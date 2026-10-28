package com.elvan.udukkai.core.platform

import android.app.Activity
import java.lang.ref.WeakReference

/** The visible activity, needed for UI that must be shown from shared code (Google account chooser). */
object CurrentActivity {
    private var ref: WeakReference<Activity>? = null

    var activity: Activity?
        get() = ref?.get()
        set(value) {
            ref = value?.let { WeakReference(it) }
        }
}
