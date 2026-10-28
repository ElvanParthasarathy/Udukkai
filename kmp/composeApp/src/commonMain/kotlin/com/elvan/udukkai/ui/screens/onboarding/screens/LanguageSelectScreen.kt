package com.elvan.udukkai.ui.screens.onboarding.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.elvan.udukkai.ui.screens.onboarding.components.AuthLayout
import com.elvan.udukkai.ui.screens.onboarding.screens.steps.AppLanguageStep

/** First screen of a fresh install, as in Neram: pick the language, then carry on to the welcome page. */
@Composable
fun LanguageSelectScreen(onContinue: () -> Unit) {
    AuthLayout {
        AppLanguageStep(onLanguageSelected = onContinue)
    }
}
