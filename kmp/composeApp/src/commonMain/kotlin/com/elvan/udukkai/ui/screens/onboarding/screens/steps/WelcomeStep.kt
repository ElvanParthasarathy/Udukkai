package com.elvan.udukkai.ui.screens.onboarding.screens.steps

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
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

private val greetings = listOf("வணக்கம்!", "Hello!", "നമസ്കാരം!")

/**
 * Animated Greeting Step matching Flutter's GreetingStep (vanakkam_padi.dart) 1:1:
 * each greeting is shown for 1200ms and hidden for 800ms, with a 500ms
 * AnimatedOpacity fade running inside those windows.
 */
@Composable
fun GreetingStep(
    onComplete: () -> Unit
) {
    val isDark = ThemeManager.isDark()
    val textColor = if (isDark) Color.White else Color(0xFF1A1A1A)

    var greetingIndex by remember { mutableStateOf(0) }
    var visible by remember { mutableStateOf(false) }
    val opacity by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(500),
        label = "greeting_opacity"
    )

    LaunchedEffect(Unit) {
        for (i in greetings.indices) {
            greetingIndex = i
            visible = true
            delay(1200)
            visible = false
            delay(800)
        }
        onComplete()
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = greetings[greetingIndex],
            fontSize = 48.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = (-1).sp,
            color = textColor,
            modifier = Modifier.alpha(opacity)
        )
    }
}
