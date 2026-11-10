package com.elvan.udukkai.ui.components.shell

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.Measured
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.elvan.udukkai.core.extensions.cssShadow
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors

/**
 * Landscape has no top or bottom bar: the page title sits at the top right next to the round button, and the page's
 * actions (search, filter, settings...) are one slim pill of icons on the left edge, mirroring the tab rail on the right.
 */
@Composable
fun BoxScope.ElvanLandscapeChrome(
    title: String,
    topInset: Dp,
    titleStart: Dp,
    titleAlpha: Float,
    hasActions: Boolean,
    onToolbarPresent: (Boolean) -> Unit,
    actions: @Composable RowScope.() -> Unit,
    colors: ShellColors
) {
    if (title.isNotBlank()) Box(
        modifier = Modifier
            .align(Alignment.TopStart)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Start))
            .padding(start = titleStart, top = topInset + 8.dp)
            .height(56.dp)
            .graphicsLayer { alpha = titleAlpha }
            .zIndex(100f),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(fontFamily = LocalAppFontFamily.current, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
        )
    }
    androidx.compose.animation.AnimatedVisibility(
        visible = hasActions && com.elvan.udukkai.core.platform.getImeBottomPadding() == 0.dp,
        enter = androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(180)),
        exit = androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(120)),
        modifier = Modifier.align(Alignment.BottomEnd)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            .padding(end = 16.dp, bottom = com.elvan.udukkai.theme.Dimens.LandscapeEdgeGap).zIndex(100f)
    ) {
        // A camera cut-out on this edge (centred on the long side of some phones) moves the toolbar a little up, clear of it
        val density = androidx.compose.ui.platform.LocalDensity.current
        val cameraOnEdge = WindowInsets.displayCutout.getRight(density, androidx.compose.ui.unit.LayoutDirection.Ltr) > 0
        Box {
            // Drawn a touch larger than the top-bar pill; the labelled tab rail on the right is the bigger of the two
            val d = androidx.compose.ui.platform.LocalDensity.current
            androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(d.density * 1.05f, d.fontScale)) {
                SideActionPill(colors, actions, onToolbarPresent)
            }
        }
    }
}

/** The row-of-buttons slot, laid out as a column inside a round pill. */
@Composable
private fun SideActionPill(colors: ShellColors, actions: @Composable RowScope.() -> Unit, onPresent: (Boolean) -> Unit) {
    val scope = remember {
        object : RowScope {
            override fun Modifier.weight(weight: Float, fill: Boolean): Modifier = this
            override fun Modifier.align(alignment: Alignment.Vertical): Modifier = this
            override fun Modifier.alignBy(alignmentLine: androidx.compose.ui.layout.HorizontalAlignmentLine): Modifier = this
            override fun Modifier.alignBy(alignmentLineBlock: (Measured) -> Int): Modifier = this
            override fun Modifier.alignByBaseline(): Modifier = this
        }
    }
    Layout(
        content = { scope.actions() },
        modifier = Modifier
            .landscapePillSurface(CircleShape, colors)
            .onSizeChanged { onPresent(it.width > 0) }
    ) { measurables, constraints ->
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val placeables = measurables.map { it.measure(loose) }
        val gap = 2.dp.roundToPx()
        val pad = if (placeables.isEmpty()) 0 else 6.dp.roundToPx()
        val inner = placeables.maxOfOrNull { it.width } ?: 0
        val width = if (placeables.isEmpty()) 0 else inner + pad * 2
        val height = if (placeables.isEmpty()) 0 else placeables.sumOf { it.height } + gap * (placeables.size - 1).coerceAtLeast(0) + pad * 2
        layout(width, height) {
            var y = pad
            placeables.forEach { p ->
                p.placeRelative((width - p.width) / 2, y)
                y += p.height + gap
            }
        }
    }
}

/** Android controls flow with the list; desktop retains its original top-band placement. */
fun Modifier.elvanTopBandRow(active: Boolean, up: Dp = 12.dp): Modifier =
    when {
        !active || com.elvan.udukkai.core.platform.platformLayoutPolicy.supportsLandscapeControls -> this
        else -> this.then(Modifier.layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            layout(placeable.width, 0) {
                placeable.placeRelative(0, -(up.roundToPx() + placeable.height / 2))
            }
        })
    }


/** The room under a list: the portrait value upright, a thin margin sideways (no bottom bar there). */
@Composable
fun androidx.compose.ui.unit.Dp.sidewaysTight(): androidx.compose.ui.unit.Dp = if (LocalElvanSideBar.current) 20.dp else this
