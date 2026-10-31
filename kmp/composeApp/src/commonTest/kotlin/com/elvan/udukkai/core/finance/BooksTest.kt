package com.elvan.udukkai.core.finance

import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.data.model.InvoiceModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BooksTest {
    private fun day(s: String) = DateUtils.parseToEpochMillis(s)!!

    private fun bill(number: String, date: String, total: Double = 100.0, id: Long = 1L) =
        InvoiceModel(id = id, invoiceNumber = number, invoiceDate = day(date), totalAmount = total)

    @Test
    fun a_financial_year_closes_by_itself_on_the_first_of_december_after_it_ends() {
        val bill = day("15/01/2026") // financial year 2025-26
        assertFalse(YearLock.isLocked(bill, now = day("30/11/2026")), "still open until the credit note and return deadline")
        assertTrue(YearLock.isLocked(bill, now = day("01/12/2026")))
        assertFalse(YearLock.isLocked(day("01/04/2026"), now = day("15/01/2027")), "the next year is not closed yet")
        assertTrue(YearLock.isLocked(day("31/03/2026"), now = day("15/01/2027")))
    }

    @Test
    fun a_closed_year_can_be_opened_for_the_session() {
        val bill = day("10/05/2020")
        assertTrue(YearLock.isLocked(bill, now = day("01/01/2030")))
        YearLock.openForSession(bill)
        assertFalse(YearLock.isLocked(bill, now = day("01/01/2030")))
        assertTrue(YearLock.isLocked(day("10/05/2021"), now = day("01/01/2030")), "only that year is opened")
    }

    @Test
    fun numbers_are_short_and_count_from_001() {
        assertEquals("VRM-001", BillNumbering.format("VRM", 1))
        assertEquals("VRM-1000", BillNumbering.format("VRM", 1000), "past 999 the number just grows")
        assertEquals(1, BillNumbering.nextSerial(emptyList(), "VRM"))
        assertEquals(41, BillNumbering.nextSerial(listOf("VRM-040", "vrm-007", "PVS-099"), "VRM"))
        assertTrue(BillRulesCheck.ok(BillNumbering.format("ABCDEFGHIJKL", 999)), "16 characters is the limit")
    }

    private object BillRulesCheck {
        fun ok(n: String) = com.elvan.udukkai.core.utils.BillRules.isValidInvoiceNumber(n)
    }
}
