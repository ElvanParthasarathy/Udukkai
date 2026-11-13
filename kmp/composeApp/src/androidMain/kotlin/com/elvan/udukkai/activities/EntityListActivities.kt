package com.elvan.udukkai.activities

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.core.platform.AppBackHandler
import com.elvan.udukkai.core.platform.ScreenLayout
import com.elvan.udukkai.core.platform.platformLayoutPolicy
import com.elvan.udukkai.factory.data.FactoryCustomerRepository
import com.elvan.udukkai.factory.data.FactoryItemRepository
import com.elvan.udukkai.factory.ui.screens.FactoryCustomerScreen
import com.elvan.udukkai.factory.ui.screens.FactoryItemScreen
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.textiles.data.TextilesCustomerRepository
import com.elvan.udukkai.textiles.data.TextilesItemRepository
import com.elvan.udukkai.textiles.ui.screens.TextilesCustomerScreen
import com.elvan.udukkai.textiles.ui.screens.TextilesItemScreen
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.ElvanShell
import com.elvan.udukkai.ui.components.shell.ElvanTopBarIconButton
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.settings.SettingsFilterSearchBar

class ItemsActivity : BaseSubpageActivity() {
    @Composable
    override fun ScreenContent() {
        EntityListScreen(
            items = true,
            factory = isFactory,
            onBack = { finish() },
            onAdd = { ItemEditorActivity.start(this, null) },
            onFactoryItem = { ItemViewActivity.start(this, it) },
            onTextilesItem = { ItemViewActivity.start(this, it) }
        )
    }

    companion object {
        fun start(context: Context) = context.startActivity(Intent(context, ItemsActivity::class.java))
    }
}

class CustomersActivity : BaseSubpageActivity() {
    @Composable
    override fun ScreenContent() {
        EntityListScreen(
            items = false,
            factory = isFactory,
            onBack = { finish() },
            onAdd = { CustomerEditorActivity.start(this, null) },
            onFactoryCustomer = { CustomerViewActivity.start(this, it) },
            onTextilesCustomer = { CustomerViewActivity.start(this, it) }
        )
    }

    companion object {
        fun start(context: Context) = context.startActivity(Intent(context, CustomersActivity::class.java))
    }
}

@Composable
private fun EntityListScreen(
    items: Boolean,
    factory: Boolean,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onFactoryItem: (com.elvan.udukkai.data.model.ItemModel) -> Unit = {},
    onTextilesItem: (com.elvan.udukkai.data.model.ItemModel) -> Unit = {},
    onFactoryCustomer: (com.elvan.udukkai.data.model.CustomerModel) -> Unit = {},
    onTextilesCustomer: (com.elvan.udukkai.data.model.CustomerModel) -> Unit = {}
) {
    val colors = rememberShellColors()
    val scrollState = androidx.compose.foundation.lazy.rememberLazyListState()
    var searchOpen by remember { mutableStateOf(false) }

    fun currentQuery(): String = when {
        items && factory -> FactoryItemRepository.searchQuery
        items -> TextilesItemRepository.searchQuery
        factory -> FactoryCustomerRepository.searchQuery
        else -> TextilesCustomerRepository.searchQuery
    }
    fun setQuery(value: String) {
        when {
            items && factory -> FactoryItemRepository.searchQuery = value
            items -> TextilesItemRepository.searchQuery = value
            factory -> FactoryCustomerRepository.searchQuery = value
            else -> TextilesCustomerRepository.searchQuery = value
        }
    }
    fun closeSearch() {
        searchOpen = false
        setQuery("")
    }

    AppBackHandler(enabled = searchOpen) { closeSearch() }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val landscape = platformLayoutPolicy.screenLayout(maxWidth, maxHeight) == ScreenLayout.AndroidLandscape
        ElvanShell(
            scrollState = scrollState,
            title = if (items) K.items.tr() else K.customers.tr(),
            onBack = onBack,
            showNavbar = false,
            hasActions = !searchOpen,
            isSearchActive = searchOpen,
            hasFloatingBar = searchOpen,
            actions = {
                ElvanTopBarIconButton(onClick = { searchOpen = true }) {
                    Icon(MaterialSymbols.Rounded.Search, K.search.tr(), tint = colors.textPrimary, modifier = Modifier.size(22.dp))
                }
                ElvanTopBarIconButton(onClick = onAdd) {
                    Icon(MaterialSymbols.Rounded.Add, K.add.tr(), tint = colors.textPrimary, modifier = Modifier.size(22.dp))
                }
            },
            floatingBar = {
                if (!landscape) SettingsFilterSearchBar(
                    visible = searchOpen,
                    query = currentQuery(),
                    onQuery = ::setQuery,
                    onClose = ::closeSearch,
                    colors = colors
                )
            }
        ) {
            when {
                items && factory -> FactoryItemScreen(scrollState = scrollState, onItemClick = onFactoryItem)
                items -> TextilesItemScreen(scrollState = scrollState, onItemClick = onTextilesItem)
                factory -> FactoryCustomerScreen(scrollState = scrollState, onCustomerClick = onFactoryCustomer, colors = colors)
                else -> TextilesCustomerScreen(scrollState = scrollState, onCustomerClick = onTextilesCustomer, colors = colors)
            }
        }

        if (landscape && searchOpen) {
            Box(
                Modifier.align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                    .padding(top = 8.dp, start = 80.dp, end = 80.dp)
            ) {
                SettingsFilterSearchBar(
                    visible = true,
                    query = currentQuery(),
                    onQuery = ::setQuery,
                    onClose = ::closeSearch,
                    colors = colors,
                    landscape = true
                )
            }
        }
    }
}
