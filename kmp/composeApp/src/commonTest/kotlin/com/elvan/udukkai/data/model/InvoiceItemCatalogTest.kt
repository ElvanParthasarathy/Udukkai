package com.elvan.udukkai.data.model

import com.elvan.udukkai.core.print.PrintPayload
import com.elvan.udukkai.data.settings.BusinessProfile
import com.elvan.udukkai.factory.ui.screens.editor.FactoryCalculation
import com.elvan.udukkai.factory.ui.screens.editor.FactoryItem
import com.elvan.udukkai.textiles.ui.screens.editor.TextilesCalculation
import com.elvan.udukkai.textiles.ui.screens.editor.TextilesItem
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNull

class InvoiceItemCatalogTest {
    private val original = ItemModel(id = 42, name = mapOf("fr" to "Ancien"), companyIds = listOf(1))
    private val renamed = original.copy(name = mapOf("fr" to "Nouveau")).let {
        it.copy(previousNames = ItemNames.history(original, it))
    }

    @Test
    fun renameKeepsTheIdAndLegacyName() {
        val catalog = InvoiceItemCatalog(listOf(renamed), companyId = 1)
        assertEquals(renamed, catalog.find("42", emptyMap()))
        assertEquals(renamed, catalog.find(null, mapOf("fr" to " ancien ")))
        assertNull(catalog.find("99", original.name))
        assertNull(InvoiceItemCatalog(listOf(renamed), companyId = 2).find(null, original.name))
    }

    @Test
    fun ambiguousLegacyNameKeepsTheSnapshot() {
        val duplicate = original.copy(id = 43)
        val catalog = InvoiceItemCatalog(listOf(renamed, duplicate), companyId = 1)
        assertNull(catalog.find(null, original.name))
    }

    @Test
    fun historySurvivesSerializationAndCloudPayloads() {
        val names = listOf(ItemNameRevision(mapOf("fr" to "Ancien \"nom\"", "ja" to "旧名"), 1794229000L))
        assertEquals(names, ItemNames.decodeHistory(ItemNames.encodeHistory(names)))
        val item = renamed.copy(previousNames = names)
        assertEquals(names, ItemFirebasePayloadConverter.fromRawMap(ItemFirebasePayloadConverter.toFirebasePayload(item))?.previousNames)
    }

    @Test
    fun printUsesBilledNameAndPriceInBothApps() {
        val profile = BusinessProfile(primaryLanguage = "en")
        val factory = FactoryCalculation.itemsToJson(listOf(FactoryItem(itemId = "42", itemNameEn = "Old", weight = 2.0, price = 37.0)))
        val textiles = TextilesCalculation.itemsToJson(listOf(TextilesItem(itemId = "42", itemNameEn = "Old", quantity = 2.0, price = 37.0)))
        val bill = InvoiceModel(companyId = 1, invoiceNumber = "INV-123", itemsJson = factory)
        val outputs = listOf(
            PrintPayload.forFactoryInvoice(bill, profile, null, "en"),
            PrintPayload.forTextilesInvoice(bill.copy(itemsJson = textiles), profile, null, "en")
        )
        for (json in outputs) {
            assertContains(json, "Old")
            assertContains(json, "\"price\":37")
            assertContains(json, "INV-123")
        }
    }
}
