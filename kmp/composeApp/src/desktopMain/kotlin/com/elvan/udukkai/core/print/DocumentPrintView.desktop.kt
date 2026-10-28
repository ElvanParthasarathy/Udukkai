package com.elvan.udukkai.core.print

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// Desktop rendering of the React print templates is not wired up yet;
// callers fall back to the placeholder preview.
actual val isDocumentPrintSupported: Boolean = false

@Composable
actual fun DocumentPrintView(
    payloadJson: String,
    fileName: String,
    controller: DocumentPrintController,
    modifier: Modifier,
    topInset: androidx.compose.ui.unit.Dp
) {
}

@Composable
actual fun DocumentPdfViewer(
    controller: DocumentPrintController,
    modifier: Modifier,
    contentPadding: androidx.compose.foundation.layout.PaddingValues
) {
}
