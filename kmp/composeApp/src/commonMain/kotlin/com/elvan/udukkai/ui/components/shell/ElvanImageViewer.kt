package com.elvan.udukkai.ui.components.shell

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale

/**
 * ElvanImageViewer — Replicates Flutter's `ElvanImageViewer` (`elvan_oavuru_kaatchi.dart`).
 * Renders an image from either a file path, content URI, or base64 encoded string.
 */
@Composable
expect fun ElvanImageViewer(
    value: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
)
