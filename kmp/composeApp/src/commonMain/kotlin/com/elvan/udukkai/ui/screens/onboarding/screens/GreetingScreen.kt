package com.elvan.udukkai.ui.screens.onboarding.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.core.auth.AuthManager
import com.elvan.udukkai.core.backup.getNirilBackupService
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.data.settings.NiruvanaTharavugal
import com.elvan.udukkai.data.settings.NiruvanaTharavugalRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.ui.screens.onboarding.components.*
import kotlinx.coroutines.launch

/**
 * Business Name Setup Page matching Flutter's VanakkamPage (vanakkam_thirai.dart) 1:1.
 * Prompts for GST Business Name (if Silk profile missing) and/or
 * Coolie Business Name (if Coolie profile missing).
 */
@Composable
fun VanakkamThirai(
    billingLanguage: String = "ta",
    onSetupComplete: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()

    val missingProfiles = remember { AuthManager.missingProfiles }
    val needsSilk = missingProfiles.any { it.equals("silk", ignoreCase = true) || it.equals("pattu", ignoreCase = true) }
    val needsCoolie = missingProfiles.any { it.equals("coolie", ignoreCase = true) || it.equals("kooli", ignoreCase = true) }

    var gstBusinessName by remember { mutableStateOf("") }
    var coolieBusinessName by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val isButtonDisabled = (needsSilk && gstBusinessName.trim().isEmpty()) ||
            (needsCoolie && coolieBusinessName.trim().isEmpty())

    AuthLayout(showBranding = true) {
        // Back Button
        if (onBack != null) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                AuthBackButton(onClick = onBack)
            }
        }

        // Header: "தரவுகளை உள்ளிடுக" / "Enter Company Details"
        AuthHeader(
            title = K.enterCompanyDetails.tr(),
            subtitle = ""
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Silk / GST Business Name input
        if (needsSilk) {
            AuthInput(
                value = gstBusinessName,
                onValueChange = { gstBusinessName = it },
                label = K.companyName.tr().ifEmpty { "GST Business Name" },
                placeholder = K.enterName.tr(),
                helperText = K.forGstInvoice.tr()
            )
        }

        // Coolie Business Name input
        if (needsCoolie) {
            AuthInput(
                value = coolieBusinessName,
                onValueChange = { coolieBusinessName = it },
                label = K.coolieCompanyName.tr(),
                placeholder = K.enterName.tr(),
                helperText = K.usedForCoolieBillsReceipts.tr()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Continue Button ("தொடரவும்" / "Continue")
        AuthButton(
            text = K.continueText.tr(),
            loading = isSaving,
            disabled = isButtonDisabled,
            onClick = {
                if (isButtonDisabled) return@AuthButton
                isSaving = true
                coroutineScope.launch {
                    try {
                        if (needsSilk) {
                            val silkProfile = NiruvanaTharavugal(
                                mudhanMozhi = billingLanguage,
                                niruvanathinPeyar = mutableMapOf(billingLanguage to gstBusinessName.trim()),
                                kurumPeyar = gstBusinessName.trim()
                            )
                            NiruvanaTharavugalRepository.createProfile(AppMode.PATTU, silkProfile)
                        }

                        if (needsCoolie) {
                            val coolieProfile = NiruvanaTharavugal(
                                mudhanMozhi = billingLanguage,
                                niruvanathinPeyar = mutableMapOf(billingLanguage to coolieBusinessName.trim()),
                                kurumPeyar = coolieBusinessName.trim()
                            )
                            NiruvanaTharavugalRepository.createProfile(AppMode.KOOLI, coolieProfile)
                        }

                        // Create backup and refresh profile status
                        AuthManager.refreshProfileStatus()
                        getNirilBackupService().createBackup()
                        onSetupComplete()
                    } finally {
                        isSaving = false
                    }
                }
            }
        )
    }
}
