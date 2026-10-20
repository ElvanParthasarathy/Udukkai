package com.elvan.udukkai.localization

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.elvan.udukkai.factory.data.FactoryProfileRepository
import com.elvan.udukkai.textiles.data.TextilesProfileRepository

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
 * 3. App Domain Isolation (Factory vs Textiles):
 *    - Factory (ஆலை) has its own independent billing language.
 *    - Textiles (சவுளி) has its own independent billing language,
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

    fun getFactoryConfig(): BillingLanguageConfig {
        val profile = FactoryProfileRepository.profile
        val isBilingual = profile.iruMozhi
        val primary = BillingLanguage.fromCode(profile.mudhanMozhi.ifEmpty { "ta" })
        val secondary = BillingLanguage.fromCode(profile.thunaiMozhi.ifEmpty { "en" })
        return BillingLanguageConfig(
            primaryLanguage = primary,
            secondaryLanguage = secondary,
            isBilingual = isBilingual
        )
    }

    fun getTextilesConfig(): BillingLanguageConfig {
        val profile = TextilesProfileRepository.profile
        val isBilingual = profile.iruMozhi
        val primary = BillingLanguage.fromCode(profile.mudhanMozhi.ifEmpty { "ta" })
        val secondary = BillingLanguage.fromCode(profile.thunaiMozhi.ifEmpty { "en" })
        return BillingLanguageConfig(
            primaryLanguage = primary,
            secondaryLanguage = secondary,
            isBilingual = isBilingual
        )
    }

    /**
     * Returns the active billing language config for the specified app scope ("factory" or "textiles").
     */
    fun getConfig(scope: String = "factory"): BillingLanguageConfig {
        return if (scope.equals("textiles", ignoreCase = true) || scope.equals("pattu", ignoreCase = true)) {
            getTextilesConfig()
        } else {
            getFactoryConfig()
        }
    }

    /**
     * Updates the primary billing language for an app scope on its active profile.
     */
    fun setPrimaryLanguage(scope: String = "factory", language: BillingLanguage) {
        if (scope.equals("textiles", ignoreCase = true) || scope.equals("pattu", ignoreCase = true)) {
            val profile = TextilesProfileRepository.profile
            TextilesProfileRepository.updateProfile(profile.copy(mudhanMozhi = language.code))
        } else {
            val profile = FactoryProfileRepository.profile
            FactoryProfileRepository.updateProfile(profile.copy(mudhanMozhi = language.code))
        }
    }

    /**
     * Updates the secondary billing language (used for bilingual bills).
     */
    fun setSecondaryLanguage(scope: String = "factory", language: BillingLanguage) {
        if (scope.equals("textiles", ignoreCase = true) || scope.equals("pattu", ignoreCase = true)) {
            val profile = TextilesProfileRepository.profile
            TextilesProfileRepository.updateProfile(profile.copy(thunaiMozhi = language.code))
        } else {
            val profile = FactoryProfileRepository.profile
            FactoryProfileRepository.updateProfile(profile.copy(thunaiMozhi = language.code))
        }
    }

    /**
     * Toggles bilingual billing mode (e.g. Tamil heading + English subheading on bills).
     */
    fun setBilingual(scope: String = "factory", enabled: Boolean) {
        if (scope.equals("textiles", ignoreCase = true) || scope.equals("pattu", ignoreCase = true)) {
            val profile = TextilesProfileRepository.profile
            TextilesProfileRepository.updateProfile(profile.copy(iruMozhi = enabled))
        } else {
            val profile = FactoryProfileRepository.profile
            FactoryProfileRepository.updateProfile(profile.copy(iruMozhi = enabled))
        }
    }

    /**
     * Translates a string key strictly for PRINT / BILLING output.
     * Uses the scope's configured print language, completely immune
     * to whatever UI language the user has chosen for the app screens.
     */
    fun printTr(key: String, scope: String = "factory"): String {
        val config = getConfig(scope)
        return key.trWithLang(config.primaryLanguage.code)
    }

    /**
     * Translates a string key for BILINGUAL print output.
     * Returns Pair(primaryTranslation, secondaryTranslationOrNull).
     */
    fun printBilingual(key: String, scope: String = "factory"): Pair<String, String?> {
        val config = getConfig(scope)
        val primary = key.trWithLang(config.primaryLanguage.code)
        val secondary = if (config.isBilingual) {
            key.trWithLang(config.secondaryLanguage.code)
        } else null
        return Pair(primary, secondary)
    }
}
