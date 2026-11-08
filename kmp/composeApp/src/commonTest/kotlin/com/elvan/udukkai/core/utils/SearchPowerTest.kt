package com.elvan.udukkai.core.utils

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SearchPowerTest {
    private fun m(vararg targets: String, q: String) = SearchHelper.matchesRecord(targets.toList(), q)

    @Test
    fun billNumbersInAnyShape() {
        for (q in listOf("SJPS-001", "sjps001", "sjps 001", "001", "sjps1", "sjps-1", "sjps 1", "SJPS/001")) assertTrue(m("SJPS-001", q = q), q)
    }

    @Test
    fun leadingZerosAreNotAWildcard() {
        assertFalse(m("SJPS-010", q = "001"))
        assertFalse(m("SJPS-001", q = "sjps2"))
        assertFalse(m("SJPS-100", q = "sjps01"))
    }

    @Test
    fun receiptNumbers() {
        for (q in listOf("rcp/sjps/02", "rcpsjps02", "rcp sjps 2", "sjps2")) assertTrue(m("RCP/SJPS/02", q = q), q)
    }

    @Test
    fun tamilTextByTamilAndByEnglishLetters() {
        val name = "\u0B9A\u0BBF\u0BB5\u0BB0\u0BBE\u0BAE\u0BCD" // சிவராம்
        assertTrue(m(name, q = "\u0B9A\u0BBF\u0BB5"), "tamil prefix")
        assertTrue(m(name, q = "sivaram"), "english letters: " + SearchHelper.latinOf(name))
        assertFalse(m(name, q = "kumar"))
    }
}
