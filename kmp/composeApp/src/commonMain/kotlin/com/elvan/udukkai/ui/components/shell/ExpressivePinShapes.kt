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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.theme.ShellColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Creates a crisp regular hexagon path centered at ([cx], [cy]) with radius [r].
 */
private fun createHexagonPath(cx: Float, cy: Float, r: Float): Path {
    val path = Path()
    for (i in 0 until 6) {
        val a = (i.toFloat() * PI.toFloat() / 3f) - (PI.toFloat() / 6f)
        val x = cx + r * cos(a)
        val y = cy + r * sin(a)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

/**
 * Creates a crisp 4-point starburst path centered at ([cx], [cy]).
 */
private fun createStarPath(cx: Float, cy: Float, rOuter: Float, rInner: Float): Path {
    val path = Path()
    for (i in 0 until 8) {
        val a = (i.toFloat() * PI.toFloat() / 4f) - (PI.toFloat() / 2f)
        val r = if (i % 2 == 0) rOuter else rInner
        val x = cx + r * cos(a)
        val y = cy + r * sin(a)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

/**
 * Draws one of the 6 distinct, geometrically crisp Material 3 Expressive shapes:
 * 0: Rounded Square (Squircle / மழுங்கிய சதுரம்)
 * 1: Diamond (45° Rotated Square / வைரம்)
 * 2: Circle (வட்டம்)
 * 3: Pill (Capsule / குளிகை)
 * 4: Hexagon (அறுகோணம்)
 * 5: 4-Point Star (நான்குகதிர் நட்சத்திரம்)
 */
fun DrawScope.drawExpressiveShape(index: Int, color: Color) {
    val r = size.minDimension * 0.42f
    val cx = center.x
    val cy = center.y

    when (index % 6) {
        0 -> {
            // Squircle / Rounded Square
            drawRoundRect(
                color = color,
                topLeft = Offset(cx - r, cy - r),
                size = Size(2f * r, 2f * r),
                cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
            )
        }
        1 -> {
            // Diamond (45° rotated rounded square)
            withTransform({
                rotate(degrees = 45f, pivot = center)
            }) {
                val dr = r * 0.88f
                drawRoundRect(
                    color = color,
                    topLeft = Offset(cx - dr, cy - dr),
                    size = Size(2f * dr, 2f * dr),
                    cornerRadius = CornerRadius(3.5.dp.toPx(), 3.5.dp.toPx())
                )
            }
        }
        2 -> {
            // Circle
            drawCircle(
                color = color,
                radius = r,
                center = center
            )
        }
        3 -> {
            // Horizontal Capsule / Pill
            val w = r * 2.3f
            val h = r * 1.5f
            drawRoundRect(
                color = color,
                topLeft = Offset(cx - w / 2f, cy - h / 2f),
                size = Size(w, h),
                cornerRadius = CornerRadius(h / 2f, h / 2f)
            )
        }
        4 -> {
            // Hexagon
            val hexPath = createHexagonPath(cx, cy, r)
            drawPath(path = hexPath, color = color)
        }
        else -> {
            // 4-Point Star
            val starPath = createStarPath(cx, cy, r, r * 0.40f)
            drawPath(path = starPath, color = color)
        }
    }
}

/**
 * One Material 3 Expressive dot of the PIN pad:
 * When empty, it rests as a subtle circular dot.
 * When a digit is entered, it springs and morphs into a distinct Material 3 Expressive
 * geometric shape (Squircle, Diamond, Circle, Pill, Hexagon, 4-Point Star) with bouncy physics.
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
        targetValue = if (filled) 1.15f else 0.80f,
        animationSpec = bouncy,
        label = "m3_dot_scale"
    )

    val rotation by animateFloatAsState(
        targetValue = if (filled) 0f else -30f,
        animationSpec = bouncy,
        label = "m3_dot_rotation"
    )

    val filledAlpha by animateFloatAsState(
        targetValue = if (filled) 1f else 0f,
        animationSpec = tween(durationMillis = 150),
        label = "m3_dot_filled_alpha"
    )

    val emptyAlpha by animateFloatAsState(
        targetValue = if (!filled) 1f else 0f,
        animationSpec = tween(durationMillis = 150),
        label = "m3_dot_empty_alpha"
    )

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
                    color = colors.textPrimary.copy(alpha = 0.20f * emptyAlpha),
                    radius = this.size.minDimension * 0.20f,
                    center = center
                )
            }
            if (filledAlpha > 0f) {
                drawExpressiveShape(
                    index = index,
                    color = colors.textPrimary.copy(alpha = filledAlpha)
                )
            }
        }
    }
}
