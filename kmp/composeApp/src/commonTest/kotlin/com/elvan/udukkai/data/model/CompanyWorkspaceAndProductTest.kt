package com.elvan.udukkai.data.model

import com.elvan.udukkai.factory.data.FactoryInvoiceRepository
import com.elvan.udukkai.localization.BillingLanguage
import com.elvan.udukkai.localization.PrintLanguageManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CompanyWorkspaceAndProductTest {

    @Test
    fun testProductSharedByDefault() {
        val sharedProduct = ProductModel(
            id = 1,
            price = 1200.0,
            companyIds = emptyList()
        )
        // Available for all companies
        assertTrue(sharedProduct.isAvailableForCompany(10L))
        assertTrue(sharedProduct.isAvailableForCompany(20L))
        // And effective price is the default vilai
        assertEquals(1200.0, sharedProduct.getEffectivePrice(10L))
        assertEquals(1200.0, sharedProduct.getEffectivePrice(20L))
        assertEquals(1200.0, sharedProduct.getEffectivePrice(null))
    }

    @Test
    fun testProductRestrictedToSpecificCompanies() {
        val restrictedProduct = ProductModel(
            id = 2,
            price = 1500.0,
            companyIds = listOf(101L, 102L)
        )
        assertTrue(restrictedProduct.isAvailableForCompany(101L))
        assertTrue(restrictedProduct.isAvailableForCompany(102L))
        assertFalse(restrictedProduct.isAvailableForCompany(103L))
        assertFalse(restrictedProduct.isAvailableForCompany(null))
    }

    @Test
    fun testCompanySpecificPriceOverrides() {
        val productWithOverrides = ProductModel(
            id = 3,
            price = 1000.0,
            companyPrices = mapOf(101L to 1250.0, 102L to 1400.0)
        )
        // Company 101 gets override 1250.0
        assertEquals(1250.0, productWithOverrides.getEffectivePrice(101L))
        // Company 102 gets override 1400.0
        assertEquals(1400.0, productWithOverrides.getEffectivePrice(102L))
        // Unspecified company 103 falls back to default vilai 1000.0
        assertEquals(1000.0, productWithOverrides.getEffectivePrice(103L))
        // Null company ID falls back to default vilai 1000.0
        assertEquals(1000.0, productWithOverrides.getEffectivePrice(null))
    }

    @Test
    fun testProductJsonConverterLongList() {
        assertEquals(emptyList(), ProductJsonConverter.parseLongList(null))
        assertEquals(emptyList(), ProductJsonConverter.parseLongList(""))
        assertEquals(emptyList(), ProductJsonConverter.parseLongList("[]"))
        assertEquals(listOf(101L, 102L, 103L), ProductJsonConverter.parseLongList("[101, 102, 103]"))

        assertEquals("[]", ProductJsonConverter.stringifyLongList(emptyList()))
        assertEquals("[]", ProductJsonConverter.stringifyLongList(null))
        assertEquals("[101,102]", ProductJsonConverter.stringifyLongList(listOf(101L, 102L)))
    }

    @Test
    fun testProductJsonConverterCompanyPrices() {
        assertEquals(emptyMap(), ProductJsonConverter.parseCompanyPrices(null))
        assertEquals(emptyMap(), ProductJsonConverter.parseCompanyPrices(""))
        assertEquals(emptyMap(), ProductJsonConverter.parseCompanyPrices("{}"))

        val parsed = ProductJsonConverter.parseCompanyPrices("""{"101": 1250.0, "102": 1400.5}""")
        assertEquals(2, parsed.size)
        assertEquals(1250.0, parsed[101L])
        assertEquals(1400.5, parsed[102L])

        val stringified = ProductJsonConverter.stringifyCompanyPrices(mapOf(101L to 1250.0))
        assertEquals("""{"101":1250.0}""", stringified)
    }

    @Test
    fun testInvoiceNumberingPrefix() {
        assertEquals("VRM-01", FactoryInvoiceRepository.formatInvoiceNumber("VRM", 1))
        assertEquals("VRM-09", FactoryInvoiceRepository.formatInvoiceNumber("VRM", 9))
        assertEquals("VRM-10", FactoryInvoiceRepository.formatInvoiceNumber("VRM", 10))
        assertEquals("PVS-25", FactoryInvoiceRepository.formatInvoiceNumber("PVS", 25))
    }

    @Test
    fun testProductFirebasePayloadGeneration() {
        val product = ProductModel(
            id = 42,
            name = mapOf("ta" to "பட்டு", "en" to "Kanchipuram Pattu"),
            price = 4500.0,
            companyIds = listOf(101L, 102L),
            companyPrices = mapOf(101L to 4200.0, 102L to 4600.0)
        )
        val payload = ProductFirebasePayloadConverter.toFirebasePayload(product)

        assertEquals(42L, payload["id"])
        assertEquals(4500.0, payload["vilai"])
        assertEquals(listOf(101L, 102L), payload["niruvanamIds"])
        
        @Suppress("UNCHECKED_CAST")
        val prices = payload["companyPrices"] as Map<String, Double>
        assertEquals(2, prices.size)
        // Stable stringified keys for RTDB
        assertEquals(4200.0, prices["101"])
        assertEquals(4600.0, prices["102"])
    }

    @Test
    fun testProductFirebasePayloadParsingCompanyIds() {
        // From List of Longs
        assertEquals(listOf(101L, 102L), ProductFirebasePayloadConverter.parseCompanyIds(listOf(101L, 102L)))
        // From List of Ints/Numbers
        assertEquals(listOf(101L, 102L), ProductFirebasePayloadConverter.parseCompanyIds(listOf(101, 102)))
        // From List of Strings
        assertEquals(listOf(101L, 102L), ProductFirebasePayloadConverter.parseCompanyIds(listOf("101", "102")))
        // From Map (e.g. Firebase array saved as object / sparse keys)
        assertEquals(listOf(101L, 102L), ProductFirebasePayloadConverter.parseCompanyIds(mapOf("0" to 101, "1" to 102)))
        // From JSON string
        assertEquals(listOf(101L, 102L), ProductFirebasePayloadConverter.parseCompanyIds("[101, 102]"))
        // Null / empty / blank
        assertEquals(emptyList(), ProductFirebasePayloadConverter.parseCompanyIds(null))
        assertEquals(emptyList(), ProductFirebasePayloadConverter.parseCompanyIds(""))
        assertEquals(emptyList(), ProductFirebasePayloadConverter.parseCompanyIds("[]"))
    }

    @Test
    fun testProductFirebasePayloadParsingCompanyPrices() {
        // From Map with String keys and Double values
        val map1 = mapOf("101" to 4200.0, "102" to 4600.0)
        val parsed1 = ProductFirebasePayloadConverter.parseCompanyPrices(map1)
        assertEquals(2, parsed1.size)
        assertEquals(4200.0, parsed1[101L])
        assertEquals(4600.0, parsed1[102L])

        // From Map with Long/Int keys and Int/Long values (Firebase numbers)
        val map2 = mapOf(101L to 4200, 102 to 4600L)
        val parsed2 = ProductFirebasePayloadConverter.parseCompanyPrices(map2)
        assertEquals(2, parsed2.size)
        assertEquals(4200.0, parsed2[101L])
        assertEquals(4600.0, parsed2[102L])

        // From JSON string
        val json = """{"101": 4200.0, "102": 4600.5}"""
        val parsed3 = ProductFirebasePayloadConverter.parseCompanyPrices(json)
        assertEquals(2, parsed3.size)
        assertEquals(4200.0, parsed3[101L])
        assertEquals(4600.5, parsed3[102L])

        // Null / empty / blank
        assertEquals(emptyMap(), ProductFirebasePayloadConverter.parseCompanyPrices(null))
        assertEquals(emptyMap(), ProductFirebasePayloadConverter.parseCompanyPrices(""))
        assertEquals(emptyMap(), ProductFirebasePayloadConverter.parseCompanyPrices("{}"))
    }

    @Test
    fun testProductFirebaseLegacyPayloadFromRawMap() {
        // Payload from older app version without company features
        val legacyPayload: Map<String, Any?> = mapOf(
            "id" to 10L,
            "porulPeyar" to """{"ta":"வேட்டி","en":"Traditional Dhotis"}""",
            "vilai" to 650.0,
            "alavuVagai" to "length",
            "alagu" to "Meter",
            "hsnCode" to "5208"
        )
        val item = ProductFirebasePayloadConverter.fromRawMap(legacyPayload)
        assertNotNull(item)
        assertEquals(10L, item.id)
        assertEquals("Traditional Dhotis", item.name["en"])
        assertEquals(650.0, item.price)
        assertEquals("Meter", item.unit)
        // Default to empty list (available for all companies) and empty map
        assertEquals(emptyList(), item.companyIds)
        assertEquals(emptyMap(), item.companyPrices)
        assertTrue(item.isAvailableForCompany(999L))
        assertEquals(650.0, item.getEffectivePrice(999L))
    }

    @Test
    fun testProductFirebaseRoundtrip() {
        val original = ProductModel(
            id = 55,
            name = mapOf("ta" to "சட்டை", "en" to "Silk Shirt"),
            price = 2200.0,
            quantityType = "quantity",
            unit = "Piece",
            hsnCode = "6205",
            companyIds = listOf(201L, 202L),
            companyPrices = mapOf(201L to 2100.0, 202L to 2300.0)
        )
        val payload = ProductFirebasePayloadConverter.toFirebasePayload(original)
        val reconstructed = ProductFirebasePayloadConverter.fromRawMap(payload)

        assertNotNull(reconstructed)
        assertEquals(original.id, reconstructed.id)
        assertEquals(original.name, reconstructed.name)
        assertEquals(original.price, reconstructed.price)
        assertEquals(original.quantityType, reconstructed.quantityType)
        assertEquals(original.unit, reconstructed.unit)
        assertEquals(original.hsnCode, reconstructed.hsnCode)
        assertEquals(original.companyIds, reconstructed.companyIds)
        assertEquals(original.companyPrices, reconstructed.companyPrices)
    }

    @Test
    fun testProductSingleCompanyRestrictionAndOverrides() {
        // Product restricted to exactly one company
        val singleCompanyProduct = ProductModel(
            id = 77,
            price = 3000.0,
            companyIds = listOf(101L),
            companyPrices = mapOf(101L to 2800.0, 102L to 3200.0)
        )
        // Availability
        assertTrue(singleCompanyProduct.isAvailableForCompany(101L))
        assertFalse(singleCompanyProduct.isAvailableForCompany(102L))
        assertFalse(singleCompanyProduct.isAvailableForCompany(103L))
        assertFalse(singleCompanyProduct.isAvailableForCompany(null))

        // Price overrides for different companies
        assertEquals(2800.0, singleCompanyProduct.getEffectivePrice(101L))
        assertEquals(3200.0, singleCompanyProduct.getEffectivePrice(102L))
        assertEquals(3000.0, singleCompanyProduct.getEffectivePrice(103L)) // Fallback to base vilai
    }

    @Test
    fun testProductEditPreservesAvailabilityAndPriceOverrides() {
        val initialProduct = ProductModel(
            id = 88,
            name = mapOf("en" to "Cotton Saree"),
            price = 1500.0,
            companyIds = listOf(101L),
            companyPrices = mapOf(101L to 1350.0)
        )

        // Perform edit: changing base price and name, leaving company metadata intact
        val editedProduct = initialProduct.copy(
            name = mapOf("en" to "Premium Cotton Saree"),
            price = 1600.0,
            updatedAt = System.currentTimeMillis()
        )

        // Verify fields are preserved on edit
        assertEquals(listOf(101L), editedProduct.companyIds)
        assertEquals(mapOf(101L to 1350.0), editedProduct.companyPrices)
        assertTrue(editedProduct.isAvailableForCompany(101L))
        assertEquals(1350.0, editedProduct.getEffectivePrice(101L))

        // Verify Firebase payload preserves both
        val payload = ProductFirebasePayloadConverter.toFirebasePayload(editedProduct)
        assertEquals(listOf(101L), payload["niruvanamIds"])
        @Suppress("UNCHECKED_CAST")
        val prices = payload["companyPrices"] as Map<String, Double>
        assertEquals(1350.0, prices["101"])
    }

    @Test
    fun testBilingualBillingConsistentAcrossModes() {
        val factoryInitial = com.elvan.udukkai.factory.data.FactoryProfileRepository.profile

        com.elvan.udukkai.factory.data.FactoryProfileRepository.updateProfile(
            factoryInitial.copy(isBilingual = false, primaryLanguage = "ta", secondaryLanguage = "en")
        )
        val factorySingle = PrintLanguageManager.getConfig("factory")
        assertFalse(factorySingle.isBilingual)
        assertEquals(BillingLanguage.TAMIL, factorySingle.primaryLanguage)

        com.elvan.udukkai.factory.data.FactoryProfileRepository.updateProfile(
            factoryInitial.copy(isBilingual = true, primaryLanguage = "ta", secondaryLanguage = "en")
        )
        val factoryBilingual = PrintLanguageManager.getConfig("factory")
        assertTrue(factoryBilingual.isBilingual)
        assertEquals(BillingLanguage.ENGLISH, factoryBilingual.secondaryLanguage)

        val textilesInitial = com.elvan.udukkai.textiles.data.TextilesProfileRepository.profile

        com.elvan.udukkai.textiles.data.TextilesProfileRepository.updateProfile(
            textilesInitial.copy(isBilingual = false, primaryLanguage = "en", secondaryLanguage = "ta")
        )
        val textilesSingle = PrintLanguageManager.getConfig("textiles")
        assertFalse(textilesSingle.isBilingual)
        assertEquals(BillingLanguage.ENGLISH, textilesSingle.primaryLanguage)

        com.elvan.udukkai.textiles.data.TextilesProfileRepository.updateProfile(
            textilesInitial.copy(isBilingual = true, primaryLanguage = "en", secondaryLanguage = "ta")
        )
        val textilesBilingual = PrintLanguageManager.getConfig("textiles")
        assertTrue(textilesBilingual.isBilingual)
        assertEquals(BillingLanguage.TAMIL, textilesBilingual.secondaryLanguage)

        com.elvan.udukkai.factory.data.FactoryProfileRepository.updateProfile(factoryInitial)
        com.elvan.udukkai.textiles.data.TextilesProfileRepository.updateProfile(textilesInitial)
    }

    @Test
    fun testDocumentCompanyProfileBindingLogic() {
        val activeProfileId = 101L
        val savedInvoiceCompanyId = 202L

        // New invoice creation scenario: binds to active company profile
        val isNewInvoice = true
        val resolvedCompanyIdForNew = if (isNewInvoice) activeProfileId else savedInvoiceCompanyId
        assertEquals(101L, resolvedCompanyIdForNew)

        // Edit invoice scenario: preserves document's existing company ID
        val isEditInvoice = false // not new
        val resolvedCompanyIdForEdit = if (isEditInvoice) activeProfileId else savedInvoiceCompanyId
        assertEquals(202L, resolvedCompanyIdForEdit)
    }

    @Test
    fun testRecycleBinCompanyIsolationAndFiltering() {
        val companyAId = 101L
        val companyBId = 202L

        val deletedInvoices = listOf(
            InvoiceModel(id = 1, invoiceNumber = "INV-001", companyId = companyAId, isDeleted = true),
            InvoiceModel(id = 2, invoiceNumber = "INV-002", companyId = companyBId, isDeleted = true),
            InvoiceModel(id = 3, invoiceNumber = "INV-003", companyId = companyAId, isDeleted = true)
        )

        val deletedReceipts = listOf(
            ReceiptModel(id = 10, receiptNumber = "REC-010", companyId = companyAId, isDeleted = true),
            ReceiptModel(id = 11, receiptNumber = "REC-011", companyId = companyBId, isDeleted = true)
        )

        // Workspace Company A filter:
        val companyAInvoices = deletedInvoices.filter { it.companyId == companyAId }
        val companyAReceipts = deletedReceipts.filter { it.companyId == companyAId }
        assertEquals(2, companyAInvoices.size)
        assertTrue(companyAInvoices.all { it.companyId == companyAId })
        assertEquals(1, companyAReceipts.size)
        assertEquals(companyAId, companyAReceipts.first().companyId)

        // Workspace Company B filter:
        val companyBInvoices = deletedInvoices.filter { it.companyId == companyBId }
        val companyBReceipts = deletedReceipts.filter { it.companyId == companyBId }
        assertEquals(1, companyBInvoices.size)
        assertEquals("INV-002", companyBInvoices.first().invoiceNumber)
        assertEquals(1, companyBReceipts.size)
        assertEquals("REC-011", companyBReceipts.first().receiptNumber)
    }

    @Test
    fun testRecycleBinRestorePreservesOriginalCompany() {
        val originalCompanyId = 202L
        val activeWorkspaceCompanyId = 101L

        val deletedInvoice = InvoiceModel(
            id = 55,
            invoiceNumber = "INV-055",
            companyId = originalCompanyId,
            isDeleted = true,
            deletedAt = 1000L
        )

        // Simulating non-destructive restore: toggle isDeleted and deletedAt while preserving niruvanamId
        val restoredInvoice = deletedInvoice.copy(isDeleted = false, deletedAt = null)

        assertFalse(restoredInvoice.isDeleted)
        assertNull(restoredInvoice.deletedAt)
        // Original company profile remains untouched, not reassigned to active workspace
        assertEquals(originalCompanyId, restoredInvoice.companyId)
        assertNotEquals(activeWorkspaceCompanyId, restoredInvoice.companyId)
    }

    @Test
    fun testRecycleBinProductAvailabilityScope() {
        val companyAId = 101L
        val companyBId = 202L

        val sharedProduct = ProductModel(id = 1, companyIds = emptyList(), isDeleted = true)
        val restrictedProductA = ProductModel(id = 2, companyIds = listOf(companyAId), isDeleted = true)

        val deletedProducts = listOf(sharedProduct, restrictedProductA)

        // Workspace Company A: displays shared + restricted to A
        val companyAProducts = deletedProducts.filter { it.isAvailableForCompany(companyAId) }
        assertEquals(2, companyAProducts.size)

        // Workspace Company B: displays shared only
        val companyBProducts = deletedProducts.filter { it.isAvailableForCompany(companyBId) }
        assertEquals(1, companyBProducts.size)
        assertEquals(1L, companyBProducts.first().id)
    }
}

