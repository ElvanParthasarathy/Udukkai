package android.print

import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.webkit.WebView
import java.io.File

/**
 * Writes a WebView's content to a PDF through Chromium's own print pipeline, so text stays real,
 * selectable vector text. Lives in `android.print` because the layout / write callbacks have
 * package-private constructors.
 */
object PdfExporter {
    /** [onDone] gets the page count, or -1 when the export failed. */
    fun export(webView: WebView, jobName: String, attributes: PrintAttributes, output: File, onDone: (Int) -> Unit) {
        try {
            val adapter = webView.createPrintDocumentAdapter(jobName)
            val cancel = CancellationSignal()
            adapter.onStart()
            adapter.onLayout(null, attributes, cancel, object : PrintDocumentAdapter.LayoutResultCallback() {
                override fun onLayoutFinished(info: PrintDocumentInfo, changed: Boolean) {
                    try {
                        val fd = ParcelFileDescriptor.open(
                            output,
                            ParcelFileDescriptor.MODE_READ_WRITE or ParcelFileDescriptor.MODE_CREATE or ParcelFileDescriptor.MODE_TRUNCATE
                        )
                        adapter.onWrite(arrayOf(PageRange.ALL_PAGES), fd, cancel, object : PrintDocumentAdapter.WriteResultCallback() {
                            override fun onWriteFinished(pages: Array<out PageRange>) {
                                try { fd.close() } catch (_: Exception) {}
                                adapter.onFinish()
                                onDone(info.pageCount.coerceAtLeast(1))
                            }

                            override fun onWriteFailed(error: CharSequence?) {
                                try { fd.close() } catch (_: Exception) {}
                                adapter.onFinish()
                                onDone(-1)
                            }
                        })
                    } catch (e: Exception) {
                        onDone(-1)
                    }
                }

                override fun onLayoutFailed(error: CharSequence?) = onDone(-1)
            }, null)
        } catch (e: Exception) {
            onDone(-1)
        }
    }
}
