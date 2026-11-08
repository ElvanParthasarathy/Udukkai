package com.elvan.udukkai.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

/** Each card is a 60px row and its id is the row number from 1, so y = (id - 1) * 60 + 30. Ids 3..8 stay clear of the auto-scroll edges of the 768px test window. */
@OptIn(ExperimentalTestApi::class)
class DragSelectGestureTest {
    private var selected by mutableStateOf(emptySet<Long>())
    private var ids by mutableStateOf((1L..30L).toList())
    private var recompositions by mutableStateOf(0)
    private var systemOwnsTouch = false

    private fun yOf(row: Int) = (row - 1) * 60f + 30f

    private fun ComposeUiTest.host() {
        mainClock.autoAdvance = false
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                val state = rememberLazyListState()
                val tick = recompositions // re-reads so a test can force the lambdas below to be rebuilt mid-gesture
                LazyColumn(
                    state = state,
                    modifier = Modifier.size(300.dp, 1000.dp).testTag("list")
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) awaitPointerEvent(PointerEventPass.Initial).changes.forEach { if (systemOwnsTouch) it.consume() }
                            }
                        }
                        .dragSelect(state, { it in selected }, { selected = selected + it + emptySet<Long>().also { tick } }, { selected = selected - it })
                ) {
                    items(ids, key = { "item_$it" }) { Box(Modifier.fillMaxWidth().height(60.dp)) { Text("$it") } }
                }
            }
        }
        mainClock.advanceTimeBy(100)
    }

    private fun ComposeUiTest.touch(block: androidx.compose.ui.test.TouchInjectionScope.() -> Unit) {
        onNodeWithTag("list").performTouchInput(block)
        mainClock.advanceTimeBy(32)
    }
    private fun ComposeUiTest.press(row: Int) = touch { down(Offset(150f, yOf(row))) }
    private fun ComposeUiTest.hold(row: Int) { press(row); mainClock.advanceTimeBy(700) }
    private fun ComposeUiTest.moveTo(row: Int) = touch { moveTo(Offset(150f, yOf(row))) }
    private fun ComposeUiTest.lift() = touch { up() }
    private fun ComposeUiTest.drag(from: Int, vararg to: Int) { hold(from); to.forEach { moveTo(it) }; lift() }
    /** What Compose delivers when the phone takes the touch for its Back gesture: a release already consumed (the test tool's cancel() is a no-op on desktop). */
    private fun ComposeUiTest.systemTakesTouch() { systemOwnsTouch = true; touch { up() }; systemOwnsTouch = false }
    private fun back() { selected = emptySet() }
    private fun set(vararg v: Long) = v.toSet()

    @Test fun holdAndDragSelectsTheRange() = runComposeUiTest {
        host(); drag(3, 5)
        assertEquals(set(3, 4, 5), selected)
    }

    @Test fun sameCardCanBeHeldAndDraggedAgainAfterRelease() = runComposeUiTest {
        host()
        hold(3); lift()
        assertEquals(set(3), selected)
        drag(3, 5)
        assertEquals(set(3, 4, 5), selected)
    }

    @Test fun repeatedSessionsOnTheSameCardKeepWorking() = runComposeUiTest {
        host()
        repeat(6) {
            drag(3, 6)
            assertEquals(set(3, 4, 5, 6), selected, "session $it out")
            drag(3, 6, 3)
            assertEquals(set(3), selected, "session $it back")
        }
    }

    @Test fun aDifferentCardStartsItsOwnRange() = runComposeUiTest {
        host(); drag(3, 4); drag(6, 7)
        assertEquals(set(3, 4, 6, 7), selected)
    }

    @Test fun changingDirectionPastTheStartSwitchesSides() = runComposeUiTest {
        host(); drag(5, 7, 3)
        assertEquals(set(3, 4, 5), selected)
    }

    @Test fun slidingBackUnselectsTheCardsLeft() = runComposeUiTest {
        host(); drag(3, 6, 4)
        assertEquals(set(3, 4), selected)
    }

    @Test fun backWithOneSelectedLeavesNothingSelected() = runComposeUiTest {
        host(); hold(4); lift()
        press(6); systemTakesTouch(); back()
        mainClock.advanceTimeBy(800)
        assertEquals(emptySet(), selected)
    }

    @Test fun backWithSeveralSelectedLeavesNothingSelected() = runComposeUiTest {
        host(); drag(3, 5)
        press(7); systemTakesTouch(); back()
        mainClock.advanceTimeBy(800)
        assertEquals(emptySet(), selected)
    }

    @Test fun aCancelledTouchNeverCountsAsAHold() = runComposeUiTest {
        host()
        press(5); systemTakesTouch()
        mainClock.advanceTimeBy(800)
        assertEquals(emptySet(), selected)
    }

    @Test fun holdAfterBackStartsCleanOnThatCardOnly() = runComposeUiTest {
        host(); drag(3, 5)
        press(7); systemTakesTouch(); back()
        hold(4); lift()
        assertEquals(set(4), selected)
    }

    @Test fun aCancelEndsARunningDrag() = runComposeUiTest {
        host()
        hold(3); moveTo(5); systemTakesTouch()
        mainClock.advanceTimeBy(800)
        assertEquals(set(3, 4, 5), selected)
        drag(7, 8)
        assertEquals(set(3, 4, 5, 7, 8), selected)
    }

    @Test fun backDuringADragIsNotUndoneByTheDrag() = runComposeUiTest {
        host()
        hold(3); moveTo(5); back(); moveTo(3); lift()
        assertEquals(emptySet(), selected)
    }

    @Test fun holdSelectsTheRecordThatIsThereAfterTheListChanges() = runComposeUiTest {
        host(); hold(3); lift()
        ids = ids - 4
        mainClock.advanceTimeBy(100)
        assertEquals(set(3), selected, "the list changing touches nothing")
        hold(4); lift() // row 4 now shows id 5
        assertEquals(set(3, 5), selected)
    }

    @Test fun recompositionDuringADragDoesNotBreakIt() = runComposeUiTest {
        host()
        hold(3); moveTo(4)
        recompositions++
        mainClock.advanceTimeBy(100)
        moveTo(6); lift()
        assertEquals(set(3, 4, 5, 6), selected)
    }
}
