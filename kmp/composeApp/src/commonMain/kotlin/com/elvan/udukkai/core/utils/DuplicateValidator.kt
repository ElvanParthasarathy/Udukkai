package com.elvan.udukkai.core.utils

import com.elvan.udukkai.data.model.CustomerModel
import com.elvan.udukkai.data.model.ItemModel

/**
 * Universal duplicate validation for items and customers across Udukkai.
 *
 * Rules:
 * - Items: Name must be unique within an overlapping business profile scope.
 * - Customers: Customer name can be shared across branches/entities, but the address
 *   (street address, city, district, state, or PIN code) MUST be different.
 *   If both name AND address match an existing active customer, it is blocked as duplicate.
 */
object DuplicateValidator {

    /**
     * Checks if two company ID lists overlap in business profile scope.
     * An empty list denotes "All Companies" (global), which overlaps with any scope.
     */
    fun isCompanyOverlap(c1: List<Long>, c2: List<Long>): Boolean =
        c1.isEmpty() || c2.isEmpty() || c1.any { it in c2 }

    /**
     * Checks if any non-blank name in [candidateNames] matches any non-blank name
     * in [existingNames], trimmed and case-insensitively.
     */
    fun isDuplicateName(
        candidateNames: Map<String, String>,
        existingNames: Map<String, String>
    ): Boolean {
        val candidateVals = candidateNames.values
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        val existingVals = existingNames.values
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (candidateVals.isEmpty() || existingVals.isEmpty()) return false

        return candidateVals.any { cVal ->
            existingVals.any { eVal ->
                eVal.equals(cVal, ignoreCase = true)
            }
        }
    }

    /**
     * Checks if two string maps have differing non-blank values for an address component
     * (e.g. city, district, state, address).
     * If both maps contain non-blank values but share no common value (case-insensitive),
     * returns true (indicating they are definitely different).
     */
    fun hasComponentMismatch(m1: Map<String, String>, m2: Map<String, String>): Boolean {
        val vals1 = m1.values.map { it.trim().lowercase() }.filter { it.isNotEmpty() }
        val vals2 = m2.values.map { it.trim().lowercase() }.filter { it.isNotEmpty() }
        if (vals1.isEmpty() || vals2.isEmpty()) return false
        val hasMatch = vals1.any { v1 -> vals2.any { v2 -> v1 == v2 } }
        return !hasMatch
    }

    /**
     * Determines whether two customer records have the same address.
     * Returns true if their address fields represent the same location
     * (or both have no address information at all).
     * Returns false if there is a clear distinction in their address
     * (different city, street address, district, state, or PIN code).
     */
    fun isSameCustomerAddress(
        candidateAddress: Map<String, String>,
        candidateCity: Map<String, String>,
        candidateDistrict: Map<String, String>,
        candidateState: Map<String, String>,
        candidatePinCode: String,
        existingCustomer: CustomerModel
    ): Boolean {
        val candPin = candidatePinCode.trim()
        val existPin = existingCustomer.pinCode.trim()

        // 1. If both have different non-blank PIN codes -> different address!
        if (candPin.isNotEmpty() && existPin.isNotEmpty() && candPin != existPin) {
            return false
        }

        // 2. If cities differ -> different address!
        if (hasComponentMismatch(candidateCity, existingCustomer.city)) {
            return false
        }

        // 3. If street addresses differ -> different address!
        if (hasComponentMismatch(candidateAddress, existingCustomer.address)) {
            return false
        }

        // 4. If districts differ -> different address!
        if (hasComponentMismatch(candidateDistrict, existingCustomer.district)) {
            return false
        }

        // 5. If states differ -> different address!
        if (hasComponentMismatch(candidateState, existingCustomer.state)) {
            return false
        }

        // Check if candidate has ANY address data
        val candHasData = candPin.isNotEmpty() ||
            candidateCity.values.any { it.trim().isNotEmpty() } ||
            candidateAddress.values.any { it.trim().isNotEmpty() } ||
            candidateDistrict.values.any { it.trim().isNotEmpty() } ||
            candidateState.values.any { it.trim().isNotEmpty() }

        // Check if existing has ANY address data
        val existHasData = existPin.isNotEmpty() ||
            existingCustomer.city.values.any { it.trim().isNotEmpty() } ||
            existingCustomer.address.values.any { it.trim().isNotEmpty() } ||
            existingCustomer.district.values.any { it.trim().isNotEmpty() } ||
            existingCustomer.state.values.any { it.trim().isNotEmpty() }

        // If one has address data and the other has none -> they are differentiated!
        if (candHasData != existHasData) {
            return false
        }

        // Otherwise (both have no address data, or neither had any component mismatch):
        // they are considered the SAME address!
        return true
    }

    /**
     * Returns true if [candidateNameMap] duplicates an existing active item
     * within an overlapping company scope, excluding [currentItemId].
     */
    fun isDuplicateItem(
        candidateNameMap: Map<String, String>,
        candidateCompanyIds: List<Long>,
        existingItems: List<ItemModel>,
        currentItemId: Long?
    ): Boolean {
        return existingItems.any { existing ->
            (currentItemId == null || currentItemId == 0L || existing.id != currentItemId) &&
                isCompanyOverlap(existing.companyIds, candidateCompanyIds) &&
                isDuplicateName(candidateNameMap, existing.name)
        }
    }

    /**
     * Returns true if a customer with the same name AND the same address already exists
     * in [existingCustomers] within an overlapping company scope, excluding [currentCustomerId].
     * Customers with the same name but different addresses are allowed.
     */
    fun isDuplicateCustomer(
        candidateNameMap: Map<String, String>,
        candidateAddress: Map<String, String> = emptyMap(),
        candidateCity: Map<String, String> = emptyMap(),
        candidateDistrict: Map<String, String> = emptyMap(),
        candidateState: Map<String, String> = emptyMap(),
        candidatePinCode: String = "",
        candidateCompanyIds: List<Long>,
        existingCustomers: List<CustomerModel>,
        currentCustomerId: Long?
    ): Boolean {
        return existingCustomers.any { existing ->
            (currentCustomerId == null || currentCustomerId == 0L || existing.id != currentCustomerId) &&
                isCompanyOverlap(existing.companyIds, candidateCompanyIds) &&
                isDuplicateName(candidateNameMap, existing.name) &&
                isSameCustomerAddress(
                    candidateAddress = candidateAddress,
                    candidateCity = candidateCity,
                    candidateDistrict = candidateDistrict,
                    candidateState = candidateState,
                    candidatePinCode = candidatePinCode,
                    existingCustomer = existing
                )
        }
    }
}
