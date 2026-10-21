package com.elvan.udukkai.ui.screens.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.data.*
import com.elvan.udukkai.core.platform.AppBackHandler
import com.elvan.udukkai.data.model.CustomerModel
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.LanguageManager
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*

/**
 * Unified, reusable Customer Editor screen for Udukkai (shared across Textiles and Factory).
 *
 * Supports:
 * - Dynamic GST field display ([showGst] = true for Textiles, optional toggle for Factory)
 * - Country, State, and District dropdowns with built-in "Custom" / "தனிப்பயன்" text entry
 * - Full bilingual capability reacting to the active profile's primary and secondary languages
 * - Unsaved changes detection and discard confirmation dialog
 */
@Composable
fun CustomerEditorScreen(
    merchant: CustomerModel? = null,
    showGst: Boolean = true,
    isBilingual: Boolean = false,
    primaryLang: String = "ta",
    secondaryLang: String = "en",
    accentColor: Color? = null,
    onSave: (CustomerModel) -> Long,
    onBack: () -> Unit
) {
    val colors = rememberShellColors()
    val ff = LocalAppFontFamily.current
    val isEditing = merchant != null && merchant.id > 0L
    val editorAccent = accentColor ?: colors.customerColor

    var nameMap by remember { mutableStateOf(merchant?.name ?: emptyMap()) }
    var gstin by remember { mutableStateOf(merchant?.gstin ?: "") }
    var phone by remember { mutableStateOf(merchant?.phone ?: "") }
    var email by remember { mutableStateOf(merchant?.email ?: "") }

    var countryMap by remember { mutableStateOf(merchant?.country ?: mapOf("en" to "India", "ta" to "இந்தியா")) }
    var stateMap by remember { mutableStateOf(merchant?.state ?: mapOf("en" to "Tamil Nadu", "ta" to "தமிழ்நாடு")) }
    var districtMap by remember { mutableStateOf(merchant?.district ?: emptyMap()) }
    var cityMap by remember { mutableStateOf(merchant?.city ?: emptyMap()) }
    var addressMap by remember { mutableStateOf(merchant?.address ?: emptyMap()) }
    var pinCode by remember { mutableStateOf(merchant?.pinCode ?: "") }

    var showGstField by remember { mutableStateOf(showGst || merchant?.gstin?.isNotBlank() == true) }

    val initialPresetCountry = remember(merchant) {
        val c = merchant?.country
        if (c.isNullOrEmpty()) worldCountries.first()
        else worldCountries.filter { it.en != "Custom" && it.ta != "தனிப்பயன்" }
            .firstOrNull { it.en.equals(c["en"], ignoreCase = true) || it.ta == c["ta"] }
    }
    var isCustomCountry by remember {
        mutableStateOf(merchant != null && merchant.country.isNotEmpty() && initialPresetCountry == null)
    }

    val initialPresetState = remember(merchant) {
        val s = merchant?.state
        if (s.isNullOrEmpty()) indianStates.first()
        else indianStates.filter { it.en != "Custom" && it.ta != "தனிப்பயன்" }
            .firstOrNull { it.en.equals(s["en"], ignoreCase = true) || it.ta == s["ta"] }
    }
    var isCustomState by remember {
        mutableStateOf(merchant != null && merchant.state.isNotEmpty() && initialPresetState == null)
    }

    val initialPresetDistrict = remember(merchant) {
        val d = merchant?.district
        if (d.isNullOrEmpty()) null
        else tamilNaduDistricts.filter { it.en != "Custom" && it.ta != "தனிப்பயன்" }
            .firstOrNull { it.en.equals(d["en"], ignoreCase = true) || it.ta == d["ta"] }
    }
    var isCustomDistrict by remember {
        mutableStateOf(merchant != null && merchant.district.isNotEmpty() && initialPresetDistrict == null)
    }

    var nameValidationError by remember { mutableStateOf<String?>(null) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var isExiting by remember { mutableStateOf(false) }

    val isIndia = remember(countryMap, isCustomCountry) {
        if (isCustomCountry) false else {
            val en = countryMap["en"]?.lowercase() ?: ""
            val ta = countryMap["ta"] ?: ""
            en == "india" || ta == "இந்தியா"
        }
    }

    val isTamilNadu = remember(stateMap, isIndia, isCustomState) {
        if (!isIndia || isCustomState) false else {
            val en = stateMap["en"]?.lowercase() ?: ""
            val ta = stateMap["ta"] ?: ""
            en.contains("tamil nadu") || ta.contains("தமிழ்நாடு")
        }
    }

    val hasChanges = remember(
        nameMap, gstin, phone, email, countryMap, stateMap, districtMap,
        cityMap, addressMap, pinCode, isCustomCountry, isCustomState, isCustomDistrict
    ) {
        if (merchant == null) {
            nameMap.values.any { it.isNotBlank() } || gstin.isNotBlank() || phone.isNotBlank() ||
                email.isNotBlank() ||
                countryMap != mapOf("en" to "India", "ta" to "இந்தியா") ||
                stateMap != mapOf("en" to "Tamil Nadu", "ta" to "தமிழ்நாடு") ||
                districtMap.values.any { it.isNotBlank() } ||
                cityMap.values.any { it.isNotBlank() } ||
                addressMap.values.any { it.isNotBlank() } ||
                pinCode.isNotBlank()
        } else {
            nameMap != merchant.name || gstin != merchant.gstin || phone != merchant.phone ||
                email != merchant.email || countryMap != merchant.country || stateMap != merchant.state ||
                districtMap != merchant.district || cityMap != merchant.city || addressMap != merchant.address ||
                pinCode != merchant.pinCode
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
            email = email.trim(),
            gstin = gstin.trim().uppercase(),
            address = addressMap.filterValues { it.isNotBlank() },
            city = cityMap.filterValues { it.isNotBlank() },
            district = districtMap.filterValues { it.isNotBlank() },
            state = stateMap.filterValues { it.isNotBlank() },
            country = countryMap.filterValues { it.isNotBlank() },
            pinCode = pinCode.trim(),
            createdAt = merchant?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val newId = onSave(merchantToSave)
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
                accentColor = editorAccent
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            CompositionLocalProvider(LocalEditorAccentColor provides editorAccent) {
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

                    // Section 1: Customer Details
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
                                    placeholder = "+91 98765 43210",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                                )

                                ElvanEditorTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    label = K.email.tr(),
                                    placeholder = "customer@example.com",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                                )

                                if (showGstField) {
                                    ElvanEditorTextField(
                                        value = gstin,
                                        onValueChange = { gstin = it.uppercase() },
                                        label = "GSTIN",
                                        placeholder = "33AAAAA0000A1Z5"
                                    )
                                } else {
                                    Text(
                                        text = "+ GSTIN",
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = colors.textSecondary
                                        ),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { showGstField = true }
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Section 2: Address
                    item(key = "address_section") {
                        ElvanEditorSection(
                            index = 1,
                            title = K.address.tr()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                val presetCountry = worldCountries.filter { it.en != "Custom" && it.ta != "தனிப்பயன்" }
                                    .firstOrNull { it.en.equals(countryMap["en"], ignoreCase = true) || it.ta == countryMap["ta"] }

                                ElvanEditorDropdown<LocationName>(
                                    label = K.country.tr(),
                                    value = if (isCustomCountry) worldCountries.last() else (presetCountry ?: worldCountries.first()),
                                    items = worldCountries,
                                    onSelected = { picked ->
                                        if (picked.en == "Custom" || picked.ta == "தனிப்பயன்") {
                                            isCustomCountry = true
                                            countryMap = emptyMap()
                                        } else {
                                            isCustomCountry = false
                                            countryMap = mapOf("en" to picked.en, "ta" to picked.ta)
                                        }
                                    },
                                    itemLabelBuilder = { if (LanguageManager.activeLanguageCode == "ta") it.ta else it.en },
                                    subtitleBuilder = { if (LanguageManager.activeLanguageCode == "ta") it.en else it.ta },
                                    showSearch = true,
                                    searchFilter = { item, query ->
                                        item.en.contains(query, ignoreCase = true) || item.ta.contains(query, ignoreCase = true)
                                    }
                                )

                                if (isCustomCountry) {
                                    ElvanIrumozhiPulan(
                                        label = "${K.country.tr()} (${K.customBtn.tr()})",
                                        value = countryMap,
                                        onChanged = { countryMap = it },
                                        isBilingual = isBilingual,
                                        primaryLang = primaryLang,
                                        secondaryLang = secondaryLang,
                                        placeholder = K.country.tr()
                                    )
                                }

                                if (isIndia) {
                                    val presetState = indianStates.filter { it.en != "Custom" && it.ta != "தனிப்பயன்" }
                                        .firstOrNull { it.en.equals(stateMap["en"], ignoreCase = true) || it.ta == stateMap["ta"] }

                                    ElvanEditorDropdown<LocationName>(
                                        label = K.state.tr(),
                                        value = if (isCustomState) indianStates.last() else (presetState ?: indianStates.first()),
                                        items = indianStates,
                                        onSelected = { picked ->
                                            if (picked.en == "Custom" || picked.ta == "தனிப்பயன்") {
                                                isCustomState = true
                                                stateMap = emptyMap()
                                                districtMap = emptyMap()
                                            } else {
                                                isCustomState = false
                                                stateMap = mapOf("en" to picked.en, "ta" to picked.ta)
                                                if (picked.en != "Tamil Nadu" && picked.ta != "தமிழ்நாடு") {
                                                    districtMap = emptyMap()
                                                }
                                            }
                                        },
                                        itemLabelBuilder = { if (LanguageManager.activeLanguageCode == "ta") it.ta else it.en },
                                        subtitleBuilder = { if (LanguageManager.activeLanguageCode == "ta") it.en else it.ta },
                                        showSearch = true,
                                        searchFilter = { item, query ->
                                            item.en.contains(query, ignoreCase = true) || item.ta.contains(query, ignoreCase = true)
                                        }
                                    )

                                    if (isCustomState) {
                                        ElvanIrumozhiPulan(
                                            label = "${K.state.tr()} (${K.customBtn.tr()})",
                                            value = stateMap,
                                            onChanged = { stateMap = it },
                                            isBilingual = isBilingual,
                                            primaryLang = primaryLang,
                                            secondaryLang = secondaryLang,
                                            placeholder = K.state.tr()
                                        )
                                    }

                                    if (isTamilNadu) {
                                        val presetDistrict = tamilNaduDistricts.filter { it.en != "Custom" && it.ta != "தனிப்பயன்" }
                                            .firstOrNull { it.en.equals(districtMap["en"], ignoreCase = true) || it.ta == districtMap["ta"] }

                                        ElvanEditorDropdown<LocationName>(
                                            label = K.district.tr(),
                                            value = if (isCustomDistrict) tamilNaduDistricts.last() else presetDistrict,
                                            items = tamilNaduDistricts,
                                            onSelected = { picked ->
                                                if (picked.en == "Custom" || picked.ta == "தனிப்பயன்") {
                                                    isCustomDistrict = true
                                                    districtMap = emptyMap()
                                                } else {
                                                    isCustomDistrict = false
                                                    districtMap = mapOf("en" to picked.en, "ta" to picked.ta)
                                                }
                                            },
                                            itemLabelBuilder = { if (LanguageManager.activeLanguageCode == "ta") it.ta else it.en },
                                            subtitleBuilder = { if (LanguageManager.activeLanguageCode == "ta") it.en else it.ta },
                                            showSearch = true,
                                            searchFilter = { item, query ->
                                                item.en.contains(query, ignoreCase = true) || item.ta.contains(query, ignoreCase = true)
                                            }
                                        )

                                        if (isCustomDistrict) {
                                            ElvanIrumozhiPulan(
                                                label = "${K.district.tr()} (${K.customBtn.tr()})",
                                                value = districtMap,
                                                onChanged = { districtMap = it },
                                                isBilingual = isBilingual,
                                                primaryLang = primaryLang,
                                                secondaryLang = secondaryLang,
                                                placeholder = K.district.tr()
                                            )
                                        }
                                    } else {
                                        ElvanIrumozhiPulan(
                                            label = K.district.tr(),
                                            value = districtMap,
                                            onChanged = { districtMap = it },
                                            isBilingual = isBilingual,
                                            primaryLang = primaryLang,
                                            secondaryLang = secondaryLang,
                                            placeholder = K.district.tr()
                                        )
                                    }
                                } else {
                                    ElvanIrumozhiPulan(
                                        label = K.state.tr(),
                                        value = stateMap,
                                        onChanged = { stateMap = it },
                                        isBilingual = isBilingual,
                                        primaryLang = primaryLang,
                                        secondaryLang = secondaryLang,
                                        placeholder = K.state.tr()
                                    )

                                    ElvanIrumozhiPulan(
                                        label = K.district.tr(),
                                        value = districtMap,
                                        onChanged = { districtMap = it },
                                        isBilingual = isBilingual,
                                        primaryLang = primaryLang,
                                        secondaryLang = secondaryLang,
                                        placeholder = K.district.tr()
                                    )
                                }

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

                                ElvanEditorTextField(
                                    value = pinCode,
                                    onValueChange = { pinCode = it },
                                    label = K.pincode.tr(),
                                    placeholder = "638001",
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
