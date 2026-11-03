package com.elvan.udukkai.core.platform

import androidx.compose.runtime.Composable

@Composable
actual fun AppPredictiveBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // Desktop platform does not have a physical back button or gesture navigation
}
