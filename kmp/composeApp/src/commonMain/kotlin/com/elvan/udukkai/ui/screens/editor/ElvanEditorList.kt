package com.elvan.udukkai.ui.screens.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Sections that always take the full width, even sideways. */
private val FULL_WIDTH_KEYS = setOf("top_spacer", "paid_locked_banner", "unconfigured_warning", "items_section")

private class RecordedItem(val key: Any?, val content: @Composable LazyItemScope.() -> Unit)

private class RecordingScope : LazyListScope {
    val items = mutableListOf<RecordedItem>()
    override fun item(key: Any?, contentType: Any?, content: @Composable LazyItemScope.() -> Unit) {
        items += RecordedItem(key, content)
    }
    override fun items(count: Int, key: ((Int) -> Any)?, contentType: (Int) -> Any?, itemContent: @Composable LazyItemScope.(Int) -> Unit) {
        repeat(count) { i -> items += RecordedItem(key?.invoke(i)) { itemContent(i) } }
    }
}

/**
 * The editors' list of sections. Upright it is a plain LazyColumn. Wider than tall the sections go two to a row, in
 * reading order (1 2 / 3 4), so the numbered steps sit side by side; a section on its own is kept to a readable width.
 */
@Composable
fun ElvanEditorList(
    state: LazyListState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    /** False keeps every section full width (the bill editors put their fields side by side inside the section instead). */
    pairSections: Boolean = true,
    fullWidthKeys: Set<Any?> = FULL_WIDTH_KEYS,
    content: LazyListScope.() -> Unit
) {
    BoxWithConstraints(modifier) {
        if (maxWidth > maxHeight && maxWidth >= 560.dp) {
            val recorded = RecordingScope().also { it.content() }.items
            val rows = mutableListOf<List<RecordedItem>>()
            var pending: RecordedItem? = null
            for (item in recorded) {
                if (!pairSections || item.key in fullWidthKeys) {
                    pending?.let { rows += listOf(it) }
                    pending = null
                    rows += listOf(item)
                } else if (pending == null) {
                    pending = item
                } else {
                    rows += listOf(pending, item)
                    pending = null
                }
            }
            pending?.let { rows += listOf(it) }
            LazyColumn(state = state, modifier = Modifier.fillMaxSize(), contentPadding = contentPadding, verticalArrangement = verticalArrangement) {
                rows.forEach { row ->
                    item(key = row.first().key) {
                        val fullKey = !pairSections || row.first().key in fullWidthKeys
                        when {
                            fullKey -> row.first().content(this)
                            row.size == 2 -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top) {
                                row.forEach { Box(Modifier.weight(1f)) { it.content(this@item) } }
                            }
                            else -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                                Box(Modifier.widthIn(max = 640.dp)) { row.first().content(this@item) }
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(state = state, modifier = Modifier.fillMaxSize(), contentPadding = contentPadding, verticalArrangement = verticalArrangement, content = content)
        }
    }
}

/**
 * A section's fields: one under another upright; sideways (wide enough) two to a row, as the web editor lays them out.
 */
@Composable
fun ElvanFieldGrid(spacing: androidx.compose.ui.unit.Dp = 24.dp, content: @Composable () -> Unit) {
    androidx.compose.ui.layout.Layout(content = content, modifier = Modifier.fillMaxWidth()) { measurables, constraints ->
        val gap = spacing.roundToPx()
        val sideGap = 16.dp.roundToPx()
        val two = constraints.maxWidth >= 520.dp.roundToPx() && measurables.size > 1
        val colWidth = if (two) (constraints.maxWidth - sideGap) / 2 else constraints.maxWidth
        val c = androidx.compose.ui.unit.Constraints(minWidth = colWidth, maxWidth = colWidth)
        val placeables = measurables.map { it.measure(c) }
        val cols = if (two) 2 else 1
        val rowHeights = placeables.chunked(cols).map { row -> row.maxOf { it.height } }
        val height = rowHeights.sum() + gap * (rowHeights.size - 1).coerceAtLeast(0)
        layout(constraints.maxWidth, height) {
            var y = 0
            placeables.chunked(cols).forEachIndexed { r, row ->
                row.forEachIndexed { i, p -> p.placeRelative(i * (colWidth + sideGap), y) }
                y += rowHeights[r] + gap
            }
        }
    }
}
