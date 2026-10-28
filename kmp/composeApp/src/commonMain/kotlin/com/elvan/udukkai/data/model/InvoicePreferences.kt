package com.elvan.udukkai.data.model

import com.elvan.udukkai.core.utils.BillRules

/**
 * Per-bill configuration stored in [InvoiceModel.customPreferencesJson].
 */
data class InvoiceDisplayOptions(
    val taxInclusive: Boolean = false,
    val showHSN: Boolean = true,
    val showDiscountColumn: Boolean = true,
    val placeOfSupply: String = "Tamil Nadu",
    val placeOfSupplyTa: String = "தமிழ்நாடு",
    val globalDiscountValue: Double = 0.0,
    val globalDiscountType: String = "%",
    /** React per-bill measure mode: "weight" shows the எடை / Weight column with " Kg". */
    val measureMode: String = ""
) {
    fun toJson(): String {
        val sb = StringBuilder("{")
        sb.append("\"taxInclusive\":").append(taxInclusive).append(",")
        sb.append("\"showHSN\":").append(showHSN).append(",")
        sb.append("\"showDiscountColumn\":").append(showDiscountColumn).append(",")
        sb.append("\"placeOfSupply\":\"").append(placeOfSupply.replace("\"", "\\\"")).append("\",")
        sb.append("\"placeOfSupplyTa\":\"").append(placeOfSupplyTa.replace("\"", "\\\"")).append("\",")
        sb.append("\"globalDiscountValue\":").append(BillRules.plainNumber(globalDiscountValue)).append(",")
        sb.append("\"globalDiscountType\":\"").append(globalDiscountType.replace("\"", "\\\"")).append("\",")
        sb.append("\"measureMode\":\"").append(measureMode.replace("\"", "\\\"")).append("\"")
        sb.append("}")
        return sb.toString()
    }

    companion object {
        fun fromJson(json: String?): InvoiceDisplayOptions {
            if (json.isNullOrBlank() || json.trim() == "{}" || json.trim() == "null") {
                return InvoiceDisplayOptions()
            }
            return try {
                val taxInc = Regex(""""taxInclusive"\s*:\s*(true|false)""").find(json)?.groupValues?.get(1)?.toBoolean() ?: false
                val hsn = Regex(""""showHSN"\s*:\s*(true|false)""").find(json)?.groupValues?.get(1)?.toBoolean() ?: true
                val disc = Regex(""""showDiscountColumn"\s*:\s*(true|false)""").find(json)?.groupValues?.get(1)?.toBoolean() ?: true
                val pos = Regex(""""placeOfSupply"\s*:\s*"([^"]*)"""").find(json)?.groupValues?.get(1)?.ifEmpty { "Tamil Nadu" } ?: "Tamil Nadu"
                val posTa = Regex(""""placeOfSupplyTa"\s*:\s*"([^"]*)"""").find(json)?.groupValues?.get(1)?.ifEmpty { "தமிழ்நாடு" } ?: "தமிழ்நாடு"
                val gDiscVal = Regex(""""globalDiscountValue"\s*:\s*([-+0-9.eE]+)""").find(json)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
                val gDiscType = Regex(""""globalDiscountType"\s*:\s*"([^"]*)"""").find(json)?.groupValues?.get(1) ?: "%"
                val measure = Regex(""""measureMode"\s*:\s*"([^"]*)"""").find(json)?.groupValues?.get(1) ?: ""

                InvoiceDisplayOptions(
                    taxInclusive = taxInc,
                    showHSN = hsn,
                    showDiscountColumn = disc,
                    placeOfSupply = pos,
                    placeOfSupplyTa = posTa,
                    globalDiscountValue = gDiscVal,
                    globalDiscountType = gDiscType,
                    measureMode = measure
                )
            } catch (e: Exception) {
                InvoiceDisplayOptions()
            }
        }
    }
}
