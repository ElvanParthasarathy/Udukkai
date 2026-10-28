package com.elvan.udukkai.ui.screens.view

import androidx.compose.ui.unit.IntOffset
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import com.elvan.udukkai.ui.components.shell.ElvanStandardTopBar
import com.elvan.udukkai.ui.components.shell.elvanStandardTopBarInset
import com.elvan.udukkai.ui.components.shell.LocalElvanTopSpacerHeight
import com.elvan.udukkai.core.platform.AppBackHandler
import com.elvan.udukkai.core.print.DocumentPrintAction
import com.elvan.udukkai.core.print.DocumentPrintController
import com.elvan.udukkai.core.print.DocumentPrintView
import com.elvan.udukkai.core.print.isDocumentPrintSupported
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.ElvanActionButton
import com.elvan.udukkai.ui.components.shell.ElvanFloatingAction
import com.elvan.udukkai.ui.components.shell.ElvanFloatingActionBar
import com.elvan.udukkai.ui.components.shell.ElvanSnackbar
import com.elvan.udukkai.ui.components.shell.ElvanSubShell
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.preview.PreviewScreen

/**
 * Floating action bar for View (Paarvai) screens.
 * Displays Edit ("மாற்றியமை"), Preview ("முன்னோட்டம்"), Print ("அச்சிடு"), and Share ("பகிர்") in a unified floating pill
 * for document views (Invoice / Receipt).
 * For all other entity views (Customer / Item), displays only Edit ("மாற்று") and Copy ("நகலெடு").
 * Powered by the shared ElvanFloatingActionBar component.
 */
@Composable
fun ElvanViewActionBar(
    onEdit: (() -> Unit)? = null,
    onPreview: (() -> Unit)? = null,
    onPrint: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null,
    onCopy: (() -> Unit)? = null,
    isDocument: Boolean = false,
    modifier: Modifier = Modifier
) {
    val editLabel = K.editToolbarBtn.tr()
    val previewLabel = K.previewToolbarBtn.tr()
    val printLabel = K.printBtn.tr()
    val shareLabel = K.shareBtn.tr()
    val copyLabel = K.copyBtn.tr()
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

    if (isDocument) {
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
    } else {
        // For non-document entities (Customer, Item, etc.): Only Edit and Copy!
        if (onCopy != null) {
            actions.add(
                ElvanFloatingAction(
                    label = copyLabel,
                    icon = MaterialSymbols.Rounded.ContentCopy,
                    onClick = onCopy
                )
            )
        }
    }

    ElvanFloatingActionBar(
        actions = actions,
        modifier = modifier
    )
}

/**
 * Universal View Shell (Paarvai) for Compose Multiplatform.
 * Replicates Flutter's `elvan_paarvai_oadu.dart`.
 * Wraps read-only detail views with One UI collapsible shell, smooth bottom fade mask,
 * and unified bottom floating action pill:
 * - Documents (Invoice / Receipt): Edit, Preview, Print, and Share.
 * - Entities (Customer / Item): Edit and Copy.
 */
@Composable
fun ViewTile(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    scrollState: LazyListState = rememberLazyListState(),
    isDocument: Boolean = false,
    onEdit: (() -> Unit)? = null,
    onPrint: (() -> Unit)? = null,
    onCopy: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null,
    onPreview: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val colors = rememberShellColors()
    Box(modifier = Modifier.fillMaxSize()) {
        if (isDocument) {
            // Document views use a plain fixed top bar instead of the collapsing One UI header.
            SideEffect { ElvanSnackbar.isBottomBarVisible = true }
            val navBarsPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background)
            ) {
                Box(modifier = modifier.fillMaxSize()) {
                    CompositionLocalProvider(LocalElvanTopSpacerHeight provides elvanStandardTopBarInset()) {
                        content()
                    }
                }
                val scrolled by remember(scrollState) {
                    derivedStateOf { scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 8 }
                }
                ElvanStandardTopBar(
                    title = title,
                    onBack = onBack,
                    scrolled = scrolled,
                    modifier = Modifier.align(Alignment.TopCenter).zIndex(100f)
                )
                // Soft fade above the system navigation bar, then the floating action pill
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(72.dp + navBarsPadding)
                        .background(
                            Brush.verticalGradient(
                                0.0f to Color.Transparent,
                                0.3f to colors.background.copy(alpha = 0.16f),
                                0.65f to colors.background.copy(alpha = 0.55f),
                                1.0f to colors.background
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .zIndex(100f),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    ElvanViewActionBar(
                        onEdit = onEdit,
                        onPreview = onPreview,
                        onPrint = onPrint,
                        onShare = onShare,
                        onCopy = onCopy,
                        isDocument = true
                    )
                }
            }
        } else {
            ElvanSubShell(
                title = title,
                onBack = onBack,
                scrollState = scrollState,
                hasActions = !isDocument && onEdit != null,
                actions = {
                    if (!isDocument && onEdit != null) {
                        ElvanActionButton(
                            label = K.editRecord.tr(),
                            onClick = onEdit
                        )
                    }
                },
                showBottomFade = isDocument,
                floatingBar = {
                    if (isDocument) {
                        ElvanViewActionBar(
                            onEdit = onEdit,
                            onPreview = onPreview,
                            onPrint = onPrint,
                            onShare = onShare,
                            onCopy = onCopy,
                            isDocument = true
                        )
                    }
                },
                content = {
                    Box(modifier = modifier.fillMaxSize()) {
                        content()
                    }
                }
            )
        }
    }
}
