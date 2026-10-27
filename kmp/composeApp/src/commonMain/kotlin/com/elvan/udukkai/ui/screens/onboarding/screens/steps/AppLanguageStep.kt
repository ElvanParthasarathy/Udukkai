package com.elvan.udukkai.ui.screens.onboarding.screens.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.Language
import com.elvan.udukkai.localization.LanguageManager
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.ThemeManager
import com.elvan.udukkai.ui.screens.onboarding.components.*

/**
 * App Language Selection Step matching Flutter's AppLanguageStep (seyali_mozhi_padi.dart) 1:1.
 * Tapping a language switches the UI language immediately; Continue moves on.
 */
@Composable
fun AppLanguageStep(
    onBack: () -> Unit,
    onLanguageSelected: () -> Unit
) {
    val isDark = ThemeManager.isDark()
    val textColor = if (isDark) Color.White else Color.Black
    val inputBg = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF5F5F5) // Colors.grey[100]
    val dividerColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.12f)

    val currentLang = LanguageManager.activeLanguageCode

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AuthBackButton(onClick = onBack)

        Icon(
            imageVector = MaterialSymbols.Rounded.Language,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        AuthHeader(
            title = K.selectLanguage.tr(),
            subtitle = K.choosePreferredLanguage.tr()
        )

        Spacer(modifier = Modifier.height(32.dp))

        AuthAnimatedElement(delayIndex = 2) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(inputBg, RoundedCornerShape(16.dp))
            ) {
                LanguageTile(
                    title = "தமிழ்",
                    isSelected = currentLang == "ta",
                    onTap = { LanguageManager.setLanguageByCode("ta") }
                )
                HorizontalDivider(thickness = 1.dp, color = dividerColor, modifier = Modifier.padding(horizontal = 24.dp))
                LanguageTile(
                    title = "English",
                    isSelected = currentLang == "en",
                    onTap = { LanguageManager.setLanguageByCode("en") }
                )
                HorizontalDivider(thickness = 1.dp, color = dividerColor, modifier = Modifier.padding(horizontal = 24.dp))
                LanguageTile(
                    title = "Thamizh Latin",
                    isSelected = currentLang == "ta-Latn",
                    onTap = { LanguageManager.setLanguageByCode("ta-Latn") }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        AuthButton(
            text = K.continueText.tr(),
            onClick = {
                // Continuing without picking explicitly saves the default (Tamil)
                if (LanguageManager.currentLanguage == Language.SYSTEM) {
                    LanguageManager.setLanguageByCode("ta")
                }
                onLanguageSelected()
            }
        )
    }
}
