package com.elvan.udukkai.ui.screens.mode

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.extensions.cssShadow
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.platform.AppBackHandler
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellDefaults
import com.elvan.udukkai.theme.ThemeManager
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.navigation.AppSvgs
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.splash.SplashBackground
import kotlinx.coroutines.delay
import com.elvan.udukkai.theme.LocalShellColors

/**
 * ModeSelectorScreen — Full screen app mode switcher with continuous SplashBackground canvas.
 */
@Composable
fun ModeSelectorScreen(
    onModeSelected: (AppMode) -> Unit,
    onDismiss: (() -> Unit)? = null,
    canDismiss: Boolean = false
) {
    if (canDismiss && onDismiss != null) {
        AppBackHandler { onDismiss() }
    }

    val isDark = ThemeManager.isDark()

    SplashBackground(isDark = isDark) {
        ModeSelectorContent(
            onModeSelected = onModeSelected,
            onDismiss = onDismiss,
            canDismiss = canDismiss
        )
    }
}

/**
 * ModeSelectorContent — Foreground mode switcher UI without its own canvas.
 */
@Composable
fun ModeSelectorContent(
    onModeSelected: (AppMode) -> Unit,
    onDismiss: (() -> Unit)? = null,
    canDismiss: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isDark = ThemeManager.isDark()
    val colors = rememberShellColors()
    val ff = LocalAppFontFamily.current

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Dismiss / Close Button at Top Left (if allowed)
        if (canDismiss && onDismiss != null) {
            Surface(
                shape = CircleShape,
                color = colors.iconBg,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = 16.dp, top = 16.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ShellDefaults.ripple(colors, bounded = true),
                        onClick = onDismiss
                    )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.Close,
                        contentDescription = K.cancel.tr(),
                        tint = colors.textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Center Content Container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // AuthHeader (delayIndex: 1 -> 100ms)
            AuthAnimatedElement(delayMillis = 100) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = K.whichWorkspace.tr(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp,
                            textAlign = TextAlign.Center
                        ),
                        color = LocalShellColors.current.textPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = K.selectYourWorkspace.tr(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center
                        ),
                        color = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp))

            // Netflix Profile Cards Row (delayIndex: 2 -> 200ms)
            AuthAnimatedElement(delayMillis = 200) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Kooli Profile Card ("Udukkai Coolie" / "உடுக்கைக் கூலி")
                    NetflixProfileCard(
                        title = K.udukkaiCoolie.tr(),
                        icon = AppSvgs.coolieMode,
                        isDark = isDark,
                        onClick = { onModeSelected(AppMode.KOOLI) }
                    )

                    // Pattu Profile Card ("Udukkai Silk" / "உடுக்கைப் பட்டு")
                    NetflixProfileCard(
                        title = K.udukkaiSilk.tr(),
                        icon = AppSvgs.silkMode,
                        isDark = isDark,
                        onClick = { onModeSelected(AppMode.PATTU) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

/**
 * Staggered entrance animation matching Flutter's `AuthAnimatedElement` 1:1.
 * Moves upward from +20dp to 0dp with Curves.easeOutBack spring overshoot and fades in over 800ms.
 */
@Composable
private fun AuthAnimatedElement(
    delayMillis: Int,
    durationMillis: Int = 800,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isStarted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        isStarted = true
    }

    val easeOutBack = remember { CubicBezierEasing(0.175f, 0.885f, 0.32f, 1.275f) }

    val offsetY by animateFloatAsState(
        targetValue = if (isStarted) 0f else 20f,
        animationSpec = tween(
            durationMillis = durationMillis,
            easing = easeOutBack
        ),
        label = "authElementOffsetY"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isStarted) 1f else 0f,
        animationSpec = tween(
            durationMillis = durationMillis,
            easing = FastOutSlowInEasing
        ),
        label = "authElementAlpha"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                translationY = offsetY * density
                this.alpha = alpha
            }
    ) {
        content()
    }
}

/**
 * NetflixProfileCard — 120dp circular avatar card with circular ripple and no bounce.
 */
@Composable
private fun NetflixProfileCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val ff = LocalAppFontFamily.current
    val colors = rememberShellColors()

    val boxColor = if (isDark) Color(0xFF222222) else Color.White
    val iconColor = if (isDark) Color.White else Color(0xFF111111)
    val textColor = if (isDark) Color(0xFF9E9E9E) else Color(0xFF757575)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 120dp Circle Avatar Box with shadow and circular ripple
        Box(
            modifier = Modifier
                .size(120.dp)
                .cssShadow(
                    color = Color.Black,
                    alpha = if (isDark) 0.20f else 0.08f,
                    borderRadius = 60.dp,
                    blurRadius = 16.dp,
                    offsetY = 8.dp
                )
                .clip(CircleShape)
                .background(boxColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ShellDefaults.ripple(colors, bounded = true),
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = TextStyle(
                fontFamily = ff,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = textColor
        )
    }
}
