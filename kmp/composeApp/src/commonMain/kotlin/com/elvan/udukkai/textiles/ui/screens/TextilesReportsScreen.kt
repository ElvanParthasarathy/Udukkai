package com.elvan.udukkai.textiles.ui.screens

import com.elvan.udukkai.textiles.data.TextilesReceiptRepository
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.platform.AppBackHandler
import kotlin.math.round
import com.elvan.udukkai.core.utils.CurrencyUtils
import com.elvan.udukkai.core.export.SalesRow
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.textiles.data.TextilesProfileRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.textiles.data.TextilesCustomerRepository
import com.elvan.udukkai.textiles.data.TextilesInvoiceRepository
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.ElvanSettingsDivider
import com.elvan.udukkai.ui.components.shell.ElvanSettingsSection
import com.elvan.udukkai.ui.components.shell.ElvanSubShell
import com.elvan.udukkai.ui.components.shell.LocalElvanTopSpacerHeight
import com.elvan.udukkai.ui.screens.create.components.ElvanPillShifter
import com.elvan.udukkai.ui.screens.create.components.PillShifterItem

/** Tax figures of one tax invoice, read from the stored bill. */
private data class ReportLine(
    val invoice: InvoiceModel,
    val taxable: Double,
    val cgst: Double,
    val sgst: Double,
    val igst: Double,
    val hasGstin: Boolean
)

private fun numberIn(json: String, key: String): Double =
    Regex(""""$key"\s*:\s*([-+0-9.eE]+)""").find(json)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0

private fun reportLine(invoice: InvoiceModel): ReportLine {
    var cgst = numberIn(invoice.taxDataJson, "cgst")
    var sgst = numberIn(invoice.taxDataJson, "sgst")
    val igst = numberIn(invoice.taxDataJson, "igst")
    // A bill saved without a split is shown as intra-state: half CGST, half SGST
    if (cgst + sgst + igst == 0.0 && invoice.taxAmount > 0.0) {
        cgst = round(invoice.taxAmount * 100.0 / 2.0) / 100.0
        sgst = invoice.taxAmount - cgst
    }
    val tax = cgst + sgst + igst
    val gstin = invoice.customerId?.let { TextilesCustomerRepository.getById(it)?.gstin }.orEmpty()
    return ReportLine(invoice, (invoice.totalAmount - tax).coerceAtLeast(0.0), cgst, sgst, igst, gstin.isNotBlank())
}

/** Month (year * 12 + month) of a bill date; the financial year runs April to March. */
private fun monthIndex(millis: Long): Int {
    val parts = DateUtils.formatEpochMillis(millis).split("/")
    return parts[2].toInt() * 12 + (parts[1].toInt() - 1)
}

/** First month index of the financial year that contains month index [m]. */
private fun financialYearFirstMonth(m: Int): Int {
    val year = m / 12
    val month = m % 12
    return if (month >= 3) year * 12 + 3 else (year - 1) * 12 + 3
}

/**
 * GST sales report for the active business: tax invoices only (proformas are estimates),
 * for this month, last month and this financial year.
 */
@Composable
fun TextilesReportsScreen(onBack: () -> Unit) {
    AppBackHandler { onBack() }
    val colors = rememberShellColors()
    val ff = LocalAppFontFamily.current
    var period by remember { mutableStateOf(0) }

    val now = monthIndex(System.currentTimeMillis())
    val fyStart = financialYearFirstMonth(now)
    val range: IntRange = when (period) {
        0 -> now..now
        1 -> (now - 1)..(now - 1)
        else -> fyStart..(fyStart + 11)
    }

    val lines = TextilesInvoiceRepository.activeCompanyInvoices
        .filter { it.invoiceType != "proforma" && monthIndex(it.invoiceDate) in range }
        .map { reportLine(it) }
    val numbers = lines.map { it.invoice.invoiceNumber }.sorted()

    val taxable = lines.sumOf { it.taxable }
    val cgst = lines.sumOf { it.cgst }
    val sgst = lines.sumOf { it.sgst }
    val igst = lines.sumOf { it.igst }
    val total = lines.sumOf { it.invoice.totalAmount }

    // Save as Excel, like the React app's sales report: nothing is written until the user taps Save in the system dialog
    val profile = TextilesProfileRepository.profile
    val exporter = com.elvan.udukkai.ui.components.shell.rememberReportExporter(com.elvan.udukkai.core.print.billBusinessFolder(profile))
    val primaryLang = profile.primaryLanguage.ifEmpty { "ta" }
    val secondaryLang = profile.secondaryLanguage.ifEmpty { "en" }
    fun bilingual(a: String, b: String) = if (profile.isBilingual && b.isNotBlank() && b != a) a + "\n" + b else a.ifBlank { b }
    fun clientName(inv: InvoiceModel) = bilingual(inv.customerName[primaryLang].orEmpty().ifBlank { inv.customerName.values.firstOrNull().orEmpty() }, inv.customerName[secondaryLang].orEmpty())
    fun exportExcel() {
        val periodKey = when (period) {
            2 -> "FY_" + (fyStart / 12).toString() + "-" + (((fyStart / 12) + 1) % 100).toString().padStart(2, '0')
            else -> range.first.let { m -> (m / 12).toString() + "-" + (m % 12 + 1).toString().padStart(2, '0') }
        }
        val title = bilingual(profile.getPrimary("businessName"), profile.getSecondary("niruvanathinPeyar")).ifBlank { profile.shortName }
        val bytes = com.elvan.udukkai.core.export.SalesReportExcel.build(title, "Sales Report - " + periodKey.replace('_', ' '), lines.sortedBy { it.invoice.invoiceDate }.map { l -> SalesRow(l.invoice.invoiceNumber, clientName(l.invoice), DateUtils.formatEpochMillis(l.invoice.invoiceDate), l.taxable, l.cgst + l.sgst + l.igst, l.invoice.totalAmount) }, withTax = true)
        exporter.save("Sales_Report_$periodKey", "xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bytes)
    }

    val b2b = lines.filter { it.hasGstin }
    val b2c = lines.filter { !it.hasGstin }

    // The GST summaries (GSTR-1 B2B table and HSN summary) of the bills of this period
    fun periodKeyText() = when (period) {
        2 -> "FY_" + (fyStart / 12).toString() + "-" + (((fyStart / 12) + 1) % 100).toString().padStart(2, '0')
        else -> range.first.let { m -> (m / 12).toString() + "-" + (m % 12 + 1).toString().padStart(2, '0') }
    }
    fun gstBill(l: ReportLine): com.elvan.udukkai.core.export.GstBill {
        val inv = l.invoice
        val cust = inv.customerId?.let { TextilesCustomerRepository.getById(it) }
        val prefs = com.elvan.udukkai.textiles.ui.screens.editor.TextilesCalculation.preferencesFromJson(inv.customPreferencesJson)
        val inclusive = Regex(""""taxInclusive"\s*:\s*true""").containsMatchIn(inv.customPreferencesJson)
        val inter = l.igst > 0.0
        val taxLines = com.elvan.udukkai.textiles.ui.screens.editor.TextilesCalculation.itemTaxLines(
            com.elvan.udukkai.textiles.ui.screens.editor.TextilesCalculation.itemsFromJson(inv.itemsJson),
            inv.globalDiscountValue.takeIf { it > 0 } ?: prefs.globalDiscountValue, inv.globalDiscountType.ifEmpty { prefs.globalDiscountType }, inter, inclusive
        )
        return com.elvan.udukkai.core.export.GstBill(
            number = inv.invoiceNumber, date = DateUtils.formatEpochMillis(inv.invoiceDate), gstin = cust?.gstin.orEmpty(),
            customer = clientName(inv), total = inv.totalAmount, placeOfSupply = prefs.placeOfSupply, interState = inter,
            taxable = l.taxable, cgst = l.cgst, sgst = l.sgst, igst = l.igst,
            lines = taxLines.map { t -> com.elvan.udukkai.core.export.GstLine(t.item.hsnCode, t.item.itemName.ifBlank { t.item.itemNameEn }, t.item.unit, t.item.quantity, t.item.taxRate, t.taxable, t.cgst, t.sgst, t.igst) }
        )
    }
    val xlsxMime = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    fun exportB2b() {
        val title = bilingual(profile.getPrimary("businessName"), profile.getSecondary("niruvanathinPeyar")).ifBlank { profile.shortName }
        val bytes = com.elvan.udukkai.core.export.GstrExcel.b2b(title, "GSTR-1 B2B - " + periodKeyText().replace('_', ' '), b2b.sortedBy { it.invoice.invoiceDate }.map { gstBill(it) })
        exporter.save("GSTR1_B2B_${periodKeyText()}", "xlsx", xlsxMime, bytes)
    }
    fun exportHsn() {
        val title = bilingual(profile.getPrimary("businessName"), profile.getSecondary("niruvanathinPeyar")).ifBlank { profile.shortName }
        val bytes = com.elvan.udukkai.core.export.GstrExcel.hsn(title, "HSN Summary - " + periodKeyText().replace('_', ' '), lines.sortedBy { it.invoice.invoiceDate }.map { gstBill(it) })
        exporter.save("GSTR1_HSN_${periodKeyText()}", "xlsx", xlsxMime, bytes)
    }

    // One scroll state for the list and the shell, so the large title collapses with the page instead of the rows passing under it
    val listState = rememberLazyListState()
    ElvanSubShell(title = K.reports.tr(), onBack = onBack, scrollState = listState) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Dimens.ContentPadding,
                end = Dimens.ContentPadding,
                bottom = Dimens.SubpageContentPaddingBottom
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
        ) {
            item(key = "top_spacer") { Spacer(Modifier.height(LocalElvanTopSpacerHeight.current)) }

            item(key = "period") {
                ElvanPillShifter(
                    items = listOf(
                        PillShifterItem(K.thisMonth.tr()),
                        PillShifterItem(K.lastMonth.tr()),
                        PillShifterItem(K.thisFinYear.tr())
                    ),
                    selectedIndex = period,
                    onIndexSelected = { period = it },
                    colors = colors,
                    isFullWidth = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (lines.isEmpty()) {
                item(key = "empty") {
                    Text(
                        text = K.noInvoicesInPeriod.tr(),
                        style = TextStyle(fontFamily = ff, fontSize = 14.sp, textAlign = TextAlign.Center),
                        color = colors.textSecondary,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                    )
                }
            } else {
                item(key = "summary") {
                    ElvanSettingsSection(title = K.salesSummary.tr(), colors = colors) {
                        ReportRow(K.invoices.tr(), "${lines.size}", colors)
                        ElvanSettingsDivider(colors = colors)
                        ReportRow(K.taxableValue.tr(), CurrencyUtils.formatInr(taxable), colors)
                        ElvanSettingsDivider(colors = colors)
                        ReportRow(K.totalTax.tr(), CurrencyUtils.formatInr(cgst + sgst + igst), colors)
                        ElvanSettingsDivider(colors = colors)
                        ReportRow(K.grandTotal.tr(), CurrencyUtils.formatInr(total), colors, bold = true)
                    }
                }
                item(key = "tax") {
                    ElvanSettingsSection(title = K.taxBreakup.tr(), colors = colors) {
                        ReportRow(K.cgstLabel.tr(), CurrencyUtils.formatInr(cgst), colors)
                        ElvanSettingsDivider(colors = colors)
                        ReportRow(K.sgstLabel.tr(), CurrencyUtils.formatInr(sgst), colors)
                        ElvanSettingsDivider(colors = colors)
                        ReportRow(K.igstLabel.tr(), CurrencyUtils.formatInr(igst), colors)
                    }
                }
                item(key = "split") {
                    ElvanSettingsSection(colors = colors) {
                        ReportRow("${K.b2bSales.tr()} (${b2b.size})", CurrencyUtils.formatInr(b2b.sumOf { it.invoice.totalAmount }), colors)
                        ElvanSettingsDivider(colors = colors)
                        ReportRow("${K.b2cSales.tr()} (${b2c.size})", CurrencyUtils.formatInr(b2c.sumOf { it.invoice.totalAmount }), colors)
                    }
                }
                item(key = "numbers") {
                    ElvanSettingsSection(colors = colors) {
                        ReportRow(K.firstToLast.tr(), "${numbers.first()} – ${numbers.last()}", colors)
                    }
                }
            }

            if (lines.isNotEmpty()) {
                item(key = "export") {
                    com.elvan.udukkai.ui.components.shell.ElvanSettingsSection(colors = colors) {
                        com.elvan.udukkai.ui.components.shell.ElvanSettingsRow(
                            icon = com.elvan.udukkai.ui.navigation.MaterialSymbols.Rounded.Download,
                            title = K.exportExcel.tr(),
                            description = K.exportExcelDesc.tr(),
                            onClick = { exportExcel() },
                            colors = colors
                        )
                        com.elvan.udukkai.ui.components.shell.ElvanSettingsDivider(colors = colors)
                        com.elvan.udukkai.ui.components.shell.ElvanSettingsRow(
                            icon = com.elvan.udukkai.ui.navigation.MaterialSymbols.Rounded.Download,
                            title = K.exportB2b.tr(),
                            description = K.exportB2bDesc.tr(),
                            onClick = { exportB2b() },
                            colors = colors
                        )
                        com.elvan.udukkai.ui.components.shell.ElvanSettingsDivider(colors = colors)
                        com.elvan.udukkai.ui.components.shell.ElvanSettingsRow(
                            icon = com.elvan.udukkai.ui.navigation.MaterialSymbols.Rounded.Download,
                            title = K.exportHsn.tr(),
                            description = K.exportHsnDesc.tr(),
                            onClick = { exportHsn() },
                            colors = colors
                        )
                    }
                }
            }

            item(key = "note") {
                Text(
                    text = K.estimatesNotCounted.tr(),
                    style = TextStyle(fontFamily = ff, fontSize = 12.sp),
                    color = colors.textTertiary,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            item(key = "financial_year") {
                com.elvan.udukkai.ui.screens.home.components.FinancialYearSection(
                    colors,
                    (TextilesInvoiceRepository.invoices.map { com.elvan.udukkai.core.utils.DateUtils.financialYearStart(it.invoiceDate) } +
                        TextilesReceiptRepository.receipts.map { com.elvan.udukkai.core.utils.DateUtils.financialYearStart(it.receiptDate) }).toSet(),
                    nextBill = TextilesInvoiceRepository.getNextInvoiceNumber(profile.id, profile.shortName.ifEmpty { "INV" }),
                    nextReceipt = TextilesReceiptRepository.formatReceiptNumber(profile.shortName, TextilesReceiptRepository.getNextVanakkam(profile.id)),
                    firstBill = com.elvan.udukkai.core.finance.BillNumbering.format(profile.shortName.ifEmpty { "INV" }, 1),
                    firstReceipt = TextilesReceiptRepository.formatReceiptNumber(profile.shortName, 1)
                )
            }
        }
    }
}

@Composable
private fun ReportRow(label: String, value: String, colors: ShellColors, bold: Boolean = false) {
    val ff = LocalAppFontFamily.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = TextStyle(fontFamily = ff, fontSize = 14.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal),
            color = colors.textSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = TextStyle(fontFamily = ff, fontSize = 15.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold),
            color = colors.textPrimary
        )
    }
}
