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
    val modeAccent: Color = Color.Unspecified,
    // Mode-aware design system tokens
    val accentBright: Color = accent,
    val accentDark: Color = accent,
    val accentContainer: Color = accent.copy(alpha = 0.15f),
    val iconAccent: Color = accent,
    val error: Color = if (isDark) Color(0xFFFFB4AB) else Color(0xFFBA1A1A),
    val danger: Color = if (isDark) Color(0xFFFF5252) else Color(0xFFE53935),
    // Unified entity card icon colors (no two-color system)
    val invoiceColor: Color = Color(0xFF0072DE),
    val receiptColor: Color = Color(0xFF16A34A),
    val productColor: Color = Color(0xFFF59E0B),
    val customerColor: Color = Color(0xFF7C3AED),
    // Unified entity gradients for card icons
    val invoiceGradient: List<Color> = listOf(Color(0xFF38BDF8), Color(0xFF0072DE)),
    val receiptGradient: List<Color> = listOf(Color(0xFF34D399), Color(0xFF16A34A)),
    val productGradient: List<Color> = listOf(Color(0xFFFBBF24), Color(0xFFEA580C)),
    val customerGradient: List<Color> = listOf(Color(0xFFC084FC), Color(0xFF7C3AED))
)

fun ShellColors.gradientFor(color: Color?): List<Color>? {
    return when (color) {
        invoiceColor -> invoiceGradient
        receiptColor -> receiptGradient
        productColor -> productGradient
        customerColor -> customerGradient
        modeAccent -> invoiceGradient
        else -> null
    }
}

val LocalShellColors = staticCompositionLocalOf<ShellColors> {
    error("No ShellColors provided")
}

@Composable
fun rememberShellColors(mode: AppMode = LocalAppMode.current): ShellColors {
    val isDark = ThemeManager.isDark()
    return remember(isDark, mode) {
        val invColor = Color(0xFF0072DE)
        val recColor = Color(0xFF16A34A)
        val prodColor = Color(0xFFF59E0B)
        val custColor = Color(0xFF7C3AED)

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
                modeAccent = Color(0xFF0072DE), // Neram SESL Brand Blue
                accentBright = Color.White,
                accentDark = Color(0xFFE5E5EA),
                accentContainer = Color.White.copy(alpha = 0.12f),
                iconAccent = Color.White,
                error = Color(0xFFFFB4AB),
                invoiceColor = invColor,
                receiptColor = recColor,
                productColor = prodColor,
                customerColor = custColor
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
                modeAccent = Color(0xFF0072DE), // Neram SESL Brand Blue
                accentBright = Color(0xFF1D1D1F),
                accentDark = Color.Black,
                accentContainer = Color.Black.copy(alpha = 0.08f),
                iconAccent = Color(0xFF1D1D1F),
                error = Color(0xFFBA1A1A),
                invoiceColor = invColor,
                receiptColor = recColor,
                productColor = prodColor,
                customerColor = custColor
            )
        }
    }
}
