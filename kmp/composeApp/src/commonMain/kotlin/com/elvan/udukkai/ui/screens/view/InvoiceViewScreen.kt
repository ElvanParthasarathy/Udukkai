package com.elvan.udukkai.ui.screens.view

import com.elvan.udukkai.data.model.ItemNames
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.elvan.udukkai.core.print.billBusinessFolder
import com.elvan.udukkai.core.print.invoicePdfName
import com.elvan.udukkai.core.utils.BillRules
import com.elvan.udukkai.core.utils.CurrencyUtils
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.core.utils.NumberToWords
import com.elvan.udukkai.data.model.InvoiceDisplayOptions
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.model.CustomerModel
import com.elvan.udukkai.data.model.CustomerSnapshot
import com.elvan.udukkai.data.model.fullAddress
import com.elvan.udukkai.data.model.PaymentType
import com.elvan.udukkai.data.model.ReceiptModel
import com.elvan.udukkai.data.settings.DataLanguageDisplay
import com.elvan.udukkai.data.settings.BusinessProfile
import com.elvan.udukkai.factory.ui.screens.editor.FactoryCalculation
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.textiles.ui.screens.editor.TextilesCalculation
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.ui.components.shell.ElvanPopupMenuItem
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.preview.PreviewScreen

/**
 * Invoice view (பட்டியல் பார்வை): a native page that opens at once, like Zoho's invoice details.
 * Status and amount on top, the customer, the receipts that paid it, the bill's numbers, its lines and totals.
 * The paper (PDF) is made behind it for Open, Print and Share in the bottom bar.
 */
@Composable
fun InvoiceViewScreen(
    invoice: InvoiceModel,
    profile: BusinessProfile,
    allProfiles: List<BusinessProfile> = emptyList(),
    isPaid: Boolean = false,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onPrint: (() -> Unit)? = null,
    onCopy: (() -> Unit)? = null,
    onConvertToTaxInvoice: (() -> Unit)? = null,
    /** The receipts that paid this bill: each opens from the page. */
    receipts: List<ReceiptModel> = emptyList(),
    onOpenReceipt: ((ReceiptModel) -> Unit)? = null,
    /** A bill still due: makes a receipt for it with the customer and amount filled in. */
    onRecordPayment: (() -> Unit)? = null,
    onOpenCustomer: (() -> Unit)? = null,
    onDelete: (() -> ViewDeleteResult)? = null,
    isFactory: Boolean = false,
    printPayload: ((activeProfile: BusinessProfile) -> String)? = null,
    currentCustomer: CustomerModel? = null
) {
    val activeProfile = (if (invoice.companyId != null) allProfiles.find { it.id == invoice.companyId } else null) ?: profile
    val appLanguage = com.elvan.udukkai.localization.LanguageManager.activeLanguageCode
    val payload = remember(invoice, activeProfile, appLanguage, currentCustomer) { printPayload?.invoke(activeProfile) }
    val isProforma = invoice.invoiceType == "proforma"
    val copyTitle = K.copyBtn.tr()
    val deleteItem = rememberViewDeleteItem(onDelete, K.invoiceDeletedMsg.tr(), onBack)

    PreviewScreen(
        title = invoice.invoiceNumber,
        onBack = onBack,
        onEdit = if (isPaid) null else onEdit,
        menuItems = buildList {
            if (onCopy != null) add(ElvanPopupMenuItem(copyTitle, MaterialSymbols.Rounded.ContentCopy, onCopy))
            deleteItem?.let { add(it) }
        },
        printPayload = payload,
        printFileName = invoicePdfName(invoice),
        businessFolder = billBusinessFolder(activeProfile),
        showPreparationOverlay = true,
        body = { InvoiceBody(invoice, activeProfile, isPaid, isProforma, receipts, onOpenReceipt, onRecordPayment, onOpenCustomer, isFactory, onConvertToTaxInvoice, currentCustomer) }
    )
}

private const val DAY = 24L * 60 * 60 * 1000

@Composable
private fun InvoiceBody(
    invoice: InvoiceModel,
    profile: BusinessProfile,
    isPaid: Boolean,
    isProforma: Boolean,
    receipts: List<ReceiptModel>,
    onOpenReceipt: ((ReceiptModel) -> Unit)?,
    onRecordPayment: (() -> Unit)?,
    onOpenCustomer: (() -> Unit)?,
    isFactory: Boolean,
    onConvertToTaxInvoice: (() -> Unit)?,
    currentCustomer: CustomerModel?
) {
    val language = DataLanguageDisplay(profile)
    val billedCustomer = remember(invoice, currentCustomer, isFactory) {
        CustomerSnapshot.forInvoice(invoice, currentCustomer, legacyAddressIsCity = !isFactory)
    }
    val name = language.first(billedCustomer.name).ifBlank { "-" }
    val secondName = language.second(billedCustomer.name)
    val town = language.first(billedCustomer.city)

    val options = remember(invoice.customPreferencesJson) { InvoiceDisplayOptions.fromJson(invoice.customPreferencesJson) }
    val chargeLabels = K.courierCharge.tr() to K.ahimsaSilk.tr()
    val summary = remember(invoice, isFactory, profile.primaryLanguage, profile.secondaryLanguage, profile.isBilingual, chargeLabels) { billSummary(invoice, options, isFactory, language, chargeLabels) }
    val daysPending = ((DateUtils.todayMillis() - invoice.invoiceDate) / DAY).toInt()

    val paidLabel = K.billFullyPaid.tr()
    val dueLabel = K.balanceDue.tr()
    val totalLabel = K.grandTotal.tr()
    val pendingNote = K.pendingForDays.tr()
    val itemsTitle = K.items.tr()
    val receiptsTitle = K.receipts.tr()
    val recordPayment = K.recordPayment.tr()
    val convertTitle = K.convertToTaxInvoice.tr()
    val proformaLabel = K.proforma.tr()
    val billNoLabel = if (isProforma) K.proformaNumber.tr() else K.billNo.tr()
    val dateLabel = K.date.tr()
    val fyLabel = K.thisFinYear.tr()
    val placeLabel = K.placeOfSupply.tr()
    val subtotalLabel = K.subtotal.tr()
    val discountLabel = K.discount.tr()
    val roundLabel = K.roundOff.tr()
    val paidRowLabel = K.statusPaid.tr()
    val wordsTitle = K.amountInWords.tr()
    val notesTitle = K.remarks.tr()
    val words = remember(invoice.totalAmount, language.languages) { language.lines { NumberToWords.forLanguage(invoice.totalAmount, it) } }

    DocPage(horizontalPadding = Dimens.ContentPadding) {
        item(key = "status") {
            when {
                isProforma -> DocStatusHeader(DocTone.Neutral, proformaLabel, invoice.totalAmount, note = totalLabel)
                isPaid -> DocStatusHeader(DocTone.Paid, paidLabel, invoice.totalAmount)
                else -> DocStatusHeader(
                    DocTone.Due, dueLabel, invoice.totalAmount,
                    note = if (daysPending > 0) pendingNote.replace("%s", daysPending.toString()) else null
                )
            }
        }
        if (!isPaid && !isProforma && onRecordPayment != null) {
            item(key = "record") { DocPrimaryButton(MaterialSymbols.Rounded.Add, recordPayment, onRecordPayment) }
        }
        if (isProforma && onConvertToTaxInvoice != null) {
            item(key = "convert") { DocPrimaryButton(MaterialSymbols.Rounded.SwapHoriz, convertTitle, onConvertToTaxInvoice) }
        }
        item(key = "customer") { DocCustomerCard(name, secondName, town, onOpenCustomer) }
        if (receipts.isNotEmpty()) {
            item(key = "receipts") {
                DocSection("$receiptsTitle (${receipts.size})") {
                    receipts.forEachIndexed { i, r ->
                        if (i > 0) DocDivider()
                        DocLinkRow(
                            icon = MaterialSymbols.Rounded.ReceiptFill,
                            title = r.receiptNumber,
                            subtitle = DateUtils.formatEpochMillis(r.receiptDate) + " • " + PaymentType.fromStored(r.paymentMode).label(),
                            amount = r.amount,
                            onClick = onOpenReceipt?.let { open -> { open(r) } },
                            tint = com.elvan.udukkai.theme.rememberShellColors().receiptColor
                        )
                    }
                }
            }
        }
        item(key = "info") {
            DocSection(null) {
                DocRow(billNoLabel, invoice.invoiceNumber)
                DocDivider()
                DocRow(dateLabel, DateUtils.formatEpochMillis(invoice.invoiceDate))
                DocDivider()
                DocRow(fyLabel, DateUtils.financialYearLabel(invoice.invoiceDate))
                DocDivider()
                DocRow(placeLabel, language.first(options.placeOfSupplyLanguages))
            }
        }
        if (summary.lines.isNotEmpty()) {
            item(key = "lines") {
                DocSection("$itemsTitle (${summary.lines.size})") {
                    summary.lines.forEachIndexed { i, line ->
                        if (i > 0) DocDivider()
                        DocLineRow(line)
                    }
                }
            }
        }
        item(key = "totals") {
            DocSection(null) {
                DocRow(subtotalLabel, CurrencyUtils.formatInr(summary.subtotal))
                if (summary.discount > 0.004) DocRow(discountLabel, "− " + CurrencyUtils.formatInr(summary.discount))
                summary.extras.forEach { (label, amount) -> DocRow(label, CurrencyUtils.formatInr(amount)) }
                if (summary.cgst > 0.004) DocRow("CGST", CurrencyUtils.formatInr(summary.cgst))
                if (summary.sgst > 0.004) DocRow("SGST", CurrencyUtils.formatInr(summary.sgst))
                if (summary.igst > 0.004) DocRow("IGST", CurrencyUtils.formatInr(summary.igst))
                if (kotlin.math.abs(summary.roundOff) > 0.004) DocRow(roundLabel, (if (summary.roundOff < 0) "− " else "+ ") + CurrencyUtils.formatInr(kotlin.math.abs(summary.roundOff)))
                DocDivider()
                DocRow(totalLabel, CurrencyUtils.formatInr(invoice.totalAmount), strong = true)
                if (isPaid) DocRow(paidRowLabel, "− " + CurrencyUtils.formatInr(invoice.totalAmount), valueColor = androidx.compose.ui.graphics.Color(0xFF16A34A))
            }
        }
        item(key = "words") { DocTextCard(wordsTitle, words) }
        if (invoice.notes.isNotBlank()) {
            item(key = "notes") { DocTextCard(notesTitle, listOf(invoice.notes to true)) }
        }
    }
}

/** What the totals card shows, worked out the same way the paper does. */
private data class BillSummary(
    val lines: List<BillLine>,
    val subtotal: Double,
    val discount: Double,
    val extras: List<Pair<String, Double>>,
    val cgst: Double,
    val sgst: Double,
    val igst: Double,
    val roundOff: Double
)

private fun taxPart(json: String, key: String): Double =
    Regex("\"$key\"\\s*:\\s*([-+0-9.eE]+)").find(json)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0

private fun billSummary(invoice: InvoiceModel, options: InvoiceDisplayOptions, isFactory: Boolean, language: DataLanguageDisplay, chargeLabels: Pair<String, String>): BillSummary {
    val cgst = taxPart(invoice.taxDataJson, "cgst")
    val sgst = taxPart(invoice.taxDataJson, "sgst")
    val igst = taxPart(invoice.taxDataJson, "igst")
    val tax = cgst + sgst + igst
    return if (isFactory) {
        val items = FactoryCalculation.itemsFromJson(invoice.itemsJson)
        val lines = items.map {
            val names = ItemNames.snapshot(it.languageMap, it.itemName, it.itemNameEn)
            BillLine(language.first(names), language.second(names), "${BillRules.plainNumber(it.weight)} kg × ${CurrencyUtils.formatInr(it.price)}", it.rowTotal.toDouble())
        }
        val subtotal = lines.sumOf { it.amount }
        val extras = buildList {
            if (invoice.postalCharges > 0) add(chargeLabels.first to invoice.postalCharges)
            if (invoice.ahimsaSilkAmount > 0) add(chargeLabels.second to invoice.ahimsaSilkAmount)
        }
        val before = subtotal - invoice.discount + extras.sumOf { it.second } + tax
        BillSummary(lines, subtotal, invoice.discount, extras, cgst, sgst, igst, invoice.totalAmount - before)
    } else {
        val items = TextilesCalculation.itemsFromJson(invoice.itemsJson)
        val totals = TextilesCalculation.calculate(items, options.globalDiscountValue, options.globalDiscountType, taxInclusive = options.taxInclusive)
        val lines = items.map {
            val detail = listOfNotNull(
                it.hsnCode.takeIf { h -> h.isNotBlank() }?.let { h -> "HSN $h" },
                "${BillRules.plainNumber(it.quantity)} × ${CurrencyUtils.formatInr(it.price)}",
                if (it.taxRate > 0) "${BillRules.plainNumber(it.taxRate)}%" else null
            ).joinToString("  •  ")
            val names = ItemNames.snapshot(it.languageMap, it.itemName, it.itemNameEn)
            BillLine(language.first(names), language.second(names), detail, it.taxableAmount)
        }
        val before = totals.subtotal - totals.totalDiscount + (if (options.taxInclusive) 0.0 else tax)
        BillSummary(lines, totals.subtotal, totals.totalDiscount, emptyList(), cgst, sgst, igst, invoice.totalAmount - before)
    }
}
