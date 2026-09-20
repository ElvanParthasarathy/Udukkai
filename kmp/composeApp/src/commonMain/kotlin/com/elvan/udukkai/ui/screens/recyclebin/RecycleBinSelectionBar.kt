package com.elvan.udukkai.ui.screens.recyclebin

import androidx.compose.animation.core.CubicBezierEasing
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.extensions.cssShadow
import com.elvan.udukkai.core.platform.navigationBarsPaddingIfMobile
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * RecycleBinSelectionBar — Floating selection action bar for Recycle Bin.
 * Contains Restore ("மீட்டெடு") and Permanent Delete ("நீக்கவும்") buttons.
 */
@Composable
fun RecycleBinSelectionBar(
    visible: Boolean,
    selectedCount: Int,
    onRestore: () -> Unit,
    onPermanentDelete: () -> Unit,
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
            val restoreLabel = K.restoreBtn.tr()
            val deleteLabel = K.deleteBtn.tr()
            val isEnabled = selectedCount > 0

            val restoreColor = if (isEnabled) colors.textPrimary else colors.textSecondary.copy(alpha = 0.35f)
            val deleteColor = if (isEnabled) colors.textPrimary else colors.textSecondary.copy(alpha = 0.35f)

            Box(
                modifier = Modifier
                    .width(168.dp)
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
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Restore Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                enabled = isEnabled,
                                onClick = onRestore
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = MaterialSymbols.Rounded.Restore,
                                contentDescription = restoreLabel,
                                tint = restoreColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = restoreLabel,
                                maxLines = 1,
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = restoreColor
                                )
                            )
                        }
                    }

                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .width(0.5.dp)
                            .height(28.dp)
                            .background(colors.floatingBorder.copy(alpha = if (isDark) 0.15f else 0.35f))
                    )

                    // Permanent Delete Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                enabled = isEnabled,
                                onClick = onPermanentDelete
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = MaterialSymbols.Rounded.DeleteForever,
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
