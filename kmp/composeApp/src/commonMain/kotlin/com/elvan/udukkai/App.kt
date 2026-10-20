package com.elvan.udukkai

import androidx.compose.runtime.Composable
import com.elvan.udukkai.factory.FactoryApp

/**
 * Legacy entry point. Directs to FactoryApp.
 * Platform-specific launchers (MainActivity on Android, main_factory/main_textiles on Desktop)
 * directly invoke FactoryApp() or TextilesApp().
 */
@Composable
fun App() {
    FactoryApp()
}
