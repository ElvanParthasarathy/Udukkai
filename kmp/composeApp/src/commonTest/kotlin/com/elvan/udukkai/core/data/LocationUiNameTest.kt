package com.elvan.udukkai.core.data

import kotlin.test.Test
import kotlin.test.assertEquals

class LocationUiNameTest {
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
