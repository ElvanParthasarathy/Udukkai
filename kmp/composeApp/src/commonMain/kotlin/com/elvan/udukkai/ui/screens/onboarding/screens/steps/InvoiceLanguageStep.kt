package com.elvan.udukkai.ui.screens.onboarding.screens.steps

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.onboarding.screens.NeramButton
import com.elvan.udukkai.ui.screens.onboarding.screens.NeramChoice
import com.elvan.udukkai.ui.screens.onboarding.screens.NeramChoiceCard
import com.elvan.udukkai.ui.screens.onboarding.screens.NeramColors
import com.elvan.udukkai.ui.screens.onboarding.screens.NeramElement
import com.elvan.udukkai.ui.screens.onboarding.screens.NeramHeader
import com.elvan.udukkai.ui.screens.onboarding.screens.NeramPage

/**
 * Billing language page of the first-run setup, built like the language page: icon, title, rounded card, and
 * Continue pinned at the bottom.
 */
@Composable
fun BillingLanguageStep(
    billingLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onContinue: () -> Unit
) {
    NeramPage(
        bottom = { NeramButton(text = K.continueText.tr(), onClick = onContinue) }
    ) {
        NeramElement(delayIndex = 0) {
            Icon(
                imageVector = MaterialSymbols.Rounded.DescriptionFill,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = NeramColors.textPrimary()
            )
        }

        Spacer(Modifier.height(14.dp))

        NeramElement(delayIndex = 1) {
            NeramHeader(
                title = K.selectPrimaryBillingLanguage.tr(),
                subtitle = K.whichBillingLanguagePrompt.tr()
            )
        }

        Spacer(Modifier.height(4.dp))

        NeramElement(delayIndex = 2, modifier = Modifier.fillMaxWidth()) {
            NeramChoiceCard(
                choices = listOf(NeramChoice("ta", "தமிழ்"), NeramChoice("en", "English")),
                selected = billingLanguage,
                onSelect = onLanguageSelected
            )
        }
    }
}
