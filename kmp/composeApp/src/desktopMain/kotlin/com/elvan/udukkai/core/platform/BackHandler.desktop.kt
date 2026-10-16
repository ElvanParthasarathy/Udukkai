package com.elvan.udukkai.core.platform

import androidx.compose.runtime.Composable

@Composable
actual fun AppPredictiveBackHandler(
    enabled: Boolean,
    onProgress: (progress: Float, edge: Int) -> Unit,
    onCancel: () -> Unit,
    onBack: () -> Unit
) {
    // Desktop platform does not have a physical back button or gesture navigation
}
