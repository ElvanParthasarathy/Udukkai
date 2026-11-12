package com.elvan.udukkai.ui.screens.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.elvan.udukkai.core.print.billBusinessFolder
import com.elvan.udukkai.core.print.receiptPdfName
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.core.utils.NumberToWords
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.model.PaymentType
import com.elvan.udukkai.data.model.ReceiptModel
import com.elvan.udukkai.data.settings.DataLanguageDisplay
import com.elvan.udukkai.data.settings.BusinessProfile
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.preview.PreviewScreen

/**
 * Receipt view (பெறுகை பார்வை): a native page like Zoho's payment receipt.
 * Amount received on top, the customer, the receipt's details and the bills it settled (each opens).
 * The paper (PDF) is made behind it for Open, Print and Share.
 */
@Composable
fun ReceiptViewScreen(
    receipt: ReceiptModel,
    profile: BusinessProfile,
    allProfiles: List<BusinessProfile>,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onPrint: (() -> Unit)? = null,
    /** The bills this receipt settled. */
    invoices: List<InvoiceModel> = emptyList(),
    onOpenInvoice: ((InvoiceModel) -> Unit)? = null,
    onOpenCustomer: (() -> Unit)? = null,
    onDelete: (() -> ViewDeleteResult)? = null,
    printPayload: ((activeProfile: BusinessProfile) -> String)? = null
) {
    val activeProfile = (if (receipt.companyId != null) allProfiles.find { it.id == receipt.companyId } else null) ?: profile
    val deleteItem = rememberViewDeleteItem(onDelete, K.receiptDeletedMsg.tr(), onBack)
    val appLanguage = com.elvan.udukkai.localization.LanguageManager.activeLanguageCode
    val payload = remember(receipt, activeProfile, appLanguage) { printPayload?.invoke(activeProfile) }
    PreviewScreen(
        title = receipt.receiptNumber,
        onBack = onBack,
        onEdit = onEdit.takeUnless { com.elvan.udukkai.core.utils.DateUtils.isPastFinancialYear(receipt.receiptDate) },
        menuItems = listOfNotNull(deleteItem),
        printPayload = payload,
        printFileName = receiptPdfName(receipt),
        businessFolder = billBusinessFolder(activeProfile),
        showPreparationOverlay = true,
        body = { ReceiptBody(receipt, activeProfile, invoices, onOpenInvoice, onOpenCustomer) }
    )
}

@Composable
private fun ReceiptBody(
    receipt: ReceiptModel,
    profile: BusinessProfile,
    invoices: List<InvoiceModel>,
    onOpenInvoice: ((InvoiceModel) -> Unit)?,
    onOpenCustomer: (() -> Unit)?
) {
    val language = DataLanguageDisplay(profile)
    val name = language.first(receipt.customerName).ifBlank { "-" }
    val secondName = language.second(receipt.customerName)
    val town = language.first(receipt.customerAddress)

    val receivedLabel = K.amountReceived.tr()
    val numberLabel = K.receiptNumber.tr()
    val dateLabel = K.date.tr()
    val fyLabel = K.thisFinYear.tr()
    val modeLabel = K.paymentMode.tr()
    val refLabel = K.referenceOrTxnNo.tr()
    val bankLabel = K.bank.tr()
    val billsTitle = K.invoices.tr()
    val wordsTitle = K.amountInWords.tr()
    val notesTitle = K.remarks.tr()
    val words = remember(receipt.amount, language.languages) { language.lines { NumberToWords.forLanguage(receipt.amount, it) } }

    DocPage(fullWidthKeys = setOf("top_spacer")) {
        item(key = "status") { DocStatusHeader(DocTone.Paid, receivedLabel, receipt.amount) }
        item(key = "customer") { DocCustomerCard(name, secondName, town, onOpenCustomer) }
        item(key = "info") {
            DocSection(null) {
                DocRow(numberLabel, receipt.receiptNumber)
                DocDivider()
                DocRow(dateLabel, DateUtils.formatEpochMillis(receipt.receiptDate))
                DocDivider()
                DocRow(fyLabel, DateUtils.financialYearLabel(receipt.receiptDate))
                DocDivider()
                DocRow(modeLabel, PaymentType.fromStored(receipt.paymentMode).label())
                if (!receipt.transactionNumber.isNullOrBlank()) {
                    DocDivider()
                    DocRow(refLabel, receipt.transactionNumber)
                }
                if (!receipt.bankName.isNullOrBlank()) {
                    DocDivider()
                    DocRow(bankLabel, receipt.bankName)
                }
            }
        }
        if (invoices.isNotEmpty()) {
            item(key = "bills") {
                DocSection("$billsTitle (${invoices.size})") {
                    invoices.forEachIndexed { i, inv ->
                        if (i > 0) DocDivider()
                        DocLinkRow(
                            icon = MaterialSymbols.Rounded.InvoiceFill,
                            title = inv.invoiceNumber,
                            subtitle = DateUtils.formatEpochMillis(inv.invoiceDate),
                            amount = inv.totalAmount,
                            onClick = onOpenInvoice?.let { open -> { open(inv) } },
                            tint = com.elvan.udukkai.theme.rememberShellColors().invoiceColor
                        )
                    }
                }
            }
        }
        item(key = "words") { DocTextCard(wordsTitle, words) }
        if (receipt.notes.isNotBlank()) {
            item(key = "notes") { DocTextCard(notesTitle, listOf(receipt.notes to true)) }
        }
    }
}
