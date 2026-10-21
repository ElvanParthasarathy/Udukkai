package com.elvan.udukkai.ui.screens.view

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.elvan.udukkai.core.platform.AppBackHandler
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.ElvanFloatingAction
import com.elvan.udukkai.ui.components.shell.ElvanFloatingActionBar
import com.elvan.udukkai.ui.components.shell.ElvanSnackbar
import com.elvan.udukkai.ui.components.shell.ElvanSubShell
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.preview.PreviewScreen

/**
 * Floating action bar for View (Paarvai) screens.
 * Displays Edit ("மாற்றியமை"), Preview ("முன்னோட்டம்"), Print ("அச்சிடு"), and Share ("பகிர்") in a unified floating pill.
 * Powered by the shared ElvanFloatingActionBar component.
 */
@Composable
fun ElvanViewActionBar(
    onEdit: (() -> Unit)? = null,
    onPreview: (() -> Unit)? = null,
    onPrint: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null,
    onCopy: (() -> Unit)? = null, // Deprecated / Ignored per requirement
    modifier: Modifier = Modifier
) {
    val editLabel = K.maatruToolbarBtn.tr()
    val previewLabel = K.munvizhiToolbarBtn.tr()
    val printLabel = K.printBtn.tr()
    val shareLabel = K.shareBtn.tr()
    val underConstructionMsg = K.previewUnderConstruction.tr()

    val actions = mutableListOf<ElvanFloatingAction>()
    if (onEdit != null) {
        actions.add(
            ElvanFloatingAction(
                label = editLabel,
                icon = MaterialSymbols.Rounded.Edit,
                onClick = onEdit
            )
        )
    }
    actions.add(
        ElvanFloatingAction(
            label = previewLabel,
            icon = MaterialSymbols.Rounded.Visibility,
            onClick = onPreview ?: { ElvanSnackbar.show(underConstructionMsg) }
        )
    )
    actions.add(
        ElvanFloatingAction(
            label = printLabel,
            icon = MaterialSymbols.Rounded.Print,
            onClick = onPrint ?: { ElvanSnackbar.show(underConstructionMsg) }
        )
    )
    actions.add(
        ElvanFloatingAction(
            label = shareLabel,
            icon = MaterialSymbols.Rounded.Share,
            onClick = onShare ?: { ElvanSnackbar.show(underConstructionMsg) }
        )
    )

    ElvanFloatingActionBar(
        actions = actions,
        modifier = modifier
    )
}

/**
 * Universal View Shell (Paarvai) for Compose Multiplatform.
 * Replicates Flutter's `elvan_paarvai_oadu.dart`.
 * Wraps read-only detail views with One UI collapsible shell, smooth bottom fade mask,
 * and unified bottom floating action pill: Edit, Preview, Print, and Share.
 */
@Composable
fun ViewTile(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    scrollState: LazyListState = rememberLazyListState(),
    onEdit: (() -> Unit)? = null,
    onPrint: (() -> Unit)? = null,
    onCopy: (() -> Unit)? = null, // Deprecated / Ignored
    onShare: (() -> Unit)? = null,
    onPreview: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val colors = rememberShellColors()
    var showPreview by remember { mutableStateOf(false) }

    ElvanSubShell(
        title = title,
        onBack = onBack,
        scrollState = scrollState,
        hasActions = false,
        actions = {},
        showBottomFade = true,
        floatingBar = {
            ElvanViewActionBar(
                onEdit = onEdit,
                onPreview = onPreview ?: { showPreview = true },
                onPrint = onPrint,
                onShare = onShare
            )
        },
        content = {
            Box(modifier = modifier.fillMaxSize()) {
                content()
            }
        }
    )

    if (showPreview) {
        Dialog(
            onDismissRequest = { showPreview = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            PreviewScreen(
                title = K.previewBtn.tr(),
                subtitle = title,
                onBack = { showPreview = false }
            )
        }
    }
}
