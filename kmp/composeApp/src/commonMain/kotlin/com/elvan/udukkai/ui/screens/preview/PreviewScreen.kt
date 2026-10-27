package com.elvan.udukkai.ui.screens.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import com.elvan.udukkai.core.print.PrintNoticeKind
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import com.elvan.udukkai.ui.components.shell.ElvanZoomableImage
import com.elvan.udukkai.ui.components.shell.ElvanFloatingAction
import com.elvan.udukkai.ui.components.shell.ElvanFloatingActionBar
import com.elvan.udukkai.ui.components.shell.ElvanStandardTopBar
import com.elvan.udukkai.ui.components.shell.elvanStandardTopBarInset
import com.elvan.udukkai.core.print.DocumentPrintController
import com.elvan.udukkai.core.print.DocumentPrintView
import com.elvan.udukkai.core.print.isDocumentPrintSupported
import com.elvan.udukkai.ui.components.shell.ElvanActionButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.extensions.cssShadow
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.ui.components.shell.LocalElvanTopSpacerHeight
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.ElvanSubShell
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * Universal Preview Screen (முன்னோட்டம்).
 * When a [printPayload] is given (and the platform supports it), renders the real
 * document with the React print templates and offers Print / Share. Otherwise
 * shows the placeholder preview.
 */
@Composable
fun PreviewScreen(
    title: String = K.previewBtn.tr(),
    subtitle: String? = null,
    onBack: () -> Unit,
    printPayload: String? = null,
    printFileName: String = subtitle ?: title,
    printController: DocumentPrintController? = null,
    externalPrintView: Boolean = false
) {
    if (printPayload != null && isDocumentPrintSupported) {
        val controller = printController ?: remember { DocumentPrintController() }
        DocumentPreview(
            title = title,
            onBack = onBack,
            payloadJson = printPayload,
            fileName = printFileName,
            controller = controller,
            externalPrintView = externalPrintView
        )
    } else {
        PlaceholderPreview(title = title, subtitle = subtitle, onBack = onBack)
    }
}

@Composable
private fun DocumentPreview(
    title: String,
    onBack: () -> Unit,
    payloadJson: String,
    fileName: String,
    controller: DocumentPrintController,
    externalPrintView: Boolean
) {
    val colors = rememberShellColors()
    // The document stays invisible until it is fully laid out, then fades in once:
    // no dark loading screen and no visible reflow.
    val documentAlpha by animateFloatAsState(
        targetValue = if (controller.isReady) 1f else 0f,
        animationSpec = tween(250),
        label = "document_fade"
    )

    val topInset = elvanStandardTopBarInset()
    val navBarsBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val sheet = controller.sheet

    var showOptions by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (controller.fileName.isBlank()) controller.fileName = fileName
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        val maxHeight = this.maxHeight
        Box(modifier = Modifier.fillMaxSize()) {
        // The web page only prepares the document (print / share / sheet image); it stays hidden.
        // Unless the caller already hosts it (view page), this prepares the PDF.
        if (!externalPrintView) {
            DocumentPrintView(
                payloadJson = payloadJson,
                fileName = fileName,
                controller = controller,
                topInset = 0.dp,
                modifier = Modifier
                    .size(1.dp)
                    .graphicsLayer { alpha = 0f }
            )
        }

        // Only the sheet is shown, on the theme background (black in dark mode, white in light)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .pointerInput(Unit) { },
            contentAlignment = Alignment.Center
        ) {
            if (sheet != null) {
                ElvanZoomableImage(
                    bitmap = sheet,
                    modifier = Modifier.fillMaxSize().graphicsLayer { alpha = documentAlpha },
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = topInset + 8.dp,
                        bottom = 96.dp + navBarsBottom
                    ),
                    onZoomChanged = { controller.zoomScale = it }
                )
            } else {
                CircularProgressIndicator(color = colors.textPrimary)
            }
        }

        // Soft fade above the system navigation bar, like the other screens
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(72.dp + navBarsBottom)
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Transparent,
                        0.3f to colors.background.copy(alpha = 0.16f),
                        0.65f to colors.background.copy(alpha = 0.55f),
                        1.0f to colors.background
                    )
                )
        )

        // Standard bar: back circle + title; the title hides once the sheet is zoomed in
        ElvanStandardTopBar(
            title = title,
            onBack = onBack,
            scrolled = controller.zoomScale > 1.05f,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Bottom area: status, options panel and the same pill as the view page
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .heightIn(max = maxHeight * 0.75f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            val notice = controller.notice
            LaunchedEffect(notice) {
                if (notice != null) {
                    delay(3500)
                    controller.notice = null
                }
            }
            if (controller.isBusy || notice != null) {
                val text = when {
                    controller.isBusy -> K.preparingPdf.tr()
                    notice?.kind == PrintNoticeKind.Saved -> "${K.pdfSavedTo.tr()}: ${notice.detail}"
                    else -> K.pdfFailed.tr()
                }
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(colors.textPrimary)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (controller.isBusy) {
                        CircularProgressIndicator(color = colors.background, strokeWidth = 2.dp, modifier = Modifier.size(14.dp))
                    }
                    Text(
                        text = text.preventBrokenLigatures(),
                        style = TextStyle(
                            fontFamily = LocalAppFontFamily.current,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.background
                        )
                    )
                }
            }
            if (showOptions && controller.isReady) {
                PrintOptionsPanel(controller, modifier = Modifier.weight(1f, fill = false))
            }
            if (controller.isReady) {
                ElvanFloatingActionBar(
                    actions = listOf(
                        ElvanFloatingAction(
                            label = K.optionsBtn.tr(),
                            icon = MaterialSymbols.Rounded.Tune,
                            onClick = { showOptions = !showOptions }
                        ),
                        ElvanFloatingAction(
                            label = K.printBtn.tr(),
                            icon = MaterialSymbols.Rounded.Print,
                            onClick = { showOptions = false; controller.print() }
                        ),
                        ElvanFloatingAction(
                            label = K.savePdfBtn.tr(),
                            icon = MaterialSymbols.Rounded.Download,
                            onClick = { showOptions = false; controller.save() }
                        ),
                        ElvanFloatingAction(
                            label = K.shareBtn.tr(),
                            icon = MaterialSymbols.Rounded.Share,
                            onClick = { showOptions = false; controller.share() }
                        )
                    )
                )
            }
        }
    }
    }
}

@Composable
private fun PlaceholderPreview(
    title: String,
    subtitle: String?,
    onBack: () -> Unit
) {
    val colors = rememberShellColors()
    val isDark = colors.isDark
    val ff = LocalAppFontFamily.current
    val scrollState = rememberLazyListState()

    ElvanSubShell(
        title = title,
        onBack = onBack,
        scrollState = scrollState,
        hasActions = false,
        showBottomFade = true
    ) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = Dimens.SubpageContentPaddingBottom + 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "top_spacer") {
                Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
            }

            // Info Notice Banner
            item(key = "notice_card") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f))
                        .border(
                            width = 0.5.dp,
                            color = colors.floatingBorder.copy(alpha = if (isDark) 0.15f else 0.4f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.accent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = MaterialSymbols.Rounded.Visibility,
                                contentDescription = null,
                                tint = colors.textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = (subtitle ?: title).preventBrokenLigatures(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = K.previewUnderConstruction.tr().preventBrokenLigatures(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = colors.textSecondary
                                )
                            )
                        }
                    }
                }
            }

            // Simulated Document Sheet (A4-Style Sheet)
            item(key = "sheet_preview") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(440.dp)
                        .cssShadow(
                            color = Color.Black,
                            alpha = if (isDark) 0.3f else 0.08f,
                            blurRadius = 20.dp,
                            offsetY = 6.dp
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isDark) Color(0xFF181818) else Color.White)
                        .border(
                            width = 0.5.dp,
                            color = colors.floatingBorder.copy(alpha = if (isDark) 0.2f else 0.5f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Header simulated lines
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .height(18.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(colors.textPrimary.copy(alpha = 0.8f))
                                )
                                Box(
                                    modifier = Modifier
                                        .width(80.dp)
                                        .height(14.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(colors.textSecondary.copy(alpha = 0.3f))
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .width(200.dp)
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(colors.textSecondary.copy(alpha = 0.25f))
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(colors.floatingBorder.copy(alpha = 0.3f))
                            )
                        }

                        // Middle simulated rows
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            repeat(4) { idx ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width((100 + (idx * 25)).dp)
                                            .height(12.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(colors.textSecondary.copy(alpha = 0.2f))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(50.dp)
                                            .height(12.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(colors.textSecondary.copy(alpha = 0.25f))
                                    )
                                }
                            }
                        }

                        // Watermark in Center
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = K.previewBtn.tr().preventBrokenLigatures(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary.copy(alpha = 0.12f),
                                    letterSpacing = 2.sp
                                )
                            )
                        }

                        // Footer simulated total
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(colors.floatingBorder.copy(alpha = 0.3f))
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(70.dp)
                                        .height(14.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(colors.textSecondary.copy(alpha = 0.35f))
                                )
                                Box(
                                    modifier = Modifier
                                        .width(100.dp)
                                        .height(20.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(colors.textPrimary.copy(alpha = 0.85f))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
