package com.elvan.udukkai.textiles.ui.screens.editor

import com.elvan.udukkai.data.settings.LanguageJsonConverter
import kotlin.math.round

private var nextTextilesId = 0L
fun generateTextilesId(): String = "${System.currentTimeMillis()}_${++nextTextilesId}"

@Deprecated("Use generateTextilesId", ReplaceWith("generateTextilesId()"))
fun generatePattuId(): String = generateTextilesId()

/**
 * Single line item for Textiles (சவுளி) mode invoices.
 * Matches Flutter's `PattuUrupadi`.
 */
data class TextilesItem(
    val id: String = generateTextilesId(),
    val productId: String? = null,
    val productName: String = "",
    val productNameEn: String = "",
    val hsnCode: String = "",
    val quantity: Double = 1.0,
    val unit: String = "Nos",
    val price: Double = 0.0,
    val taxRate: Double = 5.0,
    val discount: Double = 0.0,
    val discountType: String = "%",
    val languageMap: Map<String, String> = emptyMap()
) {
    val name: String get() = productName
    val rate: Double get() = price

    val baseAmount: Double get() = quantity * price
    val adippadaiThogai: Double get() = baseAmount

    val discountAmount: Double
        get() = if (discountType == "%") {
            baseAmount * (discount / 100.0)
        } else {
            discount
        }

    val taxableAmount: Double get() = (baseAmount - discountAmount).coerceAtLeast(0.0)
    val rowTax: Double get() = taxableAmount * (taxRate / 100.0)
    val rowTotal: Double get() = taxableAmount + rowTax


}



/**
 * Computed totals result for Textiles mode invoices.
 */
data class TextilesTotals(
    val subtotal: Double = 0.0,
    val totalDiscount: Double = 0.0,
    val cgst: Double = 0.0,
    val sgst: Double = 0.0,
    val igst: Double = 0.0,
    val totalTax: Double = 0.0,
    val roundOff: Double = 0.0,
    val grandTotal: Double = 0.0
)



/**
 * Optional invoicing preferences for Textiles mode.
 */
data class TextilesPreferences(
    val globalDiscountValue: Double = 0.0,
    val globalDiscountType: String = "%",
    val placeOfSupply: String = "Tamil Nadu",
    val placeOfSupplyTa: String = "தமிழ்நாடு"
)



/**
 * Textiles invoice calculation engine.
 * Full intra-state (CGST + SGST) and inter-state (IGST) support.
 */
object TextilesCalculation {
    fun calculate(
        items: List<TextilesItem>,
        globalDiscountValue: Double = 0.0,
        globalDiscountType: String = "%",
        businessState: String = "",
        customerState: String = "",
        country: String = "India"
    ): TextilesTotals {
        var rawSubtotal = 0.0
        var itemDiscounts = 0.0

        for (item in items) {
            val amount = item.quantity * item.price
            val rawDiscount = item.discount
            val discountAmount = if (item.discountType == "%") {
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
            val amount = item.quantity * item.price
            val rawDiscount = item.discount
            val itemDiscount = if (item.discountType == "%") {
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

            taxTotal += (taxableAmount * item.taxRate) / 100.0
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

        return TextilesTotals(
            subtotal = rawSubtotal,
            totalDiscount = totalDiscount,
            cgst = cgst,
            sgst = sgst,
            igst = igst,
            totalTax = taxTotal,
            roundOff = suttruOff,
            grandTotal = roundedTotal
        )
    }

    private fun escapeJson(s: String): String {
        return s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    fun itemsToJson(items: List<TextilesItem>): String {
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
            sb.append("\"hsnCode\":\"${escapeJson(item.hsnCode)}\",")
            sb.append("\"hsnKuriyeedu\":\"${escapeJson(item.hsnCode)}\",")
            sb.append("\"quantity\":${item.quantity},")
            sb.append("\"alavu\":${item.quantity},")
            sb.append("\"unit\":\"${escapeJson(item.unit)}\",")
            sb.append("\"alagu\":\"${escapeJson(item.unit)}\",")
            sb.append("\"price\":${item.price},")
            sb.append("\"vilai\":${item.price},")
            sb.append("\"taxRate\":${item.taxRate},")
            sb.append("\"variVizhukkaadu\":${item.taxRate},")
            sb.append("\"discount\":${item.discount},")
            sb.append("\"thallupadi\":${item.discount},")
            sb.append("\"discountType\":\"${escapeJson(item.discountType)}\",")
            sb.append("\"thallupadiVagai\":\"${escapeJson(item.discountType)}\",")
            sb.append("\"discountAmount\":${item.discountAmount},")
            sb.append("\"thallupadiThogai\":${item.discountAmount},")
            sb.append("\"languageMap\":${LanguageJsonConverter.stringify(item.languageMap)},")
            sb.append("\"mozhiMap\":${LanguageJsonConverter.stringify(item.languageMap)}")
            sb.append("}")
        }
        sb.append("]")
        return sb.toString()
    }

    fun itemsFromJson(json: String?): List<TextilesItem> {
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
                val hsn = Regex(""""(?:hsnCode|hsnKuriyeedu)"\s*:\s*"((?:\\.|[^"\\])*)"""").find(body)?.groupValues?.get(1)
                    ?.replace("\\\"", "\"")?.replace("\\\\", "\\") ?: ""
                val quantity = Regex(""""(?:quantity|alavu)"\s*:\s*([0-9.]+)""").find(body)?.groupValues?.get(1)?.toDoubleOrNull() ?: 1.0
                val unit = Regex(""""(?:unit|alagu)"\s*:\s*"((?:\\.|[^"\\])*)"""").find(body)?.groupValues?.get(1)
                    ?.replace("\\\"", "\"")?.replace("\\\\", "\\") ?: "Nos"
                val price = Regex(""""(?:price|vilai)"\s*:\s*([0-9.]+)""").find(body)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
                val taxRate = Regex(""""(?:taxRate|variVizhukkaadu)"\s*:\s*([0-9.]+)""").find(body)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
                val discount = Regex(""""(?:discount|thallupadi)"\s*:\s*([0-9.]+)""").find(body)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
                val discountType = Regex(""""(?:discountType|thallupadiVagai)"\s*:\s*"((?:\\.|[^"\\])*)"""").find(body)?.groupValues?.get(1)
                    ?.replace("\\\"", "\"")?.replace("\\\\", "\\") ?: "%"

                val langMapMatch = Regex(""""(?:languageMap|mozhiMap)"\s*:\s*(\{[^}]*\})""").find(body)?.groupValues?.get(1)
                val langMap = LanguageJsonConverter.parse(langMapMatch)

                TextilesItem(
                    productId = pId,
                    productName = pName,
                    productNameEn = pNameEn,
                    hsnCode = hsn,
                    quantity = quantity,
                    unit = unit,
                    price = price,
                    taxRate = taxRate,
                    discount = discount,
                    discountType = discountType,
                    languageMap = langMap
                )
            } catch (_: Exception) {
                null
            }
        }
    }

    fun taxToJson(totals: TextilesTotals): String {
        return "{\"cgst\":${totals.cgst},\"sgst\":${totals.sgst},\"igst\":${totals.igst}}"
    }

    fun preferencesToJson(preferences: TextilesPreferences): String {
        return "{" +
            "\"globalDiscountValue\":${preferences.globalDiscountValue}," +
            "\"globalDiscountType\":\"${escapeJson(preferences.globalDiscountType)}\"," +
            "\"placeOfSupply\":\"${escapeJson(preferences.placeOfSupply)}\"," +
            "\"placeOfSupplyTa\":\"${escapeJson(preferences.placeOfSupplyTa)}\"" +
            "}"
    }

    fun preferencesFromJson(json: String?): TextilesPreferences {
        if (json.isNullOrBlank() || json.trim() == "{}" || json.trim() == "null") {
            return TextilesPreferences()
        }
        val discountValue = Regex(""""globalDiscountValue"\s*:\s*([0-9.]+)""").find(json)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
        val discountType = Regex(""""globalDiscountType"\s*:\s*"((?:\\.|[^"\\])*)"""").find(json)?.groupValues?.get(1)
            ?.replace("\\\"", "\"")?.replace("\\\\", "\\") ?: "%"
        val placeOfSupply = Regex(""""placeOfSupply"\s*:\s*"((?:\\.|[^"\\])*)"""").find(json)?.groupValues?.get(1)
            ?.replace("\\\"", "\"")?.replace("\\\\", "\\") ?: "Tamil Nadu"
        val placeOfSupplyTa = Regex(""""placeOfSupplyTa"\s*:\s*"((?:\\.|[^"\\])*)"""").find(json)?.groupValues?.get(1)
            ?.replace("\\\"", "\"")?.replace("\\\\", "\\") ?: "தமிழ்நாடு"
        return TextilesPreferences(
            globalDiscountValue = discountValue,
            globalDiscountType = discountType,
            placeOfSupply = placeOfSupply,
            placeOfSupplyTa = placeOfSupplyTa
        )
    }

    // ── Backward-compatible Aliases ──
    fun variToJson(totals: TextilesTotals): String = taxToJson(totals)
    fun viruppangalToJson(preferences: TextilesPreferences): String = preferencesToJson(preferences)
    fun viruppangalFromJson(json: String?): TextilesPreferences = preferencesFromJson(json)

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
