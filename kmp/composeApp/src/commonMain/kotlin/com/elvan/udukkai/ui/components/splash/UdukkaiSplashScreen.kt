package com.elvan.udukkai.ui.components.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.ThemeManager
import migrate.composeapp.generated.resources.Res
import migrate.composeapp.generated.resources.udukkai_logo
import org.jetbrains.compose.resources.painterResource

private val SplashEaseInOut = CubicBezierEasing(0.42f, 0.0f, 0.58f, 1.0f)

/**
 * Fullscreen Expressive Splash Screen matching Neram's splash architecture,
 * featuring Udukkai's floating animated shapes, centered app logo, and
 * dynamic multilingual "Elvan Navil" branding footer.
 */
@Composable
fun UdukkaiSplashScreen(
    colors: ShellColors,
    modifier: Modifier = Modifier,
    onUsePin: (() -> Unit)? = null
) {
    val ff = LocalAppFontFamily.current
    val isDark = ThemeManager.isDark()
    val bgColor = colors.background
    val shapeColor = if (isDark) Color.White.copy(alpha = 0.035f) else Color(0xFFEAEAEA)

    val infiniteTransition = rememberInfiniteTransition(label = "splash_shapes_anim")

    // Shape 1 Rotate: 60s linear infinite
    val rotate1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "splash_rotate1"
    )

    // Shape 3 Rotate: 40s linear infinite reverse
    val rotate2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 40000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "splash_rotate2"
    )

    // Shape 2 Float: 4s ease-in-out alternate (0 -> 30px)
    val float1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = SplashEaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "splash_float1"
    )

    // Shape 4 Float: 5s ease-in-out alternate (30px -> 0px)
    val float2 by infiniteTransition.animateFloat(
        initialValue = 30f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = SplashEaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "splash_float2"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        val screenW = maxWidth
        val screenH = maxHeight

        // Shape 1: Top Right, Large Rounded Square (50% screen width, radius 80dp, rotate 60s)
        val s1Size = screenW * 0.5f
        Box(
            modifier = Modifier
                .offset(
                    x = screenW * 0.6f,
                    y = screenH * 0.02f
                )
                .size(s1Size)
                .graphicsLayer {
                    rotationZ = rotate1
                    transformOrigin = TransformOrigin(0.925f, 0.575f)
                }
                .background(shapeColor, RoundedCornerShape(80.dp))
        )

        // Shape 2: Bottom Left, Circle (70% screen width, float 4s)
        val s2Size = screenW * 0.7f
        Box(
            modifier = Modifier
                .offset(
                    x = -screenW * 0.1f,
                    y = screenH * 0.95f - s2Size + float1.dp
                )
                .size(s2Size)
                .background(shapeColor, CircleShape)
        )

        // Shape 3: Top Left, Small Rounded Square (20% screen width, radius 30dp, reverse rotate 40s)
        val s3Size = screenW * 0.2f
        Box(
            modifier = Modifier
                .offset(
                    x = screenW * 0.05f,
                    y = screenH * 0.15f
                )
                .size(s3Size)
                .rotate(rotate2)
                .background(shapeColor, RoundedCornerShape(30.dp))
        )

        // Shape 4: Center Right, Small Circle (15% screen width, float 5s)
        val s4Size = screenW * 0.15f
        Box(
            modifier = Modifier
                .offset(
                    x = screenW * 0.75f,
                    y = screenH * 0.60f + float2.dp
                )
                .size(s4Size)
                .background(shapeColor, CircleShape)
        )

        // Centered Content: App Logo & Optional "Use PIN" Button
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(Res.drawable.udukkai_logo),
                contentDescription = null,
                modifier = Modifier.size(160.dp),
                colorFilter = ColorFilter.tint(colors.textPrimary)
            )

            if (onUsePin != null) {
                Spacer(Modifier.height(28.dp))
                Text(
                    text = K.usePin.tr(),
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = colors.textPrimary.copy(alpha = 0.75f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.iconBg)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onUsePin
                        )
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }
        }

        // Footer: Multilingual "Elvan Navil" branding signature
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = K.elvanNavil.tr(),
                style = TextStyle(
                    fontFamily = ff,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp
                ),
                color = colors.textPrimary.copy(alpha = 0.5f)
            )
        }
    }
}
