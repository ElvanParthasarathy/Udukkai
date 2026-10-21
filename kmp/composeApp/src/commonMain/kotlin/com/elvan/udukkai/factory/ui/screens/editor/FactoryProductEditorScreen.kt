package com.elvan.udukkai.factory.ui.screens.editor

import androidx.compose.ui.Alignment
import com.elvan.udukkai.ui.screens.editor.ElvanEditorBottomBar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.platform.AppBackHandler
import com.elvan.udukkai.data.model.ProductModel
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
 * Distinct Factory labor schema: Process Name ONLY (Coolie job-work name).
 * Reactively follows FactoryProfileRepository.profile.isBilingual for bilingual toggling.
 */
@Composable
fun FactoryProductEditorScreen(
    item: ProductModel? = null,
    onBack: () -> Unit
) {
    val colors = rememberShellColors()
    val ff = LocalAppFontFamily.current
    val isEditing = item != null && item.id > 0L

    val profile = FactoryProfileRepository.profile
    val allProfiles = FactoryProfileRepository.profiles
    val activeCompanyId = profile.id
    val isBilingual = profile.isBilingual
    val primaryLang = profile.primaryLanguage.ifEmpty { "ta" }
    val secondaryLang = profile.secondaryLanguage.ifEmpty { "en" }

    var productNameMap by remember { mutableStateOf(item?.name ?: emptyMap()) }

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

    val hasChanges = remember(productNameMap, companyChanged) {
        if (item == null) {
            productNameMap.values.any { it.isNotBlank() } || companyChanged
        } else {
            productNameMap != item.name || companyChanged
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

    val pageTitle = if (isEditing) K.editProduct.tr() else K.createProduct.tr()
    val nameRequiredMsg = K.productNameRequired.tr()
    val savedMsg = K.productSavedSuccessfully.tr()
    val saveFailedMsg = K.couldNotSavePrefix.tr()

    fun handleSave() {
        val hasName = productNameMap.values.any { it.trim().isNotBlank() }
        if (!hasName) {
            validationError = nameRequiredMsg
            return
        }

        val companyIds = selectedCompanyOption.id?.let { listOf(it) } ?: emptyList()

        val itemToSave = ProductModel(
            id = item?.id ?: 0L,
            name = productNameMap.filterValues { it.isNotBlank() },
            hsnCode = "",
            price = 0.0,
            taxRate = 0.0,
            quantityType = "",
            unit = "",
            companyIds = companyIds,
            createdAt = item?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val newId = FactoryProductRepository.save(itemToSave)
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
                                    label = "${K.productName.tr()} *",
                                    value = productNameMap,
                                    onChanged = {
                                        productNameMap = it
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
