package com.elvan.udukkai.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.LocalAppLanguage
import com.elvan.udukkai.localization.trWithLang
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ThemeManager
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.theme.LocalShellColors

/**
 * Animated background with Material 3 floating and rotating geometric shapes,
 * copied 1:1 from Neram's `AuthBackground` / `AuthGradientBackground`.
 */
@Composable
fun SplashBackground(
    modifier: Modifier = Modifier,
    isDark: Boolean = ThemeManager.isDark(),
    content: @Composable BoxScope.() -> Unit
) {
    val backgroundColor = if (isDark) Color(0xFF0A0A0A) else Color(0xFFF5F6F8)
    val shapeColor = if (isDark) Color.White.copy(alpha = 0.03f) else Color(0xFFEAEAEA)

    val infiniteTransition = rememberInfiniteTransition(label = "splash_shapes_anim")

    // Rotation for top-right shape (60s linear)
    val rotation1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "splash_rotation1"
    )

    // Rotation for pill shape (80s linear reverse)
    val rotation2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 80000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "splash_rotation2"
    )

    // Floating motion (4s ease-in-out reverse)
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = CubicBezierEasing(0.37f, 0.0f, 0.63f, 1.0f)),
            repeatMode = RepeatMode.Reverse
        ),
        label = "splash_float"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .drawBehind {
                // 1. Large rounded square (top right)
                rotate(rotation1, pivot = Offset(size.width * 0.85f, size.height * 0.15f)) {
                    drawRoundRect(
                        color = shapeColor,
                        topLeft = Offset(size.width * 0.65f, size.height * 0.02f + floatOffset),
                        size = Size(size.width * 0.5f, size.width * 0.5f),
                        cornerRadius = CornerRadius(80f, 80f)
                    )
                }

                // 2. Circle (bottom left)
                drawCircle(
                    color = shapeColor,
                    radius = size.width * 0.35f,
                    center = Offset(
                        size.width * 0.1f,
                        size.height * 0.85f - floatOffset * 0.5f
                    )
                )

                // 3. Pill shape (middle left)
                rotate(rotation2 * 0.3f, pivot = Offset(size.width * 0.2f, size.height * 0.4f)) {
                    drawRoundRect(
                        color = shapeColor,
                        topLeft = Offset(-size.width * 0.1f, size.height * 0.35f),
                        size = Size(size.width * 0.4f, size.width * 0.15f),
                        cornerRadius = CornerRadius(100f, 100f)
                    )
                }

                // 4. Small diamond (bottom right)
                rotate(rotation1 * 0.5f, pivot = Offset(size.width * 0.9f, size.height * 0.7f)) {
                    drawRoundRect(
                        color = shapeColor,
                        topLeft = Offset(size.width * 0.8f, size.height * 0.6f + floatOffset * 0.3f),
                        size = Size(size.width * 0.2f, size.width * 0.2f),
                        cornerRadius = CornerRadius(30f, 30f)
                    )
                }
            },
        content = content
    )
}

/**
 * Centered Dummy Logo placeholder widget (180.dp) matching Neram's splash logo dimensions.
 */
@Composable
fun DummySplashLogo(
    modifier: Modifier = Modifier,
    isDark: Boolean = ThemeManager.isDark(),
    tint: Color = LocalShellColors.current.textPrimary
) {
    Box(
        modifier = modifier
            .size(180.dp)
            .clip(RoundedCornerShape(44.dp))
            .background(if (isDark) Color(0xFF161616) else Color.White)
            .border(
                width = 1.5.dp,
                color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f),
                shape = RoundedCornerShape(44.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = MaterialSymbols.Rounded.AutoAwesome,
            contentDescription = "Logo Placeholder",
            modifier = Modifier.size(76.dp),
            tint = tint.copy(alpha = 0.38f)
        )
    }
}

/**
 * SplashContent — Foreground logo and footer branding that can fade out independently.
 */
@Composable
fun SplashContent(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = ThemeManager.isDark(),
    language: String = LocalAppLanguage.current
) {
    val textPrimary = if (isDarkTheme) Color.White else Color(0xFF1A1A1A)
    val ff = LocalAppFontFamily.current

    Box(modifier = modifier.fillMaxSize()) {
        // Centered Content: Dummy Logo
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DummySplashLogo(
                modifier = Modifier.size(180.dp),
                isDark = isDarkTheme,
                tint = textPrimary
            )
        }

        // Footer: Language-aware Branding ("Elvan Navil" / "எல்வன் நவில்")
        val isTamil = language == "ta"
        val brandingText = K.elvanNavil.trWithLang(language)
        val tightSpacing = if (isTamil) 0.sp else (-0.2).sp

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 52.dp)
        ) {
            Text(
                text = brandingText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = ff,
                    letterSpacing = tightSpacing,
                    color = textPrimary.copy(alpha = 0.38f),
                    fontWeight = FontWeight.Medium,
                    fontSize = if (isTamil) 17.5.sp else 18.5.sp
                )
            )
        }
    }
}

/**
 * Animated Splash Screen copied 1:1 from Neram (`MainActivity.kt`),
 * with a dummy placeholder in place of `ic_splash_logo` and
 * language-aware "Elvan Navil" / "எல்வன் நவில்" footer branding.
 */
@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = ThemeManager.isDark(),
    language: String = LocalAppLanguage.current
) {
    SplashBackground(
        modifier = modifier,
        isDark = isDarkTheme
    ) {
        SplashContent(
            isDarkTheme = isDarkTheme,
            language = language
        )
    }
}

