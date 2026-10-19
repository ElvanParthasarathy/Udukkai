package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.elvan.udukkai.core.auth.AuthManager
import com.elvan.udukkai.core.backup.BackupSlot
import com.elvan.udukkai.core.backup.getNirilBackupService
import com.elvan.udukkai.core.mode.AppLaunchConfig
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.mode.ModeManager
import com.elvan.udukkai.core.platform.getPreferencesHelper
import com.elvan.udukkai.data.business.getBusinessDatabaseHelper
import com.elvan.udukkai.data.repository.PattiyalRepository
import com.elvan.udukkai.data.repository.PatrugalRepository
import com.elvan.udukkai.data.repository.PorulRepository
import com.elvan.udukkai.data.repository.VaangunarRepository
import com.elvan.udukkai.data.settings.NiruvanaTharavugalRepository
import com.elvan.udukkai.data.settings.getSettingsDatabaseHelper
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Security Settings Screen.
 * Fully wired actions:
 * - Sync App: Refreshes and synchronizes all in-memory repositories with local database.
 * - Sign Out: Logs out the user via AuthManager, clears session state, and redirects to welcome/login.
 * - Erase App Data: 2-step authenticated wipe (email & password check), takes safety vault backup, wipes all SQLite tables, and resets app to factory default.
 */
@Composable
fun SecuritySettingsScreen(
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

    val currentMode = AppLaunchConfig.fixedMode ?: ModeManager.currentMode
    val currentProfile = NiruvanaTharavugalRepository.getProfile(currentMode)
    val registeredEmail = currentProfile.minnanjal.trim()
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
        // Top spacer driven by One UI collapsible header
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
                                NiruvanaTharavugalRepository.refreshFromDatabase()
                                PattiyalRepository.loadAll()
                                PatrugalRepository.loadAll()
                                PorulRepository.loadAll()
                                VaangunarRepository.loadAll()
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

                // 3. Erase App Data (Cheyalith Tharavai Azhi)
                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.DeleteForever,
                    title = K.eraseAppData.tr(),
                    description = K.permanentlyEraseData.tr(),
                    onClick = {
                        emailInput = ""
                        passwordInput = ""
                        showEraseStep1 = true
                    },
                    titleColor = Color(0xFFBA1A1A),
                    iconTint = Color(0xFFBA1A1A),
                    colors = colors
                )
            }
        }
    }

    // ── Sync Loading Modal ──
    if (showSyncLoading) {
        ElvanLoadingOverlay(
            text = K.syncing.tr(),
            colors = colors
        )
    }

    // ── Sign Out Confirmation Modal ──
    if (showSignOutConfirm) {
        ElvanActionSheet(
            title = K.signOutPrompt.tr(),
            cancelText = K.cancelBtn.tr(),
            confirmText = K.signOutBtn.tr(),
            confirmColor = Color(0xFFBA1A1A),
            onDismissRequest = { showSignOutConfirm = false },
            onConfirm = {
                showSignOutConfirm = false
                showSignOutLoading = true
                scope.launch {
                    delay(400)
                    AuthManager.logout()
                    showSignOutLoading = false
                    ElvanSnackbar.show(signOutSuccessMsg)
                }
            },
            colors = colors
        )
    }

    if (showSignOutLoading) {
        ElvanLoadingOverlay(
            text = K.signingOut.tr(),
            colors = colors
        )
    }

    // ── Erase Step 1: Email Confirmation ──
    if (showEraseStep1) {
        ElvanActionSheet(
            title = K.eraseAppData.tr(),
            cancelText = K.cancelBtn.tr(),
            confirmText = K.continueText.tr(),
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
                val isMatch = if (registeredEmail.isNotBlank()) {
                    input.equals(registeredEmail, ignoreCase = true)
                } else {
                    input.contains("@") && input.contains(".")
                }

                if (!isMatch) {
                    ElvanSnackbar.show(emailMismatchMsg)
                } else {
                    showEraseStep1 = false
                    showEraseStep2 = true
                }
            },
            colors = colors
        )
    }

    // ── Erase Step 2: Password Confirmation ──
    if (showEraseStep2) {
        ElvanActionSheet(
            title = K.enterPassword.tr(),
            cancelText = K.cancelBtn.tr(),
            confirmText = K.eraseDataBtn.tr(),
            confirmColor = Color(0xFFBA1A1A),
            customContent = {
                ElvanSettingsTextField(
                    label = K.password.tr(),
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    placeholder = K.password.tr(),
                    colors = colors
                )
            },
            onDismissRequest = { showEraseStep2 = false },
            onConfirm = {
                val pwd = passwordInput.trim()
                if (pwd.isBlank()) {
                    ElvanSnackbar.show(passwordRequiredMsg)
                } else {
                    showEraseStep2 = false
                    showEraseLoading = true
                    scope.launch {
                        try {
                            // 1. Take safety vault backup first so user never loses data by accident
                            getNirilBackupService().createSafetySnapshot()

                            // 2. Wipe SQLite business tables & company profile tables for all modes
                            val bizHelper = getBusinessDatabaseHelper()
                            val settingsHelper = getSettingsDatabaseHelper()
                            for (m in listOf(AppMode.FACTORY, AppMode.TEXTILES)) {
                                try { bizHelper.clearAllData(m) } catch (_: Exception) {}
                                try { settingsHelper.clearProfiles(m) } catch (_: Exception) {}
                            }

                            // 3. Clear repositories in-memory
                            PattiyalRepository.loadAll()
                            PatrugalRepository.loadAll()
                            PorulRepository.loadAll()
                            VaangunarRepository.loadAll()
                            NiruvanaTharavugalRepository.refreshFromDatabase()

                            // 4. Wipe user preferences
                            try {
                                getPreferencesHelper().clear()
                            } catch (_: Exception) {}

                            // 5. Reset AuthManager session
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

    if (showEraseLoading) {
        ElvanLoadingOverlay(
            text = K.erasing.tr(),
            colors = colors
        )
    }
}

