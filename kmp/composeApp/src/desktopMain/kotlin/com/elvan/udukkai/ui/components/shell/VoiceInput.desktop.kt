package com.elvan.udukkai.ui.components.shell

import androidx.compose.runtime.Composable

@Composable
actual fun rememberVoiceInput(onResult: (String) -> Unit): (() -> Unit)? = null
