package com.elvan.udukkai.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.mode.LocalAppMode
import com.elvan.udukkai.core.mode.ModeManager
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.ShellDefaults
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.ui.navigation.AppSvgs
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * Pixel-perfect port of Flutter's GreetingPill.
 * Shows greeting pill with mode icon, "வணக்கம் ✨", and active profile/mode name.
 * Tapping the mode circle switches/toggles mode.
 */
@Composable
fun GreetingPill(
    colors: ShellColors,
    modifier: Modifier = Modifier,
    onModeClick: () -> Unit = { ModeManager.toggleMode() }
) {
    val mode = LocalAppMode.current
    val ff = LocalAppFontFamily.current
    val isDark = colors.isDark

    val subtitleText = mode.displayName()

    val pillShape = RoundedCornerShape(999.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ContentPadding, vertical = 2.dp)
            .height(84.dp)
            .clip(pillShape)
            .background(colors.surface)
            .padding(start = 11.dp, end = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val modeColor = if (mode == AppMode.KOOLI) Color(0xFF06B6D4) else Color(0xFFEC4899)
            val modeGradient = if (mode == AppMode.KOOLI) {
                listOf(Color(0xFF22D3EE), Color(0xFF0891B2))
            } else {
                listOf(Color(0xFFF472B6), Color(0xFFDB2777))
            }
            val modeBg = modeColor.copy(alpha = if (isDark) 0.16f else 0.10f)

            // Mode Circular Button (62dp concentric with 84dp pill, starting cleanly after the curve)
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(modeBg)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ShellDefaults.ripple(colors, bounded = true),
                        onClick = onModeClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (mode == AppMode.KOOLI) AppSvgs.coolieMode else AppSvgs.silkMode,
                    contentDescription = mode.displayName(),
                    tint = Color.White,
                    modifier = Modifier
                        .size(30.dp)
                        .graphicsLayer(alpha = 0.99f)
                        .drawWithCache {
                            val brush = Brush.linearGradient(
                                colors = modeGradient,
                                start = Offset.Zero,
                                end = Offset(this.size.width, this.size.height)
                            )
                            onDrawWithContent {
                                drawContent()
                                drawRect(brush = brush, blendMode = BlendMode.SrcIn)
                            }
                        }
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Greeting & Name
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = K.vanakkam.tr().preventBrokenLigatures(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.textPrimary,
                            letterSpacing = (-0.02).sp
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = MaterialSymbols.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = modeColor,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = subtitleText.preventBrokenLigatures(),
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color(0xFF9BA1A6) else Color(0xFF666666)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
