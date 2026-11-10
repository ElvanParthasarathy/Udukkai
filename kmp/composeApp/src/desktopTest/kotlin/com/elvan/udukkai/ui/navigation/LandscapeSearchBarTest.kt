package com.elvan.udukkai.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.theme.rememberShellColors
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class LandscapeSearchBarTest {
    @Test fun searchRemainsCompactOnAWideScreenWithoutAnExtraCloseButton() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                Box(Modifier.requiredSize(900.dp, 350.dp), contentAlignment = Alignment.TopCenter) {
                    ElvanSearchBar(visible = true, query = "", onQueryChange = {}, onClose = {}, colors = rememberShellColors(), landscape = true)
                }
            }
        }
        mainClock.advanceTimeBy(1000)
        val field = onNode(hasSetTextAction()).fetchSemanticsNode().boundsInRoot
        assertTrue(field.left > 200f && field.right < 700f)
        assertTrue(field.top < 60f)
        onAllNodes(hasClickAction() and hasSetTextAction().not()).assertCountEquals(0)
    }
}
