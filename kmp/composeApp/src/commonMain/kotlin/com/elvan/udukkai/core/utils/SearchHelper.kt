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
        val out = StringBuilder(text.length)
        for (c in text) if (!isDelimiter(c)) out.append(c)
        return out.toString().lowercase()
    }

    // The characters of DELIMS_REGEX, as a table for the ASCII range (a regex per field per keystroke was the cost)
    private val DELIM_TABLE = BooleanArray(128).also { t ->
        " \t\n\u000B\u000C\r-_./\\#@,;:|()[]{}+*\"'`~^=<>!%&?\$".forEach { t[it.code] = true }
    }

    private fun isDelimiter(c: Char): Boolean =
        if (c.code < 128) DELIM_TABLE[c.code] else c == '\u2014' || c == '\u2013'

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
        val list = targets as? List<String?> ?: targets.toList()
        val qStripped = stripDelimiters(qTrim)
        val stripped = arrayOfNulls<String>(list.size)
        fun strippedAt(i: Int): String = stripped[i] ?: stripDelimiters(list[i]).also { stripped[i] = it }

        // 1. Any single field matches the full query directly or delimiter-stripped
        for (i in list.indices) {
            val t = list[i]
            if (t.isNullOrBlank()) continue
            if (t.contains(qTrim, ignoreCase = true)) return true
            if (qStripped.isNotEmpty() && strippedAt(i).contains(qStripped)) return true
        }

        // 2. Delimiter-stripped match across the combined fields
        if (qStripped.isNotEmpty()) {
            val combinedStripped = buildString { for (i in list.indices) append(strippedAt(i)) }
            if (combinedStripped.contains(qStripped)) return true
        }

        // 3. Multi-token search ("pvs 23"): every token must match at least one field
        val tokens = qTrim.split(TOKEN_SPLIT_REGEX).filter { it.isNotBlank() }
        if (tokens.size > 1) {
            val allTokensMatch = tokens.all { token ->
                val tokenStripped = stripDelimiters(token)
                list.indices.any { i ->
                    val t = list[i]
                    !t.isNullOrBlank() && (t.contains(token, ignoreCase = true) ||
                        (tokenStripped.isNotEmpty() && strippedAt(i).contains(tokenStripped)))
                }
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
        val q = prepared(query) ?: return true
        // A query made only of delimiters ("-", "/") cannot use the stripped text: the plain path decides it
        if (q.stripped.isEmpty() || q.strippedTokens.any { it.isEmpty() }) return matchesInvoicePlain(inv, q.text)
        val text = inv.searchText
        if (text.contains(q.stripped)) return true
        return q.strippedTokens.size > 1 && q.strippedTokens.all { text.contains(it) }
    }

    private fun matchesInvoicePlain(inv: InvoiceModel, query: String): Boolean {
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

    private class PreparedQuery(val text: String, val stripped: String, val strippedTokens: List<String>)

    private var lastPrepared: PreparedQuery? = null

    // The query is the same for every bill of one search, so it is cleaned and split once, not once per bill
    private fun prepared(query: String): PreparedQuery? {
        val q = query.trim()
        if (q.isEmpty()) return null
        lastPrepared?.let { if (it.text == q) return it }
        val tokens = q.split(TOKEN_SPLIT_REGEX).filter { it.isNotBlank() }
        return PreparedQuery(q, stripDelimiters(q), if (tokens.size > 1) tokens.map { stripDelimiters(it) } else emptyList())
            .also { lastPrepared = it }
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
