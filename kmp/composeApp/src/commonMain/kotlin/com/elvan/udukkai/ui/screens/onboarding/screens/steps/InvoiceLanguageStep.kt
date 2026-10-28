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
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.ThemeManager
import com.elvan.udukkai.ui.screens.onboarding.components.*

/**
 * Billing Language Selection Step matching Flutter's BillingLanguageStep (pattiyal_mozhi_padi.dart) 1:1.
 */
@Composable
fun BillingLanguageStep(
    billingLanguage: String,
    onBack: (() -> Unit)? = null,
    onLanguageSelected: (String) -> Unit,
    onContinue: () -> Unit
) {
    val isDark = ThemeManager.isDark()
    val textColor = if (isDark) Color.White else Color.Black
    val inputBg = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF5F5F5) // Colors.grey[100]
    val dividerColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.12f)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (onBack != null) AuthBackButton(onClick = onBack)

        Icon(
            imageVector = MaterialSymbols.Rounded.DescriptionFill,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        AuthHeader(
            title = K.selectPrimaryBillingLanguage.tr(),
            subtitle = K.whichBillingLanguagePrompt.tr()
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
                    isSelected = billingLanguage == "ta",
                    onTap = { onLanguageSelected("ta") }
                )
                HorizontalDivider(thickness = 1.dp, color = dividerColor, modifier = Modifier.padding(horizontal = 24.dp))
                LanguageTile(
                    title = "English",
                    isSelected = billingLanguage == "en",
                    onTap = { onLanguageSelected("en") }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        AuthButton(
            text = K.continueText.tr(),
            onClick = onContinue
        )
    }
}
