package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.elvan.udukkai.core.finance.BillNumbering
import com.elvan.udukkai.core.finance.BooksSettings
import com.elvan.udukkai.core.utils.CurrencyUtils
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.MaterialSymbols

private const val UNLOCK = -1

/** Year-end lock and the financial years that have bills, shared by both apps. */
@Composable
fun BooksSettingsScreen(
    settings: BooksSettings,
    companyId: Long?,
    bills: List<InvoiceModel>,
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val bottomSheet = LocalElvanBottomSheetController.current
    // The lock lives in preferences, which Compose does not watch
    var version by remember { mutableIntStateOf(0) }
    var confirmUnlock by remember { mutableStateOf(false) }
    val lockTitle = K.booksLockTitle.tr()
    val unlockLabel = K.booksUnlock.tr()

    val currentFy = DateUtils.financialYearStart(System.currentTimeMillis())
    val summaries = remember(bills) { BillNumbering.summaries(bills) }
    val lockedThrough = remember(version) { settings.lockedThrough(companyId) }
    fun label(fy: Int) = "$fy-${((fy + 1) % 100).toString().padStart(2, '0')}"

    // Only years that have ended and are not locked yet; once something is locked, unlocking is offered too
    val lockedFy = lockedThrough?.let { DateUtils.financialYearStart(it * 86_400_000L) }
    val options = remember(summaries, lockedFy) {
        val first = maxOf(summaries.minOfOrNull { it.fyStart } ?: currentFy, (lockedFy ?: Int.MIN_VALUE) + 1)
        (first..<currentFy).toList().reversed() + if (lockedFy != null) listOf(UNLOCK) else emptyList()
    }

    LazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = Dimens.ContentPadding, end = Dimens.ContentPadding, bottom = Dimens.SubpageContentPaddingBottom),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
    ) {
        item(key = "shell_top_spacer") { Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current)) }

        item(key = "lock") {
            val description = lockedThrough?.let { "${K.booksLockedUpTo.tr()} ${DateUtils.formatEpochMillis(it * 86_400_000L)}" } ?: K.booksNothingLocked.tr()
            ElvanSettingsSection(colors = colors) {
                if (options.isEmpty()) {
                    ElvanSimpleSettingsRow(title = lockTitle, description = description, colors = colors)
                } else {
                    ElvanSettingsRow(
                        icon = MaterialSymbols.Rounded.Lock,
                        title = lockTitle,
                        description = description,
                        onClick = {
                            bottomSheet.showSelection(
                                title = lockTitle,
                                items = options,
                                currentValue = null,
                                itemLabelBuilder = { if (it == UNLOCK) unlockLabel else label(it) },
                                onSelected = { if (it == UNLOCK) confirmUnlock = true else { settings.lockThrough(companyId, it); version++ } }
                            )
                        },
                        colors = colors
                    )
                }
            }
        }

        item(key = "years") {
            ElvanSettingsSection(title = K.booksYears.tr(), colors = colors) {
                if (summaries.isEmpty()) {
                    ElvanSimpleSettingsRow(title = K.booksNoBills.tr(), colors = colors)
                }
                summaries.forEachIndexed { i, s ->
                    if (i > 0) ElvanSettingsDivider(colors = colors)
                    ElvanSimpleSettingsRow(
                        title = label(s.fyStart),
                        description = "${s.count} ${K.booksBills.tr()} • ${CurrencyUtils.formatInr(s.total, showDecimals = false)}",
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
            confirmText = unlockLabel,
            confirmColor = Color(0xFFBA1A1A),
            onConfirm = { settings.unlock(companyId); version++; confirmUnlock = false },
            onDismissRequest = { confirmUnlock = false }
        )
    }
}
