package com.elvan.udukkai.data.settings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LanguageRulesTest {
    @Test
    fun a_field_without_a_rule_is_auto() {
        assertEquals("auto", LanguageRules.get("{}", "customerName"))
        assertEquals("auto", LanguageRules.get("", "customerName"))
        assertEquals("auto", LanguageRules.get("""{"langRules":{"customerName":"nonsense"}}""", "customerName"))
    }

    @Test
    fun setting_a_rule_keeps_every_other_print_setting() {
        val before = """{"receiptLanguage":"ta","wideLogoScale":1.5,"invoiceDisplayOptions":{"showHSN":true,"items":[1,2]}}"""
        val after = LanguageRules.set(before, "businessName", "sec")
        assertEquals("sec", LanguageRules.get(after, "businessName"))
        assertTrue(after.contains(""""receiptLanguage":"ta""""))
        assertTrue(after.contains(""""wideLogoScale":1.5"""))
        assertTrue(after.contains(""""showHSN":true"""))
        assertTrue(after.contains(""""items":[1,2]"""))
    }

    @Test
    fun rules_are_set_and_cleared_one_field_at_a_time() {
        var json = LanguageRules.set("{}", "customerName", "pri")
        json = LanguageRules.set(json, "itemName", "both-rev")
        assertEquals("pri", LanguageRules.get(json, "customerName"))
        assertEquals("both-rev", LanguageRules.get(json, "itemName"))
        json = LanguageRules.set(json, "customerName", "auto")
        assertEquals("auto", LanguageRules.get(json, "customerName"))
        assertEquals("both-rev", LanguageRules.get(json, "itemName"))
        json = LanguageRules.set(json, "itemName", "auto")
        assertFalse(json.contains("langRules"), "no empty rules are left behind: $json")
    }
}
