package com.elvan.udukkai.data.settings

import com.elvan.udukkai.core.utils.NumberToWords
import kotlin.test.*

class DataLanguageDisplayTest {
    private val values = mapOf("ta" to "தமிழ்", "en" to "English", "fr" to "Français", "hi" to "हिन्दी")

    @Test fun allLanguageOrdersAndModesPreserveStoredTranslations() {
        for ((primary, secondary) in listOf("ta" to "en", "en" to "ta", "fr" to "hi", "hi" to "fr")) {
            for (bilingual in listOf(false, true, false, true)) {
                val display = DataLanguageDisplay(BusinessProfile(primaryLanguage = primary, secondaryLanguage = secondary, isBilingual = bilingual))
                assertEquals(values.getValue(primary), display.first(values))
                assertEquals(if (bilingual) values.getValue(secondary) else "", display.second(values))
                assertEquals(if (bilingual) listOf(values.getValue(primary), values.getValue(secondary)) else listOf(values.getValue(primary)), display.lines(values).map { it.first })
            }
        }
        assertEquals(4, values.size)
    }

    @Test fun missingPrimaryNeverLeaksHiddenSecondary() {
        val display = DataLanguageDisplay(BusinessProfile(primaryLanguage = "fr", secondaryLanguage = "en", isBilingual = false))
        assertEquals("", display.first(mapOf("en" to "Hidden translation")))
        assertEquals(emptyList(), display.lines(mapOf("en" to "Hidden translation")))
    }

    @Test fun duplicateLanguagesAndValuesAreNotRenderedTwice() {
        val sameLanguage = DataLanguageDisplay(BusinessProfile(primaryLanguage = "fr", secondaryLanguage = "fr", isBilingual = true))
        assertEquals(listOf("fr"), sameLanguage.languages)
        assertEquals("", sameLanguage.second(values))
        val sameValue = DataLanguageDisplay(BusinessProfile(primaryLanguage = "fr", secondaryLanguage = "hi", isBilingual = true))
        assertEquals(listOf("Same" to true), sameValue.lines(mapOf("fr" to "Same", "hi" to "Same")))
    }

    @Test fun amountWordsUseRegisteredLanguageAndNeverSubstituteAnother() {
        assertEquals(NumberToWords.toTamil(123.0), NumberToWords.forLanguage(123.0, "ta"))
        assertEquals(NumberToWords.toEnglish(123.0), NumberToWords.forLanguage(123.0, "en-IN"))
        assertEquals("", NumberToWords.forLanguage(123.0, "unsupported"))
    }
}
