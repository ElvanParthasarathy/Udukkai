package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.data.settings.BusinessProfile
import com.elvan.udukkai.data.settings.LanguageRules
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.ShellDefaults
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * Dedicated Bilingual Rules Subpage (One UI Style).
 * - Master toggle pill at the top ("On" / "Off" with Switch).
 * - When OFF: all specific rules below are hidden.
 * - When ON: displays fine-grained bilingual printing rules with an explicit Save container.
 */
@Composable
fun BilingualRulesScreen(
    profile: BusinessProfile,
    colors: ShellColors = rememberShellColors(),
    scrollState: LazyListState = rememberLazyListState(),
    onSave: (BusinessProfile) -> Unit
) {
    val english = K.enCode.tr()
    val tamil = K.taCode.tr()
    val saveSuccessMsg = K.profileSaved.tr()
    val onText = K.on.tr()
    val offText = K.off.tr()

    fun modeLabel(field: String, mode: String) = when {
        LanguageRules.isBoth(field) -> if (mode == "ta") "$tamil → $english" else "$english → $tamil"
        mode == "ta" -> tamil
        else -> english
    }

    // Rules state
    val currentBusinessName = LanguageRules.get(profile.printSettingsJson, "businessName")
    val currentBusinessAddress = LanguageRules.get(profile.printSettingsJson, "businessAddress")
    val currentCustomer = LanguageRules.get(profile.printSettingsJson, "customer")
    val currentSignatureName = LanguageRules.get(profile.printSettingsJson, "signatureName")

    var isEditingRules by remember { mutableStateOf(false) }
    var tempBusinessName by remember(profile.printSettingsJson) { mutableStateOf(currentBusinessName) }
    var tempBusinessAddress by remember(profile.printSettingsJson) { mutableStateOf(currentBusinessAddress) }
    var tempCustomer by remember(profile.printSettingsJson) { mutableStateOf(currentCustomer) }
    var tempSignatureName by remember(profile.printSettingsJson) { mutableStateOf(currentSignatureName) }

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
            item(key = "shell_top_spacer") {
                Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
            }

            // ── Top One UI Master Switch Pill ──
            item(key = "master_toggle_pill") {
                ElvanSettingsSection(
                    borderRadius = 32.dp,
                    colors = colors
                ) {
                    ElvanSimpleSettingsRow(
                        title = if (profile.isBilingual) onText else offText,
                        onClick = {
                            val updated = profile.copy(isBilingual = !profile.isBilingual)
                            onSave(updated)
                            ElvanSnackbar.show(saveSuccessMsg)
                        },
                        trailing = {
                            ElvanSettingsSwitch(
                                checked = profile.isBilingual,
                                onCheckedChange = { isChecked ->
                                    val updated = profile.copy(isBilingual = isChecked)
                                    onSave(updated)
                                    ElvanSnackbar.show(saveSuccessMsg)
                                },
                                colors = colors
                            )
                        },
                        colors = colors
                    )
                }
            }

            // ── Specific Rules (Only visible when master switch is ON) ──
            if (profile.isBilingual) {
                item(key = "bilingual_rules_card") {
                    ElvanSettingsSection(colors = colors) {
                        ElvanSettingsAnimatedExpand(
                            isEditing = isEditingRules,
                            displayContent = {
                                Column {
                                    // 1. Business Name (with circular pencil edit button)
                                    ElvanSimpleSettingsRow(
                                        title = K.ruleBusinessName.tr(),
                                        description = modeLabel("businessName", currentBusinessName),
                                        trailing = {
                                            Surface(
                                                shape = CircleShape,
                                                color = colors.iconBg,
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .clickable(
                                                        interactionSource = remember { MutableInteractionSource() },
                                                        indication = ShellDefaults.ripple(colors, bounded = true),
                                                        onClick = {
                                                            tempBusinessName = currentBusinessName
                                                            tempBusinessAddress = currentBusinessAddress
                                                            tempCustomer = currentCustomer
                                                            tempSignatureName = currentSignatureName
                                                            isEditingRules = true
                                                        }
                                                    )
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = MaterialSymbols.Rounded.Edit,
                                                        contentDescription = K.edit.tr(),
                                                        tint = colors.accent,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        },
                                        colors = colors
                                    )

                                    ElvanSettingsDivider(colors = colors)

                                    // 2. Business Address
                                    ElvanSimpleSettingsRow(
                                        title = K.ruleBusinessAddress.tr(),
                                        description = modeLabel("businessAddress", currentBusinessAddress),
                                        colors = colors
                                    )

                                    ElvanSettingsDivider(colors = colors)

                                    // 3. Customer
                                    ElvanSimpleSettingsRow(
                                        title = K.ruleCustomer.tr(),
                                        description = modeLabel("customer", currentCustomer),
                                        colors = colors
                                    )

                                    ElvanSettingsDivider(colors = colors)

                                    // 4. Signature Name
                                    ElvanSimpleSettingsRow(
                                        title = K.ruleSignatureName.tr(),
                                        description = modeLabel("signatureName", currentSignatureName),
                                        colors = colors
                                    )
                                }
                            },
                            editContent = {
                                ElvanSettingsEditContainer(
                                    title = K.languageRules.tr(),
                                    onCancel = {
                                        isEditingRules = false
                                        tempBusinessName = currentBusinessName
                                        tempBusinessAddress = currentBusinessAddress
                                        tempCustomer = currentCustomer
                                        tempSignatureName = currentSignatureName
                                    },
                                    onSave = {
                                        var updatedJson = profile.printSettingsJson
                                        updatedJson = LanguageRules.set(updatedJson, "businessName", tempBusinessName)
                                        updatedJson = LanguageRules.set(updatedJson, "businessAddress", tempBusinessAddress)
                                        updatedJson = LanguageRules.set(updatedJson, "customer", tempCustomer)
                                        updatedJson = LanguageRules.set(updatedJson, "signatureName", tempSignatureName)
                                        val updatedProfile = profile.copy(printSettingsJson = updatedJson)
                                        onSave(updatedProfile)
                                        isEditingRules = false
                                        ElvanSnackbar.show(saveSuccessMsg)
                                    },
                                    colors = colors
                                ) {
                                    // 1. Business Name Dropdown
                                    ElvanSettingsDropdown(
                                        label = K.ruleBusinessName.tr(),
                                        value = tempBusinessName,
                                        items = LanguageRules.modes,
                                        itemLabelBuilder = { modeLabel("businessName", it) },
                                        onChanged = { tempBusinessName = it },
                                        colors = colors
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // 2. Business Address Dropdown
                                    ElvanSettingsDropdown(
                                        label = K.ruleBusinessAddress.tr(),
                                        value = tempBusinessAddress,
                                        items = LanguageRules.modes,
                                        itemLabelBuilder = { modeLabel("businessAddress", it) },
                                        onChanged = { tempBusinessAddress = it },
                                        colors = colors
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // 3. Customer Dropdown
                                    ElvanSettingsDropdown(
                                        label = K.ruleCustomer.tr(),
                                        value = tempCustomer,
                                        items = LanguageRules.modes,
                                        itemLabelBuilder = { modeLabel("customer", it) },
                                        onChanged = { tempCustomer = it },
                                        colors = colors
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // 4. Signature Name Dropdown
                                    ElvanSettingsDropdown(
                                        label = K.ruleSignatureName.tr(),
                                        value = tempSignatureName,
                                        items = LanguageRules.modes,
                                        itemLabelBuilder = { modeLabel("signatureName", it) },
                                        onChanged = { tempSignatureName = it },
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
