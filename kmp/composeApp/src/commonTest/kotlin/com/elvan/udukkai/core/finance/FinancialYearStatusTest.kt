package com.elvan.udukkai.core.finance

import com.elvan.udukkai.core.utils.DateUtils
import kotlin.test.Test
import kotlin.test.assertEquals

class FinancialYearStatusTest {
    private fun at(s: String) = DateUtils.parseToEpochMillis(s)!!

    @Test
    fun midYear() {
        val s = FinancialYearStatus.of(at("07/11/2026"))
        assertEquals("2026-27", s.label); assertEquals("2027-28", s.nextLabel)
        assertEquals(145, s.daysLeft) // 7 Nov to 1 Apr
        assertEquals("2025-26", s.previousLabel) // still changeable: it closes on 1 Dec 2026
        assertEquals(24, s.previousClosesInDays)
    }

    @Test
    fun previousYearStaysChangeableUntilFirstDecember() {
        val s = FinancialYearStatus.of(at("20/06/2026"))
        assertEquals("2025-26", s.previousLabel)
        assertEquals(164, s.previousClosesInDays) // 20 Jun to 1 Dec
    }

    @Test
    fun lastDayAndFirstDayOfTheYear() {
        assertEquals(1, FinancialYearStatus.of(at("31/03/2027")).daysLeft)
        val first = FinancialYearStatus.of(at("01/04/2027"))
        assertEquals("2027-28", first.label)
        assertEquals("2026-27", first.previousLabel)
    }
}

class FinancialYearStatusLateTest {
    @Test
    fun afterDecemberThePreviousYearIsClosed() {
        val s = FinancialYearStatus.of(DateUtils.parseToEpochMillis("05/12/2026")!!)
        assertEquals(null, s.previousLabel)
    }
}
