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
    @Composable fun textMuted() = if (ThemeManager.isDark()) Color.White.copy(alpha = 0.52f) else Color(0xFF767676)
    @Composable fun inputBackground() = if (ThemeManager.isDark()) Color(0xFF1E1E1E) else Color.White
    @Composable fun divider() = if (ThemeManager.isDark()) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    // The app's blue (Neram / One UI #0072DE) for the main buttons, white on it
    @Composable fun accent() = Color(0xFF0072DE)
    @Composable fun onAccent() = Color.White
}

/** Neram's AuthAnimatedElement: each piece fades in and rises 20dp, one after another (80ms + 90ms per step, 500ms). */
@Composable
internal fun NeramElement(
    delayIndex: Int = 0,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // With system animations off the piece is simply there
    val still = remember { !com.elvan.udukkai.core.platform.systemAnimationsEnabled() }
    var started by remember { mutableStateOf(still) }
    LaunchedEffect(Unit) {
        if (still) return@LaunchedEffect
        delay(80L + delayIndex * 90L)
        started = true
    }
    val alpha by animateFloatAsState(if (started) 1f else 0f, tween(if (still) 0 else 500, easing = EaseOutCubic), label = "elem_alpha")
    val offset by animateDpAsState(if (started) 0.dp else 20.dp, tween(if (still) 0 else 500, easing = EaseOutCubic), label = "elem_offset")
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
            modifier = Modifier.padding(horizontal = 8.dp),
            style = TextStyle(fontFamily = ElvanSansFontFamily, fontSize = 23.sp, fontWeight = FontWeight.Bold, color = NeramColors.textPrimary(), lineHeight = 29.sp),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = subtitle,
            modifier = Modifier.padding(horizontal = 16.dp),
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
                indication = ripple(color = Color.Black.copy(alpha = 0.35f), bounded = true), // Neram's: dark in light and dark mode, white was lost on the blue
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
            if (maxWidth > maxHeight) {
                // Sideways: the heading on the left, the fields on the right
                val scope = androidx.compose.runtime.remember {
                    object : ColumnScope {
                        override fun Modifier.weight(weight: Float, fill: Boolean): Modifier = this
                        override fun Modifier.align(alignment: Alignment.Horizontal): Modifier = this
                        override fun Modifier.alignBy(alignmentLine: androidx.compose.ui.layout.VerticalAlignmentLine): Modifier = this
                        override fun Modifier.alignBy(alignmentLineBlock: (androidx.compose.ui.layout.Measured) -> Int): Modifier = this
                    }
                }
                Box(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 16.dp).padding(bottom = 72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.ui.layout.Layout(content = { scope.content() }, modifier = Modifier.fillMaxWidth()) { ms, c ->
                        val gap = 32.dp.roundToPx()
                        val rowGap = 12.dp.roundToPx()
                        val leftW = (c.maxWidth - gap) * 2 / 5
                        val rightW = c.maxWidth - gap - leftW
                        val left = ms.first().measure(androidx.compose.ui.unit.Constraints(minWidth = leftW, maxWidth = leftW))
                        val rights = ms.drop(1).map { it.measure(androidx.compose.ui.unit.Constraints(minWidth = rightW, maxWidth = rightW)) }
                        val rightH = rights.sumOf { it.height } + rowGap * (rights.size - 1).coerceAtLeast(0)
                        val h = maxOf(left.height, rightH)
                        layout(c.maxWidth, h) {
                            left.placeRelative(0, (h - left.height) / 2)
                            var y = (h - rightH) / 2
                            rights.forEach { it.placeRelative(leftW + gap, y); y += it.height + rowGap }
                        }
                    }
                }
            } else
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
