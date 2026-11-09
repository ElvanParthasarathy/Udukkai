package com.elvan.udukkai.data.mock

import com.elvan.udukkai.factory.ui.screens.editor.FactoryCalculation
import com.elvan.udukkai.factory.ui.screens.editor.FactoryItem
import com.elvan.udukkai.textiles.ui.screens.editor.TextilesCalculation
import com.elvan.udukkai.textiles.ui.screens.editor.TextilesItem

/**
 * Made-up English businesses for the Play Store screenshots (debug dev menu only). Nothing here is real data:
 * the names, GSTIN, bank and phone numbers are invented. One language only, so the screenshots are not bilingual.
 */
private fun en(s: String): Map<String, String> = mapOf("en" to s)

// ---------------- Textiles ----------------
val demoTextilesProfiles = listOf(
    SeedProfile(
        shortName = "ELS", businessName = en("Elvan Silks"),
        phone1 = "9876543210", phone2 = "", email = "hello@elvansilks.example", gstin = "33AAAAA0000A1Z5", pan = "",
        address = en("12, Weavers Street"), city = en("Kanchipuram"), district = en("Kanchipuram"), state = en("Tamil Nadu"),
        country = en("India"), pinCode = "631501",
        bankName = en("Sample Bank"), branch = en("Kanchipuram"), accountNumber = "000012345678", ifsc = "SMPL0000001",
        logo = "", wideLogo = "", headerLayout = "small", signature = "", signatoryName = "E. Elvan",
        tagline = en("Handloom Silk Sarees"), themeColor = "#388e3c",
        printSettings = mapOf("signatoryNames" to mapOf("en" to "E. Elvan"))
    )
)

val demoTextilesItems = listOf(
    SeedItem("i1", en("Kanchipuram Silk Saree"), "50072010", 12500.0, 5.0, "quantity", "Nos"),
    SeedItem("i2", en("Soft Silk Saree"), "50072010", 6800.0, 5.0, "quantity", "Nos"),
    SeedItem("i3", en("Bridal Silk Saree"), "50072010", 21000.0, 5.0, "quantity", "Nos"),
    SeedItem("i4", en("Raw Silk"), "50072000", 5800.0, 0.0, "weight", "kg"),
    SeedItem("i5", en("Silk Dhoti"), "50072010", 3200.0, 5.0, "quantity", "Nos")
)

val demoTextilesCustomers = listOf(
    SeedCustomer("c1", en("Sri Ganesh Textiles"), en("45, Main Bazaar"), en("Salem"), en("Salem"), en("Tamil Nadu"), en("India"), "636001", "33BBBBB1111B1Z6", "ganesh@example.com", "9876500001"),
    SeedCustomer("c2", en("Meena Fashions"), en("8, Cross Road"), en("Madurai"), en("Madurai"), en("Tamil Nadu"), en("India"), "625001", "", "", "9876500002"),
    SeedCustomer("c3", en("Anand Silks"), en("22, Gandhi Street"), en("Coimbatore"), en("Coimbatore"), en("Tamil Nadu"), en("India"), "641001", "33CCCCC2222C1Z7", "", "9876500003")
)

private fun tInv(no: String, date: String, cust: SeedCustomer, vararg lines: Triple<SeedItem, Double, Double>): SeedTextilesInvoice {
    val l = lines.map { (it, qty, disc) ->
        SeedLine(it.ref, it.name, it.hsnCode, qty, it.unit, it.price, it.taxRate, disc, "%")
    }
    val totals = TextilesCalculation.calculate(
        l.map { TextilesItem(quantity = it.quantity, price = it.price, taxRate = it.taxRate, discount = it.discount, discountType = it.discountType) },
        businessState = "Tamil Nadu", customerState = "Tamil Nadu", country = "India"
    )
    return SeedTextilesInvoice(
        company = "ELS", number = no, date = date, type = "tax-invoice", customerRef = cust.ref, lines = l,
        total = totals.grandTotal, discount = totals.totalDiscount, taxAmount = totals.totalTax,
        globalDiscountValue = 0.0, globalDiscountType = "%", taxInclusive = false, showHSN = true, showDiscountColumn = true,
        measureMode = "", terms = "", notes = "",
        customer = mapOf("name.en" to cust.name.getValue("en"), "address.en" to cust.address.getValue("en"), "city.en" to cust.city.getValue("en"),
            "state.en" to "Tamil Nadu", "country.en" to "India", "pinCode" to cust.pinCode, "gstin" to cust.gstin)
    )
}

private val dt = demoTextilesItems
private val dc = demoTextilesCustomers

val demoTextilesInvoices = listOf(
    tInv("ELS-01", "2026-08-04", dc[0], Triple(dt[0], 4.0, 0.0), Triple(dt[1], 6.0, 5.0)),
    tInv("ELS-02", "2026-08-19", dc[1], Triple(dt[2], 2.0, 0.0)),
    tInv("ELS-03", "2026-09-07", dc[2], Triple(dt[1], 10.0, 0.0), Triple(dt[4], 8.0, 0.0)),
    tInv("ELS-04", "2026-09-26", dc[0], Triple(dt[0], 3.0, 0.0), Triple(dt[2], 1.0, 0.0)),
    tInv("ELS-05", "2026-10-12", dc[2], Triple(dt[3], 24.5, 0.0)),
    tInv("ELS-06", "2026-10-30", dc[1], Triple(dt[1], 5.0, 0.0), Triple(dt[4], 4.0, 10.0)),
    tInv("ELS-07", "2026-11-05", dc[0], Triple(dt[0], 6.0, 0.0), Triple(dt[1], 4.0, 0.0))
)

val demoTextilesReceipts = listOf(
    SeedReceipt("ELS", "RCP/ELS/01", "2026-08-20", "c1", dc[0].name, dc[0].city, 50000.0, "bank_transfer", "UTR1001", "", "ELS-01"),
    SeedReceipt("ELS", "RCP/ELS/02", "2026-09-10", "c3", dc[2].name, dc[2].city, 40000.0, "upi", "UPI2002", "", "ELS-03"),
    SeedReceipt("ELS", "RCP/ELS/03", "2026-10-15", "c3", dc[2].name, dc[2].city, 30000.0, "cash", "", "Part payment", "ELS-05")
)

// ---------------- Factory ----------------
val demoFactoryProfiles = listOf(
    SeedProfile(
        shortName = "NVS", businessName = en("Navil Silks"),
        phone1 = "9876543211", phone2 = "", email = "contact@navilsilks.example", gstin = "", pan = "",
        address = en("7, Mill Road"), city = en("Arani"), district = en("Tiruvannamalai"), state = en("Tamil Nadu"),
        country = en("India"), pinCode = "632301",
        bankName = en("Sample Bank"), branch = en("Arani"), accountNumber = "000098765432", ifsc = "SMPL0000002",
        logo = "", wideLogo = "", headerLayout = "small", signature = "", signatoryName = "N. Navil",
        tagline = emptyMap(), themeColor = "#388e3c",
        printSettings = mapOf("signatoryNames" to mapOf("en" to "N. Navil"))
    )
)

val demoFactoryItems = listOf(
    SeedItem("f1", en("2-Ply Twisting Charges"), "", 0.0, 0.0, "quantity", "Nos"),
    SeedItem("f2", en("3-Ply Twisting Charges"), "", 0.0, 0.0, "quantity", "Nos"),
    SeedItem("f3", en("Zari Twisting Charges"), "", 0.0, 0.0, "quantity", "Nos")
)

val demoFactoryCustomers = listOf(
    SeedCustomer("k1", en("Sundaram Silks"), emptyMap(), en("Kumbakonam"), emptyMap(), emptyMap(), emptyMap(), "", "", "", "9876500011"),
    SeedCustomer("k2", en("Kaveri Weaving Works"), emptyMap(), en("Arani"), emptyMap(), emptyMap(), emptyMap(), "", "", "", "9876500012")
)

private fun fInv(no: String, date: String, cust: SeedCustomer, wastage: Double, courier: Double, vararg lines: Triple<SeedItem, Double, Double>): SeedFactoryInvoice {
    val l = lines.map { (it, kg, rate) -> SeedFactoryLine(it.ref, it.name, kg, rate) }
    val total = FactoryCalculation.calculate(l.map { FactoryItem(weight = it.kg, price = it.rate) }, wastage, courier, 0.0).grandTotal
    return SeedFactoryInvoice(
        company = "NVS", number = no, date = date, customerRef = cust.ref, lines = l, total = total,
        wastageGrams = wastage, postalCharges = courier, ahimsaSilkAmount = 0.0,
        bankDetails = mapOf("bankDetails" to "Sample Bank, Arani", "accountNo" to "000098765432", "ifsc" to "SMPL0000002", "showBank" to "true", "showIfsc" to "true"),
        customer = mapOf("name.en" to cust.name.getValue("en"), "city.en" to cust.city.getValue("en"))
    )
}

private val df = demoFactoryItems
private val dk = demoFactoryCustomers

val demoFactoryInvoices = listOf(
    fInv("NVS-01", "2026-08-06", dk[0], 950.0, 0.0, Triple(df[0], 26.3, 610.0)),
    fInv("NVS-02", "2026-08-21", dk[1], 1100.0, 0.0, Triple(df[1], 30.1, 380.0)),
    fInv("NVS-03", "2026-09-09", dk[0], 1250.0, 600.0, Triple(df[0], 24.0, 610.0), Triple(df[2], 12.5, 540.0)),
    fInv("NVS-04", "2026-09-28", dk[1], 900.0, 0.0, Triple(df[1], 27.8, 380.0)),
    fInv("NVS-05", "2026-10-14", dk[0], 1400.0, 700.0, Triple(df[0], 31.2, 610.0)),
    fInv("NVS-06", "2026-10-29", dk[1], 1050.0, 0.0, Triple(df[2], 22.4, 540.0)),
    fInv("NVS-07", "2026-11-06", dk[0], 1180.0, 650.0, Triple(df[1], 29.6, 380.0), Triple(df[0], 10.0, 610.0))
)

val demoFactoryReceipts = listOf(
    SeedReceipt("NVS", "RCP/NVS/01", "2026-08-25", "k1", dk[0].name, dk[0].city, 16000.0, "bank_transfer", "UTR3001", "", "NVS-01"),
    SeedReceipt("NVS", "RCP/NVS/02", "2026-09-15", "k2", dk[1].name, dk[1].city, 11000.0, "upi", "UPI4002", "", "NVS-02"),
    SeedReceipt("NVS", "RCP/NVS/03", "2026-10-20", "k1", dk[0].name, dk[0].city, 20000.0, "cash", "", "Part payment", "NVS-03")
)
