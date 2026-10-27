package com.elvan.udukkai.core.print

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PdfExporter
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.MimeTypeMap
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import com.elvan.udukkai.BuildConfig
import org.json.JSONTokener
import java.io.File
import java.security.MessageDigest

actual val isDocumentPrintSupported: Boolean = true

// The bundle in assets/print is served from this origin so the React
// stylesheet's absolute url('/fonts/...') paths resolve unchanged.
private const val PRINT_ORIGIN = "https://udukkai.print"
private const val TAG = "DocumentPrintView"
private const val CACHE_DIR = "print_pdfs"
private const val CACHE_LIMIT = 50

/**
 * Provides the document as a PDF. A PDF already cached for this exact document + options opens
 * at once and the React page is never started; only on a cache miss is a hidden WebView started
 * to render the React template once, export it as a vector-text PDF and cache it.
 * Everything shown / printed / saved / shared is that PDF.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun DocumentPrintView(
    payloadJson: String,
    fileName: String,
    controller: DocumentPrintController,
    modifier: Modifier,
    topInset: Dp
) {
    val context = LocalContext.current
    val bridge = remember { PrintBridge(controller, context.applicationContext) }
    var webNeeded by remember { mutableStateOf(false) }
    bridge.topInsetDp = topInset.value
    bridge.payloadJson = payloadJson
    bridge.fileName = fileName
    bridge.requestWeb = { webNeeded = true }

    DisposableEffect(controller) {
        controller.handler = { action -> bridge.run(action) }
        onDispose {
            controller.handler = null
            bridge.release()
        }
    }

    // New document or new options: use the cached PDF, or make one
    LaunchedEffect(payloadJson, controller.options) { bridge.ensure() }

    if (webNeeded) {
        AndroidView(
            modifier = modifier,
            factory = { ctx ->
                WebView(ctx).apply {
                    bridge.webView = this
                    if (BuildConfig.DEBUG) WebView.setWebContentsDebuggingEnabled(true)
                    keepDocumentLight(this)
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = false
                    settings.allowContentAccess = false
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    addJavascriptInterface(bridge, "UdukkaiNative")
                    webViewClient = AssetPrintClient(ctx)
                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                            Log.d(TAG, "console ${message.messageLevel()}: ${message.message()} @${message.sourceId()}:${message.lineNumber()}")
                            return true
                        }
                    }
                    loadUrl("$PRINT_ORIGIN/index.html")
                }
            }
        )
    }
}

/**
 * The paper must never be recoloured: Samsung / Android dark mode otherwise
 * darkens WebView content (white paper turns black). React's paper is always white.
 */
@Suppress("DEPRECATION")
private fun keepDocumentLight(view: WebView) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        view.isForceDarkAllowed = false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            view.settings.forceDark = WebSettings.FORCE_DARK_OFF
        }
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        view.settings.isAlgorithmicDarkeningAllowed = false
    }
}

private open class AssetPrintClient(private val context: Context) : WebViewClient() {
    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        val url = request.url
        if ("${url.scheme}://${url.host}" != PRINT_ORIGIN) return null
        val path = url.path?.trimStart('/').orEmpty().ifEmpty { "index.html" }
        return try {
            val ext = path.substringAfterLast('.', "")
            val mime = when (ext) {
                "js" -> "text/javascript"
                "css" -> "text/css"
                "woff" -> "font/woff"
                "html" -> "text/html"
                else -> MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "application/octet-stream"
            }
            WebResourceResponse(mime, "utf-8", context.assets.open("print/$path")).apply {
                responseHeaders = mapOf("Access-Control-Allow-Origin" to "*")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Missing print asset: $path")
            WebResourceResponse("text/plain", "utf-8", 404, "Not Found", emptyMap(), null)
        }
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean = true
}

private fun cacheDir(context: Context): File = File(context.cacheDir, CACHE_DIR).apply { mkdirs() }

/** Identifies one PDF: the document data (incl. paid amounts, business + print settings), the options and the app build. */
private fun cacheKey(context: Context, payloadJson: String, options: PrintOptions): String {
    val build = try {
        context.packageManager.getPackageInfo(context.packageName, 0).lastUpdateTime
    } catch (e: Exception) {
        0L
    }
    // Item ids are made from the clock each time a document is loaded; they do not affect the print
    val stable = payloadJson.replace(Regex("\"id\":\"[0-9]{10,}_[0-9]+\""), "\"id\":\"\"")
    val digest = MessageDigest.getInstance("SHA-256")
        .digest("$stable|${options.toJson()}|$build|v1".toByteArray())
    return digest.joinToString("") { "%02x".format(it) }.take(32)
}

private fun pruneCache(context: Context) {
    val files = cacheDir(context).listFiles()?.sortedByDescending { it.lastModified() } ?: return
    files.drop(CACHE_LIMIT).forEach { it.delete() }
}

/** Stacks the PDF's pages (up to 4) into one tall image for the zoom viewer. Null when it cannot be read. */
private fun renderPdfSheet(file: File): Pair<Bitmap, Int>? {
    return try {
        ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
            PdfRenderer(pfd).use { renderer ->
                val count = renderer.pageCount
                if (count < 1) return null
                val shown = minOf(count, 4)
                val gap = 24
                val targetW = 2400f
                val heights = (0 until shown).map { i ->
                    renderer.openPage(i).use { it.height * targetW / it.width }
                }
                var total = heights.sum() + gap * (shown - 1)
                val factor = if (total > 7000f) 7000f / total else 1f
                total *= factor
                val width = (targetW * factor).toInt().coerceAtLeast(1)
                val out = Bitmap.createBitmap(width, total.toInt().coerceAtLeast(1), Bitmap.Config.ARGB_8888)
                val canvas = Canvas(out)
                var y = 0f
                for (i in 0 until shown) {
                    val h = (heights[i] * factor).toInt().coerceAtLeast(1)
                    val page = Bitmap.createBitmap(width, h, Bitmap.Config.ARGB_8888)
                    page.eraseColor(android.graphics.Color.WHITE)
                    renderer.openPage(i).use { it.render(page, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY) }
                    canvas.drawBitmap(page, 0f, y, null)
                    page.recycle()
                    y += h + gap * factor
                }
                out to count
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Rendering PDF sheet failed: ${e.message}", e)
        null
    }
}

/** JS bridge: methods are called by react/print/main.tsx on a background thread. */
private class PrintBridge(
    private val controller: DocumentPrintController,
    private val context: Context
) {
    private val main = Handler(Looper.getMainLooper())
    var webView: WebView? = null
    var payloadJson: String = "{}"
    var fileName: String = "document"
    var topInsetDp: Float = 0f
    var requestWeb: () -> Unit = {}

    private var released = false
    private var webStarted = false
    private var loaded = false
    private var renderedPayload: String? = null
    private var exporting = false
    private var exportKey = ""
    private var exportWebView: WebView? = null

    private var wantKey = ""
    private var currentKey = ""
    private var currentFile: File? = null
    private var currentPages = 1
    private var pendingAction: DocumentPrintAction? = null
    private var action: DocumentPrintAction = DocumentPrintAction.Print

    fun release() {
        released = true
        webView?.destroy()
        webView = null
        exportWebView?.destroy()
        exportWebView = null
    }

    // ---- getting the PDF ---------------------------------------------------------------------

    /** Makes the PDF for the current document + options available (from cache when possible). */
    fun ensure() {
        if (released) return
        val key = cacheKey(context, payloadJson, controller.options)
        wantKey = key
        val file = File(cacheDir(context), "$key.pdf")
        if (file.exists() && file.length() > 0) {
            publish(key, file)
            return
        }
        if (key == currentKey) return
        when {
            !webStarted -> {
                webStarted = true
                requestWeb()
            }
            loaded && renderedPayload == payloadJson -> exportToCache()
            loaded -> renderNow()
            // else: onLoaded / onReady will carry on
        }
    }

    private fun renderNow() {
        val view = webView ?: return
        renderedPayload = payloadJson
        view.evaluateJavascript("window.UdukkaiPrint.setTopInset($topInsetDp)", null)
        view.evaluateJavascript("window.UdukkaiPrint.render($payloadJson)", null)
    }

    @JavascriptInterface
    fun onLoaded() = main.post {
        if (released) return@post
        Log.d(TAG, "bundle loaded, rendering ${payloadJson.length} chars")
        loaded = true
        renderNow()
    }

    @JavascriptInterface
    fun onReady() = main.post {
        if (released) return@post
        Log.d(TAG, "document ready")
        exportToCache()
    }

    // Renders the React document as a vector-text PDF into the cache
    private fun exportToCache() {
        if (exporting || released) return
        val view = webView ?: return
        val options = controller.options
        val key = cacheKey(context, payloadJson, options)
        wantKey = key
        if (key == currentKey) return
        exporting = true
        exportKey = key
        view.evaluateJavascript("window.UdukkaiPrint.printHtml(${options.toJson()})") { result ->
            val html = (JSONTokener(result).nextValue() as? String).orEmpty()
            if (html.isEmpty()) fallbackImagePdf(options) else exportVector(html, options, key)
        }
    }

    // Loads the paper-only document in an off-screen WebView and runs it through Chromium's
    // print pipeline, which keeps the text as real (selectable, copyable) PDF text.
    @SuppressLint("SetJavaScriptEnabled")
    private fun exportVector(html: String, options: PrintOptions, key: String) {
        exportWebView?.destroy()
        val printer = WebView(context)
        exportWebView = printer
        keepDocumentLight(printer)
        printer.settings.javaScriptEnabled = true
        printer.webViewClient = object : AssetPrintClient(context) {
            override fun onPageFinished(view: WebView, url: String) {
                // Let the fonts settle. (View.postDelayed would never run: this WebView is
                // never attached to a window.)
                main.postDelayed({
                    val tmp = File(cacheDir(context), "$key.tmp")
                    PdfExporter.export(view, "document", printAttributes(options), tmp) { pages ->
                        main.post {
                            exportWebView?.destroy()
                            exportWebView = null
                            if (released) {
                                tmp.delete()
                            } else if (pages < 1) {
                                tmp.delete()
                                fallbackImagePdf(options)
                            } else {
                                val file = File(cacheDir(context), "$key.pdf")
                                file.delete()
                                tmp.renameTo(file)
                                exporting = false
                                if (key == wantKey) publish(key, file) else ensure()
                            }
                        }
                    }
                }, 400)
            }
        }
        printer.loadDataWithBaseURL("$PRINT_ORIGIN/", html, "text/html", "UTF-8", null)
    }

    // Image-based PDF built by the page itself; used only when the vector export is not possible
    private fun fallbackImagePdf(options: PrintOptions) {
        val view = webView ?: return failExport()
        view.evaluateJavascript("window.UdukkaiPrint.buildPdf(${options.toJson()})", null)
    }

    @JavascriptInterface
    fun onPdf(base64: String, pages: Int) {
        val bytes = Base64.decode(base64, Base64.DEFAULT)
        main.post {
            if (released) return@post
            val key = exportKey
            val file = File(cacheDir(context), "$key.pdf")
            try {
                file.writeBytes(bytes)
            } catch (e: Exception) {
                return@post failExport()
            }
            exporting = false
            if (key == wantKey) publish(key, file) else ensure()
        }
    }

    private fun failExport() {
        exporting = false
        controller.isBusy = false
        pendingAction = null
        controller.notice = PrintNotice(PrintNoticeKind.Failed)
    }

    // The PDF for [key] is ready: show it and run a waiting action
    private fun publish(key: String, file: File) {
        if (currentKey == key && currentFile == file) {
            runPending()
            return
        }
        Thread {
            val rendered = renderPdfSheet(file)
            main.post {
                if (released) return@post
                if (rendered == null) {
                    file.delete()
                    failExport()
                    return@post
                }
                currentKey = key
                currentFile = file
                currentPages = rendered.second
                controller.sheet = rendered.first.asImageBitmap()
                controller.markReady()
                pruneCache(context)
                runPending()
            }
        }.start()
    }

    private fun runPending() {
        val next = pendingAction ?: return
        if (currentKey != wantKey) return
        pendingAction = null
        run(next)
    }

    @JavascriptInterface
    fun onZoom(scale: Double) = main.post { controller.zoomScale = scale.toFloat() }

    @JavascriptInterface
    fun onError(message: String) = main.post {
        Log.e(TAG, "Print bundle error: $message")
        failExport()
    }

    // ---- print / save / share ----------------------------------------------------------------

    fun run(requested: DocumentPrintAction) {
        val file = currentFile
        if (file == null || currentKey != wantKey) {
            // The PDF for the chosen options is still being made: continue once it is ready
            pendingAction = requested
            controller.isBusy = true
            controller.notice = null
            ensure()
            return
        }
        if (delivering) return
        controller.isBusy = true
        controller.notice = null
        action = requested
        deliver(file, currentPages)
    }

    private var delivering = false

    private fun safeName(): String =
        controller.fileName.ifBlank { fileName }
            .replace(Regex("[\\\\/:*?\"<>|]"), "-").trim().ifEmpty { "document" }

    private fun printAttributes(o: PrintOptions): PrintAttributes {
        val size = when (o.paper) {
            PaperSize.A4 -> PrintAttributes.MediaSize.ISO_A4
            PaperSize.A5 -> PrintAttributes.MediaSize.ISO_A5
            PaperSize.LETTER -> PrintAttributes.MediaSize.NA_LETTER
            PaperSize.LEGAL -> PrintAttributes.MediaSize.NA_LEGAL
        }
        return PrintAttributes.Builder()
            .setMediaSize(if (o.orientation == PageOrientation.LANDSCAPE) size.asLandscape() else size)
            .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .setColorMode(if (o.color) PrintAttributes.COLOR_MODE_COLOR else PrintAttributes.COLOR_MODE_MONOCHROME)
            .build()
    }

    private fun deliver(file: File, pages: Int) {
        delivering = true
        try {
            val bytes = file.readBytes()
            when (action) {
                DocumentPrintAction.Print -> printPdf(bytes, pages)
                DocumentPrintAction.Share -> sharePdf(bytes)
                DocumentPrintAction.Save -> savePdf(bytes)
            }
            controller.isBusy = false
        } catch (e: Exception) {
            Log.e(TAG, "Delivering PDF failed: ${e.message}", e)
            controller.isBusy = false
            controller.notice = PrintNotice(PrintNoticeKind.Failed)
        }
        delivering = false
    }

    // Hands the finished PDF to Android's print framework: the system dialog (and any installed
    // print service / "Save as PDF") opens with the chosen paper, orientation and colour.
    private fun printPdf(bytes: ByteArray, pages: Int) {
        val name = safeName()
        val adapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                old: PrintAttributes?, new: PrintAttributes, cancel: CancellationSignal?,
                callback: LayoutResultCallback, extras: Bundle?
            ) {
                if (cancel?.isCanceled == true) return callback.onLayoutCancelled()
                callback.onLayoutFinished(
                    PrintDocumentInfo.Builder("$name.pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(pages)
                        .build(),
                    old != new
                )
            }

            override fun onWrite(
                ranges: Array<out PageRange>, destination: ParcelFileDescriptor,
                cancel: CancellationSignal?, callback: WriteResultCallback
            ) {
                try {
                    ParcelFileDescriptor.AutoCloseOutputStream(destination).use { it.write(bytes) }
                    callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback.onWriteFailed(e.message)
                }
            }
        }
        val manager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        manager.print(name, adapter, printAttributes(controller.options))
    }

    private fun sharePdf(bytes: ByteArray) {
        val dir = File(context.filesDir, "shared_pdfs").apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }
        val name = safeName()
        val file = File(dir, "$name.pdf")
        file.writeBytes(bytes)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, name)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, name).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    // Saves to Documents/Udukkai (visible in Files). Before Android 10 that needs a storage
    // permission the app does not ask for, so the share sheet is used instead.
    private fun savePdf(bytes: ByteArray) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return sharePdf(bytes)
        val name = safeName()
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$name.pdf")
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOCUMENTS}/Udukkai")
        }
        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), values)
            ?: throw IllegalStateException("MediaStore insert failed")
        resolver.openOutputStream(uri)?.use { it.write(bytes) } ?: throw IllegalStateException("No output stream")
        controller.notice = PrintNotice(PrintNoticeKind.Saved, "Documents/Udukkai/$name.pdf")
    }
}
