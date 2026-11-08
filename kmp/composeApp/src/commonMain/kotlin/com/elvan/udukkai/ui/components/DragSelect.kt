package com.elvan.udukkai.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

private val SINGLE_KEY = Regex("^(?:item|cust|invoice|receipt)_([0-9]+)$")
private val PAIR_KEY = Regex("^(?:item|cust|inv)_pair_([0-9]+(?:_[0-9]+)*)$")

/** The record ids of a list key, left to right: "item_12" gives [12], a landscape row "item_pair_12_13" gives [12, 13]; headers and spacers give none. */
internal fun recordIdsOfKey(key: Any): List<Long> {
    val k = key.toString()
    SINGLE_KEY.find(k)?.let { return listOfNotNull(it.groupValues[1].toLongOrNull()) }
    PAIR_KEY.find(k)?.let { return it.groupValues[1].split('_').mapNotNull { s -> s.toLongOrNull() } }
    return emptyList()
}

/** Which card of a row a finger at [x] is over (0 for a one-card row). */
internal fun columnAt(cards: Int, x: Float, width: Float): Int =
    if (cards <= 1 || width <= 0f) 0 else (x / width * cards).toInt().coerceIn(0, cards - 1)

/**
 * Every card between two points in reading order (row by row, left to right), either end first.
 * [rows] maps a list index to the ids of the cards in that row; a point is (row index, card column).
 */
internal fun idsBetween(rows: Map<Int, List<Long>>, aRow: Int, aCol: Int, bRow: Int, bCol: Int): Set<Long> {
    val a = aRow.toLong() * 1000 + aCol
    val b = bRow.toLong() * 1000 + bCol
    val lo = minOf(a, b)
    val hi = maxOf(a, b)
    val out = LinkedHashSet<Long>()
    for (row in minOf(aRow, bRow)..maxOf(aRow, bRow)) {
        rows[row].orEmpty().forEachIndexed { col, id -> if (row * 1000L + col in lo..hi) out.add(id) }
    }
    return out
}

/**
 * Samsung My Files / Google Photos multi-select: hold a card and, without lifting, slide. Works from any card, as often as you like.
 * Every card between where the hold began and the finger is selected; slide back and the cards the finger leaves are
 * unselected again (a card selected earlier and swept over and left is unselected too, as in both reference apps).
 * Cards outside the slide are never touched. Near the top or bottom the list scrolls by itself.
 * The touch is watched before the cards see it (initial pass), so their own press handling cannot swallow the hold.
 * [onAdd] must add (never toggle); [onRemove] unselects one card.
 */
@Composable
fun Modifier.dragSelect(
    listState: LazyListState,
    isSelected: (Long) -> Boolean,
    onAdd: (Long) -> Unit,
    onRemove: (Long) -> Unit
): Modifier {
    val selected by rememberUpdatedState(isSelected)
    val add by rememberUpdatedState(onAdd)
    val remove by rememberUpdatedState(onRemove)
    return pointerInput(listState) {
        val edge = 150.dp.toPx()
        val maxStep = 16.dp.toPx()
        coroutineScope {
            val scope = this
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                // Held still for the long-press time? A quick lift or a move first means an ordinary tap or scroll.
                // Not pressed covers a lift and a cancel: when the phone takes the touch for its Back gesture Compose reports it
                // already consumed, so changedToUp() stays false and this timer would "hold" the card under the swipe.
                val movedOrLifted = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                    while (true) {
                        val change = awaitPointerEvent(PointerEventPass.Initial).changes.firstOrNull { it.id == down.id } ?: return@withTimeoutOrNull true
                        if (!change.pressed || (change.position - down.position).getDistance() > viewConfiguration.touchSlop) return@withTimeoutOrNull true
                    }
                    @Suppress("UNREACHABLE_CODE") false
                }
                if (movedOrLifted == true) return@awaitEachGesture

                // Ids by list index, remembered as rows scroll past so a range can span rows that have left the screen
                val rows = HashMap<Int, List<Long>>()
                var fingerX = down.position.x
                var fingerY = down.position.y
                val width = size.width.toFloat()
                fun noteRows() = listState.layoutInfo.visibleItemsInfo.forEach { rows[it.index] = recordIdsOfKey(it.key) }
                // The row under the finger; past the first or last row it is that edge row, in a gap between rows none
                fun rowAtFinger(): LazyListItemInfo? {
                    val items = listState.layoutInfo.visibleItemsInfo
                    val first = items.firstOrNull() ?: return null
                    val last = items.last()
                    return items.firstOrNull { fingerY >= it.offset && fingerY < it.offset + it.size }
                        ?: if (fingerY < first.offset) first else if (fingerY >= last.offset + last.size) last else null
                }
                noteRows()
                // Only a hold on a card starts a drag (header, filter pills and gaps do not)
                val start = rowAtFinger() ?: return@awaitEachGesture
                val startCards = rows[start.index].orEmpty()
                if (startCards.isEmpty()) return@awaitEachGesture
                val anchorCol = columnAt(startCards.size, fingerX, width)

                var applied = emptySet<Long>()
                fun pick() {
                    noteRows()
                    val row = rowAtFinger() ?: return
                    val range = idsBetween(rows, start.index, anchorCol, row.index, columnAt(rows[row.index].orEmpty().size, fingerX, width))
                    if (range == applied) return
                    (range - applied).forEach { add(it) }
                    (applied - range).filter(selected).forEach { remove(it) } // a card Back already cleared is left alone: remove toggles
                    applied = range
                }
                pick()
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
                        if (!change.pressed) break
                        fingerX = change.position.x
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
