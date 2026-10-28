package com.elvan.udukkai.factory.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.backup.getUdukkaiBackupService
import com.elvan.udukkai.data.settings.BusinessProfile
import com.elvan.udukkai.factory.data.FactoryProfileRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.components.shell.sheets.ElvanDeleteConfirmSheet
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * Factory Manage Profiles Screen (ஆலை நிறுவனங்களை நிர்வகி).
 * Dedicated to Udukkai Factory with zero cross-app branching or flags.
 */
@Composable
fun FactoryManageProfilesScreen(
    onBack: () -> Unit = {},
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current
    val profiles = FactoryProfileRepository.profiles
    val activeProfile = FactoryProfileRepository.profile

    var showNewProfileSheet by remember { mutableStateOf(false) }
    var profileToDelete by remember { mutableStateOf<BusinessProfile?>(null) }
    var nameInput by remember { mutableStateOf("") }

    val deleteSuccessMsg = K.profileDeleted.tr()
    val nameMismatchMsg = K.businessNameMismatch.tr()
    val lastBusinessMsg = K.lastBusinessCannotBeDeleted.tr()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = 48.dp + 72.dp
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
        ) {
            item(key = "spacer_top") {
                Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
            }

            if (profiles.isEmpty()) {
                item(key = "empty_state") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = K.noSavedProfiles.tr(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 16.sp,
                                color = colors.textPrimary.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            } else {
                item(key = "profiles_list") {
                    ElvanSectionContainer {
                        ElvanSettingsSection(colors = colors) {
                            profiles.forEachIndexed { index, profileItem ->
                                val isActive = profileItem.id == activeProfile.id
                                val primaryName = profileItem.getPrimary("businessName")
                                    .ifEmpty { K.activeCompany.tr() }
                                val isBilingual = profileItem.isBilingual

                                ElvanSettingsDisplayRow(
                                    title = if (isActive) K.activeCompany.tr() else "",
                                    primaryValue = primaryName,
                                    secondaryValue = if (isBilingual) profileItem.getSecondary("niruvanathinPeyar").takeIf { it.isNotBlank() } else null,
                                    icon = MaterialSymbols.Rounded.Delete,
                                    iconColor = null,
                                    onEdit = if (profiles.size > 1) { { profileToDelete = profileItem; nameInput = "" } } else null,
                                    onTap = if (!isActive && profileItem.id != null) {
                                        {
                                            FactoryProfileRepository.setActiveProfile(profileItem.id!!)
                                        }
                                    } else null,
                                    colors = colors
                                )
                                if (index < profiles.size - 1) {
                                    ElvanSettingsDivider(colors = colors)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (profiles.size < 5) {
            FloatingActionButton(
                onClick = { showNewProfileSheet = true },
                shape = RoundedCornerShape(16.dp),
                containerColor = if (colors.isDark) Color.White else Color.Black,
                contentColor = if (colors.isDark) Color.Black else Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 48.dp, end = 20.dp)
            ) {
                Icon(
                    imageVector = MaterialSymbols.Rounded.Add,
                    contentDescription = "New Profile",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    profileToDelete?.let { targetProfile ->
        val expectedName = targetProfile.getPrimary("businessName")
        ElvanActionSheet(
            title = K.deleteCompanyProfilePrompt.tr(),
            cancelText = K.cancel.tr(),
            confirmText = K.delete.tr(),
            confirmColor = Color(0xFFBA1A1A),
            customContent = {
                ElvanSettingsTextField(
                    label = K.typeNameToDelete.tr(),
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    placeholder = expectedName,
                    colors = colors
                )
            },
            onDismissRequest = { profileToDelete = null },
            onConfirm = {
                val id = targetProfile.id
                if (!nameInput.trim().equals(expectedName.trim(), ignoreCase = true)) {
                    ElvanSnackbar.show(nameMismatchMsg)
                } else if (id != null) {
                    // The data of every business is kept in a safety snapshot before one is removed
                    try { getUdukkaiBackupService().createSafetySnapshot() } catch (_: Exception) {}
                    if (FactoryProfileRepository.deleteProfile(id)) {
                        ElvanSnackbar.show(deleteSuccessMsg)
                    } else {
                        ElvanSnackbar.show(lastBusinessMsg)
                    }
                    profileToDelete = null
                }
            },
            colors = colors
        )
    }

    if (showNewProfileSheet) {
        FactoryNewProfileBottomSheet(
            onDismissRequest = { showNewProfileSheet = false },
            onSuccess = { showNewProfileSheet = false },
            colors = colors
        )
    }
}

@Composable
fun FactoryNewProfileBottomSheet(
    onDismissRequest: () -> Unit,
    onSuccess: () -> Unit,
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current
    var newNamePrimary by remember { mutableStateOf("") }
    var newNameSecondary by remember { mutableStateOf("") }

    val fieldBg = if (colors.isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

    val maxProfilesMsg = K.maxFiveProfiles.tr()
    val saveSuccessMsg = K.profileSaved.tr()
    val createTitle = K.createNewProfile.tr()
    val cancelLabel = K.cancel.tr()
    val createLabel = K.make.tr()

    ElvanActionSheet(
        title = createTitle,
        cancelText = cancelLabel,
        confirmText = createLabel,
        onDismissRequest = onDismissRequest,
        onConfirm = {
            if (newNamePrimary.trim().isNotEmpty()) {
                val profiles = FactoryProfileRepository.profiles
                if (profiles.size >= 5) {
                    ElvanSnackbar.show(maxProfilesMsg)
                    return@ElvanActionSheet
                }

                val newProfile = BusinessProfile(
                    primaryLanguage = "ta",
                    secondaryLanguage = "en",
                    isBilingual = true,
                    splitGst = false,
                    businessName = mutableMapOf(
                        "ta" to newNamePrimary.trim(),
                        "en" to newNameSecondary.trim()
                    ),
                    shortName = newNamePrimary.trim().take(15),
                    country = mutableMapOf(),
                    headerLayout = "small",
                    themeColor = "#388e3c"
                )
                val success = FactoryProfileRepository.createProfile(newProfile)
                if (success) {
                    ElvanSnackbar.show(saveSuccessMsg)
                    onSuccess()
                }
            }
        },
        customContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(fieldBg, RoundedCornerShape(100))
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (newNamePrimary.isEmpty()) {
                        Text(
                            text = "${K.companyName.tr()} (${K.taCode.tr()})",
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            ),
                            color = colors.textPrimary.copy(alpha = 0.4f)
                        )
                    }
                    BasicTextField(
                        value = newNamePrimary,
                        onValueChange = { newNamePrimary = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            fontFamily = ff,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary,
                            textAlign = TextAlign.Center
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(colors.accent)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(fieldBg, RoundedCornerShape(100))
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (newNameSecondary.isEmpty()) {
                        Text(
                            text = "${K.companyName.tr()} (${K.enCode.tr()})",
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            ),
                            color = colors.textPrimary.copy(alpha = 0.4f)
                        )
                    }
                    BasicTextField(
                        value = newNameSecondary,
                        onValueChange = { newNameSecondary = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            fontFamily = ff,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary,
                            textAlign = TextAlign.Center
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(colors.accent)
                    )
                }
            }
        },
        colors = colors
    )
}
