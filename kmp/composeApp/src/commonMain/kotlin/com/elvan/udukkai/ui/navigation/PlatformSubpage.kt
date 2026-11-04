package com.elvan.udukkai.ui.navigation

/**
 * Opens a subpage using a native platform window (e.g. Activity on Android).
 * Returns true if handled natively, false to use in-window Compose navigation (Desktop).
 */
expect fun openPlatformSubpage(subpage: ActiveSubpage): Boolean
