package com.elvan.udukkai.data.model

data class PorulTharavuru(
    val id: Long = 0,
    val porulPeyar: Map<String, String> = emptyMap(),
    val hsnCode: String = "",
    val vilai: Double = 0.0,
    val variVeetham: Double = 0.0,
    val alavuVagai: String = "quantity",
    val alagu: String = "Nos",
    val niruvanamIds: List<Long> = emptyList(),
    val companyPrices: Map<Long, Double> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
) {
    /**
     * Determines whether this product is available for the given company.
     * If [niruvanamIds] is empty, the product is shared across all companies.
     * If [companyId] is null, it is available if the product is shared.
     */
    fun isAvailableForCompany(companyId: Long?): Boolean {
        if (niruvanamIds.isEmpty()) return true
        if (companyId == null) return false
        return niruvanamIds.contains(companyId)
    }

    /**
     * Returns the effective selling price for the given company.
     * If a company-specific price override exists (> 0), it is returned;
     * otherwise, the default selling price [vilai] is returned.
     */
    fun getEffectivePrice(companyId: Long?): Double {
        if (companyId != null && companyPrices.containsKey(companyId)) {
            val price = companyPrices[companyId]
            if (price != null && price > 0.0) return price
        }
        return vilai
    }
}

object PorulJsonConverter {
    fun parseLongList(json: String?): List<Long> {
        if (json.isNullOrBlank() || json.trim() == "[]" || json.trim() == "null") return emptyList()
        val trimmed = json.trim()
        val content = if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            trimmed.substring(1, trimmed.length - 1)
        } else trimmed
        return content.split(",").mapNotNull { it.trim().toLongOrNull() }
    }

    fun stringifyLongList(list: List<Long>?): String {
        if (list.isNullOrEmpty()) return "[]"
        return "[${list.joinToString(",")}]"
    }

    fun parseCompanyPrices(json: String?): Map<Long, Double> {
        val result = mutableMapOf<Long, Double>()
        if (json.isNullOrBlank() || json.trim() == "{}" || json.trim() == "null") return result
        val trimmed = json.trim()
        val content = if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            trimmed.substring(1, trimmed.length - 1)
        } else trimmed
        val pattern = Regex(""""?(\d+)"?\s*:\s*([0-9.]+)""")
        for (match in pattern.findAll(content)) {
            val key = match.groupValues[1].toLongOrNull()
            val value = match.groupValues[2].toDoubleOrNull()
            if (key != null && value != null) {
                result[key] = value
            }
        }
        return result
    }

    fun stringifyCompanyPrices(map: Map<Long, Double>?): String {
        if (map.isNullOrEmpty()) return "{}"
        val entries = map.entries.joinToString(",") { (k, v) -> "\"$k\":$v" }
        return "{$entries}"
    }
}
