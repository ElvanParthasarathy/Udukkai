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
import com.elvan.udukkai.data.settings.BusinessProfile
import com.elvan.udukkai.data.model.ReceiptModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Folder name of a business: its English name, else the first name it has, made safe for a file
 * system. Every business gets its own folder with its Invoice and Receipt folders.
 */
fun billBusinessFolder(profile: BusinessProfile?): String {
    val names = profile?.businessName.orEmpty()
    val name = names["en"]?.takeIf { it.isNotBlank() } ?: names.values.firstOrNull { it.isNotBlank() }.orEmpty()
    return name.replace(Regex("[\\\\/:*?\"<>|]"), "-").trim().trim('.').ifEmpty { "Business" }
}

/** PDF file name of an invoice (same names the React app used). */
fun invoicePdfName(invoice: InvoiceModel): String =
    (if (invoice.invoiceType == "proforma") "EST_" else "INV_") + invoice.invoiceNumber.replace('/', '-')

/** PDF file name of a receipt (same names the React app used). */
fun receiptPdfName(receipt: ReceiptModel): String =
    "RCPT_" + receipt.receiptNumber.replace('/', '-')

/**
 * Every bill saved in an editor gets its PDF written to the documents folder, where the React app
 * kept them: Documents/Udukkai/<Udukkai Textiles|Udukkai Factory>/<business>/<Invoice|Receipt>. Saving the
 * bill again replaces that file; when the bill number or its business changed, the file under the old
 * name is removed, so there are no duplicates.
 */
object BillPdfAutoSave {
    enum class Kind { Invoice, Receipt }

    /** [replaces] and [replacesCompanyId] are the bill's file name and business before this save (null for a new bill). */
    data class Request(val kind: Kind, val id: Long, val replaces: String?, val replacesCompanyId: Long?, val seq: Long)

    internal val pending = mutableStateListOf<Request>()
    private var nextSeq = 0L

    /**
     * Queues the PDF of every bill that has none yet, so all bills of the app end up in the folders,
     * not only the ones saved from now on. Bills that already have their file are skipped.
     */
    suspend fun syncMissing(invoices: List<InvoiceModel>, receipts: List<ReceiptModel>, businessOf: (Long?) -> String) {
        if (!isDocumentPrintSupported) return
        val missing = withContext(Dispatchers.IO) {
            // Copies in the earlier folder layout are dropped; their bills are written again below
            removeOldBillFolders()
            invoices.filter { !it.isDeleted && it.id > 0L && !billPdfExists(invoicePdfName(it), businessOf(it.companyId)) }
                .map { Request(Kind.Invoice, it.id, null, null, 0L) } +
                receipts.filter { !it.isDeleted && it.id > 0L && !billPdfExists(receiptPdfName(it), businessOf(it.companyId)) }
                    .map { Request(Kind.Receipt, it.id, null, null, 0L) }
        }
        missing.forEach { request(it.kind, it.id) }
    }

    fun request(kind: Kind, id: Long, replaces: String? = null, replacesCompanyId: Long? = null) {
        if (!isDocumentPrintSupported || id <= 0L) return
        pending.removeAll { it.kind == kind && it.id == id }
        pending.add(Request(kind, id, replaces, replacesCompanyId, nextSeq++))
    }
}

/** What the host needs to render one bill. */
class BillPdfJob(val payloadJson: String, val fileName: String, val business: String)

/**
 * Renders the queued bills one at a time with a hidden print view and stores each PDF.
 * Hosted on the home screen, so it keeps going after the editor has closed.
 */
@Composable
fun BillPdfAutoSaveHost(businessOf: (Long?) -> String, build: (BillPdfAutoSave.Request) -> BillPdfJob?) {
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
                    val oldBusiness = businessOf(request.replacesCompanyId)
                    if (old != null && (old != job.fileName || oldBusiness != job.business)) deleteBillPdf(old, oldBusiness)
                    storeBillPdf(path, job.fileName, job.business)
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
expect fun storeBillPdf(pdfPath: String, fileName: String, business: String): String?

/** Removes the stored PDF named [fileName].pdf of [business], if there is one. */
expect fun deleteBillPdf(fileName: String, business: String)

/** Whether the PDF named [fileName].pdf of [business] is already stored. */
expect fun billPdfExists(fileName: String, business: String): Boolean

/** Removes this app's bill PDFs from the earlier top-level folders (Documents/Udukkai Textiles|Factory). */
expect fun removeOldBillFolders()
