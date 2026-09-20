package com.elvan.udukkai.ui.screens.editor.invoice.components

import com.elvan.udukkai.data.settings.MozhiJsonConverter
import kotlin.math.floor
import kotlin.math.round

private var nextKooliId = 0L
fun generateKooliId(): String = "${System.currentTimeMillis()}_${++nextKooliId}"

/**
 * Single line item for Coolie mode invoices.
 * Holds product reference, weight in kg, and rate per kg.
 * Row total is truncated (floored), matching Flutter and React 1:1.
 */
data class KooliUrupadi(
    val id: String = generateKooliId(),
    val porulId: String? = null,
    val porulPeyar: String = "",
    val porulPeyarEn: String = "",
    val edai: Double = 0.0,
    val vilai: Double = 0.0,
    val mozhiMap: Map<String, String> = emptyMap()
) {
    val varisaiThogai: Int get() = floor(edai * vilai).toInt()
}

/**
 * Additional charge line item for Coolie mode invoices (e.g. Loading, Transport).
 */
data class PiraVarivu(
    val id: String = System.currentTimeMillis().toString() + (0..999).random(),
    val peyar: String = "",
    val thogai: Double = 0.0
)

/**
 * Computed totals result for Coolie mode invoices.
 */
data class KooliMothangal(
    val adippadaiMothangal: Double = 0.0,
    val mothaEdai: Double = 0.0,
    val perumMothangal: Double = 0.0
)

/**
 * Coolie invoice calculation engine matching Flutter's `KooliKanakku` 1:1.
 * 1. Row total = floor(edai * vilai) (truncated, not rounded)
 * 2. subtotal = sum(row totals)
 * 3. totalKg = sum(item.edai) + (setharamGrams / 1000.0)
 * 4. grandTotal = subtotal + courier + ahimsa + sum(other charges)
 */
object KooliKanakku {
    fun calculate(
        items: List<KooliUrupadi>,
        setharamGrams: Double = 0.0,
        thabaalThogai: Double = 0.0,
        ahimsaPattuThogai: Double = 0.0,
        piraVarivugal: List<PiraVarivu> = emptyList()
    ): KooliMothangal {
        var subtotal = 0.0
        var totalKg = 0.0

        for (item in items) {
            val rowTotal = floor(item.edai * item.vilai)
            subtotal += rowTotal
            totalKg += item.edai
        }

        totalKg += setharamGrams / 1000.0

        var piraTotal = 0.0
        for (charge in piraVarivugal) {
            piraTotal += charge.thogai
        }

        val grandTotal = subtotal + thabaalThogai + ahimsaPattuThogai + piraTotal
        val roundedTotalKg = round(totalKg * 1000.0) / 1000.0

        return KooliMothangal(
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

    fun kooliListToJson(items: List<KooliUrupadi>): String {
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

    fun kooliListFromJson(json: String?): List<KooliUrupadi> {
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

                KooliUrupadi(
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

    fun piraVarivuListToJson(charges: List<PiraVarivu>): String {
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

    fun piraVarivuListFromJson(json: String?): List<PiraVarivu> {
        if (json.isNullOrBlank() || json.trim() == "[]" || json.trim() == "null") return emptyList()
        val objects = splitJsonObjects(json)
        return objects.mapNotNull { body ->
            try {
                val id = Regex(""""id"\s*:\s*"([^"]*)"""").find(body)?.groupValues?.get(1) ?: System.currentTimeMillis().toString()
                val peyar = Regex(""""peyar"\s*:\s*"((?:\\.|[^"\\])*)"""").find(body)?.groupValues?.get(1)
                    ?.replace("\\\"", "\"")?.replace("\\\\", "\\") ?: ""
                val thogai = Regex(""""thogai"\s*:\s*([0-9.]+)""").find(body)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0

                PiraVarivu(id = id, peyar = peyar, thogai = thogai)
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
