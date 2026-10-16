package com.elvan.udukkai.core.platform

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.Composable
import kotlinx.coroutines.CancellationException

@Composable
actual fun AppPredictiveBackHandler(
    enabled: Boolean,
    onProgress: (progress: Float, edge: Int) -> Unit,
    onCancel: () -> Unit,
    onBack: () -> Unit
) {
    PredictiveBackHandler(enabled = enabled) { progress ->
        try {
            progress.collect { backEvent ->
                onProgress(backEvent.progress, backEvent.swipeEdge)
            }
            onProgress(0f, 0)
            onBack()
        } catch (_: CancellationException) {
            onProgress(0f, 0)
            onCancel()
        }
    }
}
