package com.elvan.udukkai.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class PillNavigationInputTest {
    @Test fun portraitRapidTapsKeepOnlyTheLatestPageChange() = runComposeUiTest {
        mainClock.autoAdvance = false
        val selected = mutableStateOf<NavTab?>(NavTab.Home)
        val taps = mutableListOf<NavTab>()
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                Box(Modifier.requiredSize(900.dp, 400.dp)) {
                    BottomNavBar(selected.value, { tab, _ -> selected.value = tab; taps += tab }, modifier = Modifier.testTag("nav"))
                }
            }
        }
        mainClock.advanceTimeByFrame()
        onNodeWithTag("nav").performTouchInput {
            val left = center.x - 130f
            down(Offset(left + 2.5f * 65f, 24f)); up()
            advanceEventTime(8)
            down(Offset(left + 1.5f * 65f, 24f)); up()
        }
        runOnIdle { assertEquals(emptyList(), taps) }
        mainClock.advanceTimeBy(1200)
        waitForIdle()
        runOnIdle { assertEquals(listOf(NavTab.Create), taps) }
    }

    @Test fun landscapeAcceptsNewTouchesWhileAnOlderFingerIsStillDown() = runComposeUiTest {
        mainClock.autoAdvance = false
        val selected = mutableStateOf<NavTab?>(NavTab.Home)
        val taps = mutableListOf<NavTab>()
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                Box(Modifier.requiredSize(900.dp, 400.dp)) {
                    LandscapeSideNavBar(selected.value, { tab, _ -> selected.value = tab; taps += tab }, modifier = Modifier.testTag("nav"))
                }
            }
        }
        mainClock.advanceTimeByFrame()
        onNodeWithTag("nav").performTouchInput {
            down(0, Offset(40f, 39f))
            down(1, Offset(40f, 163f)); up(1)
            advanceEventTime(8)
            down(2, Offset(40f, 101f)); up(2)
            up(0)
        }
        runOnIdle { assertEquals(emptyList(), taps) }
        mainClock.advanceTimeBy(1200)
        waitForIdle()
        runOnIdle { assertEquals(listOf(NavTab.Create), taps) }
    }
    @Test fun latePageArrivalDoesNotOverrideTheNextLandscapeTap() = runComposeUiTest {
        mainClock.autoAdvance = false
        val arrivedPage = mutableStateOf<NavTab?>(NavTab.Home)
        val requestedPages = mutableListOf<NavTab>()
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                Box(Modifier.requiredSize(900.dp, 400.dp)) {
                    LandscapeSideNavBar(arrivedPage.value, { tab, _ -> requestedPages += tab }, modifier = Modifier.testTag("nav"))
                }
            }
        }
        mainClock.advanceTimeByFrame()
        onNodeWithTag("nav").performTouchInput { down(Offset(40f, 101f)); up() }
        mainClock.advanceTimeBy(1200)
        runOnIdle { assertEquals(listOf(NavTab.Create), requestedPages) }
        onNodeWithTag("nav").performTouchInput { down(Offset(40f, 163f)); up() }
        // The preceding page arrives while the next tap is already being handled.
        runOnIdle { arrivedPage.value = NavTab.Create }
        mainClock.advanceTimeBy(650)
        runOnIdle { assertEquals(listOf(NavTab.Create, NavTab.Items), requestedPages) }
    }

    @Test fun latePageArrivalDoesNotOverrideTheNextPortraitTap() = runComposeUiTest {
        mainClock.autoAdvance = false
        val arrivedPage = mutableStateOf<NavTab?>(NavTab.Home)
        val requestedPages = mutableListOf<NavTab>()
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                Box(Modifier.requiredSize(900.dp, 400.dp)) {
                    BottomNavBar(arrivedPage.value, { tab, _ -> requestedPages += tab }, modifier = Modifier.testTag("nav"))
                }
            }
        }
        mainClock.advanceTimeByFrame()
        onNodeWithTag("nav").performTouchInput { down(Offset(center.x - 32.5f, 24f)); up() }
        mainClock.advanceTimeBy(1200)
        runOnIdle { assertEquals(listOf(NavTab.Create), requestedPages) }
        onNodeWithTag("nav").performTouchInput { down(Offset(center.x + 32.5f, 24f)); up() }
        runOnIdle { arrivedPage.value = NavTab.Create }
        mainClock.advanceTimeBy(650)
        runOnIdle { assertEquals(listOf(NavTab.Create, NavTab.Items), requestedPages) }
    }


    @Test fun makeShifterKeepsLatestTouchAcrossLatePageArrival() = runComposeUiTest {
        mainClock.autoAdvance = false
        val arrived = mutableStateOf(0)
        val requested = mutableListOf<Int>()
        setContent {
            CompositionLocalProvider(
                LocalDensity provides Density(1f),
                com.elvan.udukkai.theme.LocalShellColors provides com.elvan.udukkai.theme.rememberShellColors()
            ) {
                Box(Modifier.requiredSize(900.dp, 400.dp)) {
                    com.elvan.udukkai.ui.screens.create.components.ElvanPillShifter(
                        items = listOf(
                            com.elvan.udukkai.ui.screens.create.components.PillShifterItem("Invoices"),
                            com.elvan.udukkai.ui.screens.create.components.PillShifterItem("Receipts")
                        ),
                        selectedIndex = arrived.value,
                        onIndexSelected = { requested += it },
                        modifier = Modifier.testTag("shifter")
                    )
                }
            }
        }
        mainClock.advanceTimeByFrame()
        onNodeWithTag("shifter").performTouchInput { down(Offset(210f, 24f)); up() }
        mainClock.advanceTimeBy(1200)
        runOnIdle { assertEquals(listOf(1), requested) }
        onNodeWithTag("shifter").performTouchInput {
            down(0, Offset(210f, 24f))
            down(1, Offset(76f, 24f)); up(1)
        }
        runOnIdle { arrived.value = 1 }
        mainClock.advanceTimeBy(650)
        runOnIdle { assertEquals(listOf(1, 0), requested) }
        onNodeWithTag("shifter").performTouchInput { up(0) }
    }

    @Test fun portraitSequentialPageArrivalsKeepAcceptingNextTap() = runComposeUiTest {
        mainClock.autoAdvance = false
        val selected = mutableStateOf<NavTab?>(NavTab.Home)
        val requested = mutableListOf<NavTab>()
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                Box(Modifier.requiredSize(900.dp, 400.dp)) {
                    BottomNavBar(selected.value, { tab, _ -> selected.value = tab; requested += tab }, modifier = Modifier.testTag("nav"))
                }
            }
        }
        mainClock.advanceTimeByFrame()
        val sequence = listOf(NavTab.Create, NavTab.Home, NavTab.Customers, NavTab.Items,
            NavTab.Customers, NavTab.Create, NavTab.Home, NavTab.Create, NavTab.Home, NavTab.Create)
        val tabs = listOf(NavTab.Home, NavTab.Create, NavTab.Items, NavTab.Customers)
        sequence.forEachIndexed { index, tab ->
            onNodeWithTag("nav").performTouchInput {
                down(Offset(center.x - 130f + (tabs.indexOf(tab) + .5f) * 65f, 24f)); up()
            }
            repeat(45) {
                if (requested.size <= index) mainClock.advanceTimeByFrame()
            }
            runOnIdle { assertEquals(sequence.take(index + 1), requested) }
            // Tap again on the first frame following destination acknowledgement.
            mainClock.advanceTimeByFrame()
        }
    }
}
