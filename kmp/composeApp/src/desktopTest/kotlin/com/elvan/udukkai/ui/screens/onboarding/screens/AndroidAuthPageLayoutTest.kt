package com.elvan.udukkai.ui.screens.onboarding.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AndroidAuthPageLayoutTest {
    @Test fun keyboardDoesNotLiftContinueIntoTheFormViewport() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f), LocalAuthKeyboardVisible provides true) {
                Box(Modifier.requiredSize(900.dp, 170.dp)) {
                    AndroidAuthPage(header = { Text("Password") }, bottom = { Text("Continue", Modifier.testTag("continue")) }) {
                        Text("Password field", Modifier.testTag("field"))
                    }
                }
            }
        }
        onNodeWithTag("field").assertIsDisplayed()
        onNodeWithTag("continue").assertDoesNotExist()
    }
    private fun ComposeUiTest.host(height: Dp, fields: Int = 2, withHeader: Boolean = true) {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                Box(Modifier.requiredSize(900.dp, height).testTag("frame")) {
                    AndroidAuthPage(
                        header = if (withHeader) ({
                            Column {
                                Text("Logo", Modifier.testTag("heading-icon"))
                                Text("Language or login", Modifier.testTag("heading-title"))
                            }
                        }) else null,
                        bottom = { Box(Modifier.fillMaxWidth().height(56.dp).testTag("continue")) { Text("Continue") } }
                    ) {
                        repeat(fields) { index ->
                            Box(Modifier.fillMaxWidth().height(56.dp).testTag("field-$index")) { Text("Field $index") }
                        }
                    }
                }
            }
        }
    }

    @Test fun portraitContinueAlsoStaysBehindTheKeyboard() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalAuthLandscape provides false, LocalAuthKeyboardVisible provides true) {
                NeramButton(text = "Continue", onClick = {})
            }
        }
        onNodeWithText("Continue").assertDoesNotExist()
    }

    @Test fun headingIsGroupedOnTheLeftAndFormStaysOnTheRight() = runComposeUiTest {
        host(360.dp)
        val title = onNodeWithTag("heading-title").fetchSemanticsNode().boundsInRoot
        val icon = onNodeWithTag("heading-icon").fetchSemanticsNode().boundsInRoot
        val field = onNodeWithTag("field-0").fetchSemanticsNode().boundsInRoot
        assertTrue(title.right < field.left)
        assertTrue(icon.right < field.left)
        onNodeWithTag("continue").assertIsDisplayed()
    }

    @Test fun actionsRemainReachableInKeyboardSizedSpace() = runComposeUiTest {
        host(160.dp, fields = 6)
        onNodeWithTag("continue").assertIsDisplayed()
        val before = onNodeWithTag("continue").fetchSemanticsNode().boundsInRoot
        onNodeWithTag("field-5").performScrollTo().assertIsDisplayed()
        assertEquals(before, onNodeWithTag("continue").fetchSemanticsNode().boundsInRoot)
        onNodeWithTag("heading-title").assertIsDisplayed()
    }

    @Test fun aPageWithoutBrandingUsesTheFullAvailableWidth() = runComposeUiTest {
        host(360.dp, withHeader = false)
        val frame = onNodeWithTag("frame").fetchSemanticsNode().boundsInRoot
        val field = onNodeWithTag("field-0").fetchSemanticsNode().boundsInRoot
        assertEquals(frame.center.x, field.center.x, 1f)
    }

    @Test fun languageCardScrollsInsideItsFixedBounds() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                Box(Modifier.requiredSize(900.dp, 300.dp)) {
                    AndroidAuthPage(
                        header = { Text("Language", Modifier.testTag("heading")) },
                        bottom = { Box(Modifier.fillMaxWidth().height(56.dp).testTag("continue")) },
                        boundedContent = true
                    ) {
                        Column(Modifier.fillMaxSize().testTag("card").verticalScroll(rememberScrollState())) {
                            repeat(12) { Text("Language $it", Modifier.height(56.dp).testTag("language-$it")) }
                        }
                    }
                }
            }
        }
        val card = onNodeWithTag("card").fetchSemanticsNode().boundsInRoot
        val button = onNodeWithTag("continue").fetchSemanticsNode().boundsInRoot
        val heading = onNodeWithTag("heading").fetchSemanticsNode().boundsInRoot
        onNodeWithTag("language-11").performScrollTo().assertIsDisplayed()
        assertEquals(card, onNodeWithTag("card").fetchSemanticsNode().boundsInRoot)
        assertEquals(button, onNodeWithTag("continue").fetchSemanticsNode().boundsInRoot)
        assertEquals(heading, onNodeWithTag("heading").fetchSemanticsNode().boundsInRoot)
        assertTrue(card.bottom < button.top)
    }

    @Test fun portraitLanguageCardWrapsItsRowsInsideThePageScroll() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f), LocalAuthLandscape provides false) {
                Column(Modifier.requiredSize(400.dp, 800.dp).verticalScroll(rememberScrollState())) {
                    Box(Modifier.testTag("compact-card")) {
                        NeramChoiceCard(
                            choices = listOf(NeramChoice("ta", "Tamil"), NeramChoice("en", "English")),
                            selected = "ta", onSelect = {}
                        )
                    }
                }
            }
        }
        onNodeWithText("English").assertIsDisplayed()
        assertTrue(onNodeWithTag("compact-card").fetchSemanticsNode().boundsInRoot.height < 200f)
    }
}
