package com.elvan.udukkai.core.print

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Whether this platform can render the React print templates. */
expect val isDocumentPrintSupported: Boolean

enum class DocumentPrintAction { Print, Share, Open }

enum class PrintNoticeKind { Saved, Failed }

/** Result message shown after a save / failure; [detail] is e.g. the saved location. */
data class PrintNotice(val kind: PrintNoticeKind, val detail: String = "")

/**
 * Drives a [DocumentPrintView]: print / share requests made before the
 * document has finished rendering are queued and run once it is ready.
 */
class DocumentPrintController {
    var isReady by mutableStateOf(false)
        internal set
    var isBusy by mutableStateOf(false)
        internal set

    /** Current zoom level of the shown sheet (1 = fitted to the screen). */
    var zoomScale by mutableStateOf(1f)

    /** True while the preview hides its bars so the page fills the screen (zoomed in, or tapped). */
    var immersive by mutableStateOf(false)

    /** Local path of the finished PDF (changes when the document or the options change). */
    var pdfPath by mutableStateOf<String?>(null)
        internal set

    /** The sheet (paper only) rendered to a sharp image once the document is ready. */
    var sheet by mutableStateOf<ImageBitmap?>(null)
        internal set

    /** Layout of the printed / saved / shared PDF; remembered between sessions. */
    var options by mutableStateOf(PrintOptions.load())
        private set

    /** PDF file name without extension (edited in the options panel). */
    /** Business folder the stored PDF goes to (see billBusinessFolder). */
    var folder by mutableStateOf("")

    var fileName by mutableStateOf("")

    var notice by mutableStateOf<PrintNotice?>(null)
        internal set

    fun updateOptions(newOptions: PrintOptions) {
        options = newOptions
        newOptions.save()
    }

    internal var handler: ((DocumentPrintAction) -> Unit)? = null
    private var pending: DocumentPrintAction? = null

    fun print() = request(DocumentPrintAction.Print)
    fun share() = request(DocumentPrintAction.Share)
    fun open() = request(DocumentPrintAction.Open)

    private fun request(action: DocumentPrintAction) {
        val h = handler
        if (isReady && h != null) h(action) else pending = action
    }

    internal fun markReady() {
        isReady = true
        val action = pending ?: return
        pending = null
        handler?.invoke(action)
    }
}

/**
 * Renders a document with the React print templates (see react/print).
 * [fileName] is used for the print job and the shared PDF, without extension.
 */
@Composable
expect fun DocumentPrintView(
    payloadJson: String,
    fileName: String,
    controller: DocumentPrintController,
    modifier: Modifier = Modifier,
    topInset: Dp = 0.dp
)

/**
 * Shows [DocumentPrintController.pdfPath] in the platform PDF viewer (Android: Google's
 * androidx.pdf viewer). [contentPadding] keeps the pages clear of the top bar / bottom pill.
 */
@Composable
expect fun DocumentPdfViewer(
    controller: DocumentPrintController,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
)
