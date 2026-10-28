package com.elvan.udukkai.core.auth

/** The photo of the signed-in Google account as it was last downloaded; no network, null if none. */
expect fun platformCachedPhotoBytes(): ByteArray?

/** Downloads the current photo of the signed-in Google account (and keeps it for next time). */
expect suspend fun platformProfilePhotoBytes(): ByteArray?
