package com.elvan.udukkai.ui.components.shell

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.extensions.cssShadow
import com.elvan.udukkai.core.platform.navigationBarsPaddingIfMobile
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.LocalAppLanguage
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * ElvanSelectionBar — Floating multi-selection action bar.
 * Matches BottomNavBar position and 56.dp height exactly.
 * In One UI mode, hosts Delete ("நீக்கவும்") and optional Copy ("நகலெடு") action pills.
 */
@Composable
fun ElvanSelectionBar(
    visible: Boolean,
    selectedCount: Int,
    onDelete: () -> Unit,
    onCopy: (() -> Unit)? = null,
    colors: ShellColors = rememberShellColors(),
    modifier: Modifier = Modifier
) {
    val isDark = colors.isDark
    val ff = LocalAppFontFamily.current

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 200, easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)),
        label = "thervuPattaiAlpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (visible) 1.0f else 0.92f,
        animationSpec = tween(durationMillis = 200, easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)),
        label = "thervuPattaiScale"
    )

    if (visible || alpha > 0f) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .navigationBarsPaddingIfMobile()
                .padding(bottom = 16.dp)
                .graphicsLayer {
                    this.alpha = alpha
                    this.scaleX = scale
                    this.scaleY = scale
                },
            contentAlignment = Alignment.Center
        ) {
            val deleteLabel = K.deleteBtn.tr()
            val copyLabel = K.copyBtn.tr()

            val isDeleteEnabled = selectedCount > 0
            val deleteColor = if (isDeleteEnabled) colors.textPrimary else colors.textSecondary.copy(alpha = 0.35f)

            val isCopyEnabled = selectedCount == 1
            val copyColor = if (isCopyEnabled) colors.textPrimary else colors.textSecondary.copy(alpha = 0.35f)

            val barWidth = if (onCopy != null) 168.dp else 84.dp

            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(56.dp)
                    .cssShadow(
                        color = Color.Black,
                        alpha = 0.05f,
                        blurRadius = 16.dp,
                        offsetY = 4.dp
                    )
                    .background(
                        color = colors.floatingBg.copy(alpha = 0.88f),
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
                    if (onCopy != null) {
                        // Copy Action (active when single item selected)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true),
                                    enabled = isCopyEnabled,
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
                                    tint = copyColor,
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
                                        color = copyColor
                                    )
                                )
                            }
                        }

                        // Vertical divider between Copy and Delete
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(24.dp)
                                .background(colors.floatingBorder.copy(alpha = if (isDark) 0.2f else 0.4f))
                        )
                    }

                    // Delete Action
                    Box(
                        modifier = Modifier
                            .then(if (onCopy != null) Modifier.weight(1f) else Modifier.fillMaxSize())
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                enabled = isDeleteEnabled,
                                onClick = onDelete
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = MaterialSymbols.Rounded.Delete,
                                contentDescription = deleteLabel,
                                tint = deleteColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = deleteLabel,
                                maxLines = 1,
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = deleteColor
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
