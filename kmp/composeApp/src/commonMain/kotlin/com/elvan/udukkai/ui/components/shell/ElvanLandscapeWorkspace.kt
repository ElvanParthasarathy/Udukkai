package com.elvan.udukkai.ui.components.shell

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Android landscape's shared overview/detail workspace, with independent scrolling and safe-area spacing. */
@Composable
fun ElvanLandscapeWorkspace(
    modifier: Modifier = Modifier,
    overviewState: LazyListState = rememberLazyListState(),
    detailState: LazyListState = rememberLazyListState(),
    overviewWeight: Float = 0.38f,
    overview: LazyListScope.() -> Unit,
    detail: LazyListScope.() -> Unit
) {
    Row(modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        ElvanLazyColumn(Modifier.weight(overviewWeight).fillMaxHeight(), overviewState,
            verticalArrangement = Arrangement.spacedBy(12.dp), landscapeRoot = true, content = overview)
        ElvanLazyColumn(Modifier.weight(1f - overviewWeight).fillMaxHeight(), detailState,
            verticalArrangement = Arrangement.spacedBy(12.dp), landscapeRoot = true, content = detail)
    }
}
