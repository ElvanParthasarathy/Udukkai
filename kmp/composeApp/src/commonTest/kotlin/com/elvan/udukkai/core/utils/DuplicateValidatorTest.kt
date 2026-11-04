package com.elvan.udukkai.core.utils

import com.elvan.udukkai.data.model.CustomerModel
import com.elvan.udukkai.data.model.ItemModel
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DuplicateValidatorTest {

    @Test
    fun testIsCompanyOverlap() {
        // Global overlaps with everything
        assertTrue(DuplicateValidator.isCompanyOverlap(emptyList(), emptyList()))
        assertTrue(DuplicateValidator.isCompanyOverlap(emptyList(), listOf(1L)))
        assertTrue(DuplicateValidator.isCompanyOverlap(listOf(1L), emptyList()))

        // Shared company ID
        assertTrue(DuplicateValidator.isCompanyOverlap(listOf(1L, 2L), listOf(2L, 3L)))

        // Distinct companies
        assertFalse(DuplicateValidator.isCompanyOverlap(listOf(1L), listOf(2L)))
    }

    @Test
    fun testIsDuplicateNameCaseAndWhitespace() {
        val existing = mapOf("en" to "Cotton Saree", "ta" to "பருத்திச் சேலை")

        // Exact match
        assertTrue(DuplicateValidator.isDuplicateName(mapOf("en" to "Cotton Saree"), existing))

        // Case-insensitive match
        assertTrue(DuplicateValidator.isDuplicateName(mapOf("en" to "cotton saree"), existing))

        // Trimmed whitespace match
        assertTrue(DuplicateValidator.isDuplicateName(mapOf("en" to "  Cotton Saree  "), existing))

        // Tamil match
        assertTrue(DuplicateValidator.isDuplicateName(mapOf("ta" to "பருத்திச் சேலை"), existing))

        // Different name
        assertFalse(DuplicateValidator.isDuplicateName(mapOf("en" to "Silk Saree", "ta" to "பட்டுச் சேலை"), existing))

        // Blank name should not match
        assertFalse(DuplicateValidator.isDuplicateName(mapOf("en" to "   "), existing))
        assertFalse(DuplicateValidator.isDuplicateName(emptyMap(), existing))
    }

    @Test
    fun testIsDuplicateItem() {
        val existingList = listOf(
            ItemModel(
                id = 101L,
                name = mapOf("ta" to "நூல்", "en" to "Yarn"),
                companyIds = listOf(1L)
            ),
            ItemModel(
                id = 102L,
                name = mapOf("ta" to "சட்டை", "en" to "Shirt"),
                companyIds = emptyList() // All companies
            )
        )

        // 1. Same item being edited (id = 101L) with its own name -> allowed
        assertFalse(
            DuplicateValidator.isDuplicateItem(
                candidateNameMap = mapOf("en" to "Yarn"),
                candidateCompanyIds = listOf(1L),
                existingItems = existingList,
                currentItemId = 101L
            )
        )

        // 2. Editing item 101L and changing name to "Shirt" -> duplicate!
        assertTrue(
            DuplicateValidator.isDuplicateItem(
                candidateNameMap = mapOf("en" to "Shirt"),
                candidateCompanyIds = listOf(1L),
                existingItems = existingList,
                currentItemId = 101L
            )
        )

        // 3. New item (id = null or 0L) with existing name "Yarn" in same company -> duplicate!
        assertTrue(
            DuplicateValidator.isDuplicateItem(
                candidateNameMap = mapOf("en" to "yarn"),
                candidateCompanyIds = listOf(1L),
                existingItems = existingList,
                currentItemId = 0L
            )
        )

        // 4. Copied item (id = 0L) with untouched name -> duplicate!
        assertTrue(
            DuplicateValidator.isDuplicateItem(
                candidateNameMap = mapOf("ta" to "நூல்"),
                candidateCompanyIds = listOf(1L),
                existingItems = existingList,
                currentItemId = 0L
            )
        )

        // 5. New item in different company for item 101L (which is scoped only to company 1)
        assertFalse(
            DuplicateValidator.isDuplicateItem(
                candidateNameMap = mapOf("en" to "Yarn"),
                candidateCompanyIds = listOf(2L),
                existingItems = existingList,
                currentItemId = 0L
            )
        )

        // 6. New item for item 102L (which is scoped to all companies) in company 2 -> duplicate!
        assertTrue(
            DuplicateValidator.isDuplicateItem(
                candidateNameMap = mapOf("en" to "shirt"),
                candidateCompanyIds = listOf(2L),
                existingItems = existingList,
                currentItemId = 0L
            )
        )

        // 7. Completely unique item name -> allowed
        assertFalse(
            DuplicateValidator.isDuplicateItem(
                candidateNameMap = mapOf("en" to "Unique Item"),
                candidateCompanyIds = listOf(1L),
                existingItems = existingList,
                currentItemId = 0L
            )
        )
    }

    @Test
    fun testIsDuplicateCustomer() {
        val existingCustomers = listOf(
            CustomerModel(
                id = 201L,
                name = mapOf("ta" to "முருகன்", "en" to "Murugan"),
                companyIds = listOf(1L)
            )
        )

        // Same customer edited
        assertFalse(
            DuplicateValidator.isDuplicateCustomer(
                candidateNameMap = mapOf("en" to "Murugan"),
                candidateCompanyIds = listOf(1L),
                existingCustomers = existingCustomers,
                currentCustomerId = 201L
            )
        )

        // New customer duplicate name
        assertTrue(
            DuplicateValidator.isDuplicateCustomer(
                candidateNameMap = mapOf("en" to "murugan"),
                candidateCompanyIds = listOf(1L),
                existingCustomers = existingCustomers,
                currentCustomerId = 0L
            )
        )

        // New customer unique name
        assertFalse(
            DuplicateValidator.isDuplicateCustomer(
                candidateNameMap = mapOf("en" to "Karthik"),
                candidateCompanyIds = listOf(1L),
                existingCustomers = existingCustomers,
                currentCustomerId = 0L
            )
        )
    }

    @Test
    fun testIsDuplicateCustomerWithAddresses() {
        val existingCustomers = listOf(
            CustomerModel(
                id = 301L,
                name = mapOf("en" to "Sundari Silks", "ta" to "சுந்தரி சில்க்ஸ்"),
                address = mapOf("en" to "12 Bazaar Street", "ta" to "12 கடைவீதி"),
                city = mapOf("en" to "Salem", "ta" to "சேலம்"),
                district = mapOf("en" to "Salem", "ta" to "சேலம்"),
                state = mapOf("en" to "Tamil Nadu", "ta" to "தமிழ்நாடு"),
                pinCode = "636001",
                companyIds = listOf(1L)
            ),
            CustomerModel(
                id = 302L,
                name = mapOf("en" to "Nalli Textiles"),
                address = emptyMap(),
                city = emptyMap(),
                district = emptyMap(),
                state = emptyMap(),
                pinCode = "",
                companyIds = listOf(1L)
            )
        )

        // 1. Same name + identical address (same city, address, pin) -> duplicate!
        assertTrue(
            DuplicateValidator.isDuplicateCustomer(
                candidateNameMap = mapOf("en" to "Sundari Silks"),
                candidateAddress = mapOf("en" to "12 Bazaar Street"),
                candidateCity = mapOf("en" to "Salem"),
                candidateDistrict = mapOf("en" to "Salem"),
                candidateState = mapOf("en" to "Tamil Nadu"),
                candidatePinCode = "636001",
                candidateCompanyIds = listOf(1L),
                existingCustomers = existingCustomers,
                currentCustomerId = 0L
            )
        )

        // 2. Same name + different city (branch in Erode) -> allowed!
        assertFalse(
            DuplicateValidator.isDuplicateCustomer(
                candidateNameMap = mapOf("en" to "Sundari Silks"),
                candidateAddress = mapOf("en" to "12 Bazaar Street"),
                candidateCity = mapOf("en" to "Erode"),
                candidateDistrict = mapOf("en" to "Erode"),
                candidateState = mapOf("en" to "Tamil Nadu"),
                candidatePinCode = "638001",
                candidateCompanyIds = listOf(1L),
                existingCustomers = existingCustomers,
                currentCustomerId = 0L
            )
        )

        // 3. Same name + same city + different street address -> allowed!
        assertFalse(
            DuplicateValidator.isDuplicateCustomer(
                candidateNameMap = mapOf("en" to "Sundari Silks"),
                candidateAddress = mapOf("en" to "99 Anna Salai"),
                candidateCity = mapOf("en" to "Salem"),
                candidateDistrict = mapOf("en" to "Salem"),
                candidateState = mapOf("en" to "Tamil Nadu"),
                candidatePinCode = "636001",
                candidateCompanyIds = listOf(1L),
                existingCustomers = existingCustomers,
                currentCustomerId = 0L
            )
        )

        // 4. Same name + same city + different PIN code -> allowed!
        assertFalse(
            DuplicateValidator.isDuplicateCustomer(
                candidateNameMap = mapOf("en" to "Sundari Silks"),
                candidateAddress = mapOf("en" to "12 Bazaar Street"),
                candidateCity = mapOf("en" to "Salem"),
                candidateDistrict = mapOf("en" to "Salem"),
                candidateState = mapOf("en" to "Tamil Nadu"),
                candidatePinCode = "636004",
                candidateCompanyIds = listOf(1L),
                existingCustomers = existingCustomers,
                currentCustomerId = 0L
            )
        )

        // 5. Same name + matching bilingual address components (Tamil candidate matches Tamil existing) -> duplicate!
        assertTrue(
            DuplicateValidator.isDuplicateCustomer(
                candidateNameMap = mapOf("ta" to "சுந்தரி சில்க்ஸ்"),
                candidateAddress = mapOf("ta" to "12 கடைவீதி"),
                candidateCity = mapOf("ta" to "சேலம்"),
                candidateDistrict = mapOf("ta" to "சேலம்"),
                candidateState = mapOf("ta" to "தமிழ்நாடு"),
                candidatePinCode = "636001",
                candidateCompanyIds = listOf(1L),
                existingCustomers = existingCustomers,
                currentCustomerId = 0L
            )
        )

        // 6. Same name + one has address and other has none -> allowed (differentiated)!
        assertFalse(
            DuplicateValidator.isDuplicateCustomer(
                candidateNameMap = mapOf("en" to "Nalli Textiles"),
                candidateAddress = mapOf("en" to "100 South Street"),
                candidateCity = mapOf("en" to "Madurai"),
                candidatePinCode = "625001",
                candidateCompanyIds = listOf(1L),
                existingCustomers = existingCustomers,
                currentCustomerId = 0L
            )
        )

        // 7. Same name + both have no address -> duplicate!
        assertTrue(
            DuplicateValidator.isDuplicateCustomer(
                candidateNameMap = mapOf("en" to "Nalli Textiles"),
                candidateAddress = emptyMap(),
                candidateCity = emptyMap(),
                candidatePinCode = "",
                candidateCompanyIds = listOf(1L),
                existingCustomers = existingCustomers,
                currentCustomerId = 0L
            )
        )
    }
}
