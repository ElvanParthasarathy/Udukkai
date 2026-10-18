package com.elvan.udukkai.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.mode.LocalAppMode
import com.elvan.udukkai.core.utils.CurrencyUtils
import com.elvan.udukkai.data.model.PattiyalTharavuru
import com.elvan.udukkai.data.repository.PattiyalRepository
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
 * Pixel-perfect Bento-style Home Dashboard (Mugappu).
 * Hosts:
 * 1. GreetingPill: “வணக்கம் ✨” and the active company's full legal business name with company avatar.
 * 2. Bento Stats Grid (Two Summary Tiles):
 *    - Motha Pattiyalgal (Invoice count for active company)
 *    - Motha Kanakku (Total invoiced ₹ billing amount for active company)
 * 3. RecentActivityHeader ("அண்மைய செயற்பாடுகள்" with "அனைத்தும்" link)
 * 4. Recent invoice cards (Kooli / Pattu) or DashboardEmptyState
 */
@Composable
fun DashboardScreen(
    scrollState: LazyListState,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
    onSeeAllReceipts: (() -> Unit)? = null,
    isRefreshing: Boolean = false,
    onInvoiceClick: (PattiyalTharavuru) -> Unit = {},
    onManageProfiles: (() -> Unit)? = null
) {
    val mode = LocalAppMode.current
    val colors = rememberShellColors()

    val recentBills = PattiyalRepository.recentInvoices
    val overallTotal = PattiyalRepository.overallTotal
    val activeCompanyInvoiceCount = PattiyalRepository.activeCompanyInvoices.size

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
                GreetingPill(colors = colors, onManageProfiles = onManageProfiles)
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
                            modifier = Modifier.weight(1f),
                            isFullWidth = false,
                            onClick = onSeeAll
                        )

                        ElvanStatsCard(
                            icon = MaterialSymbols.Rounded.CurrencyRupeeCircle,
                            label = K.totalInvoiced.tr(),
                            value = CurrencyUtils.formatInr(overallTotal),
                            colors = colors,
                            modifier = Modifier.weight(1f),
                            isFullWidth = false
                        )
                    }
                }

                // 3. Recent Activity Header (attached closely to the cards below)
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
