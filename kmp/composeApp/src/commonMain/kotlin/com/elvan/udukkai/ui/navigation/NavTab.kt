package com.elvan.udukkai.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr

enum class NavTab(
    val icon: ImageVector,
    val activeIcon: ImageVector,
    val titleKey: String,
    val headerTitleKey: String = titleKey
) {
    Home(
        icon = MaterialSymbols.CustomNav.Home,
        activeIcon = MaterialSymbols.CustomNav.HomeFill,
        titleKey = K.home,
        headerTitleKey = K.udukkai
    ),
    Create(
        icon = MaterialSymbols.CustomNav.Create,
        activeIcon = MaterialSymbols.CustomNav.CreateFill,
        titleKey = K.make,
        headerTitleKey = K.createBtn
    ),
    Items(
        icon = MaterialSymbols.CustomNav.Items,
        activeIcon = MaterialSymbols.CustomNav.ItemsFill,
        titleKey = K.item,
        headerTitleKey = K.items
    ),
    Customers(
        icon = MaterialSymbols.CustomNav.Customers,
        activeIcon = MaterialSymbols.CustomNav.CustomersFill,
        titleKey = K.customer,
        headerTitleKey = K.customers
    );

    @Composable
    fun getLocalizedLabel(): String = titleKey.tr()

    @Composable
    fun getLocalizedHeader(): String = headerTitleKey.tr()
}
