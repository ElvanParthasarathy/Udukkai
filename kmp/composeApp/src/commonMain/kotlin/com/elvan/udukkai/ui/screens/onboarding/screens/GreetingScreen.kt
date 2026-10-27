package com.elvan.udukkai.ui.screens.onboarding.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.core.auth.AppScope
import com.elvan.udukkai.core.auth.AuthManager
import com.elvan.udukkai.core.backup.getUdukkaiBackupService
import com.elvan.udukkai.data.settings.BusinessProfile
import com.elvan.udukkai.factory.data.FactoryProfileRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.textiles.data.TextilesProfileRepository
import com.elvan.udukkai.ui.components.shell.ElvanSnackbar
import com.elvan.udukkai.ui.screens.onboarding.components.AuthBackButton
import com.elvan.udukkai.ui.screens.onboarding.components.AuthButton
import com.elvan.udukkai.ui.screens.onboarding.components.AuthHeader
import com.elvan.udukkai.ui.screens.onboarding.components.AuthInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Business Name Setup Page matching Flutter's VanakkamPage (vanakkam_thirai.dart) 1:1.
 * Textiles asks for the GST business name, Factory for the Coolie business name.
 * Rendered inside the setup flow's AuthLayout (no layout of its own, like Flutter's KeyedSubtree).
 */
@Composable
fun GreetingScreen(
    scope: AppScope,
    billingLanguage: String = "ta",
    onSetupComplete: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val needsSilk = scope == AppScope.TEXTILES
    val needsCoolie = scope == AppScope.FACTORY

    var gstBusinessName by remember { mutableStateOf("") }
    var coolieBusinessName by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val enterNameMsg = K.enterName.tr()
    val isButtonDisabled = (needsSilk && gstBusinessName.trim().isEmpty()) ||
            (needsCoolie && coolieBusinessName.trim().isEmpty())

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (onBack != null) AuthBackButton(onClick = onBack)

        AuthHeader(
            title = K.enterCompanyDetails.tr(),
            subtitle = ""
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (needsSilk) {
            AuthInput(
                value = gstBusinessName,
                onValueChange = { gstBusinessName = it },
                label = K.companyName.tr().ifEmpty { "GST Business Name" },
                placeholder = K.enterName.tr(),
                helperText = K.forGstInvoice.tr()
            )
        }

        if (needsCoolie) {
            AuthInput(
                value = coolieBusinessName,
                onValueChange = { coolieBusinessName = it },
                label = K.coolieCompanyName.tr(),
                placeholder = K.enterName.tr(),
                helperText = K.usedForCoolieBillsReceipts.tr()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        AuthButton(
            text = K.continueText.tr(),
            loading = isSaving,
            disabled = isButtonDisabled,
            onClick = {
                if (isButtonDisabled) {
                    ElvanSnackbar.show(enterNameMsg)
                    return@AuthButton
                }
                isSaving = true
                coroutineScope.launch {
                    try {
                        val secondary = if (billingLanguage == "en") "ta" else "en"
                        withContext(Dispatchers.Default) {
                            if (needsSilk) {
                                TextilesProfileRepository.createProfile(
                                    BusinessProfile(
                                        primaryLanguage = billingLanguage,
                                        secondaryLanguage = secondary,
                                        isBilingual = true,
                                        businessName = mutableMapOf(billingLanguage to gstBusinessName.trim())
                                    )
                                )
                            }
                            if (needsCoolie) {
                                FactoryProfileRepository.createProfile(
                                    BusinessProfile(
                                        primaryLanguage = billingLanguage,
                                        secondaryLanguage = secondary,
                                        isBilingual = false,
                                        businessName = mutableMapOf(billingLanguage to coolieBusinessName.trim())
                                    )
                                )
                            }
                            // Save the initial setup right away (Flutter: backupService.createBackup())
                            getUdukkaiBackupService().createBackup()
                        }
                        AuthManager.refreshProfileStatus()
                        onSetupComplete()
                    } catch (e: Exception) {
                        isSaving = false
                        ElvanSnackbar.show("Error: ${e.message}")
                    }
                }
            }
        )
    }
}
