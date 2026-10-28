package com.elvan.udukkai.ui.components.shell

import com.elvan.udukkai.ui.components.ExpressiveLoadingIndicator
import com.elvan.udukkai.ui.components.ExpressiveDotsLoader
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.elvan.udukkai.core.extensions.cssShadow
import com.elvan.udukkai.core.platform.navigationBarsPaddingIfMobile
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors

data class ElvanFloatingAction(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
    val isLoading: Boolean = false,
    val isDestructive: Boolean = false,
    val customColor: Color? = null
)

/**
 * Universal Floating Action Bar for Compose Multiplatform.
 * Unified One UI floating pill component shared across View screens, Editor screens, and Recycle Bin.
 * Preserves the exact compact dimensions, circular ripples, and pixel-perfect design language.
 */
@Composable
fun ElvanFloatingActionBar(
    actions: List<ElvanFloatingAction>,
    modifier: Modifier = Modifier,
    colors: ShellColors = rememberShellColors()
) {
    if (actions.isEmpty()) return

    val isDark = colors.isDark
    val ff = LocalAppFontFamily.current

    SideEffect {
        ElvanSnackbar.isBottomBarVisible = true
    }

    val barWidth: Dp = when (actions.size) {
        1 -> 84.dp
        2 -> 168.dp
        3 -> 246.dp
        4 -> 308.dp
        else -> (actions.size * 77).dp
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .zIndex(100f)
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
                actions.forEachIndexed { index, action ->
                    if (index > 0) {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(24.dp)
                                .background(colors.floatingBorder.copy(alpha = if (isDark) 0.2f else 0.4f))
                        )
                    }

                    val itemColor = action.customColor
                        ?: if (action.isDestructive) colors.danger
                        else if (action.enabled) colors.textPrimary
                        else colors.textSecondary.copy(alpha = 0.35f)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                enabled = action.enabled && !action.isLoading,
                                onClick = action.onClick
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (action.isLoading) {
                                ExpressiveLoadingIndicator(modifier = Modifier.size(20.dp), color = itemColor, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.height(2.dp))
                            } else {
                                Icon(
                                    imageVector = action.icon,
                                    contentDescription = action.label,
                                    tint = itemColor,
                                    modifier = Modifier.size(19.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                            Text(
                                text = action.label.preventBrokenLigatures(),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = itemColor,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
