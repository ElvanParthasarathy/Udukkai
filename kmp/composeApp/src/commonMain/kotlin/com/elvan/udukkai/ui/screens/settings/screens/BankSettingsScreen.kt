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
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*

/**
 * Bank Settings Screen matching Flutter's `vangi_thirai.dart` 1:1.
 * Supports Bank Name, Branch Name, Account Number, IFSC Code and UPI ID with the same inline edit and
 * close rows as every other settings screen. The business has one bank account here; when older
 * accounts exist, the default one is kept in step.
 */
@Composable
fun BankSettingsScreen(
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors(),
    profile: com.elvan.udukkai.data.settings.BusinessProfile,
    onUpdateProfile: (com.elvan.udukkai.data.settings.BusinessProfile) -> Unit
) {
    val isBilingual = profile.isBilingual

    var editingSection by remember { mutableStateOf<String?>(null) }
    var tempPrimary by remember { mutableStateOf("") }
    var tempSecondary by remember { mutableStateOf("") }

    val primaryLangLabel = if (profile.primaryLanguage.lowercase().startsWith("ta")) K.taCode.tr() else K.enCode.tr()
    val secondaryLangLabel = if (profile.secondaryLanguage.lowercase().startsWith("ta")) K.taCode.tr() else K.enCode.tr()
    val saveSuccessMsg = K.profileSaved.tr()

    fun beginEdit(section: String, primary: String, secondary: String = "") {
        editingSection = section
        tempPrimary = primary
        tempSecondary = secondary
    }

    // The default payment account follows the plain bank fields (other accounts are left as they are)
    fun withDefaultAccount(updated: com.elvan.udukkai.data.settings.BusinessProfile): com.elvan.udukkai.data.settings.BusinessProfile {
        val accounts = com.elvan.udukkai.data.settings.PaymentAccount.parseList(updated.paymentAccountsJson)
        if (accounts.isNotEmpty()) {
            val defaultId = (accounts.find { it.isDefault } ?: accounts.first()).id
            updated.setPaymentAccounts(accounts.map {
                if (it.id == defaultId) it.copy(
                    bankName = HashMap(updated.bankName),
                    branch = HashMap(updated.branch),
                    accountNumber = updated.accountNumber,
                    ifsc = updated.ifsc,
                    upiId = updated.upiId
                ) else it
            })
        }
        return updated
    }

    fun saveBilingual(fieldName: String) {
        val updated = profile.copy()
        updated.setBilingual(fieldName, profile.primaryLanguage, tempPrimary)
        updated.setBilingual(fieldName, profile.secondaryLanguage, tempSecondary)
        onUpdateProfile(withDefaultAccount(updated))
        editingSection = null
        ElvanSnackbar.show(saveSuccessMsg)
    }

    fun saveSingle(action: (com.elvan.udukkai.data.settings.BusinessProfile, String) -> Unit) {
        val updated = profile.copy()
        action(updated, tempPrimary)
        onUpdateProfile(withDefaultAccount(updated))
        editingSection = null
        ElvanSnackbar.show(saveSuccessMsg)
    }

    val bankNamePrimary = profile.getPrimary("bankName")
    val bankNameSecondary = profile.getSecondary("bankName")
    val branchPrimary = profile.getPrimary("branch")
    val branchSecondary = profile.getSecondary("branch")

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
                            primaryValue = bankNamePrimary,
                            secondaryValue = if (isBilingual) bankNameSecondary else null,
                            onEdit = { beginEdit("vangiPeyar", bankNamePrimary, bankNameSecondary) },
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
                            primaryValue = branchPrimary,
                            secondaryValue = if (isBilingual) branchSecondary else null,
                            onEdit = { beginEdit("vangiKilai", branchPrimary, branchSecondary) },
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
                val accountNo = profile.accountNumber
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
                            onSave = { saveSingle { p, v -> p.accountNumber = v } },
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
                            onSave = { saveSingle { p, v -> p.ifsc = v } },
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
                ElvanSettingsDivider(colors = colors)

                // 5. UPI ID (for the QR code on bills)
                val upi = profile.upiId
                ElvanSettingsAnimatedExpand(
                    isEditing = editingSection == "upiId",
                    displayContent = {
                        ElvanSettingsDisplayRow(
                            title = K.upiId.tr(),
                            primaryValue = upi,
                            onEdit = { beginEdit("upiId", upi) },
                            colors = colors
                        )
                    },
                    editContent = {
                        ElvanSettingsEditContainer(
                            title = K.upiId.tr(),
                            onCancel = { editingSection = null },
                            onSave = { saveSingle { p, v -> p.upiId = v.trim() } },
                            colors = colors
                        ) {
                            ElvanSettingsTextField(
                                label = K.upiId.tr(),
                                value = tempPrimary,
                                onValueChange = { tempPrimary = it },
                                colors = colors
                            )
                        }
                    }
                )
            }
        }
    }
}
