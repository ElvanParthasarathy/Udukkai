package com.elvan.udukkai.ui.screens.settings

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

/** How deep a page is: the hub is 0, the pages opened from it 1, the pages opened from those 2. Going to a shallower one is Back. */
val SettingsRoute.depth: Int
    get() = when {
        this == SettingsRoute.Hub -> 0
        this == SettingsRoute.ManageProfiles || this in SettingsRoute.aboutPages -> 2
        else -> 1
    }
