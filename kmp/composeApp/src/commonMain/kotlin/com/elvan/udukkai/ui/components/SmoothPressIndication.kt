package com.elvan.udukkai.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import kotlin.math.hypot

/**
 * A slow, soft press ripple for cards: a gentle circle grows from the touch point over about half a second and then
 * fades away on its own, instead of the quick flash of the stock ripple. Draw it inside a clipped card.
 */
class SmoothPressIndication(private val color: Color, private val peakAlpha: Float = 0.16f) : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode = Node(interactionSource, color, peakAlpha)

    override fun equals(other: Any?) = other is SmoothPressIndication && other.color == color && other.peakAlpha == peakAlpha
    override fun hashCode() = color.hashCode() * 31 + peakAlpha.hashCode()

    private class Node(
        private val source: InteractionSource,
        private val color: Color,
        private val peakAlpha: Float
    ) : Modifier.Node(), DrawModifierNode {
        private val radius = Animatable(0f)
        private val alpha = Animatable(0f)
        private var center = Offset.Zero
        private var lastSize = androidx.compose.ui.geometry.Size.Zero

        override fun onAttach() {
            coroutineScope.launch {
                source.interactions.collect { interaction ->
                    when (interaction) {
                        is PressInteraction.Press -> {
                            center = interaction.pressPosition
                            val far = hypot(lastSize.width.toDouble(), lastSize.height.toDouble()).toFloat()
                            launch { radius.snapTo(0f); radius.animateTo(far, tween(520, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f))) }
                            launch { alpha.snapTo(0f); alpha.animateTo(peakAlpha, tween(160)) }
                        }
                        is PressInteraction.Release, is PressInteraction.Cancel ->
                            launch { alpha.animateTo(0f, tween(420, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f))) }
                    }
                }
            }
        }

        override fun ContentDrawScope.draw() {
            lastSize = size
            drawContent()
            if (alpha.value > 0.001f) drawCircle(color = color.copy(alpha = alpha.value), radius = radius.value, center = center)
        }
    }
}
