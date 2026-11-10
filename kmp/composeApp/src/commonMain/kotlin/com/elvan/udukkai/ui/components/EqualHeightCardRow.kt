package com.elvan.udukkai.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.elvan.udukkai.theme.Dimens

/** Each pair fits its tallest card, including wrapped names and optional second lines. */
@Composable
fun EqualHeightCardRow(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(Dimens.ItemSpacing),
        content = content
    )
}

@Composable
fun RowScope.EqualHeightCard(content: @Composable () -> Unit) {
    Box(Modifier.weight(1f).fillMaxHeight(), propagateMinConstraints = true) { content() }
}
