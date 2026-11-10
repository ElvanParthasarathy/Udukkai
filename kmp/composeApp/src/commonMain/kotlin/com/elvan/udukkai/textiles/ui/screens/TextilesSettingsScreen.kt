package com.elvan.udukkai.textiles.ui.screens

import androidx.compose.runtime.Composable
import com.elvan.udukkai.textiles.data.TextilesProfileRepository
import com.elvan.udukkai.textiles.ui.screens.settings.*
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.ui.screens.settings.SettingsBusiness
import com.elvan.udukkai.ui.screens.settings.SettingsRoute
import com.elvan.udukkai.ui.screens.settings.SettingsScreenShell
import com.elvan.udukkai.ui.screens.settings.screens.AddressFieldOrder

@Composable
fun TextilesSettingsScreen(
    initialRoute: SettingsRoute = SettingsRoute.Hub,
    onNavigateOverride: ((SettingsRoute) -> Boolean)? = null,
    onBack: () -> Unit = {}
) {
    SettingsScreenShell(
        business = SettingsBusiness(
            profileStore = TextilesProfileRepository,
            identityRoute = SettingsRoute.TextilesIdentity,
            appName = "Udukkai Textiles",
            appTitle = K.udukkaiTextiles.tr(),
            isFactory = false,
            addressFieldOrder = AddressFieldOrder.CountryFirst
        ),
        initialRoute = initialRoute,
        onNavigateOverride = onNavigateOverride,
        onBack = onBack
    ) { route, scrollState, _, _, colors ->
        when (route) {
            SettingsRoute.StorageBackup -> TextilesStorageBackupScreen(scrollState = scrollState, colors = colors)
            else -> Unit
        }
    }
}
