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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.mode.AppMode
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
 * Pattu Identity & Branding Screen matching Flutter's `pattu_niruvana_adaiyalangal_thirai.dart` 1:1.
 * Supports Logo, Wide Logo, Header Style (small vs wide), and Signature with official signatory name.
 */
@Composable
fun SilkIdentityScreen(
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val profile = NiruvanaTharavugalRepository.getProfile(AppMode.PATTU)
    val ff = LocalAppFontFamily.current

    var editingSection by remember { mutableStateOf<String?>(null) }
    var tempImagePath by remember { mutableStateOf<String?>(null) }
    var tempWideImagePath by remember { mutableStateOf<String?>(null) }
    var tempHeaderStyle by remember { mutableStateOf(profile.thalaippuVadivu.ifEmpty { "small" }) }
    var tempSignatoryName by remember { mutableStateOf("") }
    var activePickerField by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberImagePicker { path ->
        when (activePickerField) {
            "logo" -> tempImagePath = path
            "wide_logo" -> tempWideImagePath = path
            "kaiyoppam" -> tempImagePath = path
        }
        activePickerField = null
    }

    val logoPath = profile.oavuru.ifEmpty { null }
    val wideLogoPath = profile.agalaOavuru.ifEmpty { null }
    val headerStyle = profile.thalaippuVadivu.ifEmpty { "small" }
    val signaturePath = profile.kaiyoppam.ifEmpty { null }
    val signatoryName = profile.oppamPeyar

    val smallLabel = K.smallLogoCompanyName.tr()
    val wideLabel = K.wideLogoOnly.tr()
    val saveSuccessMsg = K.profileSaved.tr()
    val headerStyleTitle = K.billHeaderStyle.tr()
    val bottomSheet = LocalElvanBottomSheetController.current

    LazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            bottom = Dimens.SubpageContentPaddingBottom
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
    ) {
        // Top spacer driven by One UI collapsible header
        item(key = "shell_top_spacer") {
            Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
        }

        item(key = "identity_section") {
            ElvanSectionContainer {
                ElvanSettingsSection(colors = colors) {
                // 1. Logo (Square / Icon)
                ElvanSettingsAnimatedExpand(
                    isEditing = editingSection == "logo",
                    displayContent = {
                        ElvanSettingsDisplayRow(
                            title = K.companyLogo.tr(),
                            primaryValue = if (logoPath != null) "" else K.noLogo.tr(),
                            primaryWidget = if (logoPath != null) {
                                {
                                    ElvanImageViewer(
                                        value = logoPath,
                                        modifier = Modifier.height(36.dp).clip(RoundedCornerShape(6.dp)),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            } else null,
                            onEdit = {
                                tempImagePath = logoPath
                                editingSection = "logo"
                            },
                            colors = colors
                        )
                    },
                    editContent = {
                        ElvanSettingsEditContainer(
                            title = K.companyLogo.tr(),
                            onCancel = { editingSection = null },
                            onSave = {
                                val updated = profile.copy()
                                updated.oavuru = tempImagePath ?: ""
                                NiruvanaTharavugalRepository.updateProfile(AppMode.PATTU, updated)
                                editingSection = null
                                ElvanSnackbar.show(saveSuccessMsg)
                            },
                            colors = colors
                        ) {
                            ImageUploadBox(
                                imagePath = tempImagePath,
                                onPick = {
                                    activePickerField = "logo"
                                    imagePicker.launch()
                                },
                                onClear = { tempImagePath = null },
                                colors = colors
                            )
                        }
                    }
                )
                ElvanSettingsDivider(colors = colors)

                // 2. Wide Logo (Agala Oavuru)
                ElvanSettingsAnimatedExpand(
                    isEditing = editingSection == "wide_logo",
                    displayContent = {
                        ElvanSettingsDisplayRow(
                            title = K.verticalWideLogo.tr(),
                            primaryValue = if (wideLogoPath != null) "" else K.none.tr(),
                            primaryWidget = if (wideLogoPath != null) {
                                {
                                    ElvanImageViewer(
                                        value = wideLogoPath,
                                        modifier = Modifier.height(36.dp).clip(RoundedCornerShape(6.dp)),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            } else null,
                            onEdit = {
                                tempWideImagePath = wideLogoPath
                                editingSection = "wide_logo"
                            },
                            colors = colors
                        )
                    },
                    editContent = {
                        ElvanSettingsEditContainer(
                            title = K.verticalWideLogo.tr(),
                            onCancel = { editingSection = null },
                            onSave = {
                                val updated = profile.copy()
                                updated.agalaOavuru = tempWideImagePath ?: ""
                                NiruvanaTharavugalRepository.updateProfile(AppMode.PATTU, updated)
                                editingSection = null
                                ElvanSnackbar.show(saveSuccessMsg)
                            },
                            colors = colors
                        ) {
                            ImageUploadBox(
                                imagePath = tempWideImagePath,
                                onPick = {
                                    activePickerField = "wide_logo"
                                    imagePicker.launch()
                                },
                                onClear = { tempWideImagePath = null },
                                colors = colors
                            )
                        }
                    }
                )
                ElvanSettingsDivider(colors = colors)

                // 3. Header Style (Chinnathin Vadivam)
                ElvanSettingsAnimatedExpand(
                    isEditing = editingSection == "header_style",
                    displayContent = {
                        ElvanSettingsDisplayRow(
                            title = K.billHeaderStyle.tr(),
                            primaryValue = if (headerStyle == "wide") wideLabel else smallLabel,
                            onEdit = {
                                tempHeaderStyle = headerStyle
                                editingSection = "header_style"
                            },
                            colors = colors
                        )
                    },
                    editContent = {
                        ElvanSettingsEditContainer(
                            title = K.billHeaderStyle.tr(),
                            onCancel = { editingSection = null },
                            onSave = {
                                val updated = profile.copy()
                                updated.thalaippuVadivu = tempHeaderStyle
                                NiruvanaTharavugalRepository.updateProfile(AppMode.PATTU, updated)
                                editingSection = null
                                ElvanSnackbar.show(saveSuccessMsg)
                            },
                            colors = colors
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(100))
                                    .background(colors.textPrimary.copy(alpha = 0.08f))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ShellDefaults.ripple(colors, bounded = true),
                                        onClick = {
                                            bottomSheet.showSelection(
                                                title = headerStyleTitle,
                                                items = listOf("small", "wide"),
                                                currentValue = tempHeaderStyle,
                                                itemLabelBuilder = { if (it == "wide") wideLabel else smallLabel },
                                                onSelected = { tempHeaderStyle = it }
                                            )
                                        }
                                    )
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (tempHeaderStyle == "wide") wideLabel else smallLabel,
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Normal
                                        ),
                                        color = colors.textPrimary
                                    )
                                    Icon(
                                        imageVector = MaterialSymbols.Rounded.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = colors.textPrimary.copy(alpha = 0.6f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                )
                ElvanSettingsDivider(colors = colors)

                // 4. Signature (Kaiyoppam & Oppam Peyar)
                ElvanSettingsAnimatedExpand(
                    isEditing = editingSection == "kaiyoppam",
                    displayContent = {
                        ElvanSettingsDisplayRow(
                            title = K.signatureStamp.tr(),
                            primaryValue = if (signaturePath != null) signatoryName else K.noSignature.tr(),
                            primaryWidget = if (signaturePath != null) {
                                {
                                    ElvanImageViewer(
                                        value = signaturePath,
                                        modifier = Modifier.height(48.dp).clip(RoundedCornerShape(6.dp)),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            } else null,
                            onEdit = {
                                tempImagePath = signaturePath
                                tempSignatoryName = signatoryName
                                editingSection = "kaiyoppam"
                            },
                            colors = colors
                        )
                    },
                    editContent = {
                        ElvanSettingsEditContainer(
                            title = K.signatureStamp.tr(),
                            onCancel = { editingSection = null },
                            onSave = {
                                val updated = profile.copy()
                                updated.kaiyoppam = tempImagePath ?: ""
                                updated.oppamPeyar = tempSignatoryName
                                NiruvanaTharavugalRepository.updateProfile(AppMode.PATTU, updated)
                                editingSection = null
                                ElvanSnackbar.show(saveSuccessMsg)
                            },
                            colors = colors
                        ) {
                            ImageUploadBox(
                                imagePath = tempImagePath,
                                onPick = {
                                    activePickerField = "kaiyoppam"
                                    imagePicker.launch()
                                },
                                onClear = { tempImagePath = null },
                                colors = colors
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            ElvanSettingsTextField(
                                label = K.authorizedSignatoryName.tr(),
                                value = tempSignatoryName,
                                onValueChange = { tempSignatoryName = it },
                                placeholder = K.authorizedSignatoryName.tr(),
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

@Composable
private fun ImageUploadBox(
    imagePath: String?,
    onPick: () -> Unit,
    onClear: () -> Unit,
    colors: ShellColors
) {
    val ff = LocalAppFontFamily.current
    val hasImage = !imagePath.isNullOrEmpty()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.iconBg)
            .clickable(onClick = { if (!hasImage) onPick() }),
        contentAlignment = Alignment.Center
    ) {
        if (hasImage) {
            ElvanImageViewer(
                value = imagePath,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .padding(8.dp),
                contentScale = ContentScale.Fit
            )
            Surface(
                shape = CircleShape,
                color = colors.surface,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onClear)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.DeleteForever,
                        contentDescription = K.delete.tr(),
                        tint = Color(0xFFBA1A1A),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = MaterialSymbols.Rounded.CloudUpload,
                    contentDescription = null,
                    tint = colors.textPrimary.copy(alpha = 0.4f),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = K.upload.tr(),
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = colors.textPrimary.copy(alpha = 0.5f)
                )
            }
        }
    }
}
