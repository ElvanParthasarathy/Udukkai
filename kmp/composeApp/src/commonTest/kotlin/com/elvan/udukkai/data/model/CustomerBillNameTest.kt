package com.elvan.udukkai.data.model

import kotlin.test.Test
import kotlin.test.assertEquals

/** What a customer's bill prints: the business, the owner or both, and just the name for a person. */
class CustomerBillNameTest {
    private val business = CustomerModel(
        name = mapOf("en" to "Sri Silks", "ta" to "ஸ்ரீ சில்க்ஸ்"),
        extra = mapOf("type" to "b2b", "owner.en" to "Raman", "owner.ta" to "ராமன்")
    )

    @Test
    fun businessOnlyIsTheDefault() = assertEquals("Sri Silks", business.billName()["en"])

    @Test
    fun ownerOnlyAndBoth() {
        assertEquals("ராமன்", business.copy(extra = business.extra + ("show" to SHOW_OWNER)).billName()["ta"])
        assertEquals("Sri Silks (Raman)", business.copy(extra = business.extra + ("show" to SHOW_BOTH)).billName()["en"])
    }

    @Test
    fun aPersonAndABusinessWithoutOwnerPrintTheirName() {
        val person = CustomerModel(name = mapOf("en" to "Kumar"), extra = mapOf("show" to SHOW_OWNER))
        assertEquals(person.name, person.billName())
        val noOwner = business.copy(extra = mapOf("type" to "b2b", "show" to SHOW_BOTH))
        assertEquals(noOwner.name, noOwner.billName())
    }

    @Test
    fun theBillKeepsTheNameItWasPrintedWith() {
        val saved = CustomerSnapshot.toCustomer(CustomerSnapshot.of(business.copy(extra = business.extra + ("show" to SHOW_BOTH))))!!
        assertEquals("Sri Silks (Raman)", saved.name["en"])
    }
}
