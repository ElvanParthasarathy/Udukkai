package com.elvan.udukkai.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.withTimeoutOrNull

private val RECORD_KEY = Regex("^(?:item|cust|invoice|receipt)_([0-9]+)$")

/** The record id of a list key like "item_12"; null for date headers, spacers and pairs. */
internal fun recordIdOfKey(key: Any): Long? = RECORD_KEY.find(key.toString())?.groupValues?.get(1)?.toLongOrNull()

/**
 * Samsung-style multi-select: press and hold a card and, without lifting the finger, slide up or down: every card it
 * passes over is selected. Works from the plain list too, since [onSelect] starts selecting (it must add, never toggle).
 * The touch is watched before the cards see it (initial pass), so their own press handling cannot swallow the hold.
 * shortcut: only cards on screen are reached, the list does not scroll by itself at the edges; add auto-scroll if lists get long.
 */
@Composable
fun Modifier.dragSelect(listState: LazyListState, onSelect: (Long) -> Unit): Modifier {
    val select by rememberUpdatedState(onSelect)
    return pointerInput(listState) {
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
            fun pick(y: Float) {
                val item = listState.layoutInfo.visibleItemsInfo.firstOrNull { y >= it.offset && y < it.offset + it.size } ?: return
                val id = recordIdOfKey(item.key) ?: return
                if (seen.add(id)) select(id)
            }
            pick(down.position.y)
            while (true) {
                val change = awaitPointerEvent(PointerEventPass.Initial).changes.firstOrNull { it.id == down.id } ?: break
                if (change.changedToUp()) break
                pick(change.position.y)
                change.consume() // the list must not scroll while selecting
            }
        }
    }
}
