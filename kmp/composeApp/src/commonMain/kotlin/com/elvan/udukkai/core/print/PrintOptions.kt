package com.elvan.udukkai.core.print

import com.elvan.udukkai.core.platform.getPreferencesHelper

enum class PaperSize(val key: String, val label: String, val widthMm: Double, val heightMm: Double) {
    A4("A4", "A4", 210.0, 297.0),
    A5("A5", "A5", 148.0, 210.0),
    LETTER("Letter", "Letter", 215.9, 279.4),
    LEGAL("Legal", "Legal", 215.9, 355.6)
}

enum class PageOrientation(val key: String) { PORTRAIT("portrait"), LANDSCAPE("landscape") }

enum class PrintMargin(val mm: Int) { NONE(0), NARROW(5), NORMAL(10) }

/** WIDTH: scale the sheet to the page width (continues on more pages when taller). PAGE: whole sheet on one page. */
enum class PrintFit(val key: String) { WIDTH("width"), PAGE("page") }

/**
 * How a document is laid out when it is printed, saved or shared as a PDF.
 * The defaults reproduce the plain A4 sheet. The last choices are remembered per app.
 */
data class PrintOptions(
    val paper: PaperSize = PaperSize.A4,
    val orientation: PageOrientation = PageOrientation.PORTRAIT,
    val margin: PrintMargin = PrintMargin.NONE,
    val color: Boolean = true,
    val fit: PrintFit = PrintFit.WIDTH
) {
    /** The options as the JSON the print page expects. */
    fun toJson(): String =
        "{\"paper\":\"${paper.key}\",\"orientation\":\"${orientation.key}\",\"marginMm\":${margin.mm}," +
            "\"color\":$color,\"fit\":\"${fit.key}\"}"

    fun save() {
        try {
            getPreferencesHelper().apply {
                setString(KEY_PAPER, paper.name)
                setString(KEY_ORIENTATION, orientation.name)
                setString(KEY_MARGIN, margin.name)
                setBoolean(KEY_COLOR, color)
                setString(KEY_FIT, fit.name)
            }
        } catch (_: Exception) {
        }
    }

    companion object {
        private const val KEY_PAPER = "print_paper"
        private const val KEY_ORIENTATION = "print_orientation"
        private const val KEY_MARGIN = "print_margin"
        private const val KEY_COLOR = "print_color"
        private const val KEY_FIT = "print_fit"

        fun load(): PrintOptions = try {
            val prefs = getPreferencesHelper()
            PrintOptions(
                paper = prefs.getString(KEY_PAPER)?.let { n -> PaperSize.entries.find { it.name == n } } ?: PaperSize.A4,
                orientation = prefs.getString(KEY_ORIENTATION)?.let { n -> PageOrientation.entries.find { it.name == n } }
                    ?: PageOrientation.PORTRAIT,
                margin = prefs.getString(KEY_MARGIN)?.let { n -> PrintMargin.entries.find { it.name == n } } ?: PrintMargin.NONE,
                color = prefs.getBoolean(KEY_COLOR, true),
                fit = prefs.getString(KEY_FIT)?.let { n -> PrintFit.entries.find { it.name == n } } ?: PrintFit.WIDTH
            )
        } catch (_: Exception) {
            PrintOptions()
        }
    }
}
