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

private const val DAY = 24L * 60 * 60 * 1000

/**
 * Everything about the financial year in one place (the Reports page): the running year and its days left, when the
 * numbers start again, whether the years before it can still be changed or are closed, and how long deleted items are kept.
 * [dataYears] are the starting years (2026 for 2026-27) of the years that have bills or receipts.
 */
@Composable
fun FinancialYearSection(colors: ShellColors, dataYears: Set<Int>) {
    val ff = LocalAppFontFamily.current
    val now = remember { System.currentTimeMillis() }
    val status = remember { FinancialYearStatus.of(now) }
    val current = DateUtils.financialYearStart(now)
    val years = (dataYears + current + (current - 1)).filter { it <= current }.sortedDescending().take(4)

    fun label(start: Int) = "$start-${((start + 1) % 100).toString().padStart(2, '0')}"

    @Composable
    fun row(title: String, value: String, bold: Boolean = false) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.Top) {
            Text(title, Modifier.weight(0.45f), style = TextStyle(fontFamily = ff, fontSize = 14.sp), color = colors.textSecondary)
            Text(value, Modifier.weight(0.55f), style = TextStyle(fontFamily = ff, fontSize = 14.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold, textAlign = androidx.compose.ui.text.style.TextAlign.End), color = colors.textPrimary)
        }
    }

    ElvanSettingsSection(title = K.fyRunning.tr(), colors = colors) {
        row(K.fyRunning.tr(), status.label, bold = true)
        ElvanSettingsDivider(colors = colors)
        row(K.fyPeriod.tr(), "01/04/$current – 31/03/${current + 1}")
        ElvanSettingsDivider(colors = colors)
        row(K.fyDaysLeft.tr(), "${status.daysLeft}")
        ElvanSettingsDivider(colors = colors)
        row(K.fyNumbersRestart.tr(), status.nextStart + " · 001")
    }

    ElvanSettingsSection(title = K.fyYearsTitle.tr(), colors = colors) {
        years.forEachIndexed { i, y ->
            if (i > 0) ElvanSettingsDivider(colors = colors)
            val close = YearLock.closeDay(y)
            val value = when {
                y == current -> K.fyOpen.tr()
                now < close -> K.fyOpenUntil.tr().replace("%s", DateUtils.formatEpochMillis(close)).replace("%d", "${((close - now + DAY - 1) / DAY).toInt()}")
                else -> K.fyClosedSince.tr().replace("%s", DateUtils.formatEpochMillis(close))
            }
            row(label(y), value)
        }
    }

    ElvanSettingsSection(title = K.fyDeletionTitle.tr(), colors = colors) {
        Text(
            K.fyDeletionRule.tr(),
            style = TextStyle(fontFamily = ff, fontSize = 13.sp, lineHeight = 19.sp),
            color = colors.textSecondary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
        )
    }
}
