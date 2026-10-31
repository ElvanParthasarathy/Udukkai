package com.elvan.udukkai.core.utils

import com.elvan.udukkai.data.model.InvoiceModel
import kotlin.test.Test
import kotlin.test.assertEquals

/** The faster search must find exactly what the earlier regex-based search found. This keeps a copy of that search. */
class SearchEquivalenceTest {
    private object Reference {
        private val DELIMS_REGEX = Regex("""[\s\-_—–./\\#@,;:|()\[\]{}+*"'`~^=<>!%&?$]+""")
        private val TOKEN_SPLIT_REGEX = Regex("""[\s,./\-_—–|#@;:()\[\]{}]+""")

        fun strip(text: String?): String = if (text.isNullOrBlank()) "" else DELIMS_REGEX.replace(text, "").lowercase()

        fun single(target: String?, query: String): Boolean {
            val q = query.trim()
            if (q.isEmpty()) return true
            if (target.isNullOrBlank()) return false
            if (target.contains(q, ignoreCase = true)) return true
            val qs = strip(q)
            return qs.isNotEmpty() && strip(target).contains(qs)
        }

        fun record(targets: Iterable<String?>, query: String): Boolean {
            val q = query.trim()
            if (q.isEmpty()) return true
            for (t in targets) if (single(t, q)) return true
            val qs = strip(q)
            if (qs.isNotEmpty()) {
                val combined = buildString { for (t in targets) if (!t.isNullOrBlank()) append(strip(t)) }
                if (combined.contains(qs)) return true
            }
            val tokens = q.split(TOKEN_SPLIT_REGEX).filter { it.isNotBlank() }
            if (tokens.size > 1 && tokens.all { token -> targets.any { single(it, token) } }) return true
            return false
        }

        fun invoice(inv: InvoiceModel, query: String): Boolean {
            if (query.isBlank()) return true
            val list = ArrayList<String?>()
            list.add(inv.invoiceNumber)
            list.addAll(inv.customerName.values)
            list.addAll(inv.customerAddress.values)
            if (inv.itemsJson.isNotBlank() && inv.itemsJson != "[]") list.add(inv.itemsJson)
            return record(list, query)
        }
    }

    private val customers = listOf(
        "சுந்தரி சில்க்ஸ் இந்தியா" to "Sundari Silks India",
        "ஸ்ரீ ஜெய்பிரியா சில்க்ஸ்" to "Sri Jaipriya Silks",
        "வி.ஆர்.எம். பட்டு முறுக்கு ஆலை" to "V.R.M. Silk Twisting Factory",
        "P.V.S. சில்க்" to "P.V.S. Silk (Arani)",
    )

    private fun bills() = List(400) { i ->
        val (ta, en) = customers[i % customers.size]
        InvoiceModel(
            id = 1L + i,
            invoiceNumber = listOf("VRM-", "PVS/", "SJPS ", "INV_")[i % 4] + (i + 1),
            customerName = mapOf("ta" to ta, "en" to en),
            customerAddress = mapOf("ta" to "திருச்சேறை, கும்பகோணம்", "en" to "Thiruchcherai, Kumbakonam"),
            itemsJson = if (i % 7 == 0) "[]" else
                """[{"itemName": "இரண்டிழைச் சப்புரிப் பணிக்கட்டணம்", "itemNameEn": "2-Ply Sappuri Job Work Charges", "weight": ${20 + i % 9}.3, "price": 610.0}]""",
        )
    }

    private val queries = listOf(
        "VRM-1", "vrm1", "vrm 1", "VRM 12", "pvs/2", "pvs 2", "pvs2", "sjps 10", "INV_5", "inv5", "sundari", "Sundari Silks",
        "sundari 17", "silk twisting", "v.r.m", "vrm silk", "p.v.s", "pvs (arani)", "சுந்தரி", "சில்க்ஸ் இந்தியா", "ஜெய்பிரியா 5",
        "கும்பகோணம்", "kumbakonam", "thiruchcherai kumb", "2-ply", "2 ply", "2ply sappuri", "பணிக்கட்டணம்", "26.3", "weight", "price",
        "610", "610.0", "[]", "{", "-", "/", ".", "  ", "---", "+", "$", "a - b", "x+y", "zzzz", "VRM-99999", "வி.ஆர்.எம்", "v r m",
        "SUNDARI  silks", "  vrm-3 ", "INV_ 5", "sri sri", "silk silk", "arani", "ARANI SILK", "2-Ply  Sappuri", "job work 22",
    )

    @Test
    fun the_faster_search_finds_exactly_what_the_old_one_found() {
        val all = bills()
        val problems = ArrayList<String>()
        for (q in queries) {
            for (inv in all) {
                val old = Reference.invoice(inv, q)
                val new = SearchHelper.matchesInvoice(inv, q)
                if (old != new) problems.add("'$q' on ${inv.invoiceNumber}: old=$old new=$new")
            }
        }
        assertEquals(emptyList(), problems.take(10), "${problems.size} differences")
    }

    @Test
    fun the_record_matcher_is_unchanged_too() {
        val fields = listOf("RCP/SJPS/04", "சுந்தரி சில்க்ஸ்", "Sundari Silks", "Cash, UPI", "Part payment (Oct)", null, "", "  ")
        val problems = ArrayList<String>()
        for (q in queries) {
            val old = Reference.record(fields, q)
            val new = SearchHelper.matchesRecord(fields, q)
            if (old != new) problems.add("'$q': old=$old new=$new")
        }
        assertEquals(emptyList(), problems, "record matcher differs")
    }
}
