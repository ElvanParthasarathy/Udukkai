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
    if (hasActions) {
        // A camera cut-out on this edge (centred on the long side of some phones) moves the toolbar a little up, clear of it
        val density = androidx.compose.ui.platform.LocalDensity.current
        val cameraOnEdge = WindowInsets.displayCutout.getRight(density, androidx.compose.ui.unit.LayoutDirection.Ltr) > 0
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                .padding(end = 16.dp, bottom = if (cameraOnEdge) 36.dp else 12.dp)
                .zIndex(100f)
        ) {
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
            .cssShadow(color = Color.Black, alpha = 0.05f, blurRadius = 16.dp, offsetY = 4.dp)
            .frostedGlass(CircleShape, colors, fallbackAlpha = 0.88f)
            .border(0.5.dp, colors.floatingBorder.copy(alpha = if (colors.isDark) 0.15f else 0.6f), CircleShape)
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

/**
 * Sideways, the pill shifter lives in the top band beside the page title instead of below it: this keeps its place in the
 * list but takes no height, drawn [up] higher.
 */
fun Modifier.elvanTopBandRow(active: Boolean, up: Dp = 10.dp): Modifier =
    if (!active) this else this.then(
        // Centred on the band the page title and the round button share: 10dp above where the page starts
        Modifier.layout { measurable, constraints ->
            val p = measurable.measure(constraints)
            layout(p.width, 0) { p.placeRelative(0, -(up.roundToPx() + p.height / 2)) }
        }
    )


/** The room under a list: the portrait value upright, a thin margin sideways (no bottom bar there). */
@Composable
fun androidx.compose.ui.unit.Dp.sidewaysTight(): androidx.compose.ui.unit.Dp = if (LocalElvanSideBar.current) 20.dp else this
