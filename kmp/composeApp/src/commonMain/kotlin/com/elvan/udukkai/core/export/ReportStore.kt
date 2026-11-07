package com.elvan.udukkai.core.export

/** A report file that was saved: where it is (for opening it) and a readable path (for telling the user). */
class SavedReport(val uri: String, val fileName: String, val folder: String, val mimeType: String)

/**
 * Saves a report next to the bill PDFs: Documents/Udukkai/<Udukkai Textiles|Udukkai Factory>/<business>/Reports.
 * One file per report and period: saving again replaces it, so the latest figures are there and no copies pile up. Null when it could not be saved.
 */
expect fun saveReport(baseName: String, extension: String, mimeType: String, bytes: ByteArray, businessFolder: String): SavedReport?

/** Opens a saved report in the app the phone uses for that file type; false when there is none. */
expect fun openSavedReport(report: SavedReport): Boolean
