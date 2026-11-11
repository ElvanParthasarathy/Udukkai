package com.elvan.udukkai.data.model

/**
 * The customer details a bill printed when it was saved, kept on the bill (in its settings JSON) so that editing or
 * deleting the customer later does not change a bill that was already given out. Flat keys: "city.ta", "pinCode"...
 */
object CustomerSnapshot {
    fun forInvoice(invoice: InvoiceModel, currentCustomer: CustomerModel?, legacyAddressIsCity: Boolean): CustomerModel {
        val fields = InvoiceDisplayOptions.fromJson(invoice.customPreferencesJson).customer
        val saved = toCustomer(fields)
        if (saved != null) {
            // Older Factory imports saved the town as their entire address snapshot.
            val legacyTownOnly = !legacyAddressIsCity && fields.keys.all { it.startsWith("name.") || it.startsWith("city.") }
            if (!legacyTownOnly || currentCustomer == null) return saved
            return saved.copy(
                address = currentCustomer.address, city = saved.city.ifEmpty { currentCustomer.city },
                district = currentCustomer.district, state = currentCustomer.state, country = currentCustomer.country,
                internationalAddress = currentCustomer.internationalAddress, pinCode = currentCustomer.pinCode
            )
        }
        return currentCustomer?.copy(name = invoice.customerName.ifEmpty { currentCustomer.billName() })
            ?: if (legacyAddressIsCity) CustomerModel(name = invoice.customerName, city = invoice.customerAddress)
            else CustomerModel(name = invoice.customerName, address = invoice.customerAddress)
    }

    /** Reports must classify the customer as saved on the bill, including a deliberately blank GSTIN. */
    fun invoiceGstin(invoice: InvoiceModel, currentCustomer: CustomerModel?): String {
        val saved = InvoiceDisplayOptions.fromJson(invoice.customPreferencesJson).customer
        return if (saved.isNotEmpty()) saved["gstin"].orEmpty() else currentCustomer?.gstin.orEmpty()
    }

    private fun maps(c: CustomerModel) = mapOf(
        "name" to c.billName(), "address" to c.address, "city" to c.city, "district" to c.district,
        "state" to c.state, "country" to c.country, "internationalAddress" to c.internationalAddress
    )

    fun of(c: CustomerModel): Map<String, String> = buildMap {
        put("snapshotVersion", "1")
        maps(c).forEach { (field, byLang) -> byLang.forEach { (lang, v) -> if (v.isNotBlank()) put("$field.$lang", v) } }
        mapOf("pinCode" to c.pinCode, "gstin" to c.gstin, "email" to c.email, "phone" to c.phone)
            .forEach { (k, v) -> if (v.isNotBlank()) put(k, v) }
    }

    /** Null when the bill has no snapshot (a bill saved before this existed). */
    fun toCustomer(s: Map<String, String>): CustomerModel? {
        if (s.isEmpty()) return null
        fun group(field: String) = s.filterKeys { it.startsWith("$field.") }.mapKeys { it.key.removePrefix("$field.") }
        return CustomerModel(
            name = group("name"), address = group("address"), city = group("city"), district = group("district"),
            state = group("state"), country = group("country"), internationalAddress = group("internationalAddress"),
            pinCode = s["pinCode"].orEmpty(), gstin = s["gstin"].orEmpty(), email = s["email"].orEmpty(), phone = s["phone"].orEmpty()
        )
    }
}
