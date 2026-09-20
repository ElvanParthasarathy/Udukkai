package com.elvan.udukkai.ui.screens.view

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.ui.components.shell.ElvanActionButton
import com.elvan.udukkai.ui.components.shell.ElvanSubShell

/**
 * Universal View Shell (Paarvai) for Compose Multiplatform.
 * Replicates Flutter's `elvan_paarvai_oadu.dart`.
 * Wraps read-only detail & print views with optional "அச்சிடு" (Print) and "மாற்றியமை" (Edit) actions.
 */
@Composable
fun ViewTile(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    scrollState: LazyListState = rememberLazyListState(),
    onEdit: (() -> Unit)? = null,
    onPrint: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    ElvanSubShell(
        title = title,
        onBack = onBack,
        scrollState = scrollState,
        hasActions = onEdit != null || onPrint != null,
        actions = {
            if (onPrint != null) {
                ElvanActionButton(
                    label = K.printBtn.tr(),
                    onClick = onPrint
                )
            }
            if (onEdit != null) {
                ElvanActionButton(
                    label = K.editRecord.tr(),
                    onClick = onEdit
                )
            }
        },
        content = content
    )
}
