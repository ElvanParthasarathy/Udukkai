package com.elvan.udukkai.core.finance

import com.elvan.udukkai.core.utils.DateUtils
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Four financial years of bills: which year a date falls in, numbering per year, and what is locked. */
class FinancialYearsTest {
    private fun day(s: String) = DateUtils.parseToEpochMillis(s)!!

    @Test
    fun year_boundaries_run_april_to_march() {
        assertEquals(2022, DateUtils.financialYearStart(day("31/03/2023")))
        assertEquals(2023, DateUtils.financialYearStart(day("01/04/2023")))
        assertEquals(2023, DateUtils.financialYearStart(day("31/12/2023")))
        assertEquals(2023, DateUtils.financialYearStart(day("29/02/2024")))
        assertEquals("2023-24", DateUtils.financialYearLabel(day("15/08/2023")))
        assertEquals("23-24", BillNumbering.fyShort(day("15/08/2023")))
        assertEquals("99-00", BillNumbering.fyShort(day("10/10/2099")))
    }

    @Test
    fun numbering_restarts_in_each_year_and_never_reuses_a_number() {
        val years = listOf("10/05/2022", "10/05/2023", "10/05/2024", "10/05/2025")
        val byYear = years.associateWith { mutableListOf<String>() }
        for (date in years) {
            repeat(3) {
                val used = byYear.getValue(date)
                used += BillNumbering.format("VRM", BillNumbering.nextSerial(used, "VRM"))
            }
        }
        for ((_, numbers) in byYear) assertEquals(listOf("VRM-001", "VRM-002", "VRM-003"), numbers)
        // A deleted bill stays in the list the repository passes in, so its number is not given out again
        assertEquals("VRM-004", BillNumbering.format("VRM", BillNumbering.nextSerial(listOf("VRM-001", "VRM-003"), "VRM")))
        assertEquals(1, BillNumbering.nextSerial(listOf("OTHER-005"), "VRM"))
    }

    @Test
    fun only_finished_years_past_the_first_of_december_are_locked() {
        val now = day("15/01/2026")
        assertTrue(YearLock.isLocked(day("20/06/2022"), now))
        assertTrue(YearLock.isLocked(day("20/06/2024"), now))
        assertTrue(YearLock.isLocked(day("31/03/2025"), now))
        assertFalse(YearLock.isLocked(day("01/04/2025"), now))
        assertFalse(YearLock.isLocked(day("20/12/2025"), now))
    }
}
