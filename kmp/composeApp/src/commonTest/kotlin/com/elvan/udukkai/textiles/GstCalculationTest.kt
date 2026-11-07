package com.elvan.udukkai.textiles

import com.elvan.udukkai.textiles.ui.screens.editor.TextilesCalculation
import com.elvan.udukkai.textiles.ui.screens.editor.TextilesItem
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GstCalculationTest {
    private fun near(a: Double, b: Double) = assertTrue(abs(a - b) < 0.005, "$a != $b")

    private val saree = TextilesItem(itemName = "Saree", price = 8400.0, quantity = 3.0, taxRate = 5.0)

    @Test
    fun sameStateSplitsIntoCgstAndSgst() {
        val t = TextilesCalculation.calculate(listOf(saree), businessState = "Tamil Nadu", customerState = "Tamil Nadu")
        near(t.subtotal, 25200.0); near(t.totalTax, 1260.0)
        near(t.cgst, 630.0); near(t.sgst, 630.0); near(t.igst, 0.0)
        near(t.grandTotal, 26460.0)
    }

    @Test
    fun otherStateIsAllIgst() {
        val t = TextilesCalculation.calculate(listOf(saree), businessState = "Tamil Nadu", customerState = "Karnataka")
        near(t.cgst, 0.0); near(t.sgst, 0.0); near(t.igst, 1260.0)
        near(t.grandTotal, 26460.0)
    }

    @Test
    fun oddPaisaSplitsAddUpExactly() {
        val one = TextilesItem(itemName = "x", price = 33.33, quantity = 1.0, taxRate = 5.0) // tax 1.6665 -> 1.67
        val t = TextilesCalculation.calculate(listOf(one), businessState = "A", customerState = "A")
        near(t.cgst + t.sgst, t.totalTax)
    }

    @Test
    fun mixedRatesAndDiscounts() {
        val items = listOf(
            TextilesItem(itemName = "a", price = 1000.0, quantity = 2.0, taxRate = 5.0, discount = 10.0, discountType = "%"), // 1800 taxable
            TextilesItem(itemName = "b", price = 2500.0, quantity = 1.0, taxRate = 12.0, discount = 100.0, discountType = "₹") // 2400
        )
        val t = TextilesCalculation.calculate(items, businessState = "TN", customerState = "TN")
        near(t.totalDiscount, 300.0)
        near(t.totalTax, 90.0 + 288.0)
        near(t.grandTotal, 4200.0 + 378.0)
    }

    @Test
    fun globalDiscountSharesTaxProportionally() {
        val items = listOf(TextilesItem(itemName = "a", price = 1000.0, quantity = 1.0, taxRate = 5.0), TextilesItem(itemName = "b", price = 1000.0, quantity = 1.0, taxRate = 12.0))
        val t = TextilesCalculation.calculate(items, globalDiscountValue = 10.0, globalDiscountType = "%", businessState = "TN", customerState = "TN")
        near(t.totalDiscount, 200.0)
        near(t.totalTax, 45.0 + 108.0) // 900 @5% + 900 @12%
    }

    @Test
    fun taxInclusivePricesBackOutTheTax() {
        val inc = TextilesItem(itemName = "i", price = 1050.0, quantity = 1.0, taxRate = 5.0)
        val t = TextilesCalculation.calculate(listOf(inc), businessState = "TN", customerState = "TN", taxInclusive = true)
        near(t.totalTax, 50.0); near(t.grandTotal, 1050.0)
    }

    @Test
    fun missingStateOrForeignCustomerIsNotInterstate() {
        val t = TextilesCalculation.calculate(listOf(saree), businessState = "Tamil Nadu", customerState = "", country = "India")
        near(t.igst, 0.0)
        val f = TextilesCalculation.calculate(listOf(saree), businessState = "Tamil Nadu", customerState = "Texas", country = "USA")
        near(f.igst, 0.0)
    }

    @Test
    fun totalIsRoundedToWholeRupeeWithRoundOffRecorded() {
        val x = TextilesItem(itemName = "x", price = 100.10, quantity = 1.0, taxRate = 0.0)
        val t = TextilesCalculation.calculate(listOf(x), businessState = "TN", customerState = "TN")
        assertEquals(100.0, t.grandTotal)
        near(t.roundOff, -0.10)
    }
}
