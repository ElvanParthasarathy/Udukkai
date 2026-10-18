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
            // Let the caller inspect the completed gesture before clearing its progress.
            // HomeScreen uses this to avoid replaying the normal subpage transition.
            onBack()
            onProgress(0f, 0)
        } catch (_: CancellationException) {
            onProgress(0f, 0)
            onCancel()
        }
    }
}
