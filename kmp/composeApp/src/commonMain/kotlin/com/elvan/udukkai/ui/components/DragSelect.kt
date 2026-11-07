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

/**
 * Samsung-style multi-select: once cards are being selected, press and hold a card and slide the finger up or down,
 * and every card it passes over is selected, without lifting the finger. [idOfKey] turns a list item's key into the id
 * of its record (null for headers and spacers). Cards already selected stay selected.
 * The touch is watched before the cards see it (initial pass), so their own press handling cannot swallow the hold.
 */
@Composable
fun Modifier.dragSelect(
    listState: LazyListState,
    enabled: Boolean,
    selectedIds: Set<Long>,
    onSelect: (Long) -> Unit,
    idOfKey: (Any) -> Long?
): Modifier {
    val selected by rememberUpdatedState(selectedIds)
    val select by rememberUpdatedState(onSelect)
    val idOf by rememberUpdatedState(idOfKey)
    if (!enabled) return this
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
                val id = idOf(item.key) ?: return
                if (seen.add(id) && id !in selected) select(id)
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
