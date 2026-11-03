package com.elvan.udukkai.core.security

actual fun deviceLockAvailable(): Boolean = false

// A computer has no phone lock to ask: actions go through
actual fun authenticateDeviceLock(title: String, onResult: (Boolean) -> Unit) = onResult(true)
