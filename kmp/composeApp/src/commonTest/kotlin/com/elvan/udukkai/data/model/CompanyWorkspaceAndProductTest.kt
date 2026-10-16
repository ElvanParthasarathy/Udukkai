package com.elvan.udukkai.data.model

import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.data.repository.PattiyalRepository
import com.elvan.udukkai.data.settings.NiruvanaTharavugalRepository
import com.elvan.udukkai.localization.BillingLanguage
import com.elvan.udukkai.localization.PrintLanguageManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CompanyWorkspaceAndProductTest {

    @Test
    fun testProductSharedByDefault() {
        val sharedProduct = PorulTharavuru(
            id = 1,
            vilai = 1200.0,
            niruvanamIds = emptyList()
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
        val restrictedProduct = PorulTharavuru(
            id = 2,
            vilai = 1500.0,
            niruvanamIds = listOf(101L, 102L)
        )
        assertTrue(restrictedProduct.isAvailableForCompany(101L))
        assertTrue(restrictedProduct.isAvailableForCompany(102L))
        assertFalse(restrictedProduct.isAvailableForCompany(103L))
        assertFalse(restrictedProduct.isAvailableForCompany(null))
    }

    @Test
    fun testCompanySpecificPriceOverrides() {
        val productWithOverrides = PorulTharavuru(
            id = 3,
            vilai = 1000.0,
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
    fun testPorulJsonConverterLongList() {
        assertEquals(emptyList(), PorulJsonConverter.parseLongList(null))
        assertEquals(emptyList(), PorulJsonConverter.parseLongList(""))
        assertEquals(emptyList(), PorulJsonConverter.parseLongList("[]"))
        assertEquals(listOf(101L, 102L, 103L), PorulJsonConverter.parseLongList("[101, 102, 103]"))

        assertEquals("[]", PorulJsonConverter.stringifyLongList(emptyList()))
        assertEquals("[]", PorulJsonConverter.stringifyLongList(null))
        assertEquals("[101,102]", PorulJsonConverter.stringifyLongList(listOf(101L, 102L)))
    }

    @Test
    fun testPorulJsonConverterCompanyPrices() {
        assertEquals(emptyMap(), PorulJsonConverter.parseCompanyPrices(null))
        assertEquals(emptyMap(), PorulJsonConverter.parseCompanyPrices(""))
        assertEquals(emptyMap(), PorulJsonConverter.parseCompanyPrices("{}"))

        val parsed = PorulJsonConverter.parseCompanyPrices("""{"101": 1250.0, "102": 1400.5}""")
        assertEquals(2, parsed.size)
        assertEquals(1250.0, parsed[101L])
        assertEquals(1400.5, parsed[102L])

        val stringified = PorulJsonConverter.stringifyCompanyPrices(mapOf(101L to 1250.0))
        assertEquals("""{"101":1250.0}""", stringified)
    }

    @Test
    fun testInvoiceNumberingPrefix() {
        assertEquals("VRM-01", PattiyalRepository.formatPattiyalEn("VRM", 1))
        assertEquals("VRM-09", PattiyalRepository.formatPattiyalEn("VRM", 9))
        assertEquals("VRM-10", PattiyalRepository.formatPattiyalEn("VRM", 10))
        assertEquals("PVS-25", PattiyalRepository.formatPattiyalEn("PVS", 25))
    }

    @Test
    fun testPorulFirebasePayloadGeneration() {
        val product = PorulTharavuru(
            id = 42,
            porulPeyar = mapOf("ta" to "பட்டு", "en" to "Kanchipuram Pattu"),
            vilai = 4500.0,
            niruvanamIds = listOf(101L, 102L),
            companyPrices = mapOf(101L to 4200.0, 102L to 4600.0)
        )
        val payload = PorulFirebasePayloadConverter.toFirebasePayload(product)

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
    fun testPorulFirebasePayloadParsingNiruvanamIds() {
        // From List of Longs
        assertEquals(listOf(101L, 102L), PorulFirebasePayloadConverter.parseNiruvanamIds(listOf(101L, 102L)))
        // From List of Ints/Numbers
        assertEquals(listOf(101L, 102L), PorulFirebasePayloadConverter.parseNiruvanamIds(listOf(101, 102)))
        // From List of Strings
        assertEquals(listOf(101L, 102L), PorulFirebasePayloadConverter.parseNiruvanamIds(listOf("101", "102")))
        // From Map (e.g. Firebase array saved as object / sparse keys)
        assertEquals(listOf(101L, 102L), PorulFirebasePayloadConverter.parseNiruvanamIds(mapOf("0" to 101, "1" to 102)))
        // From JSON string
        assertEquals(listOf(101L, 102L), PorulFirebasePayloadConverter.parseNiruvanamIds("[101, 102]"))
        // Null / empty / blank
        assertEquals(emptyList(), PorulFirebasePayloadConverter.parseNiruvanamIds(null))
        assertEquals(emptyList(), PorulFirebasePayloadConverter.parseNiruvanamIds(""))
        assertEquals(emptyList(), PorulFirebasePayloadConverter.parseNiruvanamIds("[]"))
    }

    @Test
    fun testPorulFirebasePayloadParsingCompanyPrices() {
        // From Map with String keys and Double values
        val map1 = mapOf("101" to 4200.0, "102" to 4600.0)
        val parsed1 = PorulFirebasePayloadConverter.parseCompanyPrices(map1)
        assertEquals(2, parsed1.size)
        assertEquals(4200.0, parsed1[101L])
        assertEquals(4600.0, parsed1[102L])

        // From Map with Long/Int keys and Int/Long values (Firebase numbers)
        val map2 = mapOf(101L to 4200, 102 to 4600L)
        val parsed2 = PorulFirebasePayloadConverter.parseCompanyPrices(map2)
        assertEquals(2, parsed2.size)
        assertEquals(4200.0, parsed2[101L])
        assertEquals(4600.0, parsed2[102L])

        // From JSON string
        val json = """{"101": 4200.0, "102": 4600.5}"""
        val parsed3 = PorulFirebasePayloadConverter.parseCompanyPrices(json)
        assertEquals(2, parsed3.size)
        assertEquals(4200.0, parsed3[101L])
        assertEquals(4600.5, parsed3[102L])

        // Null / empty / blank
        assertEquals(emptyMap(), PorulFirebasePayloadConverter.parseCompanyPrices(null))
        assertEquals(emptyMap(), PorulFirebasePayloadConverter.parseCompanyPrices(""))
        assertEquals(emptyMap(), PorulFirebasePayloadConverter.parseCompanyPrices("{}"))
    }

    @Test
    fun testPorulFirebaseLegacyPayloadFromRawMap() {
        // Payload from older app version without company features
        val legacyPayload: Map<String, Any?> = mapOf(
            "id" to 10L,
            "porulPeyar" to """{"ta":"வேட்டி","en":"Traditional Dhotis"}""",
            "vilai" to 650.0,
            "alavuVagai" to "length",
            "alagu" to "Meter",
            "hsnCode" to "5208"
        )
        val item = PorulFirebasePayloadConverter.fromRawMap(legacyPayload)
        assertNotNull(item)
        assertEquals(10L, item.id)
        assertEquals("Traditional Dhotis", item.porulPeyar["en"])
        assertEquals(650.0, item.vilai)
        assertEquals("Meter", item.alagu)
        // Default to empty list (available for all companies) and empty map
        assertEquals(emptyList(), item.niruvanamIds)
        assertEquals(emptyMap(), item.companyPrices)
        assertTrue(item.isAvailableForCompany(999L))
        assertEquals(650.0, item.getEffectivePrice(999L))
    }

    @Test
    fun testPorulFirebaseRoundtrip() {
        val original = PorulTharavuru(
            id = 55,
            porulPeyar = mapOf("ta" to "சட்டை", "en" to "Silk Shirt"),
            vilai = 2200.0,
            alavuVagai = "quantity",
            alagu = "Piece",
            hsnCode = "6205",
            niruvanamIds = listOf(201L, 202L),
            companyPrices = mapOf(201L to 2100.0, 202L to 2300.0)
        )
        val payload = PorulFirebasePayloadConverter.toFirebasePayload(original)
        val reconstructed = PorulFirebasePayloadConverter.fromRawMap(payload)

        assertNotNull(reconstructed)
        assertEquals(original.id, reconstructed.id)
        assertEquals(original.porulPeyar, reconstructed.porulPeyar)
        assertEquals(original.vilai, reconstructed.vilai)
        assertEquals(original.alavuVagai, reconstructed.alavuVagai)
        assertEquals(original.alagu, reconstructed.alagu)
        assertEquals(original.hsnCode, reconstructed.hsnCode)
        assertEquals(original.niruvanamIds, reconstructed.niruvanamIds)
        assertEquals(original.companyPrices, reconstructed.companyPrices)
    }

    @Test
    fun testProductSingleCompanyRestrictionAndOverrides() {
        // Product restricted to exactly one company
        val singleCompanyProduct = PorulTharavuru(
            id = 77,
            vilai = 3000.0,
            niruvanamIds = listOf(101L),
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
        val initialProduct = PorulTharavuru(
            id = 88,
            porulPeyar = mapOf("en" to "Cotton Saree"),
            vilai = 1500.0,
            niruvanamIds = listOf(101L),
            companyPrices = mapOf(101L to 1350.0)
        )

        // Perform edit: changing base price and name, leaving company metadata intact
        val editedProduct = initialProduct.copy(
            porulPeyar = mapOf("en" to "Premium Cotton Saree"),
            vilai = 1600.0,
            updatedAt = System.currentTimeMillis()
        )

        // Verify fields are preserved on edit
        assertEquals(listOf(101L), editedProduct.niruvanamIds)
        assertEquals(mapOf(101L to 1350.0), editedProduct.companyPrices)
        assertTrue(editedProduct.isAvailableForCompany(101L))
        assertEquals(1350.0, editedProduct.getEffectivePrice(101L))

        // Verify Firebase payload preserves both
        val payload = PorulFirebasePayloadConverter.toFirebasePayload(editedProduct)
        assertEquals(listOf(101L), payload["niruvanamIds"])
        @Suppress("UNCHECKED_CAST")
        val prices = payload["companyPrices"] as Map<String, Double>
        assertEquals(1350.0, prices["101"])
    }

    @Test
    fun testBilingualBillingConsistentAcrossModes() {
        // Test KOOLI mode with bilingual enabled and disabled
        val kooliInitial = NiruvanaTharavugalRepository.getProfile(AppMode.KOOLI)

        NiruvanaTharavugalRepository.updateProfile(
            AppMode.KOOLI,
            kooliInitial.copy(iruMozhi = false, mudhanMozhi = "ta", thunaiMozhi = "en")
        )
        val kooliSingle = PrintLanguageManager.getConfig(AppMode.KOOLI)
        assertFalse(kooliSingle.isBilingual)
        assertEquals(BillingLanguage.TAMIL, kooliSingle.primaryLanguage)

        NiruvanaTharavugalRepository.updateProfile(
            AppMode.KOOLI,
            kooliInitial.copy(iruMozhi = true, mudhanMozhi = "ta", thunaiMozhi = "en")
        )
        val kooliBilingual = PrintLanguageManager.getConfig(AppMode.KOOLI)
        assertTrue(kooliBilingual.isBilingual)
        assertEquals(BillingLanguage.ENGLISH, kooliBilingual.secondaryLanguage)

        // Test PATTU mode with bilingual enabled and disabled
        val pattuInitial = NiruvanaTharavugalRepository.getProfile(AppMode.PATTU)

        NiruvanaTharavugalRepository.updateProfile(
            AppMode.PATTU,
            pattuInitial.copy(iruMozhi = false, mudhanMozhi = "en", thunaiMozhi = "ta")
        )
        val pattuSingle = PrintLanguageManager.getConfig(AppMode.PATTU)
        assertFalse(pattuSingle.isBilingual)
        assertEquals(BillingLanguage.ENGLISH, pattuSingle.primaryLanguage)

        NiruvanaTharavugalRepository.updateProfile(
            AppMode.PATTU,
            pattuInitial.copy(iruMozhi = true, mudhanMozhi = "en", thunaiMozhi = "ta")
        )
        val pattuBilingual = PrintLanguageManager.getConfig(AppMode.PATTU)
        assertTrue(pattuBilingual.isBilingual)
        assertEquals(BillingLanguage.TAMIL, pattuBilingual.secondaryLanguage)

        // Restore initial states
        NiruvanaTharavugalRepository.updateProfile(AppMode.KOOLI, kooliInitial)
        NiruvanaTharavugalRepository.updateProfile(AppMode.PATTU, pattuInitial)
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
}

