package com.elvan.niril

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.Alignment
import android.print.PrintManager
import android.print.PrintAttributes
import android.content.Context
import org.json.JSONObject

class CleanInvoiceActivity : ComponentActivity() {
    private lateinit var webView: WebView

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val invoiceJson = intent.getStringExtra("invoiceJson") ?: "{}"
        val profileJson = intent.getStringExtra("profileJson") ?: "{}"
        val intentIsDark = intent.getBooleanExtra("isDark", false)
        val invoiceType = intent.getStringExtra("invoiceType") ?: "GST"
        
        var invoiceNo = "Invoice"
        try {
            val obj = JSONObject(invoiceJson)
            if (obj.has("bill_no")) {
                invoiceNo = "#" + obj.getString("bill_no")
            } else if (obj.has("invoiceNo")) {
                invoiceNo = "#" + obj.getString("invoiceNo")
            } else if (obj.has("patrucheettuEn")) {
                invoiceNo = "#" + obj.getString("patrucheettuEn")
            }
        } catch (e: Exception) {}

        var currentInvoiceJson = invoiceJson
        
        setContent {
            val isDark = isSystemInDarkTheme() || intentIsDark
            var showSettings by remember { mutableStateOf(false) }
            
            // State for options
            var hideBankDetails by remember { mutableStateOf(false) }
            var hideIfsc by remember { mutableStateOf(false) }
            var hideLogo by remember { mutableStateOf(false) }
            var hideDigitalSignature by remember { mutableStateOf(false) }
            var showItemizedTax by remember { mutableStateOf(false) }
            var showGSTIN by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                try {
                    val obj = JSONObject(currentInvoiceJson)
                    if (obj.has("sonthaViruppangal") && obj.getString("sonthaViruppangal").isNotEmpty()) {
                        val opts = JSONObject(obj.getString("sonthaViruppangal"))
                        hideBankDetails = opts.optBoolean("hideBankDetails", false)
                        hideIfsc = opts.optBoolean("hideIfsc", false)
                        hideLogo = opts.optBoolean("hideLogo", false)
                        hideDigitalSignature = opts.optBoolean("hideDigitalSignature", false)
                        showItemizedTax = opts.optBoolean("showItemizedTax", false)
                        showGSTIN = opts.optBoolean("showGSTIN", true)
                    }
                } catch (e: Exception) {}
            }

            fun updateInvoice() {
                try {
                    val obj = JSONObject(currentInvoiceJson)
                    val opts = JSONObject()
                    opts.put("hideBankDetails", hideBankDetails)
                    opts.put("hideIfsc", hideIfsc)
                    opts.put("hideLogo", hideLogo)
                    opts.put("hideDigitalSignature", hideDigitalSignature)
                    opts.put("showItemizedTax", showItemizedTax)
                    opts.put("showGSTIN", showGSTIN)
                    
                    obj.put("sonthaViruppangal", opts.toString())
                    
                    // Also update coolie fields just in case
                    obj.put("show_bank_details", !hideBankDetails)
                    obj.put("show_ifsc", !hideIfsc)
                    
                    currentInvoiceJson = obj.toString()
                    webView.reload()
                } catch (e: Exception) {}
            }

            MaterialTheme(
                colorScheme = if (isDark) darkColorScheme(background = androidx.compose.ui.graphics.Color.Black) else lightColorScheme(background = androidx.compose.ui.graphics.Color.White)
            ) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(invoiceNo, style = MaterialTheme.typography.titleLarge) },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            },
                            actions = {
                                IconButton(onClick = { showSettings = true }) {
                                    Icon(Icons.Filled.Settings, contentDescription = "Settings")
                                }
                                IconButton(onClick = { 
                                    printWebView(invoiceNo) 
                                }) {
                                    Text("🖨️")
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                titleContentColor = MaterialTheme.colorScheme.onSurface,
                            )
                        )
                    }
                ) { innerPadding ->
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                webView = this
                                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    allowFileAccess = true
                                    allowFileAccessFromFileURLs = true
                                    allowUniversalAccessFromFileURLs = true
                                    
                                    // Enable Native Android Zoom
                                    setSupportZoom(true)
                                    builtInZoomControls = true
                                    displayZoomControls = false
                                    useWideViewPort = true
                                    loadWithOverviewMode = true
                                }
                                
                                addJavascriptInterface(object : Any() {
                                    @JavascriptInterface
                                    fun getInvoiceData(): String = currentInvoiceJson
                        
                                    @JavascriptInterface
                                    fun getProfileData(): String = profileJson
                                    
                                    @JavascriptInterface
                                    fun isDarkMode(): Boolean = isDark
                                    
                                    @JavascriptInterface
                                    fun isNativeApp(): Boolean = true

                                    @JavascriptInterface
                                    fun getInvoiceType(): String = invoiceType
                                    
                                    @JavascriptInterface
                                    fun closeInvoice() {
                                        finish()
                                    }
                                    
                                    @JavascriptInterface
                                    fun printInvoice() {
                                        runOnUiThread {
                                            printWebView(invoiceNo)
                                        }
                                    }
                                }, "FlutterBridge")
                        
                                webViewClient = WebViewClient()
                                loadUrl("file:///android_asset/react_app/pattiyal.html")
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
                
                if (showSettings) {
                    ModalBottomSheet(onDismissRequest = { showSettings = false }) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text("அமைப்புகள் (Settings)", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Text("வங்கித் தரவுகளை மறைக்க", modifier = Modifier.weight(1f))
                                Switch(checked = hideBankDetails, onCheckedChange = { hideBankDetails = it; updateInvoice() })
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Text("IFSC மறைக்க", modifier = Modifier.weight(1f))
                                Switch(checked = hideIfsc, onCheckedChange = { hideIfsc = it; updateInvoice() })
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Text("ஓவுரு மறைக்க (Hide Logo)", modifier = Modifier.weight(1f))
                                Switch(checked = hideLogo, onCheckedChange = { hideLogo = it; updateInvoice() })
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Text("கையொப்பம் மறைக்க", modifier = Modifier.weight(1f))
                                Switch(checked = hideDigitalSignature, onCheckedChange = { hideDigitalSignature = it; updateInvoice() })
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Text("பொருட்களின் வரியைக் காட்டுக", modifier = Modifier.weight(1f))
                                Switch(checked = showItemizedTax, onCheckedChange = { showItemizedTax = it; updateInvoice() })
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Text("GSTIN காட்டுக", modifier = Modifier.weight(1f))
                                Switch(checked = showGSTIN, onCheckedChange = { showGSTIN = it; updateInvoice() })
                            }
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }

    private fun printWebView(receiptNo: String) {
        val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
        val printAdapter = webView.createPrintDocumentAdapter("Receipt_$receiptNo")
        val jobName = "Receipt_$receiptNo"
        
        printManager.print(
            jobName,
            printAdapter,
            PrintAttributes.Builder().build()
        )
    }
}
