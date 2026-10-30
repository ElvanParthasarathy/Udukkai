package com.elvan.udukkai.ui.screens.onboarding.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.auth.AppScope
import com.elvan.udukkai.core.auth.AuthManager
import com.elvan.udukkai.core.backup.getUdukkaiBackupService
import com.elvan.udukkai.core.extensions.cssShadow
import com.elvan.udukkai.data.settings.BusinessProfile
import com.elvan.udukkai.factory.data.FactoryProfileRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.textiles.data.TextilesProfileRepository
import com.elvan.udukkai.theme.ElvanSansFontFamily
import com.elvan.udukkai.theme.ThemeManager
import com.elvan.udukkai.ui.components.shell.ElvanSnackbar
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Business setup page of the first-run setup:
 * 1. Asks whether Bilingual mode (இருமொழி முறை) is needed or not.
 * 2. If Bilingual is ON: offers two business name inputs (Primary language + Secondary language,
 *    with automatic Navil transliteration from Tamil to English).
 * 3. If Bilingual is OFF: offers a single business name input in the primary language.
 * Works for both Textiles (GST bills) and Factory (Coolie bills).
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
    val dark = ThemeManager.isDark()

    val primaryLang = billingLanguage
    val secondaryLang = if (billingLanguage == "en") "ta" else "en"
    val isPrimaryTamil = primaryLang.lowercase().startsWith("ta")

    var isBilingual by remember { mutableStateOf(true) }
    var primaryName by remember { mutableStateOf("") }
    var secondaryName by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val enterNameMsg = K.enterName.tr()
    val couldNotSaveMsg = K.couldNotSavePrefix.tr()
    val disabled = primaryName.trim().isEmpty() || (isBilingual && secondaryName.trim().isEmpty())

    fun save() {
        if (disabled) {
            ElvanSnackbar.show(enterNameMsg)
            return
        }
        isSaving = true
        coroutineScope.launch {
            try {
                val names = mutableMapOf<String, String>()
                names[primaryLang] = primaryName.trim()
                if (isBilingual) {
                    names[secondaryLang] = secondaryName.trim()
                }

                var created = true
                withContext(Dispatchers.Default) {
                    if (needsSilk) {
                        created = created && TextilesProfileRepository.createProfile(
                            BusinessProfile(
                                primaryLanguage = primaryLang,
                                secondaryLanguage = secondaryLang,
                                isBilingual = isBilingual,
                                businessName = names
                            )
                        )
                    }
                    if (needsCoolie) {
                        created = created && FactoryProfileRepository.createProfile(
                            BusinessProfile(
                                primaryLanguage = primaryLang,
                                secondaryLanguage = secondaryLang,
                                isBilingual = isBilingual,
                                businessName = names
                            )
                        )
                    }
                    // Save the initial setup right away
                    if (created) getUdukkaiBackupService().createBackup()
                }
                if (!created) {
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
            NeramHeader(
                title = K.enterCompanyDetails.tr(),
                subtitle = if (needsCoolie) K.usedForCoolieBillsReceipts.tr() else K.forGstInvoice.tr()
            )
        }

        Spacer(Modifier.height(16.dp))

        // Bilingual Mode Choice Card (Match React bank/bilingual preference)
        NeramElement(delayIndex = 1, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (dark) Modifier
                        else Modifier.cssShadow(color = Color.Black, alpha = 0.12f, blurRadius = 14.dp, offsetY = 4.dp)
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .background(NeramColors.inputBackground())
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isBilingual = !isBilingual }
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.Translate,
                        contentDescription = null,
                        tint = NeramColors.textPrimary(),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            text = K.bilingualMode.tr(),
                            style = TextStyle(
                                fontFamily = ElvanSansFontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NeramColors.textPrimary(),
                                lineHeight = 20.sp
                            )
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = if (isPrimaryTamil) "தமிழ் + English" else "English + தமிழ்",
                            style = TextStyle(
                                fontFamily = ElvanSansFontFamily,
                                fontSize = 12.sp,
                                color = NeramColors.textSecondary(),
                                lineHeight = 16.sp
                            )
                        )
                    }
                }
                Switch(
                    checked = isBilingual,
                    onCheckedChange = { isBilingual = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = if (dark) Color.Black else Color.White,
                        checkedTrackColor = if (dark) Color.White else Color.Black,
                        uncheckedThumbColor = NeramColors.textSecondary(),
                        uncheckedTrackColor = NeramColors.divider()
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Business Name Field(s)
        NeramElement(delayIndex = 2, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                val focusManager = LocalFocusManager.current
                val keyboardController = LocalSoftwareKeyboardController.current

                val primaryPlaceholder = when {
                    isBilingual && isPrimaryTamil -> "நிறுவனப் பெயர் (தமிழ்)"
                    isBilingual -> "Company Name (English)"
                    isPrimaryTamil -> "நிறுவனப் பெயர்"
                    else -> "Company Name"
                }

                NeramField(
                    value = primaryName,
                    onValueChange = { primaryName = it },
                    placeholder = primaryPlaceholder,
                    keyboardOptions = KeyboardOptions(
                        imeAction = if (isBilingual) ImeAction.Next else ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                        onDone = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            save()
                        }
                    )
                )

                if (isBilingual) {
                    Spacer(Modifier.height(16.dp))
                    val secondaryPlaceholder = if (isPrimaryTamil) {
                        "Company Name (English)"
                    } else {
                        "நிறுவனப் பெயர் (தமிழ்)"
                    }
                    NeramField(
                        value = secondaryName,
                        onValueChange = { secondaryName = it },
                        placeholder = secondaryPlaceholder,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                save()
                            }
                        )
                    )
                }
            }
        }
    }
}
