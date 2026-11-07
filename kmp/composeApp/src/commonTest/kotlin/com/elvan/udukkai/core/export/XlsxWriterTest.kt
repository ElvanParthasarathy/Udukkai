package com.elvan.udukkai.core.export

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class XlsxWriterTest {
    @Test
    fun crcOfKnownString() {
        assertEquals(0xCBF43926.toInt(), Zip.crc32("123456789".encodeToByteArray()))
    }

    @Test
    fun workbookIsAZipWithTheSixParts() {
        val bytes = SalesReportExcel.build(
            "Sri Jaipriya Silks", "Sales Report - 2026-11",
            listOf(SalesRow("SJPS-001", "Client", "07/11/2026", 1000.0, 50.0, 1050.0)), withTax = true
        )
        assertEquals(0x50, bytes[0].toInt())
        assertEquals(0x4B, bytes[1].toInt())
        val text = bytes.decodeToString()
        listOf("[Content_Types].xml", "xl/workbook.xml", "xl/styles.xml", "xl/worksheets/sheet1.xml").forEach { assertTrue(text.contains(it), it) }
        assertTrue(text.contains("SJPS-001"))
        assertTrue(text.contains("<v>1050</v>"))
    }

    @Test
    fun columnNames() {
        assertEquals("A", Xlsx.columnName(0))
        assertEquals("Z", Xlsx.columnName(25))
        assertEquals("AA", Xlsx.columnName(26))
    }

    @Test
    fun writeSampleForExternalCheck() {
        // Left in the temp folder so another tool can open it during development
        val bytes = SalesReportExcel.build(
            "Biz\nவணிகம்", "Sales Report - test",
            listOf(SalesRow("A-1", "Name\nநாம்", "01/04/2026", 10.5, 0.5, 11.0), SalesRow("A-2", "N2", "02/04/2026", 20.0, 1.0, 21.0)), true
        )
        assertTrue(bytes.size > 1000)
        System.getProperty("java.io.tmpdir")?.let { java.io.File(it, "udukkai_sample.xlsx").writeBytes(bytes) }
    }
}
