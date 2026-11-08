package com.elvan.udukkai.ui.components.shell

import androidx.compose.ui.draw.clip
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
 * Built with rock-solid Dialog + animated scrim. Closes by a tap on the background, Back or its buttons, not by swiping.
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
    /** Confirm runs [onConfirm] without closing the sheet (e.g. Pause/Resume on a progress sheet). */
    keepOpenOnConfirm: Boolean = false,
    /** False: only the Cancel button closes the sheet (tap outside and Back do nothing). */
    dismissible: Boolean = true,
    tertiaryText: String? = null,
    onTertiary: (() -> Unit)? = null,
    tertiaryColor: Color? = null,
    slideDistance: androidx.compose.ui.unit.Dp = 300.dp,
    /** One UI picker form: no title, a solid 26dp card 10dp from the screen edges, plain text buttons with a divider. Content sets its own padding. */
    pickerCard: Boolean = false,
    customContent: (@Composable () -> Unit)? = null
) {
    val ff = LocalAppFontFamily.current
    val haptics = com.elvan.udukkai.ui.components.rememberHaptics()
    val confirmed: () -> Unit = { haptics.confirm(); onConfirm() }
    val isDark = colors.isDark
    // Frosted where the phone blurs the screen behind; just slightly see-through where it cannot, so text stays clear of what is behind
    val glass = remember { com.elvan.udukkai.core.platform.dialogBlurAvailable() }
    // A picker is read closely, so its card is the sheet's colour without the see-through
    val cardBg = (if (isDark) Color(0xFF161616) else Color(0xFFFAFAFA)).copy(alpha = if (pickerCard) 1f else if (glass) 0.86f else 0.95f)
    val mainColor = confirmColor ?: colors.accent

    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val initialOffsetPx = with(density) { slideDistance.toPx() }
    val sheetOffsetY = remember { Animatable(initialOffsetPx) }
    var isClosing by remember { mutableStateOf(false) }

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

    val isDesktop = currentPlatform == PlatformType.DESKTOP

    Dialog(
        onDismissRequest = { if (dismissible) dismiss() },
        // Reaches under the status and navigation bars, so the dim covers the whole screen
        properties = com.elvan.udukkai.core.platform.edgeToEdgeDialogProperties(dismissOnBackPress = dismissible, dismissOnClickOutside = dismissible)
    ) {
        ConfigureDialogWindow(isDark = isDark, clearDim = true)

        val scrimAlpha by animateFloatAsState(
            targetValue = if (!isClosing) 0.45f else 0.0f,
            animationSpec = tween(durationMillis = if (!isClosing) 280 else 200),
            label = "scrimAlpha"
        )

        val navBarBottomPadding = getNavBarBottomPadding()

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = scrimAlpha))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (dismissible) dismiss()
                },
        ) {
            val isLandscape = maxWidth > maxHeight && !isDesktop
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = when {
                    isDesktop -> Alignment.Center
                    isLandscape -> Alignment.CenterEnd
                    else -> Alignment.BottomCenter
                }
            ) {
            val cardShape = RoundedCornerShape(if (pickerCard) 26.dp else if (isLandscape) 24.dp else 32.dp)
            val cardModifier = when {
                isDesktop -> {
                    modifier
                        .widthIn(max = if (tertiaryText != null) 360.dp else 460.dp)
                        .fillMaxWidth()
                        .padding(24.dp)
                        .offset { IntOffset(0, sheetOffsetY.value.roundToInt()) }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { /* Consume clicks */ }
                }
                isLandscape -> {
                    // Adaptive Side Sheet for Landscape: Right-side anchored, max-width ~420.dp
                    modifier
                        .widthIn(max = 420.dp)
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.End + WindowInsetsSides.Bottom))
                        .padding(end = 24.dp, top = 16.dp, bottom = 16.dp)
                        .offset { IntOffset(sheetOffsetY.value.roundToInt(), 0) }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { /* Consume clicks */ }
                }
                else -> {
                    modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(start = if (pickerCard) 10.dp else 16.dp, end = if (pickerCard) 10.dp else 16.dp, bottom = 16.dp)
                        .offset { IntOffset(0, sheetOffsetY.value.roundToInt()) }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { /* Consume clicks */ }
                }
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
                        .then(if (pickerCard) Modifier else Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 16.dp)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (pickerCard) {
                        customContent?.invoke()
                        Spacer(modifier = Modifier.height(4.dp))
                        PickerButtons(cancelText, confirmText, colors, onCancel = { dismiss() }, onConfirm = { dismiss { confirmed() } })
                        Spacer(modifier = Modifier.height(20.dp))
                    } else {
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
                                        color = colors.textPrimary.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                // Confirm button
                                if (isConfirmFilled) {
                                    Button(
                                        onClick = {
                                            if (keepOpenOnConfirm) confirmed() else dismiss { confirmed() }
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
                                            if (keepOpenOnConfirm) confirmed() else dismiss { confirmed() }
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
                                            color = mainColor,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            modifier = Modifier.fillMaxWidth()
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
                                        if (keepOpenOnConfirm) confirmed() else dismiss { confirmed() }
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
                                        if (keepOpenOnConfirm) confirmed() else dismiss { confirmed() }
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
    }
}


@Composable
fun ElvanDiscardChangesDialog(
    onKeepEditing: () -> Unit,
    onDiscard: () -> Unit
) {
    ElvanActionSheet(
        title = K.pendingSave.tr(),
        cancelText = K.continueBtn.tr(),
        confirmText = K.discardBtn.tr(),
        onDismissRequest = onKeepEditing,
        onConfirm = onDiscard
    )
}

/** One UI's dialog buttons: text only, two equal halves. */
@Composable
private fun PickerButtons(cancel: String, confirm: String, colors: ShellColors, onCancel: () -> Unit, onConfirm: () -> Unit) {
    @Composable
    fun RowScope.Button(text: String, onClick: () -> Unit) {
        Box(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(18.dp)).clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onClick), contentAlignment = Alignment.Center) {
            Text(text.preventBrokenLigatures(), maxLines = 1, style = TextStyle(fontFamily = LocalAppFontFamily.current, fontSize = 18.sp, fontWeight = FontWeight.Bold), color = colors.textPrimary)
        }
    }
    Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(36.dp)) {
        Button(cancel, onCancel)
        Button(confirm, onConfirm)
    }
}
