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
    // "sjps001" and "sjps1" are the same bill number: leading zeros of a number are ignored when the query has none
    private val LEAD_ZEROS = Regex("(?<![0-9])0+(?=[0-9])")

    /** The text with the leading zeros of every number removed ("sjps010" -> "sjps10"). */
    fun looseDigits(stripped: String): String = LEAD_ZEROS.replace(stripped, "")

    /** Folded English-letter form of a text when it is Tamil, else nothing (used by the cached bill search text). */
    fun foldedLatinOfTamil(s: String): String = if (hasTamil(s)) foldSounds(latinOf(s)) else ""

    private fun hasTamil(s: String): Boolean = s.any { it in '\u0B80'..'\u0BFF' }

    private val latinCache = HashMap<String, String>()

    /** A Tamil text in English letters, without delimiters ("சிவராம்" -> "civaraam"), so "sivaram" can find it. */
    fun latinOf(tamil: String): String {
        if (latinCache.size > 4000) latinCache.clear()
        return latinCache.getOrPut(tamil) {
            stripDelimiters(com.elvan.udukkai.core.navil.NavilMozhimaatri.transliterate(tamil, false))
        }
    }

    // The sounds Tamil spells with one letter but English with two ("s/c/ch", "th/d", "k/g", "b/p"): a query is compared after the same folding
    internal fun foldSounds(s: String): String = s
        .replace("ch", "s").replace("c", "s").replace("z", "s").replace("th", "t").replace("dh", "t").replace("d", "t")
        .replace("g", "k").replace("b", "p").replace("aa", "a").replace("ee", "i").replace("oo", "u").replace("w", "v")
        .replace("ii", "i").replace("uu", "u")

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

        // 4. Numbers without their leading zeros ("sjps1" finds SJPS-001), and Tamil names by English letters ("sivaram")
        if (qStripped.isNotEmpty()) {
            val qLoose = looseDigits(qStripped)
            val useLoose = qLoose == qStripped && qStripped.any { it.isDigit() }
            val latinQuery = qStripped.all { it.code < 128 } && qStripped.any { it.isLetter() }
            val qFolded = if (latinQuery) foldSounds(qStripped) else ""
            for (i in list.indices) {
                val t = list[i]
                if (t.isNullOrBlank()) continue
                if (useLoose && looseDigits(strippedAt(i)).contains(qLoose)) return true
                if (latinQuery && hasTamil(t) && foldSounds(latinOf(t)).contains(qFolded)) return true
            }
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
        if (q.strippedTokens.size > 1 && q.strippedTokens.all { text.contains(it) }) return true
        val qLoose = looseDigits(q.stripped)
        if (qLoose == q.stripped && q.stripped.any { it.isDigit() } && inv.searchTextLoose.contains(qLoose)) return true
        return q.stripped.all { it.code < 128 } && q.stripped.any { it.isLetter() } && inv.searchTextLatin.contains(foldSounds(q.stripped))
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
    fun matchesReceipt(receipt: ReceiptModel, query: String, settledInvoiceNumbers: List<String> = emptyList()): Boolean {
        if (query.isBlank()) return true
        val targetList = ArrayList<String?>(12 + receipt.customerName.size + receipt.customerAddress.size + settledInvoiceNumbers.size)
        targetList.add(receipt.receiptNumber)
        // Everything the card shows is searchable: the bills it settled, how it was paid, the amount, the date and the year
        targetList.addAll(settledInvoiceNumbers)
        targetList.add(receipt.paymentMode)
        targetList.add(receipt.amount.toLong().toString())
        targetList.add(com.elvan.udukkai.core.utils.CurrencyUtils.formatInr(receipt.amount, showDecimals = receipt.amount % 1.0 != 0.0))
        targetList.add(com.elvan.udukkai.core.utils.DateUtils.formatEpochMillis(receipt.receiptDate))
        targetList.add(com.elvan.udukkai.core.finance.BillNumbering.fyShort(receipt.receiptDate))
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
