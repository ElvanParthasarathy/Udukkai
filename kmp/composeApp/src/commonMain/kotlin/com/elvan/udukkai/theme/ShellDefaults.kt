package com.elvan.udukkai.theme

import androidx.compose.foundation.Indication
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.core.extensions.cssShadow
import androidx.compose.foundation.background
import androidx.compose.foundation.border

/**
 * Centralized visual defaults for Elvan Shell components.
 * Eliminates duplicate color/ripple/shadow logic across pill, popup, navbar, and settings.
 */
object ShellDefaults {

    /**
     * Central ripple indication using Material 3 ripple API.
     * Replaces all inline `rememberRipple(color = if (isDark) ... else ...)` calls.
     */
    @Composable
    fun ripple(
        colors: ShellColors = rememberShellColors(),
        bounded: Boolean = true
    ): Indication = ripple(color = colors.ripple, bounded = bounded)

    /**
     * Central ripple indication with explicit radius.
     */
    @Composable
    fun ripple(
        colors: ShellColors = rememberShellColors(),
        bounded: Boolean = true,
        radius: Dp
    ): Indication = ripple(color = colors.ripple, bounded = bounded, radius = radius)

    /**
     * Standard floating soft shadow used by pill, popup, navbar.
     * Eliminates duplicate `.cssShadow(color = Color.Black, alpha = 0.05f, blurRadius = 16.dp, offsetY = 4.dp)` calls.
     */
    fun Modifier.floatingShadow(alpha: Float = 0.05f): Modifier =
        this.cssShadow(
            color = Color.Black,
            alpha = alpha,
            blurRadius = 16.dp,
            offsetY = 4.dp
        )

    /**
     * Floating glass container modifier used by pill, popup, navbar.
     * Applies shadow + translucent background + thin border.
     */
    fun Modifier.floatingContainer(
        colors: ShellColors,
        bgAlpha: Float = 0.88f,
        borderAlpha: Float = 0.15f,
        shadowAlpha: Float = 0.05f
    ): Modifier =
        this
            .cssShadow(
                color = Color.Black,
                alpha = shadowAlpha,
                blurRadius = 16.dp,
                offsetY = 4.dp
            )
            .background(
                color = colors.floatingBg.copy(alpha = bgAlpha),
                shape = CircleShape
            )
            .border(
                width = 0.5.dp,
                color = colors.floatingBorder.copy(alpha = borderAlpha),
                shape = CircleShape
            )
}
