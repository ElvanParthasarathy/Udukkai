package com.elvan.udukkai.core.print

// Bills are not rendered to PDF on desktop yet
actual fun storeBillPdf(pdfPath: String, fileName: String): String? = null

actual fun deleteBillPdf(fileName: String) {}

actual fun billPdfExists(fileName: String): Boolean = true
