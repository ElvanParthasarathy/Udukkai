package com.elvan.udukkai.ui.components.shell

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.core.platform.ConfigureDialogWindow
import com.elvan.udukkai.theme.rememberShellColors

/**
 * ElvanActionSheet — Flutter & Neram-matching floating action sheet popup (modal popup).
 * Replicates `showElvanActionSheet` from Flutter's `elvan_cheyal_maeladukku.dart` and Neram's signout popup.
 *
 * Features:
 * - Floating transparent modal sheet with rounded pill surface (32.dp).
 * - Translucent background (0xFF161616 in dark / 0xFFFAFAFA in light).
 * - 14sp w500 centered title with 0.5 alpha onSurface.
 * - Side-by-side action buttons with 16sp typography.
 * - Respects safe area, navigationBarsPadding, and imePadding.
 */
@OptIn(ExperimentalMaterial3Api::class)
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

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(32.dp),
        containerColor = Color.Transparent,
        scrimColor = Color.Black.copy(alpha = 0.45f),
        dragHandle = null
    ) {
        ConfigureDialogWindow(isDark = isDark)
        Box(
            modifier = modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = cardBg,
                shadowElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Centered Title
                    Text(
                        text = title,
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
                                onClick = onDismissRequest,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = cancelText,
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
                                    onClick = onDismissRequest,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = cancelText,
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
                                            onDismissRequest()
                                            onConfirm()
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp),
                                        shape = RoundedCornerShape(50),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = mainColor,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text(
                                            text = confirmText,
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
                                            onDismissRequest()
                                            onConfirm()
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = confirmText,
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
                        // Vertical column for 3 actions
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isConfirmFilled) {
                                Button(
                                    onClick = {
                                        onDismissRequest()
                                        onConfirm()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = mainColor,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text(
                                        text = confirmText,
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
                                        onDismissRequest()
                                        onConfirm()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = confirmText,
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
                                    onDismissRequest()
                                    onTertiary?.invoke()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = tertiaryText,
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = tertiaryColor ?: MaterialTheme.colorScheme.error
                                )
                            }

                            TextButton(
                                onClick = onDismissRequest,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = cancelText,
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
