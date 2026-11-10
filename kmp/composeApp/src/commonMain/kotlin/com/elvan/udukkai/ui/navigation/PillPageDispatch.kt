package com.elvan.udukkai.ui.navigation

import androidx.compose.runtime.State
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs

/** One pending page change. Input and pill motion never wait for the page callback. */
internal class PillPageDispatch(private val scope: CoroutineScope) {
    private var pending: Job? = null

    fun cancel() { pending?.cancel(); pending = null }

    fun select(position: State<Float>, landing: Float, onSelected: () -> Unit) {
        cancel()
        pending = scope.launch {
            // Two consecutive quiet frames avoid mistaking a pill passing this tab for a landing.
            val started = withFrameNanos { it }
            var previous = position.value
            var quietFrames = 0
            while (quietFrames < 2) {
                val now = withFrameNanos { it }
                val current = position.value
                quietFrames = if (abs(current - landing) < 2f && abs(current - previous) < 0.5f) quietFrames + 1 else 0
                previous = current
                if (now - started >= 700_000_000L) break
            }
            // Let the final pill frame reach the renderer before composing the destination page.
            withFrameNanos { }
            onSelected()
        }
    }
}
