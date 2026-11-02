package com.elvan.udukkai.data.settings

import com.elvan.udukkai.core.utils.MiniJson

/**
 * Which language each field of a bilingual bill prints in, and in which order. Kept in the business's print settings
 * ([BusinessProfile.printSettingsJson], key "langRules") and read by the bill templates. A field without a rule keeps the
 * bill's built-in layout ("auto"). A single-language bill ignores every rule.
 */
object LanguageRules {
    /** The fields a rule can be set for, in the order the settings page lists them. */
    val fields = listOf("businessName", "businessAddress", "customerName", "customerAddress", "itemName")

    /** auto: the built-in layout. both: main language, then the second. both-rev: second, then main. pri / sec: that language only. */
    val modes = listOf("auto", "both", "both-rev", "pri", "sec")

    fun get(printSettingsJson: String, field: String): String {
        val rules = MiniJson.parseObject(printSettingsJson)["langRules"] as? Map<*, *> ?: return "auto"
        return (rules[field] as? String)?.takeIf { it in modes } ?: "auto"
    }

    /** The settings JSON with this field's rule set ("auto" removes it). Every other setting is kept as it was. */
    fun set(printSettingsJson: String, field: String, mode: String): String {
        val all = MiniJson.parseObject(printSettingsJson).toMutableMap()
        val rules = (all["langRules"] as? Map<*, *>).orEmpty().entries.associate { it.key.toString() to it.value }.toMutableMap()
        if (mode == "auto") rules.remove(field) else rules[field] = mode
        if (rules.isEmpty()) all.remove("langRules") else all["langRules"] = rules
        return MiniJson.stringify(all)
    }
}
