package com.elvan.udukkai.ui.screens.create.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.LocalShellColors
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * The text of a bill or receipt card.
 * Preserves the exact line count before selection mode so neither 1-line cards
 * wrap downwards nor 2-line cards shrink upwards when long-pressing.
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
    isSelectionMode: Boolean = false,
    modifier: Modifier = Modifier,
    below: @Composable ColumnScope.() -> Unit = {}
) {
    val ff = LocalAppFontFamily.current
    val secondary = LocalShellColors.current.textSecondary

    var primaryLineCount by remember { mutableStateOf<Int?>(null) }
    var secondLineCount by remember { mutableStateOf<Int?>(null) }

    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = primary.preventBrokenLigatures(),
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary
                    ),
                    maxLines = primaryLineCount ?: maxOf(2, textLines),
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { layoutResult ->
                        if (!isSelectionMode) {
                            primaryLineCount = layoutResult.lineCount
                        }
                    }
                )
                if (secondLine.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = secondLine.preventBrokenLigatures(),
                        style = TextStyle(fontFamily = ff, fontSize = 13.5.sp, color = secondary),
                        maxLines = secondLineCount ?: 2,
                        overflow = TextOverflow.Ellipsis,
                        onTextLayout = { layoutResult ->
                            if (!isSelectionMode) {
                                secondLineCount = layoutResult.lineCount
                            }
                        }
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

        // Receipts list the bills they settled right under the name
        below()
        Spacer(Modifier.height(8.dp))
        // Number and date on one line (status at the right edge), the financial year and the amount on the line below
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = number,
                    style = TextStyle(fontFamily = ff, fontSize = 13.5.sp, color = secondary),
                    maxLines = 1
                )
                Text(
                    text = "•",
                    modifier = Modifier.padding(horizontal = 7.dp),
                    style = TextStyle(fontFamily = ff, fontSize = 13.sp, color = LocalShellColors.current.textTertiary)
                )
                Text(
                    text = date.preventBrokenLigatures(),
                    style = TextStyle(fontFamily = ff, fontSize = 13.sp, color = secondary),
                    maxLines = 1
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = status.preventBrokenLigatures(),
                style = TextStyle(fontFamily = ff, fontSize = 12.sp, color = secondary),
                maxLines = 1
            )
        }
        Spacer(Modifier.height(2.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = fy,
                modifier = Modifier.weight(1f),
                style = TextStyle(fontFamily = ff, fontSize = 12.5.sp, color = LocalShellColors.current.textTertiary),
                maxLines = 1
            )
            Text(
                text = amount,
                style = TextStyle(fontFamily = ff, fontSize = if (amount.length > 11) 13.sp else 15.sp, fontWeight = FontWeight.ExtraBold, color = colors.textPrimary),
                maxLines = 1
            )
        }
    }
}
