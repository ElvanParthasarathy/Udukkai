package com.elvan.udukkai.data.model

import com.elvan.udukkai.data.settings.LanguageJsonConverter

data class ItemModel(
    val id: Long = 0,
    val name: Map<String, String> = emptyMap(),
    val hsnCode: String = "",
    val price: Double = 0.0,
    val taxRate: Double = 0.0,
    val quantityType: String = "quantity",
    val unit: String = "Nos",
    val companyIds: List<Long> = emptyList(),
    val companyPrices: Map<Long, Double> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
) {

    /**
     * Determines whether this item is available for the given company.
     * If [companyIds] is empty, the item is shared across all companies.
     * If [companyId] is null, it is available if the item is shared.
     */
    fun isAvailableForCompany(companyId: Long?): Boolean {
        if (companyIds.isEmpty()) return true
        if (companyId == null) return false
        return companyIds.contains(companyId)
    }

    /**
     * Returns the effective selling price for the given company.
     * If a company-specific price override exists (> 0), it is returned;
     * otherwise, the default selling price [price] is returned.
     */
    fun getEffectivePrice(companyId: Long?): Double {
        if (companyId != null && companyPrices.containsKey(companyId)) {
            val p = companyPrices[companyId]
            if (p != null && p > 0.0) return p
        }
        return price
    }
}

object ItemJsonConverter {
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

object ItemFirebasePayloadConverter {
    /**
     * Converts an ItemModel to a HashMap suitable for Firebase Realtime Database.
     * Serializes companyIds as a List of Longs and companyPrices as a Map<String, Double>
     * where keys are stable string representations of company IDs.
     */
    fun toFirebasePayload(item: ItemModel): HashMap<String, Any?> {
        val pricesMap = hashMapOf<String, Double>()
        item.companyPrices.forEach { (companyId, price) ->
            if (companyId > 0 && price > 0.0) {
                pricesMap[companyId.toString()] = price
            }
        }

        return hashMapOf(
            "id" to item.id,
            "name" to LanguageJsonConverter.stringify(item.name),
            "itemName" to LanguageJsonConverter.stringify(item.name),
            "productName" to LanguageJsonConverter.stringify(item.name),
            "porulPeyar" to LanguageJsonConverter.stringify(item.name),
            "hsnCode" to item.hsnCode,
            "price" to item.price,
            "vilai" to item.price,
            "taxRate" to item.taxRate,
            "variVeetham" to item.taxRate,
            "quantityType" to item.quantityType,
            "alavuVagai" to item.quantityType,
            "unit" to item.unit,
            "alagu" to item.unit,
            "companyIds" to item.companyIds,
            "niruvanamIds" to item.companyIds,
            "companyPrices" to pricesMap,
            "createdAt" to item.createdAt,
            "updatedAt" to item.updatedAt,
            "isDeleted" to item.isDeleted,
            "deletedAt" to item.deletedAt
        )
    }

    /**
     * Parses companyIds from a raw object.
     * Supports:
     * - List<*> (e.g. ArrayList from Firebase RTDB)
     * - Map<*, *> (sparse arrays in Firebase RTDB: {"0": 101, "1": 102})
     * - String (JSON array "[101, 102]")
     * - Number (single ID)
     * Returns emptyList() if null, missing, empty, or unparseable.
     */
    fun parseCompanyIds(raw: Any?): List<Long> {
        if (raw == null) return emptyList()
        return when (raw) {
            is List<*> -> {
                raw.mapNotNull { element ->
                    when (element) {
                        is Number -> element.toLong()
                        is String -> element.trim().toLongOrNull()
                        else -> null
                    }
                }
            }
            is Map<*, *> -> {
                raw.values.mapNotNull { value ->
                    when (value) {
                        is Number -> value.toLong()
                        is String -> value.trim().toLongOrNull()
                        else -> null
                    }
                }
            }
            is String -> {
                ItemJsonConverter.parseLongList(raw)
            }
            is Number -> {
                listOf(raw.toLong())
            }
            else -> emptyList()
        }
    }

    /**
     * Parses companyPrices from a raw object.
     * Supports:
     * - Map<*, *> (e.g. HashMap from Firebase RTDB with String or Number keys)
     * - String (JSON string '{"101": 450.0}')
     * Returns emptyMap() if null, missing, empty, or unparseable.
     */
    fun parseCompanyPrices(raw: Any?): Map<Long, Double> {
        if (raw == null) return emptyMap()
        return when (raw) {
            is Map<*, *> -> {
                val result = mutableMapOf<Long, Double>()
                raw.forEach { (key, value) ->
                    val companyId = when (key) {
                        is Number -> key.toLong()
                        is String -> key.trim().toLongOrNull()
                        else -> null
                    }
                    val price = when (value) {
                        is Number -> value.toDouble()
                        is String -> value.trim().toDoubleOrNull()
                        else -> null
                    }
                    if (companyId != null && price != null && price > 0.0) {
                        result[companyId] = price
                    }
                }
                result
            }
            is String -> {
                ItemJsonConverter.parseCompanyPrices(raw)
            }
            else -> emptyMap()
        }
    }

    /**
     * Reconstructs an ItemModel from a raw Map (e.g. snapshot.value or simulated payload).
     * Backwards-compatible with legacy records missing companyIds and/or companyPrices.
     */
    fun fromRawMap(map: Map<String, Any?>, fallbackId: Long = 0L): ItemModel? {
        val id = when (val rawId = map["id"]) {
            is Number -> rawId.toLong()
            is String -> rawId.trim().toLongOrNull() ?: fallbackId
            else -> fallbackId
        }
        if (id <= 0L) return null

        val nameStr = (map["itemName"] ?: map["productName"] ?: map["name"] ?: map["porulPeyar"]) as? String ?: "{}"
        val hsnCode = map["hsnCode"] as? String ?: ""
        val price = when (val rawPrice = map["price"] ?: map["vilai"]) {
            is Number -> rawPrice.toDouble()
            is String -> rawPrice.trim().toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
        val taxRate = when (val rawTax = map["taxRate"] ?: map["variVeetham"]) {
            is Number -> rawTax.toDouble()
            is String -> rawTax.trim().toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
        val quantityType = (map["quantityType"] ?: map["alavuVagai"]) as? String ?: "quantity"
        val unit = (map["unit"] ?: map["alagu"]) as? String ?: "Nos"
        val createdAt = when (val raw = map["createdAt"]) {
            is Number -> raw.toLong()
            is String -> raw.trim().toLongOrNull() ?: (System.currentTimeMillis() / 1000)
            else -> System.currentTimeMillis() / 1000
        }
        val updatedAt = when (val raw = map["updatedAt"]) {
            is Number -> raw.toLong()
            is String -> raw.trim().toLongOrNull() ?: (System.currentTimeMillis() / 1000)
            else -> System.currentTimeMillis() / 1000
        }
        val isDeleted = map["isDeleted"] as? Boolean ?: false
        val deletedAt = when (val raw = map["deletedAt"]) {
            is Number -> raw.toLong()
            is String -> raw.trim().toLongOrNull()
            else -> null
        }

        val companyIds = parseCompanyIds(map["companyIds"] ?: map["niruvanamIds"])
        val companyPrices = parseCompanyPrices(map["companyPrices"])

        return ItemModel(
            id = id,
            name = LanguageJsonConverter.parse(nameStr),
            hsnCode = hsnCode,
            price = price,
            taxRate = taxRate,
            quantityType = quantityType,
            unit = unit,
            companyIds = companyIds,
            companyPrices = companyPrices,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isDeleted = isDeleted,
            deletedAt = deletedAt
        )
    }
}
