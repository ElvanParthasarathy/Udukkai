package com.elvan.udukkai.theme

import androidx.compose.foundation.LocalIndication
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = Color(0xFFF0F0F0),
    onSurfaceVariant = Color(0xFF49454F),
    error = ErrorLight
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1C1C1C),
    onSurfaceVariant = Color(0xFFE0E0E0),
    surfaceContainer = Color.Black,
    surfaceContainerLow = Color.Black,
    surfaceContainerLowest = Color.Black,
    surfaceContainerHigh = Color(0xFF1C1C1C),
    surfaceContainerHighest = Color(0xFF2C2C2C),
    error = ErrorDark
)

@Composable
fun UdukkaiTheme(
    darkTheme: Boolean = ThemeManager.isDark(),
    font: AppFont = FontManager.currentFont,
    content: @Composable () -> Unit
) {
    val rippleColor = if (darkTheme) Color.White else Color.Black

    val currentFontFamily = when (font) {
        AppFont.NAVIL_SANS -> NavilSansFontFamily
        AppFont.ELVAN_SANS -> ElvanSansFontFamily
    }
    val typography = remember(currentFontFamily) {
        createTypography(currentFontFamily)
    }

    // Fix for devices with large default font/display scaling
    // We force the font scale to be at most 1.0f to maintain the intended design
    val currentDensity = LocalDensity.current
    val density = if (currentDensity.fontScale > 1.0f) {
        Density(currentDensity.density, fontScale = 1.0f)
    } else {
        currentDensity
    }

    val shellColors = rememberShellColors()

    val colorScheme = remember(darkTheme, shellColors) {
        if (darkTheme) {
            DarkColorScheme.copy(
                primary = shellColors.accent,
                primaryContainer = shellColors.accentContainer
            )
        } else {
            LightColorScheme.copy(
                primary = shellColors.accent,
                primaryContainer = shellColors.accentContainer
            )
        }
    }

    CompositionLocalProvider(
        LocalDensity provides density,
        LocalAppFontFamily provides currentFontFamily,
        LocalShellColors provides shellColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = UdukkaiShapes
        ) {
            @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
            CompositionLocalProvider(
                LocalIndication provides ripple(color = rippleColor, bounded = true),
                LocalRippleConfiguration provides RippleConfiguration(color = rippleColor)
            ) {
                content()
            }
        }
    }
}
