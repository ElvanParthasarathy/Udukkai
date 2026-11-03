package com.elvan.udukkai.core.platform

import androidx.compose.runtime.Composable

/**
 * Back inside the app. There is no preview animation of our own: inside one screen of the app the phone cannot show its
 * system back animation, and a copy made by the app is not the phone's. At the first screen no handler is on, so the
 * phone's own back-to-home preview plays.
 */
@Composable
expect fun AppPredictiveBackHandler(enabled: Boolean = true, onBack: () -> Unit)

@Composable
fun AppBackHandler(enabled: Boolean = true, onBack: () -> Unit) = AppPredictiveBackHandler(enabled = enabled, onBack = onBack)
