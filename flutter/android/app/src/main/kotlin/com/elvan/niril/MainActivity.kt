package com.elvan.niril

import io.flutter.embedding.android.FlutterActivity

class MainActivity : FlutterActivity() {
    private val PRINT_CHANNEL = "com.elvan.niril/print"

    override fun configureFlutterEngine(flutterEngine: io.flutter.embedding.engine.FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        
        io.flutter.plugin.common.MethodChannel(flutterEngine.dartExecutor.binaryMessenger, PRINT_CHANNEL).setMethodCallHandler { call, result ->
            if (call.method == "printReceipt") {
                val receiptJson = call.argument<String>("receiptJson")
                val profileJson = call.argument<String>("profileJson")
                val isDark = call.argument<Boolean>("isDark") ?: false
                val receiptType = call.argument<String>("receiptType") ?: "GST"

                val intent = android.content.Intent(this, CleanReceiptActivity::class.java).apply {
                    putExtra("receiptJson", receiptJson)
                    putExtra("profileJson", profileJson)
                    putExtra("isDark", isDark)
                    putExtra("receiptType", receiptType)
                }
                startActivity(intent)
                result.success(null)
            } else if (call.method == "printInvoice") {
                val invoiceJson = call.argument<String>("invoiceJson")
                val profileJson = call.argument<String>("profileJson")
                val isDark = call.argument<Boolean>("isDark") ?: false
                val invoiceType = call.argument<String>("invoiceType") ?: "GST"

                val intent = android.content.Intent(this, CleanInvoiceActivity::class.java).apply {
                    putExtra("invoiceJson", invoiceJson)
                    putExtra("profileJson", profileJson)
                    putExtra("isDark", isDark)
                    putExtra("invoiceType", invoiceType)
                }
                startActivity(intent)
                result.success(null)
            } else {
                result.notImplemented()
            }
        }
    }
}
