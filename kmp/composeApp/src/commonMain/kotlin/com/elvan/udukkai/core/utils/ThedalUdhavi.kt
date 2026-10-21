package com.elvan.udukkai.core.utils

import com.elvan.udukkai.data.model.PattiyalTharavuru
import com.elvan.udukkai.data.model.PatrugalTharavuru
import com.elvan.udukkai.data.model.PorulTharavuru
import com.elvan.udukkai.data.model.VaangunarTharavuru

/**
 * Universal high-power search matching utility (தேடல் உதவி).
 * Supports fuzzy tokenization, punctuation/delimiter-insensitivity (e.g. "pvs 23" matching "PVS-23"),
 * multi-token cross-field search, and multi-language support (Pure Tamil & English).
 */
object ThedalUdhavi {
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
    fun matchesInvoice(inv: PattiyalTharavuru, query: String): Boolean {
        if (query.isBlank()) return true
        val targetList = ArrayList<String?>(8 + inv.vaangunarPeyar.size + inv.vaangunarMunvari.size)
        targetList.add(inv.patrucheettuEn)
        targetList.addAll(inv.vaangunarPeyar.values)
        targetList.addAll(inv.vaangunarMunvari.values)
        if (inv.tharavugal.isNotBlank() && inv.tharavugal != "[]") {
            targetList.add(inv.tharavugal)
        }
        return matchesRecord(targetList, query)
    }

    /**
     * High-power receipt matcher.
     */
    fun matchesReceipt(receipt: PatrugalTharavuru, query: String): Boolean {
        if (query.isBlank()) return true
        val targetList = ArrayList<String?>(6 + receipt.vaangunarPeyar.size + receipt.vaangunarMunvari.size)
        targetList.add(receipt.patruEn)
        targetList.addAll(receipt.vaangunarPeyar.values)
        targetList.addAll(receipt.vaangunarMunvari.values)
        if (receipt.ullkurippu.isNotBlank()) targetList.add(receipt.ullkurippu)
        if (!receipt.vangiPeyar.isNullOrBlank()) targetList.add(receipt.vangiPeyar)
        if (!receipt.parivarthanaiEn.isNullOrBlank()) targetList.add(receipt.parivarthanaiEn)
        return matchesRecord(targetList, query)
    }

    /**
     * High-power product matcher.
     */
    fun matchesProduct(item: PorulTharavuru, query: String): Boolean {
        if (query.isBlank()) return true
        val targetList = ArrayList<String?>(4 + item.porulPeyar.size)
        targetList.add(item.hsnCode)
        targetList.addAll(item.porulPeyar.values)
        return matchesRecord(targetList, query)
    }

    /**
     * High-power customer/merchant matcher.
     */
    fun matchesCustomer(customer: VaangunarTharavuru, query: String): Boolean {
        if (query.isBlank()) return true
        val targetList = ArrayList<String?>(16)
        targetList.addAll(customer.peyar.values)
        targetList.addAll(customer.oor.values)
        targetList.addAll(customer.mugavari.values)
        targetList.addAll(customer.maavattam.values)
        targetList.addAll(customer.maanilam.values)
        if (customer.gstin.isNotBlank()) targetList.add(customer.gstin)
        if (customer.tholaipaesi.isNotBlank()) targetList.add(customer.tholaipaesi)
        if (customer.minnanjal.isNotBlank()) targetList.add(customer.minnanjal)
        if (customer.anjalKuriyeedu.isNotBlank()) targetList.add(customer.anjalKuriyeedu)
        return matchesRecord(targetList, query)
    }
}

typealias SearchUtils = ThedalUdhavi
