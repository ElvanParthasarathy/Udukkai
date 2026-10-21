package com.elvan.udukkai.ui.screens.home

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically

/**
 * Animation specs for Home Screen components.
 * Modeled identically after Neram HomeAnimations.
 */
object HomeAnimations {

    /**
     * Welcome Animation
     * Content transition timing for the greeting card
     */
    object WelcomeTransition {
        const val DurationMs = 3000L // 3 seconds before transition

        // Motion specs: slides up and fades in/out concurrently
        val EnterTransition = slideInVertically { height -> height } + fadeIn()
        val ExitTransition = slideOutVertically { height -> -height } + fadeOut()
    }
}
