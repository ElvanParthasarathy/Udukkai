package com.elvan.udukkai.core.print

// Bills are not rendered to PDF on desktop yet
actual fun storeBillPdf(pdfPath: String, fileName: String, business: String): String? = null

actual fun deleteBillPdf(fileName: String, business: String) {}

actual fun billPdfExists(fileName: String, business: String): Boolean = true

actual fun removeOldBillFolders() {}
