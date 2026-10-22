package com.elvan.udukkai.core.utils

import com.elvan.udukkai.data.model.CustomerModel
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.model.ItemModel
import com.elvan.udukkai.data.model.ReceiptModel

/**
 * Universal high-power search matching utility.
 * Supports fuzzy tokenization, punctuation/delimiter-insensitivity (e.g. "pvs 23" matching "PVS-23"),
 * multi-token cross-field search, and multi-language support (Pure Tamil & English).
 */
object SearchHelper {
    private val DELIMS_REGEX = Regex("""[\s\-_—–./\\#@,;:|()\[\]{}+*"'`~^=<>!%&?$]+""")
    private val TOKEN_SPLIT_REGEX = Regex("""[\s,./\-_—–|#@;:()\[\]{}]+""")

    /**
     * Strips whitespace and all delimiter/punctuation characters while preserving
     * alphanumeric and Unicode script characters (including all Tamil letters and diacritics).
     * e.g. "PVS-23" -> "pvs23", "pvs 23" -> "pvs23", "RCP/SJPS/04" -> "rcpsjps04".
     */
    fun stripDelimiters(text: String?): String {
        if (text.isNullOrBlank()) return ""
        return DELIMS_REGEX.replace(text, "").lowercase()
    }

    /**
     * Matches a single target string against a query.
     * 1. Blank query matches everything.
     * 2. Direct case-insensitive substring match.
     * 3. Delimiter-stripped match (e.g. "pvs 23" matches "PVS-23", "pvs23" matches "PVS-23").
     */
    fun matchesSingle(target: String?, query: String): Boolean {
        val qTrim = query.trim()
        if (qTrim.isEmpty()) return true
        if (target.isNullOrBlank()) return false

        // 1. Direct case-insensitive contains
        if (target.contains(qTrim, ignoreCase = true)) return true

        // 2. Delimiter-stripped match
        val qStripped = stripDelimiters(qTrim)
        if (qStripped.isNotEmpty()) {
            val tStripped = stripDelimiters(target)
            if (tStripped.contains(qStripped)) return true
        }

        return false
    }

    /**
     * Matches an entity record containing multiple target text fields against a query.
     * High-powered features:
     * 1. Any single field directly matches the query.
     * 2. Delimiter-stripped match across combined fields.
     * 3. Token-based multi-word search (e.g. "sri silk", "sivaram 23", "pvs 23"):
     *    Every token in the query must match at least one field in the record.
     */
    fun matchesRecord(targets: Iterable<String?>, query: String): Boolean {
        val qTrim = query.trim()
        if (qTrim.isEmpty()) return true

        // 1. Check if any single field matches the full query directly or delimiter-stripped
        for (t in targets) {
            if (matchesSingle(t, qTrim)) return true
        }

        // 2. Delimiter-stripped match across combined target strings
        val qStripped = stripDelimiters(qTrim)
        if (qStripped.isNotEmpty()) {
            val combinedStripped = buildString {
                for (t in targets) {
                    if (!t.isNullOrBlank()) {
                        append(stripDelimiters(t))
                    }
                }
            }
            if (combinedStripped.contains(qStripped)) return true
        }

        // 3. Multi-token / tokenized search
        // Split query into tokens (e.g. "pvs 23" -> ["pvs", "23"])
        val tokens = qTrim.split(TOKEN_SPLIT_REGEX).filter { it.isNotBlank() }
        if (tokens.size > 1) {
            val allTokensMatch = tokens.all { token ->
                targets.any { target -> matchesSingle(target, token) }
            }
            if (allTokensMatch) return true
        }

        return false
    }

    fun matchesRecord(vararg targets: String?, query: String): Boolean {
        return matchesRecord(targets.asIterable(), query)
    }

    /**
     * High-power invoice matcher.
     * Matches against invoice number (e.g. "PVS-23", "SJPS-32"), customer names (Ta, En),
     * customer locations/addresses, and items content.
     */
    fun matchesInvoice(inv: InvoiceModel, query: String): Boolean {
        if (query.isBlank()) return true
        val targetList = ArrayList<String?>(8 + inv.customerName.size + inv.customerAddress.size)
        targetList.add(inv.invoiceNumber)
        targetList.addAll(inv.customerName.values)
        targetList.addAll(inv.customerAddress.values)
        if (inv.itemsJson.isNotBlank() && inv.itemsJson != "[]") {
            targetList.add(inv.itemsJson)
        }
        return matchesRecord(targetList, query)
    }

    /**
     * High-power receipt matcher.
     */
    fun matchesReceipt(receipt: ReceiptModel, query: String): Boolean {
        if (query.isBlank()) return true
        val targetList = ArrayList<String?>(6 + receipt.customerName.size + receipt.customerAddress.size)
        targetList.add(receipt.receiptNumber)
        targetList.addAll(receipt.customerName.values)
        targetList.addAll(receipt.customerAddress.values)
        if (receipt.notes.isNotBlank()) targetList.add(receipt.notes)
        if (!receipt.bankName.isNullOrBlank()) targetList.add(receipt.bankName)
        if (!receipt.transactionNumber.isNullOrBlank()) targetList.add(receipt.transactionNumber)
        return matchesRecord(targetList, query)
    }

    /**
     * High-power item matcher.
     */
    fun matchesItem(item: ItemModel, query: String): Boolean {
        if (query.isBlank()) return true
        val targetList = ArrayList<String?>(4 + item.name.size)
        targetList.add(item.hsnCode)
        targetList.addAll(item.name.values)
        return matchesRecord(targetList, query)
    }

    /**
     * High-power customer/merchant matcher.
     */
    fun matchesCustomer(customer: CustomerModel, query: String): Boolean {
        if (query.isBlank()) return true
        val targetList = ArrayList<String?>(16)
        targetList.addAll(customer.name.values)
        targetList.addAll(customer.city.values)
        targetList.addAll(customer.address.values)
        targetList.addAll(customer.district.values)
        targetList.addAll(customer.state.values)
        if (customer.gstin.isNotBlank()) targetList.add(customer.gstin)
        if (customer.phone.isNotBlank()) targetList.add(customer.phone)
        if (customer.email.isNotBlank()) targetList.add(customer.email)
        if (customer.pinCode.isNotBlank()) targetList.add(customer.pinCode)
        return matchesRecord(targetList, query)
    }
}

typealias SearchUtils = SearchHelper
