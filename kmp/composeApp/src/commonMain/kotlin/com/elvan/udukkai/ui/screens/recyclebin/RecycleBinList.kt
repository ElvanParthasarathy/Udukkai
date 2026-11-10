package com.elvan.udukkai.ui.screens.recyclebin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.ui.components.EqualHeightCard
import com.elvan.udukkai.ui.components.EqualHeightCardRow
import com.elvan.udukkai.ui.components.recordIdsOfKey
import com.elvan.udukkai.ui.components.shell.ElvanLazyColumn

private class BinItem(val key: Any?, val body: @Composable LazyItemScope.() -> Unit)
private class BinScope : LazyListScope {
    val entries = mutableListOf<BinItem>()
    override fun item(key: Any?, contentType: Any?, content: @Composable LazyItemScope.() -> Unit) { entries += BinItem(key, content) }
    override fun items(count: Int, key: ((Int) -> Any)?, contentType: (Int) -> Any?, itemContent: @Composable LazyItemScope.(Int) -> Unit) {
        repeat(count) { index -> entries += BinItem(key?.invoke(index)) { itemContent(index) } }
    }
}

/** The same level, paired cards as the main lists, retaining record IDs for drag selection. */
@Composable
fun RecycleBinList(
    state: LazyListState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(12.dp),
    content: LazyListScope.() -> Unit
) {
    BoxWithConstraints(modifier) {
        val landscape = com.elvan.udukkai.core.platform.platformLayoutPolicy.screenLayout(maxWidth, maxHeight) == com.elvan.udukkai.core.platform.ScreenLayout.AndroidLandscape
        if (!landscape) {
            ElvanLazyColumn(state = state, modifier = Modifier.fillMaxSize(), contentPadding = contentPadding, verticalArrangement = verticalArrangement, content = content)
            return@BoxWithConstraints
        }
        val rows = mutableListOf<List<BinItem>>()
        var pending: BinItem? = null
        for (entry in BinScope().also { it.content() }.entries) {
            if (entry.key == null || recordIdsOfKey(entry.key).isEmpty()) {
                pending?.let { rows += listOf(it) }; pending = null
                rows += listOf(entry)
            } else if (pending == null) pending = entry
            else { rows += listOf(pending, entry); pending = null }
        }
        pending?.let { rows += listOf(it) }
        ElvanLazyColumn(state = state, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 12.dp), verticalArrangement = verticalArrangement, landscapeRoot = true) {
            rows.forEach { row ->
                val ids = row.flatMap { it.key?.let(::recordIdsOfKey).orEmpty() }
                item(key = if (ids.isEmpty()) row.first().key else "item_pair_${ids.joinToString("_")}") {
                    if (ids.isEmpty()) row.first().body(this)
                    else EqualHeightCardRow(Modifier.fillMaxWidth()) {
                        row.forEach { card -> EqualHeightCard { card.body(this@item) } }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
