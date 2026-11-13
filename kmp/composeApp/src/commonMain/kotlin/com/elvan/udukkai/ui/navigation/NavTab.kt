package com.elvan.udukkai.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr

enum class NavTab(
    val icon: ImageVector,
    val activeIcon: ImageVector,
    val titleKey: String,
    val headerTitleKey: String = titleKey,
    /** Optical size: different glyph silhouettes need different boxes to look equally large. */
    val navIconSize: Dp = 22.dp
) {
    Home(
        icon = MaterialSymbols.CustomNav.Home,
        activeIcon = MaterialSymbols.CustomNav.HomeFill,
        titleKey = K.home,
        headerTitleKey = K.udukkai,
        navIconSize = 22.dp
    ),
    Create(
        icon = MaterialSymbols.CustomNav.Create,
        activeIcon = MaterialSymbols.CustomNav.CreateFill,
        titleKey = K.make,
        headerTitleKey = K.createBtn,
        navIconSize = 20.dp
    ),
    Items(
        icon = MaterialSymbols.Nav.Items,
        activeIcon = MaterialSymbols.Nav.ItemsFill,
        titleKey = K.item,
        headerTitleKey = K.items
    ),
    Customers(
        icon = MaterialSymbols.Nav.Customers,
        activeIcon = MaterialSymbols.Nav.CustomersFill,
        titleKey = K.customer,
        headerTitleKey = K.customers
    ),
    Business(
        icon = GoogleStoreWeight300Outline,
        activeIcon = GoogleStoreWeight300Filled,
        titleKey = K.profile,
        headerTitleKey = K.profile,
        navIconSize = 23.dp
    ),
    Tools(
        icon = MaterialSymbols.CustomNav.Menu,
        activeIcon = MaterialSymbols.CustomNav.MenuFill,
        titleKey = K.menu,
        headerTitleKey = K.menu,
        navIconSize = 22.dp
    );

    companion object {
        /** Items and Customers are pages of the Business slot: the bar keeps Business lit while one of them is open. */
        val barTabs = listOf(Home, Create, Business, Tools)
        fun barSlot(tab: NavTab?): NavTab? = if (tab == Items || tab == Customers) Business else tab
        val NavTab.isBusinessPage: Boolean get() = this == Items || this == Customers
    }

    @Composable
    fun getLocalizedLabel(): String = titleKey.tr()

    @Composable
    fun getLocalizedHeader(): String = headerTitleKey.tr()
}
