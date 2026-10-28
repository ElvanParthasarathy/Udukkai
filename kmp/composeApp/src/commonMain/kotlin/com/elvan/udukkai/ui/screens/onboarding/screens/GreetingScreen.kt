package com.elvan.udukkai.ui.screens.onboarding.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.auth.AppScope
import com.elvan.udukkai.core.auth.AuthManager
import com.elvan.udukkai.core.backup.getUdukkaiBackupService
import com.elvan.udukkai.data.settings.BusinessProfile
import com.elvan.udukkai.factory.data.FactoryProfileRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.textiles.data.TextilesProfileRepository
import com.elvan.udukkai.ui.components.shell.ElvanSnackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Business name page of the first-run setup, built like the language page: title, one pill field, and Continue
 * pinned at the bottom. Textiles asks for the GST business name, Factory for the Coolie business name.
 */
@Composable
fun GreetingScreen(
    scope: AppScope,
    billingLanguage: String = "ta",
    onSetupComplete: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val needsSilk = scope == AppScope.TEXTILES
    val needsCoolie = scope == AppScope.FACTORY

    var businessName by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val enterNameMsg = K.enterName.tr()
    val couldNotSaveMsg = K.couldNotSavePrefix.tr()
    val disabled = businessName.trim().isEmpty()

    fun save() {
        if (disabled) {
            ElvanSnackbar.show(enterNameMsg)
            return
        }
        isSaving = true
        coroutineScope.launch {
            try {
                val secondary = if (billingLanguage == "en") "ta" else "en"
                var created = true
                withContext(Dispatchers.Default) {
                    if (needsSilk) {
                        created = created && TextilesProfileRepository.createProfile(
                            BusinessProfile(
                                primaryLanguage = billingLanguage,
                                secondaryLanguage = secondary,
                                isBilingual = true,
                                businessName = mutableMapOf(billingLanguage to businessName.trim())
                            )
                        )
                    }
                    if (needsCoolie) {
                        created = created && FactoryProfileRepository.createProfile(
                            BusinessProfile(
                                primaryLanguage = billingLanguage,
                                secondaryLanguage = secondary,
                                isBilingual = false,
                                businessName = mutableMapOf(billingLanguage to businessName.trim())
                            )
                        )
                    }
                    // Save the initial setup right away
                    if (created) getUdukkaiBackupService().createBackup()
                }
                if (!created) {
                    // Never leave the button spinning when the business could not be stored
                    isSaving = false
                    ElvanSnackbar.show(couldNotSaveMsg)
                    return@launch
                }
                AuthManager.refreshProfileStatus()
                onSetupComplete()
            } catch (e: Exception) {
                isSaving = false
                ElvanSnackbar.show("$couldNotSaveMsg ${e.message}")
            }
        }
    }

    NeramPage(
        bottom = {
            NeramButton(
                text = K.continueText.tr(),
                onClick = { save() },
                loading = isSaving,
                enabled = !disabled
            )
        }
    ) {
        NeramElement(delayIndex = 0) {
            NeramHeader(title = K.enterCompanyDetails.tr(), subtitle = "")
        }

        Spacer(Modifier.height(16.dp))

        NeramElement(delayIndex = 1, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = (if (needsCoolie) K.coolieCompanyName.tr() else K.companyName.tr().ifEmpty { "GST Business Name" }).uppercase(),
                    style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                    color = NeramColors.textSecondary(),
                    modifier = Modifier.padding(start = 28.dp, bottom = 8.dp)
                )
                NeramField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    placeholder = K.enterName.tr()
                )
                Text(
                    text = if (needsCoolie) K.usedForCoolieBillsReceipts.tr() else K.forGstInvoice.tr(),
                    style = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
                    color = NeramColors.textMuted(),
                    modifier = Modifier.padding(start = 28.dp, top = 8.dp)
                )
            }
        }
    }
}
