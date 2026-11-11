package com.elvan.udukkai.data.model

import kotlin.test.Test
import kotlin.test.assertEquals

class CustomerAddressTest {
    @Test fun legacyFactoryTownSnapshotUsesTheLinkedAddressAndRetainsItsSavedName() {
        val invoice = InvoiceModel(customPreferencesJson = InvoiceDisplayOptions(
            customer = mapOf("name.en" to "Billed name", "city.en" to "Coimbatore")
        ).toJson())
        val customer = CustomerModel(name = mapOf("en" to "Current name"),
            address = mapOf("en" to "10 Mill Street"), state = mapOf("en" to "Tamil Nadu"), country = mapOf("en" to "India"))
        val resolved = CustomerSnapshot.forInvoice(invoice, customer, legacyAddressIsCity = false)
        assertEquals("Billed name", resolved.name["en"])
        assertEquals("10 Mill Street, Coimbatore, Tamil Nadu, India", resolved.fullAddress("en"))
    }

    @Test fun newTownOnlySnapshotRemainsAsIssued() {
        val saved = CustomerModel(name = mapOf("en" to "Billed name"), city = mapOf("en" to "Coimbatore"))
        val invoice = InvoiceModel(customPreferencesJson = InvoiceDisplayOptions(customer = CustomerSnapshot.of(saved)).toJson())
        val current = saved.copy(address = mapOf("en" to "10 Mill Street"), state = mapOf("en" to "Tamil Nadu"))
        assertEquals("Coimbatore", CustomerSnapshot.forInvoice(invoice, current, legacyAddressIsCity = false).fullAddress("en"))
    }

    @Test fun fullAddressUsesEverySavedPartAndDeduplicatesTownAndDistrict() {
        val customer = CustomerModel(
            address = mapOf("en" to "10 Mill Street"), city = mapOf("en" to "Coimbatore"),
            district = mapOf("en" to "Coimbatore"), state = mapOf("en" to "Tamil Nadu"),
            country = mapOf("en" to "India"), pinCode = "641001"
        )
        val invoice = InvoiceModel(customPreferencesJson = InvoiceDisplayOptions(customer = CustomerSnapshot.of(customer)).toJson())
        assertEquals("10 Mill Street, Coimbatore, Tamil Nadu, 641001, India",
            CustomerSnapshot.forInvoice(invoice, CustomerModel(), legacyAddressIsCity = false).fullAddress("en"))
    }
}
