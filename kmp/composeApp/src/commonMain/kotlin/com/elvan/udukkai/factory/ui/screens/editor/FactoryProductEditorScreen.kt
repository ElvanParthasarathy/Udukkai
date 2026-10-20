package com.elvan.udukkai.factory.ui.screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.platform.AppBackHandler
import com.elvan.udukkai.data.model.PorulTharavuru
import com.elvan.udukkai.factory.data.FactoryProductRepository
import com.elvan.udukkai.factory.data.FactoryProfileRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.screens.editor.*

/**
 * Dedicated Product / Labor Process Editor for Udukkai Factory (ஆலை).
 * Distinct Factory labor schema: Process Name, Labor Rate, Unit of measurement.
 * Reactively follows FactoryProfileRepository.profile.iruMozhi for bilingual toggling.
 */
@Composable
fun FactoryProductEditorScreen(
    item: PorulTharavuru? = null,
    onBack: () -> Unit
) {
    val colors = rememberShellColors()
    val ff = LocalAppFontFamily.current
    val isEditing = item != null && item.id > 0L

    val profile = FactoryProfileRepository.profile
    val isBilingual = profile.iruMozhi
    val primaryLang = profile.mudhanMozhi.ifEmpty { "ta" }
    val secondaryLang = profile.thunaiMozhi.ifEmpty { "en" }

    var porulPeyarMap by remember { mutableStateOf(item?.porulPeyar ?: emptyMap()) }
    var vilai by remember {
        mutableStateOf(
            if (item != null && item.vilai > 0) {
                if (item.vilai % 1.0 == 0.0) item.vilai.toLong().toString() else item.vilai.toString()
            } else ""
        )
    }
    var alavuVagai by remember { mutableStateOf(item?.alavuVagai?.ifEmpty { "quantity" } ?: "quantity") }

    var validationError by remember { mutableStateOf<String?>(null) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    val hasChanges = remember(porulPeyarMap, vilai, alavuVagai) {
        if (item == null) {
            porulPeyarMap.values.any { it.isNotBlank() } || vilai.isNotBlank()
        } else {
            porulPeyarMap != item.porulPeyar ||
                (vilai.toDoubleOrNull() ?: 0.0) != item.vilai ||
                alavuVagai != item.alavuVagai
        }
    }

    val handleBackAttempt: () -> Unit = {
        if (hasChanges) {
            showDiscardDialog = true
        } else {
            onBack()
        }
    }

    AppBackHandler(enabled = true) {
        handleBackAttempt()
    }

    val pageTitle = if (isEditing) K.editProduct.tr() else K.createProduct.tr()
    val nameRequiredMsg = K.productNameRequired.tr()
    val savedMsg = K.productSavedSuccessfully.tr()
    val saveFailedMsg = K.couldNotSavePrefix.tr()

    fun handleSave() {
        val hasName = porulPeyarMap.values.any { it.trim().isNotBlank() }
        if (!hasName) {
            validationError = nameRequiredMsg
            return
        }

        val priceVal = vilai.toDoubleOrNull() ?: 0.0

        val itemToSave = PorulTharavuru(
            id = item?.id ?: 0L,
            porulPeyar = porulPeyarMap.filterValues { it.isNotBlank() },
            hsnCode = "",
            vilai = priceVal,
            variVeetham = 0.0,
            alavuVagai = alavuVagai,
            niruvanamIds = emptyList(),
            companyPrices = emptyMap(),
            createdAt = item?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val newId = FactoryProductRepository.save(itemToSave)
        if (newId > 0L) {
            ElvanSnackbar.show(savedMsg)
            onBack()
        } else {
            ElvanSnackbar.show("$saveFailedMsg $nameRequiredMsg")
        }
    }

    val scrollState = rememberLazyListState()

    ElvanSubShell(
        title = pageTitle,
        onBack = handleBackAttempt,
        scrollState = scrollState,
        hasActions = true,
        actions = {
            ElvanActionButton(
                label = K.saveBtn.tr(),
                onClick = { handleSave() }
            )
        }
    ) {
        CompositionLocalProvider(LocalEditorAccentColor provides colors.productColor) {
            LazyColumn(
                state = scrollState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = Dimens.SubpageContentPaddingBottom
                ),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item(key = "top_spacer") {
                    Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
                }

                // Section 1: Process Details (தொழில் / பணி விவரம்)
                item(key = "process_details") {
                    ElvanEditorSection(
                        index = 0,
                        title = K.productDetails.tr()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ElvanIrumozhiPulan(
                                label = K.productName.tr(),
                                value = porulPeyarMap,
                                onChanged = {
                                    porulPeyarMap = it
                                    validationError = null
                                },
                                isBilingual = isBilingual,
                                primaryLang = primaryLang,
                                secondaryLang = secondaryLang,
                                placeholder = K.productName.tr()
                            )

                            if (validationError != null) {
                                Text(
                                    text = validationError!!,
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.error
                                    ),
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }

                            // Measurement unit selector
                            val measureUnits = listOf(
                                "quantity" to K.measureQuantity.tr(),
                                "weight" to K.measureWeight.tr()
                            )
                            ElvanThiruthiKeezhvirivu<Pair<String, String>>(
                                label = K.unitOfMeasurement.tr(),
                                value = measureUnits.firstOrNull { it.first == alavuVagai } ?: measureUnits.first(),
                                items = measureUnits,
                                onSelected = { alavuVagai = it.first },
                                itemLabelBuilder = { it.second }
                            )

                            ElvanThiruthiUlleedu(
                                value = vilai,
                                onValueChange = { vilai = it },
                                label = K.ratePerUnit.tr(),
                                placeholder = "0.00",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDiscardDialog) {
        ElvanDiscardChangesDialog(
            onKeepEditing = { showDiscardDialog = false },
            onDiscard = {
                showDiscardDialog = false
                onBack()
            }
        )
    }
}
