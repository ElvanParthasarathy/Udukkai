package com.elvan.udukkai.core.security

/** Whether this phone can read a fingerprint right now (the hardware is there and a fingerprint is saved on the phone). */
expect fun fingerprintAvailable(): Boolean

/**
 * Asks for a fingerprint only: the phone's screen lock is never offered. [cancelText] is the sheet's cancel button
 * ("Use PIN"). [onResult] gets true only when a saved fingerprint matched; false when cancelled or on any error.
 * Always called on the main thread.
 */
expect fun authenticateFingerprint(title: String, cancelText: String, onResult: (Boolean) -> Unit)
