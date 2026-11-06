package com.elvan.udukkai.core.update

import androidx.compose.runtime.Composable
import com.elvan.udukkai.ui.components.shell.ShellBanner

/**
 * Returns a [ShellBanner] if an application update is available and has not been dismissed for this session.
 */
@Composable
expect fun rememberAppUpdateBanner(): ShellBanner?
