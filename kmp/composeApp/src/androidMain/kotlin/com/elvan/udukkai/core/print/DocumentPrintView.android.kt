package com.elvan.udukkai.core.print

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Base64
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.MimeTypeMap
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import org.json.JSONTokener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import com.elvan.udukkai.BuildConfig
import java.io.File

actual val isDocumentPrintSupported: Boolean = true

// The bundle in assets/print is served from this origin so the React
// stylesheet's absolute url('/fonts/...') paths resolve unchanged.
private const val PRINT_ORIGIN = "https://udukkai.print"
private const val TAG = "DocumentPrintView"

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun DocumentPrintView(
    payloadJson: String,
    fileName: String,
    controller: DocumentPrintController,
    modifier: Modifier
) {
    val bridge = remember { PrintBridge(controller) }
    bridge.payloadJson = payloadJson
    bridge.fileName = fileName

    DisposableEffect(controller) {
        controller.handler = { action -> bridge.run(action) }
        onDispose {
            controller.handler = null
            bridge.webView?.destroy()
            bridge.webView = null
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                bridge.webView = this
                if (BuildConfig.DEBUG) WebView.setWebContentsDebuggingEnabled(true)
                keepDocumentLight(this)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                settings.setSupportZoom(false)
                settings.builtInZoomControls = false
                settings.displayZoomControls = false
                isHorizontalScrollBarEnabled = false
                overScrollMode = WebView.OVER_SCROLL_NEVER
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                addJavascriptInterface(bridge, "UdukkaiNative")
                webViewClient = AssetPrintClient(context)
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

/** JS bridge: methods are called by react/print/main.tsx on a background thread. */
private class PrintBridge(private val controller: DocumentPrintController) {
    private val main = Handler(Looper.getMainLooper())
    var webView: WebView? = null
    var payloadJson: String = "{}"
    var fileName: String = "document"

    @JavascriptInterface
    fun onLoaded() = main.post {
        Log.d(TAG, "bundle loaded, rendering ${payloadJson.length} chars")
        controller.isReady = false
        webView?.evaluateJavascript("window.UdukkaiPrint.render($payloadJson)", null)
    }

    @JavascriptInterface
    fun onReady() = main.post {
        Log.d(TAG, "document ready")
        controller.markReady()
    }

    @JavascriptInterface
    fun onPdf(base64: String) {
        val bytes = Base64.decode(base64, Base64.DEFAULT)
        main.post {
            controller.isBusy = false
            sharePdf(bytes)
        }
    }

    @JavascriptInterface
    fun onError(message: String) = main.post {
        controller.isBusy = false
        Log.e(TAG, "Print bundle error: $message")
    }

    fun run(action: DocumentPrintAction) {
        val view = webView ?: return
        when (action) {
            DocumentPrintAction.Print -> {
                view.evaluateJavascript("window.UdukkaiPrint.printHtml()") { result ->
                    val html = (JSONTokener(result).nextValue() as? String).orEmpty()
                    if (html.isNotEmpty()) printHtml(view.context, html)
                }
            }
            DocumentPrintAction.Share -> {
                if (controller.isBusy) return
                controller.isBusy = true
                view.evaluateJavascript("window.UdukkaiPrint.buildPdf()", null)
            }
        }
    }

    // Mirrors React's NativeDocumentPlugin.printHtml: load the paper-only document
    // in a fresh WebView and print it with default attributes (A4 / margins come
    // from the document's @page rule).
    private var printWebView: WebView? = null

    @SuppressLint("SetJavaScriptEnabled")
    private fun printHtml(context: Context, html: String) {
        printWebView?.destroy()
        val printer = WebView(context)
        printWebView = printer
        keepDocumentLight(printer)
        printer.settings.javaScriptEnabled = true
        printer.webViewClient = object : AssetPrintClient(context) {
            override fun onPageFinished(view: WebView, url: String) {
                // Give the woff fonts a moment, then print. (View.postDelayed would never
                // run: this WebView is never attached to a window.)
                main.postDelayed({
                    val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                    printManager.print(fileName, view.createPrintDocumentAdapter(fileName), PrintAttributes.Builder().build())
                }, 300)
            }
        }
        printer.loadDataWithBaseURL("$PRINT_ORIGIN/", html, "text/html", "UTF-8", null)
    }

    private fun sharePdf(bytes: ByteArray) {
        val view = webView ?: return
        val context = view.context
        try {
            val dir = File(context.filesDir, "shared_pdfs").apply { mkdirs() }
            dir.listFiles()?.forEach { it.delete() }
            val safeName = fileName.replace(Regex("[\\\\/:*?\"<>|]"), "-")
            val file = File(dir, "$safeName.pdf")
            file.writeBytes(bytes)

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, safeName)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, safeName))
        } catch (e: Exception) {
            Log.e(TAG, "Share PDF failed: ${e.message}", e)
        }
    }
}
