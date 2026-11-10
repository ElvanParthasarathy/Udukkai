package com.elvan.udukkai.factory.ui.screens

import com.elvan.udukkai.factory.data.FactoryReceiptRepository
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
import com.elvan.udukkai.factory.data.FactoryProfileRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.factory.data.FactoryCustomerRepository
import com.elvan.udukkai.factory.data.FactoryInvoiceRepository
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.ElvanSettingsDivider
import com.elvan.udukkai.ui.components.shell.ElvanSettingsSection
import com.elvan.udukkai.ui.components.shell.ElvanSubShell
import com.elvan.udukkai.ui.components.shell.LocalElvanTopSpacerHeight
import com.elvan.udukkai.ui.components.shell.elvanTopBandRow
import com.elvan.udukkai.ui.screens.create.components.ElvanPillShifter
import com.elvan.udukkai.ui.screens.create.components.PillShifterItem

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
 * Factory Sales & Coolie Report for the active business.
 * Supports this month, last month, and this financial year.
 */
@Composable
fun FactoryReportsScreen(onBack: () -> Unit) {
    AppBackHandler { onBack() }
    val colors = rememberShellColors()
    val ff = LocalAppFontFamily.current
    var period by remember { mutableStateOf(0) }

    val now = monthIndex(DateUtils.todayMillis())
    val fyStart = financialYearFirstMonth(now)
    val range: IntRange = when (period) {
        0 -> now..now
        1 -> (now - 1)..(now - 1)
        else -> fyStart..(fyStart + 11)
    }

    val lines = FactoryInvoiceRepository.activeCompanyInvoices
        .filter { it.invoiceType != "proforma" && monthIndex(it.invoiceDate) in range }
    val numbers = lines.map { it.invoiceNumber }.sorted()
    val total = lines.sumOf { it.totalAmount }

    // Save as Excel, like the React app's sales report: nothing is written until the user taps Save in the system dialog
    val profile = FactoryProfileRepository.profile
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
        val bytes = com.elvan.udukkai.core.export.SalesReportExcel.build(title, "Sales Report - " + periodKey.replace('_', ' '), lines.sortedBy { it.invoiceDate }.map { l -> SalesRow(l.invoiceNumber, clientName(l), DateUtils.formatEpochMillis(l.invoiceDate), l.totalAmount, 0.0, l.totalAmount) }, withTax = false)
        exporter.save("Sales_Report_$periodKey", "xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bytes)
    }


    val listState = rememberLazyListState()
    ElvanSubShell(
        title = K.reports.tr(),
        onBack = onBack,
        scrollState = listState,
        hideTitleSideways = true
    ) {
        val sideways = com.elvan.udukkai.ui.components.shell.LocalElvanSideBar.current
        val extraTop = if (sideways) 18.dp else 0.dp
        com.elvan.udukkai.ui.screens.editor.ElvanEditorList(
            landscapeOverviewKeys = setOf("period", "summary", "numbers", "note"),
            fullWidthKeys = setOf("top_spacer", "period", "empty", "export", "note", "financial_year"),
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Dimens.ContentPadding,
                end = Dimens.ContentPadding,
                bottom = Dimens.SubpageContentPaddingBottom
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
        ) {
            item(key = "top_spacer") { Spacer(Modifier.height(LocalElvanTopSpacerHeight.current + extraTop)) }

            item(key = "period") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (sideways) Modifier.elvanTopBandRow(true, up = 12.dp + extraTop) else Modifier),
                    contentAlignment = Alignment.Center
                ) {
                    ElvanPillShifter(
                        items = listOf(
                            PillShifterItem(K.thisMonth.tr()),
                            PillShifterItem(K.lastMonth.tr()),
                            PillShifterItem(K.thisFinYear.tr())
                        ),
                        selectedIndex = period,
                        onIndexSelected = { period = it },
                        colors = colors,
                        isSmall = sideways,
                        isFullWidth = !sideways
                    )
                }
            }

            if (lines.isEmpty()) {
                item(key = "empty") {
                    Text(
                        text = K.noBillsInPeriod.tr(),
                        style = TextStyle(fontFamily = ff, fontSize = 14.sp, textAlign = TextAlign.Center),
                        color = colors.textSecondary,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                    )
                }
            } else {
                item(key = "summary") {
                    ElvanSettingsSection(title = K.salesSummary.tr(), colors = colors) {
                        FactoryReportRow(K.invoices.tr(), "${lines.size}", colors)
                        ElvanSettingsDivider(colors = colors)
                        FactoryReportRow(K.grandTotal.tr(), CurrencyUtils.formatInr(total), colors, bold = true)
                    }
                }
                item(key = "numbers") {
                    ElvanSettingsSection(colors = colors) {
                        FactoryReportRow(K.firstToLast.tr(), "${numbers.first()} – ${numbers.last()}", colors)
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
                    (FactoryInvoiceRepository.invoices.map { com.elvan.udukkai.core.utils.DateUtils.financialYearStart(it.invoiceDate) } +
                        FactoryReceiptRepository.receipts.map { com.elvan.udukkai.core.utils.DateUtils.financialYearStart(it.receiptDate) }).toSet(),
                    nextBill = FactoryInvoiceRepository.getNextInvoiceNumber(profile.id, com.elvan.udukkai.core.finance.BillNumbering.invoiceBase(profile.shortName)),
                    nextReceipt = FactoryReceiptRepository.formatReceiptNumber(profile.shortName, FactoryReceiptRepository.getNextVanakkam(profile.id)),
                    firstBill = com.elvan.udukkai.core.finance.BillNumbering.format(com.elvan.udukkai.core.finance.BillNumbering.invoiceBase(profile.shortName), 1),
                    firstReceipt = FactoryReceiptRepository.formatReceiptNumber(profile.shortName, 1)
                )
            }
        }
    }
}

@Composable
private fun FactoryReportRow(label: String, value: String, colors: ShellColors, bold: Boolean = false) {
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
