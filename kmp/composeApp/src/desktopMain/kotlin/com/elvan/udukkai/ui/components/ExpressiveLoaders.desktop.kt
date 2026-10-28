package com.elvan.udukkai.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** One rounded shape: a circle whose edge is pushed out in [lobes] places by [depth] (a share of the radius). */
private class LoaderShape(val lobes: Int, val depth: Float)

// A cookie, a star, a pentagon and a pill, like the shapes Material 3's expressive loader cycles through
private val loaderShapes = listOf(
    LoaderShape(10, 0.11f),
    LoaderShape(9, 0.08f),
    LoaderShape(5, 0.10f),
    LoaderShape(2, 0.26f),
    LoaderShape(8, 0.13f),
    LoaderShape(4, 0.07f),
    LoaderShape(2, 0.10f)
)

private fun shapeRadius(shape: LoaderShape, angle: Float): Float =
    1f + shape.depth * cos(shape.lobes * angle)

/**
 * ExpressiveDotsLoader: a shape that turns and melts from one rounded form into the next, drawn after the
 * Material 3 expressive loading indicator that Neram uses.
 */
@Composable
actual fun ExpressiveDotsLoader(
    modifier: Modifier,
    color: Color
) {
    val transition = rememberInfiniteTransition(label = "expressive_loader")
    val turn by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(4800, easing = LinearEasing)),
        label = "turn"
    )
    val step by transition.animateFloat(
        initialValue = 0f,
        targetValue = loaderShapes.size.toFloat(),
        animationSpec = infiniteRepeatable(animation = tween(loaderShapes.size * 650, easing = LinearEasing)),
        label = "step"
    )

    Canvas(modifier = modifier.graphicsLayer { rotationZ = turn }) {
        val from = step.toInt() % loaderShapes.size
        val to = (from + 1) % loaderShapes.size
        val raw = step - step.toInt()
        // Hold each shape for a moment, then melt into the next
        val blend = FastOutSlowInEasing.transform(((raw - 0.35f) / 0.65f).coerceIn(0f, 1f))

        val radius = min(size.width, size.height) / 2f / 1.28f
        val centre = Offset(size.width / 2f, size.height / 2f)
        val path = Path()
        val points = 96
        for (i in 0..points) {
            val angle = (2.0 * PI * i / points).toFloat()
            val r = radius * (
                shapeRadius(loaderShapes[from], angle) * (1f - blend) +
                    shapeRadius(loaderShapes[to], angle) * blend
                )
            val x = centre.x + r * cos(angle)
            val y = centre.y + r * sin(angle)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        drawPath(path, color)
    }
}

/**
 * The round loader of the pull-down refresh, after Material 3's ContainedLoadingIndicator: the shape sits in a
 * round container, follows the pull while [progress] is given, and keeps changing shape on its own when it is null.
 */
@Composable
actual fun ExpressiveContainedLoader(
    modifier: Modifier,
    progress: (() -> Float)?,
    containerColor: Color,
    indicatorColor: Color
) {
    Box(
        modifier = modifier.clip(CircleShape).background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        val pull = progress?.invoke()
        ExpressiveDotsLoader(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .graphicsLayer {
                    if (pull != null) {
                        scaleX = 0.6f + 0.4f * pull
                        scaleY = 0.6f + 0.4f * pull
                    }
                },
            color = indicatorColor
        )
    }
}
