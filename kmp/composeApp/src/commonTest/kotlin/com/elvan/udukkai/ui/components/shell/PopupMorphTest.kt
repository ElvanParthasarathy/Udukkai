package com.elvan.udukkai.ui.components.shell

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import kotlin.test.Test
import kotlin.test.assertEquals

class PopupMorphTest {
    private val button = Rect(200f, 10f, 250f, 60f)
    private val panel = Size(260f, 400f)

    @Test fun startsAsExactlyTheButton() = assertEquals(button, morphRect(button, panel, 0f))

    @Test fun endsAsTheWholePanel() = assertEquals(Rect(0f, 0f, 260f, 400f), morphRect(button, panel, 1f))

    @Test fun neverLeavesThePanelAndAlwaysContainsTheButtonOnTheWay() {
        for (i in 0..10) {
            val r = morphRect(button, panel, i / 10f)
            assert(r.left >= 0f && r.top >= 0f && r.right <= panel.width && r.bottom <= panel.height) { "$i: $r" }
            assert(r.left <= button.left && r.top <= button.top && r.right >= button.right && r.bottom >= button.bottom) { "$i: $r" }
        }
    }

    @Test fun panelStaysOnScreen() {
        assertEquals(8, clampToScreen(-50, 200, 1000, 8))
        assertEquals(792, clampToScreen(900, 200, 1000, 8))
        assertEquals(300, clampToScreen(300, 200, 1000, 8))
        assertEquals(8, clampToScreen(300, 1200, 1000, 8)) // wider than the screen starts at the margin
    }
}
