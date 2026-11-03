package com.elvan.udukkai.core.security

/** Whether the phone has a screen lock (PIN, pattern, password) set, so there is something to ask. */
expect fun deviceLockAvailable(): Boolean

/**
 * Asks the user to unlock with the phone's screen lock or its fingerprint / face. [onResult] gets true only when it was
 * passed; false when it was cancelled or failed. Always called on the main thread.
 */
expect fun authenticateDeviceLock(title: String, onResult: (Boolean) -> Unit)
