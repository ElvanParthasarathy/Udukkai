package com.elvan.udukkai.ui.components.shell

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.positionInRoot
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.ShellDefaults

object ElvanMenuState {
    var isMenuOpen by mutableStateOf(false)

    /** The panel (window pixels) during a menu row's onClick; a page that opens from it takes it (sets null) to grow out of it. */
    var clickedPanel: androidx.compose.ui.geometry.Rect? = null
}

/** Panel position, and whether a page opening over the panel means it should stay put rather than shrink. Not state: read only on taps. */
private class PanelRef {
    var rect: androidx.compose.ui.geometry.Rect? = null
    var coveredByNewPage = false
    /** Where the button is, in the panel's own coordinates; set when the panel is placed. */
    var trigger: Rect? = null
}

private val PANEL_RADIUS = 24.dp
/** The panel reaches this far past the button, so the button sits inside the corner the panel grows from. */
private val TRIGGER_OUTSET = 4.dp
private val SCREEN_MARGIN = 8.dp

/** The panel's outline at [progress]: exactly the button at 0, the whole panel at 1. */
internal fun morphRect(trigger: Rect, panel: Size, progress: Float): Rect = Rect(
    trigger.left * (1f - progress),
    trigger.top * (1f - progress),
    trigger.right + (panel.width - trigger.right) * progress,
    trigger.bottom + (panel.height - trigger.bottom) * progress
)

/** The panel's left or top edge: where the button puts it, kept on screen. */
internal fun clampToScreen(wanted: Int, panel: Int, screen: Int, margin: Int): Int =
    wanted.coerceIn(margin, (screen - panel - margin).coerceAtLeast(margin))

/**
 * ElvanPopupMenu for Udukkai.
 * The panel's outline starts as the button itself and opens out to the full menu, corners easing to 24dp, and closes
 * back into the button. It is revealed, never scaled, so the rows do not squash. Frosted, translucent styling.
 */
class ElvanPopupMenuItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun ElvanPopupMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    colors: ShellColors,
    items: List<ElvanPopupMenuItem>,
    modifier: Modifier = Modifier,
    /** True for a button at the bottom of the screen: the menu opens upward and grows from its bottom-right corner. */
    upward: Boolean = false
) {
    SideEffect {
        ElvanMenuState.isMenuOpen = expanded
    }
    DisposableEffect(Unit) {
        onDispose {
            ElvanMenuState.isMenuOpen = false
        }
    }

    // Opens out of the button without overshoot and closes back into it (it stays on screen until that has played)
    val progress = remember { Animatable(0f) }
    var mounted by remember { mutableStateOf(false) }
    val panelRef = remember { PanelRef() }
    LaunchedEffect(expanded) {
        if (expanded) {
            panelRef.coveredByNewPage = false
            mounted = true
            // The panel takes a frame or two to compose; started now, the opening would be used up before it is on screen
            if (progress.value == 0f) repeat(2) { withFrameNanos { } }
            progress.animateTo(1f, tween(230, easing = CubicBezierEasing(0f, 0f, 0.2f, 1f)))
        } else if (mounted) {
            if (panelRef.coveredByNewPage) {
                // shortcut: fixed wait for the new window to cover the panel, tie it to the lifecycle if a slow phone shows the panel vanish first
                kotlinx.coroutines.delay(700)
                progress.snapTo(0f)
                panelRef.coveredByNewPage = false
            } else {
                progress.animateTo(0f, tween(170, easing = CubicBezierEasing(0.4f, 0f, 1f, 1f)))
            }
            mounted = false
        }
    }

    // Where the button is, so the menu opens at the same spot when it is drawn in the page layer
    var anchor by remember { mutableStateOf(androidx.compose.ui.geometry.Rect.Zero) }
    androidx.compose.foundation.layout.Spacer(
        Modifier.size(0.dp).onGloballyPositioned { c -> c.parentLayoutCoordinates?.let { val at = it.positionInRoot(); anchor = androidx.compose.ui.geometry.Rect(at.x, at.y, at.x + it.size.width, at.y + it.size.height) } }
    )

    val host = LocalMenuHost.current
    val dismiss = {
        ElvanMenuState.isMenuOpen = false
        onDismissRequest()
    }

    val pick: (ElvanPopupMenuItem) -> Unit = pick@{ item ->
        if (panelRef.coveredByNewPage) return@pick // a second tap while the first page is opening
        // A separate popup window has its own coordinates, so only a menu in the page layer can hand over its position
        val rect = if (host != null) panelRef.rect else null
        ElvanMenuState.clickedPanel = rect
        item.onClick()
        panelRef.coveredByNewPage = rect != null && ElvanMenuState.clickedPanel == null
        ElvanMenuState.clickedPanel = null
    }

    val panel: @Composable () -> Unit = {
        Box(
            modifier = modifier
                .graphicsLayer {
                    val p = progress.value
                    val outset = TRIGGER_OUTSET.toPx()
                    // Without a placed panel (separate popup window) the button is in the corner the panel grows from
                    val button = panelRef.trigger ?: anchor.size.let {
                        val top = if (upward) size.height - outset - it.height else outset
                        Rect(size.width - outset - it.width, top, size.width - outset, top + it.height)
                    }
                    val outline = morphRect(button, size, p)
                    val corner = minOf(button.width, button.height) / 2 * (1f - p) + PANEL_RADIUS.toPx() * p
                    shape = object : Shape {
                        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density) =
                            Outline.Rounded(RoundRect(outline, CornerRadius(corner)))
                    }
                    clip = true
                    shadowElevation = 12.dp.toPx() * p
                    alpha = (p * 8f).coerceIn(0f, 1f) // only so the first frame does not pop
                }
                .onGloballyPositioned { panelRef.rect = it.boundsInRoot() }
                // Frosted glass over the page; plain translucent where there is no page to blur
                .frostedGlass(RoundedCornerShape(24.dp), colors, fallbackAlpha = 0.94f)
                .border(
                    width = 0.5.dp,
                    color = colors.floatingBorder.copy(alpha = if (colors.isDark) 0.25f else 0.6f),
                    shape = RoundedCornerShape(24.dp)
                )
                .clip(RoundedCornerShape(24.dp))
                .width(IntrinsicSize.Max)
                // Taps on the menu stay on the menu
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
        ) { MenuRows(items, colors, { progress.value }, onDismissRequest, pick) }
    }

    if (host != null) {
        if (!expanded && !mounted) {
            SideEffect { if (host.content != null && host.onDismiss === dismissKey) host.content = null }
            return
        }
        val density = androidx.compose.ui.platform.LocalDensity.current
        val content: @Composable () -> Unit = {
            var origin by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
            androidx.compose.ui.layout.Layout(
                content = panel,
                modifier = Modifier.fillMaxSize().onGloballyPositioned { origin = it.positionInRoot() }
            ) { measurables, constraints ->
                val placeable = measurables.first().measure(constraints.copy(minWidth = 0, minHeight = 0))
                layout(constraints.maxWidth, constraints.maxHeight) {
                    val button = anchor.translate(-origin)
                    val outset = TRIGGER_OUTSET.roundToPx()
                    val margin = SCREEN_MARGIN.roundToPx()
                    val x = clampToScreen(button.right.toInt() + outset - placeable.width, placeable.width, constraints.maxWidth, margin)
                    val y = clampToScreen(if (upward) button.bottom.toInt() + outset - placeable.height else button.top.toInt() - outset, placeable.height, constraints.maxHeight, margin)
                    panelRef.trigger = button.translate(-x.toFloat(), -y.toFloat())
                    placeable.place(x, y)
                }
            }
        }
        SideEffect {
            host.onDismiss = dismiss
            dismissKey = dismiss
            host.content = content
        }
        DisposableEffect(host) { onDispose { if (host.content === content) host.content = null } }
        return
    }

    if (!expanded && !mounted) return

    val outsetPx = with(androidx.compose.ui.platform.LocalDensity.current) { TRIGGER_OUTSET.roundToPx() }
    Popup(
        alignment = if (upward) Alignment.BottomEnd else Alignment.TopEnd,
        offset = IntOffset(x = outsetPx, y = if (upward) outsetPx else -outsetPx),
        onDismissRequest = dismiss,
        properties = PopupProperties(focusable = true, dismissOnClickOutside = true, dismissOnBackPress = true)
    ) { panel() }
}

/** The one dismiss callback currently shown, so a closed menu clears only its own panel. */
private var dismissKey: (() -> Unit)? = null

@Composable
private fun MenuRows(items: List<ElvanPopupMenuItem>, colors: ShellColors, progress: () -> Float, onDismissRequest: () -> Unit, onPick: (ElvanPopupMenuItem) -> Unit) {
    Column(
        // The rows come in as the panel opens over them, in step with it
        modifier = Modifier.fillMaxWidth().graphicsLayer { alpha = ((progress() - 0.3f) / 0.5f).coerceIn(0f, 1f) }
    ) {
        items.forEachIndexed { index, item ->
            val isFirst = index == 0
            val isLast = index == items.size - 1
            val shape = when {
                isFirst && isLast -> RoundedCornerShape(24.dp)
                isFirst -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                isLast -> RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                else -> RoundedCornerShape(0.dp)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(shape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ShellDefaults.ripple(colors, bounded = true),
                        onClick = {
                            onDismissRequest()
                            onPick(item)
                        }
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = item.icon, contentDescription = item.title, tint = colors.textPrimary, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = item.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary, fontFamily = LocalAppFontFamily.current)
                Spacer(modifier = Modifier.width(16.dp))
            }
        }
    }
}
