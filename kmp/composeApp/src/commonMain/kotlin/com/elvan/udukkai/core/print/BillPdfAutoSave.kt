package com.elvan.udukkai.core.print

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.model.ReceiptModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** PDF file name of an invoice (same names the React app used). */
fun invoicePdfName(invoice: InvoiceModel): String =
    (if (invoice.invoiceType == "proforma") "EST_" else "INV_") + invoice.invoiceNumber.replace('/', '-')

/** PDF file name of a receipt (same names the React app used). */
fun receiptPdfName(receipt: ReceiptModel): String =
    "RCPT_" + receipt.receiptNumber.replace('/', '-')

/**
 * Every bill saved in an editor gets its PDF written to the documents folder, where the React app
 * kept them (Documents/Udukkai/<app>/<Invoice|Receipt>). Saving the bill again replaces that file;
 * when the bill number changed, the file under the old name is removed, so there are no duplicates.
 */
object BillPdfAutoSave {
    enum class Kind { Invoice, Receipt }

    /** [replaces] is the bill's file name before this save (null for a new bill). */
    data class Request(val kind: Kind, val id: Long, val replaces: String?, val seq: Long)

    internal val pending = mutableStateListOf<Request>()
    private var nextSeq = 0L

    /**
     * Queues the PDF of every bill that has none yet, so all bills of the app end up in the folders,
     * not only the ones saved from now on. Bills that already have their file are skipped.
     */
    suspend fun syncMissing(invoices: List<InvoiceModel>, receipts: List<ReceiptModel>) {
        if (!isDocumentPrintSupported) return
        val missing = withContext(Dispatchers.IO) {
            invoices.filter { !it.isDeleted && it.id > 0L && !billPdfExists(invoicePdfName(it)) }
                .map { Request(Kind.Invoice, it.id, null, 0L) } +
                receipts.filter { !it.isDeleted && it.id > 0L && !billPdfExists(receiptPdfName(it)) }
                    .map { Request(Kind.Receipt, it.id, null, 0L) }
        }
        missing.forEach { request(it.kind, it.id) }
    }

    fun request(kind: Kind, id: Long, replaces: String? = null) {
        if (!isDocumentPrintSupported || id <= 0L) return
        pending.removeAll { it.kind == kind && it.id == id }
        pending.add(Request(kind, id, replaces, nextSeq++))
    }
}

/** What the host needs to render one bill. */
class BillPdfJob(val payloadJson: String, val fileName: String)

/**
 * Renders the queued bills one at a time with a hidden print view and stores each PDF.
 * Hosted on the home screen, so it keeps going after the editor has closed.
 */
@Composable
fun BillPdfAutoSaveHost(build: (BillPdfAutoSave.Request) -> BillPdfJob?) {
    val request = BillPdfAutoSave.pending.firstOrNull() ?: return
    val job = remember(request) { build(request) }
    if (job == null) {
        LaunchedEffect(request) { BillPdfAutoSave.pending.remove(request) }
        return
    }
    key(request.seq) {
        val controller = remember { DocumentPrintController() }
        DocumentPrintView(
            payloadJson = job.payloadJson,
            fileName = job.fileName,
            controller = controller,
            modifier = Modifier
                .size(1.dp)
                .graphicsLayer { alpha = 0f }
        )
        LaunchedEffect(controller.pdfPath, controller.notice) {
            val path = controller.pdfPath
            if (path == null && controller.notice?.kind != PrintNoticeKind.Failed) return@LaunchedEffect
            if (path != null) {
                withContext(Dispatchers.IO) {
                    val old = request.replaces
                    if (old != null && old != job.fileName) deleteBillPdf(old)
                    storeBillPdf(path, job.fileName)
                }
            }
            BillPdfAutoSave.pending.remove(request)
        }
        // Never let one bill that cannot be rendered hold up the rest
        LaunchedEffect(Unit) {
            delay(60_000)
            BillPdfAutoSave.pending.remove(request)
        }
    }
}

/**
 * Copies the PDF at [pdfPath] into the bills folder as [fileName].pdf, replacing an earlier copy.
 * Returns the path shown to the user, or null when the platform cannot store it.
 */
expect fun storeBillPdf(pdfPath: String, fileName: String): String?

/** Removes the stored PDF named [fileName].pdf, if there is one. */
expect fun deleteBillPdf(fileName: String)

/** Whether the PDF named [fileName].pdf is already stored. */
expect fun billPdfExists(fileName: String): Boolean
