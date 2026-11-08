package com.elvan.udukkai.ui.components.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import com.elvan.udukkai.theme.ShellColors
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/** The page behind the floating bars; a shell provides it so the bars can blur what scrolls under them. */
val LocalHazeState = compositionLocalOf<HazeState?> { null }

/**
 * Frosted glass like iOS: the page behind is blurred and lightly tinted with the bar colour.
 * Where there is nothing to blur (no shell around, or Android below 12) the plain translucent colour is used.
 */
@Composable
fun Modifier.frostedGlass(shape: Shape, colors: ShellColors, fallbackAlpha: Float = 0.92f): Modifier {
    val haze = LocalHazeState.current ?: return this.background(colors.floatingBg.copy(alpha = fallbackAlpha), shape)
    val tint = colors.floatingBg.copy(alpha = if (colors.isDark) 0.62f else 0.68f)
    return this.clip(shape).hazeEffect(
        state = haze,
        style = HazeStyle(
            backgroundColor = colors.background,
            tint = HazeTint(tint),
            blurRadius = 24.dp,
            noiseFactor = 0f,
            fallbackTint = HazeTint(colors.floatingBg.copy(alpha = fallbackAlpha))
        )
    )
}
