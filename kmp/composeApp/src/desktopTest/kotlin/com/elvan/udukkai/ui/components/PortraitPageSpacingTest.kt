package com.elvan.udukkai.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.ui.components.shell.*
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class PortraitPageSpacingTest {
    @Test fun rootCardStartIsIndependentOfListSpacingAndBarClearanceUpdates() = runComposeUiTest {
        val state = LazyListState()
        val spacing = mutableStateOf(16)
        val hasBar = mutableStateOf(true)
        setContent {
            CompositionLocalProvider(
                LocalDensity provides Density(1f),
                LocalElvanScrollState provides state,
                LocalElvanTopSpacerHeight provides 100.dp,
                LocalPortraitPageBottom provides Dimens.portraitBottomPadding(hasBar.value, 24.dp)
            ) {
                ElvanLazyColumn(Modifier.requiredSize(320.dp, 600.dp), state,
                    contentPadding = PaddingValues(bottom = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(spacing.value.dp)) {
                    item("shell_top_spacer") { Spacer(Modifier.height(106.dp)) }
                    item("first_card") { Box(Modifier.size(100.dp)) }
                }
            }
        }
        runOnIdle {
            assertEquals(116, state.layoutInfo.visibleItemsInfo.first { it.key == "first_card" }.offset)
            assertEquals(103, state.layoutInfo.afterContentPadding)
            spacing.value = 12
            hasBar.value = false
        }
        waitForIdle()
        runOnIdle {
            assertEquals(116, state.layoutInfo.visibleItemsInfo.first { it.key == "first_card" }.offset)
            assertEquals(36, state.layoutInfo.afterContentPadding)
        }
    }

    @Test fun nestedListKeepsItsOwnSpacing() = runComposeUiTest {
        val root = LazyListState()
        val nested = LazyListState()
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f),
                LocalElvanScrollState provides root, LocalElvanTopSpacerHeight provides 100.dp,
                LocalPortraitPageBottom provides 103.dp) {
                ElvanLazyColumn(Modifier.requiredSize(320.dp,600.dp), nested,
                    contentPadding = PaddingValues(bottom=9.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
                    item("top_spacer") { Spacer(Modifier.height(20.dp)) }
                    item("card") { Box(Modifier.size(100.dp)) }
                }
            }
        }
        runOnIdle {
            assertEquals(32, nested.layoutInfo.visibleItemsInfo.first { it.key == "card" }.offset)
            assertEquals(9, nested.layoutInfo.afterContentPadding)
        }
    }
}
