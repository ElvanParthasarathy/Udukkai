package com.elvan.udukkai.ui.screens.create.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.LocalShellColors
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * The text of a bill or receipt card. Nothing is cut short: the name and the "English name • place" line wrap
 * onto a second line when long, because the place tells apart one business in two towns.
 *
 *     Name                                   >
 *     English name • place
 *     SJPS-010  FY 26-27                Unpaid
 *     05/10/2026                       ₹97,545
 *
 * [below] holds an optional extra grey line (for example the bills a receipt pays).
 */
@Composable
fun DocCardText(
    colors: ShellColors,
    primary: String,
    secondLine: String,
    amount: String,
    date: String,
    number: String,
    fy: String,
    status: String,
    textLines: Int,
    modifier: Modifier = Modifier,
    below: @Composable ColumnScope.() -> Unit = {}
) {
    val ff = LocalAppFontFamily.current
    val secondary = LocalShellColors.current.textSecondary
    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = primary.preventBrokenLigatures(),
                    style = TextStyle(fontFamily = ff, fontSize = 16.5.sp, fontWeight = FontWeight.ExtraBold, color = colors.textPrimary),
                    maxLines = maxOf(2, textLines)
                )
                if (secondLine.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = secondLine.preventBrokenLigatures(),
                        style = TextStyle(fontFamily = ff, fontSize = 13.5.sp, color = secondary),
                        maxLines = 2
                    )
                }
            }
            Icon(
                imageVector = MaterialSymbols.Rounded.ChevronRight,
                contentDescription = null,
                tint = LocalShellColors.current.border,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = number,
                        style = TextStyle(fontFamily = ff, fontSize = 13.5.sp, color = secondary),
                        maxLines = 1
                    )
                    // The financial year, small and quiet beside the number
                    Text(
                        text = fy,
                        modifier = Modifier.padding(start = 8.dp),
                        style = TextStyle(fontFamily = ff, fontSize = 12.5.sp, color = LocalShellColors.current.textTertiary),
                        maxLines = 1
                    )
                }
                Text(
                    text = date.preventBrokenLigatures(),
                    style = TextStyle(fontFamily = ff, fontSize = 13.sp, color = secondary),
                    maxLines = 1
                )
            }
            Spacer(Modifier.width(8.dp))
            // Status over the amount, at the foot on the right
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = status.preventBrokenLigatures(),
                    style = TextStyle(fontFamily = ff, fontSize = 12.sp, color = secondary),
                    maxLines = 1
                )
                Text(
                    text = amount,
                    style = TextStyle(fontFamily = ff, fontSize = if (amount.length > 11) 13.sp else 15.sp, fontWeight = FontWeight.ExtraBold, color = colors.textPrimary),
                    maxLines = 1
                )
            }
        }
        below()
    }
}
