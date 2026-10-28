package com.elvan.udukkai.core.print

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import kotlinx.coroutines.delay
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
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
    val background = MaterialTheme.colorScheme.background.toArgb()
    var loaded by remember { mutableStateOf<String?>(null) }

    var pdfView by remember { mutableStateOf<PDFView?>(null) }
    var zoomFlag by remember { mutableStateOf(false) }
    // Zooming in goes full screen like a photo gallery; back at fit-to-width the bars return
    LaunchedEffect(pdfView) {
        val view = pdfView ?: return@LaunchedEffect
        while (true) {
            delay(100)
            val zoomed = view.zoom > 1.05f
            if (zoomed != zoomFlag) {
                zoomFlag = zoomed
                controller.immersive = zoomed
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose { controller.immersive = false }
    }

    AndroidView(
        modifier = modifier.fillMaxSize().padding(contentPadding).clipToBounds(),
        factory = { ctx -> PDFView(ctx, null).apply { setBackgroundColor(background); pdfView = this } },
        update = { view ->
            if (loaded != path) {
                loaded = path
                view.fromFile(File(path))
                    .enableSwipe(true)
                    .enableDoubletap(true)
                    .onRender { view.resetZoom() }
                    .onTap {
                        // A tap hides or shows the bars only while zoomed in; the normal page keeps them
                        if (view.zoom > 1.05f) controller.immersive = !controller.immersive
                        true
                    }
                    .swipeHorizontal(false)
                    .pageFitPolicy(FitPolicy.WIDTH)
                    .spacing(8)
                    .enableAntialiasing(true)
                    .load()
                view.minZoom = 1f
                view.midZoom = 2f
                view.maxZoom = 5f
            }
        }
    )
}
