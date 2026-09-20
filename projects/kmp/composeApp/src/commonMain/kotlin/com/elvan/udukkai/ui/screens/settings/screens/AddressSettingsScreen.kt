package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.core.data.IdangalinPeyar
import com.elvan.udukkai.core.data.indhiyaMaanilangal
import com.elvan.udukkai.core.data.tamizhnaattuMaavattangal
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.mode.LocalAppMode
import com.elvan.udukkai.data.settings.NiruvanaTharavugalRepository
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
fun AddressSettingsScreen(
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val currentMode = LocalAppMode.current
    val profile = NiruvanaTharavugalRepository.getProfile(currentMode)
    val isPattu = currentMode == AppMode.PATTU
    val isBilingual = !isPattu || profile.iruMozhi

    var editingSection by remember { mutableStateOf<String?>(null) }
    var tempPrimary by remember { mutableStateOf("") }
    var tempSecondary by remember { mutableStateOf("") }

    val stateTitle = K.state.tr()
    val districtTitle = K.district.tr()
    val primaryLangLabel = if (profile.mudhanMozhi.lowercase().startsWith("ta")) K.taCode.tr() else K.enCode.tr()
    val secondaryLangLabel = if (profile.thunaiMozhi.lowercase().startsWith("ta")) K.taCode.tr() else K.enCode.tr()
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
            items = indhiyaMaanilangal,
            currentValue = indhiyaMaanilangal.find {
                it.ta == profile.getPrimary("maanilam") || it.en == profile.getPrimary("maanilam")
            } ?: if (profile.getPrimary("maanilam").isNotBlank()) indhiyaMaanilangal.lastOrNull() else null,
            itemLabelBuilder = { if (profile.mudhanMozhi == "ta") it.ta else it.en },
            subtitleBuilder = { if (isBilingual) (if (profile.mudhanMozhi == "ta") it.en else it.ta) else null },
            showSearch = true,
            searchFilter = { item, query ->
                item.ta.contains(query, ignoreCase = true) || item.en.contains(query, ignoreCase = true)
            },
            onSelected = { stateItem ->
                if (stateItem.en == "Custom" || stateItem.ta == "தனிப்பயன்") {
                    val currP = profile.getPrimary("maanilam")
                    val currS = profile.getSecondary("maanilam")
                    val isAlreadyPreset = indhiyaMaanilangal.any { (it.ta == currP || it.en == currP) && it.en != "Custom" && it.ta != "தனிப்பயன்" }
                    val initialP = if (isAlreadyPreset || currP == "Custom" || currP == "தனிப்பயன்") "" else currP
                    val initialS = if (isAlreadyPreset || currS == "Custom" || currS == "தனிப்பயன்") "" else currS
                    beginEdit("maanilam", initialP, initialS)
                } else {
                    val p = if (profile.mudhanMozhi == "ta") stateItem.ta else stateItem.en
                    val s = if (profile.mudhanMozhi == "ta") stateItem.en else stateItem.ta
                    val updated = profile.copy()
                    updated.setBilingual("maanilam", profile.mudhanMozhi, p)
                    updated.setBilingual("maanilam", profile.thunaiMozhi, s)
                    NiruvanaTharavugalRepository.updateProfile(currentMode, updated)
                    editingSection = null
                    ElvanSnackbar.show(saveSuccessMsg)
                }
            }
        )
    }

    val openDistrictSelection = {
        bottomSheet.showSelection(
            title = districtTitle,
            items = tamizhnaattuMaavattangal,
            currentValue = tamizhnaattuMaavattangal.find {
                it.ta == profile.getPrimary("maavattam") || it.en == profile.getPrimary("maavattam")
            } ?: if (profile.getPrimary("maavattam").isNotBlank()) tamizhnaattuMaavattangal.lastOrNull() else null,
            itemLabelBuilder = { if (profile.mudhanMozhi == "ta") it.ta else it.en },
            subtitleBuilder = { if (isBilingual) (if (profile.mudhanMozhi == "ta") it.en else it.ta) else null },
            showSearch = true,
            searchFilter = { item, query ->
                item.ta.contains(query, ignoreCase = true) || item.en.contains(query, ignoreCase = true)
            },
            onSelected = { districtItem ->
                if (districtItem.en == "Custom" || districtItem.ta == "தனிப்பயன்") {
                    val currP = profile.getPrimary("maavattam")
                    val currS = profile.getSecondary("maavattam")
                    val isAlreadyPreset = tamizhnaattuMaavattangal.any { (it.ta == currP || it.en == currP) && it.en != "Custom" && it.ta != "தனிப்பயன்" }
                    val initialP = if (isAlreadyPreset || currP == "Custom" || currP == "தனிப்பயன்") "" else currP
                    val initialS = if (isAlreadyPreset || currS == "Custom" || currS == "தனிப்பயன்") "" else currS
                    beginEdit("maavattam", initialP, initialS)
                } else {
                    val p = if (profile.mudhanMozhi == "ta") districtItem.ta else districtItem.en
                    val s = if (profile.mudhanMozhi == "ta") districtItem.en else districtItem.ta
                    val updated = profile.copy()
                    updated.setBilingual("maavattam", profile.mudhanMozhi, p)
                    updated.setBilingual("maavattam", profile.thunaiMozhi, s)
                    NiruvanaTharavugalRepository.updateProfile(currentMode, updated)
                    editingSection = null
                    ElvanSnackbar.show(saveSuccessMsg)
                }
            }
        )
    }

    fun saveBilingual(fieldName: String) {
        val updated = profile.copy()
        updated.setBilingual(fieldName, profile.mudhanMozhi, tempPrimary)
        updated.setBilingual(fieldName, profile.thunaiMozhi, tempSecondary)
        NiruvanaTharavugalRepository.updateProfile(currentMode, updated)
        editingSection = null
        ElvanSnackbar.show(saveSuccessMsg)
    }

    fun saveSingle(action: (String) -> Unit) {
        val updated = profile.copy()
        action(tempPrimary)
        NiruvanaTharavugalRepository.updateProfile(currentMode, updated)
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
                    val naaduPrimary = profile.getPrimary("naadu")
                    val naaduSecondary = profile.getSecondary("naadu")
                    ElvanSettingsDisplayRow(
                        title = K.country.tr(),
                        primaryValue = naaduPrimary,
                        secondaryValue = if (isBilingual) naaduSecondary else null,
                        onEdit = null,
                        colors = colors
                    )
                    ElvanSettingsDivider(colors = colors)

                    // 2. State (Maanilam)
                    val maanilamPrimary = profile.getPrimary("maanilam")
                    val maanilamSecondary = profile.getSecondary("maanilam")
                    ElvanSettingsAnimatedExpand(
                        isEditing = editingSection == "maanilam",
                        displayContent = {
                            ElvanSettingsDisplayRow(
                                title = K.state.tr(),
                                primaryValue = maanilamPrimary,
                                secondaryValue = if (isBilingual) maanilamSecondary else null,
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
                    val maavattamPrimary = profile.getPrimary("maavattam")
                    val maavattamSecondary = profile.getSecondary("maavattam")
                    val isTamilNadu = maanilamPrimary.trim() in listOf("Tamil Nadu", "தமிழ்நாடு") || maanilamSecondary.trim() in listOf("Tamil Nadu", "தமிழ்நாடு")

                    ElvanSettingsAnimatedExpand(
                        isEditing = editingSection == "maavattam",
                        displayContent = {
                            ElvanSettingsDisplayRow(
                                title = K.district.tr(),
                                primaryValue = maavattamPrimary,
                                secondaryValue = if (isBilingual) maavattamSecondary else null,
                                onEdit = if (isTamilNadu) openDistrictSelection else { { beginEdit("maavattam", maavattamPrimary, maavattamSecondary) } },
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
                    val oorPrimary = profile.getPrimary("oor")
                    val oorSecondary = profile.getSecondary("oor")
                    ElvanSettingsAnimatedExpand(
                        isEditing = editingSection == "oor",
                        displayContent = {
                            ElvanSettingsDisplayRow(
                                title = K.city.tr(),
                                primaryValue = oorPrimary,
                                secondaryValue = if (isBilingual) oorSecondary else null,
                                onEdit = { beginEdit("oor", oorPrimary, oorSecondary) },
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
                    val mugavariPrimary = profile.getPrimary("mugavari")
                    val mugavariSecondary = profile.getSecondary("mugavari")
                    ElvanSettingsAnimatedExpand(
                        isEditing = editingSection == "mugavari",
                        displayContent = {
                            ElvanSettingsDisplayRow(
                                title = K.address.tr(),
                                primaryValue = mugavariPrimary,
                                secondaryValue = if (isBilingual) mugavariSecondary else null,
                                onEdit = { beginEdit("mugavari", mugavariPrimary, mugavariSecondary) },
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
                                    colors = colors
                                )
                                if (isBilingual) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    ElvanSettingsTextField(
                                        label = "${K.address.tr()} ($secondaryLangLabel)",
                                        value = tempSecondary,
                                        onValueChange = { tempSecondary = it },
                                        singleLine = false,
                                        colors = colors
                                    )
                                }
                            }
                        }
                    )
                    ElvanSettingsDivider(colors = colors)

                    // 6. PIN Code (Anjal Kuriyeedu)
                    val pin = profile.anjalKuriyeedu
                    ElvanSettingsAnimatedExpand(
                        isEditing = editingSection == "anjalKuriyeedu",
                        displayContent = {
                            ElvanSettingsDisplayRow(
                                title = K.pincode.tr(),
                                primaryValue = pin,
                                onEdit = { beginEdit("anjalKuriyeedu", pin) },
                                colors = colors
                            )
                        },
                        editContent = {
                            ElvanSettingsEditContainer(
                                title = K.pincode.tr(),
                                onCancel = { editingSection = null },
                                onSave = { saveSingle { profile.anjalKuriyeedu = it } },
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
                    val mugavariPrimary = profile.getPrimary("mugavari")
                    val mugavariSecondary = profile.getSecondary("mugavari")
                    ElvanSettingsAnimatedExpand(
                        isEditing = editingSection == "mugavari",
                        displayContent = {
                            ElvanSettingsDisplayRow(
                                title = K.address.tr(),
                                primaryValue = mugavariPrimary,
                                secondaryValue = mugavariSecondary,
                                onEdit = { beginEdit("mugavari", mugavariPrimary, mugavariSecondary) },
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
                                    colors = colors
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                ElvanSettingsTextField(
                                    label = "${K.address.tr()} ($secondaryLangLabel)",
                                    value = tempSecondary,
                                    onValueChange = { tempSecondary = it },
                                    singleLine = false,
                                    colors = colors
                                )
                            }
                        }
                    )
                    ElvanSettingsDivider(colors = colors)

                    // 2. City (Oor)
                    val oorPrimary = profile.getPrimary("oor")
                    val oorSecondary = profile.getSecondary("oor")
                    ElvanSettingsAnimatedExpand(
                        isEditing = editingSection == "oor",
                        displayContent = {
                            ElvanSettingsDisplayRow(
                                title = K.city.tr(),
                                primaryValue = oorPrimary,
                                secondaryValue = oorSecondary,
                                onEdit = { beginEdit("oor", oorPrimary, oorSecondary) },
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
                    val maavattamPrimary = profile.getPrimary("maavattam")
                    val maavattamSecondary = profile.getSecondary("maavattam")
                    ElvanSettingsAnimatedExpand(
                        isEditing = editingSection == "maavattam",
                        displayContent = {
                            ElvanSettingsDisplayRow(
                                title = K.district.tr(),
                                primaryValue = maavattamPrimary,
                                secondaryValue = maavattamSecondary,
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
                    val pin = profile.anjalKuriyeedu
                    ElvanSettingsAnimatedExpand(
                        isEditing = editingSection == "anjalKuriyeedu",
                        displayContent = {
                            ElvanSettingsDisplayRow(
                                title = K.pincode.tr(),
                                primaryValue = pin,
                                onEdit = { beginEdit("anjalKuriyeedu", pin) },
                                colors = colors
                            )
                        },
                        editContent = {
                            ElvanSettingsEditContainer(
                                title = K.pincode.tr(),
                                onCancel = { editingSection = null },
                                onSave = { saveSingle { profile.anjalKuriyeedu = it } },
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

