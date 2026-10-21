package com.elvan.udukkai.textiles.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.core.data.LocationName
import com.elvan.udukkai.core.data.indianStates
import com.elvan.udukkai.core.data.tamilNaduDistricts
import com.elvan.udukkai.textiles.data.TextilesProfileRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*

/**
 * Address Settings Screen matching Flutter's `mugavari_thirai.dart` 1:1.
 * Dynamically renders Coolie (`kooli_mugavari.dart`) or Silk (`pattu_mugavari.dart`)
 * fields and accordion editing with pure Senthamizh localization.
 */
@Composable
fun TextilesAddressSettingsScreen(
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors(),
    customProfile: com.elvan.udukkai.data.settings.BusinessProfile? = null,
    onUpdateProfile: ((com.elvan.udukkai.data.settings.BusinessProfile) -> Unit)? = null,
    isTextiles: Boolean = false
) {
    val isPattu = isTextiles
    val profile = customProfile ?: if (isTextiles) com.elvan.udukkai.textiles.data.TextilesProfileRepository.profile else com.elvan.udukkai.factory.data.FactoryProfileRepository.profile
    val isBilingual = profile.isBilingual

    var editingSection by remember { mutableStateOf<String?>(null) }
    var tempPrimary by remember { mutableStateOf("") }
    var tempSecondary by remember { mutableStateOf("") }

    val stateTitle = K.state.tr()
    val districtTitle = K.district.tr()
    val primaryLangLabel = if (profile.primaryLanguage.lowercase().startsWith("ta")) K.taCode.tr() else K.enCode.tr()
    val secondaryLangLabel = if (profile.secondaryLanguage.lowercase().startsWith("ta")) K.taCode.tr() else K.enCode.tr()
    val saveSuccessMsg = K.profileSaved.tr()

    val bottomSheet = LocalElvanBottomSheetController.current

    fun beginEdit(section: String, primary: String, secondary: String = "") {
        editingSection = section
        tempPrimary = primary
        tempSecondary = secondary
    }

    val openStateSelection = {
        bottomSheet.showSelection(
            title = stateTitle,
            items = indianStates,
            currentValue = indianStates.find {
                it.ta == profile.getPrimary("state") || it.en == profile.getPrimary("state")
            } ?: if (profile.getPrimary("state").isNotBlank()) indianStates.lastOrNull() else null,
            itemLabelBuilder = { if (profile.primaryLanguage == "ta") it.ta else it.en },
            subtitleBuilder = { if (isBilingual) (if (profile.primaryLanguage == "ta") it.en else it.ta) else null },
            showSearch = true,
            searchFilter = { item, query ->
                item.ta.contains(query, ignoreCase = true) || item.en.contains(query, ignoreCase = true)
            },
            onSelected = { stateItem ->
                if (stateItem.en == "Custom" || stateItem.ta == "தனிப்பயன்") {
                    val currP = profile.getPrimary("state")
                    val currS = profile.getSecondary("state")
                    val isAlreadyPreset = indianStates.any { (it.ta == currP || it.en == currP) && it.en != "Custom" && it.ta != "தனிப்பயன்" }
                    val initialP = if (isAlreadyPreset || currP == "Custom" || currP == "தனிப்பயன்") "" else currP
                    val initialS = if (isAlreadyPreset || currS == "Custom" || currS == "தனிப்பயன்") "" else currS
                    beginEdit("maanilam", initialP, initialS)
                } else {
                    val p = if (profile.primaryLanguage == "ta") stateItem.ta else stateItem.en
                    val s = if (profile.primaryLanguage == "ta") stateItem.en else stateItem.ta
                    val updated = profile.copy()
                    updated.setBilingual("maanilam", profile.primaryLanguage, p)
                    updated.setBilingual("maanilam", profile.secondaryLanguage, s)
                    TextilesProfileRepository.updateProfile(updated)
                    editingSection = null
                    ElvanSnackbar.show(saveSuccessMsg)
                }
            }
        )
    }

    val openDistrictSelection = {
        bottomSheet.showSelection(
            title = districtTitle,
            items = tamilNaduDistricts,
            currentValue = tamilNaduDistricts.find {
                it.ta == profile.getPrimary("district") || it.en == profile.getPrimary("district")
            } ?: if (profile.getPrimary("district").isNotBlank()) tamilNaduDistricts.lastOrNull() else null,
            itemLabelBuilder = { if (profile.primaryLanguage == "ta") it.ta else it.en },
            subtitleBuilder = { if (isBilingual) (if (profile.primaryLanguage == "ta") it.en else it.ta) else null },
            showSearch = true,
            searchFilter = { item, query ->
                item.ta.contains(query, ignoreCase = true) || item.en.contains(query, ignoreCase = true)
            },
            onSelected = { districtItem ->
                if (districtItem.en == "Custom" || districtItem.ta == "தனிப்பயன்") {
                    val currP = profile.getPrimary("district")
                    val currS = profile.getSecondary("district")
                    val isAlreadyPreset = tamilNaduDistricts.any { (it.ta == currP || it.en == currP) && it.en != "Custom" && it.ta != "தனிப்பயன்" }
                    val initialP = if (isAlreadyPreset || currP == "Custom" || currP == "தனிப்பயன்") "" else currP
                    val initialS = if (isAlreadyPreset || currS == "Custom" || currS == "தனிப்பயன்") "" else currS
                    beginEdit("maavattam", initialP, initialS)
                } else {
                    val p = if (profile.primaryLanguage == "ta") districtItem.ta else districtItem.en
                    val s = if (profile.primaryLanguage == "ta") districtItem.en else districtItem.ta
                    val updated = profile.copy()
                    updated.setBilingual("maavattam", profile.primaryLanguage, p)
                    updated.setBilingual("maavattam", profile.secondaryLanguage, s)
                    TextilesProfileRepository.updateProfile(updated)
                    editingSection = null
                    ElvanSnackbar.show(saveSuccessMsg)
                }
            }
        )
    }

    fun saveBilingual(fieldName: String) {
        val updated = profile.copy()
        updated.setBilingual(fieldName, profile.primaryLanguage, tempPrimary)
        updated.setBilingual(fieldName, profile.secondaryLanguage, tempSecondary)
        TextilesProfileRepository.updateProfile(updated)
        editingSection = null
        ElvanSnackbar.show(saveSuccessMsg)
    }

    fun saveSingle(action: (String) -> Unit) {
        val updated = profile.copy()
        action(tempPrimary)
        TextilesProfileRepository.updateProfile(updated)
        editingSection = null
        ElvanSnackbar.show(saveSuccessMsg)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Dimens.ContentPadding,
            end = Dimens.ContentPadding,
            bottom = Dimens.SubpageContentPaddingBottom
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
    ) {
        // Top spacer driven by One UI collapsible header
        item(key = "shell_top_spacer") {
            Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
        }

        item {
            ElvanSettingsSection(colors = colors) {
                if (isPattu) {
                    // ── Pattu Order ──
                    // 1. Country (India - locked)
                    val countryPrimary = profile.getPrimary("country")
                    val countrySecondary = profile.getSecondary("country")
                    ElvanSettingsDisplayRow(
                        title = K.country.tr(),
                        primaryValue = countryPrimary,
                        secondaryValue = if (isBilingual) countrySecondary else null,
                        onEdit = null,
                        colors = colors
                    )
                    ElvanSettingsDivider(colors = colors)

                    // 2. State (Maanilam)
                    val statePrimary = profile.getPrimary("state")
                    val stateSecondary = profile.getSecondary("state")
                    ElvanSettingsAnimatedExpand(
                        isEditing = editingSection == "maanilam",
                        displayContent = {
                            ElvanSettingsDisplayRow(
                                title = K.state.tr(),
                                primaryValue = statePrimary,
                                secondaryValue = if (isBilingual) stateSecondary else null,
                                onEdit = openStateSelection,
                                colors = colors
                            )
                        },
                        editContent = {
                            ElvanSettingsEditContainer(
                                title = K.state.tr(),
                                onCancel = { editingSection = null },
                                onSave = { saveBilingual("maanilam") },
                                colors = colors
                            ) {
                                ElvanSettingsTextField(
                                    label = "${K.state.tr()} ($primaryLangLabel)",
                                    value = tempPrimary,
                                    onValueChange = { tempPrimary = it },
                                    colors = colors
                                )
                                if (isBilingual) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    ElvanSettingsTextField(
                                        label = "${K.state.tr()} ($secondaryLangLabel)",
                                        value = tempSecondary,
                                        onValueChange = { tempSecondary = it },
                                        colors = colors
                                    )
                                }
                            }
                        }
                    )
                    ElvanSettingsDivider(colors = colors)

                    // 3. District (Maavattam)
                    val districtPrimary = profile.getPrimary("district")
                    val districtSecondary = profile.getSecondary("district")
                    val isTamilNadu = statePrimary.trim() in listOf("Tamil Nadu", "தமிழ்நாடு") || stateSecondary.trim() in listOf("Tamil Nadu", "தமிழ்நாடு")

                    ElvanSettingsAnimatedExpand(
                        isEditing = editingSection == "maavattam",
                        displayContent = {
                            ElvanSettingsDisplayRow(
                                title = K.district.tr(),
                                primaryValue = districtPrimary,
                                secondaryValue = if (isBilingual) districtSecondary else null,
                                onEdit = if (isTamilNadu) openDistrictSelection else { { beginEdit("maavattam", districtPrimary, districtSecondary) } },
                                colors = colors
                            )
                        },
                        editContent = {
                            ElvanSettingsEditContainer(
                                title = K.district.tr(),
                                onCancel = { editingSection = null },
                                onSave = { saveBilingual("maavattam") },
                                colors = colors
                            ) {
                                ElvanSettingsTextField(
                                    label = "${K.district.tr()} ($primaryLangLabel)",
                                    value = tempPrimary,
                                    onValueChange = { tempPrimary = it },
                                    colors = colors
                                )
                                if (isBilingual) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    ElvanSettingsTextField(
                                        label = "${K.district.tr()} ($secondaryLangLabel)",
                                        value = tempSecondary,
                                        onValueChange = { tempSecondary = it },
                                        colors = colors
                                    )
                                }
                            }
                        }
                    )
                    ElvanSettingsDivider(colors = colors)

                    // 4. City (Oor)
                    val cityPrimary = profile.getPrimary("city")
                    val citySecondary = profile.getSecondary("city")
                    ElvanSettingsAnimatedExpand(
                        isEditing = editingSection == "oor",
                        displayContent = {
                            ElvanSettingsDisplayRow(
                                title = K.city.tr(),
                                primaryValue = cityPrimary,
                                secondaryValue = if (isBilingual) citySecondary else null,
                                onEdit = { beginEdit("oor", cityPrimary, citySecondary) },
                                colors = colors
                            )
                        },
                        editContent = {
                            ElvanSettingsEditContainer(
                                title = K.city.tr(),
                                onCancel = { editingSection = null },
                                onSave = { saveBilingual("oor") },
                                colors = colors
                            ) {
                                ElvanSettingsTextField(
                                    label = "${K.city.tr()} ($primaryLangLabel)",
                                    value = tempPrimary,
                                    onValueChange = { tempPrimary = it },
                                    colors = colors
                                )
                                if (isBilingual) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    ElvanSettingsTextField(
                                        label = "${K.city.tr()} ($secondaryLangLabel)",
                                        value = tempSecondary,
                                        onValueChange = { tempSecondary = it },
                                        colors = colors
                                    )
                                }
                            }
                        }
                    )
                    ElvanSettingsDivider(colors = colors)

                    // 5. Address (Mugavari)
                    val addressPrimary = profile.getPrimary("address")
                    val addressSecondary = profile.getSecondary("address")
                    ElvanSettingsAnimatedExpand(
                        isEditing = editingSection == "mugavari",
                        displayContent = {
                            ElvanSettingsDisplayRow(
                                title = K.address.tr(),
                                primaryValue = addressPrimary,
                                secondaryValue = if (isBilingual) addressSecondary else null,
                                onEdit = { beginEdit("mugavari", addressPrimary, addressSecondary) },
                                colors = colors
                            )
                        },
                        editContent = {
                            ElvanSettingsEditContainer(
                                title = K.address.tr(),
                                onCancel = { editingSection = null },
                                onSave = { saveBilingual("mugavari") },
                                colors = colors
                            ) {
                                ElvanSettingsTextField(
                                    label = "${K.address.tr()} ($primaryLangLabel)",
                                    value = tempPrimary,
                                    onValueChange = { tempPrimary = it },
                                    singleLine = false,
                                    minLines = 2,
                                    maxLines = 6,
                                    colors = colors
                                )
                                if (isBilingual) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    ElvanSettingsTextField(
                                        label = "${K.address.tr()} ($secondaryLangLabel)",
                                        value = tempSecondary,
                                        onValueChange = { tempSecondary = it },
                                        singleLine = false,
                                        minLines = 2,
                                        maxLines = 6,
                                        colors = colors
                                    )
                                }
                            }
                        }
                    )
                    ElvanSettingsDivider(colors = colors)

                    // 6. PIN Code (Anjal Kuriyeedu)
                    val pin = profile.pinCode
                    ElvanSettingsAnimatedExpand(
                        isEditing = editingSection == "pinCode",
                        displayContent = {
                            ElvanSettingsDisplayRow(
                                title = K.pincode.tr(),
                                primaryValue = pin,
                                onEdit = { beginEdit("pinCode", pin) },
                                colors = colors
                            )
                        },
                        editContent = {
                            ElvanSettingsEditContainer(
                                title = K.pincode.tr(),
                                onCancel = { editingSection = null },
                                onSave = { saveSingle { profile.pinCode = it } },
                                colors = colors
                            ) {
                                ElvanSettingsTextField(
                                    label = K.pincode.tr(),
                                    value = tempPrimary,
                                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) tempPrimary = it },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    maxLength = 6,
                                    colors = colors
                                )
                            }
                        }
                    )
                } else {
                    // ── Kooli Order ──
                    // 1. Address (Mugavari)
                    val addressPrimary = profile.getPrimary("address")
                    val addressSecondary = profile.getSecondary("address")
                    ElvanSettingsAnimatedExpand(
                        isEditing = editingSection == "mugavari",
                        displayContent = {
                            ElvanSettingsDisplayRow(
                                title = K.address.tr(),
                                primaryValue = addressPrimary,
                                secondaryValue = addressSecondary,
                                onEdit = { beginEdit("mugavari", addressPrimary, addressSecondary) },
                                colors = colors
                            )
                        },
                        editContent = {
                            ElvanSettingsEditContainer(
                                title = K.address.tr(),
                                onCancel = { editingSection = null },
                                onSave = { saveBilingual("mugavari") },
                                colors = colors
                            ) {
                                ElvanSettingsTextField(
                                    label = "${K.address.tr()} ($primaryLangLabel)",
                                    value = tempPrimary,
                                    onValueChange = { tempPrimary = it },
                                    singleLine = false,
                                    minLines = 2,
                                    maxLines = 6,
                                    colors = colors
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                ElvanSettingsTextField(
                                    label = "${K.address.tr()} ($secondaryLangLabel)",
                                    value = tempSecondary,
                                    onValueChange = { tempSecondary = it },
                                    singleLine = false,
                                    minLines = 2,
                                    maxLines = 6,
                                    colors = colors
                                )
                            }
                        }
                    )
                    ElvanSettingsDivider(colors = colors)

                    // 2. City (Oor)
                    val cityPrimary = profile.getPrimary("city")
                    val citySecondary = profile.getSecondary("city")
                    ElvanSettingsAnimatedExpand(
                        isEditing = editingSection == "oor",
                        displayContent = {
                            ElvanSettingsDisplayRow(
                                title = K.city.tr(),
                                primaryValue = cityPrimary,
                                secondaryValue = citySecondary,
                                onEdit = { beginEdit("oor", cityPrimary, citySecondary) },
                                colors = colors
                            )
                        },
                        editContent = {
                            ElvanSettingsEditContainer(
                                title = K.city.tr(),
                                onCancel = { editingSection = null },
                                onSave = { saveBilingual("oor") },
                                colors = colors
                            ) {
                                ElvanSettingsTextField(
                                    label = "${K.city.tr()} ($primaryLangLabel)",
                                    value = tempPrimary,
                                    onValueChange = { tempPrimary = it },
                                    colors = colors
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                ElvanSettingsTextField(
                                    label = "${K.city.tr()} ($secondaryLangLabel)",
                                    value = tempSecondary,
                                    onValueChange = { tempSecondary = it },
                                    colors = colors
                                )
                            }
                        }
                    )
                    ElvanSettingsDivider(colors = colors)

                    // 3. District (Maavattam)
                    val districtPrimary = profile.getPrimary("district")
                    val districtSecondary = profile.getSecondary("district")
                    ElvanSettingsAnimatedExpand(
                        isEditing = editingSection == "maavattam",
                        displayContent = {
                            ElvanSettingsDisplayRow(
                                title = K.district.tr(),
                                primaryValue = districtPrimary,
                                secondaryValue = districtSecondary,
                                onEdit = openDistrictSelection,
                                colors = colors
                            )
                        },
                        editContent = {
                            ElvanSettingsEditContainer(
                                title = K.district.tr(),
                                onCancel = { editingSection = null },
                                onSave = { saveBilingual("maavattam") },
                                colors = colors
                            ) {
                                ElvanSettingsTextField(
                                    label = "${K.district.tr()} ($primaryLangLabel)",
                                    value = tempPrimary,
                                    onValueChange = { tempPrimary = it },
                                    colors = colors
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                ElvanSettingsTextField(
                                    label = "${K.district.tr()} ($secondaryLangLabel)",
                                    value = tempSecondary,
                                    onValueChange = { tempSecondary = it },
                                    colors = colors
                                )
                            }
                        }
                    )
                    ElvanSettingsDivider(colors = colors)

                    // 4. PIN Code (Anjal Kuriyeedu)
                    val pin = profile.pinCode
                    ElvanSettingsAnimatedExpand(
                        isEditing = editingSection == "pinCode",
                        displayContent = {
                            ElvanSettingsDisplayRow(
                                title = K.pincode.tr(),
                                primaryValue = pin,
                                onEdit = { beginEdit("pinCode", pin) },
                                colors = colors
                            )
                        },
                        editContent = {
                            ElvanSettingsEditContainer(
                                title = K.pincode.tr(),
                                onCancel = { editingSection = null },
                                onSave = { saveSingle { profile.pinCode = it } },
                                colors = colors
                            ) {
                                ElvanSettingsTextField(
                                    label = K.pincode.tr(),
                                    value = tempPrimary,
                                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) tempPrimary = it },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    maxLength = 6,
                                    colors = colors
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
}

