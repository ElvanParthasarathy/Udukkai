package com.elvan.udukkai.core.finance

import kotlin.test.Test
import kotlin.test.assertEquals

class ProformaNumberTest {
    @Test
    fun proformaSeriesIsPfSlashShortNameAndCountsFrom001() {
        val base = BillNumbering.proformaBase("SJPS")
        assertEquals("PF/SJPS", base)
        assertEquals("PF/SJPS-001", BillNumbering.format(base, BillNumbering.nextSerial(emptyList(), base)))
    }

    @Test
    fun proformaAndTaxInvoiceCountersAreSeparate() {
        val numbers = listOf("SJPS-001", "SJPS-002", "PF/SJPS-001", "PF/SJPS-002", "PF/SJPS-003")
        assertEquals(4, BillNumbering.nextSerial(numbers, BillNumbering.proformaBase("SJPS")))
        assertEquals(3, BillNumbering.nextSerial(numbers, "SJPS"))
    }

    @Test
    fun missingShortNameFallsBackToInv() {
        assertEquals("PF/INV", BillNumbering.proformaBase("  "))
    }
}
