package com.elvan.udukkai.ui.screens.onboarding.screens.steps

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.theme.ThemeManager
import kotlinx.coroutines.delay
import com.elvan.udukkai.theme.LocalShellColors

private val greetings = listOf("வணக்கம்!", "Hello!", "നமസ്കാരം!")

/**
 * Animated Greeting Step matching Flutter's GreetingStep (vanakkam_padi.dart) 1:1.
 * Fades in (500ms), holds (1200ms), fades out (500ms), pauses (800ms), cycles through 3 greetings.
 */
@Composable
fun GreetingStep(
    onComplete: () -> Unit
) {
    val textColor = LocalShellColors.current.textPrimary

    var greetingIndex by remember { mutableStateOf(0) }
    val opacityAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        for (i in greetings.indices) {
            greetingIndex = i
            // Fade in: 500ms
            opacityAnim.animateTo(1f, animationSpec = tween(500))
            // Hold: 1200ms
            delay(1200)
            // Fade out: 500ms
            opacityAnim.animateTo(0f, animationSpec = tween(500))
            // Pause: 800ms
            delay(800)
        }
        onComplete()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = greetings[greetingIndex],
            fontSize = 48.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = (-1).sp,
            color = textColor,
            modifier = Modifier.alpha(opacityAnim.value)
        )
    }
}
