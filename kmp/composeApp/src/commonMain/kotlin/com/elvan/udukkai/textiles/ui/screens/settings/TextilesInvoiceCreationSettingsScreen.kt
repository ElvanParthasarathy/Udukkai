package com.elvan.udukkai.textiles.ui.screens.settings

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
import com.elvan.udukkai.textiles.data.TextilesProfileRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.ShellDefaults
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.settings.SettingsRoute

/**
 * Invoice Creation Settings Screen for Textiles Edition.
 * Shows Primary & Secondary billing languages outside, Bilingual Mode & GST switches,
 * Bilingual Rules navigation, and Theme Color with explicit Save.
 */
@Composable
fun TextilesInvoiceCreationSettingsScreen(
    onNavigate: (SettingsRoute) -> Unit = {},
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val profile = TextilesProfileRepository.profile

    var isEditingLanguages by remember { mutableStateOf(false) }
    var tempPrimaryLang by remember { mutableStateOf(profile.primaryLanguage) }
    var tempSecondaryLang by remember { mutableStateOf(profile.secondaryLanguage) }

    val saveSuccessMsg = K.profileSaved.tr()
    val tamilLabel = K.taCode.tr()
    val englishLabel = K.enCode.tr()

    fun getLanguageName(code: String): String {
        return if (code.lowercase().startsWith("ta")) tamilLabel else englishLabel
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

            // ── Section 1: Billing Languages (Primary & Secondary kept outside) ──
            item(key = "languages_section") {
                ElvanSettingsSection(colors = colors) {
                    ElvanSettingsAnimatedExpand(
                        isEditing = isEditingLanguages,
                        displayContent = {
                            Column {
                                ElvanSimpleSettingsRow(
                                    title = K.primaryLanguage.tr(),
                                    description = getLanguageName(profile.primaryLanguage),
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
                                                        tempPrimaryLang = profile.primaryLanguage
                                                        tempSecondaryLang = profile.secondaryLanguage
                                                        isEditingLanguages = true
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

                                if (profile.isBilingual) {
                                    ElvanSettingsDivider(colors = colors)
                                    ElvanSimpleSettingsRow(
                                        title = K.secondaryLanguage.tr(),
                                        description = getLanguageName(profile.secondaryLanguage),
                                        colors = colors
                                    )
                                }
                            }
                        },
                        editContent = {
                            ElvanSettingsEditContainer(
                                title = K.selectLanguage.tr(),
                                onCancel = { isEditingLanguages = false },
                                onSave = {
                                    val updated = profile.copy(
                                        primaryLanguage = tempPrimaryLang,
                                        secondaryLanguage = tempSecondaryLang
                                    )
                                    TextilesProfileRepository.updateProfile(updated)
                                    isEditingLanguages = false
                                    ElvanSnackbar.show(saveSuccessMsg)
                                },
                                colors = colors
                            ) {
                                ElvanSettingsDropdown(
                                    label = K.primaryLanguage.tr(),
                                    value = tempPrimaryLang,
                                    items = listOf("ta", "en"),
                                    itemLabelBuilder = { getLanguageName(it) },
                                    onChanged = { selectedLang ->
                                        if (selectedLang == tempSecondaryLang) {
                                            tempSecondaryLang = tempPrimaryLang
                                        }
                                        tempPrimaryLang = selectedLang
                                    },
                                    colors = colors
                                )

                                if (profile.isBilingual) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    ElvanSettingsDropdown(
                                        label = K.secondaryLanguage.tr(),
                                        value = tempSecondaryLang,
                                        items = listOf("ta", "en"),
                                        itemLabelBuilder = { getLanguageName(it) },
                                        onChanged = { selectedLang ->
                                            if (selectedLang == tempPrimaryLang) {
                                                tempPrimaryLang = tempSecondaryLang
                                            }
                                            tempSecondaryLang = selectedLang
                                        },
                                        colors = colors
                                    )
                                }
                            }
                        }
                    )
                }
            }

            // ── Section 2: Switches (Bilingual Mode & GST Splits) ──
            item(key = "switches_section") {
                ElvanSettingsSection(colors = colors) {
                    // Bilingual Mode Toggle
                    ElvanSimpleSettingsRow(
                        title = K.bilingualMode.tr(),
                        trailing = {
                            ElvanSettingsSwitch(
                                checked = profile.isBilingual,
                                onCheckedChange = { isChecked ->
                                    val updated = profile.copy(isBilingual = isChecked)
                                    TextilesProfileRepository.updateProfile(updated)
                                    ElvanSnackbar.show(saveSuccessMsg)
                                },
                                colors = colors
                            )
                        },
                        colors = colors
                    )

                    ElvanSettingsDivider(colors = colors)

                    // GST Split Toggle (Goods / Pattu Mode)
                    ElvanSimpleSettingsRow(
                        title = K.showGstSplitsInTable.tr(),
                        trailing = {
                            ElvanSettingsSwitch(
                                checked = profile.splitGst,
                                onCheckedChange = { isChecked ->
                                    val updated = profile.copy(splitGst = isChecked)
                                    TextilesProfileRepository.updateProfile(updated)
                                    ElvanSnackbar.show(saveSuccessMsg)
                                },
                                colors = colors
                            )
                        },
                        colors = colors
                    )
                }
            }

            // ── Section 3: Bilingual Rules (Shown when bilingual mode is active) ──
            if (profile.isBilingual) {
                item(key = "bilingual_rules_nav") {
                    ElvanSettingsSection(colors = colors) {
                        ElvanSettingsRow(
                            icon = MaterialSymbols.Rounded.Translate,
                            title = K.languageRules.tr(),
                            onClick = { onNavigate(SettingsRoute.BilingualRules) },
                            colors = colors
                        )
                    }
                }
            }

            // ── Section 4: PDF Theme Color (With explicit Save container) ──
            item(key = "theme_section") {
                com.elvan.udukkai.ui.screens.settings.ThemeColorSection(profile, colors) {
                    TextilesProfileRepository.updateProfile(it)
                }
            }

            // ── Section 5: Logo Placement ──
            item(key = "logo_place_section") {
                com.elvan.udukkai.ui.screens.settings.LogoPlaceSection(profile, false, colors) {
                    TextilesProfileRepository.updateProfile(it)
                    ElvanSnackbar.show(saveSuccessMsg)
                }
            }
        }
    }
}
