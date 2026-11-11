package com.elvan.udukkai.ui.screens.editor

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class BilingualFieldTest {
    @Test fun togglingAndSwappingLanguagesPreservesBothTypedValues() = runComposeUiTest {
        val bilingual = mutableStateOf(true)
        val primary = mutableStateOf("ta")
        val values = mutableStateOf(mapOf("ta" to "தமிழ் முகவரி", "en" to "English address"))
        setContent {
            CompositionLocalProvider(com.elvan.udukkai.theme.LocalShellColors provides com.elvan.udukkai.theme.rememberShellColors()) {
                ElvanIrumozhiPulan("Address", values.value, { values.value = it },
                    isBilingual = bilingual.value, primaryLang = primary.value,
                    secondaryLang = if (primary.value == "ta") "en" else "ta")
            }
        }
        onNodeWithText("தமிழ் முகவரி").assertExists()
        onNodeWithText("English address").assertExists()
        runOnIdle { bilingual.value = false }
        waitForIdle()
        onNodeWithText("English address").assertDoesNotExist()
        onNodeWithText("தமிழ் முகவரி").assertExists()
        runOnIdle { primary.value = "en" }
        waitForIdle()
        onNodeWithText("English address").assertExists()
        onNodeWithText("தமிழ் முகவரி").assertDoesNotExist()
        runOnIdle { bilingual.value = true }
        waitForIdle()
        onNodeWithText("தமிழ் முகவரி").assertExists()
        onNodeWithText("English address").assertExists()
        runOnIdle { assertEquals(mapOf("ta" to "தமிழ் முகவரி", "en" to "English address"), values.value) }
    }
}
