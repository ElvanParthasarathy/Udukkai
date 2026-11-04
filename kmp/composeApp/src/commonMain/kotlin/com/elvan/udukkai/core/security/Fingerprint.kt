package com.elvan.udukkai.core.security

/** Whether this device can authenticate right now (biometric hardware or screen lock PIN/pattern is available). */
expect fun fingerprintAvailable(): Boolean

/**
 * Asks for system authentication (Fingerprint, Face, or device screen lock PIN/pattern).
 * [title] is the prompt title. [onResult] gets true only when authentication succeeded;
 * false when cancelled or on error. Always called on the main thread.
 */
expect fun authenticateFingerprint(title: String, cancelText: String, onResult: (Boolean) -> Unit)

/** Opens the device's system Security Settings so the user can configure a screen lock (PIN/pattern/biometric). */
expect fun openDeviceSecuritySettings()
