package com.elvan.udukkai.core.print

import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.core.utils.MiniJson
import com.elvan.udukkai.data.model.CustomerModel
import com.elvan.udukkai.data.model.InvoiceDisplayOptions
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.model.ReceiptModel
import com.elvan.udukkai.factory.ui.screens.editor.FactoryCalculation
import com.elvan.udukkai.data.settings.BusinessProfile
import com.elvan.udukkai.data.settings.PaymentAccount
import com.elvan.udukkai.textiles.ui.screens.editor.TextilesCalculation
import java.io.File
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Builds the JSON payload sent to the React print bundle (react/print).
 *
 * The payload keeps KMP's own English field names and `{ "ta": ..., "en": ... }`
 * language maps. All renaming to the React template's field names happens in
 * react/print/adapter.ts, so the React templates stay untouched.
 */
object PrintPayload {

    fun forTextilesInvoice(
        invoice: InvoiceModel,
        profile: BusinessProfile,
        customer: CustomerModel?,
        appLanguage: String
    ): String {
        val options = InvoiceDisplayOptions.fromJson(invoice.customPreferencesJson)
        val items = TextilesCalculation.itemsFromJson(invoice.itemsJson)
        val totals = TextilesCalculation.calculate(
            items = items,
            globalDiscountValue = invoice.globalDiscountValue.takeIf { it > 0 } ?: options.globalDiscountValue,
            globalDiscountType = invoice.globalDiscountType.ifEmpty { options.globalDiscountType },
            businessState = profile.state["en"] ?: profile.state.values.firstOrNull().orEmpty(),
            customerState = options.placeOfSupply,
            country = profile.country["en"] ?: "India",
            taxInclusive = options.taxInclusive
        )
        val primary = profile.primaryLanguage.ifEmpty { "ta" }
        val secondary = profile.secondaryLanguage.ifEmpty { "en" }

        val json = JsonWriter()
        json.obj {
            field("docType", "gst-invoice")
            field("appLanguage", appLanguage)
            field("profile") { profile(profile) }
            field("customer") { if (customer != null) customer(customer) else nullValue() }
            field("invoice") {
                obj {
                    field("invoiceNumber", invoice.invoiceNumber)
                    field("invoiceType", invoice.invoiceType.ifEmpty { "tax-invoice" })
                    field("invoiceDate", isoDate(invoice.invoiceDate))
                    field("customerName") { langMap(invoice.customerName) }
                    field("customerAddress") { langMap(invoice.customerAddress) }
                    field("placeOfSupply", options.placeOfSupply)
                    field("placeOfSupplyTa", options.placeOfSupplyTa)
                    // Per-bill options, same names as React's bill.data.invoiceOptions
                    rawField("options", MiniJson.parseObject(invoice.customPreferencesJson))
                    field("items") {
                        array(items) { item ->
                            obj {
                                field("id", item.id)
                                field("name") {
                                    langMap(item.languageMap.ifEmpty {
                                        mapOf(primary to item.itemName, secondary to item.itemNameEn)
                                    })
                                }
                                field("hsnCode", item.hsnCode)
                                field("quantity", item.quantity)
                                field("unit", item.unit)
                                field("price", item.price)
                                field("taxRate", item.taxRate)
                                field("discountAmount", item.discountAmount)
                            }
                        }
                    }
                    field("totals") {
                        obj {
                            field("subtotal", totals.subtotal)
                            field("totalDiscount", totals.totalDiscount)
                            field("cgst", totals.cgst)
                            field("sgst", totals.sgst)
                            field("igst", totals.igst)
                            field("roundOff", totals.roundOff)
                            field("total", totals.grandTotal)
                        }
                    }
                }
            }
        }
        return json.toString()
    }

    /**
     * Receipt in either app: Textiles -> "gst-receipt", Factory -> "coolie-receipt". With bilingual
     * printing on, Factory receipts use the same bilingual layout as Textiles.
     */
    fun forReceipt(
        receipt: ReceiptModel,
        profile: BusinessProfile,
        invoiceNumbers: List<String>,
        isCoolie: Boolean,
        appLanguage: String
    ): String {
        val json = JsonWriter()
        json.obj {
            field("docType", if (isCoolie && !profile.isBilingual) "coolie-receipt" else "gst-receipt")
            field("appLanguage", appLanguage)
            field("profile") { profile(profile) }
            field("receipt") {
                obj {
                    field("receiptNumber", receipt.receiptNumber)
                    field("receiptDate", isoDate(receipt.receiptDate))
                    field("amount", receipt.amount)
                    field("paymentMode", receipt.paymentMode)
                    field("transactionNumber", receipt.transactionNumber.orEmpty())
                    field("notes", receipt.notes)
                    field("customerName") { langMap(receipt.customerName) }
                    field("invoiceNumbers") { array(invoiceNumbers) { string(it) } }
                }
            }
        }
        return json.toString()
    }

    /** Factory (Coolie) invoice -> "coolie-invoice". */
    fun forFactoryInvoice(
        invoice: InvoiceModel,
        profile: BusinessProfile,
        customer: CustomerModel?,
        appLanguage: String
    ): String {
        val items = FactoryCalculation.itemsFromJson(invoice.itemsJson)
        val charges = FactoryCalculation.chargesFromJson(invoice.otherTaxesJson)
        val totals = FactoryCalculation.calculate(
            items = items,
            setharamGrams = invoice.wastageGrams,
            thabaalThogai = invoice.postalCharges,
            ahimsaPattuThogai = invoice.ahimsaSilkAmount,
            charges = charges
        )
        val json = JsonWriter()
        json.obj {
            field("docType", "coolie-invoice")
            field("appLanguage", appLanguage)
            field("profile") { profile(profile) }
            field("customer") { if (customer != null) customer(customer) else nullValue() }
            field("invoice") {
                obj {
                    field("id", invoice.id.toString())
                    field("invoiceNumber", invoice.invoiceNumber)
                    field("invoiceDate", isoDate(invoice.invoiceDate))
                    field("customerName") { langMap(invoice.customerName) }
                    field("customerAddress") { langMap(invoice.customerAddress) }
                    field("items") {
                        array(items) { item ->
                            obj {
                                field("name") {
                                    langMap(item.languageMap.ifEmpty { mapOf("ta" to item.itemName, "en" to item.itemNameEn) })
                                }
                                field("weight", item.weight)
                                field("price", item.price)
                            }
                        }
                    }
                    field("wastageGrams", invoice.wastageGrams)
                    field("postalCharges", invoice.postalCharges)
                    field("ahimsaSilkAmount", invoice.ahimsaSilkAmount)
                    field("charges") {
                        array(charges) { ch ->
                            obj {
                                field("name", ch.name)
                                field("amount", ch.amount)
                            }
                        }
                    }
                    field("grandTotal", totals.grandTotal)
                }
            }
        }
        return json.toString()
    }

    private fun JsonWriter.profile(p: BusinessProfile) = obj {
        field("id", p.id?.toString() ?: "")
        field("shortName", p.shortName)
        field("primaryLanguage", p.primaryLanguage)
        field("secondaryLanguage", p.secondaryLanguage)
        field("isBilingual", p.isBilingual)
        field("businessName") { langMap(p.businessName) }
        field("tagline") { langMap(p.tagline) }
        field("address") { langMap(p.address) }
        field("city") { langMap(p.city) }
        field("district") { langMap(p.district) }
        field("state") { langMap(p.state) }
        field("country") { langMap(p.country) }
        field("pinCode", p.pinCode)
        field("phone1", p.phone1)
        field("phone2", p.phone2)
        field("email", p.email)
        field("gstin", p.gstin)
        field("pan", p.pan)
        field("logo", imageSource(p.logo))
        field("wideLogo", imageSource(p.wideLogo))
        field("headerLayout", p.headerLayout)
        field("signature", imageSource(p.signature))
        field("signatoryName", p.fullSignatoryName)
        field("themeColor", p.themeColor)
        field("upiId", p.upiId)
        field("bankName") { langMap(p.bankName) }
        field("branch") { langMap(p.branch) }
        field("accountNumber", p.accountNumber)
        field("ifsc", p.ifsc)
        rawField("printSettings", MiniJson.parseObject(p.printSettingsJson))
        field("paymentAccounts") {
            array(PaymentAccount.parseList(p.paymentAccountsJson)) { acc ->
                obj {
                    field("id", acc.id)
                    field("label", acc.label)
                    field("bankName") { langMap(acc.bankName) }
                    field("branch") { langMap(acc.branch) }
                    field("accountNumber", acc.accountNumber)
                    field("ifsc", acc.ifsc)
                    field("upiId", acc.upiId)
                    field("isDefault", acc.isDefault)
                    field("isActive", acc.isActive)
                }
            }
        }
    }

    private fun JsonWriter.customer(c: CustomerModel) = obj {
        field("name") { langMap(c.name) }
        field("address") { langMap(c.address) }
        field("city") { langMap(c.city) }
        field("district") { langMap(c.district) }
        field("state") { langMap(c.state) }
        field("country") { langMap(c.country) }
        field("pinCode", c.pinCode)
        field("gstin", c.gstin)
        field("email", c.email)
        field("phone", c.phone)
    }

    private fun JsonWriter.langMap(map: Map<String, String>) = obj {
        map.forEach { (k, v) -> field(k, v) }
    }

    /** yyyy-MM-dd of the same calendar day KMP shows (DateUtils works in UTC). */
    private fun isoDate(millis: Long): String {
        val (d, m, y) = DateUtils.formatDate(millis).split("/")
        return "$y-$m-$d"
    }

    /** Local image files are inlined as data URLs so the WebView can draw them. */
    @OptIn(ExperimentalEncodingApi::class)
    private fun imageSource(path: String): String {
        if (path.isBlank() || path.startsWith("data:") || path.startsWith("http")) return path
        return try {
            val file = File(path)
            if (!file.isFile) {
                // Raw base64 (older mock data stores images without a data: prefix)
                val looksBase64 = path.length > 64 && Regex("^[A-Za-z0-9+/=\\s]+$").matches(path)
                return if (looksBase64) "data:image/png;base64,$path" else ""
            }
            val mime = when (file.extension.lowercase()) {
                "png" -> "image/png"
                "webp" -> "image/webp"
                "gif" -> "image/gif"
                "svg" -> "image/svg+xml"
                else -> "image/jpeg"
            }
            "data:$mime;base64," + Base64.encode(file.readBytes())
        } catch (_: Exception) {
            ""
        }
    }
}

/** Minimal streaming JSON writer (the project has no JSON library). */
internal class JsonWriter {
    private val sb = StringBuilder()
    private val needsComma = ArrayDeque<Boolean>()

    private fun separate() {
        if (needsComma.isNotEmpty()) {
            if (needsComma.last()) sb.append(',')
            needsComma[needsComma.lastIndex] = true
        }
    }

    fun obj(body: JsonWriter.() -> Unit) {
        sb.append('{')
        needsComma.addLast(false)
        body()
        needsComma.removeLast()
        sb.append('}')
    }

    fun <T> array(values: List<T>, each: JsonWriter.(T) -> Unit) {
        sb.append('[')
        needsComma.addLast(false)
        values.forEach { v ->
            separate()
            needsComma.addLast(false)
            each(v)
            needsComma.removeLast()
        }
        needsComma.removeLast()
        sb.append(']')
    }

    fun nullValue() {
        sb.append("null")
    }

    /** Writes an already-parsed JSON value (map / list / primitive) as-is. */
    fun rawField(name: String, value: Any?) = field(name) { sb.append(MiniJson.stringify(value)) }

    fun field(name: String, value: JsonWriter.() -> Unit) {
        separate()
        string(name)
        sb.append(':')
        needsComma.addLast(false)
        value()
        needsComma.removeLast()
    }

    fun field(name: String, value: String?) = field(name) { if (value == null) nullValue() else string(value) }
    fun field(name: String, value: Boolean) = field(name) { sb.append(value) }
    fun field(name: String, value: Double) = field(name) {
        sb.append(if (value.isFinite()) value.toString() else "0")
    }

    fun string(s: String) {
        sb.append('"')
        for (ch in s) {
            when {
                ch == '"' -> sb.append("\\\"")
                ch == '\\' -> sb.append("\\\\")
                ch == '\n' -> sb.append("\\n")
                ch == '\r' -> sb.append("\\r")
                ch == '\t' -> sb.append("\\t")
                ch < ' ' || ch == ' ' || ch == ' ' ->
                    sb.append("\\u").append(ch.code.toString(16).padStart(4, '0'))
                else -> sb.append(ch)
            }
        }
        sb.append('"')
    }

    override fun toString(): String = sb.toString()
}
