package com.elvan.udukkai.textiles.ui.screens

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.core.utils.CurrencyUtils
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.textiles.data.TextilesInvoiceRepository
import com.elvan.udukkai.textiles.data.TextilesProfileRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.LocalElvanTopSpacerHeight
import com.elvan.udukkai.ui.components.shell.HomeStatsSkeleton
import com.elvan.udukkai.ui.components.shell.UruvakkuCardSkeleton
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.home.components.*
import com.elvan.udukkai.ui.screens.create.components.TextilesInvoiceCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pixel-perfect Bento-style Home Dashboard for Udukkai Textiles (சவுளி).
 * Strictly communicates with Textiles repositories with zero AppMode dependency.
 */
@Composable
fun TextilesDashboardScreen(
    scrollState: LazyListState,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
    onSeeAllReceipts: (() -> Unit)? = null,
    isRefreshing: Boolean = false,
    onInvoiceClick: (InvoiceModel) -> Unit = {},
    onManageProfiles: (() -> Unit)? = null,
    onOpenReports: (() -> Unit)? = null
) {
    val colors = rememberShellColors()

    var isSwitchingCompany by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    // Grey shimmer blocks also stand in while the cloud data is still arriving on a fresh sign-in
    val restoringEmpty = com.elvan.udukkai.core.sync.getFirebaseSyncManager().restoreProgress.value != null && TextilesInvoiceRepository.activeCompanyInvoices.isEmpty()
    val showSkeleton = isRefreshing || isSwitchingCompany || restoringEmpty ||
        com.elvan.udukkai.ui.components.shell.LoginShimmer.active

    val recentBills = TextilesInvoiceRepository.recentInvoices
    val overallTotal = TextilesInvoiceRepository.overallTotal
    val activeCompanyInvoiceCount = TextilesInvoiceRepository.activeCompanyInvoices.size

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = Dimens.ContentPaddingBottom
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacing)
        ) {
            // Collapsible Top Header spacer
            item(key = "home_top_spacer") {
                Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
            }

            // 1. Greeting Vanakkam Pill
            item(key = "vanakkam_pill") {
                GreetingPill(
                    colors = colors,
                    profile = TextilesProfileRepository.profile,
                    allProfiles = TextilesProfileRepository.profiles,
                    defaultTitle = "சவுளி",
                    appTitle = if (com.elvan.udukkai.localization.LanguageManager.currentLanguage == com.elvan.udukkai.localization.Language.ENGLISH) "Udukkai Textiles" else "உடுக்கைச் சவுளி",
                    onProfileChanging = { newProfileId ->
                        scope.launch {
                            isSwitchingCompany = true
                            scrollState.scrollToItem(0, 0)
                            delay(120)
                            TextilesProfileRepository.setActiveProfile(newProfileId)
                            delay(550)
                            isSwitchingCompany = false
                        }
                    },
                    onManageProfiles = onManageProfiles
                )
            }

            // An empty list item would still add a second gap, so it exists only while a restore runs
            if (com.elvan.udukkai.core.sync.getFirebaseSyncManager().restoreProgress.value != null) item(key = "restore_banner") {
                com.elvan.udukkai.ui.screens.settings.RestoreBanner()
            }

            if (showSkeleton) {
                // Shimmer state when pull-down-to-refresh is active
                item(key = "stats_shimmer") {
                    HomeStatsSkeleton()
                }

                item(key = "recent_shimmer_header") {
                    RecentActivityHeader(
                        onSeeAll = onSeeAll,
                        colors = colors,
                        modifier = Modifier.offset(y = 6.dp)
                    )
                }

                items(5) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.ContentPadding)
                    ) {
                        UruvakkuCardSkeleton()
                    }
                }
            } else {
                // 2. Bento Stats Grid: Two Summary Tiles
                item(key = "stats_bento_grid") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.ContentPadding),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.ItemSpacing)
                    ) {
                        ElvanStatsCard(
                            icon = MaterialSymbols.Rounded.InvoiceFill,
                            label = K.totalInvoices.tr(),
                            value = "$activeCompanyInvoiceCount",
                            colors = colors,
                            accentColor = colors.invoiceColor,
                            gradientColors = colors.invoiceGradient,
                            modifier = Modifier.weight(1f),
                            isFullWidth = false,
                            onClick = onSeeAll
                        )

                        ElvanStatsCard(
                            icon = MaterialSymbols.Rounded.CurrencyRupeeCircle,
                            accentColor = colors.receiptColor,
                            gradientColors = colors.receiptGradient,
                            label = K.totalInvoiced.tr(),
                            value = CurrencyUtils.formatInr(overallTotal),
                            colors = colors,
                            modifier = Modifier.weight(1f),
                            isFullWidth = false,
                            onClick = onOpenReports
                        )
                    }
                }

                // 3. Recent Activity Header
                item(key = "recent_activity_header") {
                    RecentActivityHeader(
                        onSeeAll = onSeeAll,
                        colors = colors,
                        modifier = Modifier.offset(y = 6.dp)
                    )
                }

                // 4. Recent Invoices or Empty State
                if (recentBills.isEmpty()) {
                    item(key = "recent_empty_state") {
                        DashboardEmptyState(colors = colors)
                    }
                } else {
                    if (isLandscape) {
                        val pairs = recentBills.chunked(2)
                        itemsIndexed(
                            items = pairs,
                            key = { _, pair -> "recent_pair_${pair.first().id}" }
                        ) { rowIndex, pair ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = Dimens.ContentPadding),
                                horizontalArrangement = Arrangement.spacedBy(Dimens.ItemSpacing)
                            ) {
                                pair.forEachIndexed { colIndex, item ->
                                    val cardIndex = (rowIndex * 2) + colIndex + 1
                                    Box(modifier = Modifier.weight(1f)) {
                                        TextilesInvoiceCard(
                                            index = cardIndex,
                                            invoice = item,
                                            colors = colors,
                                            onClick = { onInvoiceClick(item) }
                                        )
                                    }
                                }
                                if (pair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    } else {
                        itemsIndexed(
                            items = recentBills,
                            key = { _, item -> "recent_inv_${item.id}" }
                        ) { index, item ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = Dimens.ContentPadding)
                            ) {
                                TextilesInvoiceCard(
                                    index = index + 1,
                                    invoice = item,
                                    colors = colors,
                                    onClick = { onInvoiceClick(item) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
