package com.elvan.udukkai.ui.screens.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

sealed interface SettingsRoute {
    data object Hub : SettingsRoute
    data object Display : SettingsRoute
    data object Language : SettingsRoute
    data object Merchant : SettingsRoute
    data object FactoryIdentity : SettingsRoute
    data object TextilesIdentity : SettingsRoute
    val KooliIdentity: SettingsRoute get() = FactoryIdentity
    val PattuIdentity: SettingsRoute get() = TextilesIdentity
    data object Address : SettingsRoute
    data object Bank : SettingsRoute
    data object InvoiceCreation : SettingsRoute
    data object StorageBackup : SettingsRoute
    data object Security : SettingsRoute
    data object AboutDeveloper : SettingsRoute
    data object AboutApp : SettingsRoute
    data object ElvanNavil : SettingsRoute
    data object ManageProfiles : SettingsRoute
    data object Privacy : SettingsRoute
    data object Terms : SettingsRoute
    data object Licences : SettingsRoute
    data object AppLock : SettingsRoute
    data object About : SettingsRoute

    companion object {
        /** The pages that are opened from the About page: Back from them returns to it. */
        val aboutPages: Set<SettingsRoute> = setOf(AboutApp, AboutDeveloper, ElvanNavil, Privacy, Terms, Licences)
    }
}

/** How deep a page is: the hub is 0, the pages opened from it 1, the pages opened from those 2. */
val SettingsRoute.depth: Int
    get() = when {
        this == SettingsRoute.Hub -> 0
        this == SettingsRoute.ManageProfiles || this in SettingsRoute.aboutPages -> 2
        else -> 1
    }

/**
 * A simple back-stack for the in-app Settings navigation.
 *
 * Call [push] when moving forward (navigate deeper), [pop] to go back.
 * [current] is the page to display; [isForward] tells the animator which
 * direction the last transition went, so it never has to guess from depth alone.
 *
 * The stack always has at least [SettingsRoute.Hub] at position 0 and is
 * never allowed to become empty.
 */
class SettingsBackStack {
    private val _stack = mutableStateListOf<SettingsRoute>(SettingsRoute.Hub)

    /** The page that should be displayed right now. */
    val current: SettingsRoute get() = _stack.last()

    /** True after a [push], false after a [pop]. Used to pick the slide direction. */
    var isForward: Boolean by mutableStateOf(true)
        private set

    /** Whether the back button should be offered (stack has more than the hub). */
    val canGoBack: Boolean get() = _stack.size > 1

    /** Navigate to [route], pushing it on top of the stack. */
    fun push(route: SettingsRoute) {
        // Avoid duplicate pushes (e.g. from a double-tap)
        if (_stack.last() == route) return
        isForward = true
        _stack.add(route)
    }

    /**
     * Go back one step. Returns the route we landed on, or null when the
     * stack was already at the root (Hub) — the caller should handle app-level back.
     */
    fun pop(): SettingsRoute? {
        if (_stack.size <= 1) return null
        isForward = false
        _stack.removeLast()
        return _stack.last()
    }
}
