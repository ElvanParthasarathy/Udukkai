package com.elvan.udukkai.core.mode

import androidx.compose.runtime.Composable
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.localization.trWithLang

/**
 * The two operating modes of Udukkai.
 *
 * KOOLI (கூலி) — Service / Labor invoicing mode.
 * PATTU (பட்டு) — Product / Goods invoicing mode.
 *
 * Each mode has its own completely isolated database,
 * settings, and data. They never touch each other.
 *
 * Zero hardcoded languages: Titles are resolved dynamically
 * via the translation engine (titleKey.tr()).
 */
enum class AppMode(
    val key: String,
    val titleKey: String,
    val databaseName: String
) {
    KOOLI(
        key = "kooli",
        titleKey = K.udukkaiCoolie,
        databaseName = "udukkai_kooli.db"
    ),
    PATTU(
        key = "pattu",
        titleKey = K.udukkaiSilk,
        databaseName = "udukkai_pattu.db"
    );

    /**
     * Returns the localized display name in Compose UI.
     */
    @Composable
    fun displayName(): String = titleKey.tr()

    /**
     * Returns the localized display name for a specific language code.
     */
    fun displayName(langCode: String): String = titleKey.trWithLang(langCode)

    companion object {
        fun fromKey(key: String): AppMode =
            entries.firstOrNull { it.key == key } ?: KOOLI
    }
}
