package com.elvan.udukkai.core.security

actual fun fingerprintAvailable(): Boolean = false

actual fun authenticateFingerprint(title: String, cancelText: String, onResult: (Boolean) -> Unit) = onResult(false)

actual fun openDeviceSecuritySettings() {}
