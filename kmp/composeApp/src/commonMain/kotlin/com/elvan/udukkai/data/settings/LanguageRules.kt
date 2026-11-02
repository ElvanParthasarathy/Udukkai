package com.elvan.udukkai.data.settings

import com.elvan.udukkai.core.utils.MiniJson

/**
 * On a bilingual bill three fields print in ONE language: the business address, the customer name and the customer
 * address. English unless this setting says Tamil. Kept in the business's print settings ([BusinessProfile.printSettingsJson],
 * key "langRules") and read by the bill papers. The business name and the item names are not set here: the business name
 * is English with the Tamil name under it, and an item name follows the main language. A single-language bill ignores this.
 */
object LanguageRules {
    /** The fields that can be set, in the order the settings page lists them. */
    val fields = listOf("businessAddress", "customerName", "customerAddress")

    /** The language a field prints in: "en" (the default) or "ta". */
    val modes = listOf("en", "ta")

    fun get(printSettingsJson: String, field: String): String {
        val rules = MiniJson.parseObject(printSettingsJson)["langRules"] as? Map<*, *> ?: return "en"
        return (rules[field] as? String)?.takeIf { it in modes } ?: "en"
    }

    /** The settings JSON with this field's language set ("en" removes the rule). Every other setting is kept as it was. */
    fun set(printSettingsJson: String, field: String, mode: String): String {
        val all = MiniJson.parseObject(printSettingsJson).toMutableMap()
        val rules = (all["langRules"] as? Map<*, *>).orEmpty().entries.associate { it.key.toString() to it.value }.toMutableMap()
        if (mode == "en") rules.remove(field) else rules[field] = mode
        if (rules.isEmpty()) all.remove("langRules") else all["langRules"] = rules
        return MiniJson.stringify(all)
    }
}
