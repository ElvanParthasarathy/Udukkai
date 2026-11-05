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
    data object BilingualRules : SettingsRoute
    data object LogoSettings : SettingsRoute
    data object StorageBackup : SettingsRoute
    data object Security : SettingsRoute
    data object AboutDeveloper : SettingsRoute
    data object AboutApp : SettingsRoute
    data object ElvanNavil : SettingsRoute
    data object ManageProfiles : SettingsRoute
    data object Privacy : SettingsRoute
    data object Terms : SettingsRoute
    data object Licences : SettingsRoute
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
        this == SettingsRoute.ManageProfiles || this in SettingsRoute.aboutPages || this == SettingsRoute.BilingualRules || this == SettingsRoute.LogoSettings -> 2
        else -> 1
    }

/**
 * Route + a monotonically-changing index so AnimatedContent always knows
 * the exact slide direction: higher index = forward push, lower index = back pop.
 */
data class SettingsNavState(val route: SettingsRoute, val index: Int)

val SettingsRoute.name: String
    get() = when (this) {
        SettingsRoute.Hub -> "Hub"
        SettingsRoute.Display -> "Display"
        SettingsRoute.Language -> "Language"
        SettingsRoute.Merchant -> "Merchant"
        SettingsRoute.FactoryIdentity -> "FactoryIdentity"
        SettingsRoute.TextilesIdentity -> "TextilesIdentity"
        SettingsRoute.Address -> "Address"
        SettingsRoute.Bank -> "Bank"
        SettingsRoute.InvoiceCreation -> "InvoiceCreation"
        SettingsRoute.BilingualRules -> "BilingualRules"
        SettingsRoute.LogoSettings -> "LogoSettings"
        SettingsRoute.StorageBackup -> "StorageBackup"
        SettingsRoute.Security -> "Security"
        SettingsRoute.AboutDeveloper -> "AboutDeveloper"
        SettingsRoute.AboutApp -> "AboutApp"
        SettingsRoute.ElvanNavil -> "ElvanNavil"
        SettingsRoute.ManageProfiles -> "ManageProfiles"
        SettingsRoute.Privacy -> "Privacy"
        SettingsRoute.Terms -> "Terms"
        SettingsRoute.Licences -> "Licences"
        SettingsRoute.About -> "About"
    }

fun settingsRouteFromName(name: String?): SettingsRoute = when (name) {
    "Display" -> SettingsRoute.Display
    "Language" -> SettingsRoute.Language
    "Merchant" -> SettingsRoute.Merchant
    "FactoryIdentity" -> SettingsRoute.FactoryIdentity
    "TextilesIdentity" -> SettingsRoute.TextilesIdentity
    "Address" -> SettingsRoute.Address
    "Bank" -> SettingsRoute.Bank
    "InvoiceCreation" -> SettingsRoute.InvoiceCreation
    "BilingualRules" -> SettingsRoute.BilingualRules
    "LogoSettings" -> SettingsRoute.LogoSettings
    "StorageBackup" -> SettingsRoute.StorageBackup
    "Security" -> SettingsRoute.Security
    "AboutDeveloper" -> SettingsRoute.AboutDeveloper
    "AboutApp" -> SettingsRoute.AboutApp
    "ElvanNavil" -> SettingsRoute.ElvanNavil
    "ManageProfiles" -> SettingsRoute.ManageProfiles
    "Privacy" -> SettingsRoute.Privacy
    "Terms" -> SettingsRoute.Terms
    "Licences" -> SettingsRoute.Licences
    "About" -> SettingsRoute.About
    else -> SettingsRoute.Hub
}

/** Opens settings using native platform window (e.g. Activity on Android). Returns true if handled natively. */
expect fun openPlatformSettings(route: SettingsRoute = SettingsRoute.Hub): Boolean


