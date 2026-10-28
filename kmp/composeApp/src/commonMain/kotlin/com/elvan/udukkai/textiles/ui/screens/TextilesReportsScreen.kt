package com.elvan.udukkai.textiles.ui.screens

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
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.data.model.InvoiceModel
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
    val b2b = lines.filter { it.hasGstin }
    val b2c = lines.filter { !it.hasGstin }

    ElvanSubShell(title = K.reports.tr(), onBack = onBack) {
        LazyColumn(
            state = rememberLazyListState(),
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
                    isSmall = true
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

            item(key = "note") {
                Text(
                    text = K.estimatesNotCounted.tr(),
                    style = TextStyle(fontFamily = ff, fontSize = 12.sp),
                    color = colors.textSecondary.copy(alpha = 0.8f),
                    modifier = Modifier.padding(horizontal = 8.dp)
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
