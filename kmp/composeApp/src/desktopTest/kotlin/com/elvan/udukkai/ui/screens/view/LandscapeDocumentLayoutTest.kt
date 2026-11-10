package com.elvan.udukkai.ui.screens.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.theme.LocalShellColors
import com.elvan.udukkai.theme.rememberShellColors
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class LandscapeDocumentLayoutTest {
    @Test fun editorFieldsRemainReachableInShortLandscapeWorkspace() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                Box(Modifier.requiredSize(900.dp, 170.dp)) {
                    com.elvan.udukkai.ui.components.shell.ElvanLandscapeWorkspace(
                        overviewWeight = 0.45f,
                        overview = { item("identity") { Text("Name and company", Modifier.height(70.dp).testTag("identity")) } },
                        detail = {
                            items(8, key = { "field-$it" }) { index ->
                                androidx.compose.material3.TextField(value = "Field $index", onValueChange = {}, modifier = Modifier.testTag("field-$index"))
                            }
                        }
                    )
                }
            }
        }
        val identity = onNodeWithTag("identity").fetchSemanticsNode().boundsInRoot
        onAllNodes(hasScrollToIndexAction())[1].performScrollToKey("field-7")
        onNodeWithTag("field-7").assertIsDisplayed()
        assertEquals(identity, onNodeWithTag("identity").fetchSemanticsNode().boundsInRoot)
    }
    @Test fun documentSectionsKeepTheirSemanticColumnsWhileDetailsScroll() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                Box(Modifier.requiredSize(900.dp, 340.dp)) {
                    LandscapeDocPage(12.dp) {
                        item("status") { Text("Status", Modifier.height(60.dp).testTag("status")) }
                        item("customer") { Text("Customer", Modifier.height(60.dp).testTag("customer")) }
                        item("receipts") { Text("Receipt", Modifier.height(60.dp).testTag("receipt")) }
                        item("info") { Text("Document details", Modifier.height(60.dp).testTag("info")) }
                        item("lines") { Text("Items", Modifier.height(450.dp).testTag("lines")) }
                        item("totals") { Text("Totals", Modifier.height(60.dp).testTag("totals")) }
                    }
                }
            }
        }
        val customer = onNodeWithTag("customer").fetchSemanticsNode().boundsInRoot
        val info = onNodeWithTag("info").fetchSemanticsNode().boundsInRoot
        val lines = onNodeWithTag("lines").fetchSemanticsNode().boundsInRoot
        assertTrue(info.right < lines.left)
        assertTrue(customer.right < lines.left)
        onAllNodes(hasScrollToIndexAction())[1].performScrollToKey("totals")
        onNodeWithTag("totals").assertIsDisplayed()
        onAllNodes(hasScrollToIndexAction())[1].performScrollToKey("receipts")
        onNodeWithTag("receipt").assertIsDisplayed()
        assertEquals(customer, onNodeWithTag("customer").fetchSemanticsNode().boundsInRoot)
    }

    @Test fun longDocumentValuesWrapWithinTheSummaryCard() = runComposeUiTest {
        val value = "A long bank reference or document number that must remain inside this narrow column"
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, 1.3f), LocalDocLandscape provides true, LocalShellColors provides rememberShellColors()) {
                Box(Modifier.requiredWidth(320.dp).testTag("frame")) {
                    DocRow("Reference or transaction number", value)
                }
            }
        }
        val frame = onNodeWithTag("frame").fetchSemanticsNode().boundsInRoot
        val text = onNodeWithText(value).fetchSemanticsNode().boundsInRoot
        assertTrue(text.left >= frame.left && text.right <= frame.right)
        assertTrue(text.height > 35f)
    }
}
