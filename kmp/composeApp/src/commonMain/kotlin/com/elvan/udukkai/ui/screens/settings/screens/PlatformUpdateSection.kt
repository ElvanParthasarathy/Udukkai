package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.runtime.Composable
import com.elvan.udukkai.theme.ShellColors

/**
 * Platform-specific UI section for checking and triggering app updates.
 * On Android, integrates with AppUpdate and WhatsNewSheet.
 */
@Composable
expect fun PlatformUpdateSection(colors: ShellColors)
