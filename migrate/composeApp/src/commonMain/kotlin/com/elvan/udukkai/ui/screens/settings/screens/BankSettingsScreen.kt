package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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
 * Bank Settings Screen matching Flutter's `vangi_thirai.dart` 1:1.
 * Supports Bank Name, Branch Name, Account Number, and IFSC Code
 * with bilingual accordion animated expand and live persistence.
 */
@Composable
fun BankSettingsScreen(
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

    val primaryLangLabel = if (profile.mudhanMozhi.lowercase().startsWith("ta")) K.taCode.tr() else K.enCode.tr()
    val secondaryLangLabel = if (profile.thunaiMozhi.lowercase().startsWith("ta")) K.taCode.tr() else K.enCode.tr()
    val saveSuccessMsg = K.profileSaved.tr()

    fun beginEdit(section: String, primary: String, secondary: String = "") {
        editingSection = section
        tempPrimary = primary
        tempSecondary = secondary
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

    val vangiPeyarPrimary = profile.getPrimary("vangiPeyar")
    val vangiPeyarSecondary = profile.getSecondary("vangiPeyar")
    val kilaiPrimary = profile.getPrimary("kilai")
    val kilaiSecondary = profile.getSecondary("kilai")

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
                // 1. Bank Name (Vangiyin Peyar)
                ElvanSettingsAnimatedExpand(
                    isEditing = editingSection == "vangiPeyar",
                    displayContent = {
                        ElvanSettingsDisplayRow(
                            title = K.bankName.tr(),
                            primaryValue = vangiPeyarPrimary,
                            secondaryValue = if (isBilingual) vangiPeyarSecondary else null,
                            onEdit = { beginEdit("vangiPeyar", vangiPeyarPrimary, vangiPeyarSecondary) },
                            colors = colors
                        )
                    },
                    editContent = {
                        ElvanSettingsEditContainer(
                            title = K.bankName.tr(),
                            onCancel = { editingSection = null },
                            onSave = { saveBilingual("vangiPeyar") },
                            colors = colors
                        ) {
                            ElvanSettingsTextField(
                                label = "${K.bankName.tr()} ($primaryLangLabel)",
                                value = tempPrimary,
                                onValueChange = { tempPrimary = it },
                                colors = colors
                            )
                            if (isBilingual) {
                                Spacer(modifier = Modifier.height(12.dp))
                                ElvanSettingsTextField(
                                    label = "${K.bankName.tr()} ($secondaryLangLabel)",
                                    value = tempSecondary,
                                    onValueChange = { tempSecondary = it },
                                    colors = colors
                                )
                            }
                        }
                    }
                )
                ElvanSettingsDivider(colors = colors)

                // 2. Branch Name (Kilaip Peyar)
                ElvanSettingsAnimatedExpand(
                    isEditing = editingSection == "vangiKilai",
                    displayContent = {
                        ElvanSettingsDisplayRow(
                            title = K.branchName.tr(),
                            primaryValue = kilaiPrimary,
                            secondaryValue = if (isBilingual) kilaiSecondary else null,
                            onEdit = { beginEdit("vangiKilai", kilaiPrimary, kilaiSecondary) },
                            colors = colors
                        )
                    },
                    editContent = {
                        ElvanSettingsEditContainer(
                            title = K.branchName.tr(),
                            onCancel = { editingSection = null },
                            onSave = { saveBilingual("kilai") },
                            colors = colors
                        ) {
                            ElvanSettingsTextField(
                                label = "${K.branchName.tr()} ($primaryLangLabel)",
                                value = tempPrimary,
                                onValueChange = { tempPrimary = it },
                                colors = colors
                            )
                            if (isBilingual) {
                                Spacer(modifier = Modifier.height(12.dp))
                                ElvanSettingsTextField(
                                    label = "${K.branchName.tr()} ($secondaryLangLabel)",
                                    value = tempSecondary,
                                    onValueChange = { tempSecondary = it },
                                    colors = colors
                                )
                            }
                        }
                    }
                )
                ElvanSettingsDivider(colors = colors)

                // 3. Account Number (Kanakku En)
                val accountNo = profile.vangiKanakku
                ElvanSettingsAnimatedExpand(
                    isEditing = editingSection == "vangiKanakku",
                    displayContent = {
                        ElvanSettingsDisplayRow(
                            title = K.accountNumber.tr(),
                            primaryValue = accountNo,
                            onEdit = { beginEdit("vangiKanakku", accountNo) },
                            colors = colors
                        )
                    },
                    editContent = {
                        ElvanSettingsEditContainer(
                            title = K.accountNumber.tr(),
                            onCancel = { editingSection = null },
                            onSave = { saveSingle { profile.vangiKanakku = it } },
                            colors = colors
                        ) {
                            ElvanSettingsTextField(
                                label = K.accountNumber.tr(),
                                value = tempPrimary,
                                onValueChange = { if (it.length <= 18 && it.all { c -> c.isDigit() }) tempPrimary = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                maxLength = 18,
                                colors = colors
                            )
                        }
                    }
                )
                ElvanSettingsDivider(colors = colors)

                // 4. IFSC Code (IFSC Kuriyeedu)
                val ifsc = profile.ifsc
                ElvanSettingsAnimatedExpand(
                    isEditing = editingSection == "ifsc",
                    displayContent = {
                        ElvanSettingsDisplayRow(
                            title = K.ifscCode.tr(),
                            primaryValue = ifsc,
                            onEdit = { beginEdit("ifsc", ifsc) },
                            colors = colors
                        )
                    },
                    editContent = {
                        ElvanSettingsEditContainer(
                            title = K.ifscCode.tr(),
                            onCancel = { editingSection = null },
                            onSave = { saveSingle { profile.ifsc = it } },
                            colors = colors
                        ) {
                            ElvanSettingsTextField(
                                label = K.ifscCode.tr(),
                                value = tempPrimary,
                                onValueChange = { if (it.length <= 11) tempPrimary = it.uppercase() },
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                                maxLength = 11,
                                colors = colors
                            )
                        }
                    }
                )
            }
        }
    }
}
