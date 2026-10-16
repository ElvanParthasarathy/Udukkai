package com.elvan.udukkai.core.platform

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.Composable
import kotlinx.coroutines.CancellationException

@Composable
actual fun AppBackHandler(enabled: Boolean, onBack: () -> Unit) {
    PredictiveBackHandler(enabled = enabled) { progress ->
        try {
            progress.collect {
                // Predictive back progress tracking
            }
            onBack()
        } catch (_: CancellationException) {
            // User cancelled back gesture
        }
    }
}
