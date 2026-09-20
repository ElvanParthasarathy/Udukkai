package com.elvan.udukkai.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontFamily
import com.elvan.udukkai.core.platform.getPreferencesHelper
import com.elvan.udukkai.localization.K

enum class AppFont(
    val id: String,
    val displayName: String,
    val titleKey: String,
    val descKey: String
) {
    NAVIL_SANS(
        id = "navil_sans",
        displayName = "Navil Sans",
        titleKey = K.navilSans,
        descKey = K.navilSansDesc
    ),
    ELVAN_SANS(
        id = "elvan_sans",
        displayName = "Elvan Sans",
        titleKey = K.elvanSans,
        descKey = K.elvanSansDesc
    );

    companion object {
        val DEFAULT = NAVIL_SANS

        fun fromId(id: String?): AppFont =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: DEFAULT
    }
}

object FontManager {
    private const val PREF_KEY_FONT = "app_font_family"

    var currentFont by mutableStateOf(AppFont.DEFAULT)
        private set

    val currentFontFamily: FontFamily
        get() = when (currentFont) {
            AppFont.NAVIL_SANS -> NavilSansFontFamily
            AppFont.ELVAN_SANS -> ElvanSansFontFamily
        }

    fun init() {
        try {
            val saved = getPreferencesHelper().getString(PREF_KEY_FONT, null)
            currentFont = AppFont.fromId(saved)
        } catch (_: Exception) {
            currentFont = AppFont.DEFAULT
        }
    }

    fun setFont(font: AppFont) {
        currentFont = font
        try {
            getPreferencesHelper().setString(PREF_KEY_FONT, font.id)
        } catch (_: Exception) {}
    }

    fun toggleFont(): AppFont {
        val next = if (currentFont == AppFont.NAVIL_SANS) AppFont.ELVAN_SANS else AppFont.NAVIL_SANS
        setFont(next)
        return next
    }
}
