package com.elvan.udukkai.localization

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.data.settings.NiruvanaTharavugalRepository

/**
 * ══════════════════════════════════════════════════════════════════════
 * THE BRICK WALL: UI Language vs Print / Billing Language
 * ══════════════════════════════════════════════════════════════════════
 *
 * 1. UI Language (LanguageManager / Cheyali Mozhi):
 *    - Governs the screen UI (buttons, dialogs, form labels).
 *
 * 2. Print / Billing Language (PrintLanguageManager / Pattiyal Mozhi):
 *    - Governs printed bills, thermal receipts, and PDF invoices.
 *    - Changing UI language NEVER affects what prints on the bill!
 *    - Changing Print language NEVER affects what appears on screen!
 *
 * 3. Mode Isolation (Kooli vs Pattu):
 *    - Kooli (Labor/Service) has its own independent billing language.
 *    - Pattu (Silk/Goods) has its own independent billing language,
 *      supporting bilingual (இருமொழி) printing (Primary + Secondary).
 */

enum class BillingLanguage(val code: String, val displayName: String) {
    TAMIL("ta", "தமிழ் (Tamil)"),
    ENGLISH("en", "English");

    companion object {
        fun fromCode(code: String): BillingLanguage =
            entries.firstOrNull { it.code == code } ?: TAMIL
    }
}

data class BillingLanguageConfig(
    val primaryLanguage: BillingLanguage = BillingLanguage.TAMIL,
    val secondaryLanguage: BillingLanguage = BillingLanguage.ENGLISH,
    val isBilingual: Boolean = false
)

object PrintLanguageManager {

    /**
     * Returns the active billing language config for the specified mode.
     * Reactively reads from NiruvanaTharavugalRepository.
     */
    fun getConfig(mode: AppMode): BillingLanguageConfig {
        val profile = NiruvanaTharavugalRepository.getProfile(mode)
        val isBilingual = if (mode == AppMode.KOOLI) true else profile.iruMozhi
        val primary = BillingLanguage.fromCode(profile.mudhanMozhi.ifEmpty { "ta" })
        val secondary = BillingLanguage.fromCode(profile.thunaiMozhi.ifEmpty { "en" })
        return BillingLanguageConfig(
            primaryLanguage = primary,
            secondaryLanguage = secondary,
            isBilingual = isBilingual
        )
    }

    /**
     * Updates the primary billing language for a mode on its active profile.
     */
    fun setPrimaryLanguage(mode: AppMode, language: BillingLanguage) {
        val profile = NiruvanaTharavugalRepository.getProfile(mode)
        NiruvanaTharavugalRepository.updateProfile(mode, profile.copy(mudhanMozhi = language.code))
    }

    /**
     * Updates the secondary billing language (used for bilingual bills).
     */
    fun setSecondaryLanguage(mode: AppMode, language: BillingLanguage) {
        val profile = NiruvanaTharavugalRepository.getProfile(mode)
        NiruvanaTharavugalRepository.updateProfile(mode, profile.copy(thunaiMozhi = language.code))
    }

    /**
     * Toggles bilingual billing mode (e.g. Tamil heading + English subheading on bills).
     */
    fun setBilingual(mode: AppMode, enabled: Boolean) {
        val profile = NiruvanaTharavugalRepository.getProfile(mode)
        NiruvanaTharavugalRepository.updateProfile(mode, profile.copy(iruMozhi = enabled))
    }

    /**
     * Translates a string key strictly for PRINT / BILLING output.
     * Uses the mode's configured print language, completely immune
     * to whatever UI language the user has chosen for the app screens.
     */
    fun printTr(key: String, mode: AppMode): String {
        val config = getConfig(mode)
        return key.trWithLang(config.primaryLanguage.code)
    }

    /**
     * Translates a string key for BILINGUAL print output.
     * Returns Pair(primaryTranslation, secondaryTranslationOrNull).
     */
    fun printBilingual(key: String, mode: AppMode): Pair<String, String?> {
        val config = getConfig(mode)
        val primary = key.trWithLang(config.primaryLanguage.code)
        val secondary = if (config.isBilingual) {
            key.trWithLang(config.secondaryLanguage.code)
        } else null
        return Pair(primary, secondary)
    }
}
