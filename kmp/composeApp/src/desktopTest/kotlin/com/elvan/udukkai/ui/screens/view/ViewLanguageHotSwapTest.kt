package com.elvan.udukkai.ui.screens.view

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.elvan.udukkai.core.utils.NumberToWords
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.data.model.*
import com.elvan.udukkai.data.settings.BusinessProfile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ViewLanguageHotSwapTest {
    private val names = mapOf("fr" to "Primary customer", "hi" to "Secondary customer")

    private fun checkScreen(screen: @Composable (BusinessProfile) -> Unit) = runComposeUiTest {
        val profile = mutableStateOf(BusinessProfile(primaryLanguage = "fr", secondaryLanguage = "hi", isBilingual = true))
        setContent {
            CompositionLocalProvider(com.elvan.udukkai.theme.LocalShellColors provides com.elvan.udukkai.theme.rememberShellColors()) { screen(profile.value) }
        }
        onNodeWithText(names.getValue("fr")).assertExists()
        onNodeWithText(names.getValue("hi")).assertExists()
        runOnIdle { profile.value = profile.value.copy(isBilingual = false) }
        waitForIdle()
        onNodeWithText(names.getValue("fr")).assertExists()
        onNodeWithText(names.getValue("hi")).assertDoesNotExist()
        runOnIdle { profile.value = profile.value.copy(primaryLanguage = "hi", secondaryLanguage = "fr") }
        waitForIdle()
        onNodeWithText(names.getValue("hi")).assertExists()
        onNodeWithText(names.getValue("fr")).assertDoesNotExist()
        runOnIdle { profile.value = profile.value.copy(isBilingual = true) }
        waitForIdle()
        onNodeWithText(names.getValue("hi")).assertExists()
        onNodeWithText(names.getValue("fr")).assertExists()
        assertEquals(2, names.size)
    }

    @Test fun receiptUpdatesWithoutReopening() = checkScreen { profile ->
        ReceiptViewScreen(ReceiptModel(customerName = names, amount = 123.0), profile, emptyList(), onBack = {}, onEdit = {})
    }

    @Test fun invoiceUpdatesWithoutReopening() = checkScreen { profile ->
        InvoiceViewScreen(InvoiceModel(customerName = names), profile, onBack = {}, onEdit = {})
    }

    @Test fun customerUpdatesWithoutReopening() = checkScreen { profile ->
        CustomerViewScreen(CustomerModel(name = names), profile, onBack = {}, onEdit = {})
    }

    @Test fun productUpdatesWithoutReopening() = checkScreen { profile ->
        ItemViewScreen(ItemModel(name = names), profile, emptyList(), onBack = {}, onEdit = {})
    }

    @Test fun previousNamesAppearOnlyOnTheHistoryTab() = runComposeUiTest {
        val profile = mutableStateOf(BusinessProfile(primaryLanguage = "fr", secondaryLanguage = "hi", isBilingual = true))
        val item = ItemModel(name = mapOf("fr" to "Current item"),
            previousNames = listOf(ItemNameRevision(mapOf("fr" to "Previous item", "hi" to "Translated previous item"))))
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(0.5f),
                com.elvan.udukkai.localization.LocalAppLanguage provides "en",
                com.elvan.udukkai.theme.LocalShellColors provides com.elvan.udukkai.theme.rememberShellColors()) {
                ItemViewScreen(item, profile.value, emptyList(), onBack = {}, onEdit = {})
            }
        }
        onNodeWithText("Previous item").assertDoesNotExist()
        onNodeWithText("History").performClick()
        onNodeWithText("Previous item names").assertDoesNotExist()
        val primary = onNodeWithText("Previous item").fetchSemanticsNode().boundsInRoot
        val subtitle = onNodeWithText("Translated previous item").fetchSemanticsNode().boundsInRoot
        assertEquals(primary.left, subtitle.left)
        assertTrue(subtitle.top >= primary.bottom)
        onNodeWithText("Created").assertExists()
        runOnIdle { profile.value = profile.value.copy(isBilingual = false) }
        onNodeWithText("Previous item").assertExists()
        onNodeWithText("Translated previous item").assertDoesNotExist()
        runOnIdle { profile.value = profile.value.copy(primaryLanguage = "hi", secondaryLanguage = "fr") }
        onNodeWithText("Translated previous item").assertExists()
        onNodeWithText("Previous item").assertDoesNotExist()
    }

    @Test fun itemTransactionFilterUsesTheClearableDatePill() = checkTransactionDateFilter(customer = false)

    @Test fun customerTransactionFilterUsesTheClearableDatePill() = checkTransactionDateFilter(customer = true)

    private fun checkTransactionDateFilter(customer: Boolean) = runComposeUiTest {
        val today = DateUtils.todayMillis()
        val current = ViewBillRow(1, "BILL-TODAY", today, 20.0, isPaid = true)
        val older = ViewBillRow(2, "BILL-OLDER", today - 86400000L, 20.0, isPaid = false)
        val activity = ViewActivity(bills = listOf(current, older))
        val profile = BusinessProfile(primaryLanguage = "en")
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(0.4f),
                com.elvan.udukkai.localization.LocalAppLanguage provides "en",
                com.elvan.udukkai.theme.LocalShellColors provides com.elvan.udukkai.theme.rememberShellColors()) {
                if (customer) {
                    CustomerViewScreen(CustomerModel(), profile, loadHistory = { activity }, onBack = {}, onEdit = {})
                } else {
                    ItemViewScreen(ItemModel(), profile, emptyList(), loadHistory = { activity }, onBack = {}, onEdit = {})
                }
            }
        }
        onNodeWithText("Transactions").performClick()
        onNodeWithContentDescription("Filter").performClick()
        onNodeWithText("Today").performClick()
        onNodeWithText("BILL-TODAY", substring = true).assertExists()
        onNodeWithText("BILL-OLDER", substring = true).assertDoesNotExist()
        onNodeWithContentDescription("Clear").assertExists().performClick()
        onNodeWithContentDescription("Clear").assertDoesNotExist()
        onNodeWithText("BILL-OLDER", substring = true).assertExists()
    }

    @Test fun invoiceCardShowsTownAndEditorShowsCompleteSavedAddress() = runComposeUiTest {
        val customer = CustomerModel(id = 8, name = mapOf("en" to "Mill customer", "fr" to "Translated mill customer"),
            address = mapOf("en" to "10 Mill Street"), city = mapOf("en" to "Coimbatore"),
            state = mapOf("en" to "Tamil Nadu"), country = mapOf("en" to "India"), pinCode = "641001")
        val profile = BusinessProfile(primaryLanguage = "en", secondaryLanguage = "fr", isBilingual = true)
        val invoice = InvoiceModel(customerId = 8, customerAddress = customer.city,
            customPreferencesJson = InvoiceDisplayOptions(customer = CustomerSnapshot.of(customer)).toJson())
        val editor = mutableStateOf(false)
        val factory = mutableStateOf(true)
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(0.35f),
                com.elvan.udukkai.theme.LocalShellColors provides com.elvan.udukkai.theme.rememberShellColors()) {
                if (editor.value) {
                    com.elvan.udukkai.ui.screens.editor.InvoiceCustomerField(
                        customers = emptyList(), profile = profile, savedCustomer = customer, selectedCustomerId = 8,
                        onCustomerSelected = {}, onCustomerCleared = {}, onRequestAddNewCustomer = {})
                } else {
                    InvoiceViewScreen(invoice, profile, isFactory = factory.value, onBack = {}, onEdit = {})
                }
            }
        }
        val full = "10 Mill Street, Coimbatore, Tamil Nadu, 641001, India"
        val subtitle = "Translated mill customer • Coimbatore"
        onNodeWithText(subtitle).assertExists()
        onNodeWithText("10 Mill Street", substring = true).assertDoesNotExist()
        runOnIdle { factory.value = false }
        onNodeWithText(subtitle).assertExists()
        onNodeWithText("10 Mill Street", substring = true).assertDoesNotExist()
        runOnIdle { editor.value = true }
        onNodeWithText(full).assertExists()
    }

    @Test fun receiptAmountWordsHideAndSwapOnTheOpenScreen() = runComposeUiTest {
        val profile = mutableStateOf(BusinessProfile(primaryLanguage = "ta", secondaryLanguage = "en", isBilingual = true))
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(0.4f), com.elvan.udukkai.theme.LocalShellColors provides com.elvan.udukkai.theme.rememberShellColors()) {
                ReceiptViewScreen(ReceiptModel(amount = 123.0), profile.value, emptyList(), onBack = {}, onEdit = {})
            }
        }
        val tamil = NumberToWords.toTamil(123.0)
        val english = NumberToWords.toEnglish(123.0)
        onNodeWithText(tamil).assertExists()
        onNodeWithText(english).assertExists()
        runOnIdle { profile.value = profile.value.copy(isBilingual = false) }
        waitForIdle()
        onNodeWithText(tamil).assertExists()
        onNodeWithText(english).assertDoesNotExist()
        runOnIdle { profile.value = profile.value.copy(primaryLanguage = "en", secondaryLanguage = "ta") }
        waitForIdle()
        onNodeWithText(english).assertExists()
        onNodeWithText(tamil).assertDoesNotExist()
    }

    @Test fun savedInvoiceItemNamesFollowLanguageMapsAfterSwapping() = runComposeUiTest {
        val profile = mutableStateOf(BusinessProfile(primaryLanguage = "fr", secondaryLanguage = "hi", isBilingual = true))
        val invoice = InvoiceModel(itemsJson = """[{"itemName":"OLD NAME","itemNameEn":"OLD SECOND NAME","languageMap":{"fr":"French product","hi":"Hindi product"},"quantity":1,"price":10}]""")
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(0.3f), com.elvan.udukkai.theme.LocalShellColors provides com.elvan.udukkai.theme.rememberShellColors()) {
                InvoiceViewScreen(invoice, profile.value, onBack = {}, onEdit = {})
            }
        }
        onNodeWithText("French product").assertExists()
        onNodeWithText("Hindi product").assertExists()
        onNodeWithText("OLD NAME").assertDoesNotExist()
        runOnIdle { profile.value = profile.value.copy(primaryLanguage = "hi", secondaryLanguage = "fr", isBilingual = false) }
        waitForIdle()
        onNodeWithText("Hindi product").assertExists()
        onNodeWithText("French product").assertDoesNotExist()
        runOnIdle { profile.value = profile.value.copy(isBilingual = true) }
        waitForIdle()
        onNodeWithText("French product").assertExists()
        onNodeWithText("Hindi product").assertExists()
    }
}
