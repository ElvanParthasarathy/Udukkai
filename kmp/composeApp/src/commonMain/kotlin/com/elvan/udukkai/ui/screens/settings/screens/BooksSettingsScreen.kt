package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.finance.BillNumbering
import com.elvan.udukkai.core.finance.BooksSettings
import com.elvan.udukkai.core.utils.CurrencyUtils
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*

/**
 * Books and financial year, shared by both apps: the bill numbering style, the year-end lock, and what each
 * financial year holds (with any bill numbers that are missing).
 */
@Composable
fun BooksSettingsScreen(
    settings: BooksSettings,
    companyId: Long?,
    bills: List<InvoiceModel>,
    removed: List<InvoiceModel>,
    nextNumber: () -> String,
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current
    val bottomSheet = LocalElvanBottomSheetController.current
    // The settings live in preferences, which Compose does not watch
    var version by remember { mutableIntStateOf(0) }
    var confirmUnlock by remember { mutableStateOf(false) }
    val lockTitle = K.booksLockTitle.tr()

    val currentFy = DateUtils.financialYearStart(System.currentTimeMillis())
    val summaries = remember(bills, removed) { BillNumbering.summaries(bills, removed) }
    val fySeries = remember(version) { settings.fySeries(companyId) }
    val lockedThrough = remember(version) { settings.lockedThrough(companyId) }
    val next = remember(version, bills, removed) { nextNumber() }
    fun label(fy: Int) = "$fy-${((fy + 1) % 100).toString().padStart(2, '0')}"
    // Only years that have ended can be locked
    val lockable = remember(summaries) { ((summaries.minOfOrNull { it.fyStart } ?: currentFy)..<currentFy).toList().reversed() }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = Dimens.ContentPadding, end = Dimens.ContentPadding, bottom = Dimens.SubpageContentPaddingBottom),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
        ) {
            item(key = "shell_top_spacer") { Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current)) }

            item(key = "current_year") {
                ElvanSettingsSection(colors = colors) {
                    ElvanSimpleSettingsRow(
                        title = K.booksCurrentYear.tr(),
                        description = "${label(currentFy)} • ${K.booksYearRange.tr()}",
                        colors = colors
                    )
                }
            }

            item(key = "numbering") {
                ElvanSettingsSection(colors = colors) {
                    ElvanSimpleSettingsRow(
                        title = K.booksSeriesSwitch.tr(),
                        description = K.booksSeriesHint.tr(),
                        trailing = {
                            ElvanSettingsSwitch(
                                checked = fySeries,
                                onCheckedChange = { settings.setFySeries(companyId, it); version++ },
                                colors = colors
                            )
                        },
                        colors = colors
                    )
                    ElvanSettingsDivider(colors = colors)
                    ElvanSimpleSettingsRow(title = K.booksNextNumber.tr(), description = next, colors = colors)
                }
            }

            item(key = "lock") {
                ElvanSettingsSection(colors = colors) {
                    ElvanSimpleSettingsRow(
                        title = lockTitle,
                        description = lockedThrough?.let { "${K.booksLockedUpTo.tr()} ${DateUtils.formatEpochMillis(it * 86_400_000L)}" } ?: K.booksNothingLocked.tr(),
                        trailing = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (lockedThrough != null) ElvanTextAction(K.booksUnlock.tr(), Color(0xFFBA1A1A), ff) { confirmUnlock = true }
                                if (lockable.isNotEmpty()) ElvanTextAction(K.booksLockYear.tr(), colors.accent, ff) {
                                    bottomSheet.showSelection(
                                        title = lockTitle,
                                        items = lockable,
                                        currentValue = null,
                                        itemLabelBuilder = { label(it) },
                                        onSelected = { settings.lockThrough(companyId, it); version++ }
                                    )
                                }
                            }
                        },
                        colors = colors
                    )
                    ElvanSettingsDivider(colors = colors)
                    ElvanSimpleSettingsRow(title = K.booksLockHint.tr(), colors = colors)
                }
            }

            item(key = "years_title") {
                Text(
                    text = K.booksYears.tr(),
                    style = TextStyle(fontFamily = ff, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary.copy(alpha = 0.6f)),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            if (summaries.isEmpty()) {
                item(key = "years_empty") {
                    ElvanSettingsSection(colors = colors) { ElvanSimpleSettingsRow(title = K.booksNoBills.tr(), colors = colors) }
                }
            } else {
                items(summaries.size, key = { "fy_${summaries[it].fyStart}" }) { i ->
                    val s = summaries[i]
                    val missing = if (s.gaps.isEmpty()) K.booksNoMissing.tr() else
                        "${K.booksMissing.tr()}: " + s.gaps.entries.joinToString("; ") { (series, serials) ->
                            series + serials.take(12).joinToString(", ") + if (serials.size > 12) " +${serials.size - 12}" else ""
                        }
                    ElvanSettingsSection(colors = colors) {
                        ElvanSimpleSettingsRow(
                            title = label(s.fyStart),
                            description = "${s.count} ${K.booksBills.tr()} • ${CurrencyUtils.formatInr(s.total, showDecimals = false)}\n${s.firstNumber} → ${s.lastNumber}\n$missing",
                            colors = colors
                        )
                    }
                }
            }
        }

        if (confirmUnlock) {
            ElvanActionSheet(
                title = K.booksUnlockWarn.tr(),
                cancelText = K.cancelBtn.tr(),
                confirmText = K.booksUnlock.tr(),
                confirmColor = Color(0xFFBA1A1A),
                onConfirm = { settings.unlock(companyId); version++; confirmUnlock = false },
                onDismissRequest = { confirmUnlock = false }
            )
        }
    }
}

@Composable
private fun ElvanTextAction(text: String, color: Color, ff: androidx.compose.ui.text.font.FontFamily, onClick: () -> Unit) {
    androidx.compose.material3.TextButton(onClick = onClick) {
        Text(text, style = TextStyle(fontFamily = ff, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = color))
    }
}
