package com.elvan.udukkai.data.model

import kotlin.test.Test
import kotlin.test.assertEquals

class InvoiceCustomerGstinTest {
    private fun bill(customer: CustomerModel) = InvoiceModel(customPreferencesJson = InvoiceDisplayOptions(customer = CustomerSnapshot.of(customer)).toJson())
    @Test fun savedGstinSurvivesCustomerEditAndDeletion() {
        val original = CustomerModel(name = mapOf("en" to "Customer"), gstin = "33ABCDE1234F1Z5")
        assertEquals(original.gstin, CustomerSnapshot.invoiceGstin(bill(original), original.copy(gstin = "")))
        assertEquals(original.gstin, CustomerSnapshot.invoiceGstin(bill(original), null))
    }
    @Test fun savedUnregisteredCustomerStaysUnregistered() {
        val original = CustomerModel(name = mapOf("en" to "Customer"))
        assertEquals("", CustomerSnapshot.invoiceGstin(bill(original), original.copy(gstin = "33ABCDE1234F1Z5")))
    }
    @Test fun legacyBillUsesCurrentCustomer() {
        val current = CustomerModel(gstin = "33ABCDE1234F1Z5")
        assertEquals(current.gstin, CustomerSnapshot.invoiceGstin(InvoiceModel(), current))
        assertEquals("", CustomerSnapshot.invoiceGstin(InvoiceModel(), null))
    }
}
