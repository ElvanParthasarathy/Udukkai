package com.elvan.udukkai.ui.screens.preview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntOffset
import com.elvan.udukkai.core.platform.AppBackHandler

/**
 * The bill as it is being edited (not saved yet), rendered like the saved one. Slides in over the
 * editor; Edit and Back both return to it. [payload] null hides it.
 */
@Composable
fun DraftBillPreview(payload: String?, title: String, fileName: String, onClose: () -> Unit) {
    // The last payload stays while the slide-out plays
    var shown by remember { mutableStateOf<String?>(null) }
    if (payload != null) shown = payload
    val slide = spring<IntOffset>(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioNoBouncy)
    AnimatedVisibility(
        visible = payload != null,
        enter = slideInHorizontally(slide) { it },
        exit = slideOutHorizontally(slide) { it }
    ) {
        AppBackHandler(enabled = payload != null) { onClose() }
        val current = shown
        if (current != null) {
            PreviewScreen(
                title = title,
                onBack = onClose,
                isDraft = true,
                printPayload = current,
                printFileName = fileName
            )
        }
    }
}
