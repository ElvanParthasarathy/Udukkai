package com.elvan.udukkai.core.print

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/** Whether this platform can render the React print templates. */
expect val isDocumentPrintSupported: Boolean

enum class DocumentPrintAction { Print, Share }

/**
 * Drives a [DocumentPrintView]: print / share requests made before the
 * document has finished rendering are queued and run once it is ready.
 */
class DocumentPrintController {
    var isReady by mutableStateOf(false)
        internal set
    var isBusy by mutableStateOf(false)
        internal set

    internal var handler: ((DocumentPrintAction) -> Unit)? = null
    private var pending: DocumentPrintAction? = null

    fun print() = request(DocumentPrintAction.Print)
    fun share() = request(DocumentPrintAction.Share)

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
    modifier: Modifier = Modifier
)
