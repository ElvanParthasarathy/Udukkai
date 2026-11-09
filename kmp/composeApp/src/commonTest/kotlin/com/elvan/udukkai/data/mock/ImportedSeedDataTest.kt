package com.elvan.udukkai.data.mock

import com.elvan.udukkai.factory.dev.FactoryMockDataSeeder
import com.elvan.udukkai.factory.ui.screens.editor.FactoryCalculation
import com.elvan.udukkai.factory.ui.screens.editor.FactoryItem
import com.elvan.udukkai.textiles.dev.TextilesMockDataSeeder
import com.elvan.udukkai.textiles.ui.screens.editor.TextilesCalculation
import com.elvan.udukkai.textiles.ui.screens.editor.TextilesItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The dev seed data (MockData.kt) against the counts and totals of the old app's export it was imported from. */
class ImportedSeedDataTest {

    @Test
    fun counts_match_the_export() {
        // Export: 13 silk invoices, one of them a copy of SJPS-31 (skipped)
        assertEquals(1, seedTextilesProfiles.size)
        assertEquals(8, seedTextilesItems.size)
        assertEquals(1, seedTextilesCustomers.size)
        assertEquals(12, seedTextilesInvoices.size)
        assertEquals(4, seedTextilesReceipts.size)
        assertEquals(2, seedFactoryProfiles.size)
        assertEquals(4, seedFactoryItems.size)
        assertEquals(2, seedFactoryCustomers.size)
        assertEquals(47, seedFactoryInvoices.size)
        assertEquals(7, seedFactoryReceipts.size)
        assertEquals(28, seedFactoryInvoices.count { it.company == "VRM" })
        assertEquals(19, seedFactoryInvoices.count { it.company == "PVS" })
    }

    @Test
    fun textiles_invoice_totals_are_what_the_old_app_printed() {
        for (inv in seedTextilesInvoices) {
            val items = inv.lines.map {
                TextilesItem(quantity = it.quantity, price = it.price, taxRate = it.taxRate, discount = it.discount, discountType = it.discountType)
            }
            val totals = TextilesCalculation.calculate(
                items, inv.globalDiscountValue, inv.globalDiscountType, "Tamil Nadu",
                inv.customer["state.en"] ?: inv.customer["state.ta"].orEmpty(), "India", inv.taxInclusive
            )
            assertEquals(inv.total, totals.grandTotal, "total of ${inv.number}")
            // The old app rounded tax per bill, the new one per line: up to a paisa apart, the total is the same
            assertEquals(inv.taxAmount, totals.totalTax, 0.011, "tax of ${inv.number}")
        }
    }

    @Test
    fun factory_invoice_totals_are_what_the_old_app_printed() {
        for (inv in seedFactoryInvoices) {
            val items = inv.lines.map { FactoryItem(weight = it.kg, price = it.rate) }
            val totals = FactoryCalculation.calculate(items, inv.wastageGrams, inv.postalCharges, inv.ahimsaSilkAmount)
            assertEquals(inv.total, totals.grandTotal, "total of ${inv.number}")
        }
    }

    @Test
    fun every_link_points_at_something_in_the_data() {
        val tx = seedTextilesProfiles.map { it.shortName }.toSet()
        assertTrue(seedTextilesInvoices.all { it.company in tx && it.customerRef in seedTextilesCustomers.map { c -> c.ref } })
        assertTrue(seedTextilesInvoices.flatMap { it.lines }.all { it.itemRef == null || it.itemRef in seedTextilesItems.map { i -> i.ref } })
        assertTrue(seedTextilesReceipts.all { it.company in tx })
        val fx = seedFactoryProfiles.map { it.shortName }.toSet()
        assertTrue(seedFactoryInvoices.all { it.company in fx && it.customerRef in seedFactoryCustomers.map { c -> c.ref } })
        assertTrue(seedFactoryInvoices.flatMap { it.lines }.all { it.itemRef == null || it.itemRef in seedFactoryItems.map { i -> i.ref } })
        assertTrue(seedFactoryReceipts.all { it.company in fx })
    }

    @Test
    fun receipt_links_never_pay_more_than_the_receipt_or_the_invoice() {
        val tx = seedTextilesInvoices.associate { it.number to it.total }
        val txIds = tx.keys.withIndex().associate { (i, n) -> n to i.toLong() + 1 }
        for (r in seedTextilesReceipts) {
            val links = TextilesMockDataSeeder.buildReceiptLinks(r.against, r.amount, txIds, tx)
            assertTrue(links.sumOf { it.appliedAmount } <= r.amount + 0.001, "receipt ${r.number}")
        }
        // The silk receipt of 5.25 crore pays SJPS-31 and SJPS-29 in full; SJPS-27 is not in the data
        val first = TextilesMockDataSeeder.buildReceiptLinks("SJPS-31, SJPS-29, SJPS-27", 52543303.0, mapOf("SJPS-31" to 1L, "SJPS-29" to 2L), tx)
        assertEquals(listOf(52365011.0, 139925.0), first.map { it.appliedAmount })
        val fx = seedFactoryInvoices.associate { it.number to it.total }
        val fxIds = fx.keys.withIndex().associate { (i, n) -> n to i.toLong() + 1 }
        for (r in seedFactoryReceipts) {
            val links = FactoryMockDataSeeder.buildReceiptLinks(r.against, r.amount, fxIds, fx)
            assertTrue(links.sumOf { it.appliedAmount } <= r.amount + 0.001)
        }
        // PVS-33, PVS-32 and PVS-31 add up to the 47,025 of their receipts
        assertEquals(47025.0, FactoryMockDataSeeder.buildReceiptLinks("PVS-33, PVS-32, PVS-31", 47025.0, fxIds, fx).sumOf { it.appliedAmount }, 0.001)
    }
}
