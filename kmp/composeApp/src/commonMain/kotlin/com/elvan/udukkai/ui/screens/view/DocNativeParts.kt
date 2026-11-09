package com.elvan.udukkai.ui.screens.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.compositionLocalOf
import com.elvan.udukkai.ui.components.shell.LocalElvanTopSpacerHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.platform.getNavBarHeightIgnoringVisibility
import com.elvan.udukkai.core.utils.CurrencyUtils
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.ElvanSettingsDivider
import com.elvan.udukkai.ui.components.shell.ElvanSettingsSection
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/** One bill line as the view page shows it. */
data class BillLine(val name: String, val secondName: String, val detail: String, val amount: Double)

/** Paid (green), money still to come (calm grey, like a draft) or neither (proforma). */
enum class DocTone { Paid, Due, Neutral }

@Composable
private fun toneColor(tone: DocTone): Color {
    val dark = rememberShellColors().isDark
    return when (tone) {
        DocTone.Paid -> if (dark) Color(0xFF4ADE80) else Color(0xFF15803D)
        DocTone.Due -> if (dark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
        DocTone.Neutral -> rememberShellColors().textPrimary
    }
}

/** The list state of the view page, given by the page shell so its header fades as the page scrolls. */
val LocalDocListState = compositionLocalOf<LazyListState?> { null }

/** The scrolling page, under the same collapsing, fading header as the customer page. */
/** True when the page is wider than tall: the status block shrinks so the bill shows sooner. */
val LocalDocLandscape = androidx.compose.runtime.compositionLocalOf { false }

@Composable
fun DocPage(
    horizontalPadding: Dp = Dimens.ContentPadding,
    fullWidthKeys: Set<Any?> = setOf("top_spacer", "status", "record", "convert", "lines"),
    content: LazyListScope.() -> Unit
) {
    androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxSize()) {
    val isLandscape = maxWidth > maxHeight
    val sideways = isLandscape || com.elvan.udukkai.ui.components.shell.LocalElvanSideBar.current
    androidx.compose.runtime.CompositionLocalProvider(LocalDocLandscape provides isLandscape) {
    com.elvan.udukkai.ui.screens.editor.ElvanEditorList(
        fullWidthKeys = fullWidthKeys,
        state = LocalDocListState.current ?: rememberLazyListState(),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = horizontalPadding,
            end = horizontalPadding,
            bottom = if (sideways) 20.dp else (getNavBarHeightIgnoringVisibility() + 120.dp)
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "top_spacer") { Spacer(Modifier.height((LocalElvanTopSpacerHeight.current - (if (com.elvan.udukkai.ui.components.shell.LocalElvanSideBar.current) 37.dp else 0.dp)).coerceAtLeast(0.dp))) }
        content()
    }
    }
    }
}

/** Big status block at the top: icon, what it means and the amount. */
@Composable
fun DocStatusHeader(tone: DocTone, label: String, amount: Double, note: String? = null) {
    val colors = rememberShellColors()
    val ff = LocalAppFontFamily.current
    val tint = toneColor(tone)
    val shape = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(28.dp))
        .background(if (tone == DocTone.Neutral) colors.surface else tint.copy(alpha = if (colors.isDark) 0.14f else 0.09f))
    val body: @Composable () -> Unit = {
        run {
            val darkGreen = tone == DocTone.Paid && colors.isDark
            Box(
                Modifier.size(if (LocalDocLandscape.current) 40.dp else 52.dp).clip(CircleShape).background(
                    // A clear, deeper tone of the hue in both modes, so the white symbol stands out and never blends into it
                    (if (tone == DocTone.Paid) colors.receiptColor else if (tone == DocTone.Neutral) toneColor(DocTone.Due) else tint).let { base ->
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            if (darkGreen) listOf(androidx.compose.ui.graphics.lerp(tint, Color.White, 0.25f), tint) // the dark mode green, with its dark symbol
                            else listOf(
                                androidx.compose.ui.graphics.lerp(base, Color.Black, if (colors.isDark) 0.25f else 0.12f),
                                androidx.compose.ui.graphics.lerp(base, Color.Black, if (colors.isDark) 0.45f else 0.32f)
                            )
                        )
                    }
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (tone) { DocTone.Paid -> MaterialSymbols.Rounded.Check; DocTone.Due -> MaterialSymbols.Rounded.Schedule; DocTone.Neutral -> MaterialSymbols.Rounded.InvoiceFill },
                    contentDescription = null,
                    tint = if (darkGreen) Color(0xFF0B1210) else Color.White,
                    modifier = Modifier.size(if (LocalDocLandscape.current) (if (darkGreen) 22.dp else 26.dp) else if (darkGreen) 28.dp else 32.dp)
                )
            }
            Spacer(Modifier.height(if (LocalDocLandscape.current) 6.dp else 14.dp))
        }
        Text(
            text = label.preventBrokenLigatures(),
            style = TextStyle(fontFamily = ff, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = if (tone == DocTone.Neutral) colors.textSecondary else tint),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = CurrencyUtils.formatInr(amount),
            style = TextStyle(fontFamily = ff, fontSize = if (LocalDocLandscape.current) 26.sp else 32.sp, fontWeight = FontWeight.ExtraBold, color = colors.textPrimary),
            maxLines = 1
        )
        if (!note.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = note.preventBrokenLigatures(),
                style = TextStyle(fontFamily = ff, fontSize = 13.sp, color = colors.textSecondary),
                textAlign = TextAlign.Center
            )
        }
    }
    if (LocalDocLandscape.current) {
        // Sideways: the tick or clock, the words and the amount in one line
        Row(shape.padding(vertical = 12.dp, horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)) { body() }
    } else {
        Column(shape.padding(vertical = 26.dp, horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) { body() }
    }
}

/** The customer: letter avatar, name in both languages and town. Opens the customer when [onClick] is given. */
@Composable
fun DocCustomerCard(name: String, secondName: String, town: String, onClick: (() -> Unit)?) {
    val colors = rememberShellColors()
    val ff = LocalAppFontFamily.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.surface)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // The same customer icon as the customer cards
        com.elvan.udukkai.ui.components.ElvanCardLeadingIcon(
            icon = MaterialSymbols.Rounded.Person,
            tint = colors.textPrimary // mono here, not the purple of the list cards
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(name.preventBrokenLigatures(), style = TextStyle(fontFamily = ff, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary), maxLines = 2, overflow = TextOverflow.Ellipsis)
            val sub = listOf(secondName, town).filter { it.isNotBlank() && it != name }.joinToString(" • ")
            if (sub.isNotBlank()) {
                Text(sub.preventBrokenLigatures(), style = TextStyle(fontFamily = ff, fontSize = 13.sp, color = colors.textSecondary), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        if (onClick != null) {
            Icon(MaterialSymbols.Rounded.ChevronRight, contentDescription = null, tint = colors.textTertiary, modifier = Modifier.size(22.dp))
        }
    }
}

/** A titled card of rows, in the app's settings-card look. */
@Composable
fun DocSection(title: String?, content: @Composable ColumnScope.() -> Unit) {
    ElvanSettingsSection(title = title, colors = rememberShellColors(), content = content)
}

@Composable
fun DocDivider() = ElvanSettingsDivider(colors = rememberShellColors())

/** Label at the left, value at the right. */
@Composable
fun DocRow(label: String, value: String, strong: Boolean = false, valueColor: Color? = null) {
    val colors = rememberShellColors()
    val ff = LocalAppFontFamily.current
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            label.preventBrokenLigatures(),
            Modifier.weight(1f),
            style = TextStyle(fontFamily = ff, fontSize = if (strong) 15.sp else 14.sp, fontWeight = if (strong) FontWeight.Bold else FontWeight.Normal, color = if (strong) colors.textPrimary else colors.textSecondary)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            value.preventBrokenLigatures(),
            style = TextStyle(fontFamily = ff, fontSize = if (strong) 16.sp else 14.sp, fontWeight = if (strong) FontWeight.ExtraBold else FontWeight.SemiBold, color = valueColor ?: colors.textPrimary),
            textAlign = TextAlign.End
        )
    }
}

/** A tappable row that opens a linked document (a receipt from a bill, a bill from a receipt). */
@Composable
fun DocLinkRow(icon: ImageVector, title: String, subtitle: String, amount: Double?, onClick: (() -> Unit)?, tint: Color? = null) {
    val colors = rememberShellColors()
    val ff = LocalAppFontFamily.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(38.dp).clip(CircleShape).background(colors.iconBg), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title.preventBrokenLigatures(), style = TextStyle(fontFamily = ff, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary), maxLines = 1)
            if (subtitle.isNotBlank()) Text(subtitle.preventBrokenLigatures(), style = TextStyle(fontFamily = ff, fontSize = 12.5.sp, color = colors.textSecondary), maxLines = 1)
        }
        if (amount != null) {
            Text(CurrencyUtils.formatInr(amount), style = TextStyle(fontFamily = ff, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary))
        }
        if (onClick != null) {
            Spacer(Modifier.width(6.dp))
            Icon(MaterialSymbols.Rounded.ChevronRight, contentDescription = null, tint = colors.textTertiary, modifier = Modifier.size(20.dp))
        }
    }
}

/** One bill line: names at the left, quantity × rate below, the amount at the right. */
@Composable
fun DocLineRow(line: BillLine) {
    val colors = rememberShellColors()
    val ff = LocalAppFontFamily.current
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f)) {
            Text(line.name.preventBrokenLigatures(), style = TextStyle(fontFamily = ff, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary))
            if (line.secondName.isNotBlank() && line.secondName != line.name) {
                Text(line.secondName.preventBrokenLigatures(), style = TextStyle(fontFamily = ff, fontSize = 13.sp, color = colors.textSecondary))
            }
            if (line.detail.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(line.detail.preventBrokenLigatures(), style = TextStyle(fontFamily = ff, fontSize = 12.5.sp, color = colors.textTertiary))
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(CurrencyUtils.formatInr(line.amount), style = TextStyle(fontFamily = ff, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary))
    }
}

/** A full-width primary button (Record payment on a bill still due). */
@Composable
fun DocPrimaryButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    val colors = rememberShellColors()
    val ff = LocalAppFontFamily.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(colors.textPrimary)
            .clickable(
                interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = androidx.compose.material3.ripple(color = colors.background.copy(alpha = 0.35f), bounded = true),
                role = androidx.compose.ui.semantics.Role.Button,
                onClick = onClick
            )
            .padding(vertical = 15.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = colors.background, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(label.preventBrokenLigatures(), style = TextStyle(fontFamily = ff, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.background))
    }
}

/** Plain text in a card (notes, amount in words). */
@Composable
fun DocTextCard(title: String, lines: List<Pair<String, Boolean>>) {
    val colors = rememberShellColors()
    val ff = LocalAppFontFamily.current
    DocSection(title) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            lines.filter { it.first.isNotBlank() }.forEach { (text, main) ->
                Text(text.preventBrokenLigatures(), style = TextStyle(fontFamily = ff, fontSize = if (main) 14.sp else 12.5.sp, color = if (main) colors.textPrimary else colors.textSecondary))
            }
        }
    }
}
