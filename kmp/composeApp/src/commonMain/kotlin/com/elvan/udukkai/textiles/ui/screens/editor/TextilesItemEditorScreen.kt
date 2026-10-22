package com.elvan.udukkai.textiles.ui.screens.editor

import androidx.compose.ui.Alignment
import com.elvan.udukkai.ui.screens.editor.ElvanEditorBottomBar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.platform.AppBackHandler
import com.elvan.udukkai.data.model.ItemModel
import com.elvan.udukkai.textiles.data.TextilesItemRepository
import com.elvan.udukkai.textiles.data.TextilesProfileRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.screens.editor.*

/**
 * Dedicated Item / Finished Goods Editor for Udukkai Textiles (சவுளி).
 * Distinct Textiles item schema: Item Name, HSN Code, Tax Rate (GST %), Measure Type, Unit Price.
 * Reactively follows TextilesProfileRepository.profile.isBilingual for bilingual toggling.
 */
@Composable
fun TextilesItemEditorScreen(
    item: ItemModel? = null,
    onBack: () -> Unit
) {
    val colors = rememberShellColors()
    val ff = LocalAppFontFamily.current
    val isEditing = item != null && item.id > 0L

    val profile = TextilesProfileRepository.profile
    val allProfiles = TextilesProfileRepository.profiles
    val activeCompanyId = profile.id
    val isBilingual = profile.isBilingual
    val primaryLang = profile.primaryLanguage.ifEmpty { "ta" }
    val secondaryLang = profile.secondaryLanguage.ifEmpty { "en" }

    var itemNameMap by remember { mutableStateOf(item?.name ?: emptyMap()) }
    var hsnCode by remember { mutableStateOf(item?.hsnCode ?: "") }
    var price by remember {
        mutableStateOf(
            if (item != null && item.price > 0) {
                if (item.price % 1.0 == 0.0) item.price.toLong().toString() else item.price.toString()
            } else ""
        )
    }
    var variVeetham by remember {
        mutableStateOf(
            if (item != null) {
                if (item.taxRate % 1.0 == 0.0) item.taxRate.toLong().toString() else item.taxRate.toString()
            } else "5"
        )
    }
    var quantityType by remember { mutableStateOf(item?.quantityType?.ifEmpty { "quantity" } ?: "quantity") }

    val companyOptions = rememberCompanyOptions(
        profiles = allProfiles,
        isBilingual = isBilingual,
        primaryLang = primaryLang,
        secondaryLang = secondaryLang
    )

    var selectedCompanyOption by remember(item, companyOptions, activeCompanyId) {
        mutableStateOf(
            if (item != null && item.companyIds.isNotEmpty()) {
                val matchId = item.companyIds.first()
                companyOptions.firstOrNull { it.id == matchId }
                    ?: companyOptions.firstOrNull { it.id == null }
                    ?: companyOptions.first()
            } else if (item == null && activeCompanyId != null) {
                companyOptions.firstOrNull { it.id == activeCompanyId }
                    ?: companyOptions.firstOrNull { it.id == null }
                    ?: companyOptions.first()
            } else {
                companyOptions.firstOrNull { it.id == null } ?: companyOptions.first()
            }
        )
    }

    var validationError by remember { mutableStateOf<String?>(null) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var isExiting by remember { mutableStateOf(false) }

    val initialCompanyId = item?.companyIds?.firstOrNull()
    val currentCompanyId = selectedCompanyOption.id
    val companyChanged = item != null && initialCompanyId != currentCompanyId

    val hasChanges = remember(itemNameMap, hsnCode, price, variVeetham, quantityType, companyChanged) {
        if (item == null) {
            itemNameMap.values.any { it.isNotBlank() } || hsnCode.isNotBlank() || price.isNotBlank() || companyChanged
        } else {
            itemNameMap != item.name || hsnCode != item.hsnCode ||
                (price.toDoubleOrNull() ?: 0.0) != item.price ||
                (variVeetham.toDoubleOrNull() ?: 0.0) != item.taxRate ||
                quantityType != item.quantityType || companyChanged
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

    val pageTitle = if (isEditing) K.editItem.tr() else K.createItem.tr()
    val nameRequiredMsg = K.itemNameRequired.tr()
    val savedMsg = K.itemSavedSuccessfully.tr()
    val saveFailedMsg = K.couldNotSavePrefix.tr()

    fun handleSave() {
        val hasName = itemNameMap.values.any { it.trim().isNotBlank() }
        if (!hasName) {
            validationError = nameRequiredMsg
            return
        }

        val priceVal = price.toDoubleOrNull() ?: 0.0
        val taxVal = variVeetham.toDoubleOrNull() ?: 0.0

        val companyIds = selectedCompanyOption.id?.let { listOf(it) } ?: emptyList()

        val itemToSave = ItemModel(
            id = item?.id ?: 0L,
            name = itemNameMap.filterValues { it.isNotBlank() },
            hsnCode = hsnCode.trim(),
            price = priceVal,
            taxRate = taxVal,
            quantityType = quantityType,
            companyIds = companyIds,
            createdAt = item?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val newId = TextilesItemRepository.save(itemToSave)
        if (newId > 0L) {
            isExiting = true
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
        },
        showBottomFade = false,
        floatingBar = {}
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            CompositionLocalProvider(LocalEditorAccentColor provides colors.itemColor) {
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

                    // Section 1: Item Details
                    item(key = "item_details") {
                        ElvanEditorSection(
                            index = 0,
                            title = K.itemDetails.tr()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                ElvanIrumozhiPulan(
                                    label = "${K.itemName.tr()} *",
                                    value = itemNameMap,
                                    onChanged = {
                                        itemNameMap = it
                                        validationError = null
                                    },
                                    isBilingual = isBilingual,
                                    primaryLang = primaryLang,
                                    secondaryLang = secondaryLang,
                                    placeholder = K.itemName.tr()
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

                                if (allProfiles.isNotEmpty()) {
                                    ElvanEditorDropdown<CompanyOption>(
                                        label = K.company.tr(),
                                        value = selectedCompanyOption,
                                        items = companyOptions,
                                        onSelected = { selectedCompanyOption = it },
                                        itemLabelBuilder = { it.name },
                                        subtitleBuilder = { it.subtitle }
                                    )
                                }

                                ElvanEditorTextField(
                                    value = hsnCode,
                                    onValueChange = { hsnCode = it },
                                    label = "HSN / SAC",
                                    placeholder = "5208"
                                )

                                val measureUnits = listOf(
                                    "quantity" to K.measureQuantity.tr(),
                                    "weight" to K.measureWeight.tr()
                                )
                                ElvanEditorDropdown<Pair<String, String>>(
                                    label = K.unitOfMeasurement.tr(),
                                    value = measureUnits.firstOrNull { it.first == quantityType } ?: measureUnits.first(),
                                    items = measureUnits,
                                    onSelected = { quantityType = it.first },
                                    itemLabelBuilder = { it.second }
                                )

                                ElvanEditorTextField(
                                    value = price,
                                    onValueChange = { price = it },
                                    label = K.ratePerUnit.tr(),
                                    placeholder = "0.00",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )

                                ElvanEditorTextField(
                                    value = variVeetham,
                                    onValueChange = { variVeetham = it },
                                    label = "${K.taxRate.tr()} (%)",
                                    placeholder = "5",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
