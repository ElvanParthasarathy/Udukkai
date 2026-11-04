package com.elvan.udukkai

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import com.elvan.udukkai.activities.BaseSubpageActivity
import com.elvan.udukkai.factory.ui.screens.FactorySettingsScreen
import com.elvan.udukkai.textiles.ui.screens.TextilesSettingsScreen
import com.elvan.udukkai.ui.screens.settings.SettingsRoute
import com.elvan.udukkai.ui.screens.settings.name
import com.elvan.udukkai.ui.screens.settings.settingsRouteFromName

/**
 * Native Android Activity for Settings screens.
 * Uses Android OS Window Manager and native OEM transitions (e.g. Samsung One UI)
 * for fluid hardware-composited screen sliding and predictive back gestures.
 */
class SettingsActivity : BaseSubpageActivity() {

    @Composable
    override fun ScreenContent() {
        val routeName = intent.getStringExtra(EXTRA_ROUTE)
        val route = settingsRouteFromName(routeName)

        if (isFactory) {
            FactorySettingsScreen(
                initialRoute = route,
                onNavigateOverride = { targetRoute ->
                    start(this@SettingsActivity, targetRoute)
                    true
                },
                onBack = { finish() }
            )
        } else {
            TextilesSettingsScreen(
                initialRoute = route,
                onNavigateOverride = { targetRoute ->
                    start(this@SettingsActivity, targetRoute)
                    true
                },
                onBack = { finish() }
            )
        }
    }

    companion object {
        const val EXTRA_ROUTE = "extra_settings_route"

        fun start(context: Context, route: SettingsRoute = SettingsRoute.Hub) {
            val intent = Intent(context, SettingsActivity::class.java).apply {
                putExtra(EXTRA_ROUTE, route.name)
            }
            context.startActivity(intent)
        }
    }
}
