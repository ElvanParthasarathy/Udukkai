package com.elvan.udukkai.factory.ui.screens

import com.elvan.udukkai.ui.components.EqualHeightCardRow
import com.elvan.udukkai.ui.components.EqualHeightCard

import com.elvan.udukkai.ui.components.shell.sidewaysTight
import com.elvan.udukkai.ui.components.dragSelect
import com.elvan.udukkai.ui.components.ExpressiveDotsLoader
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.data.model.CustomerModel
import com.elvan.udukkai.ui.screens.customer.FactoryCustomerCard
import com.elvan.udukkai.factory.data.FactoryCustomerRepository
import com.elvan.udukkai.factory.data.FactoryProfileRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.LocalShellColors
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.ElvanCardLeadingIcon
import com.elvan.udukkai.ui.components.ElvanCardSelector
import com.elvan.udukkai.ui.components.ElvanCommonCard
import com.elvan.udukkai.ui.components.shell.LocalElvanTopSpacerHeight
import com.elvan.udukkai.ui.components.shell.CustomerCardSkeleton
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import kotlinx.coroutines.delay

@Composable
fun FactoryCustomerScreen(
    onCustomerClick: (CustomerModel) -> Unit,
    modifier: Modifier = Modifier,
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors(),
    isRefreshing: Boolean = false,
    isSelectionMode: Boolean = false,
    selectedCustomerIds: Set<Long> = emptySet(),
    onToggleSelect: ((Long) -> Unit)? = null,
    onCustomerLongClick: ((Long) -> Unit)? = null
) {
    val customers = FactoryCustomerRepository.filteredMerchants
    val ff = LocalAppFontFamily.current


    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight
        com.elvan.udukkai.ui.components.shell.ElvanLazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize().dragSelect(scrollState, { it in selectedCustomerIds }, { onCustomerLongClick?.invoke(it) }, { onToggleSelect?.invoke(it) }),
            contentPadding = PaddingValues(
                start = Dimens.ContentPadding,
                end = Dimens.ContentPadding,
                bottom = Dimens.ContentPaddingBottom.sidewaysTight()
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacing)
        ) {
            item(key = "customer_top_spacer") {
                Spacer(modifier = Modifier.height((LocalElvanTopSpacerHeight.current - (if (com.elvan.udukkai.ui.components.shell.LocalElvanSideBar.current) 37.dp else 0.dp)).coerceAtLeast(0.dp)))
            }

            if (isRefreshing) {
                items(10) {
                    CustomerCardSkeleton()
                }
            } else if (customers.isEmpty()) {
                item(key = "empty_customers") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 56.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = MaterialSymbols.CustomNav.Customers,
                                contentDescription = null,
                                tint = LocalShellColors.current.textQuaternary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (FactoryCustomerRepository.searchQuery.isNotBlank()) {
                                    K.noMatchingCustomers.tr()
                                } else {
                                    K.noCustomersYet.tr()
                                },
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = LocalShellColors.current.textTertiary
                                )
                            )
                        }
                    }
                }
            } else {
                if (isLandscape) {
                    val pairs = customers.chunked(2)
                    itemsIndexed(
                        items = pairs,
                        key = { _, pair -> "cust_pair_${pair.joinToString("_") { it.id.toString() }}" }
                    ) { rowIndex, pair ->
                        EqualHeightCardRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimens.ContentPadding)
                        ) {
                            pair.forEachIndexed { colIndex, item ->
                                val cardIndex = (rowIndex * 2) + colIndex
                                val isSelected = selectedCustomerIds.contains(item.id)
                                val onCardClick: () -> Unit = {
                                    if (isSelectionMode) onToggleSelect?.invoke(item.id)
                                    else onCustomerClick(item)
                                }
                                val onCardLongClick: () -> Unit = {
                                    onCustomerLongClick?.invoke(item.id)
                                }
                                EqualHeightCard {
                                    FactoryCustomerCard(
                                        index = cardIndex,
                                        merchant = item,
                                        onClick = onCardClick,
                                        colors = colors,
                                        isSelectionMode = isSelectionMode,
                                        isSelected = isSelected,
                                        onLongClick = onCardLongClick
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
                        items = customers,
                        key = { _, item -> "cust_${item.id}" }
                    ) { index, item ->
                        val isSelected = selectedCustomerIds.contains(item.id)
                        val onCardClick: () -> Unit = {
                            if (isSelectionMode) {
                                onToggleSelect?.invoke(item.id)
                            } else {
                                onCustomerClick(item)
                            }
                        }
                        val onCardLongClick: () -> Unit = {
                            onCustomerLongClick?.invoke(item.id)
                        }

                        FactoryCustomerCard(
                            index = index,
                            merchant = item,
                            onClick = onCardClick,
                            colors = colors,
                            isSelectionMode = isSelectionMode,
                            isSelected = isSelected,
                            onLongClick = onCardLongClick
                        )
                    }
                }
            }
        }
    }
}
