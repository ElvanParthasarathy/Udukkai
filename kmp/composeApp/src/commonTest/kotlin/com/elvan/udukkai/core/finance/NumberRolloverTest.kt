package com.elvan.udukkai.core.finance

import com.elvan.udukkai.core.utils.DateUtils
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/** Numbers count per financial year, so the first bill of April starts again at 001. */
class NumberRolloverTest {
    private fun at(s: String) = DateUtils.parseToEpochMillis(s)!!

    // The same rule the invoice repository uses: only numbers of the bill's own financial year are counted
    private fun nextNumber(existing: List<Pair<String, Long>>, base: String, date: Long): String {
        val fy = DateUtils.financialYearStart(date)
        val used = existing.filter { DateUtils.financialYearStart(it.second) == fy }.map { it.first }
        return BillNumbering.format(base, BillNumbering.nextSerial(used, base))
    }

    @Test
    fun marchAndAprilAreDifferentYears() {
        assertNotEquals(DateUtils.financialYearStart(at("31/03/2027")), DateUtils.financialYearStart(at("01/04/2027")))
    }

    @Test
    fun firstBillOfTheNewYearIs001EvenAfterManyInTheOld() {
        val old = (1..25).map { "SJPS-" + it.toString().padStart(3, '0') to at("15/03/2027") }
        assertEquals("SJPS-026", nextNumber(old, "SJPS", at("31/03/2027")))
        assertEquals("SJPS-001", nextNumber(old, "SJPS", at("01/04/2027")))
    }

    @Test
    fun numbersCountOnInsideTheNewYear() {
        val list = listOf("SJPS-001" to at("02/04/2027"), "SJPS-002" to at("10/04/2027"))
        assertEquals("SJPS-003", nextNumber(list, "SJPS", at("20/04/2027")))
    }

    @Test
    fun proformaSeriesAlsoRestarts() {
        val base = BillNumbering.proformaBase("SJPS")
        val list = listOf("$base-001" to at("01/02/2027"), "$base-002" to at("01/03/2027"))
        assertEquals("PF/SJPS-003", nextNumber(list, base, at("30/03/2027")))
        assertEquals("PF/SJPS-001", nextNumber(list, base, at("02/04/2027")))
    }
}
