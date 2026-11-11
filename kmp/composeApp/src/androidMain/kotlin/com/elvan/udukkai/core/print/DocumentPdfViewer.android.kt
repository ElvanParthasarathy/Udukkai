package com.elvan.udukkai.core.print

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import kotlinx.coroutines.delay
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material3.MaterialTheme
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.util.FitPolicy
import java.io.File

/**
 * Open-source PDFium based viewer (AndroidPdfViewer): pages are rendered sharp at every zoom
 * level with smooth pinch, double-tap and fling.
 */
@Composable
actual fun DocumentPdfViewer(
    controller: DocumentPrintController,
    modifier: Modifier,
    contentPadding: PaddingValues
) {
    val path = controller.pdfPath ?: return
    val landscape = androidx.compose.ui.platform.LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val background = MaterialTheme.colorScheme.background.toArgb()


    var pdfView by remember { mutableStateOf<PDFView?>(null) }
    var zoomFlag by remember { mutableStateOf(false) }
    LaunchedEffect(landscape) { controller.immersive = false }
    // Portrait returns its controls at page fit; landscape remains an immersive document canvas.
    LaunchedEffect(pdfView, landscape) {
        val view = pdfView ?: return@LaunchedEffect
        while (true) {
            delay(100)
            val zoomed = view.zoom > 1.05f
            if (zoomed != zoomFlag) {
                zoomFlag = zoomed
                if (!landscape) controller.immersive = zoomed
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose { controller.immersive = false }
    }

    // PDFium retains its old fit dimensions during a rotation. A fresh view fits the new viewport.
    key(path, landscape) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            PDFView(ctx, null).apply {
                val view = this
                setBackgroundColor(background)
                pdfView = this
                view.fromFile(File(path))
                    .enableSwipe(true)
                    .enableDoubletap(true)
                    .onRender { view.resetZoom(); view.jumpTo(0, false) }
                    .onTap {
                        // Controls overlay the canvas, so showing them never changes the page fit.
                        controller.immersive = !controller.immersive
                        true
                    }
                    .swipeHorizontal(false)
                    .pageFitPolicy(FitPolicy.BOTH)
                    .fitEachPage(true)
                    .autoSpacing(true)
                    .spacing(8)
                    .enableAntialiasing(true)
                    .load()
                view.minZoom = 1f
                view.midZoom = 2f
                view.maxZoom = 5f
            }
        },
        onRelease = { view -> if (pdfView === view) pdfView = null; view.recycle() }
    )
    }
}
