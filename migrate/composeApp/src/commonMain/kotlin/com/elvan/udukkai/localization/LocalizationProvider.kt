package com.elvan.udukkai.localization

import androidx.compose.runtime.*
import com.elvan.udukkai.core.navil.NavilMozhimaatri
import com.elvan.udukkai.core.platform.getPreferencesHelper
import com.elvan.udukkai.core.platform.getSystemLanguageCode
import com.elvan.udukkai.localization.language_keys.en
import com.elvan.udukkai.localization.language_keys.ta
import com.elvan.udukkai.localization.language_keys.taLatn

/**
 * Supported UI Language codes: System, Tamil, English, and Tamil Latin.
 */
enum class Language(val code: String, val displayName: String) {
    SYSTEM("system", "தானியங்கி அமைப்பு"),
    TAMIL("ta", "தமிழ்"),
    ENGLISH("en", "English"),
    TAMIL_LATIN("ta-Latn", "Tamil Latin");

    companion object {
        fun fromCode(code: String): Language =
            entries.firstOrNull { it.code == code } ?: SYSTEM
    }
}

/**
 * CompositionLocal holding the currently active UI language code.
 */
val LocalAppLanguage = compositionLocalOf { Language.TAMIL.code }

/**
 * UI Language state manager with disk persistence.
 */
object LanguageManager {
    private const val PREF_KEY_LANGUAGE = "app_locale"

    var currentLanguage by mutableStateOf(Language.SYSTEM)
        private set

    /**
     * Resolves the actual language code currently in use for UI rendering:
     * - "ta"
     * - "en"
     * - "ta-Latn"
     * When currentLanguage is SYSTEM, dynamically resolves to the device's system locale.
     */
    val activeLanguageCode: String
        get() = when (currentLanguage) {
            Language.SYSTEM -> getSystemLanguageCode()
            Language.TAMIL -> "ta"
            Language.ENGLISH -> "en"
            Language.TAMIL_LATIN -> "ta-Latn"
        }

    fun init() {
        try {
            val saved = getPreferencesHelper().getString(PREF_KEY_LANGUAGE, null)
            currentLanguage = if (saved.isNullOrEmpty() || saved == "system") {
                Language.SYSTEM
            } else {
                Language.fromCode(saved)
            }
        } catch (_: Exception) {
            currentLanguage = Language.SYSTEM
        }
    }

    fun setLanguage(language: Language) {
        currentLanguage = language
        try {
            val value = if (language == Language.SYSTEM) "system" else language.code
            getPreferencesHelper().setString(PREF_KEY_LANGUAGE, value)
        } catch (_: Exception) {}
    }

    fun setLanguageByCode(code: String) {
        setLanguage(Language.fromCode(code))
    }

    fun toggleLanguage() {
        val next = if (currentLanguage == Language.TAMIL) Language.ENGLISH else Language.TAMIL
        setLanguage(next)
    }
}

/**
 * Provider composable wrapping the tree with the selected UI language.
 */
@Composable
fun ProvideAppLanguage(
    language: Language = LanguageManager.currentLanguage,
    content: @Composable () -> Unit
) {
    val activeCode = LanguageManager.activeLanguageCode
    CompositionLocalProvider(LocalAppLanguage provides activeCode) {
        content()
    }
}

/**
 * Extension on String for reactive translations in Compose UI.
 * Usage:
 *   Text(K.invoice.tr())
 */
@Composable
fun String.tr(): String {
    val currentLang = LocalAppLanguage.current
    return trWithLang(currentLang)
}

/**
 * Translates this string key explicitly into the requested language code.
 */
fun String.trWithLang(langCode: String): String {
    val effectiveCode = if (langCode == "system") getSystemLanguageCode() else langCode
    return when (effectiveCode) {
        "ta" -> ta[this] ?: this
        "en" -> en[this] ?: this
        "ta-Latn" -> taLatn[this] ?: ta[this]?.let { NavilMozhimaatri.transliterate(it) } ?: NavilMozhimaatri.transliterate(this)
        else -> en[this] ?: ta[this] ?: this
    }
}
