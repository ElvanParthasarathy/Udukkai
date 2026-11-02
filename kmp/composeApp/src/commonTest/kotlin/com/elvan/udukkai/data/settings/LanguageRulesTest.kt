package com.elvan.udukkai.data.settings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LanguageRulesTest {
    @Test
    fun a_field_without_a_rule_prints_english() {
        assertEquals("en", LanguageRules.get("{}", "customerName"))
        assertEquals("en", LanguageRules.get("", "customerName"))
        assertEquals("en", LanguageRules.get("""{"langRules":{"customerName":"nonsense"}}""", "customerName"))
    }

    @Test
    fun only_the_three_one_language_fields_can_be_set() {
        assertEquals(listOf("businessAddress", "customerName", "customerAddress"), LanguageRules.fields)
        assertEquals(listOf("en", "ta"), LanguageRules.modes)
    }

    @Test
    fun setting_tamil_keeps_every_other_print_setting() {
        val before = """{"receiptLanguage":"ta","wideLogoScale":1.5,"invoiceDisplayOptions":{"showHSN":true,"items":[1,2]}}"""
        val after = LanguageRules.set(before, "customerName", "ta")
        assertEquals("ta", LanguageRules.get(after, "customerName"))
        assertTrue(after.contains(""""receiptLanguage":"ta""""))
        assertTrue(after.contains(""""wideLogoScale":1.5"""))
        assertTrue(after.contains(""""showHSN":true"""))
        assertTrue(after.contains(""""items":[1,2]"""))
    }

    @Test
    fun english_clears_the_rule_and_leaves_nothing_behind() {
        var json = LanguageRules.set("{}", "customerName", "ta")
        json = LanguageRules.set(json, "customerAddress", "ta")
        json = LanguageRules.set(json, "customerName", "en")
        assertEquals("en", LanguageRules.get(json, "customerName"))
        assertEquals("ta", LanguageRules.get(json, "customerAddress"))
        json = LanguageRules.set(json, "customerAddress", "en")
        assertFalse(json.contains("langRules"), "no empty rules are left behind: $json")
    }
}
