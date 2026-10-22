package com.elvan.udukkai.factory.ui.screens.editor

import com.elvan.udukkai.data.settings.LanguageJsonConverter
import kotlin.math.floor
import kotlin.math.round

private var nextFactoryId = 0L
fun generateFactoryId(): String = "${System.currentTimeMillis()}_${++nextFactoryId}"

@Deprecated("Use generateFactoryId", ReplaceWith("generateFactoryId()"))
fun generateKooliId(): String = generateFactoryId()

/**
 * Single line item for Factory (ஆலை) mode invoices.
 * Holds product reference, weight in kg, and rate per kg.
 * Row total is truncated (floored), matching Flutter and React 1:1.
 */
data class FactoryItem(
    val id: String = generateFactoryId(),
    val productId: String? = null,
    val productName: String = "",
    val productNameEn: String = "",
    val weight: Double = 0.0,
    val price: Double = 0.0,
    val languageMap: Map<String, String> = emptyMap()
) {
    val name: String get() = productName
    val rate: Double get() = price
    val rowTotal: Int get() = floor(weight * price).toInt()


}



/**
 * Additional charge line item for Factory mode invoices (e.g. Loading, Transport).
 */
data class FactoryAdditionalCharge(
    val id: String = System.currentTimeMillis().toString() + (0..999).random(),
    val name: String = "",
    val nameSecondary: String = "",
    val amount: Double = 0.0
)



/**
 * Computed totals result for Factory mode invoices.
 */
data class FactoryTotals(
    val subtotal: Double = 0.0,
    val totalWeight: Double = 0.0,
    val grandTotal: Double = 0.0
)



/**
 * Factory invoice calculation engine matching Flutter's `KooliKanakku` 1:1.
 * 1. Row total = floor(edai * vilai) (truncated, not rounded)
 * 2. subtotal = sum(row totals)
 * 3. totalKg = sum(item.edai) + (setharamGrams / 1000.0)
 * 4. grandTotal = subtotal + courier + ahimsa + sum(other charges)
 */
object FactoryCalculation {
    fun calculate(
        items: List<FactoryItem>,
        setharamGrams: Double = 0.0,
        thabaalThogai: Double = 0.0,
        ahimsaPattuThogai: Double = 0.0,
        charges: List<FactoryAdditionalCharge> = emptyList()
    ): FactoryTotals {
        var subtotal = 0.0
        var totalKg = 0.0

        for (item in items) {
            val rowTotal = floor(item.weight * item.price)
            subtotal += rowTotal
            totalKg += item.weight
        }

        totalKg += setharamGrams / 1000.0

        var piraTotal = 0.0
        for (charge in charges) {
            piraTotal += charge.amount
        }

        val grandTotal = subtotal + thabaalThogai + ahimsaPattuThogai + piraTotal
        val roundedTotalKg = round(totalKg * 1000.0) / 1000.0

        return FactoryTotals(
            subtotal = subtotal,
            totalWeight = roundedTotalKg,
            grandTotal = grandTotal
        )
    }

    private fun escapeJson(s: String): String {
        return s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    fun itemsToJson(items: List<FactoryItem>): String {
        if (items.isEmpty()) return "[]"
        val sb = StringBuilder("[")
        items.forEachIndexed { idx, item ->
            if (idx > 0) sb.append(",")
            sb.append("{")
            val pId = item.productId
            sb.append("\"productId\":").append(if (pId != null) "\"${escapeJson(pId)}\"" else "null").append(",")
            sb.append("\"porulId\":").append(if (pId != null) "\"${escapeJson(pId)}\"" else "null").append(",")
            sb.append("\"productName\":\"${escapeJson(item.name)}\",")
            sb.append("\"porulPeyar\":\"${escapeJson(item.name)}\",")
            sb.append("\"productNameEn\":\"${escapeJson(item.productNameEn)}\",")
            sb.append("\"porulPeyarEn\":\"${escapeJson(item.productNameEn)}\",")
            sb.append("\"weight\":${item.weight},")
            sb.append("\"edai\":${item.weight},")
            sb.append("\"price\":${item.price},")
            sb.append("\"vilai\":${item.price},")
            sb.append("\"languageMap\":${LanguageJsonConverter.stringify(item.languageMap)},")
            sb.append("\"mozhiMap\":${LanguageJsonConverter.stringify(item.languageMap)}")
            sb.append("}")
        }
        sb.append("]")
        return sb.toString()
    }

    fun itemsFromJson(json: String?): List<FactoryItem> {
        if (json.isNullOrBlank() || json.trim() == "[]" || json.trim() == "null") return emptyList()
        val objects = splitJsonObjects(json)
        return objects.mapNotNull { body ->
            try {
                val pId = Regex(""""(?:productId|porulId)"\s*:\s*(?:"([^"]*)"|null)""").find(body)?.let {
                    if (it.groupValues[1].isNotEmpty()) it.groupValues[1] else null
                }
                val pName = Regex(""""(?:productName|porulPeyar)"\s*:\s*"((?:\\.|[^"\\])*)"""").find(body)?.groupValues?.get(1)
                    ?.replace("\\\"", "\"")?.replace("\\\\", "\\") ?: ""
                val pNameEn = Regex(""""(?:productNameEn|porulPeyarEn)"\s*:\s*"((?:\\.|[^"\\])*)"""").find(body)?.groupValues?.get(1)
                    ?.replace("\\\"", "\"")?.replace("\\\\", "\\") ?: ""
                val weight = Regex(""""(?:weight|edai)"\s*:\s*([0-9.]+)""").find(body)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
                val price = Regex(""""(?:price|vilai)"\s*:\s*([0-9.]+)""").find(body)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0

                val langMapMatch = Regex(""""(?:languageMap|mozhiMap)"\s*:\s*(\{[^}]*\})""").find(body)?.groupValues?.get(1)
                val langMap = LanguageJsonConverter.parse(langMapMatch)

                FactoryItem(
                    productId = pId,
                    productName = pName,
                    productNameEn = pNameEn,
                    weight = weight,
                    price = price,
                    languageMap = langMap
                )
            } catch (_: Exception) {
                null
            }
        }
    }

    fun chargesToJson(charges: List<FactoryAdditionalCharge>): String {
        if (charges.isEmpty()) return "[]"
        val sb = StringBuilder("[")
        charges.forEachIndexed { idx, charge ->
            if (idx > 0) sb.append(",")
            sb.append("{")
            sb.append("\"id\":\"${escapeJson(charge.id)}\",")
            sb.append("\"name\":\"${escapeJson(charge.name)}\",")
            sb.append("\"nameSecondary\":\"${escapeJson(charge.nameSecondary)}\",")
            sb.append("\"peyar\":\"${escapeJson(charge.name)}\",")
            sb.append("\"amount\":${charge.amount},")
            sb.append("\"thogai\":${charge.amount}")
            sb.append("}")
        }
        sb.append("]")
        return sb.toString()
    }

    fun chargesFromJson(json: String?): List<FactoryAdditionalCharge> {
        if (json.isNullOrBlank() || json.trim() == "[]" || json.trim() == "null") return emptyList()
        val objects = splitJsonObjects(json)
        return objects.mapNotNull { body ->
            try {
                val id = Regex(""""id"\s*:\s*"((?:\\.|[^"\\])*)"""").find(body)?.groupValues?.get(1)
                    ?.replace("\\\"", "\"")?.replace("\\\\", "\\") ?: ""
                val name = Regex(""""(?:name|peyar)"\s*:\s*"((?:\\.|[^"\\])*)"""").find(body)?.groupValues?.get(1)
                    ?.replace("\\\"", "\"")?.replace("\\\\", "\\") ?: ""
                val amount = Regex(""""(?:amount|thogai)"\s*:\s*([0-9.]+)""").find(body)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0

                FactoryAdditionalCharge(
                    id = id,
                    name = name,
                    amount = amount
                )
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun splitJsonObjects(jsonArray: String): List<String> {
        val result = mutableListOf<String>()
        var depth = 0
        var inString = false
        var isEscaped = false
        val current = StringBuilder()

        for (ch in jsonArray) {
            if (isEscaped) {
                if (depth > 0) current.append(ch)
                isEscaped = false
                continue
            }
            if (ch == '\\') {
                if (depth > 0) current.append(ch)
                isEscaped = true
                continue
            }
            if (ch == '"') {
                inString = !inString
                if (depth > 0) current.append(ch)
                continue
            }
            if (!inString) {
                if (ch == '{') {
                    if (depth > 0) current.append(ch)
                    depth++
                    continue
                } else if (ch == '}') {
                    depth--
                    if (depth == 0) {
                        result.add(current.toString())
                        current.clear()
                    } else {
                        current.append(ch)
                    }
                    continue
                }
            }
            if (depth > 0) {
                current.append(ch)
            }
        }
        return result
    }
}
