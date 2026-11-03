package com.elvan.udukkai.data.settings

import com.elvan.udukkai.core.utils.MiniJson

/**
 * How a bilingual bill picks its languages, set per business. Kept in the business's print settings
 * ([BusinessProfile.printSettingsJson], key "langRules") and read by the bill papers. A single-language bill ignores it.
 *  - "businessName": both languages, English first with the Tamil name under it ("en"), or Tamil first ("ta")
 *  - "businessAddress": ONE language, English ("en") or Tamil ("ta")
 *  - "customer": ONE language for the customer's name and address together, English ("en") or Tamil ("ta")
 *  - "signatureName": ONE language for the signatory's name under the signature, English ("en") or Tamil ("ta")
 * An item name is not set here: it follows the main language.
 */
object LanguageRules {
    /** The rows of the settings page, in order. */
    val fields = listOf("businessName", "businessAddress", "customer", "signatureName")

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

/**
 * The signatory's name in both languages, kept in the business's print settings (key "signatoryNames": {"ta": ..., "en": ...}).
 * A business that saved one name before this keeps it: it shows in the language box it is written in.
 */
object SignatoryNames {
    private fun hasTamil(s: String) = s.any { it in '\u0B80'..'\u0BFF' }

    /** (Tamil name, English name). */
    fun get(profile: BusinessProfile): Pair<String, String> {
        val saved = MiniJson.parseObject(profile.printSettingsJson)["signatoryNames"] as? Map<*, *>
        val ta = saved?.get("ta") as? String ?: ""
        val en = saved?.get("en") as? String ?: ""
        if (ta.isNotBlank() || en.isNotBlank()) return ta to en
        val old = profile.fullSignatoryName
        return if (hasTamil(old)) old to "" else "" to old
    }

    /** The settings JSON with both names set; every other setting is kept as it was. */
    fun set(printSettingsJson: String, ta: String, en: String): String {
        val all = MiniJson.parseObject(printSettingsJson).toMutableMap()
        all["signatoryNames"] = mapOf("ta" to ta.trim(), "en" to en.trim())
        return MiniJson.stringify(all)
    }
}

/** A number kept in the business's print settings (the logo sizes). 1.0, the size the bill always used, is not stored. */
object PrintNumbers {
    fun get(printSettingsJson: String, key: String, default: Double): Double =
        (MiniJson.parseObject(printSettingsJson)[key] as? Number)?.toDouble() ?: default

    fun set(printSettingsJson: String, key: String, value: Double): String {
        val all = MiniJson.parseObject(printSettingsJson).toMutableMap()
        if (kotlin.math.abs(value - 1.0) < 0.005) all.remove(key) else all[key] = value
        return MiniJson.stringify(all)
    }
}
