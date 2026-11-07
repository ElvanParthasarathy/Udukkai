package com.elvan.udukkai.core.print

import com.elvan.udukkai.data.model.CustomerModel
import com.elvan.udukkai.data.model.CustomerSnapshot
import com.elvan.udukkai.data.model.InvoiceDisplayOptions
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.model.ReceiptModel
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.data.settings.BusinessProfile
import com.elvan.udukkai.factory.ui.screens.editor.FactoryAdditionalCharge
import com.elvan.udukkai.factory.ui.screens.editor.FactoryCalculation
import com.elvan.udukkai.factory.ui.screens.editor.FactoryItem
import com.elvan.udukkai.textiles.ui.screens.editor.TextilesCalculation
import com.elvan.udukkai.textiles.ui.screens.editor.TextilesItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Every field typed into a customer, an item or a bill must reach the payload the print template reads. */
class BillFieldsTest {
    private val profile = BusinessProfile(
        businessName = mutableMapOf("ta" to "எனது கடை", "en" to "My Shop"),
        gstin = "33SHOPG1234F1Z5",
        phone1 = "9000000001",
    )

    private val customer = CustomerModel(
        id = 7,
        name = mapOf("ta" to "வாடிக்கையாளர்", "en" to "Cust Name"),
        address = mapOf("ta" to "12 தெரு", "en" to "12 Main Street"),
        city = mapOf("ta" to "ஆரணி", "en" to "Arani"),
        district = mapOf("ta" to "திருவண்ணாமலை", "en" to "Tiruvannamalai"),
        state = mapOf("ta" to "தமிழ்நாடு", "en" to "Tamil Nadu"),
        country = mapOf("en" to "India"),
        internationalAddress = mapOf("en" to "5 Intl Road Dubai"),
        pinCode = "632301",
        gstin = "33CUSTG1234F1Z5",
        email = "cust@example.com",
        phone = "9876543210",
    )

    private fun missing(json: String, expected: List<String>) = expected.filterNot { json.contains(it) }

    @Test
    fun textiles_bill_carries_every_customer_and_item_field() {
        val items = listOf(
            TextilesItem(itemName = "பட்டுப்புடவை", itemNameEn = "Silk Saree", hsnCode = "50072010", quantity = 2.5, unit = "Mtr", price = 1234.5, taxRate = 12.0, discount = 10.0),
            TextilesItem(itemName = "துண்டு", itemNameEn = "Towel", hsnCode = "63026010", quantity = 3.0, unit = "Pcs", price = 99.0, taxRate = 5.0),
        )
        val invoice = InvoiceModel(
            invoiceNumber = "VRM-007",
            customerId = customer.id,
            customerName = customer.name,
            customerAddress = customer.address,
            itemsJson = TextilesCalculation.itemsToJson(items),
            customPreferencesJson = InvoiceDisplayOptions(placeOfSupply = "Kerala").toJson(),
        )
        val json = PrintPayload.forTextilesInvoice(invoice, profile, customer, "ta")
        val gaps = missing(
            json,
            listOf(
                "VRM-007", "Cust Name", "12 Main Street", "Arani", "Tiruvannamalai", "Tamil Nadu", "India",
                "632301", "33CUSTG1234F1Z5", "cust@example.com", "9876543210", "5 Intl Road Dubai",
                "Silk Saree", "பட்டுப்புடவை", "50072010", "2.5", "Mtr", "1234.5", "Towel", "63026010", "Pcs",
                "Kerala", "33SHOPG1234F1Z5",
            )
        )
        assertTrue(gaps.isEmpty(), "Not in the textiles payload: $gaps")
    }

    @Test
    fun factory_bill_carries_every_customer_item_and_charge_field() {
        val items = listOf(FactoryItem(itemName = "ஜரி தடை", itemNameEn = "Jari Thadai", weight = 12.345, price = 150.0))
        val charges = listOf(FactoryAdditionalCharge(name = "Loading", amount = 77.0))
        val invoice = InvoiceModel(
            invoiceNumber = "VRM-012",
            customerId = customer.id,
            customerName = customer.name,
            customerAddress = customer.address,
            itemsJson = FactoryCalculation.itemsToJson(items),
            otherTaxesJson = FactoryCalculation.chargesToJson(charges),
            wastageGrams = 15.0,
            postalCharges = 40.0,
            ahimsaSilkAmount = 55.0,
        )
        val json = PrintPayload.forFactoryInvoice(invoice, profile, customer, "ta")
        val gaps = missing(
            json,
            listOf(
                "VRM-012", "Cust Name", "12 Main Street", "Arani", "Tiruvannamalai", "632301", "33CUSTG1234F1Z5",
                "9876543210", "Jari Thadai", "12.345", "150", "Loading", "77", "15", "40", "55",
            )
        )
        assertTrue(gaps.isEmpty(), "Not in the factory payload: $gaps")
    }

    private fun savedBill(snapshotOf: CustomerModel?) = InvoiceModel(
        invoiceNumber = "VRM-020",
        customerId = customer.id,
        customerName = customer.name,
        customerAddress = customer.city,
        itemsJson = TextilesCalculation.itemsToJson(listOf(TextilesItem(itemName = "x", itemNameEn = "x", price = 10.0))),
        customPreferencesJson = InvoiceDisplayOptions(customer = snapshotOf?.let(CustomerSnapshot::of).orEmpty()).toJson(),
    )

    @Test
    fun snapshot_survives_the_settings_json_round_trip() {
        val back = InvoiceDisplayOptions.fromJson(InvoiceDisplayOptions(customer = CustomerSnapshot.of(customer)).toJson())
        assertEquals(CustomerSnapshot.of(customer), back.customer)
        assertEquals(customer, CustomerSnapshot.toCustomer(back.customer)?.copy(id = customer.id, companyIds = customer.companyIds, createdAt = customer.createdAt, updatedAt = customer.updatedAt))
    }

    @Test
    fun editing_the_customer_later_does_not_change_a_saved_bill() {
        val edited = customer.copy(
            address = mapOf("en" to "NEW ADDRESS"), city = mapOf("en" to "NEWTOWN"),
            gstin = "33NEWGG1234F1Z5", phone = "1111111111",
        )
        val textiles = PrintPayload.forTextilesInvoice(savedBill(customer), profile, edited, "ta")
        assertTrue(textiles.contains("12 Main Street") && textiles.contains("Arani") && textiles.contains("33CUSTG1234F1Z5") && textiles.contains("9876543210"))
        assertFalse(textiles.contains("NEWTOWN") || textiles.contains("NEW ADDRESS") || textiles.contains("33NEWGG1234F1Z5") || textiles.contains("1111111111"))
        val factory = PrintPayload.forFactoryInvoice(savedBill(customer), profile, edited, "ta")
        assertTrue(factory.contains("12 Main Street") && factory.contains("Arani"))
        assertFalse(factory.contains("NEWTOWN") || factory.contains("NEW ADDRESS"))
    }

    @Test
    fun a_deleted_customer_still_prints_from_the_bills_own_copy() {
        val json = PrintPayload.forTextilesInvoice(savedBill(customer), profile, null, "ta")
        assertTrue(listOf("Cust Name", "12 Main Street", "Arani", "632301", "33CUSTG1234F1Z5", "9876543210").all { json.contains(it) })
    }

    @Test
    fun an_older_bill_without_a_copy_keeps_its_saved_city_and_factory_address_stays_out_of_the_city() {
        val old = savedBill(null)
        // Textiles: the saved text is the city, so it prints as the city
        val textiles = PrintPayload.forTextilesInvoice(old, profile, null, "ta")
        assertTrue(textiles.contains("\"city\":{\"ta\":\"ஆரணி\",\"en\":\"Arani\"}"))
        // Factory: the saved text is the address; the template's city fallback must stay empty
        val factoryBill = old.copy(customerAddress = customer.address)
        val factory = PrintPayload.forFactoryInvoice(factoryBill, profile, null, "ta")
        assertTrue(factory.contains("\"address\":{\"ta\":\"12 தெரு\",\"en\":\"12 Main Street\"}"))
        assertTrue(factory.contains("\"customerAddress\":{}"))
        assertTrue(factory.contains("\"city\":{}"))
    }
    @Test
    fun receipt_carries_financial_year() {
        val receipt = ReceiptModel(
            receiptNumber = "RCP/BIZ/01",
            finYear = "2026-27",
            amount = 5000.0,
            receiptDate = DateUtils.parseToEpochMillis("15/05/2026")!!
        )
        val json = PrintPayload.forReceipt(
            receipt = receipt,
            profile = profile,
            invoiceNumbers = listOf("VRM-001"),
            isCoolie = false,
            appLanguage = "ta"
        )
        assertTrue(json.contains("\"finYear\":\"2026-27\""), "finYear not in receipt payload: $json")
        // Legacy 4-digit format "2026" also normalizes to "2026-27"
        val legacy = receipt.copy(finYear = "2026")
        val jsonLegacy = PrintPayload.forReceipt(legacy, profile, listOf("VRM-001"), false, "ta")
        assertTrue(jsonLegacy.contains("\"finYear\":\"2026-27\""), "legacy finYear not normalized: $jsonLegacy")

        // Empty finYear also normalizes from date
        val emptyFy = receipt.copy(finYear = "")
        val jsonEmpty = PrintPayload.forReceipt(emptyFy, profile, listOf("VRM-001"), false, "ta")
        assertTrue(jsonEmpty.contains("\"finYear\":\"2026-27\""), "empty finYear not derived: $jsonEmpty")
    }
}