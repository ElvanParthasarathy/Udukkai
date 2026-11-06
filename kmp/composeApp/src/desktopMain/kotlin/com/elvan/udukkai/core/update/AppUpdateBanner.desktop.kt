package com.elvan.udukkai.core.update

import androidx.compose.runtime.Composable
import com.elvan.udukkai.ui.components.shell.ShellBanner

@Composable
actual fun rememberAppUpdateBanner(): ShellBanner? = null

actual fun triggerAppUpdateCheck() {
    // Desktop: no-op
}
