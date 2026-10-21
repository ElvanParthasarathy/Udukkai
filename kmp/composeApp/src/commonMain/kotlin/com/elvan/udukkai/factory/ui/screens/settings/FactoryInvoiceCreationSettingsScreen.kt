package com.elvan.udukkai.factory.ui.screens.settings

import com.elvan.udukkai.data.settings.BusinessProfile
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.ShellDefaults
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.factory.data.FactoryProfileRepository
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * Invoice Creation Settings Screen.
 * Provides unified bilingual billing configuration for both Factory (Kooli) and Textiles (Pattu):
 * - Primary billing language selection
 * - Secondary billing language selection (when bilingual is active)
 * - Bilingual billing toggle (iruMozhi)
 * - GST split presentation toggle (for Textiles / Goods mode)
 * - PDF color theme customization
 */
@Composable
fun FactoryInvoiceCreationSettingsScreen(
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors(),
    customProfile: BusinessProfile? = null,
    onUpdateProfile: ((BusinessProfile) -> Unit)? = null,
    isTextiles: Boolean? = null
) {
    val isPattu = isTextiles ?: false
    val profile = customProfile ?: if (isPattu) com.elvan.udukkai.textiles.data.TextilesProfileRepository.profile else com.elvan.udukkai.factory.data.FactoryProfileRepository.profile
    val ff = LocalAppFontFamily.current

    var isEditingLanguages by remember { mutableStateOf(false) }
    var tempPrimaryLang by remember { mutableStateOf(profile.primaryLanguage) }
    var tempSecondaryLang by remember { mutableStateOf(profile.thunaiMozhi) }

    val bottomSheet = LocalElvanBottomSheetController.current
    val pdfThemeTitle = K.pdfTheme.tr()
    val saveSuccessMsg = K.profileSaved.tr()

    val greenLabel = K.green.tr()
    val purpleLabel = K.violet.tr()
    val tamilLabel = K.taCode.tr()
    val englishLabel = K.enCode.tr()

    fun getLanguageName(code: String): String {
        return if (code.lowercase().startsWith("ta")) tamilLabel else englishLabel
    }

    fun getThemeName(hex: String): String {
        return when (hex.lowercase()) {
            "#388e3c" -> greenLabel
            "#6a1b9a" -> purpleLabel
            else -> hex
        }
    }

    fun parseColor(hex: String): Color {
        return try {
            val cleanHex = hex.removePrefix("#")
            Color(cleanHex.toLong(16) or 0x00000000FF000000L)
        } catch (_: Exception) {
            Color(0xFF388E3C)
        }
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

            // ── Section 1: Billing Languages ──
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
                                                        tempSecondaryLang = profile.thunaiMozhi
                                                        isEditingLanguages = true
                                                    }
                                                )
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = MaterialSymbols.Rounded.Edit,
                                                    contentDescription = K.edit.tr(),
                                                    tint = colors.textPrimary.copy(alpha = 0.6f),
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
                                        description = getLanguageName(profile.thunaiMozhi),
                                        colors = colors
                                    )
                                }
                            }
                        },
                        editContent = {
                            ElvanSettingsEditContainer(
                                onCancel = { isEditingLanguages = false },
                                onSave = {
                                    val updated = profile.copy()
                                    updated.primaryLanguage = tempPrimaryLang
                                    updated.thunaiMozhi = tempSecondaryLang
                                    FactoryProfileRepository.updateProfile(updated)
                                    isEditingLanguages = false
                                    ElvanSnackbar.show(saveSuccessMsg)
                                },
                                colors = colors
                            ) {
                                // Primary Language Dropdown
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
                                    // Secondary Language Dropdown
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

            // ── Section 2: Switches (Bilingual & GST) ──
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
                                    FactoryProfileRepository.updateProfile(updated)
                                    ElvanSnackbar.show(saveSuccessMsg)
                                },
                                colors = colors
                            )
                        },
                        colors = colors
                    )


                }
            }

            // ── Section 3: PDF Theme ──
            item(key = "theme_section") {
                val currentThemeColorHex = profile.themeColor.ifEmpty { "#388e3c" }
                val currentColor = parseColor(currentThemeColorHex)

                ElvanSettingsSection(colors = colors) {
                    ElvanSettingsDisplayRow(
                        title = K.pdfTheme.tr(),
                        primaryValue = "",
                        primaryWidget = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(currentColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = getThemeName(currentThemeColorHex),
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = colors.textPrimary
                                )
                            }
                        },
                        onEdit = {
                            bottomSheet.showSelection(
                                title = pdfThemeTitle,
                                items = listOf("#388e3c", "#6a1b9a"),
                                currentValue = profile.themeColor.ifEmpty { "#388e3c" },
                                itemLabelBuilder = { getThemeName(it) },
                                leadingBuilder = { colorHex ->
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(parseColor(colorHex))
                                    )
                                },
                                onSelected = { selectedColor ->
                                    val updated = profile.copy()
                                    updated.themeColor = selectedColor
                                    FactoryProfileRepository.updateProfile(updated)
                                    ElvanSnackbar.show(saveSuccessMsg)
                                }
                            )
                        },
                        colors = colors
                    )
                }
            }
        }
    }
}
