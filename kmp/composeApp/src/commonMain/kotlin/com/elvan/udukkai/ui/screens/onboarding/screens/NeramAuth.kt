package com.elvan.udukkai.ui.screens.onboarding.screens

import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import com.elvan.udukkai.core.platform.navigationBarsPaddingIfMobile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.extensions.cssShadow
import com.elvan.udukkai.theme.ElvanSansFontFamily
import com.elvan.udukkai.theme.ThemeManager
import com.elvan.udukkai.ui.components.ExpressiveLoadingIndicator
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import kotlinx.coroutines.delay

private val EaseOutCubic = CubicBezierEasing(0.215f, 0.61f, 0.355f, 1.0f)

/** The text and surface colours of the sign-in pages, as in Neram's AuthColors (the blue accent becomes black/white). */
internal object NeramColors {
    @Composable fun textPrimary() = if (ThemeManager.isDark()) Color.White else Color(0xFF1A1A1A)
    @Composable fun textSecondary() = if (ThemeManager.isDark()) Color.White.copy(alpha = 0.6f) else Color(0xFF666666)
    @Composable fun textMuted() = if (ThemeManager.isDark()) Color.White.copy(alpha = 0.4f) else Color(0xFF999999)
    @Composable fun inputBackground() = if (ThemeManager.isDark()) Color(0xFF1E1E1E) else Color.White
    @Composable fun divider() = if (ThemeManager.isDark()) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    @Composable fun accent() = if (ThemeManager.isDark()) Color.White else Color.Black
    @Composable fun onAccent() = if (ThemeManager.isDark()) Color.Black else Color.White
}

/** Neram's AuthAnimatedElement: each piece fades in and rises 20dp, one after another (80ms + 90ms per step, 500ms). */
@Composable
internal fun NeramElement(
    delayIndex: Int = 0,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(80L + delayIndex * 90L)
        started = true
    }
    val alpha by animateFloatAsState(if (started) 1f else 0f, tween(500, easing = EaseOutCubic), label = "elem_alpha")
    val offset by animateDpAsState(if (started) 0.dp else 20.dp, tween(500, easing = EaseOutCubic), label = "elem_offset")
    Box(
        modifier = modifier.graphicsLayer {
            this.alpha = alpha
            translationY = offset.toPx()
        }
    ) { content() }
}

@Composable
internal fun NeramHeader(title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = TextStyle(fontFamily = ElvanSansFontFamily, fontSize = 23.sp, fontWeight = FontWeight.Bold, color = NeramColors.textPrimary(), lineHeight = 29.sp),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = TextStyle(fontFamily = ElvanSansFontFamily, fontSize = 13.sp, color = NeramColors.textSecondary(), lineHeight = 18.sp),
            textAlign = TextAlign.Center
        )
    }
}

/** The flat pill button of Neram's AnimatedAuthButton, black on light and white on dark instead of blue. */
@Composable
internal fun NeramButton(
    text: String,
    onClick: () -> Unit,
    loading: Boolean = false,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val active = enabled && !loading
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(50))
            .background(NeramColors.accent().copy(alpha = if (active || loading) 1f else 0.4f))
            .clickable(
                enabled = active,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = NeramColors.onAccent().copy(alpha = 0.35f), bounded = true),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            ExpressiveLoadingIndicator(modifier = Modifier.size(24.dp), color = NeramColors.onAccent())
        } else {
            Text(
                text = text,
                style = TextStyle(fontFamily = ElvanSansFontFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                color = NeramColors.onAccent()
            )
        }
    }
}

/** Neram's soft shadow under pills in light mode; dark mode has none. */
@Composable
internal fun Modifier.neramShadow(): Modifier =
    if (ThemeManager.isDark()) this
    else this.cssShadow(color = Color.Black, alpha = 0.10f, blurRadius = 8.dp, offsetY = 2.dp)

/** A filled pill, the shape of the Google button. */
@Composable
internal fun Modifier.neramPill(): Modifier =
    this.neramShadow()
        .clip(RoundedCornerShape(50))
        .background(NeramColors.inputBackground())

/**
 * The layout every sign-in and setup page shares: the content sits in the middle of the free space (and scrolls
 * only when the screen is too small), and the button stays pinned at the bottom in the same place on every page.
 */
@Composable
internal fun NeramPage(
    bottom: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = maxHeight)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .padding(bottom = 96.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
            }
        }
        NeramElement(
            delayIndex = 3,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPaddingIfMobile()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            bottom()
        }
    }
}

internal data class NeramChoice(val code: String, val title: String, val subtitle: String? = null)

/** Neram's rounded card of choices with a check on the selected one (the language rows). */
@Composable
internal fun NeramChoiceCard(
    choices: List<NeramChoice>,
    selected: String,
    onSelect: (String) -> Unit
) {
    val dark = ThemeManager.isDark()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (dark) Modifier
                else Modifier.cssShadow(color = Color.Black, alpha = 0.12f, blurRadius = 14.dp, offsetY = 4.dp)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(NeramColors.inputBackground())
            .padding(vertical = 4.dp)
    ) {
        choices.forEachIndexed { index, choice ->
            val isSelected = selected == choice.code
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = if (dark) Color.White else Color.Black, bounded = true)
                    ) { onSelect(choice.code) }
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = choice.title,
                        style = TextStyle(
                            fontFamily = ElvanSansFontFamily,
                            fontSize = 16.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            lineHeight = 21.sp
                        ),
                        color = if (isSelected) NeramColors.textPrimary() else NeramColors.textPrimary().copy(alpha = 0.75f)
                    )
                    if (!choice.subtitle.isNullOrBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = choice.subtitle,
                            style = TextStyle(fontFamily = ElvanSansFontFamily, fontSize = 12.sp, lineHeight = 16.sp),
                            color = NeramColors.textSecondary()
                        )
                    }
                }
                if (isSelected) {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.CheckCircleFill,
                        contentDescription = null,
                        tint = NeramColors.textPrimary(),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            if (index < choices.size - 1) {
                HorizontalDivider(
                    color = if (dark) Color.White.copy(alpha = 0.055f) else Color.Black.copy(alpha = 0.055f),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 18.dp)
                )
            }
        }
    }
}
