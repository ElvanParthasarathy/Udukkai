package com.elvan.udukkai.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.mode.LocalAppMode
import com.elvan.udukkai.core.utils.CurrencyUtils
import com.elvan.udukkai.data.model.PattiyalTharavuru
import com.elvan.udukkai.data.repository.PattiyalRepository
import com.elvan.udukkai.data.settings.NiruvanaTharavugalRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.LocalElvanTopSpacerHeight
import com.elvan.udukkai.ui.components.shell.MugappuStatsSkeleton
import com.elvan.udukkai.ui.components.shell.UruvakkuCardSkeleton
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.home.components.*
import com.elvan.udukkai.ui.screens.create.components.CoolieInvoiceCard
import com.elvan.udukkai.ui.screens.create.components.SilkInvoiceCard

/**
 * Pixel-perfect port of Flutter's MugappuPage (Home tab).
 * Hosts:
 * 1. GreetingPill (Greeting pill with mode toggle badge)
 * 2. Bento Stats Grid:
 *    - Motha Kanakku (Total ₹ billing amount)
 *    - Niruvanangal (Active businesses)
 *    - Motha Pattiyalgal (Invoice count with per-business breakdown)
 * 3. RecentActivityHeader ("அண்மைய செயற்பாடுகள்" with "அனைத்தும்" link)
 * 4. Recent invoice cards (Kooli / Pattu) or DashboardEmptyState
 */
@Composable
fun DashboardScreen(
    scrollState: LazyListState,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
    isRefreshing: Boolean = false,
    onInvoiceClick: (PattiyalTharavuru) -> Unit = {}
) {
    val mode = LocalAppMode.current
    val colors = rememberShellColors()

    val profiles = NiruvanaTharavugalRepository.getAllProfiles(mode)
    val recentBills = PattiyalRepository.recentInvoices
    val overallTotal = PattiyalRepository.overallTotal
    val companiesSummary = PattiyalRepository.getCompaniesSummary(profiles)
    val invoiceCountSummary = PattiyalRepository.getInvoiceCountSummary(profiles)

    Box(modifier = modifier.fillMaxSize()) {
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
            GreetingPill(colors = colors)
        }

        if (isRefreshing) {
            // Shimmer state when pull-down-to-refresh is active
            item(key = "stats_shimmer") {
                MugappuStatsSkeleton()
            }

            item(key = "recent_shimmer_header") {
                RecentActivityHeader(
                    onSeeAll = onSeeAll,
                    colors = colors,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .offset(y = 8.dp)
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
            // 2. Bento Stats Grid
            item(key = "stats_bento_grid") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.ContentPadding),
                    verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacing)
                ) {
                    // Top row: 2 cards with identical size, aligned in the same horizontal line
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.ItemSpacing)
                    ) {
                        ElvanStatsCard(
                            icon = MaterialSymbols.Rounded.CurrencyRupeeCircle,
                            label = K.totalInvoiced.tr(),
                            value = CurrencyUtils.formatInr(overallTotal),
                            colors = colors,
                            modifier = Modifier.weight(1f),
                            isFullWidth = false
                        )

                        ElvanStatsCard(
                            icon = MaterialSymbols.Rounded.Apartment,
                            label = K.companies.tr(),
                            value = companiesSummary,
                            colors = colors,
                            modifier = Modifier.weight(1f),
                            isFullWidth = false
                        )
                    }

                    // Full-width 3rd card: Invoice count with company breakdown
                    ElvanStatsCard(
                        icon = MaterialSymbols.Rounded.InvoiceFill,
                        label = K.totalInvoices.tr(),
                        value = invoiceCountSummary,
                        colors = colors,
                        modifier = Modifier.fillMaxWidth(),
                        isFullWidth = true,
                        onClick = onSeeAll
                    )
                }
            }

            // 3. Recent Activity Header (closely attached to the card below)
            item(key = "recent_activity_header") {
                RecentActivityHeader(
                    onSeeAll = onSeeAll,
                    colors = colors,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .offset(y = 8.dp)
                )
            }

            // 4. Recent Invoices or Empty State
            if (recentBills.isEmpty()) {
                item(key = "recent_empty_state") {
                    DashboardEmptyState(colors = colors)
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
                        if (mode == AppMode.KOOLI) {
                            CoolieInvoiceCard(
                                index = index + 1,
                                pattiyal = item,
                                colors = colors,
                                onClick = { onInvoiceClick(item) }
                            )
                        } else {
                            SilkInvoiceCard(
                                index = index + 1,
                                pattiyal = item,
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
