package com.elvan.udukkai.ui.screens.tools

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.ElvanLazyColumn
import com.elvan.udukkai.ui.components.shell.ElvanSectionContainer
import com.elvan.udukkai.ui.components.shell.ElvanSettingsDivider
import com.elvan.udukkai.ui.components.shell.ElvanSettingsRow
import com.elvan.udukkai.ui.components.shell.ElvanSettingsSection
import com.elvan.udukkai.ui.components.shell.LocalElvanTopSpacerHeight
import com.elvan.udukkai.ui.navigation.ActiveSubpage
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.settings.SectionSearchEntry

private class Tool(val title: String, val description: String?, val icon: ImageVector, val run: () -> Unit)

/** Search destinations for the Menu tab. */
@Composable
fun toolSectionSearchEntries(onOpen: (ActiveSubpage) -> Unit): List<SectionSearchEntry> = listOf(
    SectionSearchEntry(K.letterhead.tr(), K.letterheadDesc.tr(), MaterialSymbols.Rounded.PrintFill, listOf("letterhead", "print", "preview")) { onOpen(ActiveSubpage.LetterheadPreview) },
    SectionSearchEntry(K.reports.tr(), K.reportsDesc.tr(), MaterialSymbols.Rounded.DescriptionFill, listOf("reports", "summary")) { onOpen(ActiveSubpage.Reports) },
    SectionSearchEntry(K.financialYears.tr(), K.financialYearsDesc.tr(), MaterialSymbols.Rounded.CalendarFill, listOf("financial", "year", "books")) { onOpen(ActiveSubpage.FinancialYears) },
    SectionSearchEntry(K.recycleBin.tr(), K.recycleBin30Days.tr(), MaterialSymbols.Rounded.Delete, listOf("recycle", "bin", "deleted", "restore")) { onOpen(ActiveSubpage.RecycleBin) }
)

@Composable
private fun ToolGroup(tools: List<Tool>, colors: ShellColors) {
    ElvanSectionContainer {
        ElvanSettingsSection(colors = colors) {
            tools.forEachIndexed { index, tool ->
                if (index > 0) ElvanSettingsDivider(colors = colors)
                ElvanSettingsRow(
                    icon = tool.icon,
                    title = tool.title,
                    description = tool.description,
                    onClick = tool.run,
                    colors = colors
                )
            }
        }
    }
}

/** The Menu tab: full-width settings rows keep every destination equal and easy to scan. */
@Composable
fun ToolsScreen(
    scrollState: LazyListState,
    onOpen: (ActiveSubpage) -> Unit,
    query: String = "",
    colors: ShellColors = rememberShellColors()
) {
    val primaryTools = listOf(
        Tool(K.letterhead.tr(), K.letterheadDesc.tr(), MaterialSymbols.Rounded.PrintFill) { onOpen(ActiveSubpage.LetterheadPreview) },
        Tool(K.reports.tr(), K.reportsDesc.tr(), MaterialSymbols.Rounded.DescriptionFill) { onOpen(ActiveSubpage.Reports) },
        Tool(K.financialYears.tr(), K.financialYearsDesc.tr(), MaterialSymbols.Rounded.CalendarFill) { onOpen(ActiveSubpage.FinancialYears) }
    )
    val maintenanceTools = listOf(
        Tool(K.recycleBin.tr(), K.recycleBin30Days.tr(), MaterialSymbols.Rounded.Delete) { onOpen(ActiveSubpage.RecycleBin) }
    )
    val q = query.trim()
    fun List<Tool>.matching() = if (q.isEmpty()) this else filter { tool ->
        tool.title.contains(q, ignoreCase = true) ||
            tool.description?.contains(q, ignoreCase = true) == true
    }
    val primary = primaryTools.matching()
    val maintenance = maintenanceTools.matching()
    ElvanLazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = Dimens.ContentPaddingBottom),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
    ) {
        item(key = "tools_top_spacer") { Spacer(Modifier.height(LocalElvanTopSpacerHeight.current)) }
        if (primary.isNotEmpty()) item(key = "primary_tools") { ToolGroup(primary, colors) }
        if (maintenance.isNotEmpty()) item(key = "maintenance_tools") { ToolGroup(maintenance, colors) }
    }
}
