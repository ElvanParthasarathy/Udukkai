package com.elvan.udukkai.ui.components.shell

import androidx.compose.runtime.Composable

/** Returns a function that starts speech-to-text and passes the spoken text to [onResult]; null where voice typing is unavailable. */
@Composable
expect fun rememberVoiceInput(onResult: (String) -> Unit): (() -> Unit)?
