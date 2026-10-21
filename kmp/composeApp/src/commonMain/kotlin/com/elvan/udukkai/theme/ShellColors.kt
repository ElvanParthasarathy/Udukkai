package com.elvan.udukkai.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val HotPinkAccent = Color(0xFFFF2DAB)

@Immutable
data class ShellColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val accent: Color = HotPinkAccent,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textQuaternary: Color,
    val border: Color,
    val glassBorder: Color,
    val pillBackground: Color,
    val pillBorder: Color,
    // Centralized tokens (eliminate duplication)
    val ripple: Color,
    val floatingBg: Color,
    val floatingBorder: Color,
    val iconBg: Color,
    val divider: Color,
    val icon: Color,
    val iconInactive: Color,
    val modeAccent: Color = HotPinkAccent,
    // Mode-aware design system tokens
    val accentBright: Color = accent,
    val accentDark: Color = accent,
    val accentContainer: Color = accent.copy(alpha = 0.15f),
    val iconAccent: Color = HotPinkAccent,
    val error: Color = if (isDark) Color(0xFFFFB4AB) else Color(0xFFBA1A1A),
    val danger: Color = if (isDark) Color(0xFFFF5252) else Color(0xFFE53935),
    // Unified entity card icon colors (Hot Pink Minimal Accent)
    val invoiceColor: Color = HotPinkAccent,
    val receiptColor: Color = HotPinkAccent,
    val productColor: Color = HotPinkAccent,
    val customerColor: Color = HotPinkAccent,
    // Unified entity gradients for card icons (empty/neutral in monochrome)
    val invoiceGradient: List<Color> = emptyList(),
    val receiptGradient: List<Color> = emptyList(),
    val productGradient: List<Color> = emptyList(),
    val customerGradient: List<Color> = emptyList()
)

fun ShellColors.gradientFor(color: Color?): List<Color>? {
    // Pure monochrome aesthetic — all icons render crisp solid monochrome
    return null
}

val LocalShellColors = staticCompositionLocalOf<ShellColors> {
    error("No ShellColors provided")
}

@Composable
fun rememberShellColors(): ShellColors {
    val isDark = ThemeManager.isDark()
    return remember(isDark) {
        if (isDark) {
            ShellColors(
                isDark = true,
                background = Color.Black,
                surface = Color(0xFF111111),
                accent = HotPinkAccent,
                textPrimary = Color.White,
                textSecondary = Color.White.copy(alpha = 0.54f),
                textTertiary = Color.White.copy(alpha = 0.38f),
                textQuaternary = Color.White.copy(alpha = 0.24f),
                border = Color(0xFF555555),
                glassBorder = Color(0x1AFFFFFF),
                pillBackground = Color(0xFF1E1E1E),
                pillBorder = Color(0xFF333333),
                ripple = HotPinkAccent.copy(alpha = 0.16f),
                floatingBg = Color(0xFF1E1E1E),
                floatingBorder = Color(0xFF333333),
                iconBg = Color.White.copy(alpha = 0.08f),
                divider = Color.White.copy(alpha = 0.04f),
                icon = Color.White,
                iconInactive = Color.White.copy(alpha = 0.45f),
                modeAccent = HotPinkAccent,
                accentBright = HotPinkAccent,
                accentDark = HotPinkAccent,
                accentContainer = HotPinkAccent.copy(alpha = 0.15f),
                iconAccent = HotPinkAccent,
                error = Color(0xFFFFB4AB),
                invoiceColor = HotPinkAccent,
                receiptColor = HotPinkAccent,
                productColor = HotPinkAccent,
                customerColor = HotPinkAccent
            )
        } else {
            ShellColors(
                isDark = false,
                background = Color(0xFFF5F5F7),
                surface = Color.White,
                accent = HotPinkAccent,
                textPrimary = Color.Black,
                textSecondary = Color.Black.copy(alpha = 0.54f),
                textTertiary = Color.Black.copy(alpha = 0.38f),
                textQuaternary = Color.Black.copy(alpha = 0.26f),
                border = Color(0xFFAAAAAA),
                glassBorder = Color(0x14000000),
                pillBackground = Color.White,
                pillBorder = Color(0xFFE5E5E7),
                ripple = HotPinkAccent.copy(alpha = 0.12f),
                floatingBg = Color.White,
                floatingBorder = Color.White,
                iconBg = Color.Black.copy(alpha = 0.06f),
                divider = Color.Black.copy(alpha = 0.04f),
                icon = Color.Black,
                iconInactive = Color.Black.copy(alpha = 0.45f),
                modeAccent = HotPinkAccent,
                accentBright = HotPinkAccent,
                accentDark = HotPinkAccent,
                accentContainer = HotPinkAccent.copy(alpha = 0.12f),
                iconAccent = HotPinkAccent,
                error = Color(0xFFBA1A1A),
                invoiceColor = HotPinkAccent,
                receiptColor = HotPinkAccent,
                productColor = HotPinkAccent,
                customerColor = HotPinkAccent
            )
        }
    }
}
