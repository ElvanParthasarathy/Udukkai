package com.elvan.udukkai.ui.screens.onboarding.screens.steps

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.theme.ElvanSansFontFamily
import com.elvan.udukkai.ui.screens.onboarding.screens.NeramColors
import kotlinx.coroutines.delay

private val greetings = listOf("வணக்கம்!", "Hello!", "നമസ്കാരം!")

/**
 * The opening greeting of the first-run setup: each greeting is shown for 1200ms and hidden for 800ms, with a
 * 500ms fade, in the middle of the page.
 */
@Composable
fun GreetingStep(
    onComplete: () -> Unit
) {
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

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = greetings[greetingIndex],
            fontFamily = ElvanSansFontFamily,
            fontSize = 48.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = (-1).sp,
            color = NeramColors.textPrimary(),
            modifier = Modifier.alpha(opacity)
        )
    }
}
