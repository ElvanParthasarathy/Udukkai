package com.elvan.udukkai.ui.screens.settings.screens

import com.elvan.udukkai.data.settings.BusinessProfileStore

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
import com.elvan.udukkai.core.data.worldCountries
import com.elvan.udukkai.core.data.tamilNaduDistricts
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*

enum class AddressFieldOrder { CountryFirst, StreetFirst }

/** Shared business Settings backed by the owning app's profile store. */
@Composable
fun AddressSettingsScreen(
    store: BusinessProfileStore,
    fieldOrder: AddressFieldOrder,
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val countryFirst = fieldOrder == AddressFieldOrder.CountryFirst
    val profile = store.profile
    val isBilingual = profile.isBilingual

    var editingSection by remember { mutableStateOf<String?>(null) }
    var tempPrimary by remember { mutableStateOf("") }
    var tempSecondary by remember { mutableStateOf("") }
    var tempCustom by remember { mutableStateOf(false) }

    val countryTitle = K.country.tr()
    val uiLanguage = com.elvan.udukkai.localization.LocalAppLanguage.current
    fun uiName(value: String, options: List<com.elvan.udukkai.core.data.LocationName>) = com.elvan.udukkai.core.data.locationUiName(value, options, uiLanguage)
    val stateTitle = K.state.tr()
    val districtTitle = K.district.tr()
    val primaryLangLabel = if (profile.primaryLanguage.lowercase().startsWith("ta")) K.taCode.tr() else K.enCode.tr()
    val secondaryLangLabel = if (profile.secondaryLanguage.lowercase().startsWith("ta")) K.taCode.tr() else K.enCode.tr()
    val saveSuccessMsg = K.profileSaved.tr()

    val bottomSheet = LocalElvanBottomSheetController.current

    // A listed value as (primary, secondary) in the profile's languages; null for anything typed by hand or "Custom"
    fun listed(options: List<com.elvan.udukkai.core.data.LocationName>, typed: String): Pair<String, String>? {
        val taFirst = profile.primaryLanguage == "ta"
        val hit = options.firstOrNull { it.en != "Custom" && it.ta != "தனிப்பயன்" && (if (taFirst) it.ta else it.en) == typed } ?: return null
        return if (taFirst) hit.ta to hit.en else hit.en to hit.ta
    }

    fun beginEdit(section: String, primary: String, secondary: String = "", custom: Boolean = false) {
        tempCustom = custom
        editingSection = section
        tempPrimary = primary
        tempSecondary = secondary
    }

    fun persistProfile(updated: com.elvan.udukkai.data.settings.BusinessProfile) {
        store.updateProfile(updated)
    }

    // The list as a bottom sheet for the edit panel: a pick fills the two fields, "Custom" makes both typeable
    fun pickFromList(title: String, items: List<com.elvan.udukkai.core.data.LocationName>) {
        val taFirst = profile.primaryLanguage == "ta"
        bottomSheet.showSelection(
            title = title,
            items = items,
            currentValue = if (tempCustom) items.firstOrNull { it.en == "Custom" } else items.find { (if (taFirst) it.ta else it.en) == tempPrimary },
            itemLabelBuilder = { if (uiLanguage == "ta") it.ta else it.en },
            subtitleBuilder = { if (isBilingual) (if (uiLanguage == "ta") it.en else it.ta) else null },
            showSearch = true,
            searchFilter = { item, query ->
                item.ta.contains(query, ignoreCase = true) || item.en.contains(query, ignoreCase = true)
            },
            onSelected = { item ->
                if (item.en == "Custom" || item.ta == "தனிப்பயன்") {
                    if (!tempCustom) { tempPrimary = ""; tempSecondary = "" }
                    tempCustom = true
                } else {
                    tempPrimary = if (taFirst) item.ta else item.en
                    tempSecondary = if (taFirst) item.en else item.ta
                    tempCustom = false
                }
            }
        )
    }

    val openStateSelection = {
        bottomSheet.showSelection(
            title = stateTitle,
            items = indianStates,
            currentValue = indianStates.find {
                it.ta == profile.getPrimary("state") || it.en == profile.getPrimary("state")
            } ?: if (profile.getPrimary("state").isNotBlank()) indianStates.lastOrNull() else null,
            itemLabelBuilder = { if (uiLanguage == "ta") it.ta else it.en },
            subtitleBuilder = { if (isBilingual) (if (uiLanguage == "ta") it.en else it.ta) else null },
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
                    persistProfile(updated)
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
        if (fieldName == "naadu" || fieldName == "country") {
            updated.state.clear()
            updated.district.clear()
        }
        persistProfile(updated)
        editingSection = null
        ElvanSnackbar.show(saveSuccessMsg)
    }

    fun saveSingle(action: (String) -> Unit) {
        val updated = profile.copy()
        action(tempPrimary)
        persistProfile(updated)
        editingSection = null
        ElvanSnackbar.show(saveSuccessMsg)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        com.elvan.udukkai.ui.components.shell.ElvanLazyColumn(
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
                if (countryFirst) {
                    // ── Pattu Order ──
                    // 1. Country: deliberately unselected for new profiles; tap to choose.
                    val countryPrimary = profile.getPrimary("country")
                    val countrySecondary = profile.getSecondary("country")
                    ElvanSettingsAnimatedExpand(
                        isEditing = editingSection == "naadu",
                        displayContent = {
                            ElvanSettingsDisplayRow(
                                title = K.country.tr(),
                                primaryValue = uiName(countryPrimary, worldCountries),
                                secondaryValue = if (isBilingual) countrySecondary else null,
                                onEdit = { beginEdit("naadu", countryPrimary, countrySecondary, custom = countryPrimary.isNotBlank() && listed(worldCountries, countryPrimary) == null) },
                                colors = colors
                            )
                        },
                        editContent = {
                            ElvanSettingsEditContainer(
                                title = K.country.tr(),
                                onCancel = { editingSection = null },
                                onSave = { saveBilingual("naadu") },
                                colors = colors
                            ) {
                                ElvanSettingsPickFields(
                                    label = K.country.tr(),
                                    primaryLangLabel = primaryLangLabel,
                                    secondaryLangLabel = secondaryLangLabel,
                                    isBilingual = isBilingual,
                                    primary = tempPrimary,
                                    secondary = tempSecondary,
                                    custom = tempCustom,
                                    onPick = { pickFromList(countryTitle, worldCountries) },
                                    onChange = { p, s -> tempPrimary = p; tempSecondary = s },
                                    colors = colors
                                )

                            }
                        }
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
                                primaryValue = uiName(statePrimary, indianStates),
                                secondaryValue = if (isBilingual) stateSecondary else null,
                                onEdit = if (profile.getPrimary("country").equals("India", true) || profile.getPrimary("country") == "இந்தியா") openStateSelection else { { beginEdit("maanilam", statePrimary, stateSecondary) } },
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
                                primaryValue = uiName(districtPrimary, tamilNaduDistricts),
                                secondaryValue = if (isBilingual) districtSecondary else null,
                                onEdit = { beginEdit("maavattam", districtPrimary, districtSecondary, custom = districtPrimary.isNotBlank() && listed(tamilNaduDistricts, districtPrimary) == null) },
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
                                ElvanSettingsPickFields(
                                    label = K.district.tr(),
                                    primaryLangLabel = primaryLangLabel,
                                    secondaryLangLabel = secondaryLangLabel,
                                    isBilingual = isBilingual,
                                    primary = tempPrimary,
                                    secondary = tempSecondary,
                                    custom = tempCustom,
                                    canPick = isTamilNadu,
                                    onPick = { pickFromList(districtTitle, tamilNaduDistricts) },
                                    onChange = { p, s -> tempPrimary = p; tempSecondary = s },
                                    colors = colors
                                )

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
                    // 1. Country selection is available in both Factory and Textiles.
                    val countryPrimary = profile.getPrimary("country")
                    val countrySecondary = profile.getSecondary("country")
                    ElvanSettingsAnimatedExpand(
                        isEditing = editingSection == "naadu",
                        displayContent = {
                            ElvanSettingsDisplayRow(
                                title = K.country.tr(),
                                primaryValue = uiName(countryPrimary, worldCountries),
                                secondaryValue = if (isBilingual) countrySecondary else null,
                                onEdit = { beginEdit("naadu", countryPrimary, countrySecondary, custom = countryPrimary.isNotBlank() && listed(worldCountries, countryPrimary) == null) },
                                colors = colors
                            )
                        },
                        editContent = {
                            ElvanSettingsEditContainer(
                                title = K.country.tr(),
                                onCancel = { editingSection = null },
                                onSave = { saveBilingual("naadu") },
                                colors = colors
                            ) {
                                ElvanSettingsPickFields(
                                    label = K.country.tr(),
                                    primaryLangLabel = primaryLangLabel,
                                    secondaryLangLabel = secondaryLangLabel,
                                    isBilingual = isBilingual,
                                    primary = tempPrimary,
                                    secondary = tempSecondary,
                                    custom = tempCustom,
                                    onPick = { pickFromList(countryTitle, worldCountries) },
                                    onChange = { p, s -> tempPrimary = p; tempSecondary = s },
                                    colors = colors
                                )

                            }
                        }
                    )
                    ElvanSettingsDivider(colors = colors)

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
                                primaryValue = uiName(districtPrimary, tamilNaduDistricts),
                                secondaryValue = districtSecondary,
                                onEdit = { beginEdit("maavattam", districtPrimary, districtSecondary, custom = districtPrimary.isNotBlank() && listed(tamilNaduDistricts, districtPrimary) == null) },
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
                                ElvanSettingsPickFields(
                                    label = K.district.tr(),
                                    primaryLangLabel = primaryLangLabel,
                                    secondaryLangLabel = secondaryLangLabel,
                                    isBilingual = isBilingual,
                                    primary = tempPrimary,
                                    secondary = tempSecondary,
                                    custom = tempCustom,
                                    onPick = { pickFromList(districtTitle, tamilNaduDistricts) },
                                    onChange = { p, s -> tempPrimary = p; tempSecondary = s },
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
