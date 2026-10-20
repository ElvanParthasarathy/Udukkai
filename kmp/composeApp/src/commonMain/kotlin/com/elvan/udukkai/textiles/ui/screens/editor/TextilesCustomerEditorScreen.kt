package com.elvan.udukkai.textiles.ui.screens.editor

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
import com.elvan.udukkai.core.data.*
import com.elvan.udukkai.core.platform.AppBackHandler
import com.elvan.udukkai.data.model.VaangunarTharavuru
import com.elvan.udukkai.data.settings.*
import com.elvan.udukkai.textiles.data.TextilesCustomerRepository
import com.elvan.udukkai.textiles.data.TextilesProfileRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.LanguageManager
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.screens.editor.*

/**
 * Dedicated Customer Editor for Udukkai Textiles (சவுளி).
 * Distinct Textiles customer schema: Name, GSTIN, Country, State, District, PIN, Phone, Email, Address.
 * Reactively follows TextilesProfileRepository.profile.iruMozhi for bilingual toggling.
 */
@Composable
fun TextilesCustomerEditorScreen(
    merchant: VaangunarTharavuru? = null,
    onBack: () -> Unit
) {
    val colors = rememberShellColors()
    val ff = LocalAppFontFamily.current
    val isEditing = merchant != null && merchant.id > 0L

    val profile = TextilesProfileRepository.profile
    val isBilingual = profile.iruMozhi
    val primaryLang = profile.mudhanMozhi.ifEmpty { "ta" }
    val secondaryLang = profile.thunaiMozhi.ifEmpty { "en" }

    var peyarMap by remember { mutableStateOf(merchant?.peyar ?: emptyMap()) }
    var gstin by remember { mutableStateOf(merchant?.gstin ?: "") }
    var tholaipaesi by remember { mutableStateOf(merchant?.tholaipaesi ?: "") }
    var minnanjal by remember { mutableStateOf(merchant?.minnanjal ?: "") }

    var naaduMap by remember { mutableStateOf(merchant?.naadu ?: mapOf("en" to "India", "ta" to "இந்தியா")) }
    var maanilamMap by remember { mutableStateOf(merchant?.maanilam ?: mapOf("en" to "Tamil Nadu", "ta" to "தமிழ்நாடு")) }
    var maavattamMap by remember { mutableStateOf(merchant?.maavattam ?: emptyMap()) }
    var oorMap by remember { mutableStateOf(merchant?.oor ?: emptyMap()) }
    var mugavariMap by remember { mutableStateOf(merchant?.mugavari ?: emptyMap()) }
    var anjalKuriyeedu by remember { mutableStateOf(merchant?.anjalKuriyeedu ?: "") }

    var nameValidationError by remember { mutableStateOf<String?>(null) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    val isIndia = remember(naaduMap) {
        val en = naaduMap["en"]?.lowercase() ?: ""
        val ta = naaduMap["ta"] ?: ""
        en == "india" || ta == "இந்தியா"
    }

    val isTamilNadu = remember(maanilamMap, isIndia) {
        if (!isIndia) false else {
            val en = maanilamMap["en"]?.lowercase() ?: ""
            val ta = maanilamMap["ta"] ?: ""
            en.contains("tamil nadu") || ta.contains("தமிழ்நாடு")
        }
    }

    val hasChanges = remember(peyarMap, gstin, tholaipaesi, minnanjal, naaduMap, maanilamMap, maavattamMap, oorMap, mugavariMap, anjalKuriyeedu) {
        if (merchant == null) {
            peyarMap.values.any { it.isNotBlank() } || gstin.isNotBlank() || tholaipaesi.isNotBlank()
        } else {
            peyarMap != merchant.peyar || gstin != merchant.gstin || tholaipaesi != merchant.tholaipaesi ||
                minnanjal != merchant.minnanjal || oorMap != merchant.oor || mugavariMap != merchant.mugavari
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
            minnanjal = minnanjal.trim(),
            gstin = gstin.trim().uppercase(),
            mugavari = mugavariMap.filterValues { it.isNotBlank() },
            oor = oorMap.filterValues { it.isNotBlank() },
            maavattam = maavattamMap.filterValues { it.isNotBlank() },
            maanilam = maanilamMap.filterValues { it.isNotBlank() },
            naadu = naaduMap.filterValues { it.isNotBlank() },
            anjalKuriyeedu = anjalKuriyeedu.trim(),
            createdAt = merchant?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val newId = TextilesCustomerRepository.save(merchantToSave)
        if (newId > 0L) {
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
        hasActions = true,
        actions = {
            ElvanActionButton(
                label = K.saveBtn.tr(),
                onClick = { handleSave() }
            )
        }
    ) {
        CompositionLocalProvider(LocalEditorAccentColor provides colors.customerColor) {
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

                // Section 1: Customer Details
                item(key = "customer_details") {
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
                                value = gstin,
                                onValueChange = { gstin = it.uppercase() },
                                label = K.gstin.tr(),
                                placeholder = "33ABCDE1234F1Z5"
                            )

                            ElvanThiruthiUlleedu(
                                value = tholaipaesi,
                                onValueChange = { tholaipaesi = it },
                                label = K.phone.tr(),
                                placeholder = "+91 98765 43210"
                            )

                            ElvanThiruthiUlleedu(
                                value = minnanjal,
                                onValueChange = { minnanjal = it },
                                label = K.email.tr(),
                                placeholder = "customer@example.com"
                            )
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
                            ElvanThiruthiKeezhvirivu<IdangalinPeyar>(
                                label = K.country.tr(),
                                value = ulagaNaadugal.firstOrNull { it.en.equals(naaduMap["en"], ignoreCase = true) || it.ta == naaduMap["ta"] } ?: ulagaNaadugal.first(),
                                items = ulagaNaadugal,
                                onSelected = { picked ->
                                    naaduMap = mapOf("en" to picked.en, "ta" to picked.ta)
                                },
                                itemLabelBuilder = { if (LanguageManager.activeLanguageCode == "ta") it.ta else it.en },
                                subtitleBuilder = { if (LanguageManager.activeLanguageCode == "ta") it.en else it.ta },
                                showSearch = true,
                                searchFilter = { item, query ->
                                    item.en.contains(query, ignoreCase = true) || item.ta.contains(query, ignoreCase = true)
                                }
                            )

                            if (isIndia) {
                                ElvanThiruthiKeezhvirivu<IdangalinPeyar>(
                                    label = K.state.tr(),
                                    value = indhiyaMaanilangal.firstOrNull { it.en.equals(maanilamMap["en"], ignoreCase = true) || it.ta == maanilamMap["ta"] },
                                    items = indhiyaMaanilangal,
                                    onSelected = { picked ->
                                        maanilamMap = mapOf("en" to picked.en, "ta" to picked.ta)
                                        if (picked.en != "Tamil Nadu" && picked.ta != "தமிழ்நாடு") {
                                            maavattamMap = emptyMap()
                                        }
                                    },
                                    itemLabelBuilder = { if (LanguageManager.activeLanguageCode == "ta") it.ta else it.en },
                                    subtitleBuilder = { if (LanguageManager.activeLanguageCode == "ta") it.en else it.ta },
                                    showSearch = true,
                                    searchFilter = { item, query ->
                                        item.en.contains(query, ignoreCase = true) || item.ta.contains(query, ignoreCase = true)
                                    }
                                )

                                if (isTamilNadu) {
                                    ElvanThiruthiKeezhvirivu<IdangalinPeyar>(
                                        label = K.district.tr(),
                                        value = tamizhnaattuMaavattangal.firstOrNull { it.en.equals(maavattamMap["en"], ignoreCase = true) || it.ta == maavattamMap["ta"] },
                                        items = tamizhnaattuMaavattangal,
                                        onSelected = { picked ->
                                            maavattamMap = mapOf("en" to picked.en, "ta" to picked.ta)
                                        },
                                        itemLabelBuilder = { if (LanguageManager.activeLanguageCode == "ta") it.ta else it.en },
                                        subtitleBuilder = { if (LanguageManager.activeLanguageCode == "ta") it.en else it.ta },
                                        showSearch = true,
                                        searchFilter = { item, query ->
                                            item.en.contains(query, ignoreCase = true) || item.ta.contains(query, ignoreCase = true)
                                        }
                                    )
                                }
                            }

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

                            ElvanThiruthiUlleedu(
                                value = anjalKuriyeedu,
                                onValueChange = { anjalKuriyeedu = it },
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
