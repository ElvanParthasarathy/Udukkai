package com.elvan.udukkai.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import com.elvan.udukkai.theme.LocalShellColors
import com.elvan.udukkai.theme.rememberShellColors

@OptIn(ExperimentalTestApi::class)
class EqualHeightCardRowTest {
    @Test fun wrappedAndOptionalLinesKeepBothCardSurfacesLevel() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, 1.3f), LocalShellColors provides rememberShellColors()) {
                EqualHeightCardRow(Modifier.requiredWidth(500.dp)) {
                    EqualHeightCard {
                        ElvanCommonCard(Modifier.testTag("short")) { Text("Short name") }
                    }
                    EqualHeightCard {
                        ElvanCommonCard(Modifier.testTag("tall")) {
                            Column {
                                Text("A longer company or item name that wraps onto several lines")
                                Text("Optional second language and details")
                            }
                        }
                    }
                }
            }
        }
        val short = onNodeWithTag("short").fetchSemanticsNode().boundsInRoot
        val tall = onNodeWithTag("tall").fetchSemanticsNode().boundsInRoot
        assertEquals(short.top, tall.top)
        assertEquals(short.bottom, tall.bottom)
        assertEquals(short.width, tall.width)
        assertTrue(short.height > 70f)
    }
}
