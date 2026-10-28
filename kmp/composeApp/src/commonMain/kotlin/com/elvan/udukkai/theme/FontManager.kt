package com.elvan.udukkai.theme

import androidx.compose.ui.text.font.FontFamily

/** The app has one font: Elvan Sans. */
enum class AppFont(val id: String, val displayName: String) {
    ELVAN_SANS(id = "elvan_sans", displayName = "Elvan Sans");

    companion object {
        val DEFAULT = ELVAN_SANS
    }
}

object FontManager {
    val currentFont: AppFont get() = AppFont.DEFAULT

    val currentFontFamily: FontFamily get() = ElvanSansFontFamily

    /** Kept so the start-up code does not change; there is no font choice to load any more. */
    fun init() {}
}
