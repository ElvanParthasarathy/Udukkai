package com.elvan.udukkai.ui.screens.editor.invoice.components

import com.elvan.udukkai.data.settings.MozhiJsonConverter
import kotlin.math.round

/**
 * Single line item for Silk mode invoices.
 * Matches Flutter's `PattuUrupadi`.
 */
data class PattuUrupadi(
    val porulId: String? = null,
    val porulPeyar: String = "",
    val porulPeyarEn: String = "",
    val hsnKuriyeedu: String = "",
    val alavu: Double = 1.0,
    val alagu: String = "Nos",
    val vilai: Double = 0.0,
    val variVizhukkaadu: Double = 5.0,
    val thallupadi: Double = 0.0,
    val thallupadiVagai: String = "%",
    val mozhiMap: Map<String, String> = emptyMap()
) {
    val adippadaiThogai: Double get() = alavu * vilai

    val thallupadiThogai: Double
        get() = if (thallupadiVagai == "%") {
            adippadaiThogai * (thallupadi / 100.0)
        } else {
            thallupadi
        }

    val taxableAmount: Double get() = (adippadaiThogai - thallupadiThogai).coerceAtLeast(0.0)
    val rowTax: Double get() = taxableAmount * (variVizhukkaadu / 100.0)
    val rowTotal: Double get() = taxableAmount + rowTax
}

/**
 * Computed totals result for Silk mode invoices.
 * Matches Flutter's `PattuMothangal`.
 */
data class PattuMothangal(
    val adippadaiMothangal: Double = 0.0,
    val thallupadiMothangal: Double = 0.0,
    val cgst: Double = 0.0,
    val sgst: Double = 0.0,
    val igst: Double = 0.0,
    val variMothangal: Double = 0.0,
    val suttruOff: Double = 0.0,
    val mothaMothangal: Double = 0.0
)

/**
 * Silk invoice calculation engine.
 * Matches Flutter's `PattuKanakku` 1:1.
 */
object PattuKanakku {
    fun calculate(
        items: List<PattuUrupadi>,
        globalDiscountValue: Double = 0.0,
        globalDiscountType: String = "%",
        businessState: String = "",
        customerState: String = "",
        country: String = "India"
    ): PattuMothangal {
        var rawSubtotal = 0.0
        var itemDiscounts = 0.0

        for (item in items) {
            val amount = item.alavu * item.vilai
            val rawDiscount = item.thallupadi
            val discountAmount = if (item.thallupadiVagai == "%") {
                amount * (rawDiscount / 100.0)
            } else {
                rawDiscount
            }
            rawSubtotal += amount
            itemDiscounts += discountAmount
        }

        val afterItemDiscountSubtotal = (rawSubtotal - itemDiscounts).coerceAtLeast(0.0)

        val globalDiscountAmount = if (globalDiscountType == "%") {
            afterItemDiscountSubtotal * (globalDiscountValue / 100.0)
        } else {
            globalDiscountValue
        }

        val totalDiscount = itemDiscounts + globalDiscountAmount

        var taxTotal = 0.0

        for (item in items) {
            val amount = item.alavu * item.vilai
            val rawDiscount = item.thallupadi
            val itemDiscount = if (item.thallupadiVagai == "%") {
                amount * (rawDiscount / 100.0)
            } else {
                rawDiscount
            }

            val afterItemDiscount = (amount - itemDiscount).coerceAtLeast(0.0)

            val itemWeight = if (afterItemDiscountSubtotal > 0) {
                afterItemDiscount / afterItemDiscountSubtotal
            } else {
                0.0
            }
            val itemGlobalDiscount = globalDiscountAmount * itemWeight
            val taxableAmount = (afterItemDiscount - itemGlobalDiscount).coerceAtLeast(0.0)

            taxTotal += (taxableAmount * item.variVizhukkaadu) / 100.0
        }

        val isIndia = country.equals("india", ignoreCase = true) || country.equals("in", ignoreCase = true)
        val bState = businessState.trim().lowercase()
        val cState = customerState.trim().lowercase()
        val isInterstate = isIndia && bState.isNotEmpty() && cState.isNotEmpty() && bState != cState

        val cgst: Double
        val sgst: Double
        val igst: Double

        if (isInterstate) {
            cgst = 0.0
            sgst = 0.0
            igst = taxTotal
        } else {
            cgst = taxTotal / 2.0
            sgst = taxTotal / 2.0
            igst = 0.0
        }

        val exactTotal = (rawSubtotal - totalDiscount + taxTotal).coerceAtLeast(0.0)
        val roundedTotal = round(exactTotal)
        val suttruOff = roundedTotal - exactTotal

        return PattuMothangal(
            adippadaiMothangal = rawSubtotal,
            thallupadiMothangal = totalDiscount,
            cgst = cgst,
            sgst = sgst,
            igst = igst,
            variMothangal = taxTotal,
            suttruOff = suttruOff,
            mothaMothangal = roundedTotal
        )
    }

    private fun escapeJson(s: String): String {
        return s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    fun pattuListToJson(items: List<PattuUrupadi>): String {
        if (items.isEmpty()) return "[]"
        val sb = StringBuilder("[")
        items.forEachIndexed { idx, item ->
            if (idx > 0) sb.append(",")
            sb.append("{")
            sb.append("\"porulId\":").append(if (item.porulId != null) "\"${escapeJson(item.porulId)}\"" else "null").append(",")
            sb.append("\"porulPeyar\":\"${escapeJson(item.porulPeyar)}\",")
            sb.append("\"porulPeyarEn\":\"${escapeJson(item.porulPeyarEn)}\",")
            sb.append("\"hsnKuriyeedu\":\"${escapeJson(item.hsnKuriyeedu)}\",")
            sb.append("\"alavu\":${item.alavu},")
            sb.append("\"alagu\":\"${escapeJson(item.alagu)}\",")
            sb.append("\"vilai\":${item.vilai},")
            sb.append("\"variVizhukkaadu\":${item.variVizhukkaadu},")
            sb.append("\"thallupadi\":${item.thallupadi},")
            sb.append("\"thallupadiVagai\":\"${escapeJson(item.thallupadiVagai)}\",")
            sb.append("\"thallupadiThogai\":${item.thallupadiThogai},")
            sb.append("\"mozhiMap\":${MozhiJsonConverter.stringify(item.mozhiMap)}")
            sb.append("}")
        }
        sb.append("]")
        return sb.toString()
    }

    fun pattuListFromJson(json: String?): List<PattuUrupadi> {
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
                val hsnKuriyeedu = Regex(""""hsnKuriyeedu"\s*:\s*"((?:\\.|[^"\\])*)"""").find(body)?.groupValues?.get(1)
                    ?.replace("\\\"", "\"")?.replace("\\\\", "\\") ?: ""
                val alavu = Regex(""""alavu"\s*:\s*([0-9.]+)""").find(body)?.groupValues?.get(1)?.toDoubleOrNull() ?: 1.0
                val alagu = Regex(""""alagu"\s*:\s*"((?:\\.|[^"\\])*)"""").find(body)?.groupValues?.get(1)
                    ?.replace("\\\"", "\"")?.replace("\\\\", "\\") ?: "Nos"
                val vilai = Regex(""""vilai"\s*:\s*([0-9.]+)""").find(body)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
                val variVizhukkaadu = Regex(""""variVizhukkaadu"\s*:\s*([0-9.]+)""").find(body)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
                val thallupadi = Regex(""""thallupadi"\s*:\s*([0-9.]+)""").find(body)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
                val thallupadiVagai = Regex(""""thallupadiVagai"\s*:\s*"((?:\\.|[^"\\])*)"""").find(body)?.groupValues?.get(1)
                    ?.replace("\\\"", "\"")?.replace("\\\\", "\\") ?: "%"

                val mozhiMapMatch = Regex(""""mozhiMap"\s*:\s*(\{[^}]*\})""").find(body)?.groupValues?.get(1)
                val mozhiMap = MozhiJsonConverter.parse(mozhiMapMatch)

                PattuUrupadi(
                    porulId = porulId,
                    porulPeyar = porulPeyar,
                    porulPeyarEn = porulPeyarEn,
                    hsnKuriyeedu = hsnKuriyeedu,
                    alavu = alavu,
                    alagu = alagu,
                    vilai = vilai,
                    variVizhukkaadu = variVizhukkaadu,
                    thallupadi = thallupadi,
                    thallupadiVagai = thallupadiVagai,
                    mozhiMap = mozhiMap
                )
            } catch (_: Exception) {
                null
            }
        }
    }

    fun variToJson(totals: PattuMothangal): String {
        return "{\"cgst\":${totals.cgst},\"sgst\":${totals.sgst},\"igst\":${totals.igst}}"
    }

    data class PattuViruppangal(
        val globalDiscountValue: Double = 0.0,
        val globalDiscountType: String = "%",
        val placeOfSupply: String = "Tamil Nadu",
        val placeOfSupplyTa: String = "தமிழ்நாடு"
    )

    fun viruppangalToJson(viruppangal: PattuViruppangal): String {
        return "{" +
            "\"globalDiscountValue\":${viruppangal.globalDiscountValue}," +
            "\"globalDiscountType\":\"${escapeJson(viruppangal.globalDiscountType)}\"," +
            "\"placeOfSupply\":\"${escapeJson(viruppangal.placeOfSupply)}\"," +
            "\"placeOfSupplyTa\":\"${escapeJson(viruppangal.placeOfSupplyTa)}\"" +
            "}"
    }

    fun viruppangalFromJson(json: String?): PattuViruppangal {
        if (json.isNullOrBlank() || json.trim() == "{}" || json.trim() == "null") {
            return PattuViruppangal()
        }
        val discountValue = Regex(""""globalDiscountValue"\s*:\s*([0-9.]+)""").find(json)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
        val discountType = Regex(""""globalDiscountType"\s*:\s*"((?:\\.|[^"\\])*)"""").find(json)?.groupValues?.get(1)
            ?.replace("\\\"", "\"")?.replace("\\\\", "\\") ?: "%"
        val placeOfSupply = Regex(""""placeOfSupply"\s*:\s*"((?:\\.|[^"\\])*)"""").find(json)?.groupValues?.get(1)
            ?.replace("\\\"", "\"")?.replace("\\\\", "\\") ?: "Tamil Nadu"
        val placeOfSupplyTa = Regex(""""placeOfSupplyTa"\s*:\s*"((?:\\.|[^"\\])*)"""").find(json)?.groupValues?.get(1)
            ?.replace("\\\"", "\"")?.replace("\\\\", "\\") ?: "தமிழ்நாடு"
        return PattuViruppangal(
            globalDiscountValue = discountValue,
            globalDiscountType = discountType,
            placeOfSupply = placeOfSupply,
            placeOfSupplyTa = placeOfSupplyTa
        )
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
