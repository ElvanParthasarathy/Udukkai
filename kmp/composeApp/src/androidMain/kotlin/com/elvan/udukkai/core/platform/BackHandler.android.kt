package com.elvan.udukkai.core.platform

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

/**
 * With predictive back on, Android hands Back to the app even while the keyboard is open, so a screen would leave (or ask
 * to discard) instead of just closing the keyboard. Here, while the keyboard is up, Back only closes it and drops the
 * focus, for every screen that uses this handler; otherwise it is the screen's own back.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
actual fun AppPredictiveBackHandler(enabled: Boolean, onBack: () -> Unit) {
    val imeVisible = WindowInsets.isImeVisible
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = LocalFocusManager.current
    BackHandler(enabled = enabled || imeVisible) {
        if (imeVisible) {
            keyboard?.hide()
            focus.clearFocus()
        } else {
            onBack()
        }
    }
}
