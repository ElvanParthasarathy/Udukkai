package com.elvan.udukkai.ui.screens.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.ui.components.shell.ElvanPopupMenuItem
import com.elvan.udukkai.ui.components.shell.LocalElvanTopSpacerHeight
import com.elvan.udukkai.ui.screens.create.components.ElvanPillShifter
import com.elvan.udukkai.ui.screens.create.components.PillShifterItem

private val SHIFTER_HEIGHT = 48.dp
internal val RECORD_GAP = 12.dp

/**
 * The page of a customer or item: the shifter comes first, under the plain top bar, and the name is part of the Details
 * tab, so nothing above the shifter can move it. A short tab is padded with blank space to exactly fill the screen, so
 * the cards stop under the bar and the shifter stays where it is when the tab changes.
 */
@Composable
fun ViewRecordPage(
    title: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onCopy: (() -> Unit)?,
    menuItems: List<ElvanPopupMenuItem>,
    colors: ShellColors,
    scrollState: LazyListState,
    history: ViewHistoryState,
    historyUi: ViewHistoryUi,
    onBillClick: (Long) -> Unit,
    onReceiptClick: (Long) -> Unit,
    onRetryHistory: () -> Unit,
    details: @Composable ColumnScope.() -> Unit
) {
    var section by rememberSaveable { mutableIntStateOf(0) }

    ViewTile(
        title = title,
        onBack = onBack,
        scrollState = scrollState,
        onEdit = onEdit,
        onCopy = onCopy,
        menuItems = menuItems
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val density = LocalDensity.current
            val topSpace = LocalElvanTopSpacerHeight.current
            val bottomPadding = Dimens.SubpageContentPaddingBottom + 64.dp
            val spacing = RECORD_GAP

            // How far the list has moved, for the items above the tab's content (spacer, shifter, type row)
            fun scrolledPx(): Int = scrollState.firstVisibleItemScrollOffset + with(density) {
                when (scrollState.firstVisibleItemIndex) {
                    0 -> 0
                    1 -> topSpace.roundToPx()
                    else -> (topSpace + SHIFTER_HEIGHT + spacing).roundToPx()
                }
            }
            // Kept while scrolling back up, shrinking with it: the new content can hold the old position but not scroll further
            val held = remember(section, historyUi.tab, historyUi.filter, historyUi.sort) { scrolledPx() }

            fun heightBelow(rowAbove: Dp): Dp {
                val fill = (maxHeight - topSpace - SHIFTER_HEIGHT - spacing - rowAbove - bottomPadding).coerceAtLeast(0.dp)
                return if (held > 0) fill + with(density) { minOf(held, scrolledPx()).toDp() } else fill
            }

            LazyColumn(
                state = scrollState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = Dimens.ContentPadding, end = Dimens.ContentPadding, bottom = bottomPadding),
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                item(key = "top_spacer") { Spacer(Modifier.height(topSpace - spacing)) }
                item(key = "section_tabs") {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        ElvanPillShifter(
                            items = listOf(PillShifterItem(K.detailsTab.tr()), PillShifterItem(K.transactionsTab.tr())),
                            selectedIndex = section,
                            onIndexSelected = { section = it },
                            colors = colors,
                            isFullWidth = false
                        )
                    }
                }
                if (section == 0) {
                    item(key = "details") {
                        Column(
                            Modifier.fillMaxWidth().heightIn(min = heightBelow(0.dp)),
                            verticalArrangement = Arrangement.spacedBy(spacing),
                            content = details
                        )
                    }
                } else {
                    viewHistoryItems(history, historyUi, colors, onBillClick, onReceiptClick, onRetryHistory, heightBelow(HISTORY_TOOLS_HEIGHT + spacing))
                }
            }
        }
    }
}
