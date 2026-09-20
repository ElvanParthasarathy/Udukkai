package com.elvan.udukkai.navigation

sealed class Routes(val route: String) {
    data object Home : Routes("home")
    data object Settings : Routes("settings")
}
