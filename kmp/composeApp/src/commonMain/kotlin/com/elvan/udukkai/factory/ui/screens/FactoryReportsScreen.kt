package com.elvan.udukkai.factory.ui.screens

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

    val now = monthIndex(System.currentTimeMillis())
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

            item(key = "note") {
                Text(
                    text = K.estimatesNotCounted.tr(),
                    style = TextStyle(fontFamily = ff, fontSize = 12.sp),
                    color = colors.textTertiary,
                    modifier = Modifier.padding(horizontal = 20.dp)
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
