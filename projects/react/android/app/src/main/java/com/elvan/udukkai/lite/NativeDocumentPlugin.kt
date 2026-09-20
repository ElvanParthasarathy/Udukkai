package com.elvan.udukkai.lite

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.provider.MediaStore
import android.util.Base64
import androidx.core.content.FileProvider
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

@CapacitorPlugin(name = "NativeDocument")
class NativeDocumentPlugin : Plugin() {

    @PluginMethod
    fun sharePdf(call: PluginCall) {
        val base64Data = call.getString("base64Data")
        val filename = call.getString("filename", "document.pdf")

        if (base64Data == null || filename == null) {
            call.reject("Missing base64Data or filename")
            return
        }

        try {
            val pdfBytes = Base64.decode(base64Data, Base64.DEFAULT)
            val cacheDir = context.cacheDir
            val file = File(cacheDir, filename)
            
            FileOutputStream(file).use { it.write(pdfBytes) }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                context.packageName + ".fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share PDF")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

            call.resolve()
        } catch (e: Exception) {
            e.printStackTrace()
            call.reject("Failed to share PDF: ${e.message}")
        }
    }

    @PluginMethod
    fun downloadPdf(call: PluginCall) {
        val base64Data = call.getString("base64Data")
        val filename = call.getString("filename", "document.pdf")
        val appMode = call.getString("appMode", "Udukkai Silk")
        val category = call.getString("category", "Invoice")

        if (base64Data == null || filename == null) {
            call.reject("Missing base64Data or filename")
            return
        }

        try {
            val pdfBytes = Base64.decode(base64Data, Base64.DEFAULT)
            val relativePath = Environment.DIRECTORY_DOCUMENTS + "/Udukkai/" + appMode + "/" + category

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                }

                val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { it.write(pdfBytes) }
                    call.resolve()
                } else {
                    call.reject("Failed to create file in MediaStore")
                }
            } else {
                val docsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                val targetDir = File(docsDir, "Udukkai/$appMode/$category")
                if (!targetDir.exists()) {
                    targetDir.mkdirs()
                }
                val file = File(targetDir, filename)
                FileOutputStream(file).use { it.write(pdfBytes) }
                call.resolve()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            call.reject("Failed to download PDF: ${e.message}")
        }
    }

    @PluginMethod
    fun printPdf(call: PluginCall) {
        val base64Data = call.getString("base64Data")
        val filename = call.getString("filename", "document.pdf")

        if (base64Data == null || filename == null) {
            call.reject("Missing base64Data or filename")
            return
        }

        try {
            val pdfBytes = Base64.decode(base64Data, Base64.DEFAULT)
            val cacheDir = context.cacheDir
            val file = File(cacheDir, filename)
            FileOutputStream(file).use { it.write(pdfBytes) }

            val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
            val jobName = filename
            
            val printAdapter = object : PrintDocumentAdapter() {
                override fun onLayout(
                    oldAttributes: PrintAttributes?,
                    newAttributes: PrintAttributes?,
                    cancellationSignal: CancellationSignal?,
                    callback: LayoutResultCallback?,
                    extras: Bundle?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback?.onLayoutCancelled()
                        return
                    }
                    val info = PrintDocumentInfo.Builder(filename)
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                        .build()
                    callback?.onLayoutFinished(info, true)
                }

                override fun onWrite(
                    pages: Array<out PageRange>?,
                    destination: ParcelFileDescriptor?,
                    cancellationSignal: CancellationSignal?,
                    callback: WriteResultCallback?
                ) {
                    try {
                        FileInputStream(file).use { inStream ->
                            FileOutputStream(destination?.fileDescriptor).use { outStream ->
                                val buffer = ByteArray(8192)
                                var length: Int
                                while (inStream.read(buffer).also { length = it } > 0) {
                                    if (cancellationSignal?.isCanceled == true) {
                                        callback?.onWriteCancelled()
                                        return
                                    }
                                    outStream.write(buffer, 0, length)
                                }
                            }
                        }
                        callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        callback?.onWriteFailed(e.message)
                    }
                }
            }

            printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
            call.resolve()
        } catch (e: Exception) {
            e.printStackTrace()
            call.reject("Failed to print PDF: ${e.message}")
        }
    }
    @PluginMethod
    fun printHtml(call: PluginCall) {
        val htmlContent = call.getString("html")
        val filename = call.getString("filename", "document") ?: "document"
        val baseUrl = call.getString("baseUrl")

        if (htmlContent == null) {
            call.reject("Missing html content")
            return
        }

        activity?.runOnUiThread {
            val webView = android.webkit.WebView(context)
            webView.settings.javaScriptEnabled = true
            webView.settings.defaultTextEncodingName = "utf-8"
            webView.settings.allowFileAccess = true
            webView.settings.allowContentAccess = true
            webView.settings.allowFileAccessFromFileURLs = true
            webView.settings.allowUniversalAccessFromFileURLs = true
            
            webView.webViewClient = object : android.webkit.WebViewClient() {
                override fun onPageFinished(view: android.webkit.WebView, url: String) {
                    val printManager = context.getSystemService(Context.PRINT_SERVICE) as android.print.PrintManager
                    val printAdapter = view.createPrintDocumentAdapter(filename)
                    printManager.print(filename, printAdapter, android.print.PrintAttributes.Builder().build())
                    call.resolve()
                }
            }
            webView.loadDataWithBaseURL(baseUrl, htmlContent, "text/HTML", "UTF-8", null)
        }
    }
}
