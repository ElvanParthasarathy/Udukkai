package com.elvan.udukkai.data.model

/**
 * Per-bill configuration options stored in [InvoiceModel.customPreferencesJson].
 */
data class InvoiceDisplayOptions(
    val taxInclusive: Boolean = false,
    val showHSN: Boolean = true,
    val showDiscountColumn: Boolean = true,
    val showVehicle: Boolean = false,
    val vehicleNumber: String = "",
    val showDueDate: Boolean = false,
    val dueDate: Long? = null,
    val selectedAccountId: String? = null,
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
        sb.append("\"showVehicle\":").append(showVehicle).append(",")
        sb.append("\"vehicleNumber\":\"").append(vehicleNumber.replace("\"", "\\\"")).append("\",")
        sb.append("\"showDueDate\":").append(showDueDate).append(",")
        if (dueDate != null) {
            sb.append("\"dueDate\":").append(dueDate).append(",")
        }
        if (!selectedAccountId.isNullOrEmpty()) {
            sb.append("\"selectedAccountId\":\"").append(selectedAccountId.replace("\"", "\\\"")).append("\",")
        }
        sb.append("\"placeOfSupply\":\"").append(placeOfSupply.replace("\"", "\\\"")).append("\",")
        sb.append("\"placeOfSupplyTa\":\"").append(placeOfSupplyTa.replace("\"", "\\\"")).append("\",")
        sb.append("\"globalDiscountValue\":").append(globalDiscountValue).append(",")
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
                val veh = Regex(""""showVehicle"\s*:\s*(true|false)""").find(json)?.groupValues?.get(1)?.toBoolean() ?: false
                val vehNum = Regex(""""vehicleNumber"\s*:\s*"([^"]*)"""").find(json)?.groupValues?.get(1) ?: ""
                val due = Regex(""""showDueDate"\s*:\s*(true|false)""").find(json)?.groupValues?.get(1)?.toBoolean() ?: false
                val dueDateVal = Regex(""""dueDate"\s*:\s*(\d+)""").find(json)?.groupValues?.get(1)?.toLongOrNull()
                val accId = Regex(""""selectedAccountId"\s*:\s*"([^"]*)"""").find(json)?.groupValues?.get(1)
                val pos = Regex(""""placeOfSupply"\s*:\s*"([^"]*)"""").find(json)?.groupValues?.get(1)?.ifEmpty { "Tamil Nadu" } ?: "Tamil Nadu"
                val posTa = Regex(""""placeOfSupplyTa"\s*:\s*"([^"]*)"""").find(json)?.groupValues?.get(1)?.ifEmpty { "தமிழ்நாடு" } ?: "தமிழ்நாடு"
                val gDiscVal = Regex(""""globalDiscountValue"\s*:\s*([\d.]+)""").find(json)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
                val gDiscType = Regex(""""globalDiscountType"\s*:\s*"([^"]*)"""").find(json)?.groupValues?.get(1) ?: "%"
                val measure = Regex(""""measureMode"\s*:\s*"([^"]*)"""").find(json)?.groupValues?.get(1) ?: ""

                InvoiceDisplayOptions(
                    taxInclusive = taxInc,
                    showHSN = hsn,
                    showDiscountColumn = disc,
                    showVehicle = veh,
                    vehicleNumber = vehNum,
                    showDueDate = due,
                    dueDate = dueDateVal,
                    selectedAccountId = accId,
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
