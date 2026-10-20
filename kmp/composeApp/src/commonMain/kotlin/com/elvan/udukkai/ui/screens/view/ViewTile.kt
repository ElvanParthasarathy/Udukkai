package com.elvan.udukkai.ui.screens.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.extensions.cssShadow
import com.elvan.udukkai.core.platform.navigationBarsPaddingIfMobile
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.ElvanSnackbar
import com.elvan.udukkai.ui.components.shell.ElvanSubShell
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * Floating action bar for View (Paarvai) screens.
 * Displays Print, Copy, and Edit in a unified floating pill at the bottom of the screen,
 * exactly matching the design language and dimensions of ElvanSelectionBar.
 */
@Composable
fun ElvanViewActionBar(
    onPrint: (() -> Unit)?,
    onCopy: (() -> Unit)?,
    onEdit: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val colors = rememberShellColors()
    val isDark = colors.isDark
    val ff = LocalAppFontFamily.current

    val printLabel = K.printBtn.tr()
    val copyLabel = K.copyBtn.tr()
    val editLabel = K.editRecord.tr()

    DisposableEffect(Unit) {
        ElvanSnackbar.isBottomBarVisible = true
        onDispose { }
    }

    val actionCount = (if (onPrint != null) 1 else 0) + (if (onCopy != null) 1 else 0) + (if (onEdit != null) 1 else 0)
    if (actionCount == 0) return

    val barWidth = when (actionCount) {
        3 -> 246.dp
        2 -> 168.dp
        else -> 96.dp
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPaddingIfMobile()
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(barWidth)
                .height(56.dp)
                .cssShadow(
                    color = Color.Black,
                    alpha = if (isDark) 0.25f else 0.06f,
                    blurRadius = 16.dp,
                    offsetY = 4.dp
                )
                .background(
                    color = colors.floatingBg.copy(alpha = 0.92f),
                    shape = CircleShape
                )
                .border(
                    width = 0.5.dp,
                    color = colors.floatingBorder.copy(alpha = if (isDark) 0.15f else 0.6f),
                    shape = CircleShape
                )
                .padding(horizontal = 4.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (onPrint != null) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = onPrint
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = MaterialSymbols.Rounded.Print,
                                contentDescription = printLabel,
                                tint = colors.textPrimary,
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = printLabel,
                                maxLines = 1,
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textPrimary
                                )
                            )
                        }
                    }
                }

                if (onPrint != null && (onCopy != null || onEdit != null)) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(colors.floatingBorder.copy(alpha = if (isDark) 0.2f else 0.4f))
                    )
                }

                if (onCopy != null) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = onCopy
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = MaterialSymbols.Rounded.ContentCopy,
                                contentDescription = copyLabel,
                                tint = colors.textPrimary,
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = copyLabel,
                                maxLines = 1,
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textPrimary
                                )
                            )
                        }
                    }
                }

                if (onCopy != null && onEdit != null) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(colors.floatingBorder.copy(alpha = if (isDark) 0.2f else 0.4f))
                    )
                }

                if (onEdit != null) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = onEdit
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = MaterialSymbols.Rounded.Edit,
                                contentDescription = editLabel,
                                tint = colors.textPrimary,
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = editLabel,
                                maxLines = 1,
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textPrimary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Universal View Shell (Paarvai) for Compose Multiplatform.
 * Replicates Flutter's `elvan_paarvai_oadu.dart`.
 * Wraps read-only detail & print views with bottom floating action pill: "அச்சிடு" (Print), "நகலெடு" (Copy), and "மாற்றியமை" (Edit).
 */
@Composable
fun ViewTile(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    scrollState: LazyListState = rememberLazyListState(),
    onEdit: (() -> Unit)? = null,
    onPrint: (() -> Unit)? = null,
    onCopy: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val hasActions = onEdit != null || onPrint != null || onCopy != null

    ElvanSubShell(
        title = title,
        onBack = onBack,
        scrollState = scrollState,
        hasActions = false,
        actions = {},
        content = {
            Box(modifier = modifier.fillMaxSize()) {
                content()

                if (hasActions) {
                    ElvanViewActionBar(
                        onPrint = onPrint,
                        onCopy = onCopy,
                        onEdit = onEdit,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    )
}
