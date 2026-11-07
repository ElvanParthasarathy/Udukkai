package com.elvan.udukkai.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

private val RECORD_KEY = Regex("^(?:item|cust|invoice|receipt)_([0-9]+)$")

/** The record id of a list key like "item_12"; null for date headers, spacers and pairs. */
internal fun recordIdOfKey(key: Any): Long? = RECORD_KEY.find(key.toString())?.groupValues?.get(1)?.toLongOrNull()

/**
 * Samsung-style multi-select: press and hold a card and, without lifting the finger, slide up or down: every card it
 * passes over is selected. Works from the plain list too, since [onSelect] starts selecting (it must add, never toggle).
 * The touch is watched before the cards see it (initial pass), so their own press handling cannot swallow the hold.
 */
@Composable
fun Modifier.dragSelect(listState: LazyListState, onSelect: (Long) -> Unit): Modifier {
    val select by rememberUpdatedState(onSelect)
    return pointerInput(listState) {
        val edge = 150.dp.toPx()
        val maxStep = 16.dp.toPx()
        coroutineScope {
            val scope = this
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                // Held still for the long-press time? A quick lift or a move first means an ordinary tap or scroll.
                val movedOrLifted = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                    while (true) {
                        val change = awaitPointerEvent(PointerEventPass.Initial).changes.firstOrNull { it.id == down.id } ?: return@withTimeoutOrNull true
                        if (change.changedToUp() || (change.position - down.position).getDistance() > viewConfiguration.touchSlop) return@withTimeoutOrNull true
                    }
                    @Suppress("UNREACHABLE_CODE") false
                }
                if (movedOrLifted == true) return@awaitEachGesture
                val seen = HashSet<Long>()
                var lastIndex = -1
                var fingerY = down.position.y
                // Every card between the last one and this one, so a fast slide never skips a card
                fun pick() {
                    val items = listState.layoutInfo.visibleItemsInfo
                    val hit = items.firstOrNull { fingerY >= it.offset && fingerY < it.offset + it.size } ?: return
                    val from = if (lastIndex < 0) hit.index else minOf(lastIndex, hit.index)
                    val to = if (lastIndex < 0) hit.index else maxOf(lastIndex, hit.index)
                    items.filter { it.index in from..to }.forEach { item ->
                        recordIdOfKey(item.key)?.let { if (seen.add(it)) select(it) }
                    }
                    lastIndex = hit.index
                }
                pick()
                // Near the top or bottom the list moves by itself, faster the closer the finger is to the edge
                val autoScroll = scope.launch {
                    while (true) {
                        delay(16)
                        val height = listState.layoutInfo.viewportEndOffset.toFloat()
                        val step = when {
                            fingerY > height - edge -> maxStep * ((fingerY - (height - edge)) / edge).coerceAtMost(1f)
                            fingerY < edge -> -maxStep * ((edge - fingerY) / edge).coerceAtMost(1f)
                            else -> 0f
                        }
                        if (step != 0f) {
                            listState.scrollBy(step)
                            pick()
                        }
                    }
                }
                try {
                    while (true) {
                        val change = awaitPointerEvent(PointerEventPass.Initial).changes.firstOrNull { it.id == down.id } ?: break
                        if (change.changedToUp()) break
                        fingerY = change.position.y
                        pick()
                        change.consume() // the finger selects here, it must not also scroll the list
                    }
                } finally {
                    autoScroll.cancel()
                }
            }
        }
    }
}
