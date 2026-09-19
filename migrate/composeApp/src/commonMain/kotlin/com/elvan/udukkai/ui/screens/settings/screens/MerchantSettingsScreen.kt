package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.mode.LocalAppMode
import com.elvan.udukkai.data.settings.NiruvanaTharavugalRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.ShellDefaults
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * Company Settings (நிறுவன அமைப்புகள்) Screen matching Flutter's `niruvana_amaippugal_thirai.dart` 1:1.
 * Supports smooth accordion edit expand/collapse with bilingual values and live repository updates.
 */
@Composable
fun MerchantSettingsScreen(
    onNavigateToManageProfiles: () -> Unit = {},
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val currentMode = LocalAppMode.current
    val profile = NiruvanaTharavugalRepository.getProfile(currentMode)
    val ff = LocalAppFontFamily.current

    var editingSection by remember { mutableStateOf<String?>(null) }

    // Temporary editing states
    var tempPrimary by remember { mutableStateOf("") }
    var tempSecondary by remember { mutableStateOf("") }
    var showExtraPhone by remember { mutableStateOf(false) }

    val isPattu = currentMode == AppMode.PATTU
    val isBilingual = if (!isPattu) true else profile.iruMozhi
    val saveSuccessMsg = K.profileSaved.tr()
    val defaultProfileName = K.activeCompany.tr()
    val selectCompanyTitle = K.selectCompany.tr()
    val bottomSheet = LocalElvanBottomSheetController.current

    fun saveField(action: () -> Unit) {
        action()
        NiruvanaTharavugalRepository.updateProfile(currentMode, profile)
        editingSection = null
        showExtraPhone = false
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

            // ── Top Profile Switcher Row (matching Flutter's _buildProfileSwitcher) ──
            item(key = "profile_switcher") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular Briefcase Button (aspectRatio 1.0)
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ShellDefaults.ripple(colors, bounded = true),
                                onClick = onNavigateToManageProfiles
                            ),
                        shape = CircleShape,
                        color = colors.surface,
                        shadowElevation = 0.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = MaterialSymbols.Rounded.BusinessCenter,
                                contentDescription = null,
                                tint = colors.textPrimary.copy(alpha = 0.7f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Profile Dropdown Pill (ElvanNiruvanamKeezhvirivuKooru)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(100))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ShellDefaults.ripple(colors, bounded = true),
                                onClick = {
                                    bottomSheet.showSelection(
                                        title = selectCompanyTitle,
                                        items = NiruvanaTharavugalRepository.getAllProfiles(currentMode),
                                        currentValue = profile,
                                        itemLabelBuilder = { it.getPrimary("niruvanathinPeyar").ifEmpty { defaultProfileName } },
                                        subtitleBuilder = { if (if (!isPattu) true else it.iruMozhi) it.getSecondary("niruvanathinPeyar").takeIf { s -> s.isNotBlank() } else null },
                                        onSelected = { selected ->
                                            if (selected.id != null) {
                                                NiruvanaTharavugalRepository.setActiveProfile(currentMode, selected.id!!)
                                            }
                                        }
                                    )
                                }
                            ),
                        shape = RoundedCornerShape(100),
                        color = colors.surface,
                        shadowElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = profile.getPrimary("niruvanathinPeyar").ifEmpty { K.activeCompany.tr() },
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = MaterialSymbols.Rounded.KeyboardArrowDown,
                                contentDescription = null,
                                tint = colors.textPrimary.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

        // ── Form Section ──
        item {
            ElvanSettingsSection(colors = colors) {
                // 1. Business Name (நிறுவனத்தின் பெயர்)
                ElvanSettingsAnimatedExpand(
                    isEditing = editingSection == "niruvanathinPeyar",
                    displayContent = {
                        ElvanSettingsDisplayRow(
                            title = K.companyName.tr(),
                            primaryValue = profile.getPrimary("niruvanathinPeyar"),
                            secondaryValue = if (isBilingual) profile.getSecondary("niruvanathinPeyar") else null,
                            onEdit = {
                                tempPrimary = profile.getPrimary("niruvanathinPeyar")
                                tempSecondary = profile.getSecondary("niruvanathinPeyar")
                                editingSection = "niruvanathinPeyar"
                            },
                            colors = colors
                        )
                    },
                    editContent = {
                        ElvanSettingsEditContainer(
                            title = K.companyName.tr(),
                            onCancel = { editingSection = null },
                            onSave = {
                                saveField {
                                    profile.setBilingual("niruvanathinPeyar", "ta", tempPrimary)
                                    profile.setBilingual("niruvanathinPeyar", "en", tempSecondary)
                                }
                            },
                            colors = colors
                        ) {
                            ElvanSettingsTextField(
                                label = "${K.companyName.tr()} (${K.taCode.tr()})",
                                value = tempPrimary,
                                onValueChange = { tempPrimary = it },
                                colors = colors
                            )
                            if (isBilingual) {
                                Spacer(modifier = Modifier.height(16.dp))
                                ElvanSettingsTextField(
                                    label = "${K.companyName.tr()} (${K.enCode.tr()})",
                                    value = tempSecondary,
                                    onValueChange = { tempSecondary = it },
                                    colors = colors
                                )
                            }
                        }
                    }
                )

                ElvanSettingsDivider(colors = colors)

                // 2. Short Business Name (குறுகிய நிறுவனப் பெயர்)
                ElvanSettingsAnimatedExpand(
                    isEditing = editingSection == "kurumPeyar",
                    displayContent = {
                        ElvanSettingsDisplayRow(
                            title = K.shortCompanyName.tr(),
                            primaryValue = profile.kurumPeyar,
                            onEdit = {
                                tempPrimary = profile.kurumPeyar
                                editingSection = "kurumPeyar"
                            },
                            colors = colors
                        )
                    },
                    editContent = {
                        ElvanSettingsEditContainer(
                            title = K.shortCompanyName.tr(),
                            onCancel = { editingSection = null },
                            onSave = {
                                saveField {
                                    profile.kurumPeyar = tempPrimary
                                }
                            },
                            colors = colors
                        ) {
                            ElvanSettingsTextField(
                                label = K.shortCompanyName.tr(),
                                value = tempPrimary,
                                onValueChange = { tempPrimary = it },
                                colors = colors
                            )
                        }
                    }
                )

                ElvanSettingsDivider(colors = colors)

                // 3. Tagline (அடைமொழி)
                ElvanSettingsAnimatedExpand(
                    isEditing = editingSection == "adaimozhi",
                    displayContent = {
                        ElvanSettingsDisplayRow(
                            title = K.tagline.tr(),
                            primaryValue = profile.getPrimary("adaimozhi"),
                            secondaryValue = if (isBilingual) profile.getSecondary("adaimozhi") else null,
                            onEdit = {
                                tempPrimary = profile.getPrimary("adaimozhi")
                                tempSecondary = profile.getSecondary("adaimozhi")
                                editingSection = "adaimozhi"
                            },
                            colors = colors
                        )
                    },
                    editContent = {
                        ElvanSettingsEditContainer(
                            title = K.tagline.tr(),
                            onCancel = { editingSection = null },
                            onSave = {
                                saveField {
                                    profile.setBilingual("adaimozhi", "ta", tempPrimary)
                                    profile.setBilingual("adaimozhi", "en", tempSecondary)
                                }
                            },
                            colors = colors
                        ) {
                            ElvanSettingsTextField(
                                label = if (isBilingual) "${K.tagline.tr()} (${K.taCode.tr()})" else K.tagline.tr(),
                                value = tempPrimary,
                                onValueChange = { tempPrimary = it },
                                colors = colors
                            )
                            if (isBilingual) {
                                Spacer(modifier = Modifier.height(16.dp))
                                ElvanSettingsTextField(
                                    label = "${K.tagline.tr()} (${K.enCode.tr()})",
                                    value = tempSecondary,
                                    onValueChange = { tempSecondary = it },
                                    colors = colors
                                )
                            }
                        }
                    }
                )

                ElvanSettingsDivider(colors = colors)

                // 4. Phone Numbers (பேசி எண்கள்)
                ElvanSettingsAnimatedExpand(
                    isEditing = editingSection == "tholaipesigal",
                    displayContent = {
                        ElvanSettingsDisplayRow(
                            title = K.phoneNumbers.tr(),
                            primaryValue = profile.tholaipaesi1,
                            secondaryValue = profile.tholaipaesi2.ifEmpty { null },
                            onEdit = {
                                tempPrimary = profile.tholaipaesi1
                                tempSecondary = profile.tholaipaesi2
                                showExtraPhone = profile.tholaipaesi2.isNotEmpty()
                                editingSection = "tholaipesigal"
                            },
                            colors = colors
                        )
                    },
                    editContent = {
                        ElvanSettingsEditContainer(
                            title = K.phoneNumbers.tr(),
                            extraAction = if (!showExtraPhone) {
                                {
                                    TextButton(
                                        onClick = { showExtraPhone = true },
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = "+ ${K.add.tr()}",
                                            style = TextStyle(
                                                fontFamily = ff,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = colors.accent
                                            )
                                        )
                                    }
                                }
                            } else null,
                            onCancel = {
                                editingSection = null
                                showExtraPhone = false
                            },
                            onSave = {
                                saveField {
                                    profile.tholaipaesi1 = tempPrimary
                                    profile.tholaipaesi2 = if (showExtraPhone) tempSecondary else ""
                                }
                            },
                            colors = colors
                        ) {
                            ElvanSettingsTextField(
                                label = K.phone.tr(),
                                value = tempPrimary,
                                onValueChange = { tempPrimary = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                maxLength = 13,
                                colors = colors
                            )
                            if (showExtraPhone) {
                                Spacer(modifier = Modifier.height(16.dp))
                                ElvanSettingsTextField(
                                    label = K.mobile.tr(),
                                    value = tempSecondary,
                                    onValueChange = { tempSecondary = it },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    maxLength = 13,
                                    trailingIcon = {
                                        IconButton(
                                            onClick = {
                                                tempSecondary = ""
                                                showExtraPhone = false
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = MaterialSymbols.Rounded.Close,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = colors.textPrimary.copy(alpha = 0.6f)
                                            )
                                        }
                                    },
                                    colors = colors
                                )
                            }
                        }
                    }
                )

                ElvanSettingsDivider(colors = colors)

                // 5. Email (மின்னஞ்சல்)
                ElvanSettingsAnimatedExpand(
                    isEditing = editingSection == "minnanjal",
                    displayContent = {
                        ElvanSettingsDisplayRow(
                            title = K.email.tr(),
                            primaryValue = profile.minnanjal,
                            onEdit = {
                                tempPrimary = profile.minnanjal
                                editingSection = "minnanjal"
                            },
                            colors = colors
                        )
                    },
                    editContent = {
                        ElvanSettingsEditContainer(
                            title = K.email.tr(),
                            onCancel = { editingSection = null },
                            onSave = {
                                saveField {
                                    profile.minnanjal = tempPrimary
                                }
                            },
                            colors = colors
                        ) {
                            ElvanSettingsTextField(
                                label = K.email.tr(),
                                value = tempPrimary,
                                onValueChange = { tempPrimary = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                colors = colors
                            )
                        }
                    }
                )

                // 6. GSTIN (Pattu only)
                if (isPattu) {
                    ElvanSettingsDivider(colors = colors)

                    ElvanSettingsAnimatedExpand(
                        isEditing = editingSection == "gstin",
                        displayContent = {
                            ElvanSettingsDisplayRow(
                                title = K.gstinTaxId.tr(),
                                primaryValue = profile.gstin,
                                onEdit = {
                                    tempPrimary = profile.gstin
                                    editingSection = "gstin"
                                },
                                colors = colors
                            )
                        },
                        editContent = {
                            ElvanSettingsEditContainer(
                                title = K.gstinTaxId.tr(),
                                onCancel = { editingSection = null },
                                onSave = {
                                    saveField {
                                        profile.gstin = tempPrimary.uppercase()
                                    }
                                },
                                colors = colors
                            ) {
                                ElvanSettingsTextField(
                                    label = K.gstinTaxId.tr(),
                                    value = tempPrimary,
                                    onValueChange = { tempPrimary = it.uppercase() },
                                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                                    maxLength = 15,
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

