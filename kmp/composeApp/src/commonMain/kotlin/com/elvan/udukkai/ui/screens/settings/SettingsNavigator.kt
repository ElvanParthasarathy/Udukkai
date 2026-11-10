package com.elvan.udukkai.ui.screens.settings

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf

/** Shared back stack for a full Settings screen or its independent detail pane. */
@Stable
class SettingsNavigator(initialRoute: SettingsRoute) {
    private var nextIndex = 1
    private val entries = mutableStateListOf(SettingsNavState(initialRoute, 0))
    val state: SettingsNavState get() = entries.last()
    val canGoBack: Boolean get() = entries.size > 1

    fun push(route: SettingsRoute) {
        if (state.route != route) entries.add(SettingsNavState(route, nextIndex++))
    }

    fun selectRoot(route: SettingsRoute) {
        if (!canGoBack && state.route == route) return
        entries.clear()
        entries.add(SettingsNavState(route, nextIndex++))
    }

    fun pop(): Boolean {
        if (!canGoBack) return false
        entries.removeLast()
        return true
    }

    /** Finish the current category at the landing page before leaving the two-pane screen. */
    fun popOrReturnTo(landingRoute: SettingsRoute): Boolean {
        if (pop()) return true
        if (state.route == landingRoute) return false
        selectRoot(landingRoute)
        return true
    }
}
