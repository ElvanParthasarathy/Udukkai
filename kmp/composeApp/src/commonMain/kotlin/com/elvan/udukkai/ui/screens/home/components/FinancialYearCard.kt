package com.elvan.udukkai.ui.screens.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.finance.FinancialYearStatus
import com.elvan.udukkai.core.finance.YearLock
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.ui.components.shell.ElvanSettingsDivider
import com.elvan.udukkai.ui.components.shell.ElvanSettingsSection

/**
 * Everything about the financial year in one place (the Reports page): the running year and its days left, when the
 * numbers start again, whether the years before it can still be changed or are closed, and how long deleted items are kept.
 * [dataYears] are the starting years (2026 for 2026-27) of the years that have bills or receipts. [nextBill] and
 * [nextReceipt] are the numbers the next save gets; [firstBill] and [firstReceipt] are the first ones of the new year.
 */
@Composable
fun FinancialYearSection(colors: ShellColors, dataYears: Set<Int>, nextBill: String, nextReceipt: String, firstBill: String, firstReceipt: String) {
    val ff = LocalAppFontFamily.current
    val now = remember { System.currentTimeMillis() }
    val status = remember { FinancialYearStatus.of(now) }
    val current = DateUtils.financialYearStart(now)
    val years = (dataYears + current + (current - 1)).filter { it <= current }.sortedDescending().take(4)

    fun label(start: Int) = "$start-${((start + 1) % 100).toString().padStart(2, '0')}"

    @Composable
    fun row(title: String, value: String) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(title, Modifier.weight(1f), style = TextStyle(fontFamily = ff, fontSize = 14.sp), color = colors.textSecondary, maxLines = 1)
            Spacer(Modifier.width(12.dp))
            Text(value, style = TextStyle(fontFamily = ff, fontSize = 14.sp, fontWeight = FontWeight.SemiBold), color = colors.textPrimary, maxLines = 1)
        }
    }

    @Composable
    fun note(text: String) {
        Text(
            text,
            style = TextStyle(fontFamily = ff, fontSize = 12.sp, lineHeight = 17.sp),
            color = colors.textTertiary,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ElvanSettingsSection(title = K.fyRunning.tr() + " " + status.label, colors = colors) {
            row(K.fyStarted.tr(), "01/04/$current")
            ElvanSettingsDivider(colors = colors)
            row(K.fyEnds.tr(), "31/03/${current + 1}")
            ElvanSettingsDivider(colors = colors)
            row(K.fyDaysLeft.tr(), "${status.daysLeft}")
        }

        ElvanSettingsSection(title = K.fyNumbersTitle.tr(), colors = colors) {
            row(K.fyNextBill.tr(), nextBill)
            ElvanSettingsDivider(colors = colors)
            row(K.fyNextReceipt.tr(), nextReceipt)
        }

        ElvanSettingsSection(title = K.fyFromDate.tr().replace("%s", status.nextStart), colors = colors) {
            row(K.fyFirstBill.tr(), firstBill)
            ElvanSettingsDivider(colors = colors)
            row(K.fyFirstReceipt.tr(), firstReceipt)
        }
        note(K.fyNumbersNote.tr())

        ElvanSettingsSection(title = K.fyYearsTitle.tr(), colors = colors) {
            years.forEachIndexed { i, y ->
                if (i > 0) ElvanSettingsDivider(colors = colors)
                val close = YearLock.closeDay(y)
                val value = when {
                    y == current -> K.fyOpen.tr()
                    now < close -> K.fyOpenUntil.tr().replace("%s", DateUtils.formatEpochMillis(close))
                    else -> K.fyClosed.tr()
                }
                row(label(y), value)
            }
        }
        note(K.fyCloseNote.tr())
    }
}
