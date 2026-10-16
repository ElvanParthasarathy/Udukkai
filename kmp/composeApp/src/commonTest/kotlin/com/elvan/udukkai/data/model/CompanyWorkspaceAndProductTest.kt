package com.elvan.udukkai.data.model

import com.elvan.udukkai.data.repository.PattiyalRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
}
