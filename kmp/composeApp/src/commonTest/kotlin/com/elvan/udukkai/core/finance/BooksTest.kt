package com.elvan.udukkai.core.finance

import com.elvan.udukkai.core.platform.PreferencesHelper
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.data.model.InvoiceModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BooksTest {
    private class MemoryPrefs : PreferencesHelper {
        private val map = HashMap<String, Any?>()
        override fun getString(key: String, defaultValue: String?) = map[key] as String? ?: defaultValue
        override fun setString(key: String, value: String?) { map[key] = value }
        override fun getBoolean(key: String, defaultValue: Boolean) = map[key] as Boolean? ?: defaultValue
        override fun setBoolean(key: String, value: Boolean) { map[key] = value }
        override fun getInt(key: String, defaultValue: Int) = map[key] as Int? ?: defaultValue
        override fun setInt(key: String, value: Int) { map[key] = value }
        override fun clear() = map.clear()
    }

    private fun day(s: String) = DateUtils.parseToEpochMillis(s)!!

    private fun bill(number: String, date: String, total: Double = 100.0, id: Long = 1L) =
        InvoiceModel(id = id, invoiceNumber = number, invoiceDate = day(date), totalAmount = total)

    @Test
    fun the_lock_covers_the_whole_financial_year_and_nothing_after() {
        val prefs = MemoryPrefs()
        val books = BooksSettings("t") { prefs }
        assertFalse(books.isLocked(1L, day("15/01/2026")))
        books.lockThrough(1L, 2025)
        assertTrue(books.isLocked(1L, day("01/04/2025")))
        assertTrue(books.isLocked(1L, day("31/03/2026")))
        assertFalse(books.isLocked(1L, day("01/04/2026")))
        assertFalse(books.isLocked(2L, day("15/01/2026")), "another business is not locked")
        books.unlock(1L)
        assertFalse(books.isLocked(1L, day("15/01/2026")))
    }

    @Test
    fun the_series_restarts_each_april() {
        val prefix = BillNumbering.seriesPrefix("VRM", day("31/03/2026"))
        assertEquals("VRM/25-26/", prefix)
        assertEquals("VRM/26-27/", BillNumbering.seriesPrefix("VRM", day("01/04/2026")))
        assertEquals(1, BillNumbering.nextSerial(listOf("VRM/25-26/040"), "VRM/26-27/"))
        assertEquals(41, BillNumbering.nextSerial(listOf("VRM/25-26/040", "vrm/25-26/007"), prefix))
        assertEquals("VRM/26-27/001", BillNumbering.format("VRM/26-27/", 1))
        assertEquals("VRM/26-27/1000", BillNumbering.format("VRM/26-27/", 1000), "past 999 the number just grows")
        assertTrue(BillRulesCheck.ok(BillNumbering.format("ABCDE/26-27/", 999)), "16 characters is the limit")
    }

    @Test
    fun years_are_split_at_april_and_listed_newest_first() {
        val s = BillNumbering.summaries(
            listOf(bill("A-1", "31/03/2026", 10.0, 1), bill("A-2", "01/04/2026", 20.0, 2), bill("A-3", "02/04/2026", 30.0, 3)),
        )
        assertEquals(listOf(2026, 2025), s.map { it.fyStart })
        assertEquals(50.0, s[0].total)
    }

    @Test
    fun old_bills_get_a_yearly_number_oldest_first_and_new_ones_are_left_alone() {
        val live = listOf(
            bill("SJPS-30", "10/04/2026", id = 2),
            bill("SJPS-29", "05/04/2026", id = 1),
            bill("SJPS-20", "20/03/2026", id = 3),
            bill("SJPS/26-27/001", "01/05/2026", id = 4),
            InvoiceModel(id = 5, invoiceNumber = "EST-1", invoiceType = "proforma", invoiceDate = day("02/05/2026")),
        )
        val plan = BillNumbering.renumberPlan(live, listOf("SJPS/26-27/002"), "SJPS").associate { it.first.id to it.second }
        assertEquals(mapOf(3L to "SJPS/25-26/001", 1L to "SJPS/26-27/003", 2L to "SJPS/26-27/004", 5L to "EST/26-27/001"), plan)
    }

    private object BillRulesCheck {
        fun ok(n: String) = com.elvan.udukkai.core.utils.BillRules.isValidInvoiceNumber(n)
    }
}
