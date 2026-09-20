package com.elvan.udukkai.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.mode.LocalAppMode

@Immutable
data class ShellColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val accent: Color,
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
    val modeAccent: Color = if (isDark) Color.White else Color.Black,
    // Mode-aware design system tokens
    val accentBright: Color = accent,
    val accentDark: Color = accent,
    val accentContainer: Color = accent.copy(alpha = 0.15f),
    val iconAccent: Color = accent,
    val error: Color = if (isDark) Color(0xFFFFB4AB) else Color(0xFFBA1A1A),
    val danger: Color = if (isDark) Color(0xFFFF5252) else Color(0xFFE53935),
    // Unified entity card icon colors (Pure Monochrome)
    val invoiceColor: Color = if (isDark) Color.White else Color.Black,
    val receiptColor: Color = if (isDark) Color.White else Color.Black,
    val productColor: Color = if (isDark) Color.White else Color.Black,
    val customerColor: Color = if (isDark) Color.White else Color.Black,
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
fun rememberShellColors(mode: AppMode = LocalAppMode.current): ShellColors {
    val isDark = ThemeManager.isDark()
    return remember(isDark, mode) {
        val monoColor = if (isDark) Color.White else Color.Black

        if (isDark) {
            ShellColors(
                isDark = true,
                background = Color.Black,
                surface = Color(0xFF111111),
                accent = Color.White,
                textPrimary = Color.White,
                textSecondary = Color.White.copy(alpha = 0.54f),
                textTertiary = Color.White.copy(alpha = 0.38f),
                textQuaternary = Color.White.copy(alpha = 0.24f),
                border = Color(0xFF555555),
                glassBorder = Color(0x1AFFFFFF),
                pillBackground = Color(0xFF1E1E1E),
                pillBorder = Color(0xFF333333),
                ripple = Color.White.copy(alpha = 0.16f),
                floatingBg = Color(0xFF1E1E1E),
                floatingBorder = Color(0xFF333333),
                iconBg = Color.White.copy(alpha = 0.08f),
                divider = Color.White.copy(alpha = 0.04f),
                icon = Color.White,
                iconInactive = Color.White.copy(alpha = 0.45f),
                modeAccent = Color.White,
                accentBright = Color.White,
                accentDark = Color(0xFFE5E5EA),
                accentContainer = Color.White.copy(alpha = 0.12f),
                iconAccent = Color.White,
                error = Color(0xFFFFB4AB),
                invoiceColor = monoColor,
                receiptColor = monoColor,
                productColor = monoColor,
                customerColor = monoColor
            )
        } else {
            ShellColors(
                isDark = false,
                background = Color(0xFFF5F5F7),
                surface = Color.White,
                accent = Color(0xFF1D1D1F),
                textPrimary = Color.Black,
                textSecondary = Color.Black.copy(alpha = 0.54f),
                textTertiary = Color.Black.copy(alpha = 0.38f),
                textQuaternary = Color.Black.copy(alpha = 0.26f),
                border = Color(0xFFAAAAAA),
                glassBorder = Color(0x14000000),
                pillBackground = Color.White,
                pillBorder = Color(0xFFE5E5E7),
                ripple = Color.Black.copy(alpha = 0.08f),
                floatingBg = Color.White,
                floatingBorder = Color.White,
                iconBg = Color.Black.copy(alpha = 0.06f),
                divider = Color.Black.copy(alpha = 0.04f),
                icon = Color.Black,
                iconInactive = Color.Black.copy(alpha = 0.45f),
                modeAccent = Color.Black,
                accentBright = Color(0xFF1D1D1F),
                accentDark = Color.Black,
                accentContainer = Color.Black.copy(alpha = 0.08f),
                iconAccent = Color(0xFF1D1D1F),
                error = Color(0xFFBA1A1A),
                invoiceColor = monoColor,
                receiptColor = monoColor,
                productColor = monoColor,
                customerColor = monoColor
            )
        }
    }
}
