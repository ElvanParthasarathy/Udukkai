package com.elvan.udukkai.ui.screens.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.data.model.ReceiptModel
import com.elvan.udukkai.localization.LocalAppLanguage
import com.elvan.udukkai.data.settings.BusinessProfile
import com.elvan.udukkai.theme.*
import com.elvan.udukkai.ui.screens.create.components.ReceiptCard
import kotlin.test.Test
import kotlin.test.assertFalse

@OptIn(ExperimentalTestApi::class)
class ReceiptCardLanguageTest {
    @Test fun longReceiptNumberLeavesRoomForTheDate() = runComposeUiTest {
        val receipt = ReceiptModel(
            receiptNumber = "RCP/SJPS-LONG/12345",
            receiptDate = 1_700_000_000_000,
            amount = 52_504_936.0,
            paymentMode = "vangi_maatram"
        )
        val selectionMode = mutableStateOf(false)
        setContent {
            val colors = rememberShellColors()
            CompositionLocalProvider(LocalShellColors provides colors, LocalAppLanguage provides "en") {
                Box(Modifier.width(300.dp)) {
                    ReceiptCard(0, receipt, colors, isSelectionMode = selectionMode.value)
                }
            }
        }
        for (selected in listOf(false, true)) {
            runOnIdle { selectionMode.value = selected }
            onNodeWithText("•", useUnmergedTree = true).assertExists()
            val dateLayouts = mutableListOf<TextLayoutResult>()
            onNodeWithText(DateUtils.formatEpochMillis(receipt.receiptDate), useUnmergedTree = true)
                .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(dateLayouts) }
            val layout = dateLayouts.single()
            val lastCharacter = layout.getBoundingBox(layout.layoutInput.text.length - 1)
            assertFalse(
                layout.isLineEllipsized(layout.lineCount - 1) ||
                    lastCharacter.right > layout.size.width + 1 ||
                    lastCharacter.bottom > layout.size.height + 1,
                "Date cut off with selection=$selected, size=${layout.size}, last character=$lastCharacter"
            )
            onNodeWithContentDescription("Bank Transfer", useUnmergedTree = true).assertExists()
            onNodeWithText("Bank Transfer", useUnmergedTree = true).assertDoesNotExist()
        }
    }

    @Test fun listCardUpdatesAndNeverBorrowsAHiddenTranslation() = runComposeUiTest {
        val profile = mutableStateOf(BusinessProfile(primaryLanguage = "fr", secondaryLanguage = "hi", isBilingual = true))
        val receipt = ReceiptModel(receiptNumber = "RC-123", customerName = mapOf("fr" to "French customer", "hi" to "Hindi customer"))
        setContent {
            val colors = rememberShellColors()
            CompositionLocalProvider(LocalShellColors provides colors) {
                ReceiptCard(0, receipt, colors, profile = profile.value)
            }
        }
        onNodeWithText("French customer").assertExists()
        onNodeWithText("Hindi customer").assertExists()
        runOnIdle { profile.value = profile.value.copy(primaryLanguage = "hi", secondaryLanguage = "fr", isBilingual = false) }
        waitForIdle()
        onNodeWithText("Hindi customer").assertExists()
        onNodeWithText("French customer").assertDoesNotExist()
        runOnIdle { profile.value = profile.value.copy(primaryLanguage = "de") }
        waitForIdle()
        onNodeWithText("French customer").assertDoesNotExist()
        onNodeWithText("Hindi customer").assertDoesNotExist()
    }
}
