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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.unit.dp

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

private const val MAX_FONT_SCALE = 1.0f

/** Portrait sizes times this on a phone turned sideways. */
private const val LANDSCAPE_PHONE_SCALE = 0.86f

@Composable
fun UdukkaiTheme(
    darkTheme: Boolean = ThemeManager.isDark(),
    font: AppFont = FontManager.currentFont,
    content: @Composable () -> Unit
) {
    val rippleColor = if (darkTheme) Color.White else Color.Black

    val currentFontFamily = ElvanSansFontFamily
    val typography = remember(currentFontFamily) {
        createTypography(currentFontFamily)
    }

    // The text size stays as designed: the phone's larger text setting is not applied (the layouts are made for Tamil at this size)
    val currentDensity = LocalDensity.current
    val density = if (currentDensity.fontScale > MAX_FONT_SCALE) {
        Density(currentDensity.density, fontScale = MAX_FONT_SCALE)
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

    // A phone turned sideways is only ~360dp tall: everything is drawn a little smaller (not edge-to-edge portrait sizes)
    androidx.compose.foundation.layout.BoxWithConstraints(androidx.compose.ui.Modifier.fillMaxSize()) {
    val sideways = maxWidth > maxHeight && maxHeight < 480.dp
    val sizedDensity = if (sideways) Density(density.density * LANDSCAPE_PHONE_SCALE, density.fontScale) else density
    CompositionLocalProvider(
        LocalDensity provides sizedDensity,
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
}
