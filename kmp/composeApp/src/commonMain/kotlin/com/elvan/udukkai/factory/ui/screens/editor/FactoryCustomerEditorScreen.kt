package com.elvan.udukkai.factory.ui.screens.editor

import androidx.compose.ui.Alignment
import com.elvan.udukkai.ui.screens.editor.ElvanEditorBottomBar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.platform.AppBackHandler
import com.elvan.udukkai.data.model.CustomerModel
import com.elvan.udukkai.factory.data.FactoryCustomerRepository
import com.elvan.udukkai.factory.data.FactoryProfileRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.screens.editor.ElvanEditorSection
import com.elvan.udukkai.ui.screens.editor.ElvanIrumozhiPulan
import com.elvan.udukkai.ui.screens.editor.ElvanEditorTextField
import com.elvan.udukkai.ui.screens.editor.LocalEditorAccentColor

/**
 * Dedicated Customer Editor for Udukkai Factory (ஆலை).
 * Distinct Factory customer schema: Name, City, Address, Phone.
 * Reactively follows FactoryProfileRepository.profile.isBilingual for bilingual toggling.
 */
@Composable
fun FactoryCustomerEditorScreen(
    merchant: CustomerModel? = null,
    onBack: () -> Unit
) {
    val colors = rememberShellColors()
    val ff = LocalAppFontFamily.current
    val isEditing = merchant != null && merchant.id > 0L

    val profile = FactoryProfileRepository.profile
    val isBilingual = profile.isBilingual
    val primaryLang = profile.primaryLanguage.ifEmpty { "ta" }
    val secondaryLang = profile.secondaryLanguage.ifEmpty { "en" }

    var nameMap by remember { mutableStateOf(merchant?.name ?: emptyMap()) }
    var phone by remember { mutableStateOf(merchant?.phone ?: "") }
    var cityMap by remember { mutableStateOf(merchant?.city ?: emptyMap()) }
    var addressMap by remember { mutableStateOf(merchant?.address ?: emptyMap()) }

    var nameValidationError by remember { mutableStateOf<String?>(null) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var isExiting by remember { mutableStateOf(false) }

    val hasChanges = remember(nameMap, phone, cityMap, addressMap) {
        if (merchant == null) {
            nameMap.values.any { it.isNotBlank() } || phone.isNotBlank() ||
                cityMap.values.any { it.isNotBlank() } || addressMap.values.any { it.isNotBlank() }
        } else {
            nameMap != merchant.name || phone != merchant.phone ||
                cityMap != merchant.city || addressMap != merchant.address
        }
    }

    val handleBackAttempt: () -> Unit = {
        if (!isExiting) {
            if (hasChanges) {
                showDiscardDialog = true
            } else {
                isExiting = true
                onBack()
            }
        }
    }

    AppBackHandler(enabled = !isExiting) {
        handleBackAttempt()
    }

    val pageTitle = if (isEditing) K.editCustomer.tr() else K.createCustomer.tr()
    val saveSuccessMsg = K.customerSavedSuccessfully.tr()
    val fillNameMsg = K.customerNameRequired.tr()
    val saveFailedMsg = K.couldNotSavePrefix.tr()

    fun handleSave() {
        val hasName = nameMap.values.any { it.trim().isNotBlank() }
        if (!hasName) {
            nameValidationError = fillNameMsg
            return
        }

        val merchantToSave = CustomerModel(
            id = merchant?.id ?: 0L,
            name = nameMap.filterValues { it.isNotBlank() },
            phone = phone.trim(),
            address = addressMap.filterValues { it.isNotBlank() },
            city = cityMap.filterValues { it.isNotBlank() },
            createdAt = merchant?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val newId = FactoryCustomerRepository.save(merchantToSave)
        if (newId > 0L) {
            isExiting = true
            ElvanSnackbar.show(saveSuccessMsg)
            onBack()
        } else {
            ElvanSnackbar.show("$saveFailedMsg $fillNameMsg")
        }
    }

    val scrollState = rememberLazyListState()

    ElvanSubShell(
        title = pageTitle,
        onBack = handleBackAttempt,
        scrollState = scrollState,
        hasActions = false,
        actions = {},
        floatingBar = {
            ElvanEditorBottomBar(
                label = K.saveBtn.tr(),
                onClick = { handleSave() },
                accentColor = colors.customerColor
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            CompositionLocalProvider(LocalEditorAccentColor provides colors.customerColor) {
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = Dimens.SubpageContentPaddingBottom + 72.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                item(key = "top_spacer") {
                    Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
                }

                // Section 1: Customer Details (வணிகர் பெயர் & தொலைபேசி)
                item(key = "customer_details_section") {
                    ElvanEditorSection(
                        index = 0,
                        title = K.businessDetails.tr()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ElvanIrumozhiPulan(
                                label = K.customerName.tr(),
                                value = nameMap,
                                onChanged = {
                                    nameMap = it
                                    nameValidationError = null
                                },
                                isBilingual = isBilingual,
                                primaryLang = primaryLang,
                                secondaryLang = secondaryLang,
                                placeholder = K.customerName.tr()
                            )

                            if (nameValidationError != null) {
                                Text(
                                    text = nameValidationError!!,
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.error
                                    ),
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }

                            ElvanEditorTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = K.phone.tr(),
                                placeholder = "+91 98765 43210"
                            )
                        }
                    }
                }

                // Section 2: Address (முகவரி & ஊர்)
                item(key = "address_section") {
                    ElvanEditorSection(
                        index = 1,
                        title = K.address.tr()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ElvanIrumozhiPulan(
                                label = K.city.tr(),
                                value = cityMap,
                                onChanged = { cityMap = it },
                                isBilingual = isBilingual,
                                primaryLang = primaryLang,
                                secondaryLang = secondaryLang,
                                placeholder = K.city.tr()
                            )

                            ElvanIrumozhiPulan(
                                label = K.address.tr(),
                                value = addressMap,
                                onChanged = { addressMap = it },
                                isBilingual = isBilingual,
                                primaryLang = primaryLang,
                                secondaryLang = secondaryLang,
                                maxLines = 4,
                                minLines = 2,
                                placeholder = K.address.tr()
                            )
                        }
                    }
                }
            }
        }
    }
    }

    if (showDiscardDialog && !isExiting) {
        ElvanDiscardChangesDialog(
            onKeepEditing = { showDiscardDialog = false },
            onDiscard = {
                isExiting = true
                showDiscardDialog = false
                onBack()
            }
        )
    }
}
