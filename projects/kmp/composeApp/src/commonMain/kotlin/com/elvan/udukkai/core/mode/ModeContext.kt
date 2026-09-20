package com.elvan.udukkai.core.mode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

/**
 * CompositionLocal that provides the current AppMode to the entire UI tree.
 * Any composable can read the current mode via LocalAppMode.current.
 */
val LocalAppMode = compositionLocalOf { AppMode.KOOLI }

/**
 * Wraps the content with the current mode context.
 * Place this near the root of your composable tree.
 *
 * Usage:
 *   ProvideModeContext {
 *       // All child composables can access LocalAppMode.current
 *       HomeScreen()
 *   }
 */
@Composable
fun ProvideModeContext(
    mode: AppMode = ModeManager.currentMode,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalAppMode provides mode) {
        content()
    }
}
