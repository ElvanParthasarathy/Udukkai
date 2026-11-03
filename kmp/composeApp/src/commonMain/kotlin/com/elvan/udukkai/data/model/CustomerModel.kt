package com.elvan.udukkai.data.model

/**
 * Domain model for a Customer / Merchant.
 * Standardized pure English model.
 */
data class CustomerModel(
    val id: Long = 0,
    val name: Map<String, String> = emptyMap(),
    val address: Map<String, String> = emptyMap(),
    val city: Map<String, String> = emptyMap(),
    val district: Map<String, String> = emptyMap(),
    val state: Map<String, String> = emptyMap(),
    val country: Map<String, String> = emptyMap(),
    val internationalAddress: Map<String, String> = emptyMap(),
    val pinCode: String = "",
    val gstin: String = "",
    val email: String = "",
    val phone: String = "",
    val companyIds: List<Long> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    /** What does not fit the columns above: "type" = "b2b" for a business, "owner.ta" / "owner.en" its owner, "show" = what the bill prints. */
    val extra: Map<String, String> = emptyMap()
) {
    val isBusiness: Boolean get() = extra["type"] == "b2b" || gstin.isNotBlank()

    val ownerName: Map<String, String> get() = extra.filterKeys { it.startsWith("owner.") }.mapKeys { it.key.removePrefix("owner.") }.filterValues { it.isNotBlank() }

    /** [SHOW_BOTH], [SHOW_BUSINESS] (the default) or [SHOW_OWNER]. */
    val billNameMode: String get() = extra["show"] ?: SHOW_BUSINESS

    /**
     * The name the bill prints, in each language: the business name, the owner's name, or both ("Business (Owner)"),
     * as the customer is set. A person (not a business), or a business without an owner name, prints the one name.
     */
    fun billName(): Map<String, String> {
        val owner = ownerName
        if (!isBusiness || owner.isEmpty()) return name
        return (name.keys + owner.keys).associateWith { lang ->
            val biz = name[lang].orEmpty()
            val own = owner[lang] ?: owner.values.first()
            when (billNameMode) {
                SHOW_OWNER -> own.ifBlank { biz }
                SHOW_BOTH -> if (biz.isBlank() || own.isBlank()) biz.ifBlank { own } else "$biz ($own)"
                else -> biz.ifBlank { own }
            }
        }.filterValues { it.isNotBlank() }
    }

    /**
     * Determines whether this customer is available for the given company.
     * If [companyIds] is empty, the customer is shared across all companies.
     * If [companyId] is null, it is available if the customer is shared.
     */
    fun isAvailableForCompany(companyId: Long?): Boolean {
        if (companyIds.isEmpty()) return true
        if (companyId == null) return false
        return companyIds.contains(companyId)
    }
}

const val SHOW_BOTH = "both"
const val SHOW_BUSINESS = "business"
const val SHOW_OWNER = "owner"

typealias Customer = CustomerModel
