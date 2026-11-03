package com.elvan.udukkai.data.settings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LanguageRulesTest {
    @Test
    fun a_row_without_a_rule_is_english() {
        assertEquals("en", LanguageRules.get("{}", "customer"))
        assertEquals("en", LanguageRules.get("", "businessName"))
        assertEquals("en", LanguageRules.get("""{"langRules":{"customerName":"nonsense"}}""", "customer"))
    }

    @Test
    fun four_rows_and_only_the_business_name_prints_both_languages() {
        assertEquals(listOf("businessName", "businessAddress", "customer", "signatureName"), LanguageRules.fields)
        assertTrue(LanguageRules.isBoth("businessName"))
        assertFalse(LanguageRules.isBoth("businessAddress"))
        assertFalse(LanguageRules.isBoth("customer"))
    }

    @Test
    fun the_customer_name_and_address_are_set_together() {
        val json = LanguageRules.set("{}", "customer", "ta")
        assertTrue(json.contains(""""customerName":"ta""""), json)
        assertTrue(json.contains(""""customerAddress":"ta""""), json)
        assertEquals("ta", LanguageRules.get(json, "customer"))
        assertFalse(LanguageRules.set(json, "customer", "en").contains("langRules"))
    }

    @Test
    fun setting_tamil_keeps_every_other_print_setting() {
        val before = """{"receiptLanguage":"ta","wideLogoScale":1.5,"invoiceDisplayOptions":{"showHSN":true,"items":[1,2]}}"""
        val after = LanguageRules.set(before, "businessName", "ta")
        assertEquals("ta", LanguageRules.get(after, "businessName"))
        assertTrue(after.contains(""""receiptLanguage":"ta""""))
        assertTrue(after.contains(""""wideLogoScale":1.5"""))
        assertTrue(after.contains(""""showHSN":true"""))
        assertTrue(after.contains(""""items":[1,2]"""))
    }

    @Test
    fun rows_are_independent_and_english_leaves_nothing_behind() {
        var json = LanguageRules.set("{}", "businessAddress", "ta")
        json = LanguageRules.set(json, "customer", "ta")
        json = LanguageRules.set(json, "businessAddress", "en")
        assertEquals("en", LanguageRules.get(json, "businessAddress"))
        assertEquals("ta", LanguageRules.get(json, "customer"))
        json = LanguageRules.set(json, "customer", "en")
        assertFalse(json.contains("langRules"), "no empty rules are left behind: $json")
    }
}
