package com.elvan.udukkai.ui.components.shell

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.theme.ShellColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Creates the vector Path for a Material 3 Expressive PIN shape at [index].
 *
 * Distinct Expressive Shapes:
 * 0: 4-Petal Clover (இதழ் மலர்)
 * 1: 4-Point Starburst (நட்சத்திரம்)
 * 2: 8-Petal Sunburst / Daisy (கதிர் வட்டம்)
 * 3: Squircle / Bulged Square (வளைந்த சதுரம்)
 * 4: 6-Petal Blossom (ஆறிதழ் மலர்)
 * 5: 8-Point Sparkle Star (எண்கதிர் நட்சத்திரம்)
 */
fun getExpressiveShapePath(index: Int, width: Float, height: Float): Path {
    val path = Path()
    val count = 48
    val r = minOf(width, height) * 0.45f
    val cx = width / 2f
    val cy = height / 2f
    val pi = PI.toFloat()

    fun pointAt(step: Int): Offset {
        val t = (step.toFloat() * 2f * pi / count.toFloat()) - (pi / 2f)
        val factor = when (index % 6) {
            0 -> 0.74f + 0.26f * cos(4f * t) // 4-petal clover
            1 -> 0.62f + 0.38f * cos(4f * t) // 4-point starburst
            2 -> 0.86f + 0.14f * cos(8f * t) // 8-petal sunburst
            3 -> 0.88f - 0.12f * cos(4f * t) // squircle (soft rounded square)
            4 -> 0.78f + 0.22f * cos(6f * t) // 6-petal blossom
            else -> 0.70f + 0.30f * cos(8f * t) // 8-point sparkle star
        }
        return Offset(cx + r * factor * cos(t), cy + r * factor * sin(t))
    }

    val start = pointAt(0)
    path.moveTo(start.x, start.y)
    for (i in 1 until count) {
        val pt = pointAt(i)
        path.lineTo(pt.x, pt.y)
    }
    path.close()
    return path
}

/**
 * One Material 3 Expressive dot of the PIN pad:
 * When empty, it rests as a subtle circular dot.
 * When a digit is entered, it springs and morphs into a distinct Material 3 Expressive
 * geometric shape (Clover, 4-Point Star, Sunburst, Squircle, Blossom, Sparkle) with bouncy physics.
 */
@Composable
fun ExpressivePinDot(
    index: Int,
    filled: Boolean,
    colors: ShellColors,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp
) {
    val bouncy = remember {
        spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    }

    val scale by animateFloatAsState(
        targetValue = if (filled) 1.18f else 0.82f,
        animationSpec = bouncy,
        label = "m3_dot_scale"
    )

    val rotation by animateFloatAsState(
        targetValue = if (filled) 0f else -45f,
        animationSpec = bouncy,
        label = "m3_dot_rotation"
    )

    val filledAlpha by animateFloatAsState(
        targetValue = if (filled) 1f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "m3_dot_filled_alpha"
    )

    val emptyAlpha by animateFloatAsState(
        targetValue = if (!filled) 1f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "m3_dot_empty_alpha"
    )

    val density = LocalDensity.current
    val sizePx = with(density) { size.toPx() }
    val shapePath = remember(index, sizePx) {
        getExpressiveShapePath(index, sizePx, sizePx)
    }

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = rotation
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            if (emptyAlpha > 0f) {
                drawCircle(
                    color = colors.textPrimary.copy(alpha = 0.22f * emptyAlpha),
                    radius = this.size.minDimension * 0.22f,
                    center = center
                )
            }
            if (filledAlpha > 0f) {
                drawPath(
                    path = shapePath,
                    color = colors.textPrimary.copy(alpha = filledAlpha)
                )
            }
        }
    }
}
