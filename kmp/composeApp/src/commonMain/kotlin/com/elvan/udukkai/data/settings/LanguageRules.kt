package com.elvan.udukkai.data.settings

import com.elvan.udukkai.core.utils.MiniJson

/**
 * How a bilingual bill picks its languages, set per business. Kept in the business's print settings
 * ([BusinessProfile.printSettingsJson], key "langRules") and read by the bill papers. A single-language bill ignores it.
 *  - "businessName": both languages, English first with the Tamil name under it ("en"), or Tamil first ("ta")
 *  - "businessAddress": ONE language, English ("en") or Tamil ("ta")
 *  - "customer": ONE language for the customer's name and address together, English ("en") or Tamil ("ta")
 * An item name is not set here: it follows the main language.
 */
object LanguageRules {
    /** The rows of the settings page, in order. */
    val fields = listOf("businessName", "businessAddress", "customer")

    /** The business name prints both languages (the choice is the order); the others print one language. */
    fun isBoth(field: String) = field == "businessName"

    /** "en" (the default) or "ta": which language comes first (business name) or is printed (the others). */
    val modes = listOf("en", "ta")

    /** The keys the bill papers read: the customer's name and address are set together. */
    private fun keys(field: String) = if (field == "customer") listOf("customerName", "customerAddress") else listOf(field)

    fun get(printSettingsJson: String, field: String): String {
        val rules = MiniJson.parseObject(printSettingsJson)["langRules"] as? Map<*, *> ?: return "en"
        return (rules[keys(field).first()] as? String)?.takeIf { it in modes } ?: "en"
    }

    /** The settings JSON with this row set ("en" removes the rule). Every other setting is kept as it was. */
    fun set(printSettingsJson: String, field: String, mode: String): String {
        val all = MiniJson.parseObject(printSettingsJson).toMutableMap()
        val rules = (all["langRules"] as? Map<*, *>).orEmpty().entries.associate { it.key.toString() to it.value }.toMutableMap()
        keys(field).forEach { if (mode == "en") rules.remove(it) else rules[it] = mode }
        if (rules.isEmpty()) all.remove("langRules") else all["langRules"] = rules
        return MiniJson.stringify(all)
    }
}
