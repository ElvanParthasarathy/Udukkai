package com.elvan.udukkai.ui.screens.editor.invoice.components

import com.elvan.udukkai.data.settings.MozhiJsonConverter
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
    val porulId: String? = null,
    val porulPeyar: String = "",
    val porulPeyarEn: String = "",
    val edai: Double = 0.0,
    val vilai: Double = 0.0,
    val mozhiMap: Map<String, String> = emptyMap()
) {
    val weight: Double get() = edai
    val rate: Double get() = vilai
    val rowTotal: Int get() = floor(edai * vilai).toInt()
    val varisaiThogai: Int get() = rowTotal
}

typealias KooliUrupadi = FactoryItem

/**
 * Additional charge line item for Factory mode invoices (e.g. Loading, Transport).
 */
data class FactoryAdditionalCharge(
    val id: String = System.currentTimeMillis().toString() + (0..999).random(),
    val peyar: String = "",
    val thogai: Double = 0.0
) {
    val name: String get() = peyar
    val amount: Double get() = thogai
}

typealias PiraVarivu = FactoryAdditionalCharge

/**
 * Computed totals result for Factory mode invoices.
 */
data class FactoryTotals(
    val adippadaiMothangal: Double = 0.0,
    val mothaEdai: Double = 0.0,
    val perumMothangal: Double = 0.0
) {
    val subtotal: Double get() = adippadaiMothangal
    val totalWeight: Double get() = mothaEdai
    val grandTotal: Double get() = perumMothangal
}

typealias KooliMothangal = FactoryTotals

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
            val rowTotal = floor(item.edai * item.vilai)
            subtotal += rowTotal
            totalKg += item.edai
        }

        totalKg += setharamGrams / 1000.0

        var piraTotal = 0.0
        for (charge in charges) {
            piraTotal += charge.thogai
        }

        val grandTotal = subtotal + thabaalThogai + ahimsaPattuThogai + piraTotal
        val roundedTotalKg = round(totalKg * 1000.0) / 1000.0

        return FactoryTotals(
            adippadaiMothangal = subtotal,
            mothaEdai = roundedTotalKg,
            perumMothangal = grandTotal
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
            sb.append("\"porulId\":").append(if (item.porulId != null) "\"${escapeJson(item.porulId)}\"" else "null").append(",")
            sb.append("\"porulPeyar\":\"${escapeJson(item.porulPeyar)}\",")
            sb.append("\"porulPeyarEn\":\"${escapeJson(item.porulPeyarEn)}\",")
            sb.append("\"edai\":${item.edai},")
            sb.append("\"vilai\":${item.vilai},")
            sb.append("\"mozhiMap\":${MozhiJsonConverter.stringify(item.mozhiMap)}")
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
                val porulId = Regex(""""porulId"\s*:\s*(?:"([^"]*)"|null)""").find(body)?.let {
                    if (it.groupValues[1].isNotEmpty()) it.groupValues[1] else null
                }
                val porulPeyar = Regex(""""porulPeyar"\s*:\s*"((?:\\.|[^"\\])*)"""").find(body)?.groupValues?.get(1)
                    ?.replace("\\\"", "\"")?.replace("\\\\", "\\") ?: ""
                val porulPeyarEn = Regex(""""porulPeyarEn"\s*:\s*"((?:\\.|[^"\\])*)"""").find(body)?.groupValues?.get(1)
                    ?.replace("\\\"", "\"")?.replace("\\\\", "\\") ?: ""
                val edai = Regex(""""edai"\s*:\s*([0-9.]+)""").find(body)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
                val vilai = Regex(""""vilai"\s*:\s*([0-9.]+)""").find(body)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0

                val mozhiMapMatch = Regex(""""mozhiMap"\s*:\s*(\{[^}]*\})""").find(body)?.groupValues?.get(1)
                val mozhiMap = MozhiJsonConverter.parse(mozhiMapMatch)

                FactoryItem(
                    porulId = porulId,
                    porulPeyar = porulPeyar,
                    porulPeyarEn = porulPeyarEn,
                    edai = edai,
                    vilai = vilai,
                    mozhiMap = mozhiMap
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
            sb.append("\"peyar\":\"${escapeJson(charge.peyar)}\",")
            sb.append("\"thogai\":${charge.thogai}")
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
                val peyar = Regex(""""peyar"\s*:\s*"((?:\\.|[^"\\])*)"""").find(body)?.groupValues?.get(1)
                    ?.replace("\\\"", "\"")?.replace("\\\\", "\\") ?: ""
                val thogai = Regex(""""thogai"\s*:\s*([0-9.]+)""").find(body)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0

                FactoryAdditionalCharge(
                    id = id,
                    peyar = peyar,
                    thogai = thogai
                )
            } catch (_: Exception) {
                null
            }
        }
    }

    // ── Backward-compatible Aliases ──
    fun kooliListToJson(items: List<FactoryItem>): String = itemsToJson(items)
    fun kooliListFromJson(json: String?): List<FactoryItem> = itemsFromJson(json)
    fun piraVarivuListToJson(charges: List<FactoryAdditionalCharge>): String = chargesToJson(charges)
    fun piraVarivuListFromJson(json: String?): List<FactoryAdditionalCharge> = chargesFromJson(json)

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

val KooliKanakku = FactoryCalculation
