package com.elvan.udukkai.core.mode

import androidx.compose.runtime.Composable
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.localization.trWithLang

/**
 * The two operating modes of Udukkai:
 *
 * FACTORY (ஆலை) — Service / Labor invoicing mode.
 * TEXTILES (சவுளி) — Product / Goods invoicing mode.
 *
 * Each mode has its own completely isolated database,
 * settings, and data. They never touch each other.
 */
enum class AppMode(
    val key: String,
    val titleKey: String,
    val databaseName: String
) {
    FACTORY(
        key = "factory",
        titleKey = K.udukkaiFactory,
        databaseName = "udukkai_factory.db"
    ),
    TEXTILES(
        key = "textiles",
        titleKey = K.udukkaiTextiles,
        databaseName = "udukkai_textiles.db"
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
        // Backward-compatible aliases for existing code
        val KOOLI get() = FACTORY
        val PATTU get() = TEXTILES

        fun fromKey(key: String): AppMode =
            when (key.lowercase()) {
                "factory", "kooli" -> FACTORY
                "textiles", "pattu", "silk" -> TEXTILES
                else -> FACTORY
            }
    }
}
