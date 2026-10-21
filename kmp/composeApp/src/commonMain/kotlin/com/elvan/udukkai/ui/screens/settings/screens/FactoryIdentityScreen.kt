package com.elvan.udukkai.ui.screens.settings.screens

import com.elvan.udukkai.factory.data.FactoryProfileRepository

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
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * Factory (ஆலை) Identity & Branding Screen.
 * Supports Logo and Signature (with official signatory name) editing.
 */
@Composable
fun FactoryIdentityScreen(
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val profile = FactoryProfileRepository.profile
    val ff = LocalAppFontFamily.current

    var editingSection by remember { mutableStateOf<String?>(null) }
    var tempImagePath by remember { mutableStateOf<String?>(null) }
    var tempSignatoryFirstName by remember { mutableStateOf("") }
    var tempSignatoryLastName by remember { mutableStateOf("") }
    var activePickerField by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberImagePicker { path ->
        when (activePickerField) {
            "logo" -> tempImagePath = path
            "kaiyoppam" -> tempImagePath = path
        }
        activePickerField = null
    }

    val logoPath = profile.logo.ifEmpty { null }
    val signaturePath = profile.signature.ifEmpty { null }
    val signatoryName = profile.fullSignatoryName
    val saveSuccessMsg = K.profileSaved.tr()

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
                    // 1. Logo (Niruvanathin Oavuru)
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
                                    updated.logo = tempImagePath ?: ""
                                    FactoryProfileRepository.updateProfile(updated)
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

                    // 2. Signature (Kaiyoppam & Oppam Peyar)
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
                                    tempSignatoryFirstName = profile.signatoryFirstName
                                    tempSignatoryLastName = profile.signatoryLastName
                                    if (tempSignatoryFirstName.isEmpty() && tempSignatoryLastName.isEmpty() && profile.signatoryName.isNotEmpty()) {
                                        val parts = profile.signatoryName.trim().split(" ", limit = 2)
                                        tempSignatoryFirstName = parts.firstOrNull() ?: ""
                                        tempSignatoryLastName = if (parts.size > 1) parts[1] else ""
                                    }
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
                                    updated.signature = tempImagePath ?: ""
                                    updated.signatoryFirstName = tempSignatoryFirstName.trim()
                                    updated.signatoryLastName = tempSignatoryLastName.trim()
                                    updated.signatoryName = updated.fullSignatoryName
                                    FactoryProfileRepository.updateProfile(updated)
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
                                    label = K.firstName.tr(),
                                    value = tempSignatoryFirstName,
                                    onValueChange = { tempSignatoryFirstName = it },
                                    placeholder = K.firstName.tr(),
                                    colors = colors
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                ElvanSettingsTextField(
                                    label = K.lastName.tr(),
                                    value = tempSignatoryLastName,
                                    onValueChange = { tempSignatoryLastName = it },
                                    placeholder = K.lastName.tr(),
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
            // Delete button at top-right
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

@Deprecated("Use FactoryIdentityScreen", ReplaceWith("FactoryIdentityScreen(scrollState, colors)"))
@Composable
fun CoolieIdentityScreen(
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) = FactoryIdentityScreen(scrollState, colors)
