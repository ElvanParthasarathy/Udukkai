package com.elvan.udukkai.ui.components.shell

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.elvan.udukkai.core.platform.ConfigureDialogWindow
import com.elvan.udukkai.core.platform.PlatformType
import com.elvan.udukkai.core.platform.currentPlatform
import com.elvan.udukkai.core.platform.getNavBarBottomPadding
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * ElvanActionSheet — Flutter & Neram-matching floating action sheet modal popup.
 * Built with rock-solid Dialog + animated scrim + downward drag-to-dismiss.
 *
 * Replaces Material 3's ModalBottomSheet to eliminate black screen/blocking glitches on swipe.
 */
@Composable
fun ElvanActionSheet(
    title: String,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    cancelText: String = K.cancel.tr(),
    confirmText: String = K.confirm.tr(),
    colors: ShellColors = rememberShellColors(),
    confirmColor: Color? = null,
    isConfirmFilled: Boolean = false,
    tertiaryText: String? = null,
    onTertiary: (() -> Unit)? = null,
    tertiaryColor: Color? = null,
    customContent: (@Composable () -> Unit)? = null
) {
    val ff = LocalAppFontFamily.current
    val isDark = colors.isDark
    val cardBg = if (isDark) Color(0xFF161616) else Color(0xFFFAFAFA)
    val mainColor = confirmColor ?: colors.accent

    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val initialOffsetPx = with(density) { 300.dp.toPx() }
    val sheetOffsetY = remember { Animatable(initialOffsetPx) }
    var isClosing by remember { mutableStateOf(false) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        sheetOffsetY.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = 280,
                easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
            )
        )
    }

    fun dismiss(onComplete: (() -> Unit)? = null) {
        if (isClosing) return
        isClosing = true
        coroutineScope.launch {
            val currentTotal = sheetOffsetY.value + dragOffsetY
            sheetOffsetY.snapTo(currentTotal)
            dragOffsetY = 0f
            sheetOffsetY.animateTo(
                targetValue = initialOffsetPx,
                animationSpec = tween(
                    durationMillis = 200,
                    easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
                )
            )
            onDismissRequest()
            onComplete?.invoke()
        }
    }

    val draggableState = rememberDraggableState { delta ->
        // Clamped at 0f: Only allow dragging DOWNWARDS (delta > 0).
        val target = dragOffsetY + delta
        dragOffsetY = target.coerceAtLeast(0f)
    }

    fun snapBackDrag() {
        if (dragOffsetY > 0f) {
            coroutineScope.launch {
                Animatable(dragOffsetY).animateTo(
                    targetValue = 0f,
                    animationSpec = tween(160, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f))
                ) {
                    dragOffsetY = value
                }
            }
        }
    }

    val isDesktop = currentPlatform == PlatformType.DESKTOP

    Dialog(
        onDismissRequest = { dismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        ConfigureDialogWindow(isDark = isDark, clearDim = true)

        val scrimAlpha by animateFloatAsState(
            targetValue = if (!isClosing) 0.45f else 0.0f,
            animationSpec = tween(durationMillis = if (!isClosing) 280 else 200),
            label = "scrimAlpha"
        )

        val navBarBottomPadding = getNavBarBottomPadding()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = scrimAlpha))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    dismiss()
                },
            contentAlignment = if (isDesktop) Alignment.Center else Alignment.BottomCenter
        ) {
            val cardShape = RoundedCornerShape(32.dp)
            val cardModifier = if (isDesktop) {
                modifier
                    .widthIn(max = if (tertiaryText != null) 360.dp else 460.dp)
                    .fillMaxWidth()
                    .padding(24.dp)
                    .offset { IntOffset(0, (sheetOffsetY.value + dragOffsetY).roundToInt()) }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* Consume clicks */ }
            } else {
                modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .offset { IntOffset(0, (sheetOffsetY.value + dragOffsetY).roundToInt()) }
                    .draggable(
                        state = draggableState,
                        orientation = Orientation.Vertical,
                        onDragStopped = { velocity ->
                            if (dragOffsetY > 80f || velocity > 600f) {
                                dismiss()
                            } else {
                                snapBackDrag()
                            }
                        }
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* Consume clicks */ }
            }

            Surface(
                shape = cardShape,
                color = cardBg,
                shadowElevation = 12.dp,
                modifier = cardModifier
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Centered Title
                    Text(
                        text = title.preventBrokenLigatures(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = colors.textPrimary.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (customContent != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        customContent()
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (tertiaryText == null) {
                        if (confirmText.isBlank()) {
                            // Single Cancel button
                            TextButton(
                                onClick = { dismiss() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = cancelText.preventBrokenLigatures(),
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = colors.textPrimary.copy(alpha = 0.6f)
                                )
                            }
                        } else {
                            // Side-by-side action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Cancel button
                                TextButton(
                                    onClick = { dismiss() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = cancelText.preventBrokenLigatures(),
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = colors.textPrimary.copy(alpha = 0.6f)
                                    )
                                }

                                // Confirm button
                                if (isConfirmFilled) {
                                    Button(
                                        onClick = {
                                            dismiss { onConfirm() }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp),
                                        shape = RoundedCornerShape(50),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = mainColor,
                                            contentColor = if (mainColor == colors.accent) (if (isDark) Color.Black else Color.White) else Color.White
                                        )
                                    ) {
                                        Text(
                                            text = confirmText.preventBrokenLigatures(),
                                            style = TextStyle(
                                                fontFamily = ff,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                } else {
                                    TextButton(
                                        onClick = {
                                            dismiss { onConfirm() }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = confirmText.preventBrokenLigatures(),
                                            style = TextStyle(
                                                fontFamily = ff,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = mainColor
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Vertical column for 3 actions (Confirm / Tertiary / Cancel)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isConfirmFilled) {
                                Button(
                                    onClick = {
                                        dismiss { onConfirm() }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = mainColor,
                                        contentColor = if (mainColor == colors.accent) (if (isDark) Color.Black else Color.White) else Color.White
                                    )
                                ) {
                                    Text(
                                        text = confirmText.preventBrokenLigatures(),
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            } else {
                                TextButton(
                                    onClick = {
                                        dismiss { onConfirm() }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = confirmText.preventBrokenLigatures(),
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = mainColor
                                    )
                                }
                            }

                            TextButton(
                                onClick = {
                                    dismiss { onTertiary?.invoke() }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = tertiaryText.preventBrokenLigatures(),
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = tertiaryColor ?: MaterialTheme.colorScheme.error
                                )
                            }

                            TextButton(
                                onClick = { dismiss() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = cancelText.preventBrokenLigatures(),
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = colors.textPrimary.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
