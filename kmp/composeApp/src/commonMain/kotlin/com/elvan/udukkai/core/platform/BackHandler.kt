package com.elvan.udukkai.core.platform

import androidx.compose.runtime.Composable

@Composable
expect fun AppPredictiveBackHandler(
    enabled: Boolean = true,
    onProgress: (progress: Float, edge: Int) -> Unit = { _, _ -> },
    onCancel: () -> Unit = {},
    onBack: () -> Unit
)

@Composable
fun AppBackHandler(enabled: Boolean = true, onBack: () -> Unit) {
    AppPredictiveBackHandler(
        enabled = enabled,
        onProgress = { _, _ -> },
        onCancel = {},
        onBack = onBack
    )
}
