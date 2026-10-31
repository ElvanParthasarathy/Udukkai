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
}
