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
import com.elvan.udukkai.data.model.VaangunarTharavuru
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
import com.elvan.udukkai.ui.screens.editor.ElvanThiruthiUlleedu
import com.elvan.udukkai.ui.screens.editor.LocalEditorAccentColor

/**
 * Dedicated Customer Editor for Udukkai Factory (ஆலை).
 * Distinct Factory customer schema: Name, City, Address, Phone.
 * Reactively follows FactoryProfileRepository.profile.iruMozhi for bilingual toggling.
 */
@Composable
fun FactoryCustomerEditorScreen(
    merchant: VaangunarTharavuru? = null,
    onBack: () -> Unit
) {
    val colors = rememberShellColors()
    val ff = LocalAppFontFamily.current
    val isEditing = merchant != null && merchant.id > 0L

    val profile = FactoryProfileRepository.profile
    val isBilingual = profile.iruMozhi
    val primaryLang = profile.mudhanMozhi.ifEmpty { "ta" }
    val secondaryLang = profile.thunaiMozhi.ifEmpty { "en" }

    var peyarMap by remember { mutableStateOf(merchant?.peyar ?: emptyMap()) }
    var tholaipaesi by remember { mutableStateOf(merchant?.tholaipaesi ?: "") }
    var oorMap by remember { mutableStateOf(merchant?.oor ?: emptyMap()) }
    var mugavariMap by remember { mutableStateOf(merchant?.mugavari ?: emptyMap()) }

    var nameValidationError by remember { mutableStateOf<String?>(null) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var isExiting by remember { mutableStateOf(false) }

    val hasChanges = remember(peyarMap, tholaipaesi, oorMap, mugavariMap) {
        if (merchant == null) {
            peyarMap.values.any { it.isNotBlank() } || tholaipaesi.isNotBlank() ||
                oorMap.values.any { it.isNotBlank() } || mugavariMap.values.any { it.isNotBlank() }
        } else {
            peyarMap != merchant.peyar || tholaipaesi != merchant.tholaipaesi ||
                oorMap != merchant.oor || mugavariMap != merchant.mugavari
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
        val hasName = peyarMap.values.any { it.trim().isNotBlank() }
        if (!hasName) {
            nameValidationError = fillNameMsg
            return
        }

        val merchantToSave = VaangunarTharavuru(
            id = merchant?.id ?: 0L,
            peyar = peyarMap.filterValues { it.isNotBlank() },
            tholaipaesi = tholaipaesi.trim(),
            minnanjal = "",
            gstin = "",
            mugavari = mugavariMap.filterValues { it.isNotBlank() },
            oor = oorMap.filterValues { it.isNotBlank() },
            maavattam = emptyMap(),
            maanilam = emptyMap(),
            naadu = emptyMap(),
            anjalKuriyeedu = "",
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
                                value = peyarMap,
                                onChanged = {
                                    peyarMap = it
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

                            ElvanThiruthiUlleedu(
                                value = tholaipaesi,
                                onValueChange = { tholaipaesi = it },
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
                                value = oorMap,
                                onChanged = { oorMap = it },
                                isBilingual = isBilingual,
                                primaryLang = primaryLang,
                                secondaryLang = secondaryLang,
                                placeholder = K.city.tr()
                            )

                            ElvanIrumozhiPulan(
                                label = K.address.tr(),
                                value = mugavariMap,
                                onChanged = { mugavariMap = it },
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
