package com.elvan.udukkai.ui.screens.onboarding.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.core.platform.getSystemLanguageCode
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.LanguageManager
import com.elvan.udukkai.localization.trWithLang
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * The first page of the logged-out flow, laid out and animated like Neram's LanguageSelectionScreen:
 * a globe, the title in the language being previewed, a rounded card of languages, then Continue.
 */
@Composable
fun LanguageSelectScreen(onContinue: () -> Unit) {
    var selected by remember { mutableStateOf(LanguageManager.currentLanguage.code) }
    val preview = if (selected == "system") getSystemLanguageCode() else selected
    val choices = listOf(
        NeramChoice("system", K.deviceLanguage.trWithLang(preview)),
        NeramChoice("ta", "தமிழ்", K.langTamil.trWithLang(preview)),
        NeramChoice("ta-Latn", "Thamizh", K.langTamilLatin.trWithLang(preview)),
        NeramChoice("en", "English", K.langEnglish.trWithLang(preview))
    )

    NeramPage(
        bottom = {
            NeramButton(
                text = K.continueText.trWithLang(preview),
                onClick = {
                    LanguageManager.setLanguageByCode(selected)
                    onContinue()
                }
            )
        }
    ) {
        NeramElement(delayIndex = 0) {
            Icon(
                imageVector = MaterialSymbols.Rounded.Language,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = NeramColors.textPrimary()
            )
        }

        Spacer(Modifier.height(14.dp))

        NeramElement(delayIndex = 1) {
            NeramHeader(
                title = K.language.trWithLang(preview),
                subtitle = K.selectPreferredLanguage.trWithLang(preview)
            )
        }

        Spacer(Modifier.height(4.dp))

        NeramElement(delayIndex = 2, modifier = Modifier.fillMaxWidth()) {
            NeramChoiceCard(choices = choices, selected = selected, onSelect = { selected = it })
        }
    }
}
