package com.elvan.udukkai.ui.screens.onboarding.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.extensions.cssShadow
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.ThemeManager
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/** Welcome page laid out like Neram's: the logo floating in the middle, the Get Started button at the bottom. */
@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit
) {
    val dark = ThemeManager.isDark()
    val logoBg = if (dark) Color.White else Color(0xFF111111)
    val logoTint = if (dark) Color.Black else Color.White

    val infinite = rememberInfiniteTransition(label = "logo_float")
    val floatOffset by infinite.animateFloat(
        initialValue = -3f, targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(4500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float_offset"
    )
    val pulse by infinite.animateFloat(
        initialValue = 0.985f, targetValue = 1.015f,
        animationSpec = infiniteRepeatable(tween(5000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "logo_pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))

        NeramElement(delayIndex = 0) {
            Box(
                modifier = Modifier
                    .offset(y = floatOffset.dp)
                    .scale(pulse)
                    .size(140.dp)
                    .cssShadow(color = NeramColors.textPrimary(), alpha = if (dark) 0.15f else 0.10f, blurRadius = 40.dp, offsetY = 20.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .background(logoBg, RoundedCornerShape(40.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = MaterialSymbols.Rounded.DatabaseFill,
                    contentDescription = null,
                    tint = logoTint,
                    modifier = Modifier.size(72.dp)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        NeramElement(delayIndex = 1) {
            NeramHeader(title = K.welcomeToUdukkai.tr(), subtitle = K.billingGstSorted.tr())
        }

        Spacer(Modifier.weight(1f))

        NeramElement(delayIndex = 2, modifier = Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = K.tapGetStarted.tr(),
                    style = TextStyle(fontSize = 11.sp, color = NeramColors.textMuted(), lineHeight = 16.sp),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 20.dp, start = 16.dp, end = 16.dp)
                )
                NeramButton(text = K.getStartedBtn.tr(), onClick = onNavigateToLogin)
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}
