package com.elvan.udukkai.core.platform

/**
 * Desktop does not require runtime storage permissions.
 */
actual fun isStoragePermissionGranted(): Boolean = true

actual fun requestStoragePermission() {
    // No-op on desktop
}
