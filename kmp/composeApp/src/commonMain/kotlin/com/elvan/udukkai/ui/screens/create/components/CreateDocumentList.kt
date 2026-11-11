package com.elvan.udukkai.ui.screens.create.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.platform.getImeBottomPadding
import com.elvan.udukkai.core.platform.getStatusBarTopPadding
import com.elvan.udukkai.core.utils.DateGroup
import com.elvan.udukkai.core.utils.DateGroupUtils
import com.elvan.udukkai.localization.LocalAppLanguage
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.ui.components.elvanDocumentScrollbar
import com.elvan.udukkai.ui.components.shell.ElvanLazyColumn
import com.elvan.udukkai.ui.components.shell.LocalElvanHeaderSettledCollapsed
import com.elvan.udukkai.ui.components.shell.LocalElvanTopSpacerHeight
import com.elvan.udukkai.ui.components.shell.LocalPortraitPageBottom

@Composable
internal fun CreateDocumentList(
    state: LazyListState,
    listModifier: Modifier,
    contentPadding: PaddingValues,
    landscape: Boolean,
    colors: ShellColors,
    groups: List<DateGroup<*>>,
    filter: (@Composable () -> Unit)?,
    tabs: @Composable () -> Unit,
    content: LazyListScope.() -> Unit
) {
    val bottom = LocalPortraitPageBottom.current
    val top = LocalElvanTopSpacerHeight.current
    val scrollbar = if (!landscape && bottom != null) {
        val language = LocalAppLanguage.current
        val labels = remember(groups, language) {
            buildMap {
                var index = 2 // Header spacer and tabs with the optional filter.
                groups.forEach { group ->
                    index++ // Date heading.
                    val (day, _) = DateGroupUtils.formatDateHeader(group.dateMillis, false, language)
                    val weekday = DateGroupUtils.getWeekdaySubtitle(group.dateMillis, false, language)
                    val label = "$day, $weekday".preventBrokenLigatures()
                    repeat(group.items.size) { put(index++, label) }
                }
            }
        }
        val documentIndices = remember(labels) { labels.keys.toIntArray() }
        Modifier.elvanDocumentScrollbar(
            scrollState = state,
            enabled = LocalElvanHeaderSettledCollapsed.current && getImeBottomPadding() == 0.dp,
            topInset = maxOf(top + Dimens.SectionSpacing, getStatusBarTopPadding() + 78.dp),
            bottomInset = bottom,
            color = colors.textTertiary,
            documentIndices = documentIndices,
            labelForIndex = { labels[it].orEmpty() },
            leadingItemKey = "top_shifters_container",
            labelStyle = TextStyle(color = colors.textPrimary, fontFamily = LocalAppFontFamily.current, fontSize = 13.sp),
            labelBackground = lerp(colors.background, colors.textPrimary, if (colors.isDark) 0.27f else 0.10f)
        )
    } else Modifier
    ElvanLazyColumn(
        modifier = Modifier.fillMaxSize().then(scrollbar).then(listModifier),
        state = state,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "uruvakku_top_spacer") { Spacer(Modifier.height(top)) }
        item(key = "top_shifters_container") {
            Column(Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                tabs()
                filter?.invoke()
            }
        }
        content()
    }
}
