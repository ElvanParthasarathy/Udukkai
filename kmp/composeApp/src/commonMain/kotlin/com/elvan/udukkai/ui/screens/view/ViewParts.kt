package com.elvan.udukkai.ui.screens.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.finance.BillNumbering
import com.elvan.udukkai.core.utils.CurrencyUtils
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.ShellDefaults
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.ui.components.shell.ElvanSettingsDivider
import com.elvan.udukkai.ui.components.shell.ElvanSettingsSection
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.home.components.ElvanStatsCard

/** One bill in a view page's "recent" list. */
data class ViewBillRow(
    val id: Long,
    val number: String,
    val date: Long,
    val amount: Double,
    val isPaid: Boolean,
    /** A short extra fact for this page, e.g. the weight of the item on that bill. */
    val note: String = ""
)

/** What a view page shows about an item's or customer's bills. */
data class ViewActivity(
    val count: Int = 0,
    /** Money on the bills: the customer's bills, or the rows of this item on them. */
    val billed: Double = 0.0,
    /** Customers only: the sum of their bills that have no receipt yet. */
    val unpaid: Double = 0.0,
    /** Items only: the total weight or quantity billed, ready to show (e.g. "33.680 kg"). */
    val quantityText: String = "",
    val bills: List<ViewBillRow> = emptyList()
)

/**
 * The top of a view page: the name in the main language, the second-language name in grey under it, and one quiet
 * line of facts. Text only, with no picture or circle, so the space goes to the details.
 */
@Composable
fun ViewHero(
    primary: String,
    secondary: String,
    facts: String,
    colors: ShellColors
) {
    val ff = LocalAppFontFamily.current
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp)) {
        Text(
            text = primary.preventBrokenLigatures(),
            style = TextStyle(fontFamily = ff, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary, lineHeight = 30.sp)
        )
        if (secondary.isNotEmpty()) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = secondary.preventBrokenLigatures(),
                style = TextStyle(fontFamily = ff, fontSize = 15.sp, color = colors.textSecondary)
            )
        }
        if (facts.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = facts.preventBrokenLigatures(),
                style = TextStyle(fontFamily = ff, fontSize = 13.sp, color = colors.textTertiary)
            )
        }
    }
}

/** Two totals side by side, in the same tiles as the home page. */
@Composable
fun ViewStats(
    left: Triple<ImageVector, String, String>,
    right: Triple<ImageVector, String, String>,
    colors: ShellColors
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        for ((icon, label, value) in listOf(left, right)) {
            val hue = viewIconHue(icon, colors)
            ElvanStatsCard(
                icon = icon, label = label, value = value, colors = colors, modifier = Modifier.weight(1f),
                accentColor = hue,
                gradientColors = listOf(androidx.compose.ui.graphics.lerp(hue, androidx.compose.ui.graphics.Color.White, 0.3f), hue)
            )
        }
    }
}

/** Round actions under the hero (call, email), each a 56 dp circle with its label under it. */
@Composable
fun ViewQuickActions(actions: List<Triple<ImageVector, String, () -> Unit>>, colors: ShellColors) {
    if (actions.isEmpty()) return
    val ff = LocalAppFontFamily.current
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        actions.forEach { (icon, label, onClick) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ShellDefaults.ripple(colors, bounded = true),
                        role = Role.Button,
                        onClick = onClick
                    )
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                com.elvan.udukkai.ui.components.ElvanTintedIcon(icon = icon, tint = viewIconHue(icon, colors), size = 56.dp, iconSize = 24.dp)
                Spacer(Modifier.height(6.dp))
                Text(label.preventBrokenLigatures(), style = TextStyle(fontFamily = ff, fontSize = 13.sp, color = colors.textSecondary))
            }
        }
    }
}

/** Label-and-value rows in one rounded section with slit dividers (empty values are left out). */
@Composable
fun ViewInfoSection(title: String, rows: List<Pair<String, String>>, colors: ShellColors) {
    val shown = rows.filter { it.second.isNotBlank() }
    if (shown.isEmpty()) return
    val ff = LocalAppFontFamily.current
    ElvanSettingsSection(title = title, colors = colors) {
        shown.forEachIndexed { index, (label, value) ->
            if (index > 0) ElvanSettingsDivider(colors = colors)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp)
                    .semantics(mergeDescendants = true) {},
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = label.preventBrokenLigatures(),
                    style = TextStyle(fontFamily = ff, fontSize = 14.sp, color = colors.textSecondary),
                    modifier = Modifier.weight(0.42f)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = value.preventBrokenLigatures(),
                    style = TextStyle(fontFamily = ff, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary, textAlign = TextAlign.End),
                    modifier = Modifier.weight(0.58f)
                )
            }
        }
    }
}

/** A postal address in one rounded box, left-aligned like on an envelope: one line per part (empty parts are skipped). */
@Composable
fun ViewAddressBox(title: String?, lines: List<String>, colors: ShellColors) {
    val shown = lines.filter { it.isNotBlank() }
    if (shown.isEmpty()) return
    val ff = LocalAppFontFamily.current
    ElvanSettingsSection(title = title, colors = colors) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp).semantics(mergeDescendants = true) {},
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            shown.forEach {
                Text(it.preventBrokenLigatures(), style = TextStyle(fontFamily = ff, fontSize = 16.sp, color = colors.textPrimary, lineHeight = 22.sp))
            }
        }
    }
}

/** The latest bills for this item or customer; a row opens the bill. */
@Composable
fun ViewBillsSection(title: String, bills: List<ViewBillRow>, colors: ShellColors, onBillClick: (Long) -> Unit) {
    if (bills.isEmpty()) return
    val ff = LocalAppFontFamily.current
    ElvanSettingsSection(title = title, colors = colors) {
        bills.forEachIndexed { index, bill ->
            if (index > 0) ElvanSettingsDivider(colors = colors)
            val status = if (bill.isPaid) K.statusPaid.tr() else K.statusUnpaid.tr()
            val dateStr = DateUtils.formatEpochMillis(bill.date)
            val amountStr = CurrencyUtils.formatInr(bill.amount)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ShellDefaults.ripple(colors, bounded = true),
                        role = Role.Button,
                        onClick = { onBillClick(bill.id) }
                    )
                    .semantics(mergeDescendants = true) {
                        contentDescription = listOf(bill.number, dateStr, bill.note, amountStr, status).filter { it.isNotBlank() }.joinToString(", ")
                    }
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(bill.number, style = TextStyle(fontFamily = ff, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary))
                        Text(
                            "FY ${BillNumbering.fyShort(bill.date)}",
                            modifier = Modifier.padding(start = 8.dp),
                            style = TextStyle(fontFamily = ff, fontSize = 12.5.sp, color = colors.textTertiary)
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if (bill.note.isNotBlank()) "$dateStr  •  ${bill.note}" else dateStr,
                        style = TextStyle(fontFamily = ff, fontSize = 13.sp, color = colors.textSecondary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(status.preventBrokenLigatures(), style = TextStyle(fontFamily = ff, fontSize = 12.sp, color = colors.textSecondary))
                    Spacer(Modifier.height(2.dp))
                    Text(amountStr, style = TextStyle(fontFamily = ff, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = colors.textPrimary))
                }
                Spacer(Modifier.width(4.dp))
                Icon(MaterialSymbols.Rounded.ChevronRight, null, tint = colors.border, modifier = Modifier.size(18.dp))
            }
        }
    }
}

/** The colour a view page gives an icon: money green, bills blue, unpaid amber, weight amber, call green, mail blue. */
private fun viewIconHue(icon: ImageVector, colors: ShellColors): androidx.compose.ui.graphics.Color = with(MaterialSymbols.Rounded) {
    when (icon) {
        CurrencyRupeeCircle -> colors.receiptColor
        InvoiceFill -> colors.invoiceColor
        Receipt -> colors.itemColor
        Straighten -> colors.itemColor
        Call -> colors.receiptColor
        Email -> colors.invoiceColor
        else -> colors.customerColor
    }
}
