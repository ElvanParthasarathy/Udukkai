package com.elvan.udukkai.core.print

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.ui.components.ExpressiveDotsLoader
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONObject

private class HeaderPreviewBridge(private val onLoadedCall: () -> Unit, private val onReadyCall: () -> Unit) {
    private val main = Handler(Looper.getMainLooper())

    @JavascriptInterface fun onLoaded() { main.post(onLoadedCall) }
    @JavascriptInterface fun onReady() { main.post(onReadyCall) }
    @JavascriptInterface fun onError(message: String?) { Log.w("BillHeaderPreview", "template error: $message") }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun BillHeaderPreview(payloadJson: String, settingsJson: String, modifier: Modifier) {
    var web by remember { mutableStateOf<WebView?>(null) }
    var ready by remember { mutableStateOf(false) }

    LaunchedEffect(ready, settingsJson) {
        if (ready) web?.evaluateJavascript("UdukkaiPrint.previewSettings(${JSONObject.quote(settingsJson)})", null)
    }

    // White paper with a loader until the template has drawn; the page itself stays hidden so it never shows half-drawn
    Box(modifier.background(Color.White), contentAlignment = Alignment.Center) {
    AndroidView(
        modifier = Modifier.fillMaxSize().graphicsLayer { alpha = if (ready) 1f else 0f },
        factory = { ctx ->
            WebView(ctx).apply {
                web = this
                keepDocumentLight(this)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                setBackgroundColor(android.graphics.Color.WHITE)
                // Touches belong to the controls around it, not to the page
                setOnTouchListener { _, _ -> true }
                addJavascriptInterface(
                    HeaderPreviewBridge(
                        onLoadedCall = { evaluateJavascript("UdukkaiPrint.preview(${JSONObject.quote(payloadJson)}, 300)", null) },
                        onReadyCall = { ready = true }
                    ),
                    "UdukkaiNative"
                )
                webViewClient = AssetPrintClient(ctx)
                loadUrl("$PRINT_ORIGIN/index.html")
            }
        },
        onRelease = { it.destroy() }
    )
    if (!ready) ExpressiveDotsLoader(modifier = Modifier.padding(40.dp), color = Color(0xFF9E9E9E))
    }
}
