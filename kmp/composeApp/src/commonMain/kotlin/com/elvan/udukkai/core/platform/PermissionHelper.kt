package com.elvan.udukkai.core.platform

/**
 * Cross-platform storage permission helper.
 * Android: none needed on 11+ (the app writes only its own files); READ/WRITE_EXTERNAL_STORAGE below that.
 * Desktop: always returns true (no runtime permissions needed).
 */
expect fun isStoragePermissionGranted(): Boolean

expect fun requestStoragePermission()
