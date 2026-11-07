package com.elvan.udukkai.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.finance.FinancialYearStatus
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors

/** One quiet card on the home page: the running financial year, the days left, and what happens next. */
@Composable
fun FinancialYearCard(colors: ShellColors, modifier: Modifier = Modifier) {
    val ff = LocalAppFontFamily.current
    val status = remember { FinancialYearStatus.of() }
    val next = K.fyNextStarts.tr().replace("%s", status.nextStart)
    val prev = status.previousLabel?.let { K.fyPrevCloses.tr().replaceFirst("%s", it).replace("%d", "${status.previousClosesInDays}") }
    Column(
        modifier = modifier.fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(24.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${K.fyRunning.tr()} ${status.label}", style = TextStyle(fontFamily = ff, fontSize = 15.sp, fontWeight = FontWeight.SemiBold), color = colors.textPrimary)
            Text("${status.daysLeft} ${K.fyDaysLeft.tr()}", style = TextStyle(fontFamily = ff, fontSize = 14.sp, fontWeight = FontWeight.Medium), color = colors.textSecondary)
        }
        if (status.daysLeft <= 60) Text(next, style = TextStyle(fontFamily = ff, fontSize = 12.5.sp), color = colors.textSecondary)
        if (prev != null) Text(prev, style = TextStyle(fontFamily = ff, fontSize = 12.5.sp), color = colors.textSecondary)
    }
}
