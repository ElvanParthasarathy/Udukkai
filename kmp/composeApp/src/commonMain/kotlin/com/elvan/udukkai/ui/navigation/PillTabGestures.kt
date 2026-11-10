package com.elvan.udukkai.ui.navigation

import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import kotlin.math.abs

/** New touches take over immediately, including while another finger is still down. */
internal suspend fun PointerInputScope.trackPillTabs(
    vertical: Boolean,
    onStart: (Float) -> Unit,
    onDrag: (Float) -> Unit,
    onEnd: (Boolean) -> Unit,
    onCancel: () -> Unit
) {
    try {
        awaitPointerEventScope {
            while (true) {
                val down = awaitPointerEvent().changes.lastOrNull { it.changedToDownIgnoreConsumed() } ?: continue
                var pointer = down.id
                var origin = if (vertical) down.position.y else down.position.x
                var dragging = false
                onStart(origin)
                while (true) {
                    val event = awaitPointerEvent()
                    val newer = event.changes.lastOrNull { it.changedToDownIgnoreConsumed() }
                    if (newer != null) {
                        pointer = newer.id
                        origin = if (vertical) newer.position.y else newer.position.x
                        dragging = false
                        onStart(origin)
                    }
                    val change = event.changes.firstOrNull { it.id == pointer }
                    if (change == null) { onCancel(); break }
                    if (!change.pressed) { onEnd(dragging); break }
                    val position = if (vertical) change.position.y else change.position.x
                    if (dragging || abs(position - origin) > viewConfiguration.touchSlop) {
                        dragging = true
                        onDrag(position)
                        change.consume()
                    }
                }
            }
        }
    } finally { onCancel() }
}
