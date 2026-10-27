package com.elvan.udukkai.ui.components.shell

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.min

/**
 * Photo-gallery style viewer for a single image (here: one document sheet):
 *  - fitted and centred when not zoomed; dragging does nothing then (nothing to move),
 *  - pinch zooms around the fingers, double-tap toggles fitted <-> [doubleTapZoom],
 *  - when zoomed the image pans with momentum, but only inside the image's own edges
 *    (it never slides off into empty space), with a soft rubber band and a spring back,
 *  - [contentPadding] reserves room (top bar / bottom pill) for the fitted image only;
 *    a zoomed image may use the whole area, so it can pass under those bars.
 * [onZoomChanged] reports the zoom (1 = fitted).
 */
@Composable
fun ElvanZoomableImage(
    bitmap: ImageBitmap,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    maxZoom: Float = 4f,
    doubleTapZoom: Float = 2.5f,
    onZoomChanged: (Float) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var animation by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(scale) { onZoomChanged(scale) }

    BoxWithConstraints(modifier = modifier.clipToBounds(), contentAlignment = Alignment.Center) {
        val viewW = constraints.maxWidth.toFloat()
        val viewH = constraints.maxHeight.toFloat()
        val padStart = with(density) { contentPadding.calculateStartPadding(LayoutDirection.Ltr).toPx() }
        val padEnd = with(density) { contentPadding.calculateEndPadding(LayoutDirection.Ltr).toPx() }
        val padTop = with(density) { contentPadding.calculateTopPadding().toPx() }
        val padBottom = with(density) { contentPadding.calculateBottomPadding().toPx() }

        // Size at zoom 1 (fitted inside the padded area) and where its centre sits (offset at zoom 1)
        val fit = min((viewW - padStart - padEnd) / bitmap.width, (viewH - padTop - padBottom) / bitmap.height)
        val fitW = bitmap.width * fit
        val fitH = bitmap.height * fit
        val restOffset = Offset((padStart - padEnd) / 2f, (padTop - padBottom) / 2f)

        // Allowed range of the image-centre offset for a zoom level, per axis.
        fun axisRange(content: Float, view: Float, fitted: Float, rest: Float): ClosedFloatingPointRange<Float> {
            return if (content <= view) {
                // Smaller than the screen along this axis: locked in place (blends from the
                // padded rest position to dead centre as it grows to fill the screen).
                val t = ((content - fitted) / (view - fitted).coerceAtLeast(1f)).coerceIn(0f, 1f)
                val c = rest * (1f - t)
                c..c
            } else {
                val m = (content - view) / 2f
                -m..m
            }
        }

        fun clampOffset(o: Offset, s: Float): Offset {
            val rx = axisRange(fitW * s, viewW, fitW, restOffset.x)
            val ry = axisRange(fitH * s, viewH, fitH, restOffset.y)
            return Offset(o.x.coerceIn(rx), o.y.coerceIn(ry))
        }

        // Soft resistance beyond the edges while a finger is down (rubber band)
        fun rubber(o: Offset, s: Float): Offset {
            val rx = axisRange(fitW * s, viewW, fitW, restOffset.x)
            val ry = axisRange(fitH * s, viewH, fitH, restOffset.y)
            fun r(v: Float, range: ClosedFloatingPointRange<Float>) = when {
                v < range.start -> range.start + (v - range.start) * 0.3f
                v > range.endInclusive -> range.endInclusive + (v - range.endInclusive) * 0.3f
                else -> v
            }
            return Offset(r(o.x, rx), r(o.y, ry))
        }

        val center = Offset(viewW / 2f, viewH / 2f)

        // Zoom by [ratio] keeping the content under [focus] (view coordinates) in place.
        fun zoomedOffset(current: Offset, ratio: Float, focus: Offset): Offset =
            (focus - center) - ((focus - center) - current) * ratio

        fun settle(velocity: Offset) {
            animation?.cancel()
            animation = scope.launch {
                val targetScale = scale.coerceIn(1f, maxZoom)
                val targetOffset = if (targetScale <= 1.0001f) restOffset else clampOffset(offset, targetScale)
                if (targetScale != scale || targetOffset != offset) {
                    val s0 = scale
                    val o0 = offset
                    animate(0f, 1f, animationSpec = spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMediumLow)) { f, _ ->
                        scale = s0 + (targetScale - s0) * f
                        offset = o0 + (targetOffset - o0) * f
                    }
                } else if (scale > 1.0001f && (velocity.x != 0f || velocity.y != 0f)) {
                    val rx = axisRange(fitW * scale, viewW, fitW, restOffset.x)
                    val ry = axisRange(fitH * scale, viewH, fitH, restOffset.y)
                    // Animatable stops by itself at its bounds (the sheet's edges)
                    val ax = Animatable(offset.x).apply { updateBounds(rx.start, rx.endInclusive) }
                    val ay = Animatable(offset.y).apply { updateBounds(ry.start, ry.endInclusive) }
                    coroutineScope {
                        launch {
                            ax.animateDecay(velocity.x, exponentialDecay(frictionMultiplier = 1.6f)) {
                                offset = Offset(value, offset.y)
                            }
                        }
                        launch {
                            ay.animateDecay(velocity.y, exponentialDecay(frictionMultiplier = 1.6f)) {
                                offset = Offset(offset.x, value)
                            }
                        }
                    }
                }
            }
        }

        Image(
            bitmap = bitmap,
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            filterQuality = FilterQuality.High,
            modifier = Modifier
                .size(with(density) { fitW.toDp() }, with(density) { fitH.toDp() })
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
        )

        // Rest position when the size or the reserved padding changes
        LaunchedEffect(fitW, fitH, restOffset) {
            if (scale <= 1.0001f) offset = restOffset
        }

        // Double-tap: fitted <-> zoomed, towards the tapped point
        val doubleTap = Modifier.pointerInput(fitW, fitH, restOffset) {
            detectTapGestures(onDoubleTap = { tap ->
                animation?.cancel()
                animation = scope.launch {
                    val s0 = scale
                    val o0 = offset
                    val targetScale = if (s0 > 1.05f) 1f else doubleTapZoom
                    val targetOffset = if (targetScale <= 1f) restOffset
                    else clampOffset(zoomedOffset(o0, targetScale / s0, tap), targetScale)
                    animate(0f, 1f, animationSpec = tween(300, easing = FastOutSlowInEasing)) { f, _ ->
                        scale = s0 + (targetScale - s0) * f
                        offset = o0 + (targetOffset - o0) * f
                    }
                }
            })
        }

        // Pinch + pan (+ fling on release)
        val transform = Modifier.pointerInput(fitW, fitH, restOffset) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                animation?.cancel()
                val tracker = VelocityTracker()
                do {
                    val event = awaitPointerEvent()
                    if (event.changes.any { it.isConsumed }) break
                    val zoom = event.calculateZoom()
                    val pan = event.calculatePan()
                    val focus = event.calculateCentroid(useCurrent = true)
                    val multiTouch = event.changes.count { it.pressed } > 1
                    // Fitted and one finger: nothing to move, like a real photo viewer
                    val canPan = multiTouch || scale > 1.001f
                    if (canPan && (zoom != 1f || pan != Offset.Zero)) {
                        val newScale = (scale * zoom).coerceIn(0.85f, maxZoom * 1.15f)
                        val ratio = newScale / scale
                        val raw = zoomedOffset(offset, ratio, focus) + pan
                        scale = newScale
                        offset = rubber(raw, newScale)
                        tracker.addPosition(event.changes[0].uptimeMillis, focus)
                        event.changes.forEach { if (it.positionChanged()) it.consume() }
                    }
                } while (event.changes.any { it.pressed })
                val v = tracker.calculateVelocity()
                settle(Offset(v.x, v.y))
            }
        }

        // Transparent overlay that owns the gestures
        Box(
            modifier = Modifier
                .matchParentSize()
                .then(doubleTap)
                .then(transform)
        )
    }
}
