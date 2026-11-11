package com.elvan.udukkai.core.data

import kotlin.test.Test
import kotlin.test.assertEquals

class LocationUiNameTest {
    @Test fun knownStateIdentitySuppliesMissingSelectedTranslation() {
        val stored = mapOf("ta" to "Tamil Nadu")
        assertEquals("தமிழ்நாடு", locationDataName(stored, indianStates, "ta"))
        assertEquals("Tamil Nadu", locationDataName(stored, indianStates, "en"))
        assertEquals(mapOf("ta" to "Tamil Nadu"), stored)
    }
    @Test fun missingFreeFormTranslationDoesNotExposeHiddenLanguage() {
        assertEquals("", locationDataName(mapOf("ta" to "Private free-form place"), indianStates, "en"))
    }
    @Test fun additionalLanguagesUseCatalogDataWithoutLanguageBranches() {
        val names = listOf(LocationName("Example", "எடுத்துக்காட்டு", mapOf("fr" to "Exemple", "hi" to "उदाहरण")))
        assertEquals("Exemple", locationUiName("Example", names, "fr"))
        assertEquals("उदाहरण", locationUiName("Exemple", names, "hi"))
    }
    @Test fun englishUiResolvesStoredTamilState() {
        assertEquals("Tamil Nadu", locationUiName("தமிழ்நாடு", indianStates, "en"))
    }
    @Test fun tamilUiResolvesStoredEnglishState() {
        assertEquals("தமிழ்நாடு", locationUiName("Tamil Nadu", indianStates, "ta"))
    }
    @Test fun freeFormAddressesStayUnchanged() {
        assertEquals("My district", locationUiName("My district", tamilNaduDistricts, "en"))
    }
    @Test fun districtNamesFollowUiLanguage() {
        val district = tamilNaduDistricts.first()
        assertEquals(district.en, locationUiName(district.ta, tamilNaduDistricts, "en"))
    }
}
