package com.elvan.udukkai.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
expect fun AppPredictiveBackHandler(
    enabled: Boolean = true,
    onProgress: (progress: Float, edge: Int) -> Unit = { _, _ -> },
    onCancel: () -> Unit = {},
    onBack: () -> Unit
)

/** How far a back gesture handled by [AppBackHandler] has been swiped (0 = none), and from which edge (0 left, 1 right). */
object BackGesture {
    var progress by mutableFloatStateOf(0f)
    var edge by mutableIntStateOf(0)
}

/** The screen shrinks and slides a little under the finger while a back gesture is swiped, like the system's own previews. */
fun Modifier.backGestureScale(): Modifier = graphicsLayer {
    val p = BackGesture.progress
    if (p > 0f) {
        val scale = 1f - 0.08f * p
        scaleX = scale
        scaleY = scale
        translationX = (if (BackGesture.edge == 0) 1f else -1f) * p * 24.dp.toPx()
        // Rounded corners appear as it shrinks, like the system's own preview
        shape = RoundedCornerShape((28f * p).dp)
        clip = true
    }
}

@Composable
fun AppBackHandler(enabled: Boolean = true, onBack: () -> Unit) {
    AppPredictiveBackHandler(
        enabled = enabled,
        onProgress = { p, e -> BackGesture.progress = p; BackGesture.edge = e },
        onCancel = { BackGesture.progress = 0f },
        onBack = onBack
    )
}
