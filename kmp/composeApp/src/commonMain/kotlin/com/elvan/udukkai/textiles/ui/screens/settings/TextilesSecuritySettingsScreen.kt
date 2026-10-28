package com.elvan.udukkai.textiles.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.elvan.udukkai.core.auth.AuthManager
import com.elvan.udukkai.core.backup.getUdukkaiBackupService
import com.elvan.udukkai.core.backup.reloadTextilesRepositories
import com.elvan.udukkai.core.platform.getPreferencesHelper
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.textiles.data.TextilesProfileRepository
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Textiles Security Settings Screen (சவுளி பாதுகாப்பு அமைப்புகள்).
 * Dedicated to Udukkai Textiles with zero cross-app dependencies.
 */
@Composable
fun TextilesSecuritySettingsScreen(
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val scope = rememberCoroutineScope()

    var showSyncLoading by remember { mutableStateOf(false) }
    var showSignOutConfirm by remember { mutableStateOf(false) }
    var showSignOutLoading by remember { mutableStateOf(false) }

    var showEraseStep1 by remember { mutableStateOf(false) }
    var showEraseStep2 by remember { mutableStateOf(false) }
    var showEraseLoading by remember { mutableStateOf(false) }

    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }

    val syncSuccessMsg = K.synced.tr()
    val signOutSuccessMsg = K.signedOutSuccessfully.tr()
    val eraseSuccessMsg = K.appDataErased.tr()
    val emailMismatchMsg = K.emailMismatch.tr()
    val passwordRequiredMsg = K.passwordRequired.tr()

    val currentProfile = TextilesProfileRepository.profile
    val registeredEmail = AuthManager.signedInEmail.orEmpty()
    val confirmEmailPlaceholder = if (registeredEmail.isNotBlank()) registeredEmail else K.confirmEmail.tr()

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

        item {
            ElvanSettingsSection(colors = colors) {
                // 1. Sync App (Orunginai Cheyali)
                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.Sync,
                    title = K.syncAppBtn.tr(),
                    description = K.syncRecentChanges.tr(),
                    onClick = {
                        showSyncLoading = true
                        scope.launch {
                            try {
                                reloadTextilesRepositories()
                            } catch (_: Exception) {}
                            try {
                                com.elvan.udukkai.core.sync.getFirebaseSyncManager().triggerManualSync()
                            } catch (_: Exception) {}
                            delay(600)
                            showSyncLoading = false
                            ElvanSnackbar.show(syncSuccessMsg)
                        }
                    },
                    colors = colors
                )
                ElvanSettingsDivider(colors = colors)

                // 2. Sign Out (Veliyaeru)
                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.Logout,
                    title = K.logout.tr(),
                    description = K.logoutApplication.tr(),
                    onClick = { showSignOutConfirm = true },
                    colors = colors
                )
                ElvanSettingsDivider(colors = colors)

                // 3. Erase App Data (Cheyali Tharavugalai Azhi)
                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.DeleteForever,
                    title = K.eraseAppData.tr(),
                    description = K.permanentlyEraseData.tr(),
                    onClick = {
                        emailInput = ""
                        passwordInput = ""
                        showEraseStep1 = true
                    },
                    colors = colors
                )
            }
        }
    }

    // Sign Out Confirmation Action Sheet
    if (showSignOutConfirm) {
        ElvanActionSheet(
            title = K.logout.tr(),
            cancelText = K.cancelBtn.tr(),
            confirmText = K.logout.tr(),
            confirmColor = Color(0xFFBA1A1A),
            onDismissRequest = { showSignOutConfirm = false },
            onConfirm = {
                showSignOutConfirm = false
                showSignOutLoading = true
                scope.launch {
                    try {
                        AuthManager.logout()
                    } catch (_: Exception) {}
                    delay(500)
                    showSignOutLoading = false
                    ElvanSnackbar.show(signOutSuccessMsg)
                }
            },
            colors = colors
        )
    }

    // Erase Data Step 1: Email Confirmation
    if (showEraseStep1) {
        ElvanActionSheet(
            title = K.eraseAppData.tr(),
            cancelText = K.cancelBtn.tr(),
            confirmText = K.continueText.tr(),
            confirmColor = Color(0xFFBA1A1A),
            customContent = {
                ElvanSettingsTextField(
                    label = K.email.tr(),
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    placeholder = confirmEmailPlaceholder,
                    colors = colors
                )
            },
            onDismissRequest = { showEraseStep1 = false },
            onConfirm = {
                val input = emailInput.trim()
                if (registeredEmail.isNotBlank() && input.equals(registeredEmail, ignoreCase = true)) {
                    showEraseStep1 = false
                    showEraseStep2 = true
                } else {
                    ElvanSnackbar.show(emailMismatchMsg)
                }
            },
            colors = colors
        )
    }

    // Erase Data Step 2: Password Confirmation
    if (showEraseStep2) {
        ElvanActionSheet(
            title = K.eraseAppData.tr(),
            cancelText = K.cancelBtn.tr(),
            confirmText = K.eraseDataBtn.tr(),
            confirmColor = Color(0xFFBA1A1A),
            customContent = {
                androidx.compose.material3.Text(
                    text = K.eraseConfirmFinal.tr(),
                    color = colors.textSecondary
                )
            },
            onDismissRequest = { showEraseStep2 = false },
            onConfirm = {
                run {
                    showEraseStep2 = false
                    showEraseLoading = true
                    scope.launch {
                        try {
                            getUdukkaiBackupService().createSafetySnapshot()

                            // Wipe only Textiles tables
                            try { com.elvan.udukkai.data.database.getTextilesDatabaseHelper().clearAllData() } catch (_: Exception) {}
                            try { TextilesProfileRepository.clearProfiles() } catch (_: Exception) {}

                            reloadTextilesRepositories()

                            try {
                                getPreferencesHelper().clear()
                            } catch (_: Exception) {}

                            AuthManager.logout()
                            AuthManager.refreshProfileStatus()
                        } catch (_: Exception) {}

                        delay(600)
                        showEraseLoading = false
                        ElvanSnackbar.show(eraseSuccessMsg)
                    }
                }
            },
            colors = colors
        )
    }

    if (showSyncLoading) {
        ElvanLoadingOverlay(
            text = K.syncing.tr(),
            colors = colors
        )
    }

    if (showSignOutLoading) {
        ElvanLoadingOverlay(
            text = K.signingOut.tr(),
            colors = colors
        )
    }

    if (showEraseLoading) {
        ElvanLoadingOverlay(
            text = K.erasing.tr(),
            colors = colors
        )
    }
}
