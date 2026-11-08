package com.elvan.udukkai.ui.components.shell

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
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
import androidx.compose.ui.layout.positionInRoot
import com.elvan.udukkai.core.extensions.cssShadow
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.ShellDefaults

object ElvanMenuState {
    var isMenuOpen by mutableStateOf(false)
}

/** Where the button is, in the panel's own coordinates; set when the panel is placed. Not state: read only while drawing. */
private class PanelRef {
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
    upward: Boolean = false,
    /** The glyph of a plain icon button, drawn on the glass so it dissolves as the menu opens; null where the button is more than an icon. */
    triggerIcon: ImageVector? = com.elvan.udukkai.ui.navigation.MaterialSymbols.Rounded.MoreVert
) {
    SideEffect {
        ElvanMenuState.isMenuOpen = expanded
    }
    DisposableEffect(Unit) {
        onDispose {
            ElvanMenuState.isMenuOpen = false
        }
    }

    val progress = remember { Animatable(0f) }
    var mounted by remember { mutableStateOf(false) }
    val panelRef = remember { PanelRef() }
    LaunchedEffect(expanded) {
        if (expanded) {
            mounted = true
            // The panel takes a frame or two to compose; started now, the opening would be used up before it is on screen
            if (progress.value == 0f) repeat(2) { withFrameNanos { } }
            progress.animateTo(1f, spring(dampingRatio = 0.62f, stiffness = 300f))
        } else if (mounted) {
            progress.animateTo(0f, spring(dampingRatio = 1f, stiffness = 500f))
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

    fun buttonIn(size: Size, outset: Float): Rect = panelRef.trigger ?: anchor.size.let {
        // A separate popup window has no placed panel: the button is in the corner the panel grows from
        val top = if (upward) size.height - outset - it.height else outset
        Rect(size.width - outset - it.width, top, size.width - outset, top + it.height)
    }
    val panelShape = RoundedCornerShape(PANEL_RADIUS)
    val rim = Brush.verticalGradient(
        if (colors.isDark) listOf(Color.White.copy(alpha = 0.28f), colors.floatingBorder.copy(alpha = 0.25f))
        else listOf(Color.White.copy(alpha = 0.9f), colors.floatingBorder.copy(alpha = 0.6f))
    )

    val panel: @Composable () -> Unit = {
        Box(
            modifier = modifier
                .width(IntrinsicSize.Max)
                .graphicsLayer {
                    val p = progress.value
                    val button = buttonIn(size, TRIGGER_OUTSET.toPx())
                    // Past the end of the spring the whole pane swells from the button
                    val swell = 1f + (p - 1f).coerceAtLeast(0f) * 0.5f
                    scaleX = swell
                    scaleY = swell
                    transformOrigin = TransformOrigin(button.center.x / size.width, button.center.y / size.height)
                    alpha = (p * 8f).coerceIn(0f, 1f) // only so the first frame does not pop
                }
        ) {
            // The navigation bar's shadow, kept to the moving outline
            Box(
                Modifier.matchParentSize().graphicsLayer {
                    val o = morphRect(buttonIn(size, TRIGGER_OUTSET.toPx()), size, progress.value.coerceIn(0f, 1f))
                    transformOrigin = TransformOrigin(0f, 0f)
                    translationX = o.left
                    translationY = o.top
                    scaleX = o.width / size.width
                    scaleY = o.height / size.height
                }.cssShadow(color = Color.Black, alpha = 0.05f, borderRadius = PANEL_RADIUS, blurRadius = 16.dp, offsetY = 4.dp)
            )
            Box(
                Modifier
                    .graphicsLayer {
                        val p = progress.value.coerceIn(0f, 1f)
                        val button = buttonIn(size, TRIGGER_OUTSET.toPx())
                        val outline = morphRect(button, size, p)
                        val corner = minOf(button.width, button.height) / 2 * (1f - p) + PANEL_RADIUS.toPx() * p
                        shape = object : Shape {
                            override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density) =
                                Outline.Rounded(RoundRect(outline, CornerRadius(corner)))
                        }
                        clip = true
                    }
                    // Frosted glass over the page; plain translucent where there is no page to blur
                    .frostedGlass(panelShape, colors, fallbackAlpha = 0.94f)
                    .border(0.75.dp, rim, panelShape)
                    .clip(panelShape)
                    // Taps on the menu stay on the menu
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
            ) {
                MenuRows(items, colors, { progress.value }, onDismissRequest)
                if (triggerIcon != null) {
                    Icon(
                        triggerIcon, contentDescription = null, tint = colors.textPrimary,
                        modifier = Modifier.size(22.dp).graphicsLayer {
                            val button = panelRef.trigger ?: return@graphicsLayer
                            val gone = (progress.value / 0.3f).coerceIn(0f, 1f)
                            translationX = button.center.x - size.width / 2
                            translationY = button.center.y - size.height / 2
                            alpha = 1f - gone
                            scaleX = 1f - gone * 0.3f
                            scaleY = 1f - gone * 0.3f
                        }
                    )
                }
            }
        }
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
private fun MenuRows(items: List<ElvanPopupMenuItem>, colors: ShellColors, progress: () -> Float, onDismissRequest: () -> Unit) {
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
                            item.onClick()
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
