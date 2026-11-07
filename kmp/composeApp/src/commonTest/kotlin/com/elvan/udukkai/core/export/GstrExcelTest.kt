package com.elvan.udukkai.core.export

import com.elvan.udukkai.textiles.ui.screens.editor.TextilesCalculation
import com.elvan.udukkai.textiles.ui.screens.editor.TextilesItem
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GstrExcelTest {
    private fun near(a: Double, b: Double) = assertTrue(abs(a - b) < 0.005, "$a != $b")

    @Test
    fun unitCodes() {
        assertEquals("NOS", GstrExcel.uqc("Nos"))
        assertEquals("KGS", GstrExcel.uqc("kg"))
        assertEquals("MTR", GstrExcel.uqc("Meter"))
        assertEquals("OTH", GstrExcel.uqc("bundle"))
    }

    @Test
    fun itemTaxLinesAddUpToTheBillTotals() {
        val items = listOf(
            TextilesItem(itemName = "a", price = 1000.0, quantity = 2.0, taxRate = 5.0, discount = 10.0, discountType = "%"),
            TextilesItem(itemName = "b", price = 2500.0, quantity = 1.0, taxRate = 12.0)
        )
        val totals = TextilesCalculation.calculate(items, globalDiscountValue = 5.0, globalDiscountType = "%", businessState = "TN", customerState = "TN")
        val lines = TextilesCalculation.itemTaxLines(items, 5.0, "%", interState = false, taxInclusive = false)
        near(lines.sumOf { it.cgst + it.sgst }, totals.totalTax)
        near(lines.sumOf { it.taxable } + totals.totalTax, totals.grandTotal - totals.roundOff)
    }

    @Test
    fun interStateLinesAreAllIgst() {
        val items = listOf(TextilesItem(itemName = "a", price = 1000.0, quantity = 1.0, taxRate = 18.0))
        val l = TextilesCalculation.itemTaxLines(items, 0.0, "%", interState = true, taxInclusive = false).single()
        near(l.igst, 180.0); near(l.cgst + l.sgst, 0.0)
    }

    @Test
    fun hsnSummaryGroupsByCodeUnitAndRate() {
        val line = { hsn: String, rate: Double, q: Double -> GstLine(hsn, "x", "Nos", q, rate, q * 100, q * 2.5, q * 2.5, 0.0) }
        val bill = GstBill("A-1", "01/04/2026", "33AAAAA0000A1Z5", "C", 210.0, "Tamil Nadu", false, 200.0, 5.0, 5.0, 0.0,
            listOf(line("5007", 5.0, 1.0), line("5007", 5.0, 1.0), line("5208", 5.0, 1.0)))
        val text = GstrExcel.hsn("Biz", "HSN", listOf(bill)).decodeToString()
        assertTrue(text.contains("5007")); assertTrue(text.contains("5208"))
        assertTrue(text.contains("<v>200</v>")) // 5007: two units of 100 are one row of 200
    }

    @Test
    fun b2bHasOneRowPerBillAndTotals() {
        val bill = GstBill("A-1", "01/04/2026", "33AAAAA0000A1Z5", "C", 105.0, "Tamil Nadu", false, 100.0, 2.5, 2.5, 0.0, emptyList())
        val text = GstrExcel.b2b("Biz", "B2B", listOf(bill, bill.copy(number = "A-2"))).decodeToString()
        assertTrue(text.contains("A-1")); assertTrue(text.contains("A-2")); assertTrue(text.contains("Total (2)"))
        assertTrue(text.contains("<v>210</v>"))
    }
}
