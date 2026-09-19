package com.elvan.udukkai.ui.screens.onboarding.screens.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.LanguageManager
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.ThemeManager
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.onboarding.components.*
import com.elvan.udukkai.theme.LocalShellColors

/**
 * App Language Selection Step matching Flutter's AppLanguageStep (seyali_mozhi_padi.dart) 1:1.
 */
@Composable
fun AppLanguageStep(
    onBack: () -> Unit,
    onLanguageSelected: () -> Unit
) {
    val textColor = LocalShellColors.current.textPrimary
    val containerBg = LocalShellColors.current.pillBackground
    val dividerColor = LocalShellColors.current.divider

    val currentLang = LanguageManager.activeLanguageCode

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            AuthBackButton(onClick = onBack)
        }

        // Globe Icon matching Flutter
        Icon(
            imageVector = MaterialSymbols.Rounded.Translate,
            contentDescription = "Language",
            tint = textColor,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Header: "மொழித் தேர்வு" / "Select Language"
        AuthHeader(
            title = K.selectLanguage.tr(),
            subtitle = K.choosePreferredLanguage.tr()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Language Selection Card matching Flutter
        AuthAnimatedElement(delayIndex = 2) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(containerBg, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    LanguageTile(
                        title = "தமிழ்",
                        isSelected = currentLang == "ta",
                        onTap = {
                            LanguageManager.setLanguageByCode("ta")
                            onLanguageSelected()
                        }
                    )
                    HorizontalDivider(
                        color = dividerColor,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    LanguageTile(
                        title = "English",
                        isSelected = currentLang == "en",
                        onTap = {
                            LanguageManager.setLanguageByCode("en")
                            onLanguageSelected()
                        }
                    )
                    HorizontalDivider(
                        color = dividerColor,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    LanguageTile(
                        title = "Thamizh Latin",
                        isSelected = currentLang == "ta-Latn",
                        onTap = {
                            LanguageManager.setLanguageByCode("ta-Latn")
                            onLanguageSelected()
                        }
                    )
                }
            }
        }
    }
}
