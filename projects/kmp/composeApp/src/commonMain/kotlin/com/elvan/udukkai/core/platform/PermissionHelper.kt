package com.elvan.udukkai.core.platform

/**
 * Cross-platform storage permission helper.
 * Android: checks MANAGE_EXTERNAL_STORAGE (API 30+) or READ/WRITE_EXTERNAL_STORAGE.
 * Desktop: always returns true (no runtime permissions needed).
 */
expect fun isStoragePermissionGranted(): Boolean

expect fun requestStoragePermission()
