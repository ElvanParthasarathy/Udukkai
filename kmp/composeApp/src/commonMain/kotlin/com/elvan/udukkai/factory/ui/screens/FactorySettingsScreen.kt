package com.elvan.udukkai.factory.ui.screens

import androidx.compose.runtime.Composable
import com.elvan.udukkai.factory.data.FactoryProfileRepository
import com.elvan.udukkai.factory.ui.screens.settings.*
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.ui.screens.settings.SettingsBusiness
import com.elvan.udukkai.ui.screens.settings.SettingsRoute
import com.elvan.udukkai.ui.screens.settings.SettingsScreenShell
import com.elvan.udukkai.ui.screens.settings.screens.AddressFieldOrder

@Composable
fun FactorySettingsScreen(
    initialRoute: SettingsRoute = SettingsRoute.Hub,
    onNavigateOverride: ((SettingsRoute) -> Boolean)? = null,
    onBack: () -> Unit = {}
) {
    SettingsScreenShell(
        business = SettingsBusiness(
            profileStore = FactoryProfileRepository,
            identityRoute = SettingsRoute.FactoryIdentity,
            appName = "Udukkai Factory",
            appTitle = K.udukkaiFactory.tr(),
            isFactory = true,
            addressFieldOrder = AddressFieldOrder.StreetFirst
        ),
        initialRoute = initialRoute,
        onNavigateOverride = onNavigateOverride,
        onBack = onBack
    ) { route, scrollState, _, _, colors ->
        when (route) {
            SettingsRoute.StorageBackup -> FactoryStorageBackupScreen(scrollState = scrollState, colors = colors)
            else -> Unit
        }
    }
}
