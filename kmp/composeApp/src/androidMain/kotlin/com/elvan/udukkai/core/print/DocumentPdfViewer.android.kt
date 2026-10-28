package com.elvan.udukkai.core.print

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.viewinterop.AndroidView
import androidx.pdf.PdfDocument
import androidx.pdf.SandboxedPdfLoader
import androidx.pdf.view.PdfView
import kotlinx.coroutines.Dispatchers
import java.io.File

private const val TAG = "DocumentPdfViewer"

/**
 * Google's Jetpack PDF viewer (androidx.pdf PdfView): real PDF pages rendered sharp at every
 * zoom level, continuous scrolling, fast-scroll page indicator and text selection.
 */
@Composable
actual fun DocumentPdfViewer(
    controller: DocumentPrintController,
    modifier: Modifier,
    contentPadding: PaddingValues
) {
    val path = controller.pdfPath ?: return
    val context = LocalContext.current
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    var document by remember { mutableStateOf<PdfDocument?>(null) }

    // Open the document in the PDF sandbox service; the previous one is closed once replaced
    LaunchedEffect(path) {
        val opened = try {
            SandboxedPdfLoader(context.applicationContext, Dispatchers.IO)
                .openDocument(Uri.fromFile(File(path)), null)
        } catch (e: Exception) {
            Log.e(TAG, "Opening PDF failed: ${e.message}", e)
            null
        }
        val previous = document
        document = opened
        try { previous?.close() } catch (_: Exception) {}
    }
    DisposableEffect(Unit) {
        onDispose { try { document?.close() } catch (_: Exception) {} }
    }

    val padStart = with(density) { contentPadding.calculateStartPadding(layoutDirection).roundToPx() }
    val padTop = with(density) { contentPadding.calculateTopPadding().roundToPx() }
    val padEnd = with(density) { contentPadding.calculateEndPadding(layoutDirection).roundToPx() }
    val padBottom = with(density) { contentPadding.calculateBottomPadding().roundToPx() }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            PdfView(ctx).apply {
                // Pages scroll under the (transparent) top bar and bottom pill
                clipToPadding = false
                verticalAlignment = PdfView.VERTICAL_ALIGNMENT_TOP
                fastScrollVisibility = PdfView.FastScrollVisibility.AUTO_HIDE
                addOnViewportChangedListener(object : PdfView.OnViewportChangedListener {
                    override fun onViewportChanged(
                        firstVisiblePage: Int,
                        visiblePagesCount: Int,
                        pageLocations: android.util.SparseArray<android.graphics.RectF>,
                        zoomLevel: Float
                    ) {
                        // Collapses the top bar title once the pages are zoomed past fit-to-width or scrolled
                        // (page locations are in view pixels, already zoomed)
                        val pageWidth = pageLocations.get(firstVisiblePage)?.width() ?: 0f
                        val fitWidth = (width - paddingLeft - paddingRight).toFloat()
                        val zoomed = pageWidth > fitWidth * 1.05f
                        controller.zoomScale = if (zoomed || scrollY > 8) 2f else 1f
                    }
                })
            }
        },
        update = { view ->
            view.setPadding(padStart, padTop, padEnd, padBottom)
            val doc = document
            if (doc != null && view.pdfDocument !== doc) view.pdfDocument = doc
        }
    )
        // The PDF service needs a moment the first time
        if (document == null) CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground)
    }
}
