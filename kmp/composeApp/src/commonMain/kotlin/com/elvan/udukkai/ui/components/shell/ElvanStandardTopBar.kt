package com.elvan.udukkai.ui.components.shell

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/** Height of [ElvanStandardTopBar] below the status bar. */
val ElvanStandardTopBarHeight: Dp = 64.dp

/** Space a page must leave at its top so its content starts below [ElvanStandardTopBar]. */
@Composable
fun elvanStandardTopBarInset(): Dp =
    WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + ElvanStandardTopBarHeight

/**
 * Fixed top bar for pages that must not use the large collapsing One UI header (document view
 * and preview), but should still behave like the normal screens:
 * - the back button is the same round [ElvanPill], transparent at rest and solid once [scrolled],
 * - the title fades away once [scrolled],
 * - content passing underneath fades out through the same top mask the normal screens use.
 * The bar never changes size or position. It is drawn over the page, so give the page
 * [elvanStandardTopBarInset] of top spacing.
 */
@Composable
fun ElvanStandardTopBar(
    title: String,
    onBack: () -> Unit,
    scrolled: Boolean,
    modifier: Modifier = Modifier,
    statusTop: Dp = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
    actions: @Composable RowScope.() -> Unit = {}
) {
    val colors = rememberShellColors()
    val lift by animateFloatAsState(
        targetValue = if (scrolled) 1f else 0f,
        animationSpec = tween(200),
        label = "top_bar_lift"
    )

    Box(modifier = modifier.fillMaxWidth()) {
        // Same top fade mask as the normal screens
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(statusTop + ElvanStandardTopBarHeight + 24.dp)
                .background(
                    Brush.verticalGradient(
                        0.0f to colors.background,
                        0.5f to colors.background.copy(alpha = 0.7f),
                        0.8f to colors.background.copy(alpha = 0.2f),
                        1.0f to Color.Transparent
                    )
                )
        )
        Row(
            modifier = Modifier
                .padding(top = statusTop)
                .height(ElvanStandardTopBarHeight)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ElvanPill(liftProgress = lift, colors = colors, modifier = Modifier.size(50.dp)) {
                ElvanTopBarIconButton(onClick = onBack, size = 44.dp) {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.ArrowBack,
                        contentDescription = K.back.tr(),
                        tint = colors.textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Text(
                text = title.preventBrokenLigatures(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontFamily = LocalAppFontFamily.current,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
                    .graphicsLayer { alpha = 1f - lift }
            )
            actions()
        }
    }
}
