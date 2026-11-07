package com.elvan.udukkai.core.export

actual fun saveReport(baseName: String, extension: String, mimeType: String, bytes: ByteArray, businessFolder: String): SavedReport? = try {
    // Desktop (not shipped yet): Downloads/Udukkai/<business>/Reports
    val dir = java.io.File(System.getProperty("user.home"), "Downloads/Udukkai/$businessFolder/Reports").apply { mkdirs() }
    val name = "$baseName.$extension"
    val file = java.io.File(dir, name).apply { writeBytes(bytes) }
    SavedReport(file.toURI().toString(), name, dir.path, mimeType)
} catch (_: Exception) {
    null
}

actual fun openSavedReport(report: SavedReport): Boolean = try {
    java.awt.Desktop.getDesktop().open(java.io.File(java.net.URI(report.uri)))
    true
} catch (_: Exception) {
    false
}
