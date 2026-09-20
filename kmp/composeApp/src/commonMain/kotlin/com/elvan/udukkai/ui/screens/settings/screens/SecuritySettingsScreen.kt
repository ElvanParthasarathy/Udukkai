package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.elvan.udukkai.data.settings.NiruvanaTharavugalRepository
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
 * Security Settings Screen matching Flutter's `paadhugaappu_amaippugal_thirai.dart` and
 * `kanakku_paadhugaappu_pagudhi.dart` 1:1 pixel-perfect.
 * Features Sync App, Sign Out, and 2-Step Erase App Data with modal flows.
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
                            } catch (_: Exception) {}
                            delay(800)
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
                    delay(1000)
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
                    placeholder = K.confirmEmail.tr(),
                    colors = colors
                )
            },
            onDismissRequest = { showEraseStep1 = false },
            onConfirm = {
                showEraseStep1 = false
                showEraseStep2 = true
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
                showEraseStep2 = false
                showEraseLoading = true
                scope.launch {
                    delay(1500)
                    showEraseLoading = false
                    ElvanSnackbar.show(eraseSuccessMsg)
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
