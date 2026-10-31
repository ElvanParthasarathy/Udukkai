package com.elvan.udukkai.core.utils

import com.elvan.udukkai.data.model.InvoiceModel
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * How the bills list behaves as it grows: every step the list screen runs over ALL bills (filter, search, group by
 * date) is timed at 1k, 5k and 20k bills. The numbers are printed; the asserts only guard against a step becoming
 * unusably slow (a phone is several times slower than the machine running this).
 */
class ListScaleBenchmarkTest {
    private val names = listOf(
        "சுந்தரி சில்க்ஸ் இந்தியா" to "Sundari Silks India",
        "ஸ்ரீ ஜெய்பிரியா சில்க்ஸ்" to "Sri Jaipriya Silks",
        "வி.ஆர்.எம். பட்டு முறுக்கு ஆலை" to "V.R.M. Silk Twisting Factory",
        "ஸ்ரீ சிவராம் சில்க் சாரீஸ்" to "Sri Sivaram Silk Sarees",
    )

    private fun line(i: Int) =
        """{"itemId": "17933638608${i % 90}", "itemName": "இரண்டிழைச் சப்புரிப் பணிக்கட்டணம்", "itemNameEn": "2-Ply Sappuri Job Work Charges", "weight": ${20 + i % 9}.3, "edai": 26.3, "price": 610.0, "vilai": 610.0, "languageMap": {"ta": "இரண்டிழைச் சப்புரிப் பணிக்கட்டணம்", "en": "2-Ply Sappuri Job Work Charges"}}"""

    private fun bills(n: Int): List<InvoiceModel> {
        val day = 86_400_000L
        val start = 1_760_000_000_000L
        return List(n) { i ->
            val (ta, en) = names[i % names.size]
            InvoiceModel(
                id = 1_000L + i,
                companyId = 1L,
                invoiceNumber = "VRM-${i + 1}",
                customerName = mapOf("ta" to ta, "en" to en),
                customerAddress = mapOf("ta" to "திருச்சேறை, கும்பகோணம்", "en" to "Thiruchcherai, Kumbakonam"),
                invoiceDate = start + (i / 3) * day,
                itemsJson = "[" + (0..(2 + i % 6)).joinToString(",") { line(i + it) } + "]",
                totalAmount = 10_000.0 + i,
            )
        }
    }

    private inline fun <T> time(label: String, repeat: Int = 3, block: () -> T): Long {
        block() // warm up
        var best = Long.MAX_VALUE
        repeat(repeat) {
            val t = System.nanoTime()
            block()
            best = minOf(best, (System.nanoTime() - t) / 1_000_000)
        }
        println("   %-44s %6d ms".format(label, best))
        return best
    }

    @Test
    fun bills_list_steps_at_scale() {
        for (n in listOf(1_000, 5_000, 20_000)) {
            val all = bills(n)
            println("== $n bills (${all.sumOf { it.itemsJson.length } / 1024} KB of line items)")
            // The first search after the bills are loaded: every bill builds its search text once
            val coldStart = System.nanoTime()
            all.filter { SearchHelper.matchesInvoice(it, "zzzz") }
            val cold = (System.nanoTime() - coldStart) / 1_000_000
            println("   %-44s %6d ms".format("FIRST search after load (builds the text)", cold))
            val company = time("filter by business") { all.filter { it.companyId == 1L } }
            val grouped = time("group by date") { DateGroupUtils.groupItemsByDate(all) { it.invoiceDate } }
            time("filter by date range") { val a = all[n / 2].invoiceDate; all.filter { it.invoiceDate in a..(a + 90 * 86_400_000L) } }
            val hit = time("search: bill number prefix (VRM-1)") { all.filter { SearchHelper.matchesInvoice(it, "VRM-1") } }
            val ta = time("search: Tamil customer word") { all.filter { SearchHelper.matchesInvoice(it, "சுந்தரி") } }
            val miss = time("search: no match at all (worst case)") { all.filter { SearchHelper.matchesInvoice(it, "zzzz") } }
            val multi = time("search: two words") { all.filter { SearchHelper.matchesInvoice(it, "sundari 17") } }
            if (n == 20_000) {
                assertTrue(company < 500 && grouped < 1_500, "list preparation too slow at 20k")
                println("   worst warm search at 20k: ${maxOf(hit, ta, miss, multi)} ms, first search $cold ms")
            }
        }
    }
}
