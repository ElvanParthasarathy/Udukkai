package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.data.settings.NiruvanaTharavugal
import com.elvan.udukkai.data.settings.NiruvanaTharavugalRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.core.platform.ConfigureDialogWindow
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * ManageProfilesModal — Fullscreen modal for business profiles management.
 * 1:1 port of Flutter's `showManageProfilesModal` (`thannuru_maeladukkugal.dart`).
 */
@Composable
fun ManageProfilesModal(
    mode: AppMode,
    onDismissRequest: () -> Unit,
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current

    val profiles = NiruvanaTharavugalRepository.getAllProfiles(mode)
    val activeProfile = NiruvanaTharavugalRepository.getProfile(mode)

    var showNewProfileSheet by remember { mutableStateOf(false) }
    var profileToDelete by remember { mutableStateOf<NiruvanaTharavugal?>(null) }

    val deleteSuccessMsg = K.profileDeleted.tr()
    val deletePrompt = K.deleteCompanyProfilePrompt.tr()
    val cancelLabel = K.cancel.tr()
    val deleteLabel = K.delete.tr()

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        ConfigureDialogWindow(isDark = colors.isDark)
        Scaffold(
            containerColor = colors.background,
            floatingActionButton = {
                if (profiles.size < NiruvanaTharavugalRepository.MAX_PROFILES) {
                    FloatingActionButton(
                        onClick = { showNewProfileSheet = true },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = if (colors.isDark) Color.White else Color.Black,
                        contentColor = if (colors.isDark) Color.Black else Color.White,
                        modifier = Modifier.padding(bottom = 32.dp, end = 8.dp)
                    ) {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.Add,
                            contentDescription = "New Profile",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Top Bar with Back icon and title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismissRequest) {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = K.manage.tr(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = colors.textPrimary
                    )
                }

                if (profiles.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
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
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            ElvanSettingsSection {
                                profiles.forEachIndexed { index, profileItem ->
                                    val isActive = profileItem.id == activeProfile.id
                                    val primaryName = profileItem.getPrimary("niruvanathinPeyar")
                                        .ifEmpty { K.activeCompany.tr() }
                                    val isBilingual = if (mode == AppMode.KOOLI) true else profileItem.iruMozhi
                                    val secondaryName = if (isBilingual) {
                                        profileItem.getSecondary("niruvanathinPeyar").takeIf { it.isNotBlank() }
                                    } else null

                                    ElvanSettingsDisplayRow(
                                        title = if (isActive) K.activeCompany.tr() else "",
                                        primaryValue = primaryName,
                                        secondaryValue = secondaryName,
                                        icon = MaterialSymbols.Rounded.DeleteForever,
                                        iconColor = if (!isActive) MaterialTheme.colorScheme.error else null,
                                        onEdit = if (!isActive) {
                                            { profileToDelete = profileItem }
                                        } else null,
                                        onTap = if (!isActive && profileItem.id != null) {
                                            {
                                                NiruvanaTharavugalRepository.setActiveProfile(mode, profileItem.id!!)
                                            }
                                        } else null
                                    )
                                    if (index < profiles.size - 1) {
                                        ElvanSettingsDivider()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Delete Confirm Action Sheet
        profileToDelete?.let { targetProfile ->
            com.elvan.udukkai.ui.components.shell.sheets.ElvanDeleteConfirmSheet(
                onDismissRequest = { profileToDelete = null },
                onConfirm = {
                    val id = targetProfile.id
                    if (id != null) {
                        NiruvanaTharavugalRepository.deleteProfile(mode, id)
                        ElvanSnackbar.show(deleteSuccessMsg)
                    }
                    profileToDelete = null
                },
                colors = colors
            )
        }

        // New Profile Action Sheet
        if (showNewProfileSheet) {
            NewProfileBottomSheet(
                mode = mode,
                onDismissRequest = { showNewProfileSheet = false },
                onSuccess = {
                    showNewProfileSheet = false
                }
            )
        }
    }
}
